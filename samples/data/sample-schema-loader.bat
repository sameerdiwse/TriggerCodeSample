echo off;
rem ***************************************************************************
rem Licensed Materials - Property of IBM
rem
rem
rem OpenPages GRC Platform (PID: 5725-D51)
rem
rem (c) Copyright IBM Corporation 2013 - 2020. All Rights Reserved.
rem
rem US Government Users Restricted Rights- Use, duplication or
rem disclosure restricted by GSA ADP Schedule Contract with IBM Corp.
rem
rem ***************************************************************************

call schema_loader_properties

cd /d %OBJMGR_HOME%

echo "-----------------------------------------------------------"
echo "Starting Loading Modules"
echo "-----------------------------------------------------------"

echo "Loading Objects"

SET TEMP_VARIABLE="OK"
call ObjectManager l c %OPXUserName% %OPXUserPassword% %LOADER-DATA% API-Sample-Objects
if %ERRORLEVEL% NEQ 0 SET TEMP_VARIABLE="OBJECTS"
if %TEMP_VARIABLE% EQU "OBJECTS" echo Failed to load Objects >> %OBJ_LOADER_FILE%
if %TEMP_VARIABLE% EQU "OBJECTS" echo OBJMGRLOADINGSTATUS=FAIL > %OBJ_STATUS_FILE%
if %TEMP_VARIABLE% NEQ "OBJECTS" echo ...Loaded Objects successfully >> %OBJ_LOADER_FILE%

echo "Loading Object Strings"
SET TEMP_VARIABLE="OK"
call ObjectManager l c %OPXUserName% %OPXUserPassword% %LOADER-DATA% API-Sample-Object-Strings-en_US
if %ERRORLEVEL% NEQ 0 SET TEMP_VARIABLE="STRINGS"
if %TEMP_VARIABLE% EQU "STRINGS" echo Failed to load Objects Strings >> %OBJ_LOADER_FILE%
if %TEMP_VARIABLE% EQU "STRINGS" echo OBJMGRLOADINGSTATUS=FAIL > %OBJ_STATUS_FILE%
if %TEMP_VARIABLE% NEQ "STRINGS" echo ...Loaded Object Strings successfully >> %OBJ_LOADER_FILE%

echo "Loading Object Registry Entries"
SET TEMP_VARIABLE="OK"
call ObjectManager l c %OPXUserName% %OPXUserPassword% %LOADER-DATA% API-Sample-Registry-Entries
if %ERRORLEVEL% NEQ 0 SET TEMP_VARIABLE="REGISTRYE"
if %TEMP_VARIABLE% EQU "REGISTRYE" echo Failed to load Object Registry Entries >> %OBJ_LOADER_FILE%
if %TEMP_VARIABLE% EQU "REGISTRYE" echo OBJMGRLOADINGSTATUS=FAIL > %OBJ_STATUS_FILE%
if %TEMP_VARIABLE% NEQ "REGISTRYE" echo ...Loaded Registry Entries successfully >> %OBJ_LOADER_FILE%

echo "Loading Relationships"
SET TEMP_VARIABLE="OK"
call ObjectManager l c %OPXUserName% %OPXUserPassword% %LOADER-DATA% API-Sample-Relationships
if %ERRORLEVEL% NEQ 0 SET TEMP_VARIABLE="RELATIONSHIPS"
if %TEMP_VARIABLE% EQU "RELATIONSHIPS" echo Failed to load Relationships >> %OBJ_LOADER_FILE%
if %TEMP_VARIABLE% EQU "RELATIONSHIPS" echo OBJMGRLOADINGSTATUS=FAIL > %OBJ_STATUS_FILE%
if %TEMP_VARIABLE% NEQ "RELATIONSHIPS" echo ...Loaded Relationships successfully >> %OBJ_LOADER_FILE%

echo "Loading Schema"
SET TEMP_VARIABLE="OK"
call ObjectManager l c %OPXUserName% %OPXUserPassword% %LOADER-DATA% API-Sample-Schema
if %ERRORLEVEL% NEQ 0 SET TEMP_VARIABLE="SCHEMA1"
if %TEMP_VARIABLE% EQU "SCHEMA1" echo Failed to load Master Schema and Profile >> %OBJ_LOADER_FILE%
if %TEMP_VARIABLE% EQU "SCHEMA1" echo OBJMGRLOADINGSTATUS=FAIL > %OBJ_STATUS_FILE%
if %TEMP_VARIABLE% NEQ "SCHEMA1" echo ...Loaded Schema successfully >> %OBJ_LOADER_FILE%

echo "Loading Users"
SET TEMP_VARIABLE="OK"
call ObjectManager l c %OPXUserName% %OPXUserPassword% %LOADER-DATA% API-Sample-User
if %ERRORLEVEL% NEQ 0 SET TEMP_VARIABLE="PROFILESROLETEMPLATES"
if %TEMP_VARIABLE% EQU "PROFILESROLETEMPLATES" echo Failed to load Users >> %OBJ_LOADER_FILE%
if %TEMP_VARIABLE% EQU "PROFILESROLETEMPLATES" echo OBJMGRLOADINGSTATUS=FAIL > %OBJ_STATUS_FILE%
if %TEMP_VARIABLE% NEQ "PROFILESROLETEMPLATES" echo ...Loaded Users successfully >> %OBJ_LOADER_FILE%

echo "Loading Profile Role Templates"
SET TEMP_VARIABLE="OK"
call ObjectManager l c %OPXUserName% %OPXUserPassword% %LOADER-DATA% API-Sample-RoleTemplate
if %ERRORLEVEL% NEQ 0 SET TEMP_VARIABLE="PROFILESROLETEMPLATES"
if %TEMP_VARIABLE% EQU "PROFILESROLETEMPLATES" echo Failed to load Profile Role Templates >> %OBJ_LOADER_FILE%
if %TEMP_VARIABLE% EQU "PROFILESROLETEMPLATES" echo OBJMGRLOADINGSTATUS=FAIL > %OBJ_STATUS_FILE%
if %TEMP_VARIABLE% NEQ "PROFILESROLETEMPLATES" echo ...Loaded Profile Role Templates successfully >> %OBJ_LOADER_FILE%

echo "Finished Loading Modules"
echo "-----------------------------------------------------------"