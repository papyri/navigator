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
-- PREFS_NODE
-----------------------------------------------------------------------------

CREATE TABLE PREFS_NODE
(
    NODE_ID INTEGER NOT NULL,
    PARENT_NODE_ID INTEGER,
    NODE_NAME VARCHAR(100),
    NODE_TYPE SMALLINT,
    FULL_PATH VARCHAR(254),
    CREATION_DATE TIMESTAMP,
    MODIFIED_DATE TIMESTAMP,
    PRIMARY KEY(NODE_ID),
    FOREIGN KEY (PARENT_NODE_ID) REFERENCES PREFS_NODE (NODE_ID)
        ON DELETE CASCADE
    );

CREATE INDEX IX_PREFS_NODE_1 ON PREFS_NODE (PARENT_NODE_ID);
CREATE INDEX IX_PREFS_NODE_2 ON PREFS_NODE (FULL_PATH);
    
-----------------------------------------------------------------------------
-- PREFS_PROPERTY_VALUE
-----------------------------------------------------------------------------

CREATE TABLE PREFS_PROPERTY_VALUE
(
    PROPERTY_VALUE_ID INTEGER NOT NULL,
    NODE_ID INTEGER,
    PROPERTY_NAME VARCHAR(100),
    PROPERTY_VALUE VARCHAR(254),
    CREATION_DATE TIMESTAMP,
    MODIFIED_DATE TIMESTAMP,
    PRIMARY KEY(PROPERTY_VALUE_ID),
    -- Still an issue with OJB 1.0.3 when deleting M-N. Foreign Key Violation.
    FOREIGN KEY (NODE_ID) REFERENCES PREFS_NODE (NODE_ID)
        ON DELETE CASCADE
    );

CREATE INDEX IX_FKPPV_1 ON PREFS_PROPERTY_VALUE (NODE_ID);
     