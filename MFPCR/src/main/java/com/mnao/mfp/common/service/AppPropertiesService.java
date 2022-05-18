package com.mnao.mfp.common.service;

import java.io.InputStream;
import java.util.Enumeration;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import com.mnao.mfp.common.controller.MFPCommonUserController;
import com.mnao.mfp.common.util.AppConstants;
import com.mnao.mfp.common.util.Utils;
import com.mnao.mfp.sec.service.MNAOSecurityService;
import com.mnao.mfp.user.service.UserDetailsService;

@Service
public class AppPropertiesService {
	//
	private static final Logger log = LoggerFactory.getLogger(AppPropertiesService.class);
	//

	private Properties appProps = new Properties();
	private Properties wslUserProps = new Properties();

	@Autowired
	Environment env;

	@PostConstruct
	private void postConstruct() {
		log.debug("Property Service instantiated. Profile:" + (env != null ? env.getActiveProfiles()[0] : ""));
		Utils.setAppProps(getAppProperties());
		UserDetailsService.setWslProperties(getWslUserProperties());
		MNAOSecurityService.setWslProperties(getWslUserProperties());
	}

	public Properties getAppProperties() {
		if (appProps.size() == 0) {
			String mfpProfName = getPropertiesLocation() + "/" + AppConstants.MFP_PROPS_NAME + ".properties";
			String[] prof = env.getActiveProfiles();
			if (prof != null && prof.length > 0) {
				mfpProfName = getPropertiesLocation() + "/" + AppConstants.MFP_PROPS_NAME + "-" + prof[0]
						+ ".properties";
			}
			log.debug("Reading AppProperties From:" + mfpProfName);
			try (InputStream is = Utils.class.getResourceAsStream(mfpProfName)) {
				appProps.load(is);
			} catch (Exception e) {
				log.error("ERROR Reading MFP Properties");
				log.error("", e);
			}
		}
		updateAppPropsWithEnv();
		return appProps;
	}

	private void updateAppPropsWithEnv() {
		@SuppressWarnings("unchecked")
		String[] dbprops = { "database.jdbc.mfp.url", "database.jdbc.mfp.user", "database.jdbc.mfp.pass",
				"database.jdbc.mfp.schema", "database.jdbc.cr.url", "database.jdbc.cr.user", "database.jdbc.cr.pass",
				"database.jdbc.cr.schema", "database.jdbc.mma.url", "database.jdbc.mma.user", "database.jdbc.mma.pass",
				"database.jdbc.mma.schema", "database.jdbc.edw.url", "database.jdbc.edw.user", "database.jdbc.edw.pass",
				"database.jdbc.edw.schema" };
		for (String key : dbprops) {
			String val = env.getProperty(key);
			if (val != null && !val.equalsIgnoreCase("#{null}")) {
				appProps.setProperty(key, val);
				log.debug("Updated Property Value of " + key + " with " + val + " from Environment.");
			}
		}
	}

	public Properties getWslUserProperties() {
		if (wslUserProps.size() == 0) {
			String wslPropName = getPropertiesLocation() + "/" + AppConstants.WSL_PROPS_NAME + ".properties";
			String[] prof = env.getActiveProfiles();
			if (prof != null && prof.length > 0) {
				wslPropName = getPropertiesLocation() + "/" + AppConstants.WSL_PROPS_NAME + "-" + prof[0]
						+ ".properties";
			}
			log.debug("Reading WSLUserSvcProperties From:" + wslPropName);
			try (InputStream is = Utils.class.getResourceAsStream(wslPropName)) {
				wslUserProps.load(is);
			} catch (Exception e) {
				log.error("ERROR Reading WSLUserSvc Properties");
				log.error("", e);
			}
		}
		return wslUserProps;
	}

	private String getPropertiesLocation() {
		String propLoc = env.getProperty("mfp.properties.location", "");
		return propLoc;
	}
}
