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
package eu.europa.ec.leos.services.document;

import cool.graph.cuid.Cuid;
import eu.europa.ec.leos.domain.common.InstanceType;
import eu.europa.ec.leos.i18n.MessageHelper;
import eu.europa.ec.leos.instance.Instance;
import eu.europa.ec.leos.repository.document.AnnexRepository;
import eu.europa.ec.leos.services.document.util.DocumentVOProvider;
import eu.europa.ec.leos.services.numbering.NumberService;
import eu.europa.ec.leos.services.processor.content.TableOfContentProcessor;
import eu.europa.ec.leos.services.processor.content.XmlContentProcessor;
import eu.europa.ec.leos.services.processor.node.XmlNodeConfigProcessor;
import eu.europa.ec.leos.services.processor.node.XmlNodeProcessor;
import eu.europa.ec.leos.services.support.XPathCatalog;
import eu.europa.ec.leos.services.validation.ValidationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Instance(InstanceType.COUNCIL)
public class AnnexServiceMandateImpl extends AnnexServiceImpl {

    public static final String ANNEX_NAME_PREFIX = "annex_";

    @Autowired
    AnnexServiceMandateImpl(AnnexRepository annexRepository, XmlNodeProcessor xmlNodeProcessor,
                            XmlContentProcessor xmlContentProcessor, NumberService numberService, XmlNodeConfigProcessor xmlNodeConfigProcessor,
                            ValidationService validationService, DocumentVOProvider documentVOProvider, TableOfContentProcessor tableOfContentProcessor,
                            MessageHelper messageHelper, XPathCatalog xPathCatalog) {
        super(annexRepository, xmlNodeProcessor, xmlContentProcessor, numberService, xmlNodeConfigProcessor,
                validationService, documentVOProvider, tableOfContentProcessor, messageHelper, xPathCatalog);
    }

    @Override
    public String generateAnnexReference(byte[] content, String language) {
        return ANNEX_NAME_PREFIX + Cuid.createCuid();
    }

    @Override
    public String generateAnnexReference(String templateId, byte[] content, String language) {
        return this.generateAnnexReference(content, language);
    }
}
