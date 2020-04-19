package com.docwei.arouter_api.template;

import com.docwei.annotation.RouteMeta;
import com.docwei.arouter_api.autowird.IAutoWird;

import java.util.Map;

public interface IAutoWirdGroup {
    void loadInto(Map<String, Class<? extends IAutoWird>> wareHouse) ;
}
