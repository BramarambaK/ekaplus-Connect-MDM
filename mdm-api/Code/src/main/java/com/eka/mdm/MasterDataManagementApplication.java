package com.eka.mdm;

import java.net.SocketTimeoutException;
import java.util.Arrays;
import java.util.List;

import org.apache.http.client.HttpClient;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.impl.client.HttpClientBuilder;
import org.owasp.esapi.ESAPI;
import org.owasp.esapi.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import com.eka.mdm.http.HttpProperties;

@SpringBootApplication
@EnableAspectJAutoProxy
@EnableCaching
@ComponentScan(basePackages = "com.eka.mdm")
public class MasterDataManagementApplication {

	@Autowired
	HttpProperties httpProperties;
	
	private static final int RETRY_LIMIT=3;
	private static List<Class<? extends Exception>> retriesOn = Arrays.asList(HttpHostConnectException.class,
			ResourceAccessException.class, SocketTimeoutException.class, HttpServerErrorException.class);

	final static Logger logger = ESAPI.getLogger(MasterDataManagementApplication.class);
	
	public static void main(String[] args) {
		SpringApplication.run(MasterDataManagementApplication.class, args);
	}

	@Bean
	public RestTemplate restTemplate(RestTemplateBuilder restTemplateBuilder) {
		RestTemplate restTemplate = restTemplateBuilder.build();
		restTemplate.setRequestFactory(getClientHttpRequestFactory(
						httpProperties.getHttpConnectionTimeOut(),
						httpProperties.getHttpReadTimeOut()));
		return restTemplate;
	}
	
	// https://stackoverflow.com/questions/45713767/spring-rest-template-readtimeout
	public static ClientHttpRequestFactory getClientHttpRequestFactory(
			int httpConnectionTimeOut, int httpReadTimeOut) {
		HttpComponentsClientHttpRequestFactory clientHttpRequestFactory = new HttpComponentsClientHttpRequestFactory();
		// Connect timeout
		clientHttpRequestFactory.setConnectTimeout(httpConnectionTimeOut);

		// Read timeout
		clientHttpRequestFactory.setReadTimeout(httpReadTimeOut);
		
		HttpClient customHttpClient = HttpClientBuilder.create().setRetryHandler((exception, executionCount, context) -> {
			logger.info(Logger.EVENT_SUCCESS, "Inside Retry handler.. " + exception.getClass());

			if (executionCount > RETRY_LIMIT) {
				logger.info(Logger.EVENT_SUCCESS, "Retry exceeds max limit for " + exception.getClass());
				return false;
			}
			if (exception!=null && retriesOn.contains(exception.getClass())) {
				logger.info(Logger.EVENT_SUCCESS, "ATTEMPTING RETRY for ..." + exception.getClass());
				return true;
			}
			return false;
		}).build();
		
		clientHttpRequestFactory.setHttpClient(customHttpClient);
		
		return clientHttpRequestFactory;
	}
}
