package com.docwei.compiler;

import com.docwei.annotation.Interceptor;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.WildcardTypeName;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedOptions;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

import static com.docwei.compiler.Consts.NAME_OF_INTERCEPTOR;
import static com.docwei.compiler.Consts.PACKAGE_OF_GENERATE_FILE;
import static com.docwei.compiler.Consts.SEPARATOR;

//这个类没用到，以为手动注册只能指定一个注解处理器
//自动注册autoservice 貌似不兼容


@SupportedSourceVersion(SourceVersion.RELEASE_8)
@SupportedOptions("AROUTER_MODULE_NAME")
@SupportedAnnotationTypes("com.docwei.annotation.Interceptor")
public class InterceptorProcessor extends AbstractProcessor {
    private Types mTypeUtils;
    private Messager mMessage;
    private Filer mFiler;
    private Map<String, String> mOptions;
    private Elements mElments;
    private Logger mLogger;
    public Map<Integer, TypeElement> mInterceptors = new HashMap<>();
    private String mModuleName;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        mTypeUtils = processingEnv.getTypeUtils();
        Messager message = processingEnv.getMessager();
        mFiler = processingEnv.getFiler();
        mOptions = processingEnv.getOptions();
        mElments = processingEnv.getElementUtils();
        mLogger = new Logger(message);
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        //目的是生成含有
        if (!annotations.isEmpty()) {
            mModuleName = mOptions.get("AROUTER_MODULE_NAME");
            Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(Interceptor.class);
            for (Element element : elements) {
                if (element instanceof TypeElement) {
                    mLogger.e("Route只能注解到类上");
                    return false;
                }
                Interceptor interceptor = element.getAnnotation(Interceptor.class);
                mInterceptors.put(interceptor.priority(), (TypeElement) element);
            }
            createFile();
        }
        return false;
    }

    /*   public class ARouter$$Interceptor$$app implements IInterceptorGroup {

           @Override
           public void loadInto(Map<Integer, Class<? extends IInterceptor>> wareHouse) {
               wareHouse.put(1, MyInterceptor.class);
           }
       }*/
    private void createFile() {
        //Map<Integer, Class<? extends IInterceptor>
        mLogger.e("拦截器");
        TypeElement iInterceptor=mElments.getTypeElement("com.docwei.arouter_api.interceptors.IInterceptor");
        ParameterizedTypeName parameterizedType=ParameterizedTypeName.get(Map.class,Integer.class,
                ParameterizedTypeName.get(ClassName.get(Class.class), WildcardTypeName.subtypeOf(iInterceptor.getClass())).getClass());
        ParameterSpec parameterSpec=ParameterSpec.builder(parameterizedType,"wareHouse").build();
        MethodSpec.Builder methodSpecBuilder=MethodSpec.methodBuilder("loadInto")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .addParameter(parameterSpec);

        for (Map.Entry<Integer, TypeElement> entry : mInterceptors.entrySet()) {
             Integer integer=entry.getKey();
             TypeElement element=entry.getValue();
             methodSpecBuilder.addStatement("wareHouse.put("+integer+", $T.class)",ClassName.get(element));

        }
        mLogger.d("开始创建Interceptor");
        if(mInterceptors.size()>0){
            TypeElement superElement=mElments.getTypeElement("com.docwei.arouter_api.template.IInterceptorGroup");
            String className=NAME_OF_INTERCEPTOR+SEPARATOR+mModuleName;
            try {
                JavaFile.builder(PACKAGE_OF_GENERATE_FILE,
                        TypeSpec.classBuilder(className).addSuperinterface(ClassName.get(superElement)).build())
                        .build().writeTo(mFiler);
                mLogger.d("创建Interceptor文件");
            }catch (Exception e){
                e.printStackTrace();
            }
        }

    }
}
