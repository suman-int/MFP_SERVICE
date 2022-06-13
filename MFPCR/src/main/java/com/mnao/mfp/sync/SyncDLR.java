package com.mnao.mfp.sync;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
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
import com.mnao.mfp.sync.dto.MfpSyncStatus;
import com.mnao.mfp.sync.dto.MfpSyncStatus.SyncTypes;

public class SyncDLR extends SyncBase {
	//
	private static final Logger log = LoggerFactory.getLogger(SyncDLR.class);
	//
	private static final int ACQUIRE_LOCK_TIMEOUT = 15;

	public void startSync() {
		MfpSyncStatus mfpSyncStatus = new MfpSyncStatus(SyncTypes.DLRBATCH);
		;
		Instant start = Instant.now();
		mfpSyncStatus.setStartTime(new Timestamp(start.toEpochMilli()));
		MFPDatabase srcdb = new MFPDatabase(DB.mma);
		MFPDatabase mfpdb = new MFPDatabase(DB.mfp);
		try (Connection mfpconn = mfpdb.getConnection()) {
			if (!setLocks(mfpdb, mfpconn, mfpSyncStatus)) {
				// Unable to acquire lock.
				// Implies that another node is running
				mfpSyncStatus.addMessage("FAILED to lock DEALERS_STAGE. Aborting.");
				return;
			}
			int minDays = Integer.parseInt(Utils.getAppProperty(AppConstants.DLR_SYNC_MIN_INTERVAL, "0"));
			Date lastUpdt = getLastUpdateDate(mfpdb, mfpconn, "DEALERS_STAGE", "W_UPDT_DT", mfpSyncStatus);
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
				mfpSyncStatus.addMessage("Time is less than minimum difference configured: " + minDays + " ms.");
				releaseLocks(mfpconn);
				return;
			}
			doSync(srcdb, mfpdb, mfpconn, lastUpdt, mfpSyncStatus);
			releaseLocks(mfpconn);
			Instant end = Instant.now();
			Duration timeElapsed = Duration.between(start, end);
			mfpSyncStatus.setEndTime(new Timestamp(end.toEpochMilli()));
			log.info("Time taken to Sync Dealers : " + timeElapsed.toMillis() + " milliseconds.");
		} catch (SQLException e1) {
			mfpSyncStatus.addException(e1.toString());
			log.error("", e1);
		} finally {
			insertMfpStatus(mfpSyncStatus);
		}

	}

	private void doSync(MFPDatabase srcdb, MFPDatabase mfpdb, Connection mfpconn, Date lastUpdt, MfpSyncStatus mfpSyncStatus) {
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
				mfpSyncStatus.addMessage("Retrieved " + crs.size() + " rows from source.");
				insertRecordsToStage(mfpdb, mfpconn, crs, mfpSyncStatus);
				mergeUpdateDealersFromStage(mfpdb, mfpconn, mergeSQL);
			}
		} catch (SQLException e) {
			mfpSyncStatus.addException(e.toString());
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

	private boolean setLocks(MFPDatabase mfpdb, Connection mfpconn, MfpSyncStatus mfpSyncStatus) {
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
				mfpSyncStatus.addMessage("SYNC DEALERS process is already running. Skipping sync process in this instance.");
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

	private void insertRecordsToStage(MFPDatabase db, Connection mfpconn, RowSet rs, MfpSyncStatus mfpSyncStatus) {
		db.execute(mfpconn, "DELETE FROM $SCHEMA$DEALERS_STAGE");
		String insSql = getInsertStatement(rs);
		insSql = Utils.replaceSchemaName(mfpconn, insSql);
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
			mfpSyncStatus.addMessage("Inserted " + ctr + " rows from " + mfpconn.getSchema() + " into DEALERS_STAGE.");
			log.debug("Inserting " + ctr + " rows into DEALERS_STAGE.");
			int[] rins = ps.executeBatch();
		} catch (SQLException e) {
			mfpSyncStatus.addException(e.toString());
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
			insSql = "INSERT INTO $SCHEMA$DEALERS_STAGE (";
			insSql += colList;
			insSql += " ) VALUES (";
			insSql += valList;
			insSql += " )";
		} catch (SQLException e) {
			log.error("", e);
		}
		return insSql;
	}

	private Date getLastUpdateDate(MFPDatabase db, Connection mfpconn, String tbl, String col, MfpSyncStatus mfpSyncStatus) {
		Date dt = null;
		String sql = "SELECT MAX(" + col + ") FROM $SCHEMA$" + tbl;
		try (RowSet rs = db.executeQueryCRS(mfpconn, sql, (String[]) null)) {
			while (rs.next()) {
				dt = rs.getDate(1);
				break;
			}
			rs.close();
			mfpSyncStatus.addMessage("Retrieved Last Update Date: " + dt );
		} catch (SQLException e) {
			mfpSyncStatus.addException(e.toString());
			log.error("", e);
		}
		return dt;
	}
}
