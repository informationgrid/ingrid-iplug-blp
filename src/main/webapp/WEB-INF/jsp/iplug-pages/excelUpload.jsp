<%--
  **************************************************-
  InGrid-iPlug DSC
  ==================================================
  Copyright (C) 2014 - 2019 wemove digital solutions GmbH
  ==================================================
  Licensed under the EUPL, Version 1.1 or – as soon they will be
  approved by the European Commission - subsequent versions of the
  EUPL (the "Licence");
  
  You may not use this work except in compliance with the Licence.
  You may obtain a copy of the Licence at:
  
  http://ec.europa.eu/idabc/eupl5
  
  Unless required by applicable law or agreed to in writing, software
  distributed under the Licence is distributed on an "AS IS" basis,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the Licence for the specific language governing permissions and
  limitations under the Licence.
  **************************************************#
  --%>
<%@ include file="/WEB-INF/jsp/base/include.jsp"%><%@ taglib
	uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ page contentType="text/html; charset=UTF-8"%>
<%@page import="de.ingrid.admin.security.IngridPrincipal"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" lang="de">
<head>
<title><fmt:message key="DatabaseConfig.main.title" /></title>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<meta name="description" content="" />
<meta name="keywords" content="" />
<meta name="author" content="wemove digital solutions" />
<meta name="copyright" content="wemove digital solutions GmbH" />
<link rel="StyleSheet" href="../css/base/portal_u.css" type="text/css"
	media="all" />
<script type="text/javascript" src="../js/base/jquery-1.8.0.min.js"></script>

</head>
<body>
	<div id="header">
		<img src="../images/base/logo.gif" width="168" height="60"
			alt="Portal" />
		<h1>
			<fmt:message key="DatabaseConfig.main.configuration" />
		</h1>
		<%
		    java.security.Principal principal = request.getUserPrincipal();
		    if (principal != null && !(principal instanceof IngridPrincipal.SuperAdmin)) {
		%>
		<div id="language">
			<a href="../base/auth/logout.html"><fmt:message
					key="DatabaseConfig.main.logout" /></a>
		</div>
		<%
		    }
		%>
	</div>
	<div id="help">
		<a href="#">[?]</a>
	</div>

	<c:set var="active" value="dbParams" scope="request" />
	<c:import url="../base/subNavi.jsp"></c:import>

	<div id="contentBox" class="contentMiddle">
		<h1 id="head">Datei Upload</h1>
		<div class="controls">
			<a href="../base/extras.html">Zur&uuml;ck</a> <a
				href="../base/welcome.html">Abbrechen</a> <a
				href="../base/save.html">Weiter</a>
		</div>
		<div class="controls cBottom">
			<a href="../base/extras.html">Zur&uuml;ck</a> <a
				href="../base/welcome.html">Abbrechen</a> <a href="#"
				onclick="document.getElementById('dbConfig').submit();">Weiter</a>
		</div>
		<div id="content">
			<h2>Wählen Sie eine Excel Datei aus, die Sie indizieren möchten:</h2>
			<div class="hint" onclick="$('#filterComment').toggle()">
				<span class="ui-icon ui-icon-arrow-1-e"></span>Hinweise
			</div>
			<div id="filterComment" class="comment" style="display: none;">
				<ul>
					<li>Alle bargestellt.</li>
				</ul>
			</div>

			<fieldset>
				<legend>Dateiupload</legend>
				<div id="content">
					<row> <form:form action="../iplug-pages/excelUpload.html"
						enctype="multipart/form-data" modelAttribute="uploadBean">

						<row> <label> Excel Datei: </label> <field>
						<div class="input full">
							<input type="file" name="file" />
							<form:errors path="file" cssClass="error" element="div" />
						</div>
						</field> <desc></desc> </row>
					</form:form> </row>
					<row>
					<div class="controls cBottom">
						<a href="#"
							onclick="document.getElementById('uploadBean').submit();">Upload</a>
					</div>
					</row>
			</fieldset>

			<fieldset id="statusContainer">
				<legend>Status</legend>
				<div id="importInfo" class="space"></div>
				<div id="allInfo">
					<div id="statusContent">
						<p>${resultLog}</p>
					</div>
				</div>
			</fieldset>

		</div>

		<div id="footer" style="height: 100px; width: 90%"></div>
</body>
</html>

