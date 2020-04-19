package com.docwei.arouter_api.interceptors;

import com.docwei.arouter_api.PostCard;

public interface IInterceptorCallback {

    void continuing(PostCard postCard);

    void interrupted(Throwable throwable);
}
