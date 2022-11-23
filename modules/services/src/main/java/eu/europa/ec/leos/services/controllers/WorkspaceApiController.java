package eu.europa.ec.leos.services.controllers;

import eu.europa.ec.leos.services.api.ApiService;
import eu.europa.ec.leos.services.collection.CreateCollectionException;
import eu.europa.ec.leos.services.collection.CreateCollectionResult;
import eu.europa.ec.leos.services.dto.request.CreateProposalRequest;
import eu.europa.ec.leos.services.dto.request.FilterProposalsRequest;
import eu.europa.ec.leos.services.dto.response.WorkspaceProposalResponse;
import eu.europa.ec.leos.vo.catalog.CatalogItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(value = "/secured")
public class WorkspaceApiController {

    private static final Logger LOG = LoggerFactory.getLogger(WorkspaceApiController.class);

    private final ApiService apiService;

    @Autowired
    public WorkspaceApiController(ApiService apiService) {
        this.apiService = apiService;
    }

    @RequestMapping(value = "/filterProposals", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<Object> filterProposals(@RequestBody FilterProposalsRequest request) {
        try {
            WorkspaceProposalResponse workspaceProposalResponse = apiService.listDocumentsWithFilter(request);
            if(workspaceProposalResponse != null) {
                return new ResponseEntity<>(workspaceProposalResponse, HttpStatus.OK);
            } else {
                return new ResponseEntity<>("No result found", HttpStatus.NOT_FOUND);
            }
        } catch (Exception ex) {
            LOG.error("Error occurred while retrieving list of proposals " + ex.getMessage());
            return new ResponseEntity<>("Error occurred while retrieving list of proposals " + ex.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(value = "/createPackage", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<Object> createPackage(@RequestBody CreateProposalRequest request) {
        CreateCollectionResult createCollectionResult;
        try {
            createCollectionResult = apiService.createProposal(request.getTemplateId(), request.getTemplateName(),
                    request.getLangCode(), request.getDocPurpose(), request.isEeaRelevance());
            return new ResponseEntity<>(createCollectionResult, HttpStatus.OK);
        } catch (CreateCollectionException ex) {
            LOG.error("Error occurred while creating proposal " + ex.getMessage());
            return new ResponseEntity<>("Error occurred while creating proposal", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(value = "/getTemplates", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<Object> getTemplates() {
        try {
            List<CatalogItem> catalogItems = apiService.getTemplates();
            if (catalogItems != null && catalogItems.size() > 0) {
                return new ResponseEntity<>(catalogItems, HttpStatus.OK);
            } else {
                return new ResponseEntity<>("No result found", HttpStatus.NOT_FOUND);
            }
        } catch (Exception ex) {
            LOG.error("Error occurred while retrieving list of proposals " + ex.getMessage());
            return new ResponseEntity<>("Error occurred while retrieving list of proposals " + ex.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
