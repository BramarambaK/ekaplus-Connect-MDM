package com.eka.mdm.api.controller;

import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONException;
import org.json.JSONObject;
import org.owasp.esapi.ESAPI;
import org.owasp.esapi.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.eka.mdm.api.exception.ConnectException;
import com.eka.mdm.api.exception.DataException;
import com.eka.mdm.api.util.CommonService;
import com.eka.mdm.api.util.CommonValidator;
import com.eka.mdm.api.util.ContextProvider;
import com.eka.mdm.dataobject.ApplicationProps;
import com.eka.mdm.dataobject.CpDetails;
import com.eka.mdm.dataobject.DefaultPaymentTerm;
import com.eka.mdm.dataobject.ProductDetails;
import com.eka.mdm.dataobject.QualityDetails;

/**
 * <p>
 * <code>MasterDataController</code> Controller which exposes end points to fetch
 * the master data from CTRM MDM
 * <p>
 * <hr>
 * 
 * @author Rushikesh.Bhosale
 * @version 1.0
 */

@RestController
@RequestMapping("/masterdatas")
public class MasterDataController {


	final static Logger logger = ESAPI.getLogger(MasterDataController.class);
	
	@Autowired
	CommonValidator validator;

	@Autowired
	public RestTemplate restTemplate;
	@Autowired
	public CommonService commonService;
	@Autowired
	private ContextProvider contextProvider;

	private static final String PAYMENT_TERM_ID = "paymentTermId";
	private static final String INCO_TERM_ID = "incoTermId";
	private static final String CALENDAR_NAME = "calendarName";
	/**
	 * 
	 * Serves the request for product attributes based on ProductId 
	 * 
	 * @param reqParam
	 * @return
	 */

	@PostMapping(value = "/{uuid}/paymentterm")
	public Object getPaymentTermAttributes(@RequestBody Map<String, String> payload, HttpServletRequest request) {


		logger.debug(Logger.EVENT_SUCCESS,
				ESAPI.encoder().encodeForHTML("Inside POST api of paymentTerm/" + payload));

		String paymentTermId = validator.cleanData(payload.get(PAYMENT_TERM_ID));

		HttpEntity<?> entity = new HttpEntity<>(commonService.getHttpHeader(request, true));

		ApplicationProps applicationProps = contextProvider.getCurrentContext().getApplicationProps();

		Object response = new Object();

		try {
			URI url = new URI(validator.cleanData(applicationProps.getMdmMasterDataEndPoint()
					+ "/paymentterm/" + paymentTermId));

			logger.debug(
					Logger.EVENT_SUCCESS,
					ESAPI.encoder().encodeForHTML(
							"Making POST call to TRM at endpoint: " + url
							+ " with HttpEntity: " + entity));

			ResponseEntity<Object> resultResp = restTemplate.exchange(url,
					HttpMethod.POST, entity, Object.class);

			logger.debug(
					Logger.EVENT_SUCCESS,
					ESAPI.encoder().encodeForHTML(
							"Making POST call to TRM at endpoint: completed " + url
							+ " is: " + resultResp));
			if (resultResp.getStatusCodeValue() == HttpStatus.OK.value()) {

				response = resultResp.getBody();
			}

		} catch (HttpStatusCodeException e) {
			logger.error(
					Logger.EVENT_FAILURE,
					ESAPI.encoder().encodeForHTML(
							"Inside getPaymentTerms, failed to fetch payment attributes. Reason: "
									+ e.getMessage()));
			throw new ConnectException(
					"Failed to fetch payment attributes. Reason: "
							+ e.getResponseBodyAsString());

		} catch (URISyntaxException e) {
			logger.error(
					Logger.EVENT_FAILURE,
					ESAPI.encoder().encodeForHTML(
							"Inside getPaymentTerms, Unable to form payment attributes URI. Reason: "
									+ e.getMessage()));
			throw new DataException(
					"Unable to form getPaymentTerms URI. Reason: "
							+ e.getMessage());

		}

		return response;
	}



	/**
	 * 
	 * Serves the request for INCOTERM attributes based on incotermId 
	 * 
	 * @param reqParam
	 * @return
	 */

	@PostMapping(value = "/{uuid}/incoterm")
	public Object getIncoTermAttributes(@RequestBody Map<String, String> payload, HttpServletRequest request) {

		logger.debug(Logger.EVENT_SUCCESS,
				ESAPI.encoder().encodeForHTML("Inside POST api of incoTerm/" + payload));

		String incoTermId = validator.cleanData(payload.get(INCO_TERM_ID));
		HttpEntity<?> entity = new HttpEntity<>(commonService.getHttpHeader(request, true));

		ApplicationProps applicationProps = contextProvider.getCurrentContext().getApplicationProps();

		Object response = new Object();

		try {
			URI url = new URI(validator.cleanData(applicationProps.getMdmMasterDataEndPoint()
					+ "/incoterm/" + incoTermId));

			logger.debug(
					Logger.EVENT_SUCCESS,
					ESAPI.encoder().encodeForHTML(
							"Making POST call to TRM at endpoint: " + url
							+ " with HttpEntity: " + entity));

			ResponseEntity<Object> resultResp = restTemplate.exchange(url,
					HttpMethod.GET, entity, Object.class);

			logger.debug(
					Logger.EVENT_SUCCESS,
					ESAPI.encoder().encodeForHTML(
							"Making POST call to TRM at endpoint: completed" + url
							+ " is: " + resultResp));
			if (resultResp.getStatusCodeValue() == HttpStatus.OK.value()) {

				response = resultResp.getBody();
			}

		} catch (HttpStatusCodeException e) {
			logger.error(
					Logger.EVENT_FAILURE,
					ESAPI.encoder().encodeForHTML(
							"Inside getIncoTerms, failed to fetch incoterm attributes. Reason: "
									+ e.getMessage()));
			throw new ConnectException(
					"Failed to fetch incoterm attributes. Reason: "
							+ e.getResponseBodyAsString());

		} catch (URISyntaxException e) {
			logger.error(
					Logger.EVENT_FAILURE,
					ESAPI.encoder().encodeForHTML(
							"Inside getIncoTerms, Unable to form incoterm attributes URI. Reason: "
									+ e.getMessage()));
			throw new DataException(
					"Unable to form getIncoTerms URI. Reason: "
							+ e.getMessage());

		}

		return response;
	}


	/**
	 * 
	 * Serves the request for Holiday dates and weekyly calendar holiday's attributes based on incotermId 
	 * 
	 * @param reqParam
	 * @return
	 */

	@PostMapping(value = "/{uuid}/holidayslist")
	public Map<String, List<String>> getHolidayListOfCalendar(HttpServletRequest request,
			@RequestBody Map<String, String> payload){


		logger.debug(Logger.EVENT_SUCCESS,
				ESAPI.encoder().encodeForHTML("Inside POST api of holiday/attributes" + payload));

		String calendarName = payload.get(CALENDAR_NAME);
		
		HttpEntity<?> entity = new HttpEntity<>(commonService.getHttpHeader(request, true));

		ApplicationProps applicationProps = contextProvider.getCurrentContext().getApplicationProps();

		Map<String, List<String>> response = new HashMap<>();
		
		try {
			URI url = new URI(applicationProps.getMdmMasterDataEndPoint()
					+ "/calendar/holidayslist/" + calendarName);
			UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUri(url);
			
			logger.debug(
					Logger.EVENT_SUCCESS,
					ESAPI.encoder().encodeForHTML(
							"Making POST call to TRM at endpoint: " + url
							+ " with HttpEntity: " + entity));

			ResponseEntity<Map> resultResp = restTemplate.exchange(uriBuilder.build().toUri(),
					HttpMethod.GET, entity, Map.class);

			logger.debug(
					Logger.EVENT_SUCCESS,
					ESAPI.encoder().encodeForHTML(
							"Making POST call to TRM at endpoint: completed " + url
							+ " is: " + resultResp));
			if (resultResp.getStatusCodeValue() == HttpStatus.OK.value()) {

				response = resultResp.getBody();
			}

		} catch (HttpStatusCodeException e) {
			logger.error(
					Logger.EVENT_FAILURE,
					ESAPI.encoder().encodeForHTML(
							"Inside holiday API, failed to fetch holiday attributes. Reason: "
									+ e.getMessage()));
			throw new ConnectException(
					"Failed to fetch holiday attributes. Reason: "
							+ e.getResponseBodyAsString());

		} catch (URISyntaxException e) {
			logger.error(
					Logger.EVENT_FAILURE,
					ESAPI.encoder().encodeForHTML(
							"Inside holiday API, Unable to form holiday attributes URI. Reason: "
									+ e.getMessage()));
			throw new DataException(
					"Unable to form holiday URI. Reason: "
							+ e.getMessage());

		}

		return response;
	}


	/**
	 * 
	 * Serves the request for Product properties attributes based on propertyId 
	 * 
	 * @param reqParam
	 * @return
	 */

	@PostMapping(value = "product/property/attribute/{uuid}/values")
	public List<Map<String,String>> getProductProperties(HttpServletRequest request,
			@RequestBody String reqestBodyStr){

		logger.debug(Logger.EVENT_SUCCESS,
				ESAPI.encoder().encodeForHTML("Inside POST api of product properties" + reqestBodyStr));

		HttpEntity<String> requestBody = new HttpEntity<String>(reqestBodyStr,
				commonService.getHttpHeader(request));

		ApplicationProps applicationProps = contextProvider.getCurrentContext().getApplicationProps();

		List<Map<String,String>> response = new ArrayList<>();

		try {

			URI url = new URI(applicationProps.getMdmMasterDataEndPoint()
					+ "/product/property/attribute/values");

			logger.debug(
					Logger.EVENT_SUCCESS,
					ESAPI.encoder().encodeForHTML(
							"Making POST call to TRM at endpoint: " + url
							+ " with HttpEntity: " + requestBody));

			ResponseEntity<List> resultResp = restTemplate.exchange(url,
					HttpMethod.POST, requestBody, List.class);

			logger.debug(
					Logger.EVENT_SUCCESS,
					ESAPI.encoder().encodeForHTML(
							"Response from API at endpoint " + url
							+ " is: " + resultResp));

			if (resultResp.getStatusCodeValue() == HttpStatus.OK.value()) {

				response = resultResp.getBody();
			}

		} catch (HttpStatusCodeException e) {
			logger.error(
					Logger.EVENT_FAILURE,
					ESAPI.encoder().encodeForHTML(
							"Inside product-properties API, failed to fetch product-properties attributes. Reason: "
									+ e.getMessage()));
			throw new ConnectException(
					"Failed to fetch product-properties attributes. Reason: "
							+ e.getResponseBodyAsString());

		} catch (URISyntaxException e) {
			logger.error(
					Logger.EVENT_FAILURE,
					ESAPI.encoder().encodeForHTML(
							"Inside product-properties API, Unable to form product-properties attributes URI. Reason: "
									+ e.getMessage()));
			throw new DataException(
					"Unable to form product-properties URI. Reason: "
							+ e.getMessage());
		}

		return response;
	}
	
	
	@PostMapping(value = "/{uuid}/cpAddress")
		public Object getCPAddress(
				HttpServletRequest request,
				@RequestBody CpDetails cpDetails) {
		HttpEntity<?> entity = new HttpEntity<>(commonService.getHttpHeader(
				request, true));

		ApplicationProps applicationProps = contextProvider.getCurrentContext()
				.getApplicationProps();
		ResponseEntity<String> resultResp=null;
		try {
			String cpId = cpDetails.getCpId();

			if (StringUtils.isEmpty(cpId)) {
				resultResp = new ResponseEntity<String>(
						"Invalid request, Cp Id is null/empty",
						HttpStatus.BAD_REQUEST);
				return resultResp;
			}
			

			URI url = new URI(validator.cleanData(applicationProps.getEka_ctrm_host()
					+ "/api/data/cpaddess/" + cpId));

			resultResp = restTemplate.exchange(url,
					HttpMethod.GET, entity, String.class);

			if (resultResp.getStatusCodeValue() == HttpStatus.OK.value()) {
				 resultResp.getBody();
			}

		} catch (HttpStatusCodeException e) {

			throw new ConnectException(
					"Failed to fetch CP Address info from TRM side. Reason: "
							+ e.getResponseBodyAsString());

		} catch (URISyntaxException e) {

			throw new DataException("Unable to form URI. Reason: "
					+ e.getMessage());

		} catch (Exception e) {

			throw new DataException(
					"Unable to parse response from api-endpoint: "
							+ applicationProps.getEka_ctrm_host()
							+ "\n" + e.getMessage());

		}
		return resultResp;
	}
	
	
	@PostMapping(value = "/{uuid}/cp")
	public Object getCPDetails(
			HttpServletRequest request,
			@RequestBody CpDetails cpDetails) {


		HttpEntity<?> entity = new HttpEntity<>(commonService.getHttpHeader(
				request, true));

		ApplicationProps applicationProps = contextProvider.getCurrentContext()
				.getApplicationProps();
		ResponseEntity<String> resultResp=null;
		try {
			String cpId = cpDetails.getCpId();

			if (StringUtils.isEmpty(cpId)) {
				resultResp = new ResponseEntity<String>(
						"Invalid request, Cp Id is null/empty",
						HttpStatus.BAD_REQUEST);
				return resultResp;
			}
			
			URI url = new URI(validator.cleanData(applicationProps.getEka_ctrm_host()
					+ "/api/data/cpdetails/" + cpId));

			resultResp = restTemplate.exchange(url,
					HttpMethod.GET, entity, String.class);

			if (resultResp.getStatusCodeValue() == HttpStatus.OK.value()) {
				resultResp.getBody();
			}

		} catch (HttpStatusCodeException e) {

			throw new ConnectException(
					"Failed to fetch cpDetails info from TRM side. Reason: "
							+ e.getResponseBodyAsString());

		} catch (URISyntaxException e) {

			throw new DataException("Unable to form URI. Reason: "
					+ e.getMessage());

		} catch (Exception e) {

			throw new DataException(
					"Unable to parse response from api-endpoint: "
							+ applicationProps.getEka_ctrm_host()
							+ "\n" + e.getMessage());

		}
		return resultResp;
	}
	
	/**
	 * 
	 * Serves the request for Quality Exchange details for QualityId
	 * 
	 * @param reqParam
	 * @return
	 */

	@PostMapping(value = "{uuid}/qualityexchange")
	public ResponseEntity<String> getQualityExchange(
			HttpServletRequest request,
			@RequestBody QualityDetails qualityDetails) {

		ResponseEntity<String> response = null;

		logger.debug(
				Logger.EVENT_SUCCESS,
				ESAPI.encoder().encodeForHTML(
						"Inside POST api for getting quality exchange properties"
								+ qualityDetails));

		ApplicationProps applicationProps = contextProvider.getCurrentContext()
				.getApplicationProps();

		String qualityId = qualityDetails.getQualityId();

		if (StringUtils.isEmpty(qualityId)) {
			response = new ResponseEntity<String>(
					"Invalid request, Quality Id is null/empty",
					HttpStatus.BAD_REQUEST);
			return response;
		}
		try {

			HttpEntity<String> requestBody = new HttpEntity<String>(null,
					commonService.getHttpHeader(request));

			URI url = new URI(validator.cleanData(applicationProps.getMdmMasterDataEndPoint()
					+ "/qualityexchange/" + qualityId));

			logger.debug(
					Logger.EVENT_SUCCESS,
					ESAPI.encoder().encodeForHTML(
							"Making GET API call to TRM at endpoint: " + url
									+ " with HttpEntity: " + requestBody));

			response = restTemplate.exchange(url, HttpMethod.GET, requestBody,
					String.class);

			logger.debug(
					Logger.EVENT_SUCCESS,
					ESAPI.encoder().encodeForHTML(
							"Response from API at endpoint " + url + " is: "
									+ response));

		} catch (HttpStatusCodeException e) {
			logger.error(
					Logger.EVENT_FAILURE,
					ESAPI.encoder().encodeForHTML(
							"Failed to fetch quality exchange attributes for Quality "
									+ qualityId), e);
			return new ResponseEntity<String>(e.getResponseBodyAsString(),
					e.getStatusCode());

		} catch (URISyntaxException e) {
			logger.error(
					Logger.EVENT_FAILURE,
					ESAPI.encoder().encodeForHTML(
							"Error in forming the TRM url endpoint for qualityexchange "
									+ qualityId), e);
			return new ResponseEntity<String>(
					"Error in forming the TRM url endpoint for qualityexchange ",
					HttpStatus.INTERNAL_SERVER_ERROR);

		}

		return response;
	}	
	
	
	@PostMapping(value = "{uuid}/defaultpaymentterm")
	public ResponseEntity<String> getDefaultPaymentterm(
			HttpServletRequest request,
			@RequestBody DefaultPaymentTerm defaultPaymentTerm) {

		ResponseEntity<String> response = null;

		logger.debug(
				Logger.EVENT_SUCCESS,
				ESAPI.encoder().encodeForHTML(
						"Inside POST api for getting Defaulting Payment properties"
								+ defaultPaymentTerm));

		ApplicationProps applicationProps = contextProvider.getCurrentContext()
				.getApplicationProps();

		String cpProfileId = defaultPaymentTerm.getCpProfileId();

		if (StringUtils.isEmpty(cpProfileId)) {
			response = new ResponseEntity<String>(
					"Invalid request, CP Profile Id is null/empty",
					HttpStatus.BAD_REQUEST);
			return response;
		}
		try {

			HttpEntity<String> requestBody = new HttpEntity<String>(null,
					commonService.getHttpHeader(request));

			URI url = new URI(validator.cleanData(applicationProps.getMdmMasterDataEndPoint()
					+ "/defaultpaymentterm/" + cpProfileId));

			logger.debug(
					Logger.EVENT_SUCCESS,
					ESAPI.encoder().encodeForHTML(
							"Making GET API call to TRM at endpoint: " + url
									+ " with HttpEntity: " + requestBody));

			response = restTemplate.exchange(url, HttpMethod.GET, requestBody,
					String.class);

			logger.debug(
					Logger.EVENT_SUCCESS,
					ESAPI.encoder().encodeForHTML(
							"Response from API at endpoint " + url + " is: "
									+ response));

		} catch (HttpStatusCodeException e) {
			logger.error(
					Logger.EVENT_FAILURE,
					ESAPI.encoder().encodeForHTML(
							"Failed to fetch Defaulting Payment attributes for CP Profile "
									+ cpProfileId), e);
			return new ResponseEntity<String>(e.getResponseBodyAsString(),
					e.getStatusCode());

		} catch (URISyntaxException e) {
			logger.error(
					Logger.EVENT_FAILURE,
					ESAPI.encoder().encodeForHTML(
							"Error in forming the TRM url endpoint for Defaulting Payment "
									+ cpProfileId), e);
			return new ResponseEntity<String>(
					"Error in forming the TRM url endpoint for Defaulting Payment ",
					HttpStatus.INTERNAL_SERVER_ERROR);

		}

		return response;
	}	
	
	
	@PostMapping(value = "{uuid}/baseQuantity")
	public ResponseEntity<String> getBaseQty(
			HttpServletRequest request,
			@RequestBody ProductDetails productDetails) {
	HttpEntity<?> entity = new HttpEntity<>(commonService.getHttpHeader(
			request, true));

	ApplicationProps applicationProps = contextProvider.getCurrentContext()
			.getApplicationProps();
	ResponseEntity<String> resultResp=null;
	String productId=null;
	try {
		 productId = productDetails.getProductId();

		if (StringUtils.isEmpty(productId)) {
			resultResp = new ResponseEntity<String>(
					"Invalid request, productId is null/empty",
					HttpStatus.BAD_REQUEST);
			return resultResp;
		}
		

		URI url = new URI(validator.cleanData(applicationProps.getMdmMasterDataEndPoint()
				+ "/productDetails/" + productId));

		resultResp = restTemplate.exchange(url,
				HttpMethod.POST, entity, String.class);

		if (resultResp.getStatusCodeValue() == HttpStatus.OK.value()) {
			 resultResp.getBody();
		}

	} catch (HttpStatusCodeException e) {
		logger.error(
				Logger.EVENT_FAILURE,
				ESAPI.encoder().encodeForHTML(
						"Failed to fetch Base Quantity unit  attributes for Product Id "
								+ productId), e);
		throw new ConnectException(
				"Failed to fetch Product Details info from TRM side. Reason: "
						+ e.getResponseBodyAsString());

	} catch (URISyntaxException e) {
		logger.error(
				Logger.EVENT_FAILURE,
				ESAPI.encoder().encodeForHTML(
						"Error in forming the TRM url endpoint for Base Quantity unit "
								+ productId), e);
		throw new DataException("Unable to form URI. Reason: "
				+ e.getMessage());

	} catch (Exception e) {
		logger.error(
				Logger.EVENT_FAILURE,
				ESAPI.encoder().encodeForHTML(
						"Unable to parse response from api-endpoint:"
								+ productId), e);
		throw new DataException(
				"Unable to parse response from api-endpoint: "
						+ applicationProps.getEka_ctrm_host()
						+ "\n" + e.getMessage());

	}
	return resultResp;
}

	/**
	 *
	 * Serves the request for Product PriceUnit Detail attributes
	 *
	 * @param reqParam
	 * @return
	 */

	@PostMapping(value = "/{uuid}/productPriceUnitAttributes")
	public Object getproductPriceUnitAttributes(@RequestBody String payload, HttpServletRequest request) {

		logger.debug(Logger.EVENT_SUCCESS,
				ESAPI.encoder().encodeForHTML("Inside POST api of productPriceUnitAttributes/" + payload));

		JSONObject jsonPayload = new JSONObject(payload);
		JSONObject productPriceUnitData = jsonPayload.getJSONObject("productPriceUnitAtrributes");

		HttpEntity<JSONObject> requestBody = new HttpEntity<JSONObject>(productPriceUnitData,
				commonService.getHttpHeader(request));

		ApplicationProps applicationProps = contextProvider.getCurrentContext().getApplicationProps();

		Object response = new Object();

		try {
			URI url = new URI(applicationProps.getMdmMasterDataEndPoint()
					+ "/productPriceUnit");

			logger.debug(
					Logger.EVENT_SUCCESS,
					ESAPI.encoder().encodeForHTML(
							"Making POST call to TRM at endpoint: " + url));

			ResponseEntity<Object> resultResp = restTemplate.exchange(url,
					HttpMethod.POST, requestBody, Object.class);

			logger.debug(
					Logger.EVENT_SUCCESS,
					ESAPI.encoder().encodeForHTML(
							"Making POST call to TRM at endpoint: completed" + url
									+ " is: " + resultResp));
			if (resultResp.getStatusCodeValue() == HttpStatus.OK.value()) {

				response = resultResp.getBody();
			}

		} catch (HttpStatusCodeException e) {
			logger.error(
					Logger.EVENT_FAILURE,
					ESAPI.encoder().encodeForHTML(
							"Inside getproductPriceUnitAttributes, failed to fetch productPriceUnitAttributes attributes. Reason: "
									+ e.getMessage()));
			throw new ConnectException(
					"Failed to fetch productPriceUnitAttributes attributes. Reason: "
							+ e.getResponseBodyAsString());

		} catch (URISyntaxException e) {
			logger.error(
					Logger.EVENT_FAILURE,
					ESAPI.encoder().encodeForHTML(
							"Inside getIncoTerms, Unable to form incoterm attributes URI. Reason: "
									+ e.getMessage()));
			throw new DataException(
					"Unable to form getIncoTerms URI. Reason: "
							+ e.getMessage());

		}

		return response;
	}



	@ExceptionHandler(value = { SocketTimeoutException.class })
	public ResponseEntity<String> handleSocketTimeoutException(
			SocketTimeoutException ste) {

		logger.error(
				Logger.EVENT_FAILURE,
				ESAPI.encoder().encodeForHTML(
						"Handling MDM exceptions , SocketTimeoutException "),
				ste);

		ResponseEntity<String> responseEntity = null;
		responseEntity = new ResponseEntity<String>(
				"Error in connecting to CTRM/Connect server" + ste.getMessage(),
				HttpStatus.INTERNAL_SERVER_ERROR);
		return responseEntity;
	}

	@ExceptionHandler(value = { JSONException.class })
	public ResponseEntity<String> handleJSONException(JSONException jsone) {
		logger.error(
				Logger.EVENT_FAILURE,
				ESAPI.encoder().encodeForHTML(
						"Handling MDM exceptions, JSONException "), jsone);

		ResponseEntity<String> responseEntity = null;
		responseEntity = new ResponseEntity<String>("MDM Parsing Error "
				+ jsone.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		return responseEntity;
	}

	@ExceptionHandler(value = { ConnectException.class })
	public ResponseEntity<String> handleConnectException(
			ConnectException connectException) {
		logger.error(
				Logger.EVENT_FAILURE,
				ESAPI.encoder().encodeForHTML(
						"MDM - ConnectException : " + connectException.getMessage()), connectException);
		ResponseEntity<String> responseEntity = null;
		responseEntity = new ResponseEntity<String>(
				connectException.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		return responseEntity;
	}

	@ExceptionHandler(value = { HttpMessageNotReadableException.class })
	public ResponseEntity<String> handleHttpMessageNotReadableException(
			HttpMessageNotReadableException ex) {
		ResponseEntity<String> responseEntity = null;
		logger.error(
				Logger.EVENT_FAILURE,
				ESAPI.encoder().encodeForHTML(
						"MDM - HttpMessageNotReadableException : " + ex.getMessage()), ex);
		responseEntity = new ResponseEntity<String>("Invalid Request, "
				+ ex.getMessage(), HttpStatus.BAD_REQUEST);
		return responseEntity;
	}
	
	@ExceptionHandler(value = { ResourceAccessException.class, HttpServerErrorException.class })
	public ResponseEntity<String> handleResourceAccessException(ResourceAccessException ex) {
		ResponseEntity<String> responseEntity = null;

		logger.error(
				Logger.EVENT_FAILURE,
				ESAPI.encoder().encodeForHTML(
						"MDM resource unavailable : " + ex.getMessage()), ex);
		
		responseEntity = new ResponseEntity<String>(
				"MDM resource unavailable : "+ex.getMessage(), HttpStatus.SERVICE_UNAVAILABLE);
		return responseEntity;
	}
	
	@ExceptionHandler(value = { Exception.class })
	public ResponseEntity<String> handleException(Exception ex) {
		ResponseEntity<String> responseEntity = null;

		logger.error(
				Logger.EVENT_FAILURE,
				ESAPI.encoder().encodeForHTML(
						"Error in getting  mdm data: " + ex.getMessage()), ex);
		
		responseEntity = new ResponseEntity<String>("MDM Error : "
				+ ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		return responseEntity;
	}
}