package com.eka.mdm.dataobject;

import org.owasp.esapi.ESAPI;
import org.owasp.esapi.Logger;
import org.springframework.util.StringUtils;

/**
 * <p>
 * <code>EnvirnomentProps</code> consists all the Environment property related
 * to mdm application. These property will be initialized by calling property
 * API at Handler Interceptor.
 * <p>
 * <hr>
 * 
 * @author Ranjan.Jha
 * @version 1.0
 */

public class EnvirnomentProps {

	final static Logger logger = ESAPI.getLogger(EnvirnomentProps.class);
	
	private String eka_ctrm_host;
	private String eka_connect_host;
	private String eka_utility_host;
	private String platform_url;
	private String eka_redis_host;
	private String eka_redis_port;
	private String eka_mdm_isCacheEnabled;
	private String eka_supplierconnect_host;
	private String eka_redis_password;

	public String getEka_ctrm_host() {

		return eka_ctrm_host;
	}

	public void setEka_ctrm_host(String eka_ctrm_host) {

		this.eka_ctrm_host = eka_ctrm_host;
	}

	public String getEka_connect_host() {
		return eka_connect_host;
	}

	public void setEka_connect_host(String eka_connect_host) {

		this.eka_connect_host = eka_connect_host;
	}

	public String getEka_utility_host() {
		return eka_utility_host;
	}

	public void setEka_utility_host(String eka_utility_host) {

		this.eka_utility_host = eka_utility_host;
	}

	public String getEka_platform_host() {
		return platform_url;
	}

	public void setEka_platform_host(String eka_platform_host) {

		this.platform_url = eka_platform_host;
	}

	public String getEka_redis_host() {

		return eka_redis_host;
	}

	public void setEka_redis_host(String eka_redis_host) {

		// Set this value to default. Refer Jira: http://jira.ekaplus.com/browse/EPC-892
		if (StringUtils.isEmpty(eka_redis_host))
			eka_redis_host = "localhost";

		this.eka_redis_host = eka_redis_host;
	}

	public String getEka_redis_port() {

		return eka_redis_port;
	}

	public void setEka_redis_port(String eka_redis_port) {

		// Set this value to default. Refer Jira: http://jira.ekaplus.com/browse/EPC-892
		if (StringUtils.isEmpty(eka_redis_port))
			eka_redis_port = "6379";

		this.eka_redis_port = eka_redis_port;
	}

	public String getEka_mdm_isCacheEnabled() {
		return eka_mdm_isCacheEnabled;
	}

	public void setEka_mdm_isCacheEnabled(String eka_mdm_isCacheEnabled) {

		// Default this value to "N".
		if (StringUtils.isEmpty(eka_mdm_isCacheEnabled))
			eka_mdm_isCacheEnabled = "Y";

		this.eka_mdm_isCacheEnabled = eka_mdm_isCacheEnabled;
	}
	

	public String getEka_supplierconnect_host() {
		return eka_supplierconnect_host;
	}

	public void setEka_supplierconnect_host(String eka_supplierconnect_host) {
		this.eka_supplierconnect_host = eka_supplierconnect_host;
	}

	@Override
	public String toString() {
		return "EnvirnomentProps [eka_ctrm_host=" + eka_ctrm_host + ", eka_connect_host=" + eka_connect_host
				+ ", eka_utility_host=" + eka_utility_host + ", platform_url=" + platform_url + ", eka_redis_host="
				+ eka_redis_host + ", eka_redis_port=" + eka_redis_port + ", eka_mdm_isCacheEnabled="
				+ eka_mdm_isCacheEnabled + ", eka_supplierconnect_host=" + eka_supplierconnect_host +"]";
	}

	public String getEka_redis_password() {
		return eka_redis_password;
	}

	public void setEka_redis_password(String eka_redis_password) {
		this.eka_redis_password = eka_redis_password;
	}

}
