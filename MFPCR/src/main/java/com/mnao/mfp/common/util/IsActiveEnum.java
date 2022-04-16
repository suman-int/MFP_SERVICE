package com.mnao.mfp.common.util;

import lombok.Getter;

@Getter
public enum IsActiveEnum {
    YES("Y"),
    NO("N");

    private final String value;

    IsActiveEnum(String val) {
        this.value = val;
    }
}
