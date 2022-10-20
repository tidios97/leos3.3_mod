/*
 * Copyright 2019 European Commission
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
package eu.europa.ec.leos.ui.view;

import com.vaadin.server.VaadinServletService;
import eu.europa.ec.leos.domain.cmis.Content;
import eu.europa.ec.leos.domain.cmis.document.XmlDocument;
import eu.europa.ec.leos.security.SecurityContext;
import eu.europa.ec.leos.services.compare.ContentComparatorContext;
import eu.europa.ec.leos.services.compare.ContentComparatorService;
import eu.europa.ec.leos.services.document.DocumentContentService;
import eu.europa.ec.leos.services.document.TransformationService;
import eu.europa.ec.leos.services.processor.content.XmlContentProcessor;
import eu.europa.ec.leos.services.compare.processor.PostDiffingProcessor;
import eu.europa.ec.leos.web.support.UrlBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.HashMap;

import static eu.europa.ec.leos.services.compare.ContentComparatorService.ATTR_NAME;
import static eu.europa.ec.leos.services.compare.ContentComparatorService.CONTENT_ADDED_CLASS;
import static eu.europa.ec.leos.services.compare.ContentComparatorService.CONTENT_REMOVED_CLASS;
import static eu.europa.ec.leos.services.compare.ContentComparatorService.DOUBLE_COMPARE_ADDED_CLASS;
import static eu.europa.ec.leos.services.compare.ContentComparatorService.DOUBLE_COMPARE_INTERMEDIATE_STYLE;
import static eu.europa.ec.leos.services.compare.ContentComparatorService.DOUBLE_COMPARE_ORIGINAL_STYLE;
import static eu.europa.ec.leos.services.compare.ContentComparatorService.DOUBLE_COMPARE_REMOVED_CLASS;
import static eu.europa.ec.leos.services.compare.ContentComparatorService.DOUBLE_COMPARE_RETAIN_CLASS;

@Component
@Scope("prototype")
public abstract class ComparisonDelegate<T extends XmlDocument> {

    private static final Logger LOG = LoggerFactory.getLogger(ComparisonDelegate.class);

    private final TransformationService transformerService;
    protected final ContentComparatorService compareService;
    protected final UrlBuilder urlBuilder;
    protected final SecurityContext securityContext;
    private final XmlContentProcessor xmlContentProcessor;
    protected final DocumentContentService documentContentService;

    @Autowired
    public ComparisonDelegate(TransformationService transformerService, ContentComparatorService compareService, UrlBuilder urlBuilder,
                              SecurityContext securityContext, XmlContentProcessor xmlContentProcessor, DocumentContentService documentContentService) {
        this.transformerService = transformerService;
        this.compareService = compareService;
        this.urlBuilder = urlBuilder;
        this.securityContext = securityContext;
        this.xmlContentProcessor = xmlContentProcessor;
        this.documentContentService = documentContentService;
    }

    public String getMarkedContent(T oldVersion, T newVersion, boolean includeCoverPage) {
        return getComparedContent(oldVersion, newVersion, includeCoverPage);
    }
    
    public String getMarkedContent(T oldVersion, T newVersion) {
        return getComparedContent(oldVersion, newVersion);
    }

    public String getContributionComparedContent(String oldVersion, String newVersion) {
        return compareService.compareContents(new ContentComparatorContext.Builder(oldVersion, newVersion)
                .withAttrName(ATTR_NAME)
                .withRemovedValue(CONTENT_REMOVED_CLASS)
                .withAddedValue(CONTENT_ADDED_CLASS)
                .withDisplayRemovedContentAsReadOnly(Boolean.TRUE)
                .build());
    }

    abstract protected String getComparedContent(T oldVersion, T newVersion);

    abstract protected String getComparedContent(T oldVersion, T newVersion, boolean includeCoverPage);

    public HashMap<ComparisonDisplayMode, Object> versionCompare(T oldVersion, T newVersion, ComparisonDisplayMode displayMode) {
        return this.versionCompare(oldVersion, newVersion, displayMode, false);
    }

    public HashMap<ComparisonDisplayMode, Object> versionCompare(T oldVersion, T newVersion, ComparisonDisplayMode displayMode, boolean includeCoverPage) {
        long startTime = System.currentTimeMillis();
        HashMap<ComparisonDisplayMode, Object> htmlCompareResult = new HashMap<>();
        
        switch (displayMode) {
            case SINGLE_COLUMN_MODE:
                final String singleResult = getComparedContent(oldVersion, newVersion);
                htmlCompareResult.put(displayMode, singleResult);
                break;
            case TWO_COLUMN_MODE:
                final String contextPath = urlBuilder.getWebAppPath(VaadinServletService.getCurrentServletRequest());
                final String firstItemHtml = documentContentService.getDocumentAsHtml(oldVersion, contextPath, securityContext.getPermissions(oldVersion),
                        includeCoverPage);
                final String secondItemHtml = documentContentService.getDocumentAsHtml(newVersion, contextPath, securityContext.getPermissions(newVersion),
                        includeCoverPage);
                final String[] doubleResult = compareService.twoColumnsCompareContents(new ContentComparatorContext.Builder(firstItemHtml, secondItemHtml).build());
                htmlCompareResult.put(displayMode, doubleResult);
        }
        LOG.debug("Diff exec time: {} ms", (System.currentTimeMillis() - startTime));
        return htmlCompareResult;
    }

    public String doubleCompareHtmlContents(T originalProposal, T intermediateMajor, T current, boolean threeWayEnabled) {
        return this.doubleCompareHtmlContents(originalProposal, intermediateMajor, current, threeWayEnabled, false);
    }
    public String doubleCompareHtmlContents(T originalProposal, T intermediateMajor, T current, boolean threeWayEnabled, boolean includeCoverPage) {
        //FIXME collect list of processing to be done on comparison output and do it at single place.
        final String contextPath = urlBuilder.getWebAppPath(VaadinServletService.getCurrentServletRequest());
        final String currentHtml = documentContentService.getDocumentAsHtml(current, contextPath, securityContext.getPermissions(current), includeCoverPage);

        PostDiffingProcessor postDiffingProcessor = new PostDiffingProcessor();
        if(threeWayEnabled) {
            final String proposalHtml = documentContentService.getDocumentAsHtml(originalProposal, contextPath, securityContext.getPermissions(originalProposal),
                    includeCoverPage);
            final String intermediateMajorHtml = documentContentService.getDocumentAsHtml(intermediateMajor, contextPath, securityContext.getPermissions(intermediateMajor),
                    includeCoverPage);

            String result =  compareService.compareContents(new ContentComparatorContext.Builder(proposalHtml, currentHtml, intermediateMajorHtml)
                    .withAttrName(ATTR_NAME)
                    .withRemovedValue(DOUBLE_COMPARE_REMOVED_CLASS)
                    .withAddedValue(DOUBLE_COMPARE_ADDED_CLASS)
                    .withRemovedIntermediateValue(DOUBLE_COMPARE_REMOVED_CLASS + DOUBLE_COMPARE_INTERMEDIATE_STYLE)
                    .withAddedIntermediateValue(DOUBLE_COMPARE_ADDED_CLASS + DOUBLE_COMPARE_INTERMEDIATE_STYLE)
                    .withRemovedOriginalValue(DOUBLE_COMPARE_REMOVED_CLASS + DOUBLE_COMPARE_ORIGINAL_STYLE)
                    .withAddedOriginalValue(DOUBLE_COMPARE_ADDED_CLASS + DOUBLE_COMPARE_ORIGINAL_STYLE)
                    .withRetainOriginalValue(DOUBLE_COMPARE_RETAIN_CLASS + DOUBLE_COMPARE_ORIGINAL_STYLE)
                    .withDisplayRemovedContentAsReadOnly(Boolean.TRUE)
                    .withThreeWayDiff(threeWayEnabled)
                    .build())
                    .replaceAll("(?i) id=\"", " id=\"doubleCompare-")
                    .replaceAll("(?i) leos:softmove_from=\"", " leos:softmove_from=\"doubleCompare-")
                    .replaceAll("(?i) leos:softmove_to=\"", " leos:softmove_to=\"doubleCompare-");
            result = postDiffingProcessor.adjustMarkersAuthorialNotes(result);
            return result;
        }

        return currentHtml;
    }

    public String doubleCompareXmlContents(T originalProposal, T intermediateMajor, T current, boolean threeWayEnabled) {
        return doubleCompareXmlContents(originalProposal, intermediateMajor, current, threeWayEnabled, false);
    }

    public String doubleCompareXmlContents(T originalProposal, T intermediateMajor, T current, boolean threeWayEnabled, boolean isCover) {
        final String currentXml = current.getContent().getOrError(() -> "Current document content is required!").getSource().toString();
        
        if(threeWayEnabled) {
            final Content.Source proposalXmlContent = originalProposal.getContent().getOrError(() -> "Proposal document content is required!").getSource();
            final Content.Source intermediateMajorXmlContent = intermediateMajor.getContent().getOrError(() -> "Intermediate Major Version document content is required!").getSource();

            String proposalXml = isCover ? new String (documentContentService.getCoverPageContent(proposalXmlContent.getBytes())) : proposalXmlContent.toString();
            String intermediateMajorXml = isCover ? new String (documentContentService.getCoverPageContent(intermediateMajorXmlContent.getBytes())): intermediateMajorXmlContent.toString();

            return compareService.compareContents(new ContentComparatorContext.Builder(proposalXml, currentXml, intermediateMajorXml)
                    .withAttrName(ATTR_NAME)
                    .withRemovedValue(DOUBLE_COMPARE_REMOVED_CLASS)
                    .withAddedValue(DOUBLE_COMPARE_ADDED_CLASS)
                    .withRemovedIntermediateValue(DOUBLE_COMPARE_REMOVED_CLASS + DOUBLE_COMPARE_INTERMEDIATE_STYLE)
                    .withAddedIntermediateValue(DOUBLE_COMPARE_ADDED_CLASS + DOUBLE_COMPARE_INTERMEDIATE_STYLE)
                    .withRemovedOriginalValue(DOUBLE_COMPARE_REMOVED_CLASS + DOUBLE_COMPARE_ORIGINAL_STYLE)
                    .withAddedOriginalValue(DOUBLE_COMPARE_ADDED_CLASS + DOUBLE_COMPARE_ORIGINAL_STYLE)
                    .withRetainOriginalValue(DOUBLE_COMPARE_RETAIN_CLASS + DOUBLE_COMPARE_ORIGINAL_STYLE)
                    .withDisplayRemovedContentAsReadOnly(Boolean.TRUE)
                    .withThreeWayDiff(threeWayEnabled)
                    .build());
        }

        return currentXml;
    }
}
