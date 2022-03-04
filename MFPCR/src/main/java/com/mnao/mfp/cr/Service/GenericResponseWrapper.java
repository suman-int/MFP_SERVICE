package com.mnao.mfp.cr.Service;

import com.mnao.mfp.cr.model.ContactReportResponse;
import com.mnao.mfp.cr.util.ContactReportEnum;

import java.util.Objects;
import java.util.function.BiFunction;

public class GenericResponseWrapper {

    public static BiFunction<Object, Object, ContactReportResponse> contactReportResponseFunction = (s, e) -> {
        ContactReportEnum contactReportEnum = Objects.nonNull(s) ? ContactReportEnum.SUCCESS : ContactReportEnum.ERROR;
        return new ContactReportResponse(contactReportEnum.getDisplayText(), s,
                Objects.nonNull(s),
                contactReportEnum.getStatusCode(),
                e);
    };
}
