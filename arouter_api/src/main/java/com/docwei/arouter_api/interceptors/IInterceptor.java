package com.docwei.arouter_api.interceptors;

import com.docwei.arouter_api.PostCard;
import com.docwei.arouter_api.data.IProvider;

//这个接口是给用户定义的拦截器用的
public interface IInterceptor extends IProvider {
    void process(PostCard postCard);
}
