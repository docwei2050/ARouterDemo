package com.docwei.arouter

import org.apache.commons.io.IOUtils
import org.objectweb.asm.*

import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.jar.JarOutputStream
import java.util.zip.ZipEntry

class InsertCodeUtil {
    static String LOAD_ROUTE_MAP = "loadRouteMap";

    static void insert(File rawFile, ArrayList<String> list) {
        //未被修改的文件
        //新建一个空的jar.opt文件
        File optFile = new File(rawFile.getParent(), rawFile.getName() + ".opt")
        if (optFile.exists()) {
            optFile.delete()
        }
        //从原来的jar里面获取数据用，需要封装成JarFile
        JarFile rawJarFile = new JarFile(rawFile);
        Enumeration<JarEntry> entries = rawJarFile.entries();
        // 创建新的jar.opt文件流 用于写入到文件
        JarOutputStream jarOutputStream = new JarOutputStream(new FileOutputStream(optFile))
        while (entries.hasMoreElements()) {
            JarEntry jarEntry = entries.nextElement();
            String entryName = jarEntry.getName();
            ZipEntry zipEntry = new ZipEntry(entryName);
            //从指定的JarFile获取对应的流
            InputStream inputStream = rawJarFile.getInputStream(jarEntry)
            //放入一个空的zipEntry
            jarOutputStream.putNextEntry(zipEntry)
            if (ScanUtils.LOGISTICS_CENTER == entryName) {
                Logger.e("开始准备插入代码到方法" + entryName)
                /*  public static void loadRouteMap() {
                sAutoRegister = false;
                //这个方法将被ASM修改，添加对应的代码 ,尽量不要给ASM要修改的方法添加麻烦
                //register("com.docwei.arouter.routes.ARouter$$Root$$app);
                }*/
                byte[] bytes = actuallyInsertCode(inputStream)
                jarOutputStream.write(bytes);

            } else {
                jarOutputStream.write(IOUtils.toByteArray(inputStream))
            }
            inputStream.close()
            jarOutputStream.closeEntry()
        }
        jarOutputStream.close()
        rawJarFile.close()
        //todo 不删除旧的，就一直会用旧的啊
        if (rawFile.exists()) {
            rawFile.delete()
        }
        //最后改成之前一样的文件名
        optFile.renameTo(rawFile)
    }


    static byte[] actuallyInsertCode(InputStream inputStream) {
        ClassReader cr = new ClassReader(inputStream);
        ClassWriter cw = new ClassWriter(cr, 0);
        InsertClassVisitor cv = new InsertClassVisitor(Opcodes.ASM5, cw);
        cr.accept(cv, ClassReader.EXPAND_FRAMES);
        return cw.toByteArray()
    }

    //todo  先定位到LogisticsCenter的loadRouteMap方法
    static class InsertClassVisitor extends ClassVisitor {

        InsertClassVisitor(int api) {
            super(api)
        }

        InsertClassVisitor(int api, ClassVisitor classVisitor) {
            super(api, classVisitor)
        }

        @Override
        MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
            MethodVisitor mv = super.visitMethod(access, name, descriptor, signature, exceptions);
            //todo 要先逮到方法loadRouteMap
            if (name == LOAD_ROUTE_MAP) {
                return new InsertMethodVisitor(Opcodes.ASM5, mv);
            }
            return mv;
        }
    }


    //todo   loadRouteMap()方法已经有一行方法体，那就相当于是在方法体尾部插入，我们可以判断Return来做
    static class InsertMethodVisitor extends MethodVisitor {
        InsertMethodVisitor(int api) {
            super(api)
        }

        InsertMethodVisitor(int api, MethodVisitor methodVisitor) {
            super(api, methodVisitor)
        }

        //todo loadRouteMap() 是一个空参方法，visitInsn(int opcode)是空参方法必走得方法
        @Override
        void visitInsn(int opcode) {
            if (opcode >= Opcodes.IRETURN && opcode <= Opcodes.RETURN) {
                AutoRegisterTransform.childrenForIRouterRoot.each {
                    String name = it.replaceAll("/", ".")
                    mv.visitLdcInsn(name);
                    mv.visitMethodInsn(Opcodes.INVOKESTATIC, "com/docwei/arouter_api/LogisticsCenter",
                            "register", "(Ljava/lang/String;)V", false);
                }
                AutoRegisterTransform.childrenForIProviderGroup.each {
                    String name = it.replaceAll("/", ".")
                    mv.visitLdcInsn(name);
                    mv.visitMethodInsn(Opcodes.INVOKESTATIC, "com/docwei/arouter_api/LogisticsCenter",
                            "register", "(Ljava/lang/String;)V", false);

                }
                AutoRegisterTransform.childrenForIInterceptorGroup.each {
                    String name = it.replaceAll("/", ".")
                    mv.visitLdcInsn(name);
                    mv.visitMethodInsn(Opcodes.INVOKESTATIC, "com/docwei/arouter_api/LogisticsCenter",
                            "register", "(Ljava/lang/String;)V", false);
                }
                AutoRegisterTransform.childrenForIAutoWirdGroup.each {
                    String name = it.replaceAll("/", ".")
                    mv.visitLdcInsn(name);
                    mv.visitMethodInsn(Opcodes.INVOKESTATIC, "com/docwei/arouter_api/LogisticsCenter",
                            "register", "(Ljava/lang/String;)V", false);
                }
            }
            super.visitInsn(opcode)
        }

        @Override
        void visitMaxs(int maxStack, int maxLocals) {
            super.visitMaxs(maxStack + 4, maxLocals)
        }
    }
}












