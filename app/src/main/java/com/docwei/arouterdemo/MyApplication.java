package com.docwei.arouterdemo;

import android.app.Application;

import com.docwei.arouter_api.ARouter;

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        ARouter.getInstance().init(this);
    }
}
