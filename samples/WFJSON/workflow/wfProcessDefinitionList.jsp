<!DOCTYPE HTML>

<!--=============================================================================
    Licensed Materials - Property of IBM
    
   
    5725-D51, 5725-D52, 5725-D53, 5725-D54
   
     © Copyright IBM Corporation 2018 - 2020. All Rights Reserved.
     
    US Government Users Restricted Rights- Use, duplication or disclosure restricted 
    by GSA ADP Schedule Contract with IBM Corp.
==============================================================================-->

<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>

<%@page import="java.util.Collections"%>
<%@page import="com.ibm.openpages.api.BatchIterator"%>
<%@page import="com.ibm.openpages.api.service.IWorkflowService"%>
<%@page import="com.ibm.openpages.api.resource.IResource"%>
<%@page import="com.ibm.openpages.api.service.IServiceFactory"%>
<%@page import="com.ibm.openpages.api.service.ServiceFactory"%>
<%@page import="com.ibm.openpages.api.Context"%>
<%@page import="com.ibm.openpages.api.metadata.Id"%>
<%@page import="com.ibm.openpages.api.service.IResourceService"%>
<%@page import="com.ibm.openpages.api.OpenPagesException"%>
<%@page import="com.ibm.openpages.api.workflow.*"%>
<%@page import="com.ibm.openpages.api.service.ejb.workflow.impl.*"%>
<%@page import="com.ibm.openpages.api.service.local.workflow.*"%>

<%@page import="com.openpages.apps.common.util.HttpContext"%>
<%@page import="com.openpages.sdk.OpenpagesSession"%>

<%@page import="java.io.PrintWriter"%>
<%@page import="java.util.ArrayList"%>
<%@page import="java.util.List"%>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<link rel="Stylesheet" href="../op_style/default/open_pages.css" />
<title>Workflow Process Definition List</title>

</head>
<body class="oneui">

	<div class="heading" align="center" style="font-size: 1.2em; padding-bottom: 10px;">Workflow Process Definitions</div>

	<% 
			
            PrintWriter pw = new PrintWriter(out);
			
			try {	            
	            String processDefinition = request.getParameter("processDefinition");
	            String deleteId = request.getParameter("deleteId");

				// init steps
				IServiceFactory serviceFactory = null;
				try{
					serviceFactory = ServiceFactory.getServiceFactory(request);
				}catch(OpenPagesException ope){
					Cookie cookie=new Cookie("opTargetUrl","./wfProcessDefinitionList.jsp");
					cookie.setMaxAge(-1);
					response.addCookie(cookie);
					response.sendRedirect("../log.on.do");
				}
				
	        IWorkflowService workflowService = serviceFactory.createWorkflowService();
			
			try {
				if (processDefinition != null && !processDefinition.isEmpty()) {
				    IWFProcessDefinition procDef = WFUtil.parseWFProcessDefinition(processDefinition);
	                workflowService.createProcessDefinition(procDef);   
					pw.println("<br>Process Definition '" + procDef.getName() + "' created successfully.<br>");
				}
	        } catch (Exception e) {
	            pw.println("<br>Error occured calling createProcessDefinition<br>");
	            pw.println(e.getMessage());
				e.printStackTrace();
	        }
            
            try {
                if (deleteId != null && !deleteId.isEmpty()) {
                    workflowService.deleteProcessDefinitions(Collections.singleton(new Id(deleteId)));   
                    pw.println("<br>Process Definition '" + deleteId + "' deleted successfully.<br>");
                }
            } catch (Exception e) {
                pw.println("<br>Error occured calling deleteProcessDefinitions<br>");
                pw.println(e.getMessage());
                e.printStackTrace();
            }
			
			
%>
	<table class="summarytable content" style="margin-left: 10px; width: 80%;">

		<thead>
			<tr class='objectList even' style="border: 1px solid #DDDDDD;">
				<th class="summarytableheader" colspan="6">
					<div class="heading"
						style="font-size: 1.2em; padding-bottom: 10px;">Published
						Process Definitions</div>
				</th>
			</tr>
		</thead>

		<tbody>
			<tr class='objectList odd' style="border: 1;">
				<td class="item" style="padding-left: 5px;">ID</td>
				<td class="item" style="padding-left: 5px;">Label</td>
				<td class="item" style="padding-left: 5px;">Version</td>
				<td class="item" style="padding-left: 5px;">Object Type</td>
				<td class="item" style="padding-left: 5px;">Type</td>
                <td class="item" style="padding-left: 5px;">Action</td>
			</tr>

			<%
			IWFProcessDefinitionFindOptions pdOptions = workflowService.getWorkflowFactory().createProcessDefinitionFindOptions();
			pdOptions.setRetrieveVersions(true);
			pdOptions.setProcessDefinitionState(WFProcessDefinitionState.published);
            List<IWFProcessDefinition> pDefs = workflowService.getProcessDefinitions(pdOptions).getProcessDefinitions();
			for (IWFProcessDefinition pDef : pDefs) {
%>
			<tr class='objectList even' style="border: 1px solid #DDDDDD;">
				<td class="item" style="padding-left: 5px;"><%=pDef.getId() %>
				</td>
				<td class="item" style="padding-left: 5px;"><a href="./wfProcessDefinition.jsp?processDefinitionId=<%=pDef.getId()%>&state=published">
						<%=pDef.getNameLabel() %>
				</a>
				</td>
				<td class="item" style="padding-left: 5px;"><%=pDef.getVersionNumber() %>
				</td>
				<td class="item" style="padding-left: 5px;"><%=pDef.getObjectType() %>
				</td>
				<td class="item" style="padding-left: 5px;"><%=pDef.getType() %>
				</td>
                <td class="item" style="padding-left: 5px;"><a href="./wfProcessDefinitionList.jsp?deleteId=<%=pDef.getId()%>">
                        Delete
                </a></td>
			</tr>

			<%
			}
%>
			<tr class='objectList even' style="border: 1px solid #DDDDDD;">
				<th class="summarytableheader" colspan="6">
					<div class="heading" style="font-size: 1.2em; padding-bottom: 10px;">Draft Process Definitions</div>
				</th>
			</tr>
			<tr class='objectList odd' style="border: 1;">
				<td class="item" style="padding-left: 5px;">ID</td>
				<td class="item" style="padding-left: 5px;">Label</td>
				<td class="item" style="padding-left: 5px;">Version</td>
				<td class="item" style="padding-left: 5px;">Object Type</td>
				<td class="item" style="padding-left: 5px;">Type</td>
                <td class="item" style="padding-left: 5px;">Action</td>
			</tr>

			<%			
			
            pdOptions.setProcessDefinitionState(WFProcessDefinitionState.draft);
            pDefs = workflowService.getProcessDefinitions(pdOptions).getProcessDefinitions();
			for (IWFProcessDefinition pDef : pDefs) {
%>

			<tr class='objectList even' style="border: 1px solid #DDDDDD;">
				<td class="item" style="padding-left: 5px;"><%=pDef.getId() %>
				</td>
				<td class="item" style="padding-left: 5px;"><a href="./wfProcessDefinition.jsp?processDefinitionId=<%=pDef.getId()%>">
						<%=pDef.getNameLabel() %>
				</a></td>
				<td class="item" style="padding-left: 5px;"><%=pDef.getVersionNumber() %>
				</td>
				<td class="item" style="padding-left: 5px;"><%=pDef.getObjectType() %>
				</td>
				<td class="item" style="padding-left: 5px;"><%=pDef.getType() %>
				</td>
                <td class="item" style="padding-left: 5px;"><a href="./wfProcessDefinitionList.jsp?deleteId=<%=pDef.getId()%>">
                        Delete
                </a></td>
			</tr>

			<%
			}
%>
		</tbody>
	</table>
	<%

	        } catch (Exception e) {
	            pw.println("<br>Error occurred<br>");
	            pw.println(e.getMessage());
				e.printStackTrace();
	        }
        %>
	<br />

	<div>
		<form action="wfProcessDefinitionList.jsp" method="post">
			<textarea name="processDefinition" rows="20" cols="100"></textarea>
			<br />
			<div style="vertical-align: bottom">
				<input type="submit" value="Create Process Definition" />
			</div>
		</form>
	</div>
</body>
</html>