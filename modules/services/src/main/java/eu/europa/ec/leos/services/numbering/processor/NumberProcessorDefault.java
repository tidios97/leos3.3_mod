package eu.europa.ec.leos.services.numbering.processor;

import eu.europa.ec.leos.i18n.MessageHelper;
import eu.europa.ec.leos.services.numbering.NumberProcessorHandler;
import eu.europa.ec.leos.services.numbering.config.NumberConfig;
import org.springframework.stereotype.Component;
import org.w3c.dom.Node;

import java.util.Arrays;

import static eu.europa.ec.leos.services.support.XmlHelper.RECITAL;

@Component
public class NumberProcessorDefault extends NumberProcessorAbstract implements NumberProcessor {

    public NumberProcessorDefault(MessageHelper messageHelper, NumberProcessorHandler numberProcessorHandler) {
        super(messageHelper, numberProcessorHandler);
    }

    /**
     * Default NumberProcessor with no propagation to the children.
     * List each new element with the same behaviour like this:
     * -    Arrays.asList(RECITAL, NEW_ELEMENT1, NEW_ELEMENT2).contains(node.getNodeName())
     */
    @Override
    public boolean canRenumber(Node node) {
        return Arrays.asList(RECITAL).contains(node.getNodeName());
    }

    @Override
    public void renumber(Node node, NumberConfig numberConfig, boolean numberChildren) {
        renumber(node, numberConfig, "");
        renumberChildren(node, numberChildren);
    }

    protected void renumberChildren(Node node, boolean numberChildren) {
        // no propagation for the default implementation
    }

}
