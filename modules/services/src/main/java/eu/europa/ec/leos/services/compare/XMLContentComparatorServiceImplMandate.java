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

import eu.europa.ec.leos.domain.common.InstanceType;
import eu.europa.ec.leos.i18n.MessageHelper;
import eu.europa.ec.leos.instance.Instance;
import eu.europa.ec.leos.model.action.SoftActionType;
import eu.europa.ec.leos.security.SecurityContext;
import eu.europa.ec.leos.services.compare.vo.Element;
import eu.europa.ec.leos.services.processor.content.XmlContentProcessor;
import eu.europa.ec.leos.services.processor.content.indent.IndentConversionHelper;
import eu.europa.ec.leos.services.support.XercesUtils;
import org.apache.xerces.dom.DeferredElementImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.support.MethodOverrides;
import org.springframework.stereotype.Service;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static eu.europa.ec.leos.services.compare.IndentContentComparatorHelper.findElementInOtherContext;
import static eu.europa.ec.leos.services.compare.IndentContentComparatorHelper.getAllowedTags;
import static eu.europa.ec.leos.services.compare.IndentContentComparatorHelper.getNotDeletedElementsFromContent;
import static eu.europa.ec.leos.services.compare.IndentContentComparatorHelper.isECOrigin;
import static eu.europa.ec.leos.services.compare.IndentContentComparatorHelper.isElementIndented;
import static eu.europa.ec.leos.services.compare.IndentContentComparatorHelper.isElementIndentedInOtherContext;
import static eu.europa.ec.leos.services.compare.IndentContentComparatorHelper.isElementRemovedInOtherContext;
import static eu.europa.ec.leos.services.compare.IndentContentComparatorHelper.isElementUnumberedIndentedInOtherContext;
import static eu.europa.ec.leos.services.compare.IndentContentComparatorHelper.isIndentedAndRemovedParent;
import static eu.europa.ec.leos.services.support.XercesUtils.getSoftAction;
import static eu.europa.ec.leos.services.support.XmlHelper.EMPTY_STRING;
import static eu.europa.ec.leos.services.support.XmlHelper.LIST;
import static eu.europa.ec.leos.services.support.XmlHelper.NUM;
import static eu.europa.ec.leos.services.support.XmlHelper.PARAGRAPH;
import static eu.europa.ec.leos.services.support.XmlHelper.SOFT_DELETE_PLACEHOLDER_ID_PREFIX;
import static eu.europa.ec.leos.services.support.XmlHelper.SOFT_MOVE_PLACEHOLDER_ID_PREFIX;
import static eu.europa.ec.leos.services.support.XmlHelper.SOFT_TRANSFORM_PLACEHOLDER_ID_PREFIX;
import static eu.europa.ec.leos.services.support.XmlHelper.SUBPARAGRAPH;
import static eu.europa.ec.leos.services.support.XmlHelper.SUBPOINT;
import static eu.europa.ec.leos.services.support.XmlHelper.ID;
import static eu.europa.ec.leos.services.support.XmlHelper.LEOS_SOFT_MOVE_TO;
import static eu.europa.ec.leos.services.compare.ComparisonHelper.isElementContentEqual;
import static eu.europa.ec.leos.services.compare.ComparisonHelper.isSoftAction;
import static eu.europa.ec.leos.services.compare.ComparisonHelper.withPlaceholderPrefix;

@Service
@Instance(InstanceType.COUNCIL)
public class XMLContentComparatorServiceImplMandate extends XMLContentComparatorServiceImpl {

    private static final Logger LOG = LoggerFactory.getLogger(XMLContentComparatorServiceImplMandate.class);

    @Autowired
    public XMLContentComparatorServiceImplMandate(MessageHelper messageHelper, TextComparator textComparator,
                                                  SecurityContext securityContext, XmlContentProcessor xmlContentProcessor) {
        super(messageHelper, textComparator, securityContext, xmlContentProcessor);
    }

    @Override
    protected boolean shouldAddElement(Element element, Map<String, Element> contentElements) {
        return !contentElements.containsKey(element.getTagId()) &&
                !isSoftAction(element.getNode(), SoftActionType.ADD);
    }
    protected boolean shouldIgnoreElement(Element element) {
        return element != null
                && (isSoftAction(element.getNode(), SoftActionType.DELETE)
                || isSoftAction(element.getNode(), SoftActionType.DELETE_TRANSFORM)
                || isSoftAction(element.getNode(), SoftActionType.MOVE_TO)
                || withPlaceholderPrefix(element.getNode(), SOFT_DELETE_PLACEHOLDER_ID_PREFIX)
                || element.getTagId().contains("eeaRelevance"));
    }

    @Override
    protected boolean shouldDisplayRemovedContent(Element elementOldContent, int indexOfOldElementInNewContent) {
        return !shouldIgnoreElement(elementOldContent) || isElementRemovedFromContent(indexOfOldElementInNewContent);
    }

    @Override
    protected boolean isIgnoredElement(Element element) {
        return withPlaceholderPrefix(element.getNode(), SOFT_DELETE_PLACEHOLDER_ID_PREFIX) || withPlaceholderPrefix(element.getNode(), SOFT_MOVE_PLACEHOLDER_ID_PREFIX);
    }

    @Override
    protected boolean isCurrentElementNonIgnored(Node node) {
        return isSoftAction(node, SoftActionType.MOVE_FROM);
    }

    @Override
    protected boolean isCurrentElementIgnored(Node node) {
        return withPlaceholderPrefix(node, SOFT_MOVE_PLACEHOLDER_ID_PREFIX);
    }

    @Override
    protected boolean containsDeletedElementInNewContent(ContentComparatorContext context) {
        //LEOS-4340:When element is soft deleted in NewContent but present in both old and intermediate
        //Required to check for e.g. on deletion of an article in new content and adding a new article before/after it.
        return context.getThreeWayDiff() && context.getIntermediateElement() != null &&
                context.getIntermediateElement().getTagId().equalsIgnoreCase(context.getOldElement().getTagId()) &&
                context.getNewContentElements().containsKey(SOFT_DELETE_PLACEHOLDER_ID_PREFIX + context.getIntermediateElement().getTagId());
    }

    @Override
    protected boolean isCurrentElementIgnoredInNewContent(ContentComparatorContext context) {
        return context.getIntermediateElement() != null && (withPlaceholderPrefix(context.getIntermediateElement().getNode(), SOFT_MOVE_PLACEHOLDER_ID_PREFIX) ||
                withPlaceholderPrefix(context.getIntermediateElement().getNode(), SOFT_DELETE_PLACEHOLDER_ID_PREFIX)) &&
                (context.getNewElement().getTagId().equalsIgnoreCase(context.getIntermediateElement().getTagId().
                        replace(SOFT_MOVE_PLACEHOLDER_ID_PREFIX, EMPTY_STRING)) || context.getNewElement().getTagId().equalsIgnoreCase(context.getIntermediateElement().getTagId().
                        replace(SOFT_DELETE_PLACEHOLDER_ID_PREFIX, EMPTY_STRING)));
    }

    @Override
    protected Node buildNodeForAddedElement(Element newElement, Element oldElement, ContentComparatorContext context) {
        Node node;
        if (isElementContentEqual(context)) {
            node = buildNode(context.getNewElement(), context.getStartTagAttrName(), context.getStartTagAttrValue());
        } else if (isElementInItsOriginalPosition(oldElement) && !isElementInItsOriginalPosition(newElement)) { //element is moved/deleted in live version
            node = buildNode(context.getNewElement(), context.getStartTagAttrName(), context.getAddedIntermediateValue());
        } else if (!isElementInItsOriginalPosition(oldElement) && !isElementInItsOriginalPosition(newElement)) { //element was already moved/deleted in intermediate
            node = buildNode(context.getNewElement(), context.getStartTagAttrName(), context.getAddedOriginalValue());
        } else {
            node = buildNode(context.getNewElement(), context.getStartTagAttrName(), context.getStartTagAttrValue());
        }
        return node;
    }

    @Override
    protected Node buildNodeForAddedElement(ContentComparatorContext context) {
        Node node;
        if (isElementInItsOriginalPosition(context.getOldElement()) &&
                isElementInItsOriginalPosition(context.getIntermediateElement()) &&
                !isElementInItsOriginalPosition(context.getNewElement())) { //element is moved/added in live version
            node = buildNode(context.getNewElement(), context.getAttrName(), context.getAddedIntermediateValue());
        } else if (isElementInItsOriginalPosition(context.getOldElement()) &&
                !isElementInItsOriginalPosition(context.getIntermediateElement()) && !isElementInItsOriginalPosition(context.getNewElement())) {
            node = buildNode(context.getNewElement(), context.getAttrName(), context.getAddedOriginalValue());
        } else if (isElementInItsOriginalPosition(context.getOldElement()) && isElementInItsOriginalPosition(context.getNewElement()) && containsIgnoredElement(context.getIntermediateElement().getNode())) { //element was moved/deleted in intermediate but restored in live version
            node = buildNode(context.getNewElement(), context.getAttrName(), context.getAddedIntermediateValue());
        } else if (!isElementInItsOriginalPosition(context.getNewElement())
                && !isElementInItsOriginalPosition(context.getIntermediateElement())
                && !isElementInItsOriginalPosition(context.getOldElement())
                && !isElementIndentedInOtherContext(context.getNewContentElements(), context.getIntermediateElement())
                && isElementUnumberedIndentedInOtherContext(context.getOldContentElements(), context.getIntermediateElement())
                && isElementIndentedInOtherContext(context.getOldContentElements(), context.getIntermediateElement())) {
            // Particular case: when numbered element has been indented to unumbered element, display strikethrough number
            // Temporary fix, content of element should NOT be flagged as added
            node = buildNode(context.getNewElement(), context.getAttrName(), context.getAddedIntermediateValue());
        } else {
            node = buildNode(context.getNewElement());
        }
        return node;
    }

    @Override
    protected Node buildNodeForRemovedElement(Element element, ContentComparatorContext context, Map<String, Element> contentElements) {
        String attrValue;
        if (contentElements != null && contentElements.get(element.getTagId()) == null) {
            attrValue = context.getRemovedIntermediateValue();
        } else {
            attrValue = context.getRemovedOriginalValue();
        }
        return buildNode(element, context.getAttrName(), attrValue);
    }

    @Override
    protected void appendAddedElementContent(ContentComparatorContext context) {
        if (context.getDisplayRemovedContentAsReadOnly() && !shouldIgnoreElement(context.getNewElement())) {
            if (context.getNewElement().getTagId() != null) {
                if (context.getOldContentElements().containsKey(context.getNewElement().getTagId().replace(SOFT_TRANSFORM_PLACEHOLDER_ID_PREFIX, EMPTY_STRING))
                        || context.getOldContentElements().containsKey(context.getNewElement().getTagId().replace(IndentConversionHelper.INDENT_PLACEHOLDER_ID_PREFIX, EMPTY_STRING))) {
                    //append the soft movedFrom element content compared to the original content and ignore its renumbering
                    Element originalMovedElementInOldContent = context.getOldContentElements().get(context.getNewElement().getTagId().replace(SOFT_TRANSFORM_PLACEHOLDER_ID_PREFIX, EMPTY_STRING));
                    if (isElementIndented(context.getNewElement()) && isSoftAction(context.getNewElement().getNode(), SoftActionType.ADD)) {
                        // Not moved but indented element
                        originalMovedElementInOldContent = context.getOldContentElements().get(context.getNewElement().getTagId().replace(IndentConversionHelper.INDENT_PLACEHOLDER_ID_PREFIX, EMPTY_STRING));
                    }
                    Element originalMovedElementInIntermediateContent;
                    if(context.getThreeWayDiff() && context.getIntermediateElement() != null) {
                        originalMovedElementInIntermediateContent = (!context.getNewElement().getTagId().equalsIgnoreCase(context.getIntermediateElement().getTagId()) && context.getIntermediateContentElements().containsKey(context.getNewElement().getTagId().
                                replace(SOFT_TRANSFORM_PLACEHOLDER_ID_PREFIX, EMPTY_STRING))) ?
                                context.getIntermediateContentElements().get(context.getNewElement().getTagId().replace(SOFT_TRANSFORM_PLACEHOLDER_ID_PREFIX, EMPTY_STRING)) : context.getIntermediateElement();
                        if (isElementIndented(context.getNewElement()) && isSoftAction(context.getNewElement().getNode(), SoftActionType.ADD)) {
                            // Not moved but indented element
                            originalMovedElementInIntermediateContent = (!context.getNewElement().getTagId().equalsIgnoreCase(context.getIntermediateElement().getTagId()) && context.getIntermediateContentElements().containsKey(context.getNewElement().getTagId().
                                    replace(IndentConversionHelper.INDENT_PLACEHOLDER_ID_PREFIX, EMPTY_STRING))) ?
                                    context.getIntermediateContentElements().get(context.getNewElement().getTagId().replace(IndentConversionHelper.INDENT_PLACEHOLDER_ID_PREFIX, EMPTY_STRING)) : context.getIntermediateElement();
                        }
                    } else {
                        originalMovedElementInIntermediateContent = context.getIntermediateElement();
                    }
                    // Numbering should not be ignored for indented elements
                    Boolean ignoreNumbering = (isSoftAction(context.getNewElement().getNode(), SoftActionType.MOVE_FROM)
                            || (!isSoftAction(context.getNewElement().getNode(), SoftActionType.ADD)
                            || !isElementIndented(context.getNewElement()))
                            && !isElementIndentedInOtherContext(context.getOldContentElements(), context.getNewElement()));
                    compareElementContents(new ContentComparatorContext.Builder(context)
                            .withOldElement(originalMovedElementInOldContent)
                            .withIntermediateElement(originalMovedElementInIntermediateContent)
                            .withDisplayRemovedContentAsReadOnly(Boolean.TRUE)
                            .withIgnoreElements(Boolean.TRUE)
                            .withIgnoreRenumbering(ignoreNumbering)
                            .withStartTagAttrName(context.getAttrName())
                            .withStartTagAttrValue(getStartTagValueForAddedElementFromAncestor(context.getNewElement(), context))
                            .build());

                } else if (!context.getNewElement().getTagId().startsWith(SOFT_MOVE_PLACEHOLDER_ID_PREFIX)
                        && !context.getNewElement().getTagId().startsWith(SOFT_DELETE_PLACEHOLDER_ID_PREFIX)
                        && !context.getNewElement().getTagId().startsWith(SOFT_TRANSFORM_PLACEHOLDER_ID_PREFIX)) {

                    Element intermediateElement;
                    Boolean ignoreRenumbering;
                    if(context.getThreeWayDiff()) {
                        intermediateElement = context.getIntermediateContentElements().containsKey(context.getNewElement().getTagId()) ? context.getIntermediateContentElements().get(context.getNewElement().getTagId()) : null;
                        ignoreRenumbering = Boolean.FALSE;
                    } else {
                        intermediateElement = context.getIntermediateElement();
                        ignoreRenumbering = Boolean.TRUE;
                    }
                    compareElementContents(new ContentComparatorContext.Builder(context)
                            .withIndexOfOldElementInNewContent(-1)
                            .withOldElement(null)
                            .withIntermediateElement(intermediateElement)
                            .withDisplayRemovedContentAsReadOnly(Boolean.TRUE)
                            .withIgnoreElements(Boolean.TRUE)
                            .withIgnoreRenumbering(ignoreRenumbering)
                            .withStartTagAttrName(context.getAttrName())
                            .withStartTagAttrValue(getStartTagValueForAddedElementFromAncestor(context.getNewElement(), context))
                            .build());
                }
            } else {
                String attrValue = getStartTagValueForAddedElementFromAncestor(context.getNewElement(), context);
                Node node = getChangedElementContent(context.getNewContentNode(), context.getNewElement(), context.getAttrName(), attrValue);
                addToResultNode(context, node);
            }
        } else {
            String attrValue = getStartTagValueForAddedElementFromAncestor(context.getNewElement(), context);
            Node node = getNonIgnoredChangedElementContent(context.getNewContentNode(), context.getNewElement(), context.getAttrName(), attrValue);
            addToResultNode(context, node);
        }
    }

    private String getStartTagValueForAddedElementFromAncestor(Element element, ContentComparatorContext context) {
        String attrValue;
        if (context.getThreeWayDiff()) {
            Element ancestor = getFirstAncestorWithId(element);
            if (ancestor != null && context.getIntermediateContentElements() != null
                    && !context.getIntermediateContentElements().containsKey(ancestor.getTagId().replace(SOFT_TRANSFORM_PLACEHOLDER_ID_PREFIX, EMPTY_STRING))) {
                attrValue = context.getAddedIntermediateValue();
            } else {
                attrValue = context.getAddedOriginalValue();
            }
        } else {
            attrValue = context.getAddedValue();
        }
        return attrValue;
    }

    private boolean isRemovedSubparagraphFromIndentedElement(Element oldElement, Element newElement) {
        if (oldElement.getTagName().equals(SUBPARAGRAPH) || oldElement.getTagName().equals(SUBPOINT)) {
            Optional<Element> subElement = oldElement.getParent().getChildren().stream()
                    .filter(e -> e.getTagName().equals(SUBPARAGRAPH)
                            || e.getTagName().equals(SUBPOINT))
                    .findFirst();
            if (subElement.isPresent()) {
                return newElement.getTagId().endsWith(oldElement.getParent().getTagId())
                        && newElement.getTagId().startsWith(SOFT_TRANSFORM_PLACEHOLDER_ID_PREFIX)
                        && subElement.get().equals(oldElement);
            }
        }
        return false;
    }

    private boolean isMovedElementIndentedToNewElement(ContentComparatorContext context) {
        if (containsSoftTransformedElement(context.getOldContentElements(), context.getNewElement())
                && isElementIndented(context.getNewElement())
                && containsSoftMoveToElement(context.getNewContentElements(), context.getOldElement())) {
            Element indentedSoftMovedToElement =
                    context.getNewContentElements().get(SOFT_MOVE_PLACEHOLDER_ID_PREFIX + context.getOldElement().getTagId());
            return (indentedSoftMovedToElement != null && indentedSoftMovedToElement.getParent().getParent().equals(context.getNewElement()));
        }
        return false;
    }

    @Override
    protected void appendRemovedElementContent(ContentComparatorContext context) {
        if (context.getOldElement() == null) {
            return;
        }
        if (context.getDisplayRemovedContentAsReadOnly() && !shouldIgnoreElement(context.getOldElement())) {
            if (context.getOldElement().getTagId() != null) {
                if (containsSoftMoveToTransformedElement(context.getNewContentElements(), context.getOldElement())) {
                    //append the soft movedTo element content
                    Element softMovedToTansformedElement = context.getNewContentElements().get(SOFT_MOVE_PLACEHOLDER_ID_PREFIX + SOFT_TRANSFORM_PLACEHOLDER_ID_PREFIX +
                            context.getOldElement().getParent().getTagId());
                    appendMovedOrTransformedContent(context, softMovedToTansformedElement);
                } else if(context.getNewElement().getTagName().equals(PARAGRAPH) &&
                        (containsSoftTransformedElement(context.getOldContentElements(), context.getNewElement())
                                && !isElementIndented(context.getNewElement()))
                        && (!containsSoftMoveToElement(context.getNewContentElements(), context.getOldElement()))) { //TODO: FIX this if condition remove check for PARAGRAPH
                    appendMovedOrTransformedContent(context, context.getNewElement());
                } else if (isMovedElementIndentedToNewElement(context)) {
                    // Old element was a point or paragraph with children: indented, then moved
                    return;
                } else if (SoftActionType.DELETE_TRANSFORM.equals(getSoftAction(context.getNewElement().getNode()))) {
                    getChangedElementContent(context.getNewElement().getNode(), context.getNewElement(), context.getAttrName(), context.getRemovedValue());
                    addToResultNode(context, context.getNewElement().getNode());
                } else if (containsSoftDeleteElement(context.getOldElement(), context.getNewContentElements())) {
                    //element was soft deleted, and it's ID was prepended with SOFT_DELETE_PLACEHOLDER_ID_PREFIX
                    Element softDeletedNewElement = context.getNewContentElements().get(SOFT_DELETE_PLACEHOLDER_ID_PREFIX + context.getOldElement().getTagId());
                    int indexOfSoftDeletedElementInNewContent = getBestMatchInList(softDeletedNewElement.getParent().getChildren(), softDeletedNewElement);

                    Element softDeletedOrMovedIntermediateElement = context.getIntermediateElement();
                    Node intermediateContentNode = context.getIntermediateContentNode();
                    if (context.getThreeWayDiff()) {
                        ContentComparatorContext newContext = findIntermediateElement(context);
                        softDeletedOrMovedIntermediateElement = newContext.getIntermediateElement();
                        intermediateContentNode = newContext.getIntermediateContentNode();
                    }
                    compareElementContents(new ContentComparatorContext.Builder(context)
                            .withIndexOfOldElementInNewContent(indexOfSoftDeletedElementInNewContent)
                            .withNewElement(softDeletedNewElement)
                            .withIntermediateElement(softDeletedOrMovedIntermediateElement)
                            .withIntermediateContentNode(intermediateContentNode)
                            .withDisplayRemovedContentAsReadOnly(Boolean.TRUE)
                            .withIgnoreElements(Boolean.FALSE)
                            .withIgnoreRenumbering(Boolean.FALSE)
                            .withStartTagAttrName(context.getAttrName())
                            .withStartTagAttrValue(getStartTagValueForRemovedElement(softDeletedNewElement, context))
                            .build());
                } else if (context.getNewContentElements().containsKey(SOFT_DELETE_PLACEHOLDER_ID_PREFIX + SOFT_TRANSFORM_PLACEHOLDER_ID_PREFIX +
                        context.getOldElement().getParent().getTagId())) {
                    //element was soft deleted, and it's ID was prepended with SOFT_DELETE_PLACEHOLDER_ID_PREFIX + SOFT_TRANSFORM_PLACEHOLDER_ID_PREFIX
                    Element softDeletedTransformedElement = context.getNewContentElements().get(SOFT_DELETE_PLACEHOLDER_ID_PREFIX + SOFT_TRANSFORM_PLACEHOLDER_ID_PREFIX +
                            context.getOldElement().getParent().getTagId());
                    int indexOfSoftDeletedElementInNewContent = getBestMatchInList(softDeletedTransformedElement.getParent().getChildren(),
                            softDeletedTransformedElement);

                    Element softDeletedTransformedIntermediateElement;
                    if(context.getThreeWayDiff()) {
                        //element was soft deleted transformed in intermediate, and it's ID was prepended with SOFT_DELETE_PLACEHOLDER_ID_PREFIX + SOFT_TRANSFORM_PLACEHOLDER_ID_PREFIX
                        softDeletedTransformedIntermediateElement = context.getIntermediateContentElements().containsKey(SOFT_DELETE_PLACEHOLDER_ID_PREFIX + SOFT_TRANSFORM_PLACEHOLDER_ID_PREFIX
                                + context.getOldElement().getTagId()) ? context.getIntermediateContentElements().get(SOFT_DELETE_PLACEHOLDER_ID_PREFIX + SOFT_TRANSFORM_PLACEHOLDER_ID_PREFIX
                                + context.getOldElement().getTagId()) : context.getIntermediateElement();
                    } else {
                        softDeletedTransformedIntermediateElement = context.getIntermediateElement();
                    }
                    compareElementContents(new ContentComparatorContext.Builder(context)
                            .withIndexOfOldElementInNewContent(indexOfSoftDeletedElementInNewContent)
                            .withNewElement(softDeletedTransformedElement)
                            .withIntermediateElement(softDeletedTransformedIntermediateElement)
                            .withDisplayRemovedContentAsReadOnly(Boolean.TRUE)
                            .withIgnoreElements(Boolean.FALSE)
                            .withIgnoreRenumbering(Boolean.FALSE)
                            .withStartTagAttrName(context.getAttrName())
                            .withStartTagAttrValue(getStartTagValueForRemovedElement(softDeletedTransformedElement, context))
                            .build());
                } else if (containsSoftMoveToElement(context.getNewContentElements(), context.getOldElement())) {
                    //append the soft movedTo element content
                    appendMovedToElementWithoutContent(context);
                } else if (!isSoftAction(context.getNewElement().getNode(), SoftActionType.TRANSFORM)
                        && !isSoftAction(context.getOldElement().getNode(), SoftActionType.TRANSFORM)
                        && (!isRemovedSubparagraphFromIndentedElement(context.getOldElement(), context.getNewElement()))
                        && !context.getNewContentElements().containsKey(context.getOldElement().getTagId())
                        && !emptyListInOldContext(context)
                        && !(isRemovedNumInUnumbered(context))) {
                    //Element is added/present in old content but deleted from new content, so just display the deleted content
                    String attrValue = getStartTagValueForRemovedElementFromAncestor(context.getOldElement(), context);
                    Node node = getChangedElementContent(context.getOldContentNode(), context.getOldElement(), context.getAttrName(), attrValue);
                    addReadOnlyAttributes(node);
                    addToResultNode(context, node);
                } else if (isElementIndented(context.getOldElement())) {
                    appendRemovedContent(context);
                }
            } else {
                appendRemovedContent(context);
            }
        } else if (!context.getNewContentElements().containsKey(SOFT_TRANSFORM_PLACEHOLDER_ID_PREFIX + context.getOldElement().getParent().getTagId())
                || !context.getOldElement().getTagId().equals(SOFT_TRANSFORM_PLACEHOLDER_ID_PREFIX + context.getOldElement().getParent().getTagId())
                && (context.getNewContentElements().containsKey(SOFT_DELETE_PLACEHOLDER_ID_PREFIX + SOFT_TRANSFORM_PLACEHOLDER_ID_PREFIX + context.getOldElement().getParent().getTagId())
                || (context.getNewContentElements().containsKey(SOFT_MOVE_PLACEHOLDER_ID_PREFIX + SOFT_TRANSFORM_PLACEHOLDER_ID_PREFIX + context.getOldElement().getParent().getTagId())))) {
            //append the soft movedTo element
            appendMovedToElementWithoutContent(context);
        } else if (context.getOldElement().getTagId().equals(SOFT_TRANSFORM_PLACEHOLDER_ID_PREFIX + context.getOldElement().getParent().getTagId())
                && (containsSoftDeleteElement(context.getOldElement(), context.getNewContentElements())
                || context.getNewContentElements().containsKey(SOFT_MOVE_PLACEHOLDER_ID_PREFIX + context.getOldElement().getTagId()))) {

            Element movedOrDeletedTransformedElement = context.getNewContentElements().get((context.getNewContentElements().containsKey(SOFT_MOVE_PLACEHOLDER_ID_PREFIX + context.getOldElement().getTagId())
                    ? SOFT_MOVE_PLACEHOLDER_ID_PREFIX : SOFT_DELETE_PLACEHOLDER_ID_PREFIX) + context.getOldElement().getTagId());
            String attrValue = getStartTagValueForRemovedElement(movedOrDeletedTransformedElement, context);
            Node node = getNonIgnoredChangedElementContent(context.getNewContentNode(), movedOrDeletedTransformedElement, context.getAttrName(), attrValue);
            addToResultNode(context, node);
        }
    }

    @Override
    protected void appendRemovedContent(ContentComparatorContext context) {
        if ((isSoftAction(context.getOldElement().getNode(), SoftActionType.ADD)) ||
                (isSoftAction(context.getOldElement().getNode(), SoftActionType.MOVE_FROM) && !context.getNewContentElements().containsKey(context.getOldElement().getTagId()))) {
            //If element is added in old content but deleted in the new one look for leos:softAction="add" in old element OR
            //If element contains soft action move_from in old content but deleted in new content display the move_from element as deleted
            appendChangedElement(context, null);
        }  else if (containsSoftDeleteElement(context.getOldElement(), context.getNewContentElements())) {
            //If element is soft deleted in new content then print the deleted element from new content
            appendDeletedElement(context);
        }   else if (context.getNewElement().getNode().getPreviousSibling() != null) {
            //LEOS-5999 element moved and indented (transformed)
            String newElementPreviousMovedId = XercesUtils.getAttributeValue(context.getNewElement().getNode().getPreviousSibling(), LEOS_SOFT_MOVE_TO);
            String oldElementCurrentId = XercesUtils.getAttributeValue(context.getOldElement().getNode(), ID);
            if (newElementPreviousMovedId != null && oldElementCurrentId != null && newElementPreviousMovedId.equals(oldElementCurrentId)) {
                appendChangedElement(context, oldElementCurrentId);
            }
        }
    }

    @Override
    protected boolean isElementImpactedByIndention(Map<String, Element> otherContextElements, Element element) {
        return (!isElementIndented(element) && isElementIndentedInOtherContext(otherContextElements, element)
                || (isElementIndented(element) && !isElementIndentedInOtherContext(otherContextElements, element)));
    }

    private void appendChangedElement(ContentComparatorContext context, String oldElementCurrentId) {
        Node node = getChangedElementContent(context.getOldContentNode(), context.getOldElement(), context.getAttrName(),
                getStartTagValueForRemovedElementFromAncestor(context.getOldElement(), context));
        if(oldElementCurrentId != null) {
            XercesUtils.insertOrUpdateAttributeValue(node, ID, SOFT_MOVE_PLACEHOLDER_ID_PREFIX + oldElementCurrentId);
        }
        addReadOnlyAttributes(node);
        addToResultNode(context, node);
    }

    private void appendDeletedElement(ContentComparatorContext context) {
        Element softDeletedNewElement = context.getNewContentElements().get(SOFT_DELETE_PLACEHOLDER_ID_PREFIX + context.getOldElement().getTagId());
        Node node = softDeletedNewElement.getNode();
        String attrName = context.getAttrName();
        String attrValue = getStartTagValueForRemovedElement(softDeletedNewElement, context);
        if (attrName != null && attrValue != null) {
            XercesUtils.addAttribute(node, attrName, attrValue);
        }
        addReadOnlyAttributes(node);
        addToResultNode(context, node);
    }

    @Override
    protected void appendMovedToOrDeletedElement(ContentComparatorContext context) {
        Element softMovedToOrDeletedNewElement = context.getNewElement();
        Node node = softMovedToOrDeletedNewElement.getNode();
        String attrName = context.getAttrName();
        String attrValue = getStartTagValueForRemovedElement(softMovedToOrDeletedNewElement, context);
        if (attrName != null && attrValue != null) {
            XercesUtils.addAttribute(node, attrName, attrValue);
        }
        getChangedElementContent(node, context.getNewElement(), context.getAttrName(), context.getRemovedValue());
        addToResultNode(context, node);
    }

    private String getStartTagValueForRemovedElement(Element newElement, ContentComparatorContext context) {
        String attrValue;
        if (context.getThreeWayDiff()) {
            if (context.getIntermediateContentElements() != null && newElement!= null && context.getIntermediateContentElements().get(newElement.getTagId()) == null) {
                attrValue = context.getRemovedIntermediateValue();
            } else {
                attrValue = context.getRemovedOriginalValue();
            }
        } else {
            attrValue = context.getRemovedValue();
        }
        return attrValue;
    }

    private String getStartTagValueForRemovedElementFromAncestor(Element element, ContentComparatorContext context) {
        String attrValue;
        if (context.getThreeWayDiff()) {
            Element ancestor = getFirstAncestorWithId(element);
            if (ancestor != null && context.getIntermediateContentElements() != null
                    && context.getIntermediateContentElements().containsKey(ancestor.getTagId())) {
                attrValue = context.getRemovedIntermediateValue();
            } else {
                attrValue = context.getRemovedOriginalValue();
            }
        } else {
            attrValue = context.getRemovedValue();
        }
        return attrValue;
    }

    private Element getFirstAncestorWithId(Element element) {
        Element parent = element;
        while (parent != null && parent.getTagId() == null) {
            parent = parent.getParent();
        }
        return parent;
    }

    // Checks that old list content still exists in new content
    private boolean emptyListInOldContext(ContentComparatorContext context) {
        boolean isEmptyList = true;
        if (!context.getOldElement().getTagName().equals(LIST)) {
            isEmptyList = false;
        } else if (context.getNewContentElements().containsKey(context.getOldElement().getTagId())
                || context.getNewContentElements().containsKey(SOFT_TRANSFORM_PLACEHOLDER_ID_PREFIX + context.getOldElement().getTagId())) {
            isEmptyList = false;
        } else {
            for (Element child : context.getOldElement().getChildren()) {
                if (!context.getNewContentElements().containsKey(child.getTagId())
                        && !context.getNewContentElements().containsKey(SOFT_TRANSFORM_PLACEHOLDER_ID_PREFIX + child.getTagId())) {
                    isEmptyList = false;
                    break;
                }
            }
        }
        return isEmptyList;
    }

    private boolean isRemovedNumInUnumbered(ContentComparatorContext context) {
        return ((isElementIndented(context.getNewContentRoot())
                || context.getNewContentRoot().getTagName().equals(SUBPARAGRAPH)
                || context.getNewContentRoot().getTagName().equals(SUBPOINT))
                && context.getOldElement().getTagName().equals(NUM));
    }

    private ContentComparatorContext findIntermediateElement(ContentComparatorContext context) {
        Element intermediateElement = context.getIntermediateElement();
        Node intermediateNode = context.getIntermediateContentNode();
        //element is soft deleted or moved in intermediate, and it's ID was prepended with SOFT_DELETE_PLACEHOLDER_ID_PREFIX or SOFT_MOVE_PLACEHOLDER_ID_PREFIX
        if (context.getIntermediateContentElements().containsKey(SOFT_DELETE_PLACEHOLDER_ID_PREFIX + context.getOldElement().getTagId())) {
            intermediateElement = context.getIntermediateContentElements().get(SOFT_DELETE_PLACEHOLDER_ID_PREFIX + context.getOldElement().getTagId());
        } else if (context.getIntermediateContentElements().containsKey(SOFT_MOVE_PLACEHOLDER_ID_PREFIX + context.getOldElement().getTagId())) {
            intermediateElement = context.getIntermediateContentElements().get(SOFT_MOVE_PLACEHOLDER_ID_PREFIX + context.getOldElement().getTagId());
        } else if (intermediateElement == null || isCurrentElementIgnored(context.getIntermediateElement().getNode())) {
            intermediateElement = context.getOldElement();
            intermediateNode = context.getOldContentNode();
        } else if (!context.getIntermediateElement().getTagId().equals(context.getOldElement().getTagId()) &&
                context.getIntermediateContentElements().containsKey(context.getOldElement().getTagId())) {
            intermediateElement = context.getIntermediateContentElements().get(context.getOldElement().getTagId());
        }
        ContentComparatorContext newContext = new ContentComparatorContext.Builder(context)
                .withIntermediateElement(intermediateElement)
                .withIntermediateContentNode(intermediateNode)
                .build();
        return newContext;
    }

    private void appendMovedOrTransformedContent(ContentComparatorContext context, Element element) {
        String attrValue;
        if (context.getThreeWayDiff()) {
            attrValue = containsSoftMoveToTransformedElement(context.getIntermediateContentElements(), context.getOldElement()) ?
                    context.getRemovedOriginalValue() : context.getRemovedIntermediateValue();
        } else {
            attrValue = context.getRemovedValue();
        }
        Node node = XercesUtils.getElementById(context.getNewContentNode(), element.getTagId());
        XercesUtils.insertOrUpdateAttributeValue(node, context.getAttrName(), attrValue);
        addReadOnlyAttributes(node);
        addToResultNode(context, node);
    }

    private void appendMovedToElementWithoutContent(ContentComparatorContext context) {
        Element softMovedToElement = context.getNewContentElements().get(SOFT_MOVE_PLACEHOLDER_ID_PREFIX + context.getOldElement().getTagId());
        Node removedNode = softMovedToElement == null ? null : XercesUtils.getElementById(context.getResultNode(), softMovedToElement.getTagId());
        if (softMovedToElement != null && removedNode == null) {
            String attrValue;
            if (context.getThreeWayDiff()) {
                attrValue = containsSoftMoveToElement(context.getIntermediateContentElements(), context.getOldElement()) ?
                        context.getRemovedOriginalValue() : context.getRemovedIntermediateValue();
            } else {
                attrValue = context.getRemovedValue();
            }
            Node node = XercesUtils.getElementById(context.getNewContentNode(), softMovedToElement.getTagId());
            // LEOS-5819: If the moved element is also out/indented, then append original node.
            if(isElementIndented(softMovedToElement)) {
            	node = context.getOldElement().getNode().cloneNode(true);
                Node newNode = softMovedToElement.getNode();
                NamedNodeMap attributesMap = newNode.getAttributes();
                if (attributesMap != null) {
                    for (int i = 0; i < attributesMap.getLength(); i++) {
                    	Node nodeAttribute = attributesMap.item(i);
                    	if(!XercesUtils.containsAttributeWithValue(node, nodeAttribute.getNodeName(), nodeAttribute.getTextContent())) {
                    		XercesUtils.insertOrUpdateAttributeValue(node, nodeAttribute.getNodeName(), nodeAttribute.getTextContent());
                    	}
    				}
                }
            }
            XercesUtils.insertOrUpdateAttributeValue(node, context.getAttrName(), attrValue);
            addReadOnlyAttributes(node);
            addToResultNode(context, node);
        }
    }

    @Override
    protected void appendIndentedDeletedOrMovedToContent(ContentComparatorContext context, Element element) {
        String attrName = context.getAttrName();
        String attrValue = getStartTagValueForRemovedElement(element, context);
        if (attrName != null && attrValue != null) {
            Node node = element.getNode();
            XercesUtils.insertOrUpdateAttributeValue(node, context.getAttrName(), attrValue);
            addToResultNode(context, node);
        }
    }

    // This functions appends removed CN indented element when this element is not anymore child of new content root
    @Override
    protected void appendIndentedAndRemovedParent(ContentComparatorContext context, Element element) {
        Element elementInOldContext = findElementInOtherContext(element, context.getOldContentElements());
        Element parentInOtherContext = isIndentedAndRemovedParent(context, elementInOldContext);
        if (parentInOtherContext != null) {
            List<Element> notDeletedElements = getNotDeletedElementsFromContent(context.getNewContentElements(), parentInOtherContext);
            int index = notDeletedElements.indexOf(elementInOldContext);
            if ((index <= 0)
                    || (!isECOrigin(notDeletedElements.get(index-1)) && isElementRemovedInOtherContext(context.getNewContentElements(), notDeletedElements.get(index-1)))) {
                Node node = parentInOtherContext.getNode();
                addReadOnlyAttributes(node);
                removeNotDeletedElementsFromContent(context, notDeletedElements, parentInOtherContext, node);
                XercesUtils.insertOrUpdateAttributeValue(node, context.getAttrName(), context.getThreeWayDiff() ? context.getRemovedOriginalValue() : context.getRemovedValue());
                addToResultNode(context, node);
            }
        }
    }

    // This functions appends removed CN indented element when this element is not anymore child of new content root
    @Override
    protected void appendIndentedAndRemovedIntermediateParent(ContentComparatorContext context, Element element) {
        Element elementInIntermediateContext = findElementInOtherContext(element, context.getIntermediateContentElements());
        Element parentInOtherContext = isIndentedAndRemovedParent(context, elementInIntermediateContext);
        if (parentInOtherContext != null) {
            List<Element> notDeletedElements = getNotDeletedElementsFromContent(context.getNewContentElements(), parentInOtherContext);
            int index = notDeletedElements.indexOf(elementInIntermediateContext);
            if ((index <= 0)
                    || (!isECOrigin(notDeletedElements.get(index-1)) && isElementRemovedInOtherContext(context.getNewContentElements(), notDeletedElements.get(index-1)))) {
                Node node = parentInOtherContext.getNode();
                addReadOnlyAttributes(node);
                removeNotDeletedElementsFromContent(context, notDeletedElements, parentInOtherContext, node);
                XercesUtils.insertOrUpdateAttributeValue(node, context.getAttrName(), context.getRemovedValue());
                addToResultNode(context, node);
            }
        }
    }

    private void removeNotDeletedElementsFromContent(ContentComparatorContext context, List<Element> notDeletedElements, Element element, Node node) {
        for (Element child : element.getChildren()) {
            Node childNode = child.getNode();
            if (getAllowedTags().contains(child.getTagName())) {
                if (!notDeletedElements.contains(child)
                        || isSoftAction(childNode, SoftActionType.MOVE_TO)
                        || isSoftAction(childNode, SoftActionType.DELETE)
                        || isSoftAction(childNode, SoftActionType.DELETE_TRANSFORM)) {
                    addReadOnlyAttributes(childNode);
                    XercesUtils.insertOrUpdateAttributeValue(childNode, context.getAttrName(), context.getThreeWayDiff() ? context.getRemovedOriginalValue() : context.getRemovedValue());
                    removeNotDeletedElementsFromContent(context, notDeletedElements, child, childNode);
                } else {
                    try {
                        node.removeChild(childNode);
                        if (((DeferredElementImpl) node).getChildElementCount() == 0 && element.getTagName().equals(LIST)) {
                            node.getParentNode().removeChild(node);
                            return;
                        }
                    } catch (Exception error) {
                        LOG.debug("Node already removed");
                    }
                }
            } else {
                removeNotDeletedElementsFromContent(context, notDeletedElements, child, childNode);
            }
        }
    }
}
