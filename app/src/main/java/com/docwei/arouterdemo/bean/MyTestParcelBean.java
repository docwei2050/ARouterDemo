package com.docwei.arouterdemo.bean;

import android.os.Parcel;
import android.os.Parcelable;

public class MyTestParcelBean implements Parcelable {
   public String desk;

    public MyTestParcelBean(String desk) {
        this.desk = desk;
    }

    protected MyTestParcelBean(Parcel in) {
        desk = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(desk);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<MyTestParcelBean> CREATOR = new Creator<MyTestParcelBean>() {
        @Override
        public MyTestParcelBean createFromParcel(Parcel in) {
            return new MyTestParcelBean(in);
        }

        @Override
        public MyTestParcelBean[] newArray(int size) {
            return new MyTestParcelBean[size];
        }
    };
}
