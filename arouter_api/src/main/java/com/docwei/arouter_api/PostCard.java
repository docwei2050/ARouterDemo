package com.docwei.arouter_api;

import android.content.Context;

import com.docwei.annotation.BizType;
import com.docwei.annotation.RouteMeta;
import com.docwei.arouter_api.data.IProvider;

public class PostCard extends RouteMeta {

   public IProvider provider;

    public IProvider getProvider() {
        return provider;
    }

    public void setProvider(IProvider provider) {
        this.provider = provider;
    }

    public PostCard(String path, String group, Class<?> destination) {
        super(path, group, destination);
    }

    public PostCard(String path, String group, Class<?> destination, BizType type) {
        super(path, group, destination, type);
    }

    public PostCard(String path, String group) {
        super(path, group);
    }

    public Object navgation(Context context) {
        return _ARouter.getInstance().navgation(context,this);
    }

    public Object navgation() {
        return navgation(null);
    }
}
