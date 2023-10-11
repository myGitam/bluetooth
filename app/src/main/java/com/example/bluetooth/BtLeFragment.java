package com.example.bluetooth;

import static android.bluetooth.BluetoothDevice.BOND_BONDED;
import static android.bluetooth.BluetoothDevice.BOND_NONE;
import static android.bluetooth.BluetoothDevice.TRANSPORT_LE;
import static android.bluetooth.BluetoothGatt.GATT_FAILURE;
import static android.bluetooth.BluetoothGatt.GATT_SUCCESS;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.view.MenuHost;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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
    boolean GpsStatus = false;
    LocationManager locationManager;
    PairedDev device;
    List<BluetoothGattCharacteristic> characteristicsReadWrite;
    MenuHost menuHost; // для показа в шапке кнопки поиска

    private static final String TAG = "MyApp";
    List<BluetoothGattService> supportedServices; // переменная для хранения сервисов которые поддерживаеют только чтение и запись
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
        supportedServices = new ArrayList<>(); ///ДЛя списка сервисов которые поддерживают чтение запись
        Log.d(TAG, "BLEFRAGMETNT_onResume: ");
        locationManager = (LocationManager) getContext().getSystemService(Context.LOCATION_SERVICE); // Для включения GPS
        GpsStatus = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER); // Для включения GPS
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            if (GpsStatus == true) {
                Log.d(TAG, "GpsStatus ENABLE");
            } else {
                Intent intent1 = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                getContext().startActivity(intent1);
            }
            btLe = new BtLe(getContext(), btHandler);

        }

        //Создаю меню для кнопки поиска
        menuHost.addMenuProvider(new MenuProvider() {
            @Override
            public void onPrepareMenu(@NonNull Menu menu) {
                MenuProvider.super.onPrepareMenu(menu);
            }

            @Override
            public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
                if(!menu.hasVisibleItems()) {
                    menuInflater.inflate(R.menu.btlemenu, menu); //  подключаю меню если этот фрагмнет видим
                }
            }

            @Override
            ////поиск устройств
            public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
                Log.d(TAG, "findBtLeSelected: ");
              ///проверка на какую кнопку нажал
                if (menuItem.getItemId()==R.id.findIconBt){
                    //Проверяю включен ли GPS


                    Log.d(TAG, "Finding BtLe device: ");
                    btleFindingDevList.clear(); // очищаю списки для нового сканиролвания
                    btleFindingDevListnoname.clear();// очищаю списки для нового сканиролвания
                    btLe.scanLeDevice();// скнирую BTlE
                    progressBar.setVisibility(ProgressBar.VISIBLE);
                }
                return true;
            }
        }, getViewLifecycleOwner(), Lifecycle.State.RESUMED);
        ////////////////////

        //Получаю настройки:
        setting = getContext().getSharedPreferences("settings", Context.MODE_MULTI_PROCESS);
        Log.d(TAG, "getSharedPreferences: " + setting.getString(SettingsFragment.APP_PREFERENCES_encoding, "UTF-8"));
        settingNoname = setting.getBoolean(SettingsFragment.APP_PREFERENCES_BTLEnoname, false);



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

// методы для нажатий коротких и длинных
    final StateAdapter.OnStateClickListener stateClickListener = new StateAdapter.OnStateClickListener() {
        @Override
        //короткое нажатие
        public void onStateClick(PairedDev pairedDev, int position) {

            Log.d(TAG, "onStateClick: ");

        }
///Длинное нажати
        @RequiresApi(api = Build.VERSION_CODES.M)
        @Override
        public void onLongClick(PairedDev pairedDev, int position) {
            Log.d(TAG, "onLongClick: ");
          device=pairedDev;
          //соединяет и получает список сервисов
//            @SuppressLint("MissingPermission") BluetoothGatt gatt = pairedDev.getPairBluDev().connectGatt(getContext(), false,
//                    bluetoothGattCallback, TRANSPORT_LE);
            //Отображаю меню для выбора сервиса с которым нужно работать это для теста тут наеписано/ нужно переносить в правильное место
            DialogFragment dialogFragment=new BleSelectServiceDialog(getContext(),pairedDev);
            dialogFragment.show(getParentFragmentManager(),"Select Services");


        }
    };

//     @SuppressLint("NewApi")
//     private final BluetoothGattCallback bluetoothGattCallback=new BluetoothGattCallback() {
//         @SuppressLint("MissingPermission")
//         @Override
//         public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
//             super.onConnectionStateChange(gatt, status, newState);
//             if(status == GATT_SUCCESS) {
//                 if (newState == BluetoothGatt.STATE_CONNECTED) {
//                     // Connected to the device, start discovering services
//                     //Получаю статус подключения состояние сопряжения
//                     int bondstate = device.getPairBluDev().getBondState();
//                     if(bondstate == BOND_NONE || bondstate == BOND_BONDED) {
//                         // Подключились к устройству, вызываем discoverServices с задержкой
//                         gatt.discoverServices();
//                         }
//
//
//                 } // если пользователь отключил
//                 else if (newState == BluetoothGatt.STATE_DISCONNECTED) {
//                     // Disconnected from the device
//                     Log.d(TAG, "onConnectionStateChange: " + " STATE_DISCONNECTED");
//                     gatt.close();
//                 } else {
//                     // мы или подключаемся или отключаемся, просто игнорируем эти статусы
//                 }
//             }
//             else {
//                 // Произошла ошибка... разбираемся, что случилось!
//                 Log.d(TAG, "Error connect: ");
//                 gatt.close();
//             }
//
//         }
//
//         @SuppressLint("MissingPermission")
//         @Override
//         //Метод для обнаружения сервисов
//         public void onServicesDiscovered(BluetoothGatt gatt, int status) {
//             super.onServicesDiscovered(gatt, status);
//             if (status == GATT_FAILURE) {
//                 Log.d(TAG, "Service discovery failed\" "); /// Сервисы не найдены
//                 gatt.close();
//                 return;
//             }
//             Log.d(TAG, "Discovered: "); //сервисы найдены
//             //ниже проверяю все сервисы на наличие характеристик с возможностью чтени NOTIFY и записи WRITE и добавляю их в отдельный списко с которым буду работать уже из всплывающего менюп о длителдьному нажатию
//             if (status == BluetoothGatt.GATT_SUCCESS) {
//                 // Find the service with the given UUID
//                 //получаю список сервисов
//                 final List<BluetoothGattService> services = gatt.getServices(); /// получаю список найденныс сервисов
//
//                 Log.d(TAG, "services: "+ services.size());
//                 int index = 0; ///просто задаю индексацию для себя чтоб понимать какой сервис в лог выводит
//                 //перебираю все сервисы цыклом и сразу в цикле проверяю характеристики на наличие возможности чтения/записи
//                 for (BluetoothGattService s:services){
//                     index++;
//                     Log.d(TAG, "serviceUUID: "+index +": "+s.getUuid());
//
//                     //получаю список характеристик в сервисе
//                     List<BluetoothGattCharacteristic> characteristics = s.getCharacteristics();
//
//                     // в этом цикле проверяю есть ли у сервиса нужные мне хзарактеристики если есть то я этот сервис запишу в отдельный список только сервисов которые поддерживают то что мне нужно
//                     for (BluetoothGattCharacteristic characteristic : characteristics) {
//                         // Получаю UUID характеристики конкретной
//                         UUID value =characteristic.getUuid();
//                         Log.d(TAG, "UUID  value characteristic: "+ value);
//
//                         int property = characteristic.getProperties(); // переменная чтоб понять характеристика для записи или для чтения
//                         if(((property & BluetoothGattCharacteristic.PROPERTY_NOTIFY)>0) && ((property & BluetoothGattCharacteristic.PROPERTY_WRITE|BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE)>0)){
//                            // characteristicsReadWrite.add(characteristic);
//                             Log.d(TAG, "property READ/WRITE add to supportedServices: " );
//                           supportedServices.add(s);
//
//                         }
//                         // проверха характеристики для записи или для чтения
////                         if ((property & BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
////                             Log.d(TAG, "property READ "+ ""+property );
////                         }
////                         if ((property & (BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE | BluetoothGattCharacteristic.PROPERTY_WRITE)) > 0) {
////                             Log.d(TAG, "property: "+ "WRITE"+ property  );
////                         }
//
//
//                     }
//
//                 }
//                 //вывожу списко устройств чтение запись
//                Log.d(TAG, "property READ/WRITE ALL:- "+ " "+ supportedServices.get(1).getUuid());
//
//
//             }
//
//         }
//     };




        @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause: ");
        menuHost.invalidateMenu(); //отключить меню если вышел из фрагмента
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
