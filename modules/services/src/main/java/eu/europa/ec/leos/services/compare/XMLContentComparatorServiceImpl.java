/*
 * Copyright 2021 European Commission
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
package eu.europa.ec.leos.services.compare;

import com.google.common.base.Stopwatch;
import eu.europa.ec.leos.i18n.MessageHelper;
import eu.europa.ec.leos.model.action.SoftActionType;
import eu.europa.ec.leos.security.SecurityContext;
import eu.europa.ec.leos.services.compare.vo.Element;
import eu.europa.ec.leos.services.processor.content.XmlContentProcessor;
import eu.europa.ec.leos.services.support.XercesUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static eu.europa.ec.leos.services.compare.ComparisonHelper.buildElement;
import static eu.europa.ec.leos.services.compare.ComparisonHelper.isElementContentEqual;
import static eu.europa.ec.leos.services.compare.ComparisonHelper.isSoftAction;
import static eu.europa.ec.leos.services.compare.ComparisonHelper.withPlaceholderPrefix;
import static eu.europa.ec.leos.services.compare.IndentContentComparatorHelper.containsNotDeletedElementsInOtherContext;
import static eu.europa.ec.leos.services.compare.IndentContentComparatorHelper.hasIndentedChild;
import static eu.europa.ec.leos.services.compare.IndentContentComparatorHelper.isElementIndented;
import static eu.europa.ec.leos.services.compare.IndentContentComparatorHelper.isElementIndentedInOtherContext;
import static eu.europa.ec.leos.services.compare.IndentContentComparatorHelper.isElementRemovedInOtherContext;
import static eu.europa.ec.leos.services.compare.IndentContentComparatorHelper.isIndentedRenumbering;
import static eu.europa.ec.leos.services.compare.IndentContentComparatorHelper.isNewElementOutdentedFromOld;
import static eu.europa.ec.leos.services.compare.IndentContentComparatorHelper.isRemovedElementIndentedInNewContext;
import static eu.europa.ec.leos.services.compare.IndentContentComparatorHelper.shouldBeMarkedAsSoftAdded;
import static eu.europa.ec.leos.services.compare.IndentContentComparatorHelper.wasChildOfPreviousSibling;
import static eu.europa.ec.leos.services.support.XercesUtils.createXercesDocument;
import static eu.europa.ec.leos.services.support.XercesUtils.insertAttributeIfNotPresent;
import static eu.europa.ec.leos.services.support.XercesUtils.updateXMLIDAttribute;
import static eu.europa.ec.leos.services.support.XmlHelper.EMPTY_STRING;
import static eu.europa.ec.leos.services.support.XmlHelper.LEOS_DELETABLE_ATTR;
import static eu.europa.ec.leos.services.support.XmlHelper.LEOS_EDITABLE_ATTR;
import static eu.europa.ec.leos.services.support.XmlHelper.LEOS_SOFT_ACTION_ATTR;
import static eu.europa.ec.leos.services.support.XmlHelper.LEOS_SOFT_ACTION_ROOT_ATTR;
import static eu.europa.ec.leos.services.support.XmlHelper.LEOS_SOFT_DATE_ATTR;
import static eu.europa.ec.leos.services.support.XmlHelper.LEOS_SOFT_MOVE_FROM;
import static eu.europa.ec.leos.services.support.XmlHelper.LEOS_SOFT_MOVE_TO;
import static eu.europa.ec.leos.services.support.XmlHelper.LEOS_SOFT_USER_ATTR;
import static eu.europa.ec.leos.services.support.XmlHelper.NUM;
import static eu.europa.ec.leos.services.support.XmlHelper.SOFT_DELETE_PLACEHOLDER_ID_PREFIX;
import static eu.europa.ec.leos.services.support.XmlHelper.SOFT_MOVE_PLACEHOLDER_ID_PREFIX;
import static eu.europa.ec.leos.services.support.XmlHelper.SOFT_TRANSFORM_PLACEHOLDER_ID_PREFIX;
import static eu.europa.ec.leos.services.support.XmlHelper.UTF_8;
import static eu.europa.ec.leos.services.support.XmlHelper.XMLID;
import static eu.europa.ec.leos.services.support.XmlHelper.getDateAsXml;
import static eu.europa.ec.leos.services.support.XmlHelper.getSoftUserAttribute;

public abstract class XMLContentComparatorServiceImpl implements ContentComparatorService {

    private static final Logger LOG = LoggerFactory.getLogger(XMLContentComparatorServiceImpl.class);

    protected MessageHelper messageHelper;
    protected TextComparator textComparator;
    protected SecurityContext securityContext;
    protected XmlContentProcessor xmlContentProcessor;

    @Autowired
    public XMLContentComparatorServiceImpl(MessageHelper messageHelper, TextComparator textComparator,
                                           SecurityContext securityContext, XmlContentProcessor xmlContentProcessor) {
        this.messageHelper = messageHelper;
        this.textComparator = textComparator;
        this.securityContext = securityContext;
        this.xmlContentProcessor = xmlContentProcessor;
    }

    @Override
    public String compareContents(ContentComparatorContext context) {

        Stopwatch stopwatch = Stopwatch.createStarted();

        setupCompareElements(context, false);

        computeDifferencesAtNodeLevel(context);
        
        // LEOS-5819: re-generate soft action label attributes
        byte[] xmlContent = XercesUtils.nodeToByteArray(context.getResultNode());
        Document document = createXercesDocument(xmlContent);
        Node result = document.getFirstChild();
        xmlContentProcessor.updateSoftMoveLabelAttribute(result, LEOS_SOFT_MOVE_TO);
        xmlContentProcessor.updateSoftMoveLabelAttribute(result, LEOS_SOFT_MOVE_FROM);

        LOG.debug("Comparison finished!  ({} milliseconds)", stopwatch.elapsed(TimeUnit.MILLISECONDS));
        return XercesUtils.nodeToString(result);
    }

    private void resetResultNode(ContentComparatorContext context, Node node) {
        node = XercesUtils.importNodeInDocument(node.getOwnerDocument(), node);
        node.setTextContent(EMPTY_STRING);
        context.setResultNode(node);
    }

    protected void addToResultNode(ContentComparatorContext context, Node node) {
        if (node.getOwnerDocument() != context.getResultNode().getOwnerDocument()) {
            node = XercesUtils.importNodeInDocument(context.getResultNode().getOwnerDocument(), node);
        }
        context.getResultNode().appendChild(node);
    }

    private void computeDifferencesAtNodeLevel(ContentComparatorContext context) {

        if (shouldIgnoreElement(context.getOldContentRoot())) {
            return;
        }

        int oldContentChildIndex = 0; // current index in oldContentRoot children list
        int newContentChildIndex = 0; // current index in newContentRoot children list
        int intermediateContentChildIndex = 0; // current index in intermediateContentRoot children list

        while (context.getOldContentRoot() != null && oldContentChildIndex < context.getOldContentRoot().getChildren().size()
                && newContentChildIndex < context.getNewContentRoot().getChildren().size()) {

            context.setOldElement(context.getOldContentRoot().getChildren().get(oldContentChildIndex))
                    .setNewElement(context.getNewContentRoot().getChildren().get(newContentChildIndex))
                    .setIndexOfOldElementInNewContent(getBestMatchInList(context.getNewContentRoot().getChildren(), context.getOldElement()))
                    .setIndexOfNewElementInOldContent(getBestMatchInList(context.getOldContentRoot().getChildren(), context.getNewElement()));

            if(isElementIndexLessThanRootChildren(context.getIntermediateContentRoot(), intermediateContentChildIndex)) {
                context.setIntermediateElement(context.getIntermediateContentRoot().getChildren().get(intermediateContentChildIndex));
            }

            // at each step, check for a particular structural change in this order
            if (shouldIgnoreElement(context.getNewElement())) {
                newContentChildIndex++;
                if(context.getThreeWayDiff() && shouldIgnoreElement(context.getIntermediateElement())) {
                    intermediateContentChildIndex++;
                }
            } else if (shouldIgnoreElement(context.getOldElement())) {
                oldContentChildIndex++;
                if(context.getThreeWayDiff() && shouldIgnoreElement(context.getIntermediateElement())) {
                    intermediateContentChildIndex++;
                }
            }
            // Check if indented element removed from new context
            else if (context.getIndexOfOldElementInNewContent() == -1
                    && (!isElementIndented(context.getOldElement()) && isElementIndentedInOtherContext(context.getNewContentElements(), context.getOldElement())
                    || (isElementIndented(context.getOldElement()) && !isElementIndentedInOtherContext(context.getNewContentElements(), context.getOldElement())))) {
                // means that old element is not anymore child in new context and has been removed from new content root
                oldContentChildIndex++;
                if (context.getThreeWayDiff()) {
                    int indexOfIntermediateElementInNewContent = getBestMatchInList(context.getNewContentRoot().getChildren(), context.getIntermediateElement());
                    if (indexOfIntermediateElementInNewContent == -1
                            && (!isElementIndented(context.getIntermediateElement()) && isElementIndentedInOtherContext(context.getNewContentElements(), context.getIntermediateElement())
                            || (isElementIndented(context.getIntermediateElement()) && !isElementIndentedInOtherContext(context.getNewContentElements(), context.getIntermediateElement())))) {
                        // means that intermediate element is not anymore child in new context and has been removed from new content root
                        intermediateContentChildIndex++;
                    }
                } else if (isElementRemovedInOtherContext(context.getNewContentElements(), context.getOldElement())
                        && !containsNotDeletedElementsInOtherContext(context.getNewContentElements(), context.getOldElement())) {
                    appendRemovedContent(context);
                }
            } else if (context.getThreeWayDiff() && context.getIntermediateContentRoot() != null && getBestMatchInList(context.getIntermediateContentRoot().getChildren(), context.getOldElement()) == -1
                    && ((!isElementIndented(context.getOldElement()) && isElementIndentedInOtherContext(context.getIntermediateContentElements(), context.getOldElement()))
                    || (isElementIndented(context.getOldElement()) && !isElementIndentedInOtherContext(context.getIntermediateContentElements(), context.getOldElement())))) {
                // means that old element is not anymore child in intermediate context and has been removed from intermediate content root
                oldContentChildIndex++;
            } else if (context.getThreeWayDiff() && context.getIntermediateElement() != null && getBestMatchInList(context.getNewContentRoot().getChildren(), context.getIntermediateElement()) == -1 && context.getIndexOfNewElementInOldContent() != -1 && context.getIndexOfOldElementInNewContent() != -1
                    && (!isElementIndented(context.getIntermediateElement()) && isElementIndentedInOtherContext(context.getNewContentElements(), context.getIntermediateElement())
                    || (isElementIndented(context.getIntermediateElement()) && !isElementIndentedInOtherContext(context.getNewContentElements(), context.getIntermediateElement())))) {
                // means that intermediate element is not anymore child in new context and has been removed from new content root
                intermediateContentChildIndex++;
            }
            // Check if indented element added in new context
            else if (context.getIndexOfNewElementInOldContent() == -1
                    && (!isElementIndented(context.getNewElement()) && isElementIndentedInOtherContext(context.getOldContentElements(), context.getNewElement())
                    || (isElementIndented(context.getNewElement()) && !isElementIndentedInOtherContext(context.getOldContentElements(), context.getNewElement())))) {
                // means that new element is added as child in new context
                if ((!isSoftAction(context.getOldElement().getNode(), SoftActionType.MOVE_FROM)
                        && isElementRemovedInOtherContext(context.getNewContentElements(), context.getOldElement()))
                        && (!isElementIndented(context.getNewElement()) ||
                        !wasChildOfPreviousSibling(context.getNewElement(), context, newContentChildIndex)) &&
                        !isNewElementOutdentedFromOld(context)) {
                    appendRemovedElementContentIfRequired(context);
                    oldContentChildIndex++;
                }
                appendAddedElementContent(context);
                newContentChildIndex++;
                if (context.getThreeWayDiff()) {
                    int indexOfIntermediateElementInOldContent = getBestMatchInList(context.getOldContentRoot().getChildren(), context.getIntermediateElement());
                    if (indexOfIntermediateElementInOldContent == -1 && context.getIntermediateElement() != null
                            && (!isElementIndented(context.getIntermediateElement()) && isElementIndentedInOtherContext(context.getOldContentElements(), context.getIntermediateElement())
                            || (isElementIndented(context.getIntermediateElement()) && !isElementIndentedInOtherContext(context.getOldContentElements(), context.getIntermediateElement())))) {
                        // means that intermediate element is added as child in intermediate context
                        if (!isSoftAction(context.getOldElement().getNode(), SoftActionType.MOVE_FROM) && isElementRemovedInOtherContext(context.getIntermediateContentElements(), context.getOldElement())) {
                            appendRemovedElementContentIfRequired(context);
                            oldContentChildIndex++;
                        }
                        intermediateContentChildIndex++;
                    }
                }
            } else if (context.getThreeWayDiff() && context.getIntermediateContentRoot() != null && getBestMatchInList(context.getIntermediateContentRoot().getChildren(), context.getNewElement()) == -1
                    && ((!isElementIndented(context.getNewElement()) && isElementIndentedInOtherContext(context.getIntermediateContentElements(), context.getNewElement()))
                    || (isElementIndented(context.getNewElement()) && !isElementIndentedInOtherContext(context.getIntermediateContentElements(), context.getNewElement())))) {
                // means that new element is added as child in new context
                if (!isSoftAction(context.getIntermediateElement().getNode(), SoftActionType.MOVE_FROM) && isElementRemovedInOtherContext(context.getNewContentElements(), context.getIntermediateElement())) {
                    appendIntermediateRemovedElement(context, intermediateContentChildIndex);
                    intermediateContentChildIndex++;
                } else if (isElementIndented(context.getIntermediateElement()) && context.getIntermediateElement().getTagId().contains(context.getNewElement().getTagId())) {
                    intermediateContentChildIndex++;
                }
                appendAddedElementContent(context);
                newContentChildIndex++;
            } else if (newContentChildIndex == context.getIndexOfOldElementInNewContent()
                    && (!context.getDisplayRemovedContentAsReadOnly()
                    || shouldCompareElements(context.getOldElement(), context.getNewElement())
                    && shouldCompareElements(context.getNewElement(), context.getOldElement()))) {

                if (context.getThreeWayDiff() && isIntermediateElementRemovedInNewContent(context)) {
                    if (isElementIndexLessThanRootChildren(context.getIntermediateContentRoot(), intermediateContentChildIndex)) {
                        // LEOS-4392 If old and new contents are same check if structure elements are added/moved/transformed in
                        // intermediate version but reverted in new version for three way diff.
                        if(!isCurrentElementNonIgnored(context.getIntermediateElement().getNode())) {
                            compareRevertedChanges(context);
                        }
                        intermediateContentChildIndex++;

                        if (intermediateContentChildIndex < context.getIntermediateContentRoot().getChildren().size() &&
                                isCurrentElementIgnoredInNewContent(context)) {
                            // There are still children to process in intermediate
                            oldContentChildIndex++;
                            newContentChildIndex++;
                        }
                    } else { //No more children in intermediate check for remaining child elements in new/old versions
                        if (isIgnoredElement(context.getIntermediateElement()) && !isCurrentElementIgnoredInNewContent(context)) {
                            // LEOS-4392: compare contents of old and new only as element was moved in intermediate but
                            // restored in new so no change needs to be displayed for its child elements just print as is
                            compareElementContents(new ContentComparatorContext.Builder(context)
                                    .withThreeWayDiff(false)
                                    .build());
                        }
                        oldContentChildIndex++;
                        newContentChildIndex++;
                    }
                } else {
                    if (isThreeWayDiffEnabled(context) && isAddedNonIgnoredElement(context.getIntermediateElement().getNode()) &&
                            !context.getNewElement().getTagId().equalsIgnoreCase(context.getIntermediateElement().getTagId()) &&
                            isElementIndexLessThanRootChildren(context.getIntermediateContentRoot(), intermediateContentChildIndex)) {
                        // Ignore soft added or move_from structured action in intermediate as it is already reverted in new version
                        intermediateContentChildIndex++;
                    } else {
                        // element did not changed relative position so check if it's content is changed and should be compared
                        if (context.getThreeWayDiff()) {
                            appendIndentedAndRemovedIntermediateParent(context, context.getOldElement());
                        }
                        if (!isElementIndexLessThanRootChildren(context.getIntermediateContentRoot(), intermediateContentChildIndex)) {
                            context.setIntermediateElement(null);
                        }
                        compareElementContents(context);
                        oldContentChildIndex++;
                        newContentChildIndex++;
                        if (isElementIndexLessThanRootChildren(context.getIntermediateContentRoot(), intermediateContentChildIndex)) {
                            intermediateContentChildIndex++;
                        }
                    }
                }
            } else if (context.getIndexOfNewElementInOldContent() < 0 && context.getIndexOfOldElementInNewContent() < 0) {
                // oldElement was completely replaced with newElement
                boolean newContentIncremented = false;
                int ignoredElementIndex = getIndexOfIgnoredElementInNewContent(context);
                if (shouldAppendAddedElement(context, oldContentChildIndex, newContentChildIndex, ignoredElementIndex)) {
                    appendAddedElementContent(context);
                    newContentChildIndex++;
                    newContentIncremented = true;
                } else {
                    if (ignoredElementIndex != -1) {
                        appendRemovedElementContentIfRequired(context);
                    } else if (isElementUnWrapped(context)) {
                        appendAddedElementContent(context);
                        newContentChildIndex++;
                    } else if (oldContentChildIndex == newContentChildIndex) {
                        appendAddedElementContentIfRequired(context);
                        appendRemovedElementContentIfRequired(context);
                        newContentChildIndex++;
                    }
                    oldContentChildIndex++;
                }
                if ((shouldIncrementIntermediateIndex(context) && newContentIncremented) || containsDeletedElementInNewContent(context)) {
                    intermediateContentChildIndex++;
                }
            } else if (context.getIndexOfNewElementInOldContent() >= oldContentChildIndex && context.getIndexOfOldElementInNewContent() > newContentChildIndex) {
                // newElement appears to be moved backward to newContentChildIndex and oldElement appears to be moved forward from oldContentChildIndex
                // at the same time
                // so display the element that was moved more positions because it's more likely to be the action the user actually made
                if ((context.getIndexOfNewElementInOldContent() - oldContentChildIndex > context.getIndexOfOldElementInNewContent() - newContentChildIndex)
                        || context.getDisplayRemovedContentAsReadOnly() && !shouldCompareElements(context.getOldElement(), context.getNewElement())){
                    // newElement was moved backward to newContentChildIndex more positions than oldElement was moved forward from oldContentChildIndex
                    // or the newElement should not be compared with the oldElement
                    // so display the added newElement in the new location for now
                    boolean shouldIncrementIntermediateIndex = shouldIncrementIntermediateIndex(context);
                    appendAddedElementContent(context);
                    newContentChildIndex++;
                    if(shouldIncrementIntermediateIndex) {
                        intermediateContentChildIndex = incrementIntermediateIndexIfRequired(context, context.getNewElement(), intermediateContentChildIndex);
                    }
                } else {
                    // oldElement was moved forward from oldContentChildIndex more or just as many positions as newElement was moved backward to newContentChildIndex
                    // so display the removed oldElement in the original location for now
                    appendRemovedElementContentIfRequired(context);
                    oldContentChildIndex++;
                    intermediateContentChildIndex = incrementIntermediateIndexIfRequired(context, context.getOldElement(), intermediateContentChildIndex);
                }
            } else if (context.getIndexOfNewElementInOldContent() >= 0 && context.getIndexOfNewElementInOldContent() < oldContentChildIndex) {
                // newElement was moved forward to newContentChildIndex and the removed oldElement is already displayed
                // in the original location so display the added newElement in the new location also
                boolean shouldIncrementIntermediateIndex = shouldIncrementIntermediateIndex(context);
                appendAddedElementContent(context);
                newContentChildIndex++;
                if(shouldIncrementIntermediateIndex) {
                    intermediateContentChildIndex = incrementIntermediateIndexIfRequired(context, context.getNewElement(), intermediateContentChildIndex);
                }
            } else if (context.getIndexOfOldElementInNewContent() >= 0 &&
                    context.getIndexOfOldElementInNewContent() < newContentChildIndex &&
                    !(context.getIndexOfNewElementInOldContent() < 0)) {
                // oldElement was moved backward from oldContentChildIndex and the added newElement is already displayed
                // in the new location so display the removed oldElement in the original location also provided there is
                // no new element added in the new content before it.
                // Is Moved To element has been indented, then skip it ?
                if (!isRemovedElementIndentedInNewContext(context, context.getOldElement())) {
                    appendRemovedElementContentIfRequired(context);
                }
                oldContentChildIndex++;
                intermediateContentChildIndex = incrementIntermediateIndexIfRequired(context, context.getOldElement(), intermediateContentChildIndex);
            } else if ((context.getIndexOfNewElementInOldContent() < 0 && getIndexOfIgnoredElementInNewContent(context) < 0)
                    || shouldAppendAddedElement(context, oldContentChildIndex, newContentChildIndex, getIndexOfIgnoredElementInNewContent(context))) {
                // newElement is simply added or added before deleted or moved element so display the added element
                boolean shouldIncrementIntermediateIndex = shouldIncrementIntermediateIndex(context);
                appendAddedElementContent(context);
                newContentChildIndex++;
                if (shouldIncrementIntermediateIndex) {
                    intermediateContentChildIndex = incrementIntermediateIndexIfRequired(context, context.getNewElement(), intermediateContentChildIndex);
                }
            } else if (isElementRemovedInOtherContext(context.getNewContentElements(), context.getOldElement())
                    && isSoftAction(context.getNewElement().getNode(), SoftActionType.MOVE_FROM)
                    && hasIndentedChild(context.getNewElement())) {
                // Particular case where Move To and Move From element are together at the same place
                // that could happen when elements are indented to the move from element
                if (oldContentChildIndex > newContentChildIndex) {
                    appendAddedElementContentIfRequired(context);
                    appendRemovedElementContentIfRequired(context);
                } else {
                    appendRemovedElementContentIfRequired(context);
                    appendAddedElementContentIfRequired(context);
                }
                intermediateContentChildIndex++;
                oldContentChildIndex++;
                newContentChildIndex++;
            } else {
                // oldElement was deleted or moved so only display the removed element
                appendRemovedElementContentIfRequired(context);
                oldContentChildIndex++;
                intermediateContentChildIndex = incrementIntermediateIndexIfRequired(context, context.getOldElement(), intermediateContentChildIndex);
            }
        }

        if (context.getOldContentRoot() != null && oldContentChildIndex < context.getOldContentRoot().getChildren().size()) {
            // there are still children in the old root that have not been processed
            // it means they were all moved backward or under a different parent or deleted
            // so display the removed children
            for (int i = oldContentChildIndex; i < context.getOldContentRoot().getChildren().size(); i++) {
                Element oldElementChild = context.getOldContentRoot().getChildren().get(i);
                context.setOldElement(oldElementChild);
                ContentComparatorContext newContext = context;

                if (context.getThreeWayDiff()) {
                    Element intermediateElementChild = context.getIntermediateElement();
                    if (isElementIndexLessThanRootChildren(context.getIntermediateContentRoot(), i)) {
                        intermediateElementChild = context.getIntermediateContentRoot().getChildren().get(i);
                        newContext = new ContentComparatorContext.Builder(context)
                                .withIntermediateElement(intermediateElementChild)
                                .build();
                    }
                }

                // In case of out/indentation the indented children should not be added as removed elements
                if(!shouldIgnoreElement(oldElementChild)
                        && !containsNotDeletedElementsInOtherContext(context.getNewContentElements(), oldElementChild)
                        && isElementRemovedInOtherContext(context.getNewContentElements(), oldElementChild)) {
                    appendRemovedElementContentIfRequired(newContext);
                }
            }
        } else if (newContentChildIndex < context.getNewContentRoot().getChildren().size()) {
            // there are still children in the new root that have not been processed
            // it means they were all moved forward or from a different parent or added
            // so display the added children
            int newContentIndexForChildren = newContentChildIndex;
            int intermediateContentIndexForChildren = intermediateContentChildIndex;
            while ((isElementIndexLessThanRootChildren(context.getNewContentRoot(), newContentIndexForChildren))) {
                Element newElementChild = context.getNewContentRoot().getChildren().get(newContentIndexForChildren);
                // append removed parent
                if (newContentIndexForChildren >= intermediateContentIndexForChildren) {
                    appendIndentedAndRemovedParent(context, newElementChild);
                }

                if(context.getThreeWayDiff() && isElementIndexLessThanRootChildren(context.getIntermediateContentRoot(), intermediateContentIndexForChildren)) {
                    Element intermediateElementChild = context.getIntermediateContentRoot().getChildren().get(intermediateContentIndexForChildren);
                    context.setIntermediateElement(intermediateElementChild);
                    //there are children added in intermediate and kept in new content as well.If any children
                    //deleted in the new content then display the removed content in three way diff as deleted.
                    if(intermediateElementChild != null && !newElementChild.getTagId().equalsIgnoreCase(intermediateElementChild.getTagId())
                            && !context.getNewContentElements().containsKey(intermediateElementChild.getTagId())
                            && !isElementIndentedInOtherContext(context.getNewContentElements(), intermediateElementChild)) {
                        appendRemovedContent(new ContentComparatorContext.Builder(context)
                                .withOldElement(intermediateElementChild)
                                .withOldContentNode(context.getIntermediateContentNode())
                                .build());
                        intermediateContentIndexForChildren++;
                        continue; //skip to next iteration till deleted content is fully displayed
                    }
                }
                if(!shouldIgnoreElement(newElementChild)) {
                    appendAddedElementContent(context.setIndexOfOldElementInNewContent(newContentIndexForChildren).setNewElement(newElementChild));
                } else if (shouldIgnoreElement(newElementChild) && hasIndentedChild(context.getNewContentRoot())) {
                    // Case when move to elements have been indented
                    appendIndentedDeletedOrMovedToContent(context, newElementChild);
                }
                newContentIndexForChildren++;
                intermediateContentIndexForChildren = incrementIntermediateIndexIfRequired(context, newElementChild, intermediateContentIndexForChildren);

            }
            if(context.getThreeWayDiff() && isElementIndexLessThanRootChildren(context.getIntermediateContentRoot(), intermediateContentIndexForChildren)) {
                appendIntermediateRemovedElement(context, intermediateContentIndexForChildren);
            }
        } else if (context.getThreeWayDiff() && isElementIndexLessThanRootChildren(context.getIntermediateContentRoot(), intermediateContentChildIndex)) {
            appendIntermediateRemovedElement(context, intermediateContentChildIndex);
        }
    }

    // There are still children remaining in the intermediate but not present in old and new content
    // it means that new element is added in intermediate and removed in current so it should be shown as deleted in comparison
    private void appendIntermediateRemovedElement(ContentComparatorContext context, int intermediateContentIndexForChildren) {
        for (int i = intermediateContentIndexForChildren; i  < context.getIntermediateContentRoot().getChildren().size(); i++) {
            Element intermediateElementChild = context.getIntermediateContentRoot().getChildren().get(i);
            if (!isCurrentElementNonIgnored(intermediateElementChild.getNode())
                    && !containsNotDeletedElementsInOtherContext(context.getNewContentElements(), intermediateElementChild)
                    && isElementRemovedInOtherContext(context.getNewContentElements(), intermediateElementChild)) {
                appendRemovedContent(new ContentComparatorContext.Builder(context)
                        .withOldElement(intermediateElementChild)
                        .withOldContentNode(context.getIntermediateContentNode())
                        .build());
            }
        }
    }

    private boolean isNewAddedElement(ContentComparatorContext context, int oldContentChildIndex, int newContentChildIndex, int ignoredElementIndex) {
        return ((isAddedElement(context.getNewElement().getNode()) || isAddedNonIgnoredElement(context.getNewElement().getNode()))
                && ignoredElementIndex > oldContentChildIndex && ignoredElementIndex > newContentChildIndex);
    }

    private boolean shouldAppendAddedElement(ContentComparatorContext context, int oldContentChildIndex, int newContentChildIndex, int ignoredElementIndex) {
        return (isAddedNonIgnoredElement(context.getNewElement().getNode())
                && ignoredElementIndex > oldContentChildIndex && ignoredElementIndex > newContentChildIndex);
    }

    protected final void compareElementContents(ContentComparatorContext context) {
        Node node = getNodeFromElement(context.getNewElement());
        if (!isActionRoot(node) && !containsAddedNonIgnoredElements(node)
                && ((isElementContentEqual(context) && !containsIgnoredElements(node)) || (context.getIgnoreRenumbering() && !isIndentedRenumbering(context.getOldElement(), context.getNewElement()) && shouldIgnoreRenumbering(context.getNewElement())))) {
            if (context.getThreeWayDiff()) {
                node = buildNodeForAddedElement(context.getNewElement(), context.getIntermediateElement(), context);
            } else if (!(isElementContentEqual(context) && !containsIgnoredElements(node)) && context.getStartTagAttrName() != null) {
                XercesUtils.insertOrUpdateAttributeValue(node, context.getStartTagAttrName(), context.getStartTagAttrValue());
            }
            addToResultNode(context, node);
        } else if (!shouldIgnoreElement(context.getOldElement()) && (!context.getIgnoreElements() || !shouldIgnoreElement(context.getNewElement()))) {
            //add the start tag
            if (isThreeWayDiffEnabled(context)) {
                if (isElementContentEqual(context) && !containsIgnoredElements(node)) {
                    node = buildNode(context.getNewElement());
                } else if(!shouldIgnoreElement(context.getNewElement()) && ((isAddedNonIgnoredElement(context.getIntermediateElement().getNode()) &&
                        !shouldIgnoreElement(context.getIntermediateElement()) && !isIgnoredElement(context.getIntermediateElement())) ||
                        shouldIgnoreElement(context.getIntermediateElement()))) { // build start tag for moved/added element with added styles
                    node = buildNodeForAddedElement(context);
                } else if(shouldIgnoreElement(context.getNewElement())) {
                    node = buildNodeForRemovedElement(context.getNewElement(), context, context.getIntermediateContentElements());
                } else if (isActionRoot(node)) {
                    node = buildNodeForAddedElement(context);
                } else if (context.getOldElement() == null && !shouldIgnoreElement(context.getNewElement()) && !shouldIgnoreElement(context.getIntermediateElement())) { //build start tag for added element in intermediate
                    node = buildNodeForAddedElement(context);
                } else if (context.getNewElement() != null && context.getIntermediateElement() == null && context.getOldElement() == null) {
                    node = buildNode(context.getNewElement(), context.getAttrName(), context.getAddedIntermediateValue());
                } else {
                    node = buildNode(context.getNewElement()); //build tag for children without styles
                }
            } else {
                node = buildNode(context.getNewElement(), context.getStartTagAttrName(), context.getStartTagAttrValue());
            }

            context.resetStartTagAttribute();

            if (context.getNewElement().getTagName().equalsIgnoreCase(NUM) && shouldIgnoreElement(context.getNewElement())) {
                node.setTextContent(getRemovedNumContent(context));
                addToResultNode(context, node);
            } else if ((context.getNewElement() != null && context.getNewElement().hasTextChild()) || (context.getOldElement() != null && context.getOldElement().hasTextChild())) {
                String oldContent = XercesUtils.getContentNodeAsXmlFragment(getNodeFromElement(context.getOldElement()));
                String newContent = XercesUtils.getContentNodeAsXmlFragment(getNodeFromElement(context.getNewElement()));
                String intermediateContent = null;
                if (isThreeWayDiffEnabled(context) && context.getIntermediateElement().hasTextChild()) {
                    intermediateContent = XercesUtils.getContentNodeAsXmlFragment(context.getIntermediateElement().getNode());
                }
                String result;
                try {
                    result = textComparator.compareTextNodeContents(oldContent, newContent, intermediateContent, context);
                    result = correctFormulasEscaping(result, context);
                } catch (Exception e) {
                    LOG.error("Failure during text comparison. Exception thrown from text diffing library ", e);
                    result = messageHelper.getMessage("leos.version.compare.error.message");
                }
                result = "<fake xmlns:leos=\"urn:eu:europa:ec:leos\">" + result + "</fake>";
                Node comparedContentNode = XercesUtils.createNodeFromXmlFragment(node.getOwnerDocument(), result.getBytes(UTF_8));
                node.setTextContent(EMPTY_STRING);
                node = XercesUtils.copyContent(comparedContentNode, node);
                addToResultNode(context, node);
            } else {
                node = XercesUtils.importNodeInDocument(context.getResultNode().getOwnerDocument(), node);
                node.setTextContent(EMPTY_STRING);
                ContentComparatorContext newContext = new ContentComparatorContext.Builder(context)
                        .withOldContentRoot(context.getOldElement())
                        .withNewContentRoot(context.getNewElement())
                        .withIntermediateContentRoot(context.getIntermediateElement())
                        .withResultNode(node)
                        .build();
                addToResultNode(context, node);
                computeDifferencesAtNodeLevel(newContext);
            }
        } else if (shouldDisplayRemovedContent(context.getOldElement(), context.getIndexOfOldElementInNewContent())) {
            //element removed in the new version
            appendRemovedElementContent(context);
        }
    }

    private String correctFormulasEscaping(String result, ContentComparatorContext context) {
        // Detect when the diff result contains a formula to avoid escaping of special characters - LEOS-6058
        int inlineFormulaIndex = result.indexOf("name=\"math-tex\"");
        boolean resultIsFormula = (context.getNewElement() != null && context.getNewElement().getTagName().equalsIgnoreCase("inline") &&
                context.getNewElement().getNode().getAttributes() != null &&
                context.getNewElement().getNode().getAttributes().getNamedItem("name") != null &&
                context.getNewElement().getNode().getAttributes().getNamedItem("name").getNodeValue().equalsIgnoreCase("math-tex"));
        if (resultIsFormula) {
            result = result.replaceAll("\\\\\\\\", "\\\\");
        } else if (inlineFormulaIndex > 0) {
            while (inlineFormulaIndex > 0) {
                int inlineFormulaStart = result.indexOf(">", inlineFormulaIndex)+1;
                int inlineFormulaEnd = result.indexOf("</inline", result.indexOf("name=\"math-tex\""));
                String formula = result.substring(inlineFormulaStart, inlineFormulaEnd);
                formula = formula.replaceAll("\\\\\\\\", "\\\\");
                result = result.substring(0, inlineFormulaStart) + formula + result.substring(inlineFormulaEnd);
                inlineFormulaIndex = result.indexOf("name=\"math-tex\"", inlineFormulaEnd);
            }
        }
        return result;
    }

    private Node getNodeFromElement(Element element) {
        Node node = null;
        if (element != null) {
            node = element.getNode();
        }
        return node;
    }

    private boolean isThreeWayDiffEnabled(ContentComparatorContext context) {
        return context.getThreeWayDiff() && context.getIntermediateElement() != null;
    }

    private boolean shouldIncrementIntermediateIndex(ContentComparatorContext context) {
        boolean shouldIncrementIndex;
        if (isThreeWayDiffEnabled(context)) {
            if (context.getOldElement().getTagId().equalsIgnoreCase(context.getIntermediateElement().getTagId()) ||
                    !context.getNewElement().getTagId().equalsIgnoreCase(context.getIntermediateElement().getTagId())) {
                shouldIncrementIndex = false;
            } else {
                shouldIncrementIndex = true;
            }
        } else {
            shouldIncrementIndex = false;
        }
        return shouldIncrementIndex;
    }

    private int incrementIntermediateIndexIfRequired(ContentComparatorContext context, Element element, int intermediateContentChildIndex) {
        if(isThreeWayDiffEnabled(context) &&
                element.getTagId().equalsIgnoreCase(context.getIntermediateElement().getTagId()) || shouldIgnoreElement(context.getIntermediateElement())) {
            intermediateContentChildIndex++;
        }
        return intermediateContentChildIndex;
    }

    private void compareRevertedChanges(ContentComparatorContext context) {
        //LEOS-4392 If structure is different in intermediate, update context by setting old = intermediate
        //and compare with new version like two version diff
        ContentComparatorContext newContext = new ContentComparatorContext.Builder(context)
                .withOldElement(context.getIntermediateElement())
                .withOldContentRoot(context.getIntermediateContentRoot())
                .withOldContentElements(context.getIntermediateContentElements())
                .withOldContentNode(context.getIntermediateContentNode())
                .build();
        appendRemovedElementContentIfRequired(newContext);
        if(shouldAddElement(newContext.getOldElement(), newContext.getNewContentElements()) && !containsDeletedElementInNewContent(newContext)) {
            appendAddedElementContentIfRequired(newContext);
        }
    }

    protected Node buildNode(Element element, String attrName, String attrValue) {
        Node node = buildNode(element);
        // Indented should be considered as added
        if ((attrName != null) && shouldBeMarkedAsSoftAdded(element, attrValue)) {
            XercesUtils.insertOrUpdateAttributeValue(node, attrName, attrValue);
        }
        return node;
    }

    protected Node buildNode(Element element) {
        Node node = element.getNode();
        if (shouldIgnoreElement(element)) {
            // add read-only attributes
            addReadOnlyAttributes(node);
        }
        return node;
    }

    protected void addReadOnlyAttributes(Node node) {
        XercesUtils.insertOrUpdateAttributeValue(node, LEOS_DELETABLE_ATTR, Boolean.FALSE.toString());
        XercesUtils.insertOrUpdateAttributeValue(node, LEOS_EDITABLE_ATTR, Boolean.FALSE.toString());
    }

    protected final void appendRemovedElementContentIfRequired(ContentComparatorContext context) {
        if (shouldIgnoreElement(context.getNewContentRoot()) || isElementInItsOriginalPosition(context.getNewContentRoot())) {
            appendRemovedElementContent(context);
        } else if (!isElementMovedOrTransformed(context.getNewElement()) || isCurrentElementNonIgnored(getNodeFromElement(context.getOldElement()))) {
            appendRemovedContent(context);
        }
    }

    protected final void appendAddedElementContentIfRequired(ContentComparatorContext context) {
        appendAddedElementContent(context);
    }

    protected final boolean isElementRemovedFromContent(int indexOfOldElementInNewContent){
        return indexOfOldElementInNewContent == -1;
    }

    protected boolean isActionRoot(Node node) {
        return XercesUtils.containsAttributeWithValue(node, LEOS_SOFT_ACTION_ROOT_ATTR, Boolean.TRUE.toString());
    }

    protected boolean shouldIgnoreRenumbering(Element element) {
        return NUM.equals(element.getTagName())
                && (isSoftAction(element.getParent().getNode(), SoftActionType.MOVE_FROM)
                || isSoftAction(element.getParent().getNode(), SoftActionType.ADD));
    }

    protected boolean isElementInItsOriginalPosition(Element element) {
        return element == null ||
                (!isSoftAction(element.getNode(), SoftActionType.ADD)
                        && !isSoftAction(element.getNode(), SoftActionType.TRANSFORM)
                        && !isSoftAction(element.getNode(), SoftActionType.MOVE_FROM));
    }

    protected boolean isElementMovedOrTransformed(Element element) {
        return isSoftAction(element.getNode(), SoftActionType.MOVE_FROM) || isSoftAction(element.getNode(), SoftActionType.TRANSFORM);
    }

    protected abstract boolean shouldIgnoreElement(Element element);

    private int indexInParent(Element element) {
        return element.getParent().getChildren().indexOf(element);
    }

    protected Node getChangedElementContent(Node contentNode, Element element, String attrName, String attrValue) {
        Node node = null;
        if (!shouldIgnoreElement(element)) {
            node = XercesUtils.getElementById(contentNode, element.getTagId());
            if (attrName != null && attrValue != null) {
                XercesUtils.insertOrUpdateAttributeValue(node, attrName, attrValue);
            }
        }
        return node;
    }

    protected boolean containsSoftDeleteElement(Element element, Map<String, Element> contentElements) {
        return element != null && contentElements.containsKey(SOFT_DELETE_PLACEHOLDER_ID_PREFIX + element.getTagId());
    }

    protected boolean containsSoftMoveToTransformedElement(Map<String, Element> contentElements, Element element) {
        return element != null && (contentElements.containsKey(SOFT_MOVE_PLACEHOLDER_ID_PREFIX + SOFT_TRANSFORM_PLACEHOLDER_ID_PREFIX + element.getParent().getTagId())
                && contentElements.containsKey(SOFT_TRANSFORM_PLACEHOLDER_ID_PREFIX + element.getParent().getTagId())
                && !shouldIgnoreElement(contentElements.get(SOFT_TRANSFORM_PLACEHOLDER_ID_PREFIX + element.getParent().getTagId())));
    }

    protected boolean containsSoftTransformedElement(Map<String, Element> contentElements, Element element) {
        return element != null && element.getTagId().contains(SOFT_TRANSFORM_PLACEHOLDER_ID_PREFIX) &&
                contentElements.containsKey(element.getTagId().replace(SOFT_TRANSFORM_PLACEHOLDER_ID_PREFIX, EMPTY_STRING));

    }
    protected boolean containsSoftMoveToElement(Map<String, Element> contentElements, Element element) {
        return element != null && (contentElements.containsKey(SOFT_MOVE_PLACEHOLDER_ID_PREFIX + element.getTagId())
                && contentElements.containsKey(element.getTagId()) && !shouldIgnoreElement(contentElements.get(element.getTagId())));
    }

    protected boolean isAddedNonIgnoredElement(Node node) {
        return isSoftAction(node, SoftActionType.MOVE_FROM) || isSoftAction(node, SoftActionType.ADD);
    }

    protected boolean isAddedElement(Node node) {
        if(node != null) {
            String attrValue = XercesUtils.getAttributeValue(node, XMLID);
            return attrValue == null;
        }
        return false;
    }

    protected boolean containsAddedNonIgnoredElements(Node node) {
        boolean containsAddedNonIgnoredElement = false;
        if (isAddedNonIgnoredElement(node)) {
            containsAddedNonIgnoredElement = true;
        } else {
            NodeList nodeList = node.getChildNodes();
            for (int i = 0; i < nodeList.getLength(); i++) {
                containsAddedNonIgnoredElement = containsAddedNonIgnoredElements(nodeList.item(i));
                if (containsAddedNonIgnoredElement) {
                    break;
                }
            }
        }
        return containsAddedNonIgnoredElement;
    }

    protected boolean shouldCompareElements(Element oldElement, Element newElement) {
        return newElement == null || oldElement == null
                || !(isSoftAction(newElement.getNode(), SoftActionType.MOVE_FROM) && !isSoftAction(oldElement.getNode(), SoftActionType.MOVE_FROM))
                && !(isSoftAction(newElement.getNode(), SoftActionType.TRANSFORM) && !isSoftAction(oldElement.getNode(), SoftActionType.TRANSFORM));
    }

    protected int getIndexOfIgnoredElementInNewContent(ContentComparatorContext context) {
        Element element = null;
        if (context.getNewContentElements().containsKey(SOFT_DELETE_PLACEHOLDER_ID_PREFIX + context.getOldElement().getTagId())) {
            element = context.getNewContentElements().get(SOFT_DELETE_PLACEHOLDER_ID_PREFIX + context.getOldElement().getTagId());
        } else if(context.getNewContentElements().containsKey(SOFT_MOVE_PLACEHOLDER_ID_PREFIX + context.getOldElement().getTagId())) {
            element = context.getNewContentElements().get(SOFT_MOVE_PLACEHOLDER_ID_PREFIX + context.getOldElement().getTagId());
        }
        return element != null ? context.getNewContentRoot().getChildren().indexOf(element) : -1;
    }

    protected final int getBestMatchInList(List<Element> childElements, Element element){
        if (shouldIgnoreElement(element)) {
            return -2;
        } else if (element == null) {
            return -1;
        }
        int foundPosition = -1;
        int rank[] = new int[childElements.size()];
        for (int iCount = 0; iCount < childElements.size(); iCount++) {
            Element listElement = childElements.get(iCount);

            if (listElement.getTagId() != null && element.getTagId() != null
                    && listElement.getTagId().equals(element.getTagId())) {
                rank[iCount] = 1000;
                break;
            }
            else if((listElement.getTagId()==null && element.getTagId()==null)
                    && listElement.getTagName().equals(element.getTagName())) {//only try to find match if tagID is not present

                // compute node distance
                int maxDistance = 100;
                int distanceWeight = maxDistance / 5; //after distance of 5 nodes it is discarded
                int nodeDistance = Math.abs(listElement.getNodeIndex() - element.getNodeIndex());
                nodeDistance = Math.min(nodeDistance * distanceWeight, maxDistance); // 0...maxDistance

                // compute node similarity
                int similarityWeight = 2;
                int similarity = (int) (100 * listElement.contentSimilarity(element)); //0...100
                similarity = similarity * similarityWeight;

                // compute node rank
                rank[iCount] = (maxDistance - nodeDistance)  //distance 0=100, 1=80,2=60,..5=0
                        + similarity;
            } else {
                rank[iCount] = 0;
            }
        }

        int bestRank=0;
        for (int iCount = 0; iCount < rank.length; iCount++) {
            if(bestRank < rank[iCount]){
                foundPosition=iCount;
                bestRank = rank[iCount];
            }
        }
        return bestRank > 0 ? foundPosition : -1;
    }

    // This functions appends removed CN indented element when this element is not anymore child of new content root
    protected void appendIndentedAndRemovedParent(ContentComparatorContext context, Element element) {
    }

    // This functions appends removed CN indented element when this element is not anymore child of new content root
    protected void appendIndentedAndRemovedIntermediateParent(ContentComparatorContext context, Element element) {
    }

    protected String getRemovedNumContent(ContentComparatorContext context) {
        return (context.getOldElement().getNode()).getTextContent();
    }

    protected boolean containsIgnoredElements(Node node) {
        boolean containsIgnoredElement = false;
        if (containsIgnoredElement(node)) {
            containsIgnoredElement = true;
        } else {
            NodeList nodeList = node.getChildNodes();
            for (int i = 0; i < nodeList.getLength(); i++) {
                containsIgnoredElement = containsIgnoredElements(nodeList.item(i));
                if (containsIgnoredElement) {
                    break;
                }
            }
        }
        return containsIgnoredElement;
    }

    protected boolean containsIgnoredElement(Node node) {
        return isSoftAction(node, SoftActionType.DELETE) || withPlaceholderPrefix(node, SOFT_DELETE_PLACEHOLDER_ID_PREFIX) || isSoftAction(node, SoftActionType.MOVE_TO);
    }

    protected Node getNonIgnoredChangedElementContent(Node contentNode, Element element, String attrName, String attrValue) {
        Node node = XercesUtils.getElementById(contentNode, element.getTagId());
        if (!containsIgnoredElements(node)) {
            node = getChangedElementContent(contentNode, element, attrName, attrValue);
        } else if (!shouldIgnoreElement(element)) {
            XercesUtils.insertOrUpdateAttributeValue(node, attrName, attrValue);
            for (Element child : element.getChildren()) {
                if (!shouldIgnoreElement(child)) {
                    // add child without changing the start tag
                    node.appendChild(getNonIgnoredChangedElementContent(contentNode, child, null, null));
                }
            }
        }
        return node;
    }

    @Override
    public String compareDeletedElements(ContentComparatorContext context) {
        Stopwatch stopwatch = Stopwatch.createStarted();

        boolean usesFakeParentNode = setupCompareElements(context, true);
        computeDeletedNodesAtEachLevel(context);

        LOG.debug("Comparison finished!  ({} milliseconds)", stopwatch.elapsed(TimeUnit.MILLISECONDS));
        String result = XercesUtils.nodeToString(context.getResultNode());
        return usesFakeParentNode? result.substring(result.indexOf("<", result.indexOf("<fake") + 1), result.indexOf("</fake")) : result;
    }

    private void computeDeletedNodesAtEachLevel(ContentComparatorContext context) {
        int oldContentChildIndex = 0; // current index in oldContentRoot children list
        int newContentChildIndex = 0; // current index in newContentRoot children list

        if(context.getOldContentRoot() != null && context.getOldContentRoot().getChildren().size()==0
                && context.getNewContentRoot().getChildren().size()==0) {
            context.setOldElement(context.getOldContentRoot());
            context.setNewElement(context.getNewContentRoot());
            context.setIndexOfNewElementInOldContent(0);
            context.setIndexOfOldElementInNewContent(0);
            context.setResultNode(context.getNewElement().getNode());
        }
        
        while (context.getOldContentRoot() != null && oldContentChildIndex < context.getOldContentRoot().getChildren().size()
                && newContentChildIndex < context.getNewContentRoot().getChildren().size()) {

            context.setOldElement(context.getOldContentRoot().getChildren().get(oldContentChildIndex));
            context.setNewElement(context.getNewContentRoot().getChildren().get(newContentChildIndex));
            context.setIndexOfNewElementInOldContent(getBestMatchInList(context.getOldContentRoot().getChildren(), context.getNewElement()));
            context.setIndexOfOldElementInNewContent(getBestMatchInList(context.getNewContentRoot().getChildren(), context.getOldElement()));

            if (!shouldIgnoreElement(context.getOldElement()) && context.getIndexOfOldElementInNewContent() < 0
                    && context.getIndexOfNewElementInOldContent() > 0 &&
                    !containsSoftDeleteElement(context.getOldElement(), context.getNewContentElements())) {
                //If oldElement still exists in newElement but at different position so skip it
                if (!context.getNewContentElements().containsKey(context.getOldElement().getTagId())) {
                    // oldElement was deleted so only display the removed element with soft attributes
                    Node node = context.getOldElement().getNode();
                    insertSoftDeleteAttributes(node);
                    addReadOnlyAttributes(node);
                    addToResultNode(context, node);
                }
                oldContentChildIndex++;
            } else if (context.getIndexOfNewElementInOldContent() < 0 && context.getIndexOfOldElementInNewContent() < 0) {
                // oldElement was completely replaced with newElement
                int ignoredElementIndex = getIndexOfIgnoredElementInNewContent(context);
                if (isNewAddedElement(context, oldContentChildIndex, newContentChildIndex, ignoredElementIndex)) {
                    Node newNode = context.getNewElement().getNode();
                    addToResultNode(context, newNode);
                    newContentChildIndex++;
                } else {
                    Node newNode = context.getNewElement().getNode();
                    Node oldNode = context.getOldElement().getNode();
                    if (ignoredElementIndex != -1) {
                        if(isSoftAction(newNode, SoftActionType.MOVE_TO)) {
                            addToResultNode(context, newNode);
                            newContentChildIndex++;
                        } else {
                            addReadOnlyAttributes(oldNode);
                            insertSoftDeleteAttributes(oldNode);
                            addToResultNode(context, oldNode);
                        }
                    } else if(isElementWrapped(context) || isElementUnWrapped(context)) {
                        addToResultNode(context, newNode);
                        newContentChildIndex++;
                    } else if(isElementIndented(context.getNewElement())) {
                        addToResultNode(context, newNode);
                        newContentChildIndex++;
                    }else if(oldContentChildIndex == newContentChildIndex) {
                        addToResultNode(context, newNode);
                        addReadOnlyAttributes(oldNode);
                        insertSoftDeleteAttributes(oldNode);
                        addToResultNode(context, oldNode);
                        newContentChildIndex++;
                    }
                    oldContentChildIndex++;
                }
            }  else if (context.getIndexOfNewElementInOldContent() < 0 && context.getIndexOfOldElementInNewContent() > 0) {
                // newElement is added before the existing old element so show the newly added one
                Node newNode = context.getNewElement().getNode();
                addToResultNode(context, newNode);
                newContentChildIndex++;
                if (isSoftAction(newNode, SoftActionType.MOVE_TO) && context.getNewElement().getTagId().equalsIgnoreCase(
                        SOFT_MOVE_PLACEHOLDER_ID_PREFIX + context.getOldElement().getTagId())) {
                    oldContentChildIndex++;
                }
            } else if(context.getIndexOfOldElementInNewContent() > 0 && context.getIndexOfNewElementInOldContent() < 0 &&
                    isAddedNonIgnoredElement(context.getNewElement().getNode())) {
                //New element is moved from its original position to show the moved element
                Node newNode = context.getNewElement().getNode();
                addToResultNode(context, newNode);
                newContentChildIndex++;
            } else if (context.getIndexOfNewElementInOldContent() > 0 && context.getIndexOfOldElementInNewContent() < 0
                    && containsSoftDeleteElement(context.getOldElement(), context.getNewContentElements())) {
                //old element is deleted after the existing one so show the deleted one
                Element deletedElement = context.getNewContentElements().get(SOFT_DELETE_PLACEHOLDER_ID_PREFIX +
                        context.getOldElement().getTagId());
                insertSoftDeleteAttributes(deletedElement.getNode());
                addToResultNode(context, deletedElement.getNode());
                oldContentChildIndex++;
            } else {
                compareChildNodes(context);
                oldContentChildIndex++;
                newContentChildIndex++;
            }
        }
        if(oldContentChildIndex < context.getOldContentRoot().getChildren().size()) {
            int oldContentIndexForChildren = oldContentChildIndex;
            while ((isElementIndexLessThanRootChildren(context.getOldContentRoot(), oldContentIndexForChildren))) {
                Element oldElementChild = context.getOldContentRoot().getChildren().get(oldContentIndexForChildren);
                Node node = oldElementChild.getNode();
                //boolean isList = LIST.equalsIgnoreCase(oldElementChild.getTagName());
                if(!shouldIgnoreElement(oldElementChild)
                        && !containsNotDeletedElementsInOtherContext(context.getNewContentElements(), oldElementChild)
                        && isElementRemovedInOtherContext(context.getNewContentElements(), oldElementChild)) {
                    insertSoftDeleteAttributes(node);
                    addReadOnlyAttributes(node);
                    addToResultNode(context, node);
                }
                oldContentIndexForChildren++;
            }
        }
        if (newContentChildIndex < context.getNewContentRoot().getChildren().size()) {
            int newContentIndexForChildren = newContentChildIndex;
            while ((isElementIndexLessThanRootChildren(context.getNewContentRoot(), newContentIndexForChildren))) {
                Element newElementChild = context.getNewContentRoot().getChildren().get(newContentIndexForChildren);
                Node node = newElementChild.getNode();
                addToResultNode(context, node);
                newContentIndexForChildren++;
            }
        }
    }

    private void insertSoftDeleteAttributes(Node node) {
        insertAttributeIfNotPresent(node, LEOS_SOFT_ACTION_ATTR, SoftActionType.DELETE.getSoftAction());
        insertAttributeIfNotPresent(node, LEOS_SOFT_ACTION_ROOT_ATTR, "true");
        insertAttributeIfNotPresent(node, LEOS_SOFT_USER_ATTR, getSoftUserAttribute(securityContext.getUser()));
        insertAttributeIfNotPresent(node, LEOS_SOFT_DATE_ATTR, getDateAsXml());
        updateXMLIDAttribute(node, SOFT_DELETE_PLACEHOLDER_ID_PREFIX, false);
    }

    private void compareChildNodes(ContentComparatorContext context) {
        Node node = context.getNewElement().getNode();
        if ((context.getNewElement() != null && context.getNewElement().hasTextChild())) {
            addToResultNode(context, node);
        } else {
            node = XercesUtils.importNodeInDocument(context.getResultNode().getOwnerDocument(), node);
            node.setTextContent(EMPTY_STRING);
            ContentComparatorContext newContext = new ContentComparatorContext.Builder(context)
                    .withOldContentRoot(context.getOldElement())
                    .withNewContentRoot(context.getNewElement())
                    .withIntermediateContentRoot(context.getIntermediateElement())
                    .withResultNode(node)
                    .build();
            addToResultNode(context, node);
            computeDeletedNodesAtEachLevel(newContext);
        }
    }

    private boolean isElementWrapped(ContentComparatorContext context) {
        //e.g. to check if the <content> is wrapped by <subparagraph> or <alinea> in case of outdent/indent
        if(context.getNewElement().getChildren() != null && context.getNewElement().getChildren().size() > 0) {
            return context.getOldElement().equals(context.getNewElement().getChildren().get(0));
        }
        return false;
    }

    private boolean isElementUnWrapped(ContentComparatorContext context) {
        //e.g. to check if the <alinea> or <subparagraph> is removed in case of outdent/indent
        if(context.getOldElement().getChildren() != null && context.getOldElement().getChildren().size() > 0) {
            return context.getNewElement().equals(context.getOldElement().getChildren().get(0));
        }
        return false;
    }

    private boolean setupCompareElements(ContentComparatorContext context, boolean enableFakeEncapsulation) {
        // If enabled, encapsulate the xml content in a fake parent tag so Xerces can process the content
        final String oldXml = enableFakeEncapsulation ? "<fake>" + context.getComparedVersions()[0] + "</fake>" : context.getComparedVersions()[0];
        final String newXml = enableFakeEncapsulation ? "<fake>" + context.getComparedVersions()[1] + "</fake>" : context.getComparedVersions()[1];
        boolean usesFakeEncapsulation = false;
        Node oldNode = XercesUtils.createXercesDocument(oldXml.getBytes(UTF_8), false).getDocumentElement();
        Node newNode = XercesUtils.createXercesDocument(newXml.getBytes(UTF_8), false).getDocumentElement();

        XercesUtils.addLeosNamespace(oldNode);
        XercesUtils.addLeosNamespace(newNode);

        if(enableFakeEncapsulation && XercesUtils.countChildren(oldNode, Collections.emptyList()) == 1 && XercesUtils.countChildren(newNode, Collections.emptyList()) == 1) {
            oldNode = XercesUtils.createXercesDocument(context.getComparedVersions()[0].getBytes(UTF_8), false).getDocumentElement();
            newNode = XercesUtils.createXercesDocument(context.getComparedVersions()[1].getBytes(UTF_8), false).getDocumentElement();
            XercesUtils.addLeosNamespace(oldNode);
            XercesUtils.addLeosNamespace(newNode);
        } else {
            usesFakeEncapsulation = enableFakeEncapsulation;
        }

        resetResultNode(context, newNode);
        context.setOldContentElements(new HashMap<>());
        context.setNewContentElements(new HashMap<>());

        final Element oldElement = buildElement(oldNode, new HashMap<>(), context.getOldContentElements());
        final Element newElement = buildElement(newNode, new HashMap<>(), context.getNewContentElements());

        context.setOldContentNode(oldNode)
                .setNewContentNode(newNode)
                .setOldContentRoot(oldElement)
                .setNewContentRoot(newElement);

        if (context.getThreeWayDiff()) {
            final String intermediateXml = context.getComparedVersions()[2];
            final Node intermediateNode = XercesUtils.createXercesDocument(intermediateXml.getBytes(UTF_8), false).getDocumentElement();

            context.setIntermediateContentElements(new HashMap<>());
            final Element intermediateElement = buildElement(intermediateNode, new HashMap<>(), context.getIntermediateContentElements());

            context.setIntermediateContentNode(intermediateNode)
                    .setIntermediateContentRoot(intermediateElement);
        }
        return usesFakeEncapsulation;
    }

    protected abstract boolean isCurrentElementNonIgnored(Node node);

    protected abstract boolean isCurrentElementIgnored(Node node);

    protected abstract boolean isCurrentElementIgnoredInNewContent(ContentComparatorContext context);

    protected abstract boolean containsDeletedElementInNewContent(ContentComparatorContext context);

    protected abstract boolean isIgnoredElement(Element element);

    protected abstract boolean shouldAddElement(Element oldElement, Map<String, Element> contentElements);

    protected abstract void appendAddedElementContent(ContentComparatorContext context);

    protected abstract void appendRemovedElementContent(ContentComparatorContext context);

    protected abstract void appendRemovedContent(ContentComparatorContext context);

    protected abstract void appendIndentedDeletedOrMovedToContent(ContentComparatorContext context, Element element);

    protected abstract Node buildNodeForAddedElement(Element newElement, Element oldElement, ContentComparatorContext context);

    protected abstract Node buildNodeForAddedElement(ContentComparatorContext context);

    protected abstract Node buildNodeForRemovedElement(Element element, ContentComparatorContext context, Map<String, Element> contentElements);

    protected abstract boolean shouldDisplayRemovedContent(Element elementOldContent, int indexOfOldElementInNewContent);

    @Override
    public String[] twoColumnsCompareContents(ContentComparatorContext context) {
        return new String[]{context.getLeftResultBuilder().toString(), context.getRightResultBuilder().toString()};
    }
}
