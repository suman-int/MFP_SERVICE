package com.mnao.mfp.sec.service;

import java.net.HttpURLConnection;
import java.net.URL;
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
import com.mnao.mfp.user.service.UserDetailsService;

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
		if (Utils.isNotNullOrEmpty(request.getHeader(AppConstants.RS_SEC_HDR_TOKEN_NAME))
				&& Utils.isNotNullOrEmpty(request.getHeader(AppConstants.RS_SEC_HDR_IV_NAME))
				&& Utils.isNotNullOrEmpty(request.getHeader(AppConstants.RS_SEC_HDR_VENDOR_ID))) {
			String strUri = MNAOSecurityService.wslProperties.getProperty("AUTH_SVC_URL");
			HttpGet svcReq = new HttpGet(strUri);
			svcReq.setHeader(AppConstants.RS_SEC_HDR_TOKEN_NAME, request.getHeader(AppConstants.RS_SEC_HDR_TOKEN_NAME));
			svcReq.setHeader(AppConstants.RS_SEC_HDR_IV_NAME, request.getHeader(AppConstants.RS_SEC_HDR_IV_NAME));
			svcReq.setHeader(AppConstants.RS_SEC_HDR_VENDOR_ID, request.getHeader(AppConstants.RS_SEC_HDR_VENDOR_ID));
			try (CloseableHttpClient httpClient = HttpClients.createDefault();
					CloseableHttpResponse svcResp = httpClient.execute(svcReq)) {
				int statCode = svcResp.getStatusLine().getStatusCode();
				log.debug("Status Code", statCode); // 200
				if (statCode == 200) {
					validateFlag = true;
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return validateFlag;
	}

	public boolean isValidHeaderTokensOld(HttpServletRequest request) {

		boolean validateFlag = false;
		HttpURLConnection con = null;

		try {

			URL url = new URL(wslProperties.getProperty("AUTH_SVC_URL"));
			con = (HttpURLConnection) url.openConnection();
			con.setRequestMethod("GET");
			con.setRequestProperty(wslProperties.getProperty("AUTH_RS_SEC_HDR_TOKEN_NAME"),
					request.getHeader(AppConstants.RS_SEC_HDR_TOKEN_NAME));
			con.setRequestProperty(wslProperties.getProperty("AUTH_RS_SEC_HDR_IV_NAME"),
					request.getHeader(AppConstants.RS_SEC_HDR_IV_NAME));
			con.setRequestProperty(wslProperties.getProperty("AUTH_RS_SEC_HDR_VENDOR_ID"),
					request.getHeader(AppConstants.RS_SEC_HDR_VENDOR_ID));
			con.setUseCaches(false);
			con.setDoInput(true);
			con.setDoOutput(true);

			int statusCode = con.getResponseCode();
			if (statusCode == 200) {
				validateFlag = true;
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (con != null) {
				con.disconnect();
			}
		}

		return validateFlag;
	}

}
