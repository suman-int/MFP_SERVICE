package com.mnao.mfp.common.dto;

import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

public class CommonResponse<T> {
	@NonNull
	private String desc;
	
	@Nullable
	private T result;
	private boolean success;
	private int status;
	@Nullable
	private Object error;
	
	public String getDesc() {
		return desc;
	}
	public T getResult() {
		return result;
	}
	public boolean isSuccess() {
		return success;
	}
	public int getStatus() {
		return status;
	}
	public Object getError() {
		return error;
	}
	public void setDesc(String desc) {
		this.desc = desc;
	}
	public void setResult(T result) {
		this.result = result;
	}
	public void setSuccess(boolean success) {
		this.success = success;
	}
	public void setStatus(int status) {
		this.status = status;
	}
	public void setError(Object error) {
		this.error = error;
	}
}
