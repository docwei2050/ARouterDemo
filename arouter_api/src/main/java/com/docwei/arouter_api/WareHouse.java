package com.docwei.arouter_api;


import com.docwei.annotation.RouteMeta;
import com.docwei.arouter_api.template.IRouterGroup;

import java.util.HashMap;
import java.util.Map;

//仓库
public class WareHouse {
    //在处理的时候不可能一开始就加载所有path和class映射关系，应该是只加载对应的组
    //当真正涉及到跳转的时候再去
    public static Map<String, Class<? extends IRouterGroup>> sGroups = new HashMap<>();
    public static Map<String, RouteMeta> sRoutes = new HashMap<>();
}
