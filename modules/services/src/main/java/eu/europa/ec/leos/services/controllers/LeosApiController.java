/*
 * Copyright 2019 European Commission
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

package eu.europa.ec.leos.services.controllers;

import com.google.common.eventbus.EventBus;
import eu.europa.ec.leos.domain.cmis.LeosLegStatus;
import eu.europa.ec.leos.domain.cmis.document.ExportDocument;
import eu.europa.ec.leos.domain.cmis.document.LegDocument;
import eu.europa.ec.leos.domain.cmis.document.Proposal;
import eu.europa.ec.leos.domain.cmis.document.XmlDocument;
import eu.europa.ec.leos.domain.common.Result;
import eu.europa.ec.leos.model.event.MilestoneUpdatedEvent;
import eu.europa.ec.leos.model.user.Collaborator;
import eu.europa.ec.leos.security.AuthClient;
import eu.europa.ec.leos.security.TokenService;
import eu.europa.ec.leos.services.api.ApiService;
import eu.europa.ec.leos.services.collection.CreateCollectionResult;
import eu.europa.ec.leos.services.collection.CreateCollectionService;
import eu.europa.ec.leos.services.compare.ContentComparatorContext;
import eu.europa.ec.leos.services.compare.ContentComparatorService;
import eu.europa.ec.leos.services.document.TransformationService;
import eu.europa.ec.leos.services.export.ExportLW;
import eu.europa.ec.leos.services.export.ExportOptions;
import eu.europa.ec.leos.services.export.ExportService;
import eu.europa.ec.leos.services.store.ExportPackageService;
import eu.europa.ec.leos.services.store.LegService;
import eu.europa.ec.leos.services.store.WorkspaceService;
import eu.europa.ec.leos.vo.token.JsonTokenReponse;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

import static eu.europa.ec.leos.services.compare.ContentComparatorService.ATTR_NAME;
import static eu.europa.ec.leos.services.compare.ContentComparatorService.CONTENT_ADDED_CLASS;
import static eu.europa.ec.leos.services.compare.ContentComparatorService.CONTENT_REMOVED_CLASS;

@RestController
public class LeosApiController {

    private static final Logger LOG = LoggerFactory.getLogger(LeosApiController.class);

    private final LegService legService;
    private final WorkspaceService workspaceService;
    private final ApiService apiService;
    private final TokenService tokenService;
    private final TransformationService transformationService;
    private final ContentComparatorService comparatorService;
    private final EventBus leosApplicationEventBus;
    private final ExportService exportService;
    private final CreateCollectionService createCollectionService;
    private final Properties applicationProperties;
    private final ExportPackageService exportPackageService;

    private final int SINGLE_COLUMN_MODE = 1;
    private final int TWO_COLUMN_MODE = 2;
    private static final String GRANT_TYPE = "grant-type";
    private static final String BEARER_GRANT_TYPE = "jwt-bearer";
    private static final String BEARER_PARAMETER = "assertion";

    @Autowired
    public LeosApiController(LegService legService, WorkspaceService workspaceService, TokenService tokenService,
                             TransformationService transformationService, ContentComparatorService comparatorService,
                             EventBus leosApplicationEventBus, ExportService exportService,
                             CreateCollectionService createCollectionService, Properties applicationProperties,
                             ExportPackageService exportPackageService, ApiService apiService) {
        this.legService = legService;
        this.workspaceService = workspaceService;
        this.tokenService = tokenService;
        this.transformationService = transformationService;
        this.comparatorService = comparatorService;
        this.leosApplicationEventBus = leosApplicationEventBus;
        this.exportService = exportService;
        this.createCollectionService = createCollectionService;
        this.applicationProperties = applicationProperties;
        this.exportPackageService = exportPackageService;
        this.apiService = apiService;
    }

    @RequestMapping(value = "/token", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<Object> getToken(HttpServletRequest request, HttpServletResponse response) {
        response.setHeader("Cache-Control", "no-store");
        response.setHeader("Pragma", "no-cache");

        final String grantType = request.getHeader(GRANT_TYPE);
        if (!StringUtils.isEmpty(grantType) && grantType.contains(BEARER_GRANT_TYPE)) {
            String token = request.getHeader(BEARER_PARAMETER);
            AuthClient authClient = tokenService.validateClientByJwtToken(token);
            if (authClient.isVerified()) {
                LOG.debug("Client '{}' correctly validated with jwt-bearer token provided", authClient.getName());
                String user = tokenService.extractUserFromToken(token);
                JsonTokenReponse jsonToken = new JsonTokenReponse(tokenService.getAccessToken(user), "jwt",
                        System.currentTimeMillis() + 3600000, null, null);
                LOG.debug("Created accessToken for the Client '{}", authClient.getName());
                return new ResponseEntity<>(jsonToken, HttpStatus.OK);
            } else {
                LOG.warn("Authorization failed! A client is asking for an accessToken, but the provided '{}' token is not valid!", BEARER_GRANT_TYPE);
                return new ResponseEntity<>("Wrong jwt-bearer token!", HttpStatus.FORBIDDEN);
            }
        } else {
            LOG.warn("Authorization failed! Wrong Headers: '{}' is missing or contains a wrong value", GRANT_TYPE);
        }

        return new ResponseEntity<>("Wrong Headers!", HttpStatus.FORBIDDEN);
    }

    @RequestMapping(value = "/secured/compare", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public ResponseEntity<Object> compareContents(HttpServletRequest request, @RequestParam("mode") int mode,
            @RequestParam("firstContent") MultipartFile firstContent, @RequestParam("secondContent") MultipartFile secondContent) {
        if ((mode != SINGLE_COLUMN_MODE) && (mode != TWO_COLUMN_MODE)) {
            return new ResponseEntity<>("Mode value has to be 1(single column mode) or 2(two column mode)", HttpStatus.BAD_REQUEST);
        }
        try {
            String contextPath = UriComponentsBuilder.fromHttpRequest(new ServletServerHttpRequest(request)).build().toUriString();
            String baseContextPath = contextPath.substring(0, StringUtils.ordinalIndexOf(contextPath, "/", 4));
            String firstContentHtml = transformationService
                    .formatToHtml(new ByteArrayInputStream(firstContent.getBytes()), baseContextPath, null)
                    .replaceAll("(?i)(href|onClick)=\".*?\"", "");
            String secondContentHtml = transformationService
                    .formatToHtml(new ByteArrayInputStream(secondContent.getBytes()), baseContextPath, null)
                    .replaceAll("(?i)(href|onClick)=\".*?\"", "");
            if (mode == SINGLE_COLUMN_MODE) {
                return new ResponseEntity<>(new String[]{comparatorService.compareContents(new ContentComparatorContext.Builder(firstContentHtml, secondContentHtml)
                        .withAttrName(ATTR_NAME)
                        .withRemovedValue(CONTENT_REMOVED_CLASS)
                        .withAddedValue(CONTENT_ADDED_CLASS)
                        .build())}, HttpStatus.OK);
            }
            return new ResponseEntity<>(comparatorService.twoColumnsCompareContents(new ContentComparatorContext.Builder(firstContentHtml, secondContentHtml).build()), HttpStatus.OK);
        } catch (Exception ex) {
            LOG.error("Error occurred while comparing contents", ex.getMessage());
            return new ResponseEntity<>("Error occurred while comparing contents: ", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(value = "/secured/search/{userId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<Object> getProposalsForUser(@PathVariable("userId") String userId,
                                                      @RequestParam(value = "proposalId", defaultValue = "") String proposalId,
                                                      @RequestParam(value = "legFileStatus", defaultValue = "") String legFileStatus) {
        try {
            return new ResponseEntity<>(legService.getLegDocumentDetailsByUserId(userId, proposalId, legFileStatus).toArray(), HttpStatus.OK);
        } catch (Exception ex) {
            LOG.error("Exception occurred in search "+ ex.getMessage());
            return new ResponseEntity<>("Error Occurred while getting the Leg Document for user " + userId, HttpStatus.NOT_FOUND);
        }
    }

    @RequestMapping(value = "/secured/search/{userId}/{documentRef}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<Object> getDocumentForUser(@PathVariable("userId") String userId, @PathVariable("documentRef") String documentRef) {
        XmlDocument document = null;
        try {
            document = workspaceService.findDocumentByRef(documentRef, XmlDocument.class);
        } catch (Exception ex) {
            LOG.error("Error occurred while getting document " + documentRef + " for user " + userId + ". " + ex.getMessage());
            return new ResponseEntity<>("Error occurred while getting document " + documentRef + " for user " + userId, HttpStatus.NOT_FOUND);
        }

        Optional<Collaborator> userAsCollaborator = document.getCollaborators().stream()
                .filter(x -> x.getLogin().equalsIgnoreCase(userId)).findAny();
        if (!userAsCollaborator.isPresent()) {
            LOG.error("Error occurred while getting document " + documentRef + " for user " + userId + ". User not allowed to access the document.");
            return new ResponseEntity<>("Error occurred while getting document " + documentRef + " for user " + userId, HttpStatus.FORBIDDEN);
        }

        switch (document.getCategory()) {
            case ANNEX:
            case BILL:
            case MEMORANDUM:
            case PROPOSAL:
                String documentViewUrl = applicationProperties.getProperty("leos.mapping.url") +
                        applicationProperties.getProperty("leos.document.view." + document.getCategory().toString().toLowerCase() + ".uri");
                return new ResponseEntity<>(Collections.singletonMap("url", MessageFormat.format(documentViewUrl, documentRef)), HttpStatus.OK);
            default:
                LOG.error("Error occurred while getting document " + documentRef + " for user " + userId + ". Wrong category for document!!!");
                return new ResponseEntity<>("Error occurred while getting document " + documentRef + " for user " + userId, HttpStatus.NOT_FOUND);
        }
    }

    @RequestMapping(value = "/secured/searchlegfile/{legFileId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    @ResponseBody
    public ResponseEntity<Object> getLegFile(@PathVariable("legFileId") String legFileId) {
        boolean isStatusUpdated = false;
        LeosLegStatus currentStatus = null;
        try {
            LegDocument legDocument = legService.findLegDocumentById(legFileId);
            currentStatus = legDocument.getStatus();
            if (!(currentStatus == LeosLegStatus.IN_PREPARATION || currentStatus == LeosLegStatus.FILE_ERROR)) {
                byte[] file = legDocument.getContent().get().getSource().getBytes();
                HttpHeaders headers = new HttpHeaders();
                headers.set("Content-Disposition", "attachment; filename=" + legDocument.getName());
                headers.setContentLength(file.length);
                LegDocument updatedLegDocument = legService.updateLegDocument(legFileId, LeosLegStatus.EXPORTED);
                leosApplicationEventBus.post(new MilestoneUpdatedEvent(updatedLegDocument, true));
                isStatusUpdated = true;
                return new ResponseEntity<>(file, headers, HttpStatus.OK);
            } else {
                return new ResponseEntity<>("Leg file with Id" + legFileId + " in status " + currentStatus, HttpStatus.NOT_FOUND);
            }
        } catch (Exception ex) {
            // in case of any exception reverting to current status
            if (isStatusUpdated) {
                LegDocument updatedLegDocument = legService.updateLegDocument(legFileId, currentStatus);
                leosApplicationEventBus.post(new MilestoneUpdatedEvent(updatedLegDocument, true));
            }
            LOG.error("Exception occurred in downloading leg file "+ ex.getMessage());
            return new ResponseEntity<>("Error Occurred while sending the leg file  for Leg File Id " +
                    legFileId, HttpStatus.NOT_FOUND);
        }

    }

    @RequestMapping(value = "/secured/renditionfromleg", method = RequestMethod.POST, produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    @ResponseBody
    public ResponseEntity<Object> getPdfFromLegFile(@RequestParam("legFile") MultipartFile legFile, @RequestParam("type") String type) {
        File legFileTemp  = null;
        try {
            final ExportOptions exportOptions = new ExportLW(type);
            exportOptions.setWithAnnotations(true);

            //create a temporary file with the bytes arrived as input
            legFileTemp = File.createTempFile("tmp_", ".leg");
            FileUtils.writeByteArrayToFile(legFileTemp, legFile.getBytes());
            byte[] renditionFile = exportService.exportToToolboxCoDe(legFileTemp, exportOptions);

            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Disposition", "attachment; filename=" + "TOOLBOX_RESULT_" + System.currentTimeMillis());
            headers.setContentLength(renditionFile.length);

            LOG.info("Returning zip file of {} bytes containing renditions to the external caller." + renditionFile.length);
            return new ResponseEntity<>(renditionFile, headers, HttpStatus.OK);
        } catch (Exception e) {
            String errMsg = "Error occurred while creating rendition file: " + e.getMessage();
            LOG.error(errMsg, e);
            return new ResponseEntity<>(errMsg, HttpStatus.INTERNAL_SERVER_ERROR);
        } finally{
            if (legFileTemp != null && legFileTemp.exists()) {
                legFileTemp.delete();
            }
        }
    }

    @RequestMapping(value = "/secured/milestones/{proposalRef}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<Object> getLegFilesForProposal(@PathVariable("proposalRef") String proposalRef) {
        Proposal proposal = null;
        try {
            proposal = workspaceService.findDocumentByRef(proposalRef, Proposal.class);
        } catch (Exception ex) {
            LOG.error("Error occurred while getting proposal {}. {}", proposalRef, ex.getMessage(), ex);
            return new ResponseEntity<>("Error occurred while getting proposal " + proposalRef, HttpStatus.NOT_FOUND);
        }

        try {
            List<LegDocument> legFiles = legService.findLegDocumentByProposal(proposal.getId());
            return new ResponseEntity<>(legFiles, HttpStatus.OK);
        } catch (Exception ex) {
            LOG.error("Error occurred while getting milestones for proposal {}. {}", proposalRef , ex.getMessage(), ex);
            return new ResponseEntity<>("Error occurred while getting milestones for proposal " + proposalRef, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(value = "/secured/collectionfromleg", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<Object> createCollectionFromLeg(@RequestParam("file") MultipartFile file) {
        CreateCollectionResult createCollectionResult;
        try {
            File content = new File(applicationProperties.getProperty("leos.mandate.upload.path") + file.getOriginalFilename());

            try (FileOutputStream fos = new FileOutputStream(content)) {
                fos.write(file.getBytes());
            } catch (IOException ioe) {
                LOG.error("Error Occurred while reading the Leg file: " + ioe.getMessage(), ioe);
                return new ResponseEntity<>("An error occurred during the reading of the Leg file.", HttpStatus.INTERNAL_SERVER_ERROR);
            }

            createCollectionResult = createCollectionService.createCollectionFromLeg(content);
            return new ResponseEntity<>(createCollectionResult, HttpStatus.OK);
        } catch (Exception ex) {
            LOG.error("Error Occurred while creating collection from the Leg file: " + ex.getMessage(), ex);
            return new ResponseEntity<>("An error occurred during collection creation.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(value = "/secured/cloneProposal", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<Object> cloneProposalFromLeg(@RequestParam("file") MultipartFile legFile,
                                                       @RequestParam("targetUser") String targetUser,
                                                       @RequestParam("connectedEntity") String connectedEntity,
                                                       @RequestParam("iscRef") String iscRef) {
        CreateCollectionResult createCollectionResult;
        try {
            File content = new File(legFile.getOriginalFilename());

            try (FileOutputStream fos = new FileOutputStream(content)) {
                fos.write(legFile.getBytes());
            } catch (IOException ioe) {
                LOG.error("Error Occurred while reading the Leg file: " + ioe.getMessage(), ioe);
                return new ResponseEntity<>("An error occurred during the reading of the Leg file.", HttpStatus.INTERNAL_SERVER_ERROR);
            }

            createCollectionResult = createCollectionService.cloneCollection(content, iscRef, targetUser, connectedEntity);
            return new ResponseEntity<>(createCollectionResult, HttpStatus.OK);
        } catch (Exception ex) {
            LOG.error("Error Occurred while cloning proposal from the Leg file: " + ex.getMessage(), ex);
            return new ResponseEntity<>("An error occurred during proposal cloning.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(value = "/secured/revisionDone", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<Object> updateClonedProposalRevisionStatus(@RequestParam("cloneProposalId") String cloneProposalId,
                                                            @RequestParam("legFileName") String legFileName) {
        Result<?> result;
        try {
            result = createCollectionService.updateOriginalProposalAfterRevisionDone(cloneProposalId, legFileName);
        } catch (Exception ex) {
            LOG.error("Error Occurred while getting revision done status: " + ex.getMessage(), ex);
            return new ResponseEntity<>("Error Occurred while getting revision done status", HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<>(result.get(), HttpStatus.OK);
    }

    @RequestMapping(value = "/secured/export/{proposalRef}/{exportPackageId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    @ResponseBody
    public ResponseEntity<Object> getExportPackage(@PathVariable("proposalRef") String proposalRef, @PathVariable("exportPackageId") String exportPackageId) {
        ExportDocument exportDocument = null;
        try {
            exportDocument = exportPackageService.findExportDocumentById(exportPackageId, false);
            byte[] file = exportDocument.getContent().get().getSource().getBytes();
            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Disposition", "attachment; filename=" + exportDocument.getName());
            headers.setContentLength(file.length);
            return new ResponseEntity<>(file, headers, HttpStatus.OK);
        } catch (Exception ex) {
            LOG.error("Error occurred while retrieving export package for id " + exportPackageId + ". " + ex.getMessage());
            return new ResponseEntity<>("Error occurred while retrieving export package for id " +
                    exportPackageId, HttpStatus.NOT_FOUND);
        }
    }
}
