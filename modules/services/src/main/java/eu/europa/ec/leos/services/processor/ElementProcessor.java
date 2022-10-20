/*
 * Copyright 2017 European Commission
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


import eu.europa.ec.leos.domain.cmis.document.XmlDocument;
import eu.europa.ec.leos.model.xml.Element;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

public interface ElementProcessor<T extends XmlDocument> {
    /**
     * Retrieves the element from the given document
     * @param document The document containing the article
     * @param elementId The  id of element
     * @return the xml string representation of the element
     */
    String getElement(XmlDocument document, String elementName, String elementId);

    /**
     * Retrieves first sibling after of the element from the given document
     * @param document The document containing the article
     * @param elementId The id of element
     * @return information about the element (elementId, elementTagName and elementFragment)
     */
    Element getSiblingElement(T document, String elementName, String elementId, boolean previous);

    /**
     * Retrieves child specified of the element from the given document
     * @param document The document containing the article
     * @param elementName The tag of element
     * @param elementId The id of element
     * @param elementTags Tags to take into account to retrieve the child or empty for all tags
     * @param position The position of child element to be retrieved
     * @return information about the element (elementId, elementTagName and elementFragment)
     */
    Element getChildElement(T document, String elementName, String elementId, List<String> elementTags, int position);

    /**
     * Retrieves parent of the element from the given document
     * @param document The document containing the article
     * @param elementId The id of element
     * @return elementId of parent element
     */
    String getParentElement(T document, String elementId);

    /**
     * Saves the new elemenContent of an existing element to the given document
     * or deletes the element if the given elementContent is null
     * @param document The document to update
     * @param elementContent The new element content
     * @param parentElementId The id of the parent element
     * @return The updated document or throws a LeosDocumentNotLockedByCurrentUserException if the given user doesn't have a lock on the document
     */
    @PreAuthorize("hasPermission(#document, 'CAN_UPDATE')")
    byte[] addChildToParent(T document, String elementContent, String parentElementId);

    /**
     * Saves the new elemenContent of an existing element to the given document
     * or deletes the element if the given elementContent is null
     * @param document The document to update
     * @param elementContent The new article content, or null to delete the element
     * @param elementName The element Tag Name
     * @param elementId The id of the element
     * @return The updated document or throws a LeosDocumentNotLockedByCurrentUserException if the given user doesn't have a lock on the document
     */
    @PreAuthorize("hasPermission(#document, 'CAN_UPDATE')")
    byte[] updateElement(T document, String elementContent, String elementName, String elementId);

    /**
     * Deletes an element with the given id and saves the document.
     * @param document The document to update
     * @param elementId The id of the element which is to be deleted.
     * @return The updated document or throws a LeosDocumentNotLockedByCurrentUserException if the given user doesn't have a lock on the document
     */
    @PreAuthorize("hasPermission(#document, 'CAN_UPDATE')")
    byte[] deleteElement(T document, String elementId, String elementType) throws Exception;

    /**
     * Retrieves elements containing soft action
     * @param xmlContent The xmlContent to search for element
     * @return information about the element (elementId, elementTagName and elementFragment)
     */
    List<Element> getChangedElements(byte[] xmlContent);

    /**
     * searches the {@param origText} in the element {@param elementId} and replace it with the {@param newText}.
     * @param origText
     * @param elementId
     * @param startOffset
     * @param endOffset
     * @param newText
     * @return: On success returns updated content. On failure throws exception.
     */
    @PreAuthorize("hasPermission(#document, 'CAN_MERGE_SUGGESTION')")
    byte[] replaceTextInElement(T document, String origText, String newText, String elementId, int startOffset, int endOffset);
    
    String getElementAttributeValueByNameAndId(T document, String attributeName, String tagName, String idAttributeValue);

    byte[] insertAttribute(byte[] xmlContent, String elementTag, String elementId, String attrName, String attrVal);

    byte[] removeAttribute(byte[] xmlContent, String elementId, String attrName);

    String getElementAttributeValue(byte[] xmlContent, String attributeName, String tagName, String idAttributeValue);
}
