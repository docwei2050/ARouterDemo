package com.docwei.arouter_api;


import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.docwei.annotation.RouteMeta;
import com.docwei.arouter_api.thread.DefaultPoolExecutor;

import java.util.concurrent.ThreadPoolExecutor;

//实际干活的类
public class _ARouter {
    private static Context mContext;
    private volatile static _ARouter sInstance;
    private volatile static ThreadPoolExecutor sExecutor = DefaultPoolExecutor.getInstance();

    private _ARouter() {
    }

    public static _ARouter getInstance() {
        if (sInstance == null) {
            synchronized (ARouter.class) {
                if (sInstance == null) {
                    sInstance = new _ARouter();
                }
            }
        }
        return sInstance;
    }

    public static void init(Context context) {
        mContext = context;
        LogisticsCenter.init(context, sExecutor);
    }

    public PostCard build(String path) {
        if (TextUtils.isEmpty(path)) {
            throw new IllegalArgumentException("path cannot be null");
        }
        if (!path.matches("^/[a-zA-Z0-9]*/[a-zA-Z0-9]*")) {
            throw new IllegalArgumentException("path params is error");
        }
        int index = path.lastIndexOf("/");
        String group = path.substring(1, index);
        return new PostCard(path, group, null);

    }

    public Object navgation(Context context, PostCard postCard) {
        //到这里还没有拿到要跳转的页面的.class
        //所以要到仓库里面去获取
        LogisticsCenter.completePostCard(postCard);
        switch (postCard.getType()) {
            case IPROVIDER:
                //无需跳转
                break;
            case ROUTE_PAGE:
                if (context == null) {
                    Intent intent = new Intent(mContext, postCard.destination);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    mContext.startActivity(intent);
                } else {
                    Intent intent = new Intent(context, postCard.destination);
                    context.startActivity(intent);
                }
                break;
            default:
                break;
        }
        return postCard.getProvider();
    }

    //通过接口名获取接口实例对象
    public Object navgation(Class service) {
        RouteMeta routeMeta = WareHouse.sProviders.get(service.getName());
        if (routeMeta == null) {
            return null;
        }
        PostCard postCard = new PostCard(routeMeta.getPath(), routeMeta.getGroup(), routeMeta.destination, routeMeta.type);
        LogisticsCenter.completePostCard(postCard);
        return postCard.getProvider();
    }
}
