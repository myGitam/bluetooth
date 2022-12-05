package com.example.bluetooth;


import android.bluetooth.BluetoothDevice;
import android.os.Parcel;
import android.os.Parcelable;

//Клас для найденных спаренных устройст Класиических,  для удобства чтоб передвать в адаптер когда нажимаю
public class PairedDev implements Parcelable {

    public PairedDev(BluetoothDevice pairBluDev, String devName) {
        this.pairBluDev = pairBluDev;
        this.devName = devName;
    }

    BluetoothDevice pairBluDev;
    String devName;

    public String getDevName() {
        return devName;
    }
    public BluetoothDevice getPairBluDev() {
        return pairBluDev;
    }




    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(this.pairBluDev, flags);
        dest.writeString(this.devName);
    }



    protected PairedDev(Parcel in) {
        this.pairBluDev = in.readParcelable(BluetoothDevice.class.getClassLoader());
        this.devName = in.readString();
    }

    public static final Creator<PairedDev> CREATOR = new Creator<PairedDev>() {
        @Override
        public PairedDev createFromParcel(Parcel source) {
            return new PairedDev(source);
        }

        @Override
        public PairedDev[] newArray(int size) {
            return new PairedDev[size];
        }
    };
}
