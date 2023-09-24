package cn.kduck.core.dao.datasource.condition;

import cn.kduck.core.dao.datasource.DataSourceMatcher;
import jakarta.servlet.http.HttpServletRequest;

public class RequestMethodMatcher implements DataSourceMatcher<HttpServletRequest> {


    private final String[] method;

    public RequestMethodMatcher(String[] method){
        this.method = method;
    }

    @Override
    public boolean supports(Class cls) {
        return HttpServletRequest.class.isAssignableFrom(cls);
    }

    @Override
    public boolean match(HttpServletRequest request) {
        boolean match = false;
        for (String m : method) {
            if(m.toUpperCase().equals(request.getMethod())){
                match = true;
                break;
            }
        }
        return match;
    }
}
