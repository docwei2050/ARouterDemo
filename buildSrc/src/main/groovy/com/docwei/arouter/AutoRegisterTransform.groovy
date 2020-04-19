package com.docwei.arouter

import com.android.build.api.transform.*
import com.android.build.gradle.internal.pipeline.TransformManager
import org.apache.commons.codec.digest.DigestUtils
import org.apache.commons.io.FileUtils



class AutoRegisterTransform extends Transform {
    static ArrayList<String> childrenForIRouterRoot =new ArrayList<>();
    static ArrayList<String> childrenForIProviderGroup =new ArrayList<>();
    static ArrayList<String> childrenForIInterceptorGroup =new ArrayList<>();
    static ArrayList<String> childrenForIAutoWirdGroup =new ArrayList<>();
   public  static File logisticsCenterFile;

    @Override
    String getName() {
        //建议写插件名
        return "com.docwei.arouter";
    }

    @Override
    Set<QualifiedContent.ContentType> getInputTypes() {
        return TransformManager.CONTENT_CLASS;
    }

    @Override
    Set<? super QualifiedContent.Scope> getScopes() {
        return TransformManager.SCOPE_FULL_PROJECT;
    }

    @Override
    boolean isIncremental() {
        return false;
    }

    @Override
    void transform(TransformInvocation transformInvocation) throws TransformException, InterruptedException, IOException {
        Collection<TransformInput> inputs = transformInvocation.getInputs()
        TransformOutputProvider outputProvider = transformInvocation.getOutputProvider();
        //第一步：
        //分别处理jar和Directory
        //route-api是jar出现的，LogisticCenter.class是在jar里面
        //IRouteRoot的子类是在app里面出现的
        inputs.each {
            TransformInput input ->
                input.getJarInputs().each {
                    JarInput jarInput ->
                        //远端的jar和本地的jar 其他的moudle
                        String md5Name = DigestUtils.md5Hex(jarInput.file.getAbsolutePath())
                        String jarName=jarInput.name
                        if(jarName.endsWith(".jar")){
                            jarName=jarName.substring(0,jarName.length()-4)
                        }
                        //src--->/xxx/.gradle/caches/transforms-1/files-1.1/rxbinding-2.0.0.aar/c27e5897029e60d94ef4d84f41c7a274/jars/classes.jar
                        //dest需要修改为--->/xxx/Github/ARouter/app/build/intermediates/transforms/com.alibaba.arouter/debug/0.jar
                        //直接沿用原来的路径是不行的，需要改下路径
                        File dest = outputProvider.getContentLocation(
                                jarName+"_"+md5Name, jarInput.getContentTypes(),
                                jarInput.getScopes(), Format.JAR)
                        //outputProvider.getContentLocation第一个参数是要传唯一的key 跟指定路径真没关系
                     /*  Logger.e("jar-src-->"+jarInput.file.getAbsolutePath())
                            Logger.e("jar-dest-->"+dest.getAbsolutePath())*/
                        //在jar包里面怎么扫描文件，依次遍历JarInput：xxx.jar
                        //对于类名确定的，遍历进去里面对比
                        //类名不确定的，则是用ASM对比类头看看其接口是不是IROUTERoot
                       ScanUtils.scanJar(jarInput,dest)
                        //注意导包的问题
                        FileUtils.copyFile(jarInput.getFile(), dest)
                }
                input.getDirectoryInputs().each {
                    DirectoryInput directoryInput ->
                        //我们写的代码，包括注解生成的类
                        directoryInput.file.eachFileRecurse { it ->
                            //todo 与jarInput遍历内容不一样啊
                            //获取流经的每一个文件 //com/docwei/arouter/routes/ARouter$$Root$$ 开头的类
                            //ThirdActivity.class
                            //MyApplication.class
                            //SecondActivity.class
                            if (it.getName().startsWith(ScanUtils.IROUTE_ROOT_CHILD_NAME_PREFIX)) {
                                //针对IROUTE_ROOT的子类，需要使用ASM去判断
                                //todo LogisticesCenter.class不可能在DirectoryInput
                               ScanUtils.scanClass(new FileInputStream(it));
                            }
                        }
                        File dest = outputProvider.getContentLocation(
                                directoryInput.getFile().getAbsolutePath(),
                                directoryInput.getContentTypes(),
                                directoryInput.getScopes(), Format.DIRECTORY
                        )
                        FileUtils.copyDirectory(directoryInput.getFile(), dest)
                }

        }
        //第二步：修改LogisticsCenter类，并重新封装好LogisticsCenter类所在的jar包
        //logisticsCenterFile 是一个JarInput（xxx.jar）
        if(logisticsCenterFile!=null ){
             if(!childrenForIRouterRoot.isEmpty()){
                 //可以注入代码了
                 InsertCodeUtil.insert(logisticsCenterFile,childrenForIRouterRoot)
             }
        }


    }

}
