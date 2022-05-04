package com.mnao.mfp.config;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile({"qa"})
public class QAConfiguration implements CrConfiguration {

    @Override
    public String getName() {
        return "qa profile";
    }
    
    @Override
	public String getActiveProfile() {
		return "QA";
	}
}