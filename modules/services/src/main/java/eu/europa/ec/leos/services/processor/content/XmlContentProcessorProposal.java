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
package eu.europa.ec.leos.services.processor.content;

import eu.europa.ec.leos.domain.common.InstanceType;
import eu.europa.ec.leos.instance.Instance;
import eu.europa.ec.leos.model.action.SoftActionType;
import eu.europa.ec.leos.model.user.User;
import eu.europa.ec.leos.model.xml.Element;
import eu.europa.ec.leos.services.clone.CloneContext;
import eu.europa.ec.leos.services.numbering.NumberProcessorHandler;
import eu.europa.ec.leos.services.support.XercesUtils;
import eu.europa.ec.leos.vo.toc.NumberingConfig;
import eu.europa.ec.leos.vo.toc.TableOfContentItemVO;
import eu.europa.ec.leos.vo.toc.TocItem;
import io.atlassian.fugue.Pair;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static eu.europa.ec.leos.services.support.XercesUtils.getFirstChild;
import static eu.europa.ec.leos.services.support.XmlHelper.ARTICLE;
import static eu.europa.ec.leos.services.support.XmlHelper.CITATION;
import static eu.europa.ec.leos.services.support.XmlHelper.ELEMENTS_TO_BE_NUMBERED;
import static eu.europa.ec.leos.services.support.XmlHelper.EMPTY_STRING;
import static eu.europa.ec.leos.services.support.XmlHelper.HEADING;
import static eu.europa.ec.leos.services.support.XmlHelper.INDENT;
import static eu.europa.ec.leos.services.support.XmlHelper.INTRO;
import static eu.europa.ec.leos.services.support.XmlHelper.LEOS_DEPTH_ATTR;
import static eu.europa.ec.leos.services.support.XmlHelper.LEOS_EDITABLE_ATTR;
import static eu.europa.ec.leos.services.support.XmlHelper.LEOS_ORIGIN_ATTR;
import static eu.europa.ec.leos.services.support.XmlHelper.LEOS_SOFT_ACTION_ATTR;
import static eu.europa.ec.leos.services.support.XmlHelper.LEOS_SOFT_ACTION_ROOT_ATTR;
import static eu.europa.ec.leos.services.support.XmlHelper.LEOS_SOFT_DATE_ATTR;
import static eu.europa.ec.leos.services.support.XmlHelper.LEOS_SOFT_MOVE_FROM;
import static eu.europa.ec.leos.services.support.XmlHelper.LEOS_SOFT_MOVE_TO;
import static eu.europa.ec.leos.services.support.XmlHelper.LEOS_SOFT_USER_ATTR;
import static eu.europa.ec.leos.services.support.XmlHelper.LEVEL;
import static eu.europa.ec.leos.services.support.XmlHelper.LS;
import static eu.europa.ec.leos.services.support.XmlHelper.MAIN_BODY;
import static eu.europa.ec.leos.services.support.XmlHelper.PARAGRAPH;
import static eu.europa.ec.leos.services.support.XmlHelper.POINT;
import static eu.europa.ec.leos.services.support.XmlHelper.PREFACE;
import static eu.europa.ec.leos.services.support.XmlHelper.RECITAL;
import static eu.europa.ec.leos.services.support.XmlHelper.SOFT_DELETE_PLACEHOLDER_ID_PREFIX;
import static eu.europa.ec.leos.services.support.XmlHelper.SOFT_MOVE_PLACEHOLDER_ID_PREFIX;
import static eu.europa.ec.leos.services.support.XmlHelper.SUBPARAGRAPH;
import static eu.europa.ec.leos.services.support.XmlHelper.SUBPOINT;
import static eu.europa.ec.leos.services.support.XmlHelper.XMLID;
import static eu.europa.ec.leos.services.support.XmlHelper.getDateAsXml;
import static eu.europa.ec.leos.services.support.XercesUtils.addAttribute;
import static eu.europa.ec.leos.services.support.XercesUtils.createXercesDocument;
import static eu.europa.ec.leos.services.support.XercesUtils.getId;
import static eu.europa.ec.leos.services.support.XercesUtils.nodeToByteArray;
import static eu.europa.ec.leos.services.support.XercesUtils.nodeToString;
import static eu.europa.ec.leos.services.support.XercesUtils.updateXMLIDAttributeFullStructureNode;
import static eu.europa.ec.leos.services.support.XmlHelper.getSoftUserAttribute;
import static eu.europa.ec.leos.util.LeosDomainUtil.wrapXmlFragment;

@Service
@Instance(instances = {InstanceType.OS, InstanceType.COMMISSION})
public class XmlContentProcessorProposal extends XmlContentProcessorImpl {

    private static final Logger LOG = LoggerFactory.getLogger(XmlContentProcessorProposal.class);

    private static final List<String> POINT_PARENT_ELEMENTS = Arrays.asList(ARTICLE, LEVEL, PARAGRAPH);

    @Autowired
    private CloneContext cloneContext;
    
    @Autowired
    private NumberProcessorHandler numberProcessorHandler;
    
    public Node buildTocItemContent(List<TocItem> tocItems, List<NumberingConfig> numberingConfigs, Map<TocItem, List<TocItem>> tocRules,
                                    Document document, Node parentNode, TableOfContentItemVO tocVo, User user) {

        // 1. Get the corresponding node from the XML, or create a new one using the template
        Node node = getNode(document, tocVo);
        LOG.debug("buildTocItemContent for tocItemName '{}', tocItemId '{}', nodeName '{}', nodeId '{}', children {}", tocVo.getTocItem().getAknTag().value(), tocVo.getId(), node.getNodeName(), getId(node), tocVo.getChildItemsView().size());

        // 2. Store the node details in temp variables
        Node numNode = buildNumNode(node, tocVo);
        Node headingNode = buildHeadingNode(node, tocVo, user);
        Node introNode = getFirstChild(node, INTRO);  //recitals intro
        List<Node> childrenNode = XmlContentProcessorHelper.extractLevelNonTocItems(tocItems, tocRules, node, tocVo);

        // 3. clean the node and build it again.
        node.setTextContent(EMPTY_STRING);
        updateDepthAttribute(tocVo, node);
        appendChildIfNotNull(numNode, node);
        appendChildIfNotNull(headingNode, node);
        appendChildIfNotNull(introNode, node);

        // 4. Propagate to children
        for (TableOfContentItemVO child : tocVo.getChildItemsView()) {
            Node newChild = buildTocItemContent(tocItems, numberingConfigs, tocRules, document, node, child, user);
            LOG.debug("buildTocItemContent adding {} '{}' as child of {}", newChild.getNodeName(), getId(newChild), node.getNodeName());
            XercesUtils.addChild(newChild, node);
        }

        appendChildrenIfNotNull(childrenNode, node); // only for part of the body which is not configured in structure.xml, like CLAUSE tag

        if (isClonedProposal()) {
            if (SoftActionType.MOVE_TO.equals(tocVo.getSoftActionAttr())) {
                updateXMLIDAttributeFullStructureNode(node, SOFT_MOVE_PLACEHOLDER_ID_PREFIX, false);
            } else if (SoftActionType.DELETE.equals(tocVo.getSoftActionAttr())) {
                updateXMLIDAttributeFullStructureNode(node, SOFT_DELETE_PLACEHOLDER_ID_PREFIX, false);
            }
            processSoftElements(node, tocVo, user);
        }
        return node;
    }

    private void processSoftElements(Node node, TableOfContentItemVO tocVo, User user) {
        XercesUtils.addAttribute(node, LEOS_ORIGIN_ATTR, tocVo.getOriginAttr());
        String moveId = null;
        if (tocVo.getSoftActionAttr() != null) {
            switch (tocVo.getSoftActionAttr()) {
                case MOVE_TO:
                    moveId = tocVo.getSoftMoveTo();
                    break;
                case MOVE_FROM:
                    moveId = tocVo.getSoftMoveFrom();
                    break;
                default:
                    moveId = null;
            }
        }
        XmlContentProcessorHelper.updateSoftInfo(node, tocVo.getSoftActionAttr(), tocVo.isSoftActionRoot(), user, tocVo.getOriginAttr(), moveId, tocVo.getTocItem().getAknTag().value(), tocVo);
    }

    private void updateDepthAttribute(TableOfContentItemVO tocVo, Node node) {
        if (tocVo.getItemDepth() > 0) {
            addAttribute(node, LEOS_DEPTH_ATTR, String.valueOf(tocVo.getItemDepth()));
        }
    }

    @Override
    protected Pair<byte[], Element> buildSplittedElementPair(byte[] xmlContent, Element splitElement) {
        if (splitElement == null) {
            return null;
        }
        return new Pair<>(xmlContent, splitElement);
    }

    @Override
    public Element getMergeOnElement(byte[] xmlContent, String content, String tagName, String idAttributeValue) {
        throw new IllegalStateException("Operation non implemented for this instance");
    }

    @Override
    public byte[] mergeElement(byte[] xmlContent, String content, String tagName, String idAttributeValue) {
        throw new IllegalStateException("Operation non implemented for this instance");
    }

    @Override
    public boolean needsToBeIndented(String elementContent) {
        return false;
    }

    @Override
    public byte[] indentElement(byte[] xmlContent, String elementName, String elementId, String elementContent, List<TableOfContentItemVO> toc) throws IllegalArgumentException {
        return xmlContent;
    }

    @Override
    public byte[] removeElementById(byte[] xmlContent, String elementId) {
        if (isClonedProposal()) {
            Element element = getElementById(xmlContent, elementId);
            if (element == null) {
                return xmlContent;
            }
            return removeElement(xmlContent, element, LS);
        }
        return deleteElementById(xmlContent, elementId);
    }

    @Override
    public void removeElement(Node node) {
        if (isClonedProposal()) {
            super.removeElement(node);
        } else {
            XercesUtils.deleteElement(node);
        }
    }

    @Override
    public void specificInstanceXMLPostProcessing(Node node) {
        if (isClonedProposal()) {
            removeTempIdAttributeIfExists(node);
            updateSoftMoveLabelAttribute(node, LEOS_SOFT_MOVE_TO);
            updateSoftMoveLabelAttribute(node, LEOS_SOFT_MOVE_FROM);
            updateNewElements(node, CITATION, null, LS);
            updateNewElements(node, RECITAL, null, LS);
            updateNewElements(node, ARTICLE, null, LS);
            updateNewElements(node, PARAGRAPH, SUBPARAGRAPH, LS);
            updateNewElements(node, POINT, SUBPOINT, LS);
            updateNewElements(node, INDENT, SUBPOINT, LS);
            updateNewElements(node, PREFACE, null, LS);
            updateNewElements(node, MAIN_BODY, null, LS);
            updateNewElements(node, LEVEL, SUBPARAGRAPH, LS);
        }
    }

    @Override
    public Pair<byte[], String> updateSoftMovedElement(byte[] xmlContent, String elementContent) {
        Pair<byte[], String> result = new Pair<>(xmlContent, new String()); //default result
        Document fragment = createXercesDocument(wrapXmlFragment(elementContent).getBytes(StandardCharsets.UTF_8));
        NodeList softMovedNodes = XercesUtils.getElementsByXPath(fragment, String.format("//*[@%s]", LEOS_SOFT_MOVE_FROM));

        Document document = createXercesDocument(xmlContent);
        for (int nodeIdx = 0; nodeIdx < softMovedNodes.getLength(); nodeIdx++) {
            Node node = softMovedNodes.item(nodeIdx);
            String idAttrVal = XercesUtils.getAttributeValue(node, XMLID);
            if(idAttrVal != null && idAttrVal.indexOf("temp_") != -1) {
                String updatedIdAttrVal = idAttrVal.replace("temp_", EMPTY_STRING);
                String xPath = "//*[@xml:id = '" + updatedIdAttrVal + "']";
                NodeList sourceNodes = XercesUtils.getElementsByXPath(fragment, xPath);
                if(sourceNodes != null && sourceNodes.getLength() > 0) { //If moved within article
                    insertSoftMovedAttributesAndRenumber(updatedIdAttrVal, sourceNodes.item(0), document);
                    result = new Pair<>(new byte[0], nodeToString(fragment.getFirstChild().getFirstChild()));
                } else { //If moved between articles
                    sourceNodes = XercesUtils.getElementsByXPath(document, xPath);
                    if(sourceNodes != null && sourceNodes.getLength() > 0) {
                        insertSoftMovedAttributesAndRenumber(updatedIdAttrVal, sourceNodes.item(0), document);
                        result = new Pair<>(nodeToByteArray(document), new String());
                    }
                }
            }
        }
        return result;
    }

    private void insertSoftMovedAttributesAndRenumber(String idAttrVal, Node sourceNode, Node document) {
        Validate.notNull(idAttrVal, "Id attribute should not be null");
        Validate.notNull(sourceNode, "source node should not be null");

        Node parentNode = sourceNode.getParentNode();
        String tagName = sourceNode.getNodeName();
        String xPath = "//*[@xml:id = '" + SOFT_MOVE_PLACEHOLDER_ID_PREFIX + idAttrVal + "']";
        NodeList movedNodes = XercesUtils.getElementsByXPath(document, xPath);
        if (movedNodes == null || movedNodes.getLength() == 0) {
            String sourceNodeIdVal = XercesUtils.getAttributeValue(sourceNode, XMLID);
            XercesUtils.addAttribute(sourceNode, XMLID, SOFT_MOVE_PLACEHOLDER_ID_PREFIX + sourceNodeIdVal);
            XercesUtils.addAttribute(sourceNode, LEOS_SOFT_ACTION_ATTR, SoftActionType.MOVE_TO.getSoftAction());
            XercesUtils.addAttribute(sourceNode, LEOS_SOFT_USER_ATTR, getSoftUserAttribute(securityContext.getUser()));
            XercesUtils.addAttribute(sourceNode, LEOS_SOFT_DATE_ATTR, getDateAsXml());
            XercesUtils.addAttribute(sourceNode, LEOS_SOFT_ACTION_ROOT_ATTR, "true");
            XercesUtils.addAttribute(sourceNode, LEOS_SOFT_MOVE_TO, idAttrVal);

            //Add leos:editable=false to make this element read-only inside CKE
            XercesUtils.addAttribute(sourceNode, LEOS_EDITABLE_ATTR, "false");
        } else { //Node was already moved so delete the intermediate node to avoid duplicate soft moved nodes
            XercesUtils.deleteElement(sourceNode);
            XercesUtils.addAttribute(movedNodes.item(0), LEOS_SOFT_DATE_ATTR, getDateAsXml());
        }
        while (parentNode != null && !POINT_PARENT_ELEMENTS.contains(parentNode.getNodeName())) {
            parentNode = parentNode.getParentNode();
        }
        try {
            if (ELEMENTS_TO_BE_NUMBERED.contains(tagName)) {
                numberProcessorHandler.renumberElement(parentNode, tagName, true);
            }
        } catch (Exception e) {
            LOG.error("Unable to renumber element", e);
        }
    }

    private void removeTempIdAttributeIfExists(Node node) {
        String xPath = "//*[starts-with(@xml:id,'temp_')]";
        NodeList nodes = XercesUtils.getElementsByXPath(node, xPath);
        for (int idx = 0; idx < nodes.getLength(); idx++) {
            Node tempIdNode = nodes.item(idx);
            String tempIdNodeVal = XercesUtils.getAttributeValue(tempIdNode, XMLID);
            if (tempIdNodeVal != null && tempIdNodeVal.indexOf("temp_") != -1) {
                String updatedIdAttrVal = tempIdNodeVal.replace("temp_", EMPTY_STRING);
                XercesUtils.addAttribute(tempIdNode, XMLID, updatedIdAttrVal);
            }
        }
    }

    private boolean isClonedProposal() {
        return cloneContext != null && cloneContext.isClonedProposal();
    }

    private Node buildHeadingNode(Node node, TableOfContentItemVO tocVo, User user) {
        Node headingNode = XmlContentProcessorHelper.extractOrBuildHeaderElement(node, tocVo, user);
        if (isClonedProposal()) {
            XmlContentProcessorHelper.addUserInfoIfContentHasChanged(getFirstChild(node, HEADING), headingNode, user);
        }
        return headingNode;
    }

    private Node buildNumNode(Node node, TableOfContentItemVO tocVo) {
        Node numNode = XmlContentProcessorHelper.extractOrBuildNumElement(node, tocVo);
        if (!isClonedProposal() && XercesUtils.containsAttributeWithValue(numNode, LEOS_ORIGIN_ATTR, LS)) {
            // On TOC drag & drop there´s no clone context (request scope) and then items are added with
            // soft action ADD and origin LS on no cloned proposals.
            XercesUtils.removeAttribute(numNode, LEOS_ORIGIN_ATTR);
        }
        return numNode;
    }
}