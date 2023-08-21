package com.example.bluetooth;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONException;
import org.json.JSONObject;
///Клас отображающий всплывающее окно при длительном нажатии на кнопу памяти
////И тут идет сохранение того что ввел, и есть функции получения имени кнопки и данных которые сохранены. Сохраняю в формате JSON
public class ButtonMemory extends DialogFragment implements View.OnClickListener {


    public static final String APP_PREFERENCES = "settings";// имя файла настроек
    public static final String APP_PREFERENCES_buttonId="buttonId";
    public static final String APP_PREFERENCES_argument="buttonargument";
    public static final String APP_PREFERENCES_buttonname="buttonname";
    Context context;
    String buttonId;
    String argument;
    Handler h;
    //MainDB db;
    TextInputEditText inittext;
    TextInputEditText butname;
    private SharedPreferences settings;

    private static final String TAG = "MyApp";
    //конструктор
     ButtonMemory(Context context){this.context=context;}
    //конструктор
     ButtonMemory(Context context, String id, Handler h){
        this.context=context;
        this.buttonId=id;
        this.h=h;
         TextInputEditText inittext;
       //db=db.Companion.getDB(context);

    }

    public  View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                              Bundle savedInstanceState) {
        getDialog().setTitle("Title!");
        View v = inflater.inflate(R.layout.memlayout, null);
        v.findViewById(R.id.buttonok).setOnClickListener(this);
        v.findViewById(R.id.btncancel).setOnClickListener(this);
        v.findViewById(R.id.textInputmem);
        v.findViewById(R.id.textInpunamebut);
        inittext=v.findViewById(R.id.textInputmem);
        butname= v.findViewById(R.id.textInpunamebut);
        try {
            butname.setText(getName(buttonId));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            inittext.setText(getMemArgument(buttonId));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return v;
    }

    @Override
    public void onClick(View view) {

         switch (view.getId()){
             case R.id.buttonok:
                 settings=getActivity().getSharedPreferences(SettingsFragment.APP_PREFERENCES, Context.MODE_MULTI_PROCESS);
               //  inittext= getDialog().findViewById(R.id.textInputmem);
              //   butname= getDialog().findViewById(R.id.textInpunamebut);
                 String name=butname.getText().toString();
                 String text=inittext.getText().toString();
                 Log.d(TAG, "onClick: "+inittext.getText()+"  "+butname.getText()+ " " + buttonId);

//                 Item item=new Item(null,name,text);
//                 new Thread(new Runnable() {
//                     @Override
//                     public void run() {
//                         db.getDAO().insertItem(item);
//                     }
//                 }).start();
                //открываю настройки файл и записываю туда то что ввёл
                 SharedPreferences.Editor editor=settings.edit();
                 JSONObject jsonObj = new JSONObject();
                 try {
                     jsonObj.put("name",name);
                 } catch (JSONException e) {
                     e.printStackTrace();
                 }
                 try {
                     jsonObj.put("text",text);
                 } catch (JSONException e) {
                     e.printStackTrace();
                 }
                 editor.putString(buttonId, jsonObj.toString());
                 editor.apply();
                 Message msg = h.obtainMessage(2,"");
                 h.sendMessage(msg);
                 dismiss();

                 break;
             case R.id.btncancel:
                 dismiss();
                 break;
         }
        
    }
    //// метод полоучения имени кнопки из файла настроек
    public String getName(String id) throws JSONException {
        settings=context.getSharedPreferences(SettingsFragment.APP_PREFERENCES, Context.MODE_MULTI_PROCESS);
        JSONObject jsonObj = new JSONObject(settings.getString(id,"mem1"));
        String name = jsonObj.getString("name");
        Log.d(TAG, "getName JSON: "+name); // получения имени кнопки
        return name;
    }
 //// метод полоучения параметров из файла настроек
    public String getMemArgument(String id) throws JSONException {
        settings=context.getSharedPreferences(SettingsFragment.APP_PREFERENCES, Context.MODE_MULTI_PROCESS); // доступ к файлу настроеек
        JSONObject jsonObj = new JSONObject(settings.getString(id,"arg")); //получения в фармате GSON настроек конкретной кнопки
        String text = jsonObj.getString("text"); // получение текста который хранится для передачи
        Log.d(TAG, "getName JSON: "+text);
        return text;
    }
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        Log.d(TAG, "onDismiss: ");
    }

    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
        Log.d(TAG, "onCancel: ");
    }

    @Override
    public String toString() {
        return "ButtonMemory{" +
                "settings="  +
                ", context=" + context +
                ", buttonId='" + buttonId + '\'' +
                ", argument='" + argument + '\'' +
                '}';
    }
}
