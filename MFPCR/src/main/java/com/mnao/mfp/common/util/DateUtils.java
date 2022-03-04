package com.mnao.mfp.common.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DateUtils {
	public static final String dateSepChar = "-";
	private static String displayDateFormat = "MM" + dateSepChar + "dd" + dateSepChar + "yyyy";
	private static String displayTimestampFormat = "MM" + dateSepChar + "dd" + dateSepChar + "yyyy HH:mm:ss";
	private static String dbDateFormat = "yyyy" + dateSepChar + "MM" + dateSepChar + "dd";;
	private static String dbTimestampFormat = "yyyy" + dateSepChar + "MM" + dateSepChar + "dd" + " HH:mm:ss";
	private static String ddmmmyyyyFormat = "dd" + dateSepChar + "MMM" + dateSepChar + "yyyy";
	//
	private static String ddmmmyyyyPatStr = "\\d{2}[-./][A-Za-z]{3}[-./]\\d{2,4}";
	private static String mmddyyyyPatStr = "\\d{2}[-./]*\\d{2}[-./]*\\d{2,4}";
	private static String yyyymmddPatStr = "\\d{4}[-./]*\\d{2}[-./]*\\d{2}";
	//
	private SimpleDateFormat sdfDisplayDate = new SimpleDateFormat(displayDateFormat);
	private SimpleDateFormat sdfDisplayTimestamp = new SimpleDateFormat(displayTimestampFormat);
	private SimpleDateFormat sdfDBDate = new SimpleDateFormat(dbDateFormat);
	private SimpleDateFormat sdfDBTimestamp = new SimpleDateFormat(dbTimestampFormat);
	private SimpleDateFormat sdfddmmmyyyy = new SimpleDateFormat(ddmmmyyyyFormat);

	//
	public class MtdDates {
		public String thisMonthStart;
		public String thisMonthEnd;
		public String lastMonthStart;
		public String lastMonthEnd;
	}

	public class MtdLyrDates {
		public String thisMonthStart;
		public String thisMonthEnd;
		public String lastMonthStart;
		public String lastMonthEnd;
		public String lastYrThisMonthStart;
		public String lastYrThisMonthEnd;
	}

	public class YtdDates {
		public String thisYearStart;
		public String thisYearEnd;
		public String lastYearStart;
		public String lastYearEnd;
	}

	//
	public String today() {
		String rval = "";
		rval = sdfDBDate.format(new Date());
		return rval;
	}

	public String formatDisplayDate(java.util.Date val) {
		return sdfDisplayDate.format(val);
	}

	public String formatDisplayTimestamp(java.sql.Timestamp val) {
		return sdfDisplayTimestamp.format(val);
	}

	public String formatDBDate(java.sql.Date val) {
		return sdfDBDate.format(val);
	}

	public String formatDBTimestamp(java.sql.Timestamp val) {
		return sdfDBTimestamp.format(val);
	}

	public Date parseDisplayDate(String val) throws ParseException {
		return sdfDisplayDate.parse(val);
	}

	public Date parseDisplayTimestamp(String val) throws ParseException {
		return sdfDisplayTimestamp.parse(val);
	}

	public Date parseDBTimestamp(String val) throws ParseException {
		return sdfDBTimestamp.parse(val);
	}

	public Date parseDate(String val) throws ParseException {
		Date dt = null;
		if (val != null && val.trim().length() > 0) {
			if (val.matches(ddmmmyyyyPatStr)) {
				// Adjust for 2-char years
				String[] vParts = val.split("[-./]");
				if (vParts[2].length() == 2)
					vParts[2] = "20" + vParts[2];
				val = vParts[0] + "-" + vParts[1] + "-" + vParts[2];
				dt = sdfddmmmyyyy.parse(val);
			} else if (val.matches(mmddyyyyPatStr)) {
				String[] vParts = val.split("[-./]");
				if (vParts[2].length() == 2)
					vParts[2] = "20" + vParts[2];
				val = vParts[0] + "-" + vParts[1] + "-" + vParts[2];
				dt = sdfDisplayDate.parse(val);
			} else if (val.matches(yyyymmddPatStr)) {
				dt = sdfDBDate.parse(val);
			}
		}
		return dt;
	}
	
	public java.sql.Date parseSqlDate(String val) throws ParseException {
		java.util.Date utilDt = parseDate(val);
		java.sql.Date sqlDt = new java.sql.Date(utilDt.getTime());
		return sqlDt;
	}
	
	public MtdDates getMtdDates(String asOn) throws ParseException {
		MtdDates mtd = new MtdDates();
		Date asOnDate = parseDate(asOn);
		Calendar cal = Calendar.getInstance();
		cal.setTime(asOnDate);
		int yyyy = cal.get(Calendar.YEAR);
		int mm = cal.get(Calendar.MONTH) + 1;
		int dd = cal.get(Calendar.DAY_OF_MONTH);
		int lmm = mm - 1;
		int lyyyy = yyyy;
		if (lmm < 1) {
			lmm = 12;
			lyyyy--;
		}
		if (yyyy < 100)
			yyyy += 2000;
		mtd.thisMonthEnd = String.format("%04d-%02d-%02d", yyyy, mm, dd);
		mtd.thisMonthStart = String.format("%04d-%02d-%02d", yyyy, mm, 1);
		mtd.lastMonthEnd = String.format("%04d-%02d-%02d", lyyyy, lmm, dd);
		mtd.lastMonthStart = String.format("%04d-%02d-%02d", lyyyy, lmm, 1);
		return mtd;
	}
	
	public YtdDates getYtdDates(String asOn) throws ParseException {
		YtdDates ytd = new YtdDates();
		Date asOnDate = parseDate(asOn);
		Calendar cal = Calendar.getInstance();
		cal.setTime(asOnDate);
		int yyyy = cal.get(Calendar.YEAR);
		int mm = cal.get(Calendar.MONTH) + 1;
		int dd = cal.get(Calendar.DAY_OF_MONTH);
		int lmm = mm - 1;
		int lyyyy = yyyy;
		if (lmm < 1) {
			lmm = 12;
			lyyyy--;
		}
		if (yyyy < 100)
			yyyy += 2000;
		ytd.thisYearEnd = String.format("%04d-%02d-%02d", yyyy, mm, dd);
		ytd.thisYearStart = String.format("%04d-%02d-%02d", yyyy, 1, 1);
		ytd.lastYearEnd = String.format("%04d-%02d-%02d", yyyy - 1, mm, dd);
		ytd.lastYearStart = String.format("%04d-%02d-%02d", yyyy - 1, 1, 1);
		return ytd;
	}
	public MtdLyrDates getMtdLyrDates(String asOn) throws ParseException {
		MtdLyrDates mtdLyr = new MtdLyrDates();
		Date asOnDate = parseDate(asOn);
		Calendar cal = Calendar.getInstance();
		cal.setTime(asOnDate);
		int yyyy = cal.get(Calendar.YEAR);
		int mm = cal.get(Calendar.MONTH) + 1;
		int dd = cal.get(Calendar.DAY_OF_MONTH);
		int lmm = mm - 1;
		int lyyyy = yyyy;
		if (lmm < 1) {
			lmm = 12;
			lyyyy--;
		}
		if (yyyy < 100)
			yyyy += 2000;
		mtdLyr.thisMonthEnd = String.format("%04d-%02d-%02d", yyyy, mm, dd);
		mtdLyr.thisMonthStart = String.format("%04d-%02d-%02d", yyyy, mm, 1);
		mtdLyr.lastMonthEnd = String.format("%04d-%02d-%02d", lyyyy, lmm, dd);
		mtdLyr.lastMonthStart = String.format("%04d-%02d-%02d", lyyyy, lmm, 1);
		mtdLyr.lastYrThisMonthEnd = String.format("%04d-%02d-%02d", yyyy - 1, mm, dd);
		mtdLyr.lastYrThisMonthStart = String.format("%04d-%02d-%02d", yyyy - 1, mm, 1);
		return mtdLyr;
	}

}