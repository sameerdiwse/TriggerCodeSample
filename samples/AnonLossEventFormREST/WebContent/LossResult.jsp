<!DOCTYPE HTML>
<%--
/*******************************************************************************
 * Licensed Materials - Property of IBM
 *
 * OpenPages GRC Platform (PID: 5725-D51)
 *
 * (c) Copyright IBM Corporation 2013 - 2020. All Rights Reserved.
 *  
 * The source code for this program is not published or otherwise
 * divested of its trade secrets, irrespective of what has been
 * deposited with the U.S. Copyright Office.
 *******************************************************************************/
 --%>

<%@page language="java"
	contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<%@page import="java.util.Set"%>
<%@page import="java.util.Map"%>

<html>
<head>
<title>AnonymousLossForm</title>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<style type="text/css">
.answers{
padding-top:10px;
margin:0 auto;
clear:both;
}
.answers div{
float:left;
display:block;
margin:0 6px;
}
.leftform {
    float: left;
}
</style>
</head>
<body>
<img src="images/companylogo.png" height="98px" width="157px"/>Corporate Intranet
<hr/>
<h1>Anonymous Loss Entry</h1>
<p>Instructions and legal disclaimer...</p>
<% if(request.getAttribute("error")!=null){ %>
<div id="errorpanel"><%=request.getAttribute("error") %></div>
<% } %>

<h1>Success</h1>
<div><a href="lossform">Submit Another</a> | <a href="http://www.ibm.com">Home</a></div>

</body>
</html>