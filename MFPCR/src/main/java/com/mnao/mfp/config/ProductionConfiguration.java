package com.mnao.mfp.config;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile({"prod"})
public class ProductionConfiguration implements CrConfiguration {

    @Override
    public String getName() {
        return "production profile";
    }
    
    @Override
	public String getActiveProfile() {
		return "PROD";
	}
}