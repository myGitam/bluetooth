package com.example.bluetooth;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

class ConnectedClass extends Thread {
    SharedPreferences setting;
    OutputStream mmOutStream;
    InputStream mmInStream;
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private BluetoothSocket mmSocket;
    private BluetoothDevice mmDevice;

    private static final String TAG = "MyApp";
    BluetoothSocket tmp;
    boolean isConnected=false;
    BluetoothAdapter bluetoothAdapter;
    Handler h;
    Message msg;
    String settingEncoding;

    private static ConnectedClass instance; // обьект Singleton
    //////SINGLETON////
    public static synchronized ConnectedClass createInstance(BluetoothDevice device, Handler h, String settingEncoding) throws InterruptedException {
        if (instance == null) {
            instance = new ConnectedClass(device, h,settingEncoding);
            instance.start();

        }
        return instance;
    }
    public static synchronized ConnectedClass getInstance() {
        return instance;
    }






    public ConnectedClass(BluetoothDevice device, Handler h, String settingEncoding) {
        // Use a temporary object that is later assigned to mmSocket,
        // because mmSocket is final
        msg = h.obtainMessage(1,"");
        this.h = h;
        mmDevice = device;
        this.settingEncoding=settingEncoding;
        Log.d(TAG, "ConnectedClass зашел " + mmDevice);
        // this.bluetoothAdapter = bluetoothAdapter;


    }
    @SuppressLint("MissingPermission")
    @Override
    public void run() {
        Log.d(TAG, "run: Поток подключения");
        super.run();
          bluetoothAdapter=BluetoothAdapter.getDefaultAdapter();
          // Get a BluetoothSocket to connect with the given BluetoothDevice
          try {
              // final String PBAP_UUID = "0000112f-0000-1000-8000-00805f9b34fb";
              //  tmp=device.createInsecureRfcommSocketToServiceRecord(ParcelUuid.fromString(PBAP_UUID).getUuid());
              // MY_UUID is the app's UUID string, also used by the server code
              tmp = mmDevice.createInsecureRfcommSocketToServiceRecord(MY_UUID);
              Log.d(TAG, "MY_UUID");
          }
          catch (IOException e) {
          }

          mmSocket = tmp;
          Log.d(TAG, "Открыл сокет");


          bluetoothAdapter.cancelDiscovery();
          Log.d(TAG, "сокет" + mmSocket.getRemoteDevice());
          Log.d(TAG, "run: Запустился");
          // Cancel discovery because it will slow down the connection
          try {
              // Connect the device through the socket. This will block
              // until it succeeds or throws an exception
              Method m = mmDevice.getClass().getMethod("createRfcommSocket", new Class[]{int.class});
              mmSocket = (BluetoothSocket) m.invoke(mmDevice, 1);
              mmSocket.connect();
              isConnected=mmSocket.isConnected();
              Log.d(TAG, "Соеденился");
             // Toast.makeText(context, "", Toast.LENGTH_SHORT).show();
          } catch (IOException connectException) {
              Log.d(TAG, connectException.getMessage());
              try {
                  mmSocket.close();
                  Log.d(TAG, "Закрыл сокет");
                  h.sendMessage(msg);
                  return;
              } catch (IOException closeException) {
              }
          }
          catch (NoSuchMethodException e) {
              e.printStackTrace();
          } catch (InvocationTargetException e) {
              e.printStackTrace();
          } catch (IllegalAccessException e) {
              e.printStackTrace();
          }
          // Do work to manage the connection (in a separate thread)
          try {
              Log.d(TAG, "Получаю потоки");

              mmOutStream = mmSocket.getOutputStream();
              mmInStream = mmSocket.getInputStream();
              new readThread(mmInStream,h,settingEncoding).start();
             // h.sendMessage(msg);
          } catch (IOException e) {
              Log.d(TAG, "ПОТОК НЕ ПОЛУЧИЛ");
              e.printStackTrace();
              h.sendMessage(msg);
          }
        h.sendMessage(msg);
      }

    /////////This method deleted object
    public  void delObject(){
        if(instance!=null){instance=null;}
    }
    // other instance variables can be here

    void sendData(String message) {

        byte[] msgBuffer = new byte[0];
            msgBuffer = message.getBytes();


        Log.d(TAG, "sendData: "+ mmSocket.isConnected());
        try {
            mmOutStream.write(msgBuffer);

            String s=new String(msgBuffer,"UTF-8");
            Log.d(TAG, "...Посылаем данные: " +  s);
           // Toast.makeText(context, "Sended", Toast.LENGTH_SHORT).show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    /** Will cancel an in-progress connection, and close the socket */
    public void cancel() {
        try {
            Log.d(TAG, "Закрыл соеденение");
            mmSocket.close();
        } catch (IOException e) { }
    }

      @Override
      public String toString() {
          return "ConnectedClass{" +
                  "mmOutStream=" + mmOutStream +
                  ", mmSocket=" + mmSocket +
                  ", mmDevice=" + mmDevice +
                  ", bluetoothAdapter=" + bluetoothAdapter +
                  '}';
      }



  }