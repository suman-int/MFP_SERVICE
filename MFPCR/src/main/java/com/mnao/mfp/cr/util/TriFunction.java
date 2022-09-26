package com.mnao.mfp.cr.util;

@FunctionalInterface
public interface TriFunction<A, B, C, BiPredicate> {
    Long discussionCount(A contactReportInfos, B issue, C issueType, BiPredicate p);
}
