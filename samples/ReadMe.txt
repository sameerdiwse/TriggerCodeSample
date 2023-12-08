Licensed Materials - Property of IBM

OpenPages GRC Platform (PID: 5725-D51)

(c) Copyright IBM Corporation 2013 - 2020. All Rights Reserved.

US Government Users Restricted Rights- Use, duplication or
disclosure restricted by GSA ADP Schedule Contract with IBM Corp.

I. Loading prerequisite data for samples

   The API samples are all based on a sample dataset that models a real world system. The data 
   set is a subset of the ORM standard objects 
   and fields, and includes a FastMap template with sample data used in the samples and demos.

   Warning: Loading the schema will alter the metadata and configuration of your system. These 
   operations cannot be undone. You should create a backup of your system using OPBackup before 
   proceeding. For more information on running the OPBackup utility, refer to the OpenPages 
   Administrator's Guide. You may restore your system to the original state using OPRestore.

   Disclaimer: This object model, schema and sample dataset is provided as a sample for developers,
   it is not intended for other uses. If you already have a custom schema loaded into OpenPages, 
   you may encounter incompatibility issues. Please consult your IBM OpenPages Services 
   representative before proceeding.

   To configure the schema loader:
      1. Navigate to your samples directory
      2. Modify data\schema_loader_properties.bat
      3. Change OBJMGR_HOME=C:\OpenPages\bin to the location of OpenPages\bin directory in your 
         system
      4. Change the default OPXUserPassword, if required

   To start the load (the OpenPages server must be started and running):
      1. Open a command prompt and change directory to the samples\data directory
      2. Run the schema loader command
         > sample-schema-loader.bat

   Wait until all necessary loads have completed. 
   Once the schema loader has finished, restart the OpenPages services.
   When the services have finished starting up, log in as OpenPagesAdministrator.

   Create a new Object Profile:
      1. Go to Administration menu > Profiles
      2. Click Add...
      3. Provide a new name (e.g. Samples Profile).
      4. Click Create.
      5. Include the following Object Types
      •   SOXBusEntity
      •   SOXProcess
      •   SOXRisk
      •   KeyRiskIndicator
      •   KeyRiskIndicatorValue
      •   LossEvent
      •   LossImpact
      •   SOXIssue
      •   SOXTask
      •   SOXDocument
      •   SOXExternalDocument
      6. Associate the OpenPagesAdministrator user to this new profile.

   Load the samples\data\API-Sample-FastMap.xls
      1. Go to Reporting menu > FastMap > FastMap Import
      2. Browse to the samples\data\API-Sample-FastMap.xls and import the file.
      3. If there are no validation errors, complete the import.

   This process will load 827 new objects.


II. Samples

   Each sample demonstrates portions of the OpenPages Risk API. 
   Sample directories include source files for compiling/deploying and running the sample code
   in an OpenPages system with the sample schema and dataset loaded. 
   
   Provided with each sample is a readme.txt file, which describes what the sample demonstrates.
   In general, each sample is an independent project.


III. In general, you must add the following JAR files as referenced libraries to the classpath 
     of any sample project.

   • api.jar
   • query-service.jar
   • query-syntax.jar
   
