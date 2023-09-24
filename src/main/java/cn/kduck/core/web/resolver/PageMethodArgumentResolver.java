package cn.kduck.core.web.resolver;

import jakarta.servlet.ServletRequest;
import org.springframework.core.MethodParameter;
import org.springframework.util.Assert;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ServletModelAttributeMethodProcessor;
import cn.kduck.core.service.Page;

/**
 * LiuHG
 */
public class PageMethodArgumentResolver extends ServletModelAttributeMethodProcessor {

    private final int maxPageSize;

    public PageMethodArgumentResolver(int maxPageSize) {
        super(true);
        this.maxPageSize = maxPageSize;
    }

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.getParameterType() == Page.class;
    }

    @Override
    protected void bindRequestParameters(WebDataBinder binder, NativeWebRequest request) {
        ServletRequest servletRequest = request.getNativeRequest(ServletRequest.class);
        Assert.state(servletRequest != null, "No ServletRequest");
        ServletRequestDataBinder servletBinder = (ServletRequestDataBinder) binder;
        servletBinder.bind(servletRequest);

        Page page = (Page)servletBinder.getTarget();
        if(page.getPageSize() <= 0){
            page.setPageSize(Page.DEFAULT_PAGE_SIZE);
        }else if(page.getPageSize() > maxPageSize){
            page.setPageSize(maxPageSize);
        }
    }

}
