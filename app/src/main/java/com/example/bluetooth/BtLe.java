package com.example.bluetooth;

import android.Manifest;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class BtLe {
    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        return super.equals(obj);
    }
    @SuppressLint("NewApi")
    ScanSettings scanSettings = new ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_POWER)
            .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
            .setMatchMode(ScanSettings.MATCH_MODE_AGGRESSIVE)
            .setNumOfMatches(ScanSettings.MATCH_NUM_ONE_ADVERTISEMENT)
            .setReportDelay(0L)
            .build();
    BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    Boolean nonameLe=false;
    private static final String TAG = "MyApp";
    private BluetoothLeScanner bluetoothLeScanner;
    private boolean scanning;
    boolean GpsStatus = false;
    LocationManager locationManager;
    //private Handler handler = new Handler();
    Context context;
    Message msg;
    private static final long SCAN_PERIOD = 10000;
    private RecyclerView BtLerecyclerView;
    //Class and list for BTLE

    ArrayList<PairedDev> btleFindingDevList = new ArrayList<PairedDev>();//простой список уникальных устройств для адаптера
    Set<BluetoothDevice> hashSetBlubtleFindingDevList = new HashSet<BluetoothDevice>(); // список промежуточный для всех найденных (могут повторяться) для этого 2 списка сделано чтоб писать только уникальные
    Handler handler;
    //**Class and list for BTLE
    @RequiresApi(api = Build.VERSION_CODES.M)


    public BtLe(Context context, Handler h) {
        this.handler=h;
        this.context = context;
        bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        GpsStatus = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        hashSetBlubtleFindingDevList.clear();
    }


    public void scanLeDevice() {

        if (GpsStatus == true) {
            Log.d(TAG, "GpsStatus ENABLE");
        } else {
            Intent intent1 = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            context.startActivity(intent1);
        }

        if (!scanning) {
            // Stops scanning after a predefined scan period.
            handler.postDelayed(new Runnable() {
                @SuppressLint("MissingPermission")
                @Override
                public void run() {
                    scanning = false;
                    bluetoothLeScanner.stopScan(leScanCallback);
                    Log.d(TAG, "stopScan(");
                    //добавляю в простой список который передам адаптеру
                   // btleFindingDevList.addAll(hashSetBlubtleFindingDevList);
                   // hashSetBlubtleFindingDevList.clear();
                    for(BluetoothDevice d:hashSetBlubtleFindingDevList){
                        Log.d(TAG, "hashSet "+ d.getName());
                        if(d.getName()==null){
                            btleFindingDevList.add(new PairedDev(d, "No name"));
                        }
                        else {
                            btleFindingDevList.add(new PairedDev(d, d.getName()));
                        }
                    }

                    msg=handler.obtainMessage(2,"");
                    handler.sendMessage(msg);
                }
            }, SCAN_PERIOD);

            scanning = true;

            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions

                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.

                Log.d(TAG, "\"Scan permission denied\"");
                //Запрос на включение местоположения!!! Нужно нормально доделать!
                ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_BACKGROUND_LOCATION}, 1);
                return;
            } else {

                bluetoothLeScanner.startScan(null, scanSettings, leScanCallback);
                Log.d(TAG, "StartscanLeDevice: ");
            }

        }
        else {
            scanning = false;
            bluetoothLeScanner.stopScan(leScanCallback);
            Log.d(TAG, "Not permission");

        }

    }

    public ArrayList<PairedDev> getList() {
        return btleFindingDevList;
    }

    @SuppressLint("NewApi")
    private final ScanCallback leScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            // Log.d(TAG, "onScanResult: " + result);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    Log.d(TAG, "onScanResult: "+"Not GRANTED PERMISSION");
                    return;
                } else {
                    PairedDev pairedDev= new PairedDev(result.getDevice(), result.getDevice().getName());

                    Log.d(TAG, "***");

                }
            }
            else{
                //Log.d(TAG, "/////");
              //  Log.d(TAG, "onScanResult: "+result.getDevice().getAddress());
               PairedDev pairedDev= new PairedDev(result.getDevice(), result.getDevice().getName());
                Log.d(TAG, "onScanResult: " +pairedDev.getPairBluDev().getAddress());
               hashSetBlubtleFindingDevList.add(result.getDevice());

            }
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            super.onBatchScanResults(results);
            Log.d(TAG, "onBatchScanResults: "+ results);
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
            Log.d(TAG, "onScanFailed: ");
        }
    };


}