package com.example.bluetooth;

import android.Manifest;

import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
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
import android.provider.Settings;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class BtLe {

    @SuppressLint("NewApi")
    ScanSettings scanSettings = new ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_POWER)
            .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
            .setMatchMode(ScanSettings.MATCH_MODE_AGGRESSIVE)
            .setNumOfMatches(ScanSettings.MATCH_NUM_ONE_ADVERTISEMENT)
            .setReportDelay(0L)
            .build();
    BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    private static final String TAG = "MyApp";
    private BluetoothLeScanner bluetoothLeScanner;
    private boolean scanning;
    boolean GpsStatus =false;
    LocationManager locationManager ;
    private Handler handler = new Handler();
    Context context;
    private static final long SCAN_PERIOD = 10000;
    private RecyclerView BtLerecyclerView;
    //Class and list for BTLE

    ArrayList<BtleFindingDev> btleFindingDevList = new ArrayList<BtleFindingDev>();//простой список уникальных устройств для адаптера
    HashSet<BtleFindingDev> hashSetBlubtleFindingDevList = new HashSet<BtleFindingDev>(); // список промежуточный для всех найденных (могут повторяться) для этого 2 списка сделано чтоб писать только уникальные

    //**Class and list for BTLE
    @RequiresApi(api = Build.VERSION_CODES.M)

    public BtLe() {

       // this.context = context;
        bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
        locationManager = (LocationManager)context.getSystemService(Context.LOCATION_SERVICE);
        GpsStatus = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

    }
    public BtLe(Context context) {

        this.context = context;
        bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
        locationManager = (LocationManager)context.getSystemService(Context.LOCATION_SERVICE);
        GpsStatus = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

    }


    public void scanLeDevice() {

        if(GpsStatus == true) {
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
                    btleFindingDevList.addAll(hashSetBlubtleFindingDevList);
                    for (BtleFindingDev c:btleFindingDevList) {
                        Log.d(TAG, String.valueOf(c.getBTLEdevice().getName()));

                    }

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
                ActivityCompat.requestPermissions((Activity) context,new String[] {Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_BACKGROUND_LOCATION },1);
             return;
            }
            else {

                bluetoothLeScanner.startScan(null, scanSettings, leScanCallback);
                Log.d(TAG, "StartscanLeDevice: ");
            }

        } else {
            scanning = false;

                bluetoothLeScanner.stopScan(leScanCallback);
                Log.d(TAG, "Not permission");

        }

    }
    public ArrayList<BtleFindingDev> getList(){
        return btleFindingDevList;
    }

    @SuppressLint("NewApi")
    private final ScanCallback leScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
           // Log.d(TAG, "onScanResult: " + result);
            hashSetBlubtleFindingDevList.add(new BtleFindingDev (result.getDevice())); //добавляю уникальный в хешсет

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