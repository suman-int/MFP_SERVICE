package com.mnao.mfp.cr.service;

import java.text.ParseException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.mail.MessagingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.mnao.mfp.MFPContactReportsApplication;
import com.mnao.mfp.common.controller.MfpKPIControllerBase;
import com.mnao.mfp.common.dao.DealerFilter;
import com.mnao.mfp.common.dao.DealerInfo;
import com.mnao.mfp.common.util.AppConstants;
import com.mnao.mfp.common.util.Utils;
import com.mnao.mfp.cr.entity.ContactReportInfo;
import com.mnao.mfp.cr.util.ContactReportEnum;
import com.mnao.mfp.email.EMazdamailsender;
import com.mnao.mfp.list.service.MMAListService;
import com.mnao.mfp.pdf.dao.ReviewerEmployeeInfo;
import com.mnao.mfp.user.dao.MFPUser;
import com.mnao.mfp.user.service.UserDetailsService;

@Service
public class EmailService extends MfpKPIControllerBase {
	//
	private static final Logger log = LoggerFactory.getLogger(EmailService.class);
	//
	@Value("${spring.profiles.active}")
	private String activeProfile;
	
	
	public void sendEmailNotification(ContactReportInfo report, int origCRStatus, MFPUser mfpUser)
			throws MessagingException {
		EMazdamailsender objEMazdamailsender = new EMazdamailsender();
		objEMazdamailsender.set_mimeType("text/html");
		List<String> toAddresses = new ArrayList<String>();
		List<String> ccAddresses = new ArrayList<String>();
		List<String> bccAddresses = new ArrayList<String>();
		//
		DealerInfo dInfo = getDealerInfo(mfpUser, report.getDlrCd());
		String authorID = report.getContactAuthor();
		UserDetailsService uds = new UserDetailsService();
		MFPUser authorUser = uds.getMFPUser(authorID);
		ReviewerEmployeeInfo revEmp = getReviewerEmployeeInfo(mfpUser, report.getContactReviewer(), dInfo);
		String authorName = getNameStr(authorUser.getFirstName(), authorUser.getLastName());
		String reviewerName = getNameStr(revEmp.getFirstNm(), revEmp.getLastNm());
		String dealerName = dInfo.getDbaNm() + " - " + dInfo.getDlrCd();
		String toAddr = null;
		String subjFmt = null;
		String bodyFmt = null;
		String toFmt = null;
		//
		if (report.getContactStatus() == ContactReportEnum.SUBMITTED.getStatusCode()) {
			toAddr = revEmp.getEmailAddr();
			if (origCRStatus == ContactReportEnum.DISCUSSION_REQUESTED.getStatusCode()) {
				subjFmt = Utils.getAppProperty(AppConstants.MAIL_SUBMITTED_DISC_SUBJECT);
				bodyFmt = Utils.getAppProperty(AppConstants.MAIL_SUBMITTED_DISC_BODY);
				toFmt = Utils.getAppProperty(AppConstants.MAIL_SUBMITTED_DISC_TO);
			} else {
				subjFmt = Utils.getAppProperty(AppConstants.MAIL_SUBMITTED_SUBJECT);
				bodyFmt = Utils.getAppProperty(AppConstants.MAIL_SUBMITTED_BODY);
				toFmt = Utils.getAppProperty(AppConstants.MAIL_SUBMITTED_TO);
			}
		} else if (report.getContactStatus() == ContactReportEnum.REVIEWED.getStatusCode()) {
			if( ! revEmp.getPrsnIdCd().trim().equalsIgnoreCase(mfpUser.getEmployeeNumber().trim())) {
				// Corporate user is APPROVing or DISC REQ
				reviewerName = getNameStr(mfpUser.getFirstName(), mfpUser.getLastName());
			}
			toAddr = authorUser.getEmail();
			subjFmt = Utils.getAppProperty(AppConstants.MAIL_REVIEWED_SUBJECT);
			bodyFmt = Utils.getAppProperty(AppConstants.MAIL_REVIEWED_BODY);
			toFmt = Utils.getAppProperty(AppConstants.MAIL_REVIEWED_TO);
		} else if (report.getContactStatus() == ContactReportEnum.DISCUSSION_REQUESTED.getStatusCode()) {
			if( ! revEmp.getPrsnIdCd().trim().equalsIgnoreCase(mfpUser.getEmployeeNumber().trim())) {
				// Corporate user is APPROVing or DISC REQ
				reviewerName = getNameStr(mfpUser.getFirstName(), mfpUser.getLastName());
			}
			toAddr = authorUser.getEmail();
			subjFmt = Utils.getAppProperty(AppConstants.MAIL_DISCREQ_SUBJECT);
			bodyFmt = Utils.getAppProperty(AppConstants.MAIL_DISCREQ_BODY);
			toFmt = Utils.getAppProperty(AppConstants.MAIL_DISCREQ_TO);
		}
		if (subjFmt == null || subjFmt.trim().length() == 0 || bodyFmt == null || bodyFmt.trim().length() == 0) {
			return;
		}
		//
		String emailFrom = Utils.getAppProperty(AppConstants.REVIEW_MAIL_FROM);
		String subject = getEmailSubject(report, subjFmt, authorName, reviewerName, dealerName);
		String body = getEmailBody(report, toFmt, bodyFmt, authorName, reviewerName, dealerName);
		boolean sendTestEmail = Boolean.parseBoolean(Utils.getAppProperty(AppConstants.SEND_TEST_EMAIL_ONLY, "false"));
		if(sendTestEmail) {
			String toStr = Utils.getAppProperty(AppConstants.MAIL_TEST_USERS);
			if (toStr != null && toStr.trim().length() > 0) {
				String[] tos = toStr.split("[,; ]");
				toAddresses = Arrays.asList(tos);
			}			
		} else {
			toAddresses.add(toAddr);
		}
		//
		String ccStr = Utils.getAppProperty(AppConstants.REVIEW_MAIL_CC);
		if (ccStr != null && ccStr.trim().length() > 0) {
			String[] ccs = ccStr.split("[,; ]");
			ccAddresses = Arrays.asList(ccs);
		}
		String bccStr = Utils.getAppProperty(AppConstants.REVIEW_MAIL_BCC);
		if (bccStr != null && bccStr.trim().length() > 0) {
			String[] bccs = bccStr.split("[,; ]");
			bccAddresses = Arrays.asList(bccs);
		}
		//
		String[] to = toAddresses.toArray(new String[0]);
		String[] cc = ccAddresses.toArray(new String[0]);
		String[] bcc = bccAddresses.toArray(new String[0]);
		objEMazdamailsender.SendMazdaMail(emailFrom, to, cc, bcc, subject, body);
	}

	private String getEmailSubject(ContactReportInfo report, String subjFmt, String authorName, String reviewerName,
			String dealerName) {
		return getPlaceholdersReplaced(subjFmt, authorName, reviewerName, dealerName);
	}

	private String getEmailBody(ContactReportInfo report, String toFmt, String bodyFmt, String authorName,
			String reviewerName, String dealerName) {
		StringBuilder sb = new StringBuilder();
		String toStr = getPlaceholdersReplaced(toFmt, authorName, reviewerName, dealerName);
		String bodyStr = getPlaceholdersReplaced(bodyFmt, authorName, reviewerName, dealerName);
		sb.append(toStr);
		sb.append("<br><br>");
		sb.append(bodyStr);
		sb.append("<br><br>");
		String refUrl = Utils.getAppProperty(AppConstants.VIEW_CONTACT_REPORT_URL);
		if (!refUrl.endsWith("/%d"))
			refUrl += "/%d";
		refUrl = String.format(refUrl, report.getContactReportId());
		sb.append("<a href=\"" + refUrl + "\">");
		sb.append(dealerName);
		sb.append(" - ");
		sb.append(report.getContactDt().format(DateTimeFormatter.ofPattern(AppConstants.DISPLAYDATE_FORMAT)));
		sb.append("</a><br>");
		sb.append("<br><br>Thank You<br>");
		return sb.toString();
	}

	private ReviewerEmployeeInfo getReviewerEmployeeInfo(MFPUser mfpUser, String contactReviewer, DealerInfo dInfo) {
		ReviewerEmployeeInfo revEmp = null;
		if (contactReviewer != null) {
			String sqlName = getKPIQueryFilePath(AppConstants.SQL_LIST_REVIEWER_EMPLOYEE);
			MMAListService<ReviewerEmployeeInfo> service = new MMAListService<ReviewerEmployeeInfo>();
			List<ReviewerEmployeeInfo> retRows = null;
			DealerFilter df = new DealerFilter(mfpUser, null, mfpUser.getRgnCd(), null, null, null);
			try {
				retRows = service.getListData(sqlName, ReviewerEmployeeInfo.class, df, dInfo.getRgnCd(),
						dInfo.getZoneCd(), contactReviewer);
				if (retRows != null && retRows.size() > 0)
					revEmp = retRows.get(0);
			} catch (InstantiationException | IllegalAccessException | ParseException e) {
				log.error("ERROR retrieving list of Employees:" , e);
			}
		}
		return revEmp;
	}

	private String getPlaceholdersReplaced(String fmt, String authorName, String reviewerName, String dealerName) {
		String txt = fmt.replaceAll("\\{author\\}", authorName);
		txt = txt.replaceAll("\\{dealer\\}", dealerName);
		txt = txt.replaceAll("\\{reviewer\\}", reviewerName);
		return txt;
	}

	private String getNameStr(String fName, String lName) {
		String fn = properCase(fName);
		String ln = properCase(lName);
		return fn.trim() + " " + ln.trim();
	}

	private String properCase(String nm) {
		String s = nm;
		if (s == null)
			s = " ";
		else if (s.length() == 1)
			s = s.toUpperCase();
		else
			s = s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase();
		return s;
	}

}
