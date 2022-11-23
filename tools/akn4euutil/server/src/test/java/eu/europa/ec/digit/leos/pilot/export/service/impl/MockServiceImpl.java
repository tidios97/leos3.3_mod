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
package eu.europa.ec.digit.leos.pilot.export.service.impl;

import eu.europa.ec.digit.leos.pilot.export.exception.MockServiceException;
import eu.europa.ec.digit.leos.pilot.export.service.MockService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import static eu.europa.ec.digit.leos.pilot.export.util.ConstantsTestsUtil.AMD_INPUT_TYPE;
import static eu.europa.ec.digit.leos.pilot.export.util.ConstantsTestsUtil.AMD_LANG_HRV_INPUT_TYPE;
import static eu.europa.ec.digit.leos.pilot.export.util.ConstantsTestsUtil.AMD_LANG_HUN_INPUT_TYPE;
import static eu.europa.ec.digit.leos.pilot.export.util.ConstantsTestsUtil.AMD_LANG_LIT_INPUT_TYPE;
import static eu.europa.ec.digit.leos.pilot.export.util.ConstantsTestsUtil.AMD_LANG_SLK_INPUT_TYPE;
import static eu.europa.ec.digit.leos.pilot.export.util.ConstantsTestsUtil.AMD_LUX_INPUT_TYPE;
import static eu.europa.ec.digit.leos.pilot.export.util.ConstantsTestsUtil.AMD_STR_INPUT_TYPE;
import static eu.europa.ec.digit.leos.pilot.export.util.ConstantsTestsUtil.HTML_OUTPUT_TYPE;
import static eu.europa.ec.digit.leos.pilot.export.util.ConstantsTestsUtil.XML_INPUT_TYPE;

@Service
public class MockServiceImpl implements MockService {
    private static final Map<String, String> MOCK_OUTPUT_MAPPING = new HashMap<>();
    private static final Map<String, String> MOCK_INPUT_MAPPING = new HashMap<>();

    private static final Logger LOG = LoggerFactory.getLogger(MockServiceImpl.class);

    public MockServiceImpl() {
        MOCK_OUTPUT_MAPPING.put(HTML_OUTPUT_TYPE, "samples/HtmlSample.zip");
        MOCK_INPUT_MAPPING.put(XML_INPUT_TYPE, "samples/BillSample.xml");
        MOCK_INPUT_MAPPING.put(AMD_INPUT_TYPE, "samples/ApplyMetadataSample.zip");
        MOCK_INPUT_MAPPING.put(AMD_LUX_INPUT_TYPE, "samples/ApplyMetadataLuxembourgSample.zip");
        MOCK_INPUT_MAPPING.put(AMD_STR_INPUT_TYPE, "samples/ApplyMetadataStrasbourgSample.zip");
        MOCK_INPUT_MAPPING.put(AMD_LANG_HUN_INPUT_TYPE, "samples/ApplyMetadataLanguageHunSample.zip");
        MOCK_INPUT_MAPPING.put(AMD_LANG_LIT_INPUT_TYPE, "samples/ApplyMetadataLanguageLitSample.zip");
        MOCK_INPUT_MAPPING.put(AMD_LANG_SLK_INPUT_TYPE, "samples/ApplyMetadataLanguageSlkSample.zip");
        MOCK_INPUT_MAPPING.put(AMD_LANG_HRV_INPUT_TYPE, "samples/ApplyMetadataLanguageHrvSample.zip");
    }

    private String getMockOutputFilename(String output) {
        return MOCK_OUTPUT_MAPPING.get(output.toUpperCase());
    }

    public String getMockInputFilename(String input) {
        return MOCK_INPUT_MAPPING.get(input.toUpperCase());
    }

    public byte[] getMockData(String output) throws MockServiceException {
        String mockFilename = getMockOutputFilename(output);
        byte[] documentInput;
        try {
            InputStream resource = new ClassPathResource(mockFilename).getInputStream();
            documentInput = StreamUtils.copyToByteArray(resource);
        } catch (IOException e) {
            String errorMessage = String.format("Can't read resource: %s", mockFilename);
            LOG.error(errorMessage);
            throw new MockServiceException(errorMessage, e);
        }
        return documentInput;
    }
}
