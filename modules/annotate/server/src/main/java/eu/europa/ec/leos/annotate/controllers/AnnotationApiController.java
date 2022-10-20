/*
 * Copyright 2018-2020 European Commission
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
package eu.europa.ec.leos.annotate.controllers;

import com.fasterxml.jackson.core.JsonParser.Feature;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.europa.ec.leos.annotate.aspects.NoAuthAnnotation;
import eu.europa.ec.leos.annotate.model.AuthenticatedUserStore;
import eu.europa.ec.leos.annotate.model.SimpleMetadataWithStatuses;
import eu.europa.ec.leos.annotate.model.UserInformation;
import eu.europa.ec.leos.annotate.model.entity.Annotation;
import eu.europa.ec.leos.annotate.model.search.*;
import eu.europa.ec.leos.annotate.model.search.helper.AnnotationSearchOptionsBuilder;
import eu.europa.ec.leos.annotate.model.web.IncomingSearchOptions;
import eu.europa.ec.leos.annotate.model.web.JsonFailureResponse;
import eu.europa.ec.leos.annotate.model.web.annotation.*;
import eu.europa.ec.leos.annotate.services.*;
import eu.europa.ec.leos.annotate.services.exceptions.*;
import eu.europa.ec.leos.annotate.services.impl.util.ZipContent;
import eu.europa.ec.leos.annotate.websockets.MessageBroker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.util.StreamUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.util.List;

@RestController
@RequestMapping("/api")
public class AnnotationApiController {

    private static final Logger LOG = LoggerFactory.getLogger(AnnotationApiController.class);
    private static final String CLIENT_HEADER = "x-client-id";

    // -------------------------------------
    // Required services and repositories
    // -------------------------------------
    @Autowired
    private AnnotationService annotService;

    @Autowired
    private AnnotationConversionService conversionService;

    @Autowired
    private AnnotationSearchService searchService;

    @Autowired
    private AuthenticatedUserStore authUser;

    @Autowired
    private MessageBroker messageBroker;

    @Autowired
    private ZipService zipService;

    @Autowired
    private AnnotationPermissionService permissionService;

    // -------------------------------------
    // API endpoints
    // -------------------------------------

    /**
     * Endpoint for adding a new annotation
     *
     * @param request Incoming request, containing annotation to be added as JSON body
     * @param response Outgoing response, containing persisted annotation as JSON body
     * @param jsonAnnotation JSON annotation metadata ({@link JsonAnnotation}), extracted from request body
     * @param connectedEntity the entity with which the user is connected, as request parameter
     *
     * @return
     * in case of success: HTTP status 200, JSON based response containing received annotation with some properties updated
     * in case of failure: HTTP status 400, JSON based response with error description
     *
     */
    @RequestMapping(value = "/annotations", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<Object> addAnnotation(final HttpServletRequest request, final HttpServletResponse response,
            @RequestParam(value = "connectedEntity", required = false) final String connectedEntity,
            @RequestBody final JsonAnnotation jsonAnnotation) {

        LOG.debug("Received request to add new annotation");

        String errorMsg = "";

        try {
            final UserInformation userInfo = authUser.getUserInfo();
            permissionService.validateUserAnnotationApiPermission(userInfo);

            if (StringUtils.hasLength(connectedEntity)) {
                userInfo.setConnectedEntity(connectedEntity);
            }
            final Annotation createdAnnotation = annotService.createAnnotation(jsonAnnotation, userInfo);
            final JsonAnnotation myResponseJson = conversionService.convertToJsonAnnotation(createdAnnotation, userInfo);
            messageBroker.publish(myResponseJson.getId(), MessageBroker.ACTION.CREATE, request.getHeader(CLIENT_HEADER));

            LOG.debug("Annotation was saved, return Http status 200 and annotation metadata; annotation id: '{}'", myResponseJson.getId());
            return new ResponseEntity<Object>(myResponseJson, HttpStatus.OK);

        } catch (Exception e) {
            LOG.error("Error while creating annotation", e);
            errorMsg = e.getMessage();
        }

        LOG.warn("Annotation was not saved, return Http status 400 and failure notice");
        return new ResponseEntity<Object>(new JsonFailureResponse("The annotation could not be created: " + errorMsg), HttpStatus.BAD_REQUEST);
    }

    /**
     * Endpoint for retrieving an annotation with a given ID
     *
     * @param request Incoming request
     * @param response Outgoing response
     * @param annotationId Id of the wanted annotation; contained in request parameter
     *
     * @return
     * in case of success: HTTP status 200, JSON based response containing found annotation
     * in case of failure: HTTP status 404, JSON based response with error description (e.g. if annotation not found or user may not view it)
     *
     */
    @RequestMapping(value = "/annotations/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<Object> getAnnotation(final HttpServletRequest request, final HttpServletResponse response,
            @PathVariable("id") final String annotationId) {

        LOG.debug("Received request to retrieve existing annotation");

        String errorMsg = "";

        try {
            final UserInformation userInfo = authUser.getUserInfo();
            permissionService.validateUserAnnotationApiPermission(userInfo);

            final Annotation ann = annotService.findAnnotationById(annotationId, userInfo.getLogin(), userInfo.getContext());
            if (ann == null) {
                throw new Exception("Annotation '" + annotationId + "' not found.");
            }
            final JsonAnnotation foundAnn = conversionService.convertToJsonAnnotation(ann, userInfo);

            LOG.debug("Annotation found, return Http status 200 and annotation metadata; annotation id: '{}'", annotationId);
            return new ResponseEntity<Object>(foundAnn, HttpStatus.OK);

        } catch (MissingPermissionException mpe) {
            errorMsg = mpe.getMessage();

        } catch (Exception e) {
            LOG.error("Error while retrieving annotation", e);
            errorMsg = "The annotation '" + annotationId + "' could not be found: " + e.getMessage();
        }

        LOG.warn("Annotation could not be found or error occured, return Http status 404 and failure notice");
        return new ResponseEntity<Object>(new JsonFailureResponse(errorMsg), HttpStatus.NOT_FOUND);
    }

    /**
     * Endpoint for updating an existing annotation with a given ID
     *
     * @param request Incoming request
     * @param response Outgoing response
     * @param annotationId Id of the annotation to be updated; contained in request parameter
     * @param connectedEntity the entity with which the user is connected, as request parameter
     *
     * @return
     * in case of success: HTTP status 200, JSON based response containing updated annotation
     * in case of failure:
     * - HTTP status 400, JSON based response with error description (e.g. if annotation data incomplete, annotation has SENT status or other problem during update)
     * - HTTP status 404, JSON based response with error description (e.g. if annotation not found or user may not update it)
     * - HTTP status 500, JSON based response with error description for any unforeseen error
     *
     */
    @RequestMapping(value = "/annotations/{id}", method = RequestMethod.PATCH, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<Object> updateAnnotation(final HttpServletRequest request, final HttpServletResponse response,
            @PathVariable("id") final String annotationId,
            @RequestParam(value = "connectedEntity", required = false) final String connectedEntity,
            @RequestBody final JsonAnnotation jsonAnnotation) {

        LOG.debug("Received request to update an existing annotation");

        String errorMsg = "";
        HttpStatus httpStatusToSent;

        try {
            final UserInformation userInfo = authUser.getUserInfo();
            permissionService.validateUserAnnotationApiPermission(userInfo);

            if (StringUtils.hasLength(connectedEntity)) {
                userInfo.setConnectedEntity(connectedEntity);
            }
            final Annotation updatedAnnotation = annotService.updateAnnotation(annotationId, jsonAnnotation, userInfo);
            final JsonAnnotation myResponseJson = conversionService.convertToJsonAnnotation(updatedAnnotation, userInfo);

            // standard update or was a new annotation created (special handling for SENT ISC annotations)?
            final String reqHeader = request.getHeader(CLIENT_HEADER);
            if (annotationId.equals(myResponseJson.getId())) {
                // standard update - publish the update only
                messageBroker.publish(myResponseJson.getId(), MessageBroker.ACTION.UPDATE, reqHeader);
            } else {
                // new annotation created - publish the update of the original and a creation of a new annotation
                messageBroker.publish(annotationId, MessageBroker.ACTION.UPDATE, reqHeader);
                messageBroker.publish(myResponseJson.getId(), MessageBroker.ACTION.CREATE, reqHeader);
            }

            LOG.debug("Annotation updated, return Http status 200 and annotation metadata; annotation id: '{}'", annotationId);
            return new ResponseEntity<Object>(myResponseJson, HttpStatus.OK);

        } catch (MissingPermissionException | CannotUpdateAnnotationException e) {

            httpStatusToSent = HttpStatus.NOT_FOUND;
            LOG.error("Error while updating annotation", e);
            errorMsg = e.getMessage();

        } catch (CannotUpdateSentAnnotationException e) {

            httpStatusToSent = HttpStatus.BAD_REQUEST;
            LOG.error("Error: cannot update SENT annotation", e);
            errorMsg = e.getMessage();

        } catch (Exception e) {

            httpStatusToSent = HttpStatus.INTERNAL_SERVER_ERROR;
            LOG.error("Unexpected error while updating annotation", e);
            errorMsg = e.getMessage();
        }

        LOG.warn("Annotation could not be updated, return Http status " + httpStatusToSent.value() + " and failure notice");
        return new ResponseEntity<Object>(new JsonFailureResponse("The annotation '" + annotationId + "' could not be updated: " + errorMsg),
                httpStatusToSent);
    }

    /**
     * Endpoint to reset an annotation as normal with a given ID
     *
     * @param request Incoming request
     * @param response Outgoing response
     * @param annotationId Id of the annotation to be reset to normal; contained in request parameter
     *
     * @return
     * in case of success: <br/> 
     * - HTTP status 200, JSON based response containing success information and ID of the annotation <br/>
     * in case of failure: <br/>
     * - HTTP status 400, JSON based response with error description (e.g. when annotation has SENT status and cannot be reset) <br/>
     * - HTTP status 500, JSON based response with error description for any unforeseen error
     *
     */
    @RequestMapping(value = "/annotations/reset/{id}", method = RequestMethod.PATCH, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<Object> resetAnnotation(final HttpServletRequest request, final HttpServletResponse response,
            @PathVariable("id") final String annotationId) {

        LOG.debug("Received request to reset an existing annotation as normal");

        String errorMsg = "";
        HttpStatus statusToReturnIfFailed;

        final UserInformation userInfo = authUser.getUserInfo();
        try {
            annotService.resetAnnotationById(annotationId, userInfo);
            messageBroker.publish(annotationId, MessageBroker.ACTION.UPDATE, request.getHeader(CLIENT_HEADER));

            LOG.info("Annotation reset to normal, return Http status 200 and annotation id: '{}'", annotationId);
            return new ResponseEntity<Object>(new JsonResetSuccessResponse(annotationId), HttpStatus.OK);
        } catch (CannotResetAnnotationException ex) {
            statusToReturnIfFailed = HttpStatus.BAD_REQUEST;
            LOG.error("Error while resetting annotation status", ex);
            errorMsg = ex.getMessage();
        }

        LOG.warn("Annotation could not be reset to normal, return Http status {} and failure notice", statusToReturnIfFailed.value());
        return new ResponseEntity<Object>(new JsonFailureResponse("The annotation could not be reset to normal: " + errorMsg), statusToReturnIfFailed);
    }

    /**
     * Endpoint to mark an annotation as treated with a given ID
     *
     * @param request Incoming request
     * @param response Outgoing response
     * @param annotationId Id of the annotation to be treated; contained in request parameter
     *
     * @return
     * in case of success: <br/> 
     * - HTTP status 200, JSON based response containing success information and ID of the treated annotation <br/>
     * in case of failure: <br/>
     * - HTTP status 400, JSON based response with error description (e.g. when annotation has SENT status and cannot be treated) <br/>
     * - HTTP status 500, JSON based response with error description for any unforeseen error
     *
     */
    @RequestMapping(value = "/annotations/treat/{id}", method = RequestMethod.PATCH, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<Object> treatAnnotation(final HttpServletRequest request, final HttpServletResponse response,
            @PathVariable("id") final String annotationId) {

        LOG.debug("Received request to mark an existing annotation as treated");

        String errorMsg = "";
        HttpStatus statusToReturnIfFailed;

        final UserInformation userInfo = authUser.getUserInfo();
        try {
            annotService.treatAnnotationById(annotationId, userInfo);
            messageBroker.publish(annotationId, MessageBroker.ACTION.UPDATE, request.getHeader(CLIENT_HEADER));

            LOG.info("Annotation treated, return Http status 200 and annotation id: '{}'", annotationId);
            return new ResponseEntity<Object>(new JsonTreatSuccessResponse(annotationId), HttpStatus.OK);
        } catch (CannotTreatAnnotationException ex) {
            statusToReturnIfFailed = HttpStatus.BAD_REQUEST;
            LOG.error("Error while treating annotation", ex);
            errorMsg = ex.getMessage();
        }

        LOG.warn("Annotation could not be treated, return Http status {} and failure notice", statusToReturnIfFailed.value());
        return new ResponseEntity<Object>(new JsonFailureResponse("The annotation could not be treated: " + errorMsg), statusToReturnIfFailed);
    }

    /**
     * Endpoint to mark a whole set of annotation as treated with given IDs
     *
     * @param request Incoming request
     * @param response Outgoing response
     * @param annotationList the message body contains an array "ids" that contains the IDs of all annotations
     *             that should be marked as treated
     *
     * @return
     * in case of success: HTTP status 200, JSON based response containing success information
     * in case of failure:
     * - HTTP status 500, JSON based response with error description for any unforeseen error
     *
     */
    @RequestMapping(value = "/annotations/treat", method = RequestMethod.PATCH, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<Object> bulkTreatAnnotations(final HttpServletRequest request, final HttpServletResponse response,
            @RequestBody final JsonIdList annotationList) {

        LOG.debug("Received request to mark an existing set of annotations as treated");

        String errorMsg = "";
        HttpStatus statusToReturnIfFailed;

        final UserInformation userInfo = authUser.getUserInfo();
        try {
            final List<String> annotationIds = annotService.treatAnnotationsById(annotationList.getIds(), userInfo);
            for (final String annotationId : annotationIds) {
                messageBroker.publish(annotationId, MessageBroker.ACTION.UPDATE, request.getHeader(CLIENT_HEADER));
            }

            LOG.info("Annotations treated, return Http status 200");
            return new ResponseEntity<Object>(new JsonBulkTreatSuccessResponse(), HttpStatus.OK);
        } catch (CannotTreatAnnotationException ex) {
            statusToReturnIfFailed = HttpStatus.BAD_REQUEST;
            LOG.error("Error while treating annotations", ex);
            errorMsg = ex.getMessage();
        }

        LOG.warn("Annotations could not be treated, return Http status {} and failure notice", statusToReturnIfFailed.value());
        return new ResponseEntity<Object>(new JsonFailureResponse("The annotations could not be treated: " + errorMsg), statusToReturnIfFailed);
    }

    /**
     * Endpoint to mark a whole set of annotation as normal with given IDs
     *
     * @param request Incoming request
     * @param response Outgoing response
     * @param annotationList the message body contains an array "ids" that contains the IDs of all annotations
     *             that should be marked as normal
     *
     * @return
     * in case of success: HTTP status 200, JSON based response containing success information
     * in case of failure:
     * - HTTP status 500, JSON based response with error description for any unforeseen error
     *
     */
    @RequestMapping(value = "/annotations/reset", method = RequestMethod.PATCH, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<Object> bulkResetAnnotations(final HttpServletRequest request, final HttpServletResponse response,
            @RequestBody final JsonIdList annotationList) {

        LOG.debug("Received request to mark an existing set of annotations as normal");

        String errorMsg = "";
        HttpStatus statusToReturnIfFailed;

        final UserInformation userInfo = authUser.getUserInfo();
        try {
            final List<String> annotationIds = annotService.resetAnnotationsById(annotationList.getIds(), userInfo);
            for (final String annotationId : annotationIds) {
                messageBroker.publish(annotationId, MessageBroker.ACTION.UPDATE, request.getHeader(CLIENT_HEADER));
            }

            LOG.info("Annotations reset, return Http status 200");
            return new ResponseEntity<Object>(new JsonBulkResetSuccessResponse(), HttpStatus.OK);
        } catch (CannotResetAnnotationException ex) {
            statusToReturnIfFailed = HttpStatus.BAD_REQUEST;
            LOG.error("Error while marking annotations as normal", ex);
            errorMsg = ex.getMessage();
        }

        LOG.warn("Annotations could not be reset, return Http status {} and failure notice", statusToReturnIfFailed.value());
        return new ResponseEntity<Object>(new JsonFailureResponse("The annotations could not be reset: " + errorMsg), statusToReturnIfFailed);
    }

    /**
     * Endpoint for deleting an annotation with a given ID
     *
     * @param request Incoming request
     * @param response Outgoing response
     * @param annotationId Id of the annotation to be deleted; contained in request parameter
     *
     * @return
     * in case of success: HTTP status 200, JSON based response containing success information and ID of the deleted annotation
     * in case of failure:
     * - HTTP status 400, JSON based response with error description (e.g. when annotation has SENT status and cannot be deleted) 
     * - HTTP status 404, JSON based response with error description (e.g. if annotation not found)
     * - HTTP status 500, JSON based response with error description for any unforeseen error
     *
     */
    @RequestMapping(value = "/annotations/{id}", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<Object> deleteAnnotation(final HttpServletRequest request, final HttpServletResponse response,
            @PathVariable("id") final String annotationId) {

        LOG.debug("Received request to delete an existing annotation");

        String errorMsg = "";
        HttpStatus statusToReturnIfFailed;

        try {
            final UserInformation userInfo = authUser.getUserInfo();
            permissionService.validateUserAnnotationApiPermission(userInfo);

            annotService.deleteAnnotationById(annotationId, userInfo);
            messageBroker.publish(annotationId, MessageBroker.ACTION.DELETE, request.getHeader(CLIENT_HEADER));

            LOG.info("Annotation deleted, return Http status 200 and annotation id: '{}'", annotationId);
            return new ResponseEntity<Object>(new JsonDeleteSuccessResponse(annotationId), HttpStatus.OK);

        } catch (CannotDeleteAnnotationException cdae) {

            LOG.error("Error while deleting annotation", cdae);
            errorMsg = cdae.getMessage();
            statusToReturnIfFailed = HttpStatus.NOT_FOUND;

        } catch (CannotDeleteSentAnnotationException cdsae) {

            LOG.error("Error: trying to delete SENT annotation");
            errorMsg = cdsae.getMessage();
            statusToReturnIfFailed = HttpStatus.BAD_REQUEST;

        } catch (Exception e) {

            LOG.error("Unexpected error while deleting annotation", e);
            errorMsg = e.getMessage();
            statusToReturnIfFailed = HttpStatus.INTERNAL_SERVER_ERROR;
        }

        LOG.warn("Annotation could not be deleted, return Http status {} and failure notice", statusToReturnIfFailed.value());
        return new ResponseEntity<Object>(new JsonFailureResponse("The annotation could not be deleted: " + errorMsg), statusToReturnIfFailed);
    }

    /**
     * Endpoint for deleting a whole set of annotation with given IDs
     *
     * @param request Incoming request
     * @param response Outgoing response
     * @param annotationList the message body contains an array "ids" that contains the IDs of all annotations
     *             that should be deleted
     *
     * @return
     * in case of success: HTTP status 200, JSON based response containing success information
     * in case of failure:
     * - HTTP status 500, JSON based response with error description for any unforeseen error
     *
     */
    @RequestMapping(value = "/annotations", method = RequestMethod.DELETE, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<Object> bulkDeleteAnnotations(final HttpServletRequest request, final HttpServletResponse response,
            @RequestBody final JsonIdList annotationList) {

        LOG.debug("Received request to do bulk deletion of annotations");

        String errorMsg = "";

        try {
            final UserInformation userInfo = authUser.getUserInfo();
            permissionService.validateUserAnnotationApiPermission(userInfo);

            final List<String> deleted = annotService.deleteAnnotationsById(annotationList.getIds(), userInfo);
            for (final String annotationId : deleted) {
                messageBroker.publish(annotationId, MessageBroker.ACTION.DELETE, request.getHeader(CLIENT_HEADER));
            }

            LOG.info("Annotations deleted, return Http status 200 and success");
            return new ResponseEntity<Object>(new JsonBulkDeleteSuccessResponse(), HttpStatus.OK);

        } catch (Exception e) {

            LOG.error("Unexpected error while deleting annotation", e);
            errorMsg = e.getMessage();
        }

        LOG.warn("Annotations could not all be deleted, return Http status {} and failure notice", HttpStatus.INTERNAL_SERVER_ERROR.value());
        return new ResponseEntity<Object>(new JsonFailureResponse("The annotations could not all be deleted: " + errorMsg), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Endpoint for searching for annotations matching given criteria
     *
     * @param request Incoming request
     * @param response Outgoing response
     * @param separate_replies (request parameter)
     *        search flag indicating whether replies should be mixed with
     *        annotations or be returned as separate group; default: {@value #DEFAULT_SEARCH_SEARCH_REPLIES}
     * @param limit (request parameter, wrapped in {@link IncomingSearchOptions} object)
     *        the maximum number of annotations to return; default: {@value #DEFAULT_SEARCH_LIMIT}
     * @param offset (request parameter, wrapped in {@link IncomingSearchOptions} object)
     *        minimum number of initial annotations to skip; default: {@value #DEFAULT_SEARCH_OFFSET}
     * @param sort (request parameter, wrapped in {@link IncomingSearchOptions} object)
     *        field by which annotations should be sorted; default: {@value #DEFAULT_SEARCH_SORT}
     * @param order (request parameter, wrapped in {@link IncomingSearchOptions} object)
     *        order in which the results should be sorted; default: {@value #DEFAULT_SEARCH_ORDER}; allows "asc" and "desc"
     * @param uri (request parameter, wrapped in {@link IncomingSearchOptions} object)
     *        URI to be used for the search - usually the URL of the annotated page
     * @param url (request parameter, wrapped in {@link IncomingSearchOptions} object)
     *        URL of the annotated page; alias for URI
     * @param user (request parameter, wrapped in {@link IncomingSearchOptions} object)
     *        limit the results to annotations made by the specified user (login)
     * @param group (request parameter, wrapped in {@link IncomingSearchOptions} object)
     *        limit the results to annotations made in the specified group
     * @param mode (request parameter, wrapped in {@link IncomingSearchOptions} object)
     *        indicates when search is executed by a 'contributor'
     * @param tag (request parameter, wrapped in {@link IncomingSearchOptions} object)
     *        limit the results to annotations tagged with the specified value - currently ignored
     * @param any (request parameter, wrapped in {@link IncomingSearchOptions} object)
     *        limit the results to annotations in which one of a number of common fields contain the passed value - currently ignored
     * @param metadatasets (request parameter, wrapped in {@link IncomingSearchOptions} object) 
     *        array of {@link SimpleMetadataWithStatuses} maps of metadata and statuses to be matched; logical OR matching applied
     * @param connectedEntity (request parameter, wrapped in {@link IncomingSearchOptions} object)
     *        name of the entity with which the user is currently logged on (used in ISC only)
     * @param mode (request parameter, wrapped in {@link IncomingSearchOptions} object)
     *        when set to "private", this indicates that an ISC contributor is running the search (used in ISC only)
     * @param shared (request parameter, wrapped in {@link IncomingSearchOptions} object
     *        optional flag indicating whether private (shared = false) or public (shared = true) are to be considered
     *        if the parameter is missing, both public and private annotations are considered
     * @return
     * in case of success: HTTP status 200, JSON based response containing search results
     * in case of failure: HTTP status 400, JSON based response with error description (e.g. if search failed)
     *
     */
    @RequestMapping(value = "/search", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<Object> searchAnnotations(final HttpServletRequest request, final HttpServletResponse response,
            @RequestParam(value = "_separate_replies", defaultValue = Consts.DEFAULT_SEARCH_SEPARATE_REPLIES) final boolean separate_replies,
            final IncomingSearchOptions incomingOptions) {

        // note: _separate_replies needs to remain separate request parameter,
        // as it cannot be mapped directly due to "_" prefix
        LOG.debug("Received request to search for annotations");

        // note the IncomingSearchOptions object is never null, as at least its default values are set

        String errorMsg = "";

        try {
            // convert received parameters into internal object, which contains some more logic,
            // e.g. validity checks
            final UserInformation userInfo = authUser.getUserInfo();
            permissionService.validateUserAnnotationApiPermission(userInfo);

            final AnnotationSearchOptions options = AnnotationSearchOptionsBuilder.fromIncomingSearchOptions(incomingOptions, separate_replies);
            userInfo.setConnectedEntity(incomingOptions.getConnectedEntity()); // store the user's entity from which he is connected (not persisted)

            final AnnotationSearchResult searchResult = searchService.searchAnnotations(options, userInfo);
            List<Annotation> replies = null;
            if (searchResult != null && !searchResult.isEmpty()) {
                replies = searchService.searchRepliesForAnnotations(searchResult, options, userInfo);
            }
            final JsonSearchResult result = conversionService.convertToJsonSearchResult(searchResult, replies, options, userInfo);

            LOG.debug("Annotation search successful, return Http status 200 and result: '{}'", (result == null ? "0" : result.getTotal()));
            return new ResponseEntity<Object>(result, HttpStatus.OK);

        } catch (Exception e) {
            LOG.error("Error while searching for annotations", e);
            errorMsg = e.getMessage();
        }

        LOG.warn("There was a problem during annotation search, return Http status 400 and failure notice");
        return new ResponseEntity<Object>(new JsonFailureResponse("The annotations could not be searched for: " + errorMsg), HttpStatus.BAD_REQUEST);
    }

    /**
     * Endpoint for counting the number of annotations matching given criteria
     *
     * @param request Incoming request
     * @param response Outgoing response
     * @param uri (request parameter, wrapped in {@link AnnotationSearchCountOptions} object)
     *        URI to be used for the search - usually the URL of the annotated page
     * @param group (request parameter, wrapped in {@link AnnotationSearchCountOptions} object)
     *        limit the results to annotations made in the specified group
     * @param shared (request parameter, wrapped in {@link AnnotationSearchCountOptions} object
     *        optional flag indicating whether private (shared = false) or public (shared = true) are to be considered
     *        if the parameter is missing, both public and private annotations are considered
     * @param user (request parameter, wrapped in {@link AnnotationSearchCountOptions} object)
     *        limit the results to annotations made by the specified user (login)
     * @param metadatasets (request parameter, wrapped in {@link AnnotationSearchCountOptions} object) 
     *        array of {@link SimpleMetadataWithStatuses} maps of metadata and statuses to be matched; logical OR matching applied
     * @param connectedEntity (request parameter, wrapped in {@link AnnotationSearchCountOptions} object)
     *        name of the entity with which the user is currently logged on (used in ISC only)       
     * @return
     * in case of success: HTTP status 200, JSON based response number of found annotations
     * in case of failure: HTTP status 400, JSON based response with error description (e.g. if search failed)
     *
     */
    @RequestMapping(value = "/count", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<Object> countAnnotations(final HttpServletRequest request, final HttpServletResponse response,
            final AnnotationSearchCountOptions options) {

        LOG.debug("Received request to search for annotations");

        String errorMsg = "";

        try {
            final UserInformation userInfo = authUser.getUserInfo();
            permissionService.validateUserAnnotationApiPermission(userInfo);

            // store the user's entity from which he is connected (not persisted)
            userInfo.setConnectedEntity(options.getConnectedEntity());

            // at least during test scenarios, we experienced problems when sending JSON metadata
            // with only one entry - therefore, we had encoded the curly brackets URL-conform,
            // and have to decode this again here
            options.decodeEscapedBrackets();
            final int resultVal = searchService.getAnnotationsCount(options, userInfo);

            LOG.debug("Annotation counting successful, return Http status 200 and result: '{}'", resultVal);
            return new ResponseEntity<Object>(new JsonSearchCount(resultVal), HttpStatus.OK);

        } catch (Exception e) {
            LOG.error("Error while searching for annotations", e);
            errorMsg = e.getMessage();
        }

        LOG.warn("There was a problem during annotation count search, return Http status 400 and failure notice");
        return new ResponseEntity<Object>(new JsonFailureResponse("The annotations could not be counted: " + errorMsg), HttpStatus.BAD_REQUEST);
    }

    // configuration of the JSON deserialisation: allowing fields without quotes
    // e.g. used in document/metadata entries
    @NoAuthAnnotation
    @Bean
    public ObjectMapper objectMapper() {

        final Jackson2ObjectMapperBuilder builder = new Jackson2ObjectMapperBuilder();
        builder.featuresToEnable(Feature.ALLOW_UNQUOTED_FIELD_NAMES);
        return builder.build();
    }

    /**
     * Endpoint for getting annotations of a document
     *
     * @param request Incoming request
     * @param response Outgoing response, containing persisted annotation as JSON body
     * @param documentUri (request parameter)
     *        URI to be used for the search - usually the URL of the annotated page
     * @return
     * in case of success: HTTP status 200, JSON based response containing received annotation with some properties updated
     * in case of failure: HTTP status 400, JSON based response with error description
     *
     */
    @RequestMapping(value = "/document/annotations", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<Object> getDocumentAnnotation(final HttpServletRequest request,
            final HttpServletResponse response,
            @RequestParam("uri") final String documentUri) {

        LOG.debug("Received request to get annotations for document");

        String errorMsg = "";

        try {
            final UserInformation userInfo = authUser.getUserInfo();
            final DocumentAnnotationsResult documentAnnotationsResult = searchService.getDocumentAnnotations(documentUri, userInfo);
            final JsonSearchResult result = conversionService.convertToJsonSearchResult(documentAnnotationsResult, userInfo);

            LOG.debug("Search for document annotations finished, return Http status 200 and result: '{}'", result);
            return new ResponseEntity<Object>(result, HttpStatus.OK);

        } catch (Exception e) {
            LOG.error("Error while searching document annotations", e);
            errorMsg = e.getMessage();
        }

        LOG.warn("Document annotation search failed, return Http status 400 and failure notice");
        return new ResponseEntity<Object>(new JsonFailureResponse("The document annotation search failed: " + errorMsg), HttpStatus.BAD_REQUEST);
    }

    /**
     * Endpoint for getting annotations of one or more documents
     *
     * @param request Incoming request, containing a LEG file
     * @param response Outgoing response, containing processed LEG file including annotations for documents
     *
     * @return
     * in case of success: HTTP status 200, JSON based response containing received annotation with some properties updated
     * in case of failure: HTTP status 400, JSON based response with error description
     *
     */
    @RequestMapping(value = "/document/annotations", method = RequestMethod.POST, consumes = MediaType.APPLICATION_OCTET_STREAM_VALUE, produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    @ResponseBody
    public ResponseEntity<Object> postDocumentAnnotation(final HttpServletRequest request,
            final HttpServletResponse response) {
        LOG.debug("Received request to get annotations for LEG file documents");

        String errorMsg = "";

        try {
            final UserInformation userInfo = authUser.getUserInfo();
            final byte[] requestData = StreamUtils.copyToByteArray(request.getInputStream());

            // Unzip the leg file and keep the unzipped items in a list
            final List<ZipContent> zipContentList = zipService.unzipBytes(requestData);
            // Search for Annotations of every document found in the leg file
            final List<LegDocumentAnnotationsResult> searchResults = searchService.getLegDocumentAnnotations(zipContentList, userInfo);

            // Convert the search results into json objects and add it to the ZipContent list
            for (final LegDocumentAnnotationsResult searchResult : searchResults) {
                final JsonSearchResult jsonSearchResult = conversionService.convertToJsonSearchResult(searchResult.getAnnotationsResult(), userInfo);
                zipService.addJsonSearchResultToList(zipContentList, searchResult.getDocumentName(), jsonSearchResult,
                        objectMapper());

                LOG.debug("{} Annotations added for document '{}'",
                        searchResult.getAnnotationsResult().getSearchResult().getItems().size(),
                        searchResult.getDocumentName());
            }

            // Return zip content as zip file and set zip specific headers
            final byte[] responseZip = zipService.zipContentToBytes(zipContentList);
            final HttpHeaders zipResponseHeaders = zipService.getZipResponseHeaders(responseZip.length, "leg");
            return new ResponseEntity<Object>(responseZip, zipResponseHeaders, HttpStatus.OK);
        } catch (Exception e) {
            LOG.error("Error while searching document annotations", e);
            errorMsg = e.getMessage();
        }

        LOG.warn("Document annotation search failed, return Http status 400 and failure notice");
        return new ResponseEntity<Object>(new JsonFailureResponse("The document annotation search failed: " + errorMsg), HttpStatus.BAD_REQUEST);
    }

}
