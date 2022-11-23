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

import eu.europa.ec.digit.leos.pilot.export.model.LeosConvertDocumentInput;
import eu.europa.ec.digit.leos.pilot.export.service.LeosDocumentService;
import eu.europa.ec.digit.leos.pilot.export.service.LeosLegDocumentService;
import eu.europa.ec.digit.leos.pilot.export.service.MetadataService;
import eu.europa.ec.digit.leos.pilot.export.service.XmlDocumentService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class LeosDocumentServiceImpl implements LeosDocumentService {

    private final LeosLegDocumentService leosLegDocumentService;
    private final XmlDocumentService xmlDocumentService;
    private final MetadataService metadataService;

    public LeosDocumentServiceImpl(LeosLegDocumentService leosLegDocumentService,
            XmlDocumentService xmlDocumentService,
            MetadataService metadataService) {
        this.leosLegDocumentService = leosLegDocumentService;
        this.xmlDocumentService = xmlDocumentService;
        this.metadataService = metadataService;
    }

    public LeosConvertDocumentInput createDocumentInput(MultipartFile inputFile, MultipartFile main, boolean isWithAnnotations) {
        final LeosConvertDocumentInput convertDocumentInput = new LeosConvertDocumentInput();
        convertDocumentInput.setInputFile(inputFile);
        convertDocumentInput.setMain(main);
        convertDocumentInput.setWithAnnotations(isWithAnnotations);
        return convertDocumentInput;
    }

    public LeosConvertDocumentInput createDocumentInput(MultipartFile inputFile, MultipartFile translationsFile) {
        final LeosConvertDocumentInput convertDocumentInput = new LeosConvertDocumentInput();
        convertDocumentInput.setInputFile(inputFile);
        convertDocumentInput.setTranslationsFile(translationsFile);
        return convertDocumentInput;
    }

    public byte[] getRenditions(LeosConvertDocumentInput convertDocumentInput) {
        return xmlDocumentService.xmlToHtmlPackage(convertDocumentInput);
    }

    public byte[] updateWithTranslations(LeosConvertDocumentInput convertDocumentInput) {
        return leosLegDocumentService.updateWithTranslations(convertDocumentInput);
    }

    public byte[] applyMetadata(MultipartFile inputFile) {
        return metadataService.applyMetadata(inputFile);
    }

}