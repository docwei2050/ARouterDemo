package com.docwei.arouter_api.interceptors;

import com.docwei.arouter_api.PostCard;

public interface IInterceptorCallback {

    void continu(PostCard postCard);

    void interrupted(Throwable throwable);
}
