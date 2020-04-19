package com.docwei.arouterdemo.interceptors;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.docwei.annotation.Interceptor;
import com.docwei.arouter_api.PostCard;
import com.docwei.arouter_api.interceptors.IInterceptor;
import com.docwei.arouter_api.interceptors.IInterceptorCallback;


@Interceptor(priority = 9)
public class MyInterceptor implements IInterceptor {
    public Context mContext;

    @Override
    public void process(PostCard postCard, IInterceptorCallback iInterceptorCallback) {
        //不拦截
        iInterceptorCallback.continuing(postCard);
    }

    @Override
    public void init(Context context) {
        mContext = context;
    }
}
