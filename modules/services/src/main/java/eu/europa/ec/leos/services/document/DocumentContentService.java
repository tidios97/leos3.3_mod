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


import eu.europa.ec.leos.domain.cmis.Content;
import eu.europa.ec.leos.domain.cmis.document.Annex;
import eu.europa.ec.leos.domain.cmis.document.Bill;
import eu.europa.ec.leos.domain.cmis.document.Explanatory;
import eu.europa.ec.leos.domain.cmis.document.Memorandum;
import eu.europa.ec.leos.domain.cmis.document.Proposal;
import eu.europa.ec.leos.domain.cmis.document.XmlDocument;
import eu.europa.ec.leos.security.LeosPermission;
import eu.europa.ec.leos.security.SecurityContext;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;

public interface DocumentContentService {

    default InputStream getContentInputStream(XmlDocument xmlDocument) {
        final Content content = xmlDocument.getContent().getOrError(() -> "Document content is required!");
        return content.getSource().getInputStream();
    }

    default InputStream getContentInputStream(byte[] content) {
        return new ByteArrayInputStream(content);
    }

    String toEditableContent(XmlDocument xmlDocument, String contextPath, SecurityContext securityContext, byte[] coverPageContent);
    
    XmlDocument getOriginalAnnex(Annex annex);

    XmlDocument getOriginalExplanatory(Explanatory explanatory);
    
    XmlDocument getOriginalBill(Bill bill);

    XmlDocument getOriginalMemorandum(Memorandum memorandum);

    XmlDocument getOriginalProposal(Proposal proposal);

    boolean isAnnexComparisonRequired(byte[] contentBytes);

    boolean isMemorandumComparisonRequired(byte[] contentBytes);

    boolean isProposalComparisonRequired(byte[] contentBytes);

    String getDocumentAsHtml(XmlDocument xmlDocument, String contextPath, List<LeosPermission> permissions);

    String getDocumentAsHtml(XmlDocument xmlDocument, String contextPath, List<LeosPermission> permissions, boolean includeCoverPage);

    String getCleanDocumentAsHtml(XmlDocument xmlDocument, String contextPath, List<LeosPermission> permissions);

    String getCleanDocumentAsHtml(XmlDocument xmlDocument, String contextPath, List<LeosPermission> permissions, boolean includeCoverPage);

    String getDocumentForContributionAsHtml(byte[] content, String contextPath, List<LeosPermission> permissions);

    String getDocumentForContributionAsHtml(byte[] content, String contextPath, List<LeosPermission> permissions, boolean includeCoverPage);

    byte[] getCoverPageContent(byte[] xmlContent);

    /**
     * This method is used for backward compatibility of old documents containing cover page to remove it.
     * @param xmlContent
     */
    boolean isCoverPageExists(byte[] xmlContent);

    byte[] getOriginalContentToCompare(XmlDocument xmlDocument);

    boolean isCouncilExplanatoryComparisonRequired(Explanatory explanatory, SecurityContext securityContext);
}
