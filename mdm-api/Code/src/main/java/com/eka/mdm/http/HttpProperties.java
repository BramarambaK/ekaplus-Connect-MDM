package com.eka.mdm.http;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class HttpProperties {
	// defaults to 10 seconds
	@Value("#{${httpRequest.connectionTimeOut:10000}-${extraTime:2000}}")
	private int httpConnectionTimeOut;

	// defaults to 10 seconds
	@Value("#{${httpRequest.readTimeOut:10000}-${extraTime:2000}}")
	private int httpReadTimeOut;

	public int getHttpConnectionTimeOut() {
		return httpConnectionTimeOut;
	}

	public int getHttpReadTimeOut() {
		return httpReadTimeOut;
	}
}
