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
package eu.europa.ec.digit.userdata.repositories;

import eu.europa.ec.digit.userdata.entities.SpecialUser;
import org.springframework.data.repository.CrudRepository;

public interface SpecialUserRepository extends CrudRepository<SpecialUser, Long> {

    /**
     * search for a user given its login
     *
     * @param login the user's login
     * @return the found {@link SpecialUser} object, or {@literal null}
     */
    SpecialUser findByLogin(String login);
}