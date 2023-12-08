<?xml version="1.0" encoding="UTF-8" ?>
<%--
/*******************************************************************************
 * Licensed Materials - Property of IBM
 *
 * OpenPages GRC Platform (PID: 5725-D51)
 *
 * (c) Copyright IBM Corporation 2015 - 2020. All Rights Reserved.
 *  
 * The source code for this program is not published or otherwise
 * divested of its trade secrets, irrespective of what has been
 * deposited with the U.S. Copyright Office.
 *******************************************************************************/
 --%>


<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>

<%@page import="com.ibm.openpages.api.resource.IGRCObject"%>
<%@page import="com.ibm.openpages.api.resource.IResourceFactory"%>
<%@page import="com.ibm.openpages.api.metadata.DataType"%>
<%@page import="com.ibm.openpages.api.resource.IField"%>
<%@page import="com.ibm.openpages.api.metadata.ITypeDefinition"%>
<%@page import="com.ibm.openpages.api.service.IResourceService"%>
<%@page import="com.ibm.openpages.api.service.IConfigurationService"%>
<%@page import="com.ibm.openpages.api.service.IMetaDataService"%>
<%@page import="com.ibm.openpages.api.service.ISecurityService"%>
<%@page import="com.ibm.openpages.api.security.IUser"%>
<%@page import="com.ibm.openpages.api.service.ServiceFactory"%>
<%@page import="com.ibm.openpages.api.service.IServiceFactory"%>
<%@page import="com.ibm.openpages.api.configuration.ISOCurrencyCode"%>
<%@page import="com.ibm.openpages.api.configuration.ICurrency"%>
<%@page import="com.ibm.openpages.api.resource.util.ResourceUtil"%>
<%@page import="com.ibm.openpages.api.Context"%>
<%@page import="com.ibm.openpages.api.OpenPagesException"%>
<%--  this is an OPApps import... --%>
<%@page import="com.openpages.apps.common.util.HttpContext"%>

<%--  aurora import --%>
<%@page import="com.openpages.sdk.OpenpagesSession"%>

<%@page import="org.apache.log4j.Logger"%>
<%@page import="java.util.UUID"%>
<%@page import="java.text.SimpleDateFormat"%>
<%@page import="java.util.Date"%>
<%@page import="java.io.PrintWriter"%>
<%@page import="java.util.List"%>
<%@page import="java.util.ArrayList"%>
<%@page import="java.util.HashSet"%>
<%@page import="java.util.Set"%>
<%@page import="java.util.Iterator"%>
<%@page import="java.util.Arrays"%>
<%@page import="java.util.Map"%>



<%!//Constants, Globals

	private static final String FORM_DATE_FORMAT = "M/d/yyyy";%>
<%!//local methods%>
<%
	System.out.println("==== BusEntityForm Submit jsp ====");
	//init steps
	IServiceFactory serviceFactory = null;
	
	try{
		serviceFactory = ServiceFactory.getServiceFactory(request);
	}catch(OpenPagesException ope){
		Cookie cookie=new Cookie("opTargetUrl","./BusEntityForm.jsp");
		cookie.setMaxAge(-1);
		response.addCookie(cookie);
		response.sendRedirect("./log.on.do");
	}
	
	Date before = new Date();

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
	String reportTitle = "BusEntityForm Create Helper";
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

</head>
<body>
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
			IResourceService resourceService = serviceFactory
					.createResourceService();
			//
			ITypeDefinition beType = metaDataService.getType("SOXBusEntity");

			//get the users current profile

			if (reportUser == null) {
				pw.println("Could not determine current user.");
			} else {

				//get the ResourceFactory to create the BusEntityForm Object
				IResourceFactory newGRCObjectFactory = resourceService
						.getResourceFactory();
				
				pw.println("Populating BusEntityForm fields<br/>");
				out.flush();

				SimpleDateFormat sdf = new SimpleDateFormat(
						FORM_DATE_FORMAT);
				//get all parameters
				Map<String, String[]> parameters = request
						.getParameterMap();
				Set<String> parameterNames = parameters.keySet();

				//could also autoname, 
				IGRCObject newBusEn = newGRCObjectFactory
						.createGRCObject(parameters.get("buEnName")[0] ,beType);

				//the new BusEntity is not yet saved. You must populate the fields first.

				
				for (String paramName : parameterNames) {
					String[] values = parameters.get(paramName);
					String displayValue = Arrays.toString(values);
					
					//from the user selector an extra parameter that can be skipped
					//it contains the user's display name
					if(paramName.endsWith(".displayName")){
						continue;
					}
					
					pw.println(paramName + ": " + displayValue + "...");
					
					try {
						// in case field was left blank or empty
						if (values!=null && values.length > 0) {
							//set the description, special case
							if (paramName.equals("Description")) {

								newBusEn.setDescription(values[0]);

							} else {
								IField fieldToSet = newBusEn
										.getField(paramName);
								Object valueToSet = null;

								if (fieldToSet.getDataType().equals(
										DataType.DATE_TYPE)) {
								    //Date field as date
								    valueToSet = sdf.parse(values[0]);
								} else if (fieldToSet.getDataType().equals(
										DataType.ENUM_TYPE)) {
									//enum is name of enum value
                                    valueToSet = values[0];
								} else if (fieldToSet.getDataType().equals(
										DataType.MULTI_VALUE_ENUM)) {
								    //multi-enum is array of enum values
								    valueToSet = values;
								} else if (fieldToSet.getDataType().equals(
										DataType.STRING_TYPE)) {
									//string field is just the string value
									valueToSet = values[0];
								} else if(fieldToSet.getDataType().equals(
										DataType.CURRENCY_TYPE)){
									Object[] curr = new Object[3];
									ICurrency currency = configService.getCurrency(ISOCurrencyCode.valueOf(values[0]));
									double orgExRate = configService.getExchangeRate(currency, new Date());
									curr[0] = currency;
									curr[1] = parameters.get("localAmount")[0];
									String exRate = parameters.get("exRate")[0];
									if(Double.parseDouble(exRate) != orgExRate){
										curr[2] = exRate;	
									}
									valueToSet = curr;
								}
								//Other data types are possible, see ResourceUtil.setFieldValue javadocs
							    
								if(valueToSet != null){
								    //save the value object to the field using
								    //ResourceUtil
									ResourceUtil.setFieldValue(fieldToSet,
										valueToSet);
								
								}
							}
							pw.println("set<br/>");
						}
					} catch (Exception e) {
						pw.println("not set<br/>");
						pw.println("<!-- " + e.getClass() + ":"
								+ e.getMessage() + "-->");
					}
				    
					
		            
		            
				} // end for loop for parameters
			    
				//once complete with field updates, save the BusEntity to create it
                pw.println("Saving object<br/>");
                out.flush();
                
				IGRCObject savedBE = resourceService.saveResource(newBusEn);
                
				//create a link to created BusEntity's details page
                pw.println("BusEntity created in OpenPages<br/>");
                String detailPageUrl = "view.resource.do?fileId="+savedBE.getId();
                out.println("Created: <a href=\""+detailPageUrl+"\">"+savedBE.getName()+"</a><br/>");
                out.flush();
                
                
                pw.println("<p><a href=\"BusEntityForm.jsp\">Return to BusEntity Form.</a></p>");
			}

		} catch (Exception e) {
			
			//Exception could be provided in case of invalid input or user permission
			//this is only an example
			
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
	
</body>
</html>
<%
	
%>
