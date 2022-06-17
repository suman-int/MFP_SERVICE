package com.mnao.mfp.sec.jwt;

import com.mnao.mfp.user.dao.MFPUser;

public class JwtUserTokenResponse {
	private String token;
	private MFPUser user;
	//
	public JwtUserTokenResponse() {

	}

	public JwtUserTokenResponse(String token, MFPUser user) {
		super();
		this.token = token;
		this.user = user;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public MFPUser getUser() {
		return user;
	}

	public void setUser(MFPUser user) {
		this.user = user;
	}

}
