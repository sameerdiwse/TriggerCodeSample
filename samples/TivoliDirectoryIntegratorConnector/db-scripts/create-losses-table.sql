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
--       
--
-- creates a single table to store Losses from TDI OPLossesDemo assembly line
-- run as user vision/vision
-- requires a schema in your database instance called "VISION" and a user called vision.
-- user vision needs these grants: connect, resource, create table

CREATE TABLE "VISION"."LOSSES"
  (
    "LOSS_ID"     NUMBER,
    "NAME"        VARCHAR2(256 CHAR),
    "WHO"         VARCHAR2(2048 CHAR),
    "WHEN"        DATE,
    "WHAT"        VARCHAR2(4000 CHAR),
    "LOCATION_WHERE" VARCHAR2(4000 CHAR),
    "HOWMUCH"     NUMBER,
    "INSERTED_ON"        DATE,
    PRIMARY KEY ("LOSS_ID")
);