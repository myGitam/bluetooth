package com.example.bluetooth;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;

import java.util.Objects;

public class BtleFindingDev  {
    BluetoothDevice BTLEdevice;
    public BtleFindingDev(BluetoothDevice BTLEdevice) {
        this.BTLEdevice = BTLEdevice;
    }


    public BluetoothDevice getBTLEdevice() {
        return BTLEdevice;
    }
    @SuppressLint("MissingPermission")
    public String getname(){
        return BTLEdevice.getName();
    }
    public void setBTLEdevice(BluetoothDevice BTLEdevice) {
        this.BTLEdevice = BTLEdevice;
    }


    @Override
    public String toString() {
        return "BtleFindingDev{" +
                "BTLEdevice=" + BTLEdevice +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BtleFindingDev that = (BtleFindingDev) o;
        return Objects.equals(BTLEdevice, that.BTLEdevice);
    }

    @Override
    public int hashCode() {
        return Objects.hash(BTLEdevice);
    }
}
