package com.docwei.arouter_api.interceptors;


import android.content.Context;

import com.docwei.annotation.Route;
import com.docwei.arouter_api.PostCard;
import com.docwei.arouter_api.WareHouse;
import com.docwei.arouter_api._ARouter;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

//这个类要在初始化后就创建，用来处理用户定义的拦截器
@Route(path = "/arouter/handler")
public class InterceptorHandlerImpl implements IInterceptorHandler {
    private CountDownLatch mCountDownLatch;
    public Object lock = new Object();

    @Override
    public void init(Context context) {
        if (WareHouse.sInterceptors.size() > 0) {
            try {
                //所有的拦截器都加入了，，，也就是说拦截器是全局的啊
                for (Map.Entry<Integer, Class<? extends IInterceptor>> entry : WareHouse.sInterceptors.entrySet()) {
                    IInterceptor interceptor = entry.getValue().getConstructor().newInstance();
                    interceptor.init(context);
                    WareHouse.sInterceptorObjects.add(interceptor);

                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void doInterceptor(final PostCard postcard, final IInterceptorCallback callback) {

        final int size = WareHouse.sInterceptorObjects.size();
        if (size > 0) {
            _ARouter.sExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        mCountDownLatch = new CountDownLatch(size);
                        executeInterceptor(0, postcard, callback, size);
                        mCountDownLatch.await();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            });

        }
    }

    //Arouter的源码是在线程池中执行的
    private void executeInterceptor(final int index, final PostCard postcard, final IInterceptorCallback callback, final int size) {
        if (index < size) {
            WareHouse.sInterceptorObjects.get(index).process(postcard, new IInterceptorCallback() {
                @Override
                public void continuing(PostCard postCard) {
                    executeInterceptor(index + 1, postcard, callback, size);
                    mCountDownLatch.countDown();
                    if (index + 1 == size) {
                        callback.continuing(postCard);
                    }
                }
                @Override
                public void interrupted(Throwable throwable) {
                    callback.interrupted(throwable);
                    int n = index;
                    while (n + 1 <= size) {
                        mCountDownLatch.countDown();
                        n++;
                    }
                }
            });
        }
    }
}
