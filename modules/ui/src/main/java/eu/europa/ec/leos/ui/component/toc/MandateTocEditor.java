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

import com.google.common.collect.Lists;
import com.vaadin.data.TreeData;
import com.vaadin.data.provider.TreeDataProvider;
import com.vaadin.spring.annotation.SpringComponent;
import com.vaadin.ui.TreeGrid;
import eu.europa.ec.leos.domain.common.InstanceType;
import eu.europa.ec.leos.instance.Instance;
import eu.europa.ec.leos.model.action.ActionType;
import eu.europa.ec.leos.services.numbering.depthBased.ClassToDepthType;
import eu.europa.ec.leos.services.processor.content.TableOfContentHelper;
import eu.europa.ec.leos.services.processor.content.TableOfContentProcessor;
import eu.europa.ec.leos.vo.toc.Level;
import eu.europa.ec.leos.vo.toc.NumberingConfig;
import eu.europa.ec.leos.vo.toc.NumberingType;
import eu.europa.ec.leos.vo.toc.OptionsType;
import eu.europa.ec.leos.vo.toc.StructureConfigUtils;
import eu.europa.ec.leos.vo.toc.TableOfContentItemVO;
import eu.europa.ec.leos.vo.toc.TocItem;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import static eu.europa.ec.leos.model.action.SoftActionType.ADD;
import static eu.europa.ec.leos.model.action.SoftActionType.DELETE;
import static eu.europa.ec.leos.model.action.SoftActionType.MOVE_FROM;
import static eu.europa.ec.leos.model.action.SoftActionType.MOVE_TO;
import static eu.europa.ec.leos.services.processor.content.TableOfContentHelper.ELEMENTS_WITHOUT_CONTENT;
import static eu.europa.ec.leos.services.processor.content.TableOfContentHelper.hasTocItemSoftAction;
import static eu.europa.ec.leos.services.processor.content.TableOfContentProcessor.resetUserInfo;
import static eu.europa.ec.leos.services.support.XmlHelper.BLOCK;
import static eu.europa.ec.leos.services.support.XmlHelper.CN;
import static eu.europa.ec.leos.services.support.XmlHelper.CONCLUSIONS;
import static eu.europa.ec.leos.services.support.XmlHelper.CROSSHEADING;
import static eu.europa.ec.leos.services.support.XmlHelper.DIVISION;
import static eu.europa.ec.leos.services.support.XmlHelper.EC;
import static eu.europa.ec.leos.services.support.XmlHelper.ELEMENTS_TO_BE_PROCESSED_FOR_NUMBERING;
import static eu.europa.ec.leos.services.support.XmlHelper.INDENT;
import static eu.europa.ec.leos.services.support.XmlHelper.LEVEL;
import static eu.europa.ec.leos.services.support.XmlHelper.LIST;
import static eu.europa.ec.leos.services.support.XmlHelper.MAIN_BODY;
import static eu.europa.ec.leos.services.support.XmlHelper.PARAGRAPH;
import static eu.europa.ec.leos.services.support.XmlHelper.POINT;
import static eu.europa.ec.leos.services.support.XmlHelper.POINT_ROOT_PARENT_ELEMENTS;
import static eu.europa.ec.leos.services.support.XmlHelper.SOFT_MOVE_PLACEHOLDER_ID_PREFIX;
import static eu.europa.ec.leos.services.support.XmlHelper.SUBPARAGRAPH;
import static eu.europa.ec.leos.services.support.XmlHelper.SUBPOINT;
import static eu.europa.ec.leos.services.processor.content.TableOfContentProcessor.getTagValueFromTocItemVo;
import static eu.europa.ec.leos.services.processor.content.TableOfContentProcessor.updateDepthOfTocItems;
import static eu.europa.ec.leos.services.processor.content.TableOfContentProcessor.updateStyleClassOfTocItems;
import static eu.europa.ec.leos.vo.toc.NumberingType.BULLET_NUM;

@SpringComponent
@Instance(InstanceType.COUNCIL)
public class MandateTocEditor extends AbstractTocEditor {

    private static final String TEMP_PREFIX = "temp_";
    private static final int MAX_INDENT_LEVEL = 4;
    private final TableOfContentProcessor tableOfContentProcessor;

    @Autowired
    public MandateTocEditor(TableOfContentProcessor tableOfContentProcessor) {
        this.tableOfContentProcessor = tableOfContentProcessor;
    }

    @Override
    public void setTocTreeDataFilter(boolean editionEnabled, TreeDataProvider<TableOfContentItemVO> dataProvider) {
        dataProvider.setFilter(tableOfContentItemVO -> {
            return tableOfContentItemVO.getTocItem().isDisplay() &&
                    !(editionEnabled && (tableOfContentItemVO.getTocItem().getAknTag().value().equalsIgnoreCase(SUBPARAGRAPH) || tableOfContentItemVO.getTocItem().getAknTag().value().equalsIgnoreCase(SUBPOINT))
                    && tableOfContentItemVO.getNode() != null
                    && tableOfContentItemVO.getParentItem() != null
                    && tableOfContentItemVO.getParentItem().getChildItemsView().get(0).getId().equals(tableOfContentItemVO.getId()));
        });
    }

    @Override
    public boolean isDeletableItem(TreeData<TableOfContentItemVO> treeData, TableOfContentItemVO tableOfContentItemVO) {
        String elementName = getTagValueFromTocItemVo(tableOfContentItemVO);
        return !((DELETE.equals(tableOfContentItemVO.getSoftActionAttr()) ||
                MOVE_TO.equals(tableOfContentItemVO.getSoftActionAttr()) ||
                (PARAGRAPH.equals(elementName) && checkOriginParentTocITem(tableOfContentItemVO, CN))) && isLastExistingChildElement(tableOfContentItemVO));
    }

    @Override
    public boolean checkIfConfirmDeletion(TreeData<TableOfContentItemVO> treeData, TableOfContentItemVO tableOfContentItemVO) {
        return !treeData.getChildren(tableOfContentItemVO).isEmpty() && containsNoSoftDeletedItem(treeData, tableOfContentItemVO);
    }

    @Override
    public ActionType deleteItem(TreeGrid<TableOfContentItemVO> tocTree, TableOfContentItemVO item) {
        final ActionType actionType;
        setAffectedAttribute(item, tocTree.getTreeData());
        TableOfContentItemVO parentItem = checkDeleteOnLastItemInList(item);

        // LEOS-5958: Delete selected item element.
        if (!containsItemOfOrigin(item, EC, CN)) {
            actionType = hardDeleteFromTree(tocTree, item);
        } else {
            softDeleteItem(tocTree, item, CN);
            actionType = ActionType.SOFTDELETED;
        }
        
        // LEOS-5958: If parentItem is not null, means it is a list without any points. Then delete parentItem as well.
        if(parentItem != null) {
        	if (!containsItemOfOrigin(parentItem, EC, CN)) {
                hardDeleteFromTree(tocTree, parentItem);
            } else {
                softDeleteItem(tocTree, parentItem, CN);
            }
        } else {
        	updateStyleClassOfTocItems(tocTree.getTreeData().getChildren(item.getParentItem()), DIVISION);
            updateDepthOfTocItems(tocTree.getTreeData().getChildren(item.getParentItem()));
        }
        
        tocTree.getDataProvider().refreshAll();
        tocTree.deselectAll();
        return actionType;
    }

    private TableOfContentItemVO checkDeleteOnLastItemInList(TableOfContentItemVO deletedItem) {
        TableOfContentItemVO parentItem = deletedItem.getParentItem();
        if (getTagValueFromTocItemVo(parentItem).equals(LIST) && isLastExistingChildElement(deletedItem)) {
            return parentItem;
        }
        return null;
    }

    @Override
    public TocDropResult addOrMoveItems(final boolean isAdd, final TreeGrid<TableOfContentItemVO> tocTree, final Map<TocItem, List<TocItem>> tableOfContentRules,
            final List<TableOfContentItemVO> droppedItems, final TableOfContentItemVO targetItem, final ItemPosition position) {

        TocDropResult result = validateAction(tocTree, tableOfContentRules, droppedItems, targetItem, position);
        if (result.isSuccess()) {
            TableOfContentItemVO parentItem = tocTree.getTreeData().getParent(targetItem);
            List<TableOfContentItemVO> sourceItems = ((ItemPosition.BEFORE == position) || targetItem.getTocItem().isChildrenAllowed())
                    ? droppedItems : Lists.reverse(droppedItems);
            TableOfContentItemVO originalForTarget = sourceItems.stream().filter(sourceItem -> isPlaceholderForDroppedItem(targetItem, sourceItem)).findFirst().orElse(null);
            if (originalForTarget !=  null && !isAdd) {
                //first move the original over placeholder and then the rest over the original
                performAddOrMoveAction(false, tocTree, tableOfContentRules, originalForTarget, targetItem, parentItem, position);
                parentItem = tocTree.getTreeData().getParent(originalForTarget);
                for (TableOfContentItemVO sourceItem : sourceItems) {
                    //all objects come from the same list of dropped items, so no need to use equals
                    if (sourceItem != originalForTarget) {
                        performAddOrMoveAction(false, tocTree, tableOfContentRules, sourceItem, originalForTarget, parentItem, position);
                    }
                }
            } else {
                for (TableOfContentItemVO sourceItem : sourceItems) {
                    performAddOrMoveAction(isAdd, tocTree, tableOfContentRules, sourceItem, targetItem, parentItem, position);
                }
            }
            tocTree.deselectAll();
            tocTree.getDataProvider().refreshAll();
        }
        return result;
    }

    @Override
    protected void addOrMoveItem(final boolean isAdd, final TableOfContentItemVO sourceItem, final TableOfContentItemVO targetItem,
            final TreeGrid<TableOfContentItemVO> tocTree, final TableOfContentItemVO actualTargetItem, final ItemPosition position) {
        if (isAdd) {
            super.addOrMoveItem(true, sourceItem, targetItem, tocTree, actualTargetItem, position);
            moveOriginAttribute(sourceItem, targetItem);
            setNumber(sourceItem, targetItem);
            if (sourceItem.getTocItem().isAddSoftAttr() == null ||sourceItem.getTocItem().isAddSoftAttr()) {
                sourceItem.setSoftActionAttr(ADD);
                sourceItem.setSoftActionRoot(Boolean.TRUE);
            }
            if(DIVISION.equals(sourceItem.getTocItem().getAknTag().value())){
                sourceItem.setStyle(ClassToDepthType.TYPE_1.name());
            }
        } else {
            updateMovedOnEmptyParent(sourceItem, actualTargetItem, PARAGRAPH, SUBPARAGRAPH);
            updateMovedOnEmptyParent(sourceItem, actualTargetItem, LEVEL, SUBPARAGRAPH);
            handleMoveAction(sourceItem, tocTree);
            super.addOrMoveItem(false, sourceItem, targetItem, tocTree, actualTargetItem, position);
            restoreMovedItemOrSetNumber(tocTree, sourceItem, targetItem, position);
        }
        updateStyleClassOfTocItems(tocTree.getTreeData().getChildren(sourceItem.getParentItem()), DIVISION);
        updateDepthOfTocItems(tocTree.getTreeData().getChildren(sourceItem.getParentItem()));
        handleLevelMove(sourceItem, targetItem);
        setAffectedAttribute(sourceItem, tocTree.getTreeData());
        setBlockOrCrossHeading(sourceItem);
        resetUserInfo(sourceItem);
    }

    private void setBlockOrCrossHeading(TableOfContentItemVO sourceItem) {
        boolean iscrossHeading = getTagValueFromTocItemVo(sourceItem).equalsIgnoreCase(CROSSHEADING)
                || getTagValueFromTocItemVo(sourceItem).equalsIgnoreCase(BLOCK);
        if (iscrossHeading && MAIN_BODY.equals(sourceItem.getParentItem().getTocItem().getAknTag().value())) {
            sourceItem.setBlock(true);
        } else if (iscrossHeading) {
            sourceItem.setCrossHeading(true);
        }
        if (iscrossHeading && isInList(sourceItem)) {
            sourceItem.setCrossHeadingInList(true);
        }
    }

    protected TocDropResult validateAction(final TreeGrid<TableOfContentItemVO> tocTree, final Map<TocItem, List<TocItem>> tableOfContentRules,
            final List<TableOfContentItemVO> droppedItems, final TableOfContentItemVO targetItem, final ItemPosition position) {

        TocDropResult result = validateAgainstSoftDeletedOrMoveToItems(droppedItems, targetItem, tocTree.getTreeData().getParent(targetItem), position);
        if (result.isSuccess()){
            return super.validateAction(tocTree, tableOfContentRules, droppedItems, targetItem, position);
        }
        return result;
    }

    @Override
    protected boolean validateAddingToItem(final TocDropResult result, final TableOfContentItemVO sourceItem, final TableOfContentItemVO targetItem,
            final TreeGrid<TableOfContentItemVO> tocTree, TableOfContentItemVO actualTargetItem, final ItemPosition position) {
        Validate.notNull(targetItem, "Target item should not be null");
        if(actualTargetItem == null) {
            actualTargetItem = targetItem;
        }
        String droppedElementTagName = sourceItem.getTocItem().getAknTag().value();
        NumberingType droppedElementTagNumberingType = sourceItem.getTocItem().getNumberingType();

        String targetName = actualTargetItem.getTocItem().getAknTag().value();
        result.setSourceItem(sourceItem);
        result.setTargetItem(actualTargetItem);
        boolean indentAllowed;

        switch (droppedElementTagName) {
            case SUBPARAGRAPH:
                if (!isNumbered(actualTargetItem)) {
                    result.setSuccess(false);
                    result.setMessageKey("toc.edit.window.drop.error.subparagraph.message");
                    return false;
                }
                break;
            case POINT:
            case INDENT:
                indentAllowed = isIndentAllowed(tocTree.getTreeData(), actualTargetItem, MAX_INDENT_LEVEL - getIndentLevel(sourceItem));
                if (!indentAllowed
                        || !Arrays.asList(PARAGRAPH, LEVEL, LIST, POINT, INDENT).contains(targetName)
                        || (Arrays.asList(PARAGRAPH, LEVEL).contains(targetName)
                            && actualTargetItem.containsItem(droppedElementTagName)
                            && !droppedElementTagName.equals(POINT))
                            && !Arrays.asList(NumberingType.INDENT, BULLET_NUM).contains(droppedElementTagNumberingType)
                        || (Arrays.asList(PARAGRAPH, LEVEL).contains(targetName)
                            && (actualTargetItem.containsItem(LIST) || !actualTargetItem.containsOnlySameIndentType(droppedElementTagNumberingType)))
                        || (targetName.equals(droppedElementTagName) && actualTargetItem.containsItem(LIST))
                        || !validateAgainstOtherIndentsInList(sourceItem, targetItem)) {
                    result.setSuccess(false);
                    if (!indentAllowed) {
                        result.setMessageKey("toc.edit.window.drop.error.indentation.message");
                    } else if (actualTargetItem.containsItem(LIST)) {
                        result.setMessageKey("toc.edit.window.drop.already.contains.list.error.message");
                    } else {
                        result.setMessageKey("toc.edit.window.drop.error.message");
                    }
                    return false;
                }
                break;
            case LIST:
                indentAllowed = isIndentAllowed(tocTree.getTreeData(), actualTargetItem, MAX_INDENT_LEVEL - getIndentLevel(sourceItem));
                if (!isPlaceholderForDroppedItem(targetItem, sourceItem) && (!indentAllowed || ((targetName.equals(PARAGRAPH) || targetName.equals(LEVEL)) && actualTargetItem.containsItem(LIST)) ||
                        ((targetName.equals(POINT) || targetName.equals(INDENT)) && actualTargetItem.containsItem(LIST)))) {
                    result.setSuccess(false);
                    result.setMessageKey(!indentAllowed ? "toc.edit.window.drop.error.indentation.message" : "toc.edit.window.drop.error.list.message");
                    return false;
                }
                break;
            default: // No item can be dropped after CONCLUSIONS item
                if (actualTargetItem.containsItem(CONCLUSIONS) && isDroppedAfterConclusion(targetItem, position)) {
                    result.setSuccess(false);
                    result.setMessageKey("toc.edit.window.drop.error.conclusion.message");
                    return false;
                }
                break;
        }
        return true;
    }

    @Override
    public String getNotDeletableMessageKey() {
        return "toc.edit.window.not.deletable.message.cn";
    }

    @Override
    public TableOfContentItemVO getSimplifiedTocItem(TableOfContentItemVO item) {
        if ((Arrays.asList(PARAGRAPH, POINT, INDENT, LEVEL).contains(getTagValueFromTocItemVo(item)) &&
                (item.getChildItems().size() > 1) && !getTagValueFromTocItemVo(item.getChildItems().get(1)).equals(LIST)) ||
                ((item.getChildItems().size() == 2) && getTagValueFromTocItemVo(item.getChildItems().get(1)).equals(LIST))) {
            return item.getChildItems().get(0);
        } else if (getTagValueFromTocItemVo(item).equals(LIST) && item.getParentItem().getChildItems().indexOf(item) > 0) {
            return item.getParentItem().getChildItems().get(item.getParentItem().getChildItems().indexOf(item) - 1);
        }
        return item;
    }

    private boolean isDroppedAfterConclusion(TableOfContentItemVO targetItem, ItemPosition position) {
        List<TableOfContentItemVO> siblingsOfTargetItem = targetItem.getParentItem().getChildItems();
        int targetItemIndex = siblingsOfTargetItem.indexOf(targetItem);
        int clauseItemIndex = IntStream.range(0, siblingsOfTargetItem.size())
                .filter(i -> siblingsOfTargetItem.get(i).getTocItem().getAknTag().value().equals(CONCLUSIONS))
                .findFirst().orElse(-1);
        return (targetItemIndex > clauseItemIndex) ||
                ((targetItemIndex == clauseItemIndex) && !position.equals(ItemPosition.BEFORE));
    }

    private boolean isIndentAllowed(TreeData<TableOfContentItemVO> treeData, TableOfContentItemVO targetElement, int indentLevel) {
        boolean isAllowed = true;
        if (indentLevel < 0) {
            isAllowed = false;
        } else if (targetElement != null) {
            String tagValue = getTagValueFromTocItemVo(targetElement);
            isAllowed = isIndentAllowed(treeData, treeData.getParent(targetElement),
                    tagValue.equals(POINT) || tagValue.equals(INDENT) ? indentLevel - 1 : indentLevel);
        }
        return isAllowed;
    }

    private int getIndentLevel(TableOfContentItemVO element) {
        int identLevel = 0;
        for (TableOfContentItemVO child : element.getChildItems()) {
            identLevel = Math.max(identLevel, getIndentLevel(child));
        }
        String tagValue = getTagValueFromTocItemVo(element);
        return (tagValue.equals(POINT) || tagValue.equals(INDENT)) ? identLevel + 1 : identLevel;
    }

    private void moveOriginAttribute(final TableOfContentItemVO droppedElement, final TableOfContentItemVO targetElement) {
        if (isElementAndTargetOriginDifferent(droppedElement, targetElement)) {
            droppedElement.setOriginAttr(CN);
        }
        droppedElement.setOriginNumAttr(CN);
    }

    private boolean isNumbered(TableOfContentItemVO element) {
        boolean isNumbered = true;
        if (OptionsType.NONE.equals(element.getTocItem().getItemNumber())) {
            isNumbered = false;
        } else if (OptionsType.OPTIONAL.equals(element.getTocItem().getItemNumber())
                && ((element.getNumber() == null || element.getNumber().isEmpty()) || isNumSoftDeleted(element.getNumSoftActionAttr()))) {
            isNumbered = false;
        }
        return isNumbered;
    }

    private void setAffectedAttribute(TableOfContentItemVO dropData, TreeData<TableOfContentItemVO> treeData) {
        if (ELEMENTS_TO_BE_PROCESSED_FOR_NUMBERING.contains(getTagValueFromTocItemVo(dropData))) {
            TableOfContentItemVO parentItemVO = treeData.getParent(dropData);
            while (parentItemVO != null) {
                if (ELEMENTS_TO_BE_PROCESSED_FOR_NUMBERING.contains(getTagValueFromTocItemVo(parentItemVO))) {
                    parentItemVO.setAffected(true);
                    if (POINT_ROOT_PARENT_ELEMENTS.contains(getTagValueFromTocItemVo(parentItemVO))) {
                        break;
                    }
                }
                parentItemVO = treeData.getParent(parentItemVO);
            }
        }
    }

    private boolean containsNoSoftDeletedItem(TreeData<TableOfContentItemVO> treeData, TableOfContentItemVO tableOfContentItemVO) {
        boolean containsItem = false;
        for (TableOfContentItemVO item : treeData.getChildren(tableOfContentItemVO)) {
            if (item.getSoftActionAttr() == null ||
                    (!hasTocItemSoftAction(item, DELETE))) {
                containsItem = true;
            } else {
                containsItem = containsNoSoftDeletedItem(treeData, item);
            }
            if (containsItem) break;
        }
        return containsItem;
    }

    private TableOfContentItemVO copyMovingItemToTemp(TableOfContentItemVO originalItem, Boolean isSoftActionRoot, TreeGrid<TableOfContentItemVO> tocTree) {
        TableOfContentItemVO moveToItem;

        if (!ELEMENTS_WITHOUT_CONTENT.contains(originalItem.getTocItem().getAknTag().value().toLowerCase())) {
            moveToItem = new TableOfContentItemVO(originalItem.getTocItem(), TEMP_PREFIX + SOFT_MOVE_PLACEHOLDER_ID_PREFIX + originalItem.getId(), originalItem.getOriginAttr(), originalItem.getNumber(),
                    EC, originalItem.getHeading(), originalItem.getNode(),
                    originalItem.getList(), originalItem.getContent(), MOVE_TO, isSoftActionRoot, null, null);

            moveToItem.setNumSoftActionAttr(originalItem.getNumSoftActionAttr());
            moveToItem.setContent(originalItem.getContent());
            Iterator<TableOfContentItemVO> iterator = originalItem.getChildItems().iterator();
            while (iterator.hasNext()) {
                TableOfContentItemVO child = iterator.next();
                if (EC.equals(child.getOriginAttr()) &&
                        (!MOVE_FROM.equals(child.getSoftActionAttr()) && !MOVE_TO.equals(child.getSoftActionAttr())
                                && !ADD.equals(child.getSoftActionAttr()) && !DELETE.equals(child.getSoftActionAttr()))) {
                    moveToItem.addChildItem(copyMovingItemToTemp(child, Boolean.FALSE, tocTree));
                } else if (EC.equals(child.getOriginAttr()) && (MOVE_TO.equals(child.getSoftActionAttr()) || DELETE.equals(child.getSoftActionAttr()))) {
                    moveToItem.addChildItem(copyItemToTemp(child));
                    setAffectedAttribute(child, tocTree.getTreeData());
                    iterator.remove();
                    tocTree.getTreeData().removeItem(child);
                }
            }
        } else {
            moveToItem = new TableOfContentItemVO(originalItem.getTocItem(), TEMP_PREFIX + SOFT_MOVE_PLACEHOLDER_ID_PREFIX + originalItem.getId(), originalItem.getOriginAttr(), originalItem.getNumber(),
                    EC,  null, originalItem.getNode(), originalItem.getList(), originalItem.getContent(),
                    MOVE_TO, isSoftActionRoot,null, null);

        }
        moveToItem.setSoftMoveTo(originalItem.getId());
        moveToItem.setItemDepth(originalItem.getItemDepth());
        moveToItem.setOriginalDepth(originalItem.getOriginalDepth());
        originalItem.setSoftActionAttr(MOVE_FROM);
        originalItem.setSoftActionRoot(isSoftActionRoot);
        originalItem.setSoftMoveFrom(SOFT_MOVE_PLACEHOLDER_ID_PREFIX + originalItem.getId());
        return moveToItem;
    }

    private void handleMoveAction(TableOfContentItemVO moveFromItem, TreeGrid<TableOfContentItemVO> tocTree) {
        final TreeData<TableOfContentItemVO> container = tocTree.getTreeData();
        if ((moveFromItem.getOriginAttr() != null && moveFromItem.getOriginAttr().equals(EC)) &&
           ((moveFromItem.getSoftActionAttr() == null) || ((!hasTocItemSoftAction(moveFromItem, MOVE_FROM)) && (!hasTocItemSoftAction(moveFromItem, MOVE_TO))
           && (!hasTocItemSoftAction(moveFromItem, ADD)) && (!hasTocItemSoftAction(moveFromItem, DELETE))))) {

            TableOfContentItemVO moveToTemp = copyMovingItemToTemp(moveFromItem, Boolean.TRUE, tocTree);

            // Handles specific case while moving unnumbered paragraph together with numbered paragraphs
            if (Arrays.asList(PARAGRAPH, LEVEL).contains(getTagValueFromTocItemVo(moveFromItem)) && StringUtils.isEmpty(moveFromItem.getNumber())) {
                List<TableOfContentItemVO> moveFromSiblings = container.getParent(moveFromItem).getChildItems();
                if (moveFromSiblings.size()>0) {
                    TableOfContentItemVO refItem = moveFromSiblings.get(0);
                    if (StringUtils.isNotEmpty(refItem.getNumber())) {
                        moveFromItem.setNumber(StructureConfigUtils.HASH_NUM_VALUE);
                    }
                }
            }

            dropItemAtOriginalPosition(moveFromItem, moveToTemp, container);
            moveFromItem.setOriginNumAttr(CN);
            moveFromItem.setSoftActionRoot(Boolean.TRUE);

            TableOfContentItemVO moveToFinal = copyTempItemToFinalItem(moveToTemp);
            dropItemAtOriginalPosition(moveToTemp, moveToFinal, container);
            container.removeItem(moveToTemp);
            if (moveToTemp.getParentItem() != null) {
                moveToTemp.getParentItem().removeChildItem(moveToTemp);
            }
        }
        moveFromItem.setOriginNumAttr(CN);
        moveFromItem.setSoftActionRoot(Boolean.TRUE);

        setAffectedAttribute(moveFromItem, tocTree.getTreeData());
        if (MOVE_FROM.equals(moveFromItem.getSoftActionAttr())) {
            moveFromItem.setSoftMoveFrom(SOFT_MOVE_PLACEHOLDER_ID_PREFIX + moveFromItem.getId());
            TableOfContentItemVO moveToItem = getTableOfContentItemVOById(moveFromItem.getSoftMoveFrom(), container.getRootItems());
            if (moveToItem != null) {
                moveToItem.setSoftActionRoot(Boolean.TRUE);
                setAffectedAttribute(moveToItem, tocTree.getTreeData());
            }
        }
    }

    protected void setAttributesToOriginal(TableOfContentItemVO movedItem, TableOfContentItemVO originalItem) {
        super.setAttributesToOriginal(movedItem, originalItem);
        movedItem.setOriginNumAttr(EC);
    }

    private void updateMovedOnEmptyParent(final TableOfContentItemVO dropData, final TableOfContentItemVO targetItemVO,
            final String movedOntoType, final String movedElementType) {

        if (targetItemVO != null && targetItemVO.getTocItem().getAknTag().value().equals(movedOntoType) &&
                dropData != null && dropData.getTocItem().getAknTag().value().equals(movedElementType) &&
                !containsMovedElement(targetItemVO.getChildItems(), movedElementType)) {
            dropData.setMovedOnEmptyParent(true);
        }
    }

    private boolean containsMovedElement( List<TableOfContentItemVO> childItems, String movedElementType) {
        for(TableOfContentItemVO child: childItems) {
            if(child.getTocItem().getAknTag().value().equals(movedElementType) && (child.getNode() != null)) {
                return true;
            }
        }
        return false;
    }

    private boolean checkOriginParentTocITem(TableOfContentItemVO tableOfContentItemVO, String origin){
        return tableOfContentItemVO.getOriginAttr() != null && tableOfContentItemVO.getOriginAttr().equals(origin);
    }

    @Override
    public void propagateChangeListType(TableOfContentItemVO item, TocItem tocItem, List<NumberingConfig> numberingConfigs, MultiSelectTreeGrid<TableOfContentItemVO> tocTree) {
        propagateListType(findRootList(item), tocItem, numberingConfigs, tocTree);
    }

    private void propagateListType(List<TableOfContentItemVO> list, TocItem tocItem, List<NumberingConfig> numberingConfigs, MultiSelectTreeGrid<TableOfContentItemVO> tocTree) {
        String newNumberingValue = getNewNumberingFromListTocItem(list, tocItem, numberingConfigs);
        for (TableOfContentItemVO child: list) {
            if (getTagValueFromTocItemVo(child).equals(POINT) || getTagValueFromTocItemVo(child).equals(INDENT)) {
                child.setTocItem(tocItem);
                child.setNumber(newNumberingValue);
                tocTree.getDataProvider().refreshItem(child);
                propagateListType(findChildLists(child), tocItem, numberingConfigs, tocTree);
            }
        }
    }

    private List<TableOfContentItemVO> findChildLists(TableOfContentItemVO item) {
        List<TableOfContentItemVO> childLists = new ArrayList<>();
        List<TableOfContentItemVO> childItems = TableOfContentHelper.getChildrenWithTags(item, Arrays.asList(POINT, INDENT));
        if (!childItems.isEmpty()) {
            childLists.addAll(childItems);
        } else {
            for (TableOfContentItemVO child : item.getChildItems()) {
                if (getTagValueFromTocItemVo(child).equals(LIST)) {
                    childLists.addAll(TableOfContentHelper.getChildrenWithTags(child, Arrays.asList(POINT, INDENT)));
                }
            }
        }
        return childLists;
    }

    private List<TableOfContentItemVO> findRootList(TableOfContentItemVO item) {
        TableOfContentItemVO tmpItem = item;
        TableOfContentItemVO parentItem = item.getParentItem();
        while (parentItem != null && (getTagValueFromTocItemVo(parentItem).equals(LIST)
                || getTagValueFromTocItemVo(parentItem).equals(POINT)
                || getTagValueFromTocItemVo(parentItem).equals(INDENT))) {
            tmpItem = parentItem;
            parentItem = parentItem.getParentItem();
        }

        if (!getTagValueFromTocItemVo(tmpItem).equals(LIST)) {
            tmpItem = tmpItem.getParentItem();
        }
        return TableOfContentHelper.getChildrenWithTags(tmpItem, Arrays.asList(POINT, INDENT));
    }

    private String getNewNumberingFromListTocItem(List<TableOfContentItemVO> list, TocItem tocItem, List<NumberingConfig> numberingConfigs) {
        String sequence = StructureConfigUtils.HASH_NUM_VALUE;
        NumberingConfig numberingConfig = StructureConfigUtils.getNumberingConfig(numberingConfigs, tocItem.getNumberingType());
        if (!list.isEmpty() && !numberingConfig.isNumbered()) {
            TableOfContentItemVO firstChild = list.get(0);
            if (numberingConfig != null
                    && (numberingConfig.getLevels() == null || numberingConfig.getLevels().getLevels().isEmpty())) {
                sequence = numberingConfig.getPrefix()
                        + numberingConfig.getSequence()
                        + numberingConfig.getSuffix();
            } else if (numberingConfig != null && numberingConfig.getLevels() != null && !numberingConfig.getLevels().getLevels().isEmpty()) {
                int level = TableOfContentHelper.getItemIndentLevel(firstChild, 0, Arrays.asList(INDENT, POINT));
                if (level >= 0 && level < numberingConfig.getLevels().getLevels().size()) {
                    Level numberingLevel = numberingConfig.getLevels().getLevels().get(level);
                    NumberingConfig numberingConfigLevel = StructureConfigUtils.getNumberingConfig(numberingConfigs, numberingLevel.getNumberingType());
                    if (numberingConfigLevel != null) {
                        sequence = numberingConfigLevel.getPrefix()
                                + numberingConfigLevel.getSequence()
                                + numberingConfigLevel.getSuffix();
                    }
                }
            }
        }
        return sequence;
    }

    private boolean validateAgainstOtherIndentsInList(TableOfContentItemVO sourceItem, TableOfContentItemVO targetItem) {
        if (!getTagValueFromTocItemVo(targetItem).equals(INDENT)
                && !getTagValueFromTocItemVo(targetItem).equals(POINT)
                && !targetItem.getChildItems().isEmpty()) {
            for (TableOfContentItemVO child: targetItem.getChildItems()) {
                if (!validateAgainstOtherIndent(sourceItem, child)) {
                    return false;
                }
            }
        } else if (getTagValueFromTocItemVo(targetItem).equals(POINT)
                || getTagValueFromTocItemVo(targetItem).equals(INDENT)) {
            return validateAgainstOtherIndent(sourceItem, targetItem);
        } else if (getTagValueFromTocItemVo(targetItem).equals(LIST) && targetItem.getChildItems().isEmpty()) {
            return validateAgainstOtherIndentsInList(sourceItem, targetItem.getParentItem());
        }
        return true;
    }

    private boolean validateAgainstOtherIndent(TableOfContentItemVO sourceItem, TableOfContentItemVO targetItem) {
        NumberingType targetNumberingType = targetItem.getTocItem().getNumberingType();
        NumberingType sourceNumberingType = sourceItem.getTocItem().getNumberingType();
        return targetNumberingType.equals(sourceNumberingType);
    }

    @Override
    void setItemLevel(final TableOfContentItemVO sourceItem, final TableOfContentItemVO targetItem, final ItemPosition position) {
        int targetItemLevel = TableOfContentHelper.getItemIndentLevel(targetItem, 0, Arrays.asList(LEVEL, PARAGRAPH, INDENT, POINT));
        switch (position) {
            case AS_CHILDREN:
                if (targetItem.getTocItem().isRoot()) {
                    sourceItem.setIndentLevel(0);
                } else if (Arrays.asList(LEVEL, PARAGRAPH, INDENT, POINT, LIST).contains(getTagValueFromTocItemVo(targetItem))) {
                    sourceItem.setIndentLevel(targetItemLevel + 1);
                } else {
                    sourceItem.setIndentLevel(targetItemLevel);
                }
                break;
            case BEFORE:
                sourceItem.setIndentLevel(targetItemLevel);
                break;
            case AFTER:
                if (targetItem.getTocItem().isRoot()) {
                    sourceItem.setIndentLevel(0);
                } else {
                    sourceItem.setIndentLevel(targetItemLevel);
                }
        }
    }
}
