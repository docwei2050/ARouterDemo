package com.docwei.compiler;

import com.docwei.annotation.AutoWird;
import com.docwei.annotation.BizType;
import com.docwei.annotation.Interceptor;
import com.docwei.annotation.Route;
import com.docwei.annotation.RouteMeta;
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
import java.util.List;
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
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

import static com.docwei.compiler.Consts.AUTOWIRD;
import static com.docwei.compiler.Consts.NAME_OF_AUTOWIRD;
import static com.docwei.compiler.Consts.NAME_OF_GROUP;
import static com.docwei.compiler.Consts.NAME_OF_INTERCEPTOR;
import static com.docwei.compiler.Consts.NAME_OF_PROVIDER;
import static com.docwei.compiler.Consts.NAME_OF_ROOT;
import static com.docwei.compiler.Consts.PACKAGE_OF_GENERATE_FILE;
import static com.docwei.compiler.Consts.PROJECT;
import static com.docwei.compiler.Consts.SEPARATOR;

@SupportedAnnotationTypes({"com.docwei.annotation.Route", "com.docwei.annotation.Interceptor"})
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@SupportedOptions("AROUTER_MODULE_NAME")
public class ArouterProcessor extends AbstractProcessor {

    private Types mTypeUtils;
    private Messager mMessage;
    private Filer mFiler;
    private Map<String, String> mOptions;
    private Elements mElments;
    private Logger mLogger;
    //总的
    private Map<String, Set<RouteMeta>> allRoutes = new HashMap<>();
    //provider用的
    private Set<RouteMeta> providerRoutes = new HashSet<>();
    //拦截器用
    public Map<Integer, TypeElement> mInterceptors = new HashMap<>();


    //autowird用
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
        if (annotations == null || annotations.size() == 0) {
            return false;
        }
        mModuleName = mOptions.get("AROUTER_MODULE_NAME");
        if (mModuleName == null) {
            mModuleName = "";
        }
        //代表页面跳转
        TypeMirror type_Activity = mElments.getTypeElement("android.app.Activity").asType();
        //代表提供数据--实例对象
        TypeMirror type_Provider = mElments.getTypeElement("com.docwei.arouter_api.data.IProvider").asType();

        Set<Element> elements = (Set<Element>) roundEnv.getElementsAnnotatedWith(Route.class);
        for (Element element : elements) {
            if (!(element instanceof TypeElement)) {
                mLogger.e("Route只能注解到类上");
                return false;
            } else {
                Route route = element.getAnnotation(Route.class);
                String path = route.path();
                if (path == null || path.length() == 0) {
                    mLogger.e("path不能为空");
                    return false;
                }
                if (!path.matches("^/[a-zA-Z0-9]*/[a-zA-Z0-9]*")) {
                    mLogger.e("path值不合法");
                    return false;
                }
                int index = path.lastIndexOf("/");
                String group = path.substring(1, index);
                TypeMirror typeMirror = element.asType();
                if (!allRoutes.containsKey(group)) {
                    Set<RouteMeta> sets = new HashSet<>();
                    allRoutes.put(group, sets);
                }
                Set<RouteMeta> sets = allRoutes.get(group);

                RouteMeta routeMeta = new RouteMeta(path, group);
                routeMeta.setElement((TypeElement) element);
                if (mTypeUtils.isSubtype(element.asType(), type_Activity)) {
                    routeMeta.setType(BizType.ROUTE_PAGE);
                } else if (mTypeUtils.isSubtype(typeMirror, type_Provider)) {
                    routeMeta.setType(BizType.IPROVIDER);
                    providerRoutes.add(routeMeta);
                }
                sets.add(routeMeta);


            }
        }
        createFile();


        Set<? extends Element> elementInterceptors = roundEnv.getElementsAnnotatedWith(Interceptor.class);
        for (Element element : elementInterceptors) {
            if (!(element instanceof TypeElement)) {
                mLogger.e("Route只能注解到类上 interceptor");
                return false;
            }
            Interceptor interceptor = element.getAnnotation(Interceptor.class);
            mInterceptors.put(interceptor.priority(), (TypeElement) element);
        }
        createInterceoptorFile();


        Set<? extends Element> elements2 = roundEnv.getElementsAnnotatedWith(AutoWird.class);
        for (Element element : elements2) {
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
        createAutoWirdFile();
        return false;
    }


    /*
    public class Route$$Group$$path implements IRouteGroup{
        @Override
        public void loadInto(Map<String, RouteMeta>  warehouse){
            warehouse.put(path1,new RouteMeta(path,group,destination));
            warehouse.put(path2,new RouteMeta(path,group,destination));
            warehouse.put(path3,new RouteMeta(path,group,destination));
        }
    }
    public class Route$$Root$$app implements IRouteRoot{
        @Override
        public void loadInto(Map<String, Class<?> extends IRouteGroup> warehouse){
            warehouse.put(Group1,Route$$Group$$path.class);
            warehouse.put(Group2,Route$$Group$$path.class);
        }
    }
    public class ARouter$$Provider$$app implements IRouterProvider {
      @Override
      public void loadInto(Map<String, RouteMeta> warehouse) {
        warehouse.put("/arouter/handler",new RouteMeta("/arouter/handler","arouter",InterceptorHandlerImpl.class, BizType.IPROVIDER) );
     }
    }
    */
    private void createFile() {
        mLogger.d("创建文件");
        //我们使用javaPoets，不用关心导包的问题
        //第一步：先构造方法的参数类型  Map<String, RouteMeta>
        ParameterizedTypeName map_string_routeMeta = ParameterizedTypeName
                .get(Map.class, String.class, RouteMeta.class);
        //Map<String, RouteMeta>  warehouse
        ParameterSpec warehouse_path = ParameterSpec.builder(map_string_routeMeta,
                "warehouse").build();

        //Map<String, IRouteGroup>
        // 这里访问不到IRouteGroup，怎么办---------------难点1
        //先拿到IRouteGroup的   Map<String, Class<?> extends IRouteGroup>
        TypeElement irouteGroup = mElments.getTypeElement("com.docwei.arouter_api.template.IRouterGroup");

        ParameterizedTypeName map_string_irouteGroup = ParameterizedTypeName
                .get(ClassName.get(Map.class), ClassName.get(String.class),
                        ParameterizedTypeName.get(ClassName.get(Class.class)
                                , WildcardTypeName.subtypeOf(ClassName.get(irouteGroup))));
        ParameterSpec warehouse_group = ParameterSpec.builder(map_string_irouteGroup,
                "warehouse").build();


        //第二步：方法参数都准备好了，遍历集合，去填充方法体
        MethodSpec.Builder loadIntoPath = MethodSpec.methodBuilder("loadInto")
                .addAnnotation(Override.class)
                .addParameter(warehouse_path)
                .addModifiers(Modifier.PUBLIC);


        MethodSpec.Builder loadIntoGroup = MethodSpec.methodBuilder("loadInto")
                .addAnnotation(Override.class)
                .addParameter(warehouse_group)
                .addModifiers(Modifier.PUBLIC);

        TypeElement bizTypeElement = mElments.getTypeElement("com.docwei.annotation.BizType");


        for (Map.Entry<String, Set<RouteMeta>> entry : allRoutes.entrySet()) {
            String groupName = entry.getKey();
            Set<RouteMeta> routeMetas = entry.getValue();
            for (RouteMeta routeMeta : routeMetas) {

                loadIntoPath.addStatement("warehouse.put($S,new RouteMeta($S,$S,$T.class,$T." + routeMeta.getType() + "))",
                        routeMeta.getPath(), routeMeta.getPath(), routeMeta.getGroup()
                        , ClassName.get(routeMeta.getElement()), ClassName.get(bizTypeElement));
            }

            //创建文件
            String className = NAME_OF_GROUP + SEPARATOR + groupName;
            try {
                JavaFile.builder(PACKAGE_OF_GENERATE_FILE,
                        TypeSpec.classBuilder(className).addSuperinterface(ClassName.get(irouteGroup))
                                .addMethod(loadIntoPath.build()).addModifiers(Modifier.PUBLIC).build())
                        .build().writeTo(mFiler);

            } catch (IOException e) {
                e.printStackTrace();
            }

            //构建root类的方法体
            loadIntoGroup.addStatement("warehouse.put($S,$T.class)", groupName, ClassName.get(PACKAGE_OF_GENERATE_FILE, className));

        }
        if (allRoutes.size() > 0) {
            //创建文件
            String className = NAME_OF_ROOT + SEPARATOR + mModuleName;
            TypeElement irouteRoot = mElments.getTypeElement("com.docwei.arouter_api.template.IRouterRoot");
            try {
                JavaFile.builder(PACKAGE_OF_GENERATE_FILE,
                        TypeSpec.classBuilder(className).addSuperinterface(ClassName.get(irouteRoot))
                                .addMethod(loadIntoGroup.build()).addModifiers(Modifier.PUBLIC).build())
                        .build().writeTo(mFiler);

            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        TypeElement iProvider = mElments.getTypeElement("com.docwei.arouter_api.data.IProvider");
        MethodSpec.Builder loadIntoForProvider = MethodSpec.methodBuilder("loadInto")
                .addAnnotation(Override.class)
                .addParameter(warehouse_path)
                .addModifiers(Modifier.PUBLIC);
        for (RouteMeta routeMeta : providerRoutes) {
            TypeElement element = routeMeta.getElement();
            //这里只有一层的关系,是注解类的直接父类
            List<? extends TypeMirror> interfaces = element.getInterfaces();
            for (TypeMirror tm : interfaces) {
                if (mTypeUtils.isSameType(tm, iProvider.asType())) {
                    loadIntoForProvider.addStatement("warehouse.put($S,new RouteMeta($S,$S,$T.class,$T." + routeMeta.getType() + "))",
                            routeMeta.getElement().toString(), routeMeta.getPath(), routeMeta.getGroup()
                            , ClassName.get(routeMeta.getElement()), ClassName.get(bizTypeElement));
                }
                if (mTypeUtils.isSubtype(tm, iProvider.asType())) {
                    loadIntoForProvider.addStatement("warehouse.put($S,new RouteMeta($S,$S,$T.class,$T." + routeMeta.getType() + "))",
                            tm.toString(), routeMeta.getPath(), routeMeta.getGroup()
                            , ClassName.get(routeMeta.getElement()), ClassName.get(bizTypeElement));

                }
            }
        }
        if (providerRoutes.size() > 0) {
            TypeElement irouteProvider = mElments.getTypeElement("com.docwei.arouter_api.template.IProviderGroup");

            //创建文件
            String providerCn = NAME_OF_PROVIDER + SEPARATOR + mModuleName;
            try {
                JavaFile.builder(PACKAGE_OF_GENERATE_FILE,
                        TypeSpec.classBuilder(providerCn).addSuperinterface(ClassName.get(irouteProvider))
                                .addMethod(loadIntoForProvider.build()).addModifiers(Modifier.PUBLIC).build())
                        .build().writeTo(mFiler);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void createInterceoptorFile() {
        //创建拦截器的新类

        //Map<Integer, Class<? extends IInterceptor>
        TypeElement iInterceptor = mElments.getTypeElement("com.docwei.arouter_api.interceptors.IInterceptor");
        ParameterizedTypeName parameterizedType = ParameterizedTypeName.get(ClassName.get(Map.class), ClassName.get(Integer.class),
                ParameterizedTypeName.get(ClassName.get(Class.class), WildcardTypeName.subtypeOf(ClassName.get(iInterceptor))));


        ParameterSpec parameterSpec = ParameterSpec.builder(parameterizedType, "wareHouse").build();
        MethodSpec.Builder methodSpecBuilder = MethodSpec.methodBuilder("loadInto")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .addParameter(parameterSpec);

        for (Map.Entry<Integer, TypeElement> entry : mInterceptors.entrySet()) {
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
                        TypeSpec.classBuilder(className).addSuperinterface(ClassName.get(superElement))
                                .addModifiers(Modifier.PUBLIC).addMethod(methodSpecBuilder.build()).build())
                        .build().writeTo(mFiler);
                mLogger.d("创建Interceptor文件");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

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


