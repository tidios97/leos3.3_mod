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
import eu.europa.ec.leos.domain.cmis.LeosCategory;
import eu.europa.ec.leos.domain.cmis.document.Annex;
import eu.europa.ec.leos.domain.cmis.document.LegDocument;
import eu.europa.ec.leos.i18n.MessageHelper;
import eu.europa.ec.leos.model.user.User;
import eu.europa.ec.leos.security.SecurityContext;
import eu.europa.ec.leos.services.processor.content.XmlContentProcessor;
import eu.europa.ec.leos.services.support.XercesUtils;
import eu.europa.ec.leos.ui.component.LeosDisplayField;
import eu.europa.ec.leos.ui.event.metadata.DocumentMetadataRequest;
import eu.europa.ec.leos.ui.event.metadata.DocumentMetadataResponse;
import eu.europa.ec.leos.ui.event.metadata.SearchMetadataRequest;
import eu.europa.ec.leos.ui.event.metadata.SearchMetadataResponse;
import eu.europa.ec.leos.ui.event.revision.OpenAndViewContibutionEvent;
import eu.europa.ec.leos.ui.event.security.SecurityTokenRequest;
import eu.europa.ec.leos.ui.event.security.SecurityTokenResponse;
import eu.europa.ec.leos.ui.event.view.collection.ContributionAnnexAcceptEvent;
import eu.europa.ec.leos.ui.event.view.collection.ContributionAnnexProcessedEvent;
import eu.europa.ec.leos.ui.event.view.collection.ContributionAnnexRejectEvent;
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
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MilestoneExplorer extends AbstractWindow {

    private static final long serialVersionUID = -4472838309232070251L;
    private static final Logger LOG = LoggerFactory.getLogger(MilestoneExplorer.class);

    private static final DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");

    public static final String LEOS_CONTENT_PROCESSED = "leos-content-processed";
    public static final String LEOS_CONTENT_REMOVED = "leos-content-removed";
    public static final String LEOS_CONTENT_NEW = "leos-content-new";
    public static final String LEOS_CONTENT_ADDED = "leos-content-added";
    public static final String LEOS_ANNEX_ADDED = "leos-annex-added";
    public static final String LEOS_ANNEX_PROCESSED = "leos-annex-processed";
    public static final String LEOS_ANNEX_REMOVED = "leos-annex-removed";
    private static final String PROCESSED = "_processed";
    public static final String LEOS_DOC_MODIFIED = "leos-doc-modified";
    private static final String PREFACE = "_preface";
    private static final String BODY = "_body";
    private static final String CLASS_ATTR = "class";

    private final ConfigurationHelper cfgHelper;
    private final SecurityContext securityContext;
    private final UserHelper userHelper;
    private final XmlContentProcessor xmlContentProcessor;

    private static final String HTML = ".html";
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


    private TabSheet tabsheet;
    private LegDocument legDocument;
    private LegDocument originalLegDocument; //only populated when viewing contribution milestone
    private List<Annex> annexList; //only populated when viewing contribution milestone
    private String milestoneTitle;
    private File legFileTemp;
    private File originalLegFileTemp;
    private Map<String, Object> contentFiles;
    private Map<String, Object> originalContentFiles;
    private Map<String, Object> jsFiles;
    private Map<String, Object> originalJsFiles;
    private Map<String, String> docVersionMap;
    private Map<Integer, String> annexIndexesMap;
    private Map<String, Integer> annexKeyMap;
    private Map<String, Object> annexAddedMap;
    private Map<String, Object> annexDeletedMap;
    private Map<String, Object> pdfRenditions;
    private String milestoneDir;
    private String selectedDocument;
    private Button export;
    private VerticalLayout headerLayout;
    private HorizontalLayout actionButtonsLayout;
    private Button accept;
    private Button reject;
    private Button viewAndMerge;
    protected FileDownloader fileDownloader;
    private String proposalRef;
    private boolean showCoverPage;
    private boolean isContributionMilestone;

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

    public MilestoneExplorer(LegDocument clonedLegDocument, LegDocument originalLegDocument, List<Annex> annexList,
                             String milestoneTitle, String proposalRef, MessageHelper messageHelper, EventBus eventBus,
                             ConfigurationHelper cfgHelper, SecurityContext securityContext, UserHelper userHelper,
                             XmlContentProcessor xmlContentProcessor, boolean showCoverPage, boolean isContributionMilestone) {
        super(messageHelper, eventBus);
        this.cfgHelper = cfgHelper;
        this.securityContext = securityContext;
        this.legDocument = clonedLegDocument;
        this.originalLegDocument = originalLegDocument;
        this.annexList = annexList;
        this.milestoneTitle = milestoneTitle;
        this.userHelper = userHelper;
        this.proposalRef = proposalRef;
        this.showCoverPage = showCoverPage;
        this.xmlContentProcessor = xmlContentProcessor;
        this.isContributionMilestone = isContributionMilestone;
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
            Map<String, Object> unzippedFiles = MilestoneHelper.getMilestoneFiles(legFileTemp, legDocument);
            contentFiles = MilestoneHelper.filterAndSortFiles(unzippedFiles, HTML);
            annexAddedMap = new HashMap<>();
            annexDeletedMap = new HashMap<>();
            if (isContributionMilestone) {
                originalLegFileTemp = File.createTempFile("milestoneOriginal", ".leg");
                Map<String, Object> originalUnzippedFiles = MilestoneHelper.getMilestoneFiles(originalLegFileTemp, originalLegDocument);
                //populate map with added annexes in Contribution
                annexAddedMap = MilestoneHelper.populateAnnexAddedMap(unzippedFiles, legDocument, annexList,
                        xmlContentProcessor);
                //populate map with deleted annex in Contribution
                annexDeletedMap = MilestoneHelper.populateAnnexDeletedMap(originalUnzippedFiles, unzippedFiles,
                        originalLegDocument, annexList, xmlContentProcessor);
                originalContentFiles = MilestoneHelper.filterAndSortFiles(originalUnzippedFiles, HTML);
                originalJsFiles = MilestoneHelper.filterAndSortFiles(originalUnzippedFiles, TOC_JS);
            }
            milestoneDir = MilestoneHelper.getMilestoneDir(legFileTemp);
            jsFiles = MilestoneHelper.filterAndSortFiles(unzippedFiles, TOC_JS);
            Map<String, Map> versionAndAnnexNumberMap = populateVersionAndAnnexNumberMap(unzippedFiles);
            docVersionMap = versionAndAnnexNumberMap.get("docVersionMap");
            annexIndexesMap = versionAndAnnexNumberMap.get("annexIndexesMap");
            annexKeyMap = versionAndAnnexNumberMap.get("annexKeyMap");
            pdfRenditions = MilestoneHelper.filterAndSortFiles(unzippedFiles, PDF);
        } catch (IOException e) {
            LogUtil.logError(LOG, eventBus, "Exception occurred while reading the .leg file", e);
        }
    }

    private Map<String, Map> populateVersionAndAnnexNumberMap(Map<String, Object> files) {
        Map<String, Map> docVersionAndAnnexNumberMap = new HashMap<>();
        Map<String, String> docVersionMap = new HashMap<>();
        Map<Integer, String> annexIndexesMap = new HashMap<>();
        Map<String, Integer> annexKeyMap = new HashMap<>();
        Map<String, Object> xmlFiles = MilestoneHelper.filterAndSortFiles(files, XML);
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
        mainLayout.setMargin(false);
        mainLayout.setSizeFull();

        headerLayout = new VerticalLayout();
        headerLayout.setSpacing(true);
        headerLayout.setMargin(false);
        headerLayout.setSizeFull();

        HorizontalLayout titleLayout = new HorizontalLayout();
        titleLayout.setSpacing(true);
        titleLayout.setMargin(false);
        titleLayout.setSizeFull();

        User user = userHelper.getUser(legDocument.getInitialCreatedBy());
        Label description = new Label(messageHelper.getMessage("milestone.explorer.window.description", dateFormat.format(Date.from(legDocument.getInitialCreationInstant())),
                user.getName(), milestoneTitle), ContentMode.HTML);
        titleLayout.addComponent(description);
        if(isContributionMilestone) {
            HorizontalLayout legendLayout = buildLegendLayout();
            legendLayout.setSpacing(true);
            legendLayout.setMargin(false);
            addComponentOnLeft(legendLayout);
        }
        titleLayout.setComponentAlignment(description, Alignment.TOP_LEFT);
        export = new Button(messageHelper.getMessage("collection.caption.menuitem.export"));
        if(!pdfRenditions.isEmpty()) {
            titleLayout.addComponent(export);
            titleLayout.setComponentAlignment(export, Alignment.TOP_RIGHT);
        }
        headerLayout.addComponent(titleLayout);
        headerLayout.setComponentAlignment(titleLayout, Alignment.TOP_CENTER);
        headerLayout.setExpandRatio(titleLayout, 0.70f);

        actionButtonsLayout = new HorizontalLayout();
        accept = new Button(messageHelper.getMessage("contribution.milestone.annex.accept.action"));
        reject = new Button(messageHelper.getMessage("contribution.milestone.annex.reject.action"));
        viewAndMerge = new Button(messageHelper.getMessage("contribution.merge.action"));
        actionButtonsLayout.addComponent(accept);
        actionButtonsLayout.addComponent(reject);
        actionButtonsLayout.addComponent(viewAndMerge);
        addActionButtonLayout();

        mainLayout.addComponent(headerLayout);

        tabsheet = new TabSheet();
        tabsheet.setHeight(100.0f, Unit.PERCENTAGE);
        tabsheet.addStyleName("leos-tabsheet");
        tabsheet.addStyleName(LeosTheme.TABSHEET_FRAMED);
        tabsheet.addStyleName(LeosTheme.TABSHEET_PADDED_TABBAR);

        registerTabChangeListener();
        addDocumentTabs();

        tabsheet.setSizeFull();
        mainLayout.addComponent(tabsheet);
        mainLayout.setComponentAlignment(tabsheet, Alignment.MIDDLE_CENTER);

        mainLayout.setExpandRatio(headerLayout, 0.10f);
        mainLayout.setExpandRatio(tabsheet, 0.90f);

        return mainLayout;
    }

    private HorizontalLayout buildLegendLayout() {
        Label legend = new Label(messageHelper.getMessage("contribution.milestone.legend.label"));
        Label deleted = new Label(messageHelper.getMessage("contribution.milestone.deleted.legend.label"));
        deleted.addStyleName("leos-deleted-legend");
        Label added = new Label(messageHelper.getMessage("contribution.milestone.added.legend.label"));
        added.addStyleName("leos-added-legend");
        Label modified = new Label(messageHelper.getMessage("contribution.milestone.modified.legend.label"));
        modified.addStyleName("leos-modified-legend");
        Label processed = new Label(messageHelper.getMessage("contribution.milestone.processed.legend.label"));
        processed.addStyleName("leos-processed-legend");
        HorizontalLayout legendLayout = new HorizontalLayout();
        legendLayout.addComponents(legend, deleted, added, modified, processed);
        legendLayout.setDefaultComponentAlignment(Alignment.MIDDLE_CENTER);
        return legendLayout;
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
            case FINANCIAL_STATEMENT:
                return "Financial Statement" + versionLabel;
            default:
                return "";
        }
    }

    private void addDocumentTabs() {
        HashMap<String, Boolean> annexesComparaison = new HashMap();
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
                byte[] xmlBytes = Files.readAllBytes(((File) entry.getValue()).toPath());
                String xmlContent = LeosDomainUtil.wrapXmlFragment(new String(xmlBytes));
                boolean isCompared = false;
                if(isContributionMilestone) {
                    Pattern pattern = Pattern.compile("class=\"leos-content-new\"|class=\"leos-content-removed\"",
                            Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
                    Matcher matcher = pattern.matcher(xmlContent);
                    isCompared = matcher.find();
                }
                if (isCoverPage) {
                    String tabName = getTabName(LeosCategory.COVERPAGE, 0, version);
                    TabSheet.Tab tab = tabsheet.addTab(tocSplitter, StringUtils.capitalize(tabName), null, 0);
                    if(isCompared) {
                        toggleActionButtons(false);
                        tab.setStyleName(LEOS_DOC_MODIFIED);
                    }
                    tabsheet.setSelectedTab(0);
                } else {
                    LeosCategory category = xmlContentProcessor.identifyCategory(key,
                            xmlContent.getBytes(StandardCharsets.UTF_8));
                    if (!category.equals(LeosCategory.ANNEX) && !category.equals(LeosCategory.FINANCIAL_STATEMENT)) {
                        String tabName = getTabName(category, 0, version);
                        TabSheet.Tab tab = tabsheet.addTab(tocSplitter, StringUtils.capitalize(tabName));
                        if(isCompared) {
                            tab.setStyleName(LEOS_DOC_MODIFIED);
                        }
                    } else {
                        annexesComparaison.put(entry.getKey(), isCompared);
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException("Unexpected error occurred while reading doc file", e);
            }
        }
        for (Integer annexNumber : annexIndexesMap.keySet()) {
            String annexDocument = annexIndexesMap.get(annexNumber);
            String version = docVersionMap.get(annexDocument);
            HorizontalSplitPanel tocSplitter = new HorizontalSplitPanel();
            String tabName = getTabName(LeosCategory.ANNEX, annexNumber, version);
            TabSheet.Tab tab = tabsheet.addTab(tocSplitter, StringUtils.capitalize(tabName));
            if(annexAddedMap.containsKey(annexDocument)) {
                tab.setStyleName(LEOS_ANNEX_ADDED);
            } else if (annexAddedMap.containsKey(annexDocument.concat(PROCESSED))) {
                tab.setStyleName(LEOS_ANNEX_PROCESSED);
            } else if(annexesComparaison.get(annexDocument + ".html")) {
                tab.setStyleName(LEOS_DOC_MODIFIED);
            }
        }
        if(annexDeletedMap.size() > 0) {
            for (Map.Entry<String, Object> entry : annexDeletedMap.entrySet()) {
                String xmlContent = getFileContent(entry);
                String[] annexVersionAndNumber = getAnnexVersionAndNumber(xmlContent);
                HorizontalSplitPanel tocSplitter = new HorizontalSplitPanel();
                String tabName = getTabName(LeosCategory.ANNEX, new Integer(annexVersionAndNumber[1]), annexVersionAndNumber[0]);
                TabSheet.Tab tab = tabsheet.addTab(tocSplitter, StringUtils.capitalize(tabName));
                tab.setId(entry.getKey());
                tab.setStyleName(LEOS_ANNEX_REMOVED);
                if(entry.getKey().indexOf(PROCESSED) != -1) {
                    tab.setStyleName(LEOS_ANNEX_PROCESSED);
                }
            }
        }
        for (Map.Entry<String, Object> entry : contentFiles.entrySet()) {
            String key = entry.getKey();
            String mainFileName = docVersionMap.keySet().stream().filter(value -> value.startsWith(MAIN_DOCUMENT_FILE_NAME)).findFirst().get();
            String contentFileName = key.startsWith(COVER_PAGE_CONTENT_FILE_NAME) ? mainFileName : key.substring(0, key.indexOf(HTML));
            String version = docVersionMap.get(contentFileName);

            try {
                HorizontalSplitPanel tocSplitter = new HorizontalSplitPanel();
                byte[] xmlBytes = Files.readAllBytes(((File) entry.getValue()).toPath());
                String xmlContent = LeosDomainUtil.wrapXmlFragment(new String(xmlBytes));
                boolean isCompared = false;
                if(isContributionMilestone) {
                    Pattern pattern = Pattern.compile("class=\"leos-content-new\"|class=\"leos-content-removed\"",
                            Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
                    Matcher matcher = pattern.matcher(xmlContent);
                    isCompared = matcher.find();
                }

                LeosCategory category = xmlContentProcessor.identifyCategory(key,
                        xmlContent.getBytes(StandardCharsets.UTF_8));

                if (category != null && category.equals(LeosCategory.FINANCIAL_STATEMENT)) {
                    String tabName = getTabName(category, 0, version);
                    TabSheet.Tab tab = tabsheet.addTab(tocSplitter, StringUtils.capitalize(tabName));
                    if(isCompared) {
                        tab.setStyleName(LEOS_DOC_MODIFIED);
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException("Unexpected error occurred while reading doc file", e);
            }
        }

    }

    private String getFileContent(Entry<String, Object> entry) {
        String xmlContent = null;
        try {
            xmlContent = readFileToString((File) entry.getValue());
        } catch (IOException e) {
            throw new RuntimeException("Unexpected error occurred while reading doc file", e);
        }
        return xmlContent;
    }

    private String[] getAnnexVersionAndNumber(String xmlContent) {
        Pattern pattern = Pattern.compile(DOC_VERSION_START_TAG_REG);
        Matcher matcher = pattern.matcher(xmlContent);
        String docVersion = "";
        if (matcher.find()) {
            int endIndex = xmlContent.indexOf(DOC_VERSION_END_TAG);
            docVersion = xmlContent.substring(matcher.end(), endIndex);
        }
        Pattern patternForAnnexIndex = Pattern.compile(DOC_NUMBER_START_TAG_REG);
        Matcher matcherForAnnexIndex = patternForAnnexIndex.matcher(xmlContent);
        String annexIndex = "";
        if (matcherForAnnexIndex.find()) {
            int endAnnexIndex = xmlContent.indexOf(DOC_NUMBER_END_TAG);
            annexIndex = xmlContent.substring(matcherForAnnexIndex.end(), endAnnexIndex);
        }
        return new String[]{docVersion, annexIndex};
    }

    private static String readFileToString(File file) throws IOException {
        return new String(Files.readAllBytes(file.toPath()), "UTF-8");
    }

    private void registerTabChangeListener() {
        tabsheet.addSelectedTabChangeListener(event -> {
            TabSheet tabsheet = event.getTabSheet();
            HorizontalSplitPanel splitPanel = (HorizontalSplitPanel) tabsheet.getSelectedTab();
            TabSheet.Tab selectedTab = tabsheet.getTab(splitPanel);
            String caption = selectedTab.getCaption();
            String selectedTabId = selectedTab.getId();
            boolean isValidTab = false;
            File tocFile = null;
            String content = "";
            LeosCategory category = null;
            String selectedTabName;

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
                if (!selectedDocument.startsWith(COVER_PAGE_CONTENT_FILE_NAME)) {
                    int annexNumber = 0;
                    if (annexKeyMap.get(selectedDocument) != null) {
                        annexNumber = annexKeyMap.get(selectedDocument);
                    }
                    String xmlContent = LeosDomainUtil.wrapXmlFragment(content);
                    category = xmlContentProcessor.identifyCategory(key,
                            xmlContent.getBytes(StandardCharsets.UTF_8));
                    selectedTabName = getTabName(category, annexNumber, version);
                } else {
                    selectedTabName = getTabName(LeosCategory.COVERPAGE, 0, version);
                    category = LeosCategory.COVERPAGE;
                }
                if (caption.equalsIgnoreCase(selectedTabName) && !annexDeletedMap.containsKey(selectedTabId)) {
                    tocFile = (File) entry.getValue();
                    isValidTab = true;
                    if (annexAddedMap.containsKey(selectedDocument)) {
                        toggleActionButtons(true);
                        content = injectClassAttribute(content, LEOS_CONTENT_NEW);
                        splitPanel.addStyleName(LEOS_CONTENT_ADDED);
                    } else if (annexAddedMap.containsKey(selectedDocument.concat(PROCESSED))) {
                        markAsProcessed();
                    } else if(selectedTab.getStyleName() != null && selectedTab.getStyleName().equalsIgnoreCase(LEOS_DOC_MODIFIED)) {
                        toggleActionButtons(false);
                    } else {
                        hideActionButtonLayout();
                    }
                    break;
                }
            }
            if (!isValidTab && annexDeletedMap.size() > 0) {
                for (Map.Entry<String, Object> entry : annexDeletedMap.entrySet()) {
                    selectedDocument = entry.getKey();
                    int processedIndex = selectedDocument.indexOf(PROCESSED);
                    try {
                        String updatedFileName = processedIndex != -1 ? selectedDocument.substring(0, processedIndex)
                                : selectedDocument;
                        content = readFileToString(((File) originalContentFiles.get(updatedFileName + HTML)));
                        content = injectClassAttribute(content, LEOS_CONTENT_REMOVED);
                    } catch (IOException e) {
                        throw new RuntimeException("Unexpected error occurred while reading doc file", e);
                    }
                    String xmlContent = getFileContent(entry);
                    String[] annexVersionAndNumber = getAnnexVersionAndNumber(xmlContent);
                    selectedTabName = getTabName(LeosCategory.ANNEX, new Integer(annexVersionAndNumber[1]), annexVersionAndNumber[0]);
                    if (caption.equalsIgnoreCase(selectedTabName)) {
                        String fileName;
                        if(processedIndex != -1) {
                            markAsProcessed();
                            fileName = selectedDocument.substring(0, processedIndex);
                        } else {
                            actionButtonsLayout.setVisible(true);
                            toggleActionButtons(true);
                            fileName = selectedDocument;
                            splitPanel.addStyleName(LEOS_CONTENT_REMOVED);
                        }
                        String selectedJsFile = fileName + TOC_JS;
                        tocFile = (File) originalJsFiles.get(selectedJsFile);
                        isValidTab = true;
                        break;
                    } else {
                        actionButtonsLayout.setVisible(true);
                        accept.setVisible(false);
                        reject.setVisible(false);
                    }
                }
            }
            if (isValidTab) {
                prepareTabContentLayout(splitPanel, category, tocFile, content);
            }
        });
    }

    private void toggleActionButtons(boolean visible) {
        actionButtonsLayout.setVisible(true);
        accept.setVisible(visible);
        reject.setVisible(visible);
        viewAndMerge.setVisible(!visible);
    }

    private void hideActionButtonLayout() {
        actionButtonsLayout.setVisible(false);
        accept.setVisible(false);
        reject.setVisible(false);
        viewAndMerge.setVisible(false);
    }

    private String injectClassAttribute(String content, String classAttr) {
        Document doc = XercesUtils.createXercesDocument(content.getBytes(StandardCharsets.UTF_8), true);
        Node prefaceNode = XercesUtils.getElementById(doc, PREFACE);
        if(prefaceNode != null) {
            XercesUtils.addAttribute(prefaceNode, CLASS_ATTR, classAttr);
        }
        Node bodyNode = XercesUtils.getElementById(doc, BODY);
        if(bodyNode != null) {
            XercesUtils.addAttribute(bodyNode, CLASS_ATTR, classAttr);
        }
        return XercesUtils.nodeToString(doc);
    }

    private void addActionButtonLayout() {
        headerLayout.addComponent(actionButtonsLayout);
        headerLayout.setComponentAlignment(actionButtonsLayout, Alignment.BOTTOM_RIGHT);
        headerLayout.setExpandRatio(actionButtonsLayout, 0.30f);

        accept.addClickListener(event -> handleAcceptAction());
        reject.addClickListener(event -> handleRejectAction());
        viewAndMerge.addClickListener(event -> handleMergeAction());
        accept.setVisible(false);
        reject.setVisible(false);
        viewAndMerge.setVisible(false);
    }

    private void handleAcceptAction() {
        if(annexAddedMap != null && annexAddedMap.containsKey(selectedDocument)) {
            eventBus.post(new ContributionAnnexAcceptEvent(selectedDocument,
                    (File)annexAddedMap.get(selectedDocument), true));
        } else if(annexDeletedMap != null && annexDeletedMap.containsKey(selectedDocument)) {
            eventBus.post(new ContributionAnnexAcceptEvent(selectedDocument,
                    (File)annexDeletedMap.get(selectedDocument), false));
        }
    }

    private void handleRejectAction() {
        boolean rejectedAdded = (annexAddedMap != null && annexAddedMap.containsKey(selectedDocument));
        LegDocument legFileToProcess = rejectedAdded ? legDocument : originalLegDocument;
        eventBus.post(new ContributionAnnexRejectEvent(selectedDocument, legFileToProcess));
        markAsProcessed();
    }

    private void handleMergeAction() {
        String docToViewAndMerge = selectedDocument.equals(COVER_PAGE_CONTENT_FILE_NAME) ? proposalRef : selectedDocument;
        eventBus.post(new OpenAndViewContibutionEvent(docToViewAndMerge, docToViewAndMerge +
                "_" + docVersionMap.get(docToViewAndMerge)));
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

    @Subscribe
    public void handleContributionProcessedEvent(ContributionAnnexProcessedEvent event) {
        markAsProcessed();
    }

    private void markAsProcessed() {
        HorizontalSplitPanel splitPanel = (HorizontalSplitPanel) tabsheet.getSelectedTab();
        TabSheet.Tab tab = tabsheet.getTab(splitPanel);
        tab.setStyleName(LEOS_ANNEX_PROCESSED);
        splitPanel.addStyleName(LEOS_CONTENT_PROCESSED);
        actionButtonsLayout.setVisible(false);
    }

    @Override
    protected void handleCloseButton() {
        LOG.info("Closing milestone explorer window....");
        try {
            MilestoneHelper.deleteTempFilesIfExists(legFileTemp);
            eventBus.post(new WindowClosedEvent<>(this));
            super.handleCloseButton();
        } catch (IOException e) {
            LOG.error("Exception occurred while deleting the file " + e);
        } finally {
            LOG.info("Closing milestone explorer window ...");
            super.handleCloseButton();
        }
    }
}
