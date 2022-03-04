package com.mnao.mfp.common.service;

import org.springframework.http.HttpStatus;

import com.mnao.mfp.common.dto.CommonResponse;

public class AbstractService {
	
	public static <T> CommonResponse<T> httpPostSuccess(T result, String desc) {
		CommonResponse<T> commonResp = new CommonResponse<T>();
		commonResp.setDesc(desc);
		commonResp.setResult(result);
		commonResp.setSuccess(true);
		commonResp.setStatus(HttpStatus.OK.value());
		   return commonResp;
		}

}
