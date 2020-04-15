package com.docwei.arouter_api.thread;

import android.util.Log;
import android.util.TimeUtils;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class DefaultPoolExecutor extends ThreadPoolExecutor {
    private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();
    private static final int CORE_SIZE = CPU_COUNT + 1;
    private static final int MAX_SIZE = CPU_COUNT + 1;
    private static final long THREAD_LIFE = 30L;

    public static volatile DefaultPoolExecutor sInstance;

    private DefaultPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory, new RejectedExecutionHandler() {
            @Override
            public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
                Log.e("arouter", "reject too task ");
            }
        });
    }

    public static DefaultPoolExecutor getInstance() {
        if (null == sInstance) {
            synchronized (DefaultPoolExecutor.class) {
                if (null == sInstance) {
                    sInstance = new DefaultPoolExecutor(
                            CORE_SIZE, MAX_SIZE, THREAD_LIFE, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(64),
                            new DefaultThreadFactory());

                }
            }
        }
        return sInstance;
    }

    @Override
    protected void afterExecute(Runnable r, Throwable t) {
        super.afterExecute(r, t);
        if(t==null&&r instanceof Future<?>){
            try {
                ((Future) r).get();
            } catch (ExecutionException e) {
                t=e;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if(t!=null){
            Log.d("arouter", "afterExecute: "+t.getMessage());
        }
    }
}
