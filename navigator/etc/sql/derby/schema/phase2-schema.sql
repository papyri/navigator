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
-- FOLDER
-----------------------------------------------------------------------------

CREATE TABLE FOLDER
(
    FOLDER_ID INTEGER NOT NULL,
    PARENT_ID INTEGER,
    PATH VARCHAR(240) NOT NULL,
    NAME VARCHAR(80) NOT NULL,
    TITLE VARCHAR(100),
    SHORT_TITLE VARCHAR(40),
    IS_HIDDEN INTEGER NOT NULL,
    SKIN VARCHAR(80),
    DEFAULT_LAYOUT_DECORATOR VARCHAR(80),
    DEFAULT_PORTLET_DECORATOR VARCHAR(80),
    DEFAULT_PAGE_NAME VARCHAR(80),
    SUBSITE VARCHAR(40),
    USER_PRINCIPAL VARCHAR(40),
    ROLE_PRINCIPAL VARCHAR(40),
    GROUP_PRINCIPAL VARCHAR(40),
    MEDIATYPE VARCHAR(15),
    LOCALE VARCHAR(20),
    EXT_ATTR_NAME VARCHAR(15),
    EXT_ATTR_VALUE VARCHAR(40),
    OWNER_PRINCIPAL VARCHAR(40),
    PRIMARY KEY(FOLDER_ID),
    FOREIGN KEY (PARENT_ID) REFERENCES FOLDER (FOLDER_ID)
        ON DELETE CASCADE 
  ,
    UNIQUE (PATH));

CREATE  INDEX IX_FOLDER_1 ON FOLDER (PARENT_ID);
-----------------------------------------------------------------------------
-- FOLDER_METADATA
-----------------------------------------------------------------------------

CREATE TABLE FOLDER_METADATA
(
    METADATA_ID INTEGER NOT NULL,
    FOLDER_ID INTEGER NOT NULL,
    NAME VARCHAR(15) NOT NULL,
    LOCALE VARCHAR(20) NOT NULL,
    VALUE VARCHAR(100) NOT NULL,
    PRIMARY KEY(METADATA_ID),
    FOREIGN KEY (FOLDER_ID) REFERENCES FOLDER (FOLDER_ID)
        ON DELETE CASCADE 
  ,
    UNIQUE (FOLDER_ID, NAME, LOCALE, VALUE));

CREATE  INDEX IX_FOLDER_METADATA_1 ON FOLDER_METADATA (FOLDER_ID);
-----------------------------------------------------------------------------
-- FOLDER_CONSTRAINT
-----------------------------------------------------------------------------

CREATE TABLE FOLDER_CONSTRAINT
(
    CONSTRAINT_ID INTEGER NOT NULL,
    FOLDER_ID INTEGER NOT NULL,
    APPLY_ORDER INTEGER NOT NULL,
    USER_PRINCIPALS_ACL VARCHAR(120),
    ROLE_PRINCIPALS_ACL VARCHAR(120),
    GROUP_PRINCIPALS_ACL VARCHAR(120),
    PERMISSIONS_ACL VARCHAR(120),
    PRIMARY KEY(CONSTRAINT_ID),
    FOREIGN KEY (FOLDER_ID) REFERENCES FOLDER (FOLDER_ID)
        ON DELETE CASCADE 
);

CREATE  INDEX IX_FOLDER_CONSTRAINT_1 ON FOLDER_CONSTRAINT (FOLDER_ID);
-----------------------------------------------------------------------------
-- FOLDER_CONSTRAINTS_REF
-----------------------------------------------------------------------------

CREATE TABLE FOLDER_CONSTRAINTS_REF
(
    CONSTRAINTS_REF_ID INTEGER NOT NULL,
    FOLDER_ID INTEGER NOT NULL,
    APPLY_ORDER INTEGER NOT NULL,
    NAME VARCHAR(40) NOT NULL,
    PRIMARY KEY(CONSTRAINTS_REF_ID),
    FOREIGN KEY (FOLDER_ID) REFERENCES FOLDER (FOLDER_ID)
        ON DELETE CASCADE 
  ,
    UNIQUE (FOLDER_ID, NAME));

CREATE  INDEX IX_FOLDER_CONSTRAINTS_REF_1 ON FOLDER_CONSTRAINTS_REF (FOLDER_ID);
-----------------------------------------------------------------------------
-- FOLDER_ORDER
-----------------------------------------------------------------------------

CREATE TABLE FOLDER_ORDER
(
    ORDER_ID INTEGER NOT NULL,
    FOLDER_ID INTEGER NOT NULL,
    SORT_ORDER INTEGER NOT NULL,
    NAME VARCHAR(80) NOT NULL,
    PRIMARY KEY(ORDER_ID),
    FOREIGN KEY (FOLDER_ID) REFERENCES FOLDER (FOLDER_ID)
        ON DELETE CASCADE 
  ,
    UNIQUE (FOLDER_ID, NAME));

CREATE  INDEX IX_FOLDER_ORDER_1 ON FOLDER_ORDER (FOLDER_ID);
-------------------------------------------------------------------------
-- FOLDER_MENU
-------------------------------------------------------------------------
CREATE TABLE FOLDER_MENU
(
    MENU_ID INTEGER NOT NULL,
    CLASS_NAME VARCHAR(100) NOT NULL,
    PARENT_ID INTEGER,
    FOLDER_ID INTEGER,
    ELEMENT_ORDER INTEGER,
    NAME VARCHAR(100),
    TITLE VARCHAR(100),
    SHORT_TITLE VARCHAR(40),
    TEXT VARCHAR(100),
    OPTIONS VARCHAR(255),
    DEPTH INTEGER,
    IS_PATHS INTEGER,
    IS_REGEXP INTEGER,
    PROFILE VARCHAR(80),
    OPTIONS_ORDER VARCHAR(255),
    SKIN VARCHAR(80),
    IS_NEST INTEGER,
    PRIMARY KEY(MENU_ID),
    FOREIGN KEY (PARENT_ID) REFERENCES FOLDER_MENU (MENU_ID)
        ON DELETE CASCADE 
  ,
    FOREIGN KEY (FOLDER_ID) REFERENCES FOLDER (FOLDER_ID)
        ON DELETE CASCADE 

-- Derby doesn't support UNIQUE constraints on nullable columns !!!  
-- replace UNIQUE (FOLDER_ID, NAME) with IX_FOLDER_MENU_UNIQUE_FN index below...
  );

CREATE  INDEX IX_FOLDER_MENU_1 ON FOLDER_MENU (PARENT_ID);
CREATE  INDEX IX_FOLDER_MENU_UNIQUE_FN ON FOLDER_MENU (FOLDER_ID, NAME);
--------------------------------------------------------------------------
-- FOLDER_MENU_METADATA
--------------------------------------------------------------------------
CREATE TABLE FOLDER_MENU_METADATA
(
    METADATA_ID INTEGER NOT NULL,
    MENU_ID INTEGER NOT NULL,
    NAME VARCHAR(15) NOT NULL,
    LOCALE VARCHAR(20) NOT NULL,
    VALUE VARCHAR(100) NOT NULL,
    PRIMARY KEY(METADATA_ID),
    FOREIGN KEY (MENU_ID) REFERENCES FOLDER_MENU (MENU_ID)
        ON DELETE CASCADE 
  ,
    UNIQUE (MENU_ID, NAME, LOCALE, VALUE));

CREATE  INDEX IX_FOLDER_MENU_METADATA_1 ON FOLDER_MENU_METADATA (MENU_ID);
-----------------------------------------------------------------------------
-- PAGE
-----------------------------------------------------------------------------

CREATE TABLE PAGE
(
    PAGE_ID INTEGER NOT NULL,
    PARENT_ID INTEGER NOT NULL,
    PATH VARCHAR(240) NOT NULL,
    NAME VARCHAR(80) NOT NULL,
    VERSION VARCHAR (40),
    TITLE VARCHAR(100),
    SHORT_TITLE VARCHAR(40),
    IS_HIDDEN INTEGER NOT NULL,
    SKIN VARCHAR(80),
    DEFAULT_LAYOUT_DECORATOR VARCHAR(80),
    DEFAULT_PORTLET_DECORATOR VARCHAR(80),
    SUBSITE VARCHAR(40),
    USER_PRINCIPAL VARCHAR(40),
    ROLE_PRINCIPAL VARCHAR(40),
    GROUP_PRINCIPAL VARCHAR(40),
    MEDIATYPE VARCHAR(15),
    LOCALE VARCHAR(20),
    EXT_ATTR_NAME VARCHAR(15),
    EXT_ATTR_VALUE VARCHAR(40),
    OWNER_PRINCIPAL VARCHAR(40),
    PRIMARY KEY(PAGE_ID),
    FOREIGN KEY (PARENT_ID) REFERENCES FOLDER (FOLDER_ID)
        ON DELETE CASCADE 
  ,
    UNIQUE (PATH));

CREATE  INDEX IX_PAGE_1 ON PAGE (PARENT_ID);
-----------------------------------------------------------------------------
-- PAGE_METADATA
-----------------------------------------------------------------------------

CREATE TABLE PAGE_METADATA
(
    METADATA_ID INTEGER NOT NULL,
    PAGE_ID INTEGER NOT NULL,
    NAME VARCHAR(15) NOT NULL,
    LOCALE VARCHAR(20) NOT NULL,
    VALUE VARCHAR(100) NOT NULL,
    PRIMARY KEY(METADATA_ID),
    FOREIGN KEY (PAGE_ID) REFERENCES PAGE (PAGE_ID)
        ON DELETE CASCADE 
  ,
    UNIQUE (PAGE_ID, NAME, LOCALE, VALUE));

CREATE  INDEX IX_PAGE_METADATA_1 ON PAGE_METADATA (PAGE_ID);
-----------------------------------------------------------------------------
-- PAGE_CONSTRAINT
-----------------------------------------------------------------------------

CREATE TABLE PAGE_CONSTRAINT
(
    CONSTRAINT_ID INTEGER NOT NULL,
    PAGE_ID INTEGER NOT NULL,
    APPLY_ORDER INTEGER NOT NULL,
    USER_PRINCIPALS_ACL VARCHAR(120),
    ROLE_PRINCIPALS_ACL VARCHAR(120),
    GROUP_PRINCIPALS_ACL VARCHAR(120),
    PERMISSIONS_ACL VARCHAR(120),
    PRIMARY KEY(CONSTRAINT_ID),
    FOREIGN KEY (PAGE_ID) REFERENCES PAGE (PAGE_ID)
        ON DELETE CASCADE 
);

CREATE  INDEX IX_PAGE_CONSTRAINT_1 ON PAGE_CONSTRAINT (PAGE_ID);
-----------------------------------------------------------------------------
-- PAGE_CONSTRAINTS_REF
-----------------------------------------------------------------------------

CREATE TABLE PAGE_CONSTRAINTS_REF
(
    CONSTRAINTS_REF_ID INTEGER NOT NULL,
    PAGE_ID INTEGER NOT NULL,
    APPLY_ORDER INTEGER NOT NULL,
    NAME VARCHAR(40) NOT NULL,
    PRIMARY KEY(CONSTRAINTS_REF_ID),
    FOREIGN KEY (PAGE_ID) REFERENCES PAGE (PAGE_ID)
        ON DELETE CASCADE 
  ,
    UNIQUE (PAGE_ID, NAME));

CREATE  INDEX IX_PAGE_CONSTRAINTS_REF_1 ON PAGE_CONSTRAINTS_REF (PAGE_ID);
-------------------------------------------------------------------------
-- PAGE_MENU
-------------------------------------------------------------------------
CREATE TABLE PAGE_MENU
(
    MENU_ID INTEGER NOT NULL,
    CLASS_NAME VARCHAR(100) NOT NULL,
    PARENT_ID INTEGER,
    PAGE_ID INTEGER,
    ELEMENT_ORDER INTEGER,
    NAME VARCHAR(100),
    TITLE VARCHAR(100),
    SHORT_TITLE VARCHAR(40),
    TEXT VARCHAR(100),
    OPTIONS VARCHAR(255),
    DEPTH INTEGER,
    IS_PATHS INTEGER,
    IS_REGEXP INTEGER,
    PROFILE VARCHAR(80),
    OPTIONS_ORDER VARCHAR(255),
    SKIN VARCHAR(80),
    IS_NEST INTEGER,
    PRIMARY KEY(MENU_ID),
    FOREIGN KEY (PARENT_ID) REFERENCES PAGE_MENU (MENU_ID)
        ON DELETE CASCADE 
  ,
    FOREIGN KEY (PAGE_ID) REFERENCES PAGE (PAGE_ID)
        ON DELETE CASCADE 

-- Derby doesn't support UNIQUE constraints on nullable columns !!!  
-- replace UNIQUE (PAGE_ID, NAME) with IX_PAGE_MENU_UNIQUE_PN index below...
  );

CREATE  INDEX IX_PAGE_MENU_1 ON PAGE_MENU (PARENT_ID);
CREATE  INDEX IX_PAGE_MENU_UNIQUE_PN ON PAGE_MENU (PAGE_ID, NAME);
--------------------------------------------------------------------------
-- PAGE_MENU_METADATA
--------------------------------------------------------------------------
CREATE TABLE PAGE_MENU_METADATA
(
    METADATA_ID INTEGER NOT NULL,
    MENU_ID INTEGER NOT NULL,
    NAME VARCHAR(15) NOT NULL,
    LOCALE VARCHAR(20) NOT NULL,
    VALUE VARCHAR(100) NOT NULL,
    PRIMARY KEY(METADATA_ID),
    FOREIGN KEY (MENU_ID) REFERENCES PAGE_MENU (MENU_ID)
        ON DELETE CASCADE 
  ,
    UNIQUE (MENU_ID, NAME, LOCALE, VALUE));

CREATE  INDEX IX_PAGE_MENU_METADATA_1 ON PAGE_MENU_METADATA (MENU_ID);
-----------------------------------------------------------------------------
-- FRAGMENT
-----------------------------------------------------------------------------

CREATE TABLE FRAGMENT
(
    FRAGMENT_ID INTEGER NOT NULL,
    PARENT_ID INTEGER,
    PAGE_ID INTEGER,
    NAME VARCHAR(100),
    TITLE VARCHAR(100),
    SHORT_TITLE VARCHAR(40),
    TYPE VARCHAR(40),
    SKIN VARCHAR(80),
    DECORATOR VARCHAR(80),
    STATE VARCHAR(10),
    PMODE VARCHAR(10),
    LAYOUT_ROW INTEGER,
    LAYOUT_COLUMN INTEGER,
    LAYOUT_SIZES VARCHAR(20),
    LAYOUT_X REAL,
    LAYOUT_Y REAL,
    LAYOUT_Z REAL,
    LAYOUT_WIDTH REAL,
    LAYOUT_HEIGHT REAL,
    EXT_PROP_NAME_1 VARCHAR(40),
    EXT_PROP_VALUE_1 VARCHAR(80),
    EXT_PROP_NAME_2 VARCHAR(40),
    EXT_PROP_VALUE_2 VARCHAR(80),
    OWNER_PRINCIPAL VARCHAR(40),
    PRIMARY KEY(FRAGMENT_ID),
    FOREIGN KEY (PARENT_ID) REFERENCES FRAGMENT (FRAGMENT_ID)
        ON DELETE CASCADE 
  ,
    FOREIGN KEY (PAGE_ID) REFERENCES PAGE (PAGE_ID)
        ON DELETE CASCADE 

-- Derby doesn't support UNIQUE constraints on nullable columns !!!  
-- replace UNIQUE (PAGE_ID) with IX_FRAGMENT_UNIQUE_PAGE_ID index below...
  );

CREATE  INDEX IX_FRAGMENT_1 ON FRAGMENT (PARENT_ID);
CREATE  INDEX IX_FRAGMENT_UNIQUE_PAGE_ID ON FRAGMENT (PAGE_ID);
-----------------------------------------------------------------------------
-- FRAGMENT_CONSTRAINT
-----------------------------------------------------------------------------

CREATE TABLE FRAGMENT_CONSTRAINT
(
    CONSTRAINT_ID INTEGER NOT NULL,
    FRAGMENT_ID INTEGER NOT NULL,
    APPLY_ORDER INTEGER NOT NULL,
    USER_PRINCIPALS_ACL VARCHAR(120),
    ROLE_PRINCIPALS_ACL VARCHAR(120),
    GROUP_PRINCIPALS_ACL VARCHAR(120),
    PERMISSIONS_ACL VARCHAR(120),
    PRIMARY KEY(CONSTRAINT_ID),
    FOREIGN KEY (FRAGMENT_ID) REFERENCES FRAGMENT (FRAGMENT_ID)
        ON DELETE CASCADE 
);

CREATE  INDEX IX_FRAGMENT_CONSTRAINT_1 ON FRAGMENT_CONSTRAINT (FRAGMENT_ID);
-----------------------------------------------------------------------------
-- FRAGMENT_CONSTRAINTS_REF
-----------------------------------------------------------------------------

CREATE TABLE FRAGMENT_CONSTRAINTS_REF
(
    CONSTRAINTS_REF_ID INTEGER NOT NULL,
    FRAGMENT_ID INTEGER NOT NULL,
    APPLY_ORDER INTEGER NOT NULL,
    NAME VARCHAR(40) NOT NULL,
    PRIMARY KEY(CONSTRAINTS_REF_ID),
    FOREIGN KEY (FRAGMENT_ID) REFERENCES FRAGMENT (FRAGMENT_ID)
        ON DELETE CASCADE 
  ,
    UNIQUE (FRAGMENT_ID, NAME));

CREATE  INDEX IX_FRAGMENT_CONSTRAINTS_REF_1 ON FRAGMENT_CONSTRAINTS_REF (FRAGMENT_ID);
-----------------------------------------------------------------------------
-- FRAGMENT_PREF
-----------------------------------------------------------------------------

CREATE TABLE FRAGMENT_PREF
(
    PREF_ID INTEGER NOT NULL,
    FRAGMENT_ID INTEGER NOT NULL,
    NAME VARCHAR(40) NOT NULL,
    IS_READ_ONLY INTEGER NOT NULL,
    PRIMARY KEY(PREF_ID),
    FOREIGN KEY (FRAGMENT_ID) REFERENCES FRAGMENT (FRAGMENT_ID)
        ON DELETE CASCADE 
  ,
    UNIQUE (FRAGMENT_ID, NAME));

CREATE  INDEX IX_FRAGMENT_PREF_1 ON FRAGMENT_PREF (FRAGMENT_ID);
-----------------------------------------------------------------------------
-- FRAGMENT_PREF_VALUE
-----------------------------------------------------------------------------

CREATE TABLE FRAGMENT_PREF_VALUE
(
    PREF_VALUE_ID INTEGER NOT NULL,
    PREF_ID INTEGER NOT NULL,
    VALUE_ORDER INTEGER NOT NULL,
    VALUE VARCHAR(100) NOT NULL,
    PRIMARY KEY(PREF_VALUE_ID),
    FOREIGN KEY (PREF_ID) REFERENCES FRAGMENT_PREF (PREF_ID)
        ON DELETE CASCADE 
);

CREATE  INDEX IX_FRAGMENT_PREF_VALUE_1 ON FRAGMENT_PREF_VALUE (PREF_ID);
-----------------------------------------------------------------------------
-- LINK
-----------------------------------------------------------------------------

CREATE TABLE LINK
(
    LINK_ID INTEGER NOT NULL,
    PARENT_ID INTEGER NOT NULL,
    PATH VARCHAR(240) NOT NULL,
    NAME VARCHAR(80) NOT NULL,
    VERSION VARCHAR (40),
    TITLE VARCHAR(100),
    SHORT_TITLE VARCHAR(40),
    IS_HIDDEN INTEGER NOT NULL,
    SKIN VARCHAR(80),
    TARGET VARCHAR(80),
    URL VARCHAR(255),
    SUBSITE VARCHAR(40),
    USER_PRINCIPAL VARCHAR(40),
    ROLE_PRINCIPAL VARCHAR(40),
    GROUP_PRINCIPAL VARCHAR(40),
    MEDIATYPE VARCHAR(15),
    LOCALE VARCHAR(20),
    EXT_ATTR_NAME VARCHAR(15),
    EXT_ATTR_VALUE VARCHAR(40),
    OWNER_PRINCIPAL VARCHAR(40),
    PRIMARY KEY(LINK_ID),
    FOREIGN KEY (PARENT_ID) REFERENCES FOLDER (FOLDER_ID)
        ON DELETE CASCADE 
  ,
    UNIQUE (PATH));

CREATE  INDEX IX_LINK_1 ON LINK (PARENT_ID);
-----------------------------------------------------------------------------
-- LINK_METADATA
-----------------------------------------------------------------------------

CREATE TABLE LINK_METADATA
(
    METADATA_ID INTEGER NOT NULL,
    LINK_ID INTEGER NOT NULL,
    NAME VARCHAR(15) NOT NULL,
    LOCALE VARCHAR(20) NOT NULL,
    VALUE VARCHAR(100) NOT NULL,
    PRIMARY KEY(METADATA_ID),
    FOREIGN KEY (LINK_ID) REFERENCES LINK (LINK_ID)
        ON DELETE CASCADE 
  ,
    UNIQUE (LINK_ID, NAME, LOCALE, VALUE));

CREATE  INDEX IX_LINK_METADATA_1 ON LINK_METADATA (LINK_ID);
-----------------------------------------------------------------------------
-- LINK_CONSTRAINT
-----------------------------------------------------------------------------

CREATE TABLE LINK_CONSTRAINT
(
    CONSTRAINT_ID INTEGER NOT NULL,
    LINK_ID INTEGER NOT NULL,
    APPLY_ORDER INTEGER NOT NULL,
    USER_PRINCIPALS_ACL VARCHAR(120),
    ROLE_PRINCIPALS_ACL VARCHAR(120),
    GROUP_PRINCIPALS_ACL VARCHAR(120),
    PERMISSIONS_ACL VARCHAR(120),
    PRIMARY KEY(CONSTRAINT_ID),
    FOREIGN KEY (LINK_ID) REFERENCES LINK (LINK_ID)
        ON DELETE CASCADE 
);

CREATE  INDEX IX_LINK_CONSTRAINT_1 ON LINK_CONSTRAINT (LINK_ID);
-----------------------------------------------------------------------------
-- LINK_CONSTRAINTS_REF
-----------------------------------------------------------------------------

CREATE TABLE LINK_CONSTRAINTS_REF
(
    CONSTRAINTS_REF_ID INTEGER NOT NULL,
    LINK_ID INTEGER NOT NULL,
    APPLY_ORDER INTEGER NOT NULL,
    NAME VARCHAR(40) NOT NULL,
    PRIMARY KEY(CONSTRAINTS_REF_ID),
    FOREIGN KEY (LINK_ID) REFERENCES LINK (LINK_ID)
        ON DELETE CASCADE 
  ,
    UNIQUE (LINK_ID, NAME));

CREATE  INDEX IX_LINK_CONSTRAINTS_REF_1 ON LINK_CONSTRAINTS_REF (LINK_ID);
-----------------------------------------------------------------------------
-- PAGE_SECURITY
-----------------------------------------------------------------------------

CREATE TABLE PAGE_SECURITY
(
    PAGE_SECURITY_ID INTEGER NOT NULL,
    PARENT_ID INTEGER NOT NULL,
    PATH VARCHAR(240) NOT NULL,
    NAME VARCHAR(80) NOT NULL,
    VERSION VARCHAR(40),
    SUBSITE VARCHAR(40),
    USER_PRINCIPAL VARCHAR(40),
    ROLE_PRINCIPAL VARCHAR(40),
    GROUP_PRINCIPAL VARCHAR(40),
    MEDIATYPE VARCHAR(15),
    LOCALE VARCHAR(20),
    EXT_ATTR_NAME VARCHAR(15),
    EXT_ATTR_VALUE VARCHAR(40),
    PRIMARY KEY(PAGE_SECURITY_ID),
    FOREIGN KEY (PARENT_ID) REFERENCES FOLDER (FOLDER_ID)
        ON DELETE CASCADE 
  ,
    UNIQUE (PARENT_ID),
    UNIQUE (PATH));

-----------------------------------------------------------------------------
-- PAGE_SEC_CONSTRAINTS_DEF
-----------------------------------------------------------------------------

CREATE TABLE PAGE_SEC_CONSTRAINTS_DEF
(
    CONSTRAINTS_DEF_ID INTEGER NOT NULL,
    PAGE_SECURITY_ID INTEGER NOT NULL,
    NAME VARCHAR(40) NOT NULL,
    PRIMARY KEY(CONSTRAINTS_DEF_ID),
    FOREIGN KEY (PAGE_SECURITY_ID) REFERENCES PAGE_SECURITY (PAGE_SECURITY_ID)
        ON DELETE CASCADE 
  ,
    UNIQUE (PAGE_SECURITY_ID, NAME));

CREATE  INDEX IX_PAGE_SEC_CONSTRAINTS_DEF_1 ON PAGE_SEC_CONSTRAINTS_DEF (PAGE_SECURITY_ID);
-----------------------------------------------------------------------------
-- PAGE_SEC_CONSTRAINT_DEF
-----------------------------------------------------------------------------

CREATE TABLE PAGE_SEC_CONSTRAINT_DEF
(
    CONSTRAINT_DEF_ID INTEGER NOT NULL,
    CONSTRAINTS_DEF_ID INTEGER NOT NULL,
    APPLY_ORDER INTEGER NOT NULL,
    USER_PRINCIPALS_ACL VARCHAR(120),
    ROLE_PRINCIPALS_ACL VARCHAR(120),
    GROUP_PRINCIPALS_ACL VARCHAR(120),
    PERMISSIONS_ACL VARCHAR(120),
    PRIMARY KEY(CONSTRAINT_DEF_ID),
    FOREIGN KEY (CONSTRAINTS_DEF_ID) REFERENCES PAGE_SEC_CONSTRAINTS_DEF (CONSTRAINTS_DEF_ID)
        ON DELETE CASCADE 
);

CREATE  INDEX IX_PAGE_SEC_CONSTRAINT_DEF_1 ON PAGE_SEC_CONSTRAINT_DEF (CONSTRAINTS_DEF_ID);
-----------------------------------------------------------------------------
-- PAGE_SEC_CONSTRAINTS_REF
-----------------------------------------------------------------------------

CREATE TABLE PAGE_SEC_CONSTRAINTS_REF
(
    CONSTRAINTS_REF_ID INTEGER NOT NULL,
    PAGE_SECURITY_ID INTEGER NOT NULL,
    APPLY_ORDER INTEGER NOT NULL,
    NAME VARCHAR(40) NOT NULL,
    PRIMARY KEY(CONSTRAINTS_REF_ID),
    FOREIGN KEY (PAGE_SECURITY_ID) REFERENCES PAGE_SECURITY (PAGE_SECURITY_ID)
        ON DELETE CASCADE 
);

CREATE  INDEX IX_PAGE_SEC_CONSTRAINTS_REF_1 ON PAGE_SEC_CONSTRAINTS_REF (PAGE_SECURITY_ID);
-----------------------------------------------------------------------------
-- PROFILING_RULE
-----------------------------------------------------------------------------

CREATE TABLE PROFILING_RULE
(
    RULE_ID VARCHAR(80) NOT NULL,
    CLASS_NAME VARCHAR(100) NOT NULL,
    TITLE VARCHAR(100),
    PRIMARY KEY(RULE_ID));

-----------------------------------------------------------------------------
-- RULE_CRITERION
-----------------------------------------------------------------------------

CREATE TABLE RULE_CRITERION
(
    CRITERION_ID VARCHAR(80) NOT NULL,
    RULE_ID VARCHAR(80) NOT NULL,
    FALLBACK_ORDER INTEGER NOT NULL,
    REQUEST_TYPE VARCHAR(40) NOT NULL,
    NAME VARCHAR(80) NOT NULL,
    COLUMN_VALUE VARCHAR(128),
    FALLBACK_TYPE INTEGER default 1,
    PRIMARY KEY(CRITERION_ID),
    FOREIGN KEY (RULE_ID) REFERENCES PROFILING_RULE (RULE_ID)
        ON DELETE CASCADE 
);

CREATE  INDEX IX_RULE_CRITERION_1 ON RULE_CRITERION (RULE_ID, FALLBACK_ORDER);
-----------------------------------------------------------------------------
-- PRINCIPAL_RULE_ASSOC
-----------------------------------------------------------------------------

CREATE TABLE PRINCIPAL_RULE_ASSOC
(
    PRINCIPAL_NAME VARCHAR(80) NOT NULL,
    LOCATOR_NAME VARCHAR(80) NOT NULL,
    RULE_ID VARCHAR(80) NOT NULL,
    PRIMARY KEY(PRINCIPAL_NAME,LOCATOR_NAME));

-----------------------------------------------------------------------------
-- PROFILE_PAGE_ASSOC
-----------------------------------------------------------------------------

CREATE TABLE PROFILE_PAGE_ASSOC
(
    LOCATOR_HASH VARCHAR(40) NOT NULL,
    PAGE_ID VARCHAR(80) NOT NULL,
    UNIQUE (LOCATOR_HASH, PAGE_ID));

-----------------------------------------------------------------------------
-- CLUBS
-----------------------------------------------------------------------------

CREATE TABLE CLUBS
(
    NAME VARCHAR(80) NOT NULL,
    COUNTRY VARCHAR(40) NOT NULL,
    CITY VARCHAR(40) NOT NULL,
    STADIUM VARCHAR(80) NOT NULL,
    CAPACITY INTEGER,
    FOUNDED INTEGER,
    PITCH VARCHAR(40),
    NICKNAME VARCHAR(40),
    PRIMARY KEY(NAME));

