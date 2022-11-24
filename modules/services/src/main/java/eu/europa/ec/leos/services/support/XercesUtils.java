package eu.europa.ec.leos.services.support;

import static eu.europa.ec.leos.services.support.XPathCatalog.NAMESPACE_AKN_NAME;
import static eu.europa.ec.leos.services.support.XPathCatalog.NAMESPACE_AKN_URI;
import static eu.europa.ec.leos.services.support.XmlHelper.BLOCK;
import static eu.europa.ec.leos.services.support.XmlHelper.CLASS_ATTR;
import static eu.europa.ec.leos.services.support.XmlHelper.CLOSE_END_TAG;
import static eu.europa.ec.leos.services.support.XmlHelper.CLOSE_TAG;
import static eu.europa.ec.leos.services.support.XmlHelper.CROSSHEADING;
import static eu.europa.ec.leos.services.support.XmlHelper.EMPTY_STRING;
import static eu.europa.ec.leos.services.support.XmlHelper.ID;
import static eu.europa.ec.leos.services.support.XmlHelper.INDENT;
import static eu.europa.ec.leos.services.support.XmlHelper.INLINE;
import static eu.europa.ec.leos.services.support.XmlHelper.INLINE_NUM;
import static eu.europa.ec.leos.services.support.XmlHelper.LEOS_SOFT_ACTION_ATTR;
import static eu.europa.ec.leos.services.support.XmlHelper.LIST;
import static eu.europa.ec.leos.services.support.XmlHelper.NUM;
import static eu.europa.ec.leos.services.support.XmlHelper.OPEN_END_TAG;
import static eu.europa.ec.leos.services.support.XmlHelper.OPEN_TAG;
import static eu.europa.ec.leos.services.support.XmlHelper.PARAGRAPH;
import static eu.europa.ec.leos.services.support.XmlHelper.POINT;
import static eu.europa.ec.leos.services.support.XmlHelper.SOFT_ACTIONS_PREFIXES;
import static eu.europa.ec.leos.services.support.XmlHelper.STYLE;
import static eu.europa.ec.leos.services.support.XmlHelper.UTF_8;
import static eu.europa.ec.leos.services.support.XmlHelper.XMLID;
import static eu.europa.ec.leos.services.support.XmlHelper.XML_NAME;
import static eu.europa.ec.leos.services.support.XmlHelper.convertStringDateToCalendar;
import static eu.europa.ec.leos.services.support.XmlHelper.findString;
import static eu.europa.ec.leos.services.support.XmlHelper.isExcludedNode;
import static eu.europa.ec.leos.services.support.XmlHelper.parseXml;
import static eu.europa.ec.leos.services.support.XmlHelper.removeSelfClosingElements;

import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.jaxen.dom.DOMXPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.xml.SimpleNamespaceContext;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import eu.europa.ec.leos.model.action.SoftActionType;

public class XercesUtils {

    private static final Logger LOG = LoggerFactory.getLogger(XercesUtils.class);

    public static Document createXercesDocument(byte[] xmlContent, boolean namespaceEnabled) {
        try {
            DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
            builderFactory.setNamespaceAware(namespaceEnabled);
            DocumentBuilder builder = builderFactory.newDocumentBuilder();

            Document doc = builder.parse(new ByteArrayInputStream(xmlContent));
            doc.getDocumentElement().normalize();
            return doc;
        } catch (Exception e) {
            throw new IllegalStateException("cannot create createXercesDocument ", e);
        }
    }

    public static Document createXercesDocument(byte[] xmlContent) {
        return createXercesDocument(xmlContent, true);
    }

    public static Node createNodeFromXmlFragment(byte[] xmlFragment) {
        Document document = createXercesDocument(xmlFragment, true);
        return document.getFirstChild();
    }

    public static Node createNodeFromXmlFragment(Document document, byte[] xmlFragment) {
        return createNodeFromXmlFragment(document, xmlFragment, true);
    }

    public static Node createNodeFromXmlFragment(Document document, byte[] xmlFragment, boolean namespaceEnabled) {
        Document externalDoc = createXercesDocument(xmlFragment, namespaceEnabled);
        Node externalNode = externalDoc.getDocumentElement();
        return document.importNode(externalNode, true);
    }

    public static Node addContentToNode(Node node, String newContent) {
        return addContentToNode(node, newContent, true);
    }

    public static Node addContentToNode(Node node, String newContent, boolean removeExisting) {
        if (removeExisting) {
            node.setTextContent("");
        }
        String nodeAsString = nodeToString(node);
        nodeAsString = removeSelfClosingElements(nodeAsString);
        String openTagAndActualContent = findString(nodeAsString, "<mref(.|\\S|\\n)*?>");
        String closeTagStr = findString(nodeAsString, "<\\/\\S+?>$");
        String newNodeXml = openTagAndActualContent + newContent + closeTagStr;
        Node newNode = createNodeFromXmlFragment(node.getOwnerDocument(), newNodeXml.getBytes(UTF_8));
        replaceElement(newNode, node);
        return newNode;
    }

    public static Document getDocument(Node node) {
        Document document;
        if (node.getNodeType() == Node.DOCUMENT_NODE) {
            document = ((Document) node);
        } else {
            document = node.getOwnerDocument();
        }
        return document;
    }

    public static byte[] nodeToByteArray(Node node) {
        return nodeToStringWithTransformer(node).getBytes(UTF_8);
    }

    public static String nodeToString(Node node) {
        return nodeToStringWithTransformer(node);
    }

    /**
     * This method performs better that nodeToStringSimple() for normal/big documents.
     * For small fragments nodeToStringSimple() performs better
     */
    public static String nodeToStringWithTransformer(Node node) {
        StringWriter sw = new StringWriter();
        StreamResult output = new StreamResult(sw);
        saveNodeToOutput(node, output);
        return sw.toString();
    }

    private static void saveNodeToOutput(Node node, StreamResult output) {
        try {
            final Source input = new DOMSource(node);
            final TransformerFactory transformerFactory = TransformerFactory.newInstance();

            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            //transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            transformer.transform(input, output);
        } catch (Exception e) {
            throw new IllegalStateException("Cannot save Node to output", e);
        }
    }

    public static byte[] nodeToByteArraySimple(Node node) {
        return nodeToStringSimple(node).getBytes(UTF_8);
    }

    public static String getContentNodeAsXmlFragment(Node node) {
        String xmlContent = nodeToStringSimple(node);
        return XmlHelper.removeEnclosingTags(xmlContent);
    }

    /**
     * Skips all XML headers and print only the real XML root <akomaNtoso>.
     * This method performs better that nodeToStringWithTransformer() for small contents.
     * For normal/big documents use nodeToStringWithTransformer.
     */
    public static String nodeToStringSimple(Node node) {
        StringBuffer sb = new StringBuffer();
        if (node != null) {
            if (node.getNodeType() == Node.DOCUMENT_NODE) {
                node = node.getFirstChild();
            }
            buildNodeAsString(node, sb);
            if (sb.toString().length() == 0) {
                buildNodeAsString(getNextSibling(node), sb);
            }
        }
        return sb.toString();
    }

    private static String buildNodeAsString(Node node, StringBuffer sb) {
        if (node.getNodeType() == Node.ELEMENT_NODE) {
            sb.append(OPEN_TAG + node.getNodeName());  // sb: <tagName
            NamedNodeMap attributesMap = node.getAttributes();
            if (attributesMap != null) {
                for (int i = 0; i < attributesMap.getLength(); i++) {
                    Node attr = attributesMap.item(i);
                    sb.append(" " + attr.getNodeName() + "=\"" + attr.getTextContent() + "\"");  // sb: <tagName atr="attrVal"
                }
            }
            if (node.hasChildNodes()) {
                sb.append(CLOSE_TAG);// sb: <tagName atr="attrVal">
            } else {
                sb.append(CLOSE_END_TAG);// sb: <tagName atr="attrVal"/>
            }
        } else if (node.getNodeType() == Node.TEXT_NODE) {
            sb.append(parseXml(node.getTextContent()));
        }

        NodeList nodeList = node.getChildNodes();
        for (int i = 0; i < nodeList.getLength(); i++) {
            buildNodeAsString(nodeList.item(i), sb); //propagate to children
        }

        if (node.getNodeType() == Node.ELEMENT_NODE) {
            if (node.hasChildNodes()) {
                sb.append(OPEN_END_TAG + node.getNodeName() + CLOSE_TAG); // sb:  <tagName atr="attrVal">nodeValue</tagName>
            } // else is a self closed tag
        }
        return sb.toString();
    }

    public static Node getElementById(Node node, String elementId) {
        return getElementById(node, elementId, true);
    }

    public static Node getElementById(Node node, String elementId, boolean namespaceEnabled) {
        String attrId = namespaceEnabled ? XMLID : ID;
        // Preferred to use XPath for finding elements by ID.
        // In order to use API method getElementById(elementId) rules has to be set to Xerces to indicate which
        // from the parameters will be considered as ID.
        NodeList nodes = getElementsByXPath(node, String.format("//*[@%s = '%s']", attrId, elementId), namespaceEnabled);
        if (nodes.getLength() == 0) {
            if (namespaceEnabled) { //try without namespace.
                // TODO Is a bad design! Actually we shouldn't be in a situation when we load the DOM tree with namespace enabled
                // while  we keep treating the ID attribute without namespace. Is happening in comparison when converting the
                // files in transformerService.formatToHtml()
                nodes = getElementsByXPath(node, String.format("//*[@%s = '%s']", ID, elementId), false);
            }
            if (nodes.getLength() == 0) {
                return null;
            }
        }

        if (nodes.getLength() > 1) {
            LOG.warn("Found more than 1 element with the same ID '{}', returning the first one ", elementId);
        }
        return nodes.item(0);
    }

    public static Node getElementByNameAndId(Node node, String tagName, String elementId) {
        NodeList nodeList = getElementsByName(node, tagName);
        if (elementId == null && nodeList.getLength() > 0) {
            return nodeList.item(0);
        }

        //TODO consider to remove the old logic in favor of: getElementById(node, elementId);
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node child = nodeList.item(i);
            String childId = getAttributeValue(child, XMLID);
            if (elementId.equals(childId)) {
                return child;
            }
        }
        return null;
    }

    public static Node getFirstElementByXPath(Node node, String xPath) {
        NodeList nodeList = getElementsByXPath(node, xPath);
        return nodeList.item(0);
    }

    public static Node getFirstElementByXPath(Node node, String xPath, boolean namespaceEnabled) {
        NodeList nodeList = getElementsByXPath(node, xPath, namespaceEnabled);
        return nodeList.item(0);
    }

    public static int getElementCountByXpath(Node node, String xPath, boolean namespaceEnabled) {
        NodeList list = getElementsByXPath(node, xPath, namespaceEnabled);
        return list.getLength();
    }

    public static NodeList getElementsByXPath(Node node, String xPath) {
        return getElementsByXPath(node, xPath, true);
    }

    public static NodeList getElementsByXPath(Node node, String xPath, boolean namespaceEnabled) {
        try {
            XPath xPathParser = XPathFactory.newInstance().newXPath();
            if (namespaceEnabled) {
                xPathParser.setNamespaceContext(getSimpleNamespaceContext());
            }
            NodeList nodes = (NodeList) xPathParser.evaluate(xPath, node, XPathConstants.NODESET);
            return nodes;
        } catch (XPathExpressionException e) {
            throw new IllegalArgumentException("Cannot find xpath " + xPath);
        }
    }

    public static boolean evalXPathJaxen(Node node, String xPath) {
        try {
            DOMXPath myXPath = new DOMXPath(xPath);
            String myContent = myXPath.stringValueOf(node);
            if (!StringUtils.isEmpty(myContent))
                return true;
            else {
                return false;
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Cannot find xpath " + xPath);
        }
    }

    public static boolean evalXPath(Node node, String xPath) {
        return evalXPath(node, xPath, true);
    }

    public static boolean evalXPath(Node node, String xPath, boolean namespaceEnabled) {
        boolean elementFound = false;
        try {
            XPath xPathParser = XPathFactory.newInstance().newXPath();
            if (namespaceEnabled) {
                xPathParser.setNamespaceContext(getSimpleNamespaceContext());
            }
            NodeList nodes = (NodeList) xPathParser.evaluate(xPath, node, XPathConstants.NODESET);
            if (nodes != null && nodes.getLength() > 0) {
                elementFound = true;
            }
        } catch (XPathExpressionException e) {
            elementFound = false;
        }

        return elementFound;
    }

    private static SimpleNamespaceContext getSimpleNamespaceContext() {
        SimpleNamespaceContext nsc = new SimpleNamespaceContext();
        nsc.bindNamespaceUri("xml", "http://www.w3.org/XML/1998/namespace");
        nsc.bindNamespaceUri("leos", "urn:eu:europa:ec:leos");
        nsc.bindNamespaceUri(NAMESPACE_AKN_NAME, NAMESPACE_AKN_URI); //fake to trick the parser for the default ns
        return nsc;
    }

    public static Node getFirstElementByName(Node node, String elementName) {
        NodeList nodeList = getElementsByName(node, elementName);
        return nodeList.item(0);
    }

    public static NodeList getElementsByName(Node node, String elementName) {
        NodeList nodeList;
        if (node.getNodeType() == Node.ELEMENT_NODE) {
            nodeList = ((Element) node).getElementsByTagName(elementName);
        } else if (node.getNodeType() == Node.DOCUMENT_NODE) {
            nodeList = ((Document) node).getElementsByTagName(elementName);
        } else {
            throw new IllegalArgumentException("Cannot get elements of type " + elementName + " inside node " + node.getNodeName());
        }
        return nodeList;
    }

    public static Node deleteElement(Node node) {
        return node.getParentNode().removeChild(node);
    }

    public static Node deleteElementById(Node node, String elementId) {
        node = getElementById(node, elementId);
        return node != null ? node.getParentNode().removeChild(node): null;
    }

    public static List<Node> deleteElementsByXPath(Node node, String xPath) {
        return deleteElementsByXPath(node, xPath, true);
    }

    public static List<Node> deleteElementsByXPath(Node node, String xPath, boolean namespaceEnabled) {
        List<Node> deletedNodes = new ArrayList<>();
        NodeList nodeList = getElementsByXPath(node, xPath, namespaceEnabled);
        for (int i = 0; i < nodeList.getLength(); i++) {
            if (nodeList.item(i).getParentNode() != null) {
                deletedNodes.add(nodeList.item(i).getParentNode().removeChild(nodeList.item(i)));
            }
        }
        return deletedNodes;
    }

    public static Node replaceElement(Node newNode, Node oldNode) {
        Validate.notNull(newNode, "New node cannot be null!");
        Validate.notNull(oldNode, "Old node cannot be null!");
        Validate.notNull(oldNode.getParentNode(), "Parent of Old Node '" + oldNode.getNodeName() + "' cannot be null!");
        return oldNode.getParentNode().replaceChild(newNode, oldNode);
    }

    public static Node replaceElement(Node node, String newContent) {
        Node fakeNodeWithNewContent = createNodeFromXmlFragment(node.getOwnerDocument(), ("<fake>" + newContent + "</fake>").getBytes(UTF_8), false);
        NodeList fakeNodeChildNodes = fakeNodeWithNewContent.getChildNodes();
        for (int i = fakeNodeChildNodes.getLength() - 1; i >= 0; i--) {
            addSibling(fakeNodeChildNodes.item(i), node, false);
        }
        deleteElement(node);
        return node.getOwnerDocument();
    }

    public static Node importNodeInDocument(Document document, Node node) {
        return document.importNode(node, true);
    }

    public static Node getSibling(Node node, boolean before) {
        Node sibling;
        if (before) {
            sibling = getPrevSibling(node);
        } else {
            sibling = getNextSibling(node);
        }
        return sibling;
    }

    public static Node getNextSibling(Node node) {
        while ((node = node.getNextSibling()) != null) {
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                break;
            }
        }
        return node;
    }

    public static Node getNextSibling(Node node, String elementName) {
        while ((node = node.getNextSibling()) != null) {
            if (node.getNodeType() == Node.ELEMENT_NODE && node.getNodeName().equals(elementName)) {
                break;
            }
        }
        return node;
    }

    public static Node getPrevSibling(Node node) {
        while ((node = node.getPreviousSibling()) != null) {
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                break;
            }
        }
        return node;
    }

    public static Node getPrevSibling(Node node, String elementName) {
        while ((node = node.getPreviousSibling()) != null) {
            if (node.getNodeType() == Node.ELEMENT_NODE && elementName.equals(node.getNodeName())) {
                break;
            }
        }
        return node;
    }

    public static Node addSibling(Node newNode, Node node, boolean before) {
        Validate.notNull(node, "Node cannot be null!");
        Validate.notNull(node.getParentNode(), "Node do not have a parent!");

        final Node parentNode = node.getParentNode();

        if (before) {
            newNode = parentNode.insertBefore(newNode, node);
        } else {
            node = getNextSibling(node);
            if (node != null) {
                newNode = parentNode.insertBefore(newNode, node);
            } else {
                newNode = parentNode.appendChild(newNode);
            }
        }
        return newNode;
    }

    public static Node addChild(Node newNode, Node node) {
        return node.appendChild(newNode);
    }

    public static Node addFirstChild(Node newNode, Node node) {
        Node firstChild = getFirstChild(node);
        return firstChild != null ? addSibling(newNode, firstChild, true) : addChild(newNode, node);
    }

    public static Node addLastChild(Node newNode, Node node) {
        node = node.getLastChild();
        return addSibling(newNode, node, false);
    }

    public static Element addLeosNamespace(Node node) {
        return addAttribute(node, "xmlns:leos", "urn:eu:europa:ec:leos");
    }

    public static Element addAttribute(Node node, String attrName, String attrValue) {
        Validate.notNull(node, "Node cannot be null!");
        Validate.notNull(attrName, "Attribute name should not be null");
        Element element;
        if (node instanceof Document) {
            Document document = (Document) node;
            element = document.getDocumentElement();
        } else if (node instanceof Element) {
            element = (Element) node;
        } else {
            throw new IllegalArgumentException("Not handled!");
        }

        if (attrValue != null) {
            element.setAttribute(attrName, attrValue);
        } else {
            element.removeAttribute(attrName);
        }

        return element;
    }

    public static boolean removeAttribute(Node node, String attName) {
        boolean flag = false;
        if (node.getNodeType() != Node.ELEMENT_NODE) {
            throw new IllegalArgumentException("Node is not of type Element");
        }
        Element element = (Element) node;
        if (element.hasAttribute(attName)) {
            element.removeAttribute(attName);
            flag = true;
        }
        return flag;
    }

    public static Node removeAllAttributes(Node node, String attrName, boolean namespaceEnabled) {
        String xPath = String.format("//*[@%s]", attrName);
        NodeList nodeList = getElementsByXPath(node, xPath, namespaceEnabled);
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node eachNode = nodeList.item(i);
            removeAttribute(eachNode, attrName);
        }
        return node;
    }

    public static String getAttributeValueForElementId(Node node, String elementId, String attrName) {
        node = getElementById(node, elementId);
        NamedNodeMap attributesMap = node.getAttributes();
        Node nodeAttribute = attributesMap.getNamedItem(attrName);
        String attrVal = null;
        if (nodeAttribute != null) {
            attrVal = nodeAttribute.getTextContent();
        }
        return attrVal;
    }

    public static String getAttributeValue(Node node, String attrName) {
        String attrVal = null;
        if(node != null) {
            NamedNodeMap attributesMap = node.getAttributes();
            if (attributesMap != null) {
                Node nodeAttribute = attributesMap.getNamedItem(attrName);
                if (nodeAttribute != null) {
                    attrVal = nodeAttribute.getTextContent();
                }
            }
        }
        return attrVal;
    }

    public static boolean getAttributeValueAsSimpleBoolean(Node node, String attrName) {
        Boolean attrVal = getAttributeValueAsBoolean(node, attrName);
        return attrVal != null ? Boolean.valueOf(attrVal) : false;
    }

    public static boolean containsAttribute(Node node, String attrName) {
        if (node != null) {
            String attrVal = getAttributeValue(node, attrName);
            if (!StringUtils.isEmpty(attrVal)) {
                return true;
            }
        }
        return false;
    }

    public static boolean containsAttributeWithValue(Node node, String attrName, String attrVal) {
        if (node != null) {
            String attrValue = getAttributeValue(node, attrName);
            if (!StringUtils.isEmpty(attrValue) && attrValue.equals(attrVal)) {
                return true;
            }
        }
        return false;
    }

    public static Map<String, String> getAttributes(Node node) {
        Map<String, String> attrs = new HashMap<>();
        NamedNodeMap attributesMap = node.getAttributes();
        if (attributesMap != null) {
            for (int i = 0; i < attributesMap.getLength(); i++) {
                Node nodeAttribute = attributesMap.item(i);
                attrs.put(nodeAttribute.getNodeName(), nodeAttribute.getTextContent());
            }
        }
        return attrs;
    }

    public static Integer getAttributeValueAsIntegerOrZero(Node node, String attrName) {
        Integer val = getAttributeValueAsInteger(node, attrName);
        return val != null ? val : 0;
    }

    public static Integer getAttributeValueAsInteger(Node node, String attrName) {
        String attrVal = getAttributeValue(node, attrName);
        return attrVal != null ? Integer.valueOf(attrVal) : null;
    }

    public static Boolean getAttributeValueAsBoolean(Node node, String attrName) {
        String attrVal = getAttributeValue(node, attrName);
        return attrVal != null ? Boolean.valueOf(attrVal) : null;
    }

    // Works only if name and value are of the  same value.
    public static <T extends Enum<T>> T getAttributeForType(Node node, String attrName, Class<T> enumClass) {
        String attrValue = getAttributeValue(node, attrName);
        return !StringUtils.isEmpty(attrValue) ? Enum.valueOf(enumClass, attrValue) : null;
    }

    public static SoftActionType getAttributeForSoftAction(Node node, String attrName) {
        String attrValue = getAttributeValue(node, attrName);
        return !StringUtils.isEmpty(attrValue) ? SoftActionType.of(attrValue) : null;
    }

    public static boolean isSoftChanged(Node node, SoftActionType softActionType) {
        SoftActionType attrValue = getAttributeForSoftAction(node, LEOS_SOFT_ACTION_ATTR);
        return attrValue != null && attrValue.equals(softActionType);
    }

    public static boolean checkAttributeValue(Node node, String attribute, String attributeValue) {
        String value = getAttributeValue(node, attribute);
        if (value != null && value.equals(attributeValue)) {
            return true;
        }
        return false;
    }

    public static GregorianCalendar getAttributeValueAsGregorianCalendar(Node node, String attrName) {
        String attrVal = getAttributeValue(node, attrName);
        return convertStringDateToCalendar(attrVal);
    }

    public static List<Node> getNodesAsList(NodeList nodeList) {
        List<Node> children = new ArrayList<>();
        for (int i = 0; i < nodeList.getLength(); i++) {
            children.add(nodeList.item(i));
        }
        return children;
    }

    public static Node getFirstChild(Node node) {
        Node firstChild = null;
        NodeList nodeList = node.getChildNodes();
        for (int i = 0; i < nodeList.getLength(); i++) {
            node = nodeList.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                firstChild = node;
                break;
            }
        }
        return firstChild;
    }

    public static Node getFirstChild(Node node, String elementName) {
        Node firstChild = null;
        NodeList nodeList = node.getChildNodes();
        for (int i = 0; i < nodeList.getLength(); i++) {
            node = nodeList.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                if (elementName.equals(node.getNodeName())) {
                    firstChild = node;
                    break;
                }
            }
        }
        return firstChild;
    }

    public static String getFirstChildType(Node node, List<String> types) {
        String firstChildType = null;
        List<Node> children = getChildren(node, types);
        if (children == null || children.isEmpty()) {
            throw new IllegalArgumentException("No child of type: " + types + " was found in the node");
        } else {
            firstChildType = children.get(0).getNodeName();
        }
        return firstChildType;
    }

    public static int countChildren(Node node, List<String> elementsName) {
        List<Node> children = getChildren(node, elementsName);
        return children.size();
    }

    public static List<Node> getChildrenExcluding(Node node, List<String> elementsName) {
        List<Node> children = new ArrayList<>();
        NodeList nodeList = node.getChildNodes();
        for (int i = 0; i < nodeList.getLength(); i++) {
            node = nodeList.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE
                    && !elementsName.contains(node.getNodeName())) {
                children.add(node);
            }
        }
        return children;
    }

    public static List<Node> getChildren(Node node, List<String> elementsName) {
        List<Node> children = new ArrayList<>();
        NodeList nodeList = node.getChildNodes();
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node child = nodeList.item(i);
            if (child.getNodeType() == Node.ELEMENT_NODE
                    && (elementsName.contains(child.getNodeName()) || elementsName.isEmpty())) {
                children.add(child);
            }
        }
        return children;
    }

    public static List<Node> getChildren(Node node, String elementName) {
        return getChildren(node, Arrays.asList(elementName));
    }

    public static List<Node> getChildren(Node node) {
        List<Node> children = new ArrayList<>();
        NodeList nodeList = node.getChildNodes();
        for (int i = 0; i < nodeList.getLength(); i++) {
            node = nodeList.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                children.add(node);
            }
        }
        return children;
    }

    public static List<Node> getDescendants(Node node, List<String> tagNames) {
        List<Node> descendants = new ArrayList<>();
        List<Node> children = getChildren(node);
        for (Node child : children) {
            if (tagNames.contains(child.getNodeName())) {
                descendants.add(child);
            }
            descendants.addAll(getDescendants(child, tagNames));
        }
        return descendants;
    }

    public static Node getFirstDescendant(Node node, List<String> tagNames) {
        List<Node> children = getChildren(node);
        for (Node child : children) {
            if (tagNames.contains(child.getNodeName())) {
                return child;
            }
            Node childDescendant = getFirstDescendant(child, tagNames);
            if (childDescendant != null) {
                return childDescendant;
            }
        }
        return null;
    }

    public static int getPointDepth(Node node) {
        int pointDepth = 0;
        if (Arrays.asList(POINT, INDENT).contains(node.getNodeName())) {
            Node parentNode = node.getParentNode();
            while (parentNode != null) {
                String parentName = parentNode.getNodeName();
                if (LIST.equals(parentName)) {
                    pointDepth++;
                } else if (PARAGRAPH.equals(parentName)) {
                    break;
                }
                parentNode = parentNode.getParentNode();
            }
        }
        return pointDepth;
    }

    public static boolean checkFirstChildType(Node node, String type) {
        List<Node> children = getChildren(node, type);
        if (children != null && !children.isEmpty()) {
            return true;
        }
        return false;
    }

    public static String getChildContent(Node node, String childTagName) {
        String number = null;
        Node child = getFirstChild(node, childTagName);
        if (child != null) {
            number = child.getTextContent();
        }
        return number;
    }

    public static String getParentTagName(Node node) {
        String elementTagName = null;
        Node parentNode = node.getParentNode();
        if (parentNode != null) {
            elementTagName = parentNode.getNodeName();
        }
        return elementTagName;
    }

    public static String getParentId(Node node) {
        String id = "";
        Node parentNode = node.getParentNode();
        if (parentNode != null) {
            id = getAttributeValue(parentNode, XMLID);
        }
        return id;
    }

    public static Node getParentWithTagName(Node node, String tagName) {
        Node parentNode = node.getParentNode();
        while (parentNode != null && !(parentNode.getNodeName().equalsIgnoreCase(tagName))) {
            parentNode = parentNode.getParentNode();
        }
        return parentNode;
    }

    public static String getId(Node node) {
        String id = getAttributeValue(node, XMLID);
        if (id == null) {
            id = getAttributeValue(node, ID);
        }
        return id;
    }

    public static Node setId(Node node, String id) {
        return addAttribute(node, XMLID, id);
    }

    public static String getContentByTagName(Node node, String tagName) {
        Node element = getFirstElementByName(node, tagName);
        String content = null;
        if (element != null) {
            content = element.getTextContent();
        }
        return content;
    }

    public static Node createElementAsFirstChildOfNode(Node node, String elementName, String elementContent) {
        Node newNode = createElement(node.getOwnerDocument(), elementName, elementContent);
        newNode = addFirstChild(newNode, node);
        return newNode;
    }

    public static Node createElementAsLastChildOfNode(Document document, Node node, String elementName, String elementContent) {
        Node newNode = createElement(document, elementName, elementContent);
        node.appendChild(newNode);
        return newNode;
    }

    public static Element createElementWithAknNS(Document document, String elementName, String elementContent) {
        Element element = document.createElementNS(NAMESPACE_AKN_URI, elementName);
        element.setTextContent(elementContent);
        return element;
    }

    public static Element createElement(Document document, String elementName, String elementContent) {
        Element element = document.createElement(elementName);
        element.setTextContent(elementContent);
        return element;
    }

    public static Element createElement(Document document, String elementName, String elementId, String content) {
        Element element = document.createElement(elementName);

        Attr attr = document.createAttribute(XMLID);
        attr.setValue(elementId);
        element.setAttributeNode(attr);

        element.appendChild(document.createTextNode(content));
        return element;
    }

    public static String getNodeNum(Node node) {
        String nodeNum = null;
        Node numNode = getFirstChild(node, getNumTag(node.getNodeName()));
        if (numNode != null) {
            nodeNum = numNode.getTextContent();
        }
        return nodeNum;
    }

    private static String getSoftActionPrefix(String id) {
        if (id != null) {
            return SOFT_ACTIONS_PREFIXES.stream()
                    .filter(softActionPrefix -> id.contains(softActionPrefix))
                    .findFirst()
                    .orElse(null);
        } else {
            return null;
        }
    }

    public static void updateXMLIDAttributeFullStructureNode(Node node, String newValuePrefix, boolean replacePrefix) {
        updateXMLIDAttribute(node, newValuePrefix, replacePrefix);
        List<Node> children = getChildren(node);
        for (int i = 0; i < children.size(); i++) {
            updateXMLIDAttributeFullStructureNode(children.get(i), newValuePrefix, replacePrefix);
        }
    }

    public static void updateXMLIDAttribute(Node node, String newValuePrefix, boolean replacePrefix) {
        String id = getId(node);
        if(id == null) {
            LOG.error("Node does not have ID attribute", XercesUtils.nodeToString(node));
            throw new IllegalStateException("Node does not have ID");
        }
        String newId = id;
        String softActionPrefix = getSoftActionPrefix(id);

        //Replace YES and SoftAction Exists and (New prefix is empty or different from the actual)
        if (replacePrefix && softActionPrefix != null && (StringUtils.isEmpty(newValuePrefix) || !newValuePrefix.equals(softActionPrefix))){
            newId = id.replace(softActionPrefix, newValuePrefix);
        }
        //New Value different from Actual (Avoid moved_moved labels) AND Id do not contains already the new Value
        else if (newValuePrefix != null && !newValuePrefix.equals(softActionPrefix) && !newId.startsWith(newValuePrefix)) {
            newId = newValuePrefix + id;
        }
        addAttribute(node, XMLID, newId);
    }

    public static void insertOrUpdateAttributeValue(Node node, String attrName, String attrValue) {
        String currentAttrValue = getAttributeValue(node, attrName);
        if (currentAttrValue != null && attrName.equalsIgnoreCase(CLASS_ATTR)) {
            if (!currentAttrValue.contains(attrValue)) {
                addAttribute(node, attrName, currentAttrValue + " " + attrValue);
            }
        } else {
            addAttribute(node, attrName, attrValue);
        }
    }

    public static void insertAttributeIfNotPresent(Node node, String attrName, String attrValue) {
        String currentAttrValue = getAttributeValue(node, attrName);
        if(currentAttrValue == null) {
            addAttribute(node, attrName, attrValue);
        }
    }

    public static boolean toBeSkippedForNumbering(Node node) {
        String elementActionType = getAttributeValue(node, LEOS_SOFT_ACTION_ATTR);
        return elementActionType != null &&
                (elementActionType.equals(SoftActionType.MOVE_TO.getSoftAction()) ||
                        elementActionType.equals(SoftActionType.DELETE.getSoftAction()));
    }

    public static boolean hasChildTextNode(Node node) {
        return evalXPathJaxen(node, "text()[normalize-space()][string-length() > 0]");
    }

    /**
     * If the Node is already in the Tree, appendChild() moves it from source to target. That's why we are using '0' index instead of 'i'
     *
     * @param source Node from where we want to copy the list of children
     * @param target Node to which we want to bring the list of children
     * @return target node
     */
    public static Node copyContent(Node source, Node target) {
        NodeList nodeList = source.getChildNodes();
        int length = nodeList.getLength();
        for (int i = 0; i < length; i++) {
            Node node = nodeList.item(0); //always the first child
            target.appendChild(node);
        }
        return target;
    }

    public static Node renameNode(Document document, Node node, String tagName) {
        return document.renameNode(node, node.getNamespaceURI(), tagName);
    }

    public static String getNumTag(String tagName) {
        if (tagName.equalsIgnoreCase(CROSSHEADING)
                || tagName.equalsIgnoreCase(BLOCK)) {
            return INLINE;
        } else {
            return NUM;
        }
    }

    public static Element insertOrUpdateStylingAttribute(Node node, String propertyName, Object propertyValue) {
        Validate.notNull(node, "Node cannot be null!");
        Validate.notNull(propertyName, "Property name should not be null");
        Element element;
        if (node instanceof Document) {
            Document document = (Document) node;
            element = document.getDocumentElement();
        } else if (node instanceof Element) {
            element = (Element) node;
        } else {
            throw new IllegalArgumentException("Not handled!");
        }

        String property = propertyName.concat(":").concat(propertyValue != null ? propertyValue.toString() : EMPTY_STRING).concat(";");
        String styleAttrValue = getAttributeValue(element, STYLE);
        if (styleAttrValue != null) {
            StringBuilder currentStyleAttrValue = new StringBuilder(getAttributeValue(element, STYLE));
            int propertyPosition = currentStyleAttrValue.indexOf(propertyName, 0);
            if (propertyPosition != -1) {
                int propertyValStartPos = currentStyleAttrValue.indexOf(":", propertyPosition) + 1;
                int propertyValEndPos = currentStyleAttrValue.indexOf(";", propertyValStartPos);
                currentStyleAttrValue = propertyValue != null ? currentStyleAttrValue.replace(propertyValStartPos, propertyValEndPos, propertyValue.toString()) :
                        currentStyleAttrValue.replace(propertyPosition, propertyValEndPos + 1, EMPTY_STRING);
            } else if (propertyValue != null) {
                currentStyleAttrValue.append(property);
            }
            insertOrUpdateAttributeValue(element, STYLE, currentStyleAttrValue.toString());
        } else {
            addAttribute(element, STYLE, property);
        }
        return element;
    }

    public static boolean isCrossheadingNum(Node node) {
        String name = getAttributeValue(node, XML_NAME);
        return (node.getNodeName().equalsIgnoreCase(INLINE) && name != null && name.equalsIgnoreCase(INLINE_NUM));
    }

    public static SoftActionType getSoftAction(Node node) {
        SoftActionType softActionType = null;
        String tagName = node.getNodeName();
        String attrVal = XercesUtils.getAttributeValue(node, LEOS_SOFT_ACTION_ATTR);
        if (!isExcludedNode(tagName) && attrVal != null) {
            softActionType = SoftActionType.of(attrVal);
        }
        return softActionType;
    }
    
    public static void insertOrUpdateAttributeValueRecursively(Node node, String attrName, String attrValue) {
    	insertOrUpdateAttributeValue(node, attrName, attrValue);
    	if(node.hasChildNodes()) {
    		for(Node child : getChildren(node)) {
    			insertOrUpdateAttributeValueRecursively(child, attrName, attrValue);
    		}
    	}
    }

}