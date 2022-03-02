SELECT 	
	A.DLR_CD,
	B.DBA_NM,
	B.RGN_CD,
	B.MDA_CD,
	B.ZONE_CD,
	B.DISTRICT_CD,
	A.INV_YYMM,
	A.TOTAL_GROUND_INVENTORY,
	A.WHOLESALE_INTRANSIT,
	A.PORT_WATER_NONWHOLESALE_INTRANSIT,
	A.PRODUCED_INVENTORY,
	A.ON_ORDER,
	QTY,
	DSR
FROM
	(
	SELECT
		DLR_CD,
		INV_YYMM,
		SUM(IFNULL(TOTAL_GROUND_INVENTORY, 0)) TOTAL_GROUND_INVENTORY,
		SUM(IFNULL(WHOLESALE_INTRANSIT, 0)) WHOLESALE_INTRANSIT,
		SUM(IFNULL(PORT_WATER_NONWHOLESALE_INTRANSIT, 0)) PORT_WATER_NONWHOLESALE_INTRANSIT,
		SUM(IFNULL(PRODUCED_INVENTORY, 0)) PRODUCED_INVENTORY,
		SUM(IFNULL(ON_ORDER, 0)) ON_ORDER,
		SUM(IFNULL(QTY,0)) QTY,
		SUM(IFNULL(DSR,0)) DSR
	FROM
		$SCHEMA$FLAT_RETAIL_INVENTORY_BY_DEALER_MTD
	WHERE INV_YYMM  >= TO_NUMBER(TO_CHAR(ADD_MONTHS(TO_DATE (?, 'YYYY-MM-DD'), -CONVERT(?, SQL_INTEGER)), 'YYYYMM'))
	GROUP BY
		DLR_CD,
		INV_YYMM ) A,
	$SCHEMA$DEALERS B
WHERE 
	A.DLR_CD = B.DLR_CD