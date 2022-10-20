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
package eu.europa.ec.leos.annotate.unit.services;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.europa.ec.leos.annotate.model.web.annotation.JsonSearchResult;
import eu.europa.ec.leos.annotate.services.ZipService;
import eu.europa.ec.leos.annotate.services.impl.util.ZipContent;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest(properties = "spring.config.name=anot")
@ActiveProfiles("test")
@SuppressWarnings("PMD.TooManyMethods")
public class ZipServiceTest {

    @Autowired
    private ZipService zipService;

    private InputStream zipInputStream;

    final private static String MISSING_ZIP_CONTENT_MESSAGE = "Expected zip content is missing";

    @Before
    public void beforeTest() {
        try {
            this.zipInputStream = new ClassPathResource("samples/ZipServiceSample.zip").getInputStream();
        }
        catch(Exception ex) {
            Assert.fail("Error reading zip sample file");
        }
    }

    @After
    public void afterTest() {
        try {
            zipInputStream.close();
        }
        catch(Exception ex) {
        }
    }

    @Test
    public void testAddContentToList() {
        final List<ZipContent> zipContentList = new ArrayList<>();
        zipService.addContentToList(zipContentList, "test1.json", "{}".getBytes(StandardCharsets.UTF_8));
        Assert.assertEquals("Unexpected size of zip content list",1, zipContentList.size());
        Assert.assertEquals("Unexpected name of zip content", "test1.json", zipContentList.get(0).getFullName());
    }

    @Test
    public void testRemoveContentFromList() {
        final List<ZipContent> zipContentList = new ArrayList<>();
        zipService.addContentToList(zipContentList, "test1.json", "{}".getBytes(StandardCharsets.UTF_8));
        zipService.removeContentFromList(zipContentList, zipContentList.get(0));
        Assert.assertEquals("Unexpected size of zip content list",0, zipContentList.size());
    }

    @Test
    public void testRemoveContentWithFullNameFromList() {
        final List<ZipContent> zipContentList = new ArrayList<>();
        zipService.addContentToList(zipContentList, "test1.json", "{}".getBytes(StandardCharsets.UTF_8));
        zipService.removeContentWithFullNameFromList(zipContentList, "test1.json");
        Assert.assertEquals("Unexpected size of zip content list",0, zipContentList.size());
    }

    @Test
    public void testIsContentInList() {
        final List<ZipContent> zipContentList = new ArrayList<>();
        zipService.addContentToList(zipContentList, "test1.json", "{}".getBytes(StandardCharsets.UTF_8));
        final ZipContent zipContent = new ZipContent("test2.json", "{}".getBytes(StandardCharsets.UTF_8));

        Assert.assertTrue("Content should be in list but is not found", zipService.isContentInList(zipContentList, zipContentList.get(0)));
        Assert.assertFalse("Content should NOT be in list but was found", zipService.isContentInList(zipContentList, zipContent));
    }

    @Test
    public void testIsContentWithFullNameInList() {
        final List<ZipContent> zipContentList = new ArrayList<>();
        zipService.addContentToList(zipContentList, "test1.json", "{}".getBytes(StandardCharsets.UTF_8));

        Assert.assertTrue("Content should be in list but is not found", zipService.isContentWithFullNameInList(zipContentList, "test1.json"));
        Assert.assertFalse("Content should NOT be in list but was found", zipService.isContentWithFullNameInList(zipContentList, "test2.json"));
    }

    @Test
    public void testAddJsonSearchResultToList() throws Exception {
        final List<ZipContent> zipContentList = new ArrayList<>();
        zipService.addContentToList(zipContentList, "media/annot_document1.json", "{}".getBytes(StandardCharsets.UTF_8));
        final Jackson2ObjectMapperBuilder builder = new Jackson2ObjectMapperBuilder();
        builder.featuresToEnable(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES);

        final ObjectMapper objectMapper = builder.build();
        final JsonSearchResult jsonSearchResult = new JsonSearchResult(Collections.emptyList(), 0);

        zipService.addJsonSearchResultToList(zipContentList, "document1", jsonSearchResult, objectMapper);
        Assert.assertEquals("Unexpected size of zip content list",1, zipContentList.size());
        Assert.assertEquals("Unexpected name of zip content", "media/annot_document1.json",
                zipContentList.get(0).getFullName());
    }

    @Test
    public void testUnzipBytes() {
        final List<ZipContent> zipContentList = unzipInputStream(zipInputStream);
        Assert.assertEquals("Unexpected amount of zip content", 3, zipContentList.size());
        Assert.assertEquals(MISSING_ZIP_CONTENT_MESSAGE, 1,
                zipContentList.stream().filter(zipContent -> zipContent.getFullName().equals("media/annot_dummy_bill_for_test.xml.json")).count());
        Assert.assertEquals(MISSING_ZIP_CONTENT_MESSAGE, 1,
                zipContentList.stream().filter(zipContent -> zipContent.getFullName().equals("main.xml")).count());
        Assert.assertEquals(MISSING_ZIP_CONTENT_MESSAGE, 1,
                zipContentList.stream().filter(zipContent -> zipContent.getFullName().equals("dummy_bill_for_test.xml")).count());
    }

    @Test
    public void testZipContentToBytes() {

        List<ZipContent> zipContentList = new ArrayList<>();
        zipContentList.add(new ZipContent("test1.json", "{ \"message\": \"Test text\" }".getBytes(StandardCharsets.UTF_8)));
        zipContentList.add(new ZipContent("folder/test2.json", "{ \"message\": \"New folder test\" }".getBytes(StandardCharsets.UTF_8)));
        
        try {
            final byte[] zipBytes = this.zipService.zipContentToBytes(zipContentList);
            Assert.assertTrue(zipBytes.length > 0);
            // Unzip content again to check if zip bytes were created properly
            zipContentList.clear();
            zipContentList = this.zipService.unzipBytes(zipBytes);
            Assert.assertEquals(MISSING_ZIP_CONTENT_MESSAGE, 1,
                    zipContentList.stream().filter(zipContent -> zipContent.getFullName().equals("test1.json")).count());
            Assert.assertEquals(MISSING_ZIP_CONTENT_MESSAGE, 1,
                    zipContentList.stream().filter(zipContent -> zipContent.getFullName().equals("folder/test2.json")).count());
        }
        catch(IOException ex) {
            Assert.fail("Failed while converting zip content to bytes");
        }

    }

    @Test
    public void testGetZipResponseHeaders() {
        final HttpHeaders httpHeaders = this.zipService.getZipResponseHeaders(128);
        Assert.assertEquals("Unexpected header content length", 128, httpHeaders.getContentLength());

        final List<String> headerValues = httpHeaders.get(HttpHeaders.CONTENT_DISPOSITION);
        Assert.assertFalse("Content disposition is missing", headerValues.isEmpty());
        final String headerValue = headerValues.get(0);
        Assert.assertTrue("Content disposition filename is missing", headerValue.contains("filename="));

        final String filename = headerValue.substring(headerValue.indexOf("filename=") + 9).replace("\"", "");
        Assert.assertTrue("Content disposition filename prefix is not as expected", filename.startsWith("processed_leg_"));
        Assert.assertTrue("Content disposition file extension is not as expected", filename.endsWith(".zip"));
    }

    @Test
    public void testZipContent() {
        final List<ZipContent> zipContentList = unzipInputStream(zipInputStream);
        final ZipContent zipContent = zipContentList.stream()
                .filter(content -> content.getFullName().equals("media/annot_dummy_bill_for_test.xml.json"))
                .findFirst().orElse(null);

        Assert.assertNotNull("ZipContent is null", zipContent);
        Assert.assertEquals("ZipContent full name is not as expected", "media/annot_dummy_bill_for_test.xml.json",
                zipContent.getFullName());
        Assert.assertEquals("ZipContent filename is not as expected", "annot_dummy_bill_for_test.xml.json",
                zipContent.getFilename());
        Assert.assertEquals("ZipContent name is not as expected", "annot_dummy_bill_for_test.xml",
                zipContent.getName());
        Assert.assertEquals("ZipContent path is not as expected", "media/",
                zipContent.getPath());
        Assert.assertEquals("ZipContent size is not as expected", 225781, zipContent.getData().length);
    }

    private List<ZipContent> unzipInputStream(final InputStream inputStream) {
        try {
            final byte[] zipBytes = StreamUtils.copyToByteArray(inputStream);
            Assert.assertNotNull("Byte array of zip file is empty", zipBytes);
            return this.zipService.unzipBytes(zipBytes);
        }
        catch(IOException ex) {
            Assert.fail("Failed to unzip stream");
            return null;
        }
    }
}
