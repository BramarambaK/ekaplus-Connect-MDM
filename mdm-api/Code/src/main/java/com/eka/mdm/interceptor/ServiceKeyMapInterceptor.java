
package com.eka.mdm.interceptor;

import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONException;
import org.json.JSONObject;
import org.owasp.esapi.ESAPI;
import org.owasp.esapi.Logger;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.servlet.AsyncHandlerInterceptor;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import com.eka.mdm.api.constants.GlobalConstants;
import com.eka.mdm.api.exception.ConnectException;
import com.eka.mdm.api.util.CommonService;
import com.eka.mdm.api.util.ContextProvider;
import com.eka.mdm.dataobject.ApplicationProps;
import com.eka.mdm.dataobject.RequestContext;

/**
 * <p>
 * <code>ServiceKeyMapInterceptor</code> make Meta API call and get the
 * serviceKey map json objects and set it into RequestContext.
 * <p>
 * <hr>
 * 
 * @author Ranjan.Jha
 * @version 1.0
 */

@Component
public class ServiceKeyMapInterceptor implements AsyncHandlerInterceptor {

	@Autowired
	public CommonService commonService;
	@Autowired
	private ContextProvider contextProvider;

	private static final String _SERVICE_KEY_MAP_sys__UUID = "951c3ad8-5088-4f14-b19c-a9d4bac1b5da";

	final static Logger logger = ESAPI.getLogger(ServiceKeyMapInterceptor.class);
	private static final String X_REQUEST_ID = "X-Request-Id";

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {
		addRequestId();
		setServiceKeyMap(request);
		return true;
	}

	@Override
	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
			@Nullable ModelAndView modelAndView) throws Exception {
		removeRequestId();
	}

	public void setServiceKeyMap(HttpServletRequest request) throws Exception {

		ApplicationProps applicationProps = contextProvider.getCurrentContext().getApplicationProps();
		String connectMetaEndPoint = applicationProps.getObjectMetaEndpoint() + _SERVICE_KEY_MAP_sys__UUID;

		try {

			// Call Meta API
			logger.debug(Logger.EVENT_SUCCESS,
					ESAPI.encoder().encodeForHTML("Making a GET call to Meta-API at endpoint " + connectMetaEndPoint));

			ResponseEntity<String> metaResponseEntity = commonService.fetchMeta(connectMetaEndPoint, request);

			if (HttpStatus.OK.equals(metaResponseEntity.getStatusCode())) {

				String serviceKeyMetaObj = metaResponseEntity.getBody();
				JSONObject serviceKeyMap = new JSONObject(serviceKeyMetaObj).getJSONObject("fields");

				logger.debug(Logger.EVENT_SUCCESS, ESAPI.encoder().encodeForHTML("Received Response from Meta-API"));

				contextProvider.getCurrentContext().setServiceKeyMap(serviceKeyMap);

			}

		} catch (RestClientException ex) {

			logger.error(Logger.EVENT_FAILURE, ESAPI.encoder().encodeForHTML("Failed to call Meta API: " + ex));

			throw new ConnectException(ex.getMessage());
		} catch (JSONException ex) {

			logger.error(Logger.EVENT_FAILURE,
					ESAPI.encoder().encodeForHTML("Unable to parse Meta-API Response: " + ex));

			throw new ConnectException("Unable to parse Meta-API Response: " + ex.getMessage());
		}

	}

	private void addRequestId() {
		MDC.put(X_REQUEST_ID, UUID.randomUUID().toString());
		MDC.put(GlobalConstants.X_REQUEST_ID, UUID.randomUUID().toString());
	}

	private void removeRequestId() {
		MDC.remove(X_REQUEST_ID);
		MDC.remove(GlobalConstants.X_REQUEST_ID);
	}

}
