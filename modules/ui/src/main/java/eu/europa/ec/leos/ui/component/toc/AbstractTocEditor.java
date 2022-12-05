/*
 * Copyright 2019 European Commission
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
package eu.europa.ec.leos.ui.component.toc;

import com.vaadin.data.TreeData;
import com.vaadin.data.provider.HierarchicalQuery;
import com.vaadin.data.provider.TreeDataProvider;
import com.vaadin.ui.TreeGrid;
import eu.europa.ec.leos.model.action.ActionType;
import eu.europa.ec.leos.model.action.SoftActionType;
import eu.europa.ec.leos.services.processor.content.TableOfContentHelper;
import eu.europa.ec.leos.vo.toc.AknTag;
import eu.europa.ec.leos.vo.toc.NumberingConfig;
import eu.europa.ec.leos.vo.toc.OptionsType;
import eu.europa.ec.leos.vo.toc.StructureConfigUtils;
import eu.europa.ec.leos.vo.toc.TableOfContentItemVO;
import eu.europa.ec.leos.vo.toc.TocItem;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static eu.europa.ec.leos.model.action.SoftActionType.DELETE;
import static eu.europa.ec.leos.model.action.SoftActionType.MOVE_FROM;
import static eu.europa.ec.leos.model.action.SoftActionType.MOVE_TO;
import static eu.europa.ec.leos.model.action.SoftActionType.SPLITTED;
import static eu.europa.ec.leos.model.action.SoftActionType.UNDELETE;
import static eu.europa.ec.leos.services.processor.content.TableOfContentHelper.getTocItemChildPosition;
import static eu.europa.ec.leos.services.processor.content.TableOfContentHelper.hasTocItemSoftAction;
import static eu.europa.ec.leos.services.support.XmlHelper.CLAUSE;
import static eu.europa.ec.leos.services.support.XmlHelper.CN;
import static eu.europa.ec.leos.services.support.XmlHelper.CROSSHEADING;
import static eu.europa.ec.leos.services.support.XmlHelper.DIVISION;
import static eu.europa.ec.leos.services.support.XmlHelper.EC;
import static eu.europa.ec.leos.services.support.XmlHelper.INDENT;
import static eu.europa.ec.leos.services.support.XmlHelper.LEVEL;
import static eu.europa.ec.leos.services.support.XmlHelper.LIST;
import static eu.europa.ec.leos.services.support.XmlHelper.PARAGRAPH;
import static eu.europa.ec.leos.services.support.XmlHelper.POINT;
import static eu.europa.ec.leos.services.support.XmlHelper.SOFT_DELETE_PLACEHOLDER_ID_PREFIX;
import static eu.europa.ec.leos.services.support.XmlHelper.SOFT_MOVE_PLACEHOLDER_ID_PREFIX;
import static eu.europa.ec.leos.services.support.XmlHelper.SUBPARAGRAPH;
import static eu.europa.ec.leos.services.processor.content.TableOfContentProcessor.getTagValueFromTocItemVo;

public abstract class AbstractTocEditor implements TocEditor {
    protected static final String TEMP_PREFIX = "temp_";

    protected TocDropResult validateAction(final TreeGrid<TableOfContentItemVO> tocTree, final Map<TocItem, List<TocItem>> tableOfContentRules,
                                           final List<TableOfContentItemVO> droppedItems, final TableOfContentItemVO targetItem, final ItemPosition position) {

        TocDropResult result = new TocDropResult(true, "toc.edit.window.drop.success.message",
                droppedItems.get(0), targetItem);
        TableOfContentItemVO parentItem = tocTree.getTreeData().getParent(targetItem);
        for (TableOfContentItemVO sourceItem : droppedItems) {
            if (isItemDroppedOnSameTarget(result, sourceItem, targetItem) ||
                    !validateAddingItemAsChildOrSibling(result, sourceItem, targetItem, tocTree, tableOfContentRules,
                            parentItem, position)) {
                return result;
            }
        }
        return result;
    }

    @Override
    public void undeleteItem(TreeGrid<TableOfContentItemVO> tocTree, TableOfContentItemVO tableOfContentItemVO) {
        TreeData<TableOfContentItemVO> treeData = tocTree.getTreeData();
        TableOfContentItemVO tempDeletedItem = copyDeletedItemToTempForUndelete(tableOfContentItemVO);
        dropItemAtOriginalPosition(tableOfContentItemVO, tempDeletedItem, treeData);
        treeData.removeItem(tableOfContentItemVO);
        tableOfContentItemVO.getParentItem().removeChildItem(tableOfContentItemVO);

        TableOfContentItemVO softDeletedItem = copyTempItemToFinalItem(tempDeletedItem);
        dropItemAtOriginalPosition(tempDeletedItem, softDeletedItem, treeData);
        treeData.removeItem(tempDeletedItem);
        tempDeletedItem.getParentItem().removeChildItem(tempDeletedItem);

        tocTree.getDataProvider().refreshAll();
        tocTree.deselectAll();
    }

    @Override
    public boolean isDeletedItem(TableOfContentItemVO tableOfContentItemVO) {
        return DELETE.equals(tableOfContentItemVO.getSoftActionAttr());
    }

    @Override
    public boolean isMoveToItem(TableOfContentItemVO tableOfContentItemVO) {
        return MOVE_TO.equals(tableOfContentItemVO.getSoftActionAttr());
    }

    @Override
    public boolean isUndeletableItem(TableOfContentItemVO tableOfContentItemVO) {
        return ((tableOfContentItemVO.getParentItem() != null) && !isMoveToItem(tableOfContentItemVO) && !(DELETE.equals(tableOfContentItemVO.getParentItem().getSoftActionAttr())));
    }

    private TableOfContentItemVO copyDeletedItemToTempForUndelete(TableOfContentItemVO originalItem){
        TableOfContentItemVO tempDeletedItem;
        if (DELETE.equals(originalItem.getSoftActionAttr())) {
            tempDeletedItem = new TableOfContentItemVO(originalItem.getTocItem(), TEMP_PREFIX + originalItem.getId().replace(SOFT_DELETE_PLACEHOLDER_ID_PREFIX, ""),
                    originalItem.getOriginAttr(), originalItem.getNumber(),
                    EC, originalItem.getHeading(),
                    originalItem.getNode(), originalItem.getList(), originalItem.getContent(),
                    UNDELETE, null, null, null,
                    null, null, null, true, originalItem.getNumSoftActionAttr());
        } else {
            tempDeletedItem = new TableOfContentItemVO(originalItem.getTocItem(), TEMP_PREFIX +originalItem.getId(),
                    originalItem.getOriginAttr(), originalItem.getNumber(),
                    originalItem.getOriginNumAttr(), originalItem.getHeading(),
                    originalItem.getNode(), originalItem.getList(), originalItem.getContent(),
                    originalItem.getSoftActionAttr(), originalItem.isSoftActionRoot(), originalItem.getSoftUserAttr(), originalItem.getSoftDateAttr(),
                    originalItem.getSoftMoveFrom(), originalItem.getSoftMoveTo(), originalItem.getSoftTransFrom(), originalItem.isUndeleted(), originalItem.getNumSoftActionAttr());
        }

        tempDeletedItem.setContent(originalItem.getContent());
        tempDeletedItem.setItemDepth(originalItem.getItemDepth());
        tempDeletedItem.setOriginalDepth(originalItem.getItemDepth());
        originalItem.getChildItems().forEach(child -> tempDeletedItem.addChildItem(copyDeletedItemToTempForUndelete(child)));
        return tempDeletedItem;
    }

    private boolean isItemDroppedOnSameTarget(final TocDropResult result, final TableOfContentItemVO sourceItem,
                                              final TableOfContentItemVO targetItem) {
        if (sourceItem.equals(targetItem)) {
            result.setSuccess(false);
            result.setMessageKey("toc.edit.window.drop.error.same.item.message");
            result.setSourceItem(sourceItem);
            result.setTargetItem(targetItem);
            return true;
        }
        return false;
    }

    private boolean validateAddingItemAsChildOrSibling(final TocDropResult result, final TableOfContentItemVO sourceItem,
                                                       final TableOfContentItemVO targetItem,
                                                       final TreeGrid<TableOfContentItemVO> tocTree, final Map<TocItem, List<TocItem>> tableOfContentRules,
                                                       final TableOfContentItemVO parentItem, final ItemPosition position) {

        TocItem targetTocItem = targetItem.getTocItem();
        List<TocItem> targetTocItems = tableOfContentRules.get(targetTocItem);
        if (isSourceDivision(sourceItem) || isCrossheading(sourceItem) || isDroppedOnPointOrIndent(sourceItem, targetItem) || getTagValueFromTocItemVo(sourceItem).
                equals(getTagValueFromTocItemVo(targetItem))) {
            TableOfContentItemVO actualTargetItem = getActualTargetItem(sourceItem, targetItem, parentItem, position, true);
            return validateAddingToActualTargetItem(result, sourceItem, targetItem, tocTree, tableOfContentRules, actualTargetItem, position);
        } else if (targetTocItems != null && targetTocItems.size() > 0 && targetTocItems.contains(sourceItem.getTocItem())) {
            //If target item type is root, source item will be added as child, else validate dropping item at dragged location
            TableOfContentItemVO actualTargetItem = getActualTargetItem(sourceItem, targetItem, parentItem, position, false);
            return targetTocItem.isRoot() || validateAddingToActualTargetItem(result, sourceItem, targetItem, tocTree, tableOfContentRules, actualTargetItem, position);
        } else { // If child elements not allowed in target validate adding it to its parent
            return validateAddingItemAsSibling(result, sourceItem, targetItem, tocTree, tableOfContentRules, parentItem, position);
        }
    }

    private boolean validateAddingItemAsSibling(final TocDropResult result, final TableOfContentItemVO sourceItem,
                                                final TableOfContentItemVO targetItem,
                                                final TreeGrid<TableOfContentItemVO> tocTree, final Map<TocItem, List<TocItem>> tableOfContentRules,
                                                final TableOfContentItemVO parentItem, final ItemPosition position) {

        TableOfContentItemVO actualTargetItem = getActualTargetItem(sourceItem, targetItem, parentItem, position, true);
        return validateAddingToActualTargetItem(result, sourceItem, targetItem, tocTree, tableOfContentRules, actualTargetItem, position);
    }

    private boolean validateParentAndSourceTypeCompatibility(final TocDropResult result, final TableOfContentItemVO sourceItem, final TableOfContentItemVO parentItem,
                                                             final TocItem parentTocItem, final List<TocItem> parentTocItems) {

        if (parentTocItems == null || parentTocItems.size() == 0 || !parentTocItems.contains(sourceItem.getTocItem())
                || !sourceItem.getTocItem().isSameParentAsChild() && parentTocItem.getAknTag().value().equals(sourceItem.getTocItem().getAknTag().value())){
            result.setSuccess(false);
            result.setMessageKey("toc.edit.window.drop.error.message");
            result.setSourceItem(sourceItem);
            result.setTargetItem(parentItem);
            return false;
        }
        return true;
    }

    protected boolean validateAddingToActualTargetItem(final TocDropResult result, final TableOfContentItemVO sourceItem, final TableOfContentItemVO targetItem,
                                                       final TreeGrid<TableOfContentItemVO> tocTree, final Map<TocItem, List<TocItem>> tableOfContentRules,
                                                       final TableOfContentItemVO actualTargetItem, final ItemPosition position) {

        TocItem parentTocItem = actualTargetItem != null ? actualTargetItem.getTocItem() : null;
        List<TocItem> parentTocItems = tableOfContentRules.get(parentTocItem);
        boolean parentAndSourceTypeCompatible = validateParentAndSourceTypeCompatibility(result, sourceItem, actualTargetItem, parentTocItem, parentTocItems);
        boolean validAddingToItem = validateAddingToItem(result, sourceItem, targetItem, tocTree, actualTargetItem, position);
        boolean maxDepthReached = validateMaxDepth(result, sourceItem, targetItem);
        return parentAndSourceTypeCompatible && validAddingToItem && !maxDepthReached;
    }

    protected abstract boolean validateAddingToItem(final TocDropResult result, final TableOfContentItemVO sourceItem, final TableOfContentItemVO targetItem,
                                                    final TreeGrid<TableOfContentItemVO> tocTree, final TableOfContentItemVO actualTargetItem, final ItemPosition position);

    protected boolean validateMaxDepth(final TocDropResult result, final TableOfContentItemVO sourceItem, final TableOfContentItemVO targetItem) {
        if (targetItem.getTocItem().getMaxDepth() != null) {
            int maxDepthRule = Integer.valueOf(targetItem.getTocItem().getMaxDepth());
            if (maxDepthRule > 0 && targetItem.getItemDepth() >= maxDepthRule) {
                result.setSuccess(false);
                result.setMessageKey("toc.edit.window.drop.error.depth.message");
                result.setSourceItem(sourceItem);
                result.setTargetItem(targetItem);
                return false;
            }
        }
        return true;
    }

    protected void performAddOrMoveAction(final boolean isAdd, final TreeGrid<TableOfContentItemVO> tocTree, final Map<TocItem, List<TocItem>> tableOfContentRules,
                                          final TableOfContentItemVO sourceItem, final TableOfContentItemVO targetItem, final TableOfContentItemVO parentItem, final ItemPosition position) {

        if (targetItem.getTocItem().isChildrenAllowed()) {
            TocItem targetTocItem = targetItem.getTocItem();
            List<TocItem> targetTocAllowedItems = tableOfContentRules.get(targetTocItem);
            if (isSourceDivision(sourceItem) || isCrossheading(sourceItem) || isDroppedOnPointOrIndent(sourceItem, targetItem) || getTagValueFromTocItemVo(sourceItem).equals(getTagValueFromTocItemVo(targetItem))
                    || !(targetTocAllowedItems != null && targetTocAllowedItems.size() > 0 && targetTocAllowedItems.contains(sourceItem.getTocItem()))) {
                // If items have the same type or if child elements are not allowed in target add it to its parent
                TableOfContentItemVO actualTargetItem = getActualTargetItem(sourceItem, targetItem, parentItem, position, true);
                addOrMoveItem(isAdd, sourceItem, targetItem, tocTree, actualTargetItem, position);
            } else if (!targetTocItem.isRoot()) {
                // item is dropped at dragged location
                TableOfContentItemVO actualTargetItem = getActualTargetItem(sourceItem, targetItem, parentItem, position, false);
                addOrMoveItem(isAdd, sourceItem, targetItem, tocTree, actualTargetItem, position);
            } else {
                //If target item type is root, source item will be added as child before clause item if exists
                if (targetItem.containsItem(CLAUSE)) {
                    TableOfContentItemVO clauseItem = targetItem.getChildItems().stream()
                            .filter(x -> x.getTocItem().getAknTag().value().equals(CLAUSE)).findFirst().orElse(null);
                    TableOfContentItemVO actualTargetItem = getActualTargetItem(sourceItem, clauseItem, clauseItem.getParentItem(), ItemPosition.BEFORE, true);
                    addOrMoveItem(isAdd, sourceItem, clauseItem, tocTree, actualTargetItem, ItemPosition.BEFORE);
                } else {
                    addOrMoveItem(isAdd, sourceItem, targetItem, tocTree, targetItem, ItemPosition.AS_CHILDREN);
                }
            }
        } else {
            TableOfContentItemVO actualTargetItem = getActualTargetItem(sourceItem, targetItem, parentItem, position, true);
            addOrMoveItem(isAdd, sourceItem, targetItem, tocTree, actualTargetItem, position);
        }
    }

    protected void addOrMoveItem(final boolean isAdd, final TableOfContentItemVO sourceItem, final TableOfContentItemVO targetItem,
                                 final TreeGrid<TableOfContentItemVO> tocTree, final TableOfContentItemVO actualTargetItem, final ItemPosition position) {

        if (isAdd) {
            tocTree.getTreeData().addItem(null, sourceItem);
            if (actualTargetItem == null) {
                sourceItem.setParentItem(null);
                sourceItem.setItemDepth(1);
            }
        } else if (sourceItem.getParentItem() != null) {
            sourceItem.getParentItem().removeChildItem(sourceItem);
            sourceItem.setOriginalDepth(sourceItem.getItemDepth());
        }

        if (actualTargetItem != null) {
            tocTree.getTreeData().setParent(sourceItem, actualTargetItem);
            sourceItem.setParentItem(actualTargetItem);
            if (!actualTargetItem.equals(targetItem)) {
                int indexSiblings = actualTargetItem.getChildItems().indexOf(targetItem);
                tocTree.getTreeData().moveAfterSibling(sourceItem, targetItem);
                if (ItemPosition.BEFORE == position) {
                    tocTree.getTreeData().moveAfterSibling(targetItem, sourceItem);
                    actualTargetItem.getChildItems().add(indexSiblings, sourceItem);
                } else {
                    actualTargetItem.getChildItems().add(indexSiblings + 1, sourceItem);
                }
            } else if (actualTargetItem.equals(targetItem) && LEVEL.equals(sourceItem.getTocItem().getAknTag().value())) {
                /*
                 * This else is when we add level as child or after a Part, Title, Chapter or Section,
                 * because in this case the actualTargetItem is equal to targetItem, and we need to set
                 * the level as the first of list of children
                 */
                tocTree.getTreeData().moveAfterSibling(sourceItem, null);
                actualTargetItem.getChildItems().add(0, sourceItem);
            } else {
                actualTargetItem.getChildItems().add(sourceItem);
            }
        }
        setItemDepth(sourceItem, targetItem, position);
        setItemLevel(sourceItem, targetItem, position);
    }

    protected boolean isInList(TableOfContentItemVO sourceItem) {
        TableOfContentItemVO parent = sourceItem.getParentItem();
        if (parent != null) {
            if (getTagValueFromTocItemVo(parent).equalsIgnoreCase(LIST)) {
                return true;
            }
            for (TableOfContentItemVO item : parent.getChildItemsView()) {
                if (getTagValueFromTocItemVo(item).equalsIgnoreCase(POINT)
                        || getTagValueFromTocItemVo(item).equalsIgnoreCase(INDENT)) {
                    return true;
                }
            }
        }

        return false;
    }

    protected void handleLevelMove(TableOfContentItemVO sourceItem, TableOfContentItemVO targetItem) {

        // when moving back a LEVEL restore the initial depth
        if(LEVEL.equals(sourceItem.getTocItem().getAknTag().value()) && LEVEL.equals(targetItem.getTocItem().getAknTag().value())) {
            sourceItem.setItemDepth(targetItem.getItemDepth());
        }

    }

    protected TableOfContentItemVO getActualTargetItem(final TableOfContentItemVO sourceItem, final TableOfContentItemVO targetItem, final TableOfContentItemVO parentItem,
                                                       final ItemPosition position, boolean isTocItemSibling) {

        switch (position) {
            case AS_CHILDREN:
                if ((isTocItemSibling && !targetItem.getTocItem().isSameParentAsChild() && !isCrossheading(sourceItem)
                        || (targetItem.getTocItem().isSameParentAsChild() && targetItem.containsItem(LIST)))
                        || (targetItem.getId().equals(SOFT_MOVE_PLACEHOLDER_ID_PREFIX + sourceItem.getId()))
                        || (getTagValueFromTocItemVo(targetItem).equalsIgnoreCase(SUBPARAGRAPH) && isCrossheading(sourceItem))) {
                    return parentItem;
                } else if (!sourceItem.equals(targetItem)) {
                    return targetItem;
                }
                break;
            case BEFORE:
                return parentItem != null ? parentItem : targetItem;
            case AFTER:
                return isTocItemSibling ? parentItem : targetItem;
        }
        return null;
    }

    private boolean isDroppedOnPointOrIndent(TableOfContentItemVO sourceItem, TableOfContentItemVO targetItem) {
        String sourceTagValue = getTagValueFromTocItemVo(sourceItem);
        String targetTagValue = getTagValueFromTocItemVo(targetItem);
        return (sourceTagValue.equalsIgnoreCase(CROSSHEADING) || sourceTagValue.equals(POINT) || sourceTagValue.equals(INDENT)) && (targetTagValue.equals(POINT) || targetTagValue.equals(INDENT));
    }

    private boolean isSourceDivision(TableOfContentItemVO sourceItem) {
        String sourceTagValue = getTagValueFromTocItemVo(sourceItem);
        return sourceTagValue.equals(DIVISION);
    }

    private boolean isCrossheading(TableOfContentItemVO sourceItem) {
        String sourceTagValue = getTagValueFromTocItemVo(sourceItem);
        return sourceTagValue.equalsIgnoreCase(CROSSHEADING);
    }

    private void setItemDepthInHigherElements(final TableOfContentItemVO sourceItem, final TableOfContentItemVO targetItem ) {
        sourceItem.setItemDepth(targetItem.getItemDepth() == 0 ? 1 : targetItem.getItemDepth());
        sourceItem.getChildItems().forEach(child -> setItemDepthInHigherElements(child, targetItem));
    }

    private void setItemDepth(final TableOfContentItemVO sourceItem, final TableOfContentItemVO targetItem, final ItemPosition position) {
        if ((sourceItem.getTocItem().isHigherElement() != null && sourceItem.getTocItem().isHigherElement())
                || targetItem.getTocItem().isHigherElement() != null && targetItem.getTocItem().isHigherElement()) {
            setItemDepthInHigherElements(sourceItem, targetItem);
        } else {
            switch (position) {
                case AS_CHILDREN:
                    sourceItem.setItemDepth(targetItem.getItemDepth() + 1);
                    break;
                case BEFORE:
                    sourceItem.setItemDepth(targetItem.getItemDepth() == 0 ? 1 : targetItem.getItemDepth());
                    break;
                case AFTER:
                    if (targetItem.getTocItem().isRoot()) {
                        sourceItem.setItemDepth(1);
                    } else {
                        sourceItem.setItemDepth(targetItem.getItemDepth() == 0 ? 1 : targetItem.getItemDepth());
                    }
            }
        }
    }

    protected TableOfContentItemVO getLastChildLevel(TableOfContentItemVO sourceItem, TableOfContentItemVO targetItem) {
        List<TableOfContentItemVO> list = getFullTableOfContentFromBody(sourceItem);
        int sourceItemIndex = list.indexOf(sourceItem);
        TableOfContentItemVO nextTocItemInList = null;
        while (sourceItemIndex < list.size() - 1) {
            sourceItemIndex++;
            TableOfContentItemVO nextTocItemInListToCheck = list.get(sourceItemIndex);
            if (LEVEL.equals(getTagValueFromTocItemVo(nextTocItemInListToCheck))) {
                if (nextTocItemInListToCheck.getItemDepth() >= sourceItem.getItemDepth()) {
                    nextTocItemInList = nextTocItemInListToCheck;
                } else if (nextTocItemInListToCheck.getItemDepth() < sourceItem.getItemDepth()) {
                    break;
                }
            }
        }
        return nextTocItemInList != null ? nextTocItemInList : targetItem;
    }

    protected TableOfContentItemVO getSiblingPosition(TableOfContentItemVO sourceItem, TableOfContentItemVO targetItem) {
        List<TableOfContentItemVO> list = getFullTableOfContentFromBody(sourceItem);
        int sourceItemIndex = list.indexOf(sourceItem);
        TableOfContentItemVO nextTocItemInList = null;
        while (sourceItemIndex < list.size() - 1) {
            sourceItemIndex++;
            nextTocItemInList = list.get(sourceItemIndex);
            if (LEVEL.equals(getTagValueFromTocItemVo(nextTocItemInList)) || isHigherElement(nextTocItemInList)) {
                if (nextTocItemInList.getItemDepth() > targetItem.getItemDepth()) {
                    continue;
                } else if (nextTocItemInList.getItemDepth() <= targetItem.getItemDepth()) {
                    nextTocItemInList = getTargetPosition(sourceItem, targetItem, list, sourceItemIndex);
                    break;
                }
            }
        }
        return nextTocItemInList != null ? nextTocItemInList : targetItem;
    }

    private List<TableOfContentItemVO> getFullTableOfContentFromBody(TableOfContentItemVO sourceItem) {
        TableOfContentItemVO parent = sourceItem.getParentItem();
        while (parent != null && !parent.getTocItem().getAknTag().equals(AknTag.MAIN_BODY)) {
            parent = parent.getParentItem();
        }
        return parent.flattened().collect(Collectors.toList());
    }

    private TableOfContentItemVO getTargetPosition(TableOfContentItemVO sourceItem, TableOfContentItemVO targetItem, List<TableOfContentItemVO> list, int sourceItemIndex) {
        TableOfContentItemVO nextTocItemInList;
        if (sourceItem == list.get(sourceItemIndex - 1)) {
            nextTocItemInList = targetItem;
        } else {
            nextTocItemInList = list.get(sourceItemIndex - 1);
        }
        return nextTocItemInList;
    }

    protected boolean isHigherElement(TableOfContentItemVO tocItemVO) {
        return (tocItemVO.getTocItem().isHigherElement() != null && tocItemVO.getTocItem().isHigherElement());
    }

    protected boolean isLastExistingChildElement(TableOfContentItemVO tocItemVO) {
        return tocItemVO.getParentItem().getChildItemsView().size() == 1;
    }

    protected static TableOfContentItemVO getTableOfContentItemVOById(String id, List<TableOfContentItemVO> tableOfContentItemVOS) {
        for (TableOfContentItemVO tableOfContentItemVO : tableOfContentItemVOS) {
            if (tableOfContentItemVO.getId().equals(id)) {
                return tableOfContentItemVO;
            } else {
                TableOfContentItemVO childResult = getTableOfContentItemVOById(id, tableOfContentItemVO.getChildItems());
                if (childResult != null) {
                    return childResult;
                }
            }
        }
        return null;
    }

    protected TableOfContentItemVO copyItemToTemp(TableOfContentItemVO originalItem) {
        TableOfContentItemVO temp = new TableOfContentItemVO(originalItem.getTocItem(), TEMP_PREFIX + originalItem.getId(),
                originalItem.getOriginAttr(), originalItem.getNumber(),
                originalItem.getOriginNumAttr(), originalItem.getHeading(),
                originalItem.getNode(), originalItem.getList(), originalItem.getContent(),
                originalItem.getSoftActionAttr(), originalItem.isSoftActionRoot(), originalItem.getSoftUserAttr(), originalItem.getSoftDateAttr(),
                originalItem.getSoftMoveFrom(), originalItem.getSoftMoveTo(), originalItem.getSoftTransFrom(), originalItem.isUndeleted(), originalItem.getNumSoftActionAttr());
        temp.setContent(originalItem.getContent());
        temp.setItemDepth(originalItem.getItemDepth());
        temp.setOriginalDepth(originalItem.getOriginalDepth());
        originalItem.getChildItems().forEach(child -> temp.addChildItem(copyItemToTemp(child)));
        return temp;
    }

    protected void dropItemAtOriginalPosition(TableOfContentItemVO originalPosition, TableOfContentItemVO item, TreeData<TableOfContentItemVO> container) {
        TableOfContentItemVO parentItem = originalPosition.getParentItem();
        if (parentItem != null && item != null) {
            int indexSiblings = parentItem.getChildItems().indexOf(originalPosition);
            if (indexSiblings >= 0) {
                container.addItem(parentItem, item);
                item.setParentItem(parentItem);
                parentItem.getChildItems().add(indexSiblings, item);
                container.moveAfterSibling(item, originalPosition);
                container.moveAfterSibling(originalPosition, item);
            } else {
                throw new IllegalStateException("Original element not found in its parent list of children");
            }
            dropChildrenAtOriginalPosition(item, container);
        }
    }

    protected void dropChildrenAtOriginalPosition(TableOfContentItemVO parentItem, TreeData<TableOfContentItemVO> container) {
        List<TableOfContentItemVO> children = parentItem.getChildItems();
        if (children != null) {
            for (TableOfContentItemVO child : children) {
                container.addItem(parentItem, child);
                dropChildrenAtOriginalPosition(child, container);
            }
        }
    }

    protected TableOfContentItemVO copyTempItemToFinalItem(TableOfContentItemVO tempItem) {
        TableOfContentItemVO finalItem = new TableOfContentItemVO(tempItem.getTocItem(), tempItem.getId().replace(TEMP_PREFIX, ""),
                tempItem.getOriginAttr(), tempItem.getNumber(),
                tempItem.getOriginNumAttr(), tempItem.getHeading(),
                tempItem.getNode(), tempItem.getList(), tempItem.getContent(),
                tempItem.getSoftActionAttr(), tempItem.isSoftActionRoot(), tempItem.getSoftUserAttr(), tempItem.getSoftDateAttr(),
                tempItem.getSoftMoveFrom(), tempItem.getSoftMoveTo(), tempItem.getSoftTransFrom(), tempItem.isUndeleted(), tempItem.getNumSoftActionAttr());

        finalItem.setContent(tempItem.getContent());
        finalItem.setItemDepth(tempItem.getItemDepth());
        finalItem.setOriginalDepth(tempItem.getOriginalDepth());
        finalItem.setInitialNum(tempItem.getInitialNum());
        tempItem.getChildItems().forEach(child -> finalItem.addChildItem(copyTempItemToFinalItem(child)));
        return finalItem;
    }

    protected void restoreMovedItemOrSetNumber(final TreeGrid<TableOfContentItemVO> tocTree, final TableOfContentItemVO droppedItem, final TableOfContentItemVO newPosition, final ItemPosition position) {
        List<TableOfContentItemVO> siblings = ItemPosition.AS_CHILDREN.equals(position) ? tocTree.getTreeData().getChildren(newPosition) : tocTree.getTreeData().getChildren(tocTree.getTreeData().getParent(newPosition));
        Integer droppedItemIndex = siblings.indexOf(droppedItem);
        TableOfContentItemVO previousSibling = droppedItemIndex > 0 ? siblings.get(droppedItemIndex - 1) : null;
        TableOfContentItemVO nextSibling = droppedItemIndex < siblings.size() - 1 ? siblings.get(droppedItemIndex + 1) : null;
        if (isPlaceholderForDroppedItem(newPosition, droppedItem)) {
            restoreOriginal(droppedItem, newPosition, tocTree);
            if (newPosition.getParentItem() != null) {
                newPosition.getParentItem().removeChildItem(newPosition);
            }
        } else if (isPlaceholderForDroppedItem(previousSibling, droppedItem)) {
            restoreOriginal(droppedItem, previousSibling, tocTree);
            if (newPosition.getParentItem() != null) {
                newPosition.getParentItem().removeChildItem(previousSibling);
            }
        } else if (isPlaceholderForDroppedItem(nextSibling, droppedItem)) {
            restoreOriginal(droppedItem, nextSibling, tocTree);
            if (newPosition.getParentItem() != null) {
                newPosition.getParentItem().removeChildItem(nextSibling);
            }
        } else {
            setNumber(droppedItem, newPosition);
        }
    }

    protected Boolean isPlaceholderForDroppedItem(TableOfContentItemVO candidate, TableOfContentItemVO droppedItem) {
        if (candidate != null && hasTocItemSoftAction(candidate.getParentItem(), MOVE_TO)) {
            return false;
        }
        return candidate != null && candidate.getId().equals(SOFT_MOVE_PLACEHOLDER_ID_PREFIX + droppedItem.getId());
    }

    protected void restoreOriginal(final TableOfContentItemVO movedItem, final TableOfContentItemVO originalItem, final TreeGrid<TableOfContentItemVO> tocTree) {
        setAttributesToOriginal(movedItem, originalItem);
        TreeData<TableOfContentItemVO> container = tocTree.getTreeData();
        List<TableOfContentItemVO> movedItemChildren = movedItem.getChildItems();
        int position = getPositionOfNextNonRootChild(movedItemChildren, 0);
        int index = 0;
        TableOfContentItemVO originalItemChild;
        while (index < originalItem.getChildItems().size()) {
            originalItemChild = originalItem.getChildItems().get(index);
            if (originalItemChild.isSoftActionRoot()) {
                if (position >= 0) {
                    moveItem(originalItemChild, movedItemChildren.get(position), tocTree.getTreeData());
                } else {
                    if (hasTocItemSoftAction(originalItem, MOVE_TO) && hasTocItemSoftAction(originalItemChild, DELETE)) {
                        addSoftDeletedChildrenToTree(originalItemChild, movedItem, container);
                        index++;
                    } else {
                        moveItemToLastChild(originalItemChild, movedItem, container);
                    }
                }
                position = getPositionOfNextNonRootChild(movedItemChildren, movedItemChildren.indexOf(originalItemChild));
            } else {
                if (position >= 0 && position < movedItemChildren.size()) {
                    restoreOriginal(movedItemChildren.get(position), originalItemChild, tocTree);
                    position = getPositionOfNextNonRootChild(movedItemChildren, position + 1);
                }
                index++;
            }
        }
        tocTree.getTreeData().removeItem(originalItem);
    }

    private void addSoftDeletedChildrenToTree(final TableOfContentItemVO targetItem, TableOfContentItemVO sourceItem, TreeData<TableOfContentItemVO> container) {
        if (hasTocItemSoftAction(targetItem, DELETE)) {
            if (container.contains(targetItem)) {
                container.removeItem(targetItem);
            }
            if (!targetItem.getParentItem().equals(sourceItem)) {
                sourceItem.getChildItems().add(targetItem);
            }
            container.addItem(sourceItem, targetItem);
            int position = getTocItemChildPosition(sourceItem, targetItem);
            for (TableOfContentItemVO child : targetItem.getChildItemsView()) {
                addSoftDeletedChildrenToTree(child, sourceItem.getChildItemsView().get(position), container);
            }
        }
    }

    private void moveItemToLastChild(TableOfContentItemVO item, TableOfContentItemVO parent, TreeData<TableOfContentItemVO> container) {
        TableOfContentItemVO tempDeletedItem = copyItemToTemp(item);
        container.removeItem(item);
        container.addItem(parent, tempDeletedItem);
        if (item.getParentItem() != null) {
            item.getParentItem().removeChildItem(item);
        }

        TableOfContentItemVO finalItem = copyTempItemToFinalItem(tempDeletedItem);
        container.removeItem(tempDeletedItem);
        container.addItem(parent, finalItem);
        parent.getChildItems().add(finalItem);
    }

    protected boolean isRootElement(TableOfContentItemVO element) {
        return Boolean.TRUE.equals(element.isSoftActionRoot());
    }

    protected int getPositionOfNextNonRootChild(List<TableOfContentItemVO> childrenElements, int position) {
        while (position < childrenElements.size()) {
            if (!isRootElement(childrenElements.get(position))) {
                return position;
            }
            position++;
        }
        return -1;
    }

    protected void setAttributesToOriginal(TableOfContentItemVO movedItem, TableOfContentItemVO originalItem) {
        if (originalItem.getNumSoftActionAttr() != null) {
            movedItem.setNumber(originalItem.getNumber());
            movedItem.setNumSoftActionAttr(originalItem.getNumSoftActionAttr());
        } else if (originalItem.getNumber() != null && !originalItem.getNumber().isEmpty()) {
            movedItem.setNumber(originalItem.getNumber());
            movedItem.setNumSoftActionAttr(null);
        } else {
            movedItem.setNumber(null);
        }
        movedItem.setSoftActionAttr(null);
        movedItem.setSoftMoveTo(null);
        movedItem.setSoftMoveFrom(null);
        movedItem.setSoftUserAttr(null);
        movedItem.setSoftDateAttr(null);
        movedItem.setSoftActionRoot(null);
        movedItem.setRestored(true);
        if (originalItem.isIndented()) {
            movedItem.populateIndentInfo(originalItem.getIndentOriginType(), originalItem.getIndentOriginIndentLevel()
                    , originalItem.getIndentOriginNumId(), originalItem.getIndentOriginNumValue(), originalItem.getIndentOriginNumOrigin());
        }
    }

    protected TableOfContentItemVO moveItem(TableOfContentItemVO item, TableOfContentItemVO moveBefore, TreeData<TableOfContentItemVO> treeData) {
        TableOfContentItemVO tempDeletedItem = copyItemToTemp(item);
        return movingItem(item, moveBefore, treeData, tempDeletedItem);
    }

    protected TableOfContentItemVO movingItem(TableOfContentItemVO item, TableOfContentItemVO moveBefore, TreeData<TableOfContentItemVO> treeData, TableOfContentItemVO tempDeletedItem) {
        dropItemAtOriginalPosition(moveBefore, tempDeletedItem, treeData);
        treeData.removeItem(item);
        if (item.getParentItem() != null) {
            item.getParentItem().removeChildItem(item);
        }

        TableOfContentItemVO finalItem = copyTempItemToFinalItem(tempDeletedItem);
        dropItemAtOriginalPosition(tempDeletedItem, finalItem, treeData);
        treeData.removeItem(tempDeletedItem);
        if (tempDeletedItem.getParentItem() != null) {
            tempDeletedItem.getParentItem().removeChildItem(tempDeletedItem);
        }
        return finalItem;
    }

    protected boolean isElementAndTargetOriginDifferent(TableOfContentItemVO element, TableOfContentItemVO parent) {
        boolean isDifferent = false;
        if (element.getOriginAttr() == null) {
            isDifferent = true;
        } else if (!element.getOriginAttr().equals(parent.getOriginAttr())) {
            isDifferent = true;
        }
        return isDifferent;
    }

    protected void setNumber(final TableOfContentItemVO droppedElement, final TableOfContentItemVO targetElement) {
        if (isNumbered(droppedElement, targetElement)) {
            if (!droppedElement.isAutoNumOverwritten()) {
                droppedElement.setNumber(StructureConfigUtils.HASH_NUM_VALUE);
            }
            if (isNumSoftDeleted(droppedElement.getNumSoftActionAttr())) {
                droppedElement.setNumSoftActionAttr(null);
            }
        } else {
            droppedElement.setNumber(null);
        }
    }

    protected boolean isNumSoftDeleted(final SoftActionType numSoftACtionAttr) {
        return DELETE.equals(numSoftACtionAttr);
    }

    private boolean isNumbered(TableOfContentItemVO droppedElement, TableOfContentItemVO targetElement) {
        boolean isNumbered = true;
        if (OptionsType.NONE.equals(droppedElement.getTocItem().getItemNumber())) {
            isNumbered = false;
        } else if (OptionsType.OPTIONAL.equals(droppedElement.getTocItem().getItemNumber())) {
            if (getTagValueFromTocItemVo(targetElement).equals(getTagValueFromTocItemVo(droppedElement))) {
                if ((StringUtils.isEmpty(targetElement.getNumber())) || isNumSoftDeleted(targetElement.getNumSoftActionAttr())) {
                    isNumbered = false;
                }
            } else if ((targetElement.getChildItems() != null) && (targetElement.getChildItems().size() > 0)) {
                for (TableOfContentItemVO itemVO : targetElement.getChildItems()) {
                    if (getTagValueFromTocItemVo(itemVO).equals(getTagValueFromTocItemVo(droppedElement))) {
                        if ((StringUtils.isEmpty(itemVO.getNumber())) || isNumSoftDeleted(itemVO.getNumSoftActionAttr())) {
                            isNumbered = false;
                            break;
                        }
                    }
                }
            }
        }
        if (isNumbered
                && getTagValueFromTocItemVo(droppedElement).equals(PARAGRAPH)
                && droppedElement.getParentItem().isNumberingToggled() != null
                && !droppedElement.getParentItem().isNumberingToggled()) {
            return false;
        }
        if (!isNumbered
                && getTagValueFromTocItemVo(droppedElement).equals(PARAGRAPH)
                && droppedElement.getParentItem().isNumberingToggled() != null
                && droppedElement.getParentItem().isNumberingToggled()) {
            return true;
        }
        return isNumbered;
    }

    protected boolean containsItemOfOrigin(TableOfContentItemVO tableOfContentItemVO, String origin, String elementOrigin) {
        if ((!StringUtils.isEmpty(tableOfContentItemVO.getOriginAttr()) && tableOfContentItemVO.getOriginAttr().equals(origin)) ||
                (StringUtils.isEmpty(tableOfContentItemVO.getOriginAttr()) && origin.equals(elementOrigin))) {
            return true;
        }
        boolean containsItem = false;
        for (TableOfContentItemVO item : tableOfContentItemVO.getChildItems()) {
            containsItem = containsItemOfOrigin(item, origin, elementOrigin);
            if (containsItem) break;
        }
        return containsItem;
    }

    protected TableOfContentItemVO softDeleteItem(TreeGrid<TableOfContentItemVO> tocTree,
                                                  TableOfContentItemVO item, String elementOrigin) {
        Boolean wasRoot = isRootElement(item);
        Boolean wasMoved = MOVE_FROM.equals(item.getSoftActionAttr());
        softDeleteMovedRootItems(tocTree, item);
        TableOfContentItemVO movedTableOfContentItemVO = null;
        if (wasMoved) {
            if (!wasRoot) {
                // all its moved children are now restored to their original position and deleted,
                // but the item element still needs to be restored to its original position and deleted
                revertMoveAndTransformToSoftDeleted(tocTree, item);
            }
        } else {
            // all its moved children are now restored to their original position and deleted,
            // and the item only needs to be deleted
            movedTableOfContentItemVO = transformToSoftDeleted(tocTree.getTreeData(), item);
        }
        if (elementOrigin.equals(item.getOriginAttr())) {
            if (movedTableOfContentItemVO != null) {
                tocTree.getTreeData().removeItem(movedTableOfContentItemVO);
                item.getParentItem().removeChildItem(movedTableOfContentItemVO);
            }
            item.getParentItem().removeChildItem(item);
            tocTree.getDataProvider().refreshAll();
        }
        return movedTableOfContentItemVO;
    }

    protected ActionType hardDeleteFromTree(TreeGrid<TableOfContentItemVO> tocTree, TableOfContentItemVO tableOfContentItemVO) {
        tocTree.getTreeData().removeItem(tableOfContentItemVO);
        if (tableOfContentItemVO.getParentItem() != null) {
            tableOfContentItemVO.getParentItem().removeChildItem(tableOfContentItemVO);
        }
        return ActionType.DELETED;
    }

    private int softDeleteMovedRootItems(TreeGrid<TableOfContentItemVO> tocTree, TableOfContentItemVO item) {
        int index = 0;
        while (index < item.getChildItems().size()) {
            index += softDeleteMovedRootItems(tocTree, item.getChildItems().get(index));
        }
        if (isRootElement(item) && MOVE_FROM.equals(item.getSoftActionAttr())) {
            revertMoveAndTransformToSoftDeleted(tocTree, item);
            return 0;
        } else if (CN.equals(item.getOriginAttr()) && item.getSoftActionAttr() == null) {
            tocTree.getTreeData().removeItem(item);
            item.getParentItem().removeChildItem(item);
            return 0;
        }
        return 1;
    }

    private void revertMoveAndTransformToSoftDeleted(TreeGrid<TableOfContentItemVO> tocTree, TableOfContentItemVO item) {
        TableOfContentItemVO originalItem = getTableOfContentItemVOById(item.getSoftMoveFrom(), tocTree.getTreeData().getRootItems());
        if (originalItem != null) {
            TableOfContentItemVO movedItem = moveItem(item, originalItem, tocTree.getTreeData());
            restoreOriginal(movedItem, originalItem, tocTree);
            if (originalItem.getParentItem() != null) {
                originalItem.getParentItem().removeChildItem(originalItem);
            }
            transformToSoftDeleted(tocTree.getTreeData(), movedItem);
        } else {
            throw new IllegalStateException("Soft-moved element was later hard-deleted or its id was not set in its placeholder");
        }
    }

    private TableOfContentItemVO transformToSoftDeleted(TreeData<TableOfContentItemVO> treeData, TableOfContentItemVO item) {
        TableOfContentItemVO tempDeletedItem = copyDeletedItemToTemp(item, Boolean.TRUE);
        return movingItem(item, item, treeData, tempDeletedItem);
    }

    private TableOfContentItemVO copyDeletedItemToTemp(TableOfContentItemVO originalItem, Boolean isSoftActionRoot) {
        TableOfContentItemVO tempDeletedItem;

        if (!MOVE_TO.equals(originalItem.getSoftActionAttr()) && !DELETE.equals(originalItem.getSoftActionAttr())) {
            tempDeletedItem = new TableOfContentItemVO(originalItem.getTocItem(), TEMP_PREFIX + SOFT_DELETE_PLACEHOLDER_ID_PREFIX + originalItem.getId(),
                    originalItem.getOriginAttr(), originalItem.getNumber(),
                    EC, originalItem.getHeading(), originalItem.getNode(),
                    originalItem.getList(),
                    originalItem.getContent(),
                    DELETE, isSoftActionRoot, null, null, originalItem.getSoftMoveFrom(), originalItem.getSoftMoveTo(), originalItem.getSoftTransFrom(), originalItem.isUndeleted(), originalItem.getNumSoftActionAttr());
        } else {
            tempDeletedItem = new TableOfContentItemVO(originalItem.getTocItem(), TEMP_PREFIX + originalItem.getId(),
                    originalItem.getOriginAttr(), originalItem.getNumber(),
                    originalItem.getOriginNumAttr(), originalItem.getHeading(),
                    originalItem.getNode(), originalItem.getList(), originalItem.getContent(),
                    originalItem.getSoftActionAttr(), originalItem.isSoftActionRoot(), originalItem.getSoftUserAttr(), originalItem.getSoftDateAttr(),
                    originalItem.getSoftMoveFrom(), originalItem.getSoftMoveTo(), originalItem.getSoftTransFrom(), originalItem.isUndeleted(), originalItem.getNumSoftActionAttr());
        }

        tempDeletedItem.setContent(originalItem.getContent());
        tempDeletedItem.setItemDepth(originalItem.getItemDepth());
        tempDeletedItem.setOriginalDepth(originalItem.getOriginalDepth());
        tempDeletedItem.setInitialNum(originalItem.getInitialNum());
        originalItem.getChildItems().forEach(child -> tempDeletedItem.addChildItem(copyDeletedItemToTemp(child, Boolean.FALSE)));
        return tempDeletedItem;
    }

    protected TocDropResult validateAgainstSoftDeletedOrMoveToItems(List<TableOfContentItemVO> droppedItems, TableOfContentItemVO targetItem, TableOfContentItemVO parentItem, ItemPosition position) {

        // Check if there are no soft deleted items at first level(not in children) in dropped items
        TocDropResult tocDropResult = new TocDropResult(true, "toc.edit.window.drop.success.message", droppedItems.get(0), targetItem);
        boolean originalFound = false;
        for (TableOfContentItemVO sourceItem : droppedItems) {
            if (isSoftDeletedOrMoveToItem(sourceItem)) {
                tocDropResult.setSuccess(false);
                tocDropResult.setMessageKey("toc.edit.window.drop.error.softdeleted.source.message");
                tocDropResult.setSourceItem(sourceItem);
                return tocDropResult;
            } else if (isPlaceholderForDroppedItem(targetItem, sourceItem)) {
                //if the target is the placeholder for one of the source items skip target validation
                originalFound = true;
            }
        }
        if (!originalFound) {
            tocDropResult.setSuccess(!isSoftDeletedOrMoveToItem(ItemPosition.AS_CHILDREN.equals(position) && targetItem.getTocItem().isChildrenAllowed() ? targetItem : parentItem));
            tocDropResult.setMessageKey("toc.edit.window.drop.error.softdeleted.target.message");
        }
        return tocDropResult;
    }

    private boolean isSoftDeletedOrMoveToItem(TableOfContentItemVO item) {
        return (hasTocItemSoftAction(item, DELETE) || hasTocItemSoftAction(item, MOVE_TO));
    }

    public void propagateChangeListType(TableOfContentItemVO item, TocItem tocItem, List<NumberingConfig> numberingConfigs, MultiSelectTreeGrid<TableOfContentItemVO> tocTree) {
    }

    void setItemLevel(final TableOfContentItemVO sourceItem, final TableOfContentItemVO targetItem, final ItemPosition position) {
    }

    @Override
    public String getTocTreeStyling(TableOfContentItemVO tableOfContentItemVO, MultiSelectTreeGrid<TableOfContentItemVO> tocTree, TreeDataProvider<TableOfContentItemVO> dataProvider) {
        String itemSoftStyle = TableOfContentHelper.getItemSoftStyle(tableOfContentItemVO);
        if (tocTree.getTreeData().contains(tableOfContentItemVO) && !tableOfContentItemVO.getChildItemsView().isEmpty()) {
            int numChildren = dataProvider.getChildCount(new HierarchicalQuery<>(dataProvider.getFilter(), tableOfContentItemVO));
            itemSoftStyle = numChildren > 0 ? itemSoftStyle : (itemSoftStyle + " leos-toc-no-expander-icon").trim();
        }
        return itemSoftStyle;
    }
}
