/*
 * Copyright 2022 European Commission
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
package eu.europa.ec.leos.ui.view.financialstatement;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.vaadin.annotations.DesignRoot;
import com.vaadin.data.TreeData;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.server.StreamResource;
import com.vaadin.shared.ui.ContentMode;
import com.vaadin.shared.ui.dnd.EffectAllowed;
import com.vaadin.spring.annotation.SpringComponent;
import com.vaadin.spring.annotation.ViewScope;
import com.vaadin.ui.Accordion;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.Label;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.declarative.Design;
import com.vaadin.ui.dnd.DragSourceExtension;
import com.vaadin.ui.dnd.event.DragStartListener;
import cool.graph.cuid.Cuid;
import eu.europa.ec.leos.domain.cmis.document.FinancialStatement;
import eu.europa.ec.leos.domain.cmis.document.LegDocument;
import eu.europa.ec.leos.domain.cmis.metadata.LeosMetadata;
import eu.europa.ec.leos.domain.vo.DocumentVO;
import eu.europa.ec.leos.domain.vo.SearchMatchVO;
import eu.europa.ec.leos.i18n.MessageHelper;
import eu.europa.ec.leos.model.action.ContributionVO;
import eu.europa.ec.leos.model.action.VersionVO;
import eu.europa.ec.leos.model.annex.LevelItemVO;
import eu.europa.ec.leos.model.user.User;
import eu.europa.ec.leos.security.LeosPermission;
import eu.europa.ec.leos.security.LeosPermissionAuthorityMapHelper;
import eu.europa.ec.leos.security.SecurityContext;
import eu.europa.ec.leos.services.processor.content.TableOfContentHelper;
import eu.europa.ec.leos.services.processor.content.TableOfContentProcessor;
import eu.europa.ec.leos.services.processor.content.XmlContentProcessor;
import eu.europa.ec.leos.services.support.XmlHelper;
import eu.europa.ec.leos.services.toc.StructureContext;
import eu.europa.ec.leos.ui.component.AccordionPane;
import eu.europa.ec.leos.ui.component.ComparisonComponent;
import eu.europa.ec.leos.ui.component.LeosDisplayField;
import eu.europa.ec.leos.ui.component.markedText.MarkedTextComponent;
import eu.europa.ec.leos.ui.component.toc.TableOfContentComponent;
import eu.europa.ec.leos.ui.component.toc.TableOfContentItemConverter;
import eu.europa.ec.leos.ui.component.toc.TocEditor;
import eu.europa.ec.leos.ui.component.versions.VersionsTab;
import eu.europa.ec.leos.ui.event.InitLeosEditorEvent;
import eu.europa.ec.leos.ui.event.StateChangeEvent;
import eu.europa.ec.leos.ui.event.toc.DisableEditTocEvent;
import eu.europa.ec.leos.ui.event.toc.ExpandTocSliderPanel;
import eu.europa.ec.leos.ui.event.toc.InlineTocCloseRequestEvent;
import eu.europa.ec.leos.ui.extension.ActionManagerExtension;
import eu.europa.ec.leos.ui.extension.AnnotateExtension;
import eu.europa.ec.leos.ui.extension.ChangeDetailsExtension;
import eu.europa.ec.leos.ui.extension.CheckBoxesExtension;
import eu.europa.ec.leos.ui.extension.LeosEditorExtension;
import eu.europa.ec.leos.ui.extension.MathJaxExtension;
import eu.europa.ec.leos.ui.extension.RefToLinkExtension;
import eu.europa.ec.leos.ui.extension.UserCoEditionExtension;
import eu.europa.ec.leos.ui.view.ComparisonDisplayMode;
import eu.europa.ec.leos.ui.view.ScreenLayoutHelper;
import eu.europa.ec.leos.ui.view.TriFunction;
import eu.europa.ec.leos.vo.coedition.CoEditionVO;
import eu.europa.ec.leos.vo.toc.OptionsType;
import eu.europa.ec.leos.vo.toc.TableOfContentItemVO;
import eu.europa.ec.leos.vo.toc.TocItem;
import eu.europa.ec.leos.web.event.component.LayoutChangeRequestEvent;
import eu.europa.ec.leos.web.event.view.document.CheckElementCoEditionEvent;
import eu.europa.ec.leos.web.event.view.document.DocumentUpdatedEvent;
import eu.europa.ec.leos.web.event.view.document.InstanceTypeResolver;
import eu.europa.ec.leos.web.event.view.document.RefreshDocumentEvent;
import eu.europa.ec.leos.web.model.VersionInfoVO;
import eu.europa.ec.leos.web.support.cfg.ConfigurationHelper;
import eu.europa.ec.leos.web.support.user.UserHelper;
import eu.europa.ec.leos.web.support.xml.DownloadStreamResource;
import eu.europa.ec.leos.web.ui.component.ContentPane;
import eu.europa.ec.leos.web.ui.component.FinancialstatementComponent;
import eu.europa.ec.leos.web.ui.component.SearchDelegate;
import eu.europa.ec.leos.web.ui.component.actions.FinancialstatementActionsMenuBar;
import eu.europa.ec.leos.web.ui.screen.document.ColumnPosition;
import eu.europa.ec.leos.web.ui.themes.LeosTheme;
import eu.europa.ec.leos.web.ui.window.TimeLineWindow;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.vaadin.sliderpanel.SliderPanel;
import org.vaadin.sliderpanel.SliderPanelBuilder;
import org.vaadin.sliderpanel.client.SliderMode;
import org.vaadin.sliderpanel.client.SliderTabPosition;

import javax.annotation.PostConstruct;
import javax.inject.Provider;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

@SpringComponent
@ViewScope
@DesignRoot("FinancialStatementScreenDesign.html")
abstract public class FinancialStatementScreenImpl extends VerticalLayout implements FinancialStatementScreen {

    private static final long serialVersionUID = 1L;

    private static final Logger LOG = LoggerFactory.getLogger(FinancialStatementScreenImpl.class);

    protected TimeLineWindow<FinancialStatement> timeLineWindow;
    protected HorizontalSplitPanel financialStatementSplit;
    protected HorizontalSplitPanel contentSplit;
    protected Label financialStatementTitle;

    // dummy init to avoid design exception
    protected ScreenLayoutHelper screenLayoutHelper = new ScreenLayoutHelper(null, null);
    protected SliderPanel leftSlider = new SliderPanelBuilder(new VerticalLayout()).build();

    protected ComparisonComponent<FinancialStatement> comparisonComponent;
    protected FinancialstatementComponent financialStatementDoc;
    protected HorizontalLayout mainLayout;
    protected VerticalLayout financialStatementLayout;
    protected HorizontalLayout financialStatementToolBar;
    protected LeosDisplayField financialStatementContent;

    protected TableOfContentComponent tableOfContentComponent = new TableOfContentComponent();
    protected AccordionPane accordionPane;
    protected Accordion accordion;
    protected VersionsTab<FinancialStatement> versionsTab;

    protected FinancialstatementActionsMenuBar actionsMenuBar;
    protected Label versionInfoLabel;
    protected Button refreshNoteButton;
    protected Button refreshButton;
    protected Button searchButton;
    protected Provider<StructureContext> structureContextProvider;
    protected MessageHelper messageHelper;
    protected ConfigurationHelper cfgHelper;
    protected InstanceTypeResolver instanceTypeResolver;
    protected TocEditor tocEditor;
    protected UserCoEditionExtension<LeosDisplayField, String> userCoEditionExtension;
    protected LeosEditorExtension<LeosDisplayField> leosEditorExtension;
    protected ActionManagerExtension<LeosDisplayField> actionManagerExtension;
    protected final EventBus eventBus;
    protected final UserHelper userHelper;
    protected final SecurityContext securityContext;
    protected final XmlContentProcessor xmlContentProcessor;
    private final TableOfContentProcessor tableOfContentProcessor;
    private static final String CHECKED = "\u2611";
    private static final String UNCHECKED = "\u2610";

    private AnnotateExtension<LeosDisplayField, String> annotateExtension;
    private SearchDelegate searchDelegate;

    @Value("${leos.coedition.sip.enabled}")
    private boolean coEditionSipEnabled;

    @Value("${leos.coedition.sip.domain}")
    private String coEditionSipDomain;

    @Value("${leos.searchAndReplace.enabled}")
    private boolean searchAndReplaceEnabled;

    @Autowired
    LeosPermissionAuthorityMapHelper authorityMapHelper;


    @Autowired
    FinancialStatementScreenImpl(MessageHelper messageHelper, EventBus eventBus, SecurityContext securityContext, UserHelper userHelper,
                                 ConfigurationHelper cfgHelper, TocEditor tocEditor, InstanceTypeResolver instanceTypeResolver, VersionsTab<FinancialStatement> versionsTab,
                                 Provider<StructureContext> structureContextProvider, TableOfContentProcessor tableOfContentProcessor,
                                 XmlContentProcessor xmlContentProcessor) {
        LOG.trace("Initializing explanatory screen...");
        Validate.notNull(messageHelper, "MessageHelper must not be null!");
        this.messageHelper = messageHelper;
        Validate.notNull(eventBus, "EventBus must not be null!");
        this.eventBus = eventBus;
        Validate.notNull(securityContext, "SecurityContext must not be null!");
        this.securityContext = securityContext;
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
        Validate.notNull(tableOfContentProcessor, "tableOfContentProcessor must not be null!");
        this.tableOfContentProcessor = tableOfContentProcessor;
        this.xmlContentProcessor = xmlContentProcessor;
        timeLineWindow = new TimeLineWindow<>(messageHelper, eventBus);
        Design.read(this);
        init();
    }

    void init() {
        new CheckBoxesExtension<>(financialStatementContent, eventBus, XmlHelper.INDENT, CHECKED, UNCHECKED);
        markAsDirty();
        actionsMenuBar.setChildComponentClass(MarkedTextComponent.class);
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
        financialStatementSplit.setId(ScreenLayoutHelper.TOC_SPLITTER);
        screenLayoutHelper = new ScreenLayoutHelper(eventBus, Arrays.asList(contentSplit, financialStatementSplit));
        screenLayoutHelper.addPane(financialStatementDoc, 1, true);
        screenLayoutHelper.addPane(accordionPane, 0, true);

        new MathJaxExtension<>(financialStatementContent);
        new RefToLinkExtension<>(financialStatementContent);
        userCoEditionExtension = new UserCoEditionExtension<>(financialStatementContent, messageHelper, securityContext, cfgHelper);
        new ChangeDetailsExtension<>(financialStatementContent, eventBus);

        refreshNoteButton();
        refreshButton();

        markAsDirty();

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
    public void setTitle(String title) {
        financialStatementTitle.setValue(StringEscapeUtils.escapeHtml4(title));
        financialStatementTitle.setWidth("100%");
    }

    @Override
    public void setContent(String content) {
        financialStatementContent.setValue(addTimestamp(content));
        refreshNoteButton.setVisible(false);
    }

    @Override
    public void refreshElementEditor(String elementId, String elementTagName, String elementContent) {
    }

    @Override
    public void showElementEditor(String elementId, String elementTagName, String element, LevelItemVO levelItemVO) {
    }

    @Override
    public void showTimeLineWindow(List<FinancialStatement> documentVersions) {
    }

    @Override
    public void updateTimeLineWindow(List<FinancialStatement> documentVersions) {
    }

    @Override
    public void displayComparison(HashMap<ComparisonDisplayMode, Object> htmlCompareResult) {
    }

    @Override
    public void showIntermediateVersionWindow() {
    }

    @Override
    public void setDocumentVersionInfo(VersionInfoVO versionInfoVO) {
        String baseVersionStr = "";
        String baseECVersion = messageHelper.getMessage("document.base.ec.version");
        String revisedBaseVersion = versionInfoVO.getRevisedBaseVersion();
        if(!StringUtils.isEmpty(revisedBaseVersion) && !baseECVersion.equalsIgnoreCase(revisedBaseVersion)) {
            baseVersionStr = messageHelper.getMessage("document.base.version.toolbar.info", versionInfoVO.getBaseVersionTitle(),
                    versionInfoVO.getRevisedBaseVersion());
        }
        this.versionInfoLabel.setValue(messageHelper.getMessage("document.version.caption",
                versionInfoVO.getDocumentVersion(), versionInfoVO.getLastModifiedBy(), versionInfoVO.getEntity(),
                versionInfoVO.getLastModificationInstant()) + " " + baseVersionStr);
    }

    @Override
    public void setToc(List<TableOfContentItemVO> tocItemVoList) {
        TreeData<TableOfContentItemVO> tocData = TableOfContentItemConverter.buildTocData(tocItemVoList);
        tableOfContentComponent.setTableOfContent(tocData);
    }

    @Subscribe
    public void handleElementState(StateChangeEvent event) {
        if(event.getState() != null) {
            actionsMenuBar.setSaveVersionEnabled(event.getState().isState());
            refreshButton.setEnabled(event.getState().isState());
            refreshNoteButton.setEnabled(event.getState().isState());
            searchDelegate.handleElementState(event);
        }
    }

    @Override
    public void setPermissions(DocumentVO documentVO, boolean isClonedProposal){
        boolean enableUpdate = securityContext.hasPermission(documentVO, LeosPermission.CAN_UPDATE);
        actionsMenuBar.setSaveVersionVisible(enableUpdate);
        tableOfContentComponent.setPermissions(false);
        searchButton.setVisible(enableUpdate);

        // add extensions only if the user has the permission.
        if(enableUpdate) {
            if(leosEditorExtension == null) {
                eventBus.post(new InitLeosEditorEvent(documentVO));
            }
            if(actionManagerExtension == null) {
                actionManagerExtension = new ActionManagerExtension<>(financialStatementContent, instanceTypeResolver.getInstanceType(), eventBus, structureContextProvider.get().getTocItems());
            }
        }
    }

    @Override
    public void initLeosEditor(DocumentVO documentVO, List<LeosMetadata> documentsMetadata) {
        leosEditorExtension = new LeosEditorExtension<>(financialStatementContent, eventBus, cfgHelper,
                structureContextProvider.get().getTocItems(), structureContextProvider.get().getNumberingConfigs(),
                null, documentsMetadata, documentVO.getMetadata().getInternalRef());
    }

    @PostConstruct
    public void initSearchAndReplace() {
        searchDelegate = new SearchDelegate(searchButton, financialStatementLayout, messageHelper, eventBus, financialStatementContent, searchAndReplaceEnabled);
    }

    @Override
    public void initAnnotations(DocumentVO FinancialStatement, String proposalRef, String connectedEntity) {
        annotateExtension = new AnnotateExtension<>(financialStatementContent, eventBus, cfgHelper, null, AnnotateExtension.OperationMode.NORMAL,
                ConfigurationHelper.isAnnotateAuthorityEquals(cfgHelper, "LEOS"), true, proposalRef,
                connectedEntity);
    }

    @Override
    public void scrollToMarkedChange(String elementId) {

    }

    @Override
    public void sendUserPermissions(List<LeosPermission> userPermissions) {

    }

    @Override
    public void updateUserCoEditionInfo(List<CoEditionVO> coEditionVos, String presenterId) {

    }

    @Override
    public void displayDocumentUpdatedByCoEditorWarning() {

    }

    @Override
    public void checkElementCoEdition(List<CoEditionVO> coEditionVos, User user, String elementId, String elementTagName, CheckElementCoEditionEvent.Action action, Object actionEvent) {

    }

    @Override
    public void showAlertDialog(String messageKey) {

    }

    @Override
    public void enableTocEdition(List<TableOfContentItemVO> tableOfContent) {

    }

    @Override
    public boolean isTocEnabled() {
        return false;
    }

    @Override
    public void setDataFunctions(DocumentVO FinancialStatementVO, List<VersionVO> allVersions, List<ContributionVO> allContributions, BiFunction<Integer, Integer, List<FinancialStatement>> majorVersionsFn, Supplier<Integer> countMajorVersionsFn, TriFunction<String, Integer, Integer, List<FinancialStatement>> minorVersionsFn, Function<String, Integer> countMinorVersionsFn, BiFunction<Integer, Integer, List<FinancialStatement>> recentChangesFn, Supplier<Integer> countRecentChangesFn) {

    }

    @Override
    public void setContributionsData(List<ContributionVO> allContributions) {

    }

    @Override
    public void refreshVersions(List<VersionVO> allVersions, boolean isComparisonMode) {

    }

    @Override
    public void showVersion(String versionContent, String versionInfo) {

    }

    @Override
    public void showRevision(String versionContent, ContributionVO contributionVO, List<TocItem> tocItemList) {

    }

    @Override
    public void showRevisionWithSidebar(String versionContent, ContributionVO contributionVO, List<TocItem> tocItemList, String temporaryAnnotationsId) {

    }

    @Override
    public void disableMergePane() {

    }

    @Override
    public void populateContributions(List<ContributionVO> allContributions) {

    }

    @Override
    public void showCleanVersion(String content, String versionInfo) {

    }

    @Override
    public boolean isCleanVersionVisible() {
        return false;
    }

    @Override
    public void cleanComparedContent() {

    }

    @Override
    public void showMilestoneExplorer(LegDocument legDocument, String milestoneTitle, String proposalRef) {

    }

    @Override
    public void scrollTo(String elementId) {

    }

    @Override
    public void setDownloadStreamResourceForExport(StreamResource streamResource) {

    }

    @Override
    public void setDownloadStreamResourceForMenu(DownloadStreamResource streamResource) {

    }

    @Override
    public void setDownloadStreamResourceForVersion(StreamResource streamResource, String documentId) {

    }

    @Override
    public void setDownloadStreamResourceForXmlFiles(FinancialStatement original, FinancialStatement intermediate, FinancialStatement current, String language, String comparedInfo, String leosComparedContent, String docuWriteComparedContent) {

    }

    @Override
    public void showMatchResults(Long searchId, List<SearchMatchVO> results) {

    }

    @Override
    public void closeSearchBar() {

    }

    @Override
    public void refineSearch(Long searchId, int matchedIndex, boolean isReplaced) {

    }

    @Override
    public boolean isCoverPageVisible() {
        return false;
    }

    @Override
    public Optional<ContributionVO> findContributionAndShowTab(String revisionVersion) {
        return Optional.empty();
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

    private void refreshNoteButton() {
        refreshNoteButton.setCaptionAsHtml(true);
        refreshNoteButton.setCaption(messageHelper.getMessage("document.request.refresh.msg"));
        refreshNoteButton.setIcon(LeosTheme.LEOS_INFO_YELLOW_16);
        refreshNoteButton.addClickListener(new Button.ClickListener() {
            private static final long serialVersionUID = 3714441703159576377L;

            @Override
            public void buttonClick(Button.ClickEvent event) {
                eventBus.post(new RefreshDocumentEvent());
                eventBus.post(new DocumentUpdatedEvent(false)); //Document might be updated.
            }
        });
    }

    // create text refresh button
    private void refreshButton() {
        refreshButton.addClickListener(new Button.ClickListener() {
            private static final long serialVersionUID = 3714441703159576377L;

            @Override
            public void buttonClick(Button.ClickEvent event) {
                eventBus.post(new RefreshDocumentEvent());
                eventBus.post(new DocumentUpdatedEvent(false)); //Document might be updated.
            }
        });
    }

    @Subscribe
    public void handleDisableEditToc(DisableEditTocEvent event) {
        leftSlider.collapse();
        financialStatementLayout.setEnabled(true);
        eventBus.post(new InlineTocCloseRequestEvent());
        mainLayout.removeComponent(leftSlider);
        annotateExtension.setoperationMode(AnnotateExtension.OperationMode.NORMAL);
    }

    @Subscribe
    public void expandTocSliderPanel(ExpandTocSliderPanel event) {
        leftSlider = buildTocLeftSliderPanel();
        mainLayout.addComponent(leftSlider, 0);
        financialStatementLayout.setEnabled(false);
        leftSlider.expand();
        annotateExtension.setoperationMode(AnnotateExtension.OperationMode.READ_ONLY);
    }

    private SliderPanel buildTocLeftSliderPanel() {
        VerticalLayout tocItemsContainer = buildTocDragItems();
        tocItemsContainer.setWidth(105, Unit.PIXELS);
        tocItemsContainer.setSpacing(false);
        tocItemsContainer.setMargin(false);
        SliderPanel leftSlider = new SliderPanelBuilder(tocItemsContainer)
                .expanded(false)
                .mode(SliderMode.LEFT)
                .caption(messageHelper.getMessage("toc.slider.panel.tab.title"))
                .tabPosition(SliderTabPosition.BEGINNING)
                .zIndex(9980)
                .tabSize(0)
                .build();

        return leftSlider;
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

    private VerticalLayout buildTocDragItems() {
        GridLayout gridLayout = new GridLayout();
        gridLayout.setWidth(100, Unit.PERCENTAGE);
        gridLayout.setHeight(100, Unit.PERCENTAGE);
        gridLayout.setDefaultComponentAlignment(Alignment.MIDDLE_CENTER);
        gridLayout.setSpacing(false);
        gridLayout.setMargin(true);
        gridLayout.setStyleName("leos-left-slider-gridlayout");
        List<TocItem> tocItemsList = structureContextProvider.get().getTocItems();
        for (TocItem type : tocItemsList) {
            if (!type.isRoot() && type.isDraggable()) {
                Label itemLabel = new Label(TableOfContentHelper.getDisplayableTocItem(type, messageHelper));
                itemLabel.setStyleName("leos-drag-item");

                DragSourceExtension<Label> dragSourceExtension = new DragSourceExtension<>(itemLabel);
                dragSourceExtension.addDragStartListener((DragStartListener<Label>) event -> {
                    String number = null, heading = null, content = "";
                    if (OptionsType.MANDATORY.equals(type.getItemNumber()) || OptionsType.OPTIONAL.equals(type.getItemNumber())) {
                        number = messageHelper.getMessage("toc.item.type.number");
                    }
                    if (OptionsType.MANDATORY.equals(type.getItemHeading())) {
                        heading = messageHelper.getMessage("toc.item.type." + type.getAknTag().value().toLowerCase() + ".heading");
                    }
                    if (type.isContentDisplayed()) { // TODO: Use a message property to compose the default content text here and in the XMLHelper templates for
                        // each element
                        content = (type.getAknTag().value().equalsIgnoreCase(XmlHelper.RECITAL) || type.getAknTag().value().equalsIgnoreCase(XmlHelper.CITATION))
                                ? org.springframework.util.StringUtils.capitalize(type.getAknTag().value() + "...") : "Text...";
                    }
                    TableOfContentItemVO dragData = new TableOfContentItemVO(type, Cuid.createCuid(), null, number, null, heading, null, content);
                    Set<TableOfContentItemVO> draggedItems = new HashSet<>();
                    draggedItems.add(dragData);
                    dragSourceExtension.setDragData(draggedItems);
                });

                dragSourceExtension.setEffectAllowed(EffectAllowed.COPY_MOVE);
                gridLayout.addComponent(itemLabel);
            }
        }
        VerticalLayout tocItemContainer = new VerticalLayout();
        tocItemContainer.setCaption(messageHelper.getMessage("toc.edit.window.items"));
        tocItemContainer.addStyleName("leos-left-slider-panel");

        HorizontalLayout toolbar = new HorizontalLayout();
        toolbar.setWidth(100, Unit.PERCENTAGE);
        toolbar.setSpacing(true);
        toolbar.setMargin(false);
        toolbar.setStyleName("leos-viewdoc-tocbar");
        toolbar.addStyleName("leos-slider-toolbar");

        Label sliderLabel = new Label(messageHelper.getMessage("toc.slider.panel.toolbar.title"), ContentMode.HTML);
        toolbar.addComponent(sliderLabel);

        tocItemContainer.addComponent(toolbar);
        tocItemContainer.addComponent(gridLayout);
        tocItemContainer.setExpandRatio(gridLayout, 1.0f);
        return tocItemContainer;
    }
}
