SELECT DISTINCT d.DLR_CD , d.DBA_NM , d.CITY_NM, d.ST_CD, d.ZIP1_CD
FROM $SCHEMA$DEALERS d 
{WHERE DEALERS:d}