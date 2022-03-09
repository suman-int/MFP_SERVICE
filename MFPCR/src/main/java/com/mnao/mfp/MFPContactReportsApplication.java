package com.mnao.mfp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.mnao.mfp.common.annotation.MFPCommonService;
import com.mnao.mfp.list.annotation.MFPListService;
import com.mnao.mfp.list.dao.ListDealer;
import com.mnao.mfp.user.annotation.MFPUserService;

@SpringBootApplication
//@MFPUserService
//@MFPListService
//@MFPCommonService
public class MFPContactReportsApplication {

	public static void main(String[] args) {
		SpringApplication.run(MFPContactReportsApplication.class, args);
	}

}
