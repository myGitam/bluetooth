package com.example.bluetooth;

import android.content.SharedPreferences;

public class ButtonMemory {
    private SharedPreferences settings;
    public static final String APP_PREFERENCES = "settings";// имя файла настроек
    public static final String APP_PREFERENCES_buttonId="buttonId";
    public static final String APP_PREFERENCES_argument="buttonargument";
    String buttonId;
    String argument;








    public void saveAgrgument(String id,String argument){
        this.argument=argument;
        this.buttonId=buttonId;

    }
}
