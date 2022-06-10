package com.mnao.mfp.sync;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
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

public class RealTimeSyncDlr implements Runnable{
	//
	private static final Logger log = LoggerFactory.getLogger(RealTimeSyncDlr.class);
	//
	private static final int ACQUIRE_LOCK_TIMEOUT = 15;
	//
	List<DealerInfo> newDealers;
	List<DealerInfo> modifiedDealers;

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
		Instant start = Instant.now();
		MFPDatabase mfpdb = new MFPDatabase(DB.mfp);
		try (Connection mfpconn = mfpdb.getConnection()) {
			if (!setLocks(mfpdb, mfpconn)) {
				// Unable to acquire lock.
				// Implies that another node is running
				return;
			}
			doSync(mfpdb, mfpconn);
			releaseLocks(mfpconn);
			Instant end = Instant.now();
			Duration timeElapsed = Duration.between(start, end);
			log.info("Time taken to Sync Dealers in Real Time : " + timeElapsed.toMillis() + " milliseconds.");
		} catch (SQLException e1) {
			log.error("", e1);
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

	private void doSync(MFPDatabase mfpdb, Connection mfpconn) {
		String sqlFolderName = Utils.getAppProperty(AppConstants.LOCATION_SQLFILES);
		if (!sqlFolderName.endsWith("/"))
			sqlFolderName += "/";
		if (newDealers != null && newDealers.size() > 0) {
			String sqlName = sqlFolderName + AppConstants.SYNC_SCRIPTS_FOLDER + "/" + AppConstants.SQL_RTSYNC_DEALERS_INSERT;
			String inSQL = Utils.readTextFromFile(sqlName);
			int rcnt = updateBatch(mfpdb, mfpconn, inSQL, newDealers);
			log.info("" + rcnt + " new Dealers inserted into MFP_DB");
		}
		if (modifiedDealers != null && modifiedDealers.size() > 0) {
			String sqlName = sqlFolderName + AppConstants.SYNC_SCRIPTS_FOLDER + "/" + AppConstants.SQL_RTSYNC_DEALERS_UPDATE;
			String inSQL = Utils.readTextFromFile(sqlName);
			int rcnt = updateBatch(mfpdb, mfpconn, inSQL, modifiedDealers);
			log.info("" + rcnt + " modified Dealers updated in MFP_DB");
		}
	}

	private int updateBatch(MFPDatabase mfpdb, Connection mfpconn, String inSQL, List<DealerInfo> dlrInfos) {
		int rcnt = 0;
		try (PreparedStatement ps = mfpconn.prepareStatement(inSQL)) {
			for (DealerInfo dlr : dlrInfos) {
				setStatementParameterValues(ps, dlr);
				ps.addBatch();
				rcnt++;
			}
			int[] cnt = ps.executeBatch();
		} catch (SQLException e) {
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

	private void setStringValue(PreparedStatement ps, int idx, String value) throws SQLException {
		if (value == null)
			value = "";
		ps.setString(idx, value);
	}

}
