package com.mnao.mfp.common.service;

import java.io.InputStream;
import java.util.Properties;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import com.mnao.mfp.common.util.AppConstants;
import com.mnao.mfp.common.util.Utils;

@Service
public class AppPropertiesService {

	private Properties appProps = new Properties();

	@Autowired
	Environment env;

	@PostConstruct
	private void postConstruct() {
		System.out.println("Property Service instantiated. Profile:" + (env != null ? env.getActiveProfiles()[0] : ""));
		Utils.setAppProps(getAppProperties());
	}

	public Properties getAppProperties() {
		String mfpProfName = "/mfp.properties";
		String[] prof = env.getActiveProfiles();
		if (prof != null && prof.length > 0) {
			mfpProfName = "/mfp-" + prof[0] + ".properties";
		}
		if (appProps.size() == 0) {
			try (InputStream is = Utils.class.getResourceAsStream(mfpProfName)) {
				appProps.load(is);
			} catch (Exception e) {
				System.err.println("ERROR Reading MFP Properties");
				e.printStackTrace();
			}
		}
		return appProps;
	}

}
