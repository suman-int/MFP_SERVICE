package com.mnao.mfp.common.dao;

import java.sql.ResultSet;
import java.sql.SQLException;

public abstract  class MetricData  {
	public abstract void setResultSetRow(ResultSet rs) throws SQLException ;
}