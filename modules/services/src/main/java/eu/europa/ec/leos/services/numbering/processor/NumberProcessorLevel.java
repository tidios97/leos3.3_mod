package eu.europa.ec.leos.services.numbering.processor;

import eu.europa.ec.leos.i18n.MessageHelper;
import eu.europa.ec.leos.services.numbering.NumberProcessorHandler;
import eu.europa.ec.leos.services.numbering.config.NumberConfig;
import eu.europa.ec.leos.services.support.XercesUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.w3c.dom.Node;

import java.util.List;

import static eu.europa.ec.leos.services.support.XercesUtils.getNodeNum;
import static eu.europa.ec.leos.services.support.XmlHelper.INDENT;
import static eu.europa.ec.leos.services.support.XmlHelper.LEVEL;
import static eu.europa.ec.leos.services.support.XmlHelper.LIST;
import static eu.europa.ec.leos.services.support.XmlHelper.POINT;

@Component
public class NumberProcessorLevel extends NumberProcessorDepthBasedDefault {

    private static final Logger LOG = LoggerFactory.getLogger(NumberProcessorLevel.class);

    @Autowired
    public NumberProcessorLevel(MessageHelper messageHelper, NumberProcessorHandler numberProcessorHandler) {
        super(messageHelper, numberProcessorHandler);
    }

    @Override
    public boolean canRenumber(Node node) {
        return LEVEL.equals(node.getNodeName());
    }

    @Override
    protected void renumberChildrenOfDifferentType(Node node, boolean numberChildren) {
        List<Node> listNodes = XercesUtils.getChildren(node, LIST);
        for (int i = 0; i < listNodes.size(); i++) {
            Node listNode = listNodes.get(i);
            String elementType = XercesUtils.checkFirstChildType(listNode, INDENT) ? INDENT : POINT;
            numberProcessorHandler.renumberElement(node, elementType, numberChildren);
        }
    }

    protected void readActualNumber(NumberConfig numberConfig, Node node, String elementName, String elementId) {
        String labelNumber = getNodeNum(node);
        String numAsString = numberProcessorHandler.getNumberFromLabel(numberConfig, elementName, labelNumber);
        numberConfig.parseValue(numAsString);
        LOG.trace("Skipping EC {} '{}', parsed actual value '{}'", elementName, elementId, numAsString);
    }

}
