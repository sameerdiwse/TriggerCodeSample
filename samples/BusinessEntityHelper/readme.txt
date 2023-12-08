Licensed Materials - Property of IBM

OpenPages GRC Platform (PID: 5725-D51)

(c) Copyright IBM Corporation 2015 - 2020. All Rights Reserved.

US Government Users Restricted Rights- Use, duplication or
disclosure restricted by GSA ADP Schedule Contract with IBM Corp.


0. Introduction
   This sample is for a basic "Business Entity Helper", a custom form that extends the OpenPages 
   user interface. The sample shows how you can retrieve Metadata and Profile information for a 
   SOXBusEntity Object Type and dynamically create a simple HTML form to create objects of that 
   type.

   There are two JSPs involved. The first is BusEntityForm.jsp, where you retrieve metadata and 
   profile specific information to generate a simple, one-column input form that is based on the 
   Detail View for the Business Entity object in the User's profile. It uses Configuration Service 
   to retrieve the metadata.

   On submission of the form, it will POST the values to a second JSP (BusEntitySubmit.jsp). The
   JSP associates the submitted form values to fields in the new Business Entity and saves the data 
   using the Resource Service. 

   For convenience, the source is contained within the JSP files, primarily as scriplets.

I. Setup
   1. You must have an Object Profile that includes the SOXBusEntity object type. This profile should
   be associated with the user who will test the report in OpenPages (e.g., OpenPagesAdministrator). 
   The sample schema and data set provide Business Entity fields, which are used in the Business Entity 
   Helper. If you have additional custom fields on the business entity, they may not be displayed without
   further modifications.

   2. Copy the BusEntityForm.jsp and BusEntitySubmit.jsp files to the OpenPages 'sosa' webapp directory 
   for all Application 
   Servers.

      WebSphere Liberty location:
      <OpenPages_Home>/wlp-usr/shared/apps/op-apps.ear/sosa.war

      where
      <OpenPages_Home> is the installation location for OpenPages (e.g., C:\OpenPages).
   

II. Running the test jsp
   1. Log in to the OpenPages UI (e.g., http://hostname:10108/openpages).
   2. Once logged in, manually type the following URL after /openpages: 
      /BusEntityForm.jsp

   Example:
   http://hostname:10108/openpages/BusEntityForm.jsp

   In the JSP page that loads, you may complete all the fields or leave them blank.
   Click Submit at the bottom of the form to create the Business Entity.

   On the next page, BusEntitySubmit.jsp, will read the values that were submitted and will save them to 
   a new Business Entity object.
