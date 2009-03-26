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
-- MEDIA_TYPE
-----------------------------------------------------------------------------

CREATE TABLE MEDIA_TYPE
(
    MEDIATYPE_ID INTEGER NOT NULL,
    NAME VARCHAR(80) NOT NULL,
    CHARACTER_SET VARCHAR(40),
    TITLE VARCHAR(80),
    DESCRIPTION LONG VARCHAR,
    PRIMARY KEY(MEDIATYPE_ID));

-----------------------------------------------------------------------------
-- CLIENT
-----------------------------------------------------------------------------

CREATE TABLE CLIENT
(
    CLIENT_ID INTEGER NOT NULL,
    EVAL_ORDER INTEGER NOT NULL,
    NAME VARCHAR(80) NOT NULL,
    USER_AGENT_PATTERN VARCHAR(128),
    MANUFACTURER VARCHAR(80),
    MODEL VARCHAR(80),
    VERSION VARCHAR(40),
    PREFERRED_MIMETYPE_ID INTEGER NOT NULL,
    PRIMARY KEY(CLIENT_ID));

-----------------------------------------------------------------------------
-- MIMETYPE
-----------------------------------------------------------------------------

CREATE TABLE MIMETYPE
(
    MIMETYPE_ID INTEGER NOT NULL,
    NAME VARCHAR(80) NOT NULL,
    PRIMARY KEY(MIMETYPE_ID));

-----------------------------------------------------------------------------
-- CAPABILITY
-----------------------------------------------------------------------------

CREATE TABLE CAPABILITY
(
    CAPABILITY_ID INTEGER NOT NULL,
    CAPABILITY VARCHAR(80) NOT NULL,
    PRIMARY KEY(CAPABILITY_ID));

-----------------------------------------------------------------------------
-- CLIENT_TO_CAPABILITY
-----------------------------------------------------------------------------

CREATE TABLE CLIENT_TO_CAPABILITY
(
    CLIENT_ID INTEGER NOT NULL,
    CAPABILITY_ID INTEGER NOT NULL);

-----------------------------------------------------------------------------
-- CLIENT_TO_MIMETYPE
-----------------------------------------------------------------------------

CREATE TABLE CLIENT_TO_MIMETYPE
(
    CLIENT_ID INTEGER NOT NULL,
    MIMETYPE_ID INTEGER NOT NULL);

-----------------------------------------------------------------------------
-- MEDIATYPE_TO_CAPABILITY
-----------------------------------------------------------------------------

CREATE TABLE MEDIATYPE_TO_CAPABILITY
(
    MEDIATYPE_ID INTEGER NOT NULL,
    CAPABILITY_ID INTEGER NOT NULL);

-----------------------------------------------------------------------------
-- MEDIATYPE_TO_MIMETYPE
-----------------------------------------------------------------------------

CREATE TABLE MEDIATYPE_TO_MIMETYPE
(
    MEDIATYPE_ID INTEGER NOT NULL,
    MIMETYPE_ID INTEGER NOT NULL);

-----------------------------------------------------------------------------
-- PORTLET_STATISTICS
-----------------------------------------------------------------------------

CREATE TABLE PORTLET_STATISTICS
(
    IPADDRESS VARCHAR(80),
    USER_NAME VARCHAR(80),
    TIME_STAMP TIMESTAMP,
    PAGE VARCHAR(80),
    PORTLET VARCHAR(255),
    STATUS INTEGER,
    ELAPSED_TIME BIGINT);

-----------------------------------------------------------------------------
-- PAGE_STATISTICS
-----------------------------------------------------------------------------

CREATE TABLE PAGE_STATISTICS
(
    IPADDRESS VARCHAR(80),
    USER_NAME VARCHAR(80),
    TIME_STAMP TIMESTAMP,
    PAGE VARCHAR(80),
    STATUS INTEGER,
    ELAPSED_TIME BIGINT);

-----------------------------------------------------------------------------
-- USER_STATISTICS
-----------------------------------------------------------------------------

CREATE TABLE USER_STATISTICS
(
    IPADDRESS VARCHAR(80),
    USER_NAME VARCHAR(80),
    TIME_STAMP TIMESTAMP,
    STATUS INTEGER,
    ELAPSED_TIME BIGINT);

-----------------------------------------------------------------------------
-- ADMIN_ACTIVITY
-----------------------------------------------------------------------------

CREATE TABLE ADMIN_ACTIVITY
(
  ACTIVITY VARCHAR(40),
  CATEGORY VARCHAR(40),
  ADMIN VARCHAR(80),
  USER_NAME VARCHAR(80),
  TIME_STAMP TIMESTAMP,
  IPADDRESS VARCHAR(80),
  ATTR_NAME VARCHAR(40),
  ATTR_VALUE_BEFORE VARCHAR(80),
  ATTR_VALUE_AFTER VARCHAR(80),
  DESCRIPTION VARCHAR(128));

-----------------------------------------------------------------------------
-- USER_ACTIVITY
-----------------------------------------------------------------------------

CREATE TABLE USER_ACTIVITY
(
  ACTIVITY VARCHAR(40),
  CATEGORY VARCHAR(40),
  USER_NAME VARCHAR(80),
  TIME_STAMP TIMESTAMP,
  IPADDRESS VARCHAR(80),
  ATTR_NAME VARCHAR(40),
  ATTR_VALUE_BEFORE VARCHAR(80),
  ATTR_VALUE_AFTER VARCHAR(80),
  DESCRIPTION VARCHAR(128));
