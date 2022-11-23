/*
 * Copyright 2021-2022 European Commission
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

import eu.europa.ec.digit.leos.pilot.export.exception.LegDocumentException;
import eu.europa.ec.digit.leos.pilot.export.model.LeosConvertDocumentInput;
import eu.europa.ec.digit.leos.pilot.export.service.LeosLegDocumentService;
import eu.europa.ec.digit.leos.pilot.export.util.ZipUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Map;

@Service
public class LeosLegDocumentServiceImpl implements LeosLegDocumentService {

    private static final Logger LOG = LoggerFactory.getLogger(LeosLegDocumentServiceImpl.class);

    public LeosLegDocumentServiceImpl() {
    }

    public byte[] updateWithTranslations(LeosConvertDocumentInput convertDocumentInput) {
        try {
            Map<String, Object> contentToZip = ZipUtil.unzipByteArray(convertDocumentInput.getInputFile().getBytes());
            contentToZip.putAll(ZipUtil.unzipByteArray(convertDocumentInput.getTranslationsFile().getBytes()));
            return ZipUtil.zipByteArray(contentToZip);
        } catch (IOException e) {
            LOG.info("Issue converting document", e);
            throw new LegDocumentException("Issue converting document", e);
        }
    }
    
}
