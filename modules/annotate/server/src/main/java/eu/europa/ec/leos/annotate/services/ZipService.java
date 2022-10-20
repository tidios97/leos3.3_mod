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
package eu.europa.ec.leos.annotate.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.europa.ec.leos.annotate.model.web.annotation.JsonSearchResult;
import eu.europa.ec.leos.annotate.services.impl.util.ZipContent;
import org.springframework.http.HttpHeaders;

import java.io.IOException;
import java.util.List;

public interface ZipService {
    /**
     * Add new {@link ZipContent} to a list
     * @param zipContentList {@link ZipContent}-List
     * @param fullName Full name including path within the zip file
     * @param data Bytes of the zip content
     * */
    void addContentToList(List<ZipContent> zipContentList, final String fullName, byte[] data);

    /**
     * Removes content from a {@link ZipContent}-List
     * @param zipContentList {@link ZipContent}-List
     * @param content {@link ZipContent} object to remove
     * @return `true` if the content was in the list else `false`
     * */
    boolean removeContentFromList(List<ZipContent> zipContentList, final ZipContent content);

    /**
     * Removes content from from a {@link ZipContent}-List
     * @param zipContentList {@link ZipContent}-List
     * @param fullName Full name of the zip content to remove
     * @return `true` if the content with full name was in the list else `false`
     * */
    boolean removeContentWithFullNameFromList(List<ZipContent> zipContentList, final String fullName);

    /**
     * Returns if given content is in the list
     * @param zipContentList {@link ZipContent}-List
     * @param content {@link ZipContent} to look for
     * @return `true` if the content was in the list else `false`
     * */
    boolean isContentInList(final List<ZipContent> zipContentList, final ZipContent content);

    /**
     * Returns if given content is in the list
     * @param zipContentList {@link ZipContent}-List
     * @param fullName Full name of the zip content to look for
     * @return `true` if the content with full name was in the list else `false`
     * */
    boolean isContentWithFullNameInList(final List<ZipContent> zipContentList, final String fullName);

    /**
     * Unzip a file and returning the content as list
     * @param bytes Bytes to unzip
     * @throws IOException if unzip process fail
     * @return List of {@link ZipContent} objects
     * */
    List<ZipContent> unzipBytes(byte[] bytes) throws IOException;

    /**
     * Convert a List with {@link ZipContent} objects to a zip file
     * @param zipContentList List with {@link ZipContent} objects
     * @throws IOException if zip process fail
     * @return Bytes of the zip file as byte array
     * */
    byte[] zipContentToBytes(final List<ZipContent> zipContentList) throws IOException;

    /**
     * Returns the headers for a zip response
     * @param contentLength Content length for the response
     * @return @{link HttpHeaders} object with headers for a zip response
     * */
    HttpHeaders getZipResponseHeaders(final int contentLength);

    /**
     * Returns the headers for a zip response
     * @param contentLength Content length for the response
     * @param fileExtension File extension for the resulting zip
     * @return @{link HttpHeaders} object with headers for a zip response
     * */
    HttpHeaders getZipResponseHeaders(final int contentLength, final String fileExtension);

    /**
     * Adds a {@link JsonSearchResult} object to a {@link ZipContent}-List
     *
     * If a content with the new content's documentName already exists, the method will
     * replace the old content with new one.
     *
     * @param zipContentList {@link ZipContent}-List to add a {@link JsonSearchResult}
     * @param documentName Document name of the {@link JsonSearchResult}
     * @param jsonSearchResult {@link JsonSearchResult} to add to the list
     * @param objectMapper {@link ObjectMapper} to use for JSON conversation
     * */
    void addJsonSearchResultToList(List<ZipContent> zipContentList,
                                   final String documentName,
                                   final JsonSearchResult jsonSearchResult,
                                   final ObjectMapper objectMapper) throws JsonProcessingException;
}
