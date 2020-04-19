package com.docwei.arouter_api;


import com.docwei.annotation.RouteMeta;
import com.docwei.arouter_api.autowird.IAutoWird;
import com.docwei.arouter_api.data.IProvider;
import com.docwei.arouter_api.interceptors.IInterceptor;
import com.docwei.arouter_api.template.IRouterGroup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

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


    //Integer就是优先级  treeMap按照key自然排序 源码此处重写了
    public static TreeMap<Integer, Class<? extends IInterceptor>> sInterceptors = new TreeMap<>();
    public static ArrayList<IInterceptor> sInterceptorObjects = new ArrayList<>();



    //跟源码不一样的 String是Activity类的全路径
    public static Map<String, Class<? extends IAutoWird>> sAutoWird=new HashMap<>();
}
