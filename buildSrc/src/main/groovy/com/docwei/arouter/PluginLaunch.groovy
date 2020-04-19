package com.docwei.arouter;

import com.android.build.gradle.AppExtension
import com.android.build.gradle.AppPlugin
import com.docwei.arouter.AutoRegisterTransform
import org.gradle.api.Plugin
import org.gradle.api.Project

class PluginLaunch implements Plugin<Project> {
    @Override
    void apply(Project project) {
       //这个插件只给app  应用了这个插件com.android.application才算
        def isApp=project.plugins.hasPlugin(AppPlugin);
        if(isApp){
            //app插件拓展
            def extension=project.extensions.getByType(AppExtension)
            extension.registerTransform(new AutoRegisterTransform())
          //做两件事情
            //1.遍历jarFile和Directory去找IRouterRoot IProviderGroup IInterceptorGroup的子类并且保存起来
            //2.去找Logistices这个类，并且在其方法里面插入register("IRouterRoot子类全路径")的代码

            //初始化日志
           Logger.make(project);
        }
    }
}