/*
 * Copyright 2020 European Commission
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
package integration.importoj;

import eu.europa.ec.leos.domain.cmis.document.Bill;
import eu.europa.ec.leos.integration.ExternalDocumentProvider;
import eu.europa.ec.leos.services.importoj.ConversionHelper;
import eu.europa.ec.leos.services.importoj.ImportServiceImpl;
import eu.europa.ec.leos.services.numbering.NumberService;
import eu.europa.ec.leos.services.numbering.NumberServiceMandate;
import eu.europa.ec.leos.services.numbering.NumberServiceMandateTest;
import eu.europa.ec.leos.services.support.XPathCatalog;
import eu.europa.ec.leos.services.processor.content.XmlContentProcessor;
import eu.europa.ec.leos.services.support.XercesUtils;
import eu.europa.ec.leos.services.processor.content.XmlContentProcessorMandate;
import eu.europa.ec.leos.services.processor.content.XmlContentProcessorProposal;
import eu.europa.ec.leos.services.util.TestUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.w3c.dom.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import static eu.europa.ec.leos.services.support.XercesUtils.createXercesDocument;
import static eu.europa.ec.leos.services.util.TestUtils.squeezeXmlAndRemoveAllNS;
import static eu.europa.ec.leos.test.support.model.ModelHelper.createBillForBytes;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.spy;

public class ImportServiceMandateTest_IT extends NumberServiceMandateTest {

    @Mock
    protected ExternalDocumentProvider externalDocumentProvider;
    @Mock
    protected ConversionHelper conversionHelper;

    @InjectMocks
    protected XmlContentProcessor xmlContentMandateProcessor = new XmlContentProcessorMandate();
    @InjectMocks
    protected XmlContentProcessor xmlContentProcessor = Mockito.spy(new XmlContentProcessorProposal());
    @InjectMocks
    protected XPathCatalog xPathCatalog = spy(new XPathCatalog());

    protected NumberService numberService;
    protected ImportServiceImpl importService;

    protected final static String PREFIX_FILE = "/importoj/";
    protected final static String PREFIX_FILE_CN = "/importoj/cn/";

    @Override
    protected void getStructureFile() {
        docTemplate = "BL-023";
        configFile = "/structure-test-bill-CN.xml";
    }

    @Before
    public void onSetUp() {
        numberService = new NumberServiceMandate(xmlContentMandateProcessor, structureContextProvider, numberProcessorHandler, parentChildConverter);
        importService = new ImportServiceImpl(externalDocumentProvider, conversionHelper, xmlContentProcessor, numberService, xPathCatalog);
    }

    @Test
    public void test_importElement() {
        final byte[] xmlInput = TestUtils.getFileContent(PREFIX_FILE, "test_importElement_154Articles.xml");
        final byte[] xmlStart = TestUtils.getFileContent(PREFIX_FILE_CN, "test_importElement_start.xml");
        final byte[] xmlExpected = TestUtils.getFileContent(PREFIX_FILE_CN, "test_importElement_expected.xml");

        final Bill originalDocument = createBillForBytes(xmlStart);
        List<String> elementsIds = new ArrayList<>();
        IntStream.range(1, 100).forEach(val -> elementsIds.add("art_" + val));  //total are 155, import only first 100

        // When
        long startTime = System.currentTimeMillis();
        byte[] xmlResult = importService.insertSelectedElements(originalDocument, xmlInput, elementsIds, "EN");
        long endTime = System.currentTimeMillis();

        // Then
        String result = new String(xmlResult);
        String expected = new String(xmlExpected);
        result = squeezeXmlAndRemoveAllNS(result);
        expected = squeezeXmlAndRemoveAllNS(expected);
        assertEquals(expected, result);
        assertTrue(endTime - startTime < 25_000);  // check how you are converting Node to String. The time shouldn't go exponential.
    }

    @Test
    public void test_importElement_correctNamespaces() {
        final byte[] xmlInput = TestUtils.getFileContent(PREFIX_FILE, "test_importElement_154Articles.xml");
        final byte[] xmlStart = TestUtils.getFileContent(PREFIX_FILE_CN, "test_importElement_start.xml");
        final byte[] xmlExpected = TestUtils.getFileContent(PREFIX_FILE_CN, "test_importElement_single_expected.xml");

        final Bill originalDocument = createBillForBytes(xmlStart);
        List<String> elementsIds = new ArrayList<>();
        IntStream.range(1, 2).forEach(val -> elementsIds.add("art_" + val));  //total are 155, import only 1

        // When
        byte[] xmlResult = importService.insertSelectedElements(originalDocument, xmlInput, elementsIds, "EN");

        // Then
        Document document = createXercesDocument(xmlResult);
        String actualResult = XercesUtils.nodeToString(document);
        assertFalse(actualResult.contains("xmlns:fmx"));
        assertFalse(actualResult.contains("xmlns:fn"));

        String result = new String(xmlResult);
        String expected = new String(xmlExpected);
        result = squeezeXmlAndRemoveAllNS(result);
        expected = squeezeXmlAndRemoveAllNS(expected);
        assertEquals(expected, result);
    }

}
