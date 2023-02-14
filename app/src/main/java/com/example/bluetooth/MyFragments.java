package com.example.bluetooth;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import android.os.Bundle;

public class MyFragments extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_fragments);

        ViewPager2 pager2=findViewById(R.id.vievpager2);
        AdaptePager2 pageAdapter = new AdaptePager2(this);
        pager2.setAdapter(pageAdapter);
    }
}