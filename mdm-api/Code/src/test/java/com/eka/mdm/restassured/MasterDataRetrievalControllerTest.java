package com.eka.mdm.restassured;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

import org.json.JSONArray;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;

import io.restassured.RestAssured;

@RunWith(MockitoJUnitRunner.Silent.class)
public class MasterDataRetrievalControllerTest {

	private static String tenant;
	private static String platformURI;
	private static String base64credentials;
	private static String validationUrl;

	static String token = null;

	private static final String locale = "en_US";
	private static final String content = "application/json";

	@BeforeClass
	public static void setUp() throws Exception {

		Properties prop = new Properties();
		prop.load(new FileInputStream(new File(".\\src\\main\\resources\\application.properties")));

		tenant = prop.getProperty("tenant");
		platformURI = prop.getProperty("platformURI");
		base64credentials = prop.getProperty("base64credentials");
		validationUrl = prop.getProperty("validationUrl");

		RestAssured.baseURI = platformURI;
		token = getAuthToken(base64credentials, validationUrl);
		base64credentials = "Basic YWRtaW5AZWthcGx1cy5jb206YWRtaW5AZWthcGx1cy5jb20=";

		// Now ready to test MDM APIs
		RestAssured.baseURI = "http://localhost";
		RestAssured.port = 1111;
	}

	public static String getAuthToken(String credentialsBase64, String validationUrl) {

		String authToken = given().header("Authorization", credentialsBase64).header("Content-Type", content).when()
				.post(validationUrl).then().extract().jsonPath().get("auth2AccessToken.access_token");

		return authToken;
	}

	@Test
	public void test_getMasterData_check_for_access() {

		String bodyStr = "[\r\n" + "    {	\r\n" + "    	\"serviceKey\":\"limitStatus\"\r\n" + "    }\r\n" + "]";
		given().header("Authorization", token).header("Content-Type", content).header("X-TenantID", tenant)
				.header("X-Locale", locale).body(new JSONArray(bodyStr).toString()).when()
				.post("/mdm/5d907cd2-7785-4d34-bcda-aa84b2158415/data").then().statusCode(200);

	}

	@Test
	public void test_getMasterData_check_for_empty_body() {

		given().header("Authorization", token).header("Content-Type", content).header("X-TenantID", tenant)
				.header("X-Locale", locale).when().post("/mdm/5d907cd2-7785-4d34-bcda-aa84b2158415/data").then()
				.statusCode(500);

	}

	@Test
	public void test_getMasterData_check_for_invalid_serviceKey() {

		String bodyStr = "[\r\n" + "\r\n" + "    {	\r\n" + "    	\"serviceKey\":\"serviceKeyNotPresent\"\r\n"
				+ "    }\r\n" + "]";

		String value = given().header("Authorization", token).header("Content-Type", content)
				.header("X-TenantID", tenant).header("X-Locale", locale).body(new JSONArray(bodyStr).toString()).when()
				.post("/mdm/5d907cd2-7785-4d34-bcda-aa84b2158415/data").then().assertThat()
				.statusCode(HttpStatus.OK.value()).extract().path("serviceKeyNotPresent");

		assertThat(value).isEqualTo(null);
	}

	@Test
	public void test_getMdmMappedObject_check_for_access() {

		String bodyStr = "[{\r\n" + "	\"itemDetails\": [\r\n" + "		{\r\n"
				+ "			\"toleranceType\": \"Percentage\",\r\n" + "			\"toleranceLevel\": \"Buyer\",\r\n"
				+ "			\"profitCenterId\": \"CPC-M4-4876\"      \r\n" + "		}\r\n" + "	]\r\n" + "}]";
		given().header("Authorization", token).header("Content-Type", content).header("X-TenantID", tenant)
				.header("X-Locale", locale).body(new JSONArray(bodyStr).toString()).when()
				.post("/mdm/id2value/object/5d907cd2-7785-4d34-bcda-aa84b2158415/contract").then().statusCode(200);

	}

	@Test
	public void test_getMdmMappedObject_check_for_empty_body() {

		given().header("Authorization", token).header("Content-Type", content).header("X-TenantID", tenant)
				.header("X-Locale", locale).when()
				.post("/mdm/id2value/object/5d907cd2-7785-4d34-bcda-aa84b2158415/contract").then().statusCode(500);

	}

}
