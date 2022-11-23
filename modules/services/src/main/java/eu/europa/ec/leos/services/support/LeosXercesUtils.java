package eu.europa.ec.leos.services.support;

import static eu.europa.ec.leos.services.numbering.depthBased.ClassToDepthType.TYPE_1;
import static eu.europa.ec.leos.services.numbering.depthBased.ClassToDepthType.TYPE_2;
import static eu.europa.ec.leos.services.numbering.depthBased.ClassToDepthType.TYPE_3;
import static eu.europa.ec.leos.services.support.XercesUtils.createElement;
import static eu.europa.ec.leos.services.support.XercesUtils.createElementAsFirstChildOfNode;
import static eu.europa.ec.leos.services.support.XercesUtils.getAttributeValue;
import static eu.europa.ec.leos.services.support.XercesUtils.getAttributeValueAsSimpleBoolean;
import static eu.europa.ec.leos.services.support.XercesUtils.getFirstChild;
import static eu.europa.ec.leos.services.support.XercesUtils.getNumTag;
import static eu.europa.ec.leos.services.support.XmlHelper.BOLD;
import static eu.europa.ec.leos.services.support.XmlHelper.CLASS_ATTR;
import static eu.europa.ec.leos.services.support.XmlHelper.DIVISION;
import static eu.europa.ec.leos.services.support.XmlHelper.ITALICS;
import static eu.europa.ec.leos.services.support.XmlHelper.LEOS_AUTO_NUM_OVERWRITE;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;

import eu.europa.ec.leos.vo.toc.TableOfContentItemVO;

public class LeosXercesUtils {

    private static final Logger LOG = LoggerFactory.getLogger(LeosXercesUtils.class);

    public static Node buildNumElement(Node node, String numLabel) {
        Node numNode = getFirstChild(node, getNumTag(node.getNodeName()));
        if (numNode != null) {
            if (node.getNodeName().equals(DIVISION)) {
                buildNumElementForDivision(node, numLabel, numNode);
            } else {
                numNode.setTextContent(numLabel);
            }
        } else {
            numNode = createElementAsFirstChildOfNode(node, getNumTag(node.getNodeName()), numLabel);
        }
        return numNode;
    }

    private static void buildNumElementForDivision(Node node, String numLabel, Node numNode) {
        boolean isAutoNumOverwrite = getAttributeValueAsSimpleBoolean(node, LEOS_AUTO_NUM_OVERWRITE);
        if (isAutoNumOverwrite) {
            if (!numNode.getTextContent().equals(numLabel)) {
                numNode.setTextContent(numLabel);
            }
        } else {
            String classAttr = getAttributeValue(node, CLASS_ATTR);
            formatDivisionPartWithLeosRules(node, numNode, numLabel, classAttr);
        }
    }

    public static void formatHeadingNodeForDivision(Node node, TableOfContentItemVO tocVo, Node headingNode) {
        String heading = tocVo.getHeading();
        String style = tocVo.getStyle();
        if (style.equals(TYPE_1.name().toLowerCase())) {
            heading = heading.toUpperCase();
        } else {
            heading = heading.substring(0, 1).toUpperCase() + heading.substring(1).toLowerCase();
        }
        formatDivisionPartWithLeosRules(node, headingNode, heading, style);
    }

    private static void formatDivisionPartWithLeosRules(Node node, Node divisionNodePart, String textContent, String style) {
        divisionNodePart.setTextContent("");
        Node formattedNode;
        if (style.contains(TYPE_1.name().toLowerCase()) || style.contains(TYPE_2.name().toLowerCase())) {
            formattedNode = createElement(node.getOwnerDocument(), BOLD, textContent);
        } else if (style.contains(TYPE_3.name().toLowerCase())) {
            formattedNode = createElement(node.getOwnerDocument(), BOLD, "");
            Node iNode = createElement(node.getOwnerDocument(), ITALICS, textContent);
            formattedNode.appendChild(iNode);
        } else { // TYPE_4
            formattedNode = createElement(node.getOwnerDocument(), ITALICS, textContent);
        }
        divisionNodePart.appendChild(formattedNode);
    }

}