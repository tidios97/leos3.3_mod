--
-- Copyright 2022 European Commission
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
-- Changes to initial Oracle
-- database creation scripts
--
-- change initiated by ANOT-305
------------------------------------

ALTER TABLE "ANNOTATIONS" ADD ("FORWARD_JUSTIFICATION" CLOB) LOB ("FORWARD_JUSTIFICATION") STORE AS BASICFILE ;
ALTER TABLE "ANNOTATIONS" ADD "IS_FORWARDED" NUMBER(1,0) DEFAULT 0 NOT NULL ENABLE;

COMMENT ON COLUMN "ANNOTATIONS"."FORWARD_JUSTIFICATION" IS 'Justification for forwarding the annotation';
COMMENT ON COLUMN "ANNOTATIONS"."IS_FORWARDED" IS 'Flag indicating that this annotation is forwarded';