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
package eu.europa.ec.leos.services.store;

import eu.europa.ec.leos.domain.cmis.document.LeosDocument;
import eu.europa.ec.leos.services.dto.response.WorkspaceProposalResponse;
import eu.europa.ec.leos.model.filter.QueryFilter;
import eu.europa.ec.leos.security.LeosPermissionAuthorityMap;
import eu.europa.ec.leos.security.SecurityContext;
import eu.europa.ec.leos.services.dto.request.FilterProposalsRequest;

import java.util.List;
import java.util.stream.Stream;

public interface WorkspaceService {

    <T extends LeosDocument> List<T> browseWorkspace(Class<T> filterType, Boolean fetchContent);

    <T extends LeosDocument> Stream<T> findDocuments(Class<T> filterType, Boolean fetchContent,
                                                     int startIndex, int maxResults, QueryFilter workspaceFilter);

    <T extends LeosDocument> int findDocumentCount(Class<T> filterType, QueryFilter workspaceFilter);

    <T extends LeosDocument> T findDocumentById(String id, Class<T> filterType);

    <T extends LeosDocument> T findDocumentByRef(String ref, Class<T> filterType);

    <T extends LeosDocument> WorkspaceProposalResponse listDocumentsWithFilter(FilterProposalsRequest request, SecurityContext securityContext,
                                                                               LeosPermissionAuthorityMap authorityMap);
}
