package cn.kduck.core.web;

import cn.kduck.core.exception.KduckException;
import cn.kduck.core.remote.exception.RemoteException;
import cn.kduck.core.remote.service.RemoteCircuitBreaker;
import cn.kduck.core.web.json.JsonObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.web.servlet.error.AbstractErrorController;
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorViewResolver;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collections;
import java.util.List;
import java.util.Map;


@Controller
@RequestMapping("${server.error.path:${error.path:/error}}")
public class GlobalErrorController extends AbstractErrorController {

	public static final String GLOBAL_ERROR_MESSAGE = "GLOBAL_ERROR_MESSAGE";
	public static final String GLOBAL_ERROR_CODE = "GLOBAL_ERROR_CODE";

	private ErrorAttributes errorAttributes;

	@Autowired(required = false)
	private RemoteCircuitBreaker remoteCircuitBreaker;

	public GlobalErrorController(ErrorAttributes errorAttributes, List<ErrorViewResolver> errorViewResolvers) {
		super(errorAttributes,errorViewResolvers);
		this.errorAttributes = errorAttributes;
	}
	
	@RequestMapping(produces = "text/html")
	public ModelAndView errorHtml(HttpServletRequest request,
			HttpServletResponse response) {
		HttpStatus status = getStatus(request);
		Map<String, Object> model = Collections.unmodifiableMap(getErrorAttributes(
				request, ErrorAttributeOptions.defaults()));
		response.setStatus(status.value());
		ModelAndView modelAndView = resolveErrorView(request, response, status, model);
		return (modelAndView == null ? new ModelAndView("error", model) : modelAndView);
	}
	
	@RequestMapping
	@ResponseBody
	public JsonObject errorJson(HttpServletRequest request) {

		Throwable error = errorAttributes.getError(new ServletWebRequest(request));
		if(error instanceof KduckException){
			//TODO
		}

		if(error instanceof RemoteException && remoteCircuitBreaker != null){
			return remoteCircuitBreaker.fallback((RemoteException)error);
		}

		Map<String, Object> body = getErrorAttributes(request, ErrorAttributeOptions.defaults());
		HttpStatus status = getStatus(request);
		ResponseEntity<Map<String, Object>> responseEntity = new ResponseEntity<Map<String, Object>>(body, status);

		Object message = request.getAttribute(GLOBAL_ERROR_MESSAGE);
		if(message == null){
			message = body.get("message");
		}
		if(message == null){
			message = responseEntity.toString();
		}

		Integer errorCode = (Integer)request.getAttribute(GLOBAL_ERROR_CODE);
		if(errorCode == null){
			errorCode = -1;
		}

		return new JsonObject(responseEntity.getBody(),errorCode,message.toString());
	}

}
