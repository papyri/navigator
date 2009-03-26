
/* ---------------------------------------------------------------------- */
/* FOLDER                                                      */
/* ---------------------------------------------------------------------- */

IF EXISTS (SELECT 1 FROM sysobjects WHERE type ='RI' AND name='FK_FOLDER_1')
    ALTER TABLE FOLDER DROP CONSTRAINT FK_FOLDER_1;
IF EXISTS (SELECT 1 FROM sysobjects WHERE type = 'U' AND name = 'FOLDER')
BEGIN
     DECLARE @reftable_1 nvarchar(60), @constraintname_1 nvarchar(60)
     DECLARE refcursor CURSOR FOR
     select reftables.name tablename, cons.name constraintname
      from sysobjects tables,
           sysobjects reftables,
           sysobjects cons,
           sysreferences ref
       where tables.id = ref.rkeyid
         and cons.id = ref.constid
         and reftables.id = ref.fkeyid
         and tables.name = 'FOLDER'
     OPEN refcursor
     FETCH NEXT from refcursor into @reftable_1, @constraintname_1
     while @@FETCH_STATUS = 0
     BEGIN
       exec ('alter table '+@reftable_1+' drop constraint '+@constraintname_1)
       FETCH NEXT from refcursor into @reftable_1, @constraintname_1
     END
     CLOSE refcursor
     DEALLOCATE refcursor
     DROP TABLE FOLDER
END
;

CREATE TABLE FOLDER
(
            FOLDER_ID INT NOT NULL,
            PARENT_ID INT NULL,
            PATH VARCHAR (240) NOT NULL,
            NAME VARCHAR (80) NOT NULL,
            TITLE VARCHAR (100) NULL,
            SHORT_TITLE VARCHAR (40) NULL,
            IS_HIDDEN INT NOT NULL,
            SKIN VARCHAR (80) NULL,
            DEFAULT_LAYOUT_DECORATOR VARCHAR (80) NULL,
            DEFAULT_PORTLET_DECORATOR VARCHAR (80) NULL,
            DEFAULT_PAGE_NAME VARCHAR (80) NULL,
            SUBSITE VARCHAR (40) NULL,
            USER_PRINCIPAL VARCHAR (40) NULL,
            ROLE_PRINCIPAL VARCHAR (40) NULL,
            GROUP_PRINCIPAL VARCHAR (40) NULL,
            MEDIATYPE VARCHAR (15) NULL,
            LOCALE VARCHAR (20) NULL,
            EXT_ATTR_NAME VARCHAR (15) NULL,
            EXT_ATTR_VALUE VARCHAR (40) NULL,
            OWNER_PRINCIPAL VARCHAR (40) NULL,

    CONSTRAINT FOLDER_PK PRIMARY KEY(FOLDER_ID),
    UNIQUE (PATH));

CREATE  INDEX IX_FOLDER_1 ON FOLDER (PARENT_ID);




/* ---------------------------------------------------------------------- */
/* FOLDER_METADATA                                                      */
/* ---------------------------------------------------------------------- */

IF EXISTS (SELECT 1 FROM sysobjects WHERE type ='RI' AND name='FK_FOLDER_METADATA_1')
    ALTER TABLE FOLDER_METADATA DROP CONSTRAINT FK_FOLDER_METADATA_1;
IF EXISTS (SELECT 1 FROM sysobjects WHERE type = 'U' AND name = 'FOLDER_METADATA')
BEGIN
     DECLARE @reftable_2 nvarchar(60), @constraintname_2 nvarchar(60)
     DECLARE refcursor CURSOR FOR
     select reftables.name tablename, cons.name constraintname
      from sysobjects tables,
           sysobjects reftables,
           sysobjects cons,
           sysreferences ref
       where tables.id = ref.rkeyid
         and cons.id = ref.constid
         and reftables.id = ref.fkeyid
         and tables.name = 'FOLDER_METADATA'
     OPEN refcursor
     FETCH NEXT from refcursor into @reftable_2, @constraintname_2
     while @@FETCH_STATUS = 0
     BEGIN
       exec ('alter table '+@reftable_2+' drop constraint '+@constraintname_2)
       FETCH NEXT from refcursor into @reftable_2, @constraintname_2
     END
     CLOSE refcursor
     DEALLOCATE refcursor
     DROP TABLE FOLDER_METADATA
END
;

CREATE TABLE FOLDER_METADATA
(
            METADATA_ID INT NOT NULL,
            FOLDER_ID INT NOT NULL,
            NAME VARCHAR (15) NOT NULL,
            LOCALE VARCHAR (20) NOT NULL,
            VALUE VARCHAR (100) NOT NULL,

    CONSTRAINT FOLDER_METADATA_PK PRIMARY KEY(METADATA_ID),
    UNIQUE (FOLDER_ID, NAME, LOCALE, VALUE));

CREATE  INDEX IX_FOLDER_METADATA_1 ON FOLDER_METADATA (FOLDER_ID);




/* ---------------------------------------------------------------------- */
/* FOLDER_CONSTRAINT                                                      */
/* ---------------------------------------------------------------------- */

IF EXISTS (SELECT 1 FROM sysobjects WHERE type ='RI' AND name='FK_FOLDER_CONSTRAINT_1')
    ALTER TABLE FOLDER_CONSTRAINT DROP CONSTRAINT FK_FOLDER_CONSTRAINT_1;
IF EXISTS (SELECT 1 FROM sysobjects WHERE type = 'U' AND name = 'FOLDER_CONSTRAINT')
BEGIN
     DECLARE @reftable_3 nvarchar(60), @constraintname_3 nvarchar(60)
     DECLARE refcursor CURSOR FOR
     select reftables.name tablename, cons.name constraintname
      from sysobjects tables,
           sysobjects reftables,
           sysobjects cons,
           sysreferences ref
       where tables.id = ref.rkeyid
         and cons.id = ref.constid
         and reftables.id = ref.fkeyid
         and tables.name = 'FOLDER_CONSTRAINT'
     OPEN refcursor
     FETCH NEXT from refcursor into @reftable_3, @constraintname_3
     while @@FETCH_STATUS = 0
     BEGIN
       exec ('alter table '+@reftable_3+' drop constraint '+@constraintname_3)
       FETCH NEXT from refcursor into @reftable_3, @constraintname_3
     END
     CLOSE refcursor
     DEALLOCATE refcursor
     DROP TABLE FOLDER_CONSTRAINT
END
;

CREATE TABLE FOLDER_CONSTRAINT
(
            CONSTRAINT_ID INT NOT NULL,
            FOLDER_ID INT NOT NULL,
            APPLY_ORDER INT NOT NULL,
            USER_PRINCIPALS_ACL VARCHAR (120) NULL,
            ROLE_PRINCIPALS_ACL VARCHAR (120) NULL,
            GROUP_PRINCIPALS_ACL VARCHAR (120) NULL,
            PERMISSIONS_ACL VARCHAR (120) NULL,

    CONSTRAINT FOLDER_CONSTRAINT_PK PRIMARY KEY(CONSTRAINT_ID));

CREATE  INDEX IX_FOLDER_CONSTRAINT_1 ON FOLDER_CONSTRAINT (FOLDER_ID);




/* ---------------------------------------------------------------------- */
/* FOLDER_CONSTRAINTS_REF                                                      */
/* ---------------------------------------------------------------------- */

IF EXISTS (SELECT 1 FROM sysobjects WHERE type ='RI' AND name='FK_FOLDER_CONSTRAINTS_REF_1')
    ALTER TABLE FOLDER_CONSTRAINTS_REF DROP CONSTRAINT FK_FOLDER_CONSTRAINTS_REF_1;
IF EXISTS (SELECT 1 FROM sysobjects WHERE type = 'U' AND name = 'FOLDER_CONSTRAINTS_REF')
BEGIN
     DECLARE @reftable_4 nvarchar(60), @constraintname_4 nvarchar(60)
     DECLARE refcursor CURSOR FOR
     select reftables.name tablename, cons.name constraintname
      from sysobjects tables,
           sysobjects reftables,
           sysobjects cons,
           sysreferences ref
       where tables.id = ref.rkeyid
         and cons.id = ref.constid
         and reftables.id = ref.fkeyid
         and tables.name = 'FOLDER_CONSTRAINTS_REF'
     OPEN refcursor
     FETCH NEXT from refcursor into @reftable_4, @constraintname_4
     while @@FETCH_STATUS = 0
     BEGIN
       exec ('alter table '+@reftable_4+' drop constraint '+@constraintname_4)
       FETCH NEXT from refcursor into @reftable_4, @constraintname_4
     END
     CLOSE refcursor
     DEALLOCATE refcursor
     DROP TABLE FOLDER_CONSTRAINTS_REF
END
;

CREATE TABLE FOLDER_CONSTRAINTS_REF
(
            CONSTRAINTS_REF_ID INT NOT NULL,
            FOLDER_ID INT NOT NULL,
            APPLY_ORDER INT NOT NULL,
            NAME VARCHAR (40) NOT NULL,

    CONSTRAINT FOLDER_CONSTRAINTS_REF_PK PRIMARY KEY(CONSTRAINTS_REF_ID),
    UNIQUE (FOLDER_ID, NAME));

CREATE  INDEX IX_FOLDER_CONSTRAINTS_REF_1 ON FOLDER_CONSTRAINTS_REF (FOLDER_ID);




/* ---------------------------------------------------------------------- */
/* FOLDER_ORDER                                                      */
/* ---------------------------------------------------------------------- */

IF EXISTS (SELECT 1 FROM sysobjects WHERE type ='RI' AND name='FK_FOLDER_ORDER_1')
    ALTER TABLE FOLDER_ORDER DROP CONSTRAINT FK_FOLDER_ORDER_1;
IF EXISTS (SELECT 1 FROM sysobjects WHERE type = 'U' AND name = 'FOLDER_ORDER')
BEGIN
     DECLARE @reftable_5 nvarchar(60), @constraintname_5 nvarchar(60)
     DECLARE refcursor CURSOR FOR
     select reftables.name tablename, cons.name constraintname
      from sysobjects tables,
           sysobjects reftables,
           sysobjects cons,
           sysreferences ref
       where tables.id = ref.rkeyid
         and cons.id = ref.constid
         and reftables.id = ref.fkeyid
         and tables.name = 'FOLDER_ORDER'
     OPEN refcursor
     FETCH NEXT from refcursor into @reftable_5, @constraintname_5
     while @@FETCH_STATUS = 0
     BEGIN
       exec ('alter table '+@reftable_5+' drop constraint '+@constraintname_5)
       FETCH NEXT from refcursor into @reftable_5, @constraintname_5
     END
     CLOSE refcursor
     DEALLOCATE refcursor
     DROP TABLE FOLDER_ORDER
END
;

CREATE TABLE FOLDER_ORDER
(
            ORDER_ID INT NOT NULL,
            FOLDER_ID INT NOT NULL,
            SORT_ORDER INT NOT NULL,
            NAME VARCHAR (80) NOT NULL,

    CONSTRAINT FOLDER_ORDER_PK PRIMARY KEY(ORDER_ID),
    UNIQUE (FOLDER_ID, NAME));

CREATE  INDEX IX_FOLDER_ORDER_1 ON FOLDER_ORDER (FOLDER_ID);




/* ---------------------------------------------------------------------- */
/* FOLDER_MENU                                                      */
/* ---------------------------------------------------------------------- */

IF EXISTS (SELECT 1 FROM sysobjects WHERE type ='RI' AND name='FK_FOLDER_MENU_1')
    ALTER TABLE FOLDER_MENU DROP CONSTRAINT FK_FOLDER_MENU_1;
IF EXISTS (SELECT 1 FROM sysobjects WHERE type ='RI' AND name='FK_FOLDER_MENU_2')
    ALTER TABLE FOLDER_MENU DROP CONSTRAINT FK_FOLDER_MENU_2;
IF EXISTS (SELECT 1 FROM sysobjects WHERE type = 'U' AND name = 'FOLDER_MENU')
BEGIN
     DECLARE @reftable_6 nvarchar(60), @constraintname_6 nvarchar(60)
     DECLARE refcursor CURSOR FOR
     select reftables.name tablename, cons.name constraintname
      from sysobjects tables,
           sysobjects reftables,
           sysobjects cons,
           sysreferences ref
       where tables.id = ref.rkeyid
         and cons.id = ref.constid
         and reftables.id = ref.fkeyid
         and tables.name = 'FOLDER_MENU'
     OPEN refcursor
     FETCH NEXT from refcursor into @reftable_6, @constraintname_6
     while @@FETCH_STATUS = 0
     BEGIN
       exec ('alter table '+@reftable_6+' drop constraint '+@constraintname_6)
       FETCH NEXT from refcursor into @reftable_6, @constraintname_6
     END
     CLOSE refcursor
     DEALLOCATE refcursor
     DROP TABLE FOLDER_MENU
END
;

CREATE TABLE FOLDER_MENU
(
            MENU_ID INT NOT NULL,
            CLASS_NAME VARCHAR (100) NOT NULL,
            PARENT_ID INT NULL,
            FOLDER_ID INT NULL,
            ELEMENT_ORDER INT NULL,
            NAME VARCHAR (100) NULL,
            TITLE VARCHAR (100) NULL,
            SHORT_TITLE VARCHAR (40) NULL,
            TEXT VARCHAR (100) NULL,
            OPTIONS VARCHAR (255) NULL,
            DEPTH INT NULL,
            IS_PATHS INT NULL,
            IS_REGEXP INT NULL,
            PROFILE VARCHAR (80) NULL,
            OPTIONS_ORDER VARCHAR (255) NULL,
            SKIN VARCHAR (80) NULL,
            IS_NEST INT NULL,

    CONSTRAINT FOLDER_MENU_PK PRIMARY KEY(MENU_ID));

CREATE  INDEX IX_FOLDER_MENU_1 ON FOLDER_MENU (PARENT_ID);
CREATE  INDEX UN_FOLDER_MENU_1 ON FOLDER_MENU (FOLDER_ID, NAME);




/* ---------------------------------------------------------------------- */
/* FOLDER_MENU_METADATA                                                      */
/* ---------------------------------------------------------------------- */

IF EXISTS (SELECT 1 FROM sysobjects WHERE type ='RI' AND name='FK_FOLDER_MENU_METADATA_1')
    ALTER TABLE FOLDER_MENU_METADATA DROP CONSTRAINT FK_FOLDER_MENU_METADATA_1;
IF EXISTS (SELECT 1 FROM sysobjects WHERE type = 'U' AND name = 'FOLDER_MENU_METADATA')
BEGIN
     DECLARE @reftable_7 nvarchar(60), @constraintname_7 nvarchar(60)
     DECLARE refcursor CURSOR FOR
     select reftables.name tablename, cons.name constraintname
      from sysobjects tables,
           sysobjects reftables,
           sysobjects cons,
           sysreferences ref
       where tables.id = ref.rkeyid
         and cons.id = ref.constid
         and reftables.id = ref.fkeyid
         and tables.name = 'FOLDER_MENU_METADATA'
     OPEN refcursor
     FETCH NEXT from refcursor into @reftable_7, @constraintname_7
     while @@FETCH_STATUS = 0
     BEGIN
       exec ('alter table '+@reftable_7+' drop constraint '+@constraintname_7)
       FETCH NEXT from refcursor into @reftable_7, @constraintname_7
     END
     CLOSE refcursor
     DEALLOCATE refcursor
     DROP TABLE FOLDER_MENU_METADATA
END
;

CREATE TABLE FOLDER_MENU_METADATA
(
            METADATA_ID INT NOT NULL,
            MENU_ID INT NOT NULL,
            NAME VARCHAR (15) NOT NULL,
            LOCALE VARCHAR (20) NOT NULL,
            VALUE VARCHAR (100) NOT NULL,

    CONSTRAINT FOLDER_MENU_METADATA_PK PRIMARY KEY(METADATA_ID),
    UNIQUE (MENU_ID, NAME, LOCALE, VALUE));

CREATE  INDEX IX_FOLDER_MENU_METADATA_1 ON FOLDER_MENU_METADATA (MENU_ID);




/* ---------------------------------------------------------------------- */
/* PAGE                                                      */
/* ---------------------------------------------------------------------- */

IF EXISTS (SELECT 1 FROM sysobjects WHERE type ='RI' AND name='FK_PAGE_1')
    ALTER TABLE PAGE DROP CONSTRAINT FK_PAGE_1;
IF EXISTS (SELECT 1 FROM sysobjects WHERE type = 'U' AND name = 'PAGE')
BEGIN
     DECLARE @reftable_8 nvarchar(60), @constraintname_8 nvarchar(60)
     DECLARE refcursor CURSOR FOR
     select reftables.name tablename, cons.name constraintname
      from sysobjects tables,
           sysobjects reftables,
           sysobjects cons,
           sysreferences ref
       where tables.id = ref.rkeyid
         and cons.id = ref.constid
         and reftables.id = ref.fkeyid
         and tables.name = 'PAGE'
     OPEN refcursor
     FETCH NEXT from refcursor into @reftable_8, @constraintname_8
     while @@FETCH_STATUS = 0
     BEGIN
       exec ('alter table '+@reftable_8+' drop constraint '+@constraintname_8)
       FETCH NEXT from refcursor into @reftable_8, @constraintname_8
     END
     CLOSE refcursor
     DEALLOCATE refcursor
     DROP TABLE PAGE
END
;

CREATE TABLE PAGE
(
            PAGE_ID INT NOT NULL,
            PARENT_ID INT NOT NULL,
            PATH VARCHAR (240) NOT NULL,
            NAME VARCHAR (80) NOT NULL,
            VERSION VARCHAR (40) NULL,
            TITLE VARCHAR (100) NULL,
            SHORT_TITLE VARCHAR (40) NULL,
            IS_HIDDEN INT NOT NULL,
            SKIN VARCHAR (80) NULL,
            DEFAULT_LAYOUT_DECORATOR VARCHAR (80) NULL,
            DEFAULT_PORTLET_DECORATOR VARCHAR (80) NULL,
            SUBSITE VARCHAR (40) NULL,
            USER_PRINCIPAL VARCHAR (40) NULL,
            ROLE_PRINCIPAL VARCHAR (40) NULL,
            GROUP_PRINCIPAL VARCHAR (40) NULL,
            MEDIATYPE VARCHAR (15) NULL,
            LOCALE VARCHAR (20) NULL,
            EXT_ATTR_NAME VARCHAR (15) NULL,
            EXT_ATTR_VALUE VARCHAR (40) NULL,
            OWNER_PRINCIPAL VARCHAR (40) NULL,

    CONSTRAINT PAGE_PK PRIMARY KEY(PAGE_ID),
    UNIQUE (PATH));

CREATE  INDEX IX_PAGE_1 ON PAGE (PARENT_ID);




/* ---------------------------------------------------------------------- */
/* PAGE_METADATA                                                      */
/* ---------------------------------------------------------------------- */

IF EXISTS (SELECT 1 FROM sysobjects WHERE type ='RI' AND name='FK_PAGE_METADATA_1')
    ALTER TABLE PAGE_METADATA DROP CONSTRAINT FK_PAGE_METADATA_1;
IF EXISTS (SELECT 1 FROM sysobjects WHERE type = 'U' AND name = 'PAGE_METADATA')
BEGIN
     DECLARE @reftable_9 nvarchar(60), @constraintname_9 nvarchar(60)
     DECLARE refcursor CURSOR FOR
     select reftables.name tablename, cons.name constraintname
      from sysobjects tables,
           sysobjects reftables,
           sysobjects cons,
           sysreferences ref
       where tables.id = ref.rkeyid
         and cons.id = ref.constid
         and reftables.id = ref.fkeyid
         and tables.name = 'PAGE_METADATA'
     OPEN refcursor
     FETCH NEXT from refcursor into @reftable_9, @constraintname_9
     while @@FETCH_STATUS = 0
     BEGIN
       exec ('alter table '+@reftable_9+' drop constraint '+@constraintname_9)
       FETCH NEXT from refcursor into @reftable_9, @constraintname_9
     END
     CLOSE refcursor
     DEALLOCATE refcursor
     DROP TABLE PAGE_METADATA
END
;

CREATE TABLE PAGE_METADATA
(
            METADATA_ID INT NOT NULL,
            PAGE_ID INT NOT NULL,
            NAME VARCHAR (15) NOT NULL,
            LOCALE VARCHAR (20) NOT NULL,
            VALUE VARCHAR (100) NOT NULL,

    CONSTRAINT PAGE_METADATA_PK PRIMARY KEY(METADATA_ID),
    UNIQUE (PAGE_ID, NAME, LOCALE, VALUE));

CREATE  INDEX IX_PAGE_METADATA_1 ON PAGE_METADATA (PAGE_ID);




/* ---------------------------------------------------------------------- */
/* PAGE_CONSTRAINT                                                      */
/* ---------------------------------------------------------------------- */

IF EXISTS (SELECT 1 FROM sysobjects WHERE type ='RI' AND name='FK_PAGE_CONSTRAINT_1')
    ALTER TABLE PAGE_CONSTRAINT DROP CONSTRAINT FK_PAGE_CONSTRAINT_1;
IF EXISTS (SELECT 1 FROM sysobjects WHERE type = 'U' AND name = 'PAGE_CONSTRAINT')
BEGIN
     DECLARE @reftable_10 nvarchar(60), @constraintname_10 nvarchar(60)
     DECLARE refcursor CURSOR FOR
     select reftables.name tablename, cons.name constraintname
      from sysobjects tables,
           sysobjects reftables,
           sysobjects cons,
           sysreferences ref
       where tables.id = ref.rkeyid
         and cons.id = ref.constid
         and reftables.id = ref.fkeyid
         and tables.name = 'PAGE_CONSTRAINT'
     OPEN refcursor
     FETCH NEXT from refcursor into @reftable_10, @constraintname_10
     while @@FETCH_STATUS = 0
     BEGIN
       exec ('alter table '+@reftable_10+' drop constraint '+@constraintname_10)
       FETCH NEXT from refcursor into @reftable_10, @constraintname_10
     END
     CLOSE refcursor
     DEALLOCATE refcursor
     DROP TABLE PAGE_CONSTRAINT
END
;

CREATE TABLE PAGE_CONSTRAINT
(
            CONSTRAINT_ID INT NOT NULL,
            PAGE_ID INT NOT NULL,
            APPLY_ORDER INT NOT NULL,
            USER_PRINCIPALS_ACL VARCHAR (120) NULL,
            ROLE_PRINCIPALS_ACL VARCHAR (120) NULL,
            GROUP_PRINCIPALS_ACL VARCHAR (120) NULL,
            PERMISSIONS_ACL VARCHAR (120) NULL,

    CONSTRAINT PAGE_CONSTRAINT_PK PRIMARY KEY(CONSTRAINT_ID));

CREATE  INDEX IX_PAGE_CONSTRAINT_1 ON PAGE_CONSTRAINT (PAGE_ID);




/* ---------------------------------------------------------------------- */
/* PAGE_CONSTRAINTS_REF                                                      */
/* ---------------------------------------------------------------------- */

IF EXISTS (SELECT 1 FROM sysobjects WHERE type ='RI' AND name='FK_PAGE_CONSTRAINTS_REF_1')
    ALTER TABLE PAGE_CONSTRAINTS_REF DROP CONSTRAINT FK_PAGE_CONSTRAINTS_REF_1;
IF EXISTS (SELECT 1 FROM sysobjects WHERE type = 'U' AND name = 'PAGE_CONSTRAINTS_REF')
BEGIN
     DECLARE @reftable_11 nvarchar(60), @constraintname_11 nvarchar(60)
     DECLARE refcursor CURSOR FOR
     select reftables.name tablename, cons.name constraintname
      from sysobjects tables,
           sysobjects reftables,
           sysobjects cons,
           sysreferences ref
       where tables.id = ref.rkeyid
         and cons.id = ref.constid
         and reftables.id = ref.fkeyid
         and tables.name = 'PAGE_CONSTRAINTS_REF'
     OPEN refcursor
     FETCH NEXT from refcursor into @reftable_11, @constraintname_11
     while @@FETCH_STATUS = 0
     BEGIN
       exec ('alter table '+@reftable_11+' drop constraint '+@constraintname_11)
       FETCH NEXT from refcursor into @reftable_11, @constraintname_11
     END
     CLOSE refcursor
     DEALLOCATE refcursor
     DROP TABLE PAGE_CONSTRAINTS_REF
END
;

CREATE TABLE PAGE_CONSTRAINTS_REF
(
            CONSTRAINTS_REF_ID INT NOT NULL,
            PAGE_ID INT NOT NULL,
            APPLY_ORDER INT NOT NULL,
            NAME VARCHAR (40) NOT NULL,

    CONSTRAINT PAGE_CONSTRAINTS_REF_PK PRIMARY KEY(CONSTRAINTS_REF_ID),
    UNIQUE (PAGE_ID, NAME));

CREATE  INDEX IX_PAGE_CONSTRAINTS_REF_1 ON PAGE_CONSTRAINTS_REF (PAGE_ID);




/* ---------------------------------------------------------------------- */
/* PAGE_MENU                                                      */
/* ---------------------------------------------------------------------- */

IF EXISTS (SELECT 1 FROM sysobjects WHERE type ='RI' AND name='FK_PAGE_MENU_1')
    ALTER TABLE PAGE_MENU DROP CONSTRAINT FK_PAGE_MENU_1;
IF EXISTS (SELECT 1 FROM sysobjects WHERE type ='RI' AND name='PM_M_FK_PAGE_ID_PAGE')
    ALTER TABLE PAGE_MENU DROP CONSTRAINT PM_M_FK_PAGE_ID_PAGE;
IF EXISTS (SELECT 1 FROM sysobjects WHERE type = 'U' AND name = 'PAGE_MENU')
BEGIN
     DECLARE @reftable_12 nvarchar(60), @constraintname_12 nvarchar(60)
     DECLARE refcursor CURSOR FOR
     select reftables.name tablename, cons.name constraintname
      from sysobjects tables,
           sysobjects reftables,
           sysobjects cons,
           sysreferences ref
       where tables.id = ref.rkeyid
         and cons.id = ref.constid
         and reftables.id = ref.fkeyid
         and tables.name = 'PAGE_MENU'
     OPEN refcursor
     FETCH NEXT from refcursor into @reftable_12, @constraintname_12
     while @@FETCH_STATUS = 0
     BEGIN
       exec ('alter table '+@reftable_12+' drop constraint '+@constraintname_12)
       FETCH NEXT from refcursor into @reftable_12, @constraintname_12
     END
     CLOSE refcursor
     DEALLOCATE refcursor
     DROP TABLE PAGE_MENU
END
;

CREATE TABLE PAGE_MENU
(
            MENU_ID INT NOT NULL,
            CLASS_NAME VARCHAR (100) NOT NULL,
            PARENT_ID INT NULL,
            PAGE_ID INT NULL,
            ELEMENT_ORDER INT NULL,
            NAME VARCHAR (100) NULL,
            TITLE VARCHAR (100) NULL,
            SHORT_TITLE VARCHAR (40) NULL,
            TEXT VARCHAR (100) NULL,
            OPTIONS VARCHAR (255) NULL,
            DEPTH INT NULL,
            IS_PATHS INT NULL,
            IS_REGEXP INT NULL,
            PROFILE VARCHAR (80) NULL,
            OPTIONS_ORDER VARCHAR (255) NULL,
            SKIN VARCHAR (80) NULL,
            IS_NEST INT NULL,

    CONSTRAINT PAGE_MENU_PK PRIMARY KEY(MENU_ID));

CREATE  INDEX IX_PAGE_MENU_1 ON PAGE_MENU (PARENT_ID);
CREATE  INDEX UN_PAGE_MENU_1 ON PAGE_MENU (PAGE_ID, NAME);




/* ---------------------------------------------------------------------- */
/* PAGE_MENU_METADATA                                                      */
/* ---------------------------------------------------------------------- */

IF EXISTS (SELECT 1 FROM sysobjects WHERE type ='RI' AND name='FK_PAGE_MENU_METADATA_1')
    ALTER TABLE PAGE_MENU_METADATA DROP CONSTRAINT FK_PAGE_MENU_METADATA_1;
IF EXISTS (SELECT 1 FROM sysobjects WHERE type = 'U' AND name = 'PAGE_MENU_METADATA')
BEGIN
     DECLARE @reftable_13 nvarchar(60), @constraintname_13 nvarchar(60)
     DECLARE refcursor CURSOR FOR
     select reftables.name tablename, cons.name constraintname
      from sysobjects tables,
           sysobjects reftables,
           sysobjects cons,
           sysreferences ref
       where tables.id = ref.rkeyid
         and cons.id = ref.constid
         and reftables.id = ref.fkeyid
         and tables.name = 'PAGE_MENU_METADATA'
     OPEN refcursor
     FETCH NEXT from refcursor into @reftable_13, @constraintname_13
     while @@FETCH_STATUS = 0
     BEGIN
       exec ('alter table '+@reftable_13+' drop constraint '+@constraintname_13)
       FETCH NEXT from refcursor into @reftable_13, @constraintname_13
     END
     CLOSE refcursor
     DEALLOCATE refcursor
     DROP TABLE PAGE_MENU_METADATA
END
;

CREATE TABLE PAGE_MENU_METADATA
(
            METADATA_ID INT NOT NULL,
            MENU_ID INT NOT NULL,
            NAME VARCHAR (15) NOT NULL,
            LOCALE VARCHAR (20) NOT NULL,
            VALUE VARCHAR (100) NOT NULL,

    CONSTRAINT PAGE_MENU_METADATA_PK PRIMARY KEY(METADATA_ID),
    UNIQUE (MENU_ID, NAME, LOCALE, VALUE));

CREATE  INDEX IX_PAGE_MENU_METADATA_1 ON PAGE_MENU_METADATA (MENU_ID);




/* ---------------------------------------------------------------------- */
/* FRAGMENT                                                      */
/* ---------------------------------------------------------------------- */

IF EXISTS (SELECT 1 FROM sysobjects WHERE type ='RI' AND name='FK_FRAGMENT_1')
    ALTER TABLE FRAGMENT DROP CONSTRAINT FK_FRAGMENT_1;
IF EXISTS (SELECT 1 FROM sysobjects WHERE type ='RI' AND name='FK_FRAGMENT_2')
    ALTER TABLE FRAGMENT DROP CONSTRAINT FK_FRAGMENT_2;
IF EXISTS (SELECT 1 FROM sysobjects WHERE type = 'U' AND name = 'FRAGMENT')
BEGIN
     DECLARE @reftable_14 nvarchar(60), @constraintname_14 nvarchar(60)
     DECLARE refcursor CURSOR FOR
     select reftables.name tablename, cons.name constraintname
      from sysobjects tables,
           sysobjects reftables,
           sysobjects cons,
           sysreferences ref
       where tables.id = ref.rkeyid
         and cons.id = ref.constid
         and reftables.id = ref.fkeyid
         and tables.name = 'FRAGMENT'
     OPEN refcursor
     FETCH NEXT from refcursor into @reftable_14, @constraintname_14
     while @@FETCH_STATUS = 0
     BEGIN
       exec ('alter table '+@reftable_14+' drop constraint '+@constraintname_14)
       FETCH NEXT from refcursor into @reftable_14, @constraintname_14
     END
     CLOSE refcursor
     DEALLOCATE refcursor
     DROP TABLE FRAGMENT
END
;

CREATE TABLE FRAGMENT
(
            FRAGMENT_ID INT NOT NULL,
            PARENT_ID INT NULL,
            PAGE_ID INT NULL,
            NAME VARCHAR (100) NULL,
            TITLE VARCHAR (100) NULL,
            SHORT_TITLE VARCHAR (40) NULL,
            TYPE VARCHAR (40) NULL,
            SKIN VARCHAR (80) NULL,
            DECORATOR VARCHAR (80) NULL,
            STATE VARCHAR (10) NULL,
            PMODE VARCHAR (10) NULL,
            LAYOUT_ROW INT NULL,
            LAYOUT_COLUMN INT NULL,
            LAYOUT_SIZES VARCHAR (20) NULL,
            LAYOUT_X REAL NULL,
            LAYOUT_Y REAL NULL,
            LAYOUT_Z REAL NULL,
            LAYOUT_WIDTH REAL NULL,
            LAYOUT_HEIGHT REAL NULL,
            EXT_PROP_NAME_1 VARCHAR (40) NULL,
            EXT_PROP_VALUE_1 VARCHAR (80) NULL,
            EXT_PROP_NAME_2 VARCHAR (40) NULL,
            EXT_PROP_VALUE_2 VARCHAR (80) NULL,
            OWNER_PRINCIPAL VARCHAR (40) NULL,

    CONSTRAINT FRAGMENT_PK PRIMARY KEY(FRAGMENT_ID));

CREATE  INDEX IX_FRAGMENT_1 ON FRAGMENT (PARENT_ID);
CREATE  INDEX UN_FRAGMENT_1 ON FRAGMENT (PAGE_ID);




/* ---------------------------------------------------------------------- */
/* FRAGMENT_CONSTRAINT                                                      */
/* ---------------------------------------------------------------------- */

IF EXISTS (SELECT 1 FROM sysobjects WHERE type ='RI' AND name='FK_FRAGMENT_CONSTRAINT_1')
    ALTER TABLE FRAGMENT_CONSTRAINT DROP CONSTRAINT FK_FRAGMENT_CONSTRAINT_1;
IF EXISTS (SELECT 1 FROM sysobjects WHERE type = 'U' AND name = 'FRAGMENT_CONSTRAINT')
BEGIN
     DECLARE @reftable_15 nvarchar(60), @constraintname_15 nvarchar(60)
     DECLARE refcursor CURSOR FOR
     select reftables.name tablename, cons.name constraintname
      from sysobjects tables,
           sysobjects reftables,
           sysobjects cons,
           sysreferences ref
       where tables.id = ref.rkeyid
         and cons.id = ref.constid
         and reftables.id = ref.fkeyid
         and tables.name = 'FRAGMENT_CONSTRAINT'
     OPEN refcursor
     FETCH NEXT from refcursor into @reftable_15, @constraintname_15
     while @@FETCH_STATUS = 0
     BEGIN
       exec ('alter table '+@reftable_15+' drop constraint '+@constraintname_15)
       FETCH NEXT from refcursor into @reftable_15, @constraintname_15
     END
     CLOSE refcursor
     DEALLOCATE refcursor
     DROP TABLE FRAGMENT_CONSTRAINT
END
;

CREATE TABLE FRAGMENT_CONSTRAINT
(
            CONSTRAINT_ID INT NOT NULL,
            FRAGMENT_ID INT NOT NULL,
            APPLY_ORDER INT NOT NULL,
            USER_PRINCIPALS_ACL VARCHAR (120) NULL,
            ROLE_PRINCIPALS_ACL VARCHAR (120) NULL,
            GROUP_PRINCIPALS_ACL VARCHAR (120) NULL,
            PERMISSIONS_ACL VARCHAR (120) NULL,

    CONSTRAINT FRAGMENT_CONSTRAINT_PK PRIMARY KEY(CONSTRAINT_ID));

CREATE  INDEX IX_FRAGMENT_CONSTRAINT_1 ON FRAGMENT_CONSTRAINT (FRAGMENT_ID);




/* ---------------------------------------------------------------------- */
/* FRAGMENT_CONSTRAINTS_REF                                                      */
/* ---------------------------------------------------------------------- */

IF EXISTS (SELECT 1 FROM sysobjects WHERE type ='RI' AND name='FK_FRAGMENT_CONSTRAINTS_REF_1')
    ALTER TABLE FRAGMENT_CONSTRAINTS_REF DROP CONSTRAINT FK_FRAGMENT_CONSTRAINTS_REF_1;
IF EXISTS (SELECT 1 FROM sysobjects WHERE type = 'U' AND name = 'FRAGMENT_CONSTRAINTS_REF')
BEGIN
     DECLARE @reftable_16 nvarchar(60), @constraintname_16 nvarchar(60)
     DECLARE refcursor CURSOR FOR
     select reftables.name tablename, cons.name constraintname
      from sysobjects tables,
           sysobjects reftables,
           sysobjects cons,
           sysreferences ref
       where tables.id = ref.rkeyid
         and cons.id = ref.constid
         and reftables.id = ref.fkeyid
         and tables.name = 'FRAGMENT_CONSTRAINTS_REF'
     OPEN refcursor
     FETCH NEXT from refcursor into @reftable_16, @constraintname_16
     while @@FETCH_STATUS = 0
     BEGIN
       exec ('alter table '+@reftable_16+' drop constraint '+@constraintname_16)
       FETCH NEXT from refcursor into @reftable_16, @constraintname_16
     END
     CLOSE refcursor
     DEALLOCATE refcursor
     DROP TABLE FRAGMENT_CONSTRAINTS_REF
END
;

CREATE TABLE FRAGMENT_CONSTRAINTS_REF
(
            CONSTRAINTS_REF_ID INT NOT NULL,
            FRAGMENT_ID INT NOT NULL,
            APPLY_ORDER INT NOT NULL,
            NAME VARCHAR (40) NOT NULL,

    CONSTRAINT FRAGMENT_CONSTRAINTS_REF_PK PRIMARY KEY(CONSTRAINTS_REF_ID),
    UNIQUE (FRAGMENT_ID, NAME));

CREATE  INDEX IX_FRAGMENT_CONSTRAINTS_REF_1 ON FRAGMENT_CONSTRAINTS_REF (FRAGMENT_ID);




/* ---------------------------------------------------------------------- */
/* FRAGMENT_PREF                                                      */
/* ---------------------------------------------------------------------- */

IF EXISTS (SELECT 1 FROM sysobjects WHERE type ='RI' AND name='FK_FRAGMENT_PREF_1')
    ALTER TABLE FRAGMENT_PREF DROP CONSTRAINT FK_FRAGMENT_PREF_1;
IF EXISTS (SELECT 1 FROM sysobjects WHERE type = 'U' AND name = 'FRAGMENT_PREF')
BEGIN
     DECLARE @reftable_17 nvarchar(60), @constraintname_17 nvarchar(60)
     DECLARE refcursor CURSOR FOR
     select reftables.name tablename, cons.name constraintname
      from sysobjects tables,
           sysobjects reftables,
           sysobjects cons,
           sysreferences ref
       where tables.id = ref.rkeyid
         and cons.id = ref.constid
         and reftables.id = ref.fkeyid
         and tables.name = 'FRAGMENT_PREF'
     OPEN refcursor
     FETCH NEXT from refcursor into @reftable_17, @constraintname_17
     while @@FETCH_STATUS = 0
     BEGIN
       exec ('alter table '+@reftable_17+' drop constraint '+@constraintname_17)
       FETCH NEXT from refcursor into @reftable_17, @constraintname_17
     END
     CLOSE refcursor
     DEALLOCATE refcursor
     DROP TABLE FRAGMENT_PREF
END
;

CREATE TABLE FRAGMENT_PREF
(
            PREF_ID INT NOT NULL,
            FRAGMENT_ID INT NOT NULL,
            NAME VARCHAR (40) NOT NULL,
            IS_READ_ONLY INT NOT NULL,

    CONSTRAINT FRAGMENT_PREF_PK PRIMARY KEY(PREF_ID),
    UNIQUE (FRAGMENT_ID, NAME));

CREATE  INDEX IX_FRAGMENT_PREF_1 ON FRAGMENT_PREF (FRAGMENT_ID);




/* ---------------------------------------------------------------------- */
/* FRAGMENT_PREF_VALUE                                                      */
/* ---------------------------------------------------------------------- */

IF EXISTS (SELECT 1 FROM sysobjects WHERE type ='RI' AND name='FK_FRAGMENT_PREF_VALUE_1')
    ALTER TABLE FRAGMENT_PREF_VALUE DROP CONSTRAINT FK_FRAGMENT_PREF_VALUE_1;
IF EXISTS (SELECT 1 FROM sysobjects WHERE type = 'U' AND name = 'FRAGMENT_PREF_VALUE')
BEGIN
     DECLARE @reftable_18 nvarchar(60), @constraintname_18 nvarchar(60)
     DECLARE refcursor CURSOR FOR
     select reftables.name tablename, cons.name constraintname
      from sysobjects tables,
           sysobjects reftables,
           sysobjects cons,
           sysreferences ref
       where tables.id = ref.rkeyid
         and cons.id = ref.constid
         and reftables.id = ref.fkeyid
         and tables.name = 'FRAGMENT_PREF_VALUE'
     OPEN refcursor
     FETCH NEXT from refcursor into @reftable_18, @constraintname_18
     while @@FETCH_STATUS = 0
     BEGIN
       exec ('alter table '+@reftable_18+' drop constraint '+@constraintname_18)
       FETCH NEXT from refcursor into @reftable_18, @constraintname_18
     END
     CLOSE refcursor
     DEALLOCATE refcursor
     DROP TABLE FRAGMENT_PREF_VALUE
END
;

CREATE TABLE FRAGMENT_PREF_VALUE
(
            PREF_VALUE_ID INT NOT NULL,
            PREF_ID INT NOT NULL,
            VALUE_ORDER INT NOT NULL,
            VALUE VARCHAR (100) NOT NULL,

    CONSTRAINT FRAGMENT_PREF_VALUE_PK PRIMARY KEY(PREF_VALUE_ID));

CREATE  INDEX IX_FRAGMENT_PREF_VALUE_1 ON FRAGMENT_PREF_VALUE (PREF_ID);




/* ---------------------------------------------------------------------- */
/* LINK                                                      */
/* ---------------------------------------------------------------------- */

IF EXISTS (SELECT 1 FROM sysobjects WHERE type ='RI' AND name='FK_LINK_1')
    ALTER TABLE LINK DROP CONSTRAINT FK_LINK_1;
IF EXISTS (SELECT 1 FROM sysobjects WHERE type = 'U' AND name = 'LINK')
BEGIN
     DECLARE @reftable_19 nvarchar(60), @constraintname_19 nvarchar(60)
     DECLARE refcursor CURSOR FOR
     select reftables.name tablename, cons.name constraintname
      from sysobjects tables,
           sysobjects reftables,
           sysobjects cons,
           sysreferences ref
       where tables.id = ref.rkeyid
         and cons.id = ref.constid
         and reftables.id = ref.fkeyid
         and tables.name = 'LINK'
     OPEN refcursor
     FETCH NEXT from refcursor into @reftable_19, @constraintname_19
     while @@FETCH_STATUS = 0
     BEGIN
       exec ('alter table '+@reftable_19+' drop constraint '+@constraintname_19)
       FETCH NEXT from refcursor into @reftable_19, @constraintname_19
     END
     CLOSE refcursor
     DEALLOCATE refcursor
     DROP TABLE LINK
END
;

CREATE TABLE LINK
(
            LINK_ID INT NOT NULL,
            PARENT_ID INT NOT NULL,
            PATH VARCHAR (240) NOT NULL,
            NAME VARCHAR (80) NOT NULL,
            VERSION VARCHAR (40) NULL,
            TITLE VARCHAR (100) NULL,
            SHORT_TITLE VARCHAR (40) NULL,
            IS_HIDDEN INT NOT NULL,
            SKIN VARCHAR (80) NULL,
            TARGET VARCHAR (80) NULL,
            URL VARCHAR (255) NULL,
            SUBSITE VARCHAR (40) NULL,
            USER_PRINCIPAL VARCHAR (40) NULL,
            ROLE_PRINCIPAL VARCHAR (40) NULL,
            GROUP_PRINCIPAL VARCHAR (40) NULL,
            MEDIATYPE VARCHAR (15) NULL,
            LOCALE VARCHAR (20) NULL,
            EXT_ATTR_NAME VARCHAR (15) NULL,
            EXT_ATTR_VALUE VARCHAR (40) NULL,
            OWNER_PRINCIPAL VARCHAR (40) NULL,

    CONSTRAINT LINK_PK PRIMARY KEY(LINK_ID),
    UNIQUE (PATH));

CREATE  INDEX IX_LINK_1 ON LINK (PARENT_ID);




/* ---------------------------------------------------------------------- */
/* LINK_METADATA                                                      */
/* ---------------------------------------------------------------------- */

IF EXISTS (SELECT 1 FROM sysobjects WHERE type ='RI' AND name='FK_LINK_METADATA_1')
    ALTER TABLE LINK_METADATA DROP CONSTRAINT FK_LINK_METADATA_1;
IF EXISTS (SELECT 1 FROM sysobjects WHERE type = 'U' AND name = 'LINK_METADATA')
BEGIN
     DECLARE @reftable_20 nvarchar(60), @constraintname_20 nvarchar(60)
     DECLARE refcursor CURSOR FOR
     select reftables.name tablename, cons.name constraintname
      from sysobjects tables,
           sysobjects reftables,
           sysobjects cons,
           sysreferences ref
       where tables.id = ref.rkeyid
         and cons.id = ref.constid
         and reftables.id = ref.fkeyid
         and tables.name = 'LINK_METADATA'
     OPEN refcursor
     FETCH NEXT from refcursor into @reftable_20, @constraintname_20
     while @@FETCH_STATUS = 0
     BEGIN
       exec ('alter table '+@reftable_20+' drop constraint '+@constraintname_20)
       FETCH NEXT from refcursor into @reftable_20, @constraintname_20
     END
     CLOSE refcursor
     DEALLOCATE refcursor
     DROP TABLE LINK_METADATA
END
;

CREATE TABLE LINK_METADATA
(
            METADATA_ID INT NOT NULL,
            LINK_ID INT NOT NULL,
            NAME VARCHAR (15) NOT NULL,
            LOCALE VARCHAR (20) NOT NULL,
            VALUE VARCHAR (100) NOT NULL,

    CONSTRAINT LINK_METADATA_PK PRIMARY KEY(METADATA_ID),
    UNIQUE (LINK_ID, NAME, LOCALE, VALUE));

CREATE  INDEX IX_LINK_METADATA_1 ON LINK_METADATA (LINK_ID);




/* ---------------------------------------------------------------------- */
/* LINK_CONSTRAINT                                                      */
/* ---------------------------------------------------------------------- */

IF EXISTS (SELECT 1 FROM sysobjects WHERE type ='RI' AND name='FK_LINK_CONSTRAINT_1')
    ALTER TABLE LINK_CONSTRAINT DROP CONSTRAINT FK_LINK_CONSTRAINT_1;
IF EXISTS (SELECT 1 FROM sysobjects WHERE type = 'U' AND name = 'LINK_CONSTRAINT')
BEGIN
     DECLARE @reftable_21 nvarchar(60), @constraintname_21 nvarchar(60)
     DECLARE refcursor CURSOR FOR
     select reftables.name tablename, cons.name constraintname
      from sysobjects tables,
           sysobjects reftables,
           sysobjects cons,
           sysreferences ref
       where tables.id = ref.rkeyid
         and cons.id = ref.constid
         and reftables.id = ref.fkeyid
         and tables.name = 'LINK_CONSTRAINT'
     OPEN refcursor
     FETCH NEXT from refcursor into @reftable_21, @constraintname_21
     while @@FETCH_STATUS = 0
     BEGIN
       exec ('alter table '+@reftable_21+' drop constraint '+@constraintname_21)
       FETCH NEXT from refcursor into @reftable_21, @constraintname_21
     END
     CLOSE refcursor
     DEALLOCATE refcursor
     DROP TABLE LINK_CONSTRAINT
END
;

CREATE TABLE LINK_CONSTRAINT
(
            CONSTRAINT_ID INT NOT NULL,
            LINK_ID INT NOT NULL,
            APPLY_ORDER INT NOT NULL,
            USER_PRINCIPALS_ACL VARCHAR (120) NULL,
            ROLE_PRINCIPALS_ACL VARCHAR (120) NULL,
            GROUP_PRINCIPALS_ACL VARCHAR (120) NULL,
            PERMISSIONS_ACL VARCHAR (120) NULL,

    CONSTRAINT LINK_CONSTRAINT_PK PRIMARY KEY(CONSTRAINT_ID));

CREATE  INDEX IX_LINK_CONSTRAINT_1 ON LINK_CONSTRAINT (LINK_ID);




/* ---------------------------------------------------------------------- */
/* LINK_CONSTRAINTS_REF                                                      */
/* ---------------------------------------------------------------------- */

IF EXISTS (SELECT 1 FROM sysobjects WHERE type ='RI' AND name='FK_LINK_CONSTRAINTS_REF_1')
    ALTER TABLE LINK_CONSTRAINTS_REF DROP CONSTRAINT FK_LINK_CONSTRAINTS_REF_1;
IF EXISTS (SELECT 1 FROM sysobjects WHERE type = 'U' AND name = 'LINK_CONSTRAINTS_REF')
BEGIN
     DECLARE @reftable_22 nvarchar(60), @constraintname_22 nvarchar(60)
     DECLARE refcursor CURSOR FOR
     select reftables.name tablename, cons.name constraintname
      from sysobjects tables,
           sysobjects reftables,
           sysobjects cons,
           sysreferences ref
       where tables.id = ref.rkeyid
         and cons.id = ref.constid
         and reftables.id = ref.fkeyid
         and tables.name = 'LINK_CONSTRAINTS_REF'
     OPEN refcursor
     FETCH NEXT from refcursor into @reftable_22, @constraintname_22
     while @@FETCH_STATUS = 0
     BEGIN
       exec ('alter table '+@reftable_22+' drop constraint '+@constraintname_22)
       FETCH NEXT from refcursor into @reftable_22, @constraintname_22
     END
     CLOSE refcursor
     DEALLOCATE refcursor
     DROP TABLE LINK_CONSTRAINTS_REF
END
;

CREATE TABLE LINK_CONSTRAINTS_REF
(
            CONSTRAINTS_REF_ID INT NOT NULL,
            LINK_ID INT NOT NULL,
            APPLY_ORDER INT NOT NULL,
            NAME VARCHAR (40) NOT NULL,

    CONSTRAINT LINK_CONSTRAINTS_REF_PK PRIMARY KEY(CONSTRAINTS_REF_ID),
    UNIQUE (LINK_ID, NAME));

CREATE  INDEX IX_LINK_CONSTRAINTS_REF_1 ON LINK_CONSTRAINTS_REF (LINK_ID);




/* ---------------------------------------------------------------------- */
/* PAGE_SECURITY                                                      */
/* ---------------------------------------------------------------------- */

IF EXISTS (SELECT 1 FROM sysobjects WHERE type ='RI' AND name='FK_PAGE_SECURITY_1')
    ALTER TABLE PAGE_SECURITY DROP CONSTRAINT FK_PAGE_SECURITY_1;
IF EXISTS (SELECT 1 FROM sysobjects WHERE type = 'U' AND name = 'PAGE_SECURITY')
BEGIN
     DECLARE @reftable_23 nvarchar(60), @constraintname_23 nvarchar(60)
     DECLARE refcursor CURSOR FOR
     select reftables.name tablename, cons.name constraintname
      from sysobjects tables,
           sysobjects reftables,
           sysobjects cons,
           sysreferences ref
       where tables.id = ref.rkeyid
         and cons.id = ref.constid
         and reftables.id = ref.fkeyid
         and tables.name = 'PAGE_SECURITY'
     OPEN refcursor
     FETCH NEXT from refcursor into @reftable_23, @constraintname_23
     while @@FETCH_STATUS = 0
     BEGIN
       exec ('alter table '+@reftable_23+' drop constraint '+@constraintname_23)
       FETCH NEXT from refcursor into @reftable_23, @constraintname_23
     END
     CLOSE refcursor
     DEALLOCATE refcursor
     DROP TABLE PAGE_SECURITY
END
;

CREATE TABLE PAGE_SECURITY
(
            PAGE_SECURITY_ID INT NOT NULL,
            PARENT_ID INT NOT NULL,
            PATH VARCHAR (240) NOT NULL,
            NAME VARCHAR (80) NOT NULL,
            VERSION VARCHAR (40) NULL,
            SUBSITE VARCHAR (40) NULL,
            USER_PRINCIPAL VARCHAR (40) NULL,
            ROLE_PRINCIPAL VARCHAR (40) NULL,
            GROUP_PRINCIPAL VARCHAR (40) NULL,
            MEDIATYPE VARCHAR (15) NULL,
            LOCALE VARCHAR (20) NULL,
            EXT_ATTR_NAME VARCHAR (15) NULL,
            EXT_ATTR_VALUE VARCHAR (40) NULL,

    CONSTRAINT PAGE_SECURITY_PK PRIMARY KEY(PAGE_SECURITY_ID),
    UNIQUE (PARENT_ID),
    UNIQUE (PATH));





/* ---------------------------------------------------------------------- */
/* PAGE_SEC_CONSTRAINTS_DEF                                                      */
/* ---------------------------------------------------------------------- */

IF EXISTS (SELECT 1 FROM sysobjects WHERE type ='RI' AND name='FK_PAGE_SEC_CONSTRAINTS_DEF_1')
    ALTER TABLE PAGE_SEC_CONSTRAINTS_DEF DROP CONSTRAINT FK_PAGE_SEC_CONSTRAINTS_DEF_1;
IF EXISTS (SELECT 1 FROM sysobjects WHERE type = 'U' AND name = 'PAGE_SEC_CONSTRAINTS_DEF')
BEGIN
     DECLARE @reftable_24 nvarchar(60), @constraintname_24 nvarchar(60)
     DECLARE refcursor CURSOR FOR
     select reftables.name tablename, cons.name constraintname
      from sysobjects tables,
           sysobjects reftables,
           sysobjects cons,
           sysreferences ref
       where tables.id = ref.rkeyid
         and cons.id = ref.constid
         and reftables.id = ref.fkeyid
         and tables.name = 'PAGE_SEC_CONSTRAINTS_DEF'
     OPEN refcursor
     FETCH NEXT from refcursor into @reftable_24, @constraintname_24
     while @@FETCH_STATUS = 0
     BEGIN
       exec ('alter table '+@reftable_24+' drop constraint '+@constraintname_24)
       FETCH NEXT from refcursor into @reftable_24, @constraintname_24
     END
     CLOSE refcursor
     DEALLOCATE refcursor
     DROP TABLE PAGE_SEC_CONSTRAINTS_DEF
END
;

CREATE TABLE PAGE_SEC_CONSTRAINTS_DEF
(
            CONSTRAINTS_DEF_ID INT NOT NULL,
            PAGE_SECURITY_ID INT NOT NULL,
            NAME VARCHAR (40) NOT NULL,

    CONSTRAINT PAGE_SEC_CONSTRAINTS_DEF_PK PRIMARY KEY(CONSTRAINTS_DEF_ID),
    UNIQUE (PAGE_SECURITY_ID, NAME));

CREATE  INDEX IX_PAGE_SEC_CONSTRAINTS_DEF_1 ON PAGE_SEC_CONSTRAINTS_DEF (PAGE_SECURITY_ID);




/* ---------------------------------------------------------------------- */
/* PAGE_SEC_CONSTRAINT_DEF                                                      */
/* ---------------------------------------------------------------------- */

IF EXISTS (SELECT 1 FROM sysobjects WHERE type ='RI' AND name='FK_PAGE_SEC_CONSTRAINT_DEF_1')
    ALTER TABLE PAGE_SEC_CONSTRAINT_DEF DROP CONSTRAINT FK_PAGE_SEC_CONSTRAINT_DEF_1;
IF EXISTS (SELECT 1 FROM sysobjects WHERE type = 'U' AND name = 'PAGE_SEC_CONSTRAINT_DEF')
BEGIN
     DECLARE @reftable_25 nvarchar(60), @constraintname_25 nvarchar(60)
     DECLARE refcursor CURSOR FOR
     select reftables.name tablename, cons.name constraintname
      from sysobjects tables,
           sysobjects reftables,
           sysobjects cons,
           sysreferences ref
       where tables.id = ref.rkeyid
         and cons.id = ref.constid
         and reftables.id = ref.fkeyid
         and tables.name = 'PAGE_SEC_CONSTRAINT_DEF'
     OPEN refcursor
     FETCH NEXT from refcursor into @reftable_25, @constraintname_25
     while @@FETCH_STATUS = 0
     BEGIN
       exec ('alter table '+@reftable_25+' drop constraint '+@constraintname_25)
       FETCH NEXT from refcursor into @reftable_25, @constraintname_25
     END
     CLOSE refcursor
     DEALLOCATE refcursor
     DROP TABLE PAGE_SEC_CONSTRAINT_DEF
END
;

CREATE TABLE PAGE_SEC_CONSTRAINT_DEF
(
            CONSTRAINT_DEF_ID INT NOT NULL,
            CONSTRAINTS_DEF_ID INT NOT NULL,
            APPLY_ORDER INT NOT NULL,
            USER_PRINCIPALS_ACL VARCHAR (120) NULL,
            ROLE_PRINCIPALS_ACL VARCHAR (120) NULL,
            GROUP_PRINCIPALS_ACL VARCHAR (120) NULL,
            PERMISSIONS_ACL VARCHAR (120) NULL,

    CONSTRAINT PAGE_SEC_CONSTRAINT_DEF_PK PRIMARY KEY(CONSTRAINT_DEF_ID));

CREATE  INDEX IX_PAGE_SEC_CONSTRAINT_DEF_1 ON PAGE_SEC_CONSTRAINT_DEF (CONSTRAINTS_DEF_ID);




/* ---------------------------------------------------------------------- */
/* PAGE_SEC_CONSTRAINTS_REF                                                      */
/* ---------------------------------------------------------------------- */

IF EXISTS (SELECT 1 FROM sysobjects WHERE type ='RI' AND name='FK_PAGE_SEC_CONSTRAINTS_REF_1')
    ALTER TABLE PAGE_SEC_CONSTRAINTS_REF DROP CONSTRAINT FK_PAGE_SEC_CONSTRAINTS_REF_1;
IF EXISTS (SELECT 1 FROM sysobjects WHERE type = 'U' AND name = 'PAGE_SEC_CONSTRAINTS_REF')
BEGIN
     DECLARE @reftable_26 nvarchar(60), @constraintname_26 nvarchar(60)
     DECLARE refcursor CURSOR FOR
     select reftables.name tablename, cons.name constraintname
      from sysobjects tables,
           sysobjects reftables,
           sysobjects cons,
           sysreferences ref
       where tables.id = ref.rkeyid
         and cons.id = ref.constid
         and reftables.id = ref.fkeyid
         and tables.name = 'PAGE_SEC_CONSTRAINTS_REF'
     OPEN refcursor
     FETCH NEXT from refcursor into @reftable_26, @constraintname_26
     while @@FETCH_STATUS = 0
     BEGIN
       exec ('alter table '+@reftable_26+' drop constraint '+@constraintname_26)
       FETCH NEXT from refcursor into @reftable_26, @constraintname_26
     END
     CLOSE refcursor
     DEALLOCATE refcursor
     DROP TABLE PAGE_SEC_CONSTRAINTS_REF
END
;

CREATE TABLE PAGE_SEC_CONSTRAINTS_REF
(
            CONSTRAINTS_REF_ID INT NOT NULL,
            PAGE_SECURITY_ID INT NOT NULL,
            APPLY_ORDER INT NOT NULL,
            NAME VARCHAR (40) NOT NULL,

    CONSTRAINT PAGE_SEC_CONSTRAINTS_REF_PK PRIMARY KEY(CONSTRAINTS_REF_ID),
    UNIQUE (PAGE_SECURITY_ID, NAME));

CREATE  INDEX IX_PAGE_SEC_CONSTRAINTS_REF_1 ON PAGE_SEC_CONSTRAINTS_REF (PAGE_SECURITY_ID);




/* ---------------------------------------------------------------------- */
/* PROFILING_RULE                                                      */
/* ---------------------------------------------------------------------- */

IF EXISTS (SELECT 1 FROM sysobjects WHERE type = 'U' AND name = 'PROFILING_RULE')
BEGIN
     DECLARE @reftable_27 nvarchar(60), @constraintname_27 nvarchar(60)
     DECLARE refcursor CURSOR FOR
     select reftables.name tablename, cons.name constraintname
      from sysobjects tables,
           sysobjects reftables,
           sysobjects cons,
           sysreferences ref
       where tables.id = ref.rkeyid
         and cons.id = ref.constid
         and reftables.id = ref.fkeyid
         and tables.name = 'PROFILING_RULE'
     OPEN refcursor
     FETCH NEXT from refcursor into @reftable_27, @constraintname_27
     while @@FETCH_STATUS = 0
     BEGIN
       exec ('alter table '+@reftable_27+' drop constraint '+@constraintname_27)
       FETCH NEXT from refcursor into @reftable_27, @constraintname_27
     END
     CLOSE refcursor
     DEALLOCATE refcursor
     DROP TABLE PROFILING_RULE
END
;

CREATE TABLE PROFILING_RULE
(
            RULE_ID VARCHAR (80) NOT NULL,
            CLASS_NAME VARCHAR (100) NOT NULL,
            TITLE VARCHAR (100) NULL,

    CONSTRAINT PROFILING_RULE_PK PRIMARY KEY(RULE_ID));





/* ---------------------------------------------------------------------- */
/* RULE_CRITERION                                                      */
/* ---------------------------------------------------------------------- */

IF EXISTS (SELECT 1 FROM sysobjects WHERE type ='RI' AND name='FK_RULE_CRITERION_1')
    ALTER TABLE RULE_CRITERION DROP CONSTRAINT FK_RULE_CRITERION_1;
IF EXISTS (SELECT 1 FROM sysobjects WHERE type = 'U' AND name = 'RULE_CRITERION')
BEGIN
     DECLARE @reftable_28 nvarchar(60), @constraintname_28 nvarchar(60)
     DECLARE refcursor CURSOR FOR
     select reftables.name tablename, cons.name constraintname
      from sysobjects tables,
           sysobjects reftables,
           sysobjects cons,
           sysreferences ref
       where tables.id = ref.rkeyid
         and cons.id = ref.constid
         and reftables.id = ref.fkeyid
         and tables.name = 'RULE_CRITERION'
     OPEN refcursor
     FETCH NEXT from refcursor into @reftable_28, @constraintname_28
     while @@FETCH_STATUS = 0
     BEGIN
       exec ('alter table '+@reftable_28+' drop constraint '+@constraintname_28)
       FETCH NEXT from refcursor into @reftable_28, @constraintname_28
     END
     CLOSE refcursor
     DEALLOCATE refcursor
     DROP TABLE RULE_CRITERION
END
;

CREATE TABLE RULE_CRITERION
(
            CRITERION_ID VARCHAR (80) NOT NULL,
            RULE_ID VARCHAR (80) NOT NULL,
            FALLBACK_ORDER INT NOT NULL,
            REQUEST_TYPE VARCHAR (40) NOT NULL,
            NAME VARCHAR (80) NOT NULL,
            COLUMN_VALUE VARCHAR (128) NULL,
            FALLBACK_TYPE INT default 1 NULL,

    CONSTRAINT RULE_CRITERION_PK PRIMARY KEY(CRITERION_ID));

CREATE  INDEX IX_RULE_CRITERION_1 ON RULE_CRITERION (RULE_ID, FALLBACK_ORDER);




/* ---------------------------------------------------------------------- */
/* PRINCIPAL_RULE_ASSOC                                                      */
/* ---------------------------------------------------------------------- */

IF EXISTS (SELECT 1 FROM sysobjects WHERE type = 'U' AND name = 'PRINCIPAL_RULE_ASSOC')
BEGIN
     DECLARE @reftable_29 nvarchar(60), @constraintname_29 nvarchar(60)
     DECLARE refcursor CURSOR FOR
     select reftables.name tablename, cons.name constraintname
      from sysobjects tables,
           sysobjects reftables,
           sysobjects cons,
           sysreferences ref
       where tables.id = ref.rkeyid
         and cons.id = ref.constid
         and reftables.id = ref.fkeyid
         and tables.name = 'PRINCIPAL_RULE_ASSOC'
     OPEN refcursor
     FETCH NEXT from refcursor into @reftable_29, @constraintname_29
     while @@FETCH_STATUS = 0
     BEGIN
       exec ('alter table '+@reftable_29+' drop constraint '+@constraintname_29)
       FETCH NEXT from refcursor into @reftable_29, @constraintname_29
     END
     CLOSE refcursor
     DEALLOCATE refcursor
     DROP TABLE PRINCIPAL_RULE_ASSOC
END
;

CREATE TABLE PRINCIPAL_RULE_ASSOC
(
            PRINCIPAL_NAME VARCHAR (80) NOT NULL,
            LOCATOR_NAME VARCHAR (80) NOT NULL,
            RULE_ID VARCHAR (80) NOT NULL,

    CONSTRAINT PRINCIPAL_RULE_ASSOC_PK PRIMARY KEY(PRINCIPAL_NAME,LOCATOR_NAME));





/* ---------------------------------------------------------------------- */
/* PROFILE_PAGE_ASSOC                                                      */
/* ---------------------------------------------------------------------- */

IF EXISTS (SELECT 1 FROM sysobjects WHERE type = 'U' AND name = 'PROFILE_PAGE_ASSOC')
BEGIN
     DECLARE @reftable_30 nvarchar(60), @constraintname_30 nvarchar(60)
     DECLARE refcursor CURSOR FOR
     select reftables.name tablename, cons.name constraintname
      from sysobjects tables,
           sysobjects reftables,
           sysobjects cons,
           sysreferences ref
       where tables.id = ref.rkeyid
         and cons.id = ref.constid
         and reftables.id = ref.fkeyid
         and tables.name = 'PROFILE_PAGE_ASSOC'
     OPEN refcursor
     FETCH NEXT from refcursor into @reftable_30, @constraintname_30
     while @@FETCH_STATUS = 0
     BEGIN
       exec ('alter table '+@reftable_30+' drop constraint '+@constraintname_30)
       FETCH NEXT from refcursor into @reftable_30, @constraintname_30
     END
     CLOSE refcursor
     DEALLOCATE refcursor
     DROP TABLE PROFILE_PAGE_ASSOC
END
;

CREATE TABLE PROFILE_PAGE_ASSOC
(
            LOCATOR_HASH VARCHAR (40) NOT NULL,
            PAGE_ID VARCHAR (80) NOT NULL,

    UNIQUE (LOCATOR_HASH, PAGE_ID));





/* ---------------------------------------------------------------------- */
/* CLUBS                                                      */
/* ---------------------------------------------------------------------- */

IF EXISTS (SELECT 1 FROM sysobjects WHERE type = 'U' AND name = 'CLUBS')
BEGIN
     DECLARE @reftable_31 nvarchar(60), @constraintname_31 nvarchar(60)
     DECLARE refcursor CURSOR FOR
     select reftables.name tablename, cons.name constraintname
      from sysobjects tables,
           sysobjects reftables,
           sysobjects cons,
           sysreferences ref
       where tables.id = ref.rkeyid
         and cons.id = ref.constid
         and reftables.id = ref.fkeyid
         and tables.name = 'CLUBS'
     OPEN refcursor
     FETCH NEXT from refcursor into @reftable_31, @constraintname_31
     while @@FETCH_STATUS = 0
     BEGIN
       exec ('alter table '+@reftable_31+' drop constraint '+@constraintname_31)
       FETCH NEXT from refcursor into @reftable_31, @constraintname_31
     END
     CLOSE refcursor
     DEALLOCATE refcursor
     DROP TABLE CLUBS
END
;

CREATE TABLE CLUBS
(
            NAME VARCHAR (80) NOT NULL,
            COUNTRY VARCHAR (40) NOT NULL,
            CITY VARCHAR (40) NOT NULL,
            STADIUM VARCHAR (80) NOT NULL,
            CAPACITY INT NULL,
            FOUNDED INT NULL,
            PITCH VARCHAR (40) NULL,
            NICKNAME VARCHAR (40) NULL,

    CONSTRAINT CLUBS_PK PRIMARY KEY(NAME));





/* ---------------------------------------------------------------------- */
/* CLUBS                                                      */
/* ---------------------------------------------------------------------- 
*/


BEGIN
ALTER TABLE FOLDER
    ADD CONSTRAINT FK_FOLDER_1 FOREIGN KEY (PARENT_ID)
    REFERENCES FOLDER (FOLDER_ID)
    ON DELETE NO ACTION ON UPDATE NO ACTION
END    
;




/* ---------------------------------------------------------------------- */
/* FOLDER                                                      */
/* ---------------------------------------------------------------------- */

BEGIN
ALTER TABLE FOLDER_METADATA
    ADD CONSTRAINT FK_FOLDER_METADATA_1 FOREIGN KEY (FOLDER_ID)
    REFERENCES FOLDER (FOLDER_ID)
    ON DELETE CASCADE 
END    
;




/* ---------------------------------------------------------------------- */
/* FOLDER_METADATA                                                      */
/* ---------------------------------------------------------------------- */

BEGIN
ALTER TABLE FOLDER_CONSTRAINT
    ADD CONSTRAINT FK_FOLDER_CONSTRAINT_1 FOREIGN KEY (FOLDER_ID)
    REFERENCES FOLDER (FOLDER_ID)
    ON DELETE CASCADE 
END    
;




/* ---------------------------------------------------------------------- */
/* FOLDER_CONSTRAINT                                                      */
/* ---------------------------------------------------------------------- */

BEGIN
ALTER TABLE FOLDER_CONSTRAINTS_REF
    ADD CONSTRAINT FK_FOLDER_CONSTRAINTS_REF_1 FOREIGN KEY (FOLDER_ID)
    REFERENCES FOLDER (FOLDER_ID)
    ON DELETE CASCADE 
END    
;




/* ---------------------------------------------------------------------- */
/* FOLDER_CONSTRAINTS_REF                                                      */
/* ---------------------------------------------------------------------- */

BEGIN
ALTER TABLE FOLDER_ORDER
    ADD CONSTRAINT FK_FOLDER_ORDER_1 FOREIGN KEY (FOLDER_ID)
    REFERENCES FOLDER (FOLDER_ID)
    ON DELETE CASCADE 
END    
;




/* ---------------------------------------------------------------------- */
/* FOLDER_ORDER                                                      */
/* ---------------------------------------------------------------------- */


BEGIN
ALTER TABLE FOLDER_MENU
    ADD CONSTRAINT FK_FOLDER_MENU_1 FOREIGN KEY (PARENT_ID)
    REFERENCES FOLDER_MENU (MENU_ID)
    ON DELETE NO ACTION ON UPDATE NO ACTION
END    
;

BEGIN
ALTER TABLE FOLDER_MENU
    ADD CONSTRAINT FK_FOLDER_MENU_2 FOREIGN KEY (FOLDER_ID)
    REFERENCES FOLDER (FOLDER_ID)
    ON DELETE CASCADE 
END    
;




/* ---------------------------------------------------------------------- */
/* FOLDER_MENU                                                      */
/* ---------------------------------------------------------------------- */

BEGIN
ALTER TABLE FOLDER_MENU_METADATA
    ADD CONSTRAINT FK_FOLDER_MENU_METADATA_1 FOREIGN KEY (MENU_ID)
    REFERENCES FOLDER_MENU (MENU_ID)
    ON DELETE CASCADE 
END    
;




/* ---------------------------------------------------------------------- */
/* FOLDER_MENU_METADATA                                                      */
/* ---------------------------------------------------------------------- */

BEGIN
ALTER TABLE PAGE
    ADD CONSTRAINT FK_PAGE_1 FOREIGN KEY (PARENT_ID)
    REFERENCES FOLDER (FOLDER_ID)
    ON DELETE CASCADE 
END    
;




/* ---------------------------------------------------------------------- */
/* PAGE                                                      */
/* ---------------------------------------------------------------------- */

BEGIN
ALTER TABLE PAGE_METADATA
    ADD CONSTRAINT FK_PAGE_METADATA_1 FOREIGN KEY (PAGE_ID)
    REFERENCES PAGE (PAGE_ID)
    ON DELETE CASCADE 
END    
;




/* ---------------------------------------------------------------------- */
/* PAGE_METADATA                                                      */
/* ---------------------------------------------------------------------- */

BEGIN
ALTER TABLE PAGE_CONSTRAINT
    ADD CONSTRAINT FK_PAGE_CONSTRAINT_1 FOREIGN KEY (PAGE_ID)
    REFERENCES PAGE (PAGE_ID)
    ON DELETE CASCADE 
END    
;




/* ---------------------------------------------------------------------- */
/* PAGE_CONSTRAINT                                                      */
/* ---------------------------------------------------------------------- */

BEGIN
ALTER TABLE PAGE_CONSTRAINTS_REF
    ADD CONSTRAINT FK_PAGE_CONSTRAINTS_REF_1 FOREIGN KEY (PAGE_ID)
    REFERENCES PAGE (PAGE_ID)
    ON DELETE CASCADE 
END    
;




/* ---------------------------------------------------------------------- */
/* PAGE_CONSTRAINTS_REF                                                      */
/* ---------------------------------------------------------------------- */


BEGIN
ALTER TABLE PAGE_MENU
    ADD CONSTRAINT FK_PAGE_MENU_1 FOREIGN KEY (PARENT_ID)
    REFERENCES PAGE_MENU (MENU_ID)
    ON DELETE NO ACTION ON UPDATE NO ACTION
END    
;

BEGIN
ALTER TABLE PAGE_MENU
    ADD CONSTRAINT PM_M_FK_PAGE_ID_PAGE FOREIGN KEY (PAGE_ID)
    REFERENCES PAGE (PAGE_ID)
    ON DELETE CASCADE 
END    
;




/* ---------------------------------------------------------------------- */
/* PAGE_MENU                                                      */
/* ---------------------------------------------------------------------- */

BEGIN
ALTER TABLE PAGE_MENU_METADATA
    ADD CONSTRAINT FK_PAGE_MENU_METADATA_1 FOREIGN KEY (MENU_ID)
    REFERENCES PAGE_MENU (MENU_ID)
    ON DELETE CASCADE 
END    
;




/* ---------------------------------------------------------------------- */
/* PAGE_MENU_METADATA                                                      */
/* ---------------------------------------------------------------------- */

BEGIN
ALTER TABLE FRAGMENT
    ADD CONSTRAINT FK_FRAGMENT_1 FOREIGN KEY (PARENT_ID)
    REFERENCES FRAGMENT (FRAGMENT_ID)
    ON DELETE NO ACTION ON UPDATE NO ACTION
END    
;

BEGIN
ALTER TABLE FRAGMENT
    ADD CONSTRAINT FK_FRAGMENT_2 FOREIGN KEY (PAGE_ID)
    REFERENCES PAGE (PAGE_ID)
    ON DELETE CASCADE 
END    
;




/* ---------------------------------------------------------------------- */
/* FRAGMENT                                                      */
/* ---------------------------------------------------------------------- */

BEGIN
ALTER TABLE FRAGMENT_CONSTRAINT
    ADD CONSTRAINT FK_FRAGMENT_CONSTRAINT_1 FOREIGN KEY (FRAGMENT_ID)
    REFERENCES FRAGMENT (FRAGMENT_ID)
    ON DELETE CASCADE 
END    
;




/* ---------------------------------------------------------------------- */
/* FRAGMENT_CONSTRAINT                                                      */
/* ---------------------------------------------------------------------- */

BEGIN
ALTER TABLE FRAGMENT_CONSTRAINTS_REF
    ADD CONSTRAINT FK_FRAGMENT_CONSTRAINTS_REF_1 FOREIGN KEY (FRAGMENT_ID)
    REFERENCES FRAGMENT (FRAGMENT_ID)
    ON DELETE CASCADE 
END    
;




/* ---------------------------------------------------------------------- */
/* FRAGMENT_CONSTRAINTS_REF                                                      */
/* ---------------------------------------------------------------------- */

BEGIN
ALTER TABLE FRAGMENT_PREF
    ADD CONSTRAINT FK_FRAGMENT_PREF_1 FOREIGN KEY (FRAGMENT_ID)
    REFERENCES FRAGMENT (FRAGMENT_ID)
    ON DELETE CASCADE 
END    
;




/* ---------------------------------------------------------------------- */
/* FRAGMENT_PREF                                                      */
/* ---------------------------------------------------------------------- */

BEGIN
ALTER TABLE FRAGMENT_PREF_VALUE
    ADD CONSTRAINT FK_FRAGMENT_PREF_VALUE_1 FOREIGN KEY (PREF_ID)
    REFERENCES FRAGMENT_PREF (PREF_ID)
    ON DELETE CASCADE 
END    
;




/* ---------------------------------------------------------------------- */
/* FRAGMENT_PREF_VALUE                                                      */
/* ---------------------------------------------------------------------- */

BEGIN
ALTER TABLE LINK
    ADD CONSTRAINT FK_LINK_1 FOREIGN KEY (PARENT_ID)
    REFERENCES FOLDER (FOLDER_ID)
    ON DELETE CASCADE 
END    
;




/* ---------------------------------------------------------------------- */
/* LINK                                                      */
/* ---------------------------------------------------------------------- */

BEGIN
ALTER TABLE LINK_METADATA
    ADD CONSTRAINT FK_LINK_METADATA_1 FOREIGN KEY (LINK_ID)
    REFERENCES LINK (LINK_ID)
    ON DELETE CASCADE 
END    
;




/* ---------------------------------------------------------------------- */
/* LINK_METADATA                                                      */
/* ---------------------------------------------------------------------- */

BEGIN
ALTER TABLE LINK_CONSTRAINT
    ADD CONSTRAINT FK_LINK_CONSTRAINT_1 FOREIGN KEY (LINK_ID)
    REFERENCES LINK (LINK_ID)
    ON DELETE CASCADE 
END    
;




/* ---------------------------------------------------------------------- */
/* LINK_CONSTRAINT                                                      */
/* ---------------------------------------------------------------------- */

BEGIN
ALTER TABLE LINK_CONSTRAINTS_REF
    ADD CONSTRAINT FK_LINK_CONSTRAINTS_REF_1 FOREIGN KEY (LINK_ID)
    REFERENCES LINK (LINK_ID)
    ON DELETE CASCADE 
END    
;




/* ---------------------------------------------------------------------- */
/* LINK_CONSTRAINTS_REF                                                      */
/* ---------------------------------------------------------------------- */

BEGIN
ALTER TABLE PAGE_SECURITY
    ADD CONSTRAINT FK_PAGE_SECURITY_1 FOREIGN KEY (PARENT_ID)
    REFERENCES FOLDER (FOLDER_ID)
    ON DELETE CASCADE 
END    
;




/* ---------------------------------------------------------------------- */
/* PAGE_SECURITY                                                      */
/* ---------------------------------------------------------------------- */

BEGIN
ALTER TABLE PAGE_SEC_CONSTRAINTS_DEF
    ADD CONSTRAINT FK_PAGE_SEC_CONSTRAINTS_DEF_1 FOREIGN KEY (PAGE_SECURITY_ID)
    REFERENCES PAGE_SECURITY (PAGE_SECURITY_ID)
    ON DELETE CASCADE 
END    
;




/* ---------------------------------------------------------------------- */
/* PAGE_SEC_CONSTRAINTS_DEF                                                      */
/* ---------------------------------------------------------------------- */

BEGIN
ALTER TABLE PAGE_SEC_CONSTRAINT_DEF
    ADD CONSTRAINT FK_PAGE_SEC_CONSTRAINT_DEF_1 FOREIGN KEY (CONSTRAINTS_DEF_ID)
    REFERENCES PAGE_SEC_CONSTRAINTS_DEF (CONSTRAINTS_DEF_ID)
    ON DELETE CASCADE 
END    
;




/* ---------------------------------------------------------------------- */
/* PAGE_SEC_CONSTRAINT_DEF                                                      */
/* ---------------------------------------------------------------------- */

BEGIN
ALTER TABLE PAGE_SEC_CONSTRAINTS_REF
    ADD CONSTRAINT FK_PAGE_SEC_CONSTRAINTS_REF_1 FOREIGN KEY (PAGE_SECURITY_ID)
    REFERENCES PAGE_SECURITY (PAGE_SECURITY_ID)
    ON DELETE CASCADE 
END    
;




/* ---------------------------------------------------------------------- */
/* PAGE_SEC_CONSTRAINTS_REF                                                      */
/* ---------------------------------------------------------------------- */




/* ---------------------------------------------------------------------- */
/* PROFILING_RULE                                                      */
/* ---------------------------------------------------------------------- */

BEGIN
ALTER TABLE RULE_CRITERION
    ADD CONSTRAINT FK_RULE_CRITERION_1 FOREIGN KEY (RULE_ID)
    REFERENCES PROFILING_RULE (RULE_ID)
    ON DELETE CASCADE 
END    
;




/* ---------------------------------------------------------------------- */
/* RULE_CRITERION                                                      */
/* ---------------------------------------------------------------------- */




/* ---------------------------------------------------------------------- */
/* PRINCIPAL_RULE_ASSOC                                                      */
/* ---------------------------------------------------------------------- */




/* ---------------------------------------------------------------------- */
/* PROFILE_PAGE_ASSOC                                                      */
/* ---------------------------------------------------------------------- */



