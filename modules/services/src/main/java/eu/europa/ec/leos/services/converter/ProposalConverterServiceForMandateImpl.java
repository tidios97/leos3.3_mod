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

package eu.europa.ec.leos.services.converter;

import eu.europa.ec.leos.domain.cmis.LeosCategory;
import eu.europa.ec.leos.domain.common.InstanceType;
import eu.europa.ec.leos.domain.vo.DocumentVO;
import eu.europa.ec.leos.instance.Instance;
import eu.europa.ec.leos.services.document.DocumentContentService;
import eu.europa.ec.leos.services.store.TemplateService;
import eu.europa.ec.leos.services.processor.content.XmlContentProcessor;
import eu.europa.ec.leos.services.processor.node.XmlNodeConfigProcessor;
import eu.europa.ec.leos.services.processor.node.XmlNodeProcessor;
import eu.europa.ec.leos.services.support.XPathCatalog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

@Service
@Instance(InstanceType.COUNCIL)
class ProposalConverterServiceForMandateImpl extends ProposalConverterServiceImpl {

    private static final Logger LOG = LoggerFactory.getLogger(ProposalConverterServiceForMandateImpl.class);

    @Autowired
    ProposalConverterServiceForMandateImpl(
            XmlNodeProcessor xmlNodeProcessor,
            XmlNodeConfigProcessor xmlNodeConfigProcessor,
            XmlContentProcessor xmlContentProcessor,
            TemplateService templateService, XPathCatalog xPathCatalog, DocumentContentService documentContentService) {
        super(xmlNodeProcessor, xmlNodeConfigProcessor, xmlContentProcessor, templateService, xPathCatalog,
                documentContentService);
    }

    @Override
    protected void updateSource(final DocumentVO document, File documentFile, boolean canModifySource) {
        try {
            byte[] xmlBytes = Files.readAllBytes(documentFile.toPath());
            if (canModifySource) {
                if (document.getCategory() == LeosCategory.BILL) {
                    xmlBytes = xmlContentProcessor.removeElements(xmlBytes, xPathCatalog.getXPathCoverPage(), 0);
                    // We have to remove the references to the annexes, we will add them when importing
                    xmlBytes = xmlContentProcessor.removeElements(xmlBytes, xPathCatalog.getXPathAttachments(), 0);
                }
                if (document.getCategory() == LeosCategory.ANNEX) {
                    xmlBytes = xmlContentProcessor.removeElements(xmlBytes, xPathCatalog.getXPathCoverPage(), 0);
                }
            }
            document.setSource(xmlBytes);
        } catch (IOException e) {
            LOG.error("Error updating the source of the document: {}", e);
            // the post validation will take care to analyse wether the source is there or not
            document.setSource(null);
        }
    }

}
