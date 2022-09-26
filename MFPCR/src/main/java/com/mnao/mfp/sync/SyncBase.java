package com.mnao.mfp.sync;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mnao.mfp.common.util.AppConstants;
import com.mnao.mfp.common.util.MFPDatabase;
import com.mnao.mfp.common.util.MFPDatabase.DB;
import com.mnao.mfp.common.util.Utils;
import com.mnao.mfp.sync.dto.MfpSyncStatus;

public class SyncBase {
	//
	private static final Logger log = LoggerFactory.getLogger(SyncBase.class);

	//
	protected void setStringValue(PreparedStatement ps, int idx, String value) throws SQLException {
		if (value == null)
			value = "";
		ps.setString(idx, value);
	}

	protected void setIntValue(PreparedStatement ps, int idx, Integer value) throws SQLException {
		if (value == null)
			ps.setNull(idx, java.sql.Types.INTEGER);
		ps.setInt(idx, value);
	}

	protected void setDateValue(PreparedStatement ps, int idx, Date value) throws SQLException {
		if (value == null)
			ps.setNull(idx, java.sql.Types.DATE);
		else
			ps.setDate(idx, value);
	}

	protected void setTimestampValue(PreparedStatement ps, int idx, Timestamp value) throws SQLException {
		if (value == null)
			ps.setNull(idx, java.sql.Types.TIMESTAMP);
		else
			ps.setTimestamp(idx, value);
	}

	protected void insertMfpStatus(MfpSyncStatus mfpSyncStatus) {
		String sqlFolderName = Utils.getAppProperty(AppConstants.LOCATION_SQLFILES);
		if (!sqlFolderName.endsWith("/"))
			sqlFolderName += "/";
		String sqlName = sqlFolderName + AppConstants.SYNC_SCRIPTS_FOLDER + "/"
				+ AppConstants.SQL_INSERT_SYNC_STATUS;
		String inSQL = Utils.readTextFromFile(sqlName);
		MFPDatabase mfpdb = new MFPDatabase(DB.mfp);
		try (Connection mfpconn = mfpdb.getConnection()) {
			inSQL = Utils.replaceSchemaName(mfpconn, inSQL);
			try (PreparedStatement ps = mfpconn.prepareStatement(inSQL)) {
				setStatementParameterValues(ps, mfpSyncStatus);
				ps.executeUpdate();
			} catch (SQLException e) {
				log.error("ERROR Insertinginto MFP_SYNC_STATUS:", e);
			}
		} catch (SQLException e1) {
			log.error("", e1);
		}
	}

	private void setStatementParameterValues(PreparedStatement ps, MfpSyncStatus syncStatus) throws SQLException {
		setStringValue(ps, 1, syncStatus.getSyncType());
		setTimestampValue(ps, 2, syncStatus.getStartTime());
		setTimestampValue(ps, 3, syncStatus.getEndTime());
		setIntValue(ps, 4, syncStatus.getRowsSynced());
		setStringValue(ps, 5, syncStatus.getMessages());
		setStringValue(ps, 6, syncStatus.getExceptions());
		setStringValue(ps, 7, syncStatus.getRemarks());
	}

}
