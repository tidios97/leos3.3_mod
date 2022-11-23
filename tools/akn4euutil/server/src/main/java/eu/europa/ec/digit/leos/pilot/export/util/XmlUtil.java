package eu.europa.ec.digit.leos.pilot.export.util;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.SAXException;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.ByteArrayOutputStream;
import java.net.URL;
import java.util.List;
import java.util.ArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.europa.ec.digit.leos.pilot.export.exception.XmlUtilException;
import eu.europa.ec.digit.leos.pilot.export.exception.XmlValidationException;

public class XmlUtil {

    private static final Logger LOG = LoggerFactory.getLogger(XmlUtil.class);

    public static class XmlFile {
        private Document xmlDocument;
        private String name;

        public XmlFile() {
            this("");
        }

        public XmlFile(String name) {
            this.name = name;
            this.xmlDocument = null;
        }

        public static String parseNode(Node node) throws TransformerException {
            if (XmlUtil.isNodeEmpty(node)) {
                return null;
            }
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            StreamResult result = new StreamResult(buffer);
            DOMSource source = new DOMSource(node);
            TransformerFactory.newInstance().newTransformer().transform(source, result);
            String nodeContent = new String(buffer.toByteArray()).replaceAll("(<\\?xml.*?\\?>)", "");
            nodeContent = nodeContent.replaceAll("xmlns(.*?)=(\".*?\")", "");
            return nodeContent;
        }

        public void createNewXmlDocument() throws XmlUtilException {
            try {
                this.xmlDocument = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
            } catch (ParserConfigurationException e) {
                throw new XmlUtilException("Error creating new xml document", e);
            }
        }

        public void parse(InputStream inputStream, String name) throws XmlUtilException {
            try {
                this.xmlDocument = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(inputStream);
                this.name = name;
            } catch (ParserConfigurationException | SAXException | IOException e) {
                throw new XmlUtilException("Error parsing xml stream", e);
            }
        }

        public Element createRoot(final String rootName){
            Element rootElement = this.xmlDocument.createElement(rootName);
            this.xmlDocument.appendChild(rootElement);
            return rootElement;
        }

        public Element newElement(final String elementName){
            return this.xmlDocument.createElement(elementName);
        }

        public Node getRootNode(){
            return this.xmlDocument.getFirstChild();
        }

        public Element getElementById(final String id){
            return this.xmlDocument.getElementById(id);
        }

        public Node getElementByName(final String name){
            NodeList nodes = this.xmlDocument.getElementsByTagName(name);
            return (nodes.getLength() > 0) ? nodes.item(0) : null;
        }

        public NodeList getElementsByName(final String name){
            return this.xmlDocument.getElementsByTagName(name);
        }

        public Document getXmlDocument() {
            return this.xmlDocument;
        }

        public byte[] getBytes() throws XmlUtilException {
            if (this.xmlDocument == null){ return null; }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            try {
                StreamResult xmlStreamResult = new StreamResult(outputStream);
                DOMSource xmlSource = new DOMSource(xmlDocument);
                Transformer transformer = TransformerFactory.newInstance().newTransformer();
                transformer.setOutputProperty(OutputKeys.ENCODING,"utf-8");
                transformer.setOutputProperty(OutputKeys.VERSION, "1.0");
                transformer.transform(xmlSource, xmlStreamResult);
                byte [] xmlBytes = outputStream.toByteArray();
                closeOutputStream(outputStream);
                return xmlBytes;
            } catch(TransformerException e){
                closeOutputStream(outputStream);
                throw new XmlUtilException("Error getting xml bytes", e);
            }
        }

        public Text createTextNode(String value) {
            return this.xmlDocument.createTextNode(value);
        }

        public Element addElementValue(Element element, String value) {
            element.appendChild(this.createTextNode(value));
            return element;
        }

        public String getName(){
            return this.name;
        }

        public void setName(String name) {
            this.name = name;
        }

        private void closeOutputStream(OutputStream outputStream) throws XmlUtilException {
            try {
                outputStream.close();
            } catch(IOException e){
                throw new XmlUtilException("Unable to close byte stream", e);
            }
        }
    }

    public static XmlFile newXmlFile() throws XmlUtilException {
        XmlFile xmlFile = new XmlFile();
        xmlFile.createNewXmlDocument();
        return xmlFile;
    }

    public static Validator getAknSchemaValidator() throws XmlValidationException {
        try {
            SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            URL resource = XmlUtil.class.getClassLoader().getResource("metadata/schema/akomantoso30.xsd");
            Schema schema = factory.newSchema(resource);
            return schema.newValidator();
        } catch (SAXException ex) {
            throw new XmlValidationException("Error creating schema validator", ex);
        }
    }

    public static XmlFile parseXml(InputStream inputStream) throws XmlUtilException {
        return parseXml(inputStream, "");
    }

    public static XmlFile parseXml(InputStream inputStream, String name) throws XmlUtilException {
        XmlFile xmlFile = new XmlFile();
        xmlFile.parse(inputStream, name);
        return xmlFile;
    }

    public static boolean nodeNameEquals(Node node, String nodeName){
        if (XmlUtil.isNodeEmpty(node)) {
            return false;
        }
        if (StringUtil.isEmpty(node.getNodeName())) {
            return StringUtil.isEmpty(nodeName);
        }
        return node.getNodeName().equals(nodeName);
    }

    public static boolean nodeHasAttribute(Node node, String attributeName){
        if (XmlUtil.isNodeEmpty(node)) {
            return false;
        }
        if (node.getAttributes() == null) {
            return false;
        }
        final Node attributeNode = node.getAttributes().getNamedItem(attributeName);
        return !XmlUtil.isNodeEmpty(attributeNode);
    }

    public static String getNodeAttributeValue(Node node, String attributeName){
        return XmlUtil.nodeHasAttribute(node, attributeName)
                ? node.getAttributes().getNamedItem(attributeName).getNodeValue() : null;
    }

    public static boolean nodeAttributeValueEquals(Node node, String attributeName, String attributeValue){
        String value = XmlUtil.getNodeAttributeValue(node, attributeName);
        return !StringUtil.isEmpty(value) && value.equals(attributeValue);
    }

    public static Node getChildNodeWithName(Node node, String childName){
        if (XmlUtil.isNodeEmpty(node)) {
            return null;
        }

        Node result = null;
        NodeList childNodes = node.getChildNodes();
        int index = 0;

        while(index < childNodes.getLength() && XmlUtil.isNodeEmpty(result)){
            if (nodeNameEquals(childNodes.item(index), childName)){
                result = childNodes.item(index);
            }
            index++;
        }
        return result;
    }

    public static List<Node> getChildNodesWithName(Node node, String childName){
        NodeList childNodes = node.getChildNodes();
        List<Node> result = new ArrayList<>();

        for (int i = 0; i < childNodes.getLength(); i++){
            if (nodeNameEquals(childNodes.item(i), childName)){
                result.add(childNodes.item(i));
            }
        }

        return result;
    }

    public static boolean parentNodeNameEquals(Node node, String name){
        return !XmlUtil.isNodeEmpty(node.getParentNode()) && nodeNameEquals(node.getParentNode(), name);
    }

    public static void setNodeAttributeValue(Node node, String attributeName, String attributeValue){
        ((Element)node).setAttribute(attributeName, attributeValue);
    }

    public static Node getXmlChildNodeWithNameAttributeValue(Node xmlNode, String nameAttributeValue){
        return getXmlChildNodeWithAttributeValue(xmlNode, "name", nameAttributeValue);
    }

    public static Node getXmlChildNodeWithXmlIdAttributeValue(Node xmlNode, String xmlIdAttributeValue){
        return getXmlChildNodeWithAttributeValue(xmlNode, "xml:id", xmlIdAttributeValue);
    }

    public static Node getXmlChildNodeWithAttributeValue(Node xmlNode, String attributeName, String attributeValue){
        if (XmlUtil.isNodeEmpty(xmlNode)){
            return null;
        }

        Node xmlNodeResult = null;
        NodeList xmlChildNodes = xmlNode.getChildNodes();
        int index = 0;

        while(index < xmlChildNodes.getLength() && XmlUtil.isNodeEmpty(xmlNodeResult)) {
            Node xmlChildNode = xmlChildNodes.item(index);
            if (nodeAttributeValueEquals(xmlChildNode, attributeName, attributeValue)) {
                xmlNodeResult = xmlChildNode;
            }
            index++;
        }
        return xmlNodeResult;
    }

    public static boolean isNodeEmpty(final Node node) {
        return (node == null);
    }
}