package eu.europa.ec.leos.services.numbering.processor;

import eu.europa.ec.leos.services.numbering.config.NumberConfig;
import eu.europa.ec.leos.services.numbering.depthBased.ParentChildNode;
import org.w3c.dom.Node;

public interface NumberProcessorDepthBased {

    boolean canRenumber(Node node);

    /**
     * Number the Node using the NumberConfig passed as parameter
     * @param node           Node to be numbered
     * @param numberConfig   ConfigNumber to be used for numbering
     * @param elementName    Element name to number
     * @param depth          depth in the tree structure (parent-child relationship)
     */
    void renumberDepthBased(ParentChildNode node, NumberConfig numberConfig, String elementName, int depth);

}