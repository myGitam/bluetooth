package com.example.bluetooth;

import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.RequiresApi;

import java.util.ArrayList;

public class spinnerCustomAdapter extends BaseAdapter {
    private static final String TAG = "MyApp";
    Context context;
    ArrayList<BluetoothGattService> supportedServices;

    LayoutInflater inflter;
    public spinnerCustomAdapter(Context appContex, ArrayList<BluetoothGattService> supportedServices ) {

        this.supportedServices=supportedServices;
        this.context=appContex;
    }

    @Override
    public int getCount() {

        return 0;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    public View getView(int position, View view, ViewGroup parent) {
        Log.d(TAG, "Custonspinner: ");
        view=inflter.inflate(R.layout.spinnerbleadapter,null);
        ImageView icon=view.findViewById(R.id.imageButton);
        TextView servicename=view.findViewById(R.id.spintextname);
        servicename.setText(supportedServices.get(position).getUuid().toString());
        return view;
    }
}
