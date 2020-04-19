package com.docwei.arouterdemo.createobject;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.docwei.annotation.Route;
import com.docwei.arouterdemo.MyApplication;
@Route(path = "/service/hello")
public class HelloServiceImpl implements IHelloService{
    @Override
    public void sayHello() {
        Log.e("myArouter", "sayHello: " );
        Toast.makeText(MyApplication.getInstance(),"sayHello",Toast.LENGTH_LONG).show();
    }

    @Override
    public void init(Context context) {

    }
}
