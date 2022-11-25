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
package eu.europa.ec.leos.services.document;

import eu.europa.ec.leos.domain.cmis.document.Proposal;
import eu.europa.ec.leos.domain.cmis.document.XmlDocument;
import eu.europa.ec.leos.domain.common.InstanceType;
import eu.europa.ec.leos.instance.Instance;
import eu.europa.ec.leos.security.SecurityContext;
import eu.europa.ec.leos.services.clone.CloneContext;
import eu.europa.ec.leos.services.compare.ContentComparatorContext;
import eu.europa.ec.leos.services.compare.ContentComparatorService;
import eu.europa.ec.leos.services.processor.content.XmlContentProcessor;
import eu.europa.ec.leos.services.support.XPathCatalog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static eu.europa.ec.leos.services.compare.ContentComparatorService.ATTR_NAME;
import static eu.europa.ec.leos.services.compare.ContentComparatorService.CONTENT_ADDED_CLASS;
import static eu.europa.ec.leos.services.compare.ContentComparatorService.CONTENT_REMOVED_CLASS;

@Service
@Instance(instances = {InstanceType.COMMISSION, InstanceType.OS})
public class DocumentContentServiceProposalImpl extends DocumentContentServiceImpl {

    private CloneContext cloneContext;

    @Autowired
    public DocumentContentServiceProposalImpl(TransformationService transformationService,
                                              ContentComparatorService compareService, AnnexService annexService, BillService billService,
                                              MemorandumService memorandumService, ProposalService proposalService, XmlContentProcessor xmlContentProcessor,
                                              CloneContext cloneContext, XPathCatalog xPathCatalog) {
        super(transformationService, compareService, annexService, billService, memorandumService, null, proposalService,
                xmlContentProcessor, xPathCatalog);
        this.cloneContext = cloneContext;
    }

    @Override
    public String toEditableContent(XmlDocument xmlDocument, String contextPath, SecurityContext securityContext, byte[] coverPageContent) {
        if(isCloneProposal()) {
            String[] contentsToCompare = getContentsToCompare(xmlDocument, contextPath, securityContext, coverPageContent);
            if(contentsToCompare != null) {
                switch (contentsToCompare.length) {
                    case 2:
                        String currentDocumentEditableXml = contentsToCompare[0];
                        String originalDocumentEditableXml = contentsToCompare[1];
                        return compareService.compareContents(new ContentComparatorContext.Builder(originalDocumentEditableXml, currentDocumentEditableXml)
                                .withAttrName(ATTR_NAME)
                                .withRemovedValue(CONTENT_REMOVED_CLASS)
                                .withAddedValue(CONTENT_ADDED_CLASS)
                                .withDisplayRemovedContentAsReadOnly(Boolean.TRUE)
                                .build());

                    case 1:
                        return contentsToCompare[0];
                    default:
                        LOG.error("Invalid number of documents returned");
                        return null;
                }
            }
        }
        return getEditableXml(xmlDocument, contextPath, securityContext, coverPageContent);
    }

    private boolean isCloneProposal() {
        return cloneContext != null && cloneContext.isClonedProposal();
    }

    @Override
    public boolean isMemorandumComparisonRequired(byte[] contentBytes) {
        return true;
    }

    public boolean isProposalComparisonRequired(byte[] contentBytes) {
        return true;
    }

}
