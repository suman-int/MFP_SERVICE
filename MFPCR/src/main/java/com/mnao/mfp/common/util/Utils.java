package com.mnao.mfp.common.util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//
public class Utils {
	//
	private static final Logger log = LoggerFactory.getLogger(Utils.class);
	//

	//
	private static Properties appProps = new Properties();

	//
	// private static String sqlFilesFolder = getAppProperty("location.sqlfiles");
	public static void setAppProps(Properties props) {
		appProps = props;
	}

	//
	public static String readTextFromFile(String fPath) {
		StringBuilder sb = new StringBuilder();
		try (InputStream is = Utils.class.getResourceAsStream(fPath)) {
			BufferedReader reader = new BufferedReader(new InputStreamReader(is));
			if (is != null) {
				String str = null;
				while ((str = reader.readLine()) != null) {
					sb.append(str + "\n");
				}
			}
		} catch (Exception e) {
			log.error("ERROR Reading data from text file : " + fPath, e);
		}
		return sb.toString();
	}

	public static String getAppProperty(String propKey) {
		String val = getAppProperties().getProperty(propKey);
		if (val != null && val.trim().length() > 0) {
			val = val.trim();
			if (val.charAt(0) == '"' && val.charAt(val.length() - 1) == '"') {
				val = val.substring(1, val.length() - 1);
			}
		}
		return val;
	}

	public static String getAppProperty(String propKey, String defValue) {
		String val = getAppProperty(propKey);
		if (val == null || val.trim().length() == 0)
			val = defValue;
		return val;
	}

	private static Properties getAppProperties() {
		if (appProps.size() == 0) {
			try (InputStream is = Utils.class.getResourceAsStream(AppConstants.MFP_PROPS_FILE)) {
				appProps.load(is);
			} catch (Exception e) {
				log.error("ERROR Reading MFP Properties", e);
			}
		}
		return appProps;
	}
	
//	public static Properties getWslProperties() {
//		if (appProps.size() > 0) {
//			try (InputStream is = Utils.class.getResourceAsStream(AppConstants.WSL_PROPS_FILE)) {
//				appProps.load(is);
//			} catch (Exception e) {
//				log.error("ERROR Reading MFP Properties", e);
//			}
//		}
//		return appProps;
//	}

	private static String getSchemaName(Connection conn) throws SQLException {
		String schema = conn.getSchema();
		if (schema == null)
			schema = conn.getCatalog();
		if (schema != null)
			schema = schema.trim() + ".";
		return schema;
	}

	public static String replaceSchemaName(Connection conn, String sql) {
		String retSql = sql;
		try {
			String sch = Utils.getSchemaName(conn);
			retSql = retSql.replaceAll("\\$SCHEMA\\$", sch);
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			log.error("", e1);
		}
		return retSql;
	}

	public static boolean isNotNullOrEmpty(String value) {
		return (value != null && value.trim().length() > 0);
	}

	public static boolean isNullOrEmpty(String value) {
		return !isNotNullOrEmpty(value);
	}

	public static boolean isNotNullOrEmpty(LocalDate value) {
		return (value != null);
	}

	public static String getNameString(String... nParts) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < nParts.length; i++) {
			if ((nParts[i] != null) && (nParts[i].trim().length() > 0)) {
				if (i > 0 && (!nParts[i].equalsIgnoreCase(",")))
					sb.append(" ");
				sb.append(nParts[i].substring(0, 1).toUpperCase());
				if (nParts[i].length() > 1)
					sb.append(nParts[i].substring(1).toLowerCase().trim());
			}
		}
		return sb.toString();
	}

}
