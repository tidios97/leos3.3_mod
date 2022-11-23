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

import eu.europa.ec.leos.domain.common.TocMode;
import eu.europa.ec.leos.model.action.SoftActionType;
import eu.europa.ec.leos.model.user.User;
import eu.europa.ec.leos.services.support.XmlHelper;
import eu.europa.ec.leos.services.support.XercesUtils;
import eu.europa.ec.leos.vo.toc.Attribute;
import eu.europa.ec.leos.vo.toc.NumberingConfig;
import eu.europa.ec.leos.vo.toc.NumberingType;
import eu.europa.ec.leos.vo.toc.OptionsType;
import eu.europa.ec.leos.vo.toc.TableOfContentItemVO;
import eu.europa.ec.leos.vo.toc.TocItem;
import eu.europa.ec.leos.vo.toc.StructureConfigUtils;
import eu.europa.ec.leos.vo.toc.TocItemType;
import eu.europa.ec.leos.vo.toc.TocItemTypeName;
import eu.europa.ec.leos.vo.toc.indent.IndentedItemType;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;

import static eu.europa.ec.leos.services.support.XmlHelper.BLOCK;
import static eu.europa.ec.leos.services.support.XmlHelper.CLASS_ATTR;
import static eu.europa.ec.leos.services.support.XmlHelper.CN;
import static eu.europa.ec.leos.services.support.XmlHelper.CROSSHEADING;
import static eu.europa.ec.leos.services.support.XmlHelper.EC;
import static eu.europa.ec.leos.services.support.XmlHelper.ELEMENTS_TO_HIDE_CONTENT;
import static eu.europa.ec.leos.services.support.XmlHelper.ELEMENTS_WITH_TEXT;
import static eu.europa.ec.leos.services.support.XmlHelper.EMPTY_STRING;
import static eu.europa.ec.leos.services.support.XmlHelper.HEADING;
import static eu.europa.ec.leos.services.support.XmlHelper.INDENT;
import static eu.europa.ec.leos.services.support.XmlHelper.INLINE;
import static eu.europa.ec.leos.services.support.XmlHelper.INLINE_NUM;
import static eu.europa.ec.leos.services.support.XmlHelper.INTRO;
import static eu.europa.ec.leos.services.support.XmlHelper.LEOS_AUTO_NUM_OVERWRITE;
import static eu.europa.ec.leos.services.support.XmlHelper.LEOS_CROSSHEADING_TYPE;
import static eu.europa.ec.leos.services.support.XmlHelper.LEOS_DELETABLE_ATTR;
import static eu.europa.ec.leos.services.support.XmlHelper.LEOS_DEPTH_ATTR;
import static eu.europa.ec.leos.services.support.XmlHelper.LEOS_EDITABLE_ATTR;
import static eu.europa.ec.leos.services.support.XmlHelper.LEOS_INDENT_ORIGIN_INDENT_LEVEL_ATTR;
import static eu.europa.ec.leos.services.support.XmlHelper.LEOS_INDENT_ORIGIN_NUM_ATTR;
import static eu.europa.ec.leos.services.support.XmlHelper.LEOS_INDENT_ORIGIN_NUM_ID_ATTR;
import static eu.europa.ec.leos.services.support.XmlHelper.LEOS_INDENT_ORIGIN_NUM_ORIGIN_ATTR;
import static eu.europa.ec.leos.services.support.XmlHelper.LEOS_INDENT_ORIGIN_TYPE_ATTR;
import static eu.europa.ec.leos.services.support.XmlHelper.LEOS_ORIGIN_ATTR;
import static eu.europa.ec.leos.services.support.XmlHelper.LEOS_SOFT_ACTION_ATTR;
import static eu.europa.ec.leos.services.support.XmlHelper.LEOS_SOFT_ACTION_ROOT_ATTR;
import static eu.europa.ec.leos.services.support.XmlHelper.LEOS_SOFT_DATE_ATTR;
import static eu.europa.ec.leos.services.support.XmlHelper.LEOS_SOFT_MOVED_LABEL_ATTR;
import static eu.europa.ec.leos.services.support.XmlHelper.LEOS_SOFT_MOVE_FROM;
import static eu.europa.ec.leos.services.support.XmlHelper.LEOS_SOFT_MOVE_TO;
import static eu.europa.ec.leos.services.support.XmlHelper.LEOS_SOFT_TRANS_FROM;
import static eu.europa.ec.leos.services.support.XmlHelper.LEOS_SOFT_USER_ATTR;
import static eu.europa.ec.leos.services.support.XmlHelper.LEVEL;
import static eu.europa.ec.leos.services.support.XmlHelper.LIST;
import static eu.europa.ec.leos.services.support.XmlHelper.NUM;
import static eu.europa.ec.leos.services.support.XmlHelper.PARAGRAPH;
import static eu.europa.ec.leos.services.support.XmlHelper.POINT;
import static eu.europa.ec.leos.services.support.XmlHelper.SOFT_DELETE_PLACEHOLDER_ID_PREFIX;
import static eu.europa.ec.leos.services.support.XmlHelper.SUBPARAGRAPH;
import static eu.europa.ec.leos.services.support.XmlHelper.XMLID;
import static eu.europa.ec.leos.services.support.XmlHelper.XML_NAME;
import static eu.europa.ec.leos.services.support.XmlHelper.extractNumber;
import static eu.europa.ec.leos.services.support.XmlHelper.getDateAsXml;
import static eu.europa.ec.leos.services.support.XmlHelper.getSoftUserAttribute;
import static eu.europa.ec.leos.services.support.XmlHelper.trimmedXml;
import static eu.europa.ec.leos.services.processor.content.TableOfContentProcessorImpl.getTocItemFromNumberingType;
import static eu.europa.ec.leos.services.support.XercesUtils.addAttribute;
import static eu.europa.ec.leos.services.support.XercesUtils.createElement;
import static eu.europa.ec.leos.services.support.XercesUtils.getAttributeForSoftAction;
import static eu.europa.ec.leos.services.support.XercesUtils.getAttributeForType;
import static eu.europa.ec.leos.services.support.XercesUtils.getAttributeValue;
import static eu.europa.ec.leos.services.support.XercesUtils.getAttributeValueAsBoolean;
import static eu.europa.ec.leos.services.support.XercesUtils.getAttributeValueAsGregorianCalendar;
import static eu.europa.ec.leos.services.support.XercesUtils.getAttributeValueAsInteger;
import static eu.europa.ec.leos.services.support.XercesUtils.getAttributeValueAsIntegerOrZero;
import static eu.europa.ec.leos.services.support.XercesUtils.getFirstChild;
import static eu.europa.ec.leos.services.support.XercesUtils.getNumTag;
import static eu.europa.ec.leos.services.support.XercesUtils.getParentTagName;
import static eu.europa.ec.leos.services.support.XercesUtils.insertOrUpdateAttributeValue;
import static eu.europa.ec.leos.services.support.XercesUtils.removeAttribute;
import static eu.europa.ec.leos.services.processor.content.XmlContentProcessorImpl.NBSP;
import static eu.europa.ec.leos.services.support.XmlHelper.LEOS_INITIAL_NUM;
import static eu.europa.ec.leos.vo.toc.StructureConfigUtils.HASH_NUM_VALUE;
import static eu.europa.ec.leos.vo.toc.StructureConfigUtils.getAttributeByTagNameAndTocItemType;
import static eu.europa.ec.leos.vo.toc.StructureConfigUtils.getTocItemTypesByTagName;

public class XmlContentProcessorHelper {

    private static final Logger LOG = LoggerFactory.getLogger(XmlContentProcessorHelper.class);

    public static List<TableOfContentItemVO> getAllChildTableOfContentItems(Node node, List<TocItem> tocItems, Map<TocItem, List<TocItem>> tocRules, List<NumberingConfig> numberingConfigs, TocMode mode) {
        List<TableOfContentItemVO> itemVOList = new ArrayList<>();
        Node child;
        NodeList nodeList = node.getChildNodes();
        for (int i = 0; i < nodeList.getLength(); i++) {
            child = nodeList.item(i);
            if (child.getNodeType() == Node.ELEMENT_NODE) {
                addTocItemVoToList(tocItems, tocRules, numberingConfigs, child, itemVOList, mode);
            }
        }
        return itemVOList;
    }

    private static void addTocItemVoToList(List<TocItem> tocItems, Map<TocItem, List<TocItem>> tocRules, List<NumberingConfig> numberingConfigs, Node node, List<TableOfContentItemVO> itemVOList, TocMode mode) {
        TableOfContentItemVO tableOfContentItemVO = buildTableOfContentsItemVO(numberingConfigs, tocItems, node);
        if (tableOfContentItemVO != null) {
            List<TableOfContentItemVO> itemVOChildrenList = getAllChildTableOfContentItems(node, tocItems, tocRules, numberingConfigs, mode);
            if ((!TocMode.SIMPLIFIED_CLEAN.equals(mode) || (TocMode.SIMPLIFIED_CLEAN.equals(mode) && tableOfContentItemVO.getTocItem().isDisplay()))
                    && shouldItemBeAddedToToc(tocItems, tocRules, node, tableOfContentItemVO.getTocItem())) {
                if (TocMode.SIMPLIFIED.equals(mode) || TocMode.SIMPLIFIED_CLEAN.equals(mode)) {
                    if (getTagValueFromTocItemVo(tableOfContentItemVO).equals(LIST) && !itemVOList.isEmpty()) {
                        tableOfContentItemVO = itemVOList.get(itemVOList.size() - 1);
                        tableOfContentItemVO.addAllChildItems(itemVOChildrenList);
                        return;
                    } else if (Arrays.asList(PARAGRAPH, POINT, INDENT, LEVEL).contains(getTagValueFromTocItemVo(tableOfContentItemVO))) {
                        boolean isFirstCrossHeading  = !itemVOChildrenList.isEmpty() && CROSSHEADING.equalsIgnoreCase(getTagValueFromTocItemVo(itemVOChildrenList.get(0)));
                        if ((itemVOChildrenList.size() > 1) && (itemVOChildrenList.get(0).getChildItems().isEmpty())) {
                            tableOfContentItemVO.setId(itemVOChildrenList.get(0).getId());
                            if(!isFirstCrossHeading) itemVOChildrenList.remove(0);
                        } else if (itemVOChildrenList.size() == 1) {
                            tableOfContentItemVO.setId(itemVOChildrenList.get(0).getId());
                            if(!isFirstCrossHeading) itemVOChildrenList = itemVOChildrenList.get(0).getChildItems();
                        }
                    }
                }
                itemVOList.add(tableOfContentItemVO);
                tableOfContentItemVO.addAllChildItems(itemVOChildrenList);
            } else if (tableOfContentItemVO.getParentItem() != null) {
                tableOfContentItemVO.getParentItem().addAllChildItems(itemVOChildrenList);
            } else {
                itemVOChildrenList.forEach(childItem -> itemVOList.add(childItem));
            }
        }
    }

    public static TableOfContentItemVO buildTableOfContentsItemVO(List<NumberingConfig> numberingConfigs, List<TocItem> tocItems, Node node) {
        String tagName = node.getNodeName();
        TocItem tocItem = StructureConfigUtils.getTocItemByName(tocItems, tagName);

        if (tocItem == null) {
            // unsupported tag name
            return null;
        }

        String elementId = getAttributeValue(node, XMLID);
        String originAttr = getAttributeValue(node, LEOS_ORIGIN_ATTR);
        SoftActionType softActionAttr = getAttributeForSoftAction(node, LEOS_SOFT_ACTION_ATTR);
        Boolean isSoftActionRoot = getAttributeValueAsBoolean(node, LEOS_SOFT_ACTION_ROOT_ATTR);
        String softUserAttr = getAttributeValue(node, LEOS_SOFT_USER_ATTR);
        GregorianCalendar softDateAttr = getAttributeValueAsGregorianCalendar(node, LEOS_SOFT_DATE_ATTR);
        String softMovedFrom = getAttributeValue(node, LEOS_SOFT_MOVE_FROM);
        String softMovedTo = getAttributeValue(node, LEOS_SOFT_MOVE_TO);
        String softTransFrom = getAttributeValue(node, LEOS_SOFT_TRANS_FROM);

        // get the indent attributes
        IndentedItemType indentOriginType = getAttributeForType(node, LEOS_INDENT_ORIGIN_TYPE_ATTR, IndentedItemType.class);
        Integer indentOriginDepth = getAttributeValueAsInteger(node, LEOS_INDENT_ORIGIN_INDENT_LEVEL_ATTR);
        String indentOriginNumValue = getAttributeValue(node, LEOS_INDENT_ORIGIN_NUM_ATTR);
        String indentOriginNumId = getAttributeValue(node, LEOS_INDENT_ORIGIN_NUM_ID_ATTR);
        String indentOriginNumOrigin = getAttributeValue(node, LEOS_INDENT_ORIGIN_NUM_ORIGIN_ATTR);

        // get the num
        String number = null;
        String originNumAttr = null;
        String numId = null;
        SoftActionType numSoftActionAttribute = null;
        Node numNode = getFirstChild(node, NUM);
        if (numNode != null) {
            originNumAttr = getAttributeValue(numNode, LEOS_ORIGIN_ATTR);
            numId = getAttributeValue(numNode, XMLID);
            numSoftActionAttribute = getAttributeForSoftAction(numNode, LEOS_SOFT_ACTION_ATTR);
            number = extractNumber(numNode.getTextContent(), tocItem.isNumWithType());
            if (indentOriginType != null && indentOriginNumValue == null
                    && !indentOriginType.equals(IndentedItemType.OTHER_SUBPARAGRAPH)
                    && !indentOriginType.equals(IndentedItemType.OTHER_SUBPOINT)
                    && !wasMaybeAnUnumberedParagraph(node, indentOriginType)) {
                indentOriginNumValue = number;
                indentOriginNumId = numId;
            }
            TocItem foundTocItem = getTocItemFromNumberingType(number, tagName, tocItem, numberingConfigs, tocItems, node);
            tocItem = foundTocItem != null ? foundTocItem : tocItem;
        }
        String initialNumber = getAttributeValue(node, LEOS_INITIAL_NUM);

        boolean isCrossheadingInList = false;
        Node inlineNode = getFirstChild(node, INLINE);
        if ((tagName.equalsIgnoreCase(CROSSHEADING) || tagName.equalsIgnoreCase(BLOCK)) && inlineNode != null) {
            String name = getAttributeValue(inlineNode, XML_NAME);
            if (name != null && name.equalsIgnoreCase(INLINE_NUM)) {
                originNumAttr = getAttributeValue(inlineNode, LEOS_ORIGIN_ATTR);
                numId = getAttributeValue(inlineNode, XMLID);
                numSoftActionAttribute = getAttributeForSoftAction(numNode, LEOS_SOFT_ACTION_ATTR);
                number = extractNumber(inlineNode.getTextContent(), tocItem.isNumWithType());
                if (number != null && !number.isEmpty()) {
                    NumberingType numberingType = StructureConfigUtils.getNumberingTypeBySequence(numberingConfigs, number);
                    tocItem = StructureConfigUtils.getTocItemByNumberingType(tocItems, numberingType, tocItem.getAknTag().name());
                }
            }
            String isInList = XercesUtils.getAttributeValue(node, LEOS_CROSSHEADING_TYPE);
            if (isInList != null && isInList.equalsIgnoreCase(LIST)) {
                isCrossheadingInList = true;
            }
        }

        int indentLevel = 0;
        String indentLevelStr = getAttributeValue(node, XmlHelper.LEOS_INDENT_LEVEL_ATTR);
        if (indentLevelStr != null) {
            indentLevel = Integer.parseInt(indentLevelStr);
        }

        // get the heading
        String heading = null;
        String originHeadingAttr = null;
        SoftActionType headingSoftActionAttribute = null;
        Node headingNode = getFirstChild(node, HEADING);
        if (headingNode != null) {
            heading = trimmedXml(headingNode.getTextContent());
            originHeadingAttr = getAttributeValue(headingNode, LEOS_ORIGIN_ATTR);
            headingSoftActionAttribute = getAttributeForSoftAction(headingNode, LEOS_SOFT_ACTION_ATTR);
        }

        String list = null;
        Node listNode = getFirstChild(node, LIST);
        if (listNode != null) {
            list = trimmedXml(listNode.getTextContent());
        }

        int elementDepth = getAttributeValueAsIntegerOrZero(node, LEOS_DEPTH_ATTR);

        //get the content
        String content = extractContentForTocItemsExceptNumAndHeadingAndIntro(node, tagName);
        content = trimmedXml(content);
//        content = removeNS(content);

        //get style (originally added for division number styles)
        String style =  getAttributeValue(node, CLASS_ATTR);

        //get attribute set to determine if elements auto numbering should be overwritten
        Boolean isAutoNumOverwrite = Boolean.parseBoolean(getAttributeValue(node, LEOS_AUTO_NUM_OVERWRITE));

        //get attribute set to determine if article is a definition article
        TocItemTypeName tocItemType = StructureConfigUtils.getTocItemTypeFromTagNameAndAttributes(tocItems, tagName, XercesUtils.getAttributes(node));

        // build the table of content item and return it
        TableOfContentItemVO item =  new TableOfContentItemVO(tocItem, elementId, originAttr, number, originNumAttr, heading, originHeadingAttr, node, list, content,
                softActionAttr, isSoftActionRoot, softUserAttr, softDateAttr, softMovedFrom, softMovedTo, softTransFrom, false,
                numSoftActionAttribute, headingSoftActionAttribute, elementDepth,
                indentLevel, numId, indentOriginType, indentOriginDepth, indentOriginNumId, indentOriginNumValue, indentOriginNumOrigin,
                style, isAutoNumOverwrite);
        item.setCrossHeadingInList(isCrossheadingInList);
        item.setInitialNum(initialNumber);
        item.setTocItemType(tocItemType);
        return item;
    }

    private static boolean wasMaybeAnUnumberedParagraph(Node item, IndentedItemType indentOriginType) {
        return indentOriginType != null
                && (indentOriginType.equals(IndentedItemType.FIRST_SUBPARAGRAPH) || indentOriginType.equals(IndentedItemType.PARAGRAPH))
                && (item.getNodeName().equalsIgnoreCase(POINT) || item.getNodeName().equalsIgnoreCase(INDENT));
    }

    private static boolean shouldItemBeAddedToToc(List<TocItem> tocItems, Map<TocItem, List<TocItem>> tocRules, Node node, TocItem tocItem) {
        boolean addItemToToc = false;
        if (tocItem.isRoot()) {
            addItemToToc = tocItem.isDisplay();
        } else {
            TocItem parentTocItem = StructureConfigUtils.getTocItemByName(tocItems, getParentTagName(node));
            if ((parentTocItem != null) && (tocRules.get(parentTocItem) != null)) {
                addItemToToc = tocRules.get(parentTocItem).contains(tocItem);
            }
        }
        return addItemToToc;
    }

    public static String getTagValueFromTocItemVo(TableOfContentItemVO tableOfContentItemVO) {
        return tableOfContentItemVO.getTocItem().getAknTag().value();
    }

    private static String extractContentForTocItemsExceptNumAndHeadingAndIntro(Node node, String elementName) {
        if (!ELEMENTS_TO_HIDE_CONTENT.contains(elementName)) {
        	if(PARAGRAPH.equals(elementName)) {
        		Node current = XercesUtils.getFirstChild(node, SUBPARAGRAPH);
        		if(current != null) {
        			return current.getTextContent();
        		}
        	}
            Node current = XercesUtils.getFirstChild(node, HEADING);
            if (current == null) {
                current = XercesUtils.getFirstChild(node, getNumTag(node.getNodeName()));
            }
            if (current == null) {
                current = XercesUtils.getFirstChild(node, INTRO);
            }
            if (current != null) {
                if (ELEMENTS_WITH_TEXT.contains(elementName.toLowerCase())) {
                    StringBuilder nodeContent = new StringBuilder();
                    current = current.getNextSibling();
                    while (current != null) {
                        nodeContent.append(current.getTextContent());
                        current = current.getNextSibling();
                    }
                    return nodeContent.toString();
                } else {
                    current = XercesUtils.getNextSibling(current);
                }
                if (current == null) {
                    return StringUtils.EMPTY;
                }
                return current.getTextContent();
            } else {
                return node.getTextContent();
            }
        } else {
            return EMPTY_STRING;
        }
    }

    public static Node extractOrBuildNumElement(Node node, TableOfContentItemVO tocVo) {
        Node numNode = null;
        Boolean toggleFlag = checkIfParagraphNumberingIsToggled(tocVo);

        if (toggleFlag != null && toggleFlag
                && StringUtils.isEmpty(tocVo.getNumber())) {
            if (CN.equals(tocVo.getOriginAttr())
                    || (EC.equals(tocVo.getOriginAttr())
                    && (SoftActionType.MOVE_FROM.equals(tocVo.getSoftActionAttr()) || (tocVo.isIndented()) && tocVo.getIndentOriginIndentLevel() > 0))) {
                tocVo.setNumber(HASH_NUM_VALUE);
                tocVo.setOriginNumAttr(CN);
            } else {
                List<TableOfContentItemVO> proposalParaVO = new ArrayList<>();
                for (TableOfContentItemVO itemVO : tocVo.getParentItem().getChildItems()) {
                    if (EC.equals(itemVO.getOriginAttr())) {
                        proposalParaVO.add(itemVO);
                    }
                }
                tocVo.setNumber((proposalParaVO.indexOf(tocVo) + 1) + ".");
                tocVo.setOriginNumAttr(EC);
                tocVo.setNumSoftActionAttr(SoftActionType.ADD);
            }
        } else if ((toggleFlag != null && !toggleFlag)
                && (tocVo.getNumSoftActionAttr() != null && SoftActionType.ADD.equals(tocVo.getNumSoftActionAttr())
                || CN.equals(tocVo.getOriginNumAttr() != null ? tocVo.getOriginNumAttr() : tocVo.getOriginAttr()))) {
            tocVo.setNumber(null);
        }
        if (StringUtils.isNotEmpty(tocVo.getNumber())) {
            String newNum = createNumContent(tocVo);
            numNode = XercesUtils.getFirstChild(node, XercesUtils.getNumTag(getTagValueFromTocItemVo(tocVo)));
            if (numNode != null) {
                if (!newNum.equals(numNode.getTextContent())) {
                    numNode.setTextContent(newNum);
                }
                if (tocVo.isUndeleted()) {
                    XercesUtils.updateXMLIDAttributeFullStructureNode(numNode, EMPTY_STRING, true);
                }
            } else {
                numNode = createElement(node.getOwnerDocument(), XercesUtils.getNumTag(getTagValueFromTocItemVo(tocVo)), newNum);
            }
            if (!EC.equals(tocVo.getOriginNumAttr())) { //TODO temp solution of not setting origin only for LS
                addAttribute(numNode, LEOS_ORIGIN_ATTR, tocVo.getOriginNumAttr());
            }
            if (numNode.getNodeName().equalsIgnoreCase(INLINE)) {
                insertOrUpdateAttributeValue(numNode, XML_NAME, INLINE_NUM);
            }
        }
        return numNode;
    }

    private static Boolean checkIfParagraphNumberingIsToggled(TableOfContentItemVO tableOfContentItemVO) {
        if (PARAGRAPH.equals(tableOfContentItemVO.getTocItem().getAknTag().value())) {
            if (tableOfContentItemVO.getParentItem().isNumberingToggled() != null) {
                return tableOfContentItemVO.getParentItem().isNumberingToggled();
            } else {
                int index = tableOfContentItemVO.getParentItem().getChildItemsView().indexOf(tableOfContentItemVO);
                TableOfContentItemVO paragraphToCompare = null;
                if (index > 0) {
                    paragraphToCompare = tableOfContentItemVO.getParentItem().getChildItemsView().get(0);
                } else if (tableOfContentItemVO.getParentItem().getChildItemsView().size() > 1) {
                    paragraphToCompare = tableOfContentItemVO.getParentItem().getChildItemsView().get(1);
                }
                if (paragraphToCompare != null && TableOfContentProcessor.getTagValueFromTocItemVo(paragraphToCompare).equals(PARAGRAPH) && !StringUtils.isEmpty(paragraphToCompare.getNumber()) && (paragraphToCompare.getNumSoftActionAttr() == null || !paragraphToCompare.getNumSoftActionAttr().equals(SoftActionType.DELETE)) && tableOfContentItemVO.isIndentedOrRestored()) {
                    return true;
                }
            }
        }
        return null;
    }

    public static String createNumContent(TableOfContentItemVO tocVo) {
        StringBuilder item = new StringBuilder(StringUtils.capitalize(tocVo.getTocItem().getAknTag().value()));
        String newNum = trimmedXml(tocVo.getNumber());
        if (tocVo.getTocItem().isNumWithType()) {
            newNum = item + " " + newNum;
        }
        return newNum;
    }
    
    public static Node extractOrBuildHeaderElement(Node node, TableOfContentItemVO tocVo, User user) {
        Node headingNode = null;
        String newHeading = tocVo.getHeading();
        if ((tocVo.getTocItem().getItemHeading().equals(OptionsType.MANDATORY) ||
                tocVo.getTocItem().getItemHeading().equals(OptionsType.OPTIONAL)) &&
                        ((newHeading != null) && !StringUtils.isEmpty(newHeading.replaceAll(NBSP, EMPTY_STRING).trim()))) {
            headingNode = extractOrBuildHeaderElement(node, newHeading);
            if (tocVo.isUndeleted()) {
                XercesUtils.updateXMLIDAttributeFullStructureNode(headingNode, EMPTY_STRING, true);
            }
        } else if (tocVo.getTocItem().getItemHeading().equals(OptionsType.OPTIONAL)
                && EC.equalsIgnoreCase(tocVo.getOriginHeadingAttr()) && SoftActionType.DELETE.equals(tocVo.getHeadingSoftActionAttr())) {
            headingNode = extractOrBuildHeaderElement(node, EMPTY_STRING);
            XercesUtils.updateXMLIDAttributeFullStructureNode(headingNode, SOFT_DELETE_PLACEHOLDER_ID_PREFIX, true);
            updateSoftInfo(headingNode, SoftActionType.DELETE, null, user, CN, null, null, null);
        }
        return headingNode;
    }

    private static Node extractOrBuildHeaderElement(Node node, String newHeading) {
        Node headingNode = XercesUtils.getFirstChild(node, HEADING);
        if (headingNode == null) {
            headingNode = createElement(node.getOwnerDocument(), HEADING, newHeading);
        } else if (!headingNode.getTextContent().equals(newHeading)) {
            headingNode = headingNode.cloneNode(false);
            headingNode.setTextContent(newHeading);
        }
        return headingNode;
    }
    
    public static List<Node> extractLevelNonTocItems(List<TocItem> tocItems, Map<TocItem, List<TocItem>> tocRules, Node node, TableOfContentItemVO tocVo) {
        List<Node> childrenToAppend = new ArrayList<>();
        List<Node> children = XercesUtils.getChildren(node);
        for (int i = 0; i < children.size(); i++) {
            Node remainingNode = extractNonTocItemExceptNumAndHeadingAndIntro(tocItems, tocRules, children.get(i));
            if (remainingNode != null) {
                childrenToAppend.add(remainingNode);
            }
        }
        return childrenToAppend;
    }

    public static List<Node> extractLevelNonTocItemsKeepingTextNodes(List<TocItem> tocItems, Map<TocItem, List<TocItem>> tocRules, Node node, TableOfContentItemVO tocVo) {
        List<Node> childrenToAppend = new ArrayList<>();
        NodeList children = node.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node remainingNode = extractNonTocItemExceptNumAndHeadingAndIntro(tocItems, tocRules, children.item(i));
            if (remainingNode != null) {
                childrenToAppend.add(remainingNode);
            }
        }
        return childrenToAppend;
    }

    private static Node extractNonTocItemExceptNumAndHeadingAndIntro(List<TocItem> tocItems, Map<TocItem, List<TocItem>> tocRules, Node node) {
        String tagName = node.getNodeName();
        TocItem tocItem = StructureConfigUtils.getTocItemByName(tocItems, tagName);
        if ((tocItem == null || !shouldItemBeAddedToToc(tocItems, tocRules, node, tocItem)) &&
                (!tagName.equals(NUM) && !isCrossheadingNum(node) && !tagName.equals(HEADING) && !tagName.equals(INTRO))) {
            return node;
        }
        return null;
    }

    private static boolean isCrossheadingNum(Node node) {
        return node != null && node.getNodeName().equalsIgnoreCase(INLINE) && getAttributeValue(node, XML_NAME) != null && getAttributeValue(node, XML_NAME).equalsIgnoreCase(INLINE_NUM);
    }

    public static TableOfContentItemVO buildTableOfContentFromNodeId(final List<TocItem> tocItems, final Map<TocItem, List<TocItem>> tocRules, final List<NumberingConfig> numberingConfigs, final String startingNodeId, final byte[] xmlContent, final TocMode mode) {
        LOG.trace("Start building the table of content from node id {}", startingNodeId);
        long startTime = System.currentTimeMillis();
        TableOfContentItemVO itemVO = null;
        List<TableOfContentItemVO> itemVOList;
        try {
            Node document = XercesUtils.createXercesDocument(xmlContent);
            String xPath = "//*[@xml:id = '" + startingNodeId + "']";
            Node node = XercesUtils.getFirstElementByXPath(document, xPath);
            if (node != null) {
                itemVO = buildTableOfContentsItemVO(numberingConfigs, tocItems, node);
                itemVOList = getAllChildTableOfContentItems(node, tocItems, tocRules, numberingConfigs, mode);
                itemVO.addAllChildItems(itemVOList);
            }
        } catch (Exception e) {
            LOG.error("Unable to build the Table of content item list", e);
            throw new RuntimeException("Unable to build the Table of content item list", e);
        }

        LOG.trace("Build table of content from node completed in {} ms", (System.currentTimeMillis() - startTime));
        return itemVO;
    }

    public static void updateSoftInfo(Node node, SoftActionType action, Boolean isSoftActionRoot, User user, String originAttrValue,
                                      String moveId, String tagName, TableOfContentItemVO tocVo) {
        if (originAttrValue == null) {
            return;
        }

        if (action != null) {
            switch (action) {
                case DELETE:
                    insertOrUpdateAttributeValue(node, LEOS_EDITABLE_ATTR, Boolean.FALSE.toString());
                    insertOrUpdateAttributeValue(node, LEOS_DELETABLE_ATTR, Boolean.FALSE.toString());
                    removeAttribute(node, LEOS_SOFT_MOVED_LABEL_ATTR);
                    removeAttribute(node, LEOS_SOFT_MOVE_FROM);
                    removeAttribute(node, LEOS_SOFT_MOVE_TO);
                    break;
                case MOVE_TO:
                    insertOrUpdateAttributeValue(node, LEOS_EDITABLE_ATTR, Boolean.FALSE.toString());
                    insertOrUpdateAttributeValue(node, LEOS_DELETABLE_ATTR, Boolean.FALSE.toString());
                    insertOrUpdateAttributeValue(node, LEOS_SOFT_MOVE_TO, moveId);
                    removeAttribute(node, LEOS_SOFT_MOVE_FROM);
                    break;
                case MOVE_FROM:
                    removeAttribute(node, LEOS_SOFT_MOVE_TO);
                    insertOrUpdateAttributeValue(node, LEOS_SOFT_MOVE_FROM, moveId);
                    break;
                case UNDELETE:
                    if(XercesUtils.getId(node).startsWith(SOFT_DELETE_PLACEHOLDER_ID_PREFIX)) {
                    	restoreOldId(node);
                    }
                    removeAttribute(node, LEOS_EDITABLE_ATTR);
                    removeAttribute(node, LEOS_DELETABLE_ATTR);
                    break;
                default:
                    removeAttribute(node, LEOS_SOFT_MOVED_LABEL_ATTR);
                    removeAttribute(node, LEOS_SOFT_MOVE_FROM);
                    removeAttribute(node, LEOS_SOFT_MOVE_TO);
            }
            addAttribute(node, LEOS_SOFT_ACTION_ATTR, action.getSoftAction());
        } else {
            removeAttribute(node, LEOS_SOFT_ACTION_ATTR);
            removeAttribute(node, LEOS_SOFT_USER_ATTR);
            removeAttribute(node, LEOS_SOFT_DATE_ATTR);

            removeAttribute(node, LEOS_SOFT_MOVED_LABEL_ATTR);
            removeAttribute(node, LEOS_SOFT_MOVE_FROM);
            removeAttribute(node, LEOS_SOFT_MOVE_TO);
            removeAttribute(node, LEOS_SOFT_ACTION_ROOT_ATTR);
        }

        updateUserDetails(node, tocVo, action, user);

        if (isSoftActionRoot != null) {
            insertOrUpdateAttributeValue(node, LEOS_SOFT_ACTION_ROOT_ATTR, isSoftActionRoot.toString());
        }
    }

    protected static void restoreOldId(Node node) {
        XercesUtils.updateXMLIDAttributeFullStructureNode(node, EMPTY_STRING, true);
    }

    private static void updateUserDetails(Node node, TableOfContentItemVO tocVo, SoftActionType action, User user) {
        if ((tocVo != null) && (tocVo.getSoftUserAttr() != null) && (tocVo.getSoftDateAttr() != null)) {
            insertOrUpdateAttributeValue(node, LEOS_SOFT_USER_ATTR, tocVo.getSoftUserAttr());
            insertOrUpdateAttributeValue(node, LEOS_SOFT_DATE_ATTR, getDateAsXml(tocVo.getSoftDateAttr()));
        } else if (action != null) {
            insertOrUpdateAttributeValue(node, LEOS_SOFT_USER_ATTR, user != null ? getSoftUserAttribute(user) : null);
            insertOrUpdateAttributeValue(node, LEOS_SOFT_DATE_ATTR, getDateAsXml());
        }
    }

    protected static void addUserInfoIfContentHasChanged(Node node, Node newNode, User user) {
        if ((node != null) && (newNode != null) && (!newNode.getTextContent().equals(node.getTextContent()))) {
            insertOrUpdateAttributeValue(newNode, LEOS_SOFT_USER_ATTR, user != null ? getSoftUserAttribute(user) : null);
            insertOrUpdateAttributeValue(newNode, LEOS_SOFT_DATE_ATTR, getDateAsXml());
        }
    }

    protected static void updateTocItemTypeAttributes(List<TocItem> tocItems, Node node, TableOfContentItemVO item) {
        String tagName = TableOfContentProcessor.getTagValueFromTocItemVo(item);
        List<TocItemType> tocItemTypes = getTocItemTypesByTagName(tocItems, tagName);
        // Clean attributes first
        tocItemTypes.forEach(tocItemType -> {
            if (tocItemType.getAttribute() != null) {
                XercesUtils.removeAttribute(node, tocItemType.getAttribute().getAttributeName());
            }
        });
        // Add attribute
        Attribute attribute = getAttributeByTagNameAndTocItemType(tocItems, item.getTocItemType(), tagName);
        if (attribute != null) {
            XercesUtils.addAttribute(node, attribute.getAttributeName(), attribute.getAttributeValue());
        }
    }
}
