-- Licensed to the Apache Software Foundation (ASF) under one or more
-- contributor license agreements.  See the NOTICE file distributed with
-- this work for additional information regarding copyright ownership.
-- The ASF licenses this file to You under the Apache License, Version 2.0
-- (the "License"); you may not use this file except in compliance with
-- the License.  You may obtain a copy of the License at
--
--     http://www.apache.org/licenses/LICENSE-2.0
--
-- Unless required by applicable law or agreed to in writing, software
-- distributed under the License is distributed on an "AS IS" BASIS,
-- WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
-- See the License for the specific language governing permissions and
-- limitations under the License.

-----------------------------------------------------------------------------
-- OJB_HL_SEQ
-----------------------------------------------------------------------------

CREATE TABLE OJB_HL_SEQ
(
    TABLENAME VARCHAR(175) NOT NULL,
    FIELDNAME VARCHAR(70) NOT NULL,
    MAX_KEY INTEGER,
    GRAB_SIZE INTEGER,
    VERSION INTEGER,
    PRIMARY KEY(TABLENAME,FIELDNAME));

-----------------------------------------------------------------------------
-- OJB_LOCKENTRY
-----------------------------------------------------------------------------

CREATE TABLE OJB_LOCKENTRY
(
    OID_ VARCHAR(250) NOT NULL,
    TX_ID VARCHAR(50) NOT NULL,
    TIMESTAMP_ TIMESTAMP,
    ISOLATIONLEVEL INTEGER,
    LOCKTYPE INTEGER,
    PRIMARY KEY(OID_,TX_ID));

-----------------------------------------------------------------------------
-- OJB_NRM
-----------------------------------------------------------------------------

CREATE TABLE OJB_NRM
(
    NAME VARCHAR(250) NOT NULL,
    OID_ BLOB,
    PRIMARY KEY(NAME));

-----------------------------------------------------------------------------
-- OJB_DLIST
-----------------------------------------------------------------------------

CREATE TABLE OJB_DLIST
(
    ID INTEGER NOT NULL,
    SIZE_ INTEGER,
    PRIMARY KEY(ID));

-----------------------------------------------------------------------------
-- OJB_DLIST_ENTRIES
-----------------------------------------------------------------------------

CREATE TABLE OJB_DLIST_ENTRIES
(
    ID INTEGER NOT NULL,
    DLIST_ID INTEGER,
    POSITION_ INTEGER,
    OID_ BLOB,
    PRIMARY KEY(ID));

-----------------------------------------------------------------------------
-- OJB_DSET
-----------------------------------------------------------------------------

CREATE TABLE OJB_DSET
(
    ID INTEGER NOT NULL,
    SIZE_ INTEGER,
    PRIMARY KEY(ID));

-----------------------------------------------------------------------------
-- OJB_DSET_ENTRIES
-----------------------------------------------------------------------------

CREATE TABLE OJB_DSET_ENTRIES
(
    ID INTEGER NOT NULL,
    DLIST_ID INTEGER,
    POSITION_ INTEGER,
    OID_ BLOB,
    PRIMARY KEY(ID));

-----------------------------------------------------------------------------
-- OJB_DMAP
-----------------------------------------------------------------------------

CREATE TABLE OJB_DMAP
(
    ID INTEGER NOT NULL,
    SIZE_ INTEGER,
    PRIMARY KEY(ID));

