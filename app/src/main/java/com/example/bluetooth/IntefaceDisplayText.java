package com.example.bluetooth;

import android.text.SpannableStringBuilder;

public interface IntefaceDisplayText {
    public SpannableStringBuilder writeInText(String in,String delimeter);
    public SpannableStringBuilder readWriteOutText(String out);
}
