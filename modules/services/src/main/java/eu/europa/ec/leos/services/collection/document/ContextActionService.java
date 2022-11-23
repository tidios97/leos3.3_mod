/*
 * Copyright 2018 European Commission
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

package eu.europa.ec.leos.services.collection.document;

public enum ContextActionService {
    DOCUMENT_CREATED,
    METADATA_UPDATED,
    ANNEX_BLOCK_UPDATED,
    ANNEX_STRUCTURE_UPDATED,
    ANNEX_ADDED,
    ANNEX_METADATA_UPDATED,
    ANNEX_DELETED,
    EXPLANATORY_ADDED,
    EXPLANATORY_DELETED,
    EXPLANATORY_UPDATED,
    EXPLANATORY_METADATA_UPDATED,
}