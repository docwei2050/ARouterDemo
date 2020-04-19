package com.docwei.arouterdemo;

import android.app.Application;
import android.content.Context;

import com.docwei.arouter_api.ARouter;

public class MyApplication extends Application {
    private static Context sContext;
    @Override
    public void onCreate() {
        super.onCreate();
        ARouter.getInstance().init(this);
        sContext=this;
    }
    public static Context getInstance(){
        return sContext;
    }
}
