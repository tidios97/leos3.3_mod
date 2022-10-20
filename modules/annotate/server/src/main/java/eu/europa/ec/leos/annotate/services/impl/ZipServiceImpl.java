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
package eu.europa.ec.leos.annotate.services.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.europa.ec.leos.annotate.model.web.annotation.JsonSearchResult;
import eu.europa.ec.leos.annotate.services.ZipService;
import eu.europa.ec.leos.annotate.services.impl.util.ZipContent;
import org.apache.commons.io.IOUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

@Service
public class ZipServiceImpl implements ZipService {

    // -------------------------------------
    // Constructors & Setters
    // -------------------------------------

    public ZipServiceImpl() {
        // required default constructor for autowired instantiation
    }

    /**
     * Convert a List with {@link ZipContent} objects to a zip file
     * @param zipContentList List with {@link ZipContent} objects
     * @throws IOException if zip process fail
     * @return Bytes of the zip file as byte array
     * */
    @Override
    public byte[] zipContentToBytes(final List<ZipContent> zipContentList) throws IOException {
        final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        final ZipOutputStream zipOutputStream = new ZipOutputStream(byteArrayOutputStream);

        for (final ZipContent zipContent : zipContentList) {
            final ZipEntry zipEntry = new ZipEntry(zipContent.getFullName());
            zipOutputStream.putNextEntry(zipEntry);
            zipOutputStream.write(zipContent.getData());
            zipOutputStream.closeEntry();
        }

        zipOutputStream.close();
        return byteArrayOutputStream.toByteArray();
    }

    /**
     * Unzip a file and returning the content as list
     * @param bytes Bytes to unzip
     * @throws IOException if unzip process fail
     * @return List of {@link ZipContent} objects
     * */
    @Override
    public List<ZipContent> unzipBytes(final byte[] bytes) throws IOException {
        final List<ZipContent> zipFiles = new ArrayList<>();
        try (ZipInputStream zipInputStream = new ZipInputStream(new ByteArrayInputStream(bytes))) {
            ZipEntry zipEntry;
            while ((zipEntry = zipInputStream.getNextEntry()) != null) {
                if (!zipEntry.isDirectory()) {
                    addContentToList(zipFiles, zipEntry.getName(), IOUtils.toByteArray(zipInputStream));
                }
                zipInputStream.closeEntry();
            }
        }
        return zipFiles;
    }

    /**
     * Returns the headers for a zip response
     * @param contentLength Content length for the response
     * @return @{link HttpHeaders} object with headers for a zip response
     * */
    @Override
    public HttpHeaders getZipResponseHeaders(final int contentLength) {
        return getZipResponseHeaders(contentLength, "zip");
    }

    @Override
    public HttpHeaders getZipResponseHeaders(final int contentLength, final String fileExtension) {
        final HttpHeaders headers = new HttpHeaders();
        headers.setContentDispositionFormData("attachment", String.format("processed_leg_%s.%s",
                System.currentTimeMillis(), fileExtension));
        headers.setContentLength(contentLength);
        return headers;
    }

    /**
     * Add new {@link ZipContent} to a list
     * @param zipContentList {@link ZipContent}-List
     * @param fullName Full name including path within the zip file
     * @param data Bytes of the zip content
     * */
    @Override
    public void addContentToList(final List<ZipContent> zipContentList, final String fullName, final byte[] data) {
        final ZipContent newContent = new ZipContent(fullName, data);
        zipContentList.add(newContent);
    }

    /**
     * Removes content from a {@link ZipContent}-List
     * @param zipContentList {@link ZipContent}-List
     * @param content {@link ZipContent} object to remove
     * @return `true` if the content was in the list else `false`
     * */
    @Override
    public boolean removeContentFromList(final List<ZipContent> zipContentList, final ZipContent content) {
        return removeContentWithFullNameFromList(zipContentList, content.getFullName());
    }

    /**
     * Removes content from from a {@link ZipContent}-List
     * @param zipContentList {@link ZipContent}-List
     * @param fullName Full name of the zip content to remove
     * @return `true` if the content with full name was in the list else `false`
     * */
    @Override
    public boolean removeContentWithFullNameFromList(final List<ZipContent> zipContentList, final String fullName) {
        final ZipContent contentToRemove = zipContentList.stream()
                .filter(content -> content.getFullName().equals(fullName))
                .findFirst().orElse(null);

        if (contentToRemove != null) {
            return zipContentList.remove(contentToRemove);
        }
        return false;
    }

    /**
     * Returns if given content is in the list
     * @param zipContentList {@link ZipContent}-List
     * @param content {@link ZipContent} to look for
     * @return `true` if the content was in the list else `false`
     * */
    @Override
    public boolean isContentInList(final List<ZipContent> zipContentList, final ZipContent content) {
        return isContentWithFullNameInList(zipContentList, content.getFullName());
    }

    /**
     * Returns if given content is in the list
     * @param zipContentList {@link ZipContent}-List
     * @param fullName Full name of the zip content to look for
     * @return `true` if the content with full name was in the list else `false`
     * */
    @Override
    public boolean isContentWithFullNameInList(final List<ZipContent> zipContentList, final String fullName) {
        return zipContentList.stream().anyMatch(content -> content.getFullName().equals(fullName));
    }

    /**
     * Adds a {@link JsonSearchResult} object to a {@link ZipContent}-List
     * @param zipContentList {@link ZipContent}-List to add a {@link JsonSearchResult}
     * @param documentName Document name of the {@link JsonSearchResult}
     * @param jsonSearchResult {@link JsonSearchResult} to add to the list
     * @param objectMapper {@link ObjectMapper} to use for JSON conversation
     * */
    @Override
    public void addJsonSearchResultToList(final List<ZipContent> zipContentList,
                                          final String documentName,
                                          final JsonSearchResult jsonSearchResult,
                                          final ObjectMapper objectMapper) throws JsonProcessingException {
        final byte[] jsonBytes = objectMapper.writeValueAsBytes((jsonSearchResult == null) ? new JsonSearchResult(Collections.emptyList(), 0) : jsonSearchResult);
        final String fullFileName = String.format("media/annot_%s.json", documentName);

        if (isContentWithFullNameInList(zipContentList, fullFileName)) {
            removeContentWithFullNameFromList(zipContentList, fullFileName);
        }
        addContentToList(zipContentList, fullFileName, jsonBytes);
    }
}
