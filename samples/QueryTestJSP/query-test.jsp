<?xml version="1.0" encoding="UTF-8" ?>
<%--
/*******************************************************************************
 * Licensed Materials - Property of IBM
 *
 * OpenPages with Watson (PID: 5725-D51)
 *
 * (c) Copyright IBM Corporation 2012 - 2020. All Rights Reserved.
 *  
 * The source code for this program is not published or otherwise
 * divested of its trade secrets, irrespective of what has been
 * deposited with the U.S. Copyright Office.
 *******************************************************************************/
 --%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>

<%@page import="com.ibm.openpages.api.metadata.Id"%>
<%@page import="com.ibm.openpages.api.resource.IIdField"%>
<%@page import="com.ibm.openpages.api.metadata.DataType"%>
<%@page import="com.ibm.openpages.api.metadata.IEnumValue"%>
<%@page import="com.ibm.openpages.api.resource.IMultiEnumField"%>
<%@page import="com.ibm.openpages.api.resource.IFloatField"%>
<%@page import="com.ibm.openpages.api.resource.IReferenceField"%>
<%@page import="com.ibm.openpages.api.resource.IEnumField"%>
<%@page import="com.ibm.openpages.api.resource.IStringField"%>
<%@page import="com.ibm.openpages.api.resource.IDateField"%>
<%@page import="com.ibm.openpages.api.resource.IIntegerField"%>
<%@page import="com.ibm.openpages.api.resource.IBooleanField"%>
<%@page import="com.ibm.openpages.api.configuration.ICurrency"%>
<%@page import="com.ibm.openpages.api.resource.ICurrencyField"%>
<%@page import="com.ibm.openpages.api.resource.IField"%>
<%@page import="com.ibm.openpages.api.query.IResultSetRow"%>
<%@page import="com.ibm.openpages.api.query.ITabularResultSet"%>
<%@page import="com.ibm.openpages.api.query.IQuery"%>
<%@page import="com.ibm.openpages.api.service.IQueryService"%>
<%@page import="com.ibm.openpages.api.service.IConfigurationService"%>
<%@page import="com.ibm.openpages.api.configuration.IReportingPeriod"%>
<%@page import="com.ibm.openpages.api.metadata.ITypeDefinition"%>
<%@page import="com.ibm.openpages.api.service.IMetaDataService"%>
<%@page import="com.ibm.openpages.api.service.ISecurityService"%>
<%@page import="com.ibm.openpages.api.service.IServiceFactory"%>
<%@page import="com.ibm.openpages.api.service.ServiceFactory"%>
<%@page import="com.ibm.openpages.api.ServerType"%>
<%@page import="com.ibm.openpages.api.Context"%>
<%@page import="com.ibm.openpages.api.OpenPagesException"%>

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
<%@page import="java.io.File"%>
<%@page import="java.io.FileOutputStream"%>
<%@page import="java.io.InputStream"%>
<%@page import="java.io.InputStreamReader"%>
<%@page import="java.io.BufferedReader"%>

<%!
	//Constants, Globals* 
	
	//* must be threadsafe
%>
<%!
//test required methods
        
        /**
        * Print the column headings in HTML table
        */
        private void printColHeader(final IResultSetRow row, final PrintWriter pw){
			
			
			//field names
			pw.print("<tr class=\"summarytableheader\">");
		    for (IField field : row) {
		        pw.print("<th class=\"subheading\">"+field.getName()+"</th>");
		    }
		    pw.println("</tr>");
		    //data type
		    pw.print("<tr class=\"summarytableheader\">");
            for (IField field : row) {
                pw.print("<th class=\"subheading\">"+ field.getDataType().name()+"</th>");
            }
            pw.println("</tr>");
		    pw.flush();
	    }

		/**
		* Print the value of the column value into HTML table
		*/
	   private void printFieldValue(final IField field, final PrintWriter pw) {

        System.out.print(field.getName() + ": ");
        if (field.isNull()) {
        	pw.println("NULL");
        } else if (field instanceof IBooleanField) {
            IBooleanField booleanField = (IBooleanField)field;
            Boolean value = booleanField.getValue();
            pw.println(value);
        } else if (field instanceof IIntegerField) {
            IIntegerField integerField = (IIntegerField)field;
            Integer value = integerField.getValue();
            pw.println(value);
        } else if (field instanceof IDateField) {
            IDateField dateField = (IDateField)field;
            Date date = dateField.getValue();
            pw.println(date);
        } else if (field instanceof IStringField ) {
            IStringField stringField = (IStringField)field;
            String value = stringField.getValue();
            pw.println(value);
        } else if (field instanceof IEnumField) {
            IEnumField eField = (IEnumField)field;
            pw.println(eField.getEnumValue().getName());
        } else if (field instanceof IReferenceField) {
            IReferenceField referenceField = (IReferenceField)field;
            pw.println(referenceField.getValue());
        } else if (field instanceof IFloatField) {
            IFloatField floatField = (IFloatField)field;
            Double value = floatField.getValue();
            pw.println(value);
        } else if (field instanceof IMultiEnumField) {
            IMultiEnumField enumField = (IMultiEnumField) field;
            for (IEnumValue enumValue : enumField.getEnumValues()) {
            	pw.println(enumValue.getName()+",");
            }
        } else if (field instanceof ICurrencyField) {
            ICurrencyField currencyField = (ICurrencyField)field;
            Double localAmount = currencyField.getLocalAmount();
            ICurrency localCurrency = currencyField.getLocalCurrency();
            Double baseAmount = currencyField.getBaseAmount();
            ICurrency baseCurrency = currencyField.getBaseCurrency();
            String value = "";
            if (localAmount != null) {
                value = localCurrency.getCurrencyCode() + " " + localAmount;
            }
            if (baseAmount != null) {
                value += " (" + baseCurrency.getCurrencyCode() + " " + baseAmount + ")";
            }
            pw.println(value);
        } else if (DataType.LARGE_STRING_TYPE.equals(field.getDataType())){
        	 IStringField stringField = (IStringField)field;
             String value = stringField.getValue();
             pw.println(value);
        }else if (field instanceof IIdField){
        	IIdField idField = (IIdField)field;
            Id value = idField.getValue();
            pw.println(value.toString());
        }
        else{
            pw.println("Unknown Field type: "+field.toString());
        }
    }
%>
<%

    System.out.println("==== ad-hoc query test jsp ====");
    //init steps
	IServiceFactory serviceFactory = null;
	
	try{
		serviceFactory = ServiceFactory.getServiceFactory(request);
	}catch(OpenPagesException ope){
		Cookie cookie=new Cookie("opTargetUrl","./query-test.jsp");
		cookie.setMaxAge(-1);
		response.addCookie(cookie);
		response.sendRedirect("./log.on.do");
	}
	
	
	
    ISecurityService securityService = serviceFactory.createSecurityService();			    
  
    //ReportTagClient rtc = new ReportTagClient(request);
    Date before = new Date();
    //this is the query statement that will be executed.
    String statement = request.getParameter("qstatement");
    String caseParameter = request.getParameter("caseS");
    boolean caseInsensitive = false;
    if(caseParameter!=null){
    	caseInsensitive = Boolean.parseBoolean(caseParameter);
    }
    
    SimpleDateFormat reportDateFormat = new SimpleDateFormat(
            "MMM dd, yyyy hh:mm aa");
    String reportDate = reportDateFormat.format(new Date());
    //TODO parameterize these
    //String reportUser = rtc.getReportUser();
    String reportUser = securityService.getCurrentUser().getName();
    
   	//displayName -- generated according to com.display.name.format
    String displayName = securityService.getCurrentUser().getDisplayName();
    String reportTitle = "Query Service test";
%>   
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">

<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en-US" lang="en-US">
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
	<meta http-equiv="Content-Script-Type" content="text/javascript" />
	<meta http-equiv="X-UA-Compatible" content="IE=9"/>
	<title><%=reportTitle%></title>
	<link rel="Stylesheet" href="op_style/default/open_pages.css"/>
	<link rel="Stylesheet" href="dojo_1.8.4/dojo/resources/dojo.css"/>
</head>
<body class="oneui" style="overflow:scroll">
  
	<!-- IBM oneui header -->
	<div class="idxHeaderContainer" lang="en-US" role="banner" style="position: static; top: 0px; left: 0px; min-width: 1080px;">
		<div style="visibility: hidden; display: none;"> </div>
		<div class="idxHeaderPrimaryThin idxHeaderSecondaryGray idxHeaderSecondaryGrayDoubleRow idxHeaderSecondaryDoubleRow idxHeaderWidthLiquid">
			<div class="idxHeaderPrimary">	
				<div class="idxHeaderPrimaryInner">
					<ul>
						<li><span><div class="idxHeaderPrimaryTitle">IBM OpenPages with Watson</div></span></li>
						<li><a href="home.do"><div class="idxHeaderPrimarySubtitle">Home</div></a></li>
					</ul>
				</div>
			</div>	
			<div class="idxHeaderBlueLip"></div>
			<div class="oneuiContentContainer" style="height: 72px;">
				<div class="idxHeaderSecondary">
					<div id="pageInfo" class="idxHeaderSecondaryInner">
						<span class="idxHeaderSecondaryTitleContainer">
							<span class="idxHeaderSecondaryTitle"><%=reportTitle%></span><br/>
							<span class="idxHeaderSecondarySubtitle">Run by: <%=reportUser%> ( <%=displayName%> )</span><br/>
							<span class="idxHeaderSecondarySubtitle">Run on: <%=reportDate%></span>
						</span><br/><br/>
					</div>
				</div>
			</div>
		</div>
	</div>
	<!-- Main content -->
	<div id="result" style="width:90%; margin:0px auto 0px auto;clearfix:left;">
		<div id="inputSection" >
			<form action="query-test.jsp">
			   <p>Enter query-service statement:</p>
			   <textarea rows="6" cols="80" name=qstatement><%=(statement!=null ? statement : "")%></textarea>
			   <p>Notes: case-sensitive,uses system names for object types and fields. For field names use [Field Group:Field Name].<br/>
			   Example: <code>SELECT [SOXIssue].[Resource ID],[SOXIssue].[Name] FROM [SOXIssue] WHERE [SOXIssue].[Name] LIKE '%ssue 5%'</code></p>
				Case Insensitive?<br/>
				<input type="radio" name="caseS" value="true" <% if(caseInsensitive){%>checked="checked"<%}%>/>True<br/>
				<input type="radio" name="caseS" value="false" <% if(!caseInsensitive){%>checked="checked"<%}%>/>False<br/>
				<input class="toolbutton" value="Execute" type="submit"/>
			</form>
		</div>
	<hr/>
	<%
	    
		/* *************** Put your Code here - Start ********** */
		
		PrintWriter pw = new PrintWriter(out);
	    
        // if the statement was passed to this JSP execute the query and display results
		if(statement != null){
			out.println("<p>Running Test</p>");
		    out.flush();
			try{
				out.println("Factories<br/>");
				out.flush();
			    IQueryService queryService = serviceFactory.createQueryService();
			    
			    out.println("Executing Query<br/>");
				out.flush();
				IQuery query = queryService.buildQuery(statement);
				//query.setPageSize(200);
				query.setCaseInsensitive(caseInsensitive);
	            ITabularResultSet resultset = query.fetchRows(0);
	            //using page iterator:
	            //ITabularResultSet resultset = query.fetchRows(0);
				//for (IPage page : resultset.getPages()) {
				//    for (IResultSetRow row : page) {
				//        ...
				//    }   
				//}
	            int rowCount = 0;
				%>
				
				<div id="queryResultTable" class="tablegroup">
					<table class="summarytable content">
						<tbody>
				<%
	            for (IResultSetRow row : resultset) {
	            	if(rowCount == 0){
	            		printColHeader(row,pw);
	            	}
	                System.out.println("----------------------");
	                pw.println("<tr class=\"objectList even\">");
	                for (IField field : row) {
	                	pw.print("<td class=\"item\" style=\"padding-left:10px;\">");
	                    printFieldValue(field,pw);
	                    pw.print("</td>");
	                }
	                rowCount++;
	                pw.println("</tr>");
	                pw.flush();
	            }
				%>
						</tbody>
					</table>
				</div>
				<%
	            pw.println("rowCount = " + rowCount+"<br/>");
			    
	            out.println("Completed<br/>");
				out.println("Successful</p>");
				out.flush();
				
			}catch(Exception e){
			    //compute diff of before / after time
			    out.println("Completed<br/>");
			    out.println("Errors:</p>");
			    out.println("<pre>");
			    e.printStackTrace(pw);
			    out.println("</pre>");
			    Logger.getLogger(this.getClass()).error("riskapi test jsp exception", e);
			}
		}
		
		out.flush();
	    
		
		
	    /* *************** Put your Code here - End ********** */
	    Date after = new Date();
	    long durationMillis = after.getTime() - before.getTime();
	    double durationSeconds = durationMillis / 1000.0;
	    out.println("<p>Duration : "+durationSeconds+" (sec)</p>");
	    out.flush();
	%>
	
	</div>
</body>
</html>