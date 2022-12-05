package com.example.bluetooth;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;
//Клас сервер. Тут нужно вызывать read если выбьрано быть сервером ПОКА НЕ ДЕЛАЮ
public class AcceptThread extends Thread {
    private static final String TAG = "MyApp";
    BluetoothAdapter mBluetoothAdapter;
    private final BluetoothServerSocket mmServerSocket;
    InputStream instr;
    String MY_UUID = "00001101-0000-1000-8000-00805F9B34FB";

    @SuppressLint("MissingPermission")
    public AcceptThread() {
        Log.d(TAG, ":Accept THREAD");
        mBluetoothAdapter= BluetoothAdapter.getDefaultAdapter();
        // Use a temporary object that is later assigned to mmServerSocket,
        // because mmServerSocket is final
        BluetoothServerSocket tmp = null;
        try {
            // MY_UUID is the app's UUID string, also used by the client code
            tmp = mBluetoothAdapter.listenUsingRfcommWithServiceRecord("BT_Name", UUID.fromString(MY_UUID));
            Log.d(TAG, "AcceptThread: Создаю сервер");

        } catch (IOException e) { }
        mmServerSocket = tmp;

    }

    public void run() {
        BluetoothSocket socket = null;
        // Keep listening until exception occurs or a socket is returned
        Log.d(TAG, "run: открыл серверный сокет вечный цикл");
        while (true) {
            try {
                socket = mmServerSocket.accept();

            } catch (IOException e) {
                break;
            }
            // If a connection was accepted
            if (socket != null) {
                Log.d(TAG, "Устройство присоеденилось слушаю:");
                // Do work to manage the connection (in a separate thread)
                manageConnectedSocket(socket);
                try {
                    mmServerSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            }
        }
    }

    private void manageConnectedSocket(BluetoothSocket socket) {
        try {
            instr=socket.getInputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.d(TAG, "manageConnectedSocket");
       // readThread readThread=new readThread(instr); // Сюда нужно будет тоже передать хандлер.
       // readThread.start();

    }

    /** Will cancel the listening socket, and cause the thread to finish */
    public void cancel() {
        try {
            mmServerSocket.close();
        } catch (IOException e) { }
    }
}

