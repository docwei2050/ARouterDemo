package com.docwei.arouter_api.template;

import com.docwei.annotation.RouteMeta;
import com.docwei.arouter_api.PostCard;

import java.util.Map;

public interface IRouterGroup {
    void loadInto(Map<String, RouteMeta> wareHouse) ;
}
