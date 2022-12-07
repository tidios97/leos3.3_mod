/*
 * Copyright 2017 European Commission
 *
 * Licensed under the EUPL, Version 1.1 or – as soon they will be approved by the European Commission - subsequent versions of the EUPL (the "Licence");
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

import eu.europa.ec.leos.i18n.MessageHelper;
import eu.europa.ec.leos.model.action.SoftActionType;
import eu.europa.ec.leos.model.xml.Element;
import eu.europa.ec.leos.vo.toc.NumberingType;
import eu.europa.ec.leos.vo.toc.TableOfContentItemVO;
import eu.europa.ec.leos.vo.toc.TocItem;
import eu.europa.ec.leos.vo.toc.StructureConfigUtils;
import eu.europa.ec.leos.vo.toc.TocItemTypeName;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static eu.europa.ec.leos.model.action.SoftActionType.ADD;
import static eu.europa.ec.leos.model.action.SoftActionType.DELETE;
import static eu.europa.ec.leos.model.action.SoftActionType.MOVE_FROM;
import static eu.europa.ec.leos.model.action.SoftActionType.MOVE_TO;
import static eu.europa.ec.leos.services.processor.content.TableOfContentProcessor.getTagValueFromTocItemVo;
import static eu.europa.ec.leos.services.support.XmlHelper.ARTICLE;
import static eu.europa.ec.leos.services.support.XmlHelper.CHAPTER;
import static eu.europa.ec.leos.services.support.XmlHelper.EMPTY_STRING;
import static eu.europa.ec.leos.services.support.XmlHelper.PARAGRAPH;
import static eu.europa.ec.leos.services.support.XmlHelper.PART;
import static eu.europa.ec.leos.services.support.XmlHelper.POINT;
import static eu.europa.ec.leos.services.support.XmlHelper.SECTION;
import static eu.europa.ec.leos.services.support.XmlHelper.TBLOCK;
import static eu.europa.ec.leos.services.support.XmlHelper.TITLE;
import static eu.europa.ec.leos.services.support.XmlHelper.removeTag;

public class TableOfContentHelper {

    public static final int DEFAULT_CAPTION_MAX_SIZE = 50;

    public static final List<String> ELEMENTS_WITHOUT_CONTENT = Collections.unmodifiableList(Arrays.asList(ARTICLE, SECTION, CHAPTER, TITLE, PART));
    private static final String MOVE_LABEL_SPAN_START_TAG = "<span class=\"leos-soft-move-label\">";
    private static final String MOVED_TITLE_SPAN_START_TAG = "<span class=\"leos-soft-move-title\">";
    private static final String SPAN_END_TAG = "</span>";
    private static final String SPACE = " ";
    private static final int MOVED_LABEL_SIZE = MOVED_TITLE_SPAN_START_TAG.length() + SPACE.length() + MOVE_LABEL_SPAN_START_TAG.length() + 2 * SPAN_END_TAG.length();
    
    private static String getMovedLabel(MessageHelper messageHelper) {
        return MOVE_LABEL_SPAN_START_TAG + messageHelper.getMessage("toc.edit.window.softmove.label") + SPAN_END_TAG;
    }

    private static Boolean shouldAddMoveLabel(TableOfContentItemVO tocItem) {
        return tocItem.isSoftActionRoot() != null && tocItem.isSoftActionRoot()
                && (MOVE_TO.equals(tocItem.getSoftActionAttr()) || SoftActionType.MOVE_FROM.equals(tocItem.getSoftActionAttr()));
    }

    public static String buildItemCaption(TableOfContentItemVO tocItem, int captionMaxSize, MessageHelper messageHelper) {
        Validate.notNull(tocItem.getTocItem(), "Type should not be null");

        boolean shoudlAddMovedLabel = shouldAddMoveLabel(tocItem);

        StringBuilder itemDescription = tocItem.getTocItem().isItemDescription()
                ? new StringBuilder(getDisplayableTocItem(tocItem.getTocItem(), messageHelper)).append(SPACE)
                : new StringBuilder();

        if (shoudlAddMovedLabel) {
            itemDescription.insert(0, MOVED_TITLE_SPAN_START_TAG + SPACE);
        }

        if (!StringUtils.isEmpty(tocItem.getNumber()) && !StringUtils.isEmpty(tocItem.getHeading())) {
            if(tocItem.getTocItem().getAknTag().value().equalsIgnoreCase(tocItem.getNumber().trim())){
                tocItem.setNumber(StructureConfigUtils.HASH_NUM_VALUE);
            }
            itemDescription.append(tocItem.getNumber());
            if (shoudlAddMovedLabel) {
                itemDescription.append(SPAN_END_TAG).append(getMovedLabel(messageHelper));
            }
            if (TBLOCK.equals(tocItem.getTocItem().getAknTag().name()) || StringUtils.isEmpty(tocItem.getContent())) {
                itemDescription.append(StructureConfigUtils.CONTENT_SEPARATOR).append(tocItem.getHeading());
            } else if (!StringUtils.isEmpty(tocItem.getContent())) {
                itemDescription.append(StructureConfigUtils.NUM_HEADING_SEPARATOR).append(tocItem.getHeading());
            }
        } else if (!StringUtils.isEmpty(tocItem.getNumber())) {
            SoftActionType softAction = tocItem.getNumSoftActionAttr();

            if(softAction != null){
                if (PARAGRAPH.equals(tocItem.getTocItem().getAknTag().value()) && (DELETE.equals(softAction))
                        && !MOVE_TO.equals(tocItem.getSoftActionAttr())) {
                    itemDescription.append("<span class=\"leos-soft-num-removed\">" + tocItem.getNumber() + "</span>");
                } else if (PARAGRAPH.equals(tocItem.getTocItem().getAknTag().value())
                        && SoftActionType.ADD.equals(softAction) && !MOVE_TO.equals(tocItem.getSoftActionAttr())) {
                    itemDescription.append("<span class=\"leos-soft-num-new\">" + tocItem.getNumber() + "</span>");
                }
                captionMaxSize = captionMaxSize+itemDescription.length();
            } else {
                if (tocItem.isIndented() && !tocItem.getNumber().equals(tocItem.getIndentOriginNumValue())) {
                    itemDescription.append("<span class=\"leos-soft-num-new\">" + tocItem.getNumber() + "</span>");
                    captionMaxSize = captionMaxSize+itemDescription.length();
                } else {
                    itemDescription.append(tocItem.getNumber());
                }
                if (shoudlAddMovedLabel) {
                    itemDescription.append(SPAN_END_TAG).append(getMovedLabel(messageHelper));
                }
            }
        } else if (!StringUtils.isEmpty(tocItem.getHeading())) {
            itemDescription.append(tocItem.getHeading());
            if (shoudlAddMovedLabel) {
                itemDescription.append(SPAN_END_TAG).append(getMovedLabel(messageHelper));
            }
        } else if (shoudlAddMovedLabel) {
            itemDescription.append(SPAN_END_TAG).append(getMovedLabel(messageHelper));
        }

        if (tocItem.getTocItem().isContentDisplayed()) {
            itemDescription.append(itemDescription.length() > 0 ? StructureConfigUtils.CONTENT_SEPARATOR : "").append(removeTag(tocItem.getContent()));
        }

        return StringUtils.abbreviate(itemDescription.toString(), shoudlAddMovedLabel ? captionMaxSize + MOVED_LABEL_SIZE : captionMaxSize);
    }

    public static String getDisplayableTocItem(TocItem tocItem, MessageHelper messageHelper) {
        if (tocItem.getNumberingType().equals(NumberingType.BULLET_NUM)) {
            return messageHelper.getMessage("toc.item.type.bullet");
        } else {
            return messageHelper.getMessage("toc.item.type." + tocItem.getAknTag().value().toLowerCase());
        }
    }
    
    public static String getItemSoftStyle(TableOfContentItemVO tableOfContentItemVO) {
        String itemSoftStyle = EMPTY_STRING;
        if (tableOfContentItemVO.getSoftActionAttr() != null) {
            if (hasTocItemSoftAction(tableOfContentItemVO, ADD)) {
                itemSoftStyle = "leos-soft-new";
            } else if (hasTocItemSoftAction(tableOfContentItemVO, DELETE)) {
                String initialNum = tableOfContentItemVO.getInitialNum();
                if (initialNum != null) {
                    initialNum = initialNum.replace("Article ", "");
                    tableOfContentItemVO.setNumber(initialNum);
                }
                itemSoftStyle = "leos-soft-removed";
            } else if (hasTocItemSoftAction(tableOfContentItemVO, MOVE_TO)) {
                itemSoftStyle = "leos-soft-movedto";
            } else if (hasTocItemSoftAction(tableOfContentItemVO, MOVE_FROM)) {
                itemSoftStyle = "leos-soft-movedfrom";
            }
        }
        return itemSoftStyle;
    }

    public static boolean hasTocItemSoftAction(final TableOfContentItemVO item, SoftActionType actionType) {
        return item != null && item.getSoftActionAttr() != null
                && (item.getSoftActionAttr().equals(actionType));
    }

    public static boolean hasTocItemSoftOrigin(final TableOfContentItemVO item, final String softOriginValue) {
        return item != null && item.getOriginAttr() != null && item.getOriginAttr().equals(softOriginValue);
    }

    public static boolean isTocItemFirstChild(TableOfContentItemVO item, TableOfContentItemVO child) {
        return item.getChildItems().indexOf(child) == 0;
    }

    public static int getTocItemChildPosition(TableOfContentItemVO item, TableOfContentItemVO child) {
        return item.getChildItems().indexOf(child);
    }

    public static Optional<TableOfContentItemVO> getItemFromTocById(String elementId, List<TableOfContentItemVO> toc) {
        Optional<TableOfContentItemVO> hasIndentedItem = Optional.empty();
        for (TableOfContentItemVO root : toc) {
            hasIndentedItem = getTocElementById(elementId, root);
            if (hasIndentedItem.isPresent()) {
                break;
            }
        }
        return hasIndentedItem;
    }

    public static Optional<TableOfContentItemVO> getTocElementById(final String elementId, final TableOfContentItemVO item) {
        if (item.getId().equals(elementId)) {
            return Optional.of(item);
        }
        for (TableOfContentItemVO child : item.getChildItems()) {
            Optional<TableOfContentItemVO> childItem = getTocElementById(elementId, child);
            if (childItem.isPresent()) {
                return childItem;
            }
        }
        return Optional.empty();
    }

    public static boolean isElementInToc(final Element element, final List<TableOfContentItemVO> toc) {
        for (TableOfContentItemVO itemVO: toc) {
            Optional<TableOfContentItemVO> item = getTocElementById(element.getElementId(), itemVO);
            if (item.isPresent()) {
                return true;
            }
        }
        return false;
    }

    public static List<TableOfContentItemVO> getChildrenWithTags(TableOfContentItemVO parent, List<String> tags) {
        return parent.getChildItemsView().stream().filter(child -> tags.contains(getTagValueFromTocItemVo(child))).collect(Collectors.toList());
    }

    public static int getItemIndentLevel(TableOfContentItemVO item, int startingDepth, List<String> tags) {
        TableOfContentItemVO parent = item.getParentItem();
        while (parent != null) {
            if (ArrayUtils.contains(tags.toArray(), TableOfContentProcessor.getTagValueFromTocItemVo(parent))) {
                startingDepth++;
            }
            parent = parent.getParentItem();
        }

        return startingDepth;
    }

    public static TableOfContentItemVO getFirstAscendant(TableOfContentItemVO tocItem, List<String> tagNames) {
        if (tocItem != null) {
            if (tagNames.contains(getTagValueFromTocItemVo(tocItem))) {
                return tocItem;
            } else {
                return getFirstAscendant(tocItem.getParentItem(), tagNames);
            }
        } else {
            return null;
        }
    }

    public static void convertArticle(List<TocItem> tocItems, TableOfContentItemVO article, TocItemTypeName oldValue, TocItemTypeName newValue) {
        updateTocItemsNumberingConfig(tocItems, article
                   , StructureConfigUtils.getNumberingTypeByTagNameAndTocItemType(tocItems, oldValue, POINT)
                   , StructureConfigUtils.getNumberingTypeByTagNameAndTocItemType(tocItems, newValue, POINT));
    }

    private static void updateTocItemsNumberingConfig(List<TocItem> tocItems, TableOfContentItemVO item, NumberingType fromNumberingType,
                                                      NumberingType toNumberingType) {
        for (TableOfContentItemVO child : item.getChildItems()) {
            if (child.getTocItem().getNumberingType().equals(fromNumberingType)) {
                TocItem tocItem = StructureConfigUtils.getTocItemByNumberingType(tocItems, toNumberingType, child.getTocItem().getAknTag().name());
                child.setTocItem(tocItem);
            }
            child.setAffected(true);
            updateTocItemsNumberingConfig(tocItems, child, fromNumberingType, toNumberingType);
        }
    }

}
