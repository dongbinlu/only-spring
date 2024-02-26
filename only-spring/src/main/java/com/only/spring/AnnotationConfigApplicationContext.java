package com.only.spring;

import java.beans.Introspector;
import java.io.File;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AnnotationConfigApplicationContext {

    private Class configClass;

    private Map<String, BeanDefinition> beanDefinitionMap = new HashMap<>();

    private Map<String, Object> singletonObjects = new HashMap<>();

    private List<BeanPostProcessor> beanPostProcessorList = new ArrayList<>();

    public AnnotationConfigApplicationContext(Class configClass) {
        this.configClass = configClass;

        // 1 扫描bean定义
        scan(configClass);

        // 2 创建bean
        for (Map.Entry<String, BeanDefinition> entry : beanDefinitionMap.entrySet()) {
            String beanName = entry.getKey();
            BeanDefinition beanDefinition = entry.getValue();
            if (beanDefinition.getScope().equals("singleton")) {
                Object bean = createBean(beanName, beanDefinition);
                singletonObjects.put(beanName, bean);
            }
        }

    }


    public Object getBean(String beanName) {
        if (!beanDefinitionMap.containsKey(beanName)) {
            throw new NullPointerException();
        }
        BeanDefinition beanDefinition = beanDefinitionMap.get(beanName);
        if (beanDefinition.getScope().equals("singleton")) {
            Object singletonBean = singletonObjects.get(beanName);
            if (singletonBean == null) {
                singletonBean = createBean(beanName, beanDefinition);
                singletonObjects.put(beanName, singletonBean);
            }
            return singletonBean;
        } else {
            // 原型bean,也就是prototype多例bean
            return createBean(beanName, beanDefinition);
        }

    }

    private Object createBean(String beanName, BeanDefinition beanDefinition) {

        Class clazz = beanDefinition.getType();
        Object instance = null;
        try {
            instance = clazz.getConstructor().newInstance();
            for (Field field : clazz.getDeclaredFields()) {
                if (field.isAnnotationPresent(Autowired.class)) {
                    field.setAccessible(true);
                    field.set(instance, getBean(field.getName()));
                }
            }

            if (instance instanceof BeanNameAware) {
                ((BeanNameAware) instance).setBeanName(beanName);
            }

            for (BeanPostProcessor beanPostProcessor : beanPostProcessorList) {
                instance = beanPostProcessor.postProcessBeforeInitialization(instance, beanName);
            }

            if (instance instanceof InitializingBean) {
                ((InitializingBean) instance).afterPropertiesSet();
            }

            for (BeanPostProcessor beanPostProcessor : beanPostProcessorList) {
                instance = beanPostProcessor.postProcessAfterInitialization(instance, beanName);
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return instance;
    }

    /**
     * configClass.isAnnotationPresent(ComponentScan.class)
     * 这个方法用于检查类是否被指定类型的注解修饰
     * 参数是Class对象，表示要检查的注解类型
     * 如果该类上有被指定类型的注解，则返回true，否则返回false
     *
     * configClass.isAnnotation()
     * 这个方法用于检查类是否有任何注解修饰
     * 如果该类上有任何注解，则返回true，否则返回false
     * 不需要指定具体的注解类型，只是检查是否有任何注解
     */
    private void scan(Class configClass) {
        if (!configClass.isAnnotationPresent(ComponentScan.class)) {
            throw new NullPointerException("ComponentScan Annotation is not found");
        }

        ComponentScan componentScanAnnotation = (ComponentScan) configClass.getAnnotation(ComponentScan.class);
        String path = componentScanAnnotation.value();
        path = path.replace(".", "/");

        ClassLoader classLoader = AnnotationConfigApplicationContext.class.getClassLoader();
        URL resource = classLoader.getResource(path);
        File file = new File(resource.getFile());
        if (!file.isDirectory()) {
            throw new NullPointerException("class file is not found");
        }

        for (File f : file.listFiles()) {
            String absolutePath = f.getAbsolutePath();
            absolutePath = absolutePath.substring(absolutePath.indexOf("com"), absolutePath.indexOf(".class"));
            absolutePath = absolutePath.replace("/", ".");

            try {
                Class<?> clazz = classLoader.loadClass(absolutePath);
                if (clazz.isAnnotationPresent(Component.class)) {
                    // 判断clazz是否是BeanPostProcessor实现类
                    if (BeanPostProcessor.class.isAssignableFrom(clazz)) {
                        BeanPostProcessor instance = (BeanPostProcessor) clazz.getConstructor().newInstance();
                        beanPostProcessorList.add(instance);
                    }


                    Component componentAnnotation = clazz.getAnnotation(Component.class);

                    String beanName = componentAnnotation.value();
                    if (beanName.equals("")) {
                        beanName = Introspector.decapitalize(clazz.getSimpleName());
                    }

                    BeanDefinition beanDefinition = new BeanDefinition();
                    beanDefinition.setType(clazz);

                    if (clazz.isAnnotationPresent(Scope.class)) {
                        Scope scopeAnnotation = clazz.getAnnotation(Scope.class);
                        String value = scopeAnnotation.value();
                        beanDefinition.setScope(value);

                    } else {
                        beanDefinition.setScope("singleton");
                    }

                    beanDefinitionMap.put(beanName, beanDefinition);
                }

            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

    }
}
