package eu.europa.ec.leos.services.numbering.processor;

import eu.europa.ec.leos.services.numbering.config.NumberConfig;
import org.w3c.dom.Node;

public interface NumberProcessor {

    boolean canRenumber(Node node);

    /**
     * Number the Node using the NumberConfig passed as parameter
     *
     * @param node           Node to be numbered
     * @param numberConfig   ConfigNumber to be used for numbering
     * @param numberChildren true, if numbering should be propagated to the children
     */
    void renumber(Node node, NumberConfig numberConfig, boolean numberChildren);

}