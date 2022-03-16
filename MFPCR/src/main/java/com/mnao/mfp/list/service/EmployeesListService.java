package com.mnao.mfp.list.service;

import com.mnao.mfp.common.dao.DealerFilter;
import com.mnao.mfp.common.dto.CommonResponse;
import com.mnao.mfp.list.dao.resp.ReviewerResponse;

import java.util.List;

public interface EmployeesListService {

    CommonResponse<List<ReviewerResponse>> fetchReviewerEmployeeList(DealerFilter df);
}
