package com.example.bluetooth;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;

import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

public class runActivity extends AppCompatActivity {
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    BluetoothManager bluetoothManager;
    OutputStream mmOutStream;
    private static final int REQUEST_ENABLE_BT = 0;
    private static final String TAG = "MyApp";



    ///////////////////////////////////////////////////

    BluetoothAdapter bluetoothAdapter;

    Set<BluetoothDevice> pairedDevices;
    ArrayList<PairedDev> pairedDevicesList = new ArrayList<PairedDev>();
    PairedDev arrBtDev;

    // @SuppressLint("MissingPermission")

    @Override
    public String toString() {
        return "runActivity{" +
                "mmOutStream=" + mmOutStream +
                ", bluetoothManager=" + bluetoothManager +
                ", bluetoothAdapter=" + bluetoothAdapter +
                ", pairedDevices=" + pairedDevices +
                ", pairedDevicesList=" + pairedDevicesList +
                ", arrBtDev=" + arrBtDev +

                ", stateClickListener=" + stateClickListener +
                '}';
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_run);
        //bluetoothManager = getSystemService(BluetoothManager.class);
        //bluetoothAdapter = bluetoothManager.getAdapter();
        //bluetoothAdapter=getBluetoothAdapter(); // получаю адаптер
        Button ServerButton=findViewById(R.id.ServerButton);
        ServerButton.setOnClickListener(this::myOnClick);

            Log.d(TAG, "ARGUMENTS ");

            bluetoothManager=getSystemService(BluetoothManager.class);
            bluetoothAdapter=bluetoothManager.getAdapter();
            getPaired(bluetoothAdapter);


    }





    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode==REQUEST_ENABLE_BT){
            if(grantResults[0]==PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "onRequestPermissionsResult: Есть разрешение");
            }
            else {
                Log.d(TAG, "onRequestPermissionsResult: НетРазрешения");
            }
        }
    }

    public void getPaired(BluetoothAdapter bluetoothAdapter) {
        Log.i(TAG, "getPaired");

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "PERMISSION_GRANTED: "+ PackageManager.PERMISSION_GRANTED);
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.BLUETOOTH},REQUEST_ENABLE_BT);
            // here to request the missing permissions, and then overriding
            pairedDevices = bluetoothAdapter.getBondedDevices();
        }
        else {
           // finish();
           // return;
        }

        if (pairedDevices.size() > 0) {
            Log.i(TAG, "PAIREDDEV_FUN");
            // There are paired devices. Get the name and address of each paired device.
            for (BluetoothDevice device : pairedDevices) {
                pairedDevicesList.add(new PairedDev(device,device.getName())); //записываю в список все спаренные девайсы

                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress(); // MAC address
                Log.i(TAG, deviceName);

            }
              RecyclerView recyclerView = findViewById(R.id.myRecyclerView);
              StateAdapter adapter = new StateAdapter(this, pairedDevicesList, stateClickListener);
              recyclerView.setAdapter(adapter);
        } else {
            Log.d(TAG, "no paired");
            finish();
        }

    }


    // определяем слушателя нажатия элемента в списке
    final StateAdapter.OnStateClickListener stateClickListener = new StateAdapter.OnStateClickListener() {

        @Override
        public void onStateClick(PairedDev pairedDev, int position) {
            Toast.makeText(getApplicationContext(), "Был выбран пункт " + pairedDev.getDevName(),
                    Toast.LENGTH_SHORT).show();
            Log.d(TAG, "Передаю в главную Активити " + pairedDev.devName + " " + pairedDev.getPairBluDev().getAddress());

            setResult(Activity.RESULT_OK, new Intent().putExtra("Device",  pairedDev));
            finish();

        }
    };
    public void myOnClick(View view){
        //****Раскоментить чтоб вызывать сервер
       // AcceptThread serverThread=new AcceptThread();
       // serverThread.start();
        //setResult(Activity.RESULT_OK, new Intent().putExtra("Device", (Bundle) null));
        //Запрос на включение местоположения!!! Нужно нормально доделать!
        ActivityCompat.requestPermissions(this,new String[] { Manifest.permission.ACCESS_COARSE_LOCATION },1);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            BtLe btLe=new BtLe(this);
            btLe.scanLeDevice();
        }


    }



}