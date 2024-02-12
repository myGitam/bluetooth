package com.example.bluetooth;

import static android.bluetooth.BluetoothDevice.BOND_BONDED;
import static android.bluetooth.BluetoothDevice.BOND_NONE;
import static android.bluetooth.BluetoothDevice.TRANSPORT_LE;
import static android.bluetooth.BluetoothGatt.GATT_FAILURE;
import static android.bluetooth.BluetoothGatt.GATT_SUCCESS;
import static android.bluetooth.BluetoothGattCharacteristic.PROPERTY_INDICATE;
import static android.bluetooth.BluetoothGattCharacteristic.PROPERTY_NOTIFY;
import static android.bluetooth.BluetoothGattCharacteristic.PROPERTY_SIGNED_WRITE;
import static android.bluetooth.BluetoothGattCharacteristic.PROPERTY_WRITE;
import static android.bluetooth.BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE;
import static android.bluetooth.BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT;
import static android.bluetooth.BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE;
import static android.bluetooth.BluetoothGattCharacteristic.WRITE_TYPE_SIGNED;
import static android.bluetooth.BluetoothProfile.STATE_CONNECTED;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Locale;
import java.util.UUID;



@RequiresApi(api = Build.VERSION_CODES.M)
public class BtleConnectedClass {
    private static final String TAG = "MyApp";
    BluetoothGatt gatt;
    BluetoothGattService service;
    BluetoothGattCharacteristic read;
    BluetoothGattCharacteristic write;
    BluetoothManager manager;
    protected static final UUID CHARACTERISTIC_UPDATE_NOTIFICATION_DESCRIPTOR_UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
    PairedDev pairedDev;
    BluetoothDevice device;
    Context context;
    ArrayList<BluetoothGattService> supportedServices; // переменная для хранения сервисов которые поддерживаеют только чтение и запись
    Handler handler;
    private static BtleConnectedClass instance; // обьект Singleton

    public static synchronized BtleConnectedClass createInstance(PairedDev pairedDev, Context context, Handler handler) throws InterruptedException {
        instance = null;
        if (instance == null) {
            instance = new BtleConnectedClass(pairedDev, context, handler);
        }
        return instance;
    }

    public static synchronized BtleConnectedClass getInstance() {
        return instance;
    }

    @SuppressLint("MissingPermission")
    public BtleConnectedClass(PairedDev pairedDev, Context context, Handler handler) {
        this.pairedDev = pairedDev;
        this.context = context;
        this.handler = handler;
        supportedServices = new ArrayList<BluetoothGattService>(); // сюда передам уникальные и дальше уже с ними работаю

        gatt = pairedDev.getPairBluDev().connectGatt(context, false, bluetoothGattCallback, TRANSPORT_LE);

    }


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
                        if (((property & PROPERTY_NOTIFY) > 0) && ((property & PROPERTY_WRITE) > 0)) {
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


            updateSpinnerData(supportedServices); // метод для обновления данных в спинере
        }

        ////Тестирую функцию чтения это колбек


        @Override
        public void onCharacteristicRead(@NonNull BluetoothGatt gatt, @NonNull BluetoothGattCharacteristic characteristic, @NonNull byte[] value, int status) {
           super.onCharacteristicRead(gatt, characteristic, value, status);
            Log.d(TAG, "onCharacteristicReadCallback: " + value.toString());
        }

        @SuppressLint("MissingPermission")
        @Deprecated
        @Override
        public void onCharacteristicRead (BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status){

            if (status == BluetoothGatt.GATT_SUCCESS) {


            }
        }
        //метод для новых версий
        @Override
        public void onCharacteristicChanged(@NonNull BluetoothGatt gatt, @NonNull BluetoothGattCharacteristic characteristic, @NonNull byte[] value) {
            super.onCharacteristicChanged(gatt, characteristic, value);
            Log.d(TAG, "onCharacteristicChanged Changed New: ");
            }
        @SuppressLint("MissingPermission")
        @Deprecated
        @Override
        //метод до 13 андроида
        //При изменении характеристики, копирую данные сразу в переменную. и делаю с ними что хочу. Потому что если это не сделать они могут пропасть.
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
            Log.d(TAG, "onCharacteristicChanged Changed Old: " + characteristic.getUuid().toString());

            final byte[] value = new byte[characteristic.getValue().length];
            System.arraycopy(characteristic.getValue(), 0, value, 0, characteristic.getValue().length); //копирую то что пришло, так нжно иначе не работает
            Log.d(TAG, "onCharacteristicChanged length: "+ value.length);
            String s = new String(value, StandardCharsets.UTF_8);
            Log.d(TAG, "onCharacteristicChanged data: "+ s);
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) { //Метод для записи дескриптора
            super.onDescriptorWrite(gatt, descriptor, status);
            final BluetoothGattCharacteristic parentCharacteristic = descriptor.getCharacteristic();

            if (status != GATT_SUCCESS) {
                Log.d(TAG, "onDescriptorWrite: failed");

            }
            if (descriptor.getUuid().equals((CHARACTERISTIC_UPDATE_NOTIFICATION_DESCRIPTOR_UUID))) {
                Log.d(TAG, "onDescriptorWrite: " + descriptor.getUuid().toString());
                if (status == GATT_SUCCESS) {
                    // Check if we were turning notify on or off
                    byte[] value = descriptor.getValue();
                    if (value != null) {
                        if (value[0] != 0) {
                            // Notify set to on, add it to the set of notifying characteristics          notifyingCharacteristics.add(parentCharacteristic.getUuid());
                            Log.d(TAG, "onDescriptorWrite: Notify set to on");

                        }
                    } else {
                        // Notify was turned off, so remove it from the set of notifying characteristics               notifyingCharacteristics.remove(parentCharacteristic.getUuid());
                        Log.d(TAG, "onDescriptorWrite: Notify was turned off");
                    }
                }

            } else {
                Log.d(TAG, "onDescriptorWrite: " + "// This was a normal descriptor write..");
            }

        }
    };

    public ArrayList<BluetoothGattService> getSupportedService() {
        return supportedServices;
    }

    private void updateSpinnerData(ArrayList<BluetoothGattService> supportedServices) {
        handler.post(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void run() {
                Log.d(TAG, "run: " + "RUNCHeakServices");
                if (!supportedServices.isEmpty()) {
                    handler.sendEmptyMessage(1);
                    Log.d(TAG, "updateSpinnerData: ");

                } else {
                    Log.d(TAG, "run: EMPTY supportedServices");
                }
            }
        });

        // spinAdpt.clear();

    }



    ////Тестирую функцию чтения
    @SuppressLint("MissingPermission")
    public void read(BluetoothGattCharacteristic characteristic) {
        Log.d(TAG, "read characteristic: " + characteristic.getUuid().toString());
        byte[] value;
        int properties = characteristic.getProperties();
        final BluetoothGattDescriptor descriptor = characteristic.getDescriptor(CHARACTERISTIC_UPDATE_NOTIFICATION_DESCRIPTOR_UUID);
        int connectedState;
        device = gatt.getDevice();
        manager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        connectedState = manager.getConnectionState(device, BluetoothProfile.GATT);
        Log.d(TAG, "connectedState " + connectedState);
        if (connectedState == STATE_CONNECTED) {
            if ((properties & PROPERTY_NOTIFY) > 0) {                     //определяю тип уведомления характеристики
                Log.d(TAG, "read: " + "PROPERTY_NOTIFY");
                value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE;
                descriptor.setValue(value);
                Log.d(TAG, "value: " + value.toString());
            } else if ((properties & PROPERTY_INDICATE) > 0) { //определяю тип уведомления характеристики
                Log.d(TAG, "read: " + "PROPERTY_INDICATE");
                value = BluetoothGattDescriptor.ENABLE_INDICATION_VALUE;
                descriptor.setValue(value); //записываю в дескриптор тип уведомления

            } else {
                Log.e(TAG, String.format("ERROR: Characteristic %s does not have notify or indicate property", characteristic.getUuid()));

            }
            gatt.writeDescriptor(descriptor);
            Log.d(TAG, "setCharacteristicNotification: " + characteristic.getUuid().toString());
            gatt.setCharacteristicNotification(characteristic, true); // Указываю что должен мониторить эту характеристику для чтения

            Log.d(TAG, "setCharacteristicNotification: "+gatt.setCharacteristicNotification(characteristic, true));
            Log.d(TAG, "descriptor: " + characteristic.getDescriptor(CHARACTERISTIC_UPDATE_NOTIFICATION_DESCRIPTOR_UUID).getUuid().toString());
        }
    }
    @SuppressLint("MissingPermission")
 public void write (BluetoothGattCharacteristic characteristic){
        //Перед записью проверить характеристику, поддерживает ли она нужный тип записи
        // Check if this characteristic actually supports this writeType
        int writeProperty = characteristic.getProperties();
        int mWriteType;
        if ((writeProperty  & PROPERTY_WRITE_NO_RESPONSE) != 0) {
            mWriteType = WRITE_TYPE_NO_RESPONSE;
        } else {
            mWriteType = PROPERTY_WRITE;
        }

        byte[] bytesToWrite="новая строка".getBytes(StandardCharsets.UTF_8);
        characteristic.setValue(bytesToWrite);
        characteristic.setWriteType(mWriteType);
        if (!gatt.writeCharacteristic(characteristic)) {
            Log.e(TAG, String.format("ERROR: writeCharacteristic failed for characteristic:", characteristic.getUuid()));

        } else {
            String s = new String(bytesToWrite, StandardCharsets.UTF_8);
            Log.d(TAG, "write: "+s);
        }
 }
      @SuppressLint("MissingPermission")
      public void readManual(BluetoothGattCharacteristic characteristic){
          characteristic = gatt.getService(service.getUuid()).getCharacteristic(characteristic.getUuid());
          gatt.readCharacteristic(characteristic);
          BluetoothManager manager = (BluetoothManager)context.getSystemService(Context.BLUETOOTH_SERVICE);
          
          Log.d(TAG, "readManual: " + manager.getConnectionState(gatt.getDevice(), BluetoothProfile.GATT));
          Log.d(TAG, "readManual: " +characteristic.getUuid().toString());

      }
      
    public  void delObject(){
        if(instance!=null){instance=null;}
    }
    @NonNull
    @Override
    public String toString() {
        return super.toString();
    }



    public  void unsubscribe(){

    }
    public BluetoothGattService getService() {
        return service;
    }

    public void setService(BluetoothGattService service) {
        this.service = service;
    }

    public BluetoothGattCharacteristic getRead() {
        return read;
    }

    public void setRead(BluetoothGattCharacteristic read) {
        this.read = read;
    }

    public BluetoothGattCharacteristic getWrite() {
        return write;
    }

    public void setWrite(BluetoothGattCharacteristic write) {
        this.write = write;
    }


}
