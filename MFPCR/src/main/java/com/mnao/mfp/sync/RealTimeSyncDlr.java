package com.mnao.mfp.sync;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mnao.mfp.common.dao.DealerInfo;
import com.mnao.mfp.common.util.AppConstants;
import com.mnao.mfp.common.util.MFPDatabase;
import com.mnao.mfp.common.util.MFPDatabase.DB;
import com.mnao.mfp.common.util.Utils;
import com.mnao.mfp.sync.dto.MfpSyncStatus;
import com.mnao.mfp.sync.dto.MfpSyncStatus.SyncTypes;

public class RealTimeSyncDlr extends SyncBase implements Runnable {
	//
	private static final Logger log = LoggerFactory.getLogger(RealTimeSyncDlr.class);
	//
	private static final int ACQUIRE_LOCK_TIMEOUT = 15;
	//
	private List<DealerInfo> newDealers;
	private List<DealerInfo> modifiedDealers;

	//
	public RealTimeSyncDlr(List<DealerInfo> newDealers, List<DealerInfo> modifiedDealers) {
		super();
		this.newDealers = newDealers;
		this.modifiedDealers = modifiedDealers;
	}

	//
	@Override
	public void run() {
		startSync();
	}

	//
	public void startSync() {
		MfpSyncStatus mfpSyncStatus = new MfpSyncStatus(SyncTypes.DLRRT);
		;
		Instant start = Instant.now();
		mfpSyncStatus.setStartTime(new Timestamp(start.toEpochMilli()));
		MFPDatabase mfpdb = new MFPDatabase(DB.mfp);
		try (Connection mfpconn = mfpdb.getConnection()) {
			if (!setLocks(mfpdb, mfpconn)) {
				// Unable to acquire lock.
				// Implies that another node is running
				return;
			}
			int rsynced = doSync(mfpdb, mfpconn, mfpSyncStatus);
			releaseLocks(mfpconn);
			Instant end = Instant.now();
			Duration timeElapsed = Duration.between(start, end);
			mfpSyncStatus.setEndTime(new Timestamp(end.toEpochMilli()));
			mfpSyncStatus.setRowsSynced(rsynced);
			log.info("Time taken to Sync Dealers in Real Time : " + timeElapsed.toMillis() + " milliseconds.");
		} catch (SQLException e1) {
			mfpSyncStatus.addException(e1.toString());
			log.error("", e1);
		} finally {
			insertMfpStatus(mfpSyncStatus);
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

	private int doSync(MFPDatabase mfpdb, Connection mfpconn, MfpSyncStatus mfpSyncStatus) {
		String msg = "";
		int rsynced = 0;
		String sqlFolderName = Utils.getAppProperty(AppConstants.LOCATION_SQLFILES);
		if (!sqlFolderName.endsWith("/"))
			sqlFolderName += "/";
		if (newDealers != null && newDealers.size() > 0) {
			rsynced += newDealers.size();
			String sqlName = sqlFolderName + AppConstants.SYNC_SCRIPTS_FOLDER + "/"
					+ AppConstants.SQL_RTSYNC_DEALERS_INSERT;
			String inSQL = Utils.readTextFromFile(sqlName);
			int rcnt = updateBatch(mfpdb, mfpconn, inSQL, newDealers, mfpSyncStatus);
			msg += "INSERTED: " + rcnt + " rows.";
			log.info("" + rcnt + " new Dealers inserted into MFP_DB");
		}
		msg += "|";
		if (modifiedDealers != null && modifiedDealers.size() > 0) {
			rsynced += modifiedDealers.size();
			String sqlName = sqlFolderName + AppConstants.SYNC_SCRIPTS_FOLDER + "/"
					+ AppConstants.SQL_RTSYNC_DEALERS_UPDATE;
			String inSQL = Utils.readTextFromFile(sqlName);
			int rcnt = updateBatch(mfpdb, mfpconn, inSQL, modifiedDealers, mfpSyncStatus);
			msg += "UPDATED: " + rcnt + " rows. ";
			log.info("" + rcnt + " modified Dealers updated in MFP_DB");
		}
		mfpSyncStatus.setMessages(msg);
		return rsynced;
	}

	private int updateBatch(MFPDatabase mfpdb, Connection mfpconn, String inSQL, List<DealerInfo> dlrInfos,
			MfpSyncStatus mfpSyncStatus) {
		int rcnt = 0;
		StringBuilder sb = new StringBuilder();
		String rem = mfpSyncStatus.getRemarks();
		if (rem == null)
			rem = "";
		else
			rem = " | ";
		sb.append(rem);
		inSQL = Utils.replaceSchemaName(mfpconn, inSQL);
		try (PreparedStatement ps = mfpconn.prepareStatement(inSQL)) {
			for (DealerInfo dlr : dlrInfos) {
				if (rcnt > 0) {
					sb.append(", ");
				}
				setStatementParameterValues(ps, dlr);
				ps.addBatch();
				sb.append(dlr.getDlrCd());
				rcnt++;
			}
			int[] cnt = ps.executeBatch();
			mfpSyncStatus.setRemarks(sb.toString());
		} catch (SQLException e) {
			mfpSyncStatus.addException(e.toString());
			log.error("ERROR Inserting/Updating in Batch into DEALERS:", e);
		}
		return rcnt;
	}

	private void setStatementParameterValues(PreparedStatement ps, DealerInfo dlrInfo) throws SQLException {
		setStringValue(ps, 1, dlrInfo.getDbaNm());
		setStringValue(ps, 2, dlrInfo.getStatusCd());
		setStringValue(ps, 3, dlrInfo.getStatusDt());
		setStringValue(ps, 4, dlrInfo.getRgnCd());
		setStringValue(ps, 5, dlrInfo.getCityNm());
		setStringValue(ps, 6, dlrInfo.getZip1Cd());
		setStringValue(ps, 7, dlrInfo.getCntyCd());
		setStringValue(ps, 8, dlrInfo.getStCd());
		setStringValue(ps, 9, dlrInfo.getMdaCd());
		setStringValue(ps, 10, dlrInfo.getSoaNm());
		setStringValue(ps, 11, dlrInfo.getApptDt());
		setStringValue(ps, 12, dlrInfo.getTermDt());
		setStringValue(ps, 13, dlrInfo.getPrevDLrCd());
		setStringValue(ps, 14, dlrInfo.getNxtDlrCd());
		setStringValue(ps, 15, dlrInfo.getTimeZoneCd());
		setStringValue(ps, 16, dlrInfo.getSoaCd());
		setStringValue(ps, 17, dlrInfo.getUsedCarFl());
		setStringValue(ps, 18, dlrInfo.getCntryCd());
		setStringValue(ps, 19, dlrInfo.getZip2Cd());
		setStringValue(ps, 20, dlrInfo.getDlrInactvDt());
		setStringValue(ps, 21, dlrInfo.getSvcOnlyFl());
		setStringValue(ps, 22, dlrInfo.getSvcOnlyDt());
		setStringValue(ps, 23, dlrInfo.getZoneCd());
		setStringValue(ps, 24, dlrInfo.getDistrictCd());
		setStringValue(ps, 25, dlrInfo.getFacilityType());
		setStringValue(ps, 26, dlrInfo.getDlrCd());
	}

}
