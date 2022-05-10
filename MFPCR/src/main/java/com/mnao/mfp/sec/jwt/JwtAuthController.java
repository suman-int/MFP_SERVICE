package com.mnao.mfp.sec.jwt;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.SessionAttribute;

import com.mnao.mfp.list.emp.AllEmployeesCache;
import com.mnao.mfp.user.controller.MFPUserController;
import com.mnao.mfp.user.dao.MFPUser;
import com.mnao.mfp.user.service.UserDetailsService;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/Auth/")

public class JwtAuthController {
	private static final Logger log = LoggerFactory.getLogger(MFPUserController.class);
	@Autowired
	JwtTokenUtil jwtTokenUtil;

	//
	@PostMapping("/Authorize")
	public JwtResponse authorize(@SessionAttribute(name = "mfpUser") MFPUser mfpUser) {
		String token = jwtTokenUtil.generateToken(mfpUser);
		return new JwtResponse(token);
	}


}
