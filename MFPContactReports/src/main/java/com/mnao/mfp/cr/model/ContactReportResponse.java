package com.mnao.mfp.cr.model;

import lombok.AllArgsConstructor;


@AllArgsConstructor
public class ContactReportResponse {
    private String desc;
    private Object result;
    private boolean success;
    private int status;
    private Object error;
}
