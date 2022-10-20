package eu.europa.ec.leos.annotate.services;

import eu.europa.ec.leos.annotate.model.UserInformation;
import eu.europa.ec.leos.annotate.model.entity.Annotation;
import eu.europa.ec.leos.annotate.model.entity.User;
import eu.europa.ec.leos.annotate.model.web.annotation.JsonAnnotationPermissions;
import eu.europa.ec.leos.annotate.services.exceptions.MissingPermissionException;

public interface AnnotationPermissionService {

    /**
     * check if user may see the annotation based on
     * - his user name (if the annotation is private and he is the user that created the annotation)
     * - his group membership (if the annotation is public and he is member of the group the annotation is published in)
     * 
     * @param annot
     *        the annotation to be retrieved
     * @param user
     *        the user requesting to view the annotation
     *        
     * @return true if user may see the annotation
     */
    boolean hasUserPermissionToSeeAnnotation(Annotation annot, String userlogin, final String userContext);

    /**
     * check if user may update the annotation based on
     * - his user name (if he is the user that created the annotation)
     * - his group membership (if the annotation is public and he is member of the group the annotation is published in)
     * 
     * @param annot
     *        the annotation to be updated
     * @param userinfo
     *        information about the user requesting to update the annotation
     *        
     * @return true if user may update the annotation
     */
    boolean hasUserPermissionToUpdateAnnotation(Annotation annot, UserInformation userinfo);

    /**
     * check if user may accept a suggestion based on his user name (needs to be member of the group to which the annotation belongs)
     * 
     * @param sugg
     *        the suggestion to be accepted
     * @param user
     *        user requesting to accept the suggestion
     *        
     * @return true if user may accept the suggestion
     */
    boolean hasUserPermissionToAcceptSuggestion(Annotation sugg, User user);

    /**
     * check if user may reject a suggestion based on his user name (needs to be member of the group to which the annotation belongs)
     * 
     * @param sugg
     *        the suggestion to be rejected
     * @param user
     *        the user requesting to reject the suggestion
     *        
     * @return true if user may reject the suggestion
     */
    boolean hasUserPermissionToRejectSuggestion(Annotation sugg, User user);

    /**
     * check for a given user whether he is permitted to publish a contributor's annotations
     * 
     * @param userInfo 
     *        information about the user requesting the publication
     * @return flag indicating whether he is allowed to execute the publication process
     */
    boolean userMayPublishContributions(final UserInformation userInfo);

    /**
     * check for a given user whether he is permitted to trigger a metadata status update
     * 
     * @param userInfo information about the user requesting the status update
     * @return flag indicating whether he is allowed to execute the update
     */
    boolean userMayUpdateMetadata(final UserInformation userInfo);

    /**
     * get the permissions
     * currently kept simple:
     * - private annotation -> read permission for user only
     * - public annotation -> read permission for group
     * admin, delete and update always for user only - unless the annotation has responseStatus=SENT
     * -> in that case, we don't provide any permissions there, i.e. the annotation cannot be modified
     * 
     * @param annot 
     *        annotation for which permissions are to be computed
     * @param groupName 
     *        associate group
     * @param userAccountForHypo 
     *        hypothes.is account name of the annotation's author
     * @param userInfo
     *        information about the user receiving the data
     * @return assembled {@link JsonAnnotationPermissions}
     */
    JsonAnnotationPermissions getJsonAnnotationPermissions(final Annotation annot, final String groupName,
            final String userAccountForHypo, final UserInformation userInfo);

    /**
     * Check if user is not a support authority user.
     *
     * If user is a support authority user{@link MissingPermissionException} is thrown
     *
     * @param userInfo {@link UserInformation} containing user details
     * */
    void validateUserAnnotationApiPermission(final UserInformation userInfo) throws MissingPermissionException;
}