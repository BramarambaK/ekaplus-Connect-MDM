package com.eka.mdm.interceptor;

import java.util.Arrays;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.owasp.esapi.ESAPI;
import org.owasp.esapi.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.AsyncHandlerInterceptor;
import org.springframework.web.servlet.HandlerInterceptor;

import com.eka.mdm.api.exception.ConnectException;
import com.eka.mdm.api.util.CommonValidator;
import com.eka.mdm.api.util.ContextProvider;
import com.eka.mdm.dataobject.ApplicationProps;
import com.eka.mdm.dataobject.RequestContext;
import com.eka.mdm.dataobject.TokenData;

@Component
public class RequestValidatorInterceptor implements AsyncHandlerInterceptor {

	@Autowired
	private ContextProvider contextProvider;
	
	@Autowired
	private RestTemplate restTemplate;
	@Autowired
	CommonValidator validator;

	final static Logger logger = ESAPI.getLogger(RequestValidatorInterceptor.class);

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {
		validateToken(request);
		return true;
	}

	public void validateToken(HttpServletRequest request) throws Exception {

		String token = validator.cleanData(null==contextProvider.getCurrentContext().getRequest().getHeader("Authorization")?request.getHeader("Authorization"):contextProvider.getCurrentContext().getRequest().getHeader("Authorization"));
		String uri = "";

		try {
			ApplicationProps applicationProps = contextProvider.getCurrentContext().getApplicationProps();
			uri = validator.cleanData(applicationProps.getAuthServerEndpoint() + token);
		} catch (NullPointerException ex) {
			throw new ConnectException("Platfor URL is not found");
		}

		try {

			if (!StringUtils.isEmpty(token)) {

				HttpHeaders headers = new HttpHeaders();
				headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
				HttpEntity<String> entity = new HttpEntity<String>("parameters", headers);

				logger.debug(Logger.EVENT_SUCCESS,
						ESAPI.encoder().encodeForHTML("Making a POST call to Authenticate at endpoint: " + uri));

				ResponseEntity<TokenData> response = restTemplate.exchange(uri, HttpMethod.POST, entity,
						TokenData.class);

				if (HttpStatus.OK.equals(response.getStatusCode())) {
					// setting token information in request context
					contextProvider.getCurrentContext().setTokenData(response.getBody());
				} else {
					throw new ConnectException("Unable to validate Token against: " + uri
							+ ". Your provided token information expired or not exists.");
				}
			}

		}
		catch(HttpStatusCodeException he){
			logger.error(Logger.EVENT_FAILURE, "failed to authenticate "+he.getResponseBodyAsString(), he);
			throw new ConnectException("Endpoint is not valid to validate Token against: " + uri);
		}
		catch (Exception ex) {
			logger.error(Logger.EVENT_FAILURE, "failed to authenticate "+ex.getLocalizedMessage(), ex);
			throw new ConnectException("Endpoint is not valid to validate Token against: " + uri);
		}

	}

	public void adduserToRequest(HttpServletRequest request) {
		if (contextProvider.getCurrentContext() != null) {
		}
	}
}
