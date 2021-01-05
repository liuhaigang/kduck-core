package cn.kduck.core.web.interceptor;

import cn.kduck.core.dao.datasource.DataSourceSwitch;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class DataSourceSwitchInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if(DataSourceSwitch.isEnabled() && DataSourceSwitch.hasSwitchMatcher()){
            if(DataSourceSwitch.get() != null){
                DataSourceSwitch.remove();
            }
            DataSourceSwitch.switchByCondition(request);
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        if(DataSourceSwitch.isEnabled()){
            DataSourceSwitch.remove();
        }
    }
}
