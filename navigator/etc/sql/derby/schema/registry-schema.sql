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
-- PORTLET_DEFINITION
-----------------------------------------------------------------------------

CREATE TABLE PORTLET_DEFINITION
(
    ID INTEGER NOT NULL,
    NAME VARCHAR(80),
    CLASS_NAME VARCHAR(255),
    APPLICATION_ID INTEGER NOT NULL,
    PORTLET_IDENTIFIER VARCHAR(80),
    EXPIRATION_CACHE VARCHAR(30),
    RESOURCE_BUNDLE VARCHAR(255),
    PREFERENCE_VALIDATOR VARCHAR(255),
    SECURITY_REF VARCHAR(40),    
    PRIMARY KEY(ID));

-----------------------------------------------------------------------------
-- PORTLET_APPLICATION
-----------------------------------------------------------------------------

CREATE TABLE PORTLET_APPLICATION
(
    APPLICATION_ID INTEGER NOT NULL,
    APP_NAME VARCHAR(80) NOT NULL,
    APP_IDENTIFIER VARCHAR(80),
    VERSION VARCHAR(80),
    APP_TYPE INTEGER,
    CHECKSUM VARCHAR(80),
    DESCRIPTION VARCHAR(80),
    WEB_APP_ID INTEGER NOT NULL,
    SECURITY_REF VARCHAR(40),        
    PRIMARY KEY(APPLICATION_ID),
    UNIQUE (APP_NAME));

-----------------------------------------------------------------------------
-- WEB_APPLICATION
-----------------------------------------------------------------------------

CREATE TABLE WEB_APPLICATION
(
    ID INTEGER NOT NULL,
    CONTEXT_ROOT VARCHAR(255) NOT NULL,
    PRIMARY KEY(ID));

-----------------------------------------------------------------------------
-- PA_METADATA_FIELDS
-----------------------------------------------------------------------------

CREATE TABLE PA_METADATA_FIELDS
(
    ID INTEGER NOT NULL,
    OBJECT_ID INTEGER NOT NULL,
    COLUMN_VALUE LONG VARCHAR NOT NULL,
    NAME VARCHAR(100) NOT NULL,
    LOCALE_STRING VARCHAR(50) NOT NULL,
    PRIMARY KEY(ID),
    FOREIGN KEY (OBJECT_ID) REFERENCES PORTLET_APPLICATION (APPLICATION_ID)
        ON DELETE CASCADE 
  );

-----------------------------------------------------------------------------
-- PD_METADATA_FIELDS
-----------------------------------------------------------------------------

CREATE TABLE PD_METADATA_FIELDS
(
    ID INTEGER NOT NULL,
    OBJECT_ID INTEGER NOT NULL,
    COLUMN_VALUE LONG VARCHAR NOT NULL,
    NAME VARCHAR(100) NOT NULL,
    LOCALE_STRING VARCHAR(50) NOT NULL,
    PRIMARY KEY(ID),
    FOREIGN KEY (OBJECT_ID) REFERENCES PORTLET_DEFINITION (ID)
        ON DELETE CASCADE 
  );

-----------------------------------------------------------------------------
-- LANGUAGE
-----------------------------------------------------------------------------

CREATE TABLE LANGUAGE
(
    ID INTEGER NOT NULL,
    PORTLET_ID INTEGER NOT NULL,
    TITLE VARCHAR(100),
    SHORT_TITLE VARCHAR(100),
    LOCALE_STRING VARCHAR(50) NOT NULL,
    KEYWORDS LONG VARCHAR,
    PRIMARY KEY(ID));

-----------------------------------------------------------------------------
-- PORTLET_CONTENT_TYPE
-----------------------------------------------------------------------------

CREATE TABLE PORTLET_CONTENT_TYPE
(
    CONTENT_TYPE_ID INTEGER NOT NULL,
    PORTLET_ID INTEGER NOT NULL,
    CONTENT_TYPE VARCHAR(30) NOT NULL,
    MODES LONG VARCHAR,
    PRIMARY KEY(CONTENT_TYPE_ID));

-----------------------------------------------------------------------------
-- PARAMETER
-----------------------------------------------------------------------------

CREATE TABLE PARAMETER
(
    PARAMETER_ID INTEGER NOT NULL,
    PARENT_ID INTEGER NOT NULL,
    CLASS_NAME VARCHAR(255) NOT NULL,
    NAME VARCHAR(80) NOT NULL,
    PARAMETER_VALUE LONG VARCHAR NOT NULL,
    PRIMARY KEY(PARAMETER_ID));

-----------------------------------------------------------------------------
-- PORTLET_ENTITY
-----------------------------------------------------------------------------

CREATE TABLE PORTLET_ENTITY
(
    PEID INTEGER NOT NULL,
    ID VARCHAR(255) NOT NULL,
    APP_NAME VARCHAR(255) NOT NULL,
    PORTLET_NAME VARCHAR(255) NOT NULL,
    PRIMARY KEY(PEID),
    UNIQUE (ID));

-----------------------------------------------------------------------------
-- SECURITY_ROLE_REFERENCE
-----------------------------------------------------------------------------

CREATE TABLE SECURITY_ROLE_REFERENCE
(
    ID INTEGER NOT NULL,
    PORTLET_DEFINITION_ID INTEGER NOT NULL,
    ROLE_NAME VARCHAR(150) NOT NULL,
    ROLE_LINK VARCHAR(150),
    PRIMARY KEY(ID));

-----------------------------------------------------------------------------
-- SECURITY_ROLE
-----------------------------------------------------------------------------

CREATE TABLE SECURITY_ROLE
(
    ID INTEGER NOT NULL,
    WEB_APPLICATION_ID INTEGER NOT NULL,
    ROLE_NAME VARCHAR(150) NOT NULL,
    DESCRIPTION VARCHAR(150),
    PRIMARY KEY(ID));

-----------------------------------------------------------------------------
-- USER_ATTRIBUTE_REF
-----------------------------------------------------------------------------

CREATE TABLE USER_ATTRIBUTE_REF
(
    ID INTEGER NOT NULL,
    APPLICATION_ID INTEGER NOT NULL,
    NAME VARCHAR(150),
    NAME_LINK VARCHAR(150),
    PRIMARY KEY(ID),
    FOREIGN KEY (APPLICATION_ID) REFERENCES PORTLET_APPLICATION (APPLICATION_ID)
        ON DELETE CASCADE 
  );

-----------------------------------------------------------------------------
-- USER_ATTRIBUTE
-----------------------------------------------------------------------------

CREATE TABLE USER_ATTRIBUTE
(
    ID INTEGER NOT NULL,
    APPLICATION_ID INTEGER NOT NULL,
    NAME VARCHAR(150),
    DESCRIPTION VARCHAR(150),
    PRIMARY KEY(ID),
    FOREIGN KEY (APPLICATION_ID) REFERENCES PORTLET_APPLICATION (APPLICATION_ID)
        ON DELETE CASCADE 
  );

-----------------------------------------------------------------------------
-- JETSPEED_SERVICE
-----------------------------------------------------------------------------

CREATE TABLE JETSPEED_SERVICE
(
    ID INTEGER NOT NULL,
    APPLICATION_ID INTEGER NOT NULL,
    NAME VARCHAR(150),
    PRIMARY KEY(ID));

-----------------------------------------------------------------------------
-- LOCALIZED_DESCRIPTION
-----------------------------------------------------------------------------

CREATE TABLE LOCALIZED_DESCRIPTION
(
    ID INTEGER NOT NULL,
    OBJECT_ID INTEGER NOT NULL,
    CLASS_NAME VARCHAR(255) NOT NULL,
    DESCRIPTION LONG VARCHAR NOT NULL,
    LOCALE_STRING VARCHAR(50) NOT NULL,
    PRIMARY KEY(ID));

-----------------------------------------------------------------------------
-- LOCALIZED_DISPLAY_NAME
-----------------------------------------------------------------------------

CREATE TABLE LOCALIZED_DISPLAY_NAME
(
    ID INTEGER NOT NULL,
    OBJECT_ID INTEGER NOT NULL,
    CLASS_NAME VARCHAR(255),
    DISPLAY_NAME LONG VARCHAR NOT NULL,
    LOCALE_STRING VARCHAR(50) NOT NULL,
    PRIMARY KEY(ID));

-----------------------------------------------------------------------------
-- CUSTOM_PORTLET_MODE
-----------------------------------------------------------------------------

CREATE TABLE CUSTOM_PORTLET_MODE
(
    ID INTEGER NOT NULL,
    APPLICATION_ID INTEGER NOT NULL,
    CUSTOM_NAME VARCHAR(150) NOT NULL,
    MAPPED_NAME VARCHAR(150),
    DESCRIPTION LONG VARCHAR,
    PRIMARY KEY(ID),
    FOREIGN KEY (APPLICATION_ID) REFERENCES PORTLET_APPLICATION (APPLICATION_ID)
        ON DELETE CASCADE 
  );

-----------------------------------------------------------------------------
-- CUSTOM_WINDOW_STATE
-----------------------------------------------------------------------------

CREATE TABLE CUSTOM_WINDOW_STATE
(
    ID INTEGER NOT NULL,
    APPLICATION_ID INTEGER NOT NULL,
    CUSTOM_NAME VARCHAR(150) NOT NULL,
    MAPPED_NAME VARCHAR(150),
    DESCRIPTION LONG VARCHAR,
    PRIMARY KEY(ID),
    FOREIGN KEY (APPLICATION_ID) REFERENCES PORTLET_APPLICATION (APPLICATION_ID)
        ON DELETE CASCADE 
  );
