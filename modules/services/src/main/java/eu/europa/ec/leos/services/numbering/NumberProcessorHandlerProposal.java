package eu.europa.ec.leos.services.numbering;

import eu.europa.ec.leos.domain.common.InstanceType;
import eu.europa.ec.leos.instance.Instance;
import eu.europa.ec.leos.services.numbering.config.NumberConfig;
import eu.europa.ec.leos.services.numbering.depthBased.ParentChildNode;
import eu.europa.ec.leos.services.support.XercesUtils;
import org.springframework.stereotype.Component;
import org.w3c.dom.Node;

import java.util.List;

import static eu.europa.ec.leos.services.support.XmlHelper.EC;
import static eu.europa.ec.leos.services.support.XmlHelper.LEOS_ORIGIN_ATTR;

@Component
@Instance(instances = {InstanceType.COMMISSION, InstanceType.OS})
public class NumberProcessorHandlerProposal extends NumberProcessorHandler {

    @Override
    public boolean setComplexNumbering(List<Node> nodeList) {
        return false;
    }

    @Override
    protected boolean setComplexNumbering(List<ParentChildNode> nodeList, int depth) {
        return false;
    }

    @Override
    public void incrementValue(NumberConfig numberConfig) {

    }

    @Override
    public boolean isElementSameOrigin(Node node) {
        String origin = XercesUtils.getAttributeValue(node, LEOS_ORIGIN_ATTR);
        return origin == null || EC.equals(origin);
    }

    @Override
    protected void updateStartingNumber(List<Node> nodeList, NumberConfig numberConfig, String elementName) {

    }

}
