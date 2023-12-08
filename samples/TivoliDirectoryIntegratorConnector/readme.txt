Licensed Materials - Property of IBM

OpenPages GRC Platform (PID: 5725-D51)

(c) Copyright IBM Corporation 2013 - 2020. All Rights Reserved.

US Government Users Restricted Rights- Use, duplication or
disclosure restricted by GSA ADP Schedule Contract with IBM Corp.

0. Introduction

   IBM Tivoli Directory Integrator (TDI) is an integration framework and graphical development 
   environment to build and test solutions. Its Web-based Administration and Monitoring Console 
   for management (AMC) provides connectors for many common protocols and systems. TDI provides 
   a plug-in architecture for making custom connectors/adapters and allows for data transformation; 
   mapping data from multiple systems in a defined flow called an "Assembly Line".

   This sample creates a basic Assembly Line with an OpenPages connector and uses the OpenPages 
   REST API. The sample will retrieve LossEvents from OpenPages based on a Query that is written 
   using the Query Service syntax. The LossEvents are then mapped to a database table called 
   "Losses" that represents an external system.

   For the Losses table, you must have the ability to add a new user and schema to a target Database
   instance.

   Note, IBM Tivoli Directory Integrator 7.1.1 (or higher) is required to run the sample. 
   To learn more about Tivoli Directory Integrator, refer to the following websites:
      http://www-03.ibm.com/software/products/us/en/directory-integrator/
      http://www.tdi-users.org/twiki/bin/view/Integrator/WebHome

I. Setup

   1. Start Tivoli Directory Integrator:
      For example, run C:\Program Files\IBM\TDI\V7.1.1\ibmditk.bat

   2. Import data:
      Select File menu > Import.
      Choose IBM Tivoli Directory Integrator > Configuration.
      Browse to samples/TivoliDirectoryIntegratorConnector/OPLossesDemo.xml
      Select New Project from the project menu.
      Click Finish.

   3. Configure AssemblyLines for your environment details:
      Open AssemblyLines > ExportLosses.
      Select LossesOpenPages.
      On the Connections tab, change the host, port, username, and password settings for your OpenPages
      enviroment.

      *Note: TDI 7.1.1 could not generate correct authentication header for long username/password. 
             As a result, please use short username/password in this sample (e.g. test/test).

      Save your changes.
      You can test the connection by pressing the Connect button on the Input Map tab.
      Click Next to iterate over any LossEvents in your system.

   4. Setting up the target database

      Note, this illustrates sending data from OpenPages through TDI. The target database is only an 
      illustration using TDI's standard JDBC connector.

      As needed, create a new user and a new schema for the user in the target database. Contact your 
      system DBA for assistance.

      Example on Oracle 11G:
      Perform the following commands as sysdba.

      --create tablespace vision datafile 'C:\openpages_data\repository\database112_se_x64\oradata\OP\vision.dbf' size 256 M reuse autoextend on next 128 M maxsize 512 M;
      create temporary tablespace vision_temp tempfile 'C:\openpages_data\repository\database112_se_x64\oradata\OP\vision_tmp.dbf' size 256 M reuse autoextend on next 128 M maxsize 512 M extent management local uniform size 1M;

      --create new user 
      create user vision identified by vision temporary tablespace vision_TEMP (default tablespace vision); 

      -- grant rights to the user --
      grant connect,resource to vision;
      grant create table to vision;


      Create the table in the target database using the provided db-script as new vision user.

      Example with Oracle sqlplus from the TivoliDirectoryIntegratorConnector directory:
         sqlplus vision/vision@<SID> @db-scripts/create-losses-table.sql

      where
      <SID> is your database instance

   5. Configure JDBC Connector
      Return to TDI.
      Open AssemblyLines > ExportLosses.
      Under Data Flow, select LossesJDBCConnector.
      On the Connections tab, change the connection parameters for your database environment.
      Save your changes.

II. Running the demo

   In TDI, in the ExportLosses assembly line, click Run in Console.
   Note, the OpenPages services must be started and running.

   Result: New rows of Loss events are added to the LOSSES table in the external schema.


