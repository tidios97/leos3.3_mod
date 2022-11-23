package eu.europa.ec.digit.leos.pilot.export.util;

import eu.europa.ec.digit.leos.pilot.export.exception.XmlUtilException;
import eu.europa.ec.digit.leos.pilot.export.util.XmlUtil.XmlFile;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.util.List;

public class XmlUtilsTests {
    /**
     *  {@link XmlFile} class Tests
     * */

    @Test
    public void testCreateNewXmlDocument() throws XmlUtilException {
        final XmlFile xmlFile = XmlUtil.newXmlFile();
        xmlFile.createNewXmlDocument();

        Assertions.assertNotNull(xmlFile);
        Assertions.assertNotNull(xmlFile.getXmlDocument());
    }

    @Test
    public void testCreateRoot() {
        XmlFile xmlFile = this.createNewXmlFile();
        Assertions.assertNotNull(xmlFile);

        final Element rootElement = xmlFile.createRoot("RootNode");
        Assertions.assertNotNull(rootElement);
        Assertions.assertEquals("RootNode", rootElement.getNodeName());
    }

    @Test
    public void testGetRootNode() {
        XmlFile xmlFile = this.createNewXmlFile();
        Assertions.assertNotNull(xmlFile);

        final Node emptyNode = xmlFile.getRootNode();
        Assertions.assertNull(emptyNode);

        xmlFile.createRoot("RootNode");
        final Node rootNode = xmlFile.getRootNode();
        Assertions.assertNotNull(rootNode);
        Assertions.assertEquals("RootNode", rootNode.getNodeName());
    }

    @Test
    public void testGetElementByName() {
        final XmlFile xmlFile = this.createTestXmlFile();
        Assertions.assertNotNull(xmlFile);

        final Node textNode = xmlFile.getElementByName("TextNode");
        Assertions.assertNotNull(textNode);
        Assertions.assertEquals("TextNode", textNode.getNodeName());
        Assertions.assertEquals("This is a Text", textNode.getTextContent());

        final Node emptyNode = xmlFile.getElementByName("WrongNodeName");
        Assertions.assertNull(emptyNode);
    }

    @Test
    private XmlFile createNewXmlFile() {
        try {
            final XmlFile xmlFile = XmlUtil.newXmlFile();
            xmlFile.createNewXmlDocument();
            return xmlFile;
        } catch(XmlUtilException ex) {
            return null;
        }
    }

    /**
     * {@link XmlUtil} static methods tests
     * */

    @Test
    public void testNodeNameEquals() {
        final XmlFile xmlFile = this.createTestXmlFile();
        Assertions.assertNotNull(xmlFile);

        final Node textNode = xmlFile.getElementByName("TextNode");
        Assertions.assertNotNull(textNode);
        Assertions.assertTrue(XmlUtil.nodeNameEquals(textNode, "TextNode"));
        Assertions.assertFalse(XmlUtil.nodeNameEquals(textNode, "WrongNodeName"));
    }

    @Test
    public void testNodeHasAttribute() {
        final XmlFile xmlFile = this.createTestXmlFile();
        Assertions.assertNotNull(xmlFile);

        final Node attributeNode = xmlFile.getElementByName("AttributeNode");
        Assertions.assertNotNull(attributeNode);
        Assertions.assertTrue(XmlUtil.nodeHasAttribute(attributeNode, "attribute"));
        Assertions.assertTrue(XmlUtil.nodeHasAttribute(attributeNode, "name"));
        Assertions.assertTrue(XmlUtil.nodeHasAttribute(attributeNode, "xml:id"));
        Assertions.assertFalse(XmlUtil.nodeNameEquals(attributeNode, "WrongAttributeName"));
    }

    @Test
    public void testGetNodeAttributeValue() {
        final XmlFile xmlFile = this.createTestXmlFile();
        Assertions.assertNotNull(xmlFile);

        final Node attributeNode = xmlFile.getElementByName("AttributeNode");
        Assertions.assertNotNull(attributeNode);
        final String actualAttributeValue = XmlUtil.getNodeAttributeValue(attributeNode, "attribute");
        Assertions.assertEquals("AttributeValue01", actualAttributeValue);

        final String nullValue = XmlUtil.getNodeAttributeValue(attributeNode, "notFound");
        Assertions.assertNull(nullValue);
    }

    @Test
    public void testNodeAttributeValueEquals() {
        final XmlFile xmlFile = this.createTestXmlFile();
        Assertions.assertNotNull(xmlFile);

        final Node attributeNode = xmlFile.getElementByName("AttributeNode");
        Assertions.assertNotNull(attributeNode);
        Assertions.assertTrue(XmlUtil.nodeAttributeValueEquals(attributeNode, "attribute", "AttributeValue01"));
        Assertions.assertFalse(XmlUtil.nodeAttributeValueEquals(attributeNode, "attribute", "WrongValue"));
    }

    @Test
    public void testGetChildNodeWithName() {
        final XmlFile xmlFile = this.createTestXmlFile();
        Assertions.assertNotNull(xmlFile);

        final Node rootNode = xmlFile.getElementByName("RootNode");
        Assertions.assertNotNull(rootNode);
        Assertions.assertNotNull(XmlUtil.getChildNodeWithName(rootNode, "TextNode"));
        Assertions.assertNotNull(XmlUtil.getChildNodeWithName(rootNode, "AttributeNode"));
        Assertions.assertNotNull(XmlUtil.getChildNodeWithName(rootNode, "SameNode"));
        Assertions.assertNull(XmlUtil.getChildNodeWithName(rootNode, "WrongNodeName"));
    }

    @Test
    public void testGetChildNodesWithName() {
        final XmlFile xmlFile = this.createTestXmlFile();
        Assertions.assertNotNull(xmlFile);

        final Node rootNode = xmlFile.getElementByName("RootNode");
        Assertions.assertNotNull(rootNode);

        List<Node> childNodes = XmlUtil.getChildNodesWithName(rootNode, "SameNode");
        Assertions.assertEquals(2, childNodes.size());
        Assertions.assertEquals("SameNode", childNodes.get(0).getNodeName());
        Assertions.assertEquals("SameNode", childNodes.get(1).getNodeName());

        childNodes = XmlUtil.getChildNodesWithName(rootNode, "TextNode");
        Assertions.assertEquals(1, childNodes.size());
        Assertions.assertEquals("TextNode", childNodes.get(0).getNodeName());

        childNodes = XmlUtil.getChildNodesWithName(rootNode, "WrongNodeName");
        Assertions.assertEquals(0, childNodes.size());
    }

    @Test
    public void testParentNodeNameEquals() {
        final XmlFile xmlFile = this.createTestXmlFile();
        Assertions.assertNotNull(xmlFile);

        final Node textNode = xmlFile.getElementByName("TextNode");
        Assertions.assertNotNull(textNode);
        Assertions.assertTrue(XmlUtil.parentNodeNameEquals(textNode, "RootNode"));
        Assertions.assertFalse(XmlUtil.parentNodeNameEquals(textNode, "WrongParentName"));
    }

    @Test
    public void testSetNodeAttributeValue() {
        final XmlFile xmlFile = this.createTestXmlFile();
        Assertions.assertNotNull(xmlFile);

        final Node textNode = xmlFile.getElementByName("TextNode");
        Assertions.assertNotNull(textNode);
        XmlUtil.setNodeAttributeValue(textNode, "myAttribute", "NewValue");
        Assertions.assertTrue(XmlUtil.nodeAttributeValueEquals(textNode, "myAttribute", "NewValue"));

        final Node attributeNode = xmlFile.getElementByName("AttributeNode");
        Assertions.assertNotNull(attributeNode);
        XmlUtil.setNodeAttributeValue(attributeNode, "name", "ChangedName");
        Assertions.assertTrue(XmlUtil.nodeAttributeValueEquals(attributeNode, "name", "ChangedName"));
    }

    @Test
    public void testGetXmlChildNodeWithNameAttributeValue() {
        final XmlFile xmlFile = this.createTestXmlFile();
        Assertions.assertNotNull(xmlFile);

        final Node rootNode = xmlFile.getElementByName("RootNode");
        Assertions.assertNotNull(rootNode);

        final Node childNode = XmlUtil.getXmlChildNodeWithNameAttributeValue(rootNode, "Node02");
        Assertions.assertNotNull(childNode);
        Assertions.assertTrue(XmlUtil.nodeNameEquals(childNode, "AttributeNode"));

        final Node nullNode = XmlUtil.getXmlChildNodeWithNameAttributeValue(rootNode, "WrongValue");
        Assertions.assertNull(nullNode);
    }

    @Test
    public void testGetXmlChildNodeWithXmlIdAttributeValue() {
        final XmlFile xmlFile = this.createTestXmlFile();
        Assertions.assertNotNull(xmlFile);

        final Node rootNode = xmlFile.getElementByName("RootNode");
        Assertions.assertNotNull(rootNode);

        final Node childNode = XmlUtil.getXmlChildNodeWithXmlIdAttributeValue(rootNode, "ID01");
        Assertions.assertNotNull(childNode);
        Assertions.assertTrue(XmlUtil.nodeNameEquals(childNode, "AttributeNode"));

        final Node nullNode = XmlUtil.getXmlChildNodeWithXmlIdAttributeValue(rootNode, "WrongId");
        Assertions.assertNull(nullNode);
    }

    @Test
    public void testGetXmlChildNodeWithAttributeValue() {
        final XmlFile xmlFile = this.createTestXmlFile();
        Assertions.assertNotNull(xmlFile);

        final Node rootNode = xmlFile.getElementByName("RootNode");
        Assertions.assertNotNull(rootNode);

        final Node childNode = XmlUtil.getXmlChildNodeWithAttributeValue(rootNode, "attribute", "SameNodeAttributeValue");
        Assertions.assertNotNull(childNode);
        Assertions.assertTrue(XmlUtil.nodeNameEquals(childNode, "SameNode"));

        Node nullNode = XmlUtil.getXmlChildNodeWithAttributeValue(rootNode, "WrongAttribute", "");
        Assertions.assertNull(nullNode);

        nullNode = XmlUtil.getXmlChildNodeWithAttributeValue(rootNode, "attribute", "WrongValue");
        Assertions.assertNull(nullNode);
    }

    private XmlFile createTestXmlFile() {
        try {
            XmlFile xmlFile = XmlUtil.newXmlFile();

            Element rootNode = xmlFile.createRoot("RootNode");

            Element textNode = xmlFile.getXmlDocument().createElement("TextNode");
            textNode.setTextContent("This is a Text");
            rootNode.appendChild(textNode);

            Element attributeNode = xmlFile.getXmlDocument().createElement("AttributeNode");
            attributeNode.setAttribute("attribute", "AttributeValue01");
            attributeNode.setAttribute("name", "Node02");
            attributeNode.setAttribute("xml:id", "ID01");
            rootNode.appendChild(attributeNode);

            Element sameNode01 = xmlFile.getXmlDocument().createElement("SameNode");
            sameNode01.setAttribute("attribute", "SameNodeAttributeValue");
            Element sameNode02 = xmlFile.getXmlDocument().createElement("SameNode");
            rootNode.appendChild(sameNode01);
            rootNode.appendChild(sameNode02);

            return xmlFile;
        } catch(XmlUtilException ex) {
            return null;
        }
    }

}
