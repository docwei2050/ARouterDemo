package com.docwei.arouter
import com.android.build.api.transform.JarInput

import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Opcodes


import java.util.jar.JarEntry;
import java.util.jar.JarFile;

//1.从jarInput 中扫描确定的类和不确定的类
// 确定的类直接比对  不确定的类借助ASM去比对类头上的接口名
//2.从DirectoryInput的每一个File中去比对这两个东西

class ScanUtils {
    static String IROUTE_ROOT = "IRouterRoot";
    static String IPROVIDER_ROOT = "IProviderGroup";
    static String IINTERCEPTOR_ROOT = "IInterceptorGroup";
    static String IAUTOWIRD_ROOT = "IAutoWirdGroup";
    static String IROUTE_ROOT_PACKAGE = "com/docwei/arouter_api/template/";

    static String LOGISTICS_CENTER_PACKAGE="com/docwei/arouter_api/LogisticsCenter"
    static String LOGISTICS_CENTER =  LOGISTICS_CENTER_PACKAGE+".class";
    //这个包名是固定的  com.docwei.arouter.routes
    static String IROUTE_ROOT_CHILD_PACKAGE = "com/docwei/arouter/routes/ARouter\$\$";
    static String IROUTE_ROOT_CHILD_NAME_PREFIX = "ARouter\$\$";
    //jarInput.getFile()就是 xxx.jar /Users/docwei/GitHub/ARouterDemo/annotation/build/.transforms/35a3fad3a22cbd734c336b63871cd8c1/jetified-annotation.jar
    static void scanJar(JarInput jarInput,File destFile) {
        File file = jarInput.getFile();
        try {
            //todo Q：就把JarFile当做DexFile来看
            JarFile jarFile = new JarFile(file);
            Enumeration<JarEntry> entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                JarEntry jarEntry = entries.nextElement();
                //打印出来的class类是：
                /*androidx/customview/R$attr.class
                androidx/activity/R$color.class
                androidx/activity/R$integer.class*/
                if (shouldLockUpClasses(jarEntry.getName())&& jarEntry.getName().startsWith(IROUTE_ROOT_CHILD_PACKAGE)) {
                    //针对IROUTE_ROOT的子类，需要使用ASM去判断
                    //todo Q：可以通过JarFile获取指定文件（jarEntry）的流
                    scanClass(jarFile.getInputStream(jarEntry));
                }
                if (jarEntry.getName().equals(LOGISTICS_CENTER)) {
                    //找到logisticsCenter.class所在的jar 文件
                    AutoRegisterTransform.logisticsCenterFile = destFile;
                }

            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    static void scanClass(InputStream inputStream) {
        //todo 导包很容易搞错
        ClassReader cr = new ClassReader(inputStream);
        ClassWriter cw = new ClassWriter(cr, 0);
        //使用ASM5的api
        ScanClassVistor classVistor = new ScanClassVistor(Opcodes.ASM5, cw);
        cr.accept(classVistor, ClassReader.EXPAND_FRAMES)
        inputStream.close()

    }

    static boolean shouldLockUpClasses(String name) {
        return name.endsWith(".class") && !(name.startsWith("androidx") || name.contains("R\$") || name.endsWith("R.class"));
    }
    //去查找类头的情况 找出IRouterRoot的子类
   static class ScanClassVistor extends ClassVisitor {
       ScanClassVistor(int api) {
            super(api)
        }
       ScanClassVistor(int api, ClassVisitor classVisitor) {
            super(api, classVisitor)
        }
        @Override
        void visit(int version, int access, String name, String signature,
                   String superName, String[] interfaces) {
            super.visit(version, access, name, signature, superName, interfaces)
            //public class ARouter$$Root$$app implements IRouterRoot {
            //很明显我们找的就是IRouterRoot的实现类
            interfaces.each { String interfaceName ->

                if ((IROUTE_ROOT_PACKAGE+IROUTE_ROOT).equals(interfaceName)&&!AutoRegisterTransform.childrenForIRouterRoot.contains(name)) {
                    AutoRegisterTransform.childrenForIRouterRoot.add(name);
                }
                if ((IROUTE_ROOT_PACKAGE+IPROVIDER_ROOT).equals(interfaceName)&&!AutoRegisterTransform.childrenForIProviderGroup.contains(name)) {
                    AutoRegisterTransform.childrenForIProviderGroup.add(name);
                }
                if ((IROUTE_ROOT_PACKAGE+IINTERCEPTOR_ROOT).equals(interfaceName)&&!AutoRegisterTransform.childrenForIInterceptorGroup.contains(name)) {
                    AutoRegisterTransform.childrenForIInterceptorGroup.add(name);
                }
                if ((IROUTE_ROOT_PACKAGE+IAUTOWIRD_ROOT).equals(interfaceName)&&!AutoRegisterTransform.childrenForIAutoWirdGroup.contains(name)) {
                    AutoRegisterTransform.childrenForIAutoWirdGroup.add(name);
                }
            }
        }
    }




}
