package eu.europa.ec.digit.leos.pilot.export.util;

import eu.europa.ec.digit.leos.pilot.export.exception.MetadataUtilsException;
import eu.europa.ec.digit.leos.pilot.export.exception.XmlUtilException;
import eu.europa.ec.digit.leos.pilot.export.model.metadata.*;
import eu.europa.ec.digit.leos.pilot.export.model.metadata.fieldInfo.*;
import eu.europa.ec.digit.leos.pilot.export.model.ApplyMetadataRequest;
import eu.europa.ec.digit.leos.pilot.export.model.ApplyMetadataResponse;
import eu.europa.ec.digit.leos.pilot.export.util.XmlUtil.XmlFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

public class MetadataUtil {

    private static final Logger LOG = LoggerFactory.getLogger(MetadataUtil.class);

    private static final ReferenceFieldInfo LOCATION_BRUSSELS_FIELD_INFO = new ReferenceFieldInfo("_BEL_BRU",
            "http://publications.europa.eu/resource/authority/place/BEL_BRU",
            "Brussels",
            "", MetadataFieldType.ADOPTION_LOCATION);

    private static final ReferenceFieldInfo LOCATION_LUXEMBOURG_FIELD_INFO = new ReferenceFieldInfo("_LUX_LUX",
            "http://publications.europa.eu/resource/authority/place/LUX_LUX",
            "Luxembourg",
            "", MetadataFieldType.ADOPTION_LOCATION);

    private static final ReferenceFieldInfo LOCATION_STRASBOURG_FIELD_INFO = new ReferenceFieldInfo("_FRA_SXB",
            "http://publications.europa.eu/resource/authority/place/FRA_SXB",
            "Strasbourg",
            "", MetadataFieldType.ADOPTION_LOCATION);

    private static final String EMISSION_DATE_PARSE_PATTERN = "yyyy-MM-dd";

    private static final String INSERT_COTE_PARSE_PATTERN = "([A-Za-z0-9]+)\\(([0-9]{4})\\) ([0-9]+)";

    private static final String INSERT_COTE_HREF = "http://publications.europa.eu/resource/authority/identifier/COMnumber";

    private static final String INSERT_COTE_SHORT_VALUE_PATTERN = "%s/%s/%s";

    private static final String INTERINSTITUTIONAL_COTE_PARSE_PATTERN = "([0-9]{4})/([0-9]+) \\(([A-Za-z0-9]+)\\)";

    private static final String INTERINSTITUTIONAL_COTE_ID_PATTERN = "_procedure_%s_%s";

    private static final String INTERINSTITUTIONAL_COTE_HREF_PATTERN = "http://eur-lex.europa.eu/procedure/EN/%s_%s";

    private static final String INTERINSTITUTIONAL_COTE_SHORT_VALUE_PATTERN = "%s/%s/%s";

    private static final String LINKED_DOCUMENT_HREF_PATTERN = "http://data.europa.eu/eli/%s/%s/%s";

    private static final String CONCLUSIONS = "conclusions";
    private static final String CONCLUSIONSNEW = "_" + CONCLUSIONS;
    private static final String CONCLUSION_NODE_ID = "conclusions__p_1";
    private static final String CONCLUSION_NODE_IDNEW = "_" + CONCLUSION_NODE_ID;

    private static final String STATUS_CODE="statusCode";
    private static final String KEY="key";
    private static final String DOCUMENT="document";
    private static final String TASK="task";
    private static final String TASKID="taskID";
    private static final String NAME="name";
    private static final String CLEANUP="cleanup";
    private static final String FIELD="field";
    private static final String VERSION="version";
    private static final String MIMETYPE="mimeType";
    private static final String FILENAME="filename";
    private static final String SOURCEURL="sourceURL";
    private static final String DOCUMENTID="documentId";
    private static final String ACTION="action";
    private static final String DATE="date";

    private static final String ONE="1";
    private static final String ZERO="0";
    private static final String FIELD_NOT_SUPPORTED_MESSAGE="Field not supported";
    private static final String LOCATION_NOT_SUPPORTED_MESSAGE="Location not supported";
    private static final String INVALID_ISO_DATE_MESSAGE="Invalid iso date";
    private static final String INVALID_FIELD_VALUE_MESSAGE="Invalid field value";
    
    private static final String XMLID="xml:id";
    private static final String HREF="href";
    private static final String SHOWAS="showAs";
    private static final String SHORTFORM="shortForm";
    private static final String COVERPAGE="coverPage";
    private static final String REFERSTO="refersTo";
    
    public static ReferenceFieldInfo getFieldInfoLocationBrussels(){
        return LOCATION_BRUSSELS_FIELD_INFO;
    }

    public static ReferenceFieldInfo getFieldInfoLocationLuxembourg(){ return LOCATION_LUXEMBOURG_FIELD_INFO; }

    public static ReferenceFieldInfo getFieldInfoLocationStrasbourg(){ return LOCATION_STRASBOURG_FIELD_INFO; }

    public static boolean isDocumentXmlFile(final String filename) {
        final String lowerCaseFilename = filename.toLowerCase();
        if (!lowerCaseFilename.endsWith(".xml")) {
            return false;
        }
        return MetadataUtil.isDocumentXmlFilename(lowerCaseFilename);
    }

    public static boolean isDocumentXmlFilename(final String filename) {
        if (filename.startsWith("annex")) {
            return true;
        }
        if (filename.startsWith("bill")) {
            return true;
        }
        if (filename.startsWith("expl_memorandum")) {
            return true;
        }
        if (filename.startsWith("main")) {
            return true;
        }
        if (filename.startsWith("memorandum")) {
            return true;
        }
        if (filename.startsWith("reg")) {
            return true;
        }
        return false;
    }

    public static XmlFile akn4euResponseToXmlFile(ApplyMetadataResponse response) throws MetadataUtilsException {
        try {
            XmlFile xmlFile = XmlUtil.newXmlFile();
            xmlFile.setName("content.xml");
            Element rootElement = createApplyMetadataResponseXmlRootElement(xmlFile, response);
            rootElement.appendChild(createApplyMetadataResponseXmlStatusNode(xmlFile, response.getStatus()));
            rootElement.appendChild(createApplyMetadataResponseXmlDocumentNode(xmlFile, response.getDocument()));

            if (response.getTasks() != null){
                for (ApplyMetadataResponse.TaskNode task : response.getTasks()) {
                    rootElement.appendChild(createApplyMetadataResponseXmlTaskNode(xmlFile, task));
                }
            }

            return xmlFile;
        } catch(XmlUtilException e){
            throw new MetadataUtilsException("Error converting ApplyMetadataResponse to xml");
        }
    }

    private static Element createApplyMetadataResponseXmlRootElement(XmlFile xmlFile, ApplyMetadataResponse response) {
        Element rootElement = xmlFile.createRoot("akn4euResponse");
        rootElement.setAttribute("responseId", response.getResponseId());
        rootElement.setAttribute(VERSION, response.getVersion());
        rootElement.setAttribute("xmlns", response.getXmlns());
        return rootElement;
    }

    private static Element createApplyMetadataResponseXmlStatusNode(XmlFile xmlFile, ApplyMetadataResponse.StatusNode status) {

        Element statusNode = xmlFile.newElement("status");
        if (status != null){
            statusNode.setAttribute("code", status.getCode());
            statusNode.setTextContent(status.getValue());
        }

        return statusNode;
    }

    private static Element createApplyMetadataResponseXmlDocumentNode(XmlFile xmlFile, ApplyMetadataResponse.DocumentNode document) {

        Element documentNode = xmlFile.newElement(DOCUMENT);
        if (document != null){
            documentNode.setAttribute(DOCUMENTID, document.getDocumentId());
            documentNode.setAttribute(MIMETYPE, document.getMimeType());
            documentNode.setAttribute(FILENAME, document.getFilename());
            documentNode.setAttribute(SOURCEURL, document.getSourceURL());
        }

        return documentNode;
    }

    private static Element createApplyMetadataResponseXmlTaskNode(XmlFile xmlFile, ApplyMetadataResponse.TaskNode task) {
        Element taskNode = xmlFile.newElement(TASK);
        taskNode.setAttribute(TASKID, task.getTaskId());
        taskNode.setAttribute(STATUS_CODE, task.getStatusCode());
        taskNode.appendChild(createApplyMetadataResponseXmlValidationResultNode(xmlFile, task.getValidationResult()));

        if (task.getActions() != null){
            for (ApplyMetadataResponse.ActionNode action : task.getActions()){
                taskNode.appendChild(createApplyMetadataResponseXmlActionNode(xmlFile, action));
            }
        }

        return taskNode;
    }

    private static Element createApplyMetadataResponseXmlValidationResultNode(XmlFile xmlFile, ApplyMetadataResponse.ValidationResultNode validationResult) {
        Element validationResultNode = xmlFile.newElement("validationResult");
        validationResultNode.setAttribute(KEY, validationResult.getKey());
        validationResultNode.setAttribute(STATUS_CODE, validationResult.getStatusCode());

        return validationResultNode;
    }

    private static Element createApplyMetadataResponseXmlActionNode(XmlFile xmlFile, ApplyMetadataResponse.ActionNode action) {
        Element actionNode = xmlFile.newElement(ACTION);
        actionNode.setAttribute(NAME, action.getName());

        if (action.getFields() != null){
            for (ApplyMetadataResponse.FieldNode field : action.getFields()) {
                actionNode.appendChild(createApplyMetadataResponseXmlFieldNode(xmlFile, field));
            }
        }

        return actionNode;
    }

    private static Element createApplyMetadataResponseXmlFieldNode(XmlFile xmlFile, ApplyMetadataResponse.FieldNode field) {
        Element fieldNode = xmlFile.newElement(FIELD);
        fieldNode.setAttribute(KEY, field.getKey());
        fieldNode.setAttribute(STATUS_CODE, field.getStatusCode());
        fieldNode.setTextContent(field.getValue());

        return fieldNode;
    }

    public static ApplyMetadataRequest xmlFileToApplyMetadataRequest(XmlFile xmlFile) {
        final Node nodeRequest = xmlFile.getElementByName("akn4euRequest");
        return processApplyMetadataRequestNode(nodeRequest);
    }

    public static ApplyMetadataRequest processApplyMetadataRequestNode(Node nodeRequest) {
        if (nodeRequest != null){
            Node nodeDocument = XmlUtil.getChildNodeWithName(nodeRequest, DOCUMENT);
            ApplyMetadataRequest.DocumentNode requestDocument = parseApplyMetadataRequestDocumentNode(nodeDocument);

            List<Node> taskNodes = XmlUtil.getChildNodesWithName(nodeRequest, TASK);
            List<ApplyMetadataRequest.TaskNode> requestTasks = processApplyMetadataRequestTaskNodes(taskNodes);

            ApplyMetadataRequest applyMetadataRequest = parseApplyMetadataRequestNode(nodeRequest);
            applyMetadataRequest.setDocument(requestDocument);
            applyMetadataRequest.setTasks(requestTasks);
            return applyMetadataRequest;
        }
        return null;
    }

    public static ApplyMetadataRequest.DocumentNode parseApplyMetadataRequestDocumentNode(Node nodeDocument) {
        if (nodeDocument != null){
            return new ApplyMetadataRequest.DocumentNode(XmlUtil.getNodeAttributeValue(nodeDocument, SOURCEURL),
                    XmlUtil.getNodeAttributeValue(nodeDocument, FILENAME),
                    XmlUtil.getNodeAttributeValue(nodeDocument, MIMETYPE),
                    XmlUtil.getNodeAttributeValue(nodeDocument, DOCUMENTID));
        }
        return null;
    }

    public static List<ApplyMetadataRequest.TaskNode> processApplyMetadataRequestTaskNodes(List<Node> taskNodes) {
        if (taskNodes != null){
            List<ApplyMetadataRequest.TaskNode> tasks = new ArrayList<>();
            for (int i = 0; i < taskNodes.size(); i++){
                Node nodeTask = taskNodes.get(i);
                ApplyMetadataRequest.TaskNode akn4euTask = parseApplyMetadataRequestTaskNode(nodeTask);
                akn4euTask.setActions(processApplyMetadataRequestTaskNode(nodeTask));
                tasks.add(akn4euTask);
            }
            return tasks;
        }
        return null;
    }

    public static ApplyMetadataRequest.TaskNode parseApplyMetadataRequestTaskNode(Node nodeTask) {
        if (nodeTask != null) {
            return new ApplyMetadataRequest.TaskNode(XmlUtil.getNodeAttributeValue(nodeTask, TASKID));
        }
        return null;
    }

    public static List<ApplyMetadataRequest.ActionNode> processApplyMetadataRequestTaskNode(Node nodeTask) {
        if (nodeTask != null){
            List<ApplyMetadataRequest.ActionNode> actions = new ArrayList<>();
            List<Node> actionNodes = XmlUtil.getChildNodesWithName(nodeTask, ACTION);
            for (int i = 0; i < actionNodes.size(); i++){
                Node nodeAction = actionNodes.get(i);
                ApplyMetadataRequest.ActionNode akn4euAction = parseApplyMetadataRequestActionNode(nodeAction);
                akn4euAction.setFields(processApplyMetadataRequestActionNode(nodeAction));
                actions.add(akn4euAction);
            }
            return actions;
        }
        return null;
    }

    public static ApplyMetadataRequest.ActionNode parseApplyMetadataRequestActionNode(Node nodeAction) {
        if (nodeAction != null){
            return new ApplyMetadataRequest.ActionNode(XmlUtil.getNodeAttributeValue(nodeAction, NAME),
                    XmlUtil.getNodeAttributeValue(nodeAction, CLEANUP));
        }
        return null;
    }

    public static List<ApplyMetadataRequest.FieldNode> processApplyMetadataRequestActionNode(Node nodeAction) {
        if (nodeAction != null){
            List<ApplyMetadataRequest.FieldNode> fields = new ArrayList<>();
            List<Node> fieldNodes = XmlUtil.getChildNodesWithName(nodeAction, FIELD);
            for (int i = 0; i < fieldNodes.size(); i++){
                Node nodeField = fieldNodes.get(i);
                ApplyMetadataRequest.FieldNode akn4euField = parseApplyMetadataRequestFieldNode(nodeField);
                fields.add(akn4euField);
            }
            return fields;
        }
        return null;
    }

    public static ApplyMetadataRequest.FieldNode parseApplyMetadataRequestFieldNode(Node nodeField) {
        if (nodeField != null){
            return new ApplyMetadataRequest.FieldNode(XmlUtil.getNodeAttributeValue(nodeField, KEY),
                    nodeField.getTextContent());
        }
        return null;
    }

    public static ApplyMetadataRequest parseApplyMetadataRequestNode(Node nodeRequest) {
        if (nodeRequest != null){
            return new ApplyMetadataRequest(XmlUtil.getNodeAttributeValue(nodeRequest, "xmlns"),
                    XmlUtil.getNodeAttributeValue(nodeRequest, VERSION),
                    XmlUtil.getNodeAttributeValue(nodeRequest, DATE),
                    XmlUtil.getNodeAttributeValue(nodeRequest, "requestId"));
        }
        return null;
    }

    public static ApplyMetadataResponse getApplyMetadataResponseWithErrorStatus(ApplyMetadataRequest request) {
        return new ApplyMetadataResponse(
                request != null ? request.getRequestId() : "",
                request != null ? applyMetadataRequestDocumentToResultDocument(request.getDocument()) : null,
                null, getErrorStatusResult());
    }

    public static ApplyMetadataResponse getApplyMetadataResponseWithXmlValidationError(ApplyMetadataRequest request) {
        if (request.getTasks() != null) {
            List<ApplyMetadataResponse.TaskNode> responseTasks = new ArrayList<>();
            for (ApplyMetadataRequest.TaskNode task : request.getTasks()) {
                responseTasks.add(getApplyMetadataResponseTaskWithXmlValidationError(task));
            }

            return new ApplyMetadataResponse(
                    request != null ? request.getRequestId() : "",
                    request != null ? applyMetadataRequestDocumentToResultDocument(request.getDocument()) : null,
                    responseTasks, getErrorStatusResult());
        }
        return getApplyMetadataResponseWithErrorStatus(request);
    }

    public static ApplyMetadataResponse.DocumentNode applyMetadataRequestDocumentToResultDocument(ApplyMetadataRequest.DocumentNode document) {
        return new ApplyMetadataResponse.DocumentNode(document.getSourceURL(), document.getFilename(), document.getMimeType(),
                document.getDocumentId());
    }

    public static ApplyMetadataResponse.TaskNode getApplyMetadataResponseTaskWithXmlValidationError(ApplyMetadataRequest.TaskNode requestTask) {
        List<ApplyMetadataResponse.ActionNode> responseTaskActions = new ArrayList<>();
        String taskId = null;

        if (requestTask != null) {
            taskId = requestTask.getTaskId();
            if (requestTask.getActions() != null) {
                for (ApplyMetadataRequest.ActionNode requestAction : requestTask.getActions()) {
                    responseTaskActions.add(getApplyMetadataResponseActionError(requestAction));
                }
            }
        }

        return new ApplyMetadataResponse.TaskNode(taskId, ONE, responseTaskActions,
                getValidationErrorResult(MetadataValidationResultKey.XML_VALIDATION_CHECK.getKey()));
    }

    private static ApplyMetadataResponse.ActionNode getApplyMetadataResponseActionError(ApplyMetadataRequest.ActionNode requestAction) {
        List<ApplyMetadataResponse.FieldNode> responseActionFields = new ArrayList<>();
        if (requestAction.getFields() != null) {
            for (ApplyMetadataRequest.FieldNode requestField : requestAction.getFields()) {
                responseActionFields.add(new ApplyMetadataResponse.FieldNode(requestField.getKey(), ONE, "XML validation error"));
            }
        }
        return new ApplyMetadataResponse.ActionNode(MetadataActionName.INSERT_DATA.getValue(), responseActionFields);
    }

    public static ApplyMetadataResponse.StatusNode getSuccessStatusResult() {
        return new ApplyMetadataResponse.StatusNode(ZERO, "Success");
    }

    public static ApplyMetadataResponse.StatusNode getErrorStatusResult() {
        return new ApplyMetadataResponse.StatusNode(ONE, "Failure");
    }

    public static ApplyMetadataResponse.ValidationResultNode getValidationSuccessResult(String key) {
        return new ApplyMetadataResponse.ValidationResultNode(key, ZERO);
    }

    public static ApplyMetadataResponse.ValidationResultNode getValidationErrorResult(String key) {
        return new ApplyMetadataResponse.ValidationResultNode(key, ONE);
    }

    public static ApplyMetadataResponse.FieldNode getLookupFieldInfoErrorResult(ApplyMetadataRequest.FieldNode field) {
        return new ApplyMetadataResponse.FieldNode(field.getKey(), ONE,
                String.format("tag not found (field=\"%s\", tag=\"%s\")", field.getKey(), FIELD));
    }

    public static ApplyMetadataResponse.FieldNode getLookupFieldInfoSuccessResult(ApplyMetadataRequest.FieldNode field) {
        return new ApplyMetadataResponse.FieldNode(field.getKey(), ZERO, "Inserted");
    }
    public static MetadataFieldInfo lookupFieldInfo(ApplyMetadataRequest.FieldNode field) throws MetadataUtilsException {
        return lookupFieldInfo(field.getKey(), field.getValue());
    }

    public static MetadataFieldInfo lookupFieldInfo(String field, String fieldValue) throws MetadataUtilsException {
        try {
            MetadataFieldType fieldType = MetadataFieldType.valueOfTypeName(field);
            switch(fieldType){
                case ADOPTION_LOCATION:
                    return parseAdoptionLocation(fieldValue);
                case EMISSION_DATE:
                    return parseEmissionDate(fieldValue);
                case INTERINSTITUTIONAL_COTE:
                    return parseInterinstitutionalCote(fieldValue);
                case INSERT_COTE:
                    return parseInsertCote(fieldValue);
                case LINKED_DOCUMENTS:
                    return parseLinkedDocuments(fieldValue);
                default:
                    throw new MetadataUtilsException(FIELD_NOT_SUPPORTED_MESSAGE);
            }
        } catch (IllegalArgumentException e){
            throw new MetadataUtilsException(FIELD_NOT_SUPPORTED_MESSAGE);
        } catch (MetadataUtilsException mue){
            throw mue;
        }
    }

    public static MetadataFieldInfo parseAdoptionLocation(String fieldValue) throws MetadataUtilsException {
        try {
            MetadataLocationType locationType = MetadataLocationType.valueOfLocation(fieldValue.toUpperCase());

            switch (locationType){
                case BRUSSELS:
                    return LOCATION_BRUSSELS_FIELD_INFO;
                case LUXEMBOURG:
                    return LOCATION_LUXEMBOURG_FIELD_INFO;
                case STRASBOURG:
                    return LOCATION_STRASBOURG_FIELD_INFO;
                default:
                    throw new MetadataUtilsException(LOCATION_NOT_SUPPORTED_MESSAGE);
            }
        } catch (IllegalArgumentException e){
            throw new MetadataUtilsException(LOCATION_NOT_SUPPORTED_MESSAGE);
        }
    }

    public static MetadataFieldInfo parseEmissionDate(String fieldValue) throws MetadataUtilsException {
        Date parsedDate = stringToDate(fieldValue, EMISSION_DATE_PARSE_PATTERN);
        if (parsedDate == null) {
            throw new MetadataUtilsException(INVALID_ISO_DATE_MESSAGE);
        };

        return new ReferenceFieldInfo(fieldValue, "", "", "", MetadataFieldType.EMISSION_DATE);
    }

    private static Date stringToDate(String strDate, String format) {
        try {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format);
            return simpleDateFormat.parse(strDate);
        } catch(Exception e) {
            return null;
        }
    }

    public static MetadataFieldInfo parseInterinstitutionalCote(String fieldValue) throws MetadataUtilsException {
        if (!fieldValue.matches(INTERINSTITUTIONAL_COTE_PARSE_PATTERN)){
            throw new MetadataUtilsException(INVALID_FIELD_VALUE_MESSAGE);
        }

        int slashIndex = fieldValue.indexOf("/");
        int bracketIndex = fieldValue.indexOf("(");

        String year = fieldValue.substring(0, slashIndex);
        String number = fieldValue.substring(slashIndex+1, bracketIndex-1).trim();
        String strippedNumber = removeTrailingZeros(number);
        String type = fieldValue.substring(bracketIndex+1, fieldValue.indexOf(")"));

        String id = String.format(INTERINSTITUTIONAL_COTE_ID_PATTERN, year, strippedNumber);
        String href = String.format(INTERINSTITUTIONAL_COTE_HREF_PATTERN, year, strippedNumber);
        String shortValue = String.format(INTERINSTITUTIONAL_COTE_SHORT_VALUE_PATTERN, year, strippedNumber, type);

        return new ReferenceFieldInfo(id, href, fieldValue, shortValue, MetadataFieldType.INTERINSTITUTIONAL_COTE);
    }

    private static String removeTrailingZeros(String value) {
        while (value.startsWith(ZERO)){
            value = value.substring(1);
        }
        return value;
    }

    public static MetadataFieldInfo parseInsertCote(String fieldValue) throws MetadataUtilsException {
        if (!fieldValue.matches(INSERT_COTE_PARSE_PATTERN)){
            throw new MetadataUtilsException(INVALID_FIELD_VALUE_MESSAGE);
        }

        int bracketIndex = fieldValue.indexOf("(");
        int closingBracketIndex = fieldValue.indexOf(")");

        String type = fieldValue.substring(0, bracketIndex);
        String year = fieldValue.substring(bracketIndex+1, closingBracketIndex);
        String number = fieldValue.substring(closingBracketIndex+1).trim();

        String id = "_" + generateRandomIdentifier(5);
        String shortValue = String.format(INSERT_COTE_SHORT_VALUE_PATTERN, type, year, number);

        return new ReferenceFieldInfo(id, INSERT_COTE_HREF, fieldValue, shortValue, MetadataFieldType.INSERT_COTE);
    }

    private static String generateRandomIdentifier(int length) {
        final String characters = "abcdefghijklmnopqrstuvwxyz";
        final String numbersAndCharacters =  "0123456789" + characters;
        String randomIdentifier = "";

        if (length > 0){
            randomIdentifier += getRandomCharsFromString(characters, 1);
            randomIdentifier += getRandomCharsFromString(numbersAndCharacters, length-1);
        }

        return randomIdentifier;
    }

    private static String getRandomCharsFromString(String str, int count) {
        String result = "";

        if (count > 0) {
            Random random = new Random();
            for (int i = 0; i < count; i++) {
                int randomIndex = random.nextInt(str.length()-1);
                result += str.charAt(randomIndex);
            }
        }

        return result;
    }

    public static MetadataFieldInfo parseLinkedDocuments(String fieldValue) throws MetadataUtilsException {
        String[] references = fieldValue.split("-");
        List<ReferenceFieldInfo> referenceFieldInfoList = new ArrayList<>();

        for (final String reference : references) {
            final ReferenceFieldInfo fieldInfo = parseLinkedDocumentInfo(reference);
            referenceFieldInfoList.add(fieldInfo);
        }

        return new MultipleReferencesFieldInfo(referenceFieldInfoList, MetadataFieldType.LINKED_DOCUMENTS);
    }

    private static ReferenceFieldInfo parseLinkedDocumentInfo(final String referenceValue) {
        final String displayValue = referenceValue.replace("{", "").replace("}", "").trim();

        int bracketIndex = displayValue.indexOf("(");
        final String abbreviation = displayValue.substring(0, bracketIndex).trim();

        int closingBracketIndex = displayValue.indexOf(")");
        final String year = displayValue.substring(bracketIndex+1, closingBracketIndex).trim();

        int wordPos = displayValue.indexOf("final");
        if(wordPos < 0) wordPos = displayValue.lastIndexOf("draft");
        if(wordPos < 0) wordPos = displayValue.length();
        final String number = displayValue.substring(closingBracketIndex+1, wordPos).trim();
        final String href = String.format(LINKED_DOCUMENT_HREF_PATTERN, 
                abbreviation.toLowerCase().replace("sec",  "swd"), // SEC documents are published under SWD 
                year, number);

        return new ReferenceFieldInfo("", href, displayValue, "", MetadataFieldType.LINKED_DOCUMENTS);
    }


    public static void processAdoptionLocation(ReferenceFieldInfo fieldInfo, XmlFile xmlFile) {
        MetadataUtil.addAdoptionLocationToMetaReference(fieldInfo, xmlFile);
        MetadataUtil.addAdoptionLocationToCoverPage(fieldInfo, xmlFile);
        MetadataUtil.addAdoptionLocationToConclusion(fieldInfo, xmlFile);
    }

    private static void addAdoptionLocationToMetaReference(ReferenceFieldInfo fieldInfo, XmlFile xmlFile) {
        Node xmlNodeMeta = MetadataUtil.getXmlNodeMetaReference(xmlFile, "TLCLocation");
        if (xmlNodeMeta == null) {
            return;
        }
        XmlUtil.setNodeAttributeValue(xmlNodeMeta, XMLID, fieldInfo.getId());
        XmlUtil.setNodeAttributeValue(xmlNodeMeta, HREF, fieldInfo.getHref());
        XmlUtil.setNodeAttributeValue(xmlNodeMeta, SHOWAS, fieldInfo.getDisplayValue());
    }

    public static Node getXmlNodeMetaReference(XmlFile xmlFile, String referenceNodeName) {
        Node xmlNodeMetaReference = null;

        Node xmlNode = xmlFile.getElementByName(referenceNodeName);
        if (isMetaReferenceXmlNode(xmlNode)) {
            xmlNodeMetaReference = xmlNode;
        }

        return xmlNodeMetaReference;
    }

    private static void addAdoptionLocationToCoverPage(ReferenceFieldInfo fieldInfo, XmlFile xmlFile) {
        Node xmlNodeCoverpage = xmlFile.getElementByName(COVERPAGE);
        if (xmlNodeCoverpage == null) {
            return;
        }

        Node xmlNodeMainDoc = XmlUtil.getXmlChildNodeWithNameAttributeValue(xmlNodeCoverpage, "mainDoc");
        if (xmlNodeMainDoc == null) {
            return;
        }

        Node xmlNodeBlock = XmlUtil.getXmlChildNodeWithNameAttributeValue(xmlNodeMainDoc, "placeAndDate");
        if (xmlNodeBlock == null) {
            return;
        }

        Node xmlNodeLocation = XmlUtil.getChildNodeWithName(xmlNodeBlock, "location");
        if (xmlNodeLocation == null) {
            return;
        }
        XmlUtil.setNodeAttributeValue(xmlNodeLocation, REFERSTO, "~" + fieldInfo.getId());
        xmlNodeLocation.setTextContent(fieldInfo.getDisplayValue());
    }

    private static void addAdoptionLocationToConclusion(ReferenceFieldInfo fieldInfo, XmlFile xmlFile) {
        
        Node xmlNodeConclusions = xmlFile.getElementByName(CONCLUSIONSNEW);
        if (xmlNodeConclusions == null) {
            xmlNodeConclusions = xmlFile.getElementByName(CONCLUSIONS);
        }
        if (xmlNodeConclusions == null) {
            return;
        }

        Node xmlNodeConclusionsP = XmlUtil.getXmlChildNodeWithXmlIdAttributeValue(xmlNodeConclusions, CONCLUSION_NODE_IDNEW);
        if (xmlNodeConclusionsP == null) {
            xmlNodeConclusionsP = XmlUtil.getXmlChildNodeWithXmlIdAttributeValue(xmlNodeConclusions, CONCLUSION_NODE_ID);
        }
        if (xmlNodeConclusionsP == null) {
            return;
        }

        Node xmlNodeLocation = XmlUtil.getChildNodeWithName(xmlNodeConclusionsP, "location");
        if (xmlNodeLocation == null) {
            return;
        }
        XmlUtil.setNodeAttributeValue(xmlNodeLocation, REFERSTO, "~" + fieldInfo.getId());
        xmlNodeLocation.setTextContent(fieldInfo.getDisplayValue());
    }

    public static void processEmissionDate(ReferenceFieldInfo fieldInfo, XmlFile xmlFile) {
        MetadataUtil.addEmissionDateToCoverPage(fieldInfo, xmlFile);
        MetadataUtil.addEmissionDateToConclusion(fieldInfo, xmlFile);
    }

    private static void addEmissionDateToCoverPage(ReferenceFieldInfo fieldInfo, XmlFile xmlFile) {
        Node xmlNodeCoverpage = xmlFile.getElementByName(COVERPAGE);
        if (xmlNodeCoverpage == null) {
            return;
        }

        Node xmlNodeMainDoc = XmlUtil.getXmlChildNodeWithNameAttributeValue(xmlNodeCoverpage, "mainDoc");
        if (xmlNodeMainDoc == null) { 
            return;
        }        
        
        Node xmlNodeBlock = XmlUtil.getXmlChildNodeWithNameAttributeValue(xmlNodeMainDoc, "placeAndDate");
        if (xmlNodeBlock == null) {
            return;
        }
        
        Node xmlNodeDate = XmlUtil.getChildNodeWithName(xmlNodeBlock, DATE);
        if (xmlNodeDate == null) {
            return;
        }
        XmlUtil.setNodeAttributeValue(xmlNodeDate, DATE, fieldInfo.getId());

        String displayValue = MetadataUtil.readEmissionDataDisplayValue(fieldInfo, xmlFile);
        xmlNodeDate.setTextContent(displayValue);
    }

    public static String convertIsoDateToLanguageDateFormat(String isoDate, String iso6392tCode) {
        Date parsedDate = stringToDate(isoDate, EMISSION_DATE_PARSE_PATTERN);
        MetadataLanguageDateFormat languageDateFormat = convertIso6392tCodeToMetadataLanguageDateFormat(iso6392tCode);
        return dateToString(parsedDate, languageDateFormat.getFormat());
    }

    public static MetadataLanguageDateFormat convertIso6392tCodeToMetadataLanguageDateFormat(String iso6392tCode) {
        try {
            return MetadataLanguageDateFormat.ofIso639_2T(iso6392tCode);
        } catch(IllegalArgumentException ex) {
            return MetadataLanguageDateFormat.EN;
        }
    }

    private static String dateToString(Date date, String format) {
        try {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format);
            return simpleDateFormat.format(date);
        } catch(Exception e) {
            return "";
        }
    }

    private static String parseAlpha3CountryCode(Node nodeLanguageReference) {
        if (nodeLanguageReference == null) { return null; }
        String hrefAttributeValue = XmlUtil.getNodeAttributeValue(nodeLanguageReference, HREF);
        return (hrefAttributeValue != null && hrefAttributeValue.length() >= 3)
                ? hrefAttributeValue.substring(hrefAttributeValue.length() - 3) : null;
    }

    private static void addEmissionDateToConclusion(ReferenceFieldInfo fieldInfo, XmlFile xmlFile) {
        
        Node xmlNodeConclusions = xmlFile.getElementByName(CONCLUSIONSNEW);
        if (xmlNodeConclusions == null) {
            xmlNodeConclusions = xmlFile.getElementByName(CONCLUSIONS);
        }
        if (xmlNodeConclusions == null) {
            return;
        }

        Node xmlNodeConclusionsP = XmlUtil.getXmlChildNodeWithXmlIdAttributeValue(xmlNodeConclusions, CONCLUSION_NODE_IDNEW);
        if (xmlNodeConclusionsP == null) {
            xmlNodeConclusionsP = XmlUtil.getXmlChildNodeWithXmlIdAttributeValue(xmlNodeConclusions, CONCLUSION_NODE_ID);
        }
        if (xmlNodeConclusionsP == null) {
            return;
        }

        Node xmlNodeDate = XmlUtil.getChildNodeWithName(xmlNodeConclusionsP, DATE);
        if (xmlNodeDate == null) {
            return;
        }

        XmlUtil.setNodeAttributeValue(xmlNodeDate, DATE, fieldInfo.getId());
        String displayValue = MetadataUtil.readEmissionDataDisplayValue(fieldInfo, xmlFile);
        xmlNodeDate.setTextContent(displayValue);
    }


    private static String readEmissionDataDisplayValue(ReferenceFieldInfo fieldInfo, XmlFile xmlFile) {
        Node xmlNodeLanguageReference = MetadataUtil.getLanguageReferenceNode(xmlFile);
        if (xmlNodeLanguageReference == null) {
            return "";
        }
        String displayValue = convertIsoDateToLanguageDateFormat(fieldInfo.getId(), parseAlpha3CountryCode(xmlNodeLanguageReference));
        return displayValue;
    }

    private static Node getLanguageReferenceNode(XmlFile xmlFile) {
        Node xmlNodeReferences = xmlFile.getElementByName("references");
        if (xmlNodeReferences == null) {
            return null;
        }

        Node xmlNodeLanguageReference = XmlUtil.getXmlChildNodeWithNameAttributeValue(xmlNodeReferences, "language");
        return xmlNodeLanguageReference;
    }
    public static void processInsertCote(ReferenceFieldInfo fieldInfo, XmlFile xmlFile) {
        MetadataUtil.addInsertCoteToMetaReference(fieldInfo, xmlFile);
        MetadataUtil.addInsertCoteToCoverPage(fieldInfo, xmlFile);
    }

    public static void addInsertCoteToMetaReference(ReferenceFieldInfo fieldInfo, XmlFile xmlFile) {
        Node xmlNodeMeta = MetadataUtil.getXmlNodeMetaReferenceWithNameAttributeValue(xmlFile, "TLCReference", "identifier");
        if (xmlNodeMeta == null) {
            return;
        }
        XmlUtil.setNodeAttributeValue(xmlNodeMeta, XMLID, fieldInfo.getId());
        XmlUtil.setNodeAttributeValue(xmlNodeMeta, HREF, fieldInfo.getHref());
        XmlUtil.setNodeAttributeValue(xmlNodeMeta, SHOWAS, fieldInfo.getDisplayValue());
        XmlUtil.setNodeAttributeValue(xmlNodeMeta, SHORTFORM, fieldInfo.getShortValue());
    }

    public static void addInsertCoteToCoverPage(ReferenceFieldInfo fieldInfo, XmlFile xmlFile) {
        Node xmlNodeCoverpage = xmlFile.getElementByName(COVERPAGE);
        if (xmlNodeCoverpage == null) {
            return;
        }

        Node xmlNodeMainDoc = XmlUtil.getXmlChildNodeWithNameAttributeValue(xmlNodeCoverpage, "mainDoc");
        if (xmlNodeMainDoc == null) {
            return;
        }

        Node xmlNodeBlock = XmlUtil.getXmlChildNodeWithNameAttributeValue(xmlNodeMainDoc, "reference");
        if (xmlNodeBlock == null) {
            return;
        }

        Node xmlNodeDocNumber = XmlUtil.getChildNodeWithName(xmlNodeBlock, "docNumber");
        if (xmlNodeDocNumber == null) {
            return;
        }
        XmlUtil.setNodeAttributeValue(xmlNodeDocNumber, REFERSTO, fieldInfo.getId());
        xmlNodeDocNumber.setTextContent(fieldInfo.getDisplayValue());
    }

    public static void processInterinstitutionalCote(ReferenceFieldInfo fieldInfo, XmlFile xmlFile) {
        MetadataUtil.addInterinstitutionalCoteToMetaReference(fieldInfo, xmlFile);
        MetadataUtil.addInterinstitutionalCoteToCoverPage(fieldInfo, xmlFile);
        MetadataUtil.addInterinstitutionalCoteToPreface(fieldInfo, xmlFile);
    }

    private static void addInterinstitutionalCoteToMetaReference(ReferenceFieldInfo fieldInfo, XmlFile xmlFile) {
        Node xmlNodeMeta = getXmlNodeMetaReferenceWithNameAttributeValue(xmlFile, "TLCReference", "procedureReference");
        if (xmlNodeMeta == null) {
            return;
        }
        XmlUtil.setNodeAttributeValue(xmlNodeMeta, XMLID, fieldInfo.getId());
        XmlUtil.setNodeAttributeValue(xmlNodeMeta, HREF, fieldInfo.getHref());
        XmlUtil.setNodeAttributeValue(xmlNodeMeta, SHOWAS, fieldInfo.getDisplayValue());
        XmlUtil.setNodeAttributeValue(xmlNodeMeta, SHORTFORM, fieldInfo.getShortValue());
    }

    private static void addInterinstitutionalCoteToCoverPage(ReferenceFieldInfo fieldInfo, XmlFile xmlFile) {
        Node xmlNodeCoverpage = xmlFile.getElementByName(COVERPAGE);
        if (xmlNodeCoverpage == null) {
            return;
        }
        MetadataUtil.addInterinstitutionalCoteToDocketNumber(fieldInfo, xmlNodeCoverpage);
    }

    private static void addInterinstitutionalCoteToPreface(ReferenceFieldInfo fieldInfo, XmlFile xmlFile) {
        Node xmlNodePreface = xmlFile.getElementByName("preface");
        if (xmlNodePreface == null) {
            return;
        }
        MetadataUtil.addInterinstitutionalCoteToDocketNumber(fieldInfo, xmlNodePreface);
    }

    public static void addInterinstitutionalCoteToDocketNumber(ReferenceFieldInfo fieldInfo, Node xmlParentNode) {
        Node xmlNodeContainer = XmlUtil.getXmlChildNodeWithNameAttributeValue(xmlParentNode, "procedureIdentifier");
        if (xmlNodeContainer == null) {
            return;
        }

        Node xmlNodeDocketNumber = getXmlNodeDocketNumber(xmlNodeContainer);
        if (xmlNodeDocketNumber == null) {
            return;
        }

        XmlUtil.setNodeAttributeValue(xmlNodeDocketNumber, REFERSTO, fieldInfo.getId());
        xmlNodeDocketNumber.setTextContent(fieldInfo.getDisplayValue());
    }

    public static Node getXmlNodeDocketNumber(Node xmlNode) {
        Node xmlNodeDocketNumber = null;
        if (xmlNode != null) {
            Node xmlNodeP = XmlUtil.getChildNodeWithName(xmlNode, "p");
            if (xmlNodeP != null) {
                xmlNodeDocketNumber = XmlUtil.getChildNodeWithName(xmlNodeP, "docketNumber");
            }
        }
        return xmlNodeDocketNumber;
    }

    public static void processLinkedDocuments(MultipleReferencesFieldInfo fieldInfo, XmlFile xmlFile) {
        Node xmlNodeCoverpage = xmlFile.getElementByName(COVERPAGE);
        Node xmlNodeAssociatedReferences = XmlUtil.getXmlChildNodeWithNameAttributeValue(xmlNodeCoverpage, "associatedReferences");

        if (xmlNodeAssociatedReferences == null) {
            return;
        }
        
        // remove any existing content
        if(xmlNodeAssociatedReferences.hasChildNodes()) {
            final NodeList children = xmlNodeAssociatedReferences.getChildNodes();
            for(int i = children.getLength() - 1; i >= 0; i--)
                xmlNodeAssociatedReferences.removeChild(children.item(i));
        }
        
        for (final ReferenceFieldInfo reference : fieldInfo.getReferences()) {
            final Element referenceElement = createLinkedDocumentElement(reference, xmlFile);
            xmlNodeAssociatedReferences.appendChild(referenceElement);
        }
    }

    private static Element createLinkedDocumentElement(final ReferenceFieldInfo reference, XmlFile xmlFile) {

        final Element referenceElement = xmlFile.newElement("p");
        final Element refElement = xmlFile.newElement("ref");
        
        refElement.setTextContent(reference.getDisplayValue());
        refElement.setAttribute(HREF, reference.getHref());
        
        referenceElement.appendChild(xmlFile.createTextNode("{"));
        referenceElement.appendChild(refElement);
        referenceElement.appendChild(xmlFile.createTextNode("}"));
        return referenceElement;
    }

    public static Node getXmlNodeMetaReferenceWithNameAttributeValue(XmlFile xmlFile, String referenceNodeName, String nameAttributeValue) {
        Node xmlNodeReferenceProcedureReference = null;

        int index = 0;
        NodeList xmlNodesReferences = xmlFile.getElementsByName(referenceNodeName);
        while(index < xmlNodesReferences.getLength() && xmlNodeReferenceProcedureReference == null) {
            Node xmlNodeReference = xmlNodesReferences.item(index);
            if (isMetaReferenceXmlNode(xmlNodeReference)
                    && (XmlUtil.nodeAttributeValueEquals(xmlNodeReference, NAME, nameAttributeValue))) {
                xmlNodeReferenceProcedureReference = xmlNodeReference;
            }
            index++;
        }

        return xmlNodeReferenceProcedureReference;
    }

    private static boolean isMetaReferenceXmlNode(Node xmlNode) {
        return XmlUtil.parentNodeNameEquals(xmlNode, "references")
                && XmlUtil.parentNodeNameEquals(xmlNode.getParentNode(), "meta");
    }
}