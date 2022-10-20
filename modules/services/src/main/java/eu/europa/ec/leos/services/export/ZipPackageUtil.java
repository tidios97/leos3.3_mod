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
package eu.europa.ec.leos.services.export;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import static eu.europa.ec.leos.services.support.XmlHelper.DOC_FILE_NAME_SEPARATOR;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

public class ZipPackageUtil {
    private static Logger LOG = LoggerFactory.getLogger(ZipPackageUtil.class);

    public static File zipFiles(String zipFileName, Map<String, Object> contentToZip, String language) throws IOException {
        String fileExtension = "." + FilenameUtils.getExtension(zipFileName);
        String fileName = FilenameUtils.getBaseName(zipFileName);
        File zipFile = null;
        ZipOutputStream zipOutputStream = null;
        try {
            zipFile = File.createTempFile(fileName.concat(DOC_FILE_NAME_SEPARATOR), fileExtension);
            zipFile = renameZipFile(language, fileExtension, zipFile);
            zipOutputStream = new ZipOutputStream(new FileOutputStream(zipFile));
            addContentToOutputStream(zipOutputStream, contentToZip);
            return zipFile;
        } catch (IOException e) {
            LOG.error("Error creating zip package: {}", e.getMessage());
            if (zipFile != null && zipFile.exists()) {
                zipFile.delete();
            }
            throw new IOException(e.getMessage());
        } finally {
            if (zipOutputStream != null) {
                zipOutputStream.close();
            }
        }
    }

    private static File renameZipFile(String language, String fileExtension, File zipFile) throws IOException {
        String zipName = zipFile.getName();
        zipName = zipName.substring(0, zipName.lastIndexOf("."));
        zipName = StringUtils.isNotEmpty(language) ? zipName.concat(DOC_FILE_NAME_SEPARATOR).concat(language).
                concat(fileExtension) : zipName.concat(fileExtension);
        Path path = Paths.get(zipFile.getAbsolutePath());
        path = Files.move(path, path.resolveSibling(zipName), REPLACE_EXISTING);
        zipFile = path.toFile();
        return zipFile;
    }

    public static byte[] zipByteArray(Map<String, Object> contentToZip) throws IOException {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             ZipOutputStream zos = new ZipOutputStream(bos)) {
            addContentToOutputStream(zos, contentToZip);
            zos.close();
            return bos.toByteArray();
        }
    }

    private static void addContentToOutputStream(ZipOutputStream zipOutputStream, Map<String, Object> contentToZip) throws IOException {
        for (Map.Entry<String, Object> entry : contentToZip.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            if (value instanceof File) {
                File fileValue = (File) value;
                ZipEntry ze = new ZipEntry(key);
                zipOutputStream.putNextEntry(ze);
                FileInputStream fileInputStream = new FileInputStream(fileValue);
                IOUtils.copy(fileInputStream, zipOutputStream);
                fileInputStream.close();
                zipOutputStream.closeEntry();
            } else if (value instanceof ByteArrayOutputStream) {
                ByteArrayOutputStream byteArrayOutputStreamValue = (ByteArrayOutputStream) value;
                ZipEntry ze = new ZipEntry(key);
                zipOutputStream.putNextEntry(ze);
                zipOutputStream.write(byteArrayOutputStreamValue.toByteArray());
                zipOutputStream.closeEntry();
                byteArrayOutputStreamValue.close();
            } else if (value instanceof String) {
                String stringValue = (String) value;
                ZipEntry ze = new ZipEntry(key);
                zipOutputStream.putNextEntry(ze);
                zipOutputStream.write(stringValue.getBytes(UTF_8));
                zipOutputStream.closeEntry();
            } else if (value instanceof byte[]) {
                byte[] byteArrayValue = (byte[]) value;
                ZipEntry ze = new ZipEntry(key);
                zipOutputStream.putNextEntry(ze);
                zipOutputStream.write(byteArrayValue);
                zipOutputStream.closeEntry();
            }
        }
    }

    public static Map<String, Object> unzipByteArray(byte[] zipppedData) throws IOException {
        Map<String, Object> unzippedFiles = new HashMap<>();
        try (ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(zipppedData))) {
            ZipEntry ze;
            while ((ze = zis.getNextEntry()) != null) {
                if (ze.isDirectory()) {
                    continue;
                }
                unzippedFiles.put(ze.getName(), IOUtils.toByteArray(zis));
            }
            zis.closeEntry();
        }
        return unzippedFiles;
    }

    public static Map<String, Object> unzipFiles(File file, String unzipPath) {
        Map<String, Object> unzippedFiles = new HashMap<>();
        final File destDir = new File(System.getProperty("java.io.tmpdir") + unzipPath +
                file.getName() + "_" + System.currentTimeMillis());
        // get the zip file content with try-with-resources
        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(file))) {
            // get the zipped file list entry
            ZipEntry ze;
            while ((ze = zis.getNextEntry()) != null) {
                final File newFile = newFile(destDir, ze);
                if (ze.isDirectory()) {
                    if (!newFile.isDirectory() && !newFile.mkdirs()) {
                        throw new IOException("Failed to create directory " + newFile);
                    }
                } else {
                    final File parent = newFile.getParentFile();
                    if (!parent.isDirectory() && !parent.mkdirs()) {
                        throw new IOException("Failed to create directory " + parent);
                    }
                    final FileOutputStream fos = new FileOutputStream(newFile);
                    IOUtils.copy(zis, fos);
                    fos.close();
                    unzippedFiles.put(newFile.getName(), newFile);
                }
            }
            // closeEntry should not be required. In the next step the stream will be closed.
            // close will be done by the try-with-resources block
        } catch (IOException ex) {
            LOG.error("Error unzipping the file {} : {}", file.getName(), ex.getMessage());
        }
        return unzippedFiles;
    }

    /**
     * @see <a href="https://snyk.io/research/zip-slip-vulnerability">...</a>
     */
    private static File newFile(File destinationDir, ZipEntry zipEntry) throws IOException {
        File destFile = new File(destinationDir, zipEntry.getName());
        String destDirPath = destinationDir.getCanonicalPath();
        String destFilePath = destFile.getCanonicalPath();
        if (!destFilePath.startsWith(destDirPath + File.separator)) {
            throw new IOException("Entry is outside of the target dir: " + zipEntry.getName());
        }
        return destFile;
    }

    public static File unzipFile(File singleZipInput, String singleZipEntryName) throws Exception {
        ByteArrayOutputStream baos = unzipFileToStream(singleZipInput, singleZipEntryName);
        String fileExtension = "." + FilenameUtils.getExtension(singleZipEntryName);
        String fileName = FilenameUtils.getBaseName(singleZipEntryName);
        File unzippedFile = File.createTempFile(fileName + "_", fileExtension);
        FileOutputStream fos = new FileOutputStream(unzippedFile);
        baos.writeTo(fos);
        return unzippedFile;
    }

    public static ByteArrayOutputStream unzipFileToStream(File singleZipInput, String singleZipEntryName) throws Exception {


        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipFile zf = new ZipFile(singleZipInput)) {
            ZipEntry zipEntry = zf.getEntry(singleZipEntryName);
            try (InputStream in = zf.getInputStream(zipEntry)) {
                IOUtils.copy(in, baos);
            }
        }
        return baos;
    }

    /*
        Bills are always named Bill.docx, but Annexes are multiple and could be Annex_1, Annex_2, depending on which Annex was downloaded
        Looks for the matching *Annex_* filename in the zip file and returns that filename
     */
    public static String obtainRealDocName(File singleZipInput, String singleZipEntryName) throws Exception {

        if(!"Annex".equalsIgnoreCase(singleZipEntryName) && !"Explanatory".equalsIgnoreCase(singleZipEntryName)) {
            return singleZipEntryName + ".docx";
        }

        try (ZipFile zipFile = new ZipFile(singleZipInput)) {
            Enumeration zipEntries = zipFile.entries();
            while (zipEntries.hasMoreElements()) {
                String fileName = ((ZipEntry) zipEntries.nextElement()).getName();
                if(fileName.startsWith("Annex_") || fileName.startsWith("Council_explanatory_")) {
                    return fileName;
                }
            }
        }
        throw new RuntimeException(String.format("Could not find a matching file inside .zip - Looking for entry [%s]", singleZipEntryName));
    }
}
