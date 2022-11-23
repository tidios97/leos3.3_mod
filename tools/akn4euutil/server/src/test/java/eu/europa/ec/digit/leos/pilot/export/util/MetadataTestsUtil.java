package eu.europa.ec.digit.leos.pilot.export.util;

import eu.europa.ec.digit.leos.pilot.export.model.metadata.fieldInfo.*;
import eu.europa.ec.digit.leos.pilot.export.model.metadata.MetadataLanguageDateFormat;
import eu.europa.ec.digit.leos.pilot.export.model.metadata.MetadataLocationType;
import eu.europa.ec.digit.leos.pilot.export.util.XmlUtil.XmlFile;
import org.springframework.util.Assert;
import org.w3c.dom.Node;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

public final class MetadataTestsUtil {

    public static class MetadataTestConfiguration {
        private MetadataLocationType locationTypeToTest;
        private MetadataLanguageDateFormat dateFormatTypeToTest;

        public MetadataTestConfiguration(MetadataLocationType locationTypeToTest,
                                         MetadataLanguageDateFormat dateFormatTypeToTest){
            this.locationTypeToTest = locationTypeToTest;
            this.dateFormatTypeToTest = dateFormatTypeToTest;
        }

        public MetadataLocationType getLocationTypeToTest() {
            return locationTypeToTest;
        }

        public void setLocationTypeToTest(MetadataLocationType locationTypeToTest) {
            this.locationTypeToTest = locationTypeToTest;
        }

        public MetadataLanguageDateFormat getDateFormatTypeToTest() {
            return dateFormatTypeToTest;
        }

        public void setDateFormatTypeToTest(MetadataLanguageDateFormat dateFormatTypeToTest) {
            this.dateFormatTypeToTest = dateFormatTypeToTest;
        }

        public ReferenceFieldInfo getLocationTypeToTestAsFieldInfo() {
            switch (this.locationTypeToTest) {
                case LUXEMBOURG:
                    return MetadataUtil.getFieldInfoLocationLuxembourg();
                case STRASBOURG:
                    return MetadataUtil.getFieldInfoLocationStrasbourg();
                default:
                    return MetadataUtil.getFieldInfoLocationBrussels();
            }
        }

        public static MetadataTestConfiguration getDefault() {
            return new MetadataTestConfiguration(MetadataLocationType.BRUSSELS, MetadataLanguageDateFormat.EN);
        }

        public static MetadataTestConfiguration withLocationTypeToTest(MetadataLocationType locationTypeToTest) {
            MetadataTestConfiguration configuration = MetadataTestConfiguration.getDefault();
            configuration.setLocationTypeToTest(locationTypeToTest);
            return configuration;
        }

        public static MetadataTestConfiguration withDateFormatTypeToTest(MetadataLanguageDateFormat dateFormatTypeToTest) {
            MetadataTestConfiguration configuration = MetadataTestConfiguration.getDefault();
            configuration.setDateFormatTypeToTest(dateFormatTypeToTest);
            return configuration;
        }
    }

    public static void checkMetadataResponse(byte[] response) throws Exception {
        checkMetadataResponse(response, null);
    }

    public static void checkMetadataResponse(byte[] response, MetadataTestConfiguration configuration) throws Exception {
        Assert.notNull(response, "Metadata response is null");
        Map<String, Object> responseContent = ZipUtil.unzipByteArray(response);

        if (configuration == null) {
            configuration = MetadataTestConfiguration.getDefault();
        }

        Assert.isTrue(responseContent.containsKey("content.xml"), "content.xml is null");
        XmlFile contentXml = bytesToXmlFile((byte[]) responseContent.get("content.xml"), "content.xml");
        checkMetadataResponseXml(contentXml);

        String legFileName = readContentXmlDocumentName(contentXml);
        Assert.isTrue(responseContent.containsKey(legFileName), "Leg file is null");
        checkProcessedLegFile((byte[]) responseContent.get(legFileName), configuration);
    }

    private static void checkMetadataResponseXml(XmlFile xmlFile) throws Exception {
        Node rootNode = xmlFile.getRootNode();
        Assert.notNull(rootNode, "content.xml root node is null");

        Node statusNode =  XmlUtil.getChildNodeWithName(rootNode, "status");
        Assert.notNull(statusNode, "content.xml status node is null");
        Assert.isTrue("0".equals(XmlUtil.getNodeAttributeValue(statusNode, "code")), "content.xml status code is not 0");

        Node taskNode =  XmlUtil.getChildNodeWithName(rootNode, "task");
        Assert.notNull(taskNode, "content.xml task node is null");

        Node documentNode =  XmlUtil.getChildNodeWithName(rootNode, "document");
        Assert.notNull(documentNode, "content.xml document node is null");

        Node validationResultNode = XmlUtil.getChildNodeWithName(taskNode, "validationResult");
        Assert.notNull(validationResultNode, "content.xml validation result is null");
        Assert.isTrue("0".equals(XmlUtil.getNodeAttributeValue(validationResultNode, "statusCode")), "content.xml validation result status code is not 0");

        Node actionNode = XmlUtil.getChildNodeWithName(taskNode, "action");
        Assert.notNull(actionNode, "content.xml action node is null");

        List<Node> fieldNodes = XmlUtil.getChildNodesWithName(actionNode, "field");
        Assert.notEmpty(fieldNodes, "content.xml field nodes are empty");

        for (Node fieldNode : fieldNodes) {
            Assert.notNull(XmlUtil.getNodeAttributeValue(fieldNode, "key"), "content.xmlvalidation field node key is null");
            Assert.isTrue("0".equals(XmlUtil.getNodeAttributeValue(fieldNode, "statusCode")), "content.xml field node status code is not 0");
            Assert.isTrue("Inserted".equals(fieldNode.getTextContent()), "content.xmlvalidation field node calue is not 'Inserted'");
        }
    }

    private static void checkProcessedLegFile(byte[] legFile, MetadataTestConfiguration configuration) throws Exception {
        Map<String, Object> legFileContent = ZipUtil.unzipByteArray(legFile);
        Assert.isTrue(!legFileContent.isEmpty(), "Leg file content is empty");
        String[] legFileContentNames = legFileContent.keySet().toArray(new String[0]);

        for (String contentName : legFileContentNames) {
            if (MetadataUtil.isDocumentXmlFile(contentName)) {
                checkProcessedXml((byte[]) legFileContent.get(contentName), contentName, configuration);
            }
        }
    }

    private static void checkProcessedXml(byte [] xmlData, String filename, MetadataTestConfiguration configuration) throws Exception {
        XmlFile xmlFile = bytesToXmlFile(xmlData, filename);
        checkAdoptionLocation(xmlFile, configuration);
        checkEmissionDate(xmlFile, configuration);
        checkInsertCote(xmlFile);
        checkInterinstitutionalCote(xmlFile);
        checkLinkedDocuments(xmlFile);
    }

    private static void checkAdoptionLocation(XmlFile xmlFile, MetadataTestConfiguration configuration) throws Exception {
        Node xmlNodeMeta = MetadataUtil.getXmlNodeMetaReference(xmlFile, "TLCLocation");
        if (xmlNodeMeta != null) {
            checkNodeAttributeValue(xmlNodeMeta, "xml:id",
                      configuration.getLocationTypeToTestAsFieldInfo().getId());
            checkNodeAttributeValue(xmlNodeMeta, "href",
                    configuration.getLocationTypeToTestAsFieldInfo().getHref());
            checkNodeAttributeValue(xmlNodeMeta, "showAs",
                    configuration.getLocationTypeToTestAsFieldInfo().getDisplayValue());
        }

        Node xmlNodeCoverpage = xmlFile.getElementByName("coverPage");
        Node xmlNodeMainDoc = XmlUtil.getXmlChildNodeWithNameAttributeValue(xmlNodeCoverpage, "mainDoc");
        Node xmlNodeBlock = XmlUtil.getXmlChildNodeWithNameAttributeValue(xmlNodeMainDoc, "placeAndDate");
        Node xmlNodeLocation = XmlUtil.getChildNodeWithName(xmlNodeBlock, "location");

        if (xmlNodeLocation != null) {
            checkNodeWithRefersToAttribute(xmlNodeLocation,
                    "~" + configuration.getLocationTypeToTestAsFieldInfo().getId(),
                    configuration.getLocationTypeToTestAsFieldInfo().getDisplayValue());
        }

        Node xmlNodeConclusions = xmlFile.getElementByName("conclusions");
        Node xmlNodeConclusionsP = XmlUtil.getXmlChildNodeWithXmlIdAttributeValue(xmlNodeConclusions, "conclusions__p_1");
        xmlNodeLocation = XmlUtil.getChildNodeWithName(xmlNodeConclusionsP, "location");
        if (xmlNodeLocation != null) {
            checkNodeWithRefersToAttribute(xmlNodeLocation,
                    "~" + configuration.getLocationTypeToTestAsFieldInfo().getId(),
                    configuration.getLocationTypeToTestAsFieldInfo().getDisplayValue());
        }
    }

    private static void checkEmissionDate(XmlFile xmlFile, MetadataTestConfiguration configuration) throws Exception {
        Node xmlNodeCoverpage = xmlFile.getElementByName("coverPage");
        Node xmlNodeMainDoc = XmlUtil.getXmlChildNodeWithNameAttributeValue(xmlNodeCoverpage, "mainDoc");
        Node xmlNodeBlock = XmlUtil.getXmlChildNodeWithNameAttributeValue(xmlNodeMainDoc, "placeAndDate");
        Node xmlNodeDate = XmlUtil.getChildNodeWithName(xmlNodeBlock, "date");
        String expectedDisplayValue = MetadataUtil.convertIsoDateToLanguageDateFormat("2012-09-28",
                configuration.getDateFormatTypeToTest().getIso639_2t());
        if (xmlNodeDate != null) {
            checkDateNode(xmlNodeDate, "2012-09-28", expectedDisplayValue);
        }

        Node xmlNodeConclusions = xmlFile.getElementByName("conclusions");
        Node xmlNodeConclusionsP = XmlUtil.getXmlChildNodeWithXmlIdAttributeValue(xmlNodeConclusions, "conclusions__p_1");
        xmlNodeDate = XmlUtil.getChildNodeWithName(xmlNodeConclusionsP, "date");
        if (xmlNodeDate != null) {
            checkDateNode(xmlNodeDate, "2012-09-28", expectedDisplayValue);
        }
    }

    private static void checkDateNode(Node nodeDate, String expectedDateValue, String expectedTextContent) throws Exception {
        checkNodeAttributeValue(nodeDate, "date", expectedDateValue);
        checkNodeTextContent(nodeDate, expectedTextContent);
    }

    private static void checkInsertCote(XmlFile xmlFile) throws Exception {
        Node xmlNodeMeta = MetadataUtil.getXmlNodeMetaReferenceWithNameAttributeValue(xmlFile, "TLCReference", "identifier");
        if (xmlNodeMeta != null) {
            Assert.notNull(XmlUtil.getNodeAttributeValue(xmlNodeMeta, "xml:id"));
            checkNodeAttributeValue(xmlNodeMeta, "href",
                    "http://publications.europa.eu/resource/authority/identifier/COMnumber");
            checkNodeAttributeValue(xmlNodeMeta, "showAs", "COM(2012) 466");
            checkNodeAttributeValue(xmlNodeMeta, "shortForm", "COM/2012/466");
        }

        Node xmlNodeCoverpage = xmlFile.getElementByName("coverPage");
        Node xmlNodeMainDoc = XmlUtil.getXmlChildNodeWithNameAttributeValue(xmlNodeCoverpage, "mainDoc");
        Node xmlNodeBlock = XmlUtil.getXmlChildNodeWithNameAttributeValue(xmlNodeMainDoc, "reference");
        Node xmlNodeDocNumber = XmlUtil.getChildNodeWithName(xmlNodeBlock, "docNumber");
        if (xmlNodeDocNumber != null) {
            Assert.notNull(XmlUtil.getNodeAttributeValue(xmlNodeDocNumber, "refersTo"));
            checkNodeTextContent(xmlNodeDocNumber, "COM(2012) 466");
        }
    }

    private static void checkInterinstitutionalCote(XmlFile xmlFile) throws Exception {
        Node xmlNodeMeta = MetadataUtil.getXmlNodeMetaReferenceWithNameAttributeValue(xmlFile, "TLCReference", "procedureReference");
        if (xmlNodeMeta != null) {
            checkNodeAttributeValue(xmlNodeMeta, "xml:id", "_procedure_2012_227");
            checkNodeAttributeValue(xmlNodeMeta, "href", "http://eur-lex.europa.eu/procedure/EN/2012_227");
            checkNodeAttributeValue(xmlNodeMeta, "showAs", "2012/0227 (COD)");
            checkNodeAttributeValue(xmlNodeMeta, "shortForm", "2012/227/COD");
        }

        Node xmlNodeCoverpage = xmlFile.getElementByName("coverPage");
        Node xmlNodeContainer = XmlUtil.getXmlChildNodeWithNameAttributeValue(xmlNodeCoverpage, "procedureIdentifier");
        Node xmlNodeDocketNumber = MetadataUtil.getXmlNodeDocketNumber(xmlNodeContainer);
        if (xmlNodeDocketNumber != null) {
            checkNodeWithRefersToAttribute(xmlNodeDocketNumber, "_procedure_2012_227", "2012/0227 (COD)");
        }

        if (xmlFile.getName().startsWith("bill")) {
            Node xmlNodePreface = xmlFile.getElementByName("preface");
            xmlNodeContainer = XmlUtil.getXmlChildNodeWithNameAttributeValue(xmlNodePreface, "procedureIdentifier");
            if (xmlNodeContainer != null) {
                checkNodeWithRefersToAttribute(xmlNodeDocketNumber, "_procedure_2012_227", "2012/0227 (COD)");
            }
        }
    }

    private static void checkLinkedDocuments(XmlFile xmlFile) throws Exception {
        Node xmlNodeCoverpage = xmlFile.getElementByName("coverPage");
        Node xmlNodeAssociatedReferences = XmlUtil.getXmlChildNodeWithNameAttributeValue(xmlNodeCoverpage, "associatedReferences");

        if (xmlNodeAssociatedReferences == null) {
            return;
        }

        List<Node> pNodes = XmlUtil.getChildNodesWithName(xmlNodeAssociatedReferences, "p");
        Assert.isTrue((pNodes.size() == 3), "Associated references node count is not as expected");

        Node refNode = XmlUtil.getChildNodeWithName(pNodes.get(0), "ref");
        checkNodeTextContent(refNode, "SEC(2021) 11 final");
        checkNodeAttributeValue(refNode, "href", "http://data.europa.eu/eli/swd/2021/11");

        refNode = XmlUtil.getChildNodeWithName(pNodes.get(1), "ref");
        checkNodeTextContent(refNode, "SWD(2021) 42 final");
        checkNodeAttributeValue(refNode, "href", "http://data.europa.eu/eli/swd/2021/42");

        refNode = XmlUtil.getChildNodeWithName(pNodes.get(2), "ref");
        checkNodeTextContent(refNode, "SWD(2021) 43 final");
        checkNodeAttributeValue(refNode, "href", "http://data.europa.eu/eli/swd/2021/43");
    }

    private static void checkNodeWithRefersToAttribute(Node nodeLocation, String expectedRefersToValue, String expectedTextContentValue) throws Exception  {
        checkNodeAttributeValue(nodeLocation, "refersTo", expectedRefersToValue);
        checkNodeTextContent(nodeLocation, expectedTextContentValue);
    }

    private static void checkNodeAttributeValue(Node node, String attributeName, String expectedAttributeValue) throws Exception {
        final String attributeValue = XmlUtil.getNodeAttributeValue(node, attributeName);
        Assert.isTrue(expectedAttributeValue.equals(attributeValue),
                String.format("'%s' attribute value '%s' of '%s' node is not '%s'", node.getNodeName(),
                        attributeValue, attributeName, expectedAttributeValue));
    }

    private static void checkNodeTextContent(Node node, String expectedTextContent) throws Exception {
        Assert.isTrue(expectedTextContent.equals(node.getTextContent()),
                String.format("'%s' node value '%s' is not '%s'", node.getNodeName(), node.getTextContent(),
                        expectedTextContent));
    }

    private static String readContentXmlDocumentName(XmlFile xmlFile) throws Exception {
        Node rootNode = xmlFile.getRootNode();
        Node documentNode = XmlUtil.getChildNodeWithName(rootNode, "document");
        String documentName = XmlUtil.getNodeAttributeValue(documentNode, "filename");
        Assert.notNull(documentName, "content.xml document name is null");
        return documentName;
    }

    private static XmlFile bytesToXmlFile(byte[] bytes, String name) throws Exception {
        InputStream inputStream = new ByteArrayInputStream(bytes);
        return XmlUtil.parseXml(inputStream, name);
    }
}