package com.example.bluetooth;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.CheckBoxPreference;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import android.preference.PreferenceActivity;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class SettingsFragment extends PreferenceFragmentCompat {
    private SharedPreferences settings;
    //////////**************************************////////////////////////////////
    public static final String APP_PREFERENCES = "settings";// имя файла настроек
    public static final String APP_PREFERENCES_BTLEnoname="BTLEnoname"; // gjrfpsdfnm BTlE ез имени
    public static final String APP_PREFERENCES_autoscroll = "autoscroll"; //Автопрокрутка консоли
    public static final String APP_PREFERENCES_rn="rn";
    public static final String APP_PREFERENCES_encoding="encoding";
    public static final String APP_PREFERENCES_showsent="showsent";

    ////************************************************************************//////
    private static final String TAG = "MyApp";
    String defaultValue = "UTF-8";
    List<String> charCode = new ArrayList();
    ListPreference listPreference;
    CheckBoxPreference btLebox;
    CheckBoxPreference autoscrollbox;

    CheckBoxPreference showsent;
    @Override
    public void onCreatePreferences(@Nullable Bundle savedInstanceState, @Nullable String rootKey) {
        setPreferencesFromResource(R.xml.settings, rootKey);
        //gjkexf/ yfc
        settings=getActivity().getSharedPreferences(SettingsFragment.APP_PREFERENCES, Context.MODE_MULTI_PROCESS);
        Log.d(TAG, "getSharedPreferences: " +settings.getString(SettingsFragment.APP_PREFERENCES_encoding,"UTF-8"));
        listPreference = findPreference("listPreference");
        btLebox=findPreference("bTleNoName");
        autoscrollbox=findPreference("autoscroll");

        showsent=findPreference("showsent");
        setCharCode();

        listPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(@NonNull Preference preference, Object newValue) {
                /// обновляю какую выбрал кодировку после нажатия
                listPreference.setTitle(newValue.toString());
                listPreference.setValue(newValue.toString());
                Log.d(TAG, "onPreferenceChange: " + preference);

                return false;
            }
        });
    }


    //Вывод всех кодировок и установка дефолтной:
    private void setCharCode() {
        for (String str : Charset.availableCharsets().keySet()) {
            charCode.add(str);
        }

        CharSequence[] cs = charCode.toArray(new CharSequence[charCode.size()]);
        listPreference.setEntries(cs);
        listPreference.setEntryValues(cs);
        listPreference.setValue(defaultValue);
        listPreference.setTitle(listPreference.getEntry());


    }
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    private void readSettings(){
        showsent.setChecked(settings.getBoolean(APP_PREFERENCES_showsent,true));
        btLebox.setChecked(settings.getBoolean(APP_PREFERENCES_BTLEnoname,false));
        autoscrollbox.setChecked(settings.getBoolean(APP_PREFERENCES_autoscroll,true));

        Log.d(TAG, "readSettings: " + settings.getString(APP_PREFERENCES_encoding,"UTF-8"));
        defaultValue = settings.getString(APP_PREFERENCES_encoding,"UTF-8");
        listPreference.setValue(settings.getString(APP_PREFERENCES_encoding,"UTF-8"));
        listPreference.setTitle(listPreference.getEntry());
    }

    private void writeSettings(){
        Log.d(TAG, "onStop: "+btLebox.isChecked());
        SharedPreferences.Editor editor=settings.edit();
        editor.putBoolean(APP_PREFERENCES_showsent,showsent.isChecked());
        editor.putBoolean(APP_PREFERENCES_BTLEnoname, btLebox.isChecked());
        editor.putBoolean(APP_PREFERENCES_autoscroll,autoscrollbox.isChecked());

        Log.d(TAG, "writeSettings: "+ listPreference.getEntry());
        editor.putString(APP_PREFERENCES_encoding, String.valueOf(listPreference.getEntry()));
        editor.apply();
    }

    @Override
    public void onStop() {
        super.onStop();
        writeSettings();
      // writeSettings();

    }

    @Override
    public void onPause() {
        super.onPause();
        writeSettings();
    }

    @Override
    public void onResume() {
        super.onResume();
        readSettings();

    }
}