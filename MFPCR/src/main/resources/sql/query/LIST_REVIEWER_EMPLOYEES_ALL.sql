SELECT 	PRSN_ID_CD, STATUS_CD, PRSN_TYPE_CD, FRST_NM, MIDL_NM, LAST_NM, JOB_CD, LOCTN_CD, USERID, EMAIL_ADDR, 
		RGN_CD, ZONE_CD, DISTRICT_CD, TYPE_CD, JOB_TITLE_TX	
FROM 	(
		SELECT 	DISTINCT A.PRSN_ID_CD, A.STATUS_CD, A.PRSN_TYPE_CD, A.FRST_NM, A.MIDL_NM, A.LAST_NM, B.JOB_CD, B.LOCTN_CD, C.USERID, C.EMAIL_ADDR, 
		SUBSTR(B.LOCTN_CD, 1, 2) RGN_CD, SUBSTR(B.LOCTN_CD, 4, 2) ZONE_CD, ' ' DISTRICT_CD, SUBSTR(B.LOCTN_CD, 3, 1) TYPE_CD, D.JOB_TITLE_TX
		FROM 	$SCHEMA$BTC03020 A,
				$SCHEMA$BTC03050 B,
				$SCHEMA$BTC03110 C,
				$SCHEMA$BTC03010 D
		WHERE   A.PRSN_ID_CD = B.PRSN_ID_CD
		AND 	A.PRSN_ID_CD = C.PRSN_ID_CD
		AND 	D.JOB_CD = B.JOB_CD
		AND		PRSN_TYPE_CD = 'M'
		AND 	C.END_DT = '0001-01-01'
		AND 	B.JOB_CD = 'MZ11'
		UNION
		SELECT 	DISTINCT A.PRSN_ID_CD, A.STATUS_CD, A.PRSN_TYPE_CD, A.FRST_NM, A.MIDL_NM, A.LAST_NM, B.JOB_CD, B.LOCTN_CD, C.USERID, C.EMAIL_ADDR, 
		SUBSTR(B.LOCTN_CD, 1, 2) RGN_CD, ' ' ZONE_CD, ' ' DISTRICT_CD, SUBSTR(B.LOCTN_CD, 3, 1) TYPE_CD, D.JOB_TITLE_TX
		FROM 	$SCHEMA$BTC03020 A,
				$SCHEMA$BTC03050 B,
				$SCHEMA$BTC03110 C,
				$SCHEMA$BTC03010 D
		WHERE   A.PRSN_ID_CD = B.PRSN_ID_CD
		AND 	A.PRSN_ID_CD = C.PRSN_ID_CD
		AND 	D.JOB_CD = B.JOB_CD
		AND		PRSN_TYPE_CD = 'M'
		AND 	C.END_DT = '0001-01-01'
		AND 	B.JOB_CD IN ('MF11', 'MG11')
		) X
