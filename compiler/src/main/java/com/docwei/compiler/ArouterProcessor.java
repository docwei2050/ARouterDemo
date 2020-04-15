package com.docwei.compiler;

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

import static com.docwei.compiler.Consts.NAME_OF_GROUP;
import static com.docwei.compiler.Consts.NAME_OF_ROOT;
import static com.docwei.compiler.Consts.PACKAGE_OF_GENERATE_FILE;
import static com.docwei.compiler.Consts.SEPARATOR;

@SupportedAnnotationTypes("com.docwei.annotation.Route")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@SupportedOptions("AROUTER_MODULE_NAME")
public class ArouterProcessor extends AbstractProcessor {

    private Types mTypeUtils;
    private Messager mMessage;
    private Filer mFiler;
    private Map<String, String> mOptions;
    private Elements mElments;
    private Logger mLogger;
    private Map<String, Set<RouteMeta>> routes = new HashMap<>();
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
                if (!routes.containsKey(group)) {
                    Set<RouteMeta> sets = new HashSet<>();
                    routes.put(group, sets);
                }
                Set<RouteMeta> sets = routes.get(group);
                RouteMeta routeMeta = null;
                mLogger.d(((TypeElement) element).getQualifiedName().toString());
                routeMeta = new RouteMeta(path, group);
                routeMeta.setElement((TypeElement) element);
                sets.add(routeMeta);
            }
        }
        createFile(routes);
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
    }*/
    private void createFile(Map<String, Set<RouteMeta>> routes) {
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
                .get(ClassName.get(Map.class),ClassName.get(String.class),
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

        for (Map.Entry<String, Set<RouteMeta>> entry : routes.entrySet()) {
            String groupName = entry.getKey();
            Set<RouteMeta> routeMetas = entry.getValue();
            for (RouteMeta routeMeta : routeMetas) {
                loadIntoPath.addStatement("warehouse.put($S,new RouteMeta($S,$S,$T.class))",
                        routeMeta.getPath(), routeMeta.getPath(),routeMeta.getGroup(),ClassName.get(routeMeta.getElement()));
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
            loadIntoGroup.addStatement("warehouse.put($S,$T.class)",groupName,ClassName.get(PACKAGE_OF_GENERATE_FILE,className));

        }
        //创建文件
        String className = NAME_OF_ROOT + SEPARATOR + mModuleName;
        TypeElement irouteRoot=mElments.getTypeElement("com.docwei.arouter_api.template.IRouterRoot");
        try {
            JavaFile.builder(PACKAGE_OF_GENERATE_FILE,
                    TypeSpec.classBuilder(className).addSuperinterface(ClassName.get(irouteRoot))
                            .addMethod(loadIntoGroup.build()).addModifiers(Modifier.PUBLIC).build())
                    .build().writeTo(mFiler);

        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}


