/*
 * Copyright 2021 European Commission
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
package eu.europa.ec.digit.leos.pilot.export.service;

import eu.europa.ec.digit.leos.pilot.export.model.LeosConvertDocumentInput;
import org.springframework.web.multipart.MultipartFile;

public interface LeosDocumentService {

    LeosConvertDocumentInput createDocumentInput(MultipartFile inputFile, MultipartFile main, boolean isWithAnnotations);

    LeosConvertDocumentInput createDocumentInput(MultipartFile inputFile, MultipartFile translationsFile);

    byte[] getRenditions(LeosConvertDocumentInput convertDocumentInput);

    byte[] updateWithTranslations(LeosConvertDocumentInput convertDocumentInput);

    byte[] applyMetadata(MultipartFile inputFile);
}
