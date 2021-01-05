package cn.kduck.core.utils;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.boot.autoconfigure.AutoConfigurationPackages;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScans;
import org.springframework.core.annotation.AnnotationUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class ScanPackageUtils {

    private ScanPackageUtils(){}

    public static List<String> getScanPackages(BeanFactory beanFactory) {
        List<String> packages = AutoConfigurationPackages.get(beanFactory);
        Class<?> mainClass = deduceMainApplicationClass();
        if(mainClass != null) {
            SpringBootApplication springBoot = AnnotationUtils.findAnnotation(mainClass, SpringBootApplication.class);
            if(springBoot != null){
                packages.addAll(Arrays.asList(springBoot.scanBasePackages()));
            }

            ComponentScan componentScan = AnnotationUtils.findAnnotation(mainClass, ComponentScan.class);
            if(componentScan != null){
                packages.addAll(Arrays.asList(componentScan.basePackages()));
            }
            ComponentScans componentScans = AnnotationUtils.findAnnotation(mainClass, ComponentScans.class);
            if(componentScans != null){
                ComponentScan[] scans = componentScans.value();
                for (ComponentScan scan : scans) {
                    packages.addAll(Arrays.asList(scan.basePackages()));
                }
            }
            packages = reorganizePackages(packages);
        }

        return packages;
    }

    public static List<String> reorganizePackages(List<String> packages){
        List<String> reformatList = new ArrayList();

        for (String p : packages) {
            boolean skip = false;
            for (String fp : reformatList) {
                if(fp.startsWith(p)){
                    reformatList.remove(fp);
                    break;
                }else if(p.startsWith(fp)){
                    skip = true;
                    break;
                }
            }
            if(!skip){
                reformatList.add(p);
            }
        }

        return reformatList;
    }

    public static Class<?> deduceMainApplicationClass() {
        try {
            StackTraceElement[] stackTrace = new RuntimeException().getStackTrace();
            for (StackTraceElement stackTraceElement : stackTrace) {
                if ("main".equals(stackTraceElement.getMethodName())) {
                    return Class.forName(stackTraceElement.getClassName());
                }
            }
        }
        catch (ClassNotFoundException ex) {
            // Swallow and continue
        }
        return null;
    }
}
