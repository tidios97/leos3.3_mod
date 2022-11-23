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
import eu.europa.ec.leos.domain.cmis.document.Proposal;
import eu.europa.ec.leos.domain.vo.DocumentVO;
import eu.europa.ec.leos.services.dto.response.WorkspaceProposalResponse;
import eu.europa.ec.leos.model.filter.QueryFilter;
import eu.europa.ec.leos.repository.store.WorkspaceRepository;
import eu.europa.ec.leos.security.LeosPermissionAuthorityMap;
import eu.europa.ec.leos.security.SecurityContext;
import eu.europa.ec.leos.vo.catalog.CatalogItem;
import eu.europa.ec.leos.services.dto.request.FilterProposalsRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

@Service
class WorkspaceServiceImpl implements WorkspaceService {

    private static final Logger LOG = LoggerFactory.getLogger(WorkspaceServiceImpl.class);

    private final WorkspaceRepository workspaceRepository;

    @Value("${leos.workspaces.path}")
    protected String workspacesPath;

    private final TemplateService templateService;

    @Autowired
    WorkspaceServiceImpl(WorkspaceRepository workspaceRepository, TemplateService templateService) {
        this.workspaceRepository = workspaceRepository;
        this.templateService = templateService;
    }

    @Override
    public <T extends LeosDocument> List<T> browseWorkspace(Class<T> filterType, Boolean fetchContent) {
        LOG.debug("Browsing workspace... [path={}, filter={}]", workspacesPath, filterType.getSimpleName());
        return workspaceRepository.findDocumentsByParentPath(workspacesPath, filterType, fetchContent);
    }

    public <T extends LeosDocument> Stream<T> findDocuments(Class<T> filterType, Boolean fetchContent,
                                                            int startIndex, int maxResults, QueryFilter workspaceFilter) {
        LOG.debug("Browsing workspace... [path={}, filter={}]", workspacesPath, filterType.getSimpleName());
        return workspaceRepository.findPagedDocumentsByParentPath(workspacesPath, filterType, fetchContent,
                startIndex, maxResults, workspaceFilter);
    }

    public <T extends LeosDocument> int findDocumentCount(Class<T> filterType, QueryFilter workspaceFilter) {
        LOG.debug("Browsing workspace... [path={}, filter={}]", workspacesPath, filterType.getSimpleName());
        return workspaceRepository.findDocumentCountByParentPath(workspacesPath, filterType, workspaceFilter);
    }

    @Override
    public <T extends LeosDocument> T findDocumentById(String id, Class<T> filterType) {
        return workspaceRepository.findDocumentById(id, filterType, true);
    }

    @Override
    public <T extends LeosDocument> T findDocumentByRef(String ref, Class<T> filterType) {
        return workspaceRepository.findDocumentByRef(ref, filterType);
    }

    @Override
    public <T extends LeosDocument> WorkspaceProposalResponse listDocumentsWithFilter(FilterProposalsRequest request,
                                                                                      SecurityContext securityContext,
                                                                                      LeosPermissionAuthorityMap authorityMap) {
        try {
            List<CatalogItem> catalogItems = templateService.getTemplatesCatalog();
            WorkspaceOptions workspaceOptions = new WorkspaceOptions(authorityMap, securityContext);
            workspaceOptions.initializeOptions(catalogItems, request.getFilters());
            if(request.isSortOrder()) {
                workspaceOptions.setTitleSortOrder(true);
            }
            QueryFilter workspaceFilter = workspaceOptions.getQueryFilter();

            Stream<Proposal> proposals = findDocuments(Proposal.class, false, request.getStartIndex(),
                    request.getLimit(), workspaceFilter);
            List<DocumentVO> proposalList = new ArrayList<>();
            proposals.forEach(proposal -> {
                DocumentVO documentVO = new DocumentVO(proposal);
                proposalList.add(documentVO);
            });
            Integer count = findDocumentCount(Proposal.class, workspaceFilter);
            return new WorkspaceProposalResponse(proposalList, count);
        } catch (IOException e) {
            LOG.error("Unable to fetch list of proposals " + e);
            return new WorkspaceProposalResponse(new ArrayList<>(), 0);
        }
    }
}
