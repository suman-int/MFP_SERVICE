package com.mnao.mfp.pdf.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.mnao.mfp.common.util.NullCheck;
import com.mnao.mfp.common.util.Utils;
import com.mnao.mfp.cr.entity.ContactReportDiscussion;
import com.mnao.mfp.cr.entity.ContactReportInfo;
import com.mnao.mfp.cr.entity.Dealers;
import com.mnao.mfp.cr.repository.DealerRepository;
import com.mnao.mfp.cr.util.ContactReportEnum;
import com.mnao.mfp.list.dao.ListPersonnel;
import com.mnao.mfp.list.emp.AllEmployeesCache;
import com.mnao.mfp.pdf.dao.DealerEmployeeInfo;
import com.mnao.mfp.pdf.dao.ReviewerEmployeeInfo;
import com.mnao.mfp.pdf.service.PDFService;
import com.mnao.mfp.sync.SyncDLR;
import com.mnao.mfp.user.dao.MFPUser;
import com.mnao.mfp.user.service.UserDetailsService;

@Component
public class PdfGenerateUtil {
	//
	private static final Logger log = LoggerFactory.getLogger(PdfGenerateUtil.class);
	//
	@Autowired
	PDFService pdfService;

	@Autowired
	AllEmployeesCache allEmpCache;
	
	@Autowired
	private DealerRepository dealerRepo;

	private final String HTML = "<html>\r\n" + "\r\n" + "<head>\r\n"
			+ "    <meta http-equiv=Content-Type content=\"text/html; charset=UTF-8\">\r\n"
			+ "    <style type=\"text/css\">\r\n" + "        \r\n" + "      table,\r\n" + "      tr,\r\n"
			+ "      td,\r\n" + "      th,tbody {\r\n" + "        border: 1px solid black;\r\n"
			+ "        font-size: 15px;\r\n" + "        text-align: left;\r\n"
//			+ "        margin-left: auto;\r\n"
//			+ "        margin-right: auto;\r\n"
			+ "        width: 750px;\r\n" + "      }\r\n" + "\r\n" + "@page {\r\n" + "    size: A4;"
			+ "	   margin: 0;" + "}" + "\r\n" + ":root {\r\n" + "	--blue: #007bff;\r\n" + "	--indigo: #6610f2;\r\n"
			+ "	--purple: #6f42c1;\r\n" + "	--pink: #e83e8c;\r\n" + "	--red: #dc3545;\r\n"
			+ "	--orange: #fd7e14;\r\n" + "	--yellow: #ffc107;\r\n" + "	--green: #28a745;\r\n"
			+ "	--teal: #20c997;\r\n" + "	--cyan: #17a2b8;\r\n" + "	--white: #fff;\r\n" + "	--gray: #6c757d;\r\n"
			+ "	--gray-dark: #343a40;\r\n" + "	--primary: #007bff;\r\n" + "	--secondary: #6c757d;\r\n"
			+ "	--success: #28a745;\r\n" + "	--info: #17a2b8;\r\n" + "	--warning: #ffc107;\r\n"
			+ "	--danger: #dc3545;\r\n" + "	--light: #f8f9fa;\r\n" + "	--dark: #343a40;\r\n"
			+ "	--breakpoint-xs: 0;\r\n" + "	--breakpoint-sm: 576px;\r\n" + "	--breakpoint-md: 768px;\r\n"
			+ "	--breakpoint-lg: 992px;\r\n" + "	--breakpoint-xl: 1200px;\r\n"
			+ "	--font-family-sans-serif: -apple-system, BlinkMacSystemFont, \"Segoe UI\", Roboto, \"Helvetica Neue\", Arial, \"Noto Sans\", \"Liberation Sans\", sans-serif, \"Apple Color Emoji\", \"Segoe UI Emoji\", \"Segoe UI Symbol\", \"Noto Color Emoji\";\r\n"
			+ "	--font-family-monospace: SFMono-Regular, Menlo, Monaco, Consolas, \"Liberation Mono\", \"Courier New\", monospace\r\n"
			+ "}\r\n" + "\r\n" + "*,\r\n" + "::after,\r\n" + "::before {\r\n" + "	box-sizing: border-box\r\n"
			+ "}\r\n" + "\r\n" + "html {\r\n" + "	font-family: sans-serif;\r\n" + "	line-height: 1.15;\r\n"
			+ "	-webkit-text-size-adjust: 100%;\r\n" + "	-webkit-tap-highlight-color: transparent\r\n" + "}\r\n"
			+ "\r\n" + "body {\r\n" + "	margin: 0;\r\n"
			+ "	font-family: -apple-system, BlinkMacSystemFont, \"Segoe UI\", Roboto, \"Helvetica Neue\", Arial, \"Noto Sans\", \"Liberation Sans\", sans-serif, \"Apple Color Emoji\", \"Segoe UI Emoji\", \"Segoe UI Symbol\", \"Noto Color Emoji\";\r\n"
			+ "	font-size: 16px;\r\n" + "	font-weight: 400;\r\n" + "	line-height: 1.5;\r\n" + "	color: #212529;\r\n"
			+ "	text-align: left;\r\n" + "	background-color: #fff\r\n" + "}\r\n" + "\r\n"
			+ "[tabindex=\"-1\"]:focus:not(:focus-visible) {\r\n" + "	outline: 0!important\r\n" + "}\r\n" + "\r\n"
			+ "h1,\r\n" + "h3,\r\n" + "h5 {\r\n" + "	margin-top: 0;\r\n" + "	margin-bottom: 8px\r\n" + "}\r\n"
			+ "\r\n" + "table {\r\n" + "	border-collapse: collapse\r\n" + "}\r\n" + "\r\n"
			+ "button:focus:not(:focus-visible) {\r\n" + "	outline: 0\r\n" + "}\r\n" + "\r\n"
			+ "[type=button]:not(:disabled),\r\n" + "[type=reset]:not(:disabled),\r\n"
			+ "[type=submit]:not(:disabled),\r\n" + "button:not(:disabled) {\r\n" + "	cursor: pointer\r\n" + "}\r\n"
			+ "\r\n" + "::-webkit-file-upload-button {\r\n" + "	font: inherit;\r\n" + "	-webkit-appearance: button\r\n"
			+ "}\r\n" + "\r\n" + "h1,\r\n" + "h3,\r\n" + "h5 {\r\n" + "	margin-bottom: 8px;\r\n"
			+ "	font-weight: 500;\r\n" + "	line-height: 1.2\r\n" + "}\r\n" + "\r\n" + "h1 {\r\n"
			+ "	font-size: 32px\r\n" + "}\r\n" + "\r\n" + "h3 {\r\n" + "	font-size: 28px\r\n" + "}\r\n" + "\r\n"
			+ "h5 {\r\n" + "	font-size: 20px\r\n" + "}\r\n" + "\r\n" + ".container {\r\n"
			+ "	width: 100%!important;\r\n" + "	padding-right: 15px;\r\n" + "	padding-left: 15px;\r\n"
			+ "	margin-right: auto;\r\n" + "	margin-left: auto\r\n" + "}\r\n" + "\r\n"
			+ "@media (min-width:576px) {\r\n" + "	.container {\r\n" + "		max-width: 540px\r\n" + "	}\r\n"
			+ "}\r\n" + "\r\n" + "@media (min-width:768px) {\r\n" + "	.container {\r\n"
			+ "		max-width: 720px\r\n" + "	}\r\n" + "}\r\n" + "\r\n" + "@media (min-width:992px) {\r\n"
			+ "	.container {\r\n" + "		max-width: 960px\r\n" + "	}\r\n" + "}\r\n" + "\r\n"
			+ "@media (min-width:1200px) {\r\n" + "	.container {\r\n" + "		max-width: 1140px\r\n" + "	}\r\n"
			+ "}\r\n" + "\r\n" + ".table {\r\n"
//			+ "	width: 100% !important;\r\n"
			+ "	margin-bottom: 16px;\r\n" + "	color: #212529\r\n" + "}\r\n" + "\r\n" + ".table td {\r\n"
			+ "	padding: 12px;\r\n" + "	vertical-align: top;\r\n" + "	border-top: 1px solid #dee2e6\r\n" + "}\r\n"
			+ "\r\n" + ".custom-control-input.is-valid:focus:not(:checked)~.custom-control-label::before,\r\n"
			+ ".was-validated .custom-control-input:valid:focus:not(:checked)~.custom-control-label::before {\r\n"
			+ "	border-color: #28a745\r\n" + "}\r\n" + "\r\n"
			+ ".custom-control-input.is-invalid:focus:not(:checked)~.custom-control-label::before,\r\n"
			+ ".was-validated .custom-control-input:invalid:focus:not(:checked)~.custom-control-label::before {\r\n"
			+ "	border-color: #dc3545\r\n" + "}\r\n" + "\r\n" + ".btn:not(:disabled):not(.disabled) {\r\n"
			+ "	cursor: pointer\r\n" + "}\r\n" + "\r\n" + ".btn-primary:not(:disabled):not(.disabled).active,\r\n"
			+ ".btn-primary:not(:disabled):not(.disabled):active {\r\n" + "	color: #fff;\r\n"
			+ "	background-color: #0062cc;\r\n" + "	border-color: #005cbf\r\n" + "}\r\n" + "\r\n"
			+ ".btn-primary:not(:disabled):not(.disabled).active:focus,\r\n"
			+ ".btn-primary:not(:disabled):not(.disabled):active:focus {\r\n"
			+ "	box-shadow: 0 0 0 3.2px rgba(38, 143, 255, .5)\r\n" + "}\r\n" + "\r\n"
			+ ".btn-secondary:not(:disabled):not(.disabled).active,\r\n"
			+ ".btn-secondary:not(:disabled):not(.disabled):active {\r\n" + "	color: #fff;\r\n"
			+ "	background-color: #545b62;\r\n" + "	border-color: #4e555b\r\n" + "}\r\n" + "\r\n"
			+ ".btn-secondary:not(:disabled):not(.disabled).active:focus,\r\n"
			+ ".btn-secondary:not(:disabled):not(.disabled):active:focus {\r\n"
			+ "	box-shadow: 0 0 0 3.2px rgba(130, 138, 145, .5)\r\n" + "}\r\n" + "\r\n"
			+ ".btn-success:not(:disabled):not(.disabled).active,\r\n"
			+ ".btn-success:not(:disabled):not(.disabled):active {\r\n" + "	color: #fff;\r\n"
			+ "	background-color: #1e7e34;\r\n" + "	border-color: #1c7430\r\n" + "}\r\n" + "\r\n"
			+ ".btn-success:not(:disabled):not(.disabled).active:focus,\r\n"
			+ ".btn-success:not(:disabled):not(.disabled):active:focus {\r\n"
			+ "	box-shadow: 0 0 0 3.2px rgba(72, 180, 97, .5)\r\n" + "}\r\n" + "\r\n"
			+ ".btn-info:not(:disabled):not(.disabled).active,\r\n"
			+ ".btn-info:not(:disabled):not(.disabled):active {\r\n" + "	color: #fff;\r\n"
			+ "	background-color: #117a8b;\r\n" + "	border-color: #10707f\r\n" + "}\r\n" + "\r\n"
			+ ".btn-info:not(:disabled):not(.disabled).active:focus,\r\n"
			+ ".btn-info:not(:disabled):not(.disabled):active:focus {\r\n"
			+ "	box-shadow: 0 0 0 3.2px rgba(58, 176, 195, .5)\r\n" + "}\r\n" + "\r\n"
			+ ".btn-warning:not(:disabled):not(.disabled).active,\r\n"
			+ ".btn-warning:not(:disabled):not(.disabled):active {\r\n" + "	color: #212529;\r\n"
			+ "	background-color: #d39e00;\r\n" + "	border-color: #c69500\r\n" + "}\r\n" + "\r\n"
			+ ".btn-warning:not(:disabled):not(.disabled).active:focus,\r\n"
			+ ".btn-warning:not(:disabled):not(.disabled):active:focus {\r\n"
			+ "	box-shadow: 0 0 0 .23.2px rgba(222, 170, 12, .5)\r\n" + "}\r\n" + "\r\n"
			+ ".btn-danger:not(:disabled):not(.disabled).active,\r\n"
			+ ".btn-danger:not(:disabled):not(.disabled):active {\r\n" + "	color: #fff;\r\n"
			+ "	background-color: #bd2130;\r\n" + "	border-color: #b21f2d\r\n" + "}\r\n" + "\r\n"
			+ ".btn-danger:not(:disabled):not(.disabled).active:focus,\r\n"
			+ ".btn-danger:not(:disabled):not(.disabled):active:focus {\r\n"
			+ "	box-shadow: 0 0 0 3.2px rgba(225, 83, 97, .5)\r\n" + "}\r\n" + "\r\n"
			+ ".btn-light:not(:disabled):not(.disabled).active,\r\n"
			+ ".btn-light:not(:disabled):not(.disabled):active {\r\n" + "	color: #212529;\r\n"
			+ "	background-color: #dae0e5;\r\n" + "	border-color: #d3d9df\r\n" + "}\r\n" + "\r\n"
			+ ".btn-light:not(:disabled):not(.disabled).active:focus,\r\n"
			+ ".btn-light:not(:disabled):not(.disabled):active:focus {\r\n"
			+ "	box-shadow: 0 0 0 3.2px rgba(216, 217, 219, .5)\r\n" + "}\r\n" + "\r\n"
			+ ".btn-dark:not(:disabled):not(.disabled).active,\r\n"
			+ ".btn-dark:not(:disabled):not(.disabled):active {\r\n" + "	color: #fff;\r\n"
			+ "	background-color: #1d2124;\r\n" + "	border-color: #171a1d\r\n" + "}\r\n" + "\r\n"
			+ ".btn-dark:not(:disabled):not(.disabled).active:focus,\r\n"
			+ ".btn-dark:not(:disabled):not(.disabled):active:focus {\r\n"
			+ "	box-shadow: 0 0 0 3.2px rgba(82, 88, 93, .5)\r\n" + "}\r\n" + "\r\n"
			+ ".btn-outline-primary:not(:disabled):not(.disabled).active,\r\n"
			+ ".btn-outline-primary:not(:disabled):not(.disabled):active {\r\n" + "	color: #fff;\r\n"
			+ "	background-color: #007bff;\r\n" + "	border-color: #007bff\r\n" + "}\r\n" + "\r\n"
			+ ".btn-outline-primary:not(:disabled):not(.disabled).active:focus,\r\n"
			+ ".btn-outline-primary:not(:disabled):not(.disabled):active:focus {\r\n"
			+ "	box-shadow: 0 0 0 3.2px rgba(0, 123, 255, .5)\r\n" + "}\r\n" + "\r\n"
			+ ".btn-outline-secondary:not(:disabled):not(.disabled).active,\r\n"
			+ ".btn-outline-secondary:not(:disabled):not(.disabled):active {\r\n" + "	color: #fff;\r\n"
			+ "	background-color: #6c757d;\r\n" + "	border-color: #6c757d\r\n" + "}\r\n" + "\r\n"
			+ ".btn-outline-secondary:not(:disabled):not(.disabled).active:focus,\r\n"
			+ ".btn-outline-secondary:not(:disabled):not(.disabled):active:focus {\r\n"
			+ "	box-shadow: 0 0 0 3.2px rgba(108, 117, 125, .5)\r\n" + "}\r\n" + "\r\n"
			+ ".btn-outline-success:not(:disabled):not(.disabled).active,\r\n"
			+ ".btn-outline-success:not(:disabled):not(.disabled):active {\r\n" + "	color: #fff;\r\n"
			+ "	background-color: #28a745;\r\n" + "	border-color: #28a745\r\n" + "}\r\n" + "\r\n"
			+ ".btn-outline-success:not(:disabled):not(.disabled).active:focus,\r\n"
			+ ".btn-outline-success:not(:disabled):not(.disabled):active:focus {\r\n"
			+ "	box-shadow: 0 0 0 3.2px rgba(40, 167, 69, .5)\r\n" + "}\r\n" + "\r\n"
			+ ".btn-outline-info:not(:disabled):not(.disabled).active,\r\n"
			+ ".btn-outline-info:not(:disabled):not(.disabled):active {\r\n" + "	color: #fff;\r\n"
			+ "	background-color: #17a2b8;\r\n" + "	border-color: #17a2b8\r\n" + "}\r\n" + "\r\n"
			+ ".btn-outline-info:not(:disabled):not(.disabled).active:focus,\r\n"
			+ ".btn-outline-info:not(:disabled):not(.disabled):active:focus {\r\n"
			+ "	box-shadow: 0 0 0 3.2px rgba(23, 162, 184, .5)\r\n" + "}\r\n" + "\r\n"
			+ ".btn-outline-warning:not(:disabled):not(.disabled).active,\r\n"
			+ ".btn-outline-warning:not(:disabled):not(.disabled):active {\r\n" + "	color: #212529;\r\n"
			+ "	background-color: #ffc107;\r\n" + "	border-color: #ffc107\r\n" + "}\r\n" + "\r\n"
			+ ".btn-outline-warning:not(:disabled):not(.disabled).active:focus,\r\n"
			+ ".btn-outline-warning:not(:disabled):not(.disabled):active:focus {\r\n"
			+ "	box-shadow: 0 0 0 3.2px rgba(255, 193, 7, .5)\r\n" + "}\r\n" + "\r\n"
			+ ".btn-outline-danger:not(:disabled):not(.disabled).active,\r\n"
			+ ".btn-outline-danger:not(:disabled):not(.disabled):active {\r\n" + "	color: #fff;\r\n"
			+ "	background-color: #dc3545;\r\n" + "	border-color: #dc3545\r\n" + "}\r\n" + "\r\n"
			+ ".btn-outline-danger:not(:disabled):not(.disabled).active:focus,\r\n"
			+ ".btn-outline-danger:not(:disabled):not(.disabled):active:focus {\r\n"
			+ "	box-shadow: 0 0 0 3.2px rgba(220, 53, 69, .5)\r\n" + "}\r\n" + "\r\n"
			+ ".btn-outline-light:not(:disabled):not(.disabled).active,\r\n"
			+ ".btn-outline-light:not(:disabled):not(.disabled):active {\r\n" + "	color: #212529;\r\n"
			+ "	background-color: #f8f9fa;\r\n" + "	border-color: #f8f9fa\r\n" + "}\r\n" + "\r\n"
			+ ".btn-outline-light:not(:disabled):not(.disabled).active:focus,\r\n"
			+ ".btn-outline-light:not(:disabled):not(.disabled):active:focus {\r\n"
			+ "	box-shadow: 0 0 0 3.2px rgba(248, 249, 250, .5)\r\n" + "}\r\n" + "\r\n"
			+ ".btn-outline-dark:not(:disabled):not(.disabled).active,\r\n"
			+ ".btn-outline-dark:not(:disabled):not(.disabled):active {\r\n" + "	color: #fff;\r\n"
			+ "	background-color: #343a40;\r\n" + "	border-color: #343a40\r\n" + "}\r\n" + "\r\n"
			+ ".btn-outline-dark:not(:disabled):not(.disabled).active:focus,\r\n"
			+ ".btn-outline-dark:not(:disabled):not(.disabled):active:focus {\r\n"
			+ "	box-shadow: 0 0 0 3.2px rgba(52, 58, 64, .5)\r\n" + "}\r\n" + "\r\n"
			+ ".custom-control-input:focus:not(:checked)~.custom-control-label::before {\r\n"
			+ "	border-color: #80bdff\r\n" + "}\r\n" + "\r\n"
			+ ".custom-control-input:not(:disabled):active~.custom-control-label::before {\r\n" + "	color: #fff;\r\n"
			+ "	background-color: #b3d7ff;\r\n" + "	border-color: #b3d7ff\r\n" + "}\r\n" + "\r\n"
			+ ".close:not(:disabled):not(.disabled):focus,\r\n" + ".close:not(:disabled):not(.disabled):hover {\r\n"
			+ "	opacity: .75\r\n" + "}\r\n" + "\r\n"
			+ "@supports ((position:-webkit-sticky) or (position:sticky)) {}\r\n" + "\r\n" + "@media print {\r\n"
			+ "	*,\r\n" + "	::after,\r\n" + "	::before {\r\n" + "		text-shadow: none!important;\r\n"
			+ "		box-shadow: none!important\r\n" + "	}\r\n" + "	tr {\r\n" + "		page-break-inside: avoid\r\n"
			+ "	}\r\n" + "	h3 {\r\n" + "		orphans: 3;\r\n" + "		widows: 3\r\n" + "	}\r\n" + "	h3 {\r\n"
			+ "		page-break-after: avoid\r\n" + "	}\r\n" + "	@page {\r\n" + "		size: a3\r\n" + "	}\r\n"
			+ "	body {\r\n" + "		min-width: 992px!important\r\n" + "	}\r\n" + "	.container {\r\n"
			+ "		min-width: 992px!important\r\n" + "	}\r\n" + "	.table {\r\n"
			+ "		border-collapse: collapse!important\r\n" + "	}\r\n" + "	.table td {\r\n"
			+ "		background-color: #fff!important\r\n" + "	}\r\n" + "}\r\n" + "\r\n" + "\r\n" + "\r\n" + "\r\n"
			+ "    </style>\r\n" + "   \r\n" + "  \r\n" + "</head>\r\n" + "\r\n" + "<body>\r\n"
			+ "    <!-- section 1 -->\r\n" + "    <div >\r\n" + "\r\n" + "        <div class=\"container\">\r\n"
			+ "            <h3 style=\"text-align: center;\">Contact Report</h3>     \r\n"
			+ "            <table class=\"table\">\r\n" + "                <h5>1. Dealership Details</h5>\r\n"
			+ "              <tbody>\r\n" + "                <tr>\r\n"
			+ "                  <td colspan=\"3\"><div style=\"font-weight: 600;\">DEALERSHIP NAME :</div>\r\n"
			+ "                      %DEALER_NAME% - %DEALER_CODE%</td>\r\n"
			+ "                  <td rowspan=\"3\" style=\"text-align: center; padding-top: 7%;\"><h1>STATUS : %CONTACT_STATUS%</h1></td>\r\n"
			+ "                </tr>\r\n" + "                <tr>\r\n"
			+ "                    <td colspan=\"3\"><div style=\"font-weight: 600;\">REVIEWER :</div>\r\n"
			+ "                        %REVIEWER%\r\n" + "                    </td>\r\n" + "                </tr>\r\n"
			+ "                <tr>\r\n"
			+ "                    <td colspan=\"3\"><div style=\"font-weight: 600;\">AUTHOR : (Name,Title)</div>\r\n"
			+ "                       %AUTHOR_NAME%</td>\r\n" + "                </tr>\r\n" + "                <tr>\r\n"
			+ "                  <td>\r\n"
			+ "                    <div style=\"font-weight: 600;\"> CONTACT LOCATION :</div>\r\n"
			+ "                      %ADDRESS%\r\n" + "                  </td>\r\n" + "                  <td>\r\n"
			+ "                    <div style=\"font-weight: 600;\">  CONTACT REPORT :</div>\r\n"
			+ "                      %REPORT%\r\n" + "                  </td>\r\n"
			+ "                  <td>Page %CURRENT_PAGE% of %TOTAL_PAGE%</td>\r\n" + "                  <td>\r\n"
			+ "                    <div style=\"font-weight: 600;\"> CONTACT DATE :</div>\r\n"
			+ "                    %CR_DATE%<br>\r\n" + "                    (Month, Day, year)\r\n"
			+ "                </td>\r\n" + "                </tr>\r\n" + "              </tbody>\r\n"
			+ "            </table>\r\n" + "\r\n" + "\r\n" + "            <table class=\"table\">\r\n"
			+ "                <h5>2. Personnel</h5>\r\n" + "                <tbody>\r\n" + "                  <tr>\r\n"
			+ "                    <td style=\"font-weight: 600;\">CORPORATE PERSONNEL :</td>\r\n"
			+ "                        <td> %CORP_PERSONNEL%</td>\r\n" + "                  </tr>\r\n"
//			+ "                  <tr>\r\n" + "                    <td style=\"font-weight: 600;\">REVIEWER :</td>\r\n"
//			+ "                    <td> %DEALERSHIP_REVIEWER%</td>\r\n" + "                  </tr>\r\n"
			+ "                  <tr>\r\n"
			+ "                     <td style=\"font-weight: 600;\">DEALERSHIP CONTACTS :</td>\r\n"
			+ "                     <td>%DEALERSHIP_CONTACTS%</td>   \r\n" + "                  </tr>\r\n"
			+ "                  </tbody>\r\n" + "\r\n" + "            </table>\r\n" + "\r\n" + "\r\n"
			+ "                <table class=\"table\">\r\n" + "                    <h5>3. Discussion</h5>\r\n"
			+ "                    <tbody>\r\n" + "                      <tr>\r\n"
			+ "                        <td style=\"font-weight: 600;\">TITLE :</td>\r\n"
			+ "                        <td style=\"font-weight: 600;\">DISCUSSION :</td>\r\n"
			+ "                        <td style=\"font-weight: 600;\">DATE :</td>\r\n"
			+ "                      </tr>\r\n" + " 					%DISCUSIION_ROW%" + "\r\n"
			+ "                    </tbody>\r\n" + "                  </table>\r\n" + "            </div>\r\n"
			+ "<!-- section5 -->\r\n" + "        <div class=\"container\">\r\n" + "       <h5 >4. Verification</h5>\r\n"
			+ "       <div >I have read and understand the report. I have verified that the appointment and information identified above is true to the best of my information and belief.</div>\r\n"
			+ "      <div><span>Comment :</span>  __________________________________________________________________________________________________________________________________</div>\r\n"
			+ "      <div style=\"text-align: right;\">Contact Form</div>\r\n"
			+ "      <div style=\"text-align: right;\">Toll-Free Helpline: </div>\r\n" + "    </div>\r\n"
			+ "    </div>\r\n" + "\r\n" + "</body>\r\n" + "\r\n" + "</html>";

	private final String DISCUSSION = "                      <tr>\r\n"
			+ "                        <td>%DISC_TYPE%</td>\r\n"
			+ "                        <td>%DISC_DISCUSSIONS%</td>\r\n"
			+ "                        <td>%DISC_DATE%</td>\r\n" + "                      </tr>\r\n";

	public List<String> replaceStringWithData(List<ContactReportInfo> contactReports, MFPUser mfpUser) {
		List<String> fullHtml = new ArrayList<>();
		AtomicInteger currentPageNumber = new AtomicInteger(0);
		contactReports.forEach(cr -> {
			log.debug("" + currentPageNumber.get());
			Dealers dealers = new NullCheck<>(cr).with(ContactReportInfo::getDealers).orElse(new Dealers());
			List<DealerEmployeeInfo> dps = pdfService.getDealerEmployeeInfos(mfpUser, cr.getDlrCd(),
					cr.getDealerPersonnels());
			ListPersonnel rvr = allEmpCache.getByPrsnIdCd(cr.getContactReviewer());
			String corpsStr = cr.getCorporateReps();
			List<String> corpPersons = new ArrayList<>();
			if (corpsStr != null && corpsStr.trim().length() > 0) {
				String[] cps = corpsStr.split("[,]");
				for (String s : cps) {
					ListPersonnel le = allEmpCache.getByPrsnIdCd(s);
					if (le != null)
						corpPersons.add(Utils.getNameString(le.getFirstNm(), le.getLastNm()));
					else
						corpPersons.add(s);
				}
			}
			//
			if (new NullCheck<>(cr).with(ContactReportInfo::getDealers).isNull()) {
				Optional<Dealers> dealerData = dealerRepo.findById(new NullCheck<>(cr).with(ContactReportInfo::getDlrCd).get());
				if (dealerData.isPresent()) {
					dealers = dealerData.get();
				}
			}
			String updatedHtmlText = HTML.replace("%DEALER_NAME%", dealers.getDbaNm())
					.replace("%DEALER_CODE%", dealers.getDlrCd())
					.replace("%CURRENT_PAGE%", String.valueOf(currentPageNumber.incrementAndGet()))
					.replace("%TOTAL_PAGE%", String.valueOf(contactReports.size()));
			String updatedHtml3 = updatedHtmlText
					.replace("%REVIEWER%",
							new NullCheck<>(
									rvr == null ? " " : Utils.getNameString(rvr.getFirstNm(), rvr.getLastNm(), ",", rvr.getJobTitleFx()))
									.orElse(""))
					.replace("%AUTHOR_NAME%", new NullCheck<>(getAuthorUser(mfpUser, cr.getContactAuthor())).orElse(""))
					.replace("%ADDRESS%", new NullCheck<>(cr).with(ContactReportInfo::getContactLocation).orElse(""))
					.replace("%REPORT%", cr.getContactType())
					.replace("%CORP_PERSONNEL%", corpPersons.stream().collect(Collectors.joining("<br>")))
					.replace("%CR_DATE%", cr.getContactDt().format(DateTimeFormatter.ofPattern("MM-dd-yyyy")));
			String updatedHtml = updatedHtml3.replace("%CONTACT_STATUS%",
					ContactReportEnum.valueByStatus(cr.getContactStatus()).getStatusText().toUpperCase());
			String dealerPersonnel = dps.stream()
					.map(data -> Utils.getNameString(data.getFirstNm(), data.getMidlNm(), data.getLastNm()))
					.collect(Collectors.joining("<br>"));
			if (cr.getAddDealerPersonnel() != null && cr.getAddDealerPersonnel().trim().length() > 0) {
				List<String> addDps = Arrays.asList(cr.getAddDealerPersonnel().split("[,;]"));
				String strAddDps = addDps.stream().collect(Collectors.joining("<br>"));
				dealerPersonnel += "<br>" + strAddDps;
			}
			String updateHtml4 = updatedHtml
					.replace("%DLR_AUTHOR%", new NullCheck<>(getAuthorUser(mfpUser, cr.getContactAuthor())).orElse(""))
					.replace("%DEALERSHIP_CONTACTS%", new NullCheck<>(dealerPersonnel).orElse(""));
			String discussionList = cr.getDiscussions().stream().map(disc -> DISCUSSION
					.replace("%DISC_TYPE%", new NullCheck<>(disc).with(ContactReportDiscussion::getTopic).orElse(""))
					.replace("%DISC_DISCUSSIONS%",
							new NullCheck<>(disc).with(ContactReportDiscussion::getDiscussion).orElse(""))
					.replace("%DISC_DATE%",
							new NullCheck<>(disc).with(ContactReportDiscussion::getDisscussionDt)
									.orElse(LocalDate.now()).format(DateTimeFormatter.ofPattern("MM-dd-yyyy"))))
					.collect(Collectors.joining());
			String discussionUpdate = updateHtml4.replace("%DISCUSIION_ROW%",
					discussionList != null ? discussionList : "");
			fullHtml.add(discussionUpdate);
		});
		return fullHtml;
	}

	private String getAuthorUser(MFPUser mfpUser, String contactAuthor) {
		UserDetailsService uds = new UserDetailsService();
		MFPUser musr = uds.getMFPUser(contactAuthor);
		if (new NullCheck<MFPUser>(musr).with(MFPUser::getTitle).isNotNullOrEmpty()) {
			return String.format("%s, %s", Utils.getNameString(musr.getFirstName(), musr.getLastName()),
					musr.getTitle());
		} else {
			return String.format("%s", Utils.getNameString(musr.getFirstName(), musr.getLastName()));
		}
	}

//
//	private ReviewerEmployeeInfo getReviewerEmployeeInfos(MFPUser mfpUser, String contactReviewer, DealerInfo dInfo) {
//		ReviewerEmployeeInfo revEmp = null;
//		if (contactReviewer != null) {
//			String sqlName = getKPIQueryFilePath(AppConstants.SQL_LIST_REVIEWER_EMPLOYEES);
//			MMAListService<ReviewerEmployeeInfo> service = new MMAListService<ReviewerEmployeeInfo>();
//			List<ReviewerEmployeeInfo> retRows = null;
//			DealerFilter df = new DealerFilter(mfpUser, null, mfpUser.getRgnCd(), null, null, null);
//			try {
//				retRows = service.getListData(sqlName, ReviewerEmployeeInfo.class, df, dInfo.getRgnCd(), dInfo.getZoneCd());
//			} catch (InstantiationException | IllegalAccessException | ParseException e) {
//				log.error("ERROR retrieving list of Employees:", e);
//			}
//			if ((retRows != null) && retRows.size() > 0) {
//				revEmp = getReviewerEmployeeInfo(retRows, contactReviewer);
//			}
//		}
//		return revEmp;
//	}
//
//	private ReviewerEmployeeInfo getReviewerEmployeeInfo(List<ReviewerEmployeeInfo> retRows, String contactReviewer) {
//		for (int i = 0; i < retRows.size(); i++) {
//			ReviewerEmployeeInfo rei = retRows.get(i);
//			if (rei.getPrsnIdCd().equals(contactReviewer)) {
//				return rei;
//			}
//		}
//		return null;
//	}
//
//	private List<DealerEmployeeInfo> getDealerEmployeeInfos(MFPUser mfpUser, String dlrCd,
//			List<ContactReportDealerPersonnel> dPers) {
//		List<DealerEmployeeInfo> dEmpInfos = new ArrayList<DealerEmployeeInfo>();
//		if (dPers != null) {
//			String sqlName = getKPIQueryFilePath(AppConstants.SQL_LIST_DEALER_EMPLOYEES);
//			MMAListService<DealerEmployeeInfo> service = new MMAListService<DealerEmployeeInfo>();
//			List<DealerEmployeeInfo> retRows = null;
//			DealerFilter df = new DealerFilter(mfpUser, dlrCd, null, null, null, null);
//			try {
//				retRows = service.getListData(sqlName, DealerEmployeeInfo.class, df, dlrCd);
//			} catch (InstantiationException | IllegalAccessException | ParseException e) {
//				log.error("ERROR retrieving list of Employees:", e);
//			}
//			if ((retRows != null) && retRows.size() > 0) {
//				for (ContactReportDealerPersonnel dp : dPers) {
//					DealerEmployeeInfo dei = getDealerEmployeeInfo(retRows, dp);
//					if (dei != null) {
//						dEmpInfos.add(dei);
//					}
//				}
//			}
//		}
//		return dEmpInfos;
//	}
//
//	private DealerEmployeeInfo getDealerEmployeeInfo(List<DealerEmployeeInfo> retRows,
//			ContactReportDealerPersonnel dp) {
//		for (int i = 0; i < retRows.size(); i++) {
//			DealerEmployeeInfo dei = retRows.get(i);
//			if (dei.getPrsnIdCd().equals(dp.getPersonnelIdCd())) {
//				return dei;
//			}
//		}
//		return null;
//	}

}
