package com.mnao.mfp.common.util;

import java.util.Arrays;
import java.util.List;

public class AppConstants {
	public static final String MFP_PROPS_FILE = System.getProperty("mfp.prop.file","/mfp.properties");
//	public static final String MFP_PROPS_FILE = System.getProperty("mfp.prop.file","C:/SmWrk/MFP/MicroServices/Sales/MFPSalesKPI-MS/src/main/resources/mfp.properties");
	public static final String TABLE_QUERY_CONFIG = "TableQueryConfig.json";
	public static final String KPI_QUERY_CONFIG = "KPIQueryConfig.json";
	public static final String INIT_SCRIPTS_FOLDER = "init";
	public static final String KPI_QUERY_SCRIPTS_FOLDER = "query";
	public static final String API_VIEWS_PAGINATION_PGNO="0";
	public static final String API_VIEWS_PAGINATION_DealerPgSize="10";
	public static final String API_VIEWS_PAGINATION_DatePgSize="20";
	public static final String API_VIEWS_PAGINATION_orderBy="";
	public static final String API_VIEWS_PAGINATION_orderDirection="ASC";
	//
	public static final String API_VIEWS_CURRENT_PERIOD="CURRENT_PERIOD";
	public static final String API_VIEWS_PRIOR_PERIOD="PRIOR_PERIOD";
	public static final String API_VIEWS_DATA_NOT_FOUND="NO DATA FOUND";
	public static final String API_VIEWS_METRICS_RetailSalesByDealer="RetailSalesByDealer";
	public static final String API_VIEWS_METRICS_CPOSalesByDealer="CPOSalesByDealer";
	public static final String API_VIEWS_METRICS_RetailSalesByDate="RetailSalesByDate";
	public static final String API_VIEWS_METRICS_CPOSalesByDate="CPOSalesByDate";
	public static final String API_VIEWS_METRICS_RetailInventoryByDealer="RetailInventoryByDealer";
	public static final String API_VIEWS_METRICS_RetailInventoryByDate="RetailInventoryByDate";
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
	public static final String SQL_LIST_CORPORATE_EMPLOYEES = "LIST_CORPORATE_EMPLOYEES.sql";
	//
	public static final String CR_URL_KEY = "MFP_CR_URL";
	//
    public static final int StatusSubmit=1;
    public static final int StatusDeleted=2;
    public static final int StatusDraft=0;
    public static final String file_storage_format="%s_%s_%s";
    public static final String LOCALDATE_FORMAT =  "yyyy-MM-dd";
	public static final List<String> MONTHS_LIST = Arrays.asList("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep",
			"Oct", "Nov", "Dec");
	}
