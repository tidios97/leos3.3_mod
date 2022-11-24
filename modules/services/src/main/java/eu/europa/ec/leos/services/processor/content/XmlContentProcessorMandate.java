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

import static eu.europa.ec.leos.services.compare.ContentComparatorService.CONTENT_SOFT_REMOVED_CLASS;
import static eu.europa.ec.leos.services.processor.content.TableOfContentHelper.getItemFromTocById;
import static eu.europa.ec.leos.services.processor.content.TableOfContentHelper.hasTocItemSoftAction;
import static eu.europa.ec.leos.services.processor.content.TableOfContentHelper.hasTocItemSoftOrigin;
import static eu.europa.ec.leos.services.processor.content.XmlContentProcessorHelper.createNumContent;
import static eu.europa.ec.leos.services.processor.content.XmlContentProcessorHelper.getTagValueFromTocItemVo;
import static eu.europa.ec.leos.services.processor.content.XmlContentProcessorHelper.updateSoftInfo;
import static eu.europa.ec.leos.services.processor.content.XmlContentProcessorHelper.updateTocItemTypeAttributes;
import static eu.europa.ec.leos.services.processor.content.indent.IndentConversionHelper.PARAGRAPH_LEVEL_ITEMS;
import static eu.europa.ec.leos.services.support.LeosXercesUtils.formatHeadingNodeForDivision;
import static eu.europa.ec.leos.services.support.XercesUtils.addAttribute;
import static eu.europa.ec.leos.services.support.XercesUtils.createElement;
import static eu.europa.ec.leos.services.support.XercesUtils.createNodeFromXmlFragment;
import static eu.europa.ec.leos.services.support.XercesUtils.createXercesDocument;
import static eu.europa.ec.leos.services.support.XercesUtils.getFirstChild;
import static eu.europa.ec.leos.services.support.XercesUtils.getId;
import static eu.europa.ec.leos.services.support.XercesUtils.updateXMLIDAttributeFullStructureNode;
import static eu.europa.ec.leos.services.support.XmlHelper.*;
import static eu.europa.ec.leos.vo.toc.StructureConfigUtils.HASH_NUM_VALUE;
import static eu.europa.ec.leos.vo.toc.StructureConfigUtils.getNumberingTypeByTagNameAndTocItemType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

import eu.europa.ec.leos.vo.toc.TocItemTypeName;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import eu.europa.ec.leos.domain.common.InstanceType;
import eu.europa.ec.leos.instance.Instance;
import eu.europa.ec.leos.model.action.SoftActionType;
import eu.europa.ec.leos.model.user.User;
import eu.europa.ec.leos.model.xml.Element;
import eu.europa.ec.leos.services.processor.content.indent.IndentConversionHelper;
import eu.europa.ec.leos.services.processor.content.indent.IndentHelper;
import eu.europa.ec.leos.services.support.IdGenerator;
import eu.europa.ec.leos.services.support.XercesUtils;
import eu.europa.ec.leos.services.support.XmlHelper;
import eu.europa.ec.leos.vo.toc.AknTag;
import eu.europa.ec.leos.vo.toc.NumberingConfig;
import eu.europa.ec.leos.vo.toc.NumberingType;
import eu.europa.ec.leos.vo.toc.StructureConfigUtils;
import eu.europa.ec.leos.vo.toc.TableOfContentItemVO;
import eu.europa.ec.leos.vo.toc.TocItem;
import eu.europa.ec.leos.vo.toc.indent.IndentedItemType;
import io.atlassian.fugue.Pair;

@Service
@Instance(instances = {InstanceType.COUNCIL})
public class XmlContentProcessorMandate extends XmlContentProcessorImpl {

    private static final Logger LOG = LoggerFactory.getLogger(XmlContentProcessorMandate.class);

    @Autowired
    private IndentHelper indentHelper;

    @Autowired
    private IndentConversionHelper indentConversionHelper;

    protected Node buildTocItemContent(List<TocItem> tocItems, List<NumberingConfig> numberingConfigs, Map<TocItem, List<TocItem>> tocRules,
                                       Document document, Node parentNode, TableOfContentItemVO tocVo, User user) {
        Node node = getNode(document, tocVo);
        TocItemTypeName tocItemType = StructureConfigUtils.getTocItemTypeFromTagNameAndAttributes(tocItems, getTagValueFromTocItemVo(tocVo),
                XercesUtils.getAttributes(node));
        Node newNode = node.cloneNode(false);
        LOG.debug("buildTocItemContent for tocItemName '{}', tocItemId '{}', nodeName '{}', nodeId '{}', children {}", tocVo.getTocItem().getAknTag().value(), tocVo.getId(), node.getNodeName(), getId(node), tocVo.getChildItemsView().size());

        appendChildIfNotNull(buildNumNode(node, tocVo), newNode);
        appendChildIfNotNull(buildHeadingNode(node, tocVo, user, newNode), newNode);
        appendChildIfNotNull(getFirstChild(node, INTRO), newNode); //recitals intro

        if (!tocVo.getTocItemType().equals(tocItemType) && hasTocItemSoftOrigin(tocVo, EC)) {
            NumberingType newNumberingType = getNumberingTypeByTagNameAndTocItemType(tocItems, tocVo.getTocItemType(), POINT);
            updateOriginOfPointsInArticle(tocVo, newNumberingType);
        }
        for (TableOfContentItemVO child : tocVo.getChildItemsView()) {
            Node newChild = buildTocItemContent(tocItems, numberingConfigs, tocRules, document, newNode, child, user);
            appendChildIfNotNull(newChild, newNode);
        }
        String tagName = tocVo.getTocItem().getAknTag().value();
        if (Arrays.asList(PARAGRAPH, LEVEL).contains(tagName) && skipParagraphContent(tocVo)) {
            buildParagraphOrLevelContent(tocItems, node, newNode, tocVo, user);
        } else if (Arrays.asList(POINT, INDENT).contains(tagName) && shouldWrapWithList(tocVo.getParentItem())) {
            newNode = buildPointContentAndWrapWithPoint(tocItems, numberingConfigs, node, newNode, tocVo, user);
            return constructListStructure(newNode, parentNode, tocVo, user);
        } else if (Arrays.asList(POINT, INDENT).contains(tagName) && skipPointContent(tocVo)) {
            buildPointContent(tocItems, node, newNode, tocVo, user);
        } else if (Arrays.asList(SUBPARAGRAPH, SUBPOINT).contains(tagName) && isSingleSubElement(tocVo) && !isSoftDeletedOrMoved(tocVo)) {
            return getFirstChild(node, CONTENT).cloneNode(true);
        } else if (tagName.equals(LIST) && isEmptyElement(tocVo)) {
            return null; // remove list content if there is no child
        } else {
            newNode = buildExistingNode(tocItems, numberingConfigs, tocRules, document, node, newNode, tocVo);
        }
        buildNodeAttributes(newNode, tocVo, user);
        if (tocVo.isIndentedOrRestored()) {
            setIndentAttributes(newNode, tocVo);
        }
        if (tagName.equals(LIST) && tocVo.getParentItem().isAffected()) {
            TableOfContentItemVO firstChild = tocVo.getChildItemsView().get(0);
            XercesUtils.insertOrUpdateAttributeValue(newNode, LEOS_LIST_TYPE_ATTR, firstChild.getTocItem().getNumberingType().toString().toLowerCase());
        }
        updateTocItemTypeAttributes(tocItems, newNode, tocVo);
        return newNode;
    }

    private void updateOriginOfPointsInArticle(TableOfContentItemVO item, NumberingType toNumberingType) {
        for (TableOfContentItemVO child : item.getChildItems()) {
            if (getTagValueFromTocItemVo(child).equals(PARAGRAPH)) {
                child.setAffected(true);
            }
            if (child.getTocItem().getNumberingType().equals(toNumberingType)) {
                child.setOriginNumAttr(CN);
                child.setAffected(true);
            }
            updateOriginOfPointsInArticle(child, toNumberingType);
        }
    }

    private Node buildNumNode(Node node, TableOfContentItemVO tocVo) {
        String tagName = tocVo.getTocItem().getAknTag().value();
        Node numNode = null;
        if (!tocVo.isIndentedOrRestored()
                && !(Arrays.asList(PARAGRAPH, LEVEL).contains(tagName) && skipParagraphContent(tocVo))
                && !(Arrays.asList(POINT, INDENT).contains(tagName) && skipPointContent(tocVo))
                && !Arrays.asList(SUBPARAGRAPH, CITATION).contains(tagName)
        ) {
            numNode = extractOrBuildNumElement(node, tocVo);
            //this method does the num toggle processing
            numNode = numberElementToggleProcessing(numNode, tocVo);
        } else if (tocVo.isIndentedOrRestored() && Arrays.asList(PARAGRAPH, POINT, INDENT).contains(tagName)) {
            numNode = buildNumFromIndentedElement(node, tocVo);
            if (numNode != null) {
                numNode = numberElementToggleProcessing(numNode, tocVo);
            }
        }
        return numNode;
    }

    private boolean skipParagraphContent(TableOfContentItemVO tocVo) {
        boolean skipParagraphContent = false;
        List<TableOfContentItemVO> childList = tocVo.getChildItems();
        if (!childList.isEmpty()) {
            skipParagraphContent = true;
            for (TableOfContentItemVO child : childList) {
                // if is not a new SUBPARAGRAPH
                if (child.getNode() != null && child.getTocItem().getAknTag().value().equals(SUBPARAGRAPH) && !child.isMovedOnEmptyParent()) {
                    return false;
                }
            }
        }
        return skipParagraphContent;
    }

    private boolean skipPointContent(TableOfContentItemVO tocVo) {
        boolean skipPointContent = false;
        List<TableOfContentItemVO> childList = tocVo.getChildItems();
        if (childList != null && !childList.isEmpty()) {
            TableOfContentItemVO child = childList.get(0);
            String tagValue = child.getTocItem().getAknTag().value();
            skipPointContent = Arrays.asList(LIST, POINT, INDENT, CROSSHEADING).contains(tagValue);
        }
        return skipPointContent;
    }

    private Node extractOrBuildNumElement(Node node, TableOfContentItemVO tocVo) {
        Node numNode = XmlContentProcessorHelper.extractOrBuildNumElement(node, tocVo);
        if (numNode!= null && StringUtils.isNotEmpty(tocVo.getNumber())) {
            addAttribute(numNode, LEOS_ORIGIN_ATTR, tocVo.getOriginNumAttr());
        }
        return numNode;
    }

    private Node numberElementToggleProcessing(Node node, TableOfContentItemVO tocVo) {
        if (tocVo.getTocItem().getAknTag().value().equals(PARAGRAPH)) {
            if (tocVo.getParentItem().isNumberingToggled() != null) {
                if (tocVo.getParentItem().isNumberingToggled()) {
                    if (isNumberSoftDeleted(tocVo)) {// if a para is soft deleted and numbering is toggled
                        updateSoftActionOnNumElement(node, null, BACK_TO_NUM_FROM_SOFT_DELETED);
                    } else {
                        updateSoftActionOnNumElement(node, SoftActionType.ADD, TOGGLED_TO_NUM);
                    }
                } else {
                    if (!isNumberSoftAdded(tocVo) && (!tocVo.isIndented() || tocVo.getOriginNumAttr() == null || !tocVo.getOriginNumAttr().equals(CN))) {
                        // only if a para is NOT soft added
                        updateSoftActionOnNumElement(node, SoftActionType.DELETE, null);
                    } else if (tocVo.isIndented() && (isNumberSoftAdded(tocVo) || (tocVo.getOriginNumAttr() != null && tocVo.getOriginNumAttr().equals(CN)))) {
                        // When num is CN and element has been indented: num should be removed
                        return null;
                    }
                }
            } else if (tocVo.getNumSoftActionAttr() != null){//in case paragraph is moved which was toggled to num before, removing soft attributes from num element
                updateSoftActionOnNumElement(node, tocVo.getNumSoftActionAttr(), EMPTY_STRING);
            } else {
                updateSoftActionOnNumElement(node, null, EMPTY_STRING);
            }
        }
        return node;
    }

    private boolean isNumberSoftDeleted(TableOfContentItemVO tocVo) {
        return tocVo.getNumSoftActionAttr() != null && SoftActionType.DELETE.equals(tocVo.getNumSoftActionAttr());
    }

    private boolean isNumberSoftAdded(TableOfContentItemVO tocVo) {
        return tocVo.getNumSoftActionAttr() != null && SoftActionType.ADD.equals(tocVo.getNumSoftActionAttr());
    }

    private void updateSoftActionOnNumElement(Node node, SoftActionType softAction, String setToggledToNum) {
        if (node == null) {
            return;
        }
        if (softAction != null) {
            updateSoftAction(softAction, node);
        } else if (StringUtils.isNotEmpty(setToggledToNum)) {
            removeSoftAttributes(node);
        }
        if (TOGGLED_TO_NUM.equals(setToggledToNum)) {
            XercesUtils.insertOrUpdateAttributeValue(node, setToggledToNum, Boolean.TRUE.toString());
        }
    }

    private void updateSoftAction(SoftActionType softAction, Node node) {
        updateSoftAttributes(softAction, node, true);
        if (SoftActionType.DELETE.equals(softAction)) {
            updateXMLIDAttributeFullStructureNode(node, SOFT_DELETE_PLACEHOLDER_ID_PREFIX, true);
        }
    }

    private void removeSoftAttributes(Node node) {
        XercesUtils.removeAttribute(node, LEOS_SOFT_ACTION_ATTR);
        XercesUtils.removeAttribute(node, LEOS_SOFT_ACTION_ROOT_ATTR);
        XercesUtils.removeAttribute(node, LEOS_SOFT_USER_ATTR);
        XercesUtils.removeAttribute(node, LEOS_SOFT_DATE_ATTR);
        XercesUtils.updateXMLIDAttribute(node, EMPTY_STRING, true);
    }

    private Node convertToElement(List<TocItem> tocItems, Node node, String elementName) {
        String elementTemplate = XmlHelper.getTemplate(StructureConfigUtils.getTocItemByNameOrThrow(tocItems, elementName), messageHelper);
        Node elementNode = createNodeFromXmlFragment(node.getOwnerDocument(), elementTemplate.getBytes(UTF_8), false);
        elementNode.setTextContent(EMPTY_STRING);
        appendChildIfNotNull(getFirstChild(node, CONTENT), elementNode);
        return elementNode;
    }

    private Node convertToSubParagraph(List<TocItem> tocItems, Node node, TableOfContentItemVO tocVo, User user) {
        Node subParNode = convertToElement(tocItems, node, SUBPARAGRAPH);
        updateSoftInfo(subParNode, SoftActionType.ADD, Boolean.TRUE, user, CN, null, tocVo.getTocItem().getAknTag().value(), null);
        return subParNode;
    }

    private Node buildNumFromIndentedElement(Node node, TableOfContentItemVO tocVo) {
        Node numNode = null;
        if (StringUtils.isNotEmpty(tocVo.getNumber())) {
            String newId = tocVo.getElementNumberId() != null ? tocVo.getElementNumberId()
                    : IdGenerator.generateId(NUM.substring(0, 3), 7);
            if (newId.startsWith(SOFT_DELETE_PLACEHOLDER_ID_PREFIX) && (tocVo.getNumSoftActionAttr() == null
                    || !tocVo.getNumSoftActionAttr().equals(SoftActionType.DELETE)
                    || !getTagValueFromTocItemVo(tocVo).equals(PARAGRAPH))) {
                newId = newId.substring(SOFT_DELETE_PLACEHOLDER_ID_PREFIX.length());
            }
            String newNum = createNumContent(tocVo);
            numNode = createElement(node.getOwnerDocument(), NUM, newId, newNum);
            XercesUtils.insertOrUpdateAttributeValue(numNode, LEOS_ORIGIN_ATTR, tocVo.getOriginNumAttr());
            if (tocVo.getNumSoftActionAttr() != null
                    && tocVo.getNumSoftActionAttr().equals(SoftActionType.DELETE)
                    && getTagValueFromTocItemVo(tocVo).equals(PARAGRAPH)) {
                updateSoftActionOnNumElement(numNode, SoftActionType.DELETE, null);
                XercesUtils.insertOrUpdateAttributeValue(numNode, LEOS_ORIGIN_ATTR, EC);
            }
        } else if (getTagValueFromTocItemVo(tocVo).equals(PARAGRAPH)) {
            numNode = extractOrBuildNumElement(node, tocVo);
        }
        return numNode;
    }

    private Node buildHeadingNode(Node node, TableOfContentItemVO tocVo, User user, Node newNode) {
        Node headingNode = null;
        if (!LEVEL.equals(tocVo.getTocItem().getAknTag().value()) || !skipParagraphContent(tocVo)) {
            headingNode = XmlContentProcessorHelper.extractOrBuildHeaderElement(node, tocVo, user);
            Node numNode = getFirstChild(node, XercesUtils.getNumTag(newNode.getNodeName()));
            if (node.getNodeName().equals(DIVISION) && numNode.getTextContent().equals(HASH_NUM_VALUE)) {
                formatHeadingNodeForDivision(node, tocVo, headingNode);
            }
            XmlContentProcessorHelper.addUserInfoIfContentHasChanged(getFirstChild(node, HEADING), headingNode, user);
        }
        return headingNode;
    }

    private int getPointDepthInToc(TableOfContentItemVO tocVo, int pointDepth) {
        TableOfContentItemVO parentItemVO = tocVo;
        while (true) {
            parentItemVO = parentItemVO.getParentItem();
            if (getTagValueFromTocItemVo(parentItemVO).equals(POINT) || getTagValueFromTocItemVo(parentItemVO).equals(INDENT)) {
                pointDepth++;
            } else if (getTagValueFromTocItemVo(parentItemVO).equals(PARAGRAPH) || getTagValueFromTocItemVo(parentItemVO).equals(LEVEL)) {
                break;
            }
        }
        return pointDepth;
    }

    private void setIndentAttributes(Node node, TableOfContentItemVO tocVo) {
        if (tocVo.isIndented()) {
            XercesUtils.insertOrUpdateAttributeValue(node, LEOS_INDENT_ORIGIN_TYPE_ATTR, tocVo.getIndentOriginType().name());
            XercesUtils.insertOrUpdateAttributeValue(node, LEOS_INDENT_ORIGIN_INDENT_LEVEL_ATTR, String.valueOf(tocVo.getIndentOriginIndentLevel()));
            if ((tocVo.getIndentOriginNumOrigin() == null || tocVo.getIndentOriginNumOrigin().equals(CN))
                    || tocVo.getIndentOriginType().equals(IndentedItemType.OTHER_SUBPARAGRAPH)
                    || tocVo.getIndentOriginType().equals(IndentedItemType.OTHER_SUBPOINT)
                    || tocVo.getNumber() == null
                    || tocVo.getNumber().isEmpty()
                    || !tocVo.getNumber().equals(tocVo.getIndentOriginNumValue())) {
                XercesUtils.insertOrUpdateAttributeValue(node, LEOS_INDENT_ORIGIN_NUM_ATTR, tocVo.getIndentOriginNumValue());
                XercesUtils.insertOrUpdateAttributeValue(node, LEOS_INDENT_ORIGIN_NUM_ID_ATTR, tocVo.getIndentOriginNumId());
            } else {
                XercesUtils.removeAttribute(node, LEOS_INDENT_ORIGIN_NUM_ATTR);
                XercesUtils.removeAttribute(node, LEOS_INDENT_ORIGIN_NUM_ID_ATTR);
            }
            XercesUtils.insertOrUpdateAttributeValue(node, LEOS_INDENT_ORIGIN_NUM_ORIGIN_ATTR, tocVo.getIndentOriginNumOrigin());
            if (tocVo.getTocItem().getAknTag().name().equalsIgnoreCase(PARAGRAPH) && StringUtils.isEmpty(tocVo.getNumber())) {
                XercesUtils.insertOrUpdateAttributeValue(node, LEOS_INDENT_UNUMBERED_PARAGRAPH, Boolean.TRUE.toString());
            } else {
                XercesUtils.removeAttribute(node, LEOS_INDENT_UNUMBERED_PARAGRAPH);
            }
        } else if (tocVo.isIndentedOrRestored()) {
            XercesUtils.removeAttribute(node, LEOS_INDENT_ORIGIN_NUM_ATTR);
            XercesUtils.removeAttribute(node, LEOS_INDENT_ORIGIN_NUM_ID_ATTR);
            XercesUtils.removeAttribute(node, LEOS_INDENT_ORIGIN_NUM_ORIGIN_ATTR);
            XercesUtils.removeAttribute(node, LEOS_INDENT_ORIGIN_TYPE_ATTR);
            XercesUtils.removeAttribute(node, LEOS_INDENT_ORIGIN_INDENT_LEVEL_ATTR);
            XercesUtils.removeAttribute(node, LEOS_INDENT_UNUMBERED_PARAGRAPH);
        }
    }

    private void buildParagraphOrLevelContent(List<TocItem> tocItems, Node node, Node newNode, TableOfContentItemVO tocVo, User user) {
        List<Node> parOrLevelChildrenNode = new ArrayList<>();
        if (tocVo.getNumber() != null && !tocVo.getNumber().isEmpty()) {
            parOrLevelChildrenNode.add(extractOrBuildNumElement(node, tocVo));
        }
        if (LEVEL.equals(tocVo.getTocItem().getAknTag().value())) {
            Node headingNode = XmlContentProcessorHelper.extractOrBuildHeaderElement(node, tocVo, user);
            XmlContentProcessorHelper.addUserInfoIfContentHasChanged(getFirstChild(node, HEADING), headingNode, user);
            parOrLevelChildrenNode.add(headingNode);
        }
        parOrLevelChildrenNode.add(convertToSubParagraph(tocItems, node, tocVo, user));
        parOrLevelChildrenNode.addAll(XercesUtils.getChildrenExcluding(newNode, Arrays.asList(NUM)));
        newNode.setTextContent(EMPTY_STRING);
        appendChildrenIfNotNull(parOrLevelChildrenNode, newNode);
        XercesUtils.insertOrUpdateAttributeValue(newNode, LEOS_ORIGIN_ATTR, tocVo.getOriginAttr());
    }

    private void buildPointContent(List<TocItem> tocItems, Node node, Node newNode, TableOfContentItemVO tocVo, User user) {
        if  (!hasTocItemSoftAction(tocVo, SoftActionType.TRANSFORM)) {
            List<Node> pointChildrenNode = new ArrayList<>();
            if (!skipPointContent(tocVo)) {
                pointChildrenNode.addAll(XercesUtils.getChildren(node));
            } else {
                pointChildrenNode.add(extractOrBuildNumElement(node, tocVo));
                pointChildrenNode.add(convertToSubPoint(tocItems, node, tocVo, user));
                if (newNode.getChildNodes().getLength() > 0 && !newNode.getChildNodes().item(0).getNodeName().equalsIgnoreCase(LIST)
                        && !newNode.getChildNodes().item(0).getNodeName().equalsIgnoreCase(CROSSHEADING)) {
                    pointChildrenNode.add(wrapWithList(newNode, tocVo, user));
                } else {
                    pointChildrenNode.addAll(XercesUtils.getChildren(newNode));
                }
            }
            newNode.setTextContent(EMPTY_STRING);
            appendChildrenIfNotNull(pointChildrenNode, newNode);
        }
        XercesUtils.insertOrUpdateAttributeValue(newNode, LEOS_ORIGIN_ATTR, tocVo.getOriginAttr());
    }

    private Node convertToSubPoint(List<TocItem> tocItems, Node node, TableOfContentItemVO tocVo, User user) {
        Node subPointNode = convertToElement(tocItems, node, SUBPOINT);
        updateSoftInfo(subPointNode, SoftActionType.ADD, Boolean.TRUE, user, CN, null, tocVo.getTocItem().getAknTag().value(), null);
        XercesUtils.insertOrUpdateAttributeValue(subPointNode, LEOS_ORIGIN_ATTR, CN);
        return subPointNode;
    }

    private Node wrapWithList(Node node, TableOfContentItemVO tocVo, User user) {
        Node listNode = createElement(node.getOwnerDocument(), LIST, IdGenerator.generateId(LIST.substring(0, 3), 7), EMPTY_STRING);
        SoftActionType softActionType = isParentListSoftMoved(tocVo) ? tocVo.getSoftActionAttr() : SoftActionType.ADD;
        updateSoftInfo(listNode, softActionType, Boolean.TRUE, user, CN, null, tocVo.getTocItem().getAknTag().value(), null);
        XercesUtils.insertOrUpdateAttributeValue(listNode, LEOS_ORIGIN_ATTR, CN);
        appendChildIfNotNull(node, listNode);
        return listNode;
    }

    private boolean isParentListSoftMoved(TableOfContentItemVO tocVo) {
        return LIST.equals(tocVo.getTocItem().getAknTag().value()) && tocVo.getParentItem().getSoftMoveFrom() != null;
    }

    private boolean shouldWrapWithList(TableOfContentItemVO tocVo) {
        boolean wrapWithList = true;
        List<TableOfContentItemVO> childItems = tocVo.getChildItems();
        if (!childItems.isEmpty()) {
            switch (tocVo.getTocItem().getAknTag().value()) {
                case PARAGRAPH:
                case POINT:
                case INDENT:
                case LEVEL:
                    wrapWithList = !tocVo.containsItem(LIST);
                    break;
                case LIST:
                    wrapWithList = false;
                    break;
            }
        }
        return wrapWithList;
    }

    private Node buildPointContentAndWrapWithPoint(List<TocItem> tocItems, List<NumberingConfig> numberingConfigs, Node node, Node newNode, TableOfContentItemVO tocVo, User user) {
        buildPointContent(tocItems, node, newNode, tocVo, user);
        return wrapWithPoint(numberingConfigs, newNode, tocVo, user);
    }

    private Node wrapWithPoint(List<NumberingConfig> numberingConfigs, Node node, TableOfContentItemVO tocVo, User user) {
        Node pointNode = createElement(node.getOwnerDocument(), isIndent(numberingConfigs, tocVo) ? INDENT : POINT, tocVo.getId(), EMPTY_STRING);
        updateSoftInfo(pointNode, tocVo.getSoftActionAttr(), tocVo.isSoftActionRoot(), user, tocVo.getOriginAttr(), getMoveId(tocVo), tocVo.getTocItem().getAknTag().value(), null);
        XercesUtils.insertOrUpdateAttributeValue(pointNode, LEOS_AFFECTED_ATTR, tocVo.isAffected() ? Boolean.TRUE.toString() : null);
        XercesUtils.insertOrUpdateAttributeValue(pointNode, LEOS_ORIGIN_ATTR, tocVo.getOriginAttr());
        appendChildrenIfNotNull(XercesUtils.getChildren(node), pointNode);
        return pointNode;
    }

    private Node constructListStructure(Node node, Node parentNode, TableOfContentItemVO tocVo, User user) {
        Node listNode = null;
        TableOfContentItemVO parentItem = tocVo.getParentItem();
        List<TableOfContentItemVO> childItemsOfType = constructChildListWithType(parentItem.getChildItems(), tocVo.getTocItem().getAknTag().value());
        if (tocVo.getId().equals(childItemsOfType.get(0).getId())) {
            listNode = wrapWithList(node, tocVo, user);
            updateListTypeAttributeForIndent(listNode, tocVo);
        } else {
            Node lastParentChildrenNode = parentNode.getLastChild();
            if (lastParentChildrenNode.getNodeName().equals(LIST)) {
                lastParentChildrenNode.appendChild(node.cloneNode(true));
            }
        }
        return listNode;
    }

    private List<TableOfContentItemVO> constructChildListWithType(List<TableOfContentItemVO> childItems, String type) {
        List<TableOfContentItemVO> childItemsOfType = new ArrayList<>();
        for (TableOfContentItemVO child : childItems) {
            String childTagValue = TableOfContentProcessor.getTagValueFromTocItemVo(child);
            if (childTagValue.equals(type)) {
                childItemsOfType.add(child);
            } else if (Arrays.asList(POINT,INDENT).contains(type) && Arrays.asList(POINT,INDENT).contains(childTagValue)) {
                childItemsOfType.add(child);
            }
        }
        return childItemsOfType;
    }

    private Node buildExistingNode(List<TocItem> tocItems, List<NumberingConfig> numberingConfigs, Map<TocItem, List<TocItem>> tocRules, Document document, Node node, Node newNode, TableOfContentItemVO tocVo) {
        Node existingNode = newNode;
        String tagName = tocVo.getTocItem().getAknTag().value();
        if (tagName.equals(POINT) || tagName.equals(INDENT)) {
            boolean isIndent = isIndent(numberingConfigs, tocVo);
            if (tagName.equals(POINT) && isIndent) {
                existingNode = XercesUtils.renameNode(document, newNode, INDENT);
            } else if (tagName.equals(INDENT) && !isIndent) {
                existingNode = XercesUtils.renameNode(document, newNode, POINT);
            }
        } else if (tagName.equals(BLOCK) || tagName.equalsIgnoreCase(CROSSHEADING)) {
            if (tagName.equals(BLOCK) && tocVo.isCrossHeading()) {
                existingNode = XercesUtils.renameNode(document, newNode, CROSSHEADING);
                XercesUtils.removeAttribute(existingNode, LEOS_CROSS_HEADING_BLOCK_NAME);
            } else if (tagName.equalsIgnoreCase(CROSSHEADING) && tocVo.isBlock()) {
                existingNode = XercesUtils.renameNode(document, newNode, BLOCK);
                XercesUtils.insertOrUpdateAttributeValue(existingNode, LEOS_CROSS_HEADING_BLOCK_NAME, CROSSHEADING);
            }
            if (tocVo.getNode() == null) {
                tableOfContentProcessor.setContentInNodeFromTocItem(tocVo, node);
            }
            if (tocVo.isCrossHeadingInList()) {
                XercesUtils.insertOrUpdateAttributeValue(existingNode, LEOS_CROSSHEADING_TYPE, LIST);
            }
        }
        if (ELEMENTS_WITH_TEXT.contains(tagName.toLowerCase())) {
            if ((tocVo.getNode() == null) && tocVo.getChildItemsView().isEmpty()) {
                appendChildrenIfNotNull(node.getChildNodes(), existingNode);
            } else {
                appendChildrenIfNotNull(XmlContentProcessorHelper.extractLevelNonTocItemsKeepingTextNodes(tocItems, tocRules, node, tocVo), existingNode);
            }
        } else {
            if ((tocVo.getNode() == null) && tocVo.getChildItemsView().isEmpty()) {
                existingNode.setTextContent(EMPTY_STRING);
                appendChildrenIfNotNull(XercesUtils.getChildren(node), existingNode);
            } else {
                appendChildrenIfNotNull(XmlContentProcessorHelper.extractLevelNonTocItems(tocItems, tocRules, node, tocVo), existingNode);
            }
        }
        if (hasTocItemSoftAction(tocVo, SoftActionType.TRANSFORM) && !tocVo.isIndentedOrRestored()) {
            XercesUtils.updateXMLIDAttribute(existingNode, SOFT_TRANSFORM_PLACEHOLDER_ID_PREFIX, true);
        }
        XercesUtils.insertOrUpdateAttributeValue(existingNode, LEOS_ORIGIN_ATTR, tocVo.getOriginAttr());
        return existingNode;
    }

    private boolean isSingleSubElement(TableOfContentItemVO tocVo) {
        boolean isSingle = false;
        List<TableOfContentItemVO> childList = tocVo.getParentItem().getChildItems();
        if (childList != null && !childList.isEmpty()) {
            TableOfContentItemVO firstChild = childList.get(0);
            switch (childList.size()) {
                case 1:
                    // If only single subparagraph or subpoint is remaining in the paragraph
                    if ((firstChild.getNode() != null && !firstChild.isMovedOnEmptyParent()) &&
                            (firstChild.getTocItem().getAknTag().value().equals(SUBPARAGRAPH) ||
                                    firstChild.getTocItem().getAknTag().value().equals(SUBPOINT))) {
                        isSingle = true;
                    }
                    break;
                case 2:
                    // If point inside list is deleted and empty list remaining in the paragraph.
                    TableOfContentItemVO secondChild = childList.get(1);
                    if (secondChild.getNode() != null && secondChild.getTocItem().getAknTag().value().equals(LIST) &&
                            secondChild.getChildItems().isEmpty()) {
                        isSingle = true;
                    }
                    break;
                default:
                    isSingle = false;
            }
        }
        return isSingle;
    }

    private boolean isSoftDeletedOrMoved(TableOfContentItemVO tocVo) {
        return SoftActionType.DELETE.equals(tocVo.getSoftActionAttr()) ||
                SoftActionType.MOVE_FROM.equals(tocVo.getSoftActionAttr()) ||
                SoftActionType.MOVE_TO.equals(tocVo.getSoftActionAttr());
    }

    private boolean isEmptyElement(TableOfContentItemVO tocVo) {
        List<TableOfContentItemVO> childList = tocVo.getChildItems();
        return childList == null || childList.isEmpty();
    }

    private void softDeleteAuthorialNote(Node node) {
        NodeList nodeList = node.getChildNodes();
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node childNode = nodeList.item(i);
            if ((childNode.getNodeType() == Node.ELEMENT_NODE) && (AUTHORIAL_NOTE.equals(childNode.getNodeName()))) {
                XercesUtils.insertOrUpdateAttributeValue(childNode, CLASS_ATTR, CONTENT_SOFT_REMOVED_CLASS);
            }
        }
    }

    private void softUndeleteAuthorialNote(Node node) {
        NodeList nodeList = node.getChildNodes();
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node childNode = nodeList.item(i);
            if ((childNode.getNodeType() == Node.ELEMENT_NODE) && (AUTHORIAL_NOTE.equals(childNode.getNodeName()))) {
                XercesUtils.removeAttribute(childNode, CLASS_ATTR);
            }
        }
    }

    private String getMoveId(TableOfContentItemVO tocVo) {
        String moveId = null;
        if (tocVo.getSoftActionAttr() != null && tocVo.getSoftActionAttr().equals(SoftActionType.MOVE_TO)) {
            moveId = tocVo.getSoftMoveTo();
        } else if (tocVo.getSoftActionAttr() != null && tocVo.getSoftActionAttr().equals(SoftActionType.MOVE_FROM)) {
            moveId = tocVo.getSoftMoveFrom();
        }
        return moveId;
    }

    private void updateListTypeAttributeForIndent(Node node, TableOfContentItemVO tocVo) {
        if (tocVo.getTocItem().getAknTag().equals(AknTag.LIST) && (tocVo.getParentItem().isAffected() || (tocVo.getOriginAttr() == null || tocVo.getOriginAttr().equals(CN))) && !tocVo.getChildItemsView().isEmpty() && tocVo.getChildItemsView().get(0).getTocItem().getNumberingType() != null) {
            TableOfContentItemVO firstChild = tocVo.getChildItemsView().get(0);
            for (int index = 1; index < tocVo.getChildItemsView().size() && !TableOfContentProcessor.getTagValueFromTocItemVo(firstChild).equals(INDENT) && !TableOfContentProcessor.getTagValueFromTocItemVo(firstChild).equals(POINT); index++) {
                firstChild = tocVo.getChildItemsView().get(index);
            }
            if (TableOfContentProcessor.getTagValueFromTocItemVo(firstChild).equals(INDENT) || TableOfContentProcessor.getTagValueFromTocItemVo(firstChild).equals(POINT)) {
                XercesUtils.insertOrUpdateAttributeValue(node, LEOS_LIST_TYPE_ATTR, firstChild.getTocItem().getNumberingType().toString().toLowerCase());
            }
        } else if (node.getNodeName().equals(LIST) && (tocVo.getTocItem().getAknTag().equals(AknTag.INDENT) || tocVo.getTocItem().getAknTag().equals(AknTag.POINT)) && (tocVo.getParentItem().isAffected() || (tocVo.getOriginAttr() == null || tocVo.getOriginAttr().equals(CN))) && tocVo.getTocItem().getNumberingType() != null) {
            XercesUtils.insertOrUpdateAttributeValue(node, LEOS_LIST_TYPE_ATTR, tocVo.getTocItem().getNumberingType().toString().toLowerCase());
        }
    }

    private void buildNodeAttributes(Node node, TableOfContentItemVO tocVo, User user) {
        if (StringUtils.isNotEmpty(tocVo.getStyle())) {
            if (node.getNodeName().equalsIgnoreCase(DIVISION)) {
                XercesUtils.addAttribute(node, CLASS_ATTR, tocVo.getStyle());
            } else {
                XercesUtils.insertOrUpdateAttributeValue(node, CLASS_ATTR, tocVo.getStyle());
            }
        }
        if (tocVo.isAutoNumOverwritten()) {
            XercesUtils.addAttribute(node, LEOS_AUTO_NUM_OVERWRITE, Boolean.TRUE.toString());
        } else {
            XercesUtils.removeAttribute(node, LEOS_AUTO_NUM_OVERWRITE);
        }
        if (SoftActionType.MOVE_TO.equals(tocVo.getSoftActionAttr())) {
            XercesUtils.insertOrUpdateAttributeValue(node, XMLID, tocVo.getId());
            updateXMLIDAttributeFullStructureNode(node, SOFT_MOVE_PLACEHOLDER_ID_PREFIX, true);
        } else if (SoftActionType.DELETE.equals(tocVo.getSoftActionAttr())) {
            XercesUtils.insertOrUpdateAttributeValue(node, XMLID, tocVo.getId());
            updateXMLIDAttributeFullStructureNode(node, SOFT_DELETE_PLACEHOLDER_ID_PREFIX, true);
            softDeleteAuthorialNote(node);
        } else if (SoftActionType.UNDELETE.equals(tocVo.getSoftActionAttr())) {
            softUndeleteAuthorialNote(node);
        } else if (Arrays.asList(PARAGRAPH, LEVEL, POINT, INDENT).contains(tocVo.getTocItem().getAknTag().value()) &&
                !isEmptyElement(tocVo) && isSingleSubElement(tocVo.getChildItems().get(0)) &&
                !isSoftDeletedOrMoved(tocVo.getChildItems().get(0))) {
            XercesUtils.insertOrUpdateAttributeValue(node, LEOS_SOFT_TRANS_FROM, tocVo.getChildItems().get(0).getId());
        }

        updateSoftInfo(node, tocVo.getSoftActionAttr(), tocVo.isSoftActionRoot(), user, tocVo.getOriginAttr(), getMoveId(tocVo),
                tocVo.getTocItem().getAknTag().value(), tocVo);

        XercesUtils.insertOrUpdateAttributeValue(node, LEOS_AFFECTED_ATTR, tocVo.isAffected() ? Boolean.TRUE.toString() : null);
        if (tocVo.getItemDepth() > 0 && NumberingType.LEVEL_NUM.equals(tocVo.getTocItem().getNumberingType())) {
            XercesUtils.insertOrUpdateAttributeValue(node, LEOS_DEPTH_ATTR, String.valueOf(tocVo.getItemDepth()));
        }

        if (tocVo.getTocItem().getAknTag().value().equals(LIST)) {
            updateListTypeAttributeForIndent(node, tocVo);
        } else if (getTagValueFromTocItemVo(tocVo).equalsIgnoreCase(CROSSHEADING)
                || getTagValueFromTocItemVo(tocVo).equalsIgnoreCase(BLOCK)) {
            XercesUtils.insertOrUpdateAttributeValue(node, LEOS_INDENT_LEVEL_ATTR, String.valueOf(tocVo.getIndentLevel()));
            XercesUtils.insertOrUpdateStylingAttribute(node, INDENT_LEVEL_PROPERTY, String.valueOf(tocVo.getIndentLevel()));
            XercesUtils.insertOrUpdateStylingAttribute(node, INLINE_NUM_PROPERTY, StringUtils.isNotEmpty(tocVo.getNumber()) ? "1" : null);
        }
    }

    private boolean isIndent(List<NumberingConfig> numberingConfigs, TableOfContentItemVO tocVo) {
        boolean isNumberedNumberingConfig = tocVo.getTocItem().getNumberingType() == null || StructureConfigUtils.getNumberingConfig(numberingConfigs, tocVo.getTocItem().getNumberingType()).isNumbered();
        return (!isNumberedNumberingConfig && getTagValueFromTocItemVo(tocVo).equalsIgnoreCase(INDENT))
                || (!ArrayUtils.contains(PARAGRAPH_LEVEL_ITEMS, tocVo.getTocItem().getAknTag().value()) &&
                (getPointDepthInToc(tocVo, 1) == StructureConfigUtils.getDepthByNumberingType(numberingConfigs, NumberingType.INDENT)));
    }

    @Override
    public Pair<byte[], Element> getSplittedElement(byte[] xmlContent, String content, String tagName, String idAttributeValue) {
        Element splitElement;
        if (Arrays.asList(SUBPARAGRAPH, SUBPOINT).contains(tagName) || (PARAGRAPH.equals(tagName) && !content.contains("<" + SUBPARAGRAPH + ">"))) {
            splitElement = getSiblingElement(xmlContent, tagName, idAttributeValue, Collections.emptyList(), false);
        } else if (LEVEL.equals(tagName)) {
            return null;
        } else if (CONTENT.equals(tagName)) {
            splitElement = getSiblingOfParentElement(xmlContent, CONTENT, idAttributeValue);
        } else {
            splitElement = getChildElement(xmlContent, tagName, idAttributeValue, Arrays.asList(SUBPARAGRAPH, SUBPOINT), 2);
        }

        return buildSplittedElementPair(xmlContent, splitElement);
    }

    @Override
    public Element getMergeOnElement(byte[] xmlContent, String content, String tagName, String idAttributeValue) {
        Map<String, String> attributes = getElementAttributesByPath(content.getBytes(UTF_8), "/" + tagName, false);
        if (isSoftDeletedOrMovedTo(attributes) || !isPContent(content, tagName)) {
            return null;
        }

        Element mergeOnElement = getSiblingElement(xmlContent, tagName, idAttributeValue, Arrays.asList(tagName, LIST), true);
        if ((mergeOnElement == null) || ((mergeOnElement != null) &&
                (isSoftDeletedOrMovedTo(getElementAttributesByPath(mergeOnElement.getElementFragment().getBytes(UTF_8), "/" + mergeOnElement.getElementTagName(), false)) ||
                        !isPContent(mergeOnElement.getElementFragment(), mergeOnElement.getElementTagName())))) {
            return null;
        }

        if (!isProposalElement(attributes)) {
            Element parentElement = getParentElement(xmlContent, mergeOnElement.getElementId());
            if (Arrays.asList(PARAGRAPH, POINT, INDENT).contains(parentElement.getElementTagName())
                    && getChildElement(xmlContent, parentElement.getElementTagName(), parentElement.getElementId(), Arrays.asList(SUBPARAGRAPH, SUBPOINT, LIST), 3) == null) {
                return parentElement;
            } else if (Arrays.asList(LEVEL).contains(parentElement.getElementTagName())
                    && countChildren(xmlContent, parentElement.getElementId(), Arrays.asList(SUBPARAGRAPH)) == 2) {
                //is the last subparagraph of a Level. Unwrap it and return the <content> tag.
                String contentXml = mergeOnElement.getElementFragment().replaceAll("<subparagraph.*?>", "").replaceAll("</subparagraph>", "");
                String wrappedContentXml = "<root xmlns:leos=\"urn:eu:europa:ec:leos\" xmlns:xml=\"http://www.w3.org/XML/1998/namespace\">" + contentXml + "</root>"; //for correct xml structure

                Document document = createXercesDocument(wrappedContentXml.getBytes(UTF_8));
                Node node = XercesUtils.getFirstElementByName(document, CONTENT);
                String contentId = XercesUtils.getId(node);
                mergeOnElement = new Element(contentId, CONTENT, contentXml);
            }
        }

        return mergeOnElement;
    }

    public int countChildren(byte[] xmlContent, String elementId, List<String> childrenNames) {
        Document document = createXercesDocument(xmlContent);
        Node node = XercesUtils.getElementById(document, elementId);
        return XercesUtils.countChildren(node, childrenNames);
    }

    @Override
    public byte[] mergeElement(byte[] xmlContent, String content, String tagName, String idAttributeValue) {
        Element mergeOnElement = getSiblingElement(xmlContent, tagName, idAttributeValue, Arrays.asList(tagName, LIST), true);
        String contentFragment = getElementContentFragmentByPath(content.getBytes(UTF_8), "/" + tagName + "/content/p", false);
        String contentFragmentMergeOn = getElementContentFragmentByPath(mergeOnElement.getElementFragment().getBytes(UTF_8), "/" + mergeOnElement.getElementTagName() + "/content/p", false);
        final String replace = mergeOnElement.getElementFragment().replace(contentFragmentMergeOn, contentFragmentMergeOn + " " + contentFragment);
        byte[] updatedXmlContent = replaceElementById(xmlContent, replace, mergeOnElement.getElementId());

        Map<String, String> attributes = getElementAttributesByPath(content.getBytes(UTF_8), "/" + tagName, false);
        if (isProposalElement(attributes) && !isSoftMovedFrom(attributes)) {
            updatedXmlContent = replaceElementById(updatedXmlContent, softDeleteElement(content, false), idAttributeValue);
        } else {
            updatedXmlContent = deleteElementById(updatedXmlContent, idAttributeValue);
            if (isSoftMovedFrom(attributes)) {
                Element softMovedToElement = getElementById(updatedXmlContent, getSoftMovedFromAttribute(attributes));
                updatedXmlContent = replaceElementById(updatedXmlContent, softDeleteElement(softMovedToElement.getElementFragment(), false), softMovedToElement.getElementId());
            }
            Element parentElement = getParentElement(updatedXmlContent, mergeOnElement.getElementId());
            if (Arrays.asList(PARAGRAPH, LEVEL, POINT, INDENT).contains(parentElement.getElementTagName()) && getChildElement(updatedXmlContent, parentElement.getElementTagName(), parentElement.getElementId(), Arrays.asList(SUBPARAGRAPH, SUBPOINT, LIST), 2) == null) {
                final String xPath = "/" + parentElement.getElementTagName() + "/" + mergeOnElement.getElementTagName();
                Map<String, String> mergedElementAttributes = getElementAttributesByPath(parentElement.getElementFragment().getBytes(UTF_8), xPath, false);
                if (isSoftMovedFrom(mergedElementAttributes)) {
                    Element softMovedToMergedElement = getElementById(updatedXmlContent, getSoftMovedFromAttribute(mergedElementAttributes));
                    updatedXmlContent = replaceElementById(updatedXmlContent, softDeleteElement(softMovedToMergedElement.getElementFragment(), false), softMovedToMergedElement.getElementId());
                }
                String mergedElementFragment = getElementFragmentByPath(parentElement.getElementFragment().getBytes(UTF_8), xPath, false);
                String mergedContentFragment = getElementFragmentByPath(parentElement.getElementFragment().getBytes(UTF_8), xPath + "/content", false);
                mergedElementFragment = XmlHelper.removeAllNameSpaces(mergedElementFragment);
                mergedContentFragment = XmlHelper.removeAllNameSpaces(mergedContentFragment);
                String parentElementFragment = parentElement.getElementFragment().replace(mergedElementFragment, mergedContentFragment);
                final String newContent = new String(updateSoftTransFromAttribute(parentElementFragment.getBytes(UTF_8), mergeOnElement.getElementId()), UTF_8);
                updatedXmlContent = replaceElementById(updatedXmlContent, newContent, parentElement.getElementId());
            } else if (Arrays.asList(PARAGRAPH, LEVEL, POINT, INDENT).contains(mergeOnElement.getElementTagName())) {
                updatedXmlContent = insertAffectedAttributeIntoParentElements(updatedXmlContent, mergeOnElement.getElementId());
            }
        }

        return updatedXmlContent;
    }

    private String getSoftMovedFromAttribute(Map<String, String> attributes) {
        return attributes.get(LEOS_SOFT_MOVE_FROM);
    }

    protected boolean isSoftDeletedOrMovedTo(Map<String, String> attributes) {
        return ((attributes.get(LEOS_SOFT_ACTION_ATTR) != null) && (attributes.get(LEOS_SOFT_ACTION_ATTR).equals(SoftActionType.DELETE.getSoftAction()) ||
                attributes.get(LEOS_SOFT_ACTION_ATTR).equals(SoftActionType.MOVE_TO.getSoftAction())));
    }

    protected boolean isSoftTransformed(final Map<String, String> attributes) {
        return ((attributes.get(LEOS_SOFT_ACTION_ATTR) != null) && attributes.get(LEOS_SOFT_ACTION_ATTR).equals(SoftActionType.TRANSFORM.getSoftAction()));
    }

    private boolean isPContent(String content, String tagName) {
        return getElementContentFragmentByPath(content.getBytes(UTF_8), "/" + tagName + "/content/p", false) != null;
    }

    @Override
    public boolean needsToBeIndented(String elementContent) {
        return (getAttributeValueAsBoolean(elementContent, LEOS_INDENT_NUMBERED_ATTR) != null);
    }

    @Override
    public byte[] indentElement(byte[] xmlContent, String elementName, String elementId, String elementContent, List<TableOfContentItemVO> toc) throws IllegalArgumentException {
        return this.indentElement(xmlContent, elementName, elementId, elementContent, toc, null, null, null);
    }

    private byte[] indentElement(byte[] xmlContent, String elementName, String elementId, String elementContent, List<TableOfContentItemVO> toc,
                                 Integer targetLevel, Integer originalIndentLevel, Boolean isNumbered) throws IllegalArgumentException {
        if (isNumbered == null && !needsToBeIndented(elementContent)) {
            return xmlContent;
        }
        targetLevel = targetLevel != null ? targetLevel : getAttributeValueAsInteger(elementContent, LEOS_INDENT_LEVEL_ATTR);
        isNumbered = isNumbered != null ? isNumbered : getAttributeValueAsBoolean(elementContent, LEOS_INDENT_NUMBERED_ATTR);

        List<TocItem> tocItems = structureContextProvider.get().getTocItems();
        List<NumberingConfig> numberingConfigs = structureContextProvider.get().getNumberingConfigs();

        replaceElementById(xmlContent, elementContent, elementId);

        Optional<TableOfContentItemVO> hasIndentedItem = getItemFromTocById(elementId, toc);
        TableOfContentItemVO indentedItem = hasIndentedItem.orElseThrow(() -> new IllegalArgumentException("Indentation not allowed"));
        originalIndentLevel = originalIndentLevel != null? originalIndentLevel : indentConversionHelper.getIndentedItemIndentLevel(indentedItem);
        boolean isOutdent = false;
        if (targetLevel.intValue() != originalIndentLevel.intValue()) {
            isOutdent = (targetLevel - originalIndentLevel) < 0;
        }

        //Update content of node
        Document document = createXercesDocument(xmlContent);
        Node node = XercesUtils.getElementById(document, elementId);
        if (node != null) {
            elementContent = removeAttribute(new StringBuilder(elementContent), LEOS_INDENT_LEVEL_ATTR).toString();
            elementContent = removeAttribute(new StringBuilder(elementContent), LEOS_INDENT_NUMBERED_ATTR).toString();
            document = (Document) XercesUtils.replaceElement(node, elementContent);
            Node newNode = XercesUtils.getElementById(document, elementId);
            indentedItem.setNode(newNode);
        }

        indentedItem = indentHelper.doIndentForTargetIndentLevel(targetLevel, isNumbered, indentedItem, tocItems, numberingConfigs);

        xmlContent = createDocumentContentWithNewTocList(toc, xmlContent, (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal());

        xmlContent = insertAffectedAttributeIntoParentElements(xmlContent, indentedItem.getId());

        if (isOutdent) {
            xmlContent = correctIllegalParagraphStructure(xmlContent, elementId, toc, targetLevel, originalIndentLevel);
        }
        return xmlContent;
    }

    /**
     * Further outdents subparagraphs if a paragraph with multi-subparagraphs structure is detected associated to the
     * changed element identifier (LEOS-5980)
     * @param xmlContent byte array with the xml content for the document
     * @param elementId element identifier that was indented/outdented
     * @return updated xml content for the document
     */
    private byte[] correctIllegalParagraphStructure(byte[] xmlContent, String elementId, List<TableOfContentItemVO> toc,
                                                    Integer targetLevel, Integer originalIndentLevel){
        Element parent = this.getParentElement(xmlContent, elementId);
        Element element = this.getElementById(xmlContent, elementId);
        Element sibling = this.getSiblingElement(xmlContent, null, elementId, Collections.emptyList(), false);
        if(parent != null && PARAGRAPH.equals(parent.getElementTagName()) && SUBPARAGRAPH.equals(element.getElementTagName()) && sibling != null
                && SUBPARAGRAPH.equals(sibling.getElementTagName())) {
            // Structure needs to be corrected - outdent sibling of the changed element
            xmlContent = this.indentElement(xmlContent,sibling.getElementTagName(), sibling.getElementId(),
                    sibling.getElementFragment(), toc, targetLevel, originalIndentLevel, true);
        }
        return xmlContent;
    }

    @Override
    public byte[] removeElementById(byte[] xmlContent, String elementId) {
        Element element = getElementById(xmlContent, elementId);
        if (element == null) {
            return xmlContent;
        }
        // Insert affected attribute for numbering purpose
        if (ELEMENTS_TO_BE_PROCESSED_FOR_NUMBERING.contains(element.getElementTagName())) {
            xmlContent = insertAffectedAttributeIntoParentElements(xmlContent, elementId);
        }
        return removeElement(xmlContent, element, CN);
    }

    @Override
    public void specificInstanceXMLPostProcessing(Node node) {
        if (!isExplanatoryDoc(node)) {
            updateSoftMoveLabelAttribute(node, LEOS_SOFT_MOVE_TO);
            updateSoftMoveLabelAttribute(node, LEOS_SOFT_MOVE_FROM);
            updateNewElements(node, CITATION, null, CN);
            updateNewElements(node, RECITAL, null, CN);
            updateNewElements(node, ARTICLE, null, CN);
            updateNewElements(node, PARAGRAPH, SUBPARAGRAPH, CN);
            updateNewElements(node, POINT, SUBPOINT, CN);
            updateNewElements(node, INDENT, SUBPOINT, CN);
            updateNewElements(node, PREFACE, null, CN);
            updateNewElements(node, MAIN_BODY, null, CN);
            updateNewElements(node, LEVEL, SUBPARAGRAPH, CN);
        }
    }


    private boolean isExplanatoryDoc(Node node) {
        String xPath = "//akn:doc[@name='EXPL_COUNCIL']";
        Node found = XercesUtils.getFirstElementByXPath(node, xPath);
        return found != null;
    }

    @Override
    public byte[] insertAffectedAttributeIntoParentElements(byte[] xmlContent, String elementId) {
        Node document = createXercesDocument(xmlContent);
        Node node = XercesUtils.getElementById(document, elementId);
        node = node.getParentNode();
        while (node != null && !Arrays.asList(BODY, MAIN_BODY).contains(node.getNodeName())) {
            if (ELEMENTS_TO_BE_PROCESSED_FOR_NUMBERING.contains(node.getNodeName())) {
                XercesUtils.addAttribute(node, LEOS_AFFECTED_ATTR, "true");
            }
            node = node.getParentNode();
        }
        return XercesUtils.nodeToByteArray(document);
    }

    @Override
    public byte[] prepareForRenumber(byte[] xmlContent) {
        Node document = createXercesDocument(xmlContent);
        updateAttributesForRenumbering(document, "/akn:akomaNtoso//akn:recital");
        updateAttributesForRenumbering(document, "/akn:akomaNtoso//akn:article");
        updateAttributesForRenumbering(document, "/akn:akomaNtoso//akn:paragraph");
        updateAttributesForRenumbering(document, "/akn:akomaNtoso//akn:level");
        updateAttributesForRenumbering(document, "/akn:akomaNtoso//akn:point");
        return XercesUtils.nodeToByteArray(document);
    }

    public void updateAttributesForRenumbering(Node document, String xPath) {
        NodeList nodeList = XercesUtils.getElementsByXPath(document, xPath);
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node childNode = nodeList.item(i);
            if (childNode.getNodeType() == Node.ELEMENT_NODE) {
                XercesUtils.insertOrUpdateAttributeValue(childNode, LEOS_RENUMBERED, "true");
            }
        }
    }


    @Override
    public byte[] updateDepthAttribute(byte[] xmlContent) {
        throw new IllegalStateException("Feature not implemented for the running instance");
    }

    @Override
    protected Pair<byte[], Element> buildSplittedElementPair(byte[] xmlContent, Element splitElement) {
        if (splitElement != null) {
            Element siblingElement = getSiblingElement(xmlContent, splitElement.getElementTagName(), splitElement.getElementId(), Collections.emptyList(), false);
            if (siblingElement != null && isSiblingPresentWithSameContent(splitElement, siblingElement) && isElementSoftDelete(siblingElement)) {
                xmlContent = removeElementById(xmlContent, splitElement.getElementId());
                Pair<byte[], Element> result = recoverDeletedSibling(xmlContent, siblingElement.getElementId());
                xmlContent = result.left();
                splitElement = result.right();
            } else if (siblingElement != null && isSiblingPresentWithNoContent(siblingElement)) {
                xmlContent = removeElementById(xmlContent, siblingElement.getElementId());
            }
        } else {
            return null;
        }
        return new Pair<>(xmlContent, splitElement);
    }

    private Pair<byte[], Element> recoverDeletedSibling(byte[] xmlContent, String elementId) {
        Node document = XercesUtils.createXercesDocument(xmlContent);
        Node node = XercesUtils.getElementById(document, elementId);
        Pair<Node, String> undeleteResult = undeletedElement(node, elementId);
        xmlContent = XercesUtils.nodeToByteArray(undeleteResult.left());
        String restoredId = undeleteResult.right();
        return new Pair<>(xmlContent, getElementFromXmlContent(restoredId, xmlContent));
    }

    private Element getElementFromXmlContent(String elementId, byte[] xmlContent) {
        Node docNode = XercesUtils.createXercesDocument(xmlContent);
        Node node = XercesUtils.getElementById(docNode, elementId);
        Element element = null;
        if (node != null) {
            element = new Element(elementId, node.getNodeName(), XercesUtils.nodeToString(node));
        }
        return element;
    }

    private Pair<Node, String> undeletedElement(Node node, String elementId) {
        String restoredId = null;
        Node child = XercesUtils.getElementById(node, elementId);
        if (child != null) {
            restoredId = removeSoftAttibutes(child);
            undeletedElement(child, elementId);
        }

        return new Pair<>(child, restoredId);
    }


    private String removeSoftAttibutes(Node node) {
        String elementId = XercesUtils.getAttributeValue(node, XMLID);
        if (elementId != null) {
            String restoredId = elementId.substring(elementId.indexOf(SOFT_DELETE_PLACEHOLDER_ID_PREFIX) + SOFT_DELETE_PLACEHOLDER_ID_PREFIX.length());
            XercesUtils.addAttribute(node, XMLID, restoredId);
        }
        XercesUtils.removeAttribute(node, LEOS_SOFT_ACTION_ATTR);
        XercesUtils.removeAttribute(node, LEOS_SOFT_ACTION_ROOT_ATTR);
        XercesUtils.removeAttribute(node, LEOS_SOFT_USER_ATTR);
        XercesUtils.removeAttribute(node, LEOS_SOFT_DATE_ATTR);
        XercesUtils.removeAttribute(node, LEOS_DELETABLE_ATTR);
        XercesUtils.removeAttribute(node, LEOS_EDITABLE_ATTR);
        return elementId;
    }

    private boolean isSiblingPresentWithSameContent(Element splitElement, Element siblingElement) {
        String splitElementContent = getSubElementsContent(splitElement);
        return splitElementContent != null && splitElementContent.equals(getSubElementsContent(siblingElement));
    }

    private boolean isSiblingPresentWithNoContent(Element siblingElement) {
        String siblingElementContent = getSubElementsContent(siblingElement);
        return siblingElementContent != null && Pattern.compile("^[\\s\\xA0]*$").matcher(siblingElementContent).find();
    }

    private String getSubElementsContent(Element element) {
        Validate.notNull(element, "Operation could not be performed, split element is empty");
        String contentTagSubStr = null;
        if (element.getElementFragment().indexOf("</p>") >= 0) {
            contentTagSubStr = element.getElementFragment().substring(0, element.getElementFragment().indexOf("</p>"));
        }
        if (contentTagSubStr != null && !contentTagSubStr.isEmpty()) {
            return contentTagSubStr.substring(contentTagSubStr.lastIndexOf(">") + 1);
        }
        return contentTagSubStr;
    }

    private boolean isElementSoftDelete(Element element) {
        return element != null
                && element.getElementId().startsWith(SOFT_DELETE_PLACEHOLDER_ID_PREFIX)
                && element.getElementFragment().contains("leos:softaction=\"del\"");
    }

    @Override
    public byte[] insertAutoNumOverwriteAttributeIntoParentElements(byte[] xmlContent, String idAttributeValue) {
        Document document = createXercesDocument(xmlContent);
        Node node = XercesUtils.getElementById(document, idAttributeValue);
        if(node != null) {
            addAttribute(node, LEOS_AUTO_NUM_OVERWRITE, Boolean.TRUE.toString());
        }
        return XercesUtils.nodeToByteArray(document);
    }

    /**
     * Update attributes for CN if empty origin
     * Add softaction splitted and placeholder in id
     * @param node Node to update
     * @param isEmptyOrigin is Origin empty
     * @return updated node
     */
    @Override
    public void updateIfEmptyOrigin(Node node, boolean isEmptyOrigin) {
        if (isEmptyOrigin) {
            XercesUtils.addAttribute(node, LEOS_SOFT_ACTION_ATTR, SoftActionType.SPLITTED.getSoftAction());
            updateXMLIDAttributeFullStructureNode(node, SOFT_SPLITTED_PLACEHOLDER_ID_PREFIX, false);
        }
    }

    /**
     * Check for cn elements if exists subelement deleted and splitted
     * If yes , merge on element
     * @param element Element to check
     * @return updated element with merge elements
     */
    @Override
    public void updateElementSplit(Node element) {

        boolean isSplit = false;
        List<Node> elementChilds = XercesUtils.getChildren(element, ELEMENTS_TO_BE_PROCESSED_FOR_NUMBERING);
        String textContent = "";
        if(elementChilds != null && elementChilds.size() > 0) {

            Node firstSubElement = elementChilds.get(0);
            Node firstContent = XercesUtils.getFirstChild(firstSubElement, CONTENT);
            textContent = firstContent.getTextContent();
            //Fetch subparagraphs
            for(Node child:elementChilds) {
                String idChild = XercesUtils.getAttributeValue(child, XMLID);
                String originChild = XercesUtils.getAttributeValue(child, LEOS_ORIGIN_ATTR);

                //If CN and deleted_splitted elements, keep the content and delete childs
                if(originChild != null && idChild != null && originChild.equals(CN) && idChild.startsWith(SOFT_DELETE_PLACEHOLDER_ID_PREFIX + SOFT_SPLITTED_PLACEHOLDER_ID_PREFIX)) {
                    Node content = XercesUtils.getFirstChild(child, CONTENT);
                    textContent += " " + content.getTextContent();
                    element.removeChild(child);
                    isSplit = true;
                }
            }
            //Merge on element
            if(isSplit) {
                Node child = elementChilds.get(0);
                Node content = XercesUtils.getFirstChild(child, CONTENT);
                Node p = XercesUtils.getFirstChild(content, P);
                p.setTextContent(textContent);
                XercesUtils.replaceElement(content, child);
            }
        }
    }

}