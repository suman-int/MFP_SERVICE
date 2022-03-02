package com.mnao.mfp.common.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.rowset.CachedRowSet;
import javax.sql.rowset.RowSetFactory;
import javax.sql.rowset.RowSetProvider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//
public class MFPDatabase {
	//
	private static final Logger log = LoggerFactory.getLogger(MFPDatabase.class);

	//
	public enum DB {
		mfp("mfp"), local("local"), cr("cr"), edw("edw");

		//
		private String dbID = "";

		//
		DB(String id) {
			dbID = id;
		}

		//
		public String url() {
			return Utils.getAppProperty("database.jdbc." + dbID + ".url");
		}

		public String user() {
			return Utils.getAppProperty("database.jdbc." + dbID + ".user");
		}

		public String pass() {
			return Utils.getAppProperty("database.jdbc." + dbID + ".pass");
		}

		public String schema() {
			return Utils.getAppProperty("database.jdbc." + dbID + ".schema");
		}
	}
	//

	//
	//
	public MFPDatabase() {
	}

//	//
//	public void closeConnection() {
//		try {
//			if ((connection != null) && !connection.isClosed()) {
//				connection.close();
//			}
//		} catch (SQLException e) {
//			log.error("ERROR Closing Connection", e);
//		} finally {
//			connection = null;
//		}
//	}

	//
	//
	public Connection getConnection(DB db) {
		return connect(db.url(), db.user(), db.pass(), db.schema());
	}

	//
	protected Connection connect(String jdbcUrl, String jdbcUser, String jdbcPassword, String jdbcSchema) {
		Connection conn = null;
		try {
			conn = DriverManager.getConnection(jdbcUrl, jdbcUser, jdbcPassword);
			if ((jdbcSchema != null) && (jdbcSchema.trim().length() > 0)) {
				String dbProduct = conn.getMetaData().getDatabaseProductName();
				if (dbProduct.toUpperCase().indexOf("MYSQL") >= 0)
					conn.setCatalog(jdbcSchema);
				else if (dbProduct.toUpperCase().indexOf("HSQL") >= 0) {
					execute(conn, "CREATE SCHEMA IF NOT EXISTS " + jdbcSchema.toUpperCase());
					conn.setSchema(jdbcSchema.toUpperCase());
				} else if (dbProduct.toUpperCase().indexOf("DB2") >= 0) {
					conn.setSchema(jdbcSchema.toUpperCase());
				} else if (dbProduct.toUpperCase().indexOf("ORACLE") >= 0) {
//					execute("ALTER SESSION SET CURRENT_SCHEMA=" + jdbcSchema);
				}
			}
			log.debug(String.format("Connected to database %s " + "successfully.", conn.getCatalog()));

		} catch (SQLException e) {
			conn = null;
			log.error("ERROR Connecting: ", e);
		}
		return conn;
	}

	//
	public static Class<? extends Object> getJavaDataType(int dbDataType) {
		switch (dbDataType) {
		case java.sql.Types.CHAR:
			return String.class;
		case java.sql.Types.VARCHAR:
			return String.class;
		case java.sql.Types.LONGVARCHAR:
			return String.class;
		case java.sql.Types.NUMERIC:
			return java.math.BigDecimal.class;
		case java.sql.Types.DECIMAL:
			return java.math.BigDecimal.class;
		case java.sql.Types.BIT:
			return Boolean.class;
		case java.sql.Types.TINYINT:
			return Byte.class;
		case java.sql.Types.SMALLINT:
			return Short.class;
		case java.sql.Types.INTEGER:
			return Integer.class;
		case java.sql.Types.BIGINT:
			return Long.class;
		case java.sql.Types.REAL:
			return Float.class;
		case java.sql.Types.FLOAT:
		case java.sql.Types.DOUBLE:
			return Double.class;
		case java.sql.Types.BINARY:
			return Byte[].class;
		case java.sql.Types.VARBINARY:
			return Byte[].class;
		case java.sql.Types.LONGVARBINARY:
			return Byte[].class;
		case java.sql.Types.DATE:
			return java.sql.Date.class;
		case java.sql.Types.TIME:
			return java.sql.Time.class;
		case java.sql.Types.TIMESTAMP:
			return java.sql.Timestamp.class;
		}
		return null;
	}

	/*
	 * DML Statements
	 */
	//

	//
	public boolean execute(Connection conn, String sql) {
		boolean rv = false;
		try (Statement stmt = conn.createStatement()) {
			sql = Utils.replaceSchemaName(conn, sql);
			rv = stmt.execute(sql);
		} catch (SQLException e) {
			rv = false;
			log.error("ERROR EXECUTING: " + sql, e);
		}
		return rv;
	}

	//
	//
	public CachedRowSet executeQueryCRS(Connection conn, String sql, String... prms) throws SQLException {
		RowSetFactory factory = null;
		CachedRowSet crs = null;
		sql = Utils.replaceSchemaName(conn, sql);
		try (PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = executeQuery(ps, prms)) {
			factory = RowSetProvider.newFactory();
			crs = factory.createCachedRowSet();
			if (rs != null) {
				crs.populate(rs);
			}
		}
		return crs;
	}

	//
	public ResultSet executeQuery(PreparedStatement ps, String... prms) {
		ResultSet rs = null;
		try {
			if ((prms != null) && (prms.length > 0)) {
				for (int i = 0; i < prms.length; i++) {
					ps.setString(i + 1, prms[i]);
				}
			}
			rs = ps.executeQuery();
		} catch (SQLException e) {
			log.error("ERROR executing PreparedStatement", e);
		}
		return rs;
	}

}
