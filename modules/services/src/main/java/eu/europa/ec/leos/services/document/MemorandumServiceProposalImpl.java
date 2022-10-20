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
import eu.europa.ec.leos.repository.document.MemorandumRepository;
import eu.europa.ec.leos.repository.store.PackageRepository;
import eu.europa.ec.leos.services.document.util.DocumentVOProvider;
import eu.europa.ec.leos.services.processor.content.TableOfContentProcessor;
import eu.europa.ec.leos.services.processor.content.XmlContentProcessor;
import eu.europa.ec.leos.services.processor.node.XmlNodeConfigProcessor;
import eu.europa.ec.leos.services.processor.node.XmlNodeProcessor;
import eu.europa.ec.leos.services.support.XPathCatalog;
import eu.europa.ec.leos.services.validation.ValidationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static eu.europa.ec.leos.services.support.XmlHelper.DOC_FILE_NAME_SEPARATOR;

@Service
@Instance(instances = {InstanceType.COMMISSION, InstanceType.OS})
public class MemorandumServiceProposalImpl extends MemorandumServiceImpl {

    @Autowired
    MemorandumServiceProposalImpl(MemorandumRepository memorandumRepository,
                                  PackageRepository packageRepository,
                                  XmlNodeProcessor xmlNodeProcessor,
                                  XmlContentProcessor xmlContentProcessor,
                                  XmlNodeConfigProcessor xmlNodeConfigProcessor, ValidationService validationService,
                                  DocumentVOProvider documentVOProvider, TableOfContentProcessor tableOfContentProcessor,
                                  MessageHelper messageHelper, XPathCatalog xPathCatalog) {

        super(memorandumRepository, packageRepository, xmlNodeProcessor, xmlContentProcessor, xmlNodeConfigProcessor,
                validationService, documentVOProvider, tableOfContentProcessor, messageHelper, xPathCatalog);
    }

    @Override
    public String generateMemorandumReference(byte[] content, String language) {
        String docName = xmlContentProcessor.getDocReference(content);
        return docName.concat(DOC_FILE_NAME_SEPARATOR).concat(Cuid.createCuid())
                .concat(DOC_FILE_NAME_SEPARATOR).concat(language.toLowerCase());
    }

    @Override
    public String generateMemorandumReference(String templateId, byte[] content, String language) {
        content = (content == null) ? getContent(memorandumRepository.findMemorandumById(templateId, false)) : content;
        return this.generateMemorandumReference(content, language);
    }
}
