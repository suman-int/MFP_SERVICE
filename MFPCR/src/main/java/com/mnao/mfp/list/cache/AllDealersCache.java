package com.mnao.mfp.list.cache;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.mnao.mfp.common.controller.MfpKPIControllerBase;
import com.mnao.mfp.common.dao.DealerInfo;

@Service
public class AllDealersCache extends MfpKPIControllerBase {
	//
	private static final Logger log = LoggerFactory.getLogger(AllDealersCache.class);
	//
	private static final List<DealerInfo> allDealers = new ArrayList<>();
}
