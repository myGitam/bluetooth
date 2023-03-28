package com.example.bluetooth;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;

public class DisplayText implements IntefaceDisplayText{
    SpannableStringBuilder builder;
    String inputText;
    SharedPreferences setting;
    String TAG;
    StringBuilder strBuild=new StringBuilder();
    Context context;
    int colorrYellow;
    int colorRed;
    int colorGreen;
    public DisplayText(Context context) {
        this.context = context;
        colorrYellow = context.getResources().getColor(R.color.yellow);
        colorRed=context.getResources().getColor(R.color.red);
        colorGreen=context.getResources().getColor(R.color.green);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        return super.equals(obj);
    }


    @Override
    public SpannableStringBuilder writeInText(String in, String delimeter) {

        Log.d(TAG, "IGET "+ delimeter);
        if(delimeter.equals("Non")) {
            Log.d(TAG, "DELAY: "+delimeter);
            in = in.replace("\r", "");
            in = in.replace("\n", "");
            builder = new SpannableStringBuilder();



            SpannableString str1 = new SpannableString("IN->: "+ in);
            str1.setSpan(new ForegroundColorSpan(colorRed), 6, str1.length(), 0);
            str1.setSpan(new ForegroundColorSpan(colorrYellow), 0, 6, 0);
            builder.append(str1);
            return builder;
        }
        if(delimeter.equals("CR/NL")){
            SpannableStringBuilder builder = new SpannableStringBuilder();
            strBuild.append(in); // Строю строку из входящих данных пока не дойдет до разделителя
            Log.d(TAG, " strBuild: " +strBuild.length());
            int c=strBuild.indexOf("\r\n");
            Log.d(TAG, "indexOf: "+c);
            Log.d(TAG, "writtenIN: "+strBuild);
            if((c>0)&&(strBuild.length()>0)){
                String takenData= strBuild.substring(0, c+2); // строю строку сохраняя \r\n
                strBuild.delete(0, strBuild.length());
                Log.d(TAG, "received: " + takenData+ " Char " + takenData.length());

                SpannableString str1 = new SpannableString("IN->: "+ takenData);
                str1.setSpan(new ForegroundColorSpan(colorRed), 6, str1.length(), 0);
                str1.setSpan(new ForegroundColorSpan(colorrYellow), 0, 6, 0);
                builder.append(str1);

                //  getTextView.append("\r\n");

            }
            if((c==0)){
                SpannableString str2 = new SpannableString("IN->: "+"\r\n");
                str2.setSpan(new ForegroundColorSpan(Color.YELLOW),0,str2.length(),0);
                strBuild.delete(0,strBuild.length());
                builder.append(str2);
                return builder;
            }

            return builder;
        }
        if(delimeter.equals("CR")){
            SpannableStringBuilder builder = new SpannableStringBuilder();
            strBuild.append(in); // Строю строку из входящих данных пока не дойдет до разделителя
            Log.d(TAG, " strBuild: " +strBuild.length());
            int c=strBuild.indexOf("\r");
            Log.d(TAG, "indexOf: "+c);
            Log.d(TAG, "writtenIN: "+strBuild);
            if((c>0)&&(strBuild.length()>0)){
                String takenData= strBuild.substring(0, c+1); // строю строку сохраняя \r\n
                strBuild.delete(0, strBuild.length());
                Log.d(TAG, "received: " + takenData+ " Char " + takenData.length());

                SpannableString str1 = new SpannableString("IN->: "+ takenData);
                str1.setSpan(new ForegroundColorSpan(colorRed), 6, str1.length(), 0);
                str1.setSpan(new ForegroundColorSpan(colorrYellow), 0, 6, 0);
                builder.append(str1);

                //  getTextView.append("\r\n");

            }
            if((c==0)){
                SpannableString str2 = new SpannableString("IN->: "+"\r");
                str2.setSpan(new ForegroundColorSpan(Color.YELLOW),0,str2.length(),0);
                strBuild.delete(0,strBuild.length());
                builder.append(str2);
                return builder;
            }

            return builder;
        }
        if(delimeter.equals("NL")){
            SpannableStringBuilder builder = new SpannableStringBuilder();
            strBuild.append(in); // Строю строку из входящих данных пока не дойдет до разделителя
            Log.d(TAG, " strBuild: " +strBuild.length());
            int c=strBuild.indexOf("\n");
            Log.d(TAG, "indexOf: "+c);
            Log.d(TAG, "writtenIN: "+strBuild);
            if((c>0)&&(strBuild.length()>0)){
                String takenData= strBuild.substring(0, c+1); // строю строку сохраняя \r\n
                strBuild.delete(0, strBuild.length());
                Log.d(TAG, "received: " + takenData+ " Char " + takenData.length());

                SpannableString str1 = new SpannableString("IN->: "+ takenData);
                str1.setSpan(new ForegroundColorSpan(colorRed), 6, str1.length(), 0);
                str1.setSpan(new ForegroundColorSpan(colorrYellow), 0, 6, 0);
                builder.append(str1);

                //  getTextView.append("\r\n");

            }
            if((c==0)){
                SpannableString str2 = new SpannableString("IN->: "+"\n");
                str2.setSpan(new ForegroundColorSpan(Color.YELLOW),0,str2.length(),0);
                strBuild.delete(0,strBuild.length());
                builder.append(str2);
                return builder;
            }

            return builder;
        }
        return builder;
    }
///////////метод для вывода того что написал для отправики и чего длибо для вывода в консоль
    @Override
    public SpannableStringBuilder readWriteOutText(String out) {
        SpannableStringBuilder builder = new SpannableStringBuilder();

        SpannableString str1 = new SpannableString("OUT->: "+ out);
        str1.setSpan(new ForegroundColorSpan(colorGreen), 6, str1.length(), 0);
        str1.setSpan(new ForegroundColorSpan(colorrYellow), 0, 6, 0);
        builder.append(str1);
        return builder;
    }
}
