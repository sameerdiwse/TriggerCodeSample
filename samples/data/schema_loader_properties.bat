echo off
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

echo "Setting global variables.. "

SET SCRIPTS_DIR=%CD%

SET OBJMGR_HOME=C:\OpenPages\bin
SET OPXUserName=OpenPagesAdministrator
SET OPXUserPassword=OpenPagesAdministrator

SET LOADER-DATA=%SCRIPTS_DIR%\loaderdata
SET OBJ_STATUS_FILE=sample-data-load.log
SET OBJ_LOADER_FILE=sample-data-load.log

SET TEMP_VARIABLE=" "
