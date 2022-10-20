/*
Copyright 2022 European Commission
Licensed under the EUPL, Version 1.2 or – as soon they will be approved by the European Commission - subsequent versions of the EUPL (the "Licence");
You may not use this work except in compliance with the Licence.
You may obtain a copy of the Licence at:
https://joinup.ec.europa.eu/software/page/eupl
Unless required by applicable law or agreed to in writing, software distributed under the Licence is distributed on an "AS IS" basis,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the Licence for the specific language governing permissions and limitations under the Licence.
*/
package eu.europa.ec.leos.services.export;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileHelper {

    private static final Logger LOG = LoggerFactory.getLogger(FileHelper.class);

    /**
     * Replaces the filename's extension denoted by the provided
     * <code>oldFilename</code> with the provided extension Example :
     * PROPOSAL_ANC_1231231.zip -> PROPOSAL_ANC_1231231.docx
     * <code>extensionReplacement</code> must not contain also the dot "."
     *
     * @param oldFilename
     * @param extensionReplacement
     * @return
     * @throws Exception
     */
    public static String getReplacedExtensionFilename(String oldFilename,
            String extensionReplacement) {
        return oldFilename.substring(0, oldFilename.lastIndexOf(".") + 1)
                + extensionReplacement;
    }

    public static void deleteFile(File file) {
        try {
            if (file != null && file.exists()) {
                file.delete();
            }
        } catch (Exception e) {
            LOG.error("Error when cleaning up file ", e);
        }
    }

}
