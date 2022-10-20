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
import eu.europa.ec.leos.services.clone.CloneContext;
import eu.europa.ec.leos.services.compare.vo.Element;
import eu.europa.ec.leos.services.processor.content.XmlContentProcessor;
import eu.europa.ec.leos.services.support.XercesUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.w3c.dom.Node;

import java.util.Map;

import static eu.europa.ec.leos.services.compare.ComparisonHelper.isSoftAction;
import static eu.europa.ec.leos.services.compare.ComparisonHelper.withPlaceholderPrefix;
import static eu.europa.ec.leos.services.support.XmlHelper.EMPTY_STRING;
import static eu.europa.ec.leos.services.support.XmlHelper.SOFT_DELETE_PLACEHOLDER_ID_PREFIX;
import static eu.europa.ec.leos.services.support.XmlHelper.SOFT_MOVE_PLACEHOLDER_ID_PREFIX;
import static eu.europa.ec.leos.services.support.XmlHelper.SOFT_TRANSFORM_PLACEHOLDER_ID_PREFIX;
import static eu.europa.ec.leos.services.compare.IndentContentComparatorHelper.isElementIndented;

@Service
@Instance(instances = {InstanceType.COMMISSION, InstanceType.OS})
public class XMLContentComparatorServiceImplProposal extends XMLContentComparatorServiceImpl {

    protected CloneContext cloneContext;

    @Autowired
    public XMLContentComparatorServiceImplProposal(MessageHelper messageHelper, TextComparator textComparator,
                                                   CloneContext cloneContext, SecurityContext securityContext, XmlContentProcessor xmlContentProcessor) {
        super(messageHelper, textComparator, securityContext, xmlContentProcessor);
        this.cloneContext = cloneContext;
    }

    private boolean isClonedProposalOrContribution() {
        return (cloneContext != null && (cloneContext.isClonedProposal() || cloneContext.isContribution()));
    }

    @Override
    protected boolean shouldDisplayRemovedContent(Element elementOldContent, int indexOfOldElementInNewContent) {
        return isElementRemovedFromContent(indexOfOldElementInNewContent);
    }

    @Override
    protected boolean containsIgnoredElements(Node node) {
        if (isClonedProposalOrContribution()) {
            return super.containsIgnoredElements(node);
        }
        return false;
    }

    @Override
    protected boolean containsDeletedElementInNewContent(ContentComparatorContext context) {
        return Boolean.FALSE;
    }

    @Override
    protected boolean containsAddedNonIgnoredElements(Node node) {
        if (isClonedProposalOrContribution()) {
            return super.containsAddedNonIgnoredElements(node);
        }
        return false;
    }

    @Override
    protected boolean isElementInItsOriginalPosition(Element element) {
        if (isClonedProposalOrContribution()) {
            return super.isElementInItsOriginalPosition(element);
        }
        return true;
    }

    @Override
    protected boolean shouldIgnoreElement(Element element) {
        return element != null
                && (isSoftAction(element.getNode(), SoftActionType.DELETE)
                || isSoftAction(element.getNode(), SoftActionType.MOVE_TO)
                || withPlaceholderPrefix(element.getNode(), SOFT_DELETE_PLACEHOLDER_ID_PREFIX))
                && !isSoftAction(element.getNode(), SoftActionType.DELETE_TRANSFORM)
                ;
    }

    @Override
    protected boolean isElementMovedOrTransformed(Element element) {
        if (isClonedProposalOrContribution()) {
            return super.isElementMovedOrTransformed(element);
        }
        return false;
    }

    @Override
    protected boolean shouldCompareElements(Element oldElement, Element newElement) {
        if (isClonedProposalOrContribution()) {
            return super.shouldCompareElements(oldElement, newElement);
        }
        return true;
    }

    @Override
    protected boolean shouldIgnoreRenumbering(Element element) {
        if (isClonedProposalOrContribution()) {
            return super.shouldIgnoreRenumbering(element);
        }
        return false;
    }

    @Override
    protected boolean isActionRoot(Node node) {
        if (isClonedProposalOrContribution()) {
            return super.isActionRoot(node);
        }
        return false;
    }

    @Override
    protected Node buildNodeForAddedElement(Element newElement, Element oldElement, ContentComparatorContext context) {
        return buildNode(context.getNewElement(), context.getStartTagAttrName(), context.getStartTagAttrValue());
    }

    @Override
    protected Node getChangedElementContent(Node contentNode, Element element, String attrName, String attrValue) {
        Node node;
        if (isClonedProposalOrContribution()) {
            node = super.getChangedElementContent(contentNode, element, attrName, attrValue);
        } else {
            node = element.getNode();
            XercesUtils.insertOrUpdateAttributeValue(node, attrName, attrValue);
        }
        return node;
    }

    @Override
    protected void appendAddedElementContent(ContentComparatorContext context) {
        if (isClonedProposalOrContribution()) {
            appendAddedElement(context);
        } else {
            Node node = getChangedElementContent(context.getNewElement().getNode(), context.getNewElement(), context.getAttrName(), context.getAddedValue());
            addToResultNode(context, node);
        }
    }

    private void appendAddedElement(ContentComparatorContext context) {
        String newElementTagId = context.getNewElement().getTagId();
        if (context.getDisplayRemovedContentAsReadOnly() && !shouldIgnoreElement(context.getNewElement())) {
            if (newElementTagId != null) {
                if (context.getOldContentElements().containsKey(newElementTagId.replace(
                        SOFT_TRANSFORM_PLACEHOLDER_ID_PREFIX, EMPTY_STRING))  && !isElementIndented(context.getNewElement())) {
                    //append the soft movedFrom element content compared to the original content and ignore its renumbering
                    Element originalMovedElementInOldContent = context.getOldContentElements().get(context.getNewElement().getTagId().replace(SOFT_TRANSFORM_PLACEHOLDER_ID_PREFIX, EMPTY_STRING));
                    compareElementContents(new ContentComparatorContext.Builder(context)
                            .withOldElement(originalMovedElementInOldContent)
                            .withDisplayRemovedContentAsReadOnly(Boolean.TRUE)
                            .withIgnoreElements(Boolean.TRUE)
                            .withIgnoreRenumbering(Boolean.TRUE)
                            .withStartTagAttrName(context.getAttrName())
                            .withStartTagAttrValue(context.getAddedValue())
                            .build());
                } else if(isElementIndented(context.getNewElement())) {
                    //If newElement is indented that means compare the children for text changes
                    Element originalElementInOldContent = context.getOldContentElements().get(context.getNewElement().getTagId());
                    compareElementContents(new ContentComparatorContext.Builder(context)
                            .withOldElement(originalElementInOldContent)
                            .withDisplayRemovedContentAsReadOnly(Boolean.FALSE)
                            .withIgnoreElements(Boolean.TRUE)
                            .withIgnoreRenumbering(Boolean.TRUE)
                            .withStartTagAttrName(null)
                            .withStartTagAttrValue(null)
                            .build());
                } else if (!newElementTagId.startsWith(SOFT_MOVE_PLACEHOLDER_ID_PREFIX)
                        && !newElementTagId.startsWith(SOFT_DELETE_PLACEHOLDER_ID_PREFIX)
                        && !newElementTagId.startsWith(SOFT_TRANSFORM_PLACEHOLDER_ID_PREFIX)
                        && !isElementIndented(context.getNewElement())) {
                    compareElementContents(new ContentComparatorContext.Builder(context)
                            .withIndexOfOldElementInNewContent(-1)
                            .withOldElement(null)
                            .withDisplayRemovedContentAsReadOnly(Boolean.TRUE)
                            .withIgnoreElements(Boolean.TRUE)
                            .withIgnoreRenumbering(Boolean.TRUE)
                            .withStartTagAttrName(context.getAttrName())
                            .withStartTagAttrValue(context.getAddedValue())
                            .build());
                }
            } else {
                Node node = getChangedElementContent(context.getNewContentNode(), context.getNewElement(), context.getAttrName(), context.getAddedValue());
                addToResultNode(context, node);
            }
        } else {
            Node node = getNonIgnoredChangedElementContent(context.getNewContentNode(), context.getNewElement(), context.getAttrName(), context.getAddedValue());
            addToResultNode(context, node);
        }
    }

    @Override
    protected void appendRemovedElementContent(ContentComparatorContext context) {
        if (isClonedProposalOrContribution()) {
            appendSoftRemovedElementContent(context);
        } else {
            Node node = getChangedElementContent(context.getOldElement().getNode(), context.getOldElement(), context.getAttrName(), context.getRemovedValue());
            addToResultNode(context, node);
        }
    }

    @Override
    protected void appendRemovedContent(ContentComparatorContext context) {
        if (isClonedProposalOrContribution()) {
            if ((isSoftAction(context.getOldElement().getNode(), SoftActionType.ADD)) ||
                    (isSoftAction(context.getOldElement().getNode(), SoftActionType.MOVE_FROM) && !context.getNewContentElements().containsKey(context.getOldElement().getTagId()))) {
                //If element is added in old content but deleted in the new one look for leos:softAction="add" in old element OR
                //If element contains soft action move_from in old content but deleted in new content display the move_from element as deleted
                Node node = getChangedElementContent(context.getOldContentNode(), context.getOldElement(), context.getAttrName(), context.getRemovedValue());
                addReadOnlyAttributes(node);
                addToResultNode(context, node);
            } else if (containsSoftDeleteElement(context.getOldElement(), context.getNewContentElements())) {
                //If element is soft deleted in new content then print the deleted element from new content
                Element softDeletedNewElement = context.getNewContentElements().get(SOFT_DELETE_PLACEHOLDER_ID_PREFIX + context.getOldElement().getTagId());
                Node node = XercesUtils.getElementById(context.getNewContentNode(), softDeletedNewElement.getTagId());
                XercesUtils.insertOrUpdateAttributeValue(node, context.getAttrName(), context.getRemovedValue());
                addReadOnlyAttributes(node);
                addToResultNode(context, node);
            }
        } else {
            appendRemovedElementContent(context);
        }
    }

    private String getStartTagValueForRemovedElementFromAncestor(Element element, ContentComparatorContext context) {
        return context.getRemovedValue();
    }

    @Override
    protected boolean shouldAddElement(Element oldElement, Map<String, Element> contentElements) {
        return false;
    }

    @Override
    protected boolean isIgnoredElement(Element element) {
        return false;
    }

    @Override
    protected boolean isCurrentElementIgnoredInNewContent(ContentComparatorContext context) {
        return false;
    }

    @Override
    protected boolean isCurrentElementNonIgnored(Node node) {
        if (isClonedProposalOrContribution()) {
            return isSoftAction(node, SoftActionType.MOVE_FROM);
        }
        return false;
    }

    @Override
    protected boolean isCurrentElementIgnored(Node node) {
        return false;
    }

    @Override
    protected int getIndexOfIgnoredElementInNewContent(ContentComparatorContext context) {
        if (isClonedProposalOrContribution()) {
            return super.getIndexOfIgnoredElementInNewContent(context);
        }
        return -1;
    }

    private void appendSoftRemovedElementContent(ContentComparatorContext context) {
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
                } else if (context.getNewContentElements().containsKey(SOFT_DELETE_PLACEHOLDER_ID_PREFIX + SOFT_TRANSFORM_PLACEHOLDER_ID_PREFIX +
                        context.getOldElement().getParent().getTagId())) {
                    //element was soft deleted, and it's ID was prepended with SOFT_DELETE_PLACEHOLDER_ID_PREFIX + SOFT_TRANSFORM_PLACEHOLDER_ID_PREFIX
                    Element softDeletedTransformedElement = context.getNewContentElements().get(SOFT_DELETE_PLACEHOLDER_ID_PREFIX + SOFT_TRANSFORM_PLACEHOLDER_ID_PREFIX +
                            context.getOldElement().getParent().getTagId());
                    int indexOfSoftDeletedElementInNewContent = getBestMatchInList(softDeletedTransformedElement.getParent().getChildren(),
                            softDeletedTransformedElement);

                    compareElementContents(new ContentComparatorContext.Builder(context)
                            .withIndexOfOldElementInNewContent(indexOfSoftDeletedElementInNewContent)
                            .withNewElement(softDeletedTransformedElement)
                            .withDisplayRemovedContentAsReadOnly(Boolean.TRUE)
                            .withIgnoreElements(Boolean.FALSE)
                            .withIgnoreRenumbering(Boolean.FALSE)
                            .withStartTagAttrName(context.getAttrName())
                            .withStartTagAttrValue(getStartTagValueForRemovedElement(softDeletedTransformedElement, context))
                            .build());
                } else if (containsSoftMoveToElement(context.getNewContentElements(), context.getOldElement())) {
                    //append the soft movedTo element content
                    appendMovedToElementWithoutContent(context);
                } else if (containsSoftDeleteElement(context.getOldElement(), context.getNewContentElements())) {
                    //element was soft deleted, and it's ID was prepended with SOFT_DELETE_PLACEHOLDER_ID_PREFIX
                    Element softDeletedNewElement = context.getNewContentElements().get(SOFT_DELETE_PLACEHOLDER_ID_PREFIX + context.getOldElement().getTagId());
                    int indexOfSoftDeletedElementInNewContent = getBestMatchInList(softDeletedNewElement.getParent().getChildren(), softDeletedNewElement);

                    compareElementContents(new ContentComparatorContext.Builder(context)
                            .withIndexOfOldElementInNewContent(indexOfSoftDeletedElementInNewContent)
                            .withNewElement(softDeletedNewElement)
                            .withDisplayRemovedContentAsReadOnly(Boolean.TRUE)
                            .withIgnoreElements(Boolean.FALSE)
                            .withIgnoreRenumbering(Boolean.FALSE)
                            .withStartTagAttrName(context.getAttrName())
                            .withStartTagAttrValue(getStartTagValueForRemovedElement(softDeletedNewElement, context))
                            .build());
                } else if (!isSoftAction(context.getNewElement().getNode(), SoftActionType.TRANSFORM) && !isSoftAction(context.getOldElement().getNode(), SoftActionType.TRANSFORM)
                        && !context.getNewContentElements().containsKey(context.getOldElement().getTagId())) {
                    //Element is added/present in old content but deleted from new content, so just display the deleted content
                    String attrValue = getStartTagValueForRemovedElementFromAncestor(context.getOldElement(), context);
                    Node node = getChangedElementContent(context.getOldContentNode(), context.getOldElement(), context.getAttrName(), attrValue);
                    addReadOnlyAttributes(node);
                    addToResultNode(context, node);
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

    private void appendMovedOrTransformedContent(ContentComparatorContext context, Element element) {
        Node node = XercesUtils.getElementById(context.getNewContentNode(), element.getTagId());
        XercesUtils.insertOrUpdateAttributeValue(node, context.getAttrName(), context.getRemovedValue());
        addReadOnlyAttributes(node);
        addToResultNode(context, node);
    }

    private void appendMovedToElementWithoutContent(ContentComparatorContext context) {
        Element softMovedToElement = context.getNewContentElements().get(SOFT_MOVE_PLACEHOLDER_ID_PREFIX + context.getOldElement().getTagId());
        if (softMovedToElement != null) {
            appendMovedOrTransformedContent(context, softMovedToElement);
        }
    }

    private String getStartTagValueForRemovedElement(Element newElement, ContentComparatorContext context) {
        return context.getRemovedValue();
    }

    @Override
    protected Node buildNodeForAddedElement(ContentComparatorContext context) {
        return buildNode(context.getNewElement());
    }

    @Override
    protected Node buildNodeForRemovedElement(Element element, ContentComparatorContext context, Map<String, Element> contentElements) {
        return buildNode(element);
    }

    @Override
    protected void appendIndentedDeletedOrMovedToContent(ContentComparatorContext context, Element element) {
    }

    @Override
    protected String getRemovedNumContent(ContentComparatorContext context) {
        if (isClonedProposalOrContribution()) {
            return context.getNewElement().getNode().getTextContent();
        } else {
            return super.getRemovedNumContent(context);
        }
    }
}
