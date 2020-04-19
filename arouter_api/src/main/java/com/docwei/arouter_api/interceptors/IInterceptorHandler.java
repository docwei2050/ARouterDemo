package com.docwei.arouter_api.interceptors;

import com.docwei.arouter_api.PostCard;
import com.docwei.arouter_api.data.IProvider;

public interface IInterceptorHandler extends IProvider {
    void doInterceptor(PostCard postcard, IInterceptorCallback callback);
}
