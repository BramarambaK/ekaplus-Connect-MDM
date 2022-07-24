package com.eka.mdm;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.eka.mdm.api.controller.MasterDataRetrievalController;

@RunWith(MockitoJUnitRunner.Silent.class)
public class MasterDataRetrievalControllerTest {

	@InjectMocks
	MasterDataRetrievalController masterDataRetrievalController;

	@Mock
	MultiValueMap<String, String> reqParam;

	@Mock
	HttpServletRequest request;

	@Mock
	RestTemplate restTemplate;
	@Mock
	JSONObject staticJsonMappedRequestBody;

	@Test
	public void getMasterDatumSuccessTest() {
		String expectedValue = "{\"carryChargeRateTypeList\":[{\"value\":\"Rate\",\"key\":\"Rate\"},{\"value\":\"% of Base Price\",\"key\":\"Pct of Base Price\"}]}";

		String ctrmEndPoint = "http://localhost:8080";
		ReflectionTestUtils.setField(this.masterDataRetrievalController, "ctrmEndPoint", ctrmEndPoint);
		ReflectionTestUtils.setField(this.masterDataRetrievalController, "restTemplate", restTemplate);

		JSONObject objectData = new JSONObject();
		objectData.put("key", "Rate");
		objectData.put("value", "Rate");

		JSONArray arrayData = new JSONArray();
		arrayData.put(objectData);

		JSONObject objectData1 = new JSONObject();
		objectData1.put("key", "Pct of Base Price");
		objectData1.put("value", "% of Base Price");

		arrayData.put(objectData1);

		JSONObject responseBody = new JSONObject().put("carryChargeRateTypeList", arrayData);

		ResponseEntity<Object> result = new ResponseEntity<Object>(responseBody.toString(), HttpStatus.ACCEPTED);
		Mockito.doReturn(result).when(restTemplate).exchange(ArgumentMatchers.anyString(),
				ArgumentMatchers.any(org.springframework.http.HttpMethod.class), ArgumentMatchers.<HttpEntity<?>>any(),
				ArgumentMatchers.<Class<Object>>any());
		String actualValue = masterDataRetrievalController.getMasterDatum("", reqParam, request);
		assertEquals(expectedValue, actualValue);

	}

	@Test
	public void getMasterDataCTRMTest() {

		JSONObject staticMappedObj = new JSONObject();
		JSONObject arb1 = new JSONObject();
		arb1.put("serviceKey", "carryChargeRateTypeList");
		arb1.put("isStatic", "Yes");
		staticMappedObj.put("mdm.ctrm.carryChargeRateTypeList", arb1);

		ReflectionTestUtils.setField(this.masterDataRetrievalController, "staticJsonMappedRequestBody", staticMappedObj);
		ReflectionTestUtils.setField(this.masterDataRetrievalController, "restTemplate", restTemplate);
		String ctrmEndPoint = "http://localhost:8080";
		ReflectionTestUtils.setField(this.masterDataRetrievalController, "ctrmEndPoint", ctrmEndPoint);
		String requestBodyStr = "[\r\n" + "	\r\n" + "    \r\n" + "    {	\r\n"
				+ "    	\"serviceKey\": \"carryChargeRateTypeList\"\r\n" + "    },\r\n" + "	\r\n" + "]";

		List<Object> objList = new ArrayList<>();
		Map<String, String> mapDem = new HashMap<>();
		mapDem.put("key", "Rate");
		mapDem.put("value", "Rate");
		objList.add(mapDem);
		Map<String, String> mapDem2 = new HashMap<>();
		mapDem2.put("key", "Pct of Base Price");
		mapDem2.put("value", "% of Base Price");
		objList.add(mapDem2);
		Map<String, List<Object>> expectedResultMap = new LinkedHashMap<>();
		expectedResultMap.put("carryChargeRateTypeList", objList);

		Mockito.doReturn(expectedResultMap).when(restTemplate).postForObject(ArgumentMatchers.anyString(),
				ArgumentMatchers.any(), ArgumentMatchers.<Class<Object>>any());

		Map<String, List<Map<String, Object>>> actualResultMap = masterDataRetrievalController.getMasterData("",
				requestBodyStr, request);

		assertEquals(expectedResultMap, actualResultMap);
	}

	@Test
	public void getMasterDataConnectTest() {

		JSONObject staticMappedObj = new JSONObject();
		JSONObject arb1 = new JSONObject();
		arb1.put("serviceKey", "carryChargeRateTypeList");
		arb1.put("isStatic", "Yes");
		staticMappedObj.put("mdm.connect.fx", arb1);

		ReflectionTestUtils.setField(this.masterDataRetrievalController, "staticJsonMappedRequestBody", staticMappedObj);
		ReflectionTestUtils.setField(this.masterDataRetrievalController, "restTemplate", restTemplate);
		String ctrmEndPoint = "http://localhost:8080";
		String connectEndPoint = "http://localhost:8180";
		ReflectionTestUtils.setField(this.masterDataRetrievalController, "ctrmEndPoint", ctrmEndPoint);
		ReflectionTestUtils.setField(this.masterDataRetrievalController, "connectEndPoint", connectEndPoint);
		String requestBodyStr = "[\r\n" + "	\r\n" + "    \r\n" + "    {	\r\n" + "    	\"serviceKey\": \"fx\"\r\n"
				+ "    },\r\n" + "	\r\n" + "]";

		List<Object> objList = new ArrayList<>();
		Map<String, String> mapDem = new HashMap<>();
		mapDem.put("id", "fixed");
		mapDem.put("value", "Fixed");
		mapDem.put("objectName", "fx");
		mapDem.put("appName", "mdm");
		objList.add(mapDem);
		Map<String, String> mapDem2 = new HashMap<>();
		mapDem2.put("id", "curve");
		mapDem2.put("value", "Curve");
		mapDem2.put("objectName", "fx");
		mapDem2.put("appName", "mdm");
		objList.add(mapDem2);
		Map<String, List<Object>> expectedResultMap = new LinkedHashMap<>();
		expectedResultMap.put("fx", objList);

		Mockito.doReturn(expectedResultMap).when(restTemplate).postForObject(ArgumentMatchers.anyString(),
				ArgumentMatchers.any(), ArgumentMatchers.<Class<Object>>any());

		Map<String, List<Map<String, Object>>> actualResultMap = masterDataRetrievalController.getMasterData("",
				requestBodyStr, request);

		assertEquals(expectedResultMap, actualResultMap);
	}

	@Test
	public void getMasterDataIgnoreTest() {

		JSONObject staticMappedObj = new JSONObject();
		JSONObject arb1 = new JSONObject();
		arb1.put("serviceKey", "carryChargeRateTypeList");
		arb1.put("isStatic", "Yes");
		staticMappedObj.put("mdm.ctrm.carryChargeRateTypeList", arb1);

		ReflectionTestUtils.setField(this.masterDataRetrievalController, "staticJsonMappedRequestBody", staticMappedObj);
		String requestBodyStr = "[\r\n" + "	\r\n" + "    \r\n" + "    {	\r\n"
				+ "    	\"serviceKey\": \"carryChargeRate\"\r\n" + "    },\r\n" + "	\r\n" + "]";

		Map<String, List<Object>> expectedResultMap = new LinkedHashMap<>();
		expectedResultMap.put("carryChargeRate", null);

		Map<String, List<Map<String, Object>>> actualResultMap = masterDataRetrievalController.getMasterData("",
				requestBodyStr, request);

		assertEquals(expectedResultMap, actualResultMap);
	}
}
