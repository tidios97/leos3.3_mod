package eu.europa.ec.leos.services.label.ref;

import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import static eu.europa.ec.leos.services.support.XmlHelper.AKOMANTOSO;
import static eu.europa.ec.leos.services.support.XmlHelper.CITATIONS;
import static eu.europa.ec.leos.services.support.XmlHelper.RECITALS;

@Component
public class LabelExcludeNodes extends LabelHandler {
    
    private static final List<String> NODES_TO_CONSIDER = Arrays.asList(AKOMANTOSO, CITATIONS, RECITALS);
    
    @Override
    public boolean canProcess(List<TreeNode> refs) {
        boolean canProcess = refs.stream()
                .allMatch(ref -> NODES_TO_CONSIDER.contains(ref.getType()));
        return canProcess;
    }

    
    @Override
    public void process(List<TreeNode> refs, List<TreeNode> mrefCommonNodes, TreeNode sourceNode, StringBuffer label, Locale locale, boolean withAnchor,
                        boolean capital) {
    
    }
    
    @Override
    public int getOrder() {
        return 1;
    }
}
