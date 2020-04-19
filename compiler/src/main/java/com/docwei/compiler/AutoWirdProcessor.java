package com.docwei.compiler;

import com.docwei.annotation.AutoWird;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.WildcardTypeName;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
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
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

import static com.docwei.compiler.Consts.AUTOWIRD;
import static com.docwei.compiler.Consts.NAME_OF_AUTOWIRD;
import static com.docwei.compiler.Consts.NAME_OF_INTERCEPTOR;
import static com.docwei.compiler.Consts.PACKAGE_OF_GENERATE_FILE;
import static com.docwei.compiler.Consts.PROJECT;
import static com.docwei.compiler.Consts.SEPARATOR;


@SupportedSourceVersion(SourceVersion.RELEASE_8)
@SupportedOptions("AROUTER_MODULE_NAME")
@SupportedAnnotationTypes("com.docwei.annotation.AutoWird")
public class AutoWirdProcessor extends AbstractProcessor {
    private Types mTypeUtils;
    private Messager mMessage;
    private Filer mFiler;
    private Map<String, String> mOptions;
    private Elements mElments;
    private Logger mLogger;
    public Map<TypeElement, Set<VariableElement>> autoWirds = new HashMap<>();
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
            Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(AutoWird.class);
            for (Element element : elements) {
                if (!(element instanceof VariableElement)) {
                    mLogger.e("Route只能注解到字段上");
                    return false;
                }
                VariableElement variableElement = (VariableElement) element;
                TypeElement typeElement = (TypeElement) variableElement.getEnclosingElement();
                Set<VariableElement> set = autoWirds.get(typeElement);
                if (set == null) {
                    set = new HashSet<>();
                    autoWirds.put(typeElement, set);
                }
                set.add(variableElement);

            }
            //要生成两个类
            createAutoWirdFile();
        }
        return false;
    }

    /*
     public class ARouter$$AutoWird$$SecondActivity implements IAutoWird {
      @Override
      public void inject(Object target) {
        SecondActivity substitute = (SecondActivity) target;
        substitute.name = substitute.getIntent().getStringExtra("name");
        substitute.sex = substitute.getIntent().getStringExtra("sex");
        substitute.money = substitute.getIntent().getLongExtra("money", 0);
    }
   }

     public class ARouter$$AutoWird$$app implements IAutoWirdGroup {
        @Override
         public void loadInto(Map<String, Class<? extends IAutoWird>> warehouse) {
            warehouse.put("className",ARouter$$AutoWird$$Second.class);
         }
    }*/
    private void createAutoWirdFile() {
        //Map<String, Class<? extends IAutoWird>
        TypeElement iAutoWird = mElments.getTypeElement("com.docwei.arouter_api.autowird.IAutoWird");
        TypeElement iAutoWirdGroup = mElments.getTypeElement("com.docwei.arouter_api.template.IAutoWirdGroup");
        ParameterizedTypeName parameterizedType = ParameterizedTypeName.get(ClassName.get(Map.class), ClassName.get(String.class),
                ParameterizedTypeName.get(ClassName.get(Class.class), WildcardTypeName.subtypeOf(ClassName.get(iAutoWird))));
        ParameterSpec parameterSpec = ParameterSpec.builder(parameterizedType, "wareHouse").build();
        MethodSpec.Builder methodSpecBuilderForLoadInto = MethodSpec.methodBuilder("loadInto")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .addParameter(parameterSpec);

        // Object
        ParameterSpec parameterSpecObject = ParameterSpec.builder(Object.class, "target").build();
        MethodSpec.Builder methodSpecBuilderForInject = MethodSpec.methodBuilder("inject")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .addParameter(parameterSpecObject);

        TypeElement intentElement = mElments.getTypeElement("android.content.Intent");
        for (Map.Entry<TypeElement, Set<VariableElement>> entry : autoWirds.entrySet()) {
            TypeElement typeElement = entry.getKey();
            Set<VariableElement> elements = entry.getValue();
            // SecondActivity secondActivity = (SecondActivity) target;
            methodSpecBuilderForInject.addStatement("$T  substitute= ($T) target;", ClassName.get(typeElement), ClassName.get(typeElement))
                    .addStatement("$T intent=substitute.getIntent()", ClassName.get(intentElement));
            for (VariableElement variableElement : elements) {
                //substitute.name = substitute.getIntent().getStringExtra("name");
                //这里要判断类型
                //  switch (variableElement.asType().getKind().ordinal())
                addStatement(methodSpecBuilderForInject, variableElement);
            }
            //创建文件
            String autoWirdCn = PROJECT + SEPARATOR + typeElement.getSimpleName()+SEPARATOR +AUTOWIRD;
            try {
                JavaFile.builder(PACKAGE_OF_GENERATE_FILE,
                        TypeSpec.classBuilder(autoWirdCn).addSuperinterface(ClassName.get(iAutoWird))
                                .addMethod(methodSpecBuilderForInject.build()).addModifiers(Modifier.PUBLIC).build())
                        .build().writeTo(mFiler);

            } catch (IOException e) {
                e.printStackTrace();
            }
            //因为存在格式化的问题，这里用了四个 ---》$$$$
            methodSpecBuilderForLoadInto.addStatement(" wareHouse.put($S,"+"ARouter$$$$"+typeElement.getSimpleName()+"$$$$AutoWird.class)", typeElement.getQualifiedName());
        }

        String autoWirdGroupCn = NAME_OF_AUTOWIRD + SEPARATOR + mModuleName;
        try {
            JavaFile.builder(PACKAGE_OF_GENERATE_FILE,
                    TypeSpec.classBuilder(autoWirdGroupCn).addSuperinterface(ClassName.get(iAutoWirdGroup))
                            .addMethod(methodSpecBuilderForLoadInto.build()).addModifiers(Modifier.PUBLIC).build())
                    .build().writeTo(mFiler);

        } catch (IOException e) {
            e.printStackTrace();
        }







       /* for (Map.Entry<Integer, TypeElement> entry : mInterceptors.entrySet()) {
            Integer integer = entry.getKey();
            TypeElement element = entry.getValue();
            methodSpecBuilder.addStatement("wareHouse.put(" + integer + ", $T.class)", ClassName.get(element));

        }
        mLogger.d("开始创建Interceptor");
        if (mInterceptors.size() > 0) {
            TypeElement superElement = mElments.getTypeElement("com.docwei.arouter_api.template.IInterceptorGroup");
            String className = NAME_OF_INTERCEPTOR + SEPARATOR + mModuleName;
            try {
                JavaFile.builder(PACKAGE_OF_GENERATE_FILE,
                        TypeSpec.classBuilder(className).addSuperinterface(ClassName.get(superElement)).build())
                        .build().writeTo(mFiler);
                mLogger.d("创建Interceptor文件");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }*/

    }

   /* 注:  variableElement.asType().toString()-->double
    注:  variableElement.asType().toString()-->byte
    注:  variableElement.asType().toString()-->android.os.Parcelable
    注:  variableElement.asType().toString()-->java.lang.String
    注:  variableElement.asType().toString()-->int
    注:  variableElement.asType().toString()-->char
    注:  variableElement.asType().toString()-->long
    注:  variableElement.asType().toString()-->short
    注:  variableElement.asType().toString()-->java.lang.String
    注:  variableElement.asType().toString()-->java.io.Serializable*/


    /* name=intent.getStringExtra("name");
          price=intent.getLongExtra("price",0);
          mMyTestParcelBean=intent.getParcelableExtra("mMyTestParcelBean");
          mSerializableBean= (MyTestSerializableBean) intent.getSerializableExtra("mSerializableBean");
          score=intent.getIntExtra("score",0);
          goal=intent.getDoubleExtra("goal",0);*/
    public void addStatement(MethodSpec.Builder builder, VariableElement variableElement) {
        String type = variableElement.asType().toString();
        switch (type) {
            case "double":
                builder.addStatement("substitute." + variableElement.getSimpleName() + " = intent.getDoubleExtra($S,0)", variableElement.getSimpleName());
                break;
            case "java.lang.String":
                builder.addStatement("substitute." + variableElement.getSimpleName() + " = intent.getStringExtra($S)", variableElement.getSimpleName());
                break;
            case "int":
                builder.addStatement("substitute." + variableElement.getSimpleName() + " = intent.getIntExtra($S,0)", variableElement.getSimpleName());
                break;
            case "long":
                builder.addStatement("substitute." + variableElement.getSimpleName() + " = intent.getLongExtra($S,0)", variableElement.getSimpleName());
                break;
            default:
                //判断类型是不是Serializable 或者Parcelable的子类
                TypeElement parcelable = mElments.getTypeElement("android.os.Parcelable");
                TypeElement serializable = mElments.getTypeElement("java.io.Serializable");
                if (mTypeUtils.isSubtype(variableElement.asType(), parcelable.asType())) {
                    //走android.os.Parcelable
                    builder.addStatement("substitute." + variableElement.getSimpleName() + " = intent.getParcelableExtra($S)", variableElement.getSimpleName());

                } else if (mTypeUtils.isSubtype(variableElement.asType(), serializable.asType())) {
                    builder.addStatement("substitute." + variableElement.getSimpleName() + " = ($T) intent.getSerializableExtra($S)", ClassName.get(variableElement.asType()), variableElement.getSimpleName());
                } else {
                    mLogger.e("未知类型");
                }

                break;

        }

    }

}
