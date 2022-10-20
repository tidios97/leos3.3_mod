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

import java.util.Arrays;

import static eu.europa.ec.leos.services.support.XercesUtils.getNodeNum;
import static eu.europa.ec.leos.services.support.XmlHelper.INDENT;
import static eu.europa.ec.leos.services.support.XmlHelper.LIST;
import static eu.europa.ec.leos.services.support.XmlHelper.PARAGRAPH;
import static eu.europa.ec.leos.services.support.XmlHelper.POINT;
import static eu.europa.ec.leos.services.support.XercesUtils.getFirstChild;

@Component
public class NumberProcessorParagraphAndPoint extends NumberProcessorDefault {

    private static final Logger LOG = LoggerFactory.getLogger(NumberProcessorParagraphAndPoint.class);

    @Autowired
    public NumberProcessorParagraphAndPoint(MessageHelper messageHelper, NumberProcessorHandler numberProcessorHandler) {
        super(messageHelper, numberProcessorHandler);
    }

    @Override
    public boolean canRenumber(Node node) {
        return Arrays.asList(PARAGRAPH, POINT, INDENT).contains(node.getNodeName());
    }

    protected void renumberChildren(Node node, boolean numberChildren) {
        Node listNode = getFirstChild(node, LIST);
        if (listNode != null && listNode.getFirstChild() != null) {
            String elementType = XercesUtils.getFirstChildType(listNode, Arrays.asList(INDENT, POINT));
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

