package com.docwei.arouter_api.template;

import com.docwei.annotation.RouteMeta;


import java.util.Map;

//跟IRouterRoot没关系
public interface IProviderGroup {
    void loadInto(Map<String, RouteMeta> providers);

}
