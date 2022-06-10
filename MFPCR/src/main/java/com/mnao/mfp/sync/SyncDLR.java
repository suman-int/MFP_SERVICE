package com.mnao.mfp.sync;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Duration;
import java.time.Instant;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

import javax.sql.RowSet;
import javax.sql.rowset.CachedRowSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mnao.mfp.common.util.AppConstants;
import com.mnao.mfp.common.util.MFPDatabase;
import com.mnao.mfp.common.util.MFPDatabase.DB;
import com.mnao.mfp.common.util.Utils;

public class SyncDLR {
	//
	private static final Logger log = LoggerFactory.getLogger(SyncDLR.class);
	//
	private static final int ACQUIRE_LOCK_TIMEOUT = 15;

	public void startSync() {
		Instant start = Instant.now();
		MFPDatabase srcdb = new MFPDatabase(DB.mma);
		MFPDatabase mfpdb = new MFPDatabase(DB.mfp);
		try (Connection mfpconn = mfpdb.getConnection()) {
			if (!setLocks(mfpdb, mfpconn)) {
				// Unable to acquire lock.
				// Implies that another node is running
				return;
			}
			int minDays = Integer.parseInt(Utils.getAppProperty(AppConstants.DLR_SYNC_MIN_INTERVAL, "0"));
			Date lastUpdt = getLastUpdateDate(mfpdb, mfpconn, "DEALERS_STAGE", "W_UPDT_DT");
			if (lastUpdt == null) {
				lastUpdt = new Date(0);
			}
			log.debug("Last Update Date: " + lastUpdt);
			long timeDiff = Calendar.getInstance().getTimeInMillis() - lastUpdt.getTime();
			long dayDiff = TimeUnit.MILLISECONDS.toSeconds(timeDiff);
			if (dayDiff <= minDays) {
				// Exceptional situation where the clocks
				// of the different nodes are not synchronized
				// with a standard clock
				// Although in this case there would be no records to update,
				// still avoid the DB IO
				releaseLocks(mfpconn);
				return;
			}
			doSync(srcdb, mfpdb, mfpconn, lastUpdt);
			releaseLocks(mfpconn);
			//your code
			Instant end = Instant.now();
			Duration timeElapsed = Duration.between(start, end);
			log.info("Time taken to Sync Dealers : "+ timeElapsed.toMillis() +" milliseconds.");
		} catch (SQLException e1) {
			log.error("", e1);
		}
	}

	private void doSync(MFPDatabase srcdb, MFPDatabase mfpdb, Connection mfpconn, Date lastUpdt) {
		String sqlFolderName = Utils.getAppProperty(AppConstants.LOCATION_SQLFILES);
		if (!sqlFolderName.endsWith("/"))
			sqlFolderName += "/";
		String sqlName = sqlFolderName + AppConstants.SYNC_SCRIPTS_FOLDER + "/" + AppConstants.SQL_UNIQUE_DEALERS;
		String inSQL = Utils.readTextFromFile(sqlName);
		sqlName = sqlFolderName + AppConstants.SYNC_SCRIPTS_FOLDER + "/" + AppConstants.SQL_MERGE_UPDATE_DEALERS;
		String mergeSQL = Utils.readTextFromFile(sqlName);
		try (Connection srcConn = srcdb.getConnection();
				CachedRowSet crs = srcdb.executeQueryCRS(srcConn, inSQL, lastUpdt.toString())) {
			if (crs.size() > 0) {
				insertRecordsToStage(mfpdb, mfpconn, crs);
				mergeUpdateDealersFromStage(mfpdb, mfpconn, mergeSQL);
			}
		} catch (SQLException e) {
			log.error("", e);
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
			log.error("", e);
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
				log.debug("SYNC DEALERS process is already running. Skipping sync process in this instance.");
			}
		} catch (SQLException e) {
			log.error("", e);
		}
		return rv;
	}

	private void mergeUpdateDealersFromStage(MFPDatabase mfpdb, Connection mfpconn, String mergeSQL) {
		boolean rv = mfpdb.execute(mfpconn, mergeSQL);
		if (rv)
			log.debug("Successfully merged DEALERS from DEALERS_STAGE.");
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
			log.debug("Inserting " + ctr + " rows into DEALERS_STAGE.");
			int[] rins = ps.executeBatch();
		} catch (SQLException e) {
			log.error("", e);
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
				String colName = rsMeta.getColumnLabel(i);
				if (colName == null || colName.trim().length() == 0)
					colName = rsMeta.getColumnName(i);
				colList += colName;
				valList += "?";
			}
			insSql = "INSERT INTO DEALERS_STAGE (";
			insSql += colList;
			insSql += " ) VALUES (";
			insSql += valList;
			insSql += " )";
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			log.error("", e);
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
			log.error("", e);
		}
		return dt;
	}
}
