package com.example.bluetooth;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.material.textfield.TextInputEditText;

import java.net.URLEncoder;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {


    private static final String TAG = "MyApp";
    private String[] PERMISSIONS;
    PairedDev devToConnect;
    ConnectedClass mySocket;
    TextInputEditText inputEditText;
    TextView textView;
    TextView getTextView;
    TextView inputTextView;
    String stateConnect; // подключено ли устройство или соеденение потеряно
    Button button;
    Button buttonSend;
    String Data;
    ScrollView scroll;
    String symbolDelimeter="\r\n";
    BluetoothManager bluetoothManager;
    BluetoothAdapter bluetoothAdapter;
    GetBluDevice device;
    Menu menu;
    boolean flagBlueConnect=false;
    StringBuilder strBuild=new StringBuilder();
    ProgressBar progressBar;
    ConnectedClass connectedClass;
    MenuItem item;
    SharedPreferences setting;
    String settingEncoding;
    Boolean settingsentinconsole;
    Boolean autoscroll;
    Boolean rn=true;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (Build.VERSION.SDK_INT < 16) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
            MenuItem item = menu.findItem(R.id.connectBlu);
            item.setVisible(false);
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        PERMISSIONS= new String[]{
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        };
        if (!hasPermissions(MainActivity.this,PERMISSIONS)) {

            ActivityCompat.requestPermissions(MainActivity.this,PERMISSIONS,1);
        }

        device=new GetBluDevice();
        bluetoothAdapter=device.getBluetoothAdapter();
        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        button= findViewById(R.id.buttonFind);
        button.setOnClickListener(this::onClick);
        buttonSend=findViewById(R.id.buttonSend);
        buttonSend.setOnClickListener(this::onClick);
        buttonSend.setEnabled(false);
        inputEditText=findViewById(R.id.inputText);
        scroll=findViewById(R.id.scroll);
        getTextView=findViewById(R.id.getDataText);

        //регистрирую широковещательный приёмник
        IntentFilter filter3 = new IntentFilter();
        filter3.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
        filter3.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        registerReceiver(mBroadcastReceiver, filter3);
        ////////////////////////////////////

    }

    @Override
    protected void onResume() {
        super.onResume();
        //Получаю настройки:
        setting=getSharedPreferences("settings", Context.MODE_MULTI_PROCESS);
        Log.d(TAG, "getSharedPreferences: "+ setting.getString(SettingsFragment.APP_PREFERENCES_encoding,"UTF-8"));
        settingEncoding=setting.getString(SettingsFragment.APP_PREFERENCES_encoding,"UTF-8");
        settingsentinconsole=setting.getBoolean(SettingsFragment.APP_PREFERENCES_showsent,true);
        autoscroll=setting.getBoolean(SettingsFragment.APP_PREFERENCES_autoscroll,true);

    }

    @Override
    protected void onPause() {
        super.onPause();
        //   TextInputEditText inputEditText=findViewById(R.id.inputText);
        // textView=findViewById(R.id.textView);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
   // Запрос разрешений всех
    private boolean hasPermissions(Context context, String... PERMISSIONS) {

        if (context != null && PERMISSIONS != null) {

            for (String permission: PERMISSIONS){

                if (ActivityCompat.checkSelfPermission(context,permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }

        return true;
    }
   ///Результат разрешений
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {

            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "BLUETOOTH_CONNECT Permission is granted", Toast.LENGTH_SHORT).show();
            }else {
                Toast.makeText(this, "BLUETOOTH_CONNECT Permission is denied", Toast.LENGTH_SHORT).show();
            }

            if (grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "BLUETOOTH_SCAN is granted", Toast.LENGTH_SHORT).show();
            }else {
                Toast.makeText(this, "BLUETOOTH_SCAN is denied", Toast.LENGTH_SHORT).show();
            }

            if (grantResults[2] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "ACCESS_FINE is granted", Toast.LENGTH_SHORT).show();
            }else {
                Toast.makeText(this, "ACCESS_FINE is denied", Toast.LENGTH_SHORT).show();
            }
            if (grantResults[3] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "ACCESS_COARSE_LOCATION is granted", Toast.LENGTH_SHORT).show();
            }else {
                Toast.makeText(this, "ACCESS_COARSE_LOCATION is denied", Toast.LENGTH_SHORT).show();
            }


        }
    }
/////////////////////////////////////////////////////
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        this.menu=menu;
        MenuItem item = menu.findItem(R.id.connectBlu);
        item.setVisible(false);
        return true;
    }
    //Запускаю активити для вывода спареных устройств после выбора возвращаю результат сюда же
    ActivityResultLauncher<Intent> openRunActivity= registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {


            if (result.getData()!=null) {
                Log.d(TAG, "onActivityResult Main: no null");
                Bundle s = result.getData().getExtras();
                devToConnect=s.getParcelable("DeviceClassic");
                MenuItem item = menu.findItem(R.id.connectBlu);// показать иконку
                item.setVisible(true); // показать иконку
                Log.d(TAG, "Result: DATA: " + devToConnect.getDevName()+" "+devToConnect.getPairBluDev());

            }else {
                if (!bluetoothAdapter.isEnabled()) {
                  showToastBlue();
                }
                else {
                    Toast.makeText(MainActivity.this, "You have not selected a device from the list", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "onActivityResult: NULL " + result.getResultCode());
                }
            }
        }
    });

    @SuppressLint("MissingPermission")
    @Override
    //Нажатие на кнопки ативити
    public void onClick(View view) {
        String inText;
    switch (view.getId()) {
        case R.id.buttonFind:

         //   Intent runIntent=new Intent(this,runActivity.class);
         //   startActivity(intent);

          // Intent runIntent=new Intent(this,runActivity.class);
         //  startActivity(runIntent);
            //openRunActivity.launch(runIntent);
            if (!bluetoothAdapter.isEnabled()) {
                showToastBlue();
            }
            if (bluetoothAdapter.isEnabled()) {
                Intent intent=new Intent(this,MyFragments.class);
                openRunActivity.launch(intent);
            }


             //Intent intent=new Intent(this,ScreenSlidePagerActivity.class);
            // openRunActivity.launch(intent);
            break;

        case R.id.buttonSend:
            mBroadcastReceiver.getResultCode(); // закоментировать
            if(stateConnect=="android.bluetooth.device.action.ACL_CONNECTED") {
                inText = String.valueOf(inputEditText.getText()+"\r\n");


                mySocket.sendData(inText);
                inputEditText.setText("");
                // отображать отправленное если стоит чек
                if(settingsentinconsole) {
                    writtenOUT(inText);
                }
                //тут цвет точго что ввел пользователь + пргкрутка вниз
              //  SpannableStringBuilder builder = new SpannableStringBuilder();
              //  SpannableString str1= new SpannableString(inText);
               // str1.setSpan(new ForegroundColorSpan(Color.RED), 0, str1.length(), 0);
              //  builder.append(str1);


                //getTextView.append("\n");
                //scroll.fullScroll(View.FOCUS_DOWN);//прокрутка автоматическая консольного окна
              //  mySocket.sendData("\r\n");
                //mySocket.cancel();
                break;
            }
            else {
                Toast.makeText(this, "Bluetooth divice DISCONNECTED", Toast.LENGTH_SHORT).show();
                button.setEnabled(true);
                buttonSend.setEnabled(false);
                mySocket.cancel();// Close socket if not Connected
                mySocket.delObject(); // del obgectSingleTOn
                Log.d(TAG, "Close socket");
            }
    }

    }
    //метод выбора меню
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch(id){
            case R.id.action_settings :
                startActivity(new Intent(this, RemoteControlCar.class));
                return true;
            case R.id.open_settings:
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                return true;
            case R.id.save_settings:

                return true;
            case R.id.connectBlu:
                if(flagBlueConnect==false) {
                    try {
                        connectBluetoothDev();

                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                else {
                    disconnectedBlue();
                }


        }
        //headerView.setText(item.getTitle());
        return super.onOptionsItemSelected(item);
    }
// Слушатель широковещательных
      private final BroadcastReceiver mBroadcastReceiver=new BroadcastReceiver() {
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
            Log.d(TAG, "State: " +action);
            stateConnect=action;
                Toast.makeText(context, action, Toast.LENGTH_SHORT).show();


    }
};

// методы для красивого/ ввода вывода
      void writtenIN(String d){
           if(rn==true) {
               d = d.replace("\r", "");
               d = d.replace("\n", "");
               SpannableStringBuilder builder = new SpannableStringBuilder();
               SpannableString str1 = new SpannableString("IN->: " + d);
               str1.setSpan(new ForegroundColorSpan(Color.RED), 0, str1.length(), 0);
               builder.append(str1);
               getTextView.append(builder);
               // getTextView.append("\r\n");
               if (autoscroll) {
                   scroll.fullScroll(View.FOCUS_DOWN);//прокрутка автоматическая консольного окна
               }
           }
           if (rn==false){
               Log.d(TAG, "readMessage: "+d);
                    strBuild.append(d); // Строю строку из входящих данных пока не дойдет до разделителя
                    Log.d(TAG, " strBuild: " +strBuild.length());
                    int c=strBuild.indexOf(symbolDelimeter);
                    Log.d(TAG, "indexOf: "+c);
                    Log.d(TAG, "writtenIN: "+strBuild);
                    if((c>0)&&(strBuild.length()>0)){
                        String takenData= strBuild.substring(0, c);
                        strBuild.delete(0, strBuild.length());
                        Log.d(TAG, "received: " + takenData+ " Char " + takenData.length());
                        SpannableStringBuilder builder = new SpannableStringBuilder();
                        SpannableString str2 = new SpannableString("IN->: ");
                        str2.setSpan(new ForegroundColorSpan(Color.YELLOW),0,str2.length(),0);
                        SpannableString str1 = new SpannableString(str2 + takenData);
                        str1.setSpan(new ForegroundColorSpan(Color.RED), 6, str1.length(), 0);
                        builder.append(str1);
                        getTextView.append(builder);
                        getTextView.append("\r\n");
                        if (autoscroll) {
                            scroll.fullScroll(View.FOCUS_DOWN);//прокрутка автоматическая консольного окна
                        }
                    }
                    if((c==0)){
                        strBuild.delete(0,strBuild.length());
                        getTextView.append("\r\n");
                    }
           }
      }
    // методы для красивого/ ввода вывода
    void writtenOUT(String d){

        d = d.replace("\r", "");
        d = d.replace("\n", "");
        SpannableStringBuilder builder = new SpannableStringBuilder();
        SpannableString str1= new SpannableString("OUT->: "+d);
        str1.setSpan(new ForegroundColorSpan(Color.GREEN), 0, str1.length(), 0);
        builder.append(str1);
        getTextView.append(builder);
        getTextView.append("\r\n");
        if(autoscroll) {
            scroll.fullScroll(View.FOCUS_DOWN);//прокрутка автоматическая консольного окна
        }
    }

    //void ok (){mySocket.sendData("Сообщение получено");}
    @NonNull

    @Override
    public String toString() {
        return super.toString();
    }



      //////////********
//получение адаптера
     public class  GetBluDevice  {
         BluetoothAdapter bluetoothAdapter;
         public BluetoothAdapter getBluetoothAdapter() {
             if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                 bluetoothManager = getSystemService(BluetoothManager.class);
             }
             if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                 bluetoothAdapter = bluetoothManager.getAdapter();
             }
             //ПРоверяю наличие блю на устройстве
             if (bluetoothAdapter == null) {
                 // toast = Toast.makeText("not dev");
                 //toast.show();
                 // Device doesn't support Bluetooth
                 Log.d(TAG, "NOT ADAPTER IN DEVICE");
             }
             // ЕСли есть то проверяю включено или выключено / включаю если нужно
             else {
                 Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                 if (!bluetoothAdapter.isEnabled()) {
                     mStartForResult.launch(enableBtIntent);

                 } else {

                     //getPaired(bluetoothAdapter);
                 }

             }
             return bluetoothAdapter;
         }
     }
        ///////////////////////////////////////////////////
//Обработчик Интента mStartForResult
        ActivityResultLauncher<Intent> mStartForResult = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                // обработка result
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent intentResult = result.getData();
                    Log.i(TAG, "Включил блютуз");
                    boolean s= ActivityCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED;
                    Log.d(TAG, "BluePermission: "+ s);
                    //finish(); // Close Activity
                } else {
                    Log.i(TAG, String.valueOf(result.getResultCode()));
                    boolean s= ActivityCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED;
                    Log.d(TAG, "BluePermission: "+ s);
                    Log.d(TAG, "Blu-not-ON");
                    showToastBlue();

                    //TODO
                }
            }
        });
      // отображение всплывающего
    public void showToastBlue() {
        Toast toast3 = Toast.makeText(getApplicationContext(),
                "Bluetooth OFF\nYou must turn ON", Toast.LENGTH_LONG);
        toast3.setGravity(Gravity.CENTER, 0, 0);

        LinearLayout toastContainer = (LinearLayout) toast3.getView();
        ImageView catImageView = new ImageView(getApplicationContext());
        catImageView.setImageResource(R.drawable.offblue);
        toastContainer.addView(catImageView, 0);
        toast3.show();
    }
    /// ПОказываю прогресс бар
    public void connectBluetoothDev() throws InterruptedException {
        Log.d(TAG, "ProgressBar ");
        progressBar.setVisibility(ProgressBar.VISIBLE);
        //создаю обьект класса через синглтоне createInstance
        mySocket = ConnectedClass.createInstance(devToConnect.getPairBluDev(), mHandler,settingEncoding);
        Log.d(TAG, "SingleTon: " + mySocket);
    }
    void cheakCon(){
        if (mySocket.isConnected) {
            Toast.makeText(MainActivity.this, "Connected", Toast.LENGTH_SHORT).show();
            button.setEnabled(false);
            buttonSend.setEnabled(true);
            MenuItem item = menu.findItem(R.id.connectBlu);
            item.setIcon(R.drawable.bluecon);

            flagBlueConnect = true;
            progressBar.setVisibility(ProgressBar.INVISIBLE);
            // progressBar.setVisibility(ProgressBar.INVISIBLE);
        } else {
            Toast.makeText(MainActivity.this, "Not Connected", Toast.LENGTH_LONG).show();
            Log.d(TAG, "onActivityResult: " + "Not Connected");
            mySocket.cancel();// Close socket if not Connected
            mySocket.delObject(); // del obgectSingleTOn
            buttonSend.setEnabled(false);
            MenuItem item = menu.findItem(R.id.connectBlu);
            item.setIcon(R.drawable.disconnected);
            flagBlueConnect = false;
            button.setEnabled(true);
            progressBar.setVisibility(ProgressBar.INVISIBLE);
            //  progressBar.setVisibility(ProgressBar.INVISIBLE);

        }
    }
    void disconnectedBlue(){
        mySocket.cancel();// Close socket if not Connected
        mySocket.delObject(); // del obgectSingleTOn
        buttonSend.setEnabled(false);
        MenuItem item=menu.findItem(R.id.connectBlu);
        item.setIcon(R.drawable.disconnected);
        flagBlueConnect=false;
        button.setEnabled(true);
        Toast.makeText(MainActivity.this, "disConnected", Toast.LENGTH_SHORT).show();
    }
    //Handler слушатель сообщений данных
    Handler mHandler = new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            Log.d(TAG, "handleMessage: "+msg.what);
            switch (msg.what){
                case 5:
                    Log.d(TAG, "handleMessageObj: "+msg.obj);
                    Data=String.valueOf(msg.obj);
                    // getTextView.append(Data);
                    //if (!getTextView.getText().equals("")) getTextView.append("\r\n"); //избегаем новой строки если ничего нет
                    getTextView.setTextColor(Color.parseColor("#0015ff"));
                    writtenIN(Data);


                    //if(Integer.valueOf(Data)==4){ok();}
                    break;
                case 1:
                   // Log.d(TAG, "handleMessage: 1");
                    //mySocket = connectedClass;
                    cheakCon();
                    break;
            }
        }
    };
    }


