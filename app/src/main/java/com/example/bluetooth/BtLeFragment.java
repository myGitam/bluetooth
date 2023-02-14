package com.example.bluetooth;

import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class BtLeFragment extends Fragment {

    ArrayList<BtleFindingDev> btleFindingDevList = new ArrayList<BtleFindingDev>();//простой список уникальных устройств для адаптера
    ListView btleListViev;
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override

    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        BtLe btLe= null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            btLe = new BtLe();
        }
        btLe.scanLeDevice();
        btleFindingDevList=btLe.getList();
        btleListViev.findViewById(R.id.btleListViev);

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View result=inflater.inflate(R.layout.activity_bt_le, container, false);
        // используем адаптер данных
        List<String> btleNames = null;
        for (int i=0;i<btleFindingDevList.size();i++){
            btleNames.add(btleFindingDevList.get(i).getname());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_list_item_1, btleNames);
        btleListViev.setAdapter(adapter);
        return result;
    }
}
