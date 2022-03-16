package com.mnao.mfp.list.service.impl;

import com.mnao.mfp.common.dao.DealerFilter;
import com.mnao.mfp.common.dto.CommonResponse;
import com.mnao.mfp.common.service.AbstractService;
import com.mnao.mfp.cr.entity.Employees;
import com.mnao.mfp.list.dao.resp.ReviewerResponse;
import com.mnao.mfp.list.enums.EmployeePersonTypeEnum;
import com.mnao.mfp.list.repository.EmployeeRepository;
import com.mnao.mfp.list.service.EmployeesListService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class EmployeesListServiceImpl implements EmployeesListService {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Override
    public CommonResponse<List<ReviewerResponse>> fetchReviewerEmployeeList(DealerFilter df) {
        try {
            List<Employees> reviewerList = employeeRepository.findAllByPrsnTypeCdAndJobCd(EmployeePersonTypeEnum.MAZDA.getCode(), "MZ11");
            List<ReviewerResponse> responseList = reviewerList.stream().map(value ->
                    ReviewerResponse.builder()
                            .firstNm(value.getFRST_NM())
                            .lastNm(value.getLAST_NM())
                            .midlNm(value.getMIDL_NM())
                            .jobCd(value.getJOB_CD())
                            .prsnIdCd(value.getPersonnelIdCd())
                            .prsnTypeCd(value.getPRSN_TYPE_CD())
                            .statusCd(value.getSTATUS_CD())
                            .build()
            ).collect(Collectors.toList());
            return AbstractService.httpPostSuccess(responseList, "Success");
        } catch (Exception exp) {
            return AbstractService.httpPostError(exp);
        }
    }
}
