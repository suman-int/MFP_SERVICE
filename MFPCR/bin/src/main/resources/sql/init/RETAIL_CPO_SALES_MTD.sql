SELECT DLR_CD, TRANSACTION_TYPE, SALE_YEAR_MONTH, CNT, QTY
FROM $SCHEMA$FLAT_RETAIL_CPO_SALES_MTD
WHERE SALE_YEAR_MONTH  >= TO_CHAR(ADD_MONTHS(TO_DATE (?, 'YYYY-MM-DD'), -CONVERT(?, SQL_INTEGER)), 'YYYYMM')
;
CREATE VIEW $SCHEMA$V_RETAIL_CPO_SALES_MTD_BY_MARKET
AS SELECT ASON,MDA_CD,SALE_YEAR_MONTH,B.SALES_MO_CLOSE_DAY,CNT,QTY 
FROM (SELECT ASON,Y.MDA_CD,SALE_YEAR_MONTH,COUNT(*)CNT,SUM(QTY)QTY 
	FROM $SCHEMA$RETAIL_CPO_SALES_MTD X, 
			$SCHEMA$DEALERS Y 
	WHERE X.DLR_CD=Y.DLR_CD 
	GROUP BY ASON,SALE_YEAR_MONTH,Y.MDA_CD) A,
	(SELECT DISTINCT SALES_YR_MO_ID,SALES_MO_CLOSE_DAY 
	FROM $SCHEMA$DATEMASTER) B 
	WHERE A.SALE_YEAR_MONTH=B.SALES_YR_MO_ID
;
CREATE VIEW $SCHEMA$V_RETAIL_CPO_SALES_MTD_BY_REGION 
AS SELECT ASON,RGN_CD SALE_YEAR_MONTH,B.SALES_MO_CLOSE_DAY,CNT,QTY 
FROM (SELECT ASON,Y.RGN_CD,SALE_YEAR_MONTH,COUNT(*)CNT,SUM(QTY)QTY 
		FROM $SCHEMA$RETAIL_CPO_SALES_MTD X,$SCHEMA$DEALERS Y 
		WHERE X.DLR_CD=Y.DLR_CD 
		GROUP BY ASON,SALE_YEAR_MONTH,Y.RGN_CD) A,
		(SELECT DISTINCT SALES_YR_MO_ID,SALES_MO_CLOSE_DAY 
		FROM $SCHEMA$DATEMASTER) B 
		WHERE A.SALE_YEAR_MONTH=B.SALES_YR_MO_ID
;
CREATE VIEW $SCHEMA$V_RETAIL_CPO_SALES_MTD_BY_REGION_ZONE 
AS SELECT ASON,RGN_CD,ZONE_CD,SALE_YEAR_MONTH,B.SALES_MO_CLOSE_DAY,CNT,QTY 
FROM (SELECT ASON,Y.RGN_CD,Y.ZONE_CD,SALE_YEAR_MONTH,COUNT(*)CNT,SUM(QTY)QTY 
		FROM $SCHEMA$RETAIL_CPO_SALES_MTD X,$SCHEMA$DEALERS Y 
		WHERE X.DLR_CD=Y.DLR_CD 
		GROUP BY ASON,SALE_YEAR_MONTH,Y.RGN_CD,Y.ZONE_CD) A,
		(SELECT DISTINCT SALES_YR_MO_ID,SALES_MO_CLOSE_DAY 
		FROM $SCHEMA$DATEMASTER) B 
		WHERE A.SALE_YEAR_MONTH=B.SALES_YR_MO_ID
;
-- $SCHEMA$V_RETAIL_CPO_SALES_MTD_BY_REGION_ZONE_DISTRICT source

CREATE VIEW $SCHEMA$V_RETAIL_CPO_SALES_MTD_BY_REGION_ZONE_DISTRICT
AS SELECT ASON,RGN_CD,ZONE_CD,DISTRICT_CD,SALE_YEAR_MONTH,B.SALES_MO_CLOSE_DAY,CNT,QTY 
FROM (SELECT ASON,Y.RGN_CD,Y.ZONE_CD,Y.DISTRICT_CD,SALE_YEAR_MONTH,COUNT(*)CNT,SUM(QTY)QTY 
		FROM $SCHEMA$RETAIL_CPO_SALES_MTD X,$SCHEMA$DEALERS Y 
		WHERE X.DLR_CD=Y.DLR_CD 
		GROUP BY ASON,SALE_YEAR_MONTH,Y.RGN_CD,Y.ZONE_CD,Y.DISTRICT_CD) A,
		(SELECT DISTINCT SALES_YR_MO_ID,SALES_MO_CLOSE_DAY 
		FROM $SCHEMA$DATEMASTER) B 
		WHERE A.SALE_YEAR_MONTH=B.SALES_YR_MO_ID;



