package com.mnao.mfp.common.util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.SQLException;
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
	private static String sqlFilesFolder = getAppProperty("location.sqlfiles");

	//
	public static String readTextFromFile(String fPath) {
		StringBuilder sb = new StringBuilder();
		try (InputStream is = Utils.class.getResourceAsStream(fPath)) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            if (is != null) {  
            	String str = null;
                while ((str = reader.readLine()) != null) {    
                    sb.append(str + "\n" );
                }                
            }
		} catch (Exception e) {
			log.error("ERROR Reading data from text file : " + fPath, e);
		}
		return sb.toString();
	}

	public static String getAppProperty(String propKey) {
		String val = getAppProperties().getProperty(propKey);
		if (val != null) {
			val = val.trim();
			if (val.charAt(0) == '"' && val.charAt(val.length() - 1) == '"') {
				val = val.substring(1, val.length() - 1);
			}
		}
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
			e1.printStackTrace();
		}
		return retSql;
	}
}
