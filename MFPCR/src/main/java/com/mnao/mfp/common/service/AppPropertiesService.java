package com.mnao.mfp.common.service;

import java.io.InputStream;
import java.util.Properties;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import com.mnao.mfp.common.util.AppConstants;
import com.mnao.mfp.common.util.Utils;
import com.mnao.mfp.sec.service.MNAOSecurityService;
import com.mnao.mfp.user.service.UserDetailsService;

@Service
public class AppPropertiesService {

	private Properties appProps = new Properties();
	private Properties wslUserProps = new Properties();

	@Autowired
	Environment env;

	@PostConstruct
	private void postConstruct() {
		System.out.println("Property Service instantiated. Profile:" + (env != null ? env.getActiveProfiles()[0] : ""));
		Utils.setAppProps(getAppProperties());
		UserDetailsService.setWslProperties(getWslUserProperties());
		MNAOSecurityService.setWslProperties(getWslUserProperties());
	}

	public Properties getAppProperties() {
		if (appProps.size() == 0) {
			String mfpProfName = getPropertiesLocation() + "/" + AppConstants.MFP_PROPS_NAME + ".properties";
			String[] prof = env.getActiveProfiles();
			if (prof != null && prof.length > 0) {
				mfpProfName = getPropertiesLocation() + "/" + AppConstants.MFP_PROPS_NAME + "-" + prof[0] + ".properties";
			}
			System.out.println("Reading AppProperties From:" + mfpProfName);
			try (InputStream is = Utils.class.getResourceAsStream(mfpProfName)) {
				appProps.load(is);
			} catch (Exception e) {
				System.err.println("ERROR Reading MFP Properties");
				e.printStackTrace();
			}
		}
		return appProps;
	}

	public Properties getWslUserProperties() {
		if (wslUserProps.size() == 0) {
			String wslPropName = getPropertiesLocation() + "/" + AppConstants.WSL_PROPS_NAME + ".properties";
			String[] prof = env.getActiveProfiles();
			if (prof != null && prof.length > 0) {
				wslPropName = getPropertiesLocation() + "/" + AppConstants.WSL_PROPS_NAME + "-" + prof[0] + ".properties";
			}
			System.out.println("Reading WSLUserSvcProperties From:" + wslPropName);
			try (InputStream is = Utils.class.getResourceAsStream(wslPropName)) {
				wslUserProps.load(is);
			} catch (Exception e) {
				System.err.println("ERROR Reading WSLUserSvc Properties");
				e.printStackTrace();
			}
		}
		return wslUserProps;
	}

	private String getPropertiesLocation() {
		String propLoc = env.getProperty("mfp.properties.location", "");
		return propLoc;
	}
}
