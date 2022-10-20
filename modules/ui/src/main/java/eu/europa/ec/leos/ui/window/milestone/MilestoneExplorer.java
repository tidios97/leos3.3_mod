/*
 * Copyright 2019 European Commission
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
package eu.europa.ec.leos.ui.window.milestone;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import eu.europa.ec.leos.services.support.XmlHelper;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.vaadin.server.FileDownloader;
import com.vaadin.server.FileResource;
import com.vaadin.server.Resource;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinResponse;
import com.vaadin.shared.ui.ContentMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.Label;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.VerticalLayout;
import eu.europa.ec.leos.domain.cmis.Content;
import eu.europa.ec.leos.domain.cmis.LeosCategory;
import eu.europa.ec.leos.domain.cmis.document.LegDocument;
import eu.europa.ec.leos.i18n.MessageHelper;
import eu.europa.ec.leos.model.user.User;
import eu.europa.ec.leos.security.SecurityContext;
import eu.europa.ec.leos.services.export.ZipPackageUtil;
import eu.europa.ec.leos.services.processor.content.XmlContentProcessor;
import eu.europa.ec.leos.ui.component.LeosDisplayField;
import eu.europa.ec.leos.ui.event.metadata.DocumentMetadataRequest;
import eu.europa.ec.leos.ui.event.metadata.DocumentMetadataResponse;
import eu.europa.ec.leos.ui.event.metadata.SearchMetadataRequest;
import eu.europa.ec.leos.ui.event.metadata.SearchMetadataResponse;
import eu.europa.ec.leos.ui.event.security.SecurityTokenRequest;
import eu.europa.ec.leos.ui.event.security.SecurityTokenResponse;
import eu.europa.ec.leos.ui.extension.AnnotateExtension;
import eu.europa.ec.leos.ui.extension.MathJaxExtension;
import eu.europa.ec.leos.ui.extension.SoftActionsExtension;
import eu.europa.ec.leos.ui.model.AnnotateMetadata;
import eu.europa.ec.leos.ui.model.AnnotationStatus;
import eu.europa.ec.leos.util.LeosDomainUtil;
import eu.europa.ec.leos.web.event.NotificationEvent;
import eu.europa.ec.leos.web.event.component.WindowClosedEvent;
import eu.europa.ec.leos.web.support.cfg.ConfigurationHelper;
import eu.europa.ec.leos.web.support.log.LogUtil;
import eu.europa.ec.leos.web.support.user.UserHelper;
import eu.europa.ec.leos.web.support.xml.DownloadStreamResource;
import eu.europa.ec.leos.web.ui.themes.LeosTheme;
import eu.europa.ec.leos.web.ui.window.AbstractWindow;

import java.nio.charset.StandardCharsets;

public class MilestoneExplorer extends AbstractWindow {

    private static final long serialVersionUID = -4472838309232070251L;
    private static final Logger LOG = LoggerFactory.getLogger(MilestoneExplorer.class);

    private static final DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");

    private final ConfigurationHelper cfgHelper;
    private final SecurityContext securityContext;
    private final UserHelper userHelper;
    private final XmlContentProcessor xmlContentProcessor;

    private static final String HTML = ".html";
    private static final String TOC_HTML = "_toc.html";
    private static final String TOC_JS = "_toc.js";
    private static final String XML = ".xml";
    private static final String PDF = ".pdf";

    private static final String MAIN_DOCUMENT_FILE_NAME = "main";

    private static final String COVER_PAGE_CONTENT_FILE_NAME = "coverPage";
    private static final String COVER_PAGE_TAB_TITLE_KEY = "collection.block.caption.coverpage";

    private static final String DOC_VERSION_START_TAG_REG = "<leos:docVersion\\b[^>]*>";
    private static final String DOC_VERSION_END_TAG = "</leos:docVersion>";

    private static final String DOC_NUMBER_START_TAG_REG = "<leos:annexIndex\\b[^>]*>";
    private static final String DOC_NUMBER_END_TAG = "</leos:annexIndex>";

    private static final String TMP_DIR = "java.io.tmpdir";
    private static final String MILESTONE_DIR = "/milestone/";

    private TabSheet tabsheet;
    private LegDocument legDocument;
    private String milestoneTitle;
    private File legFileTemp;
    private Map<String, Object> contentFiles;
    private Map<String, Object> jsFiles;
    private Map<String, String> docVersionMap;
    private Map<Integer, String> annexIndexesMap;
    private Map<String, Integer> annexKeyMap;
    private Map<String, Object> pdfRenditions;
    private String milestoneDir;
    private String selectedDocument;
    private Button export;
    protected FileDownloader fileDownloader;
    private String proposalRef;
    private boolean showCoverPage;

    private AnnotateExtension<LeosDisplayField, String> annotateExtension;

    public MilestoneExplorer(LegDocument legDocument, String milestoneTitle, String proposalRef, MessageHelper messageHelper, EventBus eventBus,
            ConfigurationHelper cfgHelper, SecurityContext securityContext, UserHelper userHelper,
                             XmlContentProcessor xmlContentProcessor, boolean showCoverPage) {
        super(messageHelper, eventBus);
        this.cfgHelper = cfgHelper;
        this.securityContext = securityContext;
        this.legDocument = legDocument;
        this.milestoneTitle = milestoneTitle;
        this.userHelper = userHelper;
        this.proposalRef = proposalRef;
        this.showCoverPage = showCoverPage;
        this.xmlContentProcessor = xmlContentProcessor;
        init();
    }

    @Override
    public void attach() {
        super.attach();
        eventBus.register(this);
    }

    @Override
    public void detach() {
        super.detach();
        eventBus.unregister(this);
    }

    private void init() {
        setWidth(45f, Unit.CM);
        setHeight(95, Unit.PERCENTAGE);

        filterFilesToDisplay();

        VerticalLayout explorerLayout = buildLayout();
        setBodyComponent(explorerLayout);
        setCaption(messageHelper.getMessage("milestone.explorer.window.caption"));
        setDraggable(false);
        initDownloader();
    }

    private void filterFilesToDisplay() {
        try {
            legFileTemp = File.createTempFile("milestone", ".leg");
            Content content = legDocument.getContent().getOrError(() -> "Document content is required!");
            InputStream is = content.getSource().getInputStream();
            FileUtils.copyInputStreamToFile(is, legFileTemp);
            Map<String, Object> unzippedFiles = ZipPackageUtil.unzipFiles(legFileTemp, MILESTONE_DIR);
            milestoneDir = getMilestoneDir();
            contentFiles = filterAndSortFiles(unzippedFiles, HTML);
            jsFiles = filterAndSortFiles(unzippedFiles, TOC_JS);
            Map<String, Map> versionAndAnnexNumberMap = populateVersionAndAnnexNumberMap(unzippedFiles);
            docVersionMap = versionAndAnnexNumberMap.get("docVersionMap");
            annexIndexesMap = versionAndAnnexNumberMap.get("annexIndexesMap");
            annexKeyMap = versionAndAnnexNumberMap.get("annexKeyMap");
            pdfRenditions = filterAndSortFiles(unzippedFiles, PDF);
        } catch (IOException e) {
            LogUtil.logError(LOG, eventBus, "Exception occurred while reading the .leg file", e);
        }
    }

    private Map<String, Object> filterAndSortFiles(Map<String, Object> files, String fileFilter) {
        final List<String> tabOrder = Arrays.asList(XmlHelper.ANNEX_FILE_PREFIX, XmlHelper.REG_FILE_PREFIX, XmlHelper.DIR_FILE_PREFIX, XmlHelper.DEC_FILE_PREFIX,
                XmlHelper.MEMORANDUM_FILE_PREFIX);
        Map<String, Object> sortedFiles = files.entrySet().stream().
                filter(e -> (!e.getKey().contains(TOC_HTML) && e.getKey().contains(fileFilter))).
                sorted(Collections.reverseOrder(Comparator.comparing((Map.Entry e) -> {
                    String key = e.getKey().toString();
                    int prefixSeparatorIndex = key.indexOf("-");
                    if(prefixSeparatorIndex > 0) {
                        return tabOrder.indexOf(key.substring(0, prefixSeparatorIndex));
                    } else {
                        return (int) (key.toLowerCase().charAt(0));
                    }
                }))).
                collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                        (e1, e2) -> e2, LinkedHashMap::new));

        return sortedFiles;
    }

    private Map<String, Map> populateVersionAndAnnexNumberMap(Map<String, Object> files) {
        Map<String, Map> docVersionAndAnnexNumberMap = new HashMap<>();
        Map<String, String> docVersionMap = new HashMap<>();
        Map<Integer, String> annexIndexesMap = new HashMap<>();
        Map<String, Integer> annexKeyMap = new HashMap<>();
        Map<String, Object> xmlFiles = filterAndSortFiles(files, XML);
        xmlFiles.forEach((key, value) -> {
            try {
                String xmlContent = readFileToString(((File) value));
                Pattern pattern = Pattern.compile(DOC_VERSION_START_TAG_REG);
                Matcher matcher = pattern.matcher(xmlContent);
                String selectedKey = key.substring(0, key.indexOf(XML));
                while (matcher.find()) {
                    int endIndex = xmlContent.indexOf(DOC_VERSION_END_TAG);
                    String docVersion = xmlContent.substring(matcher.end(), endIndex);
                    docVersionMap.put(selectedKey, docVersion);
                }
                Pattern patternForAnnexIndex = Pattern.compile(DOC_NUMBER_START_TAG_REG);
                Matcher matcherForAnnexIndex = patternForAnnexIndex.matcher(xmlContent);
                if (matcherForAnnexIndex.find()) {
                    int endAnnexIndex = xmlContent.indexOf(DOC_NUMBER_END_TAG);
                    String annexIndex = xmlContent.substring(matcherForAnnexIndex.end(), endAnnexIndex);
                    annexIndexesMap.put(new Integer(annexIndex), selectedKey);
                    annexKeyMap.put(selectedKey, new Integer(annexIndex));
                }
            } catch (IOException e) {
                LOG.error("Exception occurred while reading the .leg file " + e);
            }
        });
        docVersionAndAnnexNumberMap.put("docVersionMap", docVersionMap);
        docVersionAndAnnexNumberMap.put("annexIndexesMap", annexIndexesMap);
        docVersionAndAnnexNumberMap.put("annexKeyMap", annexKeyMap);
        return docVersionAndAnnexNumberMap;
    }

    private VerticalLayout buildLayout() {
        VerticalLayout mainLayout = new VerticalLayout();
        mainLayout.setSpacing(true);
        mainLayout.setMargin(true);
        mainLayout.setSizeFull();

        HorizontalLayout titleLayout = new HorizontalLayout();
        titleLayout.setSpacing(true);
        titleLayout.setMargin(false);
        titleLayout.setSizeFull();
        User user = userHelper.getUser(legDocument.getInitialCreatedBy());
        Label description = new Label(messageHelper.getMessage("milestone.explorer.window.description", dateFormat.format(Date.from(legDocument.getInitialCreationInstant())),
                user.getName(), milestoneTitle), ContentMode.HTML);
        titleLayout.addComponent(description);
        titleLayout.setComponentAlignment(description, Alignment.TOP_LEFT);
        export = new Button(messageHelper.getMessage("collection.caption.menuitem.export"));
        if(!pdfRenditions.isEmpty()) {
            titleLayout.addComponent(export);
            titleLayout.setComponentAlignment(export, Alignment.TOP_RIGHT);
        }

        mainLayout.addComponent(titleLayout);

        tabsheet = new TabSheet();
        tabsheet.setHeight(100.0f, Unit.PERCENTAGE);
        tabsheet.addStyleName("leos-tabsheet");
        tabsheet.addStyleName(LeosTheme.TABSHEET_FRAMED);
        tabsheet.addStyleName(LeosTheme.TABSHEET_PADDED_TABBAR);

        registerTabChangeListener();
        addDocumentTabs();

        tabsheet.setSizeFull();
        mainLayout.addComponent(tabsheet);
        mainLayout.setComponentAlignment(tabsheet, Alignment.TOP_CENTER);

        mainLayout.setExpandRatio(titleLayout, 0.05f);
        mainLayout.setExpandRatio(tabsheet, 0.95f);

        return mainLayout;
    }

    private String getTabName(LeosCategory category, int annexNumber, String version) {
        String versionLabel = " [" + version + "]";
        switch (category) {
            case MEMORANDUM:
                return "Explanatory Memorandum" + versionLabel;
            case BILL:
                return "Legal Act" + versionLabel;
            case ANNEX:
                return "Annex " + annexNumber + versionLabel;
            case COVERPAGE:
                return  showCoverPage ? messageHelper.getMessage(COVER_PAGE_TAB_TITLE_KEY) + " " + versionLabel : "";
            default:
                return "";
        }
    }

    private void addDocumentTabs() {
        for (Map.Entry<String, Object> entry : contentFiles.entrySet()) {
            String key = entry.getKey();
            String mainFileName = docVersionMap.keySet().stream().filter(value -> value.startsWith(MAIN_DOCUMENT_FILE_NAME)).findFirst().get();
            String contentFileName = key.startsWith(COVER_PAGE_CONTENT_FILE_NAME) ? mainFileName : key.substring(0, key.indexOf(HTML));
            String version = docVersionMap.get(contentFileName);
            boolean isCoverPage = key.startsWith(COVER_PAGE_CONTENT_FILE_NAME);
            // Skip the cover page tab if it should not be shown
            if((isCoverPage && !showCoverPage)) {
                continue;
            }
            try {
                HorizontalSplitPanel tocSplitter = new HorizontalSplitPanel();
                if (isCoverPage) {
                    String tabName = getTabName(LeosCategory.COVERPAGE, 0, version);
                    tabsheet.addTab(tocSplitter, StringUtils.capitalize(tabName), null, 0);
                    tabsheet.setSelectedTab(0);
                } else {
                    byte[] xmlBytes = Files.readAllBytes(((File) entry.getValue()).toPath());
                    String xmlContent = LeosDomainUtil.wrapXmlFragment(new String(xmlBytes));
                    LeosCategory category = xmlContentProcessor.identifyCategory(key,
                            xmlContent.getBytes(StandardCharsets.UTF_8));
                    if (!category.equals(LeosCategory.ANNEX)) {
                        String tabName = getTabName(category, 0, version);
                        tabsheet.addTab(tocSplitter, StringUtils.capitalize(tabName));
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException("Unexpected error occurred while reading doc file", e);
            }
        }
        for (Integer annexNumber : annexIndexesMap.keySet()) {
            String version = docVersionMap.get(annexIndexesMap.get(annexNumber));
            HorizontalSplitPanel tocSplitter = new HorizontalSplitPanel();
            String tabName = getTabName(LeosCategory.ANNEX, annexNumber, version);
            tabsheet.addTab(tocSplitter, StringUtils.capitalize(tabName));
        }
    }

    private static String readFileToString(File file) throws IOException {
        return new String(Files.readAllBytes(file.toPath()), "UTF-8");
    }

    private void registerTabChangeListener() {
        tabsheet.addSelectedTabChangeListener(event -> {
            TabSheet tabsheet = event.getTabSheet();
            HorizontalSplitPanel splitPanel = (HorizontalSplitPanel) tabsheet.getSelectedTab();
            String caption = tabsheet.getTab(splitPanel).getCaption();
            boolean isValidTab = false;
            File tocFile = null;
            String content = "";
            LeosCategory category = null;
            String selectedTab;

            for (Map.Entry<String, Object> entry : jsFiles.entrySet()) {
                String key = entry.getKey();
                selectedDocument = key.substring(0, key.indexOf(TOC_JS));
                String mainFileName = docVersionMap.keySet().stream().filter(value -> value.startsWith(MAIN_DOCUMENT_FILE_NAME)).findFirst().get();
                String contentFileName = key.startsWith(COVER_PAGE_CONTENT_FILE_NAME) ? mainFileName : selectedDocument;
                String version = docVersionMap.get(contentFileName);

                try {
                    content = readFileToString(((File) contentFiles.get(selectedDocument + HTML)));
                } catch (IOException e) {
                    throw new RuntimeException("Unexpected error occurred while reading content file", e);
                }
                if(!selectedDocument.startsWith(COVER_PAGE_CONTENT_FILE_NAME)) {
                    int annexNumber = 0;
                    if (annexKeyMap.get(selectedDocument) != null) {
                        annexNumber = annexKeyMap.get(selectedDocument);
                    }
                    String xmlContent = LeosDomainUtil.wrapXmlFragment(content);
                    category = xmlContentProcessor.identifyCategory(key,
                            xmlContent.getBytes(StandardCharsets.UTF_8));
                    selectedTab = getTabName(category, annexNumber, version);
                } else {
                    selectedTab = getTabName(LeosCategory.COVERPAGE, 0, version);
                    category = LeosCategory.COVERPAGE;
                }
                if (caption.equalsIgnoreCase(selectedTab)) {
                    tocFile = (File) entry.getValue();
                    isValidTab = true;
                    break;
                }
            }

            if (isValidTab) {
                prepareTabContentLayout(splitPanel, category, tocFile, content);
            }

        });
    }

    private void prepareTabContentLayout(HorizontalSplitPanel splitPanel, LeosCategory category, File tocFile, String content) {
        if (tocFile != null) {
            VerticalLayout tocLayout = buildTocLayout(tocFile);
            splitPanel.setFirstComponent(tocLayout);
        }

        LeosDisplayField docContent = new LeosDisplayField();
        docContent.setSizeFull();
        docContent.setStyleName("leos-doc-content");
        docContent.setId("milestonedocContainer");
        docContent.setValue(content);

        VerticalLayout contentLayout = new VerticalLayout();
        contentLayout.setSizeFull();
        contentLayout.setMargin(false);
        contentLayout.setSpacing(false);
        contentLayout.setId(category.name().toLowerCase() + "_container");

        contentLayout.addComponent(docContent);
        contentLayout.setExpandRatio(docContent, 1.0f);

        splitPanel.setSecondComponent(contentLayout);
        splitPanel.setSplitPosition(20f, Unit.PERCENTAGE);
        addAnnotateExtension(splitPanel);
        new SoftActionsExtension<>(docContent);
        new MathJaxExtension<>(docContent);
    }

    private void addAnnotateExtension(HorizontalSplitPanel splitPanel) {
        VerticalLayout contentLayout = (VerticalLayout) splitPanel.getSecondComponent();
        LeosDisplayField docContent = (LeosDisplayField) contentLayout.getComponent(0);
        annotateExtension = new AnnotateExtension<>(docContent, eventBus, cfgHelper, docContent.getId(), AnnotateExtension.OperationMode.READ_ONLY,true, false, proposalRef, null);
    }

    private VerticalLayout buildTocLayout(File file) {
        VerticalLayout tocLayout = new VerticalLayout();
        tocLayout.setMargin(false);
        tocLayout.setSpacing(false);

        HorizontalLayout toolbar = buildTocToolbar();
        MilestoneTocComponent treeLayout = buildTocTree(file);
        treeLayout.setSizeFull();

        tocLayout.addComponent(toolbar);
        tocLayout.addComponent(treeLayout);
        tocLayout.setExpandRatio(treeLayout, 1f);
        return tocLayout;
    }

    private HorizontalLayout buildTocToolbar() {
        LOG.info("Building table of contents toolbar...");
        HorizontalLayout toolbar = new HorizontalLayout();
        toolbar.addStyleName("leos-milestone-toc-toolbar");
        toolbar.setWidth(100, Unit.PERCENTAGE);

        Label tocLabel = new Label(messageHelper.getMessage("toc.title"), ContentMode.HTML);
        toolbar.addComponent(tocLabel);
        toolbar.setComponentAlignment(tocLabel, Alignment.TOP_CENTER);
        return toolbar;
    }

    private MilestoneTocComponent buildTocTree(File file) {
        MilestoneTocComponent milestoneToc = new MilestoneTocComponent();
        milestoneToc.setSizeFull();
        milestoneToc.setId("treeContainer");
        try {
            String fileData = readFileToString(file);
            fileData = fileData.substring(fileData.indexOf("["), fileData.length() - 1);
            milestoneToc.setTocData(fileData);
        } catch (IOException e) {
            LogUtil.logError(LOG, eventBus, "Exception occurred while reading the file", e);
        }
        return milestoneToc;
    }

    @Subscribe
    public void fetchToken(SecurityTokenRequest event) {
        eventBus.post(new SecurityTokenResponse(securityContext.getAnnotateToken(event.getUrl())));
    }

    @Subscribe
    public void fetchMetadata(DocumentMetadataRequest event) {
        AnnotateMetadata metadata = new AnnotateMetadata();
        eventBus.post(new DocumentMetadataResponse(metadata));
    }

    @Subscribe
    public void fetchSearchMetadata(SearchMetadataRequest event) {
        List<AnnotateMetadata> metadataList = new ArrayList<>();
        AnnotateMetadata metadata = new AnnotateMetadata();
        List<String> statusList = new ArrayList<String>();
        statusList.add(AnnotationStatus.ALL.name());
        metadata.setStatus(statusList);
        String criteria = cfgHelper.getProperty("leos.milestone.annotation.criteria");
        metadata.setVersion(criteria + docVersionMap.get(selectedDocument));
        metadataList.add(metadata);
        LOG.info("Sending SearchMetadataResponse event to Annotation with the status " + metadata.getStatus().get(0));
        eventBus.post(new SearchMetadataResponse(metadataList));
    }

    private String getMilestoneDir() {
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

    public void setDownloadStreamResource(Resource downloadResource) {
        fileDownloader.setFileDownloadResource(downloadResource);
    }

    private void initDownloader() {
        // Resource cannot be null at instantiation time of the FileDownloader, creating a dummy one
        FileResource downloadStreamResource = new FileResource(new File(""));
        fileDownloader = new FileDownloader(downloadStreamResource) {
            private static final long serialVersionUID = -4584979099145066535L;
            @Override
            public boolean handleConnectorRequest(VaadinRequest request, VaadinResponse response, String path) throws IOException {
                boolean result = false;
                try {
                    Entry<String, Object> entry = pdfRenditions.entrySet().iterator().next();
                    prepareDownloadPackage((File)entry.getValue());
                    result = super.handleConnectorRequest(request, response, path);
                } catch (Exception exception) {
                    LOG.error("Error occured in export to pdf", exception.getMessage());
                }
                return result;
            }
        };
        fileDownloader.extend(export);
    }

    private void prepareDownloadPackage(File packageFile) {
        if (packageFile != null) {
            DownloadStreamResource downloadStreamResource;
            try {
                final byte[] fileBytes = FileUtils.readFileToByteArray(packageFile);
                downloadStreamResource = new DownloadStreamResource(packageFile.getName(), new ByteArrayInputStream(fileBytes));
                setDownloadStreamResource(downloadStreamResource);
                eventBus.post(new NotificationEvent("menu.download.caption", "milestone.explorer.export.pdf", NotificationEvent.Type.TRAY));
                LOG.trace("Successfully prepared milestone to export as pdf");
            } catch (Exception e) {
              LOG.error("Error while exporting milestone as pdf {}", e.getMessage());
              eventBus.post(new NotificationEvent(NotificationEvent.Type.ERROR, "milestone.explorer.export.error", e.getMessage()));
            }
        }
    }

    @Override
    protected void handleCloseButton() {
        LOG.info("Closing milestone explorer window....");
        deleteTempFilesIfExists();
        eventBus.post(new WindowClosedEvent<>(this));
        super.handleCloseButton();
    }

    private void deleteTempFilesIfExists() {
        LOG.info("deleting temp files from milestone explorer window....");
            try {
                if (legFileTemp != null && legFileTemp.exists()) {
                    File folder = new File(milestoneDir);
                    Files.delete(legFileTemp.toPath());
                    legFileTemp = null;
                    recursiveDelete(folder);
                }
            } catch (IOException e) {
                LOG.error("Exception occurred while deleting the file " + e);
            } finally {
                LOG.info("Closing milestone explorer window ...");
                super.handleCloseButton();
            }
    }

    private void recursiveDelete(File rootDir) throws IOException {
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
