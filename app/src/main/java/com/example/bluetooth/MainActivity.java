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
import android.text.SpannableStringBuilder;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, View.OnLongClickListener {


    private static final String TAG = "MyApp";
    private String[] PERMISSIONS;
    PairedDev devToConnect;
    ConnectedClass mySocket;
    TextInputEditText inputEditText;
    DisplayText displayText;
    String APP_PREFERENCES_delimeter="delimeterselect";
    String delimeter;
    TextView getTextView;
    TextView inputTextView;
    String stateConnect; // подключено ли устройство или соеденение потеряно
    Button button;
    Button buttonSend;
    Button buttonMem1;
    Button buttonMem2;
    Button buttonMem3;
    Button buttonMem4;
    ArrayList<Button> listButton;
    String Data;
    ScrollView scroll;
    BluetoothManager bluetoothManager;
    BluetoothAdapter bluetoothAdapter;
    GetBluDevice device;
    Menu menu;
    boolean flagBlueConnect=false;
    StringBuilder strBuild=new StringBuilder();
    ProgressBar progressBar;
    SpannableStringBuilder builder;
    SharedPreferences setting;
    String settingEncoding;
    Boolean settingsentinconsole;
    Boolean autoscroll;
    Boolean linefeed=false;
    ButtonMemory buttonMemory;
    Spinner spinner;
    String[] delimeterArray = { "CR/NL","CR","NL","Non"};
    DialogFragment dialogFragment;

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
        //Не выключать экран пога программа открыта
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        ////
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
        buttonMem1=findViewById(R.id.memory1);
        buttonMem2=findViewById(R.id.memory2);
        buttonMem3=findViewById(R.id.memory3);
        buttonMem4=findViewById(R.id.memory4);
        buttonMem1.setOnLongClickListener(this);
        buttonMem2.setOnLongClickListener(this);
        buttonMem3.setOnLongClickListener(this);
        buttonMem4.setOnLongClickListener(this);
        buttonMem1.setOnClickListener(this::onClick);
        buttonMem2.setOnClickListener(this::onClick);
        buttonMem3.setOnClickListener(this::onClick);
        buttonMem4.setOnClickListener(this::onClick);

        inputEditText=findViewById(R.id.inputText);
        scroll=findViewById(R.id.scroll);
        getTextView=findViewById(R.id.getDataText);
        spinner=findViewById(R.id.spinnerDelimeter);
        spinner.setOnItemSelectedListener(spinerSelectedlistener);

        //регистрирую широковещательный приёмник
        IntentFilter filter3 = new IntentFilter();
        filter3.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
        filter3.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        registerReceiver(mBroadcastReceiver, filter3);
        ////////////////////////////////////

    }

         ////слушать выпадающего списка выбора конца строки
        AdapterView.OnItemSelectedListener spinerSelectedlistener=new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                SharedPreferences.Editor editor=setting.edit();
                editor.putInt(APP_PREFERENCES_delimeter,i);
                delimeter=spinner.getSelectedItem().toString();
                Log.d(TAG, "onItemSelected: " +delimeter);
                editor.apply();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        };
    @Override
    protected void onResume() {

        Log.d(TAG, "onResume: ");
        super.onResume();
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, delimeterArray);
        // Определяем разметку для использования при выборе элемента
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Применяем адаптер к элементу spinner
        spinner.setAdapter(spinnerAdapter);

        ///////******////////*********
        //Получаю настройки:
        setting=getSharedPreferences("settings", Context.MODE_MULTI_PROCESS);
        Log.d(TAG, "getSharedPreferences: "+ setting.getString(SettingsFragment.APP_PREFERENCES_encoding,"UTF-8"));
        settingEncoding=setting.getString(SettingsFragment.APP_PREFERENCES_encoding,"UTF-8");
        settingsentinconsole=setting.getBoolean(SettingsFragment.APP_PREFERENCES_showsent,true);
        autoscroll=setting.getBoolean(SettingsFragment.APP_PREFERENCES_autoscroll,true);
        spinner.setSelection(setting.getInt(APP_PREFERENCES_delimeter,0)); // читаю что сохраненно спинере разделителя И УСТАНАВЛИВАЮ ЧТОБ БЫЛО ВИДНО
        delimeter=spinner.getSelectedItem().toString();
        //Списко из кнопок памяти
        listButton=new ArrayList<>();
        listButton.add(buttonMem1);
        listButton.add(buttonMem2);
        listButton.add(buttonMem3);
        listButton.add(buttonMem4);
        try {
            //вызываю метод для чтения настроек кнопок
            setButtonNamefromMem();//Читаю настройки кнопок паямяти
        } catch (JSONException e) {
            e.printStackTrace();
        }


        displayText=new DisplayText(this);
        buttonMemory=new ButtonMemory(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause: ");
        //   TextInputEditText inputEditText=findViewById(R.id.inputText);
        // textView=findViewById(R.id.textView);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: ");
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
    ///создание меню
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

                inText = String.valueOf(inputEditText.getText());
                sendData(inText);
                inputEditText.setText("");
                //mySocket.sendData(inText);
                // отображать отправленное если стоит чек
                //    writtenOUT(inText);
                break;


        case R.id.memory1:
            Log.d(TAG, "onClick: memory1");

            try {
                String v=buttonMemory.getMemArgument(view.getResources().getResourceEntryName(view.getId()));
                sendData(buttonMemory.getMemArgument(view.getResources().getResourceEntryName(view.getId())));
                Log.d(TAG, "onClick: " +v);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            break;
        case R.id.memory2:
            Log.d(TAG, "onClick: memory2");
            try {
                String v=buttonMemory.getMemArgument(view.getResources().getResourceEntryName(view.getId()));
                sendData(v);
                Log.d(TAG, "onClick: " +v);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            break;
        case R.id.memory3:
            Log.d(TAG, "onClick: memory3");
            try {
                String v=buttonMemory.getMemArgument(view.getResources().getResourceEntryName(view.getId()));
                sendData(v);
                Log.d(TAG, "onClick: " +v);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            break;
        case R.id.memory4:
            Log.d(TAG, "onClick: memory4");
            try {
                String v=buttonMemory.getMemArgument(view.getResources().getResourceEntryName(view.getId()));
                sendData(v); //  отправляю текст
                Log.d(TAG, "onClick: " +v);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            break;
    }

    }
    //метод выбора меню
    private void sendData(String s){
        cheakCon(); // проверка есть ли подключение
        String outText;
        if(stateConnect=="android.bluetooth.device.action.ACL_CONNECTED") {
            outText = s+"\r\n";
            mySocket.sendData(outText);
            // отображать отправленное если стоит чек
            writtenOUT(outText);

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
    ////////
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

          getTextView.append(displayText.writeInText(d,delimeter));

          if (autoscroll) {
                   scroll.fullScroll(View.FOCUS_DOWN);//прокрутка автоматическая консольного окна
          }


      }
    // методы для красивого/ ввода вывода
    void writtenOUT(String d){

        if(settingsentinconsole) {
        getTextView.append(displayText.readWriteOutText(d));
        if (autoscroll) {
            scroll.fullScroll(View.FOCUS_DOWN);//прокрутка автоматическая консольного окна
        }
//
    }
      }

    //void ok (){mySocket.sendData("Сообщение получено");}
    @NonNull

    @Override
    public String toString() {
        return super.toString();
    }
//// Длинтельное нажатие на кнопки памяти для сохранения значений
    @Override
    public boolean onLongClick(View v) {
         
          switch (v.getId()){
              case R.id.memory1:
                  Log.d(TAG, "ID: "+ v.getResources().getResourceEntryName(v.getId())); //получаю ID кнопки
                  dialogFragment=new ButtonMemory(this,v.getResources().getResourceEntryName(v.getId()),mHandler);
                  dialogFragment.show(getSupportFragmentManager(),"Save presets");
                  Log.d(TAG, "onLongClick: memory1");
              break;
              case R.id.memory2:
                  Log.d(TAG, "ID: "+ v.getResources().getResourceEntryName(v.getId())); //получаю ID кнопки
                  dialogFragment=new ButtonMemory(this,v.getResources().getResourceEntryName(v.getId()),mHandler);
                  dialogFragment.show(getSupportFragmentManager(),"Save presets");
                  Log.d(TAG, "onLongClick: memory2");
              break;
              
              case R.id.memory3:
                  Log.d(TAG, "ID: "+ v.getResources().getResourceEntryName(v.getId())); //получаю ID кнопки
                  dialogFragment=new ButtonMemory(this,v.getResources().getResourceEntryName(v.getId()),mHandler);
                  dialogFragment.show(getSupportFragmentManager(),"Save presets");
                  Log.d(TAG, "onLongClick: memory3");
              break;
              
              case R.id.memory4:
                  Log.d(TAG, "ID: "+ v.getResources().getResourceEntryName(v.getId())); //получаю ID кнопки
                  dialogFragment=new ButtonMemory(this,v.getResources().getResourceEntryName(v.getId()),mHandler);
                  dialogFragment.show(getSupportFragmentManager(),"Save presets");
                  Log.d(TAG, "onLongClick: memory4");
              break;
          }
        return true;
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
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.R) {
            Toast toast3 = Toast.makeText(this, "Bluetooth OFF\nYou must turn ON", Toast.LENGTH_LONG);
            toast3.setGravity(Gravity.CENTER, 0, 0);
            toast3.show();
        }
        else {
        Toast toast3 = Toast.makeText(this, "Bluetooth OFF\nYou must turn ON", Toast.LENGTH_LONG);
        toast3.setGravity(Gravity.CENTER, 0, 0);
        LinearLayout toastContainer = (LinearLayout) toast3.getView();
        ImageView catImageView = new ImageView(getApplicationContext());
        catImageView.setImageResource(R.drawable.offblue);
        toastContainer.addView(catImageView, 0);
        toast3.show();
    }}
    /// ПОказываю прогресс бар
    public void connectBluetoothDev() throws InterruptedException {
        Log.d(TAG, "ProgressBar ");
        progressBar.setVisibility(ProgressBar.VISIBLE);
        //создаю обьект класса через синглтоне createInstance
        mySocket = ConnectedClass.createInstance(devToConnect.getPairBluDev(), mHandler,settingEncoding);
        Log.d(TAG, "SingleTon: " + mySocket);
    }
    // метод проверки соединения и ативации дизактивации кнопки
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
     //// метод для начальной инициализации кнопок памяти при загрузке приложения
    private void setButtonNamefromMem() throws JSONException {
        //Читаю настройки кнопок паямяти
        ButtonMemory b= new ButtonMemory(this);
        for(Button but: listButton){
            //вызываю метод класса ButtonMemory для чтения имени по ID кнопки. У меня жесткоо память завязана на имена кнопок// для получения названия кнопки
            but.setText(b.getName(but.getResources().getResourceEntryName(but.getId())));
        }



    }
    //Handler слушатель сообщений данных
    Handler mHandler = new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            Log.d(TAG, "handleMessage: "+msg.what);
            switch (msg.what){
                //5 получение входящих данных из потока
                case 5:
                    Log.d(TAG, "handleMessageObj: "+msg.obj);
                    Data=String.valueOf(msg.obj);
                    // getTextView.append(Data);
                    //if (!getTextView.getText().equals("")) getTextView.append("\r\n"); //избегаем новой строки если ничего нет
                    getTextView.setTextColor(Color.parseColor("#0015ff"));
                    writtenIN(Data);


                    //if(Integer.valueOf(Data)==4){ok();}
                    break;
                    //1 проверка соединения
                case 1:
                   // Log.d(TAG, "handleMessage: 1");
                    //mySocket = connectedClass;
                    cheakCon();
                    break;
                    //2 обновление названий кнопок
                case 2:
                    try {
                        setButtonNamefromMem();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    break;
            }
        }
    };
    }


