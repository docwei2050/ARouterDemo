package com.docwei.arouterdemo;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;
import android.view.View;

import com.docwei.arouter_api.ARouter;
import com.docwei.arouterdemo.createobject.HelloServiceImpl;
import com.docwei.arouterdemo.createobject.IHelloService;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void clickMe2Second(View view) {
        ARouter.getInstance().build("/test/second").navgation();
    }

    public void clickMe2Third(View view) {
        ARouter.getInstance().build("/test/third").navgation(this);
    }

    public void clickMe2TestModule(View view) {
        ARouter.getInstance().build("/testmodule/test").navgation(this);
    }

    public void createObjectByPath(View view) {
       HelloServiceImpl helloservice= (HelloServiceImpl) ARouter.getInstance().build("/service/hello").navgation();
       helloservice.sayHello();
    }

    public void createObjectByType(View view) {
        IHelloService helloService= (IHelloService) ARouter.getInstance().navigation(IHelloService.class);
        helloService.sayHello();
    }
}
