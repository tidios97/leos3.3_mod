package eu.europa.ec.leos.services.label.ref;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;

import static eu.europa.ec.leos.services.support.XmlHelper.ARTICLE;

abstract public class LabelHandler {
    protected final String THIS_REF = "this";
    
    abstract public boolean canProcess(List<TreeNode> refs);
    
    abstract public void process(List<TreeNode> refs, List<TreeNode> mrefCommonNodes, TreeNode sourceNode, StringBuffer label, Locale locale, boolean withAnchor,
                                 boolean capital);

    abstract public int getOrder();

    protected String createAnchor(TreeNode ref, Locale locale, boolean withAnchor) {
        final String rv;
        if(withAnchor) {
            StringBuilder builder = new StringBuilder("<ref");
            builder.append(" href=\"").append(ref.getDocumentRef()).append("/").append(ref.getIdentifier()).append("\"");
            if (ref.getOrigin() != null) {
                builder.append(" leos:origin=\"").append(ref.getOrigin()).append("\"");
            }
            builder.append(" xml:id=\"").append(ref.getRefId()).append("\">");
            builder.append(NumFormatter.formattedNum(ref, locale));
            builder.append("</ref>");
            rv = builder.toString();
        } else {
            rv = NumFormatter.formattedNum(ref, locale);
        }
        return rv;
    }
    protected final boolean contains(List<TreeNode> node, Function<TreeNode, Object> valueGetter, Object value) {
        for (TreeNode treeNode : node) {
            if (value.equals(valueGetter.apply(treeNode))) {
                return true;
            }
        }
        return false;
    }
    protected final List<TreeNode> seperateNodesOfMaxDepth(List<TreeNode> pendingNodes) {
        List<TreeNode> nodes = new ArrayList<>();
        if (pendingNodes.size() > 0) {
            TreeNode maxDepthNode = pendingNodes.get(0);
            for (TreeNode node : pendingNodes) {
                if (node.getDepth() > maxDepthNode.getDepth()) {
                    maxDepthNode = node;
                }
            }
            for (TreeNode node : pendingNodes) {
                if (node.getDepth() == maxDepthNode.getDepth()) {
                    nodes.add(node);
                }
            }
        }
        return nodes;
    }
    
    protected boolean inThisArticle(List<TreeNode> mrefCommonNodes) {
        return mrefCommonNodes.size() > 0 && ARTICLE.equals(mrefCommonNodes.get(0).getType());
    }

    public void addPreffix(StringBuffer label, String docType, List<TreeNode> refs) {
    }

}
