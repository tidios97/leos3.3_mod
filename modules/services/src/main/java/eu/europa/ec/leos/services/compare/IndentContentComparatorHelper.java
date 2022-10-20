package eu.europa.ec.leos.services.compare;

import eu.europa.ec.leos.model.action.SoftActionType;
import eu.europa.ec.leos.services.compare.vo.Element;
import eu.europa.ec.leos.services.support.XmlHelper;
import eu.europa.ec.leos.services.processor.content.indent.IndentConversionHelper;
import eu.europa.ec.leos.services.support.XercesUtils;
import org.apache.commons.lang.ArrayUtils;
import org.w3c.dom.Node;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static eu.europa.ec.leos.services.compare.ContentComparatorService.CONTENT_ADDED_CLASS;
import static eu.europa.ec.leos.services.compare.ContentComparatorService.CONTENT_SOFT_ADDED_CLASS;
import static eu.europa.ec.leos.services.support.XmlHelper.CONTENT;
import static eu.europa.ec.leos.services.support.XmlHelper.EC;
import static eu.europa.ec.leos.services.support.XmlHelper.INDENT;
import static eu.europa.ec.leos.services.support.XmlHelper.LEOS_INDENT_ORIGIN_TYPE_ATTR;
import static eu.europa.ec.leos.services.support.XmlHelper.LEOS_ORIGIN_ATTR;
import static eu.europa.ec.leos.services.support.XmlHelper.LIST;
import static eu.europa.ec.leos.services.support.XmlHelper.NUM;
import static eu.europa.ec.leos.services.support.XmlHelper.POINT;
import static eu.europa.ec.leos.services.support.XmlHelper.SOFT_DELETE_PLACEHOLDER_ID_PREFIX;
import static eu.europa.ec.leos.services.support.XmlHelper.SOFT_MOVE_PLACEHOLDER_ID_PREFIX;
import static eu.europa.ec.leos.services.support.XmlHelper.SOFT_TRANSFORM_PLACEHOLDER_ID_PREFIX;
import static eu.europa.ec.leos.services.support.XmlHelper.SUBPARAGRAPH;
import static eu.europa.ec.leos.services.support.XmlHelper.SUBPOINT;
import static eu.europa.ec.leos.services.compare.ComparisonHelper.isSoftAction;

class IndentContentComparatorHelper {
    public static final List<String> ID_PREFIXES = Arrays.asList(XmlHelper.SOFT_DELETE_PLACEHOLDER_ID_PREFIX,
            XmlHelper.SOFT_MOVE_PLACEHOLDER_ID_PREFIX,
            XmlHelper.SOFT_TRANSFORM_PLACEHOLDER_ID_PREFIX,
            IndentConversionHelper.INDENT_PLACEHOLDER_ID_PREFIX);

    public static Element findElementInOtherContext(Element element, Map<String, Element> otherContentElements) {
        StringBuilder elementId = new StringBuilder(element.getTagId());
        Element elementInOtherContext = otherContentElements.get(elementId.toString());

        if (elementInOtherContext == null) {
            // Trying to find element without prefix
            for (String prefix : ID_PREFIXES) {
                if (element.getTagId().startsWith(prefix)) {
                    elementId.replace(0, prefix.length(), "");
                    elementInOtherContext = otherContentElements.get(elementId.toString());
                }
            }
            if (elementInOtherContext == null) {
                // Try with prefix
                for (String prefix : ID_PREFIXES) {
                    StringBuilder elementIdToSearch = new StringBuilder(element.getTagId());
                    elementIdToSearch.insert(0, prefix);
                    elementInOtherContext = otherContentElements.get(elementIdToSearch.toString());
                    if (elementInOtherContext != null) {
                        break;
                    }
                }
            }
        }
        return elementInOtherContext;
    }

    public static List<String> getAllowedTags() {
        List<String> tocTags = new ArrayList<>();
        tocTags.addAll(Arrays.asList(IndentConversionHelper.NUMBERED_ITEMS));
        tocTags.addAll(Arrays.asList(IndentConversionHelper.UNUMBERED_ITEMS));
        return tocTags;
    }

    public static Boolean isElementRemovedInOtherContext(Map<String, Element> otherContentElements, Element element) {
        Element oldElementInNewContent = otherContentElements.get(XmlHelper.SOFT_DELETE_PLACEHOLDER_ID_PREFIX + element.getTagId());

        if (oldElementInNewContent != null) {
            return true;
        } else {
            oldElementInNewContent = otherContentElements.get(XmlHelper.SOFT_MOVE_PLACEHOLDER_ID_PREFIX + element.getTagId());
            if (oldElementInNewContent != null) {
                return true;
            } else {
                oldElementInNewContent = otherContentElements.get(element.getTagId());
                if ((oldElementInNewContent == null)) {
                    oldElementInNewContent = otherContentElements.get(element.getTagId().replace(IndentConversionHelper.INDENT_PLACEHOLDER_ID_PREFIX, ""));
                    return (oldElementInNewContent == null);
                }
            }
        }
        return false;
    }

    private static Boolean isElementSoftMoveToOrSoftDeletedInOtherContext(Map<String, Element> otherContentElements, Element element) {
        Element oldElementInNewContent = otherContentElements.get(XmlHelper.SOFT_DELETE_PLACEHOLDER_ID_PREFIX + element.getTagId());

        if (oldElementInNewContent != null) {
            return true;
        } else {
            oldElementInNewContent = otherContentElements.get(XmlHelper.SOFT_MOVE_PLACEHOLDER_ID_PREFIX + element.getTagId());
            return (oldElementInNewContent != null);
        }
    }

    public static Boolean containsNotDeletedElementsInOtherContext(Map<String, Element> otherContentElements, Element element) {
        return (!isECOrigin(element) || (element.getTagName().equals(LIST) && !isElementSoftMoveToOrSoftDeletedInOtherContext(otherContentElements, element))) && getNotDeletedElementsFromContent(otherContentElements, element).size() > 0;
    }

    public static boolean wasChildOfPreviousSibling(Element newElement, ContentComparatorContext context, int currentIndex) {
        if (currentIndex > 0) {
            Element previousSibling = context.getNewContentRoot().getChildren().get(currentIndex - 1);
            Element previousSiblingInOldContent = context.getOldContentElements().get(previousSibling.getTagId());
            Element newElementInOldContent = context.getOldContentElements().get(newElement.getTagId());
            if (previousSiblingInOldContent != null && newElementInOldContent != null) {
                return isChildOf(previousSiblingInOldContent, newElementInOldContent);
            }
        }
        return false;
    }

    public static boolean isNewElementOutdentedFromOld(ContentComparatorContext context) {
        Element oldElementInNewContent = context.getNewContentElements().get(context.getOldElement().getTagId());
        Element newElementInOldContent = context.getOldContentElements().get(context.getNewElement().getTagId());
        if(newElementInOldContent != null && oldElementInNewContent == null) {
            return isChildOf(context.getOldElement(), newElementInOldContent);
        }
        return false;
    }

    public static Element isIndentedAndRemovedParent(ContentComparatorContext context, Element elementInOtherContext) {
        Element parentInOtherContext = getIndentedAndRemovedParent(elementInOtherContext);
        if (parentInOtherContext != null && !isECOrigin(parentInOtherContext)
                && isElementIndented(parentInOtherContext)
                && ((!context.getThreeWayDiff() && isElementRemovedInOtherContext(context.getNewContentElements(), parentInOtherContext))
                || (context.getThreeWayDiff() && isElementRemovedInOtherContext(context.getNewContentElements(), parentInOtherContext)
                && isElementRemovedInOtherContext(context.getIntermediateContentElements(), parentInOtherContext)))) {
            return parentInOtherContext;
        } else {
            return null;
        }
    }

    public static List<Element> getNotDeletedElementsFromContent(Map<String, Element> otherContentElements, Element element) {
        List<Element> notDeletedElements = new ArrayList();
        if (Arrays.asList(IndentConversionHelper.NUMBERED_ITEMS).contains(element.getTagName()) || element.getTagName().equals(LIST)) {
            for (Element child : element.getChildren()) {
                if (getAllowedTags().contains(child.getTagName())
                        && (isECOrigin(child) || !isElementRemovedInOtherContext(otherContentElements, child))) {
                    notDeletedElements.add(child);
                }
                notDeletedElements.addAll(getNotDeletedElementsFromContent(otherContentElements, child));
            }
        } else if (Arrays.asList(IndentConversionHelper.UNUMBERED_ITEMS).contains(element.getTagName())) {
            Element parent = element.getParent();
            int startIndex = parent.getChildren().indexOf(element);
            for (int i = startIndex + 1; i < parent.getChildren().size(); i++) {
                Element child = parent.getChildren().get(i);
                if (getAllowedTags().contains(child.getTagName())
                        && (isECOrigin(child) || !isElementRemovedInOtherContext(otherContentElements, child))) {
                    notDeletedElements.add(child);
                }
                notDeletedElements.addAll(getNotDeletedElementsFromContent(otherContentElements, child));
            }
        }
        return notDeletedElements;
    }

    public static boolean isECOrigin(Element element) {
        return XercesUtils.containsAttributeWithValue(element.getNode(), LEOS_ORIGIN_ATTR, EC);
    }

    public static boolean hasIndentedChild(Element element) {
        List<Element> children = element.getChildren();
        for (Element child : children) {
            if (isElementIndented(child) || hasIndentedChild(child)) {
                return true;
            }
        }
        return false;
    }

    public static Boolean isElementIndentedInOtherContext(Map<String, Element> otherContextElements, Element element) {
        if (element.getTagName().equals(LIST)) {
            return (containsOnlyIndentedElements(otherContextElements, element));
        }

        Element oldElementInOtherContent = otherContextElements.get(SOFT_TRANSFORM_PLACEHOLDER_ID_PREFIX + element.getTagId());

        if (oldElementInOtherContent != null
                && isDifferentIndent(element, oldElementInOtherContent)) {
            return true;
        }
        if (isSoftAction(element.getNode(), SoftActionType.ADD)) {
            oldElementInOtherContent = otherContextElements.get(IndentConversionHelper.INDENT_PLACEHOLDER_ID_PREFIX + element.getTagId());
            if (oldElementInOtherContent != null && isDifferentIndent(element, oldElementInOtherContent)) {
                return true;
            }
        }
        oldElementInOtherContent = otherContextElements.get(element.getTagId());
        return oldElementInOtherContent != null && (isDifferentIndent(element, oldElementInOtherContent)
                // While element is from cn, indented attribute is not necessary present
                || (isSoftAction(oldElementInOtherContent.getNode(), SoftActionType.ADD)
                && ((hasIndentedParent(oldElementInOtherContent) && !hasIndentedParent(element))
                || (!hasIndentedParent(oldElementInOtherContent) && hasIndentedParent(element)))));
    }

    public static Boolean isRemovedElementIndentedInNewContext(ContentComparatorContext context, Element element) {
        Element oldElementInNewContent = context.getNewContentElements().get(SOFT_DELETE_PLACEHOLDER_ID_PREFIX + element.getTagId());

        if (oldElementInNewContent != null && isIndentAction(oldElementInNewContent.getNode())) {
            return true;
        } else if (oldElementInNewContent == null) {
            oldElementInNewContent = context.getNewContentElements().get(SOFT_MOVE_PLACEHOLDER_ID_PREFIX + element.getTagId());
            return (oldElementInNewContent != null && isIndentAction(oldElementInNewContent.getNode()));
        }
        return false;
    }

    public static boolean isIndentedRenumbering(Element oldElement, Element newElement) {
        return oldElement != null && newElement != null
                && NUM.equals(oldElement.getTagName()) && NUM.equals(newElement.getTagName())
                && oldElement.getTagId().equals(newElement.getTagId())
                && (!isSoftAction(newElement.getParent().getNode(), SoftActionType.MOVE_FROM))
                && (isElementIndented(oldElement.getParent())
                || isElementIndented(newElement.getParent()));
    }

    public static boolean isElementIndented(Element element) {
        return element != null && isIndentAction(element.getNode());
    }

    public static boolean isElementUnumberedIndentedInOtherContext(Map<String, Element> otherContextElements, Element element) {
        if (!ArrayUtils.contains(IndentConversionHelper.UNUMBERED_ITEMS, element.getTagName())) {
            return false;
        }
        Element oldElementInOtherContent = otherContextElements.get(SOFT_TRANSFORM_PLACEHOLDER_ID_PREFIX + element.getTagId());

        if (oldElementInOtherContent != null
                && ArrayUtils.contains(IndentConversionHelper.NUMBERED_ITEMS, oldElementInOtherContent.getTagName())
                && isDifferentIndent(element, oldElementInOtherContent)) {
            return true;
        }
        if (isSoftAction(element.getNode(), SoftActionType.ADD)) {
            oldElementInOtherContent = otherContextElements.get(IndentConversionHelper.INDENT_PLACEHOLDER_ID_PREFIX + element.getTagId());
            if (oldElementInOtherContent != null
                    && ArrayUtils.contains(IndentConversionHelper.NUMBERED_ITEMS, oldElementInOtherContent.getTagName())
                    && isDifferentIndent(element, oldElementInOtherContent)) {
                return true;
            }
        }
        oldElementInOtherContent = otherContextElements.get(element.getTagId());
        return oldElementInOtherContent != null && ArrayUtils.contains(IndentConversionHelper.NUMBERED_ITEMS, oldElementInOtherContent.getTagName())
                && (isDifferentIndent(element, oldElementInOtherContent) || (isSoftAction(oldElementInOtherContent.getNode(), SoftActionType.ADD)
                && ((hasIndentedParent(oldElementInOtherContent) && !hasIndentedParent(element))
                || (!hasIndentedParent(oldElementInOtherContent) && hasIndentedParent(element)))));
    }

    public static boolean shouldBeMarkedAsSoftAdded(Element element, String attrValue) {
        if (attrValue != null && attrValue.equalsIgnoreCase(CONTENT_SOFT_ADDED_CLASS)) {
            if (element.getTagName().equalsIgnoreCase(CONTENT)
                    && isElementIndented(element.getParent())
                    && (!isSoftAction(element.getParent().getNode(), SoftActionType.ADD) && !isSoftAction(element.getParent().getNode(), SoftActionType.MOVE_FROM))) {
                return false;
            } else if ((isElementIndented(element)
                    || ((element.getTagId().contains(element.getParent().getTagId())
                    || element.getParent().getTagId().contains(element.getTagId()))
                    && isElementIndented(element.getParent())))
                    && (!isSoftAction(element.getNode(), SoftActionType.ADD) && !isSoftAction(element.getNode(), SoftActionType.MOVE_FROM))) {
                return false;
            } else if ((element.getTagName().equalsIgnoreCase(POINT)
                    || element.getTagName().equalsIgnoreCase(INDENT))
                    && isECOrigin(element)
                    && (!isSoftAction(element.getNode(), SoftActionType.ADD) && !isSoftAction(element.getNode(), SoftActionType.MOVE_FROM))) {
                return false;
            } else if ((element.getTagName().equalsIgnoreCase(SUBPOINT)
                    || element.getTagName().equalsIgnoreCase(SUBPARAGRAPH))
                    && isECOrigin(element)
                    && (!isSoftAction(element.getNode(), SoftActionType.ADD)
                    && !isSoftAction(element.getNode(), SoftActionType.MOVE_FROM))
                    && (hasIndentedChild(element.getParent())
                    || isElementIndented(element.getParent()))) {
                return false;
            }
        } else if (attrValue != null && attrValue.equalsIgnoreCase(CONTENT_ADDED_CLASS)) {
            if ((Arrays.asList(SUBPOINT, SUBPARAGRAPH).contains(element.getTagName())
                    && isSoftAction(element.getNode(), SoftActionType.TRANSFORM)) ||
                    (LIST.equalsIgnoreCase(element.getTagName()) && isElementIndented(element.getChildren().get(0)))) {
                return false;
            }
        }
        return true;
    }

    private static boolean isIndentAction(Node node) {
        return XercesUtils.containsAttribute(node, LEOS_INDENT_ORIGIN_TYPE_ATTR);
    }

    private static boolean isDifferentIndent(Element firstElement, Element secondElement) {
        if (isSoftAction(firstElement.getNode(), SoftActionType.ADD)) {
            // While element is from cn, indented attribute is not necessary present
            return ((isIndentAction(firstElement.getNode()) && !isIndentAction(secondElement.getNode()))
                    || (!isIndentAction(firstElement.getNode()) && isIndentAction(secondElement.getNode())));
        }
        return isIndentAction(secondElement.getNode());
    }

    private static boolean containsOnlyIndentedElements(Map<String, Element> otherContextElements, Element element) {
        if (element.getChildren() != null) {
            for (Element child : element.getChildren()) {
                if (!isElementIndentedInOtherContext(otherContextElements, child)) {
                    return false;
                }
            }
        }
        return true;
    }

    private static Boolean hasIndentedParent(Element element) {
        Element parent = element.getParent();
        if (parent != null && (isElementIndented(parent) || hasIndentedParent(parent))) {
            return true;
        }
        return false;
    }

    private static boolean isChildOf(Element parent, Element element) {
        List<Element> children = parent.getChildren();
        for (Element child : children) {
            if (child.getTagId().equals(element.getTagId()) || isChildOf(child, element)) {
                return true;
            }
        }
        return false;
    }

    private static Element getIndentedAndRemovedParent(Element elementInOtherContext) {
        Element parentInOtherContext = null;
        if (elementInOtherContext != null) {
            parentInOtherContext = elementInOtherContext.getParent();
            if (parentInOtherContext.getTagName().equalsIgnoreCase(LIST)) {
                int index = parentInOtherContext.getParent().getChildren().indexOf(parentInOtherContext);
                if (parentInOtherContext.getParent().getChildren().get(0).getTagName().equals(NUM) && index > 2) {
                    parentInOtherContext = parentInOtherContext.getParent().getChildren().get(index - 1);
                } else if (!parentInOtherContext.getParent().getChildren().get(0).getTagName().equals(NUM) && index > 1) {
                    parentInOtherContext = parentInOtherContext.getParent().getChildren().get(index - 1);
                } else {
                    parentInOtherContext = parentInOtherContext.getParent();
                }
            }
        }
        return parentInOtherContext;
    }
}
