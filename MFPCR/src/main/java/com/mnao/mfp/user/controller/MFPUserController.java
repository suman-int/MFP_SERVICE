package com.mnao.mfp.user.controller;

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

import com.mnao.mfp.list.cache.AllEmployeesCache;
import com.mnao.mfp.user.dao.MFPUser;
import com.mnao.mfp.user.service.UserDetailsService;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/User/")
public class MFPUserController {
	//
	private static final Logger log = LoggerFactory.getLogger(MFPUserController.class);
	@Autowired
	AllEmployeesCache allEmployeesCache;

	//
	@PostMapping("/UserDetails")
	public MFPUser userDetails(@RequestParam(value = "userID", defaultValue = "") String userID,
			@SessionAttribute(name = "mfpUser") MFPUser mfpUser) {
		MFPUser musr = mfpUser;
		if( (userID != null) && userID.trim().length() > 0 ) {
			UserDetailsService uds = new UserDetailsService();
			musr = uds.getMFPUser(userID);
		}
		return musr;
	}

	@GetMapping("/CurrentUser")
	public MFPUser currentUser(@SessionAttribute(name = "mfpUser") MFPUser mfpUser) {
		if(  allEmployeesCache != null) {
			allEmployeesCache.updateDomain(mfpUser);
		}
		MFPUser musr = mfpUser;
		return musr;
	}

}
