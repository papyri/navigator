
/* ---------------------------------------------------------------------- */
/* SECURITY_PRINCIPAL                                                      */
/* ---------------------------------------------------------------------- */

IF EXISTS (SELECT 1 FROM sysobjects WHERE type = 'U' AND name = 'SECURITY_PRINCIPAL')
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
         and tables.name = 'SECURITY_PRINCIPAL'
     OPEN refcursor
     FETCH NEXT from refcursor into @reftable_1, @constraintname_1
     while @@FETCH_STATUS = 0
     BEGIN
       exec ('alter table '+@reftable_1+' drop constraint '+@constraintname_1)
       FETCH NEXT from refcursor into @reftable_1, @constraintname_1
     END
     CLOSE refcursor
     DEALLOCATE refcursor
     DROP TABLE SECURITY_PRINCIPAL
END
;

CREATE TABLE SECURITY_PRINCIPAL
(
            PRINCIPAL_ID INT NOT NULL,
            CLASSNAME VARCHAR (254) NOT NULL,
            IS_MAPPING_ONLY INT NOT NULL,
            IS_ENABLED INT NOT NULL,
            FULL_PATH VARCHAR (254) NOT NULL,
            CREATION_DATE DATETIME NOT NULL,
            MODIFIED_DATE DATETIME NOT NULL,

    CONSTRAINT SECURITY_PRINCIPAL_PK PRIMARY KEY(PRINCIPAL_ID),
    UNIQUE (FULL_PATH));





/* ---------------------------------------------------------------------- */
/* SECURITY_PERMISSION                                                      */
/* ---------------------------------------------------------------------- */

IF EXISTS (SELECT 1 FROM sysobjects WHERE type = 'U' AND name = 'SECURITY_PERMISSION')
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
         and tables.name = 'SECURITY_PERMISSION'
     OPEN refcursor
     FETCH NEXT from refcursor into @reftable_2, @constraintname_2
     while @@FETCH_STATUS = 0
     BEGIN
       exec ('alter table '+@reftable_2+' drop constraint '+@constraintname_2)
       FETCH NEXT from refcursor into @reftable_2, @constraintname_2
     END
     CLOSE refcursor
     DEALLOCATE refcursor
     DROP TABLE SECURITY_PERMISSION
END
;

CREATE TABLE SECURITY_PERMISSION
(
            PERMISSION_ID INT NOT NULL,
            CLASSNAME VARCHAR (254) NOT NULL,
            NAME VARCHAR (254) NOT NULL,
            ACTIONS VARCHAR (254) NOT NULL,
            CREATION_DATE DATETIME NOT NULL,
            MODIFIED_DATE DATETIME NOT NULL,

    CONSTRAINT SECURITY_PERMISSION_PK PRIMARY KEY(PERMISSION_ID));





/* ---------------------------------------------------------------------- */
/* PRINCIPAL_PERMISSION                                                      */
/* ---------------------------------------------------------------------- */

IF EXISTS (SELECT 1 FROM sysobjects WHERE type ='RI' AND name='FK_PRINCIPAL_PERMISSION_1')
    ALTER TABLE PRINCIPAL_PERMISSION DROP CONSTRAINT FK_PRINCIPAL_PERMISSION_1;
IF EXISTS (SELECT 1 FROM sysobjects WHERE type ='RI' AND name='FK_PRINCIPAL_PERMISSION_2')
    ALTER TABLE PRINCIPAL_PERMISSION DROP CONSTRAINT FK_PRINCIPAL_PERMISSION_2;
IF EXISTS (SELECT 1 FROM sysobjects WHERE type = 'U' AND name = 'PRINCIPAL_PERMISSION')
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
         and tables.name = 'PRINCIPAL_PERMISSION'
     OPEN refcursor
     FETCH NEXT from refcursor into @reftable_3, @constraintname_3
     while @@FETCH_STATUS = 0
     BEGIN
       exec ('alter table '+@reftable_3+' drop constraint '+@constraintname_3)
       FETCH NEXT from refcursor into @reftable_3, @constraintname_3
     END
     CLOSE refcursor
     DEALLOCATE refcursor
     DROP TABLE PRINCIPAL_PERMISSION
END
;

CREATE TABLE PRINCIPAL_PERMISSION
(
            PRINCIPAL_ID INT NOT NULL,
            PERMISSION_ID INT NOT NULL,

    CONSTRAINT PRINCIPAL_PERMISSION_PK PRIMARY KEY(PRINCIPAL_ID,PERMISSION_ID));





/* ---------------------------------------------------------------------- */
/* SECURITY_CREDENTIAL                                                      */
/* ---------------------------------------------------------------------- */

IF EXISTS (SELECT 1 FROM sysobjects WHERE type ='RI' AND name='FK_SECURITY_CREDENTIAL_1')
    ALTER TABLE SECURITY_CREDENTIAL DROP CONSTRAINT FK_SECURITY_CREDENTIAL_1;
IF EXISTS (SELECT 1 FROM sysobjects WHERE type = 'U' AND name = 'SECURITY_CREDENTIAL')
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
         and tables.name = 'SECURITY_CREDENTIAL'
     OPEN refcursor
     FETCH NEXT from refcursor into @reftable_4, @constraintname_4
     while @@FETCH_STATUS = 0
     BEGIN
       exec ('alter table '+@reftable_4+' drop constraint '+@constraintname_4)
       FETCH NEXT from refcursor into @reftable_4, @constraintname_4
     END
     CLOSE refcursor
     DEALLOCATE refcursor
     DROP TABLE SECURITY_CREDENTIAL
END
;

CREATE TABLE SECURITY_CREDENTIAL
(
            CREDENTIAL_ID INT NOT NULL,
            PRINCIPAL_ID INT NOT NULL,
            COLUMN_VALUE VARCHAR (254) NOT NULL,
            TYPE SMALLINT NOT NULL,
            CLASSNAME VARCHAR (254) NULL,
            UPDATE_REQUIRED INT NOT NULL,
            IS_ENCODED INT NOT NULL,
            IS_ENABLED INT NOT NULL,
            AUTH_FAILURES SMALLINT NOT NULL,
            IS_EXPIRED INT NOT NULL,
            CREATION_DATE DATETIME NOT NULL,
            MODIFIED_DATE DATETIME NOT NULL,
            PREV_AUTH_DATE DATETIME NULL,
            LAST_AUTH_DATE DATETIME NULL,
            EXPIRATION_DATE DATETIME NULL,

    CONSTRAINT SECURITY_CREDENTIAL_PK PRIMARY KEY(CREDENTIAL_ID));





/* ---------------------------------------------------------------------- */
/* SSO_SITE                                                      */
/* ---------------------------------------------------------------------- */

IF EXISTS (SELECT 1 FROM sysobjects WHERE type = 'U' AND name = 'SSO_SITE')
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
         and tables.name = 'SSO_SITE'
     OPEN refcursor
     FETCH NEXT from refcursor into @reftable_5, @constraintname_5
     while @@FETCH_STATUS = 0
     BEGIN
       exec ('alter table '+@reftable_5+' drop constraint '+@constraintname_5)
       FETCH NEXT from refcursor into @reftable_5, @constraintname_5
     END
     CLOSE refcursor
     DEALLOCATE refcursor
     DROP TABLE SSO_SITE
END
;

CREATE TABLE SSO_SITE
(
            SITE_ID INT NOT NULL,
            NAME VARCHAR (254) NOT NULL,
            URL VARCHAR (254) NOT NULL,
            ALLOW_USER_SET INT default 0 NULL,
            REQUIRES_CERTIFICATE INT default 0 NULL,
            CHALLENGE_RESPONSE_AUTH INT default 0 NULL,
            FORM_AUTH INT default 0 NULL,
            FORM_USER_FIELD VARCHAR (128) NULL,
            FORM_PWD_FIELD VARCHAR (128) NULL,
            REALM VARCHAR (128) NULL,

    CONSTRAINT SSO_SITE_PK PRIMARY KEY(SITE_ID),
    UNIQUE (URL));





/* ---------------------------------------------------------------------- */
/* SSO_COOKIE                                                      */
/* ---------------------------------------------------------------------- */

IF EXISTS (SELECT 1 FROM sysobjects WHERE type = 'U' AND name = 'SSO_COOKIE')
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
         and tables.name = 'SSO_COOKIE'
     OPEN refcursor
     FETCH NEXT from refcursor into @reftable_6, @constraintname_6
     while @@FETCH_STATUS = 0
     BEGIN
       exec ('alter table '+@reftable_6+' drop constraint '+@constraintname_6)
       FETCH NEXT from refcursor into @reftable_6, @constraintname_6
     END
     CLOSE refcursor
     DEALLOCATE refcursor
     DROP TABLE SSO_COOKIE
END
;

CREATE TABLE SSO_COOKIE
(
            COOKIE_ID INT NOT NULL,
            COOKIE VARCHAR (1024) NOT NULL,
            CREATE_DATE DATETIME NOT NULL,

    CONSTRAINT SSO_COOKIE_PK PRIMARY KEY(COOKIE_ID));





/* ---------------------------------------------------------------------- */
/* SSO_SITE_TO_PRINCIPALS                                                      */
/* ---------------------------------------------------------------------- */

IF EXISTS (SELECT 1 FROM sysobjects WHERE type ='RI' AND name='SSO_SITE_TO_PRINC_FK1')
    ALTER TABLE SSO_SITE_TO_PRINCIPALS DROP CONSTRAINT SSO_SITE_TO_PRINC_FK1;
IF EXISTS (SELECT 1 FROM sysobjects WHERE type ='RI' AND name='SSO_SITE_TO_PRINC_FK2')
    ALTER TABLE SSO_SITE_TO_PRINCIPALS DROP CONSTRAINT SSO_SITE_TO_PRINC_FK2;
IF EXISTS (SELECT 1 FROM sysobjects WHERE type = 'U' AND name = 'SSO_SITE_TO_PRINCIPALS')
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
         and tables.name = 'SSO_SITE_TO_PRINCIPALS'
     OPEN refcursor
     FETCH NEXT from refcursor into @reftable_7, @constraintname_7
     while @@FETCH_STATUS = 0
     BEGIN
       exec ('alter table '+@reftable_7+' drop constraint '+@constraintname_7)
       FETCH NEXT from refcursor into @reftable_7, @constraintname_7
     END
     CLOSE refcursor
     DEALLOCATE refcursor
     DROP TABLE SSO_SITE_TO_PRINCIPALS
END
;

CREATE TABLE SSO_SITE_TO_PRINCIPALS
(
            SITE_ID INT NOT NULL,
            PRINCIPAL_ID INT NOT NULL,

    CONSTRAINT SSO_SITE_TO_PRINCIPALS_PK PRIMARY KEY(SITE_ID,PRINCIPAL_ID));





/* ---------------------------------------------------------------------- */
/* SSO_PRINCIPAL_TO_REMOTE                                                      */
/* ---------------------------------------------------------------------- */

IF EXISTS (SELECT 1 FROM sysobjects WHERE type ='RI' AND name='FK_SSO_PRINCIPAL_TO_REMOTE_1')
    ALTER TABLE SSO_PRINCIPAL_TO_REMOTE DROP CONSTRAINT FK_SSO_PRINCIPAL_TO_REMOTE_1;
IF EXISTS (SELECT 1 FROM sysobjects WHERE type ='RI' AND name='FK_SSO_PRINCIPAL_TO_REMOTE_2')
    ALTER TABLE SSO_PRINCIPAL_TO_REMOTE DROP CONSTRAINT FK_SSO_PRINCIPAL_TO_REMOTE_2;
IF EXISTS (SELECT 1 FROM sysobjects WHERE type = 'U' AND name = 'SSO_PRINCIPAL_TO_REMOTE')
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
         and tables.name = 'SSO_PRINCIPAL_TO_REMOTE'
     OPEN refcursor
     FETCH NEXT from refcursor into @reftable_8, @constraintname_8
     while @@FETCH_STATUS = 0
     BEGIN
       exec ('alter table '+@reftable_8+' drop constraint '+@constraintname_8)
       FETCH NEXT from refcursor into @reftable_8, @constraintname_8
     END
     CLOSE refcursor
     DEALLOCATE refcursor
     DROP TABLE SSO_PRINCIPAL_TO_REMOTE
END
;

CREATE TABLE SSO_PRINCIPAL_TO_REMOTE
(
            PRINCIPAL_ID INT NOT NULL,
            REMOTE_PRINCIPAL_ID INT NOT NULL,

    CONSTRAINT SSO_PRINCIPAL_TO_REMOTE_PK PRIMARY KEY(PRINCIPAL_ID,REMOTE_PRINCIPAL_ID));





/* ---------------------------------------------------------------------- */
/* SSO_SITE_TO_REMOTE                                                      */
/* ---------------------------------------------------------------------- */

IF EXISTS (SELECT 1 FROM sysobjects WHERE type ='RI' AND name='FK_SSO_SITE_TO_REMOTE_1')
    ALTER TABLE SSO_SITE_TO_REMOTE DROP CONSTRAINT FK_SSO_SITE_TO_REMOTE_1;
IF EXISTS (SELECT 1 FROM sysobjects WHERE type ='RI' AND name='FK_SSO_SITE_TO_REMOTE_2')
    ALTER TABLE SSO_SITE_TO_REMOTE DROP CONSTRAINT FK_SSO_SITE_TO_REMOTE_2;
IF EXISTS (SELECT 1 FROM sysobjects WHERE type = 'U' AND name = 'SSO_SITE_TO_REMOTE')
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
         and tables.name = 'SSO_SITE_TO_REMOTE'
     OPEN refcursor
     FETCH NEXT from refcursor into @reftable_9, @constraintname_9
     while @@FETCH_STATUS = 0
     BEGIN
       exec ('alter table '+@reftable_9+' drop constraint '+@constraintname_9)
       FETCH NEXT from refcursor into @reftable_9, @constraintname_9
     END
     CLOSE refcursor
     DEALLOCATE refcursor
     DROP TABLE SSO_SITE_TO_REMOTE
END
;

CREATE TABLE SSO_SITE_TO_REMOTE
(
            SITE_ID INT NOT NULL,
            PRINCIPAL_ID INT NOT NULL,

    CONSTRAINT SSO_SITE_TO_REMOTE_PK PRIMARY KEY(SITE_ID,PRINCIPAL_ID));





/* ---------------------------------------------------------------------- */
/* SSO_COOKIE_TO_REMOTE                                                      */
/* ---------------------------------------------------------------------- */

IF EXISTS (SELECT 1 FROM sysobjects WHERE type ='RI' AND name='FK_SSO_COOKIE_TO_REMOTE_1')
    ALTER TABLE SSO_COOKIE_TO_REMOTE DROP CONSTRAINT FK_SSO_COOKIE_TO_REMOTE_1;
IF EXISTS (SELECT 1 FROM sysobjects WHERE type ='RI' AND name='FK_SSO_COOKIE_TO_REMOTE_2')
    ALTER TABLE SSO_COOKIE_TO_REMOTE DROP CONSTRAINT FK_SSO_COOKIE_TO_REMOTE_2;
IF EXISTS (SELECT 1 FROM sysobjects WHERE type = 'U' AND name = 'SSO_COOKIE_TO_REMOTE')
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
         and tables.name = 'SSO_COOKIE_TO_REMOTE'
     OPEN refcursor
     FETCH NEXT from refcursor into @reftable_10, @constraintname_10
     while @@FETCH_STATUS = 0
     BEGIN
       exec ('alter table '+@reftable_10+' drop constraint '+@constraintname_10)
       FETCH NEXT from refcursor into @reftable_10, @constraintname_10
     END
     CLOSE refcursor
     DEALLOCATE refcursor
     DROP TABLE SSO_COOKIE_TO_REMOTE
END
;

CREATE TABLE SSO_COOKIE_TO_REMOTE
(
            COOKIE_ID INT NOT NULL,
            REMOTE_PRINCIPAL_ID INT NOT NULL,

    CONSTRAINT SSO_COOKIE_TO_REMOTE_PK PRIMARY KEY(COOKIE_ID,REMOTE_PRINCIPAL_ID));





/* ---------------------------------------------------------------------- */
/* SECURITY_USER_ROLE                                                      */
/* ---------------------------------------------------------------------- */

IF EXISTS (SELECT 1 FROM sysobjects WHERE type ='RI' AND name='FK_SECURITY_USER_ROLE_1')
    ALTER TABLE SECURITY_USER_ROLE DROP CONSTRAINT FK_SECURITY_USER_ROLE_1;
IF EXISTS (SELECT 1 FROM sysobjects WHERE type ='RI' AND name='FK_SECURITY_USER_ROLE_2')
    ALTER TABLE SECURITY_USER_ROLE DROP CONSTRAINT FK_SECURITY_USER_ROLE_2;
IF EXISTS (SELECT 1 FROM sysobjects WHERE type = 'U' AND name = 'SECURITY_USER_ROLE')
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
         and tables.name = 'SECURITY_USER_ROLE'
     OPEN refcursor
     FETCH NEXT from refcursor into @reftable_11, @constraintname_11
     while @@FETCH_STATUS = 0
     BEGIN
       exec ('alter table '+@reftable_11+' drop constraint '+@constraintname_11)
       FETCH NEXT from refcursor into @reftable_11, @constraintname_11
     END
     CLOSE refcursor
     DEALLOCATE refcursor
     DROP TABLE SECURITY_USER_ROLE
END
;

CREATE TABLE SECURITY_USER_ROLE
(
            USER_ID INT NOT NULL,
            ROLE_ID INT NOT NULL,

    CONSTRAINT SECURITY_USER_ROLE_PK PRIMARY KEY(USER_ID,ROLE_ID));





/* ---------------------------------------------------------------------- */
/* SECURITY_USER_GROUP                                                      */
/* ---------------------------------------------------------------------- */

IF EXISTS (SELECT 1 FROM sysobjects WHERE type ='RI' AND name='FK_SECURITY_USER_GROUP_1')
    ALTER TABLE SECURITY_USER_GROUP DROP CONSTRAINT FK_SECURITY_USER_GROUP_1;
IF EXISTS (SELECT 1 FROM sysobjects WHERE type ='RI' AND name='FK_SECURITY_USER_GROUP_2')
    ALTER TABLE SECURITY_USER_GROUP DROP CONSTRAINT FK_SECURITY_USER_GROUP_2;
IF EXISTS (SELECT 1 FROM sysobjects WHERE type = 'U' AND name = 'SECURITY_USER_GROUP')
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
         and tables.name = 'SECURITY_USER_GROUP'
     OPEN refcursor
     FETCH NEXT from refcursor into @reftable_12, @constraintname_12
     while @@FETCH_STATUS = 0
     BEGIN
       exec ('alter table '+@reftable_12+' drop constraint '+@constraintname_12)
       FETCH NEXT from refcursor into @reftable_12, @constraintname_12
     END
     CLOSE refcursor
     DEALLOCATE refcursor
     DROP TABLE SECURITY_USER_GROUP
END
;

CREATE TABLE SECURITY_USER_GROUP
(
            USER_ID INT NOT NULL,
            GROUP_ID INT NOT NULL,

    CONSTRAINT SECURITY_USER_GROUP_PK PRIMARY KEY(USER_ID,GROUP_ID));





/* ---------------------------------------------------------------------- */
/* SECURITY_GROUP_ROLE                                                      */
/* ---------------------------------------------------------------------- */

IF EXISTS (SELECT 1 FROM sysobjects WHERE type ='RI' AND name='FK_SECURITY_GROUP_ROLE_1')
    ALTER TABLE SECURITY_GROUP_ROLE DROP CONSTRAINT FK_SECURITY_GROUP_ROLE_1;
IF EXISTS (SELECT 1 FROM sysobjects WHERE type ='RI' AND name='FK_SECURITY_GROUP_ROLE_2')
    ALTER TABLE SECURITY_GROUP_ROLE DROP CONSTRAINT FK_SECURITY_GROUP_ROLE_2;
IF EXISTS (SELECT 1 FROM sysobjects WHERE type = 'U' AND name = 'SECURITY_GROUP_ROLE')
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
         and tables.name = 'SECURITY_GROUP_ROLE'
     OPEN refcursor
     FETCH NEXT from refcursor into @reftable_13, @constraintname_13
     while @@FETCH_STATUS = 0
     BEGIN
       exec ('alter table '+@reftable_13+' drop constraint '+@constraintname_13)
       FETCH NEXT from refcursor into @reftable_13, @constraintname_13
     END
     CLOSE refcursor
     DEALLOCATE refcursor
     DROP TABLE SECURITY_GROUP_ROLE
END
;

CREATE TABLE SECURITY_GROUP_ROLE
(
            GROUP_ID INT NOT NULL,
            ROLE_ID INT NOT NULL,

    CONSTRAINT SECURITY_GROUP_ROLE_PK PRIMARY KEY(GROUP_ID,ROLE_ID));





/* ---------------------------------------------------------------------- */
/* SECURITY_GROUP_ROLE                                                      */
/* ---------------------------------------------------------------------- */




/* ---------------------------------------------------------------------- */
/* SECURITY_PRINCIPAL                                                      */
/* ---------------------------------------------------------------------- */




/* ---------------------------------------------------------------------- */
/* SECURITY_PERMISSION                                                      */
/* ---------------------------------------------------------------------- */

BEGIN
ALTER TABLE PRINCIPAL_PERMISSION
    ADD CONSTRAINT FK_PRINCIPAL_PERMISSION_1 FOREIGN KEY (PERMISSION_ID)
    REFERENCES SECURITY_PERMISSION (PERMISSION_ID)
    ON DELETE CASCADE 
END    
;

BEGIN
ALTER TABLE PRINCIPAL_PERMISSION
    ADD CONSTRAINT FK_PRINCIPAL_PERMISSION_2 FOREIGN KEY (PRINCIPAL_ID)
    REFERENCES SECURITY_PRINCIPAL (PRINCIPAL_ID)
    ON DELETE CASCADE 
END    
;




/* ---------------------------------------------------------------------- */
/* PRINCIPAL_PERMISSION                                                      */
/* ---------------------------------------------------------------------- */

BEGIN
ALTER TABLE SECURITY_CREDENTIAL
    ADD CONSTRAINT FK_SECURITY_CREDENTIAL_1 FOREIGN KEY (PRINCIPAL_ID)
    REFERENCES SECURITY_PRINCIPAL (PRINCIPAL_ID)
    ON DELETE CASCADE 
END    
;




/* ---------------------------------------------------------------------- */
/* SECURITY_CREDENTIAL                                                      */
/* ---------------------------------------------------------------------- */




/* ---------------------------------------------------------------------- */
/* SSO_SITE                                                      */
/* ---------------------------------------------------------------------- */




/* ---------------------------------------------------------------------- */
/* SSO_COOKIE                                                      */
/* ---------------------------------------------------------------------- */

BEGIN
ALTER TABLE SSO_SITE_TO_PRINCIPALS
    ADD CONSTRAINT SSO_SITE_TO_PRINC_FK1 FOREIGN KEY (SITE_ID)
    REFERENCES SSO_SITE (SITE_ID)
    ON DELETE CASCADE 
END    
;

BEGIN
ALTER TABLE SSO_SITE_TO_PRINCIPALS
    ADD CONSTRAINT SSO_SITE_TO_PRINC_FK2 FOREIGN KEY (PRINCIPAL_ID)
    REFERENCES SECURITY_PRINCIPAL (PRINCIPAL_ID)
    ON DELETE CASCADE 
END    
;




/* ---------------------------------------------------------------------- */
/* SSO_SITE_TO_PRINCIPALS                                                      */
/* ---------------------------------------------------------------------- */


BEGIN
ALTER TABLE SSO_PRINCIPAL_TO_REMOTE
    ADD CONSTRAINT FK_SSO_PRINCIPAL_TO_REMOTE_1 FOREIGN KEY (PRINCIPAL_ID)
    REFERENCES SECURITY_PRINCIPAL (PRINCIPAL_ID)
    ON DELETE NO ACTION ON UPDATE NO ACTION
END    
; 


BEGIN
ALTER TABLE SSO_PRINCIPAL_TO_REMOTE
    ADD CONSTRAINT FK_SSO_PRINCIPAL_TO_REMOTE_2 FOREIGN KEY (REMOTE_PRINCIPAL_ID)
    REFERENCES SECURITY_PRINCIPAL (PRINCIPAL_ID)
    ON DELETE NO ACTION ON UPDATE NO ACTION
END    
; 



/* ---------------------------------------------------------------------- */
/* SSO_PRINCIPAL_TO_REMOTE                                                      */
/* ---------------------------------------------------------------------- */

BEGIN
ALTER TABLE SSO_SITE_TO_REMOTE
    ADD CONSTRAINT FK_SSO_SITE_TO_REMOTE_1 FOREIGN KEY (SITE_ID)
    REFERENCES SSO_SITE (SITE_ID)
    ON DELETE CASCADE 
END    
;

BEGIN
ALTER TABLE SSO_SITE_TO_REMOTE
    ADD CONSTRAINT FK_SSO_SITE_TO_REMOTE_2 FOREIGN KEY (PRINCIPAL_ID)
    REFERENCES SECURITY_PRINCIPAL (PRINCIPAL_ID)
    ON DELETE CASCADE 
END    
;




/* ---------------------------------------------------------------------- */
/* SSO_SITE_TO_REMOTE                                                      */
/* ---------------------------------------------------------------------- */

BEGIN
ALTER TABLE SSO_COOKIE_TO_REMOTE
    ADD CONSTRAINT FK_SSO_COOKIE_TO_REMOTE_1 FOREIGN KEY (COOKIE_ID)
    REFERENCES SSO_COOKIE (COOKIE_ID)
    ON DELETE CASCADE 
END    
;

BEGIN
ALTER TABLE SSO_COOKIE_TO_REMOTE
    ADD CONSTRAINT FK_SSO_COOKIE_TO_REMOTE_2 FOREIGN KEY (REMOTE_PRINCIPAL_ID)
    REFERENCES SECURITY_PRINCIPAL (PRINCIPAL_ID)
    ON DELETE CASCADE 
END    
;




/* ---------------------------------------------------------------------- */
/* SSO_COOKIE_TO_REMOTE                                                      */
/* ---------------------------------------------------------------------- */

BEGIN
ALTER TABLE SECURITY_USER_ROLE
    ADD CONSTRAINT FK_SECURITY_USER_ROLE_1 FOREIGN KEY (ROLE_ID)
    REFERENCES SECURITY_PRINCIPAL (PRINCIPAL_ID)
    ON DELETE NO ACTION ON UPDATE NO ACTION
END    
; 

BEGIN
ALTER TABLE SECURITY_USER_ROLE
    ADD CONSTRAINT FK_SECURITY_USER_ROLE_2 FOREIGN KEY (USER_ID)
    REFERENCES SECURITY_PRINCIPAL (PRINCIPAL_ID)
    ON DELETE NO ACTION ON UPDATE NO ACTION 
END    
; 



/* ---------------------------------------------------------------------- */
/* SECURITY_USER_ROLE                                                      */
/* ---------------------------------------------------------------------- */

BEGIN
ALTER TABLE SECURITY_USER_GROUP
    ADD CONSTRAINT FK_SECURITY_USER_GROUP_1 FOREIGN KEY (GROUP_ID)
    REFERENCES SECURITY_PRINCIPAL (PRINCIPAL_ID)
    ON DELETE NO ACTION ON UPDATE NO ACTION
END    
;


BEGIN
ALTER TABLE SECURITY_USER_GROUP
    ADD CONSTRAINT FK_SECURITY_USER_GROUP_2 FOREIGN KEY (USER_ID)
    REFERENCES SECURITY_PRINCIPAL (PRINCIPAL_ID)
    ON DELETE NO ACTION ON UPDATE NO ACTION
END    
;




/* ---------------------------------------------------------------------- */
/* SECURITY_USER_GROUP                                                      */
/* ---------------------------------------------------------------------- */
/*
BEGIN
ALTER TABLE SECURITY_GROUP_ROLE
    ADD CONSTRAINT FK_SECURITY_GROUP_ROLE_1 FOREIGN KEY (GROUP_ID)
    REFERENCES SECURITY_PRINCIPAL (PRINCIPAL_ID)
    ON DELETE CASCADE 
END    
; */

/*
BEGIN
ALTER TABLE SECURITY_GROUP_ROLE
    ADD CONSTRAINT FK_SECURITY_GROUP_ROLE_2 FOREIGN KEY (ROLE_ID)
    REFERENCES SECURITY_PRINCIPAL (PRINCIPAL_ID)
    ON DELETE CASCADE 
END    
; */


