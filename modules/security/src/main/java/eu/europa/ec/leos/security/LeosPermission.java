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
package eu.europa.ec.leos.security;

public enum LeosPermission {
    CAN_CREATE,
    CAN_READ,
    CAN_UPDATE,
    CAN_EDIT_ALL_ANNOTATIONS,
    CAN_DELETE,
    CAN_COMMENT,
    CAN_SUGGEST,
    CAN_MERGE_SUGGESTION,
    CAN_MARK_TREATED,
    CAN_EXPORT_LW,
    CAN_EXPORT_DW,
    CAN_CREATE_MILESTONE,
    CAN_RESTORE_PREVIOUS_VERSION,
    CAN_ADD_REMOVE_COLLABORATOR,
    CAN_DOWNLOAD_PROPOSAL,
    CAN_UPLOAD,
    CAN_DOWNLOAD_XML_COMPARISON,
    CAN_SEE_SOURCE,
    CAN_SEE_ALL_DOCUMENTS,
    CAN_WORK_WITH_EXPORT_PACKAGE,
    CAN_CLOSE_PROPOSAL,
    CAN_RENUMBER
}
