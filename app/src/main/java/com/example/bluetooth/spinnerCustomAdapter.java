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
import java.util.LinkedHashSet;
import java.util.Set;

public class spinnerCustomAdapter extends BaseAdapter {
    private static final String TAG = "MyApp";
    Context context;
    ArrayList<BluetoothGattService>  supportedServices ;

    LayoutInflater inflter;
    public spinnerCustomAdapter(Context appContex, ArrayList<BluetoothGattService> supportedServices ) {
        this.supportedServices = new ArrayList<>();

        this.supportedServices=supportedServices;
        this.context=appContex;
        inflter = (LayoutInflater.from(this.context));
    }

    @Override
    public int getCount() {

        return supportedServices.size();
    }

    @Override
    public Object getItem(int position) {
        return supportedServices.get(position);
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
        servicename.setText(supportedServices.get(position).getUuid().toString());
        return view;
    }

}
