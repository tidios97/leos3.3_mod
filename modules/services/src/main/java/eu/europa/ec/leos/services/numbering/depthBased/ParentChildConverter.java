package eu.europa.ec.leos.services.numbering.depthBased;

import eu.europa.ec.leos.services.support.XercesUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static eu.europa.ec.leos.services.support.XmlHelper.CLASS_ATTR;
import static eu.europa.ec.leos.services.support.XmlHelper.DIVISION;
import static eu.europa.ec.leos.services.support.XmlHelper.LEOS_DEPTH_ATTR;
import static eu.europa.ec.leos.services.support.XercesUtils.getAttributeValue;
import static eu.europa.ec.leos.services.support.XercesUtils.getAttributeValueAsInteger;
import static eu.europa.ec.leos.services.support.XercesUtils.getId;
import static eu.europa.ec.leos.services.support.XercesUtils.getNodesAsList;

@Component
public class ParentChildConverter {

    private static final Logger LOG = LoggerFactory.getLogger(ParentChildConverter.class);

    public List<ParentChildNode> getParentChildStructure(NodeList nodeList, boolean showParentSuffix) {
        List<Node> nodes = getNodesAsList(nodeList);
        List<ParentChildNode> basedOnDepthList = new ArrayList<>();
        for (int i = 0; i < nodes.size(); i++) {
            Node node = nodes.get(i);
            Integer depth = getNodeDepth(node);
            if(depth == null) {
                LOG.warn("Node {} does not contain attribute 'depth'", getId(node));
            }
            basedOnDepthList.add(new ParentChildNode(depth, nodes.get(i), showParentSuffix));
        }

        basedOnDepthList = groupByDepth(basedOnDepthList);
        return basedOnDepthList;
    }

    public static Integer getNodeDepth(Node node) {
        Integer depth = 1;
        if (DIVISION.equals(node.getNodeName())) {
            String classAttr = getAttributeValue(node, CLASS_ATTR);
            ClassToDepthType depthType = ClassToDepthType.of(classAttr);
            if (depthType != null) {
                depth = depthType.getDepth();
            }
        } else {
            depth = getAttributeValueAsInteger(node, LEOS_DEPTH_ATTR);
            if (depth == null) {
                depth = 1;
            }
        }
        return depth;
    }

    private static List<ParentChildNode> groupByDepth(List<ParentChildNode> nodeList) {
        List<ParentChildNode> basedOnDepthList = new ArrayList<>();
        ParentChildNode lastNode = null;
        int elementsToProcess = nodeList.size();
        for (int i = 0; i < elementsToProcess; i++) {
            if (i == 0) {
                lastNode = nodeList.get(i);
            }
            lastNode = findChildrenForNode(basedOnDepthList, nodeList, lastNode);
        }
        return basedOnDepthList;
    }

    private static ParentChildNode findChildrenForNode(List<ParentChildNode> basedOnDepthList, List<ParentChildNode> nodeList, ParentChildNode lastNode) {
        ParentChildNode node = nodeList.remove(0);
        LOG.trace("-> NODE: [ {} ] --- LASTNODE: [ {} ]", node, lastNode);
        int lastDepth = (lastNode.getDepth() != null) ? lastNode.getDepth() : 1;
        int depth = (node.getDepth() != null) ? node.getDepth() : 1;
        if (depth - lastDepth == 1) {
            LOG.trace("Added [ {} ] as child of [ {} ]", node, lastNode);
            lastNode.addChild(node);  //add as child. Parent will be updated to lastNode.
        } else if (depth - lastDepth == 0) {
            addSiblingOrInRoot(basedOnDepthList, lastNode, node); // add as sibling
        } else {
            if(lastDepth < depth) { // decreasing
                XercesUtils.addAttribute(node.getNode(), LEOS_DEPTH_ATTR, String.valueOf(lastDepth + 1));
                node.setDepth(lastDepth + 1);
                LOG.trace("Added [ {} ] as child of [ {} ]", node, lastNode);
                lastNode.addChild(node);
            } else {
                addChildAtDepth(basedOnDepthList, node);
            }
            LOG.trace("Added [ {} ] as last element of the list", node, lastNode);
        }
        return node;
    }

    private static void addChildAtDepth(List<ParentChildNode> nodeList, ParentChildNode node) {
        int depthNode = getNodeDepth(node.getNode());
        List<ParentChildNode> flatNodeList = nodeList.stream()
                .flatMap(ParentChildNode::flattened)
                .filter(n -> getNodeDepth(n.getNode()) == depthNode)
                .collect(Collectors.toList());
        if (flatNodeList.size() == 0) {
            throw new IllegalStateException("No element found with depth: " + depthNode);
        }
        ParentChildNode lastOfSameDepth = flatNodeList.get(flatNodeList.size() - 1);

        addSiblingOrInRoot(nodeList, lastOfSameDepth, node);
    }

    private static void addSiblingOrInRoot(List<ParentChildNode> rv, ParentChildNode lastNode, ParentChildNode node) {
        if (lastNode.getParent() != null) {
            LOG.trace("Added [ {} ] as sibling of [ {} ]", node, lastNode);
            lastNode.addSibling(node);
        } else {
            LOG.trace("Added [ {} ] as last element of the list", node, lastNode);
            rv.add(node); //if last processed does not have a parent, is a root node, first level.
        }
    }
}
