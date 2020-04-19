package com.docwei.arouterdemo.interceptors;

import android.content.Context;
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
        Toast.makeText(mContext,"我是MyInterceptor，我不拦截你的跳转",Toast.LENGTH_SHORT).show();
        iInterceptorCallback.continu(postCard);
    }

    @Override
    public void init(Context context) {
        mContext = context;
    }
}
