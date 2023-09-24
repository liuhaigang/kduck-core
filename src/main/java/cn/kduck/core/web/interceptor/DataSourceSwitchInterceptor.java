package cn.kduck.core.web.interceptor;

import cn.kduck.core.dao.datasource.DataSourceSwitch;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;

public class DataSourceSwitchInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if(DataSourceSwitch.isEnabled() && DataSourceSwitch.hasSwitchMatcher()){
            if(DataSourceSwitch.get() != null){
                DataSourceSwitch.remove();
            }
            String defaultDsName = DataSourceSwitch.getDefaultDsName();
            boolean matched = DataSourceSwitch.switchByCondition(request, defaultDsName);
            if(!matched){
                DataSourceSwitch.switchByCondition(handler, defaultDsName);
            }
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
