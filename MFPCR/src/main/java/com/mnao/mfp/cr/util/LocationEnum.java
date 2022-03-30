package com.mnao.mfp.cr.util;

public enum LocationEnum {
    REGION("REGION"),
    ZONE("ZONE"),
    DEALER("DEALER"),
    DISTRICT("DISTRICT"),
    ALL("all");

    private String locationText;

    LocationEnum(String text) {
        this.locationText = text;
    }

    public String getLocationText() {
        return locationText;
    }
}
