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
-- SECURITY_PRINCIPAL
-----------------------------------------------------------------------------

CREATE TABLE SECURITY_PRINCIPAL
(
    PRINCIPAL_ID INTEGER NOT NULL,
    CLASSNAME VARCHAR(254) NOT NULL,
    IS_MAPPING_ONLY INTEGER NOT NULL,
    IS_ENABLED INTEGER NOT NULL,
    FULL_PATH VARCHAR(254) NOT NULL,
    CREATION_DATE TIMESTAMP NOT NULL,
    MODIFIED_DATE TIMESTAMP NOT NULL,
    PRIMARY KEY(PRINCIPAL_ID),
    UNIQUE (FULL_PATH));

-----------------------------------------------------------------------------
-- SECURITY_PERMISSION
-----------------------------------------------------------------------------

CREATE TABLE SECURITY_PERMISSION
(
    PERMISSION_ID INTEGER NOT NULL,
    CLASSNAME VARCHAR(254) NOT NULL,
    NAME VARCHAR(254) NOT NULL,
    ACTIONS VARCHAR(254) NOT NULL,
    CREATION_DATE TIMESTAMP NOT NULL,
    MODIFIED_DATE TIMESTAMP NOT NULL,
    PRIMARY KEY(PERMISSION_ID));

-----------------------------------------------------------------------------
-- PRINCIPAL_PERMISSION
-----------------------------------------------------------------------------

CREATE TABLE PRINCIPAL_PERMISSION
(
    PRINCIPAL_ID INTEGER NOT NULL,
    PERMISSION_ID INTEGER NOT NULL,
    PRIMARY KEY(PRINCIPAL_ID,PERMISSION_ID),
    FOREIGN KEY (PERMISSION_ID) REFERENCES SECURITY_PERMISSION (PERMISSION_ID)
        ON DELETE CASCADE 
  ,
    FOREIGN KEY (PRINCIPAL_ID) REFERENCES SECURITY_PRINCIPAL (PRINCIPAL_ID)
        ON DELETE CASCADE 
  );

-----------------------------------------------------------------------------
-- SECURITY_CREDENTIAL
-----------------------------------------------------------------------------

CREATE TABLE SECURITY_CREDENTIAL
(
    CREDENTIAL_ID INTEGER NOT NULL,
    PRINCIPAL_ID INTEGER NOT NULL,
    COLUMN_VALUE VARCHAR(254) NOT NULL,
    TYPE SMALLINT NOT NULL,
    CLASSNAME VARCHAR(254),
    UPDATE_REQUIRED INTEGER NOT NULL,
    IS_ENCODED INTEGER NOT NULL,
    IS_ENABLED INTEGER NOT NULL,
    AUTH_FAILURES SMALLINT NOT NULL,
    IS_EXPIRED INTEGER NOT NULL,
    CREATION_DATE TIMESTAMP NOT NULL,
    MODIFIED_DATE TIMESTAMP NOT NULL,
    PREV_AUTH_DATE TIMESTAMP,
    LAST_AUTH_DATE TIMESTAMP,
    EXPIRATION_DATE DATE,
    PRIMARY KEY(CREDENTIAL_ID),
    FOREIGN KEY (PRINCIPAL_ID) REFERENCES SECURITY_PRINCIPAL (PRINCIPAL_ID)
        ON DELETE CASCADE 
  );

-----------------------------------------------------------------------------
-- SSO_SITE
-----------------------------------------------------------------------------

CREATE TABLE SSO_SITE
(
    SITE_ID INTEGER NOT NULL,
    NAME VARCHAR(254) NOT NULL,
    URL VARCHAR(254) NOT NULL,
    ALLOW_USER_SET INTEGER default 0,
    REQUIRES_CERTIFICATE INTEGER default 0,
    CHALLENGE_RESPONSE_AUTH INTEGER default 0,
    FORM_AUTH INTEGER default 0,
    FORM_USER_FIELD VARCHAR(128),
    FORM_PWD_FIELD VARCHAR(128),
    REALM VARCHAR(128),
    
    PRIMARY KEY(SITE_ID),
    UNIQUE (URL));
-----------------------------------------------------------------------------
-- SSO_COOKIE
-----------------------------------------------------------------------------
    
CREATE TABLE SSO_COOKIE
(
    COOKIE_ID INTEGER NOT NULL,
    COOKIE VARCHAR(254) NOT NULL, 
    CREATE_DATE TIMESTAMP NOT NULL,
    PRIMARY KEY(COOKIE_ID)
 );   

-----------------------------------------------------------------------------
-- SSO_SITE_TO_PRINCIPALS
-----------------------------------------------------------------------------

CREATE TABLE SSO_SITE_TO_PRINCIPALS
(
    SITE_ID INTEGER NOT NULL,
    PRINCIPAL_ID INTEGER NOT NULL,
    PRIMARY KEY(SITE_ID,PRINCIPAL_ID),
    FOREIGN KEY (SITE_ID) REFERENCES SSO_SITE (SITE_ID)
        ON DELETE CASCADE 
  ,
    FOREIGN KEY (PRINCIPAL_ID) REFERENCES SECURITY_PRINCIPAL (PRINCIPAL_ID)
        ON DELETE CASCADE 
  );

-----------------------------------------------------------------------------
-- SSO_PRINCIPAL_TO_REMOTE
-----------------------------------------------------------------------------

CREATE TABLE SSO_PRINCIPAL_TO_REMOTE
(
    PRINCIPAL_ID INTEGER NOT NULL,
    REMOTE_PRINCIPAL_ID INTEGER NOT NULL,
    PRIMARY KEY(PRINCIPAL_ID,REMOTE_PRINCIPAL_ID),
    FOREIGN KEY (PRINCIPAL_ID) REFERENCES SECURITY_PRINCIPAL (PRINCIPAL_ID)
        ON DELETE CASCADE 
  ,
    FOREIGN KEY (REMOTE_PRINCIPAL_ID) REFERENCES SECURITY_PRINCIPAL (PRINCIPAL_ID)
        ON DELETE CASCADE 
  );

-----------------------------------------------------------------------------
-- SSO_SITE_TO_REMOTE
-----------------------------------------------------------------------------

CREATE TABLE SSO_SITE_TO_REMOTE
(
    SITE_ID INTEGER NOT NULL,
    PRINCIPAL_ID INTEGER NOT NULL,
    PRIMARY KEY(SITE_ID,PRINCIPAL_ID),
    FOREIGN KEY (SITE_ID) REFERENCES SSO_SITE (SITE_ID)
        ON DELETE CASCADE 
  ,
    FOREIGN KEY (PRINCIPAL_ID) REFERENCES SECURITY_PRINCIPAL (PRINCIPAL_ID)
        ON DELETE CASCADE 
  );

----------------------------------------------------------------------------
-- SSO_COOKIE_TO_REMOTE
-----------------------------------------------------------------------------  
CREATE TABLE SSO_COOKIE_TO_REMOTE
(
    COOKIE_ID INTEGER NOT NULL,
    REMOTE_PRINCIPAL_ID INTEGER NOT NULL,
    PRIMARY KEY(COOKIE_ID,REMOTE_PRINCIPAL_ID),
    FOREIGN KEY (COOKIE_ID) REFERENCES SSO_COOKIE (COOKIE_ID)
        ON DELETE CASCADE 
  ,
    FOREIGN KEY (REMOTE_PRINCIPAL_ID) REFERENCES SECURITY_PRINCIPAL (PRINCIPAL_ID)
        ON DELETE CASCADE 
  );

-----------------------------------------------------------------------------
-- SECURITY_USER_ROLE
-----------------------------------------------------------------------------

CREATE TABLE SECURITY_USER_ROLE
(
    USER_ID INTEGER NOT NULL,
    ROLE_ID INTEGER NOT NULL,
    PRIMARY KEY(USER_ID,ROLE_ID),
    FOREIGN KEY (ROLE_ID) REFERENCES SECURITY_PRINCIPAL (PRINCIPAL_ID)
        ON DELETE CASCADE 
  ,
    FOREIGN KEY (USER_ID) REFERENCES SECURITY_PRINCIPAL (PRINCIPAL_ID)
        ON DELETE CASCADE 
  );

-----------------------------------------------------------------------------
-- SECURITY_USER_GROUP
-----------------------------------------------------------------------------

CREATE TABLE SECURITY_USER_GROUP
(
    USER_ID INTEGER NOT NULL,
    GROUP_ID INTEGER NOT NULL,
    PRIMARY KEY(USER_ID,GROUP_ID),
    FOREIGN KEY (GROUP_ID) REFERENCES SECURITY_PRINCIPAL (PRINCIPAL_ID)
        ON DELETE CASCADE 
  ,
    FOREIGN KEY (USER_ID) REFERENCES SECURITY_PRINCIPAL (PRINCIPAL_ID)
        ON DELETE CASCADE 
  );

-----------------------------------------------------------------------------
-- SECURITY_GROUP_ROLE
-----------------------------------------------------------------------------

CREATE TABLE SECURITY_GROUP_ROLE
(
    GROUP_ID INTEGER NOT NULL,
    ROLE_ID INTEGER NOT NULL,
    PRIMARY KEY(GROUP_ID,ROLE_ID),
    FOREIGN KEY (GROUP_ID) REFERENCES SECURITY_PRINCIPAL (PRINCIPAL_ID)
        ON DELETE CASCADE 
  ,
    FOREIGN KEY (ROLE_ID) REFERENCES SECURITY_PRINCIPAL (PRINCIPAL_ID)
        ON DELETE CASCADE 
  );

