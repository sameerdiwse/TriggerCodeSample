Licensed Materials - Property of IBM

OpenPages with Watson (PID: 5725-D51)

(c) Copyright IBM Corporation 2013 - 2020. All Rights Reserved.

US Government Users Restricted Rights- Use, duplication or
disclosure restricted by GSA ADP Schedule Contract with IBM Corp.

0. Introduction

This sample is provided as-is to demonstrate the capabilities of the OpenPages with Watson API for educational purposes, this is not intended to be a production ready application. 

This sample illustrates using the Remote REST APIs to fulfill a use case for anonymous data entry.

- Allows anonymous users to enter in financial losses without logging in to the OpenPages system.
- It is not required to create the users in OpenPages security.
- Users will navigate to a company website or existing web portal infrastructure and fill out the 
  form with details of the loss. 
- Upon submission, the loss may be created in OpenPages using the REST API.

Possible features that could be built on this sample:
- Validate the input data based on OpenPages metadata
- Autonaming, default field values, associate to parents, and attachments

Implementation:
External form is hosted on a separate server, outside of OpenPages with Watson application servers.
Running in a servlet container (e.g., Apache Tomcat v6.0 or higher).
Web form and integration implemented with Java/JSP.
REST Client code implemented with Apache Wink REST client library.


I. Setup

The sample has been developed using Tomcat integrated with Eclipse. 

To install Tomcat 6.0 Server Runtime in Eclipse:
   1. In Eclipse, go to Window > Server > Runtime Environments
   2. Click Add...
   3. Choose Apache > Apache Tomcat v6.0
   4. Check 'Create new local server'.
   5. Specify a server name, or use the default.
   6. Click Download and Install and follow the steps to install the server runtime.
   7. Select an installation location.
   8. Click Finish.

To create a project in Eclipse:
   1. Create a new dynamic web project in Eclipse named as LossEventClient
   2. Copy and replace AnonLossEventFormREST/src and AnonLossEventFormREST/WebContent directories
      to the LossEventClient project folder.
   3. Add the following JAR files from <OpenPages_Home>/wlp-usr/shared/apps/op-apps.ear/
      to the project LossEventClient/WebContent/WEB-INF/lib directory:
       * com.ibm.openpages.api.marshalling.jar
       * jsr311-api-1.1.1.jar
       * wink-1.2.1-incubating.jar
       * commons-codec-1.10.jar
       * slf4j-api-1.6.1.jar
       * slf4j-jdk14-1.6.1.jar
       * com.ibm.openpages.api.jar
       * com.ibm.openpages.api.rest.jar
       where <OpenPages_Home> is the root path of OpenPages installation, (e.g., /home/opuser/OP/OpenPages or C:\OpenPages)
   4. The following jars are no longer provided by IBM but are available from several open source repositories:
       * jackson-core-asl-1.9.13.jar
       * jackson-mapper-asl-1.9.13.jar
   5. Modify servlet init-parameters in web.xml to fit your local environment.
   6. Refresh project LossEventClient
   
II. Running the sample

   1. Right-click on the LossEventClient project in Eclipse, and select Run on Server.
   2. If you have not defined an existing server, select Manually define a new server.
   3. Choose Apache > Tomcat v6.0 Server.
   4. Select your server runtime environment.
   5. Select Always use this server when running this project.
   6. Click Finish.

   Open this URL in your browser:
   http://localhost:8080/LossEventClient/lossform
   
   Enter the Loss field values and click Submit when ready.
