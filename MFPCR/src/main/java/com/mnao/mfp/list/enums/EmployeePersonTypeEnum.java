package com.mnao.mfp.list.enums;

import lombok.Getter;

@Getter
public enum EmployeePersonTypeEnum {

    DEALER("D"),

    MAZDA("M"),

    VENDOR("V");

    private final String code;

    EmployeePersonTypeEnum(String code) {
        this.code = code;
    }


}
