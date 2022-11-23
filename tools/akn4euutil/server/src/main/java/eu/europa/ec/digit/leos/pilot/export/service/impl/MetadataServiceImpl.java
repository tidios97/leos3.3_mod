/*
 * Copyright 2021-2022 European Commission
 *
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 *     https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and limitations under the Licence.
 */
package eu.europa.ec.digit.leos.pilot.export.service.impl;

import eu.europa.ec.digit.leos.pilot.export.exception.MetadataServiceException;
import eu.europa.ec.digit.leos.pilot.export.exception.MetadataUtilsException;
import eu.europa.ec.digit.leos.pilot.export.exception.XmlUtilException;
import eu.europa.ec.digit.leos.pilot.export.exception.XmlValidationException;
import eu.europa.ec.digit.leos.pilot.export.model.ApplyMetadataRequest;
import eu.europa.ec.digit.leos.pilot.export.model.ApplyMetadataResponse;
import eu.europa.ec.digit.leos.pilot.export.model.metadata.fieldInfo.MetadataFieldInfo;
import eu.europa.ec.digit.leos.pilot.export.model.metadata.fieldInfo.MultipleReferencesFieldInfo;
import eu.europa.ec.digit.leos.pilot.export.model.metadata.fieldInfo.ReferenceFieldInfo;
import eu.europa.ec.digit.leos.pilot.export.service.MetadataService;
import eu.europa.ec.digit.leos.pilot.export.util.MetadataUtil;
import eu.europa.ec.digit.leos.pilot.export.util.XmlUtil;
import eu.europa.ec.digit.leos.pilot.export.util.XmlUtil.XmlFile;
import eu.europa.ec.digit.leos.pilot.export.util.ZipUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.xml.sax.SAXException;

import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Validator;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
class MetadataServiceImpl implements MetadataService {
    private static final Logger LOG = LoggerFactory.getLogger(MetadataServiceImpl.class);

    public byte[] applyMetadata(MultipartFile inputFile) {
        ApplyMetadataRequest request = null;

        try {
            LOG.debug("Start applying meta data ...");
            Map<String, Object> zipContent = ZipUtil.unzipByteArray(inputFile.getBytes());
            request = readContentXml(zipContent);

            Map<String, Object> documentZipContent = readAndUnzipDocument(zipContent, request.getDocument());
            List<XmlFile> documentXmlFiles = readDocumentXmlFiles(documentZipContent);
            Map<String, Object> documentFurtherContent = readFurtherDocumentContent(documentZipContent);

            validateDocumentXmlFiles(documentXmlFiles);
            ApplyMetadataResponse response = processApplyMetadataRequest(request, documentXmlFiles);
            XmlFile xmlResponse = MetadataUtil.akn4euResponseToXmlFile(response);
            return buildResponse(response, documentXmlFiles, documentFurtherContent);
        }
        catch(XmlValidationException ex) {
            LOG.error("One or more xml files do not match the xml schema", ex);
            return buildXmlValidationErrorResponse(request);
        }
        catch(Exception ex){
            LOG.error("Error applying metadata", ex);
            return buildErrorResponse(request);
        }
    }

    private ApplyMetadataRequest readContentXml(Map<String, Object> zipContent) throws MetadataServiceException {
        byte[] contentXmlData = objectToByteArray(zipContent.get("content.xml"));

        if (contentXmlData == null || contentXmlData.length == 0) {
            throw new MetadataServiceException("content.xml not found");
        }

        try {
            InputStream xmlInputStream = new ByteArrayInputStream(contentXmlData);
            XmlFile contentXmlFile = XmlUtil.parseXml(xmlInputStream, "content.xml");
            ApplyMetadataRequest request = MetadataUtil.xmlFileToApplyMetadataRequest(contentXmlFile);
            closeInputStream(xmlInputStream);
            return request;
        } catch (XmlUtilException e) {
            throw new MetadataServiceException("Error unzip document", e);
        }
    }

    private Map<String, Object> readAndUnzipDocument(Map<String, Object> zipContent, ApplyMetadataRequest.DocumentNode document) throws MetadataServiceException {
        if (document == null) {
            throw new MetadataServiceException("Document not found");
        }

        byte[] documentZipData = objectToByteArray(zipContent.get(document.getFilename()));
        if (documentZipData == null || documentZipData.length == 0) {
            throw new MetadataServiceException("Document file not found");
        }

        try {
            Map<String, Object> documentZipContent = ZipUtil.unzipByteArray(documentZipData);
            return documentZipContent;
        } catch (IOException e) {
            throw new MetadataServiceException("Error unzip document", e);
        }
    }

    private List<XmlFile> readDocumentXmlFiles(Map<String, Object> documentZipContent){
        List<XmlFile> xmlDocuments = new ArrayList<>();
        String[] contentNames = (String[]) documentZipContent.keySet().toArray(new String[0]);

        for (String contentName : contentNames) {
            if (MetadataUtil.isDocumentXmlFile(contentName)) {
                try {
                    byte[] documentZipBytes = objectToByteArray(documentZipContent.get(contentName));
                    xmlDocuments.add(XmlUtil.parseXml(new ByteArrayInputStream(documentZipBytes), contentName));
                } catch(Exception e){
                    LOG.error("Error parsing xml document", e);
                }
            }
        }

        return xmlDocuments;
    }

    private Map<String, Object> readFurtherDocumentContent(Map<String, Object> documentZipContent){
        Map<String, Object> furtherContent = new HashMap<>();
        String[] contentNames = (String[]) documentZipContent.keySet().toArray(new String[0]);

        for (String contentName : contentNames) {
            if (!MetadataUtil.isDocumentXmlFile(contentName)) {
                try {
                    furtherContent.put(contentName, documentZipContent.get(contentName));
                } catch(Exception e){
                    LOG.error("Error parsing xml document", e);
                }
            }
        }

        return furtherContent;
    }

    private void validateDocumentXmlFiles(List<XmlFile> xmlFiles) throws XmlValidationException {
        Validator schemaValidator = XmlUtil.getAknSchemaValidator();
        for (XmlFile xmlFile : xmlFiles) {
            ByteArrayInputStream inputStream = null;
            try {
                inputStream = new ByteArrayInputStream(xmlFile.getBytes());
                schemaValidator.validate(new StreamSource(inputStream));
            } catch (XmlUtilException ex) {
                LOG.error("Error reading xml file", ex);
                throw new XmlValidationException("Error reading xml file", ex);
            } catch (IOException | SAXException ex) {
                LOG.error("Error validate xml file '" + xmlFile.getName() + "'", ex);
                throw new XmlValidationException("Error validate xml file '" + xmlFile.getName() + "'", ex);
            } finally {
                closeInputStream(inputStream);
            }
        }
    }

    private ApplyMetadataResponse processApplyMetadataRequest(ApplyMetadataRequest request, List<XmlFile> documentXmlFiles) {
        List<ApplyMetadataResponse.TaskNode> taskResponses = new ArrayList<>();
        if (request.getTasks() != null) {
            for (ApplyMetadataRequest.TaskNode task : request.getTasks()) {
                taskResponses.add(processApplyMetadataRequestTask(task, documentXmlFiles));
            }
        }

        final ApplyMetadataResponse.StatusNode successResult = isContainsTaskResponseWithErrors(taskResponses)
                ? MetadataUtil.getErrorStatusResult() : MetadataUtil.getSuccessStatusResult();
        return new ApplyMetadataResponse(request.getRequestId(),
                MetadataUtil.applyMetadataRequestDocumentToResultDocument(request.getDocument()),
                taskResponses, successResult);
    }

    private ApplyMetadataResponse.TaskNode processApplyMetadataRequestTask(ApplyMetadataRequest.TaskNode task, List<XmlFile> documentXmlFiles) {
        List<ApplyMetadataResponse.ActionNode> actionResponses = new ArrayList<>();
        for (ApplyMetadataRequest.ActionNode action : task.getActions()){
            actionResponses.add(processApplyMetadataRequestAction(action, documentXmlFiles));
        }

        final String statusCode = isContainsActionResponseWithErrors(actionResponses) ? "1" : "0";
        return new ApplyMetadataResponse.TaskNode(task.getTaskId(), statusCode, actionResponses,
                MetadataUtil.getValidationSuccessResult("XMLValidationCheck"));
    }

    private ApplyMetadataResponse.ActionNode processApplyMetadataRequestAction(ApplyMetadataRequest.ActionNode action, List<XmlFile> documentXmlFiles){
        List<ApplyMetadataResponse.FieldNode> fieldResponses = new ArrayList<>();
        for (ApplyMetadataRequest.FieldNode field : action.getFields()){
            fieldResponses.add(processApplyMetadataRequestField(field, documentXmlFiles));
        }
        return new ApplyMetadataResponse.ActionNode(action.getName(), fieldResponses);
    }

    private ApplyMetadataResponse.FieldNode processApplyMetadataRequestField(ApplyMetadataRequest.FieldNode field, List<XmlFile> documentXmlFiles){
        try {
            MetadataFieldInfo fieldInfo = MetadataUtil.lookupFieldInfo(field);
            processMetadataFieldInfo(fieldInfo, documentXmlFiles);
            return MetadataUtil.getLookupFieldInfoSuccessResult(field);
        } catch(MetadataUtilsException e) {
            LOG.error("Lookup field info failed: {}", e);
            return MetadataUtil.getLookupFieldInfoErrorResult(field);
        }
    }

    private void processMetadataFieldInfo(MetadataFieldInfo fieldInfo, List<XmlFile> documentXmlFiles){
        LOG.debug("Process field info  '{}'", fieldInfo);
        for (XmlFile xmlFile : documentXmlFiles){
            LOG.debug("Process xml file '{}'", xmlFile.getName());
            switch(fieldInfo.getFieldType()){
                case ADOPTION_LOCATION:
                    MetadataUtil.processAdoptionLocation((ReferenceFieldInfo)fieldInfo, xmlFile);
                    break;
                case EMISSION_DATE:
                    MetadataUtil.processEmissionDate((ReferenceFieldInfo)fieldInfo, xmlFile);
                    break;
                case INTERINSTITUTIONAL_COTE:
                    MetadataUtil.processInterinstitutionalCote((ReferenceFieldInfo)fieldInfo, xmlFile);
                    break;
                case INSERT_COTE:
                    MetadataUtil.processInsertCote((ReferenceFieldInfo)fieldInfo, xmlFile);
                    break;
                case LINKED_DOCUMENTS:
                    MetadataUtil.processLinkedDocuments((MultipleReferencesFieldInfo)fieldInfo, xmlFile);
                    break;
            }
        }
    }

    private boolean isContainsActionResponseWithErrors(List<ApplyMetadataResponse.ActionNode> actions){
        return (actions != null) &&  (actions.stream()
                .filter(action -> isContainsFieldResponseWithErrors(action.getFields())).count() > 0);
    }

    private boolean isContainsFieldResponseWithErrors(List<ApplyMetadataResponse.FieldNode> fields){
        return (fields != null) && (fields.stream().filter(field -> field.getStatusCode().equals("1")).count() > 0);
    }

    private boolean isContainsTaskResponseWithErrors(List<ApplyMetadataResponse.TaskNode> tasks){
        return (tasks != null) &&  (tasks.stream().filter(task -> task.getStatusCode().equals("1")).count() > 0);
    }

    private byte[] buildResponse(ApplyMetadataResponse response, List<XmlFile> documentXmlFiles, Map<String, Object> documentFurtherContent){
        try {
            Map<String, Object> responseContent = new HashMap<>();
            XmlFile xmlResponse = MetadataUtil.akn4euResponseToXmlFile(response);
            responseContent.put(xmlResponse.getName(), xmlResponse.getBytes());
            responseContent.put(response.getDocument().getFilename(), buildResponseLegFile(documentXmlFiles, documentFurtherContent));

            return ZipUtil.zipByteArray(responseContent);
        } catch(Exception e) {
            LOG.error("Error building response {}", e);
            return null;
        }
    }

    private byte[] buildResponseLegFile(List<XmlFile> xmlDocuments, Map<String, Object> documentFurtherContent){
        try {
            Map<String, Object> legFileContent = new HashMap<>();

            for (XmlFile xmlDocument : xmlDocuments) {
                legFileContent.put(xmlDocument.getName(), xmlDocument.getBytes());
            }
            legFileContent.putAll(documentFurtherContent);

            return ZipUtil.zipByteArray(legFileContent);
        } catch(Exception e) {
            LOG.error("Error building response leg file {}", e);
            return new byte[0];
        }
    }

    private byte[] buildXmlValidationErrorResponse(ApplyMetadataRequest request) {
        if (request.getTasks() != null) {
            try {
                ApplyMetadataResponse response = MetadataUtil.getApplyMetadataResponseWithXmlValidationError(request);
                Map<String, Object> responseContent = new HashMap<>();
                XmlFile xmlResponse = MetadataUtil.akn4euResponseToXmlFile(response);
                responseContent.put(xmlResponse.getName(), xmlResponse.getBytes());
                return ZipUtil.zipByteArray(responseContent);
            } catch(Exception e) {
                LOG.error("Error building xml validation error response {}", e);
            }
        }
        return buildErrorResponse(request);
    }

    private byte[] buildErrorResponse(ApplyMetadataRequest request){
        try {
            ApplyMetadataResponse response = MetadataUtil.getApplyMetadataResponseWithErrorStatus(request);
            Map<String, Object> responseContent = new HashMap<>();
            XmlFile xmlResponse = MetadataUtil.akn4euResponseToXmlFile(response);
            responseContent.put(xmlResponse.getName(), xmlResponse.getBytes());
            return ZipUtil.zipByteArray(responseContent);
        } catch(Exception e) {
            LOG.error("Error building error response {}", e);
            return new byte[0];
        }
    }

    private void closeInputStream(InputStream inputStream) {
        try {
            if (inputStream != null) {
                inputStream.close();
            }
        } catch(Exception e){
            LOG.error("Error closing Stream", e);
        }
    }

    private byte[] objectToByteArray(Object obj){
        if (obj != null && obj instanceof byte[]) {
            return (byte[]) obj;
        }
        return null;
    }

}