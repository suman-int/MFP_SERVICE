package com.mnao.mfp.list.controller;

import com.mnao.mfp.common.dao.DealerFilter;
import com.mnao.mfp.common.dto.CommonResponse;
import com.mnao.mfp.list.dao.resp.ReviewerResponse;
import com.mnao.mfp.list.service.EmployeesListService;
import com.mnao.mfp.user.dao.MFPUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/employees")
public class EmployeesListController {

    @Autowired
    private EmployeesListService employeesListService;

    @PostMapping("/reviewer/list")
    public CommonResponse<List<ReviewerResponse>> fetchReviewerEmployeeList(
            @RequestParam(value = "rgnCd", defaultValue = "") String rgnCd,
            @RequestParam(value = "zoneCd", defaultValue = "") String zoneCd,
            @RequestParam(value = "districtCd", defaultValue = "") String districtCd,
            @RequestParam(value = "mdaCd", defaultValue = "") String mdaCd,
            @RequestParam(value = "dlrCd", defaultValue = "") String dlrCd,
            @SessionAttribute(name = "mfpUser") MFPUser mfpUser
    ) {
        DealerFilter df = new DealerFilter(mfpUser, dlrCd, rgnCd, zoneCd, districtCd, mdaCd);
        return employeesListService.fetchReviewerEmployeeList(df);
    }
}
