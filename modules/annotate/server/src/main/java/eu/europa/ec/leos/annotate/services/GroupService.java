/*
 * Copyright 2018 European Commission
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
package eu.europa.ec.leos.annotate.services;

import eu.europa.ec.leos.annotate.model.entity.Group;
import eu.europa.ec.leos.annotate.services.exceptions.DefaultGroupNotFoundException;
import eu.europa.ec.leos.annotate.services.exceptions.GroupAlreadyExistingException;

public interface GroupService {

    /**
     * find a group given its name
     * 
     * @param groupName
     *        name to be matched; note: if not found, matching an internal name (e.g. "__world__") is tried also
     * @return found {@link Group}, or {@literal null}
     */
    Group findGroupByName(String groupName);

    /**
     * create a new group with a given name and visibility
     *  
     * @param name
     *        name of the new group, will also be used for display name and other properties
     * @param isPublic
     *        flag indicating if this group is public (not used yet)
     * @return created {@link Group}
     * @throws GroupAlreadyExistingException
     *         thrown if a group with the same name already exists
     */
    Group createGroup(String name, boolean isPublic) throws GroupAlreadyExistingException;

    /**
     * gives the name of the default group
     * 
     * @return default group name
     */
    String getDefaultGroupName();
    
    /**
     * looks up the default group
     * 
     * @return found default {@link Group}, or {@literal null} 
     */
    Group findDefaultGroup();
    
    /*
     * group assignments
     */
    
    /**
     * check if the default group is configured; throw exception if not
     * 
     * @throws DefaultGroupNotFoundException
     *         thrown when no default group can be found in the database
     */
    void throwIfNotExistsDefaultGroup() throws DefaultGroupNotFoundException;

    /**
     * returns the name of the {@link Group} having a given ID
     * 
     * @param groupId
     *        ID of the wanted group
     * @return display name of the group
     */
    String getGroupName(long groupId);

}
