package com.docwei.arouterdemo.createobject;

import com.docwei.arouter_api.data.IProvider;

//实际使用场景是给两个不相互依赖的模块提供数据
public interface IHelloService extends IProvider {
    void sayHello();
}
