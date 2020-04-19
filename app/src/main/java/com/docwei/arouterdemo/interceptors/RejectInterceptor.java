package com.docwei.arouterdemo.interceptors;

import android.content.Context;
import android.os.Looper;
import android.widget.Toast;

import com.docwei.annotation.Interceptor;
import com.docwei.arouter_api.PostCard;
import com.docwei.arouter_api.interceptors.IInterceptor;
import com.docwei.arouter_api.interceptors.IInterceptorCallback;

/*@Interceptor(priority = 10)*/
public class RejectInterceptor implements IInterceptor {
    public Context mContext;

    @Override
    public void process(PostCard postCard, IInterceptorCallback iInterceptorCallback) {
       /* Looper.prepare();
        Toast.makeText(mContext, "拦截跳转了，，哈哈", Toast.LENGTH_SHORT).show();
        Looper.loop();*/
        iInterceptorCallback.interrupted(new Exception("我就是要拦截你的跳转"));
    }

    @Override
    public void init(Context context) {
        mContext = context;
    }
}
