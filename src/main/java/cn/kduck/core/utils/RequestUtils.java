package cn.kduck.core.utils;

import cn.kduck.core.service.ValueMap;
import jakarta.servlet.http.HttpServletRequest;

import java.util.Iterator;
import java.util.Map;

public final class RequestUtils {

	private RequestUtils() {}
	
	public static ValueMap getParameterMap(HttpServletRequest request) {
		if(request == null){
			return ValueMap.EMPTY_VALUE_MAP;
		}
		Map<String, String[]> parameterMap = request.getParameterMap();
		ValueMap valueMap = new ValueMap();
		Iterator<String> keys = parameterMap.keySet().iterator();
		while(keys.hasNext()){
			String name = keys.next();
			String[] v = parameterMap.get(name);
			valueMap.put(name,v.length == 1 ? v[0]: v);
		}
		return valueMap;
	}

	public static boolean isAjax(HttpServletRequest request){
		return "XMLHttpRequest".equals(request.getHeader("X-Requested-With"));
	}
	
}
