package com.example.bluetooth;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import android.os.Bundle;
import android.view.Menu;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class MyFragments extends AppCompatActivity {
    Menu menu;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_fragments);

        ViewPager2 pager2=findViewById(R.id.vievpager2);
        AdaptePager2 pageAdapter = new AdaptePager2(this);
        pager2.setAdapter(pageAdapter);
       // pager2.setCurrentItem(1); устанавливать можно какую вкладку открыть
        //// Создание шапки с названиями фрагментов
        TabLayout tabLayout = findViewById(R.id.tab_layout);


        TabLayoutMediator tabLayoutMediator= new TabLayoutMediator(tabLayout, pager2, new TabLayoutMediator.TabConfigurationStrategy() {
            @Override
            public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {

                switch (position){
                    case 0:
                        tab.setText("BT Classic");

                        break;
                    case 1:

                        tab.setText("BTle");
                        break;
                }
            }
        }
        );

        tabLayoutMediator.attach();
    }

}