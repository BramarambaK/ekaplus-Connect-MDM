package com.eka.mdm.dataobject;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONObject;
import org.springframework.stereotype.Component;

@Component
public class RequestContext {

	private TokenData tokenData;

	private ApplicationProps applicationProps;

	private JSONObject serviceKeyMap;
	
	private HttpServletRequest request;

	private String requestId;
	
	public TokenData getTokenData() {
		return tokenData;
	}

	public void setTokenData(TokenData tokenData) {
		this.tokenData = tokenData;
	}

	public ApplicationProps getApplicationProps() {
		return applicationProps;
	}

	public void setApplicationProps(ApplicationProps applicationProps) {
		this.applicationProps = applicationProps;
	}

	public JSONObject getServiceKeyMap() {
		return serviceKeyMap;
	}

	public void setServiceKeyMap(JSONObject serviceKeyMap) {
		this.serviceKeyMap = serviceKeyMap;
	}

	public HttpServletRequest getRequest() {
		return request;
	}

	public void setRequest(HttpServletRequest request) {
		this.request = request;
	}

	public String getRequestId() {
		return requestId;
	}

	public void setRequestId(String requestId) {
		this.requestId = requestId;
	}
}
