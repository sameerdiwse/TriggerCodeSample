-------------------------------------------------------------------------------
-- Licensed Materials - Property of IBM
--
--
-- OpenPages GRC Platform (PID: 5725-D51)
--
--  (c) Copyright IBM Corporation 2013 - 2020. All Rights Reserved.
--
-- US Government Users Restricted Rights- Use, duplication or
-- disclosure restricted by GSA ADP Schedule Contract with IBM Corp.
--
-------------------------------------------------------------------------------
SET SERVEROUTPUT ON SIZE 100000

@AddToRegistryKey '/OpenPages/Applications/GRCM/ObjectTypes' ',KeyRiskIndicator,KeyRiskIndicatorValue,LossEvent,LossImpact,SOXProcess,SOXRisk'

\
commit;
