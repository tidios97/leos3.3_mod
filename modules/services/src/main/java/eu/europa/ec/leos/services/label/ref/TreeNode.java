package eu.europa.ec.leos.services.label.ref;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static eu.europa.ec.leos.services.support.XmlHelper.INDENT;
import static eu.europa.ec.leos.services.support.XmlHelper.SUBPOINT;
import static eu.europa.ec.leos.services.support.XmlHelper.SUBPOINT_LABEL;

//TreeNode representation to be used for reference tree
public class TreeNode {
    private String type;        //tag name to used as type of tag
    private int depth;          //depth in xml tree
    private int siblingNumber;  //children order in xml
    private String identifier;  //xml Id of the node
    private String refId;       //xml Id of the node
    private String num;         //num associated with TreeNode in XML
    private TreeNode parent;    //parent in TreeNode
    private String documentRef;
    private String origin;
    private List<TreeNode> children = new ArrayList<>();

    //Children of this node in tree(only refs)
    public TreeNode(String type, int depth, int siblingNumber, String identifier, String num, TreeNode parent, String documentRef, String origin) {
        this.depth = depth;
        this.siblingNumber = siblingNumber;
        this.identifier = identifier;
        this.num = num;
        this.type = getDecoratedType(type, num);
        this.parent = parent;
        this.documentRef = documentRef;
        this.origin = origin;
    }

    public String getType() {
        return type;
    }

    public int getDepth() {
        return depth;
    }

    public int getSiblingNumber() {
        return siblingNumber;
    }

    public String getIdentifier() {
        return identifier;
    }

    public String getNum() {
        return num;
    }

    public String getRefId() {
        return refId;
    }

    public void setRefId(String refId) {
        this.refId = refId;
    }

    public TreeNode getParent() {
        return parent;
    }

    public List<TreeNode> getChildren() {
        return children;//Intentionally returning the ref
    }

    public void setParent(TreeNode parent) {
        this.parent = parent;
    }

    public void addChildren(TreeNode child) {
        this.children.add(child);
    }

    public String getDocumentRef() {
        return documentRef;
    }

    public String getOrigin() {
        return origin;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TreeNode)) return false;
        TreeNode node = (TreeNode) o;
        return Objects.equals(type, node.type) &&
                Objects.equals(identifier, node.identifier) &&
                Objects.equals(num, node.num) &&
                Objects.equals(documentRef, node.documentRef);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, identifier, num, documentRef);
    }

    private String getDecoratedType(String type, String num) {
        if("-".equals(num)) {
            type = INDENT;
        } else if(type.equals(SUBPOINT)) {
            type = SUBPOINT_LABEL;
        }
        return type;
    }
}
