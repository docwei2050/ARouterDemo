package com.docwei.arouter_api;


import com.docwei.annotation.RouteMeta;
import com.docwei.arouter_api.data.IProvider;
import com.docwei.arouter_api.template.IRouterGroup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

//仓库
public class WareHouse {
    //在处理的时候不可能一开始就加载所有path和class映射关系，应该是只加载对应的组
    //当真正涉及到跳转的时候再去
    //warehouse.put("test",ARouter$$Group$$test.class);
    public static Map<String, Class<? extends IRouterGroup>> sGroups = new HashMap<>();
    //warehouse.put("/test/third",new RouteMeta("/test/third","test",ThirdActivity.class,BizType.ROUTE_PAGE));
    public static Map<String, RouteMeta> sRoutes = new HashMap<>();

    //public class InterceptorHandlerImpl implements IInterceptorHandler
    //String是：com.docwei.arouter_api.interceptors.IInterceptorHandler
    public static Map<String, RouteMeta> sProviders = new HashMap<>();


    //InterceptorHandlerImpl.class 对应其实例对象
    public static Map<Class, IProvider> sProviderObjects = new HashMap<>();



}
