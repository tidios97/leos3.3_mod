/*
 * Copyright 2022 European Commission
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
package eu.europa.ec.leos.services.processor;

import eu.europa.ec.leos.domain.cmis.Content;
import eu.europa.ec.leos.domain.cmis.document.FinancialStatement;
import eu.europa.ec.leos.i18n.MessageHelper;
import eu.europa.ec.leos.services.numbering.NumberService;
import eu.europa.ec.leos.services.processor.content.TableOfContentProcessor;
import eu.europa.ec.leos.services.processor.content.XmlContentProcessor;
import eu.europa.ec.leos.services.toc.StructureContext;
import org.apache.commons.lang3.Validate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.inject.Provider;

@Service
public class FinancialStatementProcessorImpl implements FinancialStatementProcessor {

    protected ElementProcessor elementProcessor;
    private XmlContentProcessor xmlContentProcessor;
    protected NumberService numberService;
    protected final TableOfContentProcessor tableOfContentProcessor;
    private Provider<StructureContext> structureContextProvider;
    protected MessageHelper messageHelper;


    @Autowired
    public FinancialStatementProcessorImpl(XmlContentProcessor xmlContentProcessor, NumberService numberService, ElementProcessor elementProcessor,
                                           Provider<StructureContext> structureContextProvider, TableOfContentProcessor tableOfContentProcessor,
                                           MessageHelper messageHelper) {
        this.xmlContentProcessor = xmlContentProcessor;
        this.numberService = numberService;
        this.elementProcessor = elementProcessor;
        this.structureContextProvider = structureContextProvider;
        this.tableOfContentProcessor = tableOfContentProcessor;
        this.messageHelper = messageHelper;
    }

    @Override
    public byte[] updateElement(FinancialStatement document, String elementName, String elementId, String elementFragment) throws Exception {
        Validate.notNull(document, "Document is required.");
        Validate.notNull(elementId, "Element id is required.");
        Validate.notNull(elementFragment, "Element Fragment is required.");
        byte[] updatedContent = elementProcessor.updateElement(document, elementFragment, elementName, elementId);

        return updateFinancialStatementContent(elementId, elementName, updatedContent);
    }

    private byte[] updateFinancialStatementContent(String elementId, String tagName, byte[] xmlContent) {
        return xmlContentProcessor.doXMLPostProcessing(xmlContent);
    }

    private byte[] getContent(FinancialStatement financialStatement) {
        final Content content = financialStatement.getContent().getOrError(() -> "Financial statement content is required!");
        return content.getSource().getBytes();
    }
}
