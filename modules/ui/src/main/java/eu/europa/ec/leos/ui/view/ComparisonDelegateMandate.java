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
import eu.europa.ec.leos.domain.cmis.document.XmlDocument;
import eu.europa.ec.leos.domain.common.InstanceType;
import eu.europa.ec.leos.instance.Instance;
import eu.europa.ec.leos.security.SecurityContext;
import eu.europa.ec.leos.services.compare.ContentComparatorContext;
import eu.europa.ec.leos.services.compare.ContentComparatorService;
import eu.europa.ec.leos.services.compare.processor.LeosPostDiffingProcessor;
import eu.europa.ec.leos.services.document.DocumentContentService;
import eu.europa.ec.leos.services.document.TransformationService;
import eu.europa.ec.leos.services.processor.content.XmlContentProcessor;
import eu.europa.ec.leos.web.support.UrlBuilder;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import static eu.europa.ec.leos.services.compare.ContentComparatorService.ATTR_NAME;
import static eu.europa.ec.leos.services.compare.ContentComparatorService.CONTENT_ADDED_CLASS_CN;
import static eu.europa.ec.leos.services.compare.ContentComparatorService.CONTENT_REMOVED_CLASS_CN;

@Component
@Scope("prototype")
@Instance(InstanceType.COUNCIL)
public class ComparisonDelegateMandate<T extends XmlDocument> extends ComparisonDelegate<T> {

    public ComparisonDelegateMandate(TransformationService transformerService, ContentComparatorService compareService, UrlBuilder urlBuilder,
                                     SecurityContext securityContext, XmlContentProcessor xmlContentProcessor, DocumentContentService documentContentService) {
        super(transformerService, compareService, urlBuilder, securityContext, xmlContentProcessor, documentContentService);
    }

    @Override
    protected String getComparedContent(T oldVersion, T newVersion, boolean includeCoverPage) {
        final String contextPath = urlBuilder.getWebAppPath(VaadinServletService.getCurrentServletRequest());
        final String firstItemHtml = documentContentService.getDocumentAsHtml(oldVersion, contextPath, securityContext.getPermissions(oldVersion),
                includeCoverPage);
        String secondItemHtml = documentContentService.getDocumentAsHtml(newVersion, contextPath, securityContext.getPermissions(newVersion),
                includeCoverPage);
        LeosPostDiffingProcessor postDiffingProcessor = new LeosPostDiffingProcessor();
        secondItemHtml = postDiffingProcessor.adjustSoftActionDiffing(secondItemHtml);
        String result = compareService.compareContents(new ContentComparatorContext.Builder(firstItemHtml, secondItemHtml)
                .withAttrName(ATTR_NAME)
                .withRemovedValue(CONTENT_REMOVED_CLASS_CN)
                .withAddedValue(CONTENT_ADDED_CLASS_CN)
                .withDisplayRemovedContentAsReadOnly(Boolean.TRUE)
                .build());
        result = postDiffingProcessor.adjustMarkersAuthorialNotes(result);
        return result;
    }

    @Override
    protected String getComparedContent(T oldVersion, T newVersion) {
        return this.getComparedContent(oldVersion,newVersion, false);
    }
}
