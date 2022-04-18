package com.mnao.mfp.common.util;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class AppConstants {
	private static String mfpProfName = "/mfp.properties";
	static {
		String prof = System.getProperty("spring.profiles.active", "dev");
		if (prof.length() > 0)
			mfpProfName = "/mfp-" + prof + ".properties";
	}
	public static final String MFP_PROPS_FILE = System.getProperty("mfp.prop.file", mfpProfName);
	public static final String EMP_USE_DB_RGN_ZONE_DSTR = "emp.use.db.rgn.zone.dstr";
	public static final String TABLE_QUERY_CONFIG = "TableQueryConfig.json";
	public static final String KPI_QUERY_CONFIG = "KPIQueryConfig.json";
	public static final String INIT_SCRIPTS_FOLDER = "init";
	public static final String KPI_QUERY_SCRIPTS_FOLDER = "query";
	public static final String API_VIEWS_PAGINATION_PGNO = "0";
	public static final String API_VIEWS_PAGINATION_DealerPgSize = "10";
	public static final String API_VIEWS_PAGINATION_DatePgSize = "20";
	public static final String API_VIEWS_PAGINATION_orderBy = "";
	public static final String API_VIEWS_PAGINATION_orderDirection = "ASC";
	//
	public static final String API_VIEWS_CURRENT_PERIOD = "CURRENT_PERIOD";
	public static final String API_VIEWS_PRIOR_PERIOD = "PRIOR_PERIOD";
	public static final String API_VIEWS_DATA_NOT_FOUND = "NO DATA FOUND";
	public static final String API_VIEWS_METRICS_RetailSalesByDealer = "RetailSalesByDealer";
	public static final String API_VIEWS_METRICS_CPOSalesByDealer = "CPOSalesByDealer";
	public static final String API_VIEWS_METRICS_RetailSalesByDate = "RetailSalesByDate";
	public static final String API_VIEWS_METRICS_CPOSalesByDate = "CPOSalesByDate";
	public static final String API_VIEWS_METRICS_RetailInventoryByDealer = "RetailInventoryByDealer";
	public static final String API_VIEWS_METRICS_RetailInventoryByDate = "RetailInventoryByDate";
	//
	public static final String SQL_LIST_DEALERS = "LIST_DEALERS.sql";
	public static final String SQL_LIST_DEALERS_LIKE = "LIST_DEALERS_LIKE.sql";
	public static final String SQL_LIST_DEALERS_BY_MARKET = "LIST_DEALERS.sql";
	public static final String SQL_LIST_DISTRICTS = "LIST_DISTRICTS.sql";
	public static final String SQL_LIST_MARKETS = "LIST_MARKETS.sql";
	public static final String SQL_LIST_REGIONS = "LIST_REGIONS.sql";
	public static final String SQL_LIST_ZONES = "LIST_ZONES.sql";
	public static final String SQL_LIST_DEALER_EMPLOYEES = "LIST_DEALER_EMPLOYEES.sql";
	public static final String SQL_LIST_REVIEWER_EMPLOYEES = "LIST_REVIEWER_EMPLOYEES.sql";
	public static final String SQL_LIST_REVIEWER_EMPLOYEE = "LIST_REVIEWER_EMPLOYEE.sql";
	public static final String SQL_LIST_CORPORATE_EMPLOYEES = "LIST_CORPORATE_EMPLOYEES.sql";
	public static final String SQL_LIST_ALL_EMPLOYEES = "LIST_ALL_EMPLOYEES_V2.sql";
	//
	public static final String CR_URL_KEY = "MFP_CR_URL";
	public static final String SMTP_HOST = "cr.email.smtp.host";
	public static final String DEFAULT_DELIM = "cr.email.default.delim";
	public static final String REVIEW_MAIL_FROM = "cr.review.request.mail.from";
	public static final String REVIEW_MAIL_CC = "cr.review.request.mail.cc";
	public static final String REVIEW_MAIL_BCC = "cr.review.request.mail.bcc";
	public static final String REVIEW_MAIL_SUBJECT = "cr.review.request.mail.subject";
	public static final String VIEW_CONTACT_REPORT_URL = "cr.view.report.url";
	public static final String MAIL_SUBMITTED_TO = "cr.mail.submitted.to";
	public static final String MAIL_SUBMITTED_SUBJECT = "cr.mail.submitted.subject";
	public static final String MAIL_SUBMITTED_BODY = "cr.mail.submitted.body";
	public static final String MAIL_SUBMITTED_DISC_TO = "cr.mail.submitted.disc.to";
	public static final String MAIL_SUBMITTED_DISC_SUBJECT = "cr.mail.submitted.disc.subject";
	public static final String MAIL_SUBMITTED_DISC_BODY = "cr.mail.submitted.disc.body";
	public static final String MAIL_REVIEWED_TO = "cr.mail.reviewed.to";
	public static final String MAIL_REVIEWED_SUBJECT = "cr.mail.reviewed.subject";
	public static final String MAIL_REVIEWED_BODY = "cr.mail.reviewed.body";
	public static final String MAIL_DISCREQ_TO = "cr.mail.discreq.to";
	public static final String MAIL_DISCREQ_SUBJECT = "cr.mail.discreq.subject";
	public static final String MAIL_DISCREQ_BODY = "cr.mail.discreq.body";
	//
	public static final int StatusSubmit = 1;
	public static final int StatusDeleted = 2;
	public static final int StatusDraft = 0;
	public static final String file_storage_format = "%s_%s_%s";
	public static final String LOCALDATE_FORMAT = "yyyy-MM-dd";
	public static final String DISPLAYDATE_FORMAT = "MM-dd-yyyy";
	public static final List<String> MONTHS_LIST = Arrays.asList("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug",
			"Sep", "Oct", "Nov", "Dec");
	public static final List<String> SALES_TOPIC_LIST = Arrays.asList("Annual Business Plan-0", "Co-op Sales",
			"CPO/Remarketing", "CX360 Record Health", "CX360 Survey Health", "Dealer Financials", "Dealer Loyalty",
			"Dealer Risk Assessment", "Inventory & Ordering", "Lead Management", "Marketing", "MBEP Training", "MCVP",
			"MCVP Vehicle Expiring", "Other", "Owner Loyalty", "RDR information", "Registration Market Share",
			"Retail Sales", "Sales Customer Experience (MBEP 2.1 Index)", "SPI", "Training");
	public static final List<String> SERVICE_TOPIC_LIST = Arrays.asList("Accessory Business", "Annual Business Plan",
			"Co-op Service", "CX360 Record Health", "CX360 Survey Health", "Dealer Financials",
			"Dealer Risk Assessment", "FIRFT", "Marketing", "MBEP Training", "MCVP", "MCVP Vehicle Expiring",
			"Missed Recall Tiers", "MPC (PartsEye) Utilization", "Other", "Parts Purchase Loyalty", "Parts Sales",
			"Recalls", "Repair Orders", "Service Customer Experience (MBEP 2.1 Index)", "Service Retention/FYSL",
			"Shop Capacity", "Training", "X-Time Service Scheduling");
	public static final List<String> OTHER_TOPIC_LIST = Arrays.asList("Dealer Dev Deficiencies Identified",
			"Dealer Staffing", "Facility", "NDAC Engagement", "Network Activity", "Other", "UMX");

	public static final String DOUBLE_INT_PERCENT = "%d/%d";
	public static final String DOUBLE_STRING_FORMAT = "%s-%s";
}
