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
package eu.europa.ec.leos.services.processor;

import eu.europa.ec.leos.services.processor.content.XmlContentProcessor;
import eu.europa.ec.leos.services.support.XPathCatalog;
import eu.europa.ec.leos.services.support.XercesUtils;
import eu.europa.ec.leos.services.support.XercesUtils;
import eu.europa.ec.leos.services.support.XmlHelper;
import org.apache.commons.lang3.Validate;
import org.apache.jena.sparql.util.RomanNumeral;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;


import static eu.europa.ec.leos.services.support.XercesUtils.createXercesDocument;
import static eu.europa.ec.leos.services.support.XmlHelper.HREF;
import static eu.europa.ec.leos.services.support.XmlHelper.XML_DOC_EXT;
import static eu.europa.ec.leos.services.support.XmlHelper.XML_SHOW_AS;
import static eu.europa.ec.leos.services.support.XmlHelper.ANNEX;

import static eu.europa.ec.leos.services.support.XercesUtils.createXercesDocument;
import static eu.europa.ec.leos.services.support.XmlHelper.HREF;
import static eu.europa.ec.leos.services.support.XmlHelper.XML_DOC_EXT;
import static eu.europa.ec.leos.services.support.XmlHelper.XML_SHOW_AS;

@Service
public class AttachmentProcessorImpl implements AttachmentProcessor {
    private XmlContentProcessor xmlContentProcessor;


    private XPathCatalog xPathCatalog;

    @Autowired
    public AttachmentProcessorImpl(XmlContentProcessor xmlContentProcessor, XPathCatalog xPathCatalog) {
        this.xmlContentProcessor = xmlContentProcessor;
        this.xPathCatalog = xPathCatalog;
    }

    @Override
    public byte[] addAttachmentInBill(byte[] xmlContent, String href, String showAs) {
        Validate.notNull(xmlContent, "Xml content is required.");
        Validate.notNull(href, "href is required.");
        byte[] updatedContent = xmlContent;

        String attachments = xmlContentProcessor.getElementByNameAndId(xmlContent, "attachments", null);
        if (attachments == null) {
            String attachmentsTag = new StringBuilder("<attachments>")
                    .append(createAttachmentTag(href, showAs))
                    .append("</attachments>").toString();
            updatedContent = xmlContentProcessor.appendElementToTag(xmlContent, "bill", attachmentsTag, false);
        } else {
            updatedContent = xmlContentProcessor.appendElementToTag(xmlContent, "attachments", createAttachmentTag(href, showAs), false);
        }
        return xmlContentProcessor.doXMLPostProcessing(updatedContent);
    }

    @Override
    public byte[] removeAttachmentFromBill(byte[] xmlContent, String href) {
        String attachments = xmlContentProcessor.getElementByNameAndId(xmlContent, "attachments", null);
        byte[] updatedContent = xmlContent;

        if (attachments == null) {
            return updatedContent;
        }

        updatedContent = xmlContentProcessor.removeElements(xmlContent, xPathCatalog.getXPathDocumentRefByHrefAttr(href), 1);
        String attachment = xmlContentProcessor.getElementByNameAndId(updatedContent, "attachment", null);
        if (attachment == null) { // if no other attachment tag is present, remove it
            updatedContent = xmlContentProcessor.removeElements(updatedContent, xPathCatalog.getXPathAttachments(), 0);
        }
        return updatedContent;
    }

    @Override
    public Map<String, String> getAttachmentsIdFromBill(byte[] xmlContent) {
        Map<String, String> attachmentsId = new HashMap<>();
        List<Map<String, String>> attrsElts = xmlContentProcessor.getElementsAttributesByPath(xmlContent, xPathCatalog.getXPathDocumentRef());
        attrsElts.forEach(element -> {
            if (element.containsKey("xml:id") && element.containsKey("href")) {
                attachmentsId.put(element.get("href"), element.get("xml:id"));
            }
        });
        return attachmentsId;
    }

    @Override
    public Map<String, String> getAttachmentsHrefFromBill(byte[] xmlContent) {
        Map<String, String> attachmentRefs = new HashMap<>();
        Document document = createXercesDocument(xmlContent);
        NodeList documentRefs = XercesUtils.getElementsByXPath(document, xPathCatalog.getXPathDocumentRef());
        for (int nodeIdx = 0; nodeIdx < documentRefs.getLength(); nodeIdx++) {
            Node documentRef = documentRefs.item(nodeIdx);
            String showAs = XercesUtils.getAttributeValue(documentRef, XML_SHOW_AS).replaceAll(XML_DOC_EXT, "");
            String docRef = XercesUtils.getAttributeValue(documentRef, HREF).replaceAll(XML_DOC_EXT, "");
            if(showAs.equals("ANNEX")) {
                showAs = showAs + " I";
            }
            attachmentRefs.put(showAs, docRef);
        }
        return attachmentRefs;
    }

    private String createAttachmentTag(String href, String showAs) {
        return String.format("<attachment><documentRef href=\"%s\" showAs=\"%s\"/></attachment>", href, showAs);
    }

    private String createDocumentRefTag(String xmlid, String href, String showAs) {
        return String.format("<documentRef xml:id=\"%s\" href=\"%s\" showAs=\"%s\"/>", xmlid, href, showAs);
    }

    @Override
    public byte[] updateAttachmentsInBill(byte[] xmlContent, HashMap<String, String> attachmentsElements) {

        for (String elementRef : attachmentsElements.keySet()) {
            String elementId = xmlContentProcessor.getElementIdByPath(xmlContent, xPathCatalog.getXPathDocumentRefByHrefAttr(elementRef));
            String updatedElement = createDocumentRefTag(elementId, elementRef, attachmentsElements.get(elementRef));
            xmlContent = xmlContentProcessor.replaceElementById(xmlContent, updatedElement, elementId);
        }

        return sortAttributesByAnnexRoman(xmlContent);
    }

    private byte[] sortAttributesByAnnexRoman(byte[] xmlContent) {

        XercesUtils xercesUtils = new XercesUtils();
        Document document = xercesUtils.createXercesDocument(xmlContent, true);
        NodeList nodeList = document.getElementsByTagName("attachments");
        if (nodeList.getLength() > 0){
            Node parentNode = nodeList.item(0);
            NodeList attachments = parentNode.getChildNodes();
            TreeMap<Integer,Node> list = new TreeMap<Integer,Node>();
            for (int i = attachments.getLength()-1; i >= 0 ; --i){
                Node attachment = attachments.item(i);
                String showAsAttr = XercesUtils.getAttributeValue(attachment.getFirstChild(), XmlHelper.XML_SHOW_AS);
                if(ANNEX.equalsIgnoreCase(showAsAttr)) {
                    showAsAttr = "I";
                } else {
                    showAsAttr = showAsAttr.substring(ANNEX.length() + 1);
                }
                Integer num = RomanNumeral.parse(showAsAttr);
                list.put(num,attachment);
                parentNode.removeChild(attachment);
            }
            list.forEach((key,node)->{
                parentNode.appendChild(node);
            });
            xmlContent = xercesUtils.nodeToByteArray(document);
            xmlContent = xmlContentProcessor.doXMLPostProcessing(xmlContent);
        }
        return xmlContent;
    }

}
