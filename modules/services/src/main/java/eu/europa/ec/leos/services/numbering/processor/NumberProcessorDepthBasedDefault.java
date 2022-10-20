package eu.europa.ec.leos.services.numbering.processor;

import eu.europa.ec.leos.i18n.MessageHelper;
import eu.europa.ec.leos.services.numbering.NumberProcessorHandler;
import eu.europa.ec.leos.services.numbering.config.NumberConfig;
import eu.europa.ec.leos.services.numbering.depthBased.ParentChildNode;
import org.springframework.stereotype.Component;
import org.w3c.dom.Node;

import static eu.europa.ec.leos.services.support.XmlHelper.DIVISION;

@Component
public class NumberProcessorDepthBasedDefault extends NumberProcessorAbstract implements NumberProcessorDepthBased {

    public NumberProcessorDepthBasedDefault(MessageHelper messageHelper, NumberProcessorHandler numberProcessorHandler) {
        super(messageHelper, numberProcessorHandler);
    }

    @Override
    public boolean canRenumber(Node node) {
        return DIVISION.equals(node.getNodeName());
    }

    public void renumberDepthBased(ParentChildNode numberNode, NumberConfig numberConfig, String elementName, int depth) {
        final Node node = numberNode.getNode();
        final String parentPrefix = numberNode.getParentPrefix();
        renumber(node, numberConfig, parentPrefix);
        renumberChildren(numberNode, elementName, depth);
        renumberChildrenOfDifferentType(node, true); // Points
    }

    private void renumberChildren(ParentChildNode numberNode, String elementName, int depth) {
        if (numberNode.getChildren().size() > 0) {
            numberProcessorHandler.renumberDepthBased(numberNode.getChildren(), elementName, ++depth);
        }
    }

    protected void renumberChildrenOfDifferentType(Node node, boolean numberChildren) {

    }
}
