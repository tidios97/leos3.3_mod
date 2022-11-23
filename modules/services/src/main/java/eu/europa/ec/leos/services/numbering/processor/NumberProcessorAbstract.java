package eu.europa.ec.leos.services.numbering.processor;

import static eu.europa.ec.leos.services.numbering.NumberProcessorHandler.skipAutoRenumbering;
import static eu.europa.ec.leos.services.support.LeosXercesUtils.buildNumElement;
import static eu.europa.ec.leos.services.support.XercesUtils.getFirstChild;
import static eu.europa.ec.leos.services.support.XercesUtils.getId;
import static eu.europa.ec.leos.services.support.XercesUtils.isSoftChanged;
import static eu.europa.ec.leos.services.support.XmlHelper.NUM;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;

import eu.europa.ec.leos.i18n.MessageHelper;
import eu.europa.ec.leos.model.action.SoftActionType;
import eu.europa.ec.leos.services.numbering.NumberProcessorHandler;
import eu.europa.ec.leos.services.numbering.config.NumberConfig;
import eu.europa.ec.leos.services.support.XercesUtils;

public class NumberProcessorAbstract {

    private static final Logger LOG = LoggerFactory.getLogger(NumberProcessorDefault.class);

    final protected MessageHelper messageHelper;
    final protected NumberProcessorHandler numberProcessorHandler;

    public NumberProcessorAbstract(MessageHelper messageHelper, NumberProcessorHandler numberProcessorHandler) {
        this.messageHelper = messageHelper;
        this.numberProcessorHandler = numberProcessorHandler;
    }

    protected void renumber(Node node, NumberConfig numberConfig, String parentPrefix) {
        final String elementName = node.getNodeName();
        final String elementId = getId(node);
        if (isNumberedElement(node)) {
            if (numberConfig.isComplex()) {
                // COMPLEX numbering: CN runningInstance, mixed EC with CN elements
                complexNumbering(node, numberConfig, elementName, elementId, parentPrefix);
            } else {
                // SIMPLE numbering: EC runningInstance, EC or LS elements; CN runningInstance, all presents are CN elements
                simpleNumbering(node, numberConfig, elementName, elementId, parentPrefix, 0);
            }
        }
    }

    // Numbers like: 1, 2, 3, etc
    private void simpleNumbering(Node node, NumberConfig numberConfig, String elementName, String elementId, String parentPrefix, int depth) {
        String elementNum = numberConfig.getPrefix() + parentPrefix + numberConfig.getNextNumberToShow() + numberConfig.getSuffix();
        if (skipAutoRenumbering(node)) {
            String insertedNum = XercesUtils.getContentByTagName(node, NUM);
            LOG.trace("{} (depth {}) '{}', skipping calculated number '{}', keeping manual insertion '{}'", node.getNodeName(), depth, getId(node), elementNum, insertedNum);
        } else {
            elementNum = messageHelper.getMessage("numbering.label." + elementName, elementNum);
            buildNumElement(node, elementNum);
            LOG.trace("{} (depth {}) '{}' numbered to '{}'", elementName, depth, elementId, elementNum);
        }
    }

    // Numbers like: 1a, 1b, 1c, etc
    private void complexNumbering(Node node, NumberConfig numberConfig, String elementName, String elementId, String parentPrefix) {
        // COMPLEX numbering: CN runningInstance, mixed EC with CN elements
        /*
         * To better understand this if, it would be this in else:
         * ( !numberProcessorHandler.isElementSameOrigin(node) || (leosRenumbered != null && leosRenumbered.equals("true")) )
         * But then, to change it to if:
         * ( numberProcessorHandler.isElementSameOrigin(node) && (leosRenumbered == null || !leosRenumbered.equals("true")) )
         */
        String leosRenumbered = XercesUtils.getAttributeValue(node, "leos:renumbered");
        if (numberProcessorHandler.isElementSameOrigin(node) && (leosRenumbered == null || !leosRenumbered.equals("true"))) {
            // found an CN element
            // complex numbering, CN element in CN runningInstance
            numberConfig.incrementComplexValue();
            String actualNumberToShow = numberConfig.getActualNumberToShow();
            String elementNum = numberConfig.getPrefix() + parentPrefix + actualNumberToShow + numberConfig.getSuffix();
            elementNum = messageHelper.getMessage("numbering.label." + elementName, elementNum);
            buildNumElement(node, elementNum);
            LOG.trace("CN {} '{}', numbered to '{}'", elementName, elementId, elementNum);
        } else {
            // Found an EC element.
            // Skip numbering and declare "done" any eventual "complex" numbering going on.
            // Increment the next value, or read the actual EC number and parse it as current value for numbering algorithm.
            numberConfig.resetComplexValue();
            if (leosRenumbered != null && leosRenumbered.equals("true")) {
                String num = readActualNumberForRenumber(numberConfig, elementName, elementId);
                String elementNum = numberConfig.getPrefix() + parentPrefix + num + numberConfig.getSuffix();
                elementNum = messageHelper.getMessage("numbering.label." + elementName, elementNum);
                buildNumElement(node, elementNum);
            } else {
                readActualNumber(numberConfig, node, elementName, elementId);
            }
        }
    }

    protected void readActualNumber(NumberConfig numberConfig, Node node, String elementName, String elementId) {
        readActualNumberForRenumber(numberConfig, elementName, elementId);
    }

    protected String readActualNumberForRenumber(NumberConfig numberConfig, String elementName, String elementId) {
        String elementNum = numberConfig.getNextNumberToShow();
        LOG.trace("Skipping EC {} '{}', number '{}'", elementName, elementId, elementNum);
        return elementNum;
    }

    /**
     * Returns true if the node contains NUM element as first child.
     * This check cannot be done in the NumberProcessorHandler because we need to keep numbering the children of an Un-numbered element.
     * Example: Unnumbered paragraph contains Points which need to be numbered.
     */
    private static boolean isNumberedElement(Node node) {
        boolean isNumNotSoftDeleted = false;
        Node firstChild = getFirstChild(node, NUM);
        if (firstChild != null) {
            //check if Num is soft deleted
            isNumNotSoftDeleted = !isSoftChanged(node, SoftActionType.DELETE);
            if (!isNumNotSoftDeleted) {
                String actualNum = firstChild.getTextContent();
                LOG.trace("Skipping softdeleted {} '{}', number '{}'", firstChild.getNodeName(), getId(firstChild), actualNum);
            }
        } else {
            LOG.trace("Skipping unnumbered {} '{}'", node.getNodeName(), getId(node));
        }
        return isNumNotSoftDeleted;
    }

}
