package com.example.bluetooth;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

// это для управления Например машинкой
public class RemoteControlCar extends AppCompatActivity {
    Button Up;
    Button down;
    Button right;
    Button left;
    Button upleft;
    Button upright;
    Button downleft;
    Button downright;
    ConnectedClass socket;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_remote_control_car);
        ConnectedClass connectedClass= ConnectedClass.getInstance();
        socket=connectedClass;
        Up=findViewById(R.id.Up);
        Up.setOnClickListener(this::onClick);
    }
    public void onClick(View view) {
        String inText;
        switch (view.getId()) {
            case R.id.Up:
               socket.sendData("1");


                break;

        }
    }

}