package com.docwei.arouter_api;


import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.text.TextUtils;
import android.util.Log;

import com.docwei.annotation.BizType;
import com.docwei.annotation.RouteMeta;
import com.docwei.arouter_api.data.IProvider;
import com.docwei.arouter_api.template.IRouterGroup;
import com.docwei.arouter_api.template.IRouterProvider;
import com.docwei.arouter_api.template.IRouterRoot;
import com.docwei.compiler.Consts;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ThreadPoolExecutor;

import dalvik.system.DexFile;

import static com.docwei.compiler.Consts.NAME_OF_PROVIDER;
import static com.docwei.compiler.Consts.NAME_OF_ROOT;


//承担去找
// dex中的指定的类
//当然也可以使用autoRegister在编译器去做
public class LogisticsCenter {
    static boolean sAutoRegister;

    public static void loadRouteMap() {
        sAutoRegister = false;
        //这个方法将被ASM修改，添加对应的代码 ,尽量不要给ASM要修改的方法添加麻烦
        //register("com.docwei.arouter.routes.ARouter$$Root$$app);
        //register("com.docwei.arouter.routes.ARouter$$Root$$app");

    }

    public static void register(String name) {
        Log.e("myArouter", "register方法的方法--》" + name);
        if (!TextUtils.isEmpty(name)) {
            try {
                Object obj = Class.forName(name).getConstructor().newInstance();
                if (obj instanceof IRouterRoot) {
                    ((IRouterRoot) obj).loadInto(WareHouse.sGroups);
                }
                Log.e("myArouter", "register方法--》" + name);
                sAutoRegister = true;
            } catch (IllegalAccessException e) {
                e.printStackTrace();
                Log.e("myArouter", "registerIllegalAccessException--》" + e.getMessage());
            } catch (InstantiationException e) {
                e.printStackTrace();
                Log.e("myArouter", "registerInstantiationException--》" + e.getMessage());
            } catch (InvocationTargetException e) {
                e.printStackTrace();
                Log.e("myArouter", "registerInvocationTargetException --》" + e.getMessage());
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
                Log.e("myArouter", "registerNoSuchMethodException--》" + e.getMessage());
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                Log.e("myArouter", "registerClassNotFoundException--》" + e.getMessage());
            }


        }
    }


    public static void init(Context context, ThreadPoolExecutor executor) {

        //方式一；耗时的从base.apk（dexFile）去找IRouteRoot的子类全路径 ,这里就耗时1s左右
        //优化的方式，使用auto-register
        //方式二：loadRouteMap()
     /*   loadRouteMap();
       if(sAutoRegister){
           Log.e("myArouter", "init: 走auto-register" );
           return;
       }*/
        final Set<String> fileNames = new HashSet<>();
        ApplicationInfo applicationInfo = context.getApplicationInfo();
        //获取app的apk的路径
        final String path = applicationInfo.sourceDir;
        //因为5.0以上直接就是一个apk的路径，所以不考虑多个路径
        //耗时大概1s左右，这个是Arouter耗时的关键
        DexFile dexFile = null;
        try {
            dexFile = new DexFile(path);
            Enumeration<String> entries = dexFile.entries();
            while (entries.hasMoreElements()) {
                String element = entries.nextElement();
                //去找含有这个com.docwei.arouter.routes路径的文件名
                if (element.contains(Consts.PACKAGE_OF_GENERATE_FILE)) {
                    fileNames.add(element);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }


        for (String fileName : fileNames) {
            //反射去创建这个类对象，然后保存到仓库
            try {
                if (fileName.startsWith(Consts.PACKAGE_OF_GENERATE_FILE + "." + NAME_OF_ROOT)) {
                    ((IRouterRoot) (Class.forName(fileName).getConstructor().newInstance())).loadInto(WareHouse.sGroups);
                }
                if (fileName.startsWith(Consts.PACKAGE_OF_GENERATE_FILE + "." + NAME_OF_PROVIDER)) {
                    ((IRouterProvider) (Class.forName(fileName).getConstructor().newInstance())).loadInto(WareHouse.sProviders);
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }


    public static void completePostCard(PostCard postCard) {
        RouteMeta routeMeta = WareHouse.sRoutes.get(postCard.getPath());
        if (routeMeta == null) {
            Class<? extends IRouterGroup> iRouterGroup = WareHouse.sGroups.get(postCard.getGroup());
            if (iRouterGroup == null) {
                Log.e("myRouter", "completePostCard: " + "path map page not found");
                return;
            }
            try {
                IRouterGroup routerGroup = iRouterGroup.getConstructor().newInstance();
                routerGroup.loadInto(WareHouse.sRoutes);
                //这种类似递归的搞法可以
                completePostCard(postCard);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            }

        } else {
            postCard.destination = routeMeta.destination;
            postCard.type=routeMeta.getType();
            //获取对象实例
            if (postCard.getType() == BizType.IPROVIDER) {
                IProvider iProvider = WareHouse.sProviderObjects.get(postCard.destination);
                if (iProvider == null) {
                    try {
                        iProvider = (IProvider) postCard.getDestination().getConstructor().newInstance();
                        postCard.setProvider(iProvider);
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
        }
    }

}
