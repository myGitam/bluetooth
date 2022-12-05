package com.example.bluetooth;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;

public class readThread extends Thread {
    InputStream mmInStream;
    String symbolDelimeter="\r\n";
    Looper looper=Looper.getMainLooper();
    StringBuilder strBuild=new StringBuilder();
    Handler h;
    public readThread(InputStream inStream, Handler h) {
        this.mmInStream=inStream;
        this.h=h;
    }

    private static final String TAG = "MyApp";
    @Override
    public void run() {

        Message msg;
        super.run();
        Log.d(TAG, "Поток вечно ждущий приходящие данные");
        byte[] buffer = new byte[1024];  // buffer store for the stream
        int bytes; // bytes returned from read()
        // Keep listening to the InputStream until an exception occurs
        while (true) {
            try {
                while (mmInStream.available()>0) {
                    Log.d(TAG, "available: "+mmInStream.available());

                    bytes = mmInStream.read(buffer);
                    Log.d(TAG, "bytes: "+ bytes);
                                                                 // формируем строку
                    String readMessage = new String(buffer, 0, bytes,"UTF-8");

                    Log.d(TAG, "readMessage: "+readMessage);
                    strBuild.append(readMessage);
                    Log.d(TAG, " strBuild: " +strBuild.length());
                    int c=strBuild.indexOf(symbolDelimeter);
                    Log.d(TAG, "indexOf: "+c);
                    if((c>0)&&(strBuild.length()>0)){
                        String takenData= strBuild.substring(0, c);
                        strBuild.delete(0, strBuild.length());
                        Log.d(TAG, "received: " + takenData+ " Char " + takenData.length());
                        msg = h.obtainMessage(5,takenData);
                        h.sendMessage(msg);
                    }
                    if((c==0)){

                        msg = h.obtainMessage(5,strBuild.substring(0, c));
                        strBuild.delete(0, strBuild.length());
                        h.sendMessage(msg);
                    }
                    // Read from the InputStream
                    // Send the obtained bytes to the UI activity
                    //   mHandler.obtainMessage(MESSAGE_READ, bytes, -1, buffer).sendToTarget();

                }
            } catch (IOException e) {
                break;
            }
        }
    }
}
