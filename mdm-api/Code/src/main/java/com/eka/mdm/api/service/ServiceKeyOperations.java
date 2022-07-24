package com.eka.mdm.api.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


import org.json.JSONArray;
import org.json.JSONObject;
import org.owasp.esapi.ESAPI;
import org.owasp.esapi.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.eka.mdm.api.util.ContextProvider;
import com.eka.mdm.dataobject.RequestContext;

@Service
public class ServiceKeyOperations {

	@Value("${mdm.dependent.key.name}")
	private String dependencyKey;
	@Autowired
	private ContextProvider contextProvider;

	private final String _SERVICEKEY = "serviceKey";
	private final String _SKEY_DELIMITER = "-";


	final static Logger logger = ESAPI.getLogger(ServiceKeyOperations.class);
	
	public String prepareCacheKey(JSONObject clientRequestObj) {

		String serviceKey = clientRequestObj.getString(_SERVICEKEY);
		//Integer userId = contextProvider.getCurrentContext().getTokenData().getUserId();

		JSONArray dependencies = null;
		if (clientRequestObj.has(dependencyKey)) {
			dependencies = clientRequestObj.getJSONArray(dependencyKey);
		}

		String dependenciesStr = "";
		if (dependencies != null) {
			for (int indx = 0; indx < dependencies.length(); indx++) {
				dependenciesStr = dependenciesStr + _SKEY_DELIMITER + dependencies.getString(indx);
			}
		}
		return serviceKey + _SKEY_DELIMITER  +dependenciesStr;
	}

	public String prepareCacheKey(JSONObject serviceKeyEntityPayload, JSONObject dependentEntityPayload) {

		String serviceKey = serviceKeyEntityPayload.getString(_SERVICEKEY);
		//Integer userId = contextProvider.getCurrentContext().getTokenData().getUserId();

		JSONArray dependencies = null;
		if (serviceKeyEntityPayload.has(dependencyKey)) {
			dependencies = serviceKeyEntityPayload.getJSONArray(dependencyKey);
		}

		String dependenciesStr = "";
		String dependentKeyStr = "";
		if (dependencies != null) {
			for (int indx = 0; indx < dependencies.length(); indx++) {

				dependentKeyStr = dependencies.getString(indx);
				dependenciesStr = dependenciesStr + _SKEY_DELIMITER + dependentEntityPayload.getString(dependentKeyStr);
			}
		}

		return serviceKey + _SKEY_DELIMITER + dependenciesStr;
	}

	public List<Map<String, Object>> prepareErrorList(JSONArray jsonArray, String identifier, String message) {

		Map<String, Object> serviceKeyErrorPairs = new LinkedHashMap<String, Object>();
		for (int indx = 0; indx < jsonArray.length(); indx++) {
			serviceKeyErrorPairs.put(jsonArray.getJSONObject(indx).get(identifier).toString(), message);
		}

		ArrayList<Map<String, Object>> arrayList = new ArrayList<Map<String, Object>>();
		arrayList.add(serviceKeyErrorPairs);
		return arrayList;
	}

}
