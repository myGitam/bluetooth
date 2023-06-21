package com.example.bluetooth;

import static android.bluetooth.BluetoothDevice.TRANSPORT_LE;
import static android.bluetooth.BluetoothGatt.GATT_SUCCESS;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.view.MenuHost;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class BtLeFragment extends Fragment {
    BtLe btLe = null;
    ArrayList<PairedDev> btleFindingDevList = new ArrayList<PairedDev>();//простой список уникальных устройств для адаптера показывать с именем
    ArrayList<PairedDev> btleFindingDevListnoname = new ArrayList<PairedDev>();//простой список уникальных устройств для адаптера если в настройки показывать без имени
    RecyclerView recyclerView;
    Handler btHandler;
    List<String> btleNames = new ArrayList<>();
    StateAdapter adapter;
    SharedPreferences setting;
    Boolean settingNoname;
    ProgressBar progressBar;
    MenuHost menuHost; // для показа в шапке кнопки поиска
    private static final String TAG = "MyApp";

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override

    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        menuHost=getActivity();

    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onResume() {
        super.onResume();
        //Создаю меню для кнопки поиска

        menuHost.addMenuProvider(new MenuProvider() {
            @Override
            public void onPrepareMenu(@NonNull Menu menu) {
                MenuProvider.super.onPrepareMenu(menu);
            }

            @Override
            public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
                if(!menu.hasVisibleItems()) {
                    menuInflater.inflate(R.menu.btlemenu, menu);
                }
            }

            @Override
            public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
                Log.d(TAG, "findBtLeSelected: ");
                if (menuItem.getItemId()==R.id.findiconbBt){
                    Log.d(TAG, "Finding BtLe device: ");
                    btleFindingDevList.clear(); // очищаю списки для нового сканиролвания
                    btleFindingDevListnoname.clear();// очищаю списки для нового сканиролвания
                    btLe.scanLeDevice();// скнирую BTlE
                    progressBar.setVisibility(ProgressBar.VISIBLE);
                }
                return true;
            }
        }, getViewLifecycleOwner(), Lifecycle.State.RESUMED);

        //Получаю настройки:
        setting = getContext().getSharedPreferences("settings", Context.MODE_MULTI_PROCESS);
        Log.d(TAG, "getSharedPreferences: " + setting.getString(SettingsFragment.APP_PREFERENCES_encoding, "UTF-8"));
        settingNoname = setting.getBoolean(SettingsFragment.APP_PREFERENCES_BTLEnoname, false);
        Log.d(TAG, "BLEFRAGMETNT_onResume: ");

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {

            btLe = new BtLe(getContext(), btHandler);
        }
     //   btLe.scanLeDevice();



        // используем адаптер данных


    }

    @Nullable
    @Override
    // тут вся логика 
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View result = inflater.inflate(R.layout.activity_bt_le, container, false);
        progressBar=result.findViewById(R.id.progressBar2);

        recyclerView = result.findViewById(R.id.btlerecyclerview);
        //  adapter = new StateAdapter(getContext(), btleFindingDevList, stateClickListener);
        //  recyclerView.setAdapter(adapter);
        //handelr  чтоб понимать когда закончился поиск устройств я его создаю тут, после поиска уже делаю по результатам
        btHandler = new Handler(Looper.getMainLooper()) {
            //handelr
            @Override
            public void handleMessage(@NonNull Message msg) {
                super.handleMessage(msg);

                if (msg.what == 2) {
                    //Проверяю существует ли это окно и только в этом случае выполняю код и заполняю адаптер// это сделано на случай если начал поиск но выключил это окно
                    if (getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.CREATED)) {
                        Log.d(TAG, "View  visible ");
                        progressBar.setVisibility(ProgressBar.INVISIBLE);
                    String name;
                    btleFindingDevListnoname.clear();
                    Log.d(TAG, "BTlE_handleMessage: " + msg.what);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        btleFindingDevList = btLe.getList();
                        Log.d(TAG, "SIZE: " + btleFindingDevList.size());
                        for (int i = 0; i < btleFindingDevList.size(); i++) {
                            name = btleFindingDevList.get(i).getDevName();
                            Log.d(TAG, "SETTING_BTLENAME: " + name);
                            Log.d(TAG, "BTLESetting: " + settingNoname);

                            //   btleNames.add(btleFindingDevList.get(i).getDevName());
                            //записываю в список те которые с именем только
                            if (name != "No name") {
                                Log.d(TAG, "No name: " + btleFindingDevList.get(i).getDevName());
                                btleFindingDevListnoname.add(btleFindingDevList.get(i));
                            }
                        }
                        // Log.d(TAG, "NAME "+btleNames);
                    }
                    //показываю все тоько с именем
                    if (settingNoname == true) {
                        adapter = new StateAdapter(getContext(), btleFindingDevListnoname, stateClickListener);

                    }
                    //если показывать без имени и с именем
                    else {

                        adapter = new StateAdapter(getContext(), btleFindingDevList, stateClickListener);
                    }
                    recyclerView.setAdapter(adapter);
                    adapter.notifyDataSetChanged();

                    Log.d(TAG, "notifyDataSetChanged() ");
                }
                }


            }

        };


        return result;
    }
    //функция поиска устройст и добавление их в список

// методы для нажатий
    final StateAdapter.OnStateClickListener stateClickListener = new StateAdapter.OnStateClickListener() {
        @Override
        public void onStateClick(PairedDev pairedDev, int position) {

            Log.d(TAG, "onStateClick: ");

        }

        @Override
        public void onLongClick(PairedDev pairedDev, int position) {
            Log.d(TAG, "onLongClick: ");
            @SuppressLint("MissingPermission") BluetoothGatt gatt = pairedDev.getPairBluDev().connectGatt(this, false,
                    bluetoothGattCallback, TRANSPORT_LE);
        }
    };





        @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause: ");
        menuHost.invalidateMenu();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            btLe.stopScan();
        }


    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: ");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            btLe.stopScan();
        }
    }
//создание меню - для кнопки поиска

}