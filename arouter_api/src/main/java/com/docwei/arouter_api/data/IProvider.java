package com.docwei.arouter_api.data;


import android.content.Context;

//不参与页面跳转，只是提供一个对象
public interface IProvider {
    void init(Context context);
}
