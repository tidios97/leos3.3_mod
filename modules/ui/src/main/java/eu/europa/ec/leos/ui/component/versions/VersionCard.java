package eu.europa.ec.leos.ui.component.versions;

import com.google.common.base.Strings;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.vaadin.annotations.DesignRoot;
import com.vaadin.data.HasValue;
import com.vaadin.data.provider.CallbackDataProvider;
import com.vaadin.data.provider.DataProvider;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.shared.ui.grid.HeightMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Grid;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.declarative.Design;
import eu.europa.ec.leos.domain.cmis.common.VersionType;
import eu.europa.ec.leos.domain.cmis.document.XmlDocument;
import eu.europa.ec.leos.i18n.MessageHelper;
import eu.europa.ec.leos.model.action.ActionType;
import eu.europa.ec.leos.model.action.VersionVO;
import eu.europa.ec.leos.model.filter.QueryFilter;
import eu.europa.ec.leos.model.user.User;
import eu.europa.ec.leos.security.SecurityContext;
import eu.europa.ec.leos.services.support.VersionsUtil;
import eu.europa.ec.leos.ui.event.ChangeBaseVersionEvent;
import eu.europa.ec.leos.ui.event.DownloadClickedVersionRequestEvent;
import eu.europa.ec.leos.ui.event.FetchMilestoneByVersionedReferenceEvent;
import eu.europa.ec.leos.ui.event.toc.CloseAndRefreshTocEvent;
import eu.europa.ec.leos.ui.view.TriFunction;
import eu.europa.ec.leos.web.event.component.CleanComparedContentEvent;
import eu.europa.ec.leos.web.event.component.RestoreVersionRequestEvent;
import eu.europa.ec.leos.web.event.component.ShowVersionRequestEvent;
import eu.europa.ec.leos.web.event.view.document.DocumentUpdatedEvent;
import eu.europa.ec.leos.web.support.user.UserHelper;
import eu.europa.ec.leos.web.ui.component.actions.VersionsActionsMenuBar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vaadin.dialogs.ConfirmDialog;

import java.util.List;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

@DesignRoot("VersionCardDesign.html")
public class VersionCard<D extends XmlDocument> extends VerticalLayout {
    private static final long serialVersionUID = -1;
    private static final Logger LOG = LoggerFactory.getLogger(VersionCard.class);

    private HorizontalLayout versionCardHeader;
    private Label versionLabel;
    private Label lastUpdate;
    
    private HorizontalLayout titleDescriptionIconBlock;
    private Label title;
    private Label description;
    private VerticalLayout versionCardAction;
    private VerticalLayout versionCardActionBlock;
    
    private Grid<VersionVO> grid = new Grid<>();
    private VerticalLayout versionsBlock;
    private HorizontalLayout showVersionsBlock;
    private Button showVersionsButton;
    private Label showVersionsLabel;
    private boolean showVersions;
    private boolean mostRecentVersionCard;

    private HorizontalLayout versionsHolderWrapper;
    private VerticalLayout versionsHolder;

    private final MessageHelper messageHelper;
    private final EventBus eventBus;
    private final UserHelper userHelper;
    private final SecurityContext securityContext;
    private final VersionVO versionVO;
    private final Function<VersionCard<D>, Boolean> cardMatcher;
    private final Function<VersionRow, Boolean> rowMatcher;
    private final boolean comparisonMode;
    private final boolean comparisonAvailable;
    private final boolean isBaseVersionMenuAvailable;
    private final Set<VersionCheckBox> allCheckBoxes;
    private final Set<VersionVO> selectedCheckBoxes;
    private final Set<VersionRow> allVersionRows;
    private static final int singleRowHeight = 26;
    private static final int defaultGridHeight = 105;
    private int nrRowsRecentChanges;

    private final TriFunction<String, Integer, Integer, List<D>> minorVersionsFn;
    private final Function<String, Integer> countMinorVersionsFn;
    private final BiFunction<Integer, Integer, List<D>> recentChangesFn;
    private final Supplier<Integer> countRecentChangesFn;
    private final VersionComparator versionComparator;
    private int currentOffset;
    private int currentLimit;

    private boolean canRestorePreviousVersion;
    private boolean canDownload;

    public VersionCard(VersionVO versionVO,
                       TriFunction<String, Integer, Integer, List<D>> minorVersionsFn,
                       Function<String, Integer> countMinorVersionsFn,
                       BiFunction<Integer, Integer, List<D>> recentChangesFn,
                       Supplier<Integer> countRecentChangesFn,
                       MessageHelper messageHelper, EventBus eventBus, UserHelper userHelper, SecurityContext securityContext,
                       boolean comparisonMode, boolean comparisonAvailable, Set<VersionCheckBox> allCheckboxes,
                       Set<VersionVO> selectedCheckboxes, Set<VersionRow> allVersionRows,
                       VersionComparator versionComparator, Function<VersionCard<D>, Boolean> cardMatcher,
                       Function<VersionRow, Boolean> rowMatcher, boolean isBaseVersionMenuAvailable,
                       boolean canRestorePreviousVersion, boolean canDownload) {
        this.versionVO = versionVO;
        this.messageHelper = messageHelper;
        this.eventBus = eventBus;
        this.userHelper = userHelper;
        this.securityContext = securityContext;
        this.minorVersionsFn = minorVersionsFn;
        this.countMinorVersionsFn = countMinorVersionsFn;
        this.recentChangesFn = recentChangesFn;
        this.countRecentChangesFn = countRecentChangesFn;
        this.comparisonMode = comparisonMode;
        this.comparisonAvailable = comparisonAvailable;
        this.isBaseVersionMenuAvailable = isBaseVersionMenuAvailable;
        this.allCheckBoxes = allCheckboxes;
        this.selectedCheckBoxes = selectedCheckboxes;
        this.allVersionRows = allVersionRows;
        this.versionComparator = versionComparator;
        this.cardMatcher = cardMatcher;
        this.rowMatcher = rowMatcher;

        this.mostRecentVersionCard = versionVO == null;

        this.canRestorePreviousVersion = canRestorePreviousVersion;
        this.canDownload = canDownload;

        Design.read(this);
        init();
    }

    @Override
    public void attach() {
        super.attach();
        eventBus.register(this);
    }

    @Override
    public void detach() {
        eventBus.unregister(this);
        super.detach();
    }

    public VersionVO getVersionVO() {
        return versionVO;
    }

    private void init() {
        initGrid();
        initView();
        initEventListeners();
        initActions();
    }

    private void initActions() {
        if(comparisonAvailable && !mostRecentVersionCard) {
            VersionsActionsMenuBar actionsMenuBar = createActionsMenuBar(versionVO);
            versionCardAction.addComponent(actionsMenuBar);
        }
    }
    
    private void downloadVersion(VersionVO version) {
        eventBus.post(new DownloadClickedVersionRequestEvent(version.getDocumentId()));
    }
    
    private void viewVersion(VersionVO version) {
        eventBus.post(new ShowVersionRequestEvent(version.getDocumentId()));
    }

    private void restoreVersion(VersionVO version) {
        ConfirmDialog confirmDialog = ConfirmDialog.getFactory().create(
                messageHelper.getMessage("document.restore.version.window.title"),
                messageHelper.getMessage("document.restore.version.comment.caption", version.getVersionNumber()),
                messageHelper.getMessage("document.restore.version.button.revert"),
                messageHelper.getMessage("document.restore.version.button.cancel"),
                null
        );
        confirmDialog.setContentMode(ConfirmDialog.ContentMode.HTML);

        confirmDialog.show(getUI(), dialog -> {
            if (dialog.isConfirmed()) {
                eventBus.post(new RestoreVersionRequestEvent(version.getDocumentId()));
                eventBus.post(new CloseAndRefreshTocEvent());
            }
        }, true);
    }
    
    private void viewMilestone(VersionVO version) {
        eventBus.post(new FetchMilestoneByVersionedReferenceEvent(version.getVersionedReference()));
    }

    private VersionsActionsMenuBar createActionsMenuBar(VersionVO versionVO) {
        VersionsActionsMenuBar actionsMenuBar = new VersionsActionsMenuBar(eventBus, messageHelper);
        if (!versionVO.isMostRecentVersion()) { //Skip the first recent row
            actionsMenuBar.createMenuItem(messageHelper.getMessage("document.version.actions.view"),
                    selectedItem -> viewVersion(versionVO));

            if(this.canRestorePreviousVersion) {
                actionsMenuBar.createMenuItem(messageHelper.getMessage("document.version.actions.revert"),
                        selectedItem -> restoreVersion(versionVO));
            }
    
            if(this.canDownload){
                actionsMenuBar.createMenuItem(messageHelper.getMessage("menu.actions.download.version"),
                        selectedItem -> downloadVersion(versionVO));
            }

            if(isBaseVersionMenuAvailable) {
                actionsMenuBar.createMenuItem(messageHelper.getMessage("document.version.actions.base"),
                        selectedItem -> changeBaseVersion(versionVO));
            }
        }
        if (versionVO.getVersionType() == VersionType.MAJOR) {
            actionsMenuBar.createMenuItem(messageHelper.getMessage("document.version.actions.exploreMilestone"),
                 selectedItem -> viewMilestone(versionVO)
            );
        }

        return actionsMenuBar;
    }

    private void changeBaseVersion(VersionVO versionVO) {
        eventBus.post(new ChangeBaseVersionEvent(versionVO.getDocumentId(), versionVO.getVersionNumber().toString(),
                versionVO.getCheckinCommentVO().getTitle()));
    }

    private void initEventListeners() {
        showVersionsBlock.addLayoutClickListener(event -> {
            if (mostRecentVersionCard) {
                handleShowRecentChanges();
            } else {
                handleShowMinorVersions();
            }
        });
        showVersionsButton.addClickListener(event -> {
            if (mostRecentVersionCard) {
                handleShowRecentChanges();
            } else {
                handleShowMinorVersions();
            }
        });
    }
    
    private void handleShowMinorVersions() {
        showVersions = !showVersions;
        versionsHolderWrapper.setVisible(showVersions);
        showVersionsButton.setIcon(showVersions ? VaadinIcons.CHEVRON_DOWN : VaadinIcons.CHEVRON_RIGHT);
        showVersionsLabel.setValue(showVersions ? messageHelper.getMessage("document.version.minorVersions.hide") :
                messageHelper.getMessage("document.version.minorVersions.show"));
    }

    private void handleShowRecentChanges() {
        showVersions = !showVersions;
        int height = nrRowsRecentChanges * 23 + 10;
        if (showVersions) {
            versionsHolder.removeStyleName("version-recent-remove-scrollbar");
            versionsHolder.setHeight(Math.min(height, defaultGridHeight), Unit.PIXELS);
        } else {
            versionsHolder.setHeight(singleRowHeight, Unit.PIXELS);
            grid.scrollToStart();
            versionsHolder.addStyleName("version-recent-remove-scrollbar");
        }
        showVersionsButton.setIcon(showVersions ? VaadinIcons.CHEVRON_UP : VaadinIcons.CHEVRON_RIGHT);
        showVersionsLabel.setValue(showVersions ? messageHelper.getMessage("document.version.recentChanges.hide")
                : messageHelper.getMessage("document.version.recentChanges.show"));

    }
    
    public void initView() {
        showVersionsButton.setIcon(VaadinIcons.CHEVRON_RIGHT);
        showVersionsLabel.setValue(messageHelper.getMessage("document.version.minorVersions.show"));
        final String style;
        final String versionString;
        final String lastUpdateString;
        if (mostRecentVersionCard) {
            style = "recent";
            showVersionsButton.setIcon(VaadinIcons.CHEVRON_RIGHT);
            showVersionsLabel.setValue(messageHelper.getMessage("document.version.recentChanges.show"));
            versionString = messageHelper.getMessage("document.version.label.recent");
            lastUpdateString = "";// will be set by dataProvider callback
            titleDescriptionIconBlock.setVisible(false);
            versionsHolderWrapper.setVisible(true); //show the grid by default in case of Recent
            versionsHolder.setHeight(singleRowHeight, Unit.PIXELS); //show only 1 row
            // move showVersionsBlock after versionsHolderWrapper component
            versionsBlock.removeComponent(showVersionsBlock);
            versionsBlock.addComponent(showVersionsBlock, 1);
            versionsHolder.addStyleName("version-recent-remove-scrollbar");
        } else {
            versionString = messageHelper.getMessage("document.version.label") + " " + versionVO.getVersionNumber();
            lastUpdateString = versionVO.getUpdatedDate() + "   " + userHelper.convertToPresentation(versionVO.getUsername());
            switch (versionVO.getVersionType()) {
                case INTERMEDIATE:
                    style = "intermediate";
                    break;
                case MAJOR:
                    style = "milestone";
                    final Label milestoneTag = new Label(messageHelper.getMessage("document.version.milestone"));
                    milestoneTag.setStyleName("milestone-tag-block");
                    versionCardHeader.addComponent(milestoneTag);
                    versionCardHeader.setComponentAlignment(milestoneTag, Alignment.BOTTOM_RIGHT);
                    break;
                default:
                    style = "recent";
            }

            title.setValue(versionVO.getCheckinCommentVO().getTitle());
            if (Strings.isNullOrEmpty(versionVO.getCheckinCommentVO().getDescription())) {
                description.setVisible(false);
            } else {
                description.setValue(versionVO.getCheckinCommentVO().getDescription());
            }
            versionsHolderWrapper.setVisible(false); // by default the grid is not show
            
            if (comparisonMode) {
                VersionCheckBox checkbox = new VersionCheckBox(versionVO);
                checkbox.addValueChangeListener(this::handleVersionSelection);
                setCheckBoxValue(checkbox);
                addCheckBox(checkbox);
                
                versionCardActionBlock.addComponent(checkbox);
                versionCardActionBlock.removeComponent(versionCardAction);
            }
        }
        
        addStyleName(style);
        applyMatcher();
        versionLabel.setStyleName(style);
        versionLabel.setValue(versionString);
        lastUpdate.setValue(lastUpdateString);
    }

    private void setCheckBoxValue(VersionCheckBox checkbox) {
        final boolean checked = selectedCheckBoxes.contains(checkbox.getVersionVO());
        final boolean maxChecksAllowed = selectedCheckBoxes.size() < versionComparator.getNumberVersionsForComparing();
        checkbox.setValue(checked);
        checkbox.setEnabled(maxChecksAllowed || checked);
    }
    
    private void addCheckBox(VersionCheckBox newCheckbox) {
        allCheckBoxes.removeIf(s -> s.getVersionVO().equals(newCheckbox.getVersionVO()));
        allCheckBoxes.add(newCheckbox);
    }
    
    private void enableAllCheckBoxes() {
        allCheckBoxes.forEach(checkBox -> checkBox.setEnabled(true));
    }
    
    private void disableAllCheckBoxes() {
        if (selectedCheckBoxes.size() >= versionComparator.getNumberVersionsForComparing()) {
            allCheckBoxes
                    .stream()
                    .filter(checkBox -> !selectedCheckBoxes.contains(checkBox.getVersionVO()))
                    .forEach(checkBox -> checkBox.setEnabled(false));
        }
    }
    
    private void updateRecentChangesCard(List<VersionVO> recentVersionsVO) {
        final String lastUpdateString;
        if (recentVersionsVO.size() == 0) {
            lastUpdateString = messageHelper.getMessage("document.version.recentChanges.notPresent");
            versionsBlock.setVisible(false); //hide the grid if no recent changes present
        } else {
            final String username = recentVersionsVO.get(0).getUpdatedDate();
            lastUpdateString = messageHelper.getMessage("document.version.recentChanges.lastChange") + " " + username;
            if (recentVersionsVO.size() == 1) {
                showVersionsBlock.setVisible(false); //hide the "Show more" option if only one change present
            }
        }
        lastUpdate.setValue(lastUpdateString);
    }

    private void initGrid() {
        CallbackDataProvider<VersionVO, QueryFilter> dataProvider;
        if (mostRecentVersionCard) {
            dataProvider = getRecentVersionsDataProvider();
        } else {
            dataProvider = getMinorVersionsDataProvider(versionVO.getCmisVersionNumber());
        }

        grid = new Grid<>();
        grid.setSelectionMode(Grid.SelectionMode.NONE);
        grid.addComponentColumn(this::createGridRow)
                .setMinimumWidthFromContent(false);
        grid.setDefaultHeaderRow(null);
        grid.setDataProvider(dataProvider);
        grid.setWidth("100%");
        grid.setBodyRowHeight(singleRowHeight);// px
        grid.setHeightMode(HeightMode.UNDEFINED);
        while (grid.getHeaderRowCount() > 0) {
            grid.removeHeaderRow(0);
        }

        versionsHolder.addComponentsAndExpand(grid);
        versionsHolder.setHeight(defaultGridHeight, Unit.PIXELS); // important for pagination on dataProvider to work properly
    }

    private void handleVersionSelection(HasValue.ValueChangeEvent<Boolean> event) {
        VersionVO version = ((VersionCheckBox) event.getSource()).getVersionVO();
        if (!event.getValue()) { //unchecked
            LOG.debug("un-selected: " + version.getVersionNumber());
            selectedCheckBoxes.remove(version);
            enableAllCheckBoxes();
            handleCompareAction();
        } else {
            LOG.debug("selected   : " + version.getVersionNumber());
            if (!selectedCheckBoxes.contains(version)) {
                selectedCheckBoxes.add(version);
                disableAllCheckBoxes();
                handleCompareAction();
            }
        }
    }
    
    private void handleCompareAction() {
        int numberOfSelectedCheckBoxes = selectedCheckBoxes.size();
        if (numberOfSelectedCheckBoxes <= versionComparator.getNumberVersionsForComparing()) {
            if (numberOfSelectedCheckBoxes <= 1) {
                eventBus.post(new CleanComparedContentEvent());
            } else if (numberOfSelectedCheckBoxes == 2) {
                versionComparator.compare(selectedCheckBoxes);
            } else if (numberOfSelectedCheckBoxes == 3) {
                versionComparator.doubleCompare(selectedCheckBoxes);
            }
        }
    }
    
    private HorizontalLayout createGridRow(VersionVO versionVO) {
        VersionRow gridRow = new VersionRow(versionVO, rowMatcher, userHelper);
        gridRow.setHeightUndefined();
        gridRow.setHeight(singleRowHeight, Unit.PIXELS);
        gridRow.setSizeFull();
        gridRow.applyMatcher();
        addVersionRow(gridRow);

        final String labelStr;
        if (versionVO.getCheckinCommentVO().getCheckinElement() != null
                && versionVO.getCheckinCommentVO().getCheckinElement().getActionType() == ActionType.STRUCTURAL) {
            labelStr = messageHelper.getMessage("operation.toc.updated");
        } else {
            labelStr = VersionsUtil.buildLabel(versionVO.getCheckinCommentVO(), messageHelper);
        }

        Label label = new Label();
        label.setValue(versionVO.getVersionNumber() + " " + labelStr);
        label.setDescription(getVersionDescription(versionVO));
        gridRow.addComponent(label);
        
        if(comparisonAvailable) {
            if (!comparisonMode) {
                VersionsActionsMenuBar minorVersionMenuBar = createActionsMenuBar(versionVO);
                gridRow.addComponent(minorVersionMenuBar);
                gridRow.setComponentAlignment(minorVersionMenuBar, Alignment.MIDDLE_RIGHT);
            } else {
                VersionCheckBox checkboxMinor = new VersionCheckBox(versionVO);
                checkboxMinor.addValueChangeListener(this::handleVersionSelection);
                setCheckBoxValue(checkboxMinor);
                addCheckBox(checkboxMinor);
                gridRow.addComponent(checkboxMinor);
                gridRow.setComponentAlignment(checkboxMinor, Alignment.MIDDLE_RIGHT);
                gridRow.removeComponent(versionCardAction);
            }
        }
        return gridRow;
    }

    private void setFirstToMostRecentVersion(List<VersionVO> newVersionsVO) {
        newVersionsVO
                .stream()
                .findFirst()
                .ifPresent(newVersionVO -> {
                    newVersionVO.setMostRecentVersion(true);
                    selectedCheckBoxes
                            .stream()
                            .filter(VersionVO::isMostRecentVersion)
                            .findFirst()
                            .ifPresent(oldVersionVO -> {
                                if (!newVersionVO.equals(oldVersionVO)) {
                                    selectedCheckBoxes.remove(oldVersionVO);
                                    selectedCheckBoxes.add(newVersionVO);
                                    handleCompareAction();
                                }
                            });
                });
    }

    private String getVersionDescription(VersionVO versionVO) {
        StringBuilder versionDescription = new StringBuilder(userHelper.convertToPresentation(versionVO.getUsername()));
        User user = userHelper.getUser(versionVO.getUsername());
        if (user.getDefaultEntity() != null) {
            versionDescription.append(" (");
            versionDescription.append(user.getDefaultEntity().getOrganizationName());
            versionDescription.append(") - ");
        }
        versionDescription.append(versionVO.getUpdatedDate());
        return versionDescription.toString();
    }

    private CallbackDataProvider<VersionVO, QueryFilter> getRecentVersionsDataProvider() {
        return DataProvider.fromFilteringCallbacks(
                query -> {
                    currentOffset = query.getOffset();
                    currentLimit = query.getLimit();
                    List<VersionVO> recentVersionsVO = getRecentVersions(currentOffset, currentLimit);
                    nrRowsRecentChanges = recentVersionsVO.size();
                    if (query.getOffset() == 0) {
                        setFirstToMostRecentVersion(recentVersionsVO);
                    }
                    updateRecentChangesCard(recentVersionsVO);
                    return recentVersionsVO.stream();
                },
                query -> countRecentChangesFn.get()
        );
    }

    private List<VersionVO> getRecentVersions(int offset, int limit) {
        LOG.debug("dataFn requested from offset:{},limit:{}", offset, limit);
        List<D> recentVersions = recentChangesFn.apply(offset, limit);
        return VersionsUtil.buildVersionVO(recentVersions, messageHelper);
    }

    @Subscribe
    public void updateComparisonComponent(DocumentUpdatedEvent event) {
        if (comparisonMode && mostRecentVersionCard) {
            List<VersionVO> recentVersionsVO = getRecentVersions(0, currentLimit);
            setFirstToMostRecentVersion(recentVersionsVO);
        }
    }

    private CallbackDataProvider<VersionVO, QueryFilter> getMinorVersionsDataProvider(String currentVersion) {
        return DataProvider.fromFilteringCallbacks(
                query -> {
                    LOG.debug("dataFn requested from offset:{},limit:{}", query.getOffset(), query.getLimit());
                    List<D> minorVersions = minorVersionsFn.apply(currentVersion, query.getOffset(), query.getLimit());

                    List<VersionVO> intermediateAndMajorVersionsVO = VersionsUtil.buildVersionVO(minorVersions, messageHelper);
                    updateMinorGridByNumberOfRows(intermediateAndMajorVersionsVO.size());
                    return intermediateAndMajorVersionsVO.stream();
                },
                query -> countMinorVersionsFn.apply(currentVersion));
    }
    
    private void updateMinorGridByNumberOfRows(int size) {
        if (size == 0) {
            Label label = new Label(messageHelper.getMessage("operation.check.modification"));
            versionsHolderWrapper.removeComponent(versionsHolderWrapper.getComponent(1));
            label.addStyleName("version-label-no-minors-present");
            versionsHolderWrapper.addComponent(label, 1);

        } else if (size > 4) {
            versionsHolder.removeStyleName("versions-holder-row1 versions-holder-row2 versions-holder-row3 versions-holder-row4");
        } else {
            grid.setHeightByRows(size);
            versionsHolder.setHeight(size * 23 + 10, Unit.PIXELS);
            if (size == 1) {
                versionsHolder.addStyleName("versions-holder-row1");
            } else if (size == 2) {
                versionsHolder.addStyleName("versions-holder-row2");
            } else if (size == 3) {
                versionsHolder.addStyleName("versions-holder-row3");
            } else if (size == 4) {
                versionsHolder.addStyleName("versions-holder-row4");
            }
        }
    }

    public void applyMatcher() {
        boolean visible = cardMatcher.apply(this);
        if (visible != isVisible()) {
            setVisible(visible);
        }
    }

    private void addVersionRow(VersionRow versionRow) {
        allVersionRows.removeIf(s -> s.getVersionVO().equals(versionRow.getVersionVO()));
        allVersionRows.add(versionRow);
    }

    public static class VersionCheckBox extends CheckBox {
        private static final long serialVersionUID = 3923920868263439263L;
        private final VersionVO versionVO;

        public VersionCheckBox (VersionVO versionVO) {
            this.versionVO = versionVO;
        }

        public VersionVO getVersionVO() {
            return versionVO;
        }
    }

    public static class VersionRow extends HorizontalLayout {
        private static final long serialVersionUID = -953019786438622408L;
        private final VersionVO versionVO;
        private final String userName;
        private final Function<VersionRow, Boolean> rowMatcher;

        public VersionRow (VersionVO versionVO, Function<VersionRow, Boolean> rowMatcher, UserHelper userHelper) {
            this.versionVO = versionVO;
            this.userName = userHelper.convertToPresentation(versionVO.getUsername()).toLowerCase();
            this.rowMatcher = rowMatcher;
            addStyleName("version-row");
        }

        public VersionVO getVersionVO() {
            return versionVO;
        }

        public String getUserName() {
            return userName;
        }

        public void applyMatcher() {
            setStyleName("unmatched-card", !rowMatcher.apply(this));
        }
    }

}
