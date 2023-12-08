<!DOCTYPE HTML>

<!--=============================================================================
    Licensed Materials - Property of IBM
    
   
    5725-D51, 5725-D52, 5725-D53, 5725-D54
   
     © Copyright IBM Corporation 2018 - 2020. All Rights Reserved.
     
    US Government Users Restricted Rights- Use, duplication or disclosure restricted 
    by GSA ADP Schedule Contract with IBM Corp.
==============================================================================-->

<%@page import="com.openpages.sdk.search.RelationalOperators"%>
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
<%@page import="java.util.*"%>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<link rel="Stylesheet" href="../op_style/default/open_pages.css" />
<title>Workflow Process Instance Test</title>

</head>
<body class="oneui">
	<div class="heading" align="center" style="font-size: 1.2em; padding-bottom: 10px;">Process Instance Test</div>
	
	<div>
		<form action="wfProcessInstance.jsp" method="post">

			<%
PrintWriter pw = new PrintWriter(out);

try {	            
    String transitionNameParam = request.getParameter("transitionName");
	String resourceidParam = request.getParameter("resourceid");
	String processNameParam = request.getParameter("processName");
	String comment = request.getParameter("comment");
	
	String buttonLabel = "Submit";
	// init steps
	IServiceFactory serviceFactory = null;
	try{
		serviceFactory = ServiceFactory.getServiceFactory(request);
	}catch(OpenPagesException ope){
		Cookie cookie=new Cookie("opTargetUrl","./wfProcessInstance.jsp?resourceid=" + resourceidParam);
		cookie.setMaxAge(-1);
		response.addCookie(cookie);
		response.sendRedirect("../log.on.do");
	}

	IWorkflowService workflowService = serviceFactory.createWorkflowService();
	IResourceService resourceService = serviceFactory.createResourceService();
	
	IWFProcess activeProcess = null;
	IWFProcessDefinition procDef = null;
	if (resourceidParam == null) {
%>
         <label style="margin-left: 15px;" for="resourceid">Enter a Resource Id to Start</label>
	     <input type="text" id="resourceid" name="resourceid" placeholder="Type resource id for workflow" size="35px" />
	<%} else {%>
	    
	    <input type="hidden" name="resourceid" value="<%=resourceidParam %>" />
	    <table class="summarytable content" style="margin-left: 10px; width: 500px;border-collapse:separate;">

<% 
	    Id resId = new Id(resourceidParam);
	    IGRCObject resource = resourceService.getGRCObject(resId);
	    ITypeDefinition typeDef = resource.getType();
%>
			<tr class='objectList odd'>
				<td class="item" style="padding-left: 5px;">Resource</td>
				<td class="item" style="padding-left: 5px;"><%=resource.getName() %></td>
			</tr>
<%
	    List<IWFProcess> activeProcesses = workflowService.getActiveProcesses(resId);
        if (activeProcesses != null && activeProcesses.size() > 0) {
            activeProcess = activeProcesses.get(0);
        }
	    if (processNameParam != null && activeProcess == null) {
	        procDef = workflowService.getProcessDefinition(processNameParam, typeDef.getId(), false);
	        activeProcess = workflowService.startProcess(procDef.getId(), resId);
	    }
	    if (transitionNameParam != null) {
	        workflowService.processTransition(activeProcess.getId(), transitionNameParam);  
	        activeProcess = workflowService.getProcess(activeProcess.getId());
	    }
	    if (activeProcess == null) {
%>
			<tr class='objectList odd'>
				<td class="item" style="padding-left: 5px;">Available Processes</td>
<%
			String procList = "";
            IWFProcessDefinitionFindOptions options = workflowService.getWorkflowFactory().createProcessDefinitionFindOptions();
            IWFSearchCondition wfCondition = workflowService.getWorkflowFactory().createSearchCondition(WFConditionAttribute.contentTypeId, RelationalOperators.EQUAL_TO, typeDef.getId().toString());
            options.addCondition(wfCondition);
            options.setProcessDefinitionState(WFProcessDefinitionState.published);
            options.setRetrieveVersions(true);
            WFProcessDefinitionList createPDefs = workflowService.getProcessDefinitions(options);
	        List<IWFProcessDefinition> pDefs = createPDefs.getProcessDefinitions();
	        for (IWFProcessDefinition pDef : pDefs) {
	            procList = procList + pDef.getName() + " (" + pDef.getNameLabel() + ")<br>";
	        }
%>
            <td class="item" style="padding-left: 5px;"><%=procList %></td>
			</tr>
			</table>
			
			<br>
			<label style="margin-left: 15px;" for="processName">Enter a Workflow</label>
			<input type="text" id="processName" name="processName" placeholder="Type name of workflow" size="35px" />
<%
	    } else {
	        procDef = workflowService.getProcessDefinitionByVersionId(activeProcess.getProcessVersionId());
%>
			<tr class='objectList odd'>
				<td class="item" style="padding-left: 5px;">Process Definition</td>
				<td class="item" style="padding-left: 5px;"><%=procDef.getName() + " (" + procDef.getNameLabel() + ")"%></td>
			</tr>
			<tr class='objectList odd'>
				<td class="item" style="padding-left: 5px;">Process Status</td>
				<td class="item" style="padding-left: 5px;"><%=activeProcess.getStatusMessage() %></td>
			</tr>
			<tr class='objectList odd'>
		    <td class="item" style="padding-left: 5px;">Available Transition</td>
<%
			String tranList = "";
	        List<IWFTransition> transitions = workflowService.getTransitions(activeProcess.getId());
	        if (transitions != null) {
		        for (IWFTransition transition : transitions) {
		            tranList = tranList + transition.getName() + "<br>";
		        }
	        }
%>
            <td class="item" style="padding-left: 5px;"><%=tranList %></td>
            </tr>
			</table>
			
			<br>
			<label style="margin-left: 15px;" for="transitionName">Enter Transition</label>
			<input type="text" id="transitionName" name="transitionName" placeholder="Type name of transition" size="35px" />
			<label for="comment">Add a Comment</label>
			<input type="text" id="comment" name="comment" size="35px" />
<%
			

	    }
	}
    
%>

			<input type="submit" value="<%=buttonLabel %>" />
			<br><br><br>
<%
		if (activeProcess != null) {
			List<IWFActivityInstance> activityInstances = workflowService.getActivityInstances(activeProcess.getId());
			Map<Id, IWFActivity> activityMap = new HashMap<Id, IWFActivity>();
%>
			<table class="summarytable content" style="margin-left: 10px; width: 80%;">

					<thead>
						<tr class='objectList even' style="border: 1px solid #DDDDDD;">
							<th class="summarytableheader" colspan="5">
								<div class="heading" style="font-size: 1.2em; padding-bottom: 10px;">Activities</div>
							</th>
						</tr>
					</thead>

						<tr class='objectList odd' style="border: 1;">
							<td class="item" style="padding-left: 5px;">ID</td>
							<td class="item" style="padding-left: 5px;">Name</td>
							<td class="item" style="padding-left: 5px;">Assignee</td>
							<td class="item" style="padding-left: 5px;">State</td>
							<td class="item" style="padding-left: 5px;">Action</td>
						</tr>
<% 			
			for (IWFActivityInstance activityInstance : activityInstances) {
			    IWFActivity activity = procDef.getActivity(activityInstance.getActivityId());
			    activityMap.put(activityInstance.getId(), activity);
%>
			    <tr class='objectList even' style="border: 1px solid #DDDDDD;">
				<td class="item" style="padding-left: 5px;"><%=activityInstance.getId() %> </td>
			    <td class="item" style="padding-left: 5px;"><%=activity.getName() %> </td>
			    <td class="item" style="padding-left: 5px;"><%=activityInstance.getAssignees() == null ? "" : activityInstance.getAssignees().toString() %> </td>
			    <td class="item" style="padding-left: 5px;"><%=activityInstance.getState() %> </td>
			    <td class="item" style="padding-left: 5px;"><%=activityInstance.getAction() == null ? "" : activityInstance.getAction() %> </td>
			    </tr>
<% 	
			    if (activityInstance.getSubActivities().size() > 0) {
%>
<tr class='objectList even' style="border: 1px solid #DDDDDD;background-color:#F6F6F6">
				<td class="item" style="padding-left: 5px;"></td>
				<td class="item" colspan="4" style="padding-left: 5px;">
					<table class="summarytable content" style="margin-left: 10px; width: 95%;">

					<thead>
						<tr class='objectList even' style="border: 1px solid #DDDDDD;">
							<th class="summarytableheader" colspan="4">
								<div class="heading" style="font-size: 1.2em; padding-bottom: 10px;">Assignee Actions</div>
							</th>
						</tr>
					</thead>

						<tr class='objectList even' style="border: 1;">
							<td class="item" style="padding-left: 5px;">ID</td>
							<td class="item" style="padding-left: 5px;">Assignee</td>
							<td class="item" style="padding-left: 5px;">Action</td>
							<td class="item" style="padding-left: 5px;">State</td>
						</tr>
<% 	
			    	for(IWFActivityInstance subActInst : activityInstance.getSubActivities()) {
%>
						<tr class='objectList even' style="border: 1;">
						<td class="item" style="padding-left: 5px;"><%=subActInst.getId() %></td>
						<td class="item" style="padding-left: 5px;"><%=subActInst.getAssignees() == null ? "" : subActInst.getAssignees().toString()  %></td>
						<td class="item" style="padding-left: 5px;"><%=subActInst.getAction() == null ? "" : subActInst.getAction() %></td>
						<td class="item" style="padding-left: 5px;"><%=subActInst.getState() %></td>
					    </tr>
			    	    
			    	    
<%
			    	}
%>
						</table>
						
</tr>
<% 
			    }
	
			}
%>
</table>

			<table class="summarytable content" style="margin-left: 10px; width: 80%;">

					<thead>
						<tr class='objectList even' style="border: 1px solid #DDDDDD;">
							<th class="summarytableheader" colspan="6">
								<div class="heading" style="font-size: 1.2em; padding-bottom: 10px;">Process Event Log</div>
							</th>
						</tr>
					</thead>

						<tr class='objectList odd' style="border: 1;">
							<td class="item" style="padding-left: 5px;">ID</td>
							<td class="item" style="padding-left: 5px;">Event</td>
							<td class="item" style="padding-left: 5px;">Activity</td>
							<td class="item" style="padding-left: 5px;">Actor</td>
							<td class="item" style="padding-left: 5px;">Created By</td>
							<td class="item" style="padding-left: 5px;">Date</td>
						</tr>
<% 			
			List<IWFProcessEvent> processEvents = workflowService.getProcessEventLog(activeProcess.getId());
			for (IWFProcessEvent processEvent : processEvents) {
			    IWFActivity activity = activityMap.get(processEvent.getActivityInstanceId());
%>
						<tr class='objectList odd' style="border: 1;">
							<td class="item" style="padding-left: 5px;"><%=processEvent.getId() %></td>
							<td class="item" style="padding-left: 5px;"><%=processEvent.getMessage() %></td>
							<td class="item" style="padding-left: 5px;"><%=activity == null ? "" : activity.getName() %></td>
							<td class="item" style="padding-left: 5px;"><%=processEvent.getActor() == null? "" : processEvent.getActor() %></td>
							<td class="item" style="padding-left: 5px;"><%=processEvent.getCreatedBy() %></td>
							<td class="item" style="padding-left: 5px;"><%=processEvent.getCreatedDate() %></td>
						</tr>		
<% 			
			}
%>						
</table>
<%
		}
%>
		</form>
	</div>
	<div>
		<p>
<% 		        
	        } catch (Exception e) {
	            pw.println(e.getMessage());
		        if (e.getCause() != null) {
		            pw.println("<br><b>" + e.getCause().getMessage() + "</b>");
                }
	        }
        %>
		</p>
	</div>

</body>
</html>