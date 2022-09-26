package com.mnao.mfp.sec.service;

import java.util.Properties;

import javax.servlet.http.HttpServletRequest;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mnao.mfp.common.util.AppConstants;
import com.mnao.mfp.common.util.Utils;

public class MNAOSecurityService {
	private static final Logger log = LoggerFactory.getLogger(MNAOSecurityService.class);
	private static Properties wslProperties = new Properties();

	public static Properties getWslProperties() {
		return MNAOSecurityService.wslProperties;
	}

	public static void setWslProperties(Properties wslProperties) {
		MNAOSecurityService.wslProperties = wslProperties;
	}

	public boolean isValidHeaderTokens(HttpServletRequest request) {
		boolean validateFlag = false;
		log.debug(AppConstants.RS_SEC_HDR_TOKEN_NAME + " := " + request.getHeader(AppConstants.RS_SEC_HDR_TOKEN_NAME));
		log.debug(AppConstants.RS_SEC_HDR_IV_NAME + " := " + request.getHeader(AppConstants.RS_SEC_HDR_IV_NAME));
		log.debug(AppConstants.RS_SEC_HDR_VENDOR_ID + " := " + request.getHeader(AppConstants.RS_SEC_HDR_VENDOR_ID));
		//
		if (Utils.isNotNullOrEmpty(request.getHeader(AppConstants.RS_SEC_HDR_TOKEN_NAME))
				&& Utils.isNotNullOrEmpty(request.getHeader(AppConstants.RS_SEC_HDR_IV_NAME))
				&& Utils.isNotNullOrEmpty(request.getHeader(AppConstants.RS_SEC_HDR_VENDOR_ID))) {
			String strUri = MNAOSecurityService.wslProperties.getProperty("AUTH_SVC_URL");
			if (strUri != null && strUri.trim().length() > 0) {
				HttpGet svcReq = new HttpGet(strUri);
				svcReq.setHeader(AppConstants.RS_SEC_HDR_TOKEN_NAME,
						request.getHeader(AppConstants.RS_SEC_HDR_TOKEN_NAME));
				svcReq.setHeader(AppConstants.RS_SEC_HDR_IV_NAME, request.getHeader(AppConstants.RS_SEC_HDR_IV_NAME));
				svcReq.setHeader(AppConstants.RS_SEC_HDR_VENDOR_ID,
						request.getHeader(AppConstants.RS_SEC_HDR_VENDOR_ID));
				try (CloseableHttpClient httpClient = HttpClients.createDefault();
						CloseableHttpResponse svcResp = httpClient.execute(svcReq)) {
					int statCode = svcResp.getStatusLine().getStatusCode();
					log.debug("Status Code", statCode); // 200
					if (statCode == 200) {
						validateFlag = true;
					}

				} catch (Exception e) {
					log.error("", e);
				}
			}
		}
		return validateFlag;
	}

}
