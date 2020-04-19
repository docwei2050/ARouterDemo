package com.docwei.arouter_api.interceptors;

import com.docwei.arouter_api.PostCard;

//页面跳转进行进行回调
public interface NavgationCallback {
    void arrival(PostCard postCard);

    void interrupted(PostCard postCard);
}
