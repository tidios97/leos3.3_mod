/*
 * Copyright 2020 European Commission
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
package eu.europa.ec.digit.leos.pilot.export;

import eu.europa.ec.digit.leos.pilot.export.service.MockService;
import eu.europa.ec.digit.leos.pilot.export.util.ConstantsTestsUtil;
import eu.europa.ec.digit.leos.pilot.export.util.MetadataTestsUtil;
import eu.europa.ec.digit.leos.pilot.export.util.MetadataTestsUtil.MetadataTestConfiguration;
import eu.europa.ec.digit.leos.pilot.export.model.metadata.MetadataLocationType;
import eu.europa.ec.digit.leos.pilot.export.model.metadata.MetadataLanguageDateFormat;
import jdk.nashorn.internal.ir.annotations.Ignore;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.util.Assert;

import java.io.File;
import java.nio.file.Files;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AKN4EUUtilApplicationTests {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private MockService mockService;

    private MockMultipartFile convertToMockMultipartFile(String mockFileName) throws Exception {
        File resource = new ClassPathResource(mockFileName).getFile();
        return new MockMultipartFile("inputFile", resource.getName(), null, Files.readAllBytes(resource.toPath()));
    }

    private ResultActions createConvertRequest(String outputDescriptor) throws Exception {
        MockMultipartFile mockMultipartFile = convertToMockMultipartFile(mockService.getMockInputFilename("leg"));
        MockMultipartHttpServletRequestBuilder builder = MockMvcRequestBuilders.multipart("/convertDocument")
                .file(mockMultipartFile);
        builder.param("outputDescriptor", outputDescriptor);
        return mvc.perform(builder);
    }

    @Ignore
    public void convertLegToLw() throws Exception {
        MvcResult mvcResult = createConvertRequest("{ \"format\" : [\"LW\"], \"mode\" : \"individual\", \"convertAnnotations\" : \"yes\", \"documents\" : [\"bill_xxxxx.xml\"] }")
                .andExpect(status().isOk())
                .andExpect(content().contentType(ConstantsTestsUtil.APPLICATION_ZIP_VALUE))
                .andReturn();
        byte[] content = mvcResult.getResponse().getContentAsByteArray();
        Assert.notNull(content, "Content is null");
    }

    @Ignore
    public void convertLegToPdf() throws Exception {
        MvcResult mvcResult = createConvertRequest("{ \"format\" : [\"PDF\"], \"mode\" : \"individual\", \"convertAnnotations\" : \"yes\", \"documents\" : [\"bill_xxxxx.xml\"] }")
                .andExpect(status().isOk())
                .andExpect(content().contentType(ConstantsTestsUtil.APPLICATION_ZIP_VALUE))
                .andReturn();
        byte[] content = mvcResult.getResponse().getContentAsByteArray();
        Assert.notNull(content, "Content is null");
    }

    private ResultActions createGetRenditionsRequest(Boolean isWithAnnotations) throws Exception {
        MockMultipartFile mockMultipartFile = convertToMockMultipartFile(mockService.getMockInputFilename("xml"));
        MockMultipartHttpServletRequestBuilder builder = MockMvcRequestBuilders.multipart("/getRenditions")
                .file(mockMultipartFile);
        if (isWithAnnotations != null) {
            builder.param("isWithAnnotations", Boolean.toString(isWithAnnotations));
        }
        return mvc.perform(builder);
    }

    @Test
    public void getRenditions() throws Exception {
        MvcResult mvcResult = createGetRenditionsRequest(null)
                .andExpect(status().isOk())
                .andExpect(content().contentType(ConstantsTestsUtil.APPLICATION_ZIP_VALUE))
                .andReturn();
        byte[] content = mvcResult.getResponse().getContentAsByteArray();
        Assert.notNull(content, "Content is null");
    }

    @Test
    public void applyMetadataTest() throws Exception {
        MvcResult mvcResult = createApplyMetadataRequest("amd")
                .andExpect(status().isOk())
                .andExpect(content().contentType(ConstantsTestsUtil.APPLICATION_ZIP_VALUE))
                .andReturn();
        byte[] content = mvcResult.getResponse().getContentAsByteArray();
        Assert.notNull(content, "Content is null");
        MetadataTestsUtil.checkMetadataResponse(content);
    }

    @Test
    public void applyMetadataLuxembourgLocationTest() throws Exception {
        MvcResult mvcResult = createApplyMetadataRequest("amd_lux")
                .andExpect(status().isOk())
                .andExpect(content().contentType(ConstantsTestsUtil.APPLICATION_ZIP_VALUE))
                .andReturn();
        byte[] content = mvcResult.getResponse().getContentAsByteArray();
        Assert.notNull(content, "Content is null");
        MetadataTestConfiguration configuration = MetadataTestConfiguration.withLocationTypeToTest(MetadataLocationType.LUXEMBOURG);
        MetadataTestsUtil.checkMetadataResponse(content, configuration);
    }

    @Test
    public void applyMetadataStrasbourgLocationTest() throws Exception {
        MvcResult mvcResult = createApplyMetadataRequest("amd_str")
                .andExpect(status().isOk())
                .andExpect(content().contentType(ConstantsTestsUtil.APPLICATION_ZIP_VALUE))
                .andReturn();
        byte[] content = mvcResult.getResponse().getContentAsByteArray();
        Assert.notNull(content, "Content is null");
        MetadataTestConfiguration configuration = MetadataTestConfiguration.withLocationTypeToTest(MetadataLocationType.STRASBOURG);
        MetadataTestsUtil.checkMetadataResponse(content, configuration);
    }

    @Test
    public void applyMetadataHunDateFormatTest() throws Exception {
        MvcResult mvcResult = createApplyMetadataRequest("amd_hun")
                .andExpect(status().isOk())
                .andExpect(content().contentType(ConstantsTestsUtil.APPLICATION_ZIP_VALUE))
                .andReturn();
        byte[] content = mvcResult.getResponse().getContentAsByteArray();
        Assert.notNull(content, "Content is null");
        MetadataTestConfiguration configuration = MetadataTestConfiguration.withDateFormatTypeToTest(MetadataLanguageDateFormat.HU);
        MetadataTestsUtil.checkMetadataResponse(content, configuration);
    }

    @Test
    public void applyMetadataLitDateFormatTest() throws Exception {
        MvcResult mvcResult = createApplyMetadataRequest("amd_lit")
                .andExpect(status().isOk())
                .andExpect(content().contentType(ConstantsTestsUtil.APPLICATION_ZIP_VALUE))
                .andReturn();
        byte[] content = mvcResult.getResponse().getContentAsByteArray();
        Assert.notNull(content, "Content is null");
        MetadataTestConfiguration configuration = MetadataTestConfiguration.withDateFormatTypeToTest(MetadataLanguageDateFormat.LT);
        MetadataTestsUtil.checkMetadataResponse(content, configuration);
    }

    @Test
    public void applyMetadataSlkDateFormatTest() throws Exception {
        MvcResult mvcResult = createApplyMetadataRequest("amd_slk")
                .andExpect(status().isOk())
                .andExpect(content().contentType(ConstantsTestsUtil.APPLICATION_ZIP_VALUE))
                .andReturn();
        byte[] content = mvcResult.getResponse().getContentAsByteArray();
        Assert.notNull(content, "Content is null");
        MetadataTestConfiguration configuration = MetadataTestConfiguration.withDateFormatTypeToTest(MetadataLanguageDateFormat.SK);
        MetadataTestsUtil.checkMetadataResponse(content, configuration);
    }

    @Test
    public void applyMetadataHrvDateFormatTest() throws Exception {
        MvcResult mvcResult = createApplyMetadataRequest("amd_hrv")
                .andExpect(status().isOk())
                .andExpect(content().contentType(ConstantsTestsUtil.APPLICATION_ZIP_VALUE))
                .andReturn();
        byte[] content = mvcResult.getResponse().getContentAsByteArray();
        Assert.notNull(content, "Content is null");
        MetadataTestConfiguration configuration = MetadataTestConfiguration.withDateFormatTypeToTest(MetadataLanguageDateFormat.HR);
        MetadataTestsUtil.checkMetadataResponse(content, configuration);
    }

    private ResultActions createApplyMetadataRequest(String mockInputFilename) throws Exception {
        MockMultipartFile mockMultipartFile = convertToMockMultipartFile(mockService.getMockInputFilename(mockInputFilename));
        MockMultipartHttpServletRequestBuilder builder = MockMvcRequestBuilders.multipart("/applyMetadata")
                .file(mockMultipartFile);
        return mvc.perform(builder);
    }
/*
    @Test
    public void convertXmlToHtmlWithoutOutputType() throws Exception {
        convertWithOK(EC_CONVERT_DOCUMENT_URI, EC_SERVICE_TYPE, mockService.getMockInputFilename("xml"), MediaType.APPLICATION_XML_VALUE, null, "true");
    }

    @Ignore
    public void convertLegToPdfWithoutAnnotations() throws Exception {
        convertWithOK(EC_CONVERT_DOCUMENT_URI, EC_SERVICE_TYPE, mockService.getMockInputFilename("leg"), ZipUtils.APPLICATION_ZIP_VALUE, "pdf", "false");
    }

    @Test
    public void convertErrorOnOutputParam() throws Exception {
        convertWithBadRequest(EC_CONVERT_DOCUMENT_URI, mockService.getMockInputFilename("leg"), ZipUtils.APPLICATION_ZIP_VALUE, "aaa", "true");
    }

    @Test
    public void convertErrorOnContentTypeWithWrongFilename() throws Exception {
        convertWithBadRequest(EC_CONVERT_DOCUMENT_URI, mockService.getMockInputFilename("xml"), MediaType.TEXT_PLAIN_VALUE, "pdf", "true");
    }

    @Test
    public void convertErrorWrongAnnotation() throws Exception {
        convertWithBadRequest(EC_CONVERT_DOCUMENT_URI, mockService.getMockInputFilename("leg"), ZipUtils.APPLICATION_ZIP_VALUE, "pdf", "aaa");
    }

    @Test
    public void convertErrorOnOutputHtmlWithoutXml() throws Exception {
        convertWithBadRequest(EC_CONVERT_DOCUMENT_URI, mockService.getMockInputFilename("leg"), ZipUtils.APPLICATION_ZIP_VALUE, "html", "true");
    }

    @Test
    public void convertXmlToPdf() throws Exception {
        convertWithOK(EC_CONVERT_DOCUMENT_URI, EC_SERVICE_TYPE, mockService.getMockInputFilename("xml"), MediaType.APPLICATION_XML_VALUE, "pdf", "true");
    }

    @Test
    public void cnConvertErrorLegToPdf() throws Exception {
        convertWithBadRequest(CN_CONVERT_DOCUMENT_URI, mockService.getMockInputFilename("leg"), ZipUtils.APPLICATION_ZIP_VALUE, "pdf", "true");
    }

    @Ignore
    public void cnConvertLegToLw() throws Exception {
        convertWithOK(CN_CONVERT_DOCUMENT_URI, CN_SERVICE_TYPE, mockService.getMockInputFilename("leg"), ZipUtils.APPLICATION_ZIP_VALUE, "lw", "true");
    }*/
}
