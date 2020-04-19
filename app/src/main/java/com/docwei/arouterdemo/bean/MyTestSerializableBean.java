package com.docwei.arouterdemo.bean;

import java.io.Serializable;

public class MyTestSerializableBean implements Serializable {
    public  String book;

    public MyTestSerializableBean(String book) {
        this.book = book;
    }
}
