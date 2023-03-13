package com.example.bluetooth;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
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
    private static final String TAG = "MyApp";

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override

    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onResume() {
        super.onResume();
        //Получаю настройки:
        setting = getContext().getSharedPreferences("settings", Context.MODE_MULTI_PROCESS);
        Log.d(TAG, "getSharedPreferences: " + setting.getString(SettingsFragment.APP_PREFERENCES_encoding, "UTF-8"));
        settingNoname = setting.getBoolean(SettingsFragment.APP_PREFERENCES_BTLEnoname, false);
        Log.d(TAG, "BLEFRAGMETNT_onResume: ");

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            btLe = new BtLe(getContext(), btHandler);
        }
        btLe.scanLeDevice();



        // используем адаптер данных


    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View result = inflater.inflate(R.layout.activity_bt_le, container, false);

        recyclerView = result.findViewById(R.id.btlerecyclerview);
        //  adapter = new StateAdapter(getContext(), btleFindingDevList, stateClickListener);
        //  recyclerView.setAdapter(adapter);
        //handelr  чтоб понимать когда закончился поиск устройств
        btHandler = new Handler(Looper.getMainLooper()) {
            //handelr
            @Override
            public void handleMessage(@NonNull Message msg) {
                super.handleMessage(msg);

                if (msg.what == 2) {
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
                    if(settingNoname==true) {
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

        };


        return result;
    }

    final StateAdapter.OnStateClickListener stateClickListener = new StateAdapter.OnStateClickListener() {
        @Override
        public void onStateClick(PairedDev pairedDev, int position) {

        }
    };


}
