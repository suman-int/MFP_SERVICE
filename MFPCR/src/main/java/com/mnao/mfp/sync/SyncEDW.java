package com.mnao.mfp.sync;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

import javax.sql.RowSet;
import javax.sql.RowSetMetaData;
import javax.sql.rowset.CachedRowSet;

import com.mnao.mfp.common.util.AppConstants;
import com.mnao.mfp.common.util.MFPDatabase;
import com.mnao.mfp.common.util.MFPDatabase.DB;
import com.mnao.mfp.common.util.Utils;

public class SyncEDW {
	private static final int ACQUIRE_LOCK_TIMEOUT = 15;

	public void startSync() {
		MFPDatabase edwdb = new MFPDatabase(DB.edw);
		MFPDatabase mfpdb = new MFPDatabase(DB.mfp);
		try (Connection mfpconn = mfpdb.getConnection()) {
			if (!setLocks(mfpdb, mfpconn)) {
				// Unable to acquire lock.
				// Implies that another node is running
				return;
			}
			int minDays = Integer.parseInt(Utils.getAppProperty(AppConstants.EDW_SYNC_MIN_INTERVAL, "0"));
			Date lastUpdt = getLastUpdateDate(mfpdb, mfpconn, "DEALERS_STAGE", "W_UPDT_DT");
			if (lastUpdt == null) {
				lastUpdt = new Date(0);
			}
			System.out.println("Last Update Date: " + lastUpdt);
			long timeDiff = Calendar.getInstance().getTimeInMillis() - lastUpdt.getTime();
			long dayDiff = TimeUnit.MILLISECONDS.toDays(timeDiff);
			if (dayDiff <= minDays) {
				// Exceptional situation where the clocks
				// of the different nodes are not synchronized
				// with a standard clock
				// Although in this case there would be no records to update,
				// still avoid the DB IO
				releaseLocks(mfpconn);
				return;
			}
			doSync(edwdb, mfpdb, mfpconn, lastUpdt);
			releaseLocks(mfpconn);
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
	}

	private void doSync(MFPDatabase edwdb, MFPDatabase mfpdb, Connection mfpconn, Date lastUpdt) {
		String sqlFolderName = Utils.getAppProperty(AppConstants.LOCATION_SQLFILES);
		if (!sqlFolderName.endsWith("/"))
			sqlFolderName += "/";
		String sqlName = sqlFolderName + AppConstants.SYNC_SCRIPTS_FOLDER + "/" + AppConstants.SQL_UNIQUE_EDW_DEALERS;
		String inSQL = Utils.readTextFromFile(sqlName);
		sqlName = sqlFolderName + AppConstants.SYNC_SCRIPTS_FOLDER + "/" + AppConstants.SQL_MERGE_UPDATE_DEALERS;
		String mergeSQL = Utils.readTextFromFile(sqlName);
		try (Connection edwconn = edwdb.getConnection();
				CachedRowSet crs = edwdb.executeQueryCRS(edwconn, inSQL, lastUpdt.toString())) {
			if (crs.size() > 0) {
				insertRecordsToStage(mfpdb, mfpconn, crs);
				mergeUpdateDEalersFromStage(mfpdb, mfpconn, mergeSQL);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private boolean releaseLocks(Connection mfpconn) {
		boolean rv = false;
		try {
			mfpconn.commit();
			mfpconn.setAutoCommit(true);
			mfpconn.close();
			rv = true;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return rv;
	}

	private boolean setLocks(MFPDatabase mfpdb, Connection mfpconn) {
		boolean rv = false;
		try {
			mfpconn.setAutoCommit(false);
			mfpconn.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
			String sql = "LOCK TABLE $SCHEMA$DEALERS_STAGE IN EXCLUSIVE MODE";
			try (Statement stmt = mfpconn.createStatement()) {
				sql = Utils.replaceSchemaName(mfpconn, sql);
				stmt.setQueryTimeout(ACQUIRE_LOCK_TIMEOUT);
				stmt.execute(sql);
				rv = true;
			} catch (com.ibm.db2.jcc.am.SqlTimeoutException e) {
				System.out.println("SYNC WITH EDW process is already running. Skipping sync process in this instance.");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return rv;
	}

	private void mergeUpdateDEalersFromStage(MFPDatabase mfpdb, Connection mfpconn, String mergeSQL) {
		boolean rv = mfpdb.execute(mfpconn, mergeSQL);
		if (rv)
			System.out.println("Successfully merged DEALERS from DEALERS_STAGE.");
	}

	private void insertRecordsToStage(MFPDatabase db, Connection mfpconn, RowSet rs) {
		db.execute(mfpconn, "DELETE FROM $SCHEMA$DEALERS_STAGE");
		String insSql = getInsertStatement(rs);
		try (PreparedStatement ps = mfpconn.prepareStatement(insSql)) {
			ResultSetMetaData rsMeta = rs.getMetaData();
			int ctr = 0;
			while (rs.next()) {
				for (int i = 1; i <= rsMeta.getColumnCount(); i++) {
					if (MFPDatabase.getJavaDataType(rsMeta.getColumnType(i)).toString().endsWith("Timestamp"))
						ps.setDate(i, rs.getDate(i));
					else
						ps.setString(i, rs.getString(i));
				}
				ps.addBatch();
				ctr++;
			}
			System.out.println("Inserting " + ctr + " rows into DEALERS_STAGE.");
			int[] rins = ps.executeBatch();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private String getInsertStatement(RowSet rs) {
		String insSql = "";
		try {
			String colList = "";
			String valList = "";
			ResultSetMetaData rsMeta = rs.getMetaData();
			for (int i = 1; i <= rsMeta.getColumnCount(); i++) {
				if (i > 1) {
					colList += ", ";
					valList += ", ";
				}
				colList += rsMeta.getColumnName(i);
				valList += "?";
			}
			insSql = "INSERT INTO DEALERS_STAGE (";
			insSql += colList;
			insSql += " ) VALUES (";
			insSql += valList;
			insSql += " )";
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return insSql;
	}

	private Date getLastUpdateDate(MFPDatabase db, Connection mfpconn, String tbl, String col) {
		Date dt = null;
		String sql = "SELECT MAX(" + col + ") FROM $SCHEMA$" + tbl;
		try (RowSet rs = db.executeQueryCRS(mfpconn, sql, (String[]) null)) {
			while (rs.next()) {
				dt = rs.getDate(1);
				break;
			}
			rs.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return dt;
	}
}
