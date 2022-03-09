package com.mnao.mfp.cr.util;

public enum ContactReportEnum {
    CANCELLED(-1,"",""),
    SUBMITTED(1, "Submitted","submitted"),
    DRAFT(0, "Draft","draft"),
    DISCUSSION_REQUESTED(2, "Discussion Requested", "discussionRequested"),
    REVIEWED(3,"Reviewed","reviewed"),
    COMPLETED(4,"Completed","Completed"),
    SUCCESS(200,"Success","Success"),
    ERROR(500,"Error","Error");
    private int statusCode;
    private String statusText;
    private String displayText;

    ContactReportEnum(int statusCode, String statusText,String displayText) {
        this.statusCode = statusCode;
        this.statusText = statusText;
        this.displayText = displayText;
    }

    public int getStatusCode(){
        return this.statusCode;
    }

    public String getStatusText(){
        return statusText;
    }

    public String getDisplayText() {
        return displayText;
    }
}
