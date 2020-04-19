package com.docwei.arouter_api.template;

import java.util.Map;

public interface IRouterRoot {
    void loadInto(Map<String, Class<? extends IRouterGroup>> wareHouse) ;
}
