package com.example.bluetooth;

import static android.bluetooth.BluetoothDevice.BOND_BONDED;
import static android.bluetooth.BluetoothDevice.BOND_NONE;
import static android.bluetooth.BluetoothDevice.TRANSPORT_LE;
import static android.bluetooth.BluetoothGatt.GATT_FAILURE;
import static android.bluetooth.BluetoothGatt.GATT_SUCCESS;

import static androidx.viewpager.widget.PagerAdapter.POSITION_NONE;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.DialogFragment;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

///Класс для отображения диалогового окна для выбора сервисов для записи и чтения
public class BleSelectServiceDialog extends DialogFragment implements View.OnClickListener {
    ArrayList<BluetoothGattService> supportedServices; // переменная для хранения сервисов которые поддерживаеют только чтение и запись

    private static final String TAG = "MyApp";
    Context context;
    Spinner spinner;
    ArrayAdapter spinAdpt;
    PairedDev pairedDev;
    spinnerCustomAdapter spinnerCustomAdapter;

    //конструктор
    BleSelectServiceDialog (Context context, PairedDev pairedDev){this.context=context;
        this.pairedDev=pairedDev;
        Log.d(TAG, "BleSelectServiceDialog: ");
    }
    @SuppressLint("MissingPermission")
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public  View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                              Bundle savedInstanceState) {
        getDialog().setTitle("Title!");
        View v = inflater.inflate(R.layout.blucchangecharacteristiclayout, null);
        v.findViewById(R.id.spinnerSelectservice);
        v.findViewById(R.id.buttonOK).setOnClickListener(this::onClick);
        v.findViewById(R.id.buttonCanc).setOnClickListener(this::onClick);
        v.findViewById(R.id.spinnerSelectservice);
        supportedServices=new ArrayList<BluetoothGattService>();

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            @SuppressLint("MissingPermission") BluetoothGatt gatt = pairedDev.getPairBluDev().connectGatt(getContext(), false, bluetoothGattCallback, TRANSPORT_LE);
        }
        return v;
    }



    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.buttonOK:
                Log.d(TAG, "property READ/WRITE ALL:- "+ " "+ supportedServices.get(0).getUuid());

                //соединяет и получает список сервисов
                Log.d(TAG, "ok: ");
                break;
            case R.id.buttonCanc:
                Log.d(TAG, "cancel ");
                this.getDialog().cancel();
                break;
        }

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(TAG, "onViewCreated: ");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: ");
        spinnerCustomAdapter=new spinnerCustomAdapter(this.getContext(),supportedServices);
        spinnerCustomAdapter.notifyDataSetChanged();
        spinner=getView().findViewById(R.id.spinnerSelectservice);
        spinAdpt=new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item,supportedServices);
        spinner.setAdapter(spinnerCustomAdapter);
        spinner.setSelection(0);
    }

    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        Log.d(TAG, "onDismiss: ");
    }
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
        Log.d(TAG, "onCancel: ");
    }




    @SuppressLint("NewApi")
    private final BluetoothGattCallback bluetoothGattCallback=new BluetoothGattCallback() {

        @SuppressLint("MissingPermission")
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            if(status == GATT_SUCCESS) {
                if (newState == BluetoothGatt.STATE_CONNECTED) {
                    // Connected to the device, start discovering services
                    //Получаю статус подключения состояние сопряжения
                    int bondstate = pairedDev.getPairBluDev().getBondState();
                    if(bondstate == BOND_NONE || bondstate == BOND_BONDED) {
                        // Подключились к устройству, вызываем discoverServices с задержкой
                        gatt.discoverServices();
                    }


                } // если пользователь отключил
                else if (newState == BluetoothGatt.STATE_DISCONNECTED) {
                    // Disconnected from the device
                    Log.d(TAG, "onConnectionStateChange: " + " STATE_DISCONNECTED");
                    gatt.close();
                } else {
                    // мы или подключаемся или отключаемся, просто игнорируем эти статусы
                }
            }
            else {
                // Произошла ошибка... разбираемся, что случилось!
                Log.d(TAG, "Error connect: ");
                gatt.close();
                getDialog().cancel();
            }

        }

        @SuppressLint("MissingPermission")
        @Override
        //Метод для обнаружения сервисов
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
            if (status == GATT_FAILURE) {
                Log.d(TAG, "Service discovery failed\" "); /// Сервисы не найдены
                gatt.close();
                return;
            }
            Log.d(TAG, "Discovered: "); //сервисы найдены
            //ниже проверяю все сервисы на наличие характеристик с возможностью чтени NOTIFY и записи WRITE и добавляю их в отдельный списко с которым буду работать уже из всплывающего менюп о длителдьному нажатию
            if (status == BluetoothGatt.GATT_SUCCESS) {
                // Find the service with the given UUID
                //получаю список сервисов
                final List<BluetoothGattService> services = gatt.getServices(); /// получаю список найденныс сервисов

                Log.d(TAG, "services: "+ services.size());
                int index = 0; ///просто задаю индексацию для себя чтоб понимать какой сервис в лог выводит
                //перебираю все сервисы цыклом и сразу в цикле проверяю характеристики на наличие возможности чтения/записи
                for (BluetoothGattService s:services){
                    index++;
                    Log.d(TAG, "serviceUUID: "+index +": "+s.getUuid());

                    //получаю список характеристик в сервисе
                    List<BluetoothGattCharacteristic> characteristics = s.getCharacteristics();

                    // в этом цикле проверяю есть ли у сервиса нужные мне хзарактеристики если есть то я этот сервис запишу в отдельный список только сервисов которые поддерживают то что мне нужно
                    for (BluetoothGattCharacteristic characteristic : characteristics) {
                        // Получаю UUID характеристики конкретной
                        UUID value =characteristic.getUuid();
                        Log.d(TAG, "UUID  value characteristic: "+ value);

                        int property = characteristic.getProperties(); // переменная чтоб понять характеристика для записи или для чтения
                        if(((property & BluetoothGattCharacteristic.PROPERTY_NOTIFY)>0) && ((property & BluetoothGattCharacteristic.PROPERTY_WRITE|BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE)>0)){
                            // characteristicsReadWrite.add(characteristic);
                            Log.d(TAG, "property READ/WRITE add to supportedServices: " );
                            supportedServices.add(s);

                        }
                        // проверха характеристики для записи или для чтения
//                         if ((property & BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
//                             Log.d(TAG, "property READ "+ ""+property );
//                         }
//                         if ((property & (BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE | BluetoothGattCharacteristic.PROPERTY_WRITE)) > 0) {
//                             Log.d(TAG, "property: "+ "WRITE"+ property  );
//                         }


                    }

                }
                //вывожу списко устройств чтение запись
                Log.d(TAG, "property READ/WRITE ALL:- "+ " "+ supportedServices.get(0).getUuid());

            }
            updateSpinnerData(supportedServices);
        }

    };
             ///метод для обновления спинера, запусккаю в нем поток и в случае изменгения шлю уведомление в слушатель хандлера
               public void updateSpinnerData(ArrayList<BluetoothGattService>  supportedServices) {
                   udpadeSpinHandler.post(new Runnable() {
                       private static final String TAG = "spinerHandler start";
                       @Override
                       public void run() {
                           if (!supportedServices.isEmpty()){udpadeSpinHandler.sendEmptyMessage(1);
                           }
                       }
                   });
                   Log.d(TAG, "updateSpinnerData: ");
                   Log.d(TAG, "updateSpinnerData: " + getLifecycle().toString());
               // spinAdpt.clear();

            }

    Handler udpadeSpinHandler=new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            if(msg.what==1){
                Log.d(TAG, "handleMessage: 1");
                spinnerCustomAdapter.notifyDataSetChanged();
                spinAdpt=new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item,supportedServices);
                spinAdpt.notifyDataSetChanged();
                spinner.setAdapter(spinnerCustomAdapter);
                spinner.setSelection(0, true);
            }
        }
    };


}
