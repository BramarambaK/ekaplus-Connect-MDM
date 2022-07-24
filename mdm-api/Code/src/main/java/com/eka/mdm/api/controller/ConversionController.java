package com.eka.mdm.api.controller;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.owasp.esapi.ESAPI;
import org.owasp.esapi.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import com.eka.mdm.api.exception.ConnectException;
import com.eka.mdm.api.exception.DataException;
import com.eka.mdm.api.util.CommonService;
import com.eka.mdm.api.util.ContextProvider;
import com.eka.mdm.dataobject.ApplicationProps;
import com.eka.mdm.dataobject.RequestContext;

@RestController
public class ConversionController {

	final static Logger logger = ESAPI.getLogger(ConversionController.class);
	
	@Autowired
	public RestTemplate restTemplate;
	@Autowired
	public CommonService commonService;
	@Autowired
	private ContextProvider contextProvider;
	
	/**
	 * 
	 * Serves the request for getting currecnyCode,currencyName,conversionFactor 
	 * and to check whether it's suCurrency or not based on currencyCode as a part
	 * of request body.
	 * 
	 * Sample request { "currencyCode":"UDF" }
	 * 
	 * 
	 * @return
	 */
	
	@PostMapping(value = "/{uuid}/currency-details")
	public Map<String, String> getCurrencyDetails(@PathVariable String uuid,
			HttpServletRequest request,@RequestBody String reqestBodyStr) {

		logger.debug(Logger.EVENT_SUCCESS,
				ESAPI.encoder().encodeForHTML("Inside POST api of currency/details/" + reqestBodyStr));

		HttpEntity<String> requestBody = new HttpEntity<String>(reqestBodyStr,
				commonService.getHttpHeader(request));

		ApplicationProps applicationProps = contextProvider.getCurrentContext().getApplicationProps();
		Map<String, String> response = new HashMap<>();
		
		try {

			URI url = new URI(applicationProps.getMdmEndPoint() + "/currency-details");

			logger.debug(Logger.EVENT_SUCCESS, ESAPI.encoder()
					.encodeForHTML("Making POST call to TRM at endpoint: " + url + " with HttpEntity: " + requestBody));
			ResponseEntity<Map> resultResp = restTemplate.exchange(url, HttpMethod.POST, requestBody, Map.class);

			logger.debug(Logger.EVENT_SUCCESS, ESAPI.encoder()
					.encodeForHTML("Making POST call to TRM at endpoint: completed " + url + " is: " + resultResp));
			if (resultResp.getStatusCodeValue() == HttpStatus.OK.value()) {
				response = resultResp.getBody();
			}

		} catch (HttpStatusCodeException e) {
			logger.error(Logger.EVENT_FAILURE, ESAPI.encoder().encodeForHTML(
					"Inside getCurrencyDetails, failed to fetch CurrencyDetails. Reason: " + e.getMessage()));
			throw new ConnectException("Failed to fetch CurrencyDetails. Reason: " + e.getResponseBodyAsString());

		} catch (URISyntaxException e) {
			logger.error(Logger.EVENT_FAILURE, ESAPI.encoder().encodeForHTML(
					"Inside getCurrencyDetails, Unable to form CurrencyDetails URI. Reason: " + e.getMessage()));
			throw new DataException("Unable to form CurrencyDetails URI. Reason: " + e.getMessage());

		}

		return response;
	}
	
	
	
}
