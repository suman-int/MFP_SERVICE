<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="ISO-8859-1">
<title>Contact Reports</title>
<script type="text/javascript">

	var callbackOnReady = function() {
		var hst = location.host;
		var crUrl = location.protocol + "//" + hst + "/m220/mfpwebui";
		var pth = location.pathname;
		var txt = document.getElementById("txtUrl");
		txt.value = crUrl;
	};

	if (document.readyState === "complete"
			|| (document.readyState !== "loading" && !document.documentElement.doScroll)) {
		callbackOnReady();
	} else {
		document.addEventListener("DOMContentLoaded", callbackOnReady);
	}
	//
	function doRedirect() {
		var txt = document.getElementById("txtUrl");
		var tgt = txt.value;
		if (tgt && tgt.trim().length > 0) {
			location.replace(tgt);
		}
	}
	//
	function deleteAllCookies() {
		var cookies = document.cookie.split(";");

		for (var i = 0; i < cookies.length; i++) {
			var cookie = cookies[i];
			var eqPos = cookie.indexOf("=");
			var name = eqPos > -1 ? cookie.substr(0, eqPos) : cookie;
			document.cookie = name + "=;expires=Thu, 01 Jan 1970 00:00:00 GMT";
		}
	}
</script>
</head>
<body>
	<h1>Welcome to Contact Reports</h1>
	<div id="mainDiv">
		<input type="text" style="min-width:400px" id="txtUrl" /> 
		<br> 
		<input type="button" id="redirect" value="Click to proceed" onClick="doRedirect()" />
		<br>
		<input type="button" id="clearCookies" value="Clear all cookies" onClick="deleteAllCookies()" />
	</div>
</body>
</html>