package eu.europa.ec.leos.services.numbering.depthBased;

import org.w3c.dom.Node;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static eu.europa.ec.leos.services.support.XmlHelper.CLASS_ATTR;
import static eu.europa.ec.leos.services.support.XmlHelper.DIVISION;
import static eu.europa.ec.leos.services.support.XmlHelper.LEOS_DEPTH_ATTR;
import static eu.europa.ec.leos.services.support.XmlHelper.NUM;
import static eu.europa.ec.leos.services.support.XercesUtils.getAttributeValue;
import static eu.europa.ec.leos.services.support.XercesUtils.getAttributeValueAsInteger;
import static eu.europa.ec.leos.services.support.XercesUtils.getChildContent;
import static eu.europa.ec.leos.services.support.XercesUtils.getId;
import static eu.europa.ec.leos.util.LeosDomainUtil.calculateLeftPadd;

public class ParentChildNode {

    private Integer depth; // reporting the depth outside, to avoid penalties using getAttributeValue(node)
    private Node node;  // node ot be changed for numbering
    private List<ParentChildNode> children = new ArrayList<>();
    private ParentChildNode parent;
    private boolean showParentSuffix;

    public ParentChildNode(Integer depth, Node node, boolean showParentSuffix) {
        this.depth = depth;
        this.node = node;
        this.showParentSuffix = showParentSuffix;
    }

    public Integer getDepth() {
        return depth;
    }

    public void setDepth(Integer depth) {
        this.depth = depth;
    }

    public Node getNode() {
        return node;
    }

    public void setNode(Node node) {
        this.node = node;
    }

    public List<ParentChildNode> getChildren() {
        return children;
    }

    public void setChildren(List<ParentChildNode> children) {
        this.children = children;
    }

    public ParentChildNode getParent() {
        return parent;
    }

    public void setParent(ParentChildNode parent) {
        this.parent = parent;
    }

    public void addChild(ParentChildNode child) {
        children.add(child);
        child.setParent(this);
    }

    public void addSibling(ParentChildNode node) {
        parent.addChild(node);
    }

    public Stream<ParentChildNode> flattened() {
        return Stream.concat(
                Stream.of(this),
                children.stream().flatMap(ParentChildNode::flattened)
        );
    }

    public String toString() {
        String depthAttr = LEOS_DEPTH_ATTR;
        if (node.getNodeName().equals(DIVISION)) {
            depthAttr = CLASS_ATTR;
        }
        return getBasedOnDepthNodeAsString(this, 0, depthAttr);
    }

    private String getBasedOnDepthNodeAsString(ParentChildNode node, int depth, String depthAttr) {
        final String RIGHT_CHAR = "\n";
        String str = calculateLeftPadd(depth, "\t");
        Integer depthNode = null;
        if (node.getNode().getNodeName().equals(DIVISION)) {
            String typeAttr = getAttributeValue(node.getNode(), depthAttr);
            ClassToDepthType classToDepthType = ClassToDepthType.of(typeAttr);
            if (classToDepthType != null) {
                depthNode = classToDepthType.getDepth();
            }
        } else {
            depthNode = getAttributeValueAsInteger(node.getNode(), depthAttr);
        }

        if (depthNode == null) {
            depthNode = 1;
        }

        String nr = getChildContent(node.getNode(), NUM);
//        if (depthNode == null || (depthNode != depth + 1)) {
//            throw new IllegalStateException("Wrong algorithm implementation");
//        }

        // TODO correct the following code to further verify when the structure is wrong. (After test phase)
//        if(!nr.equals("#") && nr.split("\\.").length != depthNode){
//            System.out.println("nr: "+ nr + ", nr.split().length" + nr.split("\\.").length + ", depth: " + depth + ", for id: " + getId(node.getNode()));
//            throw new IllegalStateException("Wrong algorithm implementation. ");
//        }

        str = str + "\"" + nr + "\", id: " + getId(node.getNode()) + ", depth: " + depth + ", depthAttr: " + depthNode + RIGHT_CHAR;

        String children;
        for (int c = 0; c < node.children.size(); c++) {
            children = getBasedOnDepthNodeAsString(node.children.get(c), ++depth, depthAttr);
            --depth;
            str = str + children;
        }
        return str.substring(0, str.length() - 1);
    }

    public static String getBasedOnDepthNodeListAsString(List<ParentChildNode> list) {
        String s = "";
        for (int i = 0; i < list.size(); i++) {
            ParentChildNode numberNode2 = list.get(i);
            s = s + numberNode2.toString() + "\n";
        }
        return s;
    }

    public String getParentPrefix() {
        String parentPrefix = "";
        if (showParentSuffix) {
            ParentChildNode p = parent;
            if (p != null) {
                parentPrefix += getChildContent(p.getNode(), NUM);
            }
        }
        return parentPrefix;
    }
}
