package com.docwei.arouter_api.interceptors;


import android.content.Context;

import com.docwei.annotation.Route;
import com.docwei.arouter_api.PostCard;

//这个类要在初始化后就创建，用来处理用户定义的拦截器
@Route(path="/arouter/handler")
public class InterceptorHandlerImpl implements IInterceptorHandler {

    @Override
    public void init(Context context) {

    }

    @Override
    public void doInterceptor(PostCard postcard, IInterceptorCallback callback) {

    }
}
