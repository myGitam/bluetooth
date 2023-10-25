package com.example.bluetooth;

import static android.bluetooth.BluetoothDevice.BOND_BONDED;
import static android.bluetooth.BluetoothDevice.BOND_NONE;
import static android.bluetooth.BluetoothDevice.TRANSPORT_LE;
import static android.bluetooth.BluetoothGatt.GATT_FAILURE;
import static android.bluetooth.BluetoothGatt.GATT_SUCCESS;

import android.annotation.SuppressLint;
import android.widget.AdapterView;
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
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.DialogFragment;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

///Класс для отображения диалогового окна для выбора сервисов для записи и чтения
public class BleSelectServiceDialog extends DialogFragment implements View.OnClickListener {
    ArrayList<BluetoothGattService> supportedServices; // переменная для хранения сервисов которые поддерживаеют только чтение и запись
    ArrayList<BluetoothGattCharacteristic> listReadChar; // переменная для хранения характеристики конуретного сервиса выбранного
    ArrayList<BluetoothGattCharacteristic> listWriteChar; // переменная для хранения характеристики конуретного сервиса выбранного
    private static final String TAG = "MyApp";
    Context context;
    Spinner spinnerSelectService; // Для сервисов сспинер
    Spinner spinnerSelectRead; // спинер для характеристик на чтение
    Spinner spinnerSelectWrite; //синер для характеристик на запись
    ArrayAdapter spinAdpt;
    PairedDev pairedDev;

    spinnerCustomServiceAdapter spinnerCustomServiceAdapterService; //Адаптер для сервисов
    spinnerCustomCharacteristicAdapter spinnerCustomCharacteristicAdapterRead; // адаптер для спинера для характеристик чтение запись один и тот же
    spinnerCustomCharacteristicAdapter spinnerCustomServiceAdapterWrite; // адаптер для спинера для характеристик чтение запись один и тот же
    Set<BluetoothGattService> set;

    //конструктор
    BleSelectServiceDialog(Context context, PairedDev pairedDev) {
        this.context = context;
        this.pairedDev = pairedDev;

        Log.d(TAG, "BleSelectServiceDialog: ");
    }
    /// Данные из диалога тут

///////метод для отправки данных из диалога в фрагмент из которого вызван диалог
    private void sendDataToFragment(String key, String data) {
        Bundle result = new Bundle();
        result.putString(key, data);
        getParentFragmentManager().setFragmentResult("requestKey", result);
        //  dismiss();
    }

    @SuppressLint("MissingPermission")
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        getDialog().setTitle("Title!");
        View v = inflater.inflate(R.layout.blucchangecharacteristiclayout, null);
        v.findViewById(R.id.buttonOK).setOnClickListener(this::onClick);
        v.findViewById(R.id.buttonCanc).setOnClickListener(this::onClick);
        v.findViewById(R.id.spinnerSelectservice);
        v.findViewById(R.id.spinnerSelectReadChar);
        v.findViewById(R.id.spinnerSelectWriteChar);

        return v;
    }


    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.buttonOK:
                Log.d(TAG, "property READ/WRITE ALL:- " + " " + supportedServices.get(0).getUuid());
                sendDataToFragment("DATA Sending1");
                sendDataToFragment("DATA Sending2");
                sendDataToFragment("DATA Sending3");
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
        supportedServices = new ArrayList<BluetoothGattService>(); // сюда передам уникальные и дальше уже с ними работаю
        listReadChar =new ArrayList<BluetoothGattCharacteristic>(); // сюда передам доступные характеристики
        listWriteChar =new ArrayList<BluetoothGattCharacteristic>(); // сюда передам доступные характеристики
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            @SuppressLint("MissingPermission") BluetoothGatt gatt = pairedDev.getPairBluDev().connectGatt(getContext(), false, bluetoothGattCallback, TRANSPORT_LE);
        }

        spinnerSelectService = (Spinner) getView().findViewById(R.id.spinnerSelectservice);
        spinnerSelectService.setClickable(false);
        spinnerCustomServiceAdapterService = new spinnerCustomServiceAdapter(this.getContext(),supportedServices); // указываю список спинеру для сервисов
        spinnerSelectRead=(Spinner) getView().findViewById(R.id.spinnerSelectReadChar);
        spinnerSelectRead.setClickable(false); // настройка спенера чтоб нажмалось
        spinnerCustomCharacteristicAdapterRead = new spinnerCustomCharacteristicAdapter(this.getContext(),listReadChar); // Заполняю адаптер спинера для характеристик на чтение
        spinnerSelectWrite=(Spinner) getView().findViewById(R.id.spinnerSelectWriteChar);
        spinnerSelectWrite.setClickable(false);
        spinnerCustomServiceAdapterWrite=new spinnerCustomCharacteristicAdapter(this.getContext(),listWriteChar); // Заполняю адаптер спинера для характеристик на запись
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: ");
        spinnerCustomServiceAdapterService.notifyDataSetChanged();
      //  spinAdpt = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, supportedServices);
        spinnerSelectService.setAdapter(spinnerCustomServiceAdapterService);
        spinnerSelectService.setSelection(0);
        spinnerSelectService.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                spinnerSelectService.setSelection(position);
                cheakCharacteristicforService(supportedServices.get(position));
                Log.d(TAG, "onItemSelected: ");
                spinnerSelectRead.setAdapter(spinnerCustomCharacteristicAdapterRead);
                spinnerSelectWrite.setAdapter(spinnerCustomServiceAdapterWrite);

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                spinnerSelectService.setSelection(0);
            }
        });
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
    private final BluetoothGattCallback bluetoothGattCallback = new BluetoothGattCallback() {

        @SuppressLint("MissingPermission")
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            if (status == GATT_SUCCESS) {
                if (newState == BluetoothGatt.STATE_CONNECTED) {
                    // Connected to the device, start discovering services
                    //Получаю статус подключения состояние сопряжения
                    int bondstate = pairedDev.getPairBluDev().getBondState();
                    if (bondstate == BOND_NONE || bondstate == BOND_BONDED) {
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
            } else {
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

                Log.d(TAG, "services: " + services.size());
                int index = 0; ///просто задаю индексацию для себя чтоб понимать какой сервис в лог выводит
                //перебираю все сервисы цыклом и сразу в цикле проверяю характеристики на наличие возможности чтения/записи
                for (BluetoothGattService s : services) {
                    index++;
                    Log.d(TAG, "serviceUUID: " + index + ": " + s.getUuid());

                    //получаю список характеристик в сервисе
                    List<BluetoothGattCharacteristic> characteristics = s.getCharacteristics();

                    // в этом цикле проверяю есть ли у сервиса нужные мне хзарактеристики если есть то я этот сервис запишу в отдельный список только сервисов которые поддерживают то что мне нужно
                    for (BluetoothGattCharacteristic characteristic : characteristics) {
                        // Получаю UUID характеристики конкретной
                        UUID value = characteristic.getUuid();
                        Log.d(TAG, "UUID  value characteristic: " + value);

                        int property = characteristic.getProperties(); // переменная чтоб понять характеристика для записи или для чтения
                        if (((property & BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) && ((property & BluetoothGattCharacteristic.PROPERTY_WRITE) > 0)) {
                            // characteristicsReadWrite.add(characteristic);
                            Log.d(TAG, "property READ/WRITE add to supportedServices: ");
                            supportedServices.add(s);
                            break;
                        }

                    }

                }
                //вывожу списко устройств чтение запись
                for (BluetoothGattService ser : supportedServices) {
                    Log.d(TAG, "property READ/WRITE ALL:- " + " " + ser.getUuid());

                }

            }

            updateSpinnerData(supportedServices);


        }

    };

    ///метод для обновления спинера, запусккаю в нем поток и в случае изменгения шлю уведомление в слушатель хандлера
    public void updateSpinnerData(ArrayList<BluetoothGattService> supportedServices) {
        udpadeSpinHandler.post(new Runnable() {
            private static final String TAG = "spinerHandler start";

            @Override
            public void run() {
                if (!supportedServices.isEmpty()) {
                    udpadeSpinHandler.sendEmptyMessage(1);
                    Log.d(TAG, "updateSpinnerData: ");
                    Log.d(TAG, "updateSpinnerData: " + getLifecycle().toString());
                } else {
                    Log.d(TAG, "run: EMPTY supportedServices");
                }
            }
        });

        // spinAdpt.clear();

    }

    Handler udpadeSpinHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            if (msg.what == 1) {
                Log.d(TAG, "handleMessage: 1");
                spinnerCustomServiceAdapterService.notifyDataSetChanged();

                //spinAdpt.notifyDataSetChanged();
                spinnerSelectService.setAdapter(spinnerCustomServiceAdapterService);
                spinnerSelectService.setSelection(0, true);

            }
        }
    };

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    private void cheakCharacteristicforService(BluetoothGattService service) {
        // проверха характеристики для записи или для чтения
        List<BluetoothGattCharacteristic> characteristics = service.getCharacteristics();
        listReadChar.clear();
        listWriteChar.clear();
        for (BluetoothGattCharacteristic characteristic : characteristics) {
            int property = characteristic.getProperties(); // переменная чтоб понять характеристика для записи или для чтения
            if ((property & BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
                Log.d(TAG, "property READ " + "" + property);
                listReadChar.add(characteristic);
            }
            if ((property & (BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE | BluetoothGattCharacteristic.PROPERTY_WRITE)) > 0) {
                Log.d(TAG, "property: " + "WRITE" + property+characteristic.getUuid());
                listWriteChar.add(characteristic);
            }
        }
    }

}
