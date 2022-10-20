--
-- Copyright 2018 European Commission
--
-- Licensed under the EUPL, Version 1.2 or – as soon they will be approved by the European Commission - subsequent versions of the EUPL (the "Licence");
-- You may not use this work except in compliance with the Licence.
-- You may obtain a copy of the Licence at:
--
--     https://joinup.ec.europa.eu/software/page/eupl
--
-- Unless required by applicable law or agreed to in writing, software distributed under the Licence is distributed on an "AS IS" basis,
-- WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
-- See the Licence for the specific language governing permissions and limitations under the Licence.
--

------------------------------------
-- Oracle database creation scripts
------------------------------------

------------------------------------
-- USERS
-- requires sequence, table, index
--  and trigger
------------------------------------
CREATE SEQUENCE "USERS_SEQ"  MINVALUE 1 MAXVALUE 9999999999999999999999999999 INCREMENT BY 1 START WITH 1 CACHE 20 NOORDER  NOCYCLE;

CREATE TABLE "USERS" (
  "USER_ID" NUMBER NOT NULL ENABLE, 
  "LOGIN" VARCHAR2(30 BYTE) NOT NULL ENABLE, 
  "SIDEBAR_TUTORIAL_DISMISSED" NUMBER(1,0) DEFAULT 0 NOT NULL ENABLE, 
  "ACCESS_TOKEN" VARCHAR2(50 BYTE), 
  "ACCESS_TOKEN_CREATED" DATE, 
  "REFRESH_TOKEN" VARCHAR2(50 BYTE),
  CONSTRAINT "USERS_PK" PRIMARY KEY ("USER_ID") USING INDEX ENABLE, 
  CONSTRAINT "USERS_UK_LOGIN" UNIQUE ("LOGIN") USING INDEX ENABLE
);

COMMENT ON COLUMN "USERS"."USER_ID" IS 'Internal user ID';
COMMENT ON COLUMN "USERS"."LOGIN" IS 'User login name';
COMMENT ON COLUMN "USERS"."SIDEBAR_TUTORIAL_DISMISSED" IS 'Flag indicating whether the small tutorial in the sidebar was closed already';
COMMENT ON COLUMN "USERS"."ACCESS_TOKEN" IS 'Latest access token generated for the user';
COMMENT ON COLUMN "USERS"."ACCESS_TOKEN_CREATED" IS 'Timestamp of access token creation';
COMMENT ON COLUMN "USERS"."REFRESH_TOKEN" IS 'Latest refresh token generated for the user';
   
CREATE INDEX "USERS_IX_ACCESS_TOKEN" ON "USERS" ("ACCESS_TOKEN");
  
CREATE OR REPLACE TRIGGER "USERS_TRG" 
  BEFORE INSERT ON USERS 
  FOR EACH ROW 
  BEGIN
    <<COLUMN_SEQUENCES>>
    BEGIN
      IF INSERTING AND :NEW.USER_ID IS NULL THEN
        SELECT USERS_SEQ.NEXTVAL INTO :NEW.USER_ID FROM SYS.DUAL;
      END IF;
    END COLUMN_SEQUENCES;
  END;
/
ALTER TRIGGER "USERS_TRG" ENABLE;


------------------------------------
-- GROUPS
-- requires sequence, table, trigger
------------------------------------
CREATE SEQUENCE "GROUPS_SEQ"  MINVALUE 1 MAXVALUE 9999999999999999999999999999 INCREMENT BY 1 START WITH 1 CACHE 20 NOORDER  NOCYCLE;

CREATE TABLE "GROUPS" (    
  "GROUP_ID" NUMBER NOT NULL ENABLE, 
  "NAME" VARCHAR2(25 BYTE) NOT NULL ENABLE, 
  "DESCRIPTION" VARCHAR2(50 BYTE) NOT NULL ENABLE, 
  "DISPLAYNAME" VARCHAR2(30 BYTE) NOT NULL ENABLE, 
  CONSTRAINT "GROUPS_PK" PRIMARY KEY ("GROUP_ID") USING INDEX ENABLE, 
  CONSTRAINT "GROUPS_UK_NAMES" UNIQUE ("NAME", "DISPLAYNAME") USING INDEX ENABLE
);

COMMENT ON COLUMN "GROUPS"."GROUP_ID" IS 'Group ID';
COMMENT ON COLUMN "GROUPS"."NAME" IS 'Internal group ID';
COMMENT ON COLUMN "GROUPS"."DESCRIPTION" IS 'Description of the group''s purpose';
COMMENT ON COLUMN "GROUPS"."DISPLAYNAME" IS 'Nice name shown to the user';

CREATE OR REPLACE TRIGGER "GROUPS_TRG" 
  BEFORE INSERT ON GROUPS 
  FOR EACH ROW 
  BEGIN
    <<COLUMN_SEQUENCES>>
    BEGIN
      IF INSERTING AND :NEW.GROUP_ID IS NULL THEN
        SELECT GROUPS_SEQ.NEXTVAL INTO :NEW.GROUP_ID FROM SYS.DUAL;
      END IF;
    END COLUMN_SEQUENCES;
  END;
/
ALTER TRIGGER "GROUPS_TRG" ENABLE;


------------------------------------
-- USERS_GROUPS
-- requires sequence, table, trigger, index
------------------------------------
CREATE SEQUENCE "USERS_GROUPS_SEQ" MINVALUE 1 MAXVALUE 9999999999999999999999999999 INCREMENT BY 1 START WITH 1 CACHE 20 NOORDER  NOCYCLE;

CREATE TABLE "USERS_GROUPS" (
  "ID" NUMBER NOT NULL ENABLE, 
  "USER_ID" NUMBER NOT NULL ENABLE, 
  "GROUP_ID" NUMBER NOT NULL ENABLE, 
  CONSTRAINT "USERS_GROUPS_PK" PRIMARY KEY ("ID") USING INDEX ENABLE, 
  CONSTRAINT "USERS_GROUPS_UK_USER_GROUP" UNIQUE ("USER_ID", "GROUP_ID") USING INDEX ENABLE, 
  CONSTRAINT "USERS_GROUPS_FK_GROUPS" FOREIGN KEY ("GROUP_ID") REFERENCES "GROUPS" ("GROUP_ID") ON DELETE CASCADE ENABLE, 
  CONSTRAINT "USERS_GROUPS_FK_USERS" FOREIGN KEY ("USER_ID") REFERENCES "USERS" ("USER_ID") ON DELETE CASCADE ENABLE
);

COMMENT ON COLUMN "USERS_GROUPS"."ID" IS 'internal ID';
COMMENT ON COLUMN "USERS_GROUPS"."USER_ID" IS 'ID of the user belonging to a group';
COMMENT ON COLUMN "USERS_GROUPS"."GROUP_ID" IS 'Group ID of the belonging group';

CREATE INDEX "USERS_GROUPS_IX_GROUPS" ON "USERS_GROUPS" ("GROUP_ID");

CREATE OR REPLACE TRIGGER "USERS_GROUPS_TRG" 
  BEFORE INSERT ON USERS_GROUPS 
  FOR EACH ROW 
  BEGIN
    <<COLUMN_SEQUENCES>>
      BEGIN
      IF INSERTING AND :NEW.ID IS NULL THEN
        SELECT USERS_GROUPS_SEQ.NEXTVAL INTO :NEW.ID FROM SYS.DUAL;
      END IF;
    END COLUMN_SEQUENCES;
  END;
/
ALTER TRIGGER "USERS_GROUPS_TRG" ENABLE;


------------------------------------
-- DOCUMENTS
-- requires sequence, table, trigger
------------------------------------
CREATE SEQUENCE "DOCUMENTS_SEQ"  MINVALUE 1 MAXVALUE 9999999999999999999999999999 INCREMENT BY 1 START WITH 1 CACHE 20 NOORDER  NOCYCLE;

CREATE TABLE "DOCUMENTS" (
  "DOCUMENT_ID" NUMBER NOT NULL ENABLE, 
  "TITLE" VARCHAR2(100 BYTE), 
  "URI" VARCHAR2(500 BYTE) NOT NULL ENABLE, 
  CONSTRAINT "DOCUMENTS_PK" PRIMARY KEY ("DOCUMENT_ID") USING INDEX ENABLE, 
  CONSTRAINT "DOCUMENTS_UK_URI" UNIQUE ("URI") USING INDEX ENABLE
);

COMMENT ON COLUMN "DOCUMENTS"."DOCUMENT_ID" IS 'Internal document ID';
COMMENT ON COLUMN "DOCUMENTS"."TITLE" IS 'Document''s title';
COMMENT ON COLUMN "DOCUMENTS"."URI" IS 'URI of the annotated document';

CREATE OR REPLACE TRIGGER "DOCUMENTS_TRG" 
  BEFORE INSERT ON DOCUMENTS 
  FOR EACH ROW 
  BEGIN
    <<COLUMN_SEQUENCES>>
    BEGIN
      IF INSERTING AND :NEW.DOCUMENT_ID IS NULL THEN
        SELECT DOCUMENTS_SEQ.NEXTVAL INTO :NEW.DOCUMENT_ID FROM SYS.DUAL;
      END IF;
    END COLUMN_SEQUENCES;
  END;
/
ALTER TRIGGER "DOCUMENTS_TRG" ENABLE;


------------------------------------
-- ANNOTATIONS
-- requires table, indexes
------------------------------------
CREATE TABLE "ANNOTATIONS" (
  "ANNOTATION_ID" VARCHAR2(22 BYTE) NOT NULL ENABLE, 
  "TEXT" CLOB, 
  "CREATED" DATE DEFAULT sysdate NOT NULL ENABLE, 
  "UPDATED" DATE DEFAULT sysdate NOT NULL ENABLE, 
  "USER_ID" NUMBER NOT NULL ENABLE, 
  "GROUP_ID" NUMBER NOT NULL ENABLE, 
  "SHARED" NUMBER(1,0) DEFAULT 1 NOT NULL ENABLE, 
  "TARGET_SELECTORS" CLOB NOT NULL ENABLE, 
  "DOCUMENT_ID" NUMBER NOT NULL ENABLE, 
  "REFERENCES" CLOB, 
  "ROOT" VARCHAR2(4000 BYTE) GENERATED ALWAYS AS (SYS_OP_CL2C(CASE  WHEN "REFERENCES" IS NULL THEN NULL ELSE SUBSTR("REFERENCES",1,22) END )) VIRTUAL VISIBLE, 
  CONSTRAINT "ANNOTATIONS_PK" PRIMARY KEY ("ANNOTATION_ID") USING INDEX ENABLE, 
  CONSTRAINT "ANNOTATIONS_FK_GROUPS" FOREIGN KEY ("GROUP_ID") REFERENCES "GROUPS" ("GROUP_ID") ON DELETE CASCADE ENABLE, 
  CONSTRAINT "ANNOTATIONS_FK_USERS" FOREIGN KEY ("USER_ID") REFERENCES "USERS" ("USER_ID") ON DELETE CASCADE ENABLE, 
  CONSTRAINT "ANNOTATIONS_FK_DOCUMENTS" FOREIGN KEY ("DOCUMENT_ID") REFERENCES "DOCUMENTS" ("DOCUMENT_ID") ON DELETE CASCADE ENABLE, 
  CONSTRAINT "ANNOTATIONS_FK_ROOT" FOREIGN KEY ("ROOT") REFERENCES "ANNOTATIONS" ("ANNOTATION_ID") ON DELETE CASCADE ENABLE
) 
LOB ("TEXT") STORE AS BASICFILE 
LOB ("TARGET_SELECTORS") STORE AS BASICFILE  
LOB ("REFERENCES") STORE AS BASICFILE;

COMMENT ON COLUMN "ANNOTATIONS"."ANNOTATION_ID" IS 'UUID';
COMMENT ON COLUMN "ANNOTATIONS"."TEXT" IS 'annotated text';
COMMENT ON COLUMN "ANNOTATIONS"."CREATED" IS 'date of creation of the annotation';
COMMENT ON COLUMN "ANNOTATIONS"."UPDATED" IS 'date of last update of the annotation';
COMMENT ON COLUMN "ANNOTATIONS"."USER_ID" IS 'user''s ID, see USERS table';
COMMENT ON COLUMN "ANNOTATIONS"."GROUP_ID" IS 'group''s ID, see GROUPS table';
COMMENT ON COLUMN "ANNOTATIONS"."SHARED" IS 'flag indicating whether annotation is private or group-public';
COMMENT ON COLUMN "ANNOTATIONS"."TARGET_SELECTORS" IS 'serialized selectors; JSON';
COMMENT ON COLUMN "ANNOTATIONS"."DOCUMENT_ID" IS 'ID of annotated document, see DOCUMENTS table';
COMMENT ON COLUMN "ANNOTATIONS"."REFERENCES" IS 'List of parent annotations';
COMMENT ON COLUMN "ANNOTATIONS"."ROOT" IS 'ID of thread root (for replies)';

CREATE INDEX "ANNOTATIONS_IX_DOCUMENTS" ON "ANNOTATIONS" ("DOCUMENT_ID");
CREATE INDEX "ANNOTATIONS_IX_GROUPS" ON "ANNOTATIONS" ("GROUP_ID");
CREATE INDEX "ANNOTATIONS_IX_USERS" ON "ANNOTATIONS" ("USER_ID");


------------------------------------
-- TAGS
-- requires sequence, table, trigger
------------------------------------
CREATE SEQUENCE "TAGS_SEQ"  MINVALUE 1 MAXVALUE 9999999999999999999999999999 INCREMENT BY 1 START WITH 1 CACHE 20 NOORDER  NOCYCLE;

CREATE TABLE "TAGS" (
  "NAME" VARCHAR2(50 BYTE) NOT NULL ENABLE, 
  "ANNOTATION_ID" VARCHAR2(22 BYTE) NOT NULL ENABLE, 
  "TAG_ID" NUMBER NOT NULL ENABLE, 
  CONSTRAINT "TAGS_PK" PRIMARY KEY ("TAG_ID") USING INDEX ENABLE, 
  CONSTRAINT "TAGS_UK_NAME_ANNOT" UNIQUE ("NAME", "ANNOTATION_ID") USING INDEX ENABLE, 
  CONSTRAINT "TAGS_FK_ANNOTATION" FOREIGN KEY ("ANNOTATION_ID") REFERENCES "ANNOTATIONS" ("ANNOTATION_ID") ON DELETE CASCADE ENABLE
);

COMMENT ON COLUMN "TAGS"."NAME" IS 'Tag';
COMMENT ON COLUMN "TAGS"."ANNOTATION_ID" IS 'Annotation to which the tag belongs to';
COMMENT ON COLUMN "TAGS"."TAG_ID" IS 'ID';

CREATE OR REPLACE TRIGGER "TAGS_TRG" 
  BEFORE INSERT ON TAGS 
  FOR EACH ROW 
  BEGIN
    <<COLUMN_SEQUENCES>>
    BEGIN
      IF INSERTING AND :NEW.TAG_ID IS NULL THEN
        SELECT TAGS_SEQ.NEXTVAL INTO :NEW.TAG_ID FROM SYS.DUAL;
      END IF;
    END COLUMN_SEQUENCES;
  END;
/
ALTER TRIGGER "TAGS_TRG" ENABLE;
