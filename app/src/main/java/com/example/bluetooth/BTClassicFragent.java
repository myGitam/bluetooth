package com.example.bluetooth;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;

import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

public class BTClassicFragent extends Fragment {
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    BluetoothManager bluetoothManager;
    OutputStream mmOutStream;
    private static final int REQUEST_ENABLE_BT = 0;
    private static final String TAG = "MyApp";
    RecyclerView recyclerView;


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
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bluetoothManager=getActivity().getSystemService(BluetoothManager.class);
        bluetoothAdapter=bluetoothManager.getAdapter();
        getPaired(bluetoothAdapter);

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View result=inflater.inflate(R.layout.activity_btclassic_fragment, container, false);
        recyclerView = result.findViewById(R.id.myRecyclerView);
        StateAdapter adapter = new StateAdapter(getContext(), pairedDevicesList, stateClickListener);
        recyclerView = result.findViewById(R.id.myRecyclerView);
        recyclerView.setAdapter(adapter);
        return result;
    }

   private ActivityResultLauncher<String> requestPermissionLauncher=registerForActivityResult(new ActivityResultContracts.RequestPermission(), new ActivityResultCallback<Boolean>() {
       @Override
       public void onActivityResult(Boolean result) {
           if (result) {
               Log.d(TAG, "Blue-permission Granted");
               requestPermissionLauncher.launch(Manifest.permission.BLUETOOTH_CONNECT);
               requestPermissionLauncher.launch(Manifest.permission.BLUETOOTH_SCAN);

           } else {
               Log.d(TAG, "Blue-permission not granted");
           }
       }
   });

    public void getPaired(BluetoothAdapter bluetoothAdapter) {
        Log.i(TAG, "getPaired");
        //Если андроид выше 12го
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                Boolean s = ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED;
                Log.d(TAG, "PERMISSION_GRANTED: " + s);
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // ActivityCompat.requestPermissions(getActivity(),new String[]{Manifest.permission.BLUETOOTH},REQUEST_ENABLE_BT);
                requestPermissionLauncher.launch(Manifest.permission.BLUETOOTH_SCAN);
                requestPermissionLauncher.launch(Manifest.permission.BLUETOOTH_CONNECT);
                // here to request the missing permissions, and then overriding

            } else {
                pairedDevices = bluetoothAdapter.getBondedDevices();
                // finish();
                // return;
            }
        }
        pairedDevices = bluetoothAdapter.getBondedDevices();

        if(pairedDevices!=null) {
            if (pairedDevices.size() > 0) {
                Log.i(TAG, "PAIREDDEV_FUN");
                // There are paired devices. Get the name and address of each paired device.
                for (BluetoothDevice device : pairedDevices) {
                    pairedDevicesList.add(new PairedDev(device, device.getName())); //записываю в список все спаренные девайсы
                    String deviceName = device.getName();
                    String deviceHardwareAddress = device.getAddress(); // MAC address
                    Log.i(TAG, deviceName);

                }
            }
        }else {
            Log.d(TAG, "No paired");
            getActivity().finish();
        }

    }


    // определяем слушателя нажатия элемента в списке
    final StateAdapter.OnStateClickListener stateClickListener = new StateAdapter.OnStateClickListener() {

        @Override
        public void onStateClick(PairedDev pairedDev, int position) {
            Toast.makeText(getActivity().getApplicationContext(), "Был выбран пункт " + pairedDev.getDevName(),
                    Toast.LENGTH_SHORT).show();
            Log.d(TAG, "Передаю в главную Активити " + pairedDev.devName + " " + pairedDev.getPairBluDev().getAddress());

            getActivity().setResult(Activity.RESULT_OK, new Intent().putExtra("DeviceClassic",  pairedDev));
            getActivity().finish();

        }

        @Override
        public void onLongClick(PairedDev pairedDev, int position) {
            Log.d(TAG, "onLongClick: ");
        }


    };
    public void myOnClick(View view){
        //****Раскоментить чтоб вызывать сервер
        // AcceptThread serverThread=new AcceptThread();
        // serverThread.start();
        //setResult(Activity.RESULT_OK, new Intent().putExtra("Device", (Bundle) null));
        switch (view.getId()) {
            case R.id.ButRecView:
                Intent intent = new Intent(view.getContext(), MyFragments.class);
                startActivity(intent);

                break;
            case R.id.scanBtLe:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                //    BtLe btLe=new BtLe(getContext());
                 //   btLe.scanLeDevice();
                }
                break;
        }




    }
}