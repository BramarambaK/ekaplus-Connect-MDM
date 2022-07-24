package com.eka.mdm.interceptor;

import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.owasp.esapi.ESAPI;
import org.owasp.esapi.Logger;
import org.slf4j.MDC;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.AsyncHandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import com.eka.mdm.api.exception.ConnectException;
import com.eka.mdm.api.util.CommonService;
import com.eka.mdm.api.util.CommonValidator;
import com.eka.mdm.api.util.ContextProvider;
import com.eka.mdm.dataobject.ApplicationProps;
import com.eka.mdm.dataobject.EnvirnomentProps;
import com.eka.mdm.dataobject.RequestContext;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

/**
 * <p>
 * <code>PropertyInterceptor</code> make Property API call and injects the same
 * into ApplicationProps.
 * <p>
 * <hr>
 * 
 * @author Ranjan.Jha
 * @version 1.0
 */

@Component
public class PropertyInterceptor implements AsyncHandlerInterceptor {

	@Autowired
	public RestTemplate restTemplate;
	@Autowired
	public CommonService commonService;
	@Autowired
	ApplicationProps applicationProps;
	@Autowired
	private ContextProvider contextProvider;
	@Autowired
	CommonValidator validator;

	@Value("${property.list}")
	private String propertyAPIEndpoint;

	final static Logger logger = ESAPI.getLogger(PropertyInterceptor.class);
	public static final String X_TENANT_ID = "X-TenantID";
	public static final String REGEX_DOT = "\\.";

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {
		RequestContext requestContext=new RequestContext();
		requestContext.setRequest(request);
		contextProvider.setCurrentContext(requestContext);
		//addRequestId(request);
		setTenantNameAndRequestIdToLog(request);
		String requestURI = request.getRequestURI();
		String requestMethod = request.getMethod();
		logger.info(Logger.EVENT_SUCCESS, "********* MDM-PreHandle Started......"+"Request Details: " + requestMethod + " " + requestURI);
		
		
		RequestResponseLogger.logRequest(request);
		setEnvirnomentProps(request);
	 
		return true;
	}

	@Override
	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
			@Nullable ModelAndView modelAndView) throws Exception {
		RequestResponseLogger.logResponseHeaders(response);
	}

	@Override
	public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
			throws Exception {
		
		String requestURI = validator.cleanData(request.getRequestURI());
		String requestMethod = validator.cleanData(request.getMethod());
		response.addHeader("requestId",contextProvider.getCurrentContext().getRequestId());
		if(ex!=null){
			RequestResponseLogger.logResponseHeaderDetails(response);
		}
		logger.info(Logger.EVENT_SUCCESS, "********* MDM User Request completed......"+"Request Details: " + requestMethod + " " + requestURI);
		MDC.clear();
		contextProvider.remove();
	}
	
	public void setEnvirnomentProps(HttpServletRequest request) throws Exception {
		
		HttpHeaders httpRequestHeaders = commonService.getHttpHeader(request, false);
		HttpEntity<String> requestBody = new HttpEntity<String>(new JsonObject().toString(), httpRequestHeaders);

		// Find uuid from url request and substitue it's value in propertyAPIEndpoint.
		String requestURL = request.getRequestURL().toString();
		String[] requestURLArr = requestURL.split("/");
		String propertyAPIEndpointNew = propertyAPIEndpoint.replace("{uuid}", requestURLArr[requestURLArr.length - 2]);

		try {

			// Call Property API

			logger.info(Logger.EVENT_SUCCESS, ESAPI.encoder()
					.encodeForHTML("Making a POST call to propertyAPI at endpoint: " + propertyAPIEndpointNew));

			ResponseEntity<String> responseEntity = restTemplate.exchange(validator.cleanData(propertyAPIEndpointNew), HttpMethod.POST,
					requestBody, String.class);

			if (HttpStatus.OK.equals(responseEntity.getStatusCode())) {

				String envPropsStr = responseEntity.getBody();

				Gson gson = new Gson();
				EnvirnomentProps envProps = gson.fromJson(envPropsStr, EnvirnomentProps.class);
				logger.debug(Logger.EVENT_SUCCESS,
						ESAPI.encoder().encodeForHTML("After response from property API: " + envProps.toString()));

				// Prepare ApplicationPros obj and set it
				BeanUtils.copyProperties(envProps, applicationProps);

				contextProvider.getCurrentContext().setApplicationProps(applicationProps);
				

			} else {
				logger.error(Logger.EVENT_FAILURE,
						ESAPI.encoder().encodeForHTML("MDM Properties names (keys) are not as per standard."));

				throw new ConnectException("MDM Properties names (keys) are not as per standard.");
			}

		} catch (RestClientException ex) {

			logger.error(Logger.EVENT_FAILURE, ESAPI.encoder().encodeForHTML("Failed to call Property API: " + ex));
			throw new ConnectException("Failed to call Property API: " + ex.getMessage());

		}
		
	}
	
	private void setTenantNameAndRequestIdToLog(HttpServletRequest request) {
		String requestId = null;
		String tenantName = null;
		if (null != request.getHeader("requestId")) {
			requestId = validator.cleanData(request.getHeader("requestId"));
		} else {
			requestId = UUID.randomUUID().toString().replace("-", "")+"-GEN";

		}

		if (null == request.getHeader(X_TENANT_ID)) {
			tenantName = validator.cleanData(request.getServerName());
			tenantName = tenantName.split(REGEX_DOT)[0];
		}
		else{
		tenantName = validator.cleanData(request.getHeader(X_TENANT_ID));
		}

		MDC.put("requestId", requestId);
		MDC.put("tenantName", tenantName);
		contextProvider.getCurrentContext().setRequestId(requestId);

	}
	
}
