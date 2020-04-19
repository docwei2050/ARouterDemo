package com.docwei.arouterdemo;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.docwei.annotation.AutoWird;
import com.docwei.annotation.Route;
import com.docwei.arouter_api.ARouter;
import com.docwei.arouterdemo.bean.MyTestParcelBean;
import com.docwei.arouterdemo.bean.MyTestSerializableBean;

@Route(path = "/test/second")
public class SecondActivity extends AppCompatActivity {
    @AutoWird
    public String name;
    @AutoWird
    public long price;
    @AutoWird
    public MyTestSerializableBean mSerializableBean;
    @AutoWird
    public MyTestParcelBean mMyTestParcelBean;
    @AutoWird
    public int score;
    @AutoWird
    public double goal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);
        ARouter.getInstance().inject(this);
        TextView textView = findViewById(R.id.tv);
        textView.setText(name + "---" + mMyTestParcelBean.desk
                + "---" + mSerializableBean.book + "---" +
                price + "---" + score + "---" + goal);

    }


}