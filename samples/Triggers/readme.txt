﻿Licensed Materials - Property of IBM

OpenPages with Watson (PID: 5725-D51)

(c) Copyright IBM Corporation 2018 - 2020. All Rights Reserved.

US Government Users Restricted Rights- Use, duplication or
disclosure restricted by GSA ADP Schedule Contract with IBM Corp.

0. Introduction

This sample is provided as-is to demonstrate the capabilities of the OpenPages with Watson API for educational purposes, this is not intended to be a production ready application. 

This sample contains a set of triggers using the GRC Trigger framework. There are Create and Update triggers on LossEvent and a Create trigger on SOXBusEntity. The triggers use ContentTypeMatchRule.

I. Setup



II. Compile Triggers
   The triggers need to be compiled and built into openpages-ext.jar.

   To create as a Project in Eclipse:
   1. From the File menu, select New > Java Project.
   2. In the Create Project Wizard, uncheck 'Use default location'. 
      In Location, use the Browse button to select the Triggers sample folder and click Next.
   3. Add the following JAR files from from <OpenPages_Home>\wlp-usr\shared\apps\op-apps.ear to the project's lib folder:
       * com.ibm.openpages.api.jar
       * aurora.jar
       * commons-logging-1.2.jar
       * opappcommon.jar
       * slf4j-api-1.6.1.jar
       where <OpenPages_Home> is the root path of OpenPages installation, (e.g., C:\OpenPages or /home/opuser/OP/OpenPages)
   4. Add servlet.jar (later than v2.5) to the project's lib folder
   	  (e.g., WebSphere - <WebSphere_home>\AppServer\plugins\javax.j2ee.servlet.jar
   	  	or Tomcat - <Tomcat_home>\lib\servlet-api.jar)
   5. Export project to openpages-ext.jar, select only .java (.class) to be exported to the jar.

III. Deploying triggers

1. To deploy the server, you must copy the openpages-ext.jar server [All application servers]
   <OpenPages_Home>\aurora\lib

   where
   <OpenPages_Home> is the installation location for OpenPages (e.g., C:\OpenPages)

2. Log in to the OpenPages with Watson application as OpenPagesAdministrator.
   For example, https://<host>:10111/
   Switch profile to 'OpenPages Platform 3'
   Click on Gear icon, Users and Security, System Files and expand / and select _trigger_config_.xml
   Click Update
   Choose Yes, then browse to the _trigger_config_.xml file in the Triggers project.
   Click Finish.

2. Restart all the OpenPages services for the new triggers to be picked up.


IV. Running the tests

Test No.1:
1. Log in to the OpenPages GRC application as OpenPagesAdministrator.
2. Navigate to any Business Entity object details page.
3. Click on Loss Events.
3. Under the Actions menu, choose 'Add new Loss Event'.
4. Set the occurence start date after the occurence end date.
   Clicking the "Save" button will result in a validation error. Correct the error by setting the start date before the end date. 
   The Recognition Date will show today's date. The value for Risk Category will be set to 'External Fraud'.
5. Click on the newly created Loss Event.
6. Under the Actions menu, choose 'Edit this LossEvent'.
7. Enter a name in the Approver field. 
8. In the Business Line field, choose 'Commercial Banking' and set the Discovery Date to today's date.  
9. Once saved, the Status will be set to 'Approved'.

Test No.2:
1. Navigate to the Business Entities page.
2. Click on Add new.
3. Enter in Business Entity details.
4. Once saved, the value for Business Entity type will be set to "Business".

Test No.3:
1. Navigate to any Business Entity object details page.
2. Clink on Issues.
3. Under the Actions menu, choose 'Add a new Issue'.
4. Enter a name for the issue.
5. Once saved, the value for Status will be set to "New".
6. Clink on the newly created Issue.
7. Under the Actions menu, choose 'Edit this Issue'.
8. Edit the Additional Description field.
9. Once saved, the Status will be set to "Closed" and the Due Date will be set to the current date.

For more information on Job Types and Java Actions, refer to the OpenPages Trigger Developer Guide.

