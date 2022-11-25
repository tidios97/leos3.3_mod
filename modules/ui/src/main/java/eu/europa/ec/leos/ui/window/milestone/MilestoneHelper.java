package eu.europa.ec.leos.ui.window.milestone;

import eu.europa.ec.leos.domain.cmis.Content;
import eu.europa.ec.leos.domain.cmis.document.Annex;
import eu.europa.ec.leos.domain.cmis.document.LegDocument;
import eu.europa.ec.leos.services.processor.content.XmlContentProcessor;
import eu.europa.ec.leos.services.support.XmlHelper;
import org.apache.commons.io.FileUtils;
import eu.europa.ec.leos.services.export.ZipPackageUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static eu.europa.ec.leos.services.support.XmlHelper.ANNEX_FILE_PREFIX;

public class MilestoneHelper {

    private static final Logger LOG = LoggerFactory.getLogger(MilestoneHelper.class);

    private static final String TOC_HTML = "_toc.html";
    private static final String XML = ".xml";
    private static final String PROCESSED = "_processed";

    private static final String TMP_DIR = "java.io.tmpdir";
    private static final String MILESTONE_DIR = "/milestone/";

    public static Map<String, Object> filterAndSortFiles(Map<String, Object> files, String fileFilter) {
        final List<String> tabOrder = Arrays.asList(XmlHelper.FINANCIAL_STATEMENT, XmlHelper.ANNEX_FILE_PREFIX, XmlHelper.REG_FILE_PREFIX, XmlHelper.DIR_FILE_PREFIX, XmlHelper.DEC_FILE_PREFIX,
                XmlHelper.MEMORANDUM_FILE_PREFIX);
        Map<String, Object> sortedFiles = files.entrySet().stream().
                filter(e -> (!e.getKey().contains(TOC_HTML) && e.getKey().endsWith(fileFilter))).
                sorted(Collections.reverseOrder(Comparator.comparing((Map.Entry e) -> {
                    String key = e.getKey().toString();
                    int prefixSeparatorIndex = key.indexOf("-");
                    if (prefixSeparatorIndex > 0) {
                        return tabOrder.indexOf(key.substring(0, prefixSeparatorIndex));
                    } else {
                        return (int) (key.toLowerCase().charAt(0));
                    }
                }))).
                collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                        (e1, e2) -> e2, LinkedHashMap::new));
        return sortedFiles;
    }

    public static Map<String, Object> getMilestoneFiles(File legFileTemp, LegDocument legDocument) throws IOException {
        Content content = legDocument.getContent().getOrError(() -> "Document content is required!");
        InputStream is = content.getSource().getInputStream();
        FileUtils.copyInputStreamToFile(is, legFileTemp);
        Map<String, Object> unzippedFiles = ZipPackageUtil.unzipFiles(legFileTemp, MILESTONE_DIR);
        return unzippedFiles;
    }

    public static Map<String, Object> populateAnnexAddedMap(Map<String, Object> files, LegDocument legDocument,
                                                            List<Annex> annexList, XmlContentProcessor xmlContentProcessor) {
        Map<String, Object> annexAddedMap = new HashMap<>();
        Map<String, Object> xmlFiles = filterAndSortFiles(files, XML);
        xmlFiles.forEach((key, value) -> {
            try {
                byte[] xmlBytes = Files.readAllBytes(((File) value).toPath());
                String entryKey = key.substring(0, key.indexOf(XML));
                if (entryKey.startsWith(ANNEX_FILE_PREFIX) && !xmlContentProcessor.isClonedDocument(xmlBytes)) {
                    final String annexFilename = key.substring(0, key.indexOf(XML));
                    //Populate list of new annexes from contribution which are already accepted in original proposal
                    Optional<Annex> acceptedAnnex = annexList.stream().filter(annex ->
                            annexFilename.equalsIgnoreCase(annex.getMetadata().get().getClonedRef())).findFirst();
                    //Populate list of new annexes from contribution which are rejected
                    Optional<String> rejectedAnnex = legDocument.getContainedDocuments().stream().filter(
                            fileName -> fileName.contains(PROCESSED) && fileName.startsWith(annexFilename)).findFirst();

                    if (acceptedAnnex.isPresent() || rejectedAnnex.isPresent()) {
                        //Add the processed annexes with the suffix "_processed"
                        String processedAnnexFilename = annexFilename.concat(PROCESSED);
                        annexAddedMap.put(processedAnnexFilename, value);
                    } else {
                        annexAddedMap.put(annexFilename, value);
                    }
                }
            } catch (IOException e) {
                LOG.error("Exception occurred while reading the .leg file " + e);
            }
        });
        return annexAddedMap;
    }

    public static Map<String, Object> populateAnnexDeletedMap(Map<String, Object> originalContentFiles,
                                                              Map<String, Object> contentFiles, LegDocument originalLegDocument,
                                                              List<Annex> annexList, XmlContentProcessor xmlContentProcessor) {
        Map<String, Object> annexDeletedMap = new HashMap<>();
        Map<String, Object> originalXmlFiles = MilestoneHelper.filterAndSortFiles(originalContentFiles, XML);
        Map<String, Object> xmlFiles = MilestoneHelper.filterAndSortFiles(contentFiles, XML);
        for (Map.Entry<String, Object> entry : originalXmlFiles.entrySet()) {
            boolean found = false;
            String originalEntryKey = entry.getKey().substring(0, entry.getKey().indexOf(XML));
            if (originalEntryKey.startsWith(ANNEX_FILE_PREFIX)) {
                for (Map.Entry<String, Object> entry1 : xmlFiles.entrySet()) {
                    String entryKey = entry1.getKey();
                    try {
                        byte[] xmlBytes = Files.readAllBytes(((File) entry1.getValue()).toPath());
                        if (entryKey.startsWith(ANNEX_FILE_PREFIX)) {
                            String originalDocRef = xmlContentProcessor.getOriginalDocRefFromClonedContent(xmlBytes);
                            if (originalEntryKey.equalsIgnoreCase(originalDocRef)) {
                                found = true;
                                break;
                            }
                        }
                    } catch (IOException e) {
                        throw new RuntimeException("Unexpected error occurred while reading doc file", e);
                    }
                }
                if (!found) {
                    //Populate list of deleted annexes from contribution which are already accepted in original proposal
                    Optional<Annex> acceptedAnnex = annexList.stream().filter(annex ->
                            originalEntryKey.equalsIgnoreCase(annex.getMetadata().get().getRef())).findFirst();
                    //Populate list of deleted annexes from contribution which are rejected
                    Optional<String> rejectedAnnex = originalLegDocument.getContainedDocuments().stream().filter(
                            fileName -> fileName.contains(PROCESSED) && fileName.startsWith(originalEntryKey)).findFirst();

                    if (!acceptedAnnex.isPresent() || rejectedAnnex.isPresent()) {
                        //Add the processed annexes with the suffix "_processed"
                        annexDeletedMap.put(originalEntryKey.concat(PROCESSED), entry.getValue());
                    } else {
                        annexDeletedMap.put(originalEntryKey, entry.getValue());
                    }
                }
            }
        }
        return annexDeletedMap;
    }

    public static String getMilestoneDir(File legFileTemp) {
        String tempDir = System.getProperty(TMP_DIR);
        String rootDir = tempDir + MILESTONE_DIR;
        File folder = new File(rootDir);
        if (folder.exists()) {
            File[] files = folder.listFiles();
            for (File file : files) {
                if (file.getName().contains(legFileTemp.getName())) {
                    return file.getPath();
                }
            }
        }
        return "";
    }

    public static void deleteTempFilesIfExists(File legFileTemp) throws IOException {
        LOG.info("deleting temp files from file system....");
        if (legFileTemp != null && legFileTemp.exists()) {
            File folder = new File(getMilestoneDir(legFileTemp));
            Files.delete(legFileTemp.toPath());
            recursiveDelete(folder);
        }
    }

    private static void recursiveDelete(File rootDir) throws IOException {
        File[] files = rootDir.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                recursiveDelete(file);
            } else {
                if (!file.delete()) {
                    throw new IOException("Could not delete: " + file.getAbsolutePath());
                }
            }
        }
        if (!rootDir.delete()) {
            throw new IOException("Could not delete: " + rootDir.getAbsolutePath());
        }
    }
}
