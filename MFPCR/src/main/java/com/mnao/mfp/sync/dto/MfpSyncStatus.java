package com.mnao.mfp.sync.dto;

import java.sql.Timestamp;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor

public class MfpSyncStatus {
	private int syncSeq; 
	private String syncType;
	private Timestamp startTime;
	private Timestamp endTime;
	private int rowsSynced;
	private String messages;
	private String exceptions;
	private String remarks;
	public enum SyncTypes {
		DLRRT,
		DLRBATCH
	}
	public MfpSyncStatus(SyncTypes sType) {
		super();
		this.syncType = sType.toString();
	}
	public void addMessage(String msg) {
		if( this.messages == null )
			this.messages = msg;
		else
			this.messages += "|" + msg;
	}
	public void addException(String excpt) {
		if( this.messages == null )
			this.messages = excpt;
		else
			this.messages += "|" + excpt;
	}
}
