package com.mnao.mfp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.mnao.mfp.common.annotation.MFPCommonService;
import com.mnao.mfp.user.annotation.MFPUserService;

@SpringBootApplication
@MFPUserService
@MFPCommonService
public class MfpListServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(MfpListServiceApplication.class, args);
	}

}
