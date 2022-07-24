package com.eka.mdm.api.controller;

import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.websocket.server.PathParam;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.owasp.esapi.ESAPI;
import org.owasp.esapi.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.eka.mdm.api.ecache.RedisCacheManager;
import com.eka.mdm.api.exception.ConnectException;
import com.eka.mdm.api.exception.DataException;
import com.eka.mdm.api.service.ServiceKeyOperations;
import com.eka.mdm.api.util.CommonService;
import com.eka.mdm.api.util.CommonValidator;
import com.eka.mdm.api.util.ContextProvider;
import com.eka.mdm.dataobject.ApplicationProps;
import com.eka.mdm.dataobject.RequestContext;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import redis.clients.jedis.Jedis;

/**
 * <p>
 * <code>MasterDataRetrievalController</code> Controller which exposes end
 * points to fetch the master data from CTRM MDM & Connect MDM based on
 * serviceKey APIs
 * <p>
 * <hr>
 * 
 * @author Ranjan.Jha
 * @version 1.0
 */

@RestController
public class MasterDataRetrievalController {

	@Value("${mdm.dependent.key.name}")
	private String dependencyKey;

	final static Logger logger = ESAPI
			.getLogger(MasterDataRetrievalController.class);

	private final String ID2VALUE_FIELD_KEY = "contractItems";
	private String mappedServiceKey = "";

	@Autowired
	public RestTemplate restTemplate;
	@Autowired
	public CommonService commonService;
	@Lazy
	@Autowired
	private RedisCacheManager redisOps;

	@Autowired
	ServiceKeyOperations serviceKeyOps;
	@Autowired
	private ContextProvider contextProvider;
	@Autowired
	CommonValidator validator;

	private final String _CTRM = "ctrm";
	private final String _CONNECT = "connect";
	private final String _LOGISTIC = "logistic";
	private final String _IGNORE = "invalidServiceKeys";
	private final String _SERVICEKEY = "serviceKey";
	private final String _PLATFORM = "platform";
	private final String _COLLECTION = "collectionName";
	private final String _FILTER = "filter";
	private final String _ERRORS = "errors";
	private final String _QUANTITY_CONVERSION = "quantConv";
	private final String _SETUP_ATTRIBUTES = "setupAttributes";
	private final String _RATE_CONVERSION = "priceConv";
	private final String _SUB_CURRENCY_FACTOR = "subCurrFactor";
	private final String _SERVICEKEY_DELIMITER = "_";
	private final String _REF_TYPE_KEY = "refTypeId";
	private final String MDM = "mdm";
	private final String ACTION_ID = "actionId";

	/**
	 * 
	 * Serves the request for single serviceKey as part of query params.
	 * 
	 * @param reqParam
	 * @return
	 */
	// Keeping it for now. Need to review whether its required or it can be
	// managed
	// with/data api

	@RequestMapping(value = "/{uuid}/data", method = RequestMethod.GET, produces = "application/json")
	public String getMasterDatum(@PathParam("uuid") String uuid,
			@RequestParam MultiValueMap<String, String> reqParam,
			HttpServletRequest request) {

		ApplicationProps applicationProps = contextProvider.getCurrentContext()
				.getApplicationProps();
		UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(
				applicationProps.getCtrmMdmEndpoint()).queryParams(reqParam);
		HttpEntity<?> entity = new HttpEntity<>(commonService.getHttpHeader(
				request, true));

		String response = new JSONObject().toString();

		try {
			ResponseEntity<String> resultResp = restTemplate.exchange(
					uriBuilder.toUriString(), HttpMethod.GET, entity,
					String.class);
			response = resultResp.getBody();
		} catch (Exception e) {
			logger.error(
					Logger.EVENT_FAILURE,
					ESAPI.encoder().encodeForHTML(
							"getMasterDatum: " + e.getMessage()));
		}

		return response;
	}

	/***
	 * Accepts array of Json Object as part of request body. sample request
	 * body: [ { _SERVICEKEY: "mdm.ctrm.countriesComboDataFromDB" }, {
	 * _SERVICEKEY: "mdm.ctrm.cityComboDataFromDB", "countryId": "CYM-M0-37013",
	 * "type": "Port" }, { _SERVICEKEY: "mdm.connect.pricePoint" }
	 * 
	 * ]
	 * 
	 */

	@RequestMapping(value = "/{uuid}/data", method = RequestMethod.POST, produces = "application/json")
	public Map<String, List<Map<String, Object>>> getMasterData(
			@PathVariable("uuid") String uuid,
			@RequestBody String reqestBodyStr, HttpServletRequest request) {

		Map<String, List<Map<String, Object>>> resultMap = new LinkedHashMap<>();
		Map<String, List<Map<String, Object>>> respResultMap = new LinkedHashMap<>();

		ApplicationProps applicationProps = contextProvider.getCurrentContext()
				.getApplicationProps();

		HttpHeaders httpRequestHeaders = commonService.getHttpHeader(request,
				true);
		try {

			JSONArray clientRequestBody = new JSONArray(reqestBodyStr);
			logger.debug(
					Logger.EVENT_SUCCESS,
					ESAPI.encoder().encodeForHTML(
							"MDM Request Body: " + clientRequestBody));

			/*********************************
			 * Commenting below code for now, since we are not serving data from
			 * Cache to reflect the real time changes. Jira Id EPC-1038 @
			 * http://jira.ekaplus.com/browse/EPC-1038
			 **********************************/
			// Retrieve values corresponding to the serviceKey which is present
			// in cache
			/*
			 * if (!("N").equalsIgnoreCase(request.getHeader("isCachedData"))) {
			 * 
			 * logger.debug("Retrieving from cache..."); resultMap =
			 * retriveFromCache(clientRequestBody);
			 * logger.debug("Retrieved from cache: " + resultMap);
			 * 
			 * // Remove serviceKey from request which is present in cache
			 * clientRequestBody =
			 * removeCacheEntryFromJSONArray(clientRequestBody); if
			 * (!clientRequestBody.isEmpty())
			 * logger.debug("serviceKeys are not present in cache: " +
			 * clientRequestBody); }
			 */

			resultMap.put(_ERRORS, new ArrayList<Map<String, Object>>());

			JSONObject mappedRequest = getMappedRequest(clientRequestBody);

			if (mappedRequest.has(_CTRM)
					&& !mappedRequest.getJSONArray(_CTRM).isNull(0)) {

				// Keeping userName as admin for now, since it won't affect
				// Contract. It is
				// needed for GMR
				httpRequestHeaders.remove("userName");
				httpRequestHeaders.add("userName", "admin");
				HttpEntity<String> requestBody = new HttpEntity<String>(
						mappedRequest.getJSONArray(_CTRM).toString(),
						httpRequestHeaders);

				try {

					logger.debug(
							Logger.EVENT_SUCCESS,
							ESAPI.encoder().encodeForHTML(
									"Making call to TRM at endpoint: "
											+ applicationProps
													.getCtrmMdmEndpoint()
											+ " with request body: "
											+ mappedRequest.getJSONArray(_CTRM)
													.toString()));

					ResponseEntity<String> responseEntity = restTemplate
							.exchange(applicationProps.getCtrmMdmEndpoint(),
									HttpMethod.POST, requestBody, String.class);

					String respResultMapStr = responseEntity.getBody();
					logger.debug(
							Logger.EVENT_SUCCESS,
							ESAPI.encoder().encodeForHTML(
									"Making POST call to TRM at endpoint: completed "
											+ respResultMapStr));

					if (responseEntity.getStatusCode().is2xxSuccessful()) {
						respResultMap = new Gson()
								.fromJson(
										respResultMapStr,
										new TypeToken<Map<String, List<Map<String, Object>>>>() {
										}.getType());
					} else {
						List<Map<String, Object>> errors = serviceKeyOps
								.prepareErrorList(
										mappedRequest.getJSONArray(_CTRM),
										_SERVICEKEY,
										"Error from TRM: Status Code-"
												+ responseEntity
														.getStatusCode());
						Map<String, Object> errorPairs = new HashMap<String, Object>();
						errors.add((Map<String, Object>) errorPairs.put(
								"Possible Reason: ", responseEntity.getBody()));
						resultMap.get(_ERRORS).addAll(errors);
					}

					resultMap.putAll(respResultMap);

					// Cache the response to serve for the next request
					addInCache(respResultMap, clientRequestBody);

				} catch (HttpClientErrorException he) {

					logger.error(
							Logger.EVENT_FAILURE,
							ESAPI.encoder().encodeForHTML(
									"Error in getting MDM data from CTRM "), he);

					String errMsg = he.getRawStatusCode() + ""
							+ he.getResponseBodyAsString()
							+ he.getResponseHeaders();

					List<Map<String, Object>> errors = serviceKeyOps
							.prepareErrorList(
									mappedRequest.getJSONArray(_CTRM),
									_SERVICEKEY, errMsg);

					resultMap.get(_ERRORS).addAll(errors);

				} catch (HttpStatusCodeException ex) {

					logger.error(Logger.EVENT_FAILURE, ESAPI.encoder()
							.encodeForHTML("Error in fetching CTRM data "), ex);

					List<Map<String, Object>> errors = serviceKeyOps
							.prepareErrorList(
									mappedRequest.getJSONArray(_CTRM),
									_SERVICEKEY, ex.getResponseBodyAsString());

					if (HttpStatus.MOVED_TEMPORARILY.equals(ex.getStatusCode())) {

						Map<String, Object> errorPairs = new HashMap<String, Object>();
						errors.add((Map<String, Object>) errorPairs.put(
								"Reason: ", "TRM Host is mis-configured"));
						logger.error(
								Logger.EVENT_FAILURE,
								ESAPI.encoder().encodeForHTML(
										"TRM Host is mis-configured " + errors));

					}

					logger.error(
							Logger.EVENT_FAILURE,
							ESAPI.encoder().encodeForHTML(
									"Error lists corresponding to serviceKeys: "
											+ errors));
					resultMap.get(_ERRORS).addAll(errors);
				} catch (Exception ex) {

					logger.error(Logger.EVENT_FAILURE, ESAPI.encoder()
							.encodeForHTML("Error in fetching CTRM data "), ex);

					List<Map<String, Object>> errors = serviceKeyOps
							.prepareErrorList(
									mappedRequest.getJSONArray(_CTRM),
									_SERVICEKEY, ex.getMessage());
					logger.error(
							Logger.EVENT_FAILURE,
							ESAPI.encoder().encodeForHTML(
									"Error lists corresponding to serviceKeys: "
											+ errors));
					resultMap.get(_ERRORS).addAll(errors);
				}

			}

			if (mappedRequest.has(_QUANTITY_CONVERSION)
					&& !mappedRequest.getJSONArray(_QUANTITY_CONVERSION)
							.isNull(0)) {

				HttpEntity<String> requestBody = new HttpEntity<String>(
						mappedRequest.getJSONArray(_QUANTITY_CONVERSION).get(0)
								.toString(), httpRequestHeaders);
				try {

					logger.debug(
							Logger.EVENT_SUCCESS,
							ESAPI.encoder()
									.encodeForHTML(
											"Making call for QUANTITY CONVERSION at endpoint "
													+ applicationProps
															.getQuantConvEndpoint()
													+ " with request body: "
													+ mappedRequest
															.getJSONArray(
																	_QUANTITY_CONVERSION)
															.get(0).toString()));

					ResponseEntity<Map> responseEntity = restTemplate.exchange(
							applicationProps.getQuantConvEndpoint(),
							HttpMethod.POST, requestBody, Map.class);
					respResultMap = responseEntity.getBody();
					logger.debug(
							Logger.EVENT_SUCCESS,
							ESAPI.encoder().encodeForHTML(
									"Response for QUANTITY CONVERSION: "
											+ respResultMap));

					resultMap.putAll(respResultMap);

					// Cache the response to serve for the next request
					addInCache(respResultMap, clientRequestBody);

				} catch (RestClientException e) {

					logger.error(
							Logger.EVENT_FAILURE,
							ESAPI.encoder().encodeForHTML(
									"Error in _QUANTITY_CONVERSION of data: "
											+ e.getMessage()), e);

					List<Map<String, Object>> errors = serviceKeyOps
							.prepareErrorList(mappedRequest
									.getJSONArray(_QUANTITY_CONVERSION),
									_SERVICEKEY, e.getMessage());
					logger.error(
							Logger.EVENT_FAILURE,
							ESAPI.encoder().encodeForHTML(
									"Error lists corresponding to serviceKeys: "
											+ errors), e);

					resultMap.get(_ERRORS).addAll(errors);
				}

			}

			if (mappedRequest.has(_SETUP_ATTRIBUTES)
					&& !mappedRequest.getJSONArray(_SETUP_ATTRIBUTES).isNull(0)) {

				JSONObject setupAttrJsonObj = new JSONObject(mappedRequest
						.getJSONArray(_SETUP_ATTRIBUTES).get(0).toString());
				String serviceKey = setupAttrJsonObj.getString(_SERVICEKEY);
				String actionId = setupAttrJsonObj.getString(ACTION_ID);
				String mdmSetupEndPoint = applicationProps
						.getCtrmSetupMasterDataEndPoint() + actionId;

				httpRequestHeaders.add(ACTION_ID, actionId);

				HttpEntity<String> requestBody = new HttpEntity<String>(
						mappedRequest.getJSONArray(_SETUP_ATTRIBUTES).get(0)
								.toString(), httpRequestHeaders);
				try {

					logger.debug(
							Logger.EVENT_SUCCESS,
							ESAPI.encoder().encodeForHTML(
									"Making call for _SETUP_ATTRIBUTES at endpoint "
											+ mdmSetupEndPoint
											+ " with request body: "
											+ mappedRequest
													.getJSONArray(
															_SETUP_ATTRIBUTES)
													.get(0).toString()));

					ResponseEntity<String> responseEntity = restTemplate
							.exchange(mdmSetupEndPoint, HttpMethod.POST,
									requestBody, String.class);

					String respResultListStr = responseEntity.getBody();
					logger.debug(
							Logger.EVENT_SUCCESS,
							ESAPI.encoder().encodeForHTML(
									"Response from TRM: " + respResultListStr));

					List<Map<String, Object>> respResultList = new ArrayList<>();
					if (responseEntity.getStatusCode().is2xxSuccessful()) {
						respResultList = new Gson().fromJson(respResultListStr,
								new TypeToken<List<Map<String, Object>>>() {
								}.getType());
					} else {
						List<Map<String, Object>> errors = serviceKeyOps
								.prepareErrorList(
										mappedRequest
												.getJSONArray(_SETUP_ATTRIBUTES),
										_SERVICEKEY,
										"Error from TRM: Status Code-"
												+ responseEntity
														.getStatusCode());
						Map<String, Object> errorPairs = new HashMap<String, Object>();
						errors.add((Map<String, Object>) errorPairs.put(
								"Possible Reason: ", respResultListStr));
						resultMap.get(_ERRORS).addAll(errors);
					}

					respResultMap.put(serviceKey, respResultList);

					logger.debug(
							Logger.EVENT_SUCCESS,
							ESAPI.encoder().encodeForHTML(
									"Response for SETUP ATTRIBUTES: "
											+ respResultMap));

					resultMap.putAll(respResultMap);

					// Cache the response to serve for the next request
					addInCache(respResultMap, clientRequestBody);

				} catch (RestClientException e) {

					logger.error(
							Logger.EVENT_FAILURE,
							ESAPI.encoder().encodeForHTML(
									"Error in _SETUP_ATTRIBUTES of data: "
											+ e.getMessage()), e);

					List<Map<String, Object>> errors = serviceKeyOps
							.prepareErrorList(mappedRequest
									.getJSONArray(_SETUP_ATTRIBUTES),
									_SERVICEKEY, e.getMessage());
					logger.error(
							Logger.EVENT_FAILURE,
							ESAPI.encoder().encodeForHTML(
									"Error lists corresponding to serviceKeys: "
											+ errors), e);

					resultMap.get(_ERRORS).addAll(errors);
				}
			}

			if (mappedRequest.has(_RATE_CONVERSION)
					&& !mappedRequest.getJSONArray(_RATE_CONVERSION).isNull(0)) {

				HttpEntity<String> requestBody = new HttpEntity<String>(
						mappedRequest.getJSONArray(_RATE_CONVERSION).get(0)
								.toString(), httpRequestHeaders);
				try {

					logger.debug(
							Logger.EVENT_SUCCESS,
							ESAPI.encoder().encodeForHTML(
									"Making call for RATE_CONVERSION at endpoint "
											+ applicationProps
													.getFxRateConvEndpoint()
											+ " with request body: "
											+ mappedRequest
													.getJSONArray(
															_RATE_CONVERSION)
													.get(0).toString()));

					ResponseEntity<Map> responseEntity = restTemplate.exchange(
							applicationProps.getFxRateConvEndpoint(),
							HttpMethod.POST, requestBody, Map.class);
					respResultMap = responseEntity.getBody();
					logger.debug(
							Logger.EVENT_SUCCESS,
							ESAPI.encoder().encodeForHTML(
									"Response for RATE_CONVERSION: "
											+ respResultMap));
					resultMap.putAll(respResultMap);

					// Cache the response to serve for the next request
					addInCache(respResultMap, clientRequestBody);

				} catch (RestClientException e) {
					logger.error(
							Logger.EVENT_FAILURE,
							ESAPI.encoder().encodeForHTML(
									"Error in _RATE_CONVERSION of data: "
											+ e.getMessage()), e);
					List<Map<String, Object>> errors = serviceKeyOps
							.prepareErrorList(mappedRequest
									.getJSONArray(_QUANTITY_CONVERSION),
									_SERVICEKEY, e.getMessage());
					logger.error(
							Logger.EVENT_FAILURE,
							ESAPI.encoder().encodeForHTML(
									"Error lists corresponding to serviceKeys: "
											+ errors), e);

					resultMap.get(_ERRORS).addAll(errors);
				}

			}

			if (mappedRequest.has(_SUB_CURRENCY_FACTOR)
					&& !mappedRequest.getJSONArray(_SUB_CURRENCY_FACTOR)
							.isNull(0)) {

				HttpEntity<String> requestBody = new HttpEntity<String>(
						mappedRequest.getJSONArray(_SUB_CURRENCY_FACTOR).get(0)
								.toString(), httpRequestHeaders);
				try {

					logger.debug(
							Logger.EVENT_SUCCESS,
							ESAPI.encoder()
									.encodeForHTML(
											"Making call for SUB_CURRENCY_FACTOR at endpoint "
													+ applicationProps
															.getCurrencyFactorEndpoint()
													+ " with request body: "
													+ mappedRequest
															.getJSONArray(
																	_SUB_CURRENCY_FACTOR)
															.get(0).toString()));
					ResponseEntity<Map> responseEntity = restTemplate.exchange(
							applicationProps.getCurrencyFactorEndpoint(),
							HttpMethod.POST, requestBody, Map.class);
					respResultMap = responseEntity.getBody();
					logger.debug(
							Logger.EVENT_SUCCESS,
							ESAPI.encoder().encodeForHTML(
									"Response for SUB_CURRENCY_FACTOR: "
											+ respResultMap));
					resultMap.putAll(respResultMap);

					// Cache the response to serve for the next request
					addInCache(respResultMap, clientRequestBody);

				} catch (RestClientException e) {
					logger.error(
							Logger.EVENT_FAILURE,
							ESAPI.encoder().encodeForHTML(
									"Error in _SUB_CURRENCY_FACTOR of data: "
											+ e.getMessage()), e);
					List<Map<String, Object>> errors = serviceKeyOps
							.prepareErrorList(mappedRequest
									.getJSONArray(_SUB_CURRENCY_FACTOR),
									_SERVICEKEY, e.getMessage());
					logger.error(
							Logger.EVENT_FAILURE,
							ESAPI.encoder().encodeForHTML(
									"Error lists corresponding to serviceKeys: "
											+ errors), e);

					resultMap.get(_ERRORS).addAll(errors);
				}

			}

			if (mappedRequest.has(_CONNECT)
					&& !mappedRequest.getJSONArray(_CONNECT).isNull(0)) {

				httpRequestHeaders.add(_REF_TYPE_KEY, uuid);
				HttpEntity<String> requestBody = new HttpEntity<String>(
						mappedRequest.getJSONArray(_CONNECT).toString(),
						httpRequestHeaders);

				try {

					logger.debug(
							Logger.EVENT_SUCCESS,
							ESAPI.encoder().encodeForHTML(
									"Making call for CONNECT at endpoint "
											+ applicationProps
													.getConnectMdmEndpoint()
											+ " with request body: "
											+ mappedRequest.getJSONArray(
													_CONNECT).toString()));

					ResponseEntity<Map> responseEntity = restTemplate.exchange(
							applicationProps.getConnectMdmEndpoint(),
							HttpMethod.POST, requestBody, Map.class);
					respResultMap = responseEntity.getBody();
					logger.debug(
							Logger.EVENT_SUCCESS,
							ESAPI.encoder().encodeForHTML(
									"Response for CONNECT: " + respResultMap));

					resultMap.putAll(respResultMap);

					// Cache the response to serve for the next request
					addInCache(respResultMap, clientRequestBody);

				} catch (RestClientException e) {
					logger.error(
							Logger.EVENT_FAILURE,
							ESAPI.encoder()
									.encodeForHTML(
											"Error in _CONNECT data: "
													+ e.getMessage()), e);

					List<Map<String, Object>> errors = serviceKeyOps
							.prepareErrorList(
									mappedRequest.getJSONArray(_CONNECT),
									"object", e.getMessage());
					logger.error(
							Logger.EVENT_FAILURE,
							ESAPI.encoder().encodeForHTML(
									"Error lists corresponding to serviceKeys: "
											+ errors), e);

					resultMap.get(_ERRORS).addAll(errors);
				}

			}

			if (mappedRequest.has(_PLATFORM)
					&& !mappedRequest.getJSONArray(_PLATFORM).isNull(0)) {

				JSONArray clientMappedReqJsonArray = mappedRequest
						.getJSONArray(_PLATFORM);

				for (int indx = 0; indx < clientMappedReqJsonArray.length(); indx++) {

					JSONObject mappedReq = clientMappedReqJsonArray
							.getJSONObject(indx);

					String key = mappedReq.getString("key");
					String value = mappedReq.getString("value");
					String serviceKey = mappedReq.getString("displayKey");

					JSONObject reqData = new JSONObject();
					reqData.put(_COLLECTION, mappedReq.getString(_COLLECTION));
					reqData.put(_FILTER, mappedReq.getJSONObject(_FILTER));

					httpRequestHeaders.clear();
					httpRequestHeaders.add("Authorization",
							request.getHeader("Authorization"));
					httpRequestHeaders.add("Content-Type",
							request.getHeader("Content-Type"));

					HttpEntity<String> requestBody = new HttpEntity<String>(
							reqData.toString(), httpRequestHeaders);

					String resultTemp = "{}";
					try {
						logger.debug(
								Logger.EVENT_SUCCESS,
								ESAPI.encoder()
										.encodeForHTML(
												"Making call for _PLATFORM at endpoint "
														+ applicationProps
																.getPlatformCollectionEndpoint()
														+ " with request body: "
														+ reqData.toString()));

						ResponseEntity<String> responseEntity = restTemplate
								.exchange(applicationProps
										.getPlatformCollectionEndpoint(),
										HttpMethod.POST, requestBody,
										String.class);
						resultTemp = responseEntity.getBody();
						logger.debug(
								Logger.EVENT_SUCCESS,
								ESAPI.encoder()
										.encodeForHTML(
												"Response for _PLATFORM: "
														+ resultTemp));

					} catch (RestClientException e) {

						logger.error(
								Logger.EVENT_FAILURE,
								ESAPI.encoder().encodeForHTML(
										"Error in fetching Platform data: "
												+ e.getMessage()), e);
						List<Map<String, Object>> errors = serviceKeyOps
								.prepareErrorList(mappedRequest
										.getJSONArray("displayKey"), "object",
										e.getMessage());

						logger.error(
								Logger.EVENT_FAILURE,
								ESAPI.encoder().encodeForHTML(
										"Error lists corresponding to serviceKeys: "
												+ errors), e);

						resultMap.get(_ERRORS).addAll(errors);
					}

					List<Map<String, Object>> formatedQueryResult = formatQueryResult(
							key, value, new JSONObject(resultTemp));
					resultMap.put(serviceKey, formatedQueryResult);

					respResultMap.put(serviceKey, formatedQueryResult);
					// Cache the response to serve for the next request
					addInCache(respResultMap, clientRequestBody);

				}

			}

			if (mappedRequest.has(_LOGISTIC)
					&& !mappedRequest.getJSONArray(_LOGISTIC).isNull(0)) {

				HttpEntity<String> requestBody = null;

				JSONArray logisticReqArr = mappedRequest
						.getJSONArray(_LOGISTIC);
				String entityName = "";
				for (int i = 0; i < logisticReqArr.length(); i++) {

					if (logisticReqArr.getJSONObject(i).has("entity"))
						entityName = logisticReqArr.getJSONObject(i).getString(
								"entity");

					if (entityName.equals("contractQuality")) {
						try {
							requestBody = new HttpEntity<String>(
									new JSONArray().put(logisticReqArr.get(i))
											.toString(), httpRequestHeaders);
							logger.debug(
									Logger.EVENT_SUCCESS,
									ESAPI.encoder()
											.encodeForHTML(
													"Making call for _LOGISTIC at endpoint "
															+ applicationProps
																	.getCollectionEntityEndpoint()
															+ " with request body: "
															+ logisticReqArr
																	.get(i))
											.toString());

							ResponseEntity<Map> responseEntity = restTemplate
									.exchange(applicationProps
											.getCollectionEntityEndpoint(),
											HttpMethod.POST, requestBody,
											Map.class);
							respResultMap = responseEntity.getBody();
							logger.debug(
									Logger.EVENT_SUCCESS,
									ESAPI.encoder().encodeForHTML(
											"Response for contractQuality: "
													+ respResultMap));
							resultMap.putAll(respResultMap);
						} catch (Exception e) {
							logger.error(
									Logger.EVENT_FAILURE,
									ESAPI.encoder().encodeForHTML(
											"Error in fetching Logistic data: "
													+ e.getMessage()), e);

							resultMap.get(_ERRORS).addAll(
									serviceKeyOps.prepareErrorList(
											mappedRequest
													.getJSONArray(_LOGISTIC),
											"entity", e.getMessage()));
						}

					} else if (entityName.equals("incoTermId")) {

						try {
							requestBody = new HttpEntity<String>(
									new JSONArray().put(logisticReqArr.get(i))
											.toString(), httpRequestHeaders);
							logger.debug(
									Logger.EVENT_SUCCESS,
									ESAPI.encoder()
											.encodeForHTML(
													"Making call for incoTermId at endpoint "
															+ applicationProps
																	.getIncoTermEndpoint()
															+ " with request body: "
															+ logisticReqArr
																	.get(i))
											.toString());

							ResponseEntity<Map> responseEntity = restTemplate
									.exchange(applicationProps
											.getIncoTermEndpoint(),
											HttpMethod.POST, requestBody,
											Map.class);
							respResultMap = responseEntity.getBody();
							logger.debug(
									Logger.EVENT_SUCCESS,
									ESAPI.encoder().encodeForHTML(
											"Response for incoTermId: "
													+ respResultMap));
							resultMap.putAll(respResultMap);
						} catch (Exception e) {

							logger.error(
									Logger.EVENT_FAILURE,
									ESAPI.encoder().encodeForHTML(
											"Error in fetching Logistic data: "
													+ e.getMessage()), e);
							resultMap.get(_ERRORS).addAll(
									serviceKeyOps.prepareErrorList(
											mappedRequest
													.getJSONArray(_LOGISTIC),
											"entity", e.getMessage()));
						}

					} else if (entityName.equals("incoTermDestination")) {

						try {
							requestBody = new HttpEntity<String>(
									new JSONArray().put(logisticReqArr.get(i))
											.toString(), httpRequestHeaders);
							logger.debug(
									Logger.EVENT_SUCCESS,
									ESAPI.encoder()
											.encodeForHTML(
													"Making call for incoTermDestination at endpoint "
															+ applicationProps
																	.getCollectionDestEndpoint()
															+ " with request body: "
															+ logisticReqArr
																	.get(i))
											.toString());

							ResponseEntity<Map> responseEntity = restTemplate
									.exchange(applicationProps
											.getCollectionDestEndpoint(),
											HttpMethod.POST, requestBody,
											Map.class);
							respResultMap = responseEntity.getBody();
							logger.debug(
									Logger.EVENT_SUCCESS,
									ESAPI.encoder().encodeForHTML(
											"Response for incoTermDestination: "
													+ respResultMap));
							resultMap.putAll(respResultMap);
						} catch (Exception e) {
							logger.error(
									Logger.EVENT_FAILURE,
									ESAPI.encoder().encodeForHTML(
											"Error in fetching Logistic data: "
													+ e.getMessage()), e);
							resultMap.get(_ERRORS).addAll(
									serviceKeyOps.prepareErrorList(
											mappedRequest
													.getJSONArray(_LOGISTIC),
											"entity", e.getMessage()));
						}

					}

				}

			}

			// Add response for the servicekey, which is not present.
			if (!mappedRequest.getJSONArray(_IGNORE).isNull(0)) {

				JSONArray invalidServiceKeys = mappedRequest
						.getJSONArray(_IGNORE);
				for (int index = 0; index < invalidServiceKeys.length(); index++) {

					resultMap.put(invalidServiceKeys.getString(index), null);
				}
				logger.error(
						Logger.EVENT_FAILURE,
						ESAPI.encoder().encodeForHTML(
								"Invalid serviceKeys: " + invalidServiceKeys));
			}

		} catch (JSONException e) {
			logger.error(
					Logger.EVENT_FAILURE,
					ESAPI.encoder().encodeForHTML(
							"Error in MDM API Execution: " + e.getMessage()), e);
		}

		return resultMap;
	}

	private Map<String, List<Map<String, Object>>> retriveFromCache(
			JSONArray jsonArray) {

		Map<String, List<Map<String, Object>>> resultMap = new LinkedHashMap<>();
		ApplicationProps applicationProps = contextProvider.getCurrentContext()
				.getApplicationProps();
		// Check if cache is not enabled in properties file
		if (("N")
				.equalsIgnoreCase(applicationProps.getEka_mdm_isCacheEnabled())) {
			return resultMap;
		}

		Jedis jedis = redisOps.getJedis();
		for (int indx = 0; indx < jsonArray.length(); indx++) {

			JSONObject entry = jsonArray.getJSONObject(indx);
			String serviceKey = entry.getString(_SERVICEKEY);

			// Prepare cache key
			String cacheKey = serviceKeyOps.prepareCacheKey(entry);

			if (jedis.exists(cacheKey)) {
				List<Map<String, Object>> mapList = new Gson().fromJson(
						jedis.get(cacheKey),
						new TypeToken<List<Map<String, Object>>>() {
						}.getType());
				resultMap.put(serviceKey, mapList);
			}

		}
		redisOps.closeRedisResource(jedis);
		return resultMap;
	}

	private JSONArray removeCacheEntryFromJSONArray(JSONArray clientRequestBody) {

		ApplicationProps applicationProps = contextProvider.getCurrentContext()
				.getApplicationProps();
		// Check if cache is not enabled in properties file
		if (("N")
				.equalsIgnoreCase(applicationProps.getEka_mdm_isCacheEnabled())) {
			return clientRequestBody;
		}

		Jedis jedis = redisOps.getJedis();
		for (int indx = clientRequestBody.length() - 1; indx >= 0; indx--) {

			JSONObject entry = clientRequestBody.getJSONObject(indx);

			// Prepare cache key
			String cacheKey = serviceKeyOps.prepareCacheKey(entry);

			if (jedis.exists(cacheKey)) {
				clientRequestBody.remove(indx);
			}
		}
		redisOps.closeRedisResource(jedis);
		return clientRequestBody;
	}

	public void addInCache(Map<String, List<Map<String, Object>>> resultMap,
			JSONArray clientRequestBody) {

		ApplicationProps applicationProps = contextProvider.getCurrentContext()
				.getApplicationProps();
		/*******************
		 * Commenting below code for now, since eka_mdm_isCacheEnabled is no
		 * more mendatory. . Jira Id EPC-1038 @
		 * http://jira.ekaplus.com/browse/EPC-1038
		 *******************/
		// Check if cache is not enabled in properties file

		if (("N")
				.equalsIgnoreCase(applicationProps.getEka_mdm_isCacheEnabled())) {
			return;
		}

		logger.debug(
				Logger.EVENT_SUCCESS,
				ESAPI.encoder().encodeForHTML(
						"Inside addInCache(): Storing/Updating fetched result into cache"
								+ clientRequestBody));
		Jedis jedis = null;
		try {
			jedis = redisOps.getJedis();
		} catch (Exception e) {
			logger.error(
					Logger.EVENT_FAILURE,
					ESAPI.encoder().encodeForHTML(
							"Inside addInCache(): Unable to create redis resource from Redis Pool."
									+ e.getMessage()));
		}

		if (jedis == null) {
			logger.error(
					Logger.EVENT_FAILURE,
					ESAPI.encoder()
							.encodeForHTML(
									"Inside addInCache(): Unable to get redis instance from the pool."));
			return;
		}

		String serviceKey, cacheKey;
		for (int indx = 0; indx < clientRequestBody.length(); indx++) {

			JSONObject clientRequestJsonObj = clientRequestBody
					.getJSONObject(indx);

			// Prepare cache key
			cacheKey = serviceKeyOps.prepareCacheKey(clientRequestJsonObj);

			serviceKey = clientRequestJsonObj.getString(_SERVICEKEY);

			// If value is present fetched/present corresponding to that
			// serviceKey then
			// cache it
			if (resultMap.get(serviceKey) != null) {
				jedis.getSet(cacheKey,
						new Gson().toJson(resultMap.get(serviceKey)));
			}
		}

		redisOps.closeRedisResource(jedis);

		logger.debug(
				Logger.EVENT_SUCCESS,
				ESAPI.encoder().encodeForHTML(
						"Inside addInCache(): Cache is updated."));
	}

	JSONObject metaJsonObject = null;

	@PostMapping("/id2value/object/{uuid}/{sysUuid}")
	public ResponseEntity<Object> getMdmMappedObject(
			@PathVariable("uuid") String uuid,
			@PathVariable("sysUuid") String sysUuid,
			@RequestBody String jsonRequestStrArr, HttpServletRequest request) {

		ApplicationProps applicationProps = contextProvider.getCurrentContext()
				.getApplicationProps();
		String connectMetaEndPoint = applicationProps.getObjectMetaEndpoint()
				+ sysUuid;
		logger.debug(
				Logger.EVENT_SUCCESS,
				ESAPI.encoder().encodeForHTML(
						"Making a GET call to Meta-API at endpoint "
								+ connectMetaEndPoint + " with request: "
								+ request));
		ResponseEntity<String> metaResponseEntity = commonService.fetchMeta(
				connectMetaEndPoint, request);

		if (metaResponseEntity.getStatusCode().equals(HttpStatus.BAD_REQUEST)) {
			logger.error(Logger.EVENT_FAILURE,
					ESAPI.encoder().encodeForHTML(metaResponseEntity.getBody()));
			throw new ConnectException(metaResponseEntity.getBody());
		}

		JSONArray reqArr = new JSONArray();
		try {
			metaJsonObject = new JSONObject(metaResponseEntity.getBody());

			reqArr = new JSONObject(jsonRequestStrArr)
					.getJSONArray(ID2VALUE_FIELD_KEY);
		} catch (JSONException e) {
			logger.error(
					Logger.EVENT_FAILURE,
					ESAPI.encoder()
							.encodeForHTML(
									"The Request body is not as per expected JSONArray format.\nError in calling ide2value: "
											+ e.getMessage()));
			throw new ConnectException(
					"The Request body is not as per expected JSONArray format.\n"
							+ e.getMessage());
		}

		List<Map<String, Object>> resultList = new ArrayList<>();
		for (int indx = 0; indx < reqArr.length(); indx++) {

			Map mapResult = (Map) loopThroughJson(reqArr.getJSONObject(indx),
					new LinkedHashMap<String, Object>());
			resultList.add(mapResult);
		}

		Map<String, Object> resultMap = new HashMap<>();
		resultMap.put(ID2VALUE_FIELD_KEY, resultList);

		return new ResponseEntity<Object>(resultMap, HttpStatus.OK);

	}

	public Object loopThroughJson(Object input, Object result)
			throws JSONException {

		if (input instanceof JSONObject) {

			Iterator<?> keys = ((JSONObject) input).keys();

			while (keys.hasNext()) {

				String key = (String) keys.next();

				if (!(((JSONObject) input).get(key) instanceof JSONArray)) {

					// If Json Object
					if (((JSONObject) input).get(key) instanceof JSONObject) {

						LinkedHashMap map = null;
						if (result instanceof Map) {
							map = (LinkedHashMap) result;
							map.put(key, new LinkedHashMap());
						}
						loopThroughJson(((JSONObject) input).get(key),
								map.get(key));
					} else {
						// If Json String
						// Find serviceKey for this key
						String entityIdValue = ((JSONObject) input).get(key)
								.toString();
						LinkedHashMap resultMap = (LinkedHashMap) result;
						resultMap.put(key, entityIdValue);

						if (!entityIdValue.equals("null")) {

							String cacheKey = getCacheKeyForEntityIdKey(key,
									(JSONObject) input);
							if (!StringUtils.isEmpty(cacheKey)) {
								// If serviceKey is present then add displayText
								// for it
								String entityDisplayText = getMdmDisplayVal(
										entityIdValue, cacheKey);
								resultMap.put(key + ".displayText",
										entityDisplayText);
							}
						}
					}
				} else {
					// If Json Array
					LinkedHashMap map = (LinkedHashMap) result;
					map.put(key, new LinkedList());

					loopThroughJson(new JSONArray(((JSONObject) input).get(key)
							.toString()), map.get(key));
				}
			}
		}

		if (input instanceof JSONArray) {

			List list = (List) result;

			for (int i = 0; i < ((JSONArray) input).length(); i++) {

				JSONObject a = ((JSONArray) input).getJSONObject(i);
				list.add(new LinkedHashMap());
				loopThroughJson(a, list.get(i));
			}
		}

		return result;
	}

	private String getCacheKeyForEntityIdKey(String fieldName,
			JSONObject payload) {

		if (StringUtils.isEmpty(fieldName))
			return null;

		String cacheKey = null;

		try {
			JSONObject fields = metaJsonObject.getJSONObject("fields");

			JSONObject serviceKeyObj = fields.getJSONObject(fieldName);

			// Prepare cache key
			cacheKey = serviceKeyOps.prepareCacheKey(serviceKeyObj, payload);
			logger.debug(
					Logger.EVENT_SUCCESS,
					ESAPI.encoder().encodeForHTML(
							"Prepared cacheKey: " + cacheKey));
		} catch (JSONException e) {
			logger.error(
					Logger.EVENT_FAILURE,
					ESAPI.encoder().encodeForHTML(
							"Error in preparing cacheKey for payload: "
									+ payload + " " + e.getMessage()));
		}

		return cacheKey;
	}

	private String getMdmDisplayVal(String entityId, String cacheKey) {

		String tempKey = null;
		String displayVal = null;

		if (StringUtils.isEmpty(cacheKey) || StringUtils.isEmpty(entityId)) {
			return null;
		}
		Jedis jedis = redisOps.getJedis();
		List<Map<String, String>> mapList = new ArrayList<Map<String, String>>();
		if (jedis.exists(cacheKey)) {
			mapList = new Gson().fromJson(jedis.get(cacheKey),
					new TypeToken<List<Map<String, String>>>() {
					}.getType());
		}

		for (Map<String, String> entryMap : mapList) {

			if (entryMap.get("key") != null) {
				tempKey = entryMap.get("key").toString();

				if (tempKey.equals(entityId)) {
					displayVal = entryMap.get("value");
					break;
				}
			}
		}
		redisOps.closeRedisResource(jedis);
		return displayVal;
	}

	private List<Map<String, Object>> formatQueryResult(String key,
			String value, JSONObject jsonObject) {

		JSONObject item;
		Map<String, String> uniqueEntry = new LinkedHashMap<>();
		List<Map<String, Object>> result = new ArrayList<>();

		JSONArray jsonArray = jsonObject.getJSONArray("data");
		for (int indx = 0; indx < jsonArray.length(); indx++) {

			item = jsonArray.getJSONObject(indx);

			// Logic to feed only unique values
			uniqueEntry.put(item.getString(key), item.getString(value));

		}

		// Format into Key-Value pair
		Map<String, Object> transformedEntry = null;
		Iterator it = uniqueEntry.entrySet().iterator();
		while (it.hasNext()) {

			Map.Entry pair = (Map.Entry) it.next();

			transformedEntry = new LinkedHashMap<>();
			transformedEntry.put("key:", (String) pair.getKey());
			transformedEntry.put("value", (String) pair.getValue());
			result.add(transformedEntry);

		}

		return result;
	}

	/***
	 * Accepts Json Object as part of request body. sample request body: {
	 * "key": "paymentTermId", "id": "PYM-M0-7565", "actionId":
	 * "PYM_PAYMENT_TERMS_MASTER" }
	 * 
	 */

	@RequestMapping(value = "payment/{paymentTermId}", method = RequestMethod.GET, produces = "application/json")
	public String getPaymentTermDetails(
			@PathVariable("paymentTermId") String paymentTermId,
			HttpServletRequest request) {

		String paymentDetails = "";
		HttpHeaders httpRequestHeaders = commonService.getHttpHeader(request,
				true);
		ApplicationProps applicationProps = contextProvider.getCurrentContext()
				.getApplicationProps();

		try {

			JSONObject reqBodyJson = new JSONObject();
			reqBodyJson.put("key", "paymentTermId");
			reqBodyJson.put("id", paymentTermId);
			reqBodyJson.put("actionId", "PYM_PAYMENT_TERMS_MASTER");

			HttpEntity<String> requestBody = new HttpEntity<String>(
					reqBodyJson.toString(), httpRequestHeaders);

			try {
				paymentDetails = restTemplate.postForObject(
						applicationProps.getCtrmSetupDataEndpoint(),
						requestBody, String.class);
			} catch (RestClientException e) {
				logger.error(
						Logger.EVENT_FAILURE,
						ESAPI.encoder().encodeForHTML(
								"Error in calling REST api at endpoint "
										+ applicationProps
												.getCtrmSetupDataEndpoint()
										+ " with requestBody: " + requestBody
										+ " Error: " + e));
			}

		} catch (JSONException e) {
			logger.error(
					Logger.EVENT_FAILURE,
					ESAPI.encoder().encodeForHTML(
							"Error in parsing JSON data in payment/{paymentTermId} endpoint call. Error: "
									+ e.getMessage()));
		}

		return paymentDetails;
	}

	/**
	 * This method is responsible to prepare the actual request body by lookup
	 * {classpath:request-mapping.json} file.
	 */

	private JSONObject getMappedRequest(JSONArray clientRequestArr) {

		JSONObject mappedRequest = new JSONObject();

		try {

			mappedRequest = new JSONObject("{" + _CTRM + ":[], " + _CONNECT
					+ ":[], " + _QUANTITY_CONVERSION + ":[], "
					+ _RATE_CONVERSION + ":[], " + _SUB_CURRENCY_FACTOR
					+ ":[], " + _PLATFORM + ":[], " + _LOGISTIC + ":[], "
					+ _SETUP_ATTRIBUTES + ":[], " + _IGNORE + ":[]}");

			JSONObject singleResourceReqBody = new JSONObject();
			JSONObject currClientReqObj = new JSONObject();

			for (int index = 0; index < clientRequestArr.length(); index++) {

				currClientReqObj = clientRequestArr.getJSONObject(index);

				singleResourceReqBody = getSingleResourceRequest(currClientReqObj);
				if (singleResourceReqBody == null) {
					mappedRequest.getJSONArray(_IGNORE).put(
							currClientReqObj.getString(_SERVICEKEY));
					continue;
				}
            logger.debug(Logger.EVENT_SUCCESS, "###########--mappedservicekey--######"+mappedServiceKey);
            logger.debug(Logger.EVENT_SUCCESS, "###########--singleResourceReqBody--######"+singleResourceReqBody);
				mappedRequest.getJSONArray(
						mappedServiceKey.split(Pattern
								.quote(_SERVICEKEY_DELIMITER))[1]).put(
						singleResourceReqBody);
				mappedServiceKey = "";
			}

		} catch (JSONException e) {
			logger.error(
					Logger.EVENT_FAILURE,
					ESAPI.encoder().encodeForHTML(
							"Error in mapping mdm request payload: "
									+ clientRequestArr + ". Error: "
									+ e.getMessage()), e);
		} catch (Exception e){
			logger.error(
					Logger.EVENT_FAILURE,
					ESAPI.encoder().encodeForHTML(
							"Execption in mapping mdm request payload: "
									+ clientRequestArr + "mappedServiceKey----->"+mappedServiceKey+". Error: "
									+ e.getMessage()), e);
		}

		return mappedRequest;
	}

	/**
	 * Feed the values for secondary attributes to prepare the proper Request
	 * for particular serviceKey.
	 * 
	 * Example : If requested JSON from client is { _SERVICEKEY:
	 * "mdm.ctrm.cityComboDataFromDB", "countryId": "CYM-M0-37013", "type":
	 * "Port" }
	 * 
	 * then the corresponding mapped request will become like
	 * 
	 * { _SERVICEKEY: "cityComboDataFromDB", "dropDownType":"combo",
	 * "attributeOne": "CYM-M0-37013", "attributeThree": "Port" }
	 * 
	 */

	private JSONObject getSingleResourceRequest(JSONObject currClientReqObj)
			throws JSONException {

		JSONObject staticJsonMappedRequestBody = contextProvider
				.getCurrentContext().getServiceKeyMap();

		String serviceKeyFromClient = currClientReqObj.getString(_SERVICEKEY);
		String ctrmEquivalentMappedServiceKey = MDM + _SERVICEKEY_DELIMITER
				+ _CTRM + _SERVICEKEY_DELIMITER + serviceKeyFromClient;
		String connectEquivalentMappedServiceKey = MDM + _SERVICEKEY_DELIMITER
				+ _CONNECT + _SERVICEKEY_DELIMITER + serviceKeyFromClient;
		String platformEquivalentMappedServiceKey = MDM + _SERVICEKEY_DELIMITER
				+ _PLATFORM + _SERVICEKEY_DELIMITER + serviceKeyFromClient;
		String logisticEquivalentMappedServiceKey = MDM + _SERVICEKEY_DELIMITER
				+ _LOGISTIC + _SERVICEKEY_DELIMITER + serviceKeyFromClient;
		String quantConvEquivalentMappedServiceKey = MDM
				+ _SERVICEKEY_DELIMITER + _QUANTITY_CONVERSION
				+ _SERVICEKEY_DELIMITER + serviceKeyFromClient;
		String rateConvEquivalentMappedServiceKey = MDM + _SERVICEKEY_DELIMITER
				+ _RATE_CONVERSION + _SERVICEKEY_DELIMITER
				+ serviceKeyFromClient;
		String subCurrFactorEquivalentMappedServiceKey = MDM
				+ _SERVICEKEY_DELIMITER + _SUB_CURRENCY_FACTOR
				+ _SERVICEKEY_DELIMITER + serviceKeyFromClient;
		String setupAttributesServiceKey = MDM + _SERVICEKEY_DELIMITER
				+ _SETUP_ATTRIBUTES + _SERVICEKEY_DELIMITER
				+ serviceKeyFromClient;

		// Follow the precedence of for service key present in ctrm, if not then
		// check
		// in connect, if not then check in platform

		if (staticJsonMappedRequestBody.has(ctrmEquivalentMappedServiceKey)) {
			mappedServiceKey = ctrmEquivalentMappedServiceKey;
		} else if (staticJsonMappedRequestBody
				.has(connectEquivalentMappedServiceKey)) {
			mappedServiceKey = connectEquivalentMappedServiceKey;
		} else if (staticJsonMappedRequestBody
				.has(platformEquivalentMappedServiceKey)) {
			mappedServiceKey = platformEquivalentMappedServiceKey;
		} else if (staticJsonMappedRequestBody
				.has(logisticEquivalentMappedServiceKey)) {
			mappedServiceKey = logisticEquivalentMappedServiceKey;
		} else if (staticJsonMappedRequestBody
				.has(quantConvEquivalentMappedServiceKey)) {
			mappedServiceKey = quantConvEquivalentMappedServiceKey;
		} else if (staticJsonMappedRequestBody
				.has(rateConvEquivalentMappedServiceKey)) {
			mappedServiceKey = rateConvEquivalentMappedServiceKey;
		} else if (staticJsonMappedRequestBody
				.has(subCurrFactorEquivalentMappedServiceKey)) {
			mappedServiceKey = subCurrFactorEquivalentMappedServiceKey;
		} else if (staticJsonMappedRequestBody.has(setupAttributesServiceKey)) {
			mappedServiceKey = setupAttributesServiceKey;
		}

		if (StringUtils.isEmpty(mappedServiceKey))
			return null;

		String mappedReqObjStr = staticJsonMappedRequestBody.getJSONObject(
				mappedServiceKey).toString();

		// Feed the dependencies passed from client to construct the request
		// object, if
		// any.

		JSONArray dependencies = null;
		if (currClientReqObj.has(dependencyKey)) {
			dependencies = currClientReqObj.getJSONArray(dependencyKey);
		}

		if (dependencies != null) {
			for (int indx = 0; indx < dependencies.length(); indx++) {
				mappedReqObjStr = mappedReqObjStr.replaceFirst("\\$.*?\\$",
						dependencies.getString(indx));
			}
		}

		// Remove unfilled dependency entry.
		JSONObject mappedJsonObjTemp = new JSONObject(mappedReqObjStr.trim());
		JSONObject mappedJsonObj = new JSONObject();

		Iterator<String> keys = mappedJsonObjTemp.keys();
		// Ignoring references that are not replaced with values
		while (keys.hasNext()) {
			String key = keys.next();
			if (mappedJsonObjTemp.get(key) instanceof String) {
				if (!mappedJsonObjTemp.getString(key).contains("$")) {
					mappedJsonObj
							.putOnce(key, mappedJsonObjTemp.getString(key));
				}
			} else if (mappedJsonObjTemp.get(key) instanceof JSONArray) {
				// assumption : Array holds only string values
				JSONArray list = mappedJsonObjTemp.getJSONArray(key);
				for (int i = 0; i < list.length(); i++) {
					if (list.getString(i).contains("$")) {
						list.remove(i);
					}
				}
				if (!list.isEmpty()) {
					mappedJsonObj.putOnce(key, list);
				}
			}

		}

		return mappedJsonObj;
	}

	/**
	 * <p>
	 * <code>getCorporateInfo</code> is exposed to get corporate details
	 * <p>
	 * <hr>
	 * 
	 * @author Suresh
	 */

	@GetMapping(value = "/{uuid}/corporateInfo")
	public Map<String, String> getCorporateInfo(@PathVariable String uuid,
			HttpServletRequest request) {

		HttpEntity<?> entity = new HttpEntity<>(commonService.getHttpHeader(
				request, true));
		Map<String, String> corpInfo = new HashMap<>();

		ApplicationProps applicationProps = contextProvider.getCurrentContext()
				.getApplicationProps();

		try {

			URI url = new URI(validator.cleanData(applicationProps.getCorporateInfoEndPoint()));

			ResponseEntity<Map> resultResp = restTemplate.exchange(url,
					HttpMethod.GET, entity, Map.class);

			if (resultResp.getStatusCodeValue() == HttpStatus.OK.value()) {
				corpInfo = resultResp.getBody();
			}

		} catch (HttpStatusCodeException e) {

			throw new ConnectException(
					"Failed to fetch corporate info from TRM side. Reason: "
							+ e.getResponseBodyAsString());

		} catch (URISyntaxException e) {

			throw new DataException("Unable to form URI. Reason: "
					+ e.getMessage());

		} catch (Exception e) {

			throw new DataException(
					"Unable to parse response from api-endpoint: "
							+ applicationProps.getCorporateInfoEndPoint()
							+ "\n" + e.getMessage());

		}
		return corpInfo;
	}

	/**
	 * <p>
	 * <code>getProductConstituent</code> is used to expose to get
	 * ProductConstituent details
	 * <p>
	 * 
	 * @author suresh
	 */
	@GetMapping(value = "/productConstituent/{uuid}/{productId}", produces = "application/json")
	public Object getProductConstituent(
			@PathVariable("uuid") String uuid,
			@PathVariable("productId") String productId,
			HttpServletRequest request) {

		HttpEntity<?> entity = new HttpEntity<>(
				commonService.getHttpHeader(request));
		ResponseEntity<String> resultResp = null;

		ApplicationProps applicationProps = contextProvider.getCurrentContext()
				.getApplicationProps();

		try {

			URI url = new URI(validator.cleanData(applicationProps
					.getProductConstituentEndPoint() + "/" + productId));

			resultResp = restTemplate.exchange(url, HttpMethod.GET, entity,
					String.class);

		} catch (HttpStatusCodeException e) {

			throw new ConnectException(
					"Failed to fetch Product Constituent info from TRM side. Reason: "
							+ e.getResponseBodyAsString());

		} catch (URISyntaxException e) {

			throw new DataException("Unable to form URI. Reason: "
					+ e.getMessage());

		} catch (Exception e) {

			throw new DataException(
					"Unable to parse response from api-endpoint: "
							+ applicationProps.getProductConstituentEndPoint()
							+ "\n" + e.getMessage());
		}
		return resultResp;
	}

	@ExceptionHandler(value = { SocketTimeoutException.class })
	public ResponseEntity<String> handleSocketTimeoutException(
			SocketTimeoutException ste) {
		ResponseEntity<String> responseEntity = null;

		logger.error(
				Logger.EVENT_FAILURE,
				ESAPI.encoder().encodeForHTML(
						"Error in connecting to CTRM/Connect server"
								+ ste.getMessage()), ste);

		responseEntity = new ResponseEntity<String>(
				"Error in connecting to CTRM/Connect server" + ste.getMessage(),
				HttpStatus.INTERNAL_SERVER_ERROR);
		return responseEntity;
	}

	@ExceptionHandler(value = { JSONException.class })
	public ResponseEntity<String> handleJSONException(JSONException jsone) {
		ResponseEntity<String> responseEntity = null;

		logger.error(
				Logger.EVENT_FAILURE,
				ESAPI.encoder().encodeForHTML(
						"Error in parsing mdm data" + jsone.getMessage()),
				jsone);

		responseEntity = new ResponseEntity<String>("Error in parsing mdm data"
				+ jsone.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		return responseEntity;
	}

	@ExceptionHandler(value = { ConnectException.class })
	public ResponseEntity<String> handleConnectException(
			ConnectException connectException) {
		ResponseEntity<String> responseEntity = null;

		logger.error(
				Logger.EVENT_FAILURE,
				ESAPI.encoder().encodeForHTML(
						"Error in get data from connect "
								+ connectException.getMessage()),
				connectException);
		responseEntity = new ResponseEntity<String>(
				connectException.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
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

		responseEntity = new ResponseEntity<String>(
				"Error in getting  mdm data: " + ex.getMessage(),
				HttpStatus.INTERNAL_SERVER_ERROR);
		return responseEntity;
	}

}
