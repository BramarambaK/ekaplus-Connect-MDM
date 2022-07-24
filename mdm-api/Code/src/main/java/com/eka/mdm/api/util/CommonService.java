package com.eka.mdm.api.util;

import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.eka.mdm.dataobject.RequestContext;
import com.eka.mdm.dataobject.TokenData;

@Service
public class CommonService {

	@Autowired
	ContextProvider contextProvider;

	@Autowired
	public RestTemplate restTemplate;
	@Autowired
	CommonValidator validator;

	/**
	 * Assign the attributes being passed from client as a header attributes.
	 * 
	 */

	public HttpHeaders getHttpHeader(HttpServletRequest request, boolean aadUser) {

		HttpHeaders headers = new HttpHeaders();

		Enumeration<?> names = request.getHeaderNames();

		while (names.hasMoreElements()) {

			String name = (String) names.nextElement();
			headers.add(name, validator.cleanData(request.getHeader(name)));
		}
		
		if(null==headers.get("requestId"))
			headers.add("requestId", MDC.get("requestId"));
		
		//if (aadUser)
			//addUserNameToHeaders(headers);

		return headers;

	}

	public HttpHeaders getHttpHeader(HttpServletRequest request) {

		HttpHeaders headers = new HttpHeaders();

		Enumeration<?> names = request.getHeaderNames();

		while (names.hasMoreElements()) {

			String name = (String) names.nextElement();
			headers.add(name, validator.cleanData(request.getHeader(name)));
		}

		return headers;

	}

	/*private void addUserNameToHeaders(HttpHeaders httpHeaders) {

		if (contextProvider.getCurrentContext() != null) {
			TokenData tokenData = contextProvider.getCurrentContext().getTokenData();
			httpHeaders.add("userName", validator.cleanData(tokenData.getUserName()));
		}

	}*/

	public ResponseEntity<String> fetchMeta(String connectMetaEndPoint, HttpServletRequest request) {

		HttpHeaders httpRequestHeaders = getHttpHeader(request);

		HttpEntity<String> httpEntity = new HttpEntity<String>("", httpRequestHeaders);

		ResponseEntity<String> responseEntity = null;

		try {
			responseEntity = restTemplate.exchange(validator.cleanData(connectMetaEndPoint), HttpMethod.GET, httpEntity, String.class);

		} catch (Exception e) {
			responseEntity = new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
			return responseEntity;
		}

		return responseEntity;
	}
}
