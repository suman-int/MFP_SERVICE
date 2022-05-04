package com.mnao.mfp.config;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile({"test"})
public class TestConfiguration implements CrConfiguration {

    @Override
    public String getName() {
        return "test profile";
    }
    
    @Override
	public String getActiveProfile() {
		return "TEST";
	}
}