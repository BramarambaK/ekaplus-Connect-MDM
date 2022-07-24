package com.eka.mdm.api.controller;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.owasp.esapi.ESAPI;
import org.owasp.esapi.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import com.eka.mdm.api.exception.ConnectException;
import com.eka.mdm.api.exception.DataException;
import com.eka.mdm.api.util.CommonService;
import com.eka.mdm.api.util.CommonValidator;
import com.eka.mdm.api.util.ContextProvider;
import com.eka.mdm.dataobject.ApplicationProps;
import com.eka.mdm.dataobject.RequestContext;

/**
 * <p>
 * <code>ProductController</code> Controller which exposes end points to fetch
 * the master data related to a product from CTRM MDM based on productId (might
 * need some other attributes).
 * <p>
 * <hr>
 * 
 * @author Ranjan.Jha
 * @version 1.0
 */

@RestController
@RequestMapping("/product")
public class ProductController {

	final static Logger logger = ESAPI.getLogger(ProductController.class);

	@Autowired
	public RestTemplate restTemplate;
	@Autowired
	public CommonService commonService;
	@Autowired
	private ContextProvider contextProvider;
	@Autowired
	CommonValidator validator;

	/**
	 * 
	 * Serves the request for quality spec based on ProductId and QualityId as part
	 * of request body.
	 * 
	 * Sample request { "productId": "PDM-M0-10200", "qualityId": "PPM-22520" }
	 * 
	 * @param reqParam
	 * @return
	 */

	@GetMapping(value = "/quality/{uuid}/{qualityId}")
	public List<Object> getProductQualitySpec(@PathVariable String uuid, @PathVariable String qualityId,
			HttpServletRequest request) {

		logger.debug(Logger.EVENT_SUCCESS,
				ESAPI.encoder().encodeForHTML("Inside GET api of product/quality/" + qualityId));

		HttpEntity<?> entity = new HttpEntity<>(commonService.getHttpHeader(request, true));
		List<Object> qualitySpec = new ArrayList<>();

		ApplicationProps applicationProps = contextProvider.getCurrentContext().getApplicationProps();

		try {

			URI url = new URI(validator.cleanData(applicationProps.getProductEndpoint() + "/quality/" + qualityId));

			logger.debug(Logger.EVENT_SUCCESS, ESAPI.encoder()
					.encodeForHTML("Making GET call to TRM at endpoint: " + url + " with HttpEntity: " + entity));
			ResponseEntity<List> resultResp = restTemplate.exchange(url, HttpMethod.GET, entity, List.class);

			logger.debug(Logger.EVENT_SUCCESS, ESAPI.encoder()
					.encodeForHTML("Making GET call to TRM at endpoint: completed " + url + " is: " + resultResp));
			if (resultResp.getStatusCodeValue() == HttpStatus.OK.value()) {
				qualitySpec = resultResp.getBody();
			}

		} catch (HttpStatusCodeException e) {
			logger.error(Logger.EVENT_FAILURE, ESAPI.encoder().encodeForHTML(
					"Inside getProductQualitySpec, failed to fetch qualitySpec. Reason: " + e.getMessage()));
			throw new ConnectException("Failed to fetch qualitySpec. Reason: " + e.getResponseBodyAsString());

		} catch (URISyntaxException e) {
			logger.error(Logger.EVENT_FAILURE, ESAPI.encoder().encodeForHTML(
					"Inside getProductQualitySpec, Unable to form Quality URI. Reason: " + e.getMessage()));
			throw new DataException("Unable to form Quality URI. Reason: " + e.getMessage());

		}

		return qualitySpec;
	}
	/**
	 * 
	 * Serves the request for product attributes based on filter attributes 
	 * 
	 * @param reqParam
	 * @return
	 */

	@PostMapping(value = "/{uuid}/attributes")
	public List<Object> getProductAttributes(@PathVariable String uuid,
			@RequestBody String reqestBodyStr, HttpServletRequest request) {

		logger.debug(
				Logger.EVENT_SUCCESS,
				ESAPI.encoder().encodeForHTML(
						"Inside POST api of product/atributes/" + reqestBodyStr));

		HttpEntity<String> entity = new HttpEntity<String>(reqestBodyStr,
				commonService.getHttpHeader(request));
		List<Object> productAttributes = new ArrayList<>();

		ApplicationProps applicationProps = contextProvider.getCurrentContext()
				.getApplicationProps();

		try {

			URI url = new URI(applicationProps.getProductEndpoint()
					+ "/attributes");

			logger.debug(
					Logger.EVENT_SUCCESS,
					ESAPI.encoder().encodeForHTML(
							"Making POST call to TRM at endpoint: " + url
									+ " with HttpEntity: " + entity));
			ResponseEntity<List> resultResp = restTemplate.exchange(url,
					HttpMethod.POST, entity, List.class);

			logger.debug(
					Logger.EVENT_SUCCESS,
					ESAPI.encoder().encodeForHTML(
							"Making POST call to TRM at endpoint: completed " + url
									+ " is: " + resultResp));
			if (resultResp.getStatusCodeValue() == HttpStatus.OK.value()) {
				productAttributes = resultResp.getBody();
			}

		} catch (HttpStatusCodeException e) {
			logger.error(
					Logger.EVENT_FAILURE,
					ESAPI.encoder().encodeForHTML(
							"Inside getProductAttributes, failed to fetch product attributes. Reason: "
									+ e.getMessage()));
			throw new ConnectException(
					"Failed to fetch product attributes. Reason: "
							+ e.getResponseBodyAsString());

		} catch (URISyntaxException e) {
			logger.error(
					Logger.EVENT_FAILURE,
					ESAPI.encoder().encodeForHTML(
							"Inside getProductAttributes, Unable to form product attributes URI. Reason: "
									+ e.getMessage()));
			throw new DataException(
					"Unable to form Product attributes URI. Reason: "
							+ e.getMessage());

		}

		return productAttributes;
	}

}
