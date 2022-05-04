package com.mnao.mfp.config;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile({"dev", "default"})
public class DevelopmentConfiguration implements CrConfiguration {

    @Override
    public String getName() {
        return "development profile";
    }

	@Override
	public String getActiveProfile() {
		return "DEV";
	}
}