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
<%@page import="com.ibm.openpages.api.service.ejb.workflow.impl.*"%>
<%@page import="com.ibm.openpages.api.service.local.workflow.*"%>
<%@page import="org.codehaus.jackson.map.ObjectMapper"%>
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
<title>Workflow Process Definition Detail</title>

</head>
<body class="oneui">


	<% 
			
            PrintWriter pw = new PrintWriter(out);
			
			try {	            
	            String processDefinitionId = request.getParameter("processDefinitionId");
				String processDefinitionJSON = request.getParameter("processDefinition");
				String state = request.getParameter("state");
				String publish = request.getParameter("publish");
				// init steps
				IServiceFactory serviceFactory = null;
				try{
					serviceFactory = ServiceFactory.getServiceFactory(request);
				}catch(OpenPagesException ope){
					Cookie cookie=new Cookie("opTargetUrl","./wfProcessDefinition.jsp?processDefinitionId=" + processDefinitionId);
					cookie.setMaxAge(-1);
					response.addCookie(cookie);
					response.sendRedirect("../log.on.do");
				}
				
	        IWorkflowService workflowService = serviceFactory.createWorkflowService();
			
			try {
				if (processDefinitionJSON != null && !processDefinitionJSON.isEmpty()) {
				    IWFProcessDefinition procDef = WFUtil.parseWFProcessDefinition(processDefinitionJSON);
	                processDefinitionId = procDef.getId().toString();	
	                workflowService.updateProcessDefinition(procDef); 			 
	                if (publish != null && !publish.isEmpty()) {
	                    workflowService.publishProcessDefinition(procDef.getId());
	                    state = "published";
	                }
				}
	        } catch (Exception e) {
	            pw.println("<br>Error occured calling updateProcessDefinition<br>");
	            pw.println(e.getMessage());
				e.printStackTrace();
	        }
			
			boolean isDraft = state == null || state.equals("draft");
			IWFProcessDefinition pDef = null;
			try {
			    pDef = workflowService.getProcessDefinition(new Id(processDefinitionId), isDraft);
			} catch (WFException e) {
                pDef = workflowService.getProcessDefinition(new Id(processDefinitionId), !isDraft);
			}
			
			if (pDef != null) {
				pDef = pDef.clone();
%>
	<div class="heading" align="center" style="font-size: 1.2em; padding-bottom: 10px;">
		<%=pDef.getNameLabel() %> Detail (<%=pDef.getState() %>)
	</div>
    <p>Back to <a href="./wfProcessDefinitionList.jsp">Process Definition List</a>.</p>
	<div>
		<form action="wfProcessDefinition.jsp" method="post">
			<textarea name="processDefinition" rows="20" cols="100"><%=WFUtil.writeAsJSON((WFProcessDefinitionImpl)pDef, true, true)%></textarea>
			<br />
			<div style="vertical-align: bottom">
				<input type="submit" value="Update Process Definition" />
                <input type="submit" name="publish" value="Publish Process Definition" />
			</div>
		</form>
	</div>

	<br />


	<table class="summarytable content" style="margin-left: 10px; width: 95%;">
		<thead>
			<tr class='objectList even' style="border: 1px solid #DDDDDD;">
				<th class="summarytableheader" colspan="7">
					<div class="heading"
						style="font-size: 1.2em; padding-bottom: 10px;">Activities (Current Version)</div>
				</th>
			</tr>
		</thead>
		<tbody>
			<tr class='objectList odd' style="border: 1;">
				<td class="item" style="padding-left: 5px;">Activity ID</td>
				<td class="item" style="padding-left: 5px;">Activity Name</td>
				<td class="item" style="padding-left: 5px;">Activity Label</td>
				<td class="item" style="padding-left: 5px;">Activity Type</td>
				<td class="item" style="padding-left: 5px;">Available Transitions</td>
				<td class="item" style="padding-left: 5px;">Pre-Actions</td>
				<td class="item" style="padding-left: 5px;">Post-Actions</td>
			</tr>
			<%


        List<IWFActivity> activities = pDef.getActivities();
		for (IWFActivity activity : activities) {
%>

			<tr class='objectList even' style="border: 1px solid #DDDDDD;">
				<td class="item" style="padding-left: 5px;"><%=activity.getId() %>
				</td>
				<td class="item" style="padding-left: 5px;"><%=activity.getName() %>
				</td>
				<td class="item" style="padding-left: 5px;"><%=activity.getNameLabel() %>
				</td>
				<td class="item" style="padding-left: 5px;"><%=activity.getType() %>
				</td>
				<td class="item" style="padding-left: 5px;">
					<%
	    if (activity.getTransitions() != null) {
%>
					<table class="summarytable content">

						<%
	    for (IWFTransition transition : activity.getTransitions()) {
%>
						<tr class='objectList even' style="border: 1px solid #DDDDDD;">
							<td class="item" style="padding-left: 5px; width: 50%;"><%=transition.getName() %>
							</td>
							<td class="item" style="padding-left: 5px;"><%=transition.getTarget() == null ? "" : transition.getTarget() %>
							</td>
						</tr>
						<%
	    }
%>
					</table> <%
	    } else {pw.println("none");}
%>
				</td>
				<td class="item" style="padding-left: 5px;">
					<%
	    if (activity.getPreActions() != null) {
%> <textarea rows="8" readonly cols="50"><%=new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(activity.getPreActions())%></textarea>
					<%
	    }
%>
				</td>
				<td class="item" style="padding-left: 5px;">
					<%
	    if (activity.getPostActions() != null) {
%> <textarea rows="8" readonly cols="50"><%=new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(activity.getPostActions())%></textarea>
					<%
	    }
%>
				</td>
			</tr>
			<%
	    }
%>

		</tbody>
	</table>
	<br />

	<table class="summarytable content" style="margin-left: 10px; width: 95%;">
		<thead>
			<tr class='objectList even' style="border: 1px solid #DDDDDD;">
				<th class="summarytableheader" colspan="7">
					<div class="heading" style="font-size: 1.2em; padding-bottom: 10px;">Transitions (Current Version)</div>
				</th>
			</tr>
		</thead>
		<tbody>
			<tr class='objectList odd' style="border: 1;">
				<td class="item" style="padding-left: 5px;">Transition Name</td>
				<td class="item" style="padding-left: 5px;">Transition Label</td>
				<td class="item" style="padding-left: 5px;">Source Activity</td>
				<td class="item" style="padding-left: 5px;">Target Activity</td>
				<td class="item" style="padding-left: 5px;">Condition</td>
				<td class="item" style="padding-left: 5px;">Actions</td>
			</tr>


			<%


	    for (IWFActivity activity : activities) {
		    for (IWFTransition transition : activity.getTransitions()) {
%>

			<tr class='objectList even' style="border: 1px solid #DDDDDD;">
				<td class="item" style="padding-left: 5px;"><%=transition.getName() %>
				</td>
				<td class="item" style="padding-left: 5px;"><%=transition.getNameLabel() %>
				</td>
				<td class="item" style="padding-left: 5px;"><%=activity.getName() == null ? "" : activity.getName() %>
				</td>
				<td class="item" style="padding-left: 5px;"><%=transition.getTarget() == null ? "" : transition.getTarget() %>
				</td>
				<td class="item" style="padding-left: 5px;">
					<%
	    if (transition.getCondition() != null) {
%> <textarea rows="8" readonly cols="50"><%=new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(transition.getCondition())%></textarea>
					<%
	    }
%>
				</td>
				<td class="item" style="padding-left: 5px;">
					<%
	    if (transition.getActions() != null && transition.getActions().size() > 0) {
%> <textarea rows="8" readonly cols="50"><%=new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(transition.getActions())%></textarea>
					<%
	    }
%>
				</td>
			</tr>
			<%
		    }
	    }
%>


		</tbody>
	</table>
	<br />

	<table class="summarytable content" style="margin-left: 10px; width: 95%;">

		<thead>
			<tr class='objectList even' style="border: 1px solid #DDDDDD;">
				<th class="summarytableheader" colspan="5">
					<div class="heading" style="font-size: 1.2em; padding-bottom: 10px;">'<%=pDef.getName() %>' Versions
					</div>
				</th>
			</tr>
		</thead>

		<tbody>
			<tr class='objectList odd' style="border: 1;">
				<td class="item" style="padding-left: 5px;">ID</td>
				<td class="item" style="padding-left: 5px;">Name</td>
				<td class="item" style="padding-left: 5px;">Version</td>
				<td class="item" style="padding-left: 5px;">Object Type</td>
				<td class="item" style="padding-left: 5px;">Type</td>
			</tr>
			<tr class='objectList even' style="border: 1px solid #DDDDDD;">
				<td class="item" style="padding-left: 5px;"><%=pDef.getId() %>
				</td>
				<td class="item" style="padding-left: 5px;"><%=pDef.getName() %>
				</td>
				<td class="item" style="padding-left: 5px;"><%=pDef.getVersionNumber() %>
				</td>
				<td class="item" style="padding-left: 5px;"><%=pDef.getObjectType() %>
				</td>
				<td class="item" style="padding-left: 5px;"><%=pDef.getType() %>
				</td>
			</tr>
		</tbody>
	</table>
	<%		
			} else {pw.println("No Process Definition exists for id = '" + processDefinitionId + "'");}       
	        } catch (Exception e) {
	            pw.println("<br>Error occurred<br>");
	            pw.println(e.getMessage());
				e.printStackTrace();
	        }
        %>

</body>
</html>