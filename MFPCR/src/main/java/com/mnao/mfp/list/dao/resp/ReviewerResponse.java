package com.mnao.mfp.list.dao.resp;

import lombok.*;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class ReviewerResponse {
    private String prsnIdCd, statusCd, jobCd, prsnTypeCd, firstNm, midlNm, lastNm;
}
