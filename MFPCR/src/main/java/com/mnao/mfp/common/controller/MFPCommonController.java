package com.mnao.mfp.common.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.SessionAttribute;
import org.springframework.web.servlet.ModelAndView;

import com.mnao.mfp.common.dto.CommonResponse;
import com.mnao.mfp.common.service.AbstractService;
import com.mnao.mfp.user.dao.MFPUser;
import com.mnao.mfp.user.service.UserDetailsService;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/Common/")
public class MFPCommonController extends MfpKPIControllerBase {
	//
	private static final Logger log = LoggerFactory.getLogger(MFPCommonController.class);

	//
	@PostMapping("/UserDetails")
	public CommonResponse<MFPUser> listDesalers(@RequestParam(value = "userID", defaultValue = "") String userID,
			@SessionAttribute(name = "mfpUser") MFPUser mfpUser) {
		MFPUser musr = mfpUser;
		if( (userID != null) && userID.trim().length() > 0 ) {
			UserDetailsService uds = new UserDetailsService();
			musr = uds.getMFPUser(userID);
		}
		return AbstractService.httpPostSuccess(musr, "Success");
	}

    @GetMapping("/crhome")
    public ModelAndView crHome(@RequestParam(value = "waitms", defaultValue = "0") Integer waitms,
    		@SessionAttribute(name = "mfpUser") MFPUser mfpUser) {
    	  ModelAndView mv = new ModelAndView();
    	  mv.setViewName("CRHome");
    	  mv.addObject("waitms", waitms);
    	  mv.addObject("mfpUser", mfpUser);
        return mv;
    }
}
