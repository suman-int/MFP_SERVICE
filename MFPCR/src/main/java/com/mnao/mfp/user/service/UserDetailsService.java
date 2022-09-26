package com.mnao.mfp.user.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.mnao.mfp.common.util.Utils;
import com.mnao.mfp.user.dao.MFPUser;

@Service
public class UserDetailsService {
	private static final Logger log = LoggerFactory.getLogger(UserDetailsService.class);

	private String[][] defaultHeaderTokens = { { "RS_SEC_HDR_IV_NAME", "VFQXyd4hz5SETe39G5mvJQ==" },
			{ "RS_SEC_HDR_TOKEN_NAME",
					"QXc0UTKC5D8o3YE7A0shwnqkWaloIetAgYjihm+NYpbKEIDtxMS+WAqLuHSPNZbUqYUT14n7jOPpXcM0x1OaoQ==" },
			{ "RS_SEC_HDR_VENDOR_ID", "MFPMNAOIT" } };
	private String[][] uriStrings = { { "TEST", "http://dcs371.mnao.net/WSLUserInfoService/users/%s/domain/yes" },
			{ "QA", "http://dcs275.mnao.net/WSLUserInfoService/users/%s/domain/yes" },
			{ "PROD", "http://dcs175.mnao.net/WSLUserInfoService/users/%s/domain/yes" } };
	private Map<String, String> uris = new HashMap<String, String>();
	private String activeURI = null;
	private String[][] headerTokens = new String[3][2];
	//
	private String staticJsonFile = "/testUserAttribs.json";
	private HashMap<String, String> mfpUsers = null;
	//
	private static Properties wslProperties = new Properties();

	//
	public UserDetailsService() {
		uris.put(uriStrings[0][0], uriStrings[0][1]);
		uris.put(uriStrings[1][0], uriStrings[1][1]);
		uris.put(uriStrings[2][0], uriStrings[2][1]);
		if (wslProperties.size() == 0) {
			activeURI = uris.get("QA");
			for (int i = 0; i < defaultHeaderTokens.length; i++) {
				headerTokens[i][0] = defaultHeaderTokens[i][0];
				headerTokens[i][1] = defaultHeaderTokens[i][1];
			}
		} else {
			activeURI = wslProperties.getProperty("SVC_URL");
			for (int i = 0; i < defaultHeaderTokens.length; i++) {
				headerTokens[i][0] = defaultHeaderTokens[i][0];
				headerTokens[i][1] = wslProperties.getProperty(headerTokens[i][0]);
			}
		}
		//
		if (mfpUsers == null) {
			mfpUsers = new HashMap<String, String>();
//			if (staticJsonFile != null && staticJsonFile.trim().length() > 0) {
//				Map<String, String> staticJsons = loadStaticJson(staticJsonFile);
//				if (staticJsons != null)
//					mfpUsers.putAll(staticJsons);
//			}
		}
	}

	public static Properties getWslProperties() {
		return wslProperties;
	}

	public static void setWslProperties(Properties wslProperties) {
		UserDetailsService.wslProperties = wslProperties;
	}

	public MFPUser getMFPUser(String userID) {
		log.debug("Fetching details for user : " + userID);
		MFPUser mfpUser = null;
		String userInfoJson = null;
		if (mfpUsers.size() > 0)
			userInfoJson = mfpUsers.get(userID.toLowerCase());
		if (userInfoJson == null) {
			userInfoJson = fetchUserDetails(userID);
		}
		if (userInfoJson != null) {
			mfpUsers.put(userID, userInfoJson);
			mfpUser = processUserInfoJson(userInfoJson);
		}
		if (mfpUser == null)
			log.debug("FAILED to retrieve user details for {}", userID);
		else
			log.debug("Returning from getMFPUser: EMP {}, WSLID {}", mfpUser.getEmployeeNumber(), mfpUser.getUserid());
		return mfpUser;
	}

//
	private boolean isInMnaoNet() {
		String fqdn = "";
		try {
			java.net.InetAddress addr = java.net.InetAddress.getLocalHost();
			fqdn = addr.getCanonicalHostName();
			log.debug("FQDN : " + fqdn);
			log.debug("FQDN : " + fqdn);
		} catch (Exception e) {
		}
		// return fqdn.endsWith("mnao.net");
		return true;
	}

	//
	private Map<String, String> loadStaticJson(String staticJsonFile) {
		List<Map<Object, Object>> jsons = null;
		Map<String, String> retJsons = new HashMap<String, String>();
		// File jf = new File(staticJsonFile);
		try {
//			String allJsonsStr = new String(Files.readAllBytes(jf.toPath()));
			String allJsonsStr = readUserJsonFromFile(staticJsonFile);
			if (allJsonsStr != null) {
				allJsonsStr = allJsonsStr.trim();
				if (allJsonsStr.length() > 0 && (allJsonsStr.startsWith("[") || allJsonsStr.startsWith("{"))) {
					ObjectMapper mapper = new ObjectMapper();
					jsons = mapper.readValue(allJsonsStr, new TypeReference<List<Map<Object, Object>>>() {
					});
					for (int i = 0; i < jsons.size(); i++) {
						ObjectMapper m1 = new ObjectMapper();
						String jstr = m1.writeValueAsString(jsons.get(i)).toString();
						MFPUser user = processUserInfoJson(jstr);
						if (user != null) {
							retJsons.put(user.getUserid(), jstr);
							log.debug("Loaded from static JSON : " + user.getUserid());
						}
					}
				}
			}
		} catch (IOException e) {
			log.error("", e);
		}
		return retJsons;
	}

	private String readUserJsonFromFile(String fPath) {
		StringBuilder sb = new StringBuilder();
		try (InputStream is = Utils.class.getResourceAsStream(fPath)) {
			if (is != null) {
				BufferedReader reader = new BufferedReader(new InputStreamReader(is));
				String str = null;
				while ((str = reader.readLine()) != null) {
					sb.append(str + "\n");
				}
			}
		} catch (Exception e) {
			log.error("ERROR Reading data from text file : " + fPath, e);
		}
		return sb.toString();
	}

	//
	private String fetchUserDetails(String user) {
		String rv = null;
		String qStrUri = String.format(activeURI, user);
		HttpGet request = new HttpGet(qStrUri);

		// add request headers
		for (int i = 0; i < headerTokens.length; i++) {
			request.addHeader(headerTokens[i][0], headerTokens[i][1]);
		}

		try (CloseableHttpClient httpClient = HttpClients.createDefault();
				CloseableHttpResponse response = httpClient.execute(request)) {

			// Get HttpResponse Status
			log.debug("ProtocolVersion", response.getProtocolVersion()); // HTTP/1.1
			log.debug("Status Code", response.getStatusLine().getStatusCode()); // 200
			log.debug("Reason Phrase", response.getStatusLine().getReasonPhrase()); // OK
			log.debug("Status Line", response.getStatusLine().toString()); // HTTP/1.1 200 OK

			HttpEntity entity = response.getEntity();
			if (entity != null) {
				// return it as a String
				String result = EntityUtils.toString(entity);
				log.debug(result);
				rv = result;
			}

		} catch (Exception e) {
			log.error("", e);
		}
		log.info("Returning from fetchUserDetails (WSLServiceInvocation): " + rv);
		return rv;
	}

	//
	private MFPUser processUserInfoJson(String jsonString) {
		MFPUser user = null;
		ObjectMapper mapper = new ObjectMapper();
		JsonNode jObj;
		try {
			jObj = mapper.readTree(jsonString);
			if (jObj instanceof ObjectNode) {
				Object obj = ((ObjectNode) jObj).get("status");
				if (obj instanceof TextNode) {
					String status = ((TextNode) obj).asText();
					if (status.equalsIgnoreCase("Success")) {
						Object data = ((ObjectNode) jObj).get("data");
						if ((data != null) && (data instanceof ObjectNode)) {
							user = processJsonObj((ObjectNode) data);
						}
					}
				}
			}
		} catch (JsonProcessingException e) {
			log.error("ERROR PROCESSING STATIC JSON STRING : " + jsonString, e);
		}
		return user;
	}

	//
	private MFPUser processJsonObj(ObjectNode data) {
		MFPUser user = null;
		if (data != null) {
			ObjectMapper mapper = new ObjectMapper();
			try {
				mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
				user = mapper.readValue(data.toString(), MFPUser.class);
			} catch (IOException e) {
				log.error("", e);
			}
		}
		return user;
	}

	public String getUserFromFile() {
		String uid = null;
		java.net.InetAddress addr;
		try {
			addr = java.net.InetAddress.getLocalHost();
			String fqdn = addr.getHostName();
			if (fqdn.startsWith("VDIX-DEV-")) {
				// Developer workstation. Userid may be in file
				File f = new File("/config/user.txt");
				if (f.exists() && f.canRead() && f.isFile()) {
					byte[] b = Files.readAllBytes(f.toPath());
					uid = new String(b);
					if (uid != null)
						uid = uid.trim();
				}
			}
		} catch (UnknownHostException e) {
			log.error("", e);
		} catch (IOException e) {
			log.error("", e);
		}
		return uid;
	}

}
