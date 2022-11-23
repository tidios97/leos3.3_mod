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
package eu.europa.ec.digit.leos.pilot.export.service.impl;

import eu.europa.ec.digit.leos.pilot.export.exception.TemplateEngineException;
import eu.europa.ec.digit.leos.pilot.export.exception.XmlDocumentException;
import eu.europa.ec.digit.leos.pilot.export.exception.XmlUtilException;
import eu.europa.ec.digit.leos.pilot.export.model.LeosConvertDocumentInput;
import eu.europa.ec.digit.leos.pilot.export.service.TemplateEngineService;
import eu.europa.ec.digit.leos.pilot.export.service.XmlDocumentService;
import eu.europa.ec.digit.leos.pilot.export.util.ConvertUtil;
import eu.europa.ec.digit.leos.pilot.export.util.XmlUtil;
import eu.europa.ec.digit.leos.pilot.export.util.ZipUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;
import org.springframework.web.multipart.MultipartFile;
import org.w3c.dom.Node;

import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

@Service
public class XmlDocumentServiceImpl implements XmlDocumentService {

    private static final Logger LOG = LoggerFactory.getLogger(XmlDocumentServiceImpl.class);

    private final TemplateEngineService templateEngineService;

    private final String CSS_EXT = ".css";
    private final String CSS_PATH = "css/";
    private final String REF_NODE = "leos:ref";
    private final String coverPage= "coverPage";

    public XmlDocumentServiceImpl(TemplateEngineService templateEngineService) {
        this.templateEngineService = templateEngineService;
    }

    public byte[] xmlToHtmlPackage(LeosConvertDocumentInput convertDocumentInput) {
        try {
            Map<String, Object> contentToZip = new HashMap<String, Object>();
            String coverPage = prepareCoverPage(convertDocumentInput);

            String styleSheetFileName =  getStyleSheetName(convertDocumentInput);
            String styleSheet = CSS_PATH + styleSheetFileName;
            InputStream styleSheetInputStream = new ClassPathResource(styleSheet).getInputStream();
            byte[] styleSheetOutput = StreamUtils.copyToByteArray(styleSheetInputStream);
            byte[] htmlOutput = templateEngineService.xmlToHtml(convertDocumentInput, styleSheetFileName, coverPage);
            addCoverPageStyleSheet(contentToZip);
            contentToZip.put(ConvertUtil.getFilename(convertDocumentInput, "html"), htmlOutput);
            contentToZip.put(styleSheet, styleSheetOutput);
            return ZipUtil.zipByteArray(contentToZip);
        } catch (IOException e) {
            LOG.error("Failed getting stylesheet or generating zip file", e);
            throw new XmlDocumentException("Failed getting stylesheet or generating zip file", e);
        } catch (TemplateEngineException e) {
            LOG.error("Error calling template engine", e);
            throw new XmlDocumentException("Error calling template engine", e);
        } catch (XmlUtilException e) {
            LOG.error("Error parsing XML", e);
            throw new XmlDocumentException("Error parsing XML", e);
        } catch (TransformerException e) {
            LOG.error("Error parsing Node", e);
            throw new XmlDocumentException("Error parsing Node", e);
        }
    }

    private String prepareCoverPage(LeosConvertDocumentInput convertDocumentInput) throws IOException, XmlUtilException, TransformerException {
        MultipartFile main = convertDocumentInput.getMain();
        if(main != null && !main.isEmpty()){
            InputStream inputStream = main.getInputStream();
            Node node = XmlUtil.parseXml(inputStream).getElementByName(coverPage);
            String coverPageContent = XmlUtil.XmlFile.parseNode(node);
            return coverPageContent;
        }
        return null;
    }

    private void addCoverPageStyleSheet(Map<String, Object> contentToZip) throws IOException {
        String coverPageStyleSheet = CSS_PATH + coverPage.toLowerCase() + CSS_EXT;
        InputStream coverPageCSSInputStream = new ClassPathResource(coverPageStyleSheet).getInputStream();
        byte[] coverPageCSSOutput = StreamUtils.copyToByteArray(coverPageCSSInputStream);
        contentToZip.put(coverPageStyleSheet, coverPageCSSOutput);
    }

    public byte[] xmlToPdfPackage(LeosConvertDocumentInput convertDocumentInput) {
        return "test".getBytes();
    }

    private String getStyleSheetName(LeosConvertDocumentInput convertDocumentInput) {
        XmlUtil.XmlFile xmlFile = null;
        try {
            xmlFile = XmlUtil.parseXml(convertDocumentInput.getInputFile().getInputStream());
        } catch (XmlUtilException e) {
            LOG.error("Error getting style sheet name", e);
            throw new XmlDocumentException("Error getting style sheet name", e);
        } catch (IOException e) {
            LOG.error("Error reading input file", e);
            throw new XmlDocumentException("IO exception Error reading input file", e);
        }
        Node refNode = xmlFile.getElementByName(REF_NODE);
        String refValue = refNode.getTextContent();
        String fileName = refValue.substring(0, refValue.indexOf("_"));
        return fileName + CSS_EXT;
    }
}
