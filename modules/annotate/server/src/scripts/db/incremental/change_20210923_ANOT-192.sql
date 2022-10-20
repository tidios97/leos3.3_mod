--
-- Copyright 2021 European Commission
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
-- change initiated by ANOT-192
------------------------------------

ALTER TABLE "ANNOTATIONS" ADD ("JUSTIFICATION_TEXT" CLOB) LOB ("JUSTIFICATION_TEXT") STORE AS BASICFILE ;

COMMENT ON COLUMN "ANNOTATIONS"."JUSTIFICATION_TEXT" IS 'Justification for annotations of type suggestions';
