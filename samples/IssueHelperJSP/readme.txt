Licensed Materials - Property of IBM

OpenPages GRC Platform (PID: 5725-D51)

(c) Copyright IBM Corporation 2013 - 2020. All Rights Reserved.

US Government Users Restricted Rights- Use, duplication or
disclosure restricted by GSA ADP Schedule Contract with IBM Corp.

0. Introduction
   This sample is for a basic "Issue Helper", a custom form that extends the OpenPages user 
   interface. The sample shows how you can retrieve Metadata and Profile information for a 
   SOXIssue Object Type and dynamically creates a simple HTML form to create objects of that
   type.

   There are two JSPs involved. The first is IssueForm.jsp, where you retrieve metadata and 
   profile specific information to generate a simple, one column input form that is based on 
   the Detail View for the Issue object in the User's profile. It uses Configuration Service 
   mainly to retrieve the metadata.

   On submission of the form, it will POST the values to a second JSP (IssueSubmit.jsp) which 
   takes the form values and sets them on the fields of a new Issue that it will save using 
   the Resource Service. 

   For convenience, the source is contained within the JSP files, primarily as scriplets.

I. Setup
   1. You must have an Object Profile that includes the SOXIssue object type. This profile 
      should be associated with the user who will test the report in OpenPages (e.g., 
      OpenPagesAdministrator). The sample schema and data set provide Issue fields, which 
      are used in the Issue Helper. If you have additional custom fields on the issue, they 
      may not be displayed without further modifications.

   2. Copy the IssueForm.jsp and IssueSubmit.jsp to the OpenPages 'sosa' webapp directory for 
      all Application Servers.

      WebSphere Liberty location:
      <OpenPages_Home>/wlp-usr/shared/apps/op-apps.ear/sosa.war

      where
      <OpenPages_Home> is the installation location for OpenPages (e.g., C:\OpenPages).
   

II. Running the test jsp
   1. Log in to OpenPages UI (e.g., http://hostname:10108/openpages).
   2. Once logged in, manually type the following URL after /openpages: 
      /IssueForm.jsp

   Example:
   http://hostname:10108/openpages/IssueForm.jsp

   In the JSP page that loads, you may complete all the fields or leave them blank.
   Click Submit at the bottom of the form to create the Issue.

   On the next page, IssueSubmit.jsp, will read the values that were submitted and will save 
   them to a new Issue object.
