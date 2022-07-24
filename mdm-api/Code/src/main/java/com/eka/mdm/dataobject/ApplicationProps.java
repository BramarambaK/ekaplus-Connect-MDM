package com.eka.mdm.dataobject;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * <p>
 * <code>ApplicationProps</code> consists all property related to mdm
 * application.
 * <p>
 * <hr>
 * 
 * @author Ranjan.Jha
 * @version 1.0
 */

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class ApplicationProps extends EnvirnomentProps {

	@Value("${eka_connect_host}")
	private String connectHost;

	private final String ctrmMdmEndpoint = "/mdmapi/data";
	private final String quantConvEndpoint = "/mdmapi/converter/quantity";
	private final String fxRateConvEndpoint = "/mdmapi/converter/fxRate";
	private final String currencyFactorEndpoint = "/mdmapi/subcurrency/factor";
	private final String ctrmSetupDataEndpoint = "/mdmapi/setupdata";
	private final String productEndpoint = "/mdmapi/product";

	private final String connectMdmEndpoint = "/mdm/data";
	private final String metaEndpoint = "/meta/";
	private final String objectMetaEndpoint = "/meta/object/";

	private final String collectionEntityEndpoint = "/collection/mdm/entity";
	private final String collectionDestEndpoint = "/collection/mdm/destination";
	private final String incoTermEndpoint = "/collection/mdm/incoTerm";

	private final String platformCollectionEndpoint = "/spring/smartapp/collection/data";

	private final String authValidateUrl = "/cac-security/api/oauth/validate_token?";
	private final String authEndpoint = "deviceIdentifier=1&token=";

	private String authServerEndpoint = authValidateUrl + authEndpoint;

	private final String corporateInfoEndPoint = "/mdmapi/corporateInfo";

	private final String productConstituentEndPoint = "/mdmapi/productConstituent";
	private final String ctrmSetupMasterDataEndPoint = "/mdmapi/masterdata/";

	private final String mdmMasterDataEndPoint = "/mdmapi/masterdatas";
	
	private final String mdmEndPoint = "/mdmapi";
	
	public String getCtrmMdmEndpoint() {
		return getEka_ctrm_host() + ctrmMdmEndpoint;
	}

	public String getQuantConvEndpoint() {
		return getEka_ctrm_host() + quantConvEndpoint;
	}

	public String getFxRateConvEndpoint() {
		return getEka_ctrm_host() + fxRateConvEndpoint;
	}

	public String getCurrencyFactorEndpoint() {
		return getEka_ctrm_host() + currencyFactorEndpoint;
	}

	public String getCtrmSetupDataEndpoint() {
		return getEka_ctrm_host() + ctrmSetupDataEndpoint;
	}

	public String getProductEndpoint() {
		return getEka_ctrm_host() + productEndpoint;
	}

	public String getConnectMdmEndpoint() {
		return connectHost + connectMdmEndpoint;
	}

	public String getMetaEndpoint() {
		return connectHost + metaEndpoint;
	}

	public String getObjectMetaEndpoint() {
		return connectHost + objectMetaEndpoint;
	}

	public String getCollectionEntityEndpoint() {
		return getEka_supplierconnect_host() + collectionEntityEndpoint;
	}

	public String getCollectionDestEndpoint() {
		return getEka_supplierconnect_host() + collectionDestEndpoint;
	}

	public String getIncoTermEndpoint() {
		return getEka_supplierconnect_host() + incoTermEndpoint;
	}

	public String getPlatformCollectionEndpoint() {
		return getEka_platform_host() + platformCollectionEndpoint;
	}

	public String getAuthServerEndpoint() {

		return getEka_platform_host() + authServerEndpoint;
	}

	public String getCorporateInfoEndPoint() {
		return getEka_ctrm_host() + corporateInfoEndPoint;
	}

	public String getProductConstituentEndPoint() {
		return getEka_ctrm_host() + productConstituentEndPoint;
	}

	public String getCtrmSetupMasterDataEndPoint() {
		return getEka_ctrm_host() + ctrmSetupMasterDataEndPoint;
	}

	public String getMdmMasterDataEndPoint() {
		return getEka_ctrm_host() + mdmMasterDataEndPoint;
	}

	public String getMdmEndPoint() {
		return getEka_ctrm_host() + mdmEndPoint;
	}
	

}
