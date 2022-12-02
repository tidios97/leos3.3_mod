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
package eu.europa.ec.leos.services.processor;

import eu.europa.ec.leos.domain.cmis.document.FinancialStatement;
import org.springframework.security.access.prepost.PreAuthorize;

public interface FinancialStatementProcessor {

    /** This method updates the attribute of an element
     * @param document
     * @param elementName: tag name of the element where attribute should be updated
     * @param elementId: id of the element where attribute should be updated
     * @param elementFragment: updated content of the element
     * @return udpated xml content
     */
    @PreAuthorize("hasPermission(#document, 'CAN_UPDATE')")
    byte[] updateElement(FinancialStatement document, String elementName, String elementId, String elementFragment) throws Exception;

}
