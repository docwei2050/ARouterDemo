package com.docwei.arouter_api.template;

import com.docwei.annotation.RouteMeta;


import java.util.Map;

//
public interface IRouterProvider {
    void loadInto(Map<String, RouteMeta> providers);

}
