package eu.europa.ec.leos.services.processor.content.indent;

import com.google.common.base.Strings;
import eu.europa.ec.leos.services.support.XmlHelper;
import eu.europa.ec.leos.services.processor.content.TableOfContentProcessor;
import eu.europa.ec.leos.vo.toc.TableOfContentItemVO;
import eu.europa.ec.leos.vo.toc.TocItem;
import eu.europa.ec.leos.vo.toc.StructureConfigUtils;
import eu.europa.ec.leos.vo.toc.indent.IndentedItemType;
import io.atlassian.fugue.Pair;
import org.apache.commons.lang.ArrayUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import static eu.europa.ec.leos.model.action.SoftActionType.DELETE;
import static eu.europa.ec.leos.model.action.SoftActionType.MOVE_FROM;
import static eu.europa.ec.leos.model.action.SoftActionType.MOVE_TO;
import static eu.europa.ec.leos.model.action.SoftActionType.TRANSFORM;
import static eu.europa.ec.leos.services.processor.content.TableOfContentHelper.hasTocItemSoftAction;
import static eu.europa.ec.leos.services.processor.content.TableOfContentHelper.hasTocItemSoftOrigin;
import static eu.europa.ec.leos.services.support.XmlHelper.ARTICLE;
import static eu.europa.ec.leos.services.support.XmlHelper.CN;
import static eu.europa.ec.leos.services.support.XmlHelper.EC;
import static eu.europa.ec.leos.services.support.XmlHelper.INDENT;
import static eu.europa.ec.leos.services.support.XmlHelper.LEVEL;
import static eu.europa.ec.leos.services.support.XmlHelper.LIST;
import static eu.europa.ec.leos.services.support.XmlHelper.MAIN_BODY;
import static eu.europa.ec.leos.services.support.XmlHelper.PARAGRAPH;
import static eu.europa.ec.leos.services.support.XmlHelper.POINT;
import static eu.europa.ec.leos.services.support.XmlHelper.SOFT_DELETE_PLACEHOLDER_ID_PREFIX;
import static eu.europa.ec.leos.services.support.XmlHelper.SOFT_MOVE_PLACEHOLDER_ID_PREFIX;
import static eu.europa.ec.leos.services.support.XmlHelper.SUBPARAGRAPH;
import static eu.europa.ec.leos.services.support.XmlHelper.SUBPOINT;
import static eu.europa.ec.leos.services.processor.content.TableOfContentProcessor.getTagValueFromTocItemVo;

@Component
public class IndentConversionHelper {
    public static final String INDENT_PLACEHOLDER_ID_PREFIX = "indented_";
    public static final String[] NUMBERED_ITEMS = {PARAGRAPH, POINT, INDENT};
    public static final String[] NUMBERED_AND_LEVEL_ITEMS = {PARAGRAPH, POINT, LEVEL, INDENT};
    public static final String[] UNUMBERED_ITEMS = {SUBPARAGRAPH, SUBPOINT};
    public static final String[] PARAGRAPH_LEVEL_ITEMS = {SUBPARAGRAPH, PARAGRAPH};

    @Autowired
    TableOfContentProcessor tableOfContentProcessor;

    public Pair<TableOfContentItemVO, Boolean> convertIndentedItem(List<TocItem> tocItems, TableOfContentItemVO originalItem
            , boolean isNumbered, IndentedItemType beforeIndentItemType, int originalIndentLevel, boolean isIndent) {
        IndentedItemType originalIndentItemType = originalItem.getIndentOriginType() == null ? beforeIndentItemType : originalItem.getIndentOriginType();
        IndentedItemType targetIndentItemType;

        if (getTagValueFromTocItemVo(originalItem).equals(LEVEL)) {
            return new Pair<>(originalItem, false);
        }

        int currentIndentLevel = getIndentedItemIndentLevel(originalItem);
        boolean paragraphIndentLevel = (!isIndent && !isNumbered && currentIndentLevel == 1)
                || (!isIndent && isNumbered && currentIndentLevel == 0)
                || (isIndent && !isNumbered && originalIndentLevel == 0);

        if (isNumbered) {
            // Should be converted to a point
            // Now we must know if it's a point-alinea or a single point

            if (originalItem.getChildItems().isEmpty()) {
                // For sure, convert it to a single point or single paragraph
                targetIndentItemType = paragraphIndentLevel ? IndentedItemType.PARAGRAPH : IndentedItemType.POINT;
            } else if ((originalItem.getChildItems().size() == 1) &&
                    (beforeIndentItemType.equals(IndentedItemType.FIRST_SUBPOINT) ||
                            beforeIndentItemType.equals(IndentedItemType.FIRST_SUBPARAGRAPH))) {
                // Should be converted to a single point
                targetIndentItemType = paragraphIndentLevel ? IndentedItemType.PARAGRAPH : IndentedItemType.POINT;
            } else {
                targetIndentItemType = paragraphIndentLevel ? IndentedItemType.FIRST_SUBPARAGRAPH : IndentedItemType.FIRST_SUBPOINT;
            }
        } else {
            targetIndentItemType = paragraphIndentLevel ? IndentedItemType.OTHER_SUBPARAGRAPH : IndentedItemType.OTHER_SUBPOINT;
        }

        if (targetIndentItemType.equals(beforeIndentItemType)) {
            return new Pair<>(originalItem, targetIndentItemType.equals(originalIndentItemType));
        }

        boolean restored = originalIndentItemType.equals(targetIndentItemType);

        switch (targetIndentItemType) {
            case FIRST_SUBPOINT:
                if (beforeIndentItemType.equals(IndentedItemType.OTHER_SUBPOINT)) {
                    originalItem = buildFirstElementFromSubElement(tocItems, originalItem, originalIndentLevel, restored, false, false);
                } else if (beforeIndentItemType.equals(IndentedItemType.OTHER_SUBPARAGRAPH)) {
                    originalItem = buildFirstElementFromSubElement(tocItems, originalItem, originalIndentLevel, restored, true, false);
                } else if (beforeIndentItemType.equals(IndentedItemType.FIRST_SUBPARAGRAPH)) {
                    originalItem = buildFirstElementFromFirstElement(tocItems, originalItem, originalIndentLevel, restored, false);
                } else if (beforeIndentItemType.equals(IndentedItemType.PARAGRAPH)) {
                    originalItem = buildFirstElementFromElement(tocItems, originalItem, originalIndentLevel, restored,true, false);
                } else {
                    originalItem = buildFirstElementFromElement(tocItems, originalItem, originalIndentLevel, restored, false, false);
                }
                break;
            case OTHER_SUBPOINT:
                if (beforeIndentItemType.equals(IndentedItemType.POINT)) {
                    originalItem = buildSubElementFromElement(tocItems, originalItem, originalIndentLevel, restored, false, false);
                } else if (beforeIndentItemType.equals(IndentedItemType.OTHER_SUBPARAGRAPH)) {
                    originalItem = buildSubElementFromSubElement(tocItems, originalItem, originalIndentLevel, restored, false);
                } else if (beforeIndentItemType.equals(IndentedItemType.FIRST_SUBPARAGRAPH)) {
                    originalItem = buildSubElementFromFirstElement(tocItems, originalItem, originalIndentLevel, restored, true, false);
                } else if (beforeIndentItemType.equals(IndentedItemType.PARAGRAPH)) {
                    originalItem = buildSubElementFromElement(tocItems, originalItem, originalIndentLevel, restored, true, false);
                }  else {
                    originalItem = buildSubElementFromFirstElement(tocItems, originalItem, originalIndentLevel, restored, false, false);
                }
                break;
            case POINT:
                if (beforeIndentItemType.equals(IndentedItemType.OTHER_SUBPOINT)) {
                    originalItem = buildElementFromSubElement(tocItems, originalItem, originalIndentLevel, restored, false, false);
                } else if (beforeIndentItemType.equals(IndentedItemType.OTHER_SUBPARAGRAPH)) {
                    originalItem = buildElementFromSubElement(tocItems, originalItem, originalIndentLevel, restored, true, false);
                } else if (beforeIndentItemType.equals(IndentedItemType.FIRST_SUBPARAGRAPH)) {
                    originalItem = buildElementFromFirstElement(tocItems, originalItem, originalIndentLevel, restored, true, false);
                } else if (beforeIndentItemType.equals(IndentedItemType.PARAGRAPH)) {
                    originalItem = buildElementFromElement(tocItems, originalItem, originalIndentLevel, restored, false);
                } else {
                    originalItem = buildElementFromFirstElement(tocItems, originalItem, originalIndentLevel, restored, false, false);
                }
                break;
            case FIRST_SUBPARAGRAPH:
                if (beforeIndentItemType.equals(IndentedItemType.OTHER_SUBPOINT)) {
                    originalItem = buildFirstElementFromSubElement(tocItems, originalItem, originalIndentLevel, restored, true, true);
                } else if (beforeIndentItemType.equals(IndentedItemType.OTHER_SUBPARAGRAPH)) {
                    originalItem = buildFirstElementFromSubElement(tocItems, originalItem, originalIndentLevel, restored, false, true);
                } else if (beforeIndentItemType.equals(IndentedItemType.FIRST_SUBPOINT)) {
                    originalItem = buildFirstElementFromFirstElement(tocItems, originalItem, originalIndentLevel, restored, true);
                } else if (beforeIndentItemType.equals(IndentedItemType.PARAGRAPH)) {
                    originalItem = buildFirstElementFromElement(tocItems, originalItem, originalIndentLevel, restored,false, true);
                } else {
                    originalItem = buildFirstElementFromElement(tocItems, originalItem, originalIndentLevel, restored,true, true);
                }
                manageNumOnUnumberedParagraph(originalItem);
                break;
            case OTHER_SUBPARAGRAPH:
                if (beforeIndentItemType.equals(IndentedItemType.POINT)) {
                    originalItem = buildSubElementFromElement(tocItems, originalItem, originalIndentLevel, restored, true, true);
                } else if (beforeIndentItemType.equals(IndentedItemType.OTHER_SUBPOINT)) {
                    originalItem = buildSubElementFromSubElement(tocItems, originalItem, originalIndentLevel, restored, true);
                } else if (beforeIndentItemType.equals(IndentedItemType.FIRST_SUBPARAGRAPH)) {
                    originalItem = buildSubElementFromFirstElement(tocItems, originalItem, originalIndentLevel, restored, false, true);
                } else if (beforeIndentItemType.equals(IndentedItemType.PARAGRAPH)) {
                    originalItem = buildSubElementFromElement(tocItems, originalItem, originalIndentLevel, restored, false, true);
                }  else {
                    originalItem = buildSubElementFromFirstElement(tocItems, originalItem, originalIndentLevel, restored, true, true);
                }
                break;
            case PARAGRAPH:
                if (beforeIndentItemType.equals(IndentedItemType.OTHER_SUBPOINT)) {
                    originalItem = buildElementFromSubElement(tocItems, originalItem, originalIndentLevel, restored, true, true);
                } else if (beforeIndentItemType.equals(IndentedItemType.OTHER_SUBPARAGRAPH)) {
                    originalItem = buildElementFromSubElement(tocItems, originalItem, originalIndentLevel, restored, false, true);
                } else if (beforeIndentItemType.equals(IndentedItemType.FIRST_SUBPARAGRAPH)) {
                    originalItem = buildElementFromFirstElement(tocItems, originalItem, originalIndentLevel, restored, false, true);
                } else if (beforeIndentItemType.equals(IndentedItemType.POINT)) {
                    originalItem = buildElementFromElement(tocItems, originalItem, originalIndentLevel, restored, true);
                } else {
                    originalItem = buildElementFromFirstElement(tocItems, originalItem, originalIndentLevel, restored, true,  true);
                }
                manageNumOnUnumberedParagraph(originalItem);
                break;
        }
        return new Pair<>(originalItem,restored);
    }

    private void manageNumOnUnumberedParagraph(TableOfContentItemVO originalItem) {
        if (originalItem.isIndented() && originalItem.getIndentOriginNumId() != null) {
            int index = originalItem.getParentItem().getChildItemsView().indexOf(originalItem);
            TableOfContentItemVO paragraphToCompare = null;
            if (index > 0) {
                paragraphToCompare = originalItem.getParentItem().getChildItemsView().get(0);
            } else if (originalItem.getParentItem().getChildItemsView().size() > 1) {
                paragraphToCompare = originalItem.getParentItem().getChildItemsView().get(1);
            }

            if (paragraphToCompare != null) {
                if (DELETE.equals(paragraphToCompare.getNumSoftActionAttr())
                        || Strings.isNullOrEmpty(paragraphToCompare.getNumber())) {
                    originalItem.setNumSoftActionAttr(DELETE);
                }
            } else if (originalItem.getIndentOriginNumId().startsWith(SOFT_DELETE_PLACEHOLDER_ID_PREFIX)) {
                originalItem.setNumSoftActionAttr(DELETE);
            }
        }
    }

    public void convertOtherItem(List<TocItem> tocItems, TableOfContentItemVO originalItem) {
        //Only possible conversions here:
        // 1. From point to first-subpoint
        // 2. From first-subpoint to point
        // 3. From paragraph to first-subparagraph
        // 2. From first-subparagraph to paragraph

        String tagName = getTagValueFromTocItemVo(originalItem);

        if ((tagName.equals(LIST) || tagName.equals(ARTICLE)) && !originalItem.getChildItemsView().isEmpty()) {
            return;
        } else if (tagName.equals(LIST) && originalItem.getChildItemsView().isEmpty()) {
            // It means that there is an empty list that should be removed
            originalItem.getParentItem().removeChildItem(originalItem);
            originalItem = originalItem.getParentItem();
            tagName = getTagValueFromTocItemVo(originalItem);
        }
        if (tagName.equals(LEVEL) ||tagName.equalsIgnoreCase(MAIN_BODY)) {
            return;
        }

        boolean paragraphLevel = ArrayUtils.contains(PARAGRAPH_LEVEL_ITEMS, tagName);
        int originalIndentLevel = (originalItem.isIndented()) ? originalItem.getIndentOriginIndentLevel() : getIndentedItemIndentLevel(originalItem);
        IndentedItemType beforeIndentItemType = tableOfContentProcessor.isFirstElement(originalItem, SUBPOINT) ? IndentedItemType.FIRST_SUBPOINT
                : tableOfContentProcessor.containsElement(originalItem, SUBPARAGRAPH) ? IndentedItemType.FIRST_SUBPARAGRAPH : paragraphLevel ? IndentedItemType.PARAGRAPH : IndentedItemType.POINT;
        IndentedItemType originalIndentItemType = originalItem.getIndentOriginType() == null ? beforeIndentItemType : originalItem.getIndentOriginType();
        IndentedItemType targetIndentItemType;

        if (originalItem.getChildItems().isEmpty()) {
            // For sure, convert it to a single point
            targetIndentItemType = paragraphLevel ? IndentedItemType.PARAGRAPH : IndentedItemType.POINT;
        } else if ((originalItem.getChildItems().size() == 1) &&
                (beforeIndentItemType.equals(IndentedItemType.FIRST_SUBPOINT) || beforeIndentItemType.equals(IndentedItemType.FIRST_SUBPARAGRAPH)) ) {
            // Should be converted to a single point
            targetIndentItemType = paragraphLevel ? IndentedItemType.PARAGRAPH : IndentedItemType.POINT;
        } else {
            targetIndentItemType = paragraphLevel ? IndentedItemType.FIRST_SUBPARAGRAPH : IndentedItemType.FIRST_SUBPOINT;
        }

        if (targetIndentItemType.equals(beforeIndentItemType)) {
            return;
        }

        boolean restored = originalIndentItemType.equals(targetIndentItemType);

        switch (targetIndentItemType) {
            case FIRST_SUBPOINT:
                originalItem = buildFirstElementFromElement(tocItems, originalItem, originalIndentLevel, restored, false, false);
                if (restored) {
                    isRestored(originalItem, IndentedItemType.FIRST_SUBPOINT);
                }
                break;
            case POINT:
                originalItem = buildElementFromFirstElement(tocItems, originalItem, originalIndentLevel, restored, false, false);
                if (restored) {
                    isRestored(originalItem, IndentedItemType.POINT);
                }
                break;
            case FIRST_SUBPARAGRAPH:
                originalItem = buildFirstElementFromElement(tocItems, originalItem, originalIndentLevel, restored, false, true);
                if (restored) {
                    isRestored(originalItem, IndentedItemType.FIRST_SUBPARAGRAPH);
                }
                break;
            case PARAGRAPH:
                originalItem = buildElementFromFirstElement(tocItems, originalItem, originalIndentLevel, restored, false, true);
                if (restored) {
                    isRestored(originalItem, IndentedItemType.PARAGRAPH);
                }
                break;
        }
    }

    void isRestored(TableOfContentItemVO item, IndentedItemType newIndentedItemType) {
        int newDepth = getIndentedItemIndentLevel(item);
        if (!item.isIndented()) {
            return;
        }
        if (newDepth == item.getIndentOriginIndentLevel() && newIndentedItemType.equals(item.getIndentOriginType())) {
            item.setIndentOriginType(IndentedItemType.RESTORED);
        }
    }

    // All methods to convert to point or to paragraph
    public TableOfContentItemVO buildElementFromSubElement(List<TocItem> tocItems,
                                                    TableOfContentItemVO originalItem,
                                                    int originalIndentLevel,
                                                    boolean restored, boolean notSameKind, boolean toParagraph) {
        removeTransformActionIfRestored(originalItem, restored);
        populateIndentInfoIfNotRestored(originalItem, originalIndentLevel, (notSameKind == toParagraph) ? IndentedItemType.OTHER_SUBPOINT : IndentedItemType.OTHER_SUBPARAGRAPH, restored);

        // Ok, checking is done, convert to point
        TocItem parentTocItem = StructureConfigUtils.getTocItemByName(tocItems, toParagraph ? PARAGRAPH : POINT);
        originalItem.setTocItem(parentTocItem);
        if (restored) {
            removeTransformPrefix(originalItem);
        }
        tableOfContentProcessor.convertTocItemContent(originalItem, null, (notSameKind == toParagraph) ? IndentedItemType.OTHER_SUBPOINT : IndentedItemType.OTHER_SUBPARAGRAPH, toParagraph ? IndentedItemType.PARAGRAPH : IndentedItemType.POINT, restored);
        return originalItem;
    }

    public TableOfContentItemVO buildElementFromFirstElement(List<TocItem> tocItems,
                                                      TableOfContentItemVO originalItem,
                                                      int originalIndentLevel,
                                                      boolean restored, boolean notSameKind, boolean toParagraph) {
        TableOfContentItemVO firstSubelement;

        // If it has more than one child or does not contain the subpoint, conversion is not possible
        if (originalItem.getChildItems().isEmpty()) {
            return originalItem;
        } else {
            firstSubelement = originalItem.getChildItems().get(0);
            if (!getTagValueFromTocItemVo(firstSubelement).equals((notSameKind == toParagraph) ? SUBPOINT : SUBPARAGRAPH)) {
                return originalItem;
            }
        }

        removeTransformActionIfRestored(originalItem, restored);
        populateIndentInfoIfNotRestored(originalItem, originalIndentLevel, (notSameKind == toParagraph) ? IndentedItemType.FIRST_SUBPOINT : IndentedItemType.FIRST_SUBPARAGRAPH, restored);

        handleTransformAction(originalItem, firstSubelement, restored, true);

        //Convert paragraph to point or reverse
        if (notSameKind) {
            TocItem parentTocItem = StructureConfigUtils.getTocItemByName(tocItems, toParagraph ? PARAGRAPH : POINT);
            originalItem.setTocItem(parentTocItem);
        }

        // Ok, checking is done, remove the first subparagraph
        originalItem.removeChildItem(firstSubelement);
        removeTransformPrefix(originalItem);
        tableOfContentProcessor.convertTocItemContent(originalItem, null, (notSameKind == toParagraph) ? IndentedItemType.FIRST_SUBPOINT : IndentedItemType.FIRST_SUBPARAGRAPH, toParagraph ? IndentedItemType.PARAGRAPH : IndentedItemType.POINT, restored);
        return originalItem;
    }

    public TableOfContentItemVO buildElementFromElement(List<TocItem> tocItems,
                                                 TableOfContentItemVO originalItem,
                                                 int originalIndentLevel,
                                                 boolean restored, boolean toParagraph) {
        removeTransformActionIfRestored(originalItem, restored);
        populateIndentInfoIfNotRestored(originalItem, originalIndentLevel, toParagraph ? IndentedItemType.POINT : IndentedItemType.PARAGRAPH, restored);

        // Ok, checking is done, convert to point or paragraph
        TocItem parentTocItem = StructureConfigUtils.getTocItemByName(tocItems, toParagraph ? PARAGRAPH : POINT);
        originalItem.setTocItem(parentTocItem);

        tableOfContentProcessor.convertTocItemContent(originalItem, null, toParagraph ? IndentedItemType.POINT : IndentedItemType.PARAGRAPH, toParagraph ? IndentedItemType.PARAGRAPH : IndentedItemType.POINT, false);
        return originalItem;
    }

    // All methods related to conversion to a first subpoint or a first subparagraph
    public TableOfContentItemVO buildFirstElementFromElement(List<TocItem> tocItems,
                                                     TableOfContentItemVO originalItem,
                                                     int originalIndentLevel,
                                                     boolean restored, boolean notSameKind, boolean toParagraph) {
        removeTransformActionIfRestored(originalItem, restored);
        populateIndentInfoIfNotRestored(originalItem, originalIndentLevel, (notSameKind == toParagraph) ? IndentedItemType.POINT : IndentedItemType.PARAGRAPH, restored);

        // Ok, checking is done, convert the point
        TocItem subElementTocItem = StructureConfigUtils.getTocItemByName(tocItems, toParagraph ? SUBPARAGRAPH : SUBPOINT);
        TableOfContentItemVO firstSubelement = buildTransItemFromItem(originalItem);
        handleTransformActionForFirstElements(originalItem, firstSubelement, restored, true);

        firstSubelement.setTocItem(subElementTocItem);

        // Remove numbering
        removeNumbering(firstSubelement);

        // Add first child
        originalItem.addChildItem(0, firstSubelement);

        if (notSameKind) {
            TocItem parentTocItem = StructureConfigUtils.getTocItemByName(tocItems, toParagraph ? PARAGRAPH : POINT);
            originalItem.setTocItem(parentTocItem);
        }
        if (restored) {
            removeTransformPrefix(originalItem);
        }
        if (originalItem.getSoftActionAttr() != null && originalItem.getSoftActionAttr().equals(firstSubelement.getSoftActionAttr())) {
            firstSubelement.setSoftActionRoot(false);
        }
        tableOfContentProcessor.convertTocItemContent(originalItem, firstSubelement, (notSameKind == toParagraph) ? IndentedItemType.POINT : IndentedItemType.PARAGRAPH, toParagraph ? IndentedItemType.FIRST_SUBPARAGRAPH : IndentedItemType.FIRST_SUBPOINT, restored);
        return originalItem;
    }

    public TableOfContentItemVO buildFirstElementFromFirstElement(List<TocItem> tocItems,
                                                                 TableOfContentItemVO originalItem,
                                                                 int originalIndentLevel,
                                                                 boolean restored, boolean toParagraph) {
        TableOfContentItemVO firstSubelement;
        TocItem parentTocItem = StructureConfigUtils.getTocItemByName(tocItems, toParagraph ? PARAGRAPH : POINT);
        TocItem subElementTocItem = StructureConfigUtils.getTocItemByName(tocItems, toParagraph ? SUBPARAGRAPH : SUBPOINT);

        // If it has more than one child or does not contain the subpoint, conversion is not possible
        if (originalItem.getChildItems().isEmpty()) {
            return originalItem;
        } else {
            firstSubelement = originalItem.getChildItems().get(0);
            if (!getTagValueFromTocItemVo(firstSubelement).equals(toParagraph ? SUBPOINT : SUBPARAGRAPH)) {
                return originalItem;
            }
        }

        populateIndentInfoIfNotRestored(originalItem, originalIndentLevel, IndentedItemType.FIRST_SUBPARAGRAPH, restored);

        //Convert point to paragraph or the reverse
        originalItem.setTocItem(parentTocItem);
        firstSubelement.setTocItem(subElementTocItem);

        tableOfContentProcessor.convertTocItemContent(originalItem, firstSubelement, toParagraph ? IndentedItemType.FIRST_SUBPOINT : IndentedItemType.FIRST_SUBPARAGRAPH, toParagraph ? IndentedItemType.FIRST_SUBPARAGRAPH : IndentedItemType.FIRST_SUBPOINT, restored);
        return originalItem;
    }

    public TableOfContentItemVO buildFirstElementFromSubElement(List<TocItem> tocItems,
                                                         TableOfContentItemVO originalItem,
                                                         int originalIndentLevel,
                                                         boolean restored,
                                                         boolean notSameKind,
                                                         boolean toParagraph) {
        // Ok, checking is done, convert the subelement
        TocItem parentTocItem = StructureConfigUtils.getTocItemByName(tocItems, toParagraph ? PARAGRAPH : POINT);
        TableOfContentItemVO parent = buildTransItemFromItem(originalItem);
        parent.setTocItem(parentTocItem);

        populateAndCopyIndentInfo(parent, originalItem, originalIndentLevel, (toParagraph == notSameKind) ? IndentedItemType.OTHER_SUBPOINT : IndentedItemType.OTHER_SUBPARAGRAPH, true);
        handleTransformActionForFirstElements(parent, originalItem, restored, false);

        // Add first child
        int originalPosition = originalItem.getParentItem().getChildItems().indexOf(originalItem);

        originalItem.getParentItem().removeChildItem(originalItem);
        originalItem.getParentItem().addChildItem(originalPosition, parent);
        parent.addChildItem(0, originalItem);
        moveChildren(originalItem, parent);

        removeTransformPrefix(originalItem);
        if (notSameKind) {
            TocItem subElementTocItem = StructureConfigUtils.getTocItemByName(tocItems, toParagraph ? SUBPARAGRAPH : SUBPOINT);
            originalItem.setTocItem(subElementTocItem);
        }

        TableOfContentItemVO firstSubelement = originalItem;
        firstSubelement.resetIndentInfo();
        originalItem = parent;
        if (originalItem.getSoftActionAttr() != null && originalItem.getSoftActionAttr().equals(firstSubelement.getSoftActionAttr())) {
            firstSubelement.setSoftActionRoot(false);
        }
        tableOfContentProcessor.convertTocItemContent(originalItem, firstSubelement, (toParagraph == notSameKind) ? IndentedItemType.OTHER_SUBPOINT : IndentedItemType.OTHER_SUBPARAGRAPH, toParagraph ? IndentedItemType.FIRST_SUBPARAGRAPH : IndentedItemType.FIRST_SUBPOINT, restored);
        return originalItem;
    }

    public Pair<TableOfContentItemVO, Boolean> forceBuildFirstSubpointFromPoint(List<TocItem> tocItems, TableOfContentItemVO originalItem, int originalIndentLevel) {
        if (getTagValueFromTocItemVo(originalItem).equals(LEVEL)) {
            return new Pair<>(originalItem, false);
        }

        boolean restored = originalItem.isIndented() && originalItem.getIndentOriginType().equals(IndentedItemType.FIRST_SUBPOINT);
        originalItem = buildFirstElementFromElement(tocItems, originalItem, originalIndentLevel, restored, false, false);
        return new Pair<>(originalItem, true);
    }

    public Pair<TableOfContentItemVO, Boolean> forceBuildFirstSubparagraphFromParagraph(List<TocItem> tocItems, TableOfContentItemVO originalItem, int originalIndentLevel) {
        if (getTagValueFromTocItemVo(originalItem).equals(LEVEL)) {
            return new Pair<>(originalItem, false);
        }

        boolean restored = originalItem.isIndented() && originalItem.getIndentOriginType().equals(IndentedItemType.FIRST_SUBPARAGRAPH);
        originalItem = buildFirstElementFromElement(tocItems, originalItem, originalIndentLevel, restored, false, true);
        return new Pair<>(originalItem, true);
    }

    // All methods related to conversion to a subpoint or to a subparagraph
    public TableOfContentItemVO buildSubElementFromFirstElement(List<TocItem> tocItems,
                                                         TableOfContentItemVO originalItem,
                                                         int originalIndentLevel,
                                                         boolean restored, boolean notSameKind, boolean toParagraph) {
        TableOfContentItemVO firstSubelement;

        // If it has more than one child or does not contain the subpoint or subparagraph, conversion is not possible
        if (originalItem.getChildItems().isEmpty()) {
            return originalItem;
        } else {
            firstSubelement = originalItem.getChildItems().get(0);
            if (!getTagValueFromTocItemVo(firstSubelement).equals((notSameKind == toParagraph) ? SUBPOINT : SUBPARAGRAPH)) {
                return originalItem;
            }
        }

        restoreMoveFromAttrs(originalItem, firstSubelement);
        populateAndCopyIndentInfo(originalItem, firstSubelement, originalIndentLevel, (notSameKind == toParagraph) ? IndentedItemType.FIRST_SUBPOINT : IndentedItemType.FIRST_SUBPARAGRAPH, false);

        // Remove "transformed" prefix -> For comparaison, id must be the same
        removeTransformPrefix(firstSubelement);
        handleTransformAction(originalItem, firstSubelement, restored, false);

        // Ok, checking is done, remove the parent paragraph
        int originalPosition = originalItem.getParentItem().getChildItems().indexOf(originalItem);
        // Parent should be a paragraph here no need to check if it's a list
        originalItem.getParentItem().removeChildItem(originalItem);
        originalItem.removeChildItem(firstSubelement);
        originalItem.getParentItem().addChildItem(originalPosition, firstSubelement);

        if (notSameKind) {
            TocItem subElementTocItem = StructureConfigUtils.getTocItemByName(tocItems, toParagraph ? SUBPARAGRAPH : SUBPOINT);
            firstSubelement.setTocItem(subElementTocItem);
        }

        copySoftActionToDestConvertItem(originalItem, firstSubelement);
        originalItem = firstSubelement;

        tableOfContentProcessor.convertTocItemContent(originalItem, null, (notSameKind == toParagraph) ? IndentedItemType.FIRST_SUBPOINT : IndentedItemType.FIRST_SUBPARAGRAPH, toParagraph ? IndentedItemType.OTHER_SUBPARAGRAPH : IndentedItemType.OTHER_SUBPOINT, restored);
        return originalItem;
    }

    public TableOfContentItemVO buildSubElementFromElement(List<TocItem> tocItems,
                                                    TableOfContentItemVO originalItem,
                                                    int originalIndentLevel,
                                                    boolean restored, boolean notSameKind, boolean toParagraph) {
        removeTransformActionIfRestored(originalItem, restored);
        populateIndentInfoIfNotRestored(originalItem, originalIndentLevel, (notSameKind == toParagraph) ? IndentedItemType.POINT : IndentedItemType.PARAGRAPH, restored);

        // Ok, checking is done, convert the element
        TocItem subelementTocItem = StructureConfigUtils.getTocItemByName(tocItems, toParagraph ? SUBPARAGRAPH : SUBPOINT);
        originalItem.setTocItem(subelementTocItem);
        // Remove numbering
        removeNumbering(originalItem);
        // Remove "transformed" prefix -> For comparaison, id must be the same
        removeTransformPrefix(originalItem);

        tableOfContentProcessor.convertTocItemContent(originalItem, null, (notSameKind == toParagraph) ? IndentedItemType.POINT : IndentedItemType.PARAGRAPH, toParagraph ? IndentedItemType.OTHER_SUBPARAGRAPH : IndentedItemType.OTHER_SUBPOINT, restored);
        return originalItem;
    }

    public TableOfContentItemVO buildSubElementFromSubElement(List<TocItem> tocItems,
                                                       TableOfContentItemVO originalItem,
                                                       int originalIndentLevel,
                                                       boolean restored, boolean toParagraph) {
        removeTransformActionIfRestored(originalItem, restored);
        populateIndentInfoIfNotRestored(originalItem, originalIndentLevel, toParagraph ? IndentedItemType.OTHER_SUBPOINT : IndentedItemType.OTHER_SUBPARAGRAPH, restored);

        // Ok, checking is done, convert the sub element
        TocItem subElementTocItem = StructureConfigUtils.getTocItemByName(tocItems, toParagraph ? SUBPARAGRAPH : SUBPOINT);
        originalItem.setTocItem(subElementTocItem);

        // Remove "transformed" prefix -> For comparaison, id must be the same
        removeTransformPrefix(originalItem);
        tableOfContentProcessor.convertTocItemContent(originalItem, null, toParagraph ? IndentedItemType.OTHER_SUBPOINT : IndentedItemType.OTHER_SUBPARAGRAPH, toParagraph ? IndentedItemType.OTHER_SUBPARAGRAPH : IndentedItemType.OTHER_SUBPOINT, restored);
        return originalItem;
    }

    private void removeTransformPrefix(TableOfContentItemVO item) {
        if (item.getId().startsWith(XmlHelper.SOFT_TRANSFORM_PLACEHOLDER_ID_PREFIX) && hasTocItemSoftOrigin(item, EC)) {
            item.setId(item.getId().substring(XmlHelper.SOFT_TRANSFORM_PLACEHOLDER_ID_PREFIX.length()));
            if (hasTocItemSoftAction(item, TRANSFORM)) {
                item.setSoftActionAttr(null);
                item.setSoftActionRoot(null);
                item.setSoftUserAttr(null);
                item.setSoftDateAttr(null);
            }
        }
        if (item.getId().startsWith(INDENT_PLACEHOLDER_ID_PREFIX) && hasTocItemSoftOrigin(item, CN)) {
            item.setId(item.getId().substring(INDENT_PLACEHOLDER_ID_PREFIX.length()));
        }
    }

    private void addTransformPrefix(TableOfContentItemVO item) {
        if (!item.getId().startsWith(XmlHelper.SOFT_TRANSFORM_PLACEHOLDER_ID_PREFIX)
                && !item.getId().startsWith(XmlHelper.SOFT_MOVE_PLACEHOLDER_ID_PREFIX)
                && hasTocItemSoftOrigin(item, EC)) {
            item.setId(XmlHelper.SOFT_TRANSFORM_PLACEHOLDER_ID_PREFIX + item.getId());
            if (item.getSoftActionAttr() == null) {
                item.setSoftActionAttr(TRANSFORM);
                item.setSoftActionRoot(true);
            }
        } else if (!item.getId().startsWith(INDENT_PLACEHOLDER_ID_PREFIX) && hasTocItemSoftOrigin(item, CN)) {
            item.setId(INDENT_PLACEHOLDER_ID_PREFIX + item.getId());
        }
    }

    public TableOfContentItemVO buildTransItemFromItem(TableOfContentItemVO originalItem) {
        TableOfContentItemVO transItem;
        if (hasTocItemSoftOrigin(originalItem, EC)) {
            if (originalItem.getId().startsWith(XmlHelper.SOFT_TRANSFORM_PLACEHOLDER_ID_PREFIX)
                    || originalItem.getId().startsWith(XmlHelper.SOFT_MOVE_PLACEHOLDER_ID_PREFIX)) {
                transItem = new TableOfContentItemVO(originalItem.getTocItem(), originalItem.getId(), originalItem.getOriginAttr(), originalItem.getNumber(), originalItem.getOriginNumAttr(), originalItem.getHeading(),
                        originalItem.getNode(), null, originalItem.getContent(), originalItem.getSoftActionAttr(), originalItem.isSoftActionRoot(), originalItem.getSoftUserAttr(), originalItem.getSoftDateAttr(),
                        originalItem.getSoftMoveFrom(), originalItem.getSoftMoveTo(), originalItem.getSoftTransFrom(), originalItem.isUndeleted(), originalItem.getNumSoftActionAttr());
            } else {
                transItem = new TableOfContentItemVO(originalItem.getTocItem(), XmlHelper.SOFT_TRANSFORM_PLACEHOLDER_ID_PREFIX + originalItem.getId(), originalItem.getOriginAttr(), originalItem.getNumber(), originalItem.getOriginNumAttr(), originalItem.getHeading(),
                        originalItem.getNode(), null, originalItem.getContent(), originalItem.getSoftActionAttr(), originalItem.isSoftActionRoot(), originalItem.getSoftUserAttr(), originalItem.getSoftDateAttr(),
                        originalItem.getSoftMoveFrom(), originalItem.getSoftMoveTo(), originalItem.getSoftTransFrom(), originalItem.isUndeleted(), originalItem.getNumSoftActionAttr());
            }
        } else {
            if (originalItem.getId().startsWith(INDENT_PLACEHOLDER_ID_PREFIX)) {
                transItem = new TableOfContentItemVO(originalItem.getTocItem(), originalItem.getId(), originalItem.getOriginAttr(), originalItem.getNumber(), originalItem.getOriginNumAttr(), originalItem.getHeading(),
                        originalItem.getNode(), null, originalItem.getContent(), originalItem.getSoftActionAttr(), originalItem.isSoftActionRoot(), originalItem.getSoftUserAttr(), originalItem.getSoftDateAttr(),
                        originalItem.getSoftMoveFrom(), originalItem.getSoftMoveTo(), originalItem.getSoftTransFrom(), originalItem.isUndeleted(), originalItem.getNumSoftActionAttr());
            } else {
                transItem = new TableOfContentItemVO(originalItem.getTocItem(), INDENT_PLACEHOLDER_ID_PREFIX + originalItem.getId(), originalItem.getOriginAttr(), originalItem.getNumber(), originalItem.getOriginNumAttr(), originalItem.getHeading(),
                        originalItem.getNode(), null, originalItem.getContent(), originalItem.getSoftActionAttr(), originalItem.isSoftActionRoot(), originalItem.getSoftUserAttr(), originalItem.getSoftDateAttr(),
                        originalItem.getSoftMoveFrom(), originalItem.getSoftMoveTo(), originalItem.getSoftTransFrom(), originalItem.isUndeleted(), originalItem.getNumSoftActionAttr());
            }
        }

        if (transItem.getSoftActionAttr() == null && hasTocItemSoftOrigin(originalItem, EC)) {
            transItem.setSoftActionAttr(TRANSFORM);
            transItem.setSoftActionRoot(true);
        }
        return transItem;
    }

    public int getIndentedItemIndentLevel(TableOfContentItemVO item) {
        return getItemIndentLevel(item, 0);
    }

    int getItemIndentLevel(TableOfContentItemVO item, int startingDepth) {
        TableOfContentItemVO parent = item.getParentItem();
        while (parent != null) {
            if (ArrayUtils.contains(NUMBERED_AND_LEVEL_ITEMS, TableOfContentProcessor.getTagValueFromTocItemVo(parent))) {
                startingDepth++;
            }
            parent = parent.getParentItem();
        }

        return startingDepth;
    }

    private void moveChildren(TableOfContentItemVO source, TableOfContentItemVO target) {
        List<TableOfContentItemVO> children = new ArrayList<>();
        children.addAll(source.getChildItems());

        for (TableOfContentItemVO child : children) {
            source.removeChildItem(child);
            target.addChildItem(child);
        }
    }

    // While converting to a unumbered element, remove number
    private void removeNumbering(TableOfContentItemVO item) {
        item.setNumber(null);
        item.setOriginNumAttr(null);
    }

    // Restore to its original state, thus, it has been transformed before, reset it
    private void removeTransformActionIfRestored(TableOfContentItemVO item, boolean restored) {
        if (restored && hasTocItemSoftAction(item, TRANSFORM)) {
            item.setSoftActionAttr(null);
            item.setSoftActionRoot(null);
            item.setSoftUserAttr(null);
            item.setSoftDateAttr(null);
        }
    }

    // Populate indent information on non restored items
    private void populateIndentInfoIfNotRestored(TableOfContentItemVO item, int originalIndentLevel, IndentedItemType originatIndentedItemType, boolean restored) {
        if (!restored) {
            item.populateIndentInfo(originatIndentedItemType, originalIndentLevel
                    , item.getElementNumberId(), item.getNumber(), item.getOriginNumAttr());
        }
    }

    // Copy indent information (Used while converting to a first subparagraph or a first subpoint)
    private void populateAndCopyIndentInfo(TableOfContentItemVO parentItem, TableOfContentItemVO subElement, int originalIndentLevel, IndentedItemType originalIndentedItemType, boolean toNumbered) {
        if (!toNumbered) {
            if (parentItem.isIndented()) {
                subElement.populateIndentInfo(parentItem.getIndentOriginType(), parentItem.getIndentOriginIndentLevel()
                        , parentItem.getIndentOriginNumId(), parentItem.getIndentOriginNumValue(), parentItem.getIndentOriginNumOrigin());
            } else if (!subElement.isIndented()) {
                subElement.populateIndentInfo(originalIndentedItemType, originalIndentLevel
                        , parentItem.getElementNumberId(), parentItem.getNumber(), parentItem.getOriginNumAttr());
            }
        } else {
            if (!subElement.isIndented()) {
                parentItem.populateIndentInfo(originalIndentedItemType, originalIndentLevel
                        , subElement.getElementNumberId(), subElement.getNumber(), subElement.getOriginNumAttr());
            } else if (subElement.isIndented()) {
                parentItem.populateIndentInfo(subElement.getIndentOriginType(), subElement.getIndentOriginIndentLevel()
                        , subElement.getIndentOriginNumId(), subElement.getIndentOriginNumValue(), subElement.getIndentOriginNumOrigin());
            }
        }
    }

    // While converting a first subpoint or a first subparagraph, it needs to handle the transform action attributes
    private void handleTransformAction(TableOfContentItemVO parentItem, TableOfContentItemVO subElement, boolean restored, boolean toNumbered) {
        if (!restored && (IndentedItemType.FIRST_SUBPOINT.equals(subElement.getIndentOriginType())
                || IndentedItemType.FIRST_SUBPARAGRAPH.equals(subElement.getIndentOriginType())
                || IndentedItemType.FIRST_SUBPOINT.equals(parentItem.getIndentOriginType())
                || IndentedItemType.FIRST_SUBPARAGRAPH.equals(parentItem.getIndentOriginType()))) {
            if (!toNumbered) {
                subElement.setSoftTransFrom(subElement.getId());
                removeTransformPrefix(parentItem);
                subElement.setId(parentItem.getId());
                if (subElement.getSoftActionAttr() == null && hasTocItemSoftOrigin(subElement, EC)) {
                    subElement.setSoftActionAttr(TRANSFORM);
                    subElement.setSoftActionRoot(true);
                }
            } else {
                parentItem.setSoftTransFrom(subElement.getId());
                if (parentItem.getSoftActionAttr() == null && hasTocItemSoftOrigin(parentItem, EC)) {
                    parentItem.setSoftActionAttr(TRANSFORM);
                    parentItem.setSoftActionRoot(true);
                }
            }
        }
    }

    // Handle transform action attributes while converting to first subpoint or first subparagraph
    private void handleTransformActionForFirstElements(TableOfContentItemVO parentItem, TableOfContentItemVO subElement, boolean restored, boolean fromNumbered) {
        if (restored) {
            if (hasTocItemSoftAction(subElement, TRANSFORM)) {
                subElement.setSoftActionAttr(null);
                subElement.setSoftActionRoot(null);
                subElement.setSoftUserAttr(null);
                subElement.setSoftDateAttr(null);
            }
            if (hasTocItemSoftAction(parentItem, TRANSFORM)) {
                parentItem.setSoftActionAttr(null);
                parentItem.setSoftActionRoot(null);
                parentItem.setSoftUserAttr(null);
                parentItem.setSoftDateAttr(null);
            }
            if (fromNumbered) {
                if (parentItem.getSoftTransFrom() != null) {
                    subElement.setId(parentItem.getSoftTransFrom());
                    subElement.setSoftTransFrom(null);
                    if (hasTocItemSoftAction(parentItem, MOVE_FROM)) {
                        subElement.setSoftMoveFrom(SOFT_MOVE_PLACEHOLDER_ID_PREFIX + parentItem.getSoftTransFrom());
                    }
                    parentItem.setSoftTransFrom(null);
                }
            } else {
                if (subElement.getSoftTransFrom() != null) {
                    subElement.setId(subElement.getSoftTransFrom());
                    if (hasTocItemSoftAction(subElement, MOVE_FROM)) {
                        subElement.setSoftMoveFrom(SOFT_MOVE_PLACEHOLDER_ID_PREFIX + subElement.getSoftTransFrom());
                    }
                    subElement.setSoftTransFrom(null);
                    parentItem.setSoftTransFrom(null);
                    removeTransformPrefix(parentItem);
                }
            }
        } else {
            if ((parentItem.getIndentOriginType().equals(IndentedItemType.FIRST_SUBPARAGRAPH) || parentItem.getIndentOriginType().equals(IndentedItemType.FIRST_SUBPOINT))
                    && parentItem.getSoftTransFrom() != null) {
                removeTransformPrefix(parentItem);
                subElement.setId(parentItem.getSoftTransFrom());
            } else {
                removeTransformPrefix(subElement);
                addTransformPrefix(parentItem);
            }
        }
    }

    // Copy DELETE or MOVE_TO soft action attributes to destination convertion item
    // These elements cannot be directly indented, this is happening only for following convertions:
    // 1. First subpoint to point
    // 2. First subparagraph to paragraph
    // 3. Point to first subpoint
    // 4. Paragraph to first subparagraph
    private void copySoftActionToDestConvertItem(TableOfContentItemVO sourceItem, TableOfContentItemVO destItem) {
        if (hasTocItemSoftAction(sourceItem, MOVE_TO) || hasTocItemSoftAction(sourceItem, DELETE)) {
            destItem.setSoftActionRoot(sourceItem.isSoftActionRoot());
            if (!sourceItem.getSoftActionAttr().equals(destItem.getSoftActionAttr())) {
                destItem.setSoftActionAttr(sourceItem.getSoftActionAttr());
                destItem.setSoftMoveTo(sourceItem.getSoftMoveTo());
                destItem.setSoftDateAttr(sourceItem.getSoftDateAttr());
                destItem.setSoftUserAttr(sourceItem.getSoftUserAttr());
            }
        }
    }

    // Copy MOVE_FROM soft action attributes to destination convertion item
    private void restoreMoveFromAttrs(TableOfContentItemVO sourceItem, TableOfContentItemVO destItem) {
        if (hasTocItemSoftAction(sourceItem, MOVE_FROM) && destItem.getSoftActionAttr() == null) {
            destItem.setSoftActionRoot(sourceItem.isSoftActionRoot());
            destItem.setSoftActionAttr(sourceItem.getSoftActionAttr());
            destItem.setSoftMoveFrom(sourceItem.getSoftMoveFrom());
            destItem.setSoftDateAttr(sourceItem.getSoftDateAttr());
            destItem.setSoftUserAttr(sourceItem.getSoftUserAttr());
        }
    }
}
