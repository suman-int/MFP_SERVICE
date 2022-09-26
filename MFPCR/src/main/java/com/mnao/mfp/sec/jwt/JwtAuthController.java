package com.mnao.mfp.sec.jwt;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.SessionAttribute;

import com.mnao.mfp.user.controller.MFPUserController;
import com.mnao.mfp.user.dao.MFPUser;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/Auth/")

public class JwtAuthController {
	private static final Logger log = LoggerFactory.getLogger(MFPUserController.class);
	@Autowired
	JwtTokenUtil jwtTokenUtil;

	//
	// @PostMapping("/Authorize")
	@RequestMapping(value = "/Authorize", method = { RequestMethod.GET, RequestMethod.POST })
	public JwtResponse authorize(@SessionAttribute(name = "mfpUser") MFPUser mfpUser, HttpServletResponse response) {
		String token = jwtTokenUtil.generateToken(mfpUser);
//		boolean useCookie = Utils.getAppProperty(AppConstants.USE_JWT_TOKEN_AUTH_COOKIE, "true")
//				.equalsIgnoreCase("true");
//		if (useCookie) {
//			Cookie ck = new Cookie(AppConstants.AUTH_COOKIE, token);
//			ck.setMaxAge(5 * 60 * 60);
//			ck.setPath("/");
//			ck.setHttpOnly(true);
//			response.addCookie(ck);
//		}
		log.info("Returning from /Authorize:" + token);
		return new JwtResponse(token);
	}

	@RequestMapping(value = "/AuthorizeUser", method = { RequestMethod.GET, RequestMethod.POST })
	public JwtUserTokenResponse authorizeUser(@SessionAttribute(name = "mfpUser") MFPUser mfpUser,
			HttpServletResponse response) {
		String token = jwtTokenUtil.generateToken(mfpUser);
//		boolean useCookie = Utils.getAppProperty(AppConstants.USE_JWT_TOKEN_AUTH_COOKIE, "true")
//				.equalsIgnoreCase("true");
//		if (useCookie) {
//			Cookie ck = new Cookie(AppConstants.AUTH_COOKIE, token);
//			ck.setMaxAge(5 * 60 * 60);
//			ck.setPath("/");
//			ck.setHttpOnly(true);
//			response.addCookie(ck);
//		}
		log.info("Returning from /AuthorizeUser:" + token);
		return new JwtUserTokenResponse(token, mfpUser);
	}
	
}
