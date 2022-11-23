package eu.europa.ec.leos.services.numbering;

import eu.europa.ec.leos.i18n.MessageHelper;
import eu.europa.ec.leos.model.action.SoftActionType;
import eu.europa.ec.leos.services.clone.CloneContext;
import eu.europa.ec.leos.services.numbering.config.NumberConfig;
import eu.europa.ec.leos.services.numbering.config.NumberConfigFactory;
import eu.europa.ec.leos.services.numbering.depthBased.ParentChildNode;
import eu.europa.ec.leos.services.numbering.processor.NumberProcessor;
import eu.europa.ec.leos.services.numbering.processor.NumberProcessorDepthBased;
import eu.europa.ec.leos.services.support.XmlHelper;
import eu.europa.ec.leos.services.support.XercesUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.Arrays;
import java.util.List;

import static eu.europa.ec.leos.services.support.XmlHelper.INDENT;
import static eu.europa.ec.leos.services.support.XmlHelper.LEOS_AUTO_NUM_OVERWRITE;
import static eu.europa.ec.leos.services.support.XmlHelper.LEOS_SOFT_ACTION_ATTR;
import static eu.europa.ec.leos.services.support.XmlHelper.LIST;
import static eu.europa.ec.leos.services.support.XmlHelper.PARAGRAPH;
import static eu.europa.ec.leos.services.support.XmlHelper.POINT;
import static eu.europa.ec.leos.services.support.XmlHelper.ARTICLE;
import static eu.europa.ec.leos.services.support.XercesUtils.getAttributeForSoftAction;
import static eu.europa.ec.leos.services.support.XercesUtils.getId;
import static eu.europa.ec.leos.services.support.XercesUtils.getNodeNum;
import static eu.europa.ec.leos.services.support.XercesUtils.removeAttribute;
import static java.util.Arrays.asList;

/**
 * NumberProcessorHandler handles a list of Processors following the Composite Pattern fashion.
 *
 * Composite:
 *      NumberProcessorHandler
 * Leafs:
 *      NumberProcessorArticle
 *      NumberProcessorPoint
 *      NumberProcessorParagraph
 *      NumberProcessorDefault
 *      NumberProcessorDivision
 *
 * Each NumberProcessor describes the logic to be applied for numbering the element of its ownership.
 *
 * Depending of the need, NumberingConfigProcessor implementations (Arabic, Alpha, etc) are used to perform the
 * incremental operations (1, 2, 3 or a, b, c) as configured in the structure_xx.xml file.
 */
public abstract class NumberProcessorHandler {

    private static final Logger LOG = LoggerFactory.getLogger(NumberProcessorHandler.class);

    @Autowired
    @Lazy
    protected List<NumberProcessor> numberProcessors;
    @Autowired
    @Lazy
    protected List<NumberProcessorDepthBased> numberProcessorsDepthBased;
    @Autowired
    protected NumberConfigFactory numberConfigFactory;
    @Autowired
    protected CloneContext cloneContext;
    @Autowired
    protected MessageHelper messageHelper;

    public static List<SoftActionType> softActionTypesToSkip = asList(SoftActionType.DELETE, SoftActionType.MOVE_TO);

    /**
     * Numbers all elements of type "elementName" present in the Document associated to the node passed as parameter.
     *
     * This method is different from the method renumberElement.
     * - renumberElement  -> Number children of the first level to node of type  "elementName"
     * - renumberDocument -> Number all elements present in the document of type  "elementName"  (no matter what the level inside the dom is)
     */
    public void renumberDocument(Document document, String elementName, boolean renumberChildren) {
        NodeList elements = document.getElementsByTagName(elementName);
        List<Node> nodeList = XercesUtils.getNodesAsList(elements);
        LOG.trace("renumberElementsAndChildren - Found {} '{}'s to number inside nodeName '{}', nodeId '{}'", nodeList.size(), elementName, document.getNodeName(), getId(document));
        renumber(nodeList, renumberChildren);
    }

    /**
     * Numbers all elements of type "elementName" present in the Node.
     *
     * W3C Node already express the tree structure of a single Node and its children.
     * Example how it looks like conceptually a Document containing 2 Article Nodes:
     * {
     *      name: Article 1
     *      children: [
     *          {
     *              name: Paragraph
     *              children: [
     *                  {
     *                      name: Point 1,
     *                      children: null
     *                  },
     *                  {
     *                      name: Point 1,
     *                      children: null
     *                  }
     *          }
     *      ]
     *  },
     *  {
     *      name: Article 2
     *      children: []
     *  }
     *
     * @param node             Document or initial Node where the numbering will start
     * @param elementName      elements name to number inside the node
     * @param renumberChildren true, if numbering should be propagated to the children
     */
    public void renumberElement(Node node, String elementName, boolean renumberChildren) {
        if (Arrays.asList(POINT, INDENT).contains(elementName)) {
            List<Node> LISTs = XercesUtils.getChildren(node, LIST);
            LOG.trace("getChildren. Found {} LISTs inside nodeName {}, nodeId {}", LISTs.size(), node.getNodeName(), getId(node));
            for (int i = 0; i < LISTs.size(); i++) {
                Node list = LISTs.get(i);
                List<Node> nodeList = XercesUtils.getChildren(list, elementName);
                renumber(nodeList, renumberChildren);
            }
        } else {
            List<Node> nodeList = XercesUtils.getChildren(node, elementName);
            renumber(nodeList, renumberChildren);
        }
    }

    /**
     * Numbers all elements of type "elementName" present in the List<ParentChildNode>.
     *
     * W3C Node already gives the way to express the tree structure (parent-child), but sometime we use xml elements which do not have a
     * parent-child relationship.
     * Example how it looks like an Division w3c Node List:
     *  {
     *      name: Division I
     *      depthAtt: 1
     *      children: []
     *  },
     *  {
     *      name: Division A
     *      depthAtt: 2
     *      children: []
     *  },
     *  {
     *      name: Division I)
     *      depthAtt: 3
     *      children: []
     *  }
     *  {
     *      name: Division II
     *      depthAtt: 1
     *      children: []
     *  }
     *
     * With the help of List<ParentChildNode> we can have the following structure:
     * {
     *      name: Division I
     *      depth: 1
     *      children: [
     *          {
     *              name: Division A
     *              depth: 2
     *              children: [
     *                  {
     *                      name: Division I)
     *                      depth: 3
     *                  }
     *          }
     *      ]
     *  } ,
     *  {
     *      name: Division II
     *      depth: 1
     *      children: []
     *  }
     * @param nodeList         List with all Nodes to be numbered
     * @param elementName      elements name to number inside the list
     * @param depth            depth in the tree structure (parent-child relationship)
     */
    public void renumberDepthBased(List<ParentChildNode> nodeList, String elementName, int depth) {
        if (nodeList.size() > 0) {
            final Node firstElement = nodeList.get(0).getNode();
            final NumberConfig numberConfig = numberConfigFactory.getNumberConfig(elementName, depth, firstElement);
            numberConfig.setComplex(setComplexNumbering(nodeList, depth));
            for (int i = 0; i < nodeList.size(); i++) {
                final ParentChildNode parentChildNode = nodeList.get(i);
                final Node node = parentChildNode.getNode();
                numberProcessorsDepthBased.stream()
                        .filter(numberProcessor -> numberProcessor.canRenumber(node))
                        .findFirst()
                        .get()
                        .renumberDepthBased(parentChildNode, numberConfig, elementName, depth);
                removeAttribute(node, XmlHelper.LEOS_AFFECTED_ATTR);//TODO temp, until migration finishes
            }
        }
    }

    private void renumber(List<Node> nodeList, boolean renumberChildren) {
        if (nodeList.size() > 0) {
            final Node firstElement = nodeList.get(0);
            final int elementDepth = XercesUtils.getPointDepth(firstElement);
            final String elementName = firstElement.getNodeName();
            final NumberConfig numberConfig = numberConfigFactory.getNumberConfig(elementName, elementDepth, firstElement);
            numberConfig.setComplex(setComplexNumbering(nodeList));
            String leosRenumbered = XercesUtils.getAttributeValue(firstElement, "leos:renumbered");
            if (leosRenumbered == null || !leosRenumbered.equals("true")) {
                updateStartingNumber(nodeList, numberConfig, elementName);
            }

            for (int i = 0; i < nodeList.size(); i++) {
                final Node node = nodeList.get(i);
                if (skipAutoRenumbering(node)) {
                    String leosRenumberedForNode = XercesUtils.getAttributeValue(node, "leos:renumbered");
                    if (leosRenumberedForNode == null || !leosRenumberedForNode.equals("true")) {
                        incrementValue(numberConfig);
                    }
                    LOG.trace("Skipping SoftChanged {} '{}', number '{}'", elementName, getId(node), getNodeNum(node));
                } else {
                    numberProcessors.stream()
                            .filter(numberProcessor -> numberProcessor.canRenumber(node))
                            .findFirst()
                            .get()
                            .renumber(node, numberConfig, renumberChildren);
                }
                removeAttribute(node, XmlHelper.LEOS_AFFECTED_ATTR);//TODO temp, until migration finishes
            }
        }
    }

    /**
     * SIMPLE numbering:  (1, 2, 3, or a, b, c, etc)
     *      - EC running instance; All presents are EC elements
     *      - EC running instance; Cloned Proposal, no matter what element origin is (EC or LS)
     *      - CN running instance, All presents are CN elements
     * COMPLEX numbering:  (-1, -1a, 1, 2, 2a, etc)
     *      - CN running instance; At least one EC element present between CN elements
     *
     * @param nodeList List of Nodes from which to determine if simple or complex algorithm has to be applied
     * @return true if complex algorithm should be executed
     */
    protected abstract boolean setComplexNumbering(List<Node> nodeList);

    public abstract void incrementValue(NumberConfig numberConfig);
    protected abstract boolean setComplexNumbering(List<ParentChildNode> nodeList, int depth);

    public abstract boolean isElementSameOrigin(Node node);

    protected abstract void updateStartingNumber(List<Node> nodeList, NumberConfig numberConfig, String elementName);

    public String getNumberFromLabel(NumberConfig numberConfig, String elementName, String labelNumber) {
        String prefix;
        String suffix;
        if (ARTICLE.equals(elementName)) {
            prefix = messageHelper.getMessage("toc.item.type." + elementName) + " ";
            suffix = "";
        } else {
            prefix = numberConfig.getPrefix();
            suffix = numberConfig.getSuffix();
        }

        String numAsString = labelNumber;
        if (numAsString.startsWith(prefix)) {
            numAsString = numAsString.substring(prefix.length());
        }
        if (!suffix.isEmpty()) {
            // In the case of 1. or 1.4. to be 1 and 1.4 (remove the suffix if finishes with it)
            if (numAsString.endsWith(suffix)) {
                numAsString = numAsString.substring(0, numAsString.lastIndexOf(suffix));
            }
            // In the case of 1, it is already fine. But in case of 1.4, we need get only 4
            if (numAsString.contains(suffix)) {
                numAsString = numAsString.substring(numAsString.lastIndexOf(suffix) + 1);
            }
        }
        return numAsString;
    }

    public static boolean skipAutoRenumbering(Node node) {
        SoftActionType actionType = getAttributeForSoftAction(node, LEOS_SOFT_ACTION_ATTR);
        boolean containsSoftAttribute = softActionTypesToSkip.contains(actionType);

        Boolean isOverWritten = XercesUtils.getAttributeValueAsBoolean(node, LEOS_AUTO_NUM_OVERWRITE);
        isOverWritten = isOverWritten != null && isOverWritten;

        return containsSoftAttribute || isOverWritten;
    }

}
