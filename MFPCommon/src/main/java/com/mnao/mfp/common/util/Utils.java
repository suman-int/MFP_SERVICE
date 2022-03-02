package com.mnao.mfp.common.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
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
//	public static String readSQL(String fName) {
//		String s = null;
//		if (!sqlFilesFolder.endsWith(File.separator))
//			sqlFilesFolder = sqlFilesFolder + File.separator;
//		String fPath = sqlFilesFolder + fName;
//		s = readTextFromFile(fPath);
//		return s;
//	}

	public static String readTextFromFile(String fPath) {
		String s = null;
		try {
			s = new String(Files.readAllBytes(Paths.get(fPath)));
		} catch (IOException e) {
			log.error("ERROR Reading data from text file : " + fPath, e);
		}
		return s;
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
//			try (InputStream is = Utils.class.getClassLoader().getResourceAsStream(AppConstants.MFP_PROPS_FILE)) {
			try (InputStream is = new FileInputStream(AppConstants.MFP_PROPS_FILE)) {
				appProps.load(is);
			} catch (Exception e) {
				log.error("ERROR Reading MFP Properties", e);			}
		}
		return appProps;
	}
	
	private static String getSchemaName(Connection conn) throws SQLException {
		String schema = conn.getSchema();
		if( schema == null ) 
			schema = conn.getCatalog();
		if( schema != null )
			schema = schema + ".";
		return schema;
	}
	
	public static String replaceSchemaName(Connection conn, String sql) {
		String retSql = sql;
		try {
			retSql = retSql.replaceAll("\\$SCHEMA\\$", Utils.getSchemaName(conn));
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		return retSql;
	}
}
