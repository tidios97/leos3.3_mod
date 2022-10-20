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
-- Migration script to transform existing data to the
-- new format/interpretation of justifications to
-- suggestions in the ISC context.
--
-- change initiated by ANOT-192
------------------------------------


-- Copy the content of the annotation that is marked as a reply to the field '"JUSTIFICATION_TEXT"'
-- of the annotation to which the marked annotation is a reply.
update "ANNOTATIONS" set "JUSTIFICATION_TEXT"=(
    SELECT "TEXT" FROM (
        SELECT "REPLIES".*, ROW_NUMBER() OVER (PARTITION BY TO_CHAR("REPLIES"."REFERENCES") ORDER BY "REPLIES"."CREATED" ASC) RN FROM (
            -- Suggestion from ISC context
            SELECT "ISC_ANNOTATIONS"."ANNOTATION_ID","ISC_ANNOTATIONS"."TEXT","ISC_ANNOTATIONS"."REFERENCES","ISC_ANNOTATIONS"."CREATED" FROM (
                -- Annotations FROM ISC context
                SELECT "ANNOTATIONS".* FROM
                "ANNOTATIONS"
                INNER JOIN
                "METADATA"
                ON "ANNOTATIONS"."METADATA_ID"="METADATA"."ID" WHERE "SYSTEM_ID"='ISC'
            ) ISC_ANNOTATIONS
            INNER JOIN (
                SELECT * FROM (
                    -- Suggestions
                    SELECT "ANNOTATIONS".* FROM
                    "ANNOTATIONS" INNER JOIN "TAGS"
                    ON "ANNOTATIONS"."ANNOTATION_ID"="TAGS"."ANNOTATION_ID" WHERE "TAGS"."NAME"='suggestion'
                ) SUGGESTIONS
                INNER JOIN
                "METADATA"
                ON "SUGGESTIONS"."METADATA_ID"="METADATA"."ID" WHERE "SYSTEM_ID"='ISC'
            ) ISC_SUGGESTIONS
            -- Replies to suggestions from ISC context
            ON TO_CHAR("ISC_ANNOTATIONS"."REFERENCES")="ISC_SUGGESTIONS"."ANNOTATION_ID"
            ) REPLIES
            ) ORDERED_REPLIES
        -- Limit the number of replies to 1. This should always be the case for replies to suggestions (i.e. the justification)
        -- in ISC. Therefore, this is just a conuntermeasure for legacy/ill structured data.
        WHERE TO_CHAR("ORDERED_REPLIES"."REFERENCES")="ANNOTATIONS"."ANNOTATION_ID" AND "ORDERED_REPLIES"."RN"=1
);


-- Delete the replies whose content has been transferred to its parent suggestion by the previous step
DELETE FROM "ANNOTATIONS" WHERE "ANNOTATIONS"."ANNOTATION_ID"=(
	SELECT "REPLIES"."ANNOTATION_ID" FROM (
		-- Suggestion from ISC context
		SELECT "ISC_ANNOTATIONS"."ANNOTATION_ID","ISC_ANNOTATIONS"."TEXT","ISC_ANNOTATIONS"."REFERENCES" FROM (
			-- Annotations FROM ISC context
			SELECT "ANNOTATIONS".* FROM
			"ANNOTATIONS"
			INNER JOIN
			"METADATA"
			ON "ANNOTATIONS"."METADATA_ID"="METADATA"."ID" WHERE "SYSTEM_ID"='ISC'
		) ISC_ANNOTATIONS
		INNER JOIN (
			SELECT * FROM (
                -- Suggestions
                SELECT "ANNOTATIONS".* FROM
                "ANNOTATIONS" INNER JOIN "TAGS"
                ON "ANNOTATIONS"."ANNOTATION_ID"="TAGS"."ANNOTATION_ID" WHERE "TAGS"."NAME"='suggestion'
            ) SUGGESTIONS
            INNER JOIN
            "METADATA"
			ON "SUGGESTIONS"."METADATA_ID"="METADATA"."ID" WHERE "SYSTEM_ID"='ISC'
		) ISC_SUGGESTIONS
		-- Replies to suggestions from ISC context
		ON TO_CHAR("ISC_ANNOTATIONS"."REFERENCES")="ISC_SUGGESTIONS"."ANNOTATION_ID") REPLIES
	WHERE "REPLIES"."ANNOTATION_ID"="ANNOTATIONS"."ANNOTATION_ID"
);
