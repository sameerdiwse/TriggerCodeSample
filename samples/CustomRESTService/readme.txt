Licensed Materials - Property of IBM

OpenPages with Watson (PID: 5725-D51)

(c) Copyright IBM Corporation 2018 - 2020. All Rights Reserved.

US Government Users Restricted Rights- Use, duplication or
disclosure restricted by GSA ADP Schedule Contract with IBM Corp.

## 0. Introduction

This sample is provided as-is to demonstrate the capabilities of the OpenPages with Watson API for educational purposes, this is not intended to be a production ready application. 

This sample introduces how to build and deploy custom REST services under the Public GRC REST 
API web application. This feature is available since IBM OpenPages with Watson v8.0.0.2. These 
services can be written to expose customer specific business logic to external clients or 
applications that will integrate with OpenPages.


## I. Setup

- You must have Java 8 JDK to compile and build your source code.
- Recommend that you have Eclipse, or preferred Java IDE to compile the source code and to be 
  able to make modifications yourself.
- This sample assumes you have an installed OpenPages 8.0.0.2 Environment. 
- You will need server access in order to get required files and deploy your jars.


## II. Compile REST Services
   The sample rest services are Java classes which are examples of Spring RestControllers and 
   need to be compiled and built into a jar.

### To create as a Project in Eclipse:
   1. From the File menu, select New > Java Project.
   2. In the Create Project Wizard, uncheck 'Use default location'. 
      In Location, use the Browse button to select the Triggers sample folder and click Next.
   3. Add the following JAR files from <OpenPages_Home>\wlp-usr\shared\apps\op-apps.ear 
      to the project's lib folder:
       - com.ibm.openpages.api.jar
       - slf4j-api-1.6.1.jar
       - spring-core-5.1.3.RELEASE.jar 
       - spring-web-5.1.3.RELEASE.jar 
       - spring-aop-5.1.3.RELEASE.jar 
       - spring-webmvc-5.1.3.RELEASE.jar 
       - spring-oxm-5.1.3.RELEASE.jar 
       - spring-beans-5.1.3.RELEASE.jar 
       - spring-context-5.1.3.RELEASE.jar 
       - spring-expression-5.1.3.RELEASE.jar 
       - spring-aspects-5.1.3.RELEASE.jar 
       where <OpenPages_Home> is the root path of OpenPages installation, (e.g., /home/opuser/OP/OpenPages or C:\OpenPages)
   4. The following jars are no longer provided by IBM but are available from several open source repositories:
       * jackson-core-asl-1.9.13.jar
       * jackson-mapper-asl-1.9.13.jar
   5. Add servlet.jar (later than v2.5) to the project's lib folder
   	  (e.g., WebSphere Liberty - <WebSphere_Liberty_home>\dev\api\spec\com.ibm.websphere.javaee.servlet.4.0_1.0.38.jar)
   6. [Optional] If you wish to build Custom Services that leverage the OpenPages Legacy SDK, also 
      add the following JAR file from <OpenPages_Home>\wlp-usr\shared\apps\op-apps.ear\
       - aurora.jar
   7. Export project as a jar to *sample-rest.jar*, select only .java (.class) to be exported to 
      the jar.

### Alternative build - using Ant	
	If desired, you can build from a command-line which has Ant 1.8.x or later installed
	
	Run command from the CustomRESTService directory:
	`ant build-jar`

	This will compile the classes, and build a `sample-rest.jar` in the CustomRESTService

	
## III. Deploying REST Services

1. To deploy to the application server, you must copy the sample-rest.jar [All application servers]
   to:
      `<OpenPages_Home>\aurora\op-ext-lib`

   where
   `<OpenPages_Home>` is the installation location for OpenPages (e.g., C:\OpenPages or 
    /home/opuser/OP/OpenPages)

2. Restart all the OpenPages services for the new rest services to be picked up.

3. In OpenPages as an administrator, confirm that the Registry Setting is set to true to enable 
   Custom REST Services:
	- /Platform/API/Custom/Enable Custom REST Service
	
	Note: changing this registry setting does not require service restart


## IV. Executing the Samples

You will need to use a REST client of your choice, these would be any third-party application which
can be used to construct http requests to send the to sample REST end points that you have deployed.

The custom REST endpoints use the same security that all the Public GRC REST API end points have. 
This requires using BASIC authentication headers for sending credentials to the server.

### Sample 1: Hello World
Source: HelloController.java
Simple "Hello World" example of a REST API Controller. Verifies Custom REST APIs sample was deployed
and is responding to requests

#### Request
 Request Method: GET
 Request URL: https://[server]:10111/grc/ext/sample1/hello
 
Response will be a plain text message: "Hello [user's first name]"

### Sample 2: Create Objects
Source: NewObjectController.java

#### Request
 Request Method: POST
 Request URL: https://[server]:10111/grc/ext/sample2/createObject
 Request Header: Content-Type = application/json
 Request Body:
 {
 	"name":"MyObject",
 	"description":"My Object Description",
 	"type":"SOXBusEntity"
 }
 
#### Response
 Response Body will be a JSON structure:
 {
   "id":123,
   "name":"MyObject",
   "description":"My Object description",
   "type":"SOXBusEntity"
 } 


### Sample 3: Exception Handling
Source: ErrorHandlingController.java
Example of how Error Handling can be done in Spring-Rest. Custom REST API can have the desired 
responses when an exception is thrown (either an error or validation business logic). This 
illustrates what happens with an unhandled as well as handled exception thrown by the Controller. 


#### Unhandled use-case:
 Request Method: GET
 Request URL: https://[server]:10111/grc/ext/sample3/rawException
 
 Response code: 500
 Response message: a partial stack trace of the Exception thrown
 
#### Handled use-case:
 Request Method: GET
 Request URL: https://[server]:10111/grc/ext/sample3/handledException
 
 Response code: 404
 Response message: User Not Found
 