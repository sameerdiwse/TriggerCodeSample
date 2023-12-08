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
<%@page import="com.ibm.openpages.api.resource.*"%>
<%@page import="com.ibm.openpages.api.metadata.*"%>

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
<title>Workflow Process Instance List</title>

</head>
<body class="oneui">
	<div class="heading" align="center" style="font-size: 1.2em; padding-bottom: 10px;">Workflow Process Instance List</div>
	<% 
			
            PrintWriter pw = new PrintWriter(out);
			
			try {	            

				// init steps
				IServiceFactory serviceFactory = null;
				try{
					serviceFactory = ServiceFactory.getServiceFactory(request);
				}catch(OpenPagesException ope){
					Cookie cookie=new Cookie("opTargetUrl","./wfProcessInstanceList.jsp");
					cookie.setMaxAge(-1);
					response.addCookie(cookie);
					response.sendRedirect("../log.on.do");
				}
				
	        IWorkflowService workflowService = serviceFactory.createWorkflowService();
	    	IResourceService resourceService = serviceFactory.createResourceService();
			
			
%>
   <div>
		<form action="wfProcessInstance.jsp" method="post">
			<label style="margin-left: 15px;" for="resourceid">Enter a Resource Id to Start</label>
	        <input type="text" id="resourceid" name="resourceid" placeholder="Type resource id for workflow" size="35px" />
				<input type="submit" value="Start New Process" />
		</form>
	</div>
	<br><br>
	<table class="summarytable content" style="margin-left: 10px; width: 80%;">

		<thead>
			<tr class='objectList even' style="border: 1px solid #DDDDDD;">
				<th class="summarytableheader" colspan="10">
					<div class="heading" style="font-size: 1.2em; padding-bottom: 10px;">Active Process Instances</div>
				</th>
			</tr>
		</thead>

		<tbody>
			<tr class='objectList odd' style="border: 1;">
				<td class="item" style="padding-left: 5px;">ID</td>
				<td class="item" style="padding-left: 5px;">Resource Name</td>
				<td class="item" style="padding-left: 5px;">Object Type</td>
				<td class="item" style="padding-left: 5px;">Process Name</td>
				<td class="item" style="padding-left: 5px;">Process Version</td>
				<td class="item" style="padding-left: 5px;">Process Owner</td>
				<td class="item" style="padding-left: 5px;">Activity</td>
				<td class="item" style="padding-left: 5px;">Assignee</td>
				<td class="item" style="padding-left: 5px;">Status</td>
				<td class="item" style="padding-left: 5px;">Comments</td>
			</tr>

			<%			
			List<IWFProcess> processes = workflowService.getActiveProcesses();
			for (IWFProcess process : processes) {
			    IGRCObject resource = resourceService.getGRCObject(process.getResourceId());
			    IWFProcessDefinition pDef = workflowService.getProcessDefinitionByVersionId(process.getProcessVersionId());
			    IWFActivityInstance activityInstance = process.getCurrentActivityInstance();
			    IWFActivity activity = pDef.getActivity(activityInstance.getActivityId());
%>
			<tr class='objectList even' style="border: 1px solid #DDDDDD;">
				<td class="item" style="padding-left: 5px;"><%=process.getId() %>
				</td>
				<td class="item" style="padding-left: 5px;"><a href="./wfProcessInstance.jsp?resourceid=<%=resource.getId()%>">
						<%=resource.getName() %>
				</a>
				</td>
				<td class="item" style="padding-left: 5px;"><%=pDef.getObjectType() %>
				<td class="item" style="padding-left: 5px;"><a href="./wfProcessDefinition.jsp?processDefinitionId=<%=pDef.getId()%>&state=published">
						<%=pDef.getName() %>
				</a>
				</td>
				<td class="item" style="padding-left: 5px;"><%=pDef.getVersionNumber() %></td>
				<td class="item" style="padding-left: 5px;"><%=process.getOwners() %></td>
				<td class="item" style="padding-left: 5px;"><%=activity.getName() %>
				<td class="item" style="padding-left: 5px;"><%=activityInstance.getAssignees() %></td>
				<td class="item" style="padding-left: 5px;"><%=process.getStatusMessage() %></td>
				<td class="item" style="padding-left: 5px;"><%=process.getComments() == null ? "" : process.getComments() %></td>
			</tr>
<%
				List<IWFProcess> subProcesses = activityInstance.getSubProcesses();
				if (subProcesses != null && subProcesses.size() > 0) {
%>
                <tr class='objectList even' style="border: 1px solid #DDDDDD;background-color:#F6F6F6">
				<td class="item" style="padding-left: 5px;"></td>
				<td class="item" colspan="7" style="padding-left: 5px;">
				<table class="summarytable content" style="margin-left: 10px; width: 99%;">

					<thead>
						<tr class='objectList even' style="border: 1px solid #DDDDDD;">
							<th class="summarytableheader" colspan="10">
								<div class="heading" style="font-size: 1.2em; padding-bottom: 10px;">Sub Process Instances</div>
							</th>
						</tr>
					</thead>
			
					<tbody>
						<tr class='objectList even' style="border: 1;">
							<td class="item" style="padding-left: 5px;">ID</td>
							<td class="item" style="padding-left: 5px;">Resource Name</td>
				            <td class="item" style="padding-left: 5px;">Object Type</td>
							<td class="item" style="padding-left: 5px;">Process Name</td>
							<td class="item" style="padding-left: 5px;">Process Version</td>
							<td class="item" style="padding-left: 5px;">Activity</td>
							<td class="item" style="padding-left: 5px;">Assignee</td>
							<td class="item" style="padding-left: 5px;">State</td>
							<td class="item" style="padding-left: 5px;">Status</td>
							<td class="item" style="padding-left: 5px;">Comments</td>
						</tr>
<% 
				    for (IWFProcess subProc : subProcesses) {
					    IGRCObject childRes = resourceService.getGRCObject(subProc.getResourceId());
					    IWFProcessDefinition childPDef = workflowService.getProcessDefinitionByVersionId(subProc.getProcessVersionId());
%>
						<tr class='objectList even' style="border: 1px solid #DDDDDD;">
							<td class="item" style="padding-left: 5px;"><%=subProc.getId() %>
							</td>
							<td class="item" style="padding-left: 5px;"><a href="./wfProcessInstance.jsp?resourceid=<%=childRes.getId()%>">
									<%=childRes.getName() %>
							</a>
							</td>
							<td class="item" style="padding-left: 5px;"><%=childPDef.getObjectType() %>
							<td class="item" style="padding-left: 5px;"><a href="./wfProcessDefinition.jsp?processDefinitionId=<%=childPDef.getId()%>&state=published">
									<%=childPDef.getName() %>
							</a>
							</td>
							<td class="item" style="padding-left: 5px;"><%=childPDef.getVersionNumber() %>
							</td>
<%
							if (subProc.getState() == WFProcessState.open) {
							    IWFActivityInstance subActivityInstance = subProc.getCurrentActivityInstance();
							    IWFActivity subActivity = childPDef.getActivity(subActivityInstance.getActivityId());
%>
							<td class="item" style="padding-left: 5px;"><%=subActivity.getName() %></td>
							<td class="item" style="padding-left: 5px;"><%=subActivityInstance.getAssignees() %></td>
<%
							} else {
%>
                            <td class="item" style="padding-left: 5px;"></td>
							<td class="item" style="padding-left: 5px;"></td>
<%
							}
%>
							<td class="item" style="padding-left: 5px;"><%=subProc.getState() %></td>
							<td class="item" style="padding-left: 5px;"><%=subProc.getStatusMessage() %></td>
							<td class="item" style="padding-left: 5px;"><%=subProc.getComments() == null ? "" : subProc.getComments() %></td>
						</tr>
<%
				    }
%>
                </tbody>
                </table>
<%
				}
%>

				</td></tr>
<%
			}
%>
		</tbody>
	</table>
	
	
	
	<table class="summarytable content" style="margin-left: 10px; width: 80%;">

		<thead>
			<tr class='objectList even' style="border: 1px solid #DDDDDD;">
				<th class="summarytableheader" colspan="7">
					<div class="heading" style="font-size: 1.2em; padding-bottom: 10px;">Completed Process Instances</div>
				</th>
			</tr>
		</thead>

		<tbody>
			<tr class='objectList odd' style="border: 1;">
				<td class="item" style="padding-left: 5px;">ID</td>
				<td class="item" style="padding-left: 5px;">Resource Name</td>
				            <td class="item" style="padding-left: 5px;">Object Type</td>
				<td class="item" style="padding-left: 5px;">Process Name</td>
				<td class="item" style="padding-left: 5px;">Process Version</td>
				<td class="item" style="padding-left: 5px;">Status</td>
				<td class="item" style="padding-left: 5px;">Comments</td>
			</tr>

			<%			
			processes = workflowService.getCompletedProcesses();
			for (IWFProcess process : processes) {
			    IGRCObject resource = resourceService.getGRCObject(process.getResourceId());
			    IWFProcessDefinition pDef = workflowService.getProcessDefinitionByVersionId(process.getProcessVersionId());
%>
			<tr class='objectList even' style="border: 1px solid #DDDDDD;">
				<td class="item" style="padding-left: 5px;"><%=process.getId() %>
				</td>
				<td class="item" style="padding-left: 5px;"><%=resource.getName() %>
				</td>
				<td class="item" style="padding-left: 5px;"><%=pDef.getObjectType() %>
				</td>
				<td class="item" style="padding-left: 5px;"><a href="./wfProcessDefinition.jsp?processDefinitionId=<%=pDef.getId()%>&state=published">
						<%=pDef.getName() %>
				</a>
				</td>
				<td class="item" style="padding-left: 5px;"><%=pDef.getVersionNumber() %></td>
				<td class="item" style="padding-left: 5px;"><%=process.getStatusMessage() %></td>
				<td class="item" style="padding-left: 5px;"><%=process.getComments() == null ? "" : process.getComments() %></td>
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

</body>
</html>