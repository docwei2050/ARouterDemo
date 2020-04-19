package com.docwei.arouter_api;

import android.content.Context;

//采用外观模式
public class ARouter {
    private volatile static  ARouter sInstance;
    private ARouter() {

    }
    public static ARouter getInstance() {
        if (sInstance == null) {
            synchronized (ARouter.class) {
                if(sInstance==null){
                    sInstance=new ARouter();
                }
            }
        }
        return sInstance;
    }
    public void init(Context context){
        _ARouter.init(context);

        //至少把组的信息加入到WareHouse
        //再在页面跳转的时候把组内的映射关系加入WareHouse
    }


    public PostCard build(String path){
        return _ARouter.getInstance().build(path);
    }


    public Object navigation(Class service){
        return  _ARouter.getInstance().navgation(service);
    }
}
