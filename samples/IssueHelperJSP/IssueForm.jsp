<?xml version="1.0" encoding="UTF-8" ?>
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

<%@page import="com.ibm.openpages.api.metadata.IEnumValue"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>

<%@page import="com.ibm.openpages.api.metadata.Id"%>
<%@page import="com.ibm.openpages.api.metadata.DataType"%>
<%@page import="com.ibm.openpages.api.configuration.DisplayTypes"%>
<%@page import="com.ibm.openpages.api.configuration.IDisplayType"%>
<%@page import="com.ibm.openpages.api.security.IUser"%>
<%@page import="com.ibm.openpages.api.configuration.IProfileFieldDefinition"%>
<%@page import="com.ibm.openpages.api.configuration.IProfile"%>
<%@page import="com.ibm.openpages.api.service.ISecurityService"%>
<%@page import="com.ibm.openpages.api.service.IConfigurationService"%>
<%@page import="com.ibm.openpages.api.metadata.ITypeDefinition"%>
<%@page import="com.ibm.openpages.api.service.IMetaDataService"%>
<%@page import="com.ibm.openpages.api.service.IServiceFactory"%>
<%@page import="com.ibm.openpages.api.service.ServiceFactory"%>
<%@page import="com.ibm.openpages.api.ServerType"%>
<%@page import="com.ibm.openpages.api.Context"%>
<%@page import="com.ibm.openpages.api.OpenPagesException"%>
<%--  this is an OPApps import... --%>
<%@page import="com.openpages.apps.common.util.HttpContext"%>

<%--  aurora import --%>
<%@page import="com.openpages.sdk.OpenpagesSession"%>

<%@page import="org.apache.log4j.Logger"%>
<%@page import="java.text.SimpleDateFormat"%>
<%@page import="java.util.Date"%>
<%@page import="java.io.PrintWriter"%>
<%@page import="java.util.List"%>
<%@page import="java.util.ArrayList"%>
<%@page import="java.util.HashSet"%>
<%@page import="java.util.Set"%>
<%@page import="java.util.Iterator"%>


<%!//Constants, Globals%>
<%!//local methods

	/**
	 * Creates the HTML for the User Selector widget emulating
	 * the stype in the OpenPages UI with the traditional user selector and
	 * search options.
	 *
	 * This sample does not contain ALL application business logic for configuration of the
	 * selector, this is an exercise left to the developer.
	 */
	private String getUserSelectorHTML(IProfileFieldDefinition userField) {
		//must be user name selector field
		String defaultText = (String) userField.getDefaultValue();
		if (defaultText == null)
			defaultText = "";

		String fieldName = userField.getName();
		Id profileTypeId = userField.getProfileTypeDefinitionId();
		Id profileFieldId = userField.getProfileFieldId();
		int order = userField.getObjectOrder();

		StringBuffer sb = new StringBuffer();

		//hidden form input stores value to be used to save the field value search.user.popup.do
		sb.append("<input id=\"" + fieldName + "\" name=\"" + fieldName
				+ "\" type=\"hidden\">" + defaultText + "</input>");
		//.displayname text box gets filled by the localized display name for the user that is selected
		sb.append("<input type=\"text\" size=\"30\" readonly=\"readonly\" class=\"shortText\" onclick=\"openUserSelector(document.getElementById('"
				+ fieldName
				+ "'), 'select.user.popup.do', '"
				+ profileTypeId
				+ "', '"
				+ profileFieldId
				+ "'); return false;\" value=\"\" id=\""
				+ fieldName
				+ "2\" name=\""
				+ fieldName
				+ ".displayName\" maxlength=\"4000\" />");
		//user selector popup link
		sb.append("<a href=\"#\" onclick=\"openUserSelector(document.getElementById('"
				+ fieldName
				+ "'), 'select.user.popup.do', '"
				+ profileTypeId
				+ "', '"
				+ profileFieldId
				+ "'); return false;\" name=\"anchor"
				+ order
				+ "\" title=\"Select User in a new window\" id=\"anchor"
				+ order + "\">");
		sb.append("<img border=\"0\" src=\"images/icon-user.gif\" alt=\"Select a user.\"></a>");
		//user search popup link
		sb.append("<a href=\"#\" onclick=\"openUserSelector(document.getElementById('"
				+ fieldName
				+ "'), 'search.user.popup.do', '"
				+ profileTypeId
				+ "', '"
				+ profileFieldId
				+ "'); return false;\" name=\"anchorsearch"
				+ order
				+ "\" title=\"Select Group in a new window\" id=\"anchorsearch"
				+ order + "\">");
		sb.append("<img border=\"0\" src=\"images/icon-search.gif\" alt=\"Search a user.\">");
		sb.append("</a>");

		return sb.toString();
	}%>
<%
	System.out.println("==== Issue Helper jsp ====");
	//init steps
	IServiceFactory serviceFactory = null;
	
	try{
		serviceFactory = ServiceFactory.getServiceFactory(request);
	}catch(OpenPagesException ope){
		Cookie cookie=new Cookie("opTargetUrl","./IssueForm.jsp");
		cookie.setMaxAge(-1);
		response.addCookie(cookie);
		response.sendRedirect("./log.on.do");
	}
			
			
	//ReportTagClient rtc = new ReportTagClient(request);
	Date before = new Date();
	//this is the query statement that will be executed.

	SimpleDateFormat reportDateFormat = new SimpleDateFormat(
			"MMM dd, yyyy hh:mm aa");
	String reportDate = reportDateFormat.format(new Date());

	//the current report user
	String reportUserName = "";
	IUser reportUser = null;
	try {
		ISecurityService secService = serviceFactory
				.createSecurityService();
		reportUser = secService.getCurrentUser();
		reportUserName = reportUser.getName();
	} catch (Exception e) {
		//reportUserName = opSession.getInfo().getUserName();
	}
	String reportTitle = "Issue Create Helper";
%>   
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">



<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en-US" lang="en-US">
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
	<meta http-equiv="Content-Script-Type" content="text/javascript" />
	<title><%=reportTitle%></title>
	<link rel="StyleSheet" href="styles/open_pages.css" type="text/css" />
	<!-- Custom styles -->
	<style type="text/css">
		.fieldRow{
		padding-top:10px;
		margin:0 auto;
		clear:both;
		}
		.fieldRow_item{
		float:left;
		display:block;
		margin:0 6px;
		}
		.leftform {
		    float: left;
		}
    </style>

    <style type="text/css">
		@import "toolkits/dojo/resources/dojo.css";
		@import "toolkits/dijit/themes/tundra/tundra.css";
		@import "toolkits/dojox/widget/Calendar/Calendar.css";
    </style>
    
    <!-- Required scripts -->
    <script type="text/javascript" src="toolkits/dojo/dojo.js" djConfig="parseOnLoad: true" ></script>
    <script type="text/javascript" src="include/common.js" ></script>
    <script type="text/javascript" src="include/userSelectorCallback.js" ></script>
	<script type="text/javascript">
	    dojo.require("dojox.widget.Calendar");
    </script>
</head>
<body class="tundra">
	<div class="pageColor" align="center">
	<span class="reportTitle"><%=reportTitle%></span><br/>
	<span class="reportSubHead">Run by: <%=reportUserName%></span><br/>
	<span class="reportDate">Reporting Period: <%=""%><br/>
	<%=reportDate%></span>
	</div>
	
<%
		// For simplicity the body of this JSP contains JSP scriptlets with Java code performing
		// business logic for this helper. AS a best practice, externalize the business logic in Java classes.
		PrintWriter pw = new PrintWriter(out);
		try {
			IMetaDataService metaDataService = serviceFactory
					.createMetaDataService();
			IConfigurationService configService = serviceFactory
					.createConfigurationService();

			//
			ITypeDefinition issueType = metaDataService.getType("SOXIssue");

			//get the users current profile

			if (reportUser == null) {
				pw.println("Could not determine current user.");
			} else {

				IProfile userProfile = configService.getPreferredProfile();
				String currentProfileName = userProfile.getName();
				pw.println("Current Profile is: " + currentProfileName
						+ "<br/>");

				//get the Issue fields and display them in a form
				List<IProfileFieldDefinition> issueFields = configService
						.getProfileFields(currentProfileName,
								issueType.getName(), "Detail");
	%>	
	<div id="mainform">
	<form id="issueForm" method="post" action="IssueSubmit.jsp">
	   <p>Enter new Issues:</p>
			<%
				int i = 0;
						//fields are in display order
						for (IProfileFieldDefinition profileField : issueFields) {
							i++;
							//this div contains the form "row" where first column is the field label, second is the widget to enter data
							pw.println("<div class=\"fieldRow\">");

							//put label
							pw.println("<div class=\"fieldRow_item\" width=\"60px\">"
									+ profileField.getLocalizedLabel() + "</div>");
							//for each field type display correct widget

							IDisplayType fieldType = profileField.getDisplayType();
							String fieldDisplayType = fieldType.getName();

							//put form field widget
							pw.println("<div class=\"fieldRow_item\">");
							if (profileField.isReadOnly()) {
								//blank space for read only field on create
								//in update case you may have logic to display current value
								pw.println("<p></p>");

								//Date picker
							} else if (DisplayTypes.DATE.equals(fieldDisplayType)) {

								//to simplify a little used default Dojo calendar widget rather than OP specific popup

								//store the actual date value in an input text box
								pw.println("<input type=\"text\" size=\"30\" class=\"shortText\" value=\"\" id=\""
										+ profileField.getName()
										+ "\" name=\""
										+ profileField.getName()
										+ "\"></input> (M/d/yyyy)<br/>");
								//uses dojox.widget.Calendar
								//Note if you use this calendar you must not include report.css! report.css breaks the style of 
								//Calendar due to a large margin for nested div elements
								pw.println("<div id=\"cal" + i
										+ "\" dojoType=\"dojox.widget.Calendar\">");
								//this script updates the text input on value selection in the Calendar
								pw.println("<script type=\"dojo/connect\" event=\"onValueSelected\" args=\"date\">dojo.byId('"
										+ profileField.getName()
										+ "').value = (date.getMonth()+1) + \"/\" + date.getDate() + \"/\" + date.getFullYear();</script></div>");

								//enum value drop down
							} else if (DisplayTypes.ENUMERATION
									.equals(fieldDisplayType)
									&& DataType.ENUM_TYPE.equals(profileField
											.getDataType())) {
								pw.println("<select id=\"" + profileField.getName()
										+ "\" name=\"" + profileField.getName()
										+ "\">");

								List<IEnumValue> enumVals = profileField
										.getEnumValues();
								for (IEnumValue enumVal : enumVals) {
									pw.println("<option value=\""
											+ enumVal.getName() + "\">"
											+ enumVal.getLocalizedLabel()
											+ "</option>");
								}
								pw.println("</select>");

								//enum value multiselect - same as drop down but data type is different
							} else if (DisplayTypes.ENUMERATION
									.equals(fieldDisplayType)
									&& DataType.MULTI_VALUE_ENUM
											.equals(profileField.getDataType())) {
								pw.println("<select id=\"" + profileField.getName()
										+ "\" name=\"" + profileField.getName()
										+ "\" multiple>");

								List<IEnumValue> enumVals = profileField
										.getEnumValues();
								for (IEnumValue enumVal : enumVals) {
									pw.println("<option value=\""
											+ enumVal.getName() + "\">"
											+ enumVal.getLocalizedLabel()
											+ "</option>");
								}
								pw.println("</select>");
							} else if (DisplayTypes.USER_SELECTOR
									.equals(fieldDisplayType)) {
								//this is a more complex widget using OP functions to mimick product UI user selector
								//see above method
								String userSelectorHTML = getUserSelectorHTML(profileField);
								pw.println(userSelectorHTML);
								
								// multi-line text area
							} else if (DisplayTypes.TEXT_AREA
									.equals(fieldDisplayType)) {
								String defaultText = (String) profileField
										.getDefaultValue();
								if (defaultText == null)
									defaultText = "";
								pw.println("<textarea id=\""
										+ profileField.getName() + "\" name=\""
										+ profileField.getName()
										+ "\" rows=\"5\" cols=\"60\">"
										+ defaultText + "</textarea>");
								
								// single-line text box
							} else if (DisplayTypes.TEXT_BOX
									.equals(fieldDisplayType)) {
								String defaultText = (String) profileField
										.getDefaultValue();
								if (defaultText == null)
									defaultText = "";
								pw.println("<input id=\"" + profileField.getName()
										+ "\" name=\"" + profileField.getName()
										+ "\" type=\"text\">" + defaultText
										+ "</input>");
							}
							pw.println("</div>"); //end form field
							pw.println("</div>"); //end form row
						}
			%>
			<br />
	   <input type="submit"/>
	</form>
	<%
		}

		} catch (Exception e) {
			//compute diff of before / after time
			out.println("Completed<br/>");
			out.println("Errors:</p>");
			out.println("<pre>");
			e.printStackTrace(pw);
			out.println("</pre>");
			Logger.getLogger(this.getClass()).error(
					"riskapi test jsp exception", e);
		}

		out.flush();

		/* *************** Put your Code here - End ********** */
		Date after = new Date();
		long durationMillis = after.getTime() - before.getTime();
		double durationSeconds = durationMillis / 1000.0;
		out.println("<p>Duration : " + durationSeconds + " (sec)</p>");
		out.flush();
	%>
	
	</div>
</body>
</html>
<%
	
%>
