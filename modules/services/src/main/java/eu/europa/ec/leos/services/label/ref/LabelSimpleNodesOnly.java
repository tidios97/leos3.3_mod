package eu.europa.ec.leos.services.label.ref;

import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import static eu.europa.ec.leos.services.support.XmlHelper.CLAUSE;

@Component
public class LabelSimpleNodesOnly extends LabelHandler {
    
    private static final List<String> NODES_TO_CONSIDER = Arrays.asList(CLAUSE);
    
    @Override
    public boolean canProcess(List<TreeNode> refs) {
        boolean canProcess = refs.stream()
                .allMatch(ref -> NODES_TO_CONSIDER.contains(ref.getType()));
        return canProcess;
    }
    
    
    /**
     * This label processor shows only the type name as label, without calculating any numbering.
     */
    @Override
    public void process(List<TreeNode> refs, List<TreeNode> mrefCommonNodes, TreeNode sourceNode, StringBuffer label, Locale locale, boolean withAnchor,
                        boolean capital) {
        refs.stream().forEach(ref -> {
            label.append(ref.getType())
                    .append(", ");
        });

        if(label.substring(label.length()-2, label.length()).equals(", ")){
            label.delete(label.length()-2, label.length());
        }
    }
    
    @Override
    public int getOrder() {
        return 1;
    }
}
