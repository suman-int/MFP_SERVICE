package com.mnao.mfp.cr.service;

import com.mnao.mfp.cr.model.ContactReportResponse;
import org.springframework.http.HttpStatus;

import java.util.Objects;
import java.util.function.BiFunction;

public class GenericResponseWrapper {

    public static BiFunction<Object, Object, ContactReportResponse> contactReportResponseFunction = (s, e) -> {
        HttpStatus httpStatus = Objects.nonNull(s) ? HttpStatus.OK : HttpStatus.INTERNAL_SERVER_ERROR;
        return new ContactReportResponse(httpStatus.getReasonPhrase(), s,
                Objects.nonNull(s),
                httpStatus.value(),
                e);
    };
}
