<%--
  **************************************************-
  InGrid-iPlug DSC
  ==================================================
  Copyright (C) 2014 - 2026 wemove digital solutions GmbH
  ==================================================
  Licensed under the EUPL, Version 1.2 or – as soon they will be
  approved by the European Commission - subsequent versions of the
  EUPL (the "Licence");
  
  You may not use this work except in compliance with the Licence.
  You may obtain a copy of the Licence at:
  
  https://joinup.ec.europa.eu/software/page/eupl
  
  Unless required by applicable law or agreed to in writing, software
  distributed under the Licence is distributed on an "AS IS" basis,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the Licence for the specific language governing permissions and
  limitations under the Licence.
  **************************************************#
  --%>
<%@ include file="/WEB-INF/jsp/base/include.jsp" %>
<%@ taglib
        uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" lang="de">
<head>
    <title><fmt:message key="DatabaseConfig.main.title"/></title>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
    <meta name="description" content=""/>
    <meta name="keywords" content=""/>
    <meta name="author" content="wemove digital solutions"/>
    <meta name="copyright" content="wemove digital solutions GmbH"/>
    <link rel="StyleSheet" href="../css/base/portal_u.css" type="text/css"
          media="all"/>
    <script type="text/javascript" src="../js/base/jquery-1.8.0.min.js"></script>
    <script type="text/javascript">
        $(document).ready(function () {
            checkState();
        });

        function checkState() {
            $.ajax("../rest/uploadStatus", {
                type: "GET",
                cache: false,
                contentType: 'application/json',
                success: function (data) {
                    if (data == "") {
                        $("#importInfo").html("Es läuft zur Zeit kein Import.");
                        setTimeout(checkState, 60000);
                        return;
                    } else if (data.some(function (item) {
                        return item.key === "FINISHED" || item.key === "ERROR" || item.key === "ABORT"
                    })) {
                        $("#importInfo").html(data);
                        // repeat execution every 60s
                        setTimeout(checkState, 60000);
                        return;
                    }
                    setLog(data);
                    //$("#importInfo").html( data );


                    // repeat execution every 3s until finished
                    setTimeout(checkState, 3000);
                },
                error: function (jqXHR, text, error) {
                    // if it's not a real error, but just saying, that no process is running
                    $("#importInfo").html("Es trat ein Fehler beim Laden des Logs auf. ");
                    console.error(error, jqXHR);
                }
            });
        }

        var formatTime = function (ts) {
            var date = new Date(ts);
            var d = date.getDate();
            var m = date.getMonth() + 1;
            var y = date.getFullYear();
            var time = date.toTimeString().substring(0, 8);
            return '' + y + '-' + (m <= 9 ? '0' + m : m) + '-' + (d <= 9 ? '0' + d : d) + ' ' + time;
        };

        function setLog(data) {

            // fill div with data from content
            var content = "";
            for (var i = 0; i < data.length; i++) {
                var row = data[i];
                if (row.value) {
                    content += "<div class='" + row.classification.toLowerCase() + "'>" + formatTime(row.time) + " - [" + row.classification + "] " + row.value + "</div>";
                }
            }

            $("#importInfo").html(content);
        }

        checkState();
    </script>
</head>
<body>
<div id="header">
    <img src="../images/base/logo.gif" width="168" height="60"
         alt="Portal"/>
    <h1>
        <fmt:message key="DatabaseConfig.main.configuration"/>
    </h1>
    <security:authorize access="isAuthenticated()">
        <div id="language">
            <a href="../base/auth/logout.html"><fmt:message
                    key="DatabaseConfig.main.logout"/></a>
        </div>
    </security:authorize>
</div>
<div id="help">
    <a href="#">[?]</a>
</div>

<c:set var="active" value="excelUpload" scope="request"/>
<c:import url="../base/subNavi.jsp"></c:import>

<div id="contentBox" class="contentMiddle">
    <h1 id="head">Datei Upload</h1>
    <div class="controls">
        <a href="../base/provider.html">Zur&uuml;ck</a> <a
            href="../base/welcome.html">Abbrechen</a> <a
            href="../base/save.html">Weiter</a>
    </div>
    <div class="controls cBottom">
        <a href="../base/provider.html">Zur&uuml;ck</a> <a
            href="../base/welcome.html">Abbrechen</a> <a
            href="../base/save.html">Weiter</a>
    </div>
    <div id="content">
        <h2>Wählen Sie eine Excel Datei aus, die Sie indizieren möchten:</h2>
        <div class="hint" onclick="$('#filterComment').toggle()">
            <a href="#">&#8594; Hinweise</a>
        </div>
        <div id="filterComment" class="comment" style="display: none;">
            <ul>
                <li>Eine bereits bestehende Excel-Datei wird bei einem Upload gelöscht.</li>
                <li>Sollte es Fehler während des Imports geben, werden diese unter "Status" angezeigt, sodass das
                    Excelfile entsprechend korrigiert werden kann.
                </li>
                <li>Nach dem erfolgreichen Upload, muss unter "Indexieren" neu indiziert werden.</li>
            </ul>
        </div>

        <fieldset>
            <legend>Dateiupload</legend>
            <div id="content">
                <row>
                    <form:form action="../iplug-pages/excelUpload.html"
                               enctype="multipart/form-data" modelAttribute="uploadBean">

                        <row>
                            <label> Excel Datei: </label>
                            <field>
                                <div class="full">
                                    <div class="input" style="width: 80%; float:left;">
                                        <input type="file" name="file" style="width: 100%"/>
                                        <form:errors path="file" cssClass="error" element="div"/>
                                    </div>
                                    <div style="width: 19%;float: right;">
                                        <button href="#" onclick="document.getElementById('uploadBean').submit();"
                                                style="width: 100%;">Upload
                                        </button>
                                    </div>
                                </div>
                            </field>
                            <desc></desc>
                        </row>
                    </form:form>
                </row>
            </div>
        </fieldset>

        <fieldset id="statusContainer">
            <legend>Status</legend>
            <div id="importInfo" class="space"></div>
        </fieldset>

    </div>

    <div id="footer" style="height: 100px; width: 90%"></div>
</div>
</body>
</html>
