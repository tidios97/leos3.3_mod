/*
 * Copyright 2017 European Commission
 *
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 *     https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and limitations under the Licence.
 */

package eu.europa.ec.leos.domain.common;

public enum ErrorCode {
    EXCEPTION, 
    DOCUMENT_NOT_FOUND, 
    DOCUMENT_SOURCE_NOT_FOUND, 
    DOCUMENT_XML_SYNTAX_NOT_VALID,
    DOCUMENT_XSD_VALIDATION_FAILED,
    DOCUMENT_CATEGORY_NOT_FOUND, 
    DOCUMENT_PURPOSE_NOT_FOUND, 
    DOCUMENT_TEMPLATE_NOT_FOUND, 
    DOCUMENT_PROPOSAL_TEMPLATE_NOT_FOUND, 
    DOCUMENT_ANNEX_INDEX_NOT_FOUND, 
    DOCUMENT_ANNEX_TITLE_NOT_FOUND,
    DOCUMENT_ANNEX_NUMBER_NOT_FOUND,
    DOCUMENT_EXPLANATORY_TITLE_NOT_FOUND,
    DOCUMENT_REFERENCE_NOT_VALID
}