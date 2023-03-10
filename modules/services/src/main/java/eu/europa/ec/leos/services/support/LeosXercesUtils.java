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
import static eu.europa.ec.leos.services.support.XmlHelper.DIV;
import static eu.europa.ec.leos.services.support.XmlHelper.CLASS_ATTR;
import static eu.europa.ec.leos.services.support.XmlHelper.DIVISION;
import static eu.europa.ec.leos.services.support.XmlHelper.ORIENTATION_LANDSCAPE;
import static eu.europa.ec.leos.services.support.XmlHelper.ORIENTATION_PORTRAIT;
import static eu.europa.ec.leos.services.support.XmlHelper.ITALICS;
import static eu.europa.ec.leos.services.support.XmlHelper.LEOS_AUTO_NUM_OVERWRITE;
import static eu.europa.ec.leos.services.support.XmlHelper.MAIN_BODY;
import static eu.europa.ec.leos.services.support.XmlHelper.LEVEL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import eu.europa.ec.leos.vo.toc.TableOfContentItemVO;

import java.util.List;

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
    	Node formattedNode;
    	if (style.contains(TYPE_1.name().toLowerCase()) || style.contains(TYPE_2.name().toLowerCase())) {
    		formattedNode = getBoldNode(node, divisionNodePart, textContent);
    	} else if (style.contains(TYPE_3.name().toLowerCase())) {
    		formattedNode = getBoldNode(node, divisionNodePart, "");
    		Node iNode = getItalicNode(node, divisionNodePart, textContent);
    		formattedNode.appendChild(iNode);
    	} else { // TYPE_4
    		formattedNode = getItalicNode(node, divisionNodePart, textContent);
    	}
    	divisionNodePart.setTextContent("");
    	divisionNodePart.appendChild(formattedNode);
    }
    
    private static Node getBoldNode(Node node, Node divisionNodePart, String textContent) {
    	Node bNode;
    	if(XercesUtils.getFirstElementByName(divisionNodePart, BOLD) != null) {
    		bNode = XercesUtils.getFirstElementByName(divisionNodePart, BOLD).cloneNode(true);
    		bNode.setTextContent(textContent);
    	} else {
    		bNode = createElement(node.getOwnerDocument(), BOLD, textContent);
    	}
    	
    	return bNode;
    }
    
    private static Node getItalicNode(Node node, Node divisionNodePart, String textContent) {
    	Node iNode;
    	Node bNode = XercesUtils.getFirstElementByName(divisionNodePart, BOLD);
    	if(bNode != null && XercesUtils.getFirstElementByName(bNode, ITALICS) != null) {
    		iNode = XercesUtils.getFirstElementByName(bNode, ITALICS).cloneNode(true);
    		iNode.setTextContent(textContent);
    	} else {
    		iNode = createElement(node.getOwnerDocument(), ITALICS, textContent);
    	}
    	
    	return iNode;
    }

    public static byte[] wrapWithPageOrientationDivs(Document document) {
        Element landscapeDiv = XercesUtils.createElement(document, DIV, CLASS_ATTR, ORIENTATION_LANDSCAPE, "");
        Element portraitDiv = XercesUtils.createElement(document, DIV, CLASS_ATTR, ORIENTATION_PORTRAIT, "");

        Node mainBody = XercesUtils.getElementsByXPath(document, XPathCatalog.getXPathElement(MAIN_BODY)).item(0);
        List<Node> children = XercesUtils.getChildren(mainBody);
        String prevElement = null;
        mainBody.setTextContent("");
        for (int i = 0; i < children.size(); i++) {
            Node node = children.get(i);
            if (node.getNodeName().equals(LEVEL)) {
                String orientationClass = XercesUtils.getAttributeValue(node, "class");
                if (ORIENTATION_LANDSCAPE.equals(orientationClass)) {
                    if (prevElement == null) {
                        mainBody.appendChild(landscapeDiv);
                    }
                    if (ORIENTATION_LANDSCAPE.equals(prevElement)) {
                        landscapeDiv.appendChild(node);
                    } else {
                        landscapeDiv = XercesUtils.createElement(document, DIV, CLASS_ATTR, ORIENTATION_LANDSCAPE, "");
                        landscapeDiv.appendChild(node);
                        mainBody.appendChild(landscapeDiv);
                    }
                    prevElement = ORIENTATION_LANDSCAPE;
                } else {
                    if (ORIENTATION_PORTRAIT.equals(prevElement) || prevElement == null) {
                        if (prevElement == null) {
                            mainBody.appendChild(portraitDiv);
                        }
                        portraitDiv.appendChild(node);
                    } else {
                        portraitDiv = XercesUtils.createElement(document, DIV, CLASS_ATTR, ORIENTATION_PORTRAIT, "");
                        portraitDiv.appendChild(node);
                        mainBody.appendChild(portraitDiv);
                    }
                    prevElement = ORIENTATION_PORTRAIT;
                }
            }
        }

        return XercesUtils.nodeToByteArray(document);
    }
}
