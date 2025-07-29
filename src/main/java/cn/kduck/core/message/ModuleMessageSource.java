package cn.kduck.core.message;

import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;
import org.springframework.lang.Nullable;

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Properties;

public class ModuleMessageSource extends ReloadableResourceBundleMessageSource implements ResourceLoaderAware {

    private static final String PROPERTIES_SUFFIX = ".properties";

    private ResourceLoader resourceLoader = new DefaultResourceLoader();

    @Override
    public void setResourceLoader(@Nullable ResourceLoader resourceLoader) {
        super.setResourceLoader(resourceLoader);
        this.resourceLoader = (resourceLoader != null ? resourceLoader : new DefaultResourceLoader());
    }

    @Override
    protected PropertiesHolder refreshProperties(String filename, PropertiesHolder propHolder) {

        Enumeration<URL> resourceFileUrls;
        try {
            resourceFileUrls = resourceLoader.getClassLoader().getResources(filename+ PROPERTIES_SUFFIX);
        } catch (IOException e) {
            throw new RuntimeException("获取国际化资源文件错误：" + filename);
        }
        while (resourceFileUrls.hasMoreElements()) {
            URL url = resourceFileUrls.nextElement();
            try {
                String file = url.toURI().toURL().toString();
                Properties oldProperties = propHolder.getProperties();
                propHolder = super.refreshProperties(file.substring(0,file.length() - PROPERTIES_SUFFIX.length()),propHolder);
                if(oldProperties != null){
                    Properties newProperties = propHolder.getProperties();
                    newProperties.putAll(oldProperties);
                }

            } catch (Exception e) {
                throw new RuntimeException("加载国际化文件时发生错误：" + url, e);
            }
        }

        return propHolder;

    }
}
