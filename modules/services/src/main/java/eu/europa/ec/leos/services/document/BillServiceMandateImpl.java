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
package eu.europa.ec.leos.services.document;

import cool.graph.cuid.Cuid;
import eu.europa.ec.leos.domain.common.InstanceType;
import eu.europa.ec.leos.i18n.MessageHelper;
import eu.europa.ec.leos.instance.Instance;
import eu.europa.ec.leos.repository.document.BillRepository;
import eu.europa.ec.leos.repository.store.PackageRepository;
import eu.europa.ec.leos.services.document.util.DocumentVOProvider;
import eu.europa.ec.leos.services.numbering.NumberService;
import eu.europa.ec.leos.services.processor.AttachmentProcessor;
import eu.europa.ec.leos.services.processor.content.TableOfContentProcessor;
import eu.europa.ec.leos.services.processor.content.XmlContentProcessor;
import eu.europa.ec.leos.services.processor.node.XmlNodeConfigProcessor;
import eu.europa.ec.leos.services.processor.node.XmlNodeProcessor;
import eu.europa.ec.leos.services.support.XPathCatalog;
import eu.europa.ec.leos.services.validation.ValidationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Instance(InstanceType.COUNCIL)
public class BillServiceMandateImpl extends BillServiceImpl {

    public static final String BILL_NAME_PREFIX = "bill_";

    @Autowired
    BillServiceMandateImpl(BillRepository billRepository, PackageRepository packageRepository,
                           XmlNodeProcessor xmlNodeProcessor, XmlContentProcessor xmlContentProcessor,
                           XmlNodeConfigProcessor xmlNodeConfigProcessor, AttachmentProcessor attachmentProcessor,
                           ValidationService validationService, DocumentVOProvider documentVOProvider, NumberService numberService,
                           MessageHelper messageHelper, TableOfContentProcessor tableOfContentProcessor,
                           XPathCatalog xPathCatalog) {
        super(billRepository, packageRepository, xmlNodeProcessor, xmlContentProcessor,xmlNodeConfigProcessor,
                attachmentProcessor, validationService, documentVOProvider, numberService, messageHelper, tableOfContentProcessor,
                xPathCatalog);
    }

    @Override
    public String generateBillReference(byte[] content, String language) {
        return BILL_NAME_PREFIX + Cuid.createCuid();
    }

    @Override
    public String generateBillReference(String templateId, byte[] content, String language) {
        return this.generateBillReference(content, language);
    }
}
