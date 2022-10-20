package eu.europa.ec.leos.services.numbering;

import static eu.europa.ec.leos.services.numbering.depthBased.ParentChildConverter.getNodeDepth;
import static eu.europa.ec.leos.services.support.XercesUtils.getNodeNum;
import static eu.europa.ec.leos.services.support.XmlHelper.CN;
import static eu.europa.ec.leos.services.support.XmlHelper.LEOS_ORIGIN_ATTR;
import static eu.europa.ec.leos.services.support.XmlHelper.NUM;

import java.util.List;

import org.springframework.stereotype.Component;
import org.w3c.dom.Node;

import eu.europa.ec.leos.domain.common.InstanceType;
import eu.europa.ec.leos.instance.Instance;
import eu.europa.ec.leos.services.numbering.config.NumberConfig;
import eu.europa.ec.leos.services.numbering.depthBased.ParentChildNode;
import eu.europa.ec.leos.services.support.XercesUtils;

@Component
@Instance(InstanceType.COUNCIL)
public class NumberProcessorHandlerMandate extends NumberProcessorHandler {

    @Override
    protected boolean setComplexNumbering(List<ParentChildNode> nodeList, int depth) {
        boolean isDifferentOrigin = nodeList
                .stream()
                .filter(n -> getNodeDepth(n.getNode()) == depth)
                .anyMatch(n ->  !isElementSameOrigin(n.getNode()));
        return isDifferentOrigin;
    }

    @Override
    public boolean setComplexNumbering(List<Node> nodeList) {
        boolean isComplex = nodeList
                .stream()
                .anyMatch(node -> !isElementSameOrigin(node));
        return isComplex;
    }

    @Override
    protected void incrementValue(NumberConfig numberConfig) {
        numberConfig.getNextNumberToShow();
        numberConfig.resetComplexValue();
    }

    @Override
    public boolean isElementSameOrigin(Node node) {
        String origin = XercesUtils.getAttributeValue(node, LEOS_ORIGIN_ATTR);
        boolean isNodeCNOrigin = origin == null || CN.equals(origin);

        boolean isNumCNOrigin = true;
        if (!isNodeCNOrigin) {
            //check if NUM is of CN origin; in case node is EC
            Node numNode = XercesUtils.getFirstChild(node, NUM);
            if (numNode != null) {
                origin = XercesUtils.getAttributeValue(numNode, LEOS_ORIGIN_ATTR);
                isNumCNOrigin = CN.equals(origin);
            }
        }

        return isNodeCNOrigin || isNumCNOrigin;
    }

    @Override
    public void updateStartingNumber(List<Node> nodeList, NumberConfig numberConfig, String elementName) {
        if (numberConfig.isComplex()) {
            Node node = nodeList
                    .stream()
                    .filter(n -> !isElementSameOrigin(n))
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("Incongruent situation. If is a COMPLEX numbering, 1 Node should be of the same instance type"));
            String labelNumber = getNodeNum(node);
            String numAsString = getNumberFromLabel(numberConfig, elementName, labelNumber);
            numberConfig.parseInitialValue(numAsString);
        }
    }

}
