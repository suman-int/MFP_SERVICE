package com.mnao.mfp.cr.util;

import lombok.Getter;

@Getter
public enum ContactReportEnum {
    CANCELLED(-1,"",""),
    SUBMITTED(1, "Submitted","submitted"),
    DRAFT(0, "Draft","draft"),
    DISCUSSION_REQUESTED(2, "Discussion Requested", "discussionRequested"),
    REVIEWED(3,"Reviewed","reviewed");
    private int statusCode;
    private String statusText;
    private String displayText;

    ContactReportEnum(int statusCode, String statusText,String displayText) {
        this.statusCode = statusCode;
        this.statusText = statusText;
        this.displayText = displayText;
    }
    
    public static ContactReportEnum valueByStatus(int statusCode) {
    	for (ContactReportEnum element : values()) {
			if (element.statusCode == statusCode) {
				return element;
			}
		}
    	throw new IllegalArgumentException("Please check the " + statusCode + ", Unable to find it in enum");
    }
}
