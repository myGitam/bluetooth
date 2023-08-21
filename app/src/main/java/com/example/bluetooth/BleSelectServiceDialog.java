package com.example.bluetooth;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import org.json.JSONException;

///Класс для отображения диалогового окна для выбора сервисов для записи и чтения
public class BleSelectServiceDialog extends DialogFragment implements View.OnClickListener {
    private static final String TAG = "MyApp";
    Context context;
    public  View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                              Bundle savedInstanceState) {
        getDialog().setTitle("Title!");
        View v = inflater.inflate(R.layout.blucchangecharacteristiclayout, null);
        v.findViewById(R.id.spinnerSelectservice);
        v.findViewById(R.id.buttonOK);
        v.findViewById(R.id.buttonCanc);
        return v;
    }
    //конструктор
    BleSelectServiceDialog (Context context){this.context=context;}

    @Override
    public void onClick(View v) {

    }
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        Log.d(TAG, "onDismiss: ");
    }
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
        Log.d(TAG, "onCancel: ");
    }
}
