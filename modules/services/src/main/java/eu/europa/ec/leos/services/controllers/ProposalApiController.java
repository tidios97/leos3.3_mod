/*
 * Copyright 2022 European Commission
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

import eu.europa.ec.leos.integration.rest.UserJSON;
import eu.europa.ec.leos.services.api.ApiService;
import eu.europa.ec.leos.services.collection.CreateCollectionException;
import eu.europa.ec.leos.services.collection.CreateCollectionResult;
import eu.europa.ec.leos.services.dto.request.ExplanatoryRequest;
import eu.europa.ec.leos.services.dto.request.ExportPdfRequest;
import eu.europa.ec.leos.services.dto.request.UpdateProposalRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping(value = "/secured/proposal")
public class ProposalApiController {

    private static final Logger LOG = LoggerFactory.getLogger(ProposalApiController.class);

    private final ApiService apiService;

    @Autowired
    public ProposalApiController(ApiService apiService) {
        this.apiService = apiService;
    }

    @RequestMapping (value = "/{proposalRef}", method = RequestMethod.PUT)
    @ResponseBody
    public ResponseEntity<Object> updateProposalMetadata(@PathVariable String proposalRef, @RequestBody UpdateProposalRequest request) {
        try {
            return new ResponseEntity<>(apiService.updateProposalMetadata(proposalRef, request), HttpStatus.OK);
        } catch (Exception e) {
            LOG.error("Error occurred while updating proposal title - " + e.getMessage());
            return new ResponseEntity<>("Error occurred while updating proposal title: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping (value = "/{proposalRef}", method = RequestMethod.DELETE)
    @ResponseBody
    public ResponseEntity<Object> deleteProposal(@PathVariable("proposalRef") String proposalRef) {
        try {
            apiService.deleteCollection(proposalRef);
            return new ResponseEntity<>("Proposal successfully deleted", HttpStatus.OK);
        } catch (Exception e) {
            LOG.error("Error occurred while deleting proposal - " + e.getMessage());
            return new ResponseEntity<>("Error occurred while deleting proposal: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping (value = "/searchUser/{searchKey}", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<Object> searchUser(@PathVariable("searchKey") String searchKey) {
        try {
            List<UserJSON> users = apiService.searchUser(searchKey);
            return new ResponseEntity<>(users, HttpStatus.OK);
        } catch (Exception e) {
            LOG.error("Error occurred while searching for users in repository - " + e.getMessage());
            return new ResponseEntity<>("Error occurred while searching for users for repository: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping (value = "/createExplanatory", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<Object> createExplanatory(@RequestBody ExplanatoryRequest request) {
        try {
            apiService.createExplanatoryDocument(request.getProposalRef(), request.getTemplate());
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception e) {
            LOG.error("Unexpected error occurred while creating the explanatory", e);
            return new ResponseEntity<>("Error occurred while creating the explanatory document: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping (value = "/export", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<Object> exportProposal(@RequestBody ExportPdfRequest request) {
        try {
            String jobId = apiService.exportProposal(request.getProposalRef(), request.getExportOutput());
            return new ResponseEntity<>(jobId , HttpStatus.OK);
        } catch (Exception e) {
            LOG.error("Unexpected error occurred while creating the explanatory", e);
            return new ResponseEntity<>("Error occurred while creating the explanatory document: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(value = "/upload", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<Object> uploadProposal(@RequestParam("legFile") MultipartFile legFile) {
        CreateCollectionResult createCollectionResult;
        try {
            File content = new File(legFile.getName());
            try (FileOutputStream fos = new FileOutputStream(content)) {
                fos.write(legFile.getBytes());
            } catch (IOException ioe) {
                LOG.error("Error Occurred while reading the Leg file: " + ioe.getMessage(), ioe);
                return new ResponseEntity<>("An error occurred during the reading of the Leg file.", HttpStatus.INTERNAL_SERVER_ERROR);
            }
            createCollectionResult = apiService.uploadProposal(content);
            return new ResponseEntity<>(createCollectionResult, HttpStatus.OK);
        } catch (CreateCollectionException ex) {
            LOG.error("Error occurred while creating proposal " + ex.getMessage());
            return new ResponseEntity<>("Error occurred while creating proposal", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
