/*
 * Copyright 2018 European Commission
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
package eu.europa.ec.leos.ui.view.coverpage;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.vaadin.annotations.DesignRoot;
import com.vaadin.annotations.StyleSheet;
import com.vaadin.data.TreeData;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.server.StreamResource;
import com.vaadin.ui.Accordion;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.Label;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.declarative.Design;
import eu.europa.ec.leos.domain.cmis.LeosCategory;
import eu.europa.ec.leos.domain.cmis.document.LegDocument;
import eu.europa.ec.leos.domain.cmis.document.Proposal;
import eu.europa.ec.leos.domain.common.InstanceType;
import eu.europa.ec.leos.domain.vo.DocumentVO;
import eu.europa.ec.leos.domain.vo.SearchMatchVO;
import eu.europa.ec.leos.i18n.MessageHelper;
import eu.europa.ec.leos.model.action.ContributionVO;
import eu.europa.ec.leos.model.action.VersionVO;
import eu.europa.ec.leos.model.user.User;
import eu.europa.ec.leos.security.LeosPermission;
import eu.europa.ec.leos.security.LeosPermissionAuthorityMapHelper;
import eu.europa.ec.leos.security.SecurityContext;
import eu.europa.ec.leos.services.export.ExportVersions;
import eu.europa.ec.leos.services.export.ZipPackageUtil;
import eu.europa.ec.leos.services.processor.content.TableOfContentProcessor;
import eu.europa.ec.leos.services.processor.content.XmlContentProcessor;
import eu.europa.ec.leos.services.toc.StructureContext;
import eu.europa.ec.leos.ui.component.AccordionPane;
import eu.europa.ec.leos.ui.component.ComparisonComponent;
import eu.europa.ec.leos.ui.component.LeosDisplayField;
import eu.europa.ec.leos.ui.component.toc.TableOfContentComponent;
import eu.europa.ec.leos.ui.component.toc.TableOfContentItemConverter;
import eu.europa.ec.leos.ui.component.versions.VersionComparator;
import eu.europa.ec.leos.ui.component.versions.VersionsTab;
import eu.europa.ec.leos.ui.event.StateChangeEvent;
import eu.europa.ec.leos.ui.event.search.ReplaceMatchResponseEvent;
import eu.europa.ec.leos.ui.event.search.SearchTextResponseEvent;
import eu.europa.ec.leos.ui.event.security.SecurityTokenRequest;
import eu.europa.ec.leos.ui.event.security.SecurityTokenResponse;
import eu.europa.ec.leos.ui.event.view.DownloadXmlFilesRequestEvent;
import eu.europa.ec.leos.ui.extension.AnnotateExtension;
import eu.europa.ec.leos.ui.extension.ChangeDetailsExtension;
import eu.europa.ec.leos.ui.extension.RefToLinkExtension;
import eu.europa.ec.leos.ui.extension.UserCoEditionExtension;
import eu.europa.ec.leos.ui.extension.UserGuidanceExtension;
import eu.europa.ec.leos.ui.view.ComparisonDisplayMode;
import eu.europa.ec.leos.ui.view.ScreenLayoutHelper;
import eu.europa.ec.leos.ui.view.TriFunction;
import eu.europa.ec.leos.ui.window.milestone.MilestoneExplorer;
import eu.europa.ec.leos.ui.component.toc.TocEditor;
import eu.europa.ec.leos.vo.coedition.CoEditionVO;
import eu.europa.ec.leos.vo.coedition.InfoType;
import eu.europa.ec.leos.vo.toc.TableOfContentItemVO;
import eu.europa.ec.leos.web.event.NotificationEvent;
import eu.europa.ec.leos.web.event.component.ComparisonResponseEvent;
import eu.europa.ec.leos.web.event.component.LayoutChangeRequestEvent;
import eu.europa.ec.leos.web.event.view.document.CancelActionElementRequestEvent;
import eu.europa.ec.leos.web.event.view.document.CheckElementCoEditionEvent.Action;
import eu.europa.ec.leos.web.event.view.document.CreateEventParameter;
import eu.europa.ec.leos.web.event.view.document.DocumentUpdatedEvent;
import eu.europa.ec.leos.web.event.view.document.FetchUserGuidanceResponse;
import eu.europa.ec.leos.web.event.view.document.InstanceTypeResolver;
import eu.europa.ec.leos.web.event.view.document.RefreshDocumentEvent;
import eu.europa.ec.leos.web.event.view.document.RefreshElementEvent;
import eu.europa.ec.leos.web.model.VersionInfoVO;
import eu.europa.ec.leos.web.support.LeosCacheToken;
import eu.europa.ec.leos.web.support.cfg.ConfigurationHelper;
import eu.europa.ec.leos.web.support.user.UserHelper;
import eu.europa.ec.leos.web.support.xml.DownloadStreamResource;
import eu.europa.ec.leos.web.ui.component.ContentPane;
import eu.europa.ec.leos.web.ui.component.CoverpageComponent;
import eu.europa.ec.leos.web.ui.component.SearchDelegate;
import eu.europa.ec.leos.web.ui.component.actions.CoverpageActionsMenuBar;
import eu.europa.ec.leos.web.ui.screen.document.ColumnPosition;
import eu.europa.ec.leos.web.ui.themes.LeosTheme;
import eu.europa.ec.leos.web.ui.window.IntermediateVersionWindow;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.vaadin.dialogs.ConfirmDialog;

import javax.annotation.PostConstruct;
import javax.inject.Provider;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

@DesignRoot("CoverPageScreenDesign.html")
@StyleSheet({"vaadin://../assets/css/coverpage.css" + LeosCacheToken.TOKEN})
abstract class CoverPageScreenImpl extends VerticalLayout implements CoverPageScreen {

    private static final long serialVersionUID = 1L;
    private static final Logger LOG = LoggerFactory.getLogger(CoverPageScreenImpl.class);
    public static SimpleDateFormat dataFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm");

    protected EventBus eventBus;
    protected UserHelper userHelper;
    protected SecurityContext securityContext;
    protected MessageHelper messageHelper;
    protected ConfigurationHelper cfgHelper;
    protected TocEditor tocEditor;
    protected InstanceTypeResolver instanceTypeResolver;
    private XmlContentProcessor xmlContentProcessor;

    //dummy init to avoid design exception
    protected ScreenLayoutHelper screenLayoutHelper = new ScreenLayoutHelper(null, null) ;
    
    protected HorizontalSplitPanel coverPageSplit;
    protected HorizontalSplitPanel contentSplit;
    protected Label coverPageTitle;

    protected TableOfContentComponent tableOfContentComponent = new TableOfContentComponent();
    protected AccordionPane accordionPane;
    protected Accordion accordion;
    protected VersionsTab<Proposal> versionsTab;

    protected ComparisonComponent<Proposal> comparisonComponent;
    protected CoverpageComponent coverDoc;
    protected LeosDisplayField coverContent;

    protected Button refreshNoteButton;
    protected Button refreshButton;
    protected Button searchButton;

    protected CoverpageActionsMenuBar actionsMenuBar;
    protected Label versionInfoLabel;

    protected UserCoEditionExtension<LeosDisplayField, String> userCoEditionExtension;
    protected Provider<StructureContext> structureContextProvider;
    protected VersionComparator versionComparator;
    private SearchDelegate searchDelegate;
    private TableOfContentProcessor tableOfContentProcessor;

    @Value("${leos.coedition.sip.enabled}")
    private boolean coEditionSipEnabled;

    @Value("${leos.coedition.sip.domain}")
    private String coEditionSipDomain;

    @Value("${leos.searchAndReplace.enabled}")
    private boolean searchAndReplaceEnabled;

    @Value("${leos.coverpage.separated}")
    private boolean coverPageSeparated;

    @Autowired
    LeosPermissionAuthorityMapHelper authorityMapHelper;

    CoverPageScreenImpl(SecurityContext securityContext, EventBus eventBus, MessageHelper messageHelper, ConfigurationHelper cfgHelper,
                        UserHelper userHelper, TocEditor tocEditor, VersionsTab<Proposal> versionsTab,
                        InstanceTypeResolver instanceTypeResolver, Provider<StructureContext> structureContextProvider,
                        VersionComparator versionComparator, TableOfContentProcessor tableOfContentProcessor,
                        XmlContentProcessor xmlContentProcessor) {
        LOG.trace("Initializing coverPage screen...");
        Validate.notNull(securityContext, "SecurityContext must not be null!");
        this.securityContext = securityContext;
        Validate.notNull(eventBus, "EventBus must not be null!");
        this.eventBus = eventBus;
        Validate.notNull(messageHelper, "MessageHelper must not be null!");
        this.messageHelper = messageHelper;
        Validate.notNull(userHelper, "UserHelper must not be null!");
        this.userHelper = userHelper;
        Validate.notNull(cfgHelper, "Configuration helper must not be null!");
        this.cfgHelper = cfgHelper;
        Validate.notNull(tocEditor, "TocEditor must not be null!");
        this.tocEditor = tocEditor;
        Validate.notNull(instanceTypeResolver, "instanceTypeResolver must not be null!");
        this.instanceTypeResolver = instanceTypeResolver;
        Validate.notNull(versionsTab, "versionsTab must not be null!");
        this.versionsTab = versionsTab;
        Validate.notNull(structureContextProvider, "structureContextProvider must not be null!");
        this.structureContextProvider = structureContextProvider;
        Validate.notNull(versionComparator, "versionComparator must not be null!");
        this.versionComparator = versionComparator;
        Validate.notNull(tableOfContentProcessor, "tableOfContentProcessor must not be null!");
        this.tableOfContentProcessor = tableOfContentProcessor;
        this.xmlContentProcessor = xmlContentProcessor;

        Design.read(this);
        initListeners();
    }

    @PostConstruct
    public void initSearchAndReplace() {
        searchDelegate = new SearchDelegate(searchButton, coverDoc, messageHelper, eventBus, coverContent, searchAndReplaceEnabled);
    }

    protected void changeLayout(LayoutChangeRequestEvent event, Object obj) {
        if (event.getOriginatingComponent() == ComparisonComponent.class) {
            if (!event.getPosition().equals(ColumnPosition.OFF)) {
                comparisonComponent.setContent((ContentPane) obj);
            } else {
                comparisonComponent.setContent(null);
            }
        }
        screenLayoutHelper.changePosition(event.getPosition(), event.getOriginatingComponent());
    }

    @Override
    public void setTitle(String title) {
        coverPageTitle.setValue(title);
    }

    @Override
    public void setContent(String content) {
        coverContent.setValue(addTimestamp(content));
        refreshNoteButton.setVisible(false);
    }

    void init() {
        tableOfContentComponent = new TableOfContentComponent(messageHelper, eventBus, securityContext, cfgHelper, tocEditor, structureContextProvider, tableOfContentProcessor);
        accordion.addTab(tableOfContentComponent, messageHelper.getMessage("toc.title"), VaadinIcons.CHEVRON_DOWN);
        accordion.addTab(versionsTab, messageHelper.getMessage("document.accordion.versions"), VaadinIcons.CHEVRON_RIGHT);

        accordion.addListener(event -> {
            final Component selected = ((Accordion) event.getSource()).getSelectedTab();
            for (int i = 0; i < accordion.getComponentCount(); i++) {
                TabSheet.Tab tab = accordion.getTab(i);
                if (tab.getComponent().getClass().equals(selected.getClass())) {
                    tab.setIcon(VaadinIcons.CHEVRON_DOWN);
                } else {
                    tab.setIcon(VaadinIcons.CHEVRON_RIGHT);
                }
            }
        });

        contentSplit.setId(ScreenLayoutHelper.CONTENT_SPLITTER);
        coverPageSplit.setId(ScreenLayoutHelper.TOC_SPLITTER);
        screenLayoutHelper = new ScreenLayoutHelper(eventBus, Arrays.asList(contentSplit, coverPageSplit));
        screenLayoutHelper.addPane(coverDoc, 1, true);
        screenLayoutHelper.addPane(accordionPane, 0, true);
        screenLayoutHelper.layoutComponents();

        new UserGuidanceExtension<>(coverContent, eventBus);
        new RefToLinkExtension<>(coverContent);
        userCoEditionExtension = new UserCoEditionExtension<>(coverContent, messageHelper, securityContext, cfgHelper);
        new ChangeDetailsExtension<>(coverContent, eventBus);

        refreshNoteButton();
        refreshButton();

        markAsDirty();
    }

    private void initListeners() {
        //LEOS-5435: Screen level controls are disabled for now as they are interrupting editor level scrolling.
        //this.addShortcutListener(new CtrlHomeListener("leos-doc-content"));
        //this.addShortcutListener(new CtrlEndListener("leos-doc-content"));
    }

    @Override
    public void attach() {
        eventBus.register(this);
        eventBus.register(screenLayoutHelper);
        super.attach();
    }

    @Override
    public void detach() {
        searchDelegate.detach();
        super.detach();
        eventBus.unregister(screenLayoutHelper);
        eventBus.unregister(this);
    }

    @Override
    public void setUserGuidance(String userGuidance) {
        eventBus.post(new FetchUserGuidanceResponse(userGuidance));
    }

    private void refreshNoteButton() {
        refreshNoteButton.setCaptionAsHtml(true);
        refreshNoteButton.setCaption(messageHelper.getMessage("document.request.refresh.msg"));
        refreshNoteButton.setIcon(LeosTheme.LEOS_INFO_YELLOW_16);
        refreshNoteButton.addClickListener(new ClickListener() {
            private static final long serialVersionUID = 3714441703159576377L;

            @Override
            public void buttonClick(ClickEvent event) {
                eventBus.post(new RefreshDocumentEvent());
                eventBus.post(new DocumentUpdatedEvent(false)); //Document might be updated.
            }
        });
    }

    // create text refresh button
    private void refreshButton() {
        refreshButton.addClickListener(new ClickListener() {
            private static final long serialVersionUID = 3714441703159576377L;

            @Override
            public void buttonClick(ClickEvent event) {
                eventBus.post(new RefreshDocumentEvent());
                eventBus.post(new DocumentUpdatedEvent(false)); //Document might be updated.
            }
        });
    }

    public abstract void showVersion(String content, String versionInfo);

    public abstract void showCleanVersion(String content, String versionInfo);

    public abstract boolean isCleanVersionShowed();

    public abstract void populateComparisonContent(String comparedContent, String versionInfo, Proposal original, Proposal current);

    public abstract void populateDoubleComparisonContent(String comparedContent, String versionInfo);

    @Override
    public void setToc(final List<TableOfContentItemVO> tableOfContentItemVoList) {
        TreeData<TableOfContentItemVO> treeTocData= TableOfContentItemConverter.buildTocData(tableOfContentItemVoList);
        tableOfContentComponent.setTableOfContent(treeTocData);
    }

    @Override
    public void showElementEditor(final String elementId, final String  elementTagName, final String elementFragment) {
        eventBus.post(instanceTypeResolver.createEvent(new CreateEventParameter(elementId, elementTagName, elementFragment, LeosCategory.PROPOSAL.name(),
                securityContext.getUser(),authorityMapHelper.getPermissionsForRoles(securityContext.getUser().getRoles()))));
    }

    @Override
    public void refreshElementEditor(final String elementId, final String  elementTagName, final String elementFragment) {
        eventBus.post(new RefreshElementEvent(elementId, elementTagName, elementFragment));
    }

    @Override
    public void displayComparison(HashMap<ComparisonDisplayMode, Object> htmlResult){
        eventBus.post(new ComparisonResponseEvent(htmlResult,LeosCategory.PROPOSAL.name().toLowerCase()));
    }

    @Override
    public void showIntermediateVersionWindow() {
        IntermediateVersionWindow intermediateVersionWindow = new IntermediateVersionWindow(messageHelper, eventBus);
        UI.getCurrent().addWindow(intermediateVersionWindow);
        intermediateVersionWindow.center();
        intermediateVersionWindow.focus();
    }

    @Override
    public void setDocumentVersionInfo(VersionInfoVO versionInfoVO) {
        this.versionInfoLabel.setValue(messageHelper.getMessage("document.version.caption", versionInfoVO.getDocumentVersion(), versionInfoVO.getLastModifiedBy(), versionInfoVO.getEntity(), versionInfoVO.getLastModificationInstant()));
    }

    @Subscribe
    public void handleElementState(StateChangeEvent event) {
        if(event.getState() != null) {
            actionsMenuBar.setSaveVersionEnabled(event.getState().isState());
            refreshButton.setEnabled(event.getState().isState());
            refreshNoteButton.setEnabled(event.getState().isState());
        }
    }

    private String addTimestamp(String docContentText) {
        /* KLUGE: In order to force the update of the docContent on the client side
         * the unique seed is added on every docContent update, please note markDirty
         * method did not work, this was the only solution worked.*/
        String seed = "<div style='display:none' >" +
                new Date().getTime() +
                "</div>";
        return docContentText + seed;
    }

    @Subscribe
    public void fetchToken(SecurityTokenRequest event){
        eventBus.post(new SecurityTokenResponse(securityContext.getAnnotateToken(event.getUrl())));
    }

    @Override
    public void updateUserCoEditionInfo(List<CoEditionVO> coEditionVos, String presenterId) {
        this.getUI().access(() -> {
            tableOfContentComponent.updateUserCoEditionInfo(coEditionVos, presenterId);
            userCoEditionExtension.updateUserCoEditionInfo(coEditionVos, presenterId);
        });
    }

    @Override
    public void displayDocumentUpdatedByCoEditorWarning() {
        this.getUI().access(() -> {
            refreshNoteButton.setVisible(true);
        });
    }

    @Override
    public void checkElementCoEdition(List<CoEditionVO> coEditionVos, User user, final String elementId, final String elementTagName, final Action action, final Object actionEvent) {
        StringBuilder coEditorsList = new StringBuilder();
        coEditionVos.stream().filter((x) -> InfoType.ELEMENT_INFO.equals(x.getInfoType()) && x.getElementId().equals(elementId))
                .sorted(Comparator.comparing(CoEditionVO::getUserName).thenComparingLong(CoEditionVO::getEditionTime)).forEach(x -> {
                    StringBuilder userDescription = new StringBuilder();
                    if (!x.getUserLoginName().equals(user.getLogin())) {
                        userDescription.append("<a href=\"")
                                .append(StringUtils.isEmpty(x.getUserEmail()) ? "" : (coEditionSipEnabled ? new StringBuilder("sip:").append(x.getUserEmail().replaceFirst("@.*", "@" + coEditionSipDomain)).toString()
                                        : new StringBuilder("mailto:").append(x.getUserEmail()).toString()))
                                .append("\">").append(x.getUserName()).append(" (").append(StringUtils.isEmpty(x.getEntity()) ? "-" : x.getEntity())
                                .append(")</a>");
                    } else {
                        userDescription.append(x.getUserName()).append(" (").append(StringUtils.isEmpty(x.getEntity()) ? "-" : x.getEntity()).append(")");
                    }
                    coEditorsList.append("&nbsp;&nbsp;-&nbsp;")
                            .append(messageHelper.getMessage("coedition.tooltip.message", userDescription, dataFormat.format(new Date(x.getEditionTime()))))
                            .append("<br>");
                });
        if (!StringUtils.isEmpty(coEditorsList)) {
            confirmCoEdition(coEditorsList.toString(), elementId, action, actionEvent);
        } else {
            eventBus.post(actionEvent);
        }
    }

    private void confirmCoEdition(String coEditorsList, String elementId, Action action, Object actionEvent) {
        ConfirmDialog confirmDialog = ConfirmDialog.getFactory().create(
                messageHelper.getMessage("coedition." + action.getValue() + ".element.confirmation.title"),
                messageHelper.getMessage("coedition." + action.getValue() + ".element.confirmation.message", coEditorsList),
                messageHelper.getMessage("coedition." + action.getValue() + ".element.confirmation.confirm"),
                messageHelper.getMessage("coedition." + action.getValue() + ".element.confirmation.cancel"), null);
        confirmDialog.setContentMode(ConfirmDialog.ContentMode.HTML);
        confirmDialog.getContent().setHeightUndefined();
        confirmDialog.setHeightUndefined();
        confirmDialog.show(getUI(), dialog -> {
            if (dialog.isConfirmed()) {
                eventBus.post(actionEvent);
            } else {
                eventBus.post(new CancelActionElementRequestEvent(elementId));
            }
        }, true);
    }

    @Override
    public void showAlertDialog(String messageKey) {
        ConfirmDialog confirmDialog = ConfirmDialog.getFactory().create(
                messageHelper.getMessage(messageKey + ".alert.title"),
                messageHelper.getMessage(messageKey + ".alert.message"),
                messageHelper.getMessage(messageKey + ".alert.confirm"), null, null);
        confirmDialog.setContentMode(ConfirmDialog.ContentMode.HTML);
        confirmDialog.getContent().setHeightUndefined();
        confirmDialog.setHeightUndefined();
        confirmDialog.getCancelButton().setVisible(false);
        confirmDialog.show(getUI(), dialog -> {}, true);
    }

    protected boolean componentEnabled(Class className){
        return screenLayoutHelper.isPaneEnabled(className);
    }


    @Override
    public void scrollToMarkedChange(String elementId) {
    }

    @Override
    public boolean isTocEnabled() {
        return screenLayoutHelper.isTocPaneEnabled();
    }


    @Override
    public void setDataFunctions(DocumentVO annexVO, List<VersionVO> allVersions,
            List<ContributionVO> allContributions,
            BiFunction<Integer, Integer, List<Proposal>> majorVersionsFn, Supplier<Integer> countMajorVersionsFn,
            TriFunction<String, Integer, Integer, List<Proposal>> minorVersionsFn, Function<String, Integer> countMinorVersionsFn,
            BiFunction<Integer, Integer, List<Proposal>> recentChangesFn, Supplier<Integer> countRecentChangesFn) {

        boolean canRestorePreviousVersion = securityContext.hasPermission(annexVO, LeosPermission.CAN_RESTORE_PREVIOUS_VERSION);
        boolean canDownload = securityContext.hasPermission(annexVO, LeosPermission.CAN_DOWNLOAD_XML_COMPARISON);

        versionsTab.setDataFunctions(allVersions, minorVersionsFn, countMinorVersionsFn,
                recentChangesFn, countRecentChangesFn, versionComparator.isCompareModeAvailable(), false,
                canRestorePreviousVersion, canDownload);

    }

    @Override
    public void refreshVersions(List<VersionVO> allVersions, boolean isComparisonMode) {
        versionsTab.refreshVersions(allVersions, isComparisonMode);
    }

    @Override
    public void showMilestoneExplorer(LegDocument legDocument, String milestoneTitle, String proposalRef) {
        MilestoneExplorer milestoneExplorer = new MilestoneExplorer(legDocument, milestoneTitle, proposalRef, messageHelper, eventBus, cfgHelper,
                securityContext, userHelper, xmlContentProcessor, isCoverPageVisible());
        UI.getCurrent().addWindow(milestoneExplorer);
        milestoneExplorer.center();
        milestoneExplorer.focus();
    }

    @Override
    public void initAnnotations(DocumentVO coverPage, String proposalRef, String connectedEntity) {
        new AnnotateExtension<>(coverContent, eventBus, cfgHelper, null, AnnotateExtension.OperationMode.NORMAL,
                ConfigurationHelper.isAnnotateAuthorityEquals(cfgHelper, "LEOS"), true, proposalRef,
                connectedEntity);
    }

    @Override
    public void setDownloadStreamResourceForVersion(StreamResource streamResource, String documentId){
        versionsTab.setDownloadStreamResourceForVersion(streamResource, documentId);
    }

    @Override
    public void showMatchResults(Long searchId, List<SearchMatchVO> results) {
        eventBus.post(new SearchTextResponseEvent(searchId, results));
    }

    @Override
    public void closeSearchBar() {
        searchDelegate.closeSearchBarComponent();
    }

    @Override
    public void refineSearch(Long searchId, int matchedIndex, boolean isReplaced) {
        eventBus.post(new ReplaceMatchResponseEvent(searchId, matchedIndex, isReplaced));
    }

    @Override
    public boolean isCoverPageVisible() {
        return !coverPageSeparated;
    }

    @Override
    public Optional<ContributionVO> findContributionAndShowTab(String revisionVersion) {
        return Optional.empty();
    }
}