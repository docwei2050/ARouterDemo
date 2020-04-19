package com.docwei.arouter_api.template;

import com.docwei.annotation.RouteMeta;
import com.docwei.arouter_api.interceptors.IInterceptor;

import java.util.Map;

//单独的
public interface IInterceptorGroup {
    void loadInto(Map<Integer, Class<? extends IInterceptor>> wareHouse) ;
}
