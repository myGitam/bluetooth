package com.example.bluetooth;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class AdaptePager2 extends FragmentStateAdapter {
    public AdaptePager2(@NonNull FragmentActivity fragmentActivity) {

        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {

        return new BtLeFragment();
    }

    @Override
    public int getItemCount() {
        return 1;
    }
}
