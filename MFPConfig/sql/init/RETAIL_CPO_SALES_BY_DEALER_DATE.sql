SELECT DLR_CD, SALES_DATE, TRANSACTION_TYPE, QTY
FROM $SCHEMA$FLAT_RETAIL_CPO_SALES_BY_DEALER_DATE
WHERE SALES_DATE BETWEEN ? AND ?
