package com.docwei.arouter_api;

import android.content.Context;

import com.docwei.annotation.RouteMeta;

public class PostCard extends RouteMeta {

    public PostCard(String path, String group, Class<?> destination) {
        super(path, group, destination);
    }

    public PostCard(String path, String group) {
        super(path, group);
    }

    public void navgation(Context context) {
        _ARouter.getInstance().navgation(context,this);
    }

    public void navgation() {
        navgation(null);
    }
}
