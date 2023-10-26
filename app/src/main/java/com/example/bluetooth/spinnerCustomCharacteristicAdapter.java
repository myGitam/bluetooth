package com.example.bluetooth;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import java.util.ArrayList;

public class spinnerCustomCharacteristicAdapter extends BaseAdapter {
    private static final String TAG = "MyApp";
    Context context;

    ArrayList<BluetoothGattCharacteristic>  supportedHaracteristic ;
    LayoutInflater inflter;
    public spinnerCustomCharacteristicAdapter(Context appContex, ArrayList<BluetoothGattCharacteristic> supportedHaracteristic ) {
        this.supportedHaracteristic = new ArrayList<>();

        this.supportedHaracteristic=supportedHaracteristic;
        this.context=appContex;
        inflter = (LayoutInflater.from(this.context));
    }


    @Override
    public int getCount() {

        return supportedHaracteristic.size();
    }

    @Override
    public Object getItem(int position) {
        return supportedHaracteristic.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    public View getView(int position, View view, ViewGroup parent) {

        Log.d(TAG, "Custonspinner: ");
        //view=inflter.inflate(R.layout.spinnerbleadapter,parent,false);
        view  = inflter.inflate(R.layout.spinnerbleadapter,parent,false);
        ImageView icon=view.findViewById(R.id.imageButton);
        icon.setImageResource(R.drawable.downrightarrowpng);
        TextView servicename=view.findViewById(R.id.spintextname);
        servicename.setText(supportedHaracteristic.get(position).getUuid().toString());
        return view;
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        return super.equals(obj);
    }

    @NonNull
    @Override
    public String toString() {
        return super.toString();
    }
}
