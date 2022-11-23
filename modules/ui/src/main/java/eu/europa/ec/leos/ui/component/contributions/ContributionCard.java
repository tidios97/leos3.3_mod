package eu.europa.ec.leos.ui.component.contributions;

import com.google.common.base.Strings;
import com.google.common.eventbus.EventBus;
import com.vaadin.annotations.DesignRoot;
import com.vaadin.data.provider.ListDataProvider;
import com.vaadin.shared.ui.ContentMode;
import com.vaadin.shared.ui.grid.HeightMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Grid;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.MenuBar.Command;
import com.vaadin.ui.MenuBar.MenuItem;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.declarative.Design;
import eu.europa.ec.leos.domain.cmis.document.XmlDocument;
import eu.europa.ec.leos.i18n.MessageHelper;
import eu.europa.ec.leos.model.action.ActionType;
import eu.europa.ec.leos.model.action.ContributionVO;
import eu.europa.ec.leos.model.user.User;
import eu.europa.ec.leos.services.support.VersionsUtil;
import eu.europa.ec.leos.ui.event.revision.DeclineRevisionDocumentEvent;
import eu.europa.ec.leos.ui.event.revision.OpenRevisionDocumentEvent;
import eu.europa.ec.leos.web.event.view.document.DocumentUpdatedEvent;
import eu.europa.ec.leos.web.event.view.document.RefreshDocumentEvent;
import eu.europa.ec.leos.web.support.user.UserHelper;
import eu.europa.ec.leos.web.ui.component.actions.VersionsActionsMenuBar;
import eu.europa.ec.leos.web.ui.themes.LeosTheme;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vaadin.dialogs.ConfirmDialog;

import java.util.Set;
import java.util.function.Function;

@DesignRoot("ContributionCardDesign.html")
public class ContributionCard<D extends XmlDocument> extends VerticalLayout {
    private static final long serialVersionUID = -1;
    private static final Logger LOG = LoggerFactory.getLogger(ContributionCard.class);

    private HorizontalLayout contributionCardHeader;
    private Label contributionLabel;
    private Label contributionReference;
    private Label lastUpdate;
    private Label contributionStatus;

    private HorizontalLayout titleDescriptionIconBlock;
    private Label title;
    private Label description;
    private VerticalLayout contributionCardAction;
    private VerticalLayout contributionCardActionBlock;

    private Grid<ContributionVO> grid = new Grid<>();

    String declineStlye = "decline-contribution";

    private final MessageHelper messageHelper;
    private final EventBus eventBus;
    private final UserHelper userHelper;
    private final ContributionVO contributionVO;
    private final Function<ContributionRow, Boolean> rowMatcher;
    private final Set<ContributionRow> allContributionRows;
    private final ListDataProvider dataProvider;
    private static final int singleRowHeight = 26;
    private static final int defaultGridHeight = 105;

    public ContributionCard(ContributionVO contributionVO,
                            MessageHelper messageHelper, EventBus eventBus, UserHelper userHelper,
                            ListDataProvider dataProvider,
                            Set<ContributionRow> allContributionRows,
                            Function<ContributionRow, Boolean> rowMatcher) {
        this.contributionVO = contributionVO;
        this.messageHelper = messageHelper;
        this.eventBus = eventBus;
        this.userHelper = userHelper;
        this.allContributionRows = allContributionRows;
        this.rowMatcher = rowMatcher;
        this.dataProvider = dataProvider;

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

    private void init() {
        initGrid();
        initView();
        initActions();
    }

    private void initActions() {
        VersionsActionsMenuBar actionsMenuBar = createActionsMenuBar(contributionVO);
        contributionCardAction.addComponent(actionsMenuBar);
    }

    private VersionsActionsMenuBar createActionsMenuBar(ContributionVO contributionVO) {
        VersionsActionsMenuBar actionsMenuBar = new VersionsActionsMenuBar(eventBus, messageHelper);
        if(contributionVO.getContributionStatus().equals(ContributionVO.ContributionStatus.CONTRIBUTION_DONE)) {
            actionsMenuBar.createMenuItem(messageHelper.getMessage("document.contribution.actions.processed"),
                    selectedItem -> {
                        openRevision(contributionVO);
                    });
        }

        if(!contributionVO.getContributionStatus().equals(ContributionVO.ContributionStatus.CONTRIBUTION_DONE)) {
            actionsMenuBar.createMenuItem(messageHelper.getMessage("document.contribution.actions.merge"),
                    selectedItem -> {
                        openRevision(contributionVO);
                    });
             actionsMenuBar.createMenuItem(messageHelper.getMessage("document.contribution.actions.decline"), new DeclineContributionCommand());
        }
        return actionsMenuBar;
    }

    public class DeclineContributionCommand implements Command {

        @Override
        public void menuSelected(MenuItem selectedItem) {
            LOG.debug("Decline contribution menu item clicked...");
            confirmDeclineRevision(contributionVO, selectedItem);
        }
    }

    private void openRevision(ContributionVO contributionVO) {
        eventBus.post(new OpenRevisionDocumentEvent(contributionVO));
    }

    private void confirmDeclineRevision(ContributionVO contributionVO, MenuItem selectedItem) {
        // ask confirmation before delete
        ConfirmDialog confirmDialog = ConfirmDialog.getFactory().create(
                messageHelper.getMessage("card.contribution.decline.revision.confirmation.title"),
                messageHelper.getMessage("card.contribution.decline.revision.confirmation.message"),
                messageHelper.getMessage("card.contribution.decline.confirmation.confirm"),
                messageHelper.getMessage("card.contribution.decline.confirmation.cancel"), null);
        confirmDialog.setContentMode(ConfirmDialog.ContentMode.HTML);

        confirmDialog.show(getUI(),
                new ConfirmDialog.Listener() {
                    private static final long serialVersionUID = 144198814274639L;
                    public void onClose(ConfirmDialog dialog) {
                        if (dialog.isConfirmed()) {
                            declineRevision(contributionVO, selectedItem);
                        }
                    }
                }, true);
    }

    private void declineRevision(ContributionVO contributionVO, MenuItem selectedItem) {
        setContributionDone(contributionVO);
        eventBus.post(new DeclineRevisionDocumentEvent(contributionVO, selectedItem));
        eventBus.post(new DocumentUpdatedEvent());
        contributionCardAction.removeAllComponents();
        initActions();
    }

    private void setContributionDone(ContributionVO contributionVO) {
        contributionVO.setContributionStatus(ContributionVO.ContributionStatus.CONTRIBUTION_DONE.getValue());
        addStyleName(declineStlye);
        contributionStatus.setIcon(LeosTheme.LEOS_CONTRIBUTION_DONE_16);
        contributionStatus.setDescription(contributionVO.getContributionStatus().getValue());
    }

    public void initView() {
        final String style;
        final String contributionString;
        final String lastUpdateString;

        contributionString = messageHelper.getMessage("document.version.label") + " " + contributionVO.getVersionNumber();
        lastUpdateString = contributionVO.getUpdatedDate() + " - " + contributionVO.getUsername();
        style = "milestone";

        title.setValue(contributionVO.getCheckinCommentVO().getTitle());
        if (Strings.isNullOrEmpty(contributionVO.getCheckinCommentVO().getDescription())) {
            description.setVisible(false);
        } else {
            description.setValue(contributionVO.getCheckinCommentVO().getDescription());
        }

        addStyleName(style);
        if(contributionVO.getContributionStatus().equals(ContributionVO.ContributionStatus.CONTRIBUTION_DONE)) {
            addStyleName(declineStlye);
        }
        contributionLabel.setStyleName(style);
        contributionLabel.setValue(contributionString);
        contributionReference.setStyleName(style);
        String contributionRef = contributionVO.getContributionCreator();
        contributionReference.setValue(contributionRef == null || contributionRef.isEmpty() ? messageHelper.getMessage("document.contribution.edit.reference") : contributionRef);
        lastUpdate.setValue(lastUpdateString);
        contributionStatus.setContentMode(ContentMode.HTML);
        if (contributionVO.getContributionStatus().equals(ContributionVO.ContributionStatus.RECEIVED)) {
            contributionStatus.setIcon(LeosTheme.LEOS_CONTRIBUTION_RECEIVED_16);
        } else {
            contributionStatus.setIcon(LeosTheme.LEOS_CONTRIBUTION_DONE_16);
        }
        contributionStatus.setDescription(contributionVO.getContributionStatus().getValue());
    }

    private void initGrid() {
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
    }

    private HorizontalLayout createGridRow(ContributionVO contributionVO) {
        ContributionRow gridRow = new ContributionRow(contributionVO, rowMatcher, userHelper);
        gridRow.setHeightUndefined();
        gridRow.setHeight(singleRowHeight, Unit.PIXELS);
        gridRow.setSizeFull();
        gridRow.applyMatcher();
        addContributionRow(gridRow);

        final String labelStr;
        if (contributionVO.getCheckinCommentVO().getCheckinElement() != null
                && contributionVO.getCheckinCommentVO().getCheckinElement().getActionType() == ActionType.STRUCTURAL) {
            labelStr = messageHelper.getMessage("operation.toc.updated");
        } else {
            labelStr = VersionsUtil.buildLabel(contributionVO.getCheckinCommentVO(), messageHelper);
        }
        Label label = new Label();
        label.setValue(contributionVO.getVersionNumber() + " " + labelStr);
        label.setDescription(getContributionDescription(contributionVO));
        gridRow.addComponent(label);
        VersionsActionsMenuBar contributionMenuBar = createActionsMenuBar(contributionVO);
        gridRow.addComponent(contributionMenuBar);
        gridRow.setComponentAlignment(contributionMenuBar, Alignment.MIDDLE_RIGHT);
        return gridRow;
    }

    private String getContributionDescription(ContributionVO contributionVO) {
        StringBuilder contributionDescription = new StringBuilder(userHelper.convertToPresentation(contributionVO.getUsername()));
        User user = userHelper.getUser(contributionVO.getUsername());
        if (user.getDefaultEntity() != null) {
            contributionDescription.append(" (");
            contributionDescription.append(user.getDefaultEntity().getOrganizationName());
            contributionDescription.append(") - ");
        }
        contributionDescription.append(contributionVO.getUpdatedDate());
        return contributionDescription.toString();
    }

    private void addContributionRow(ContributionRow contributionRow) {
        allContributionRows.removeIf(s -> s.getContributionVO().equals(contributionRow.getContributionVO()));
        allContributionRows.add(contributionRow);
    }

    public static class ContributionRow extends HorizontalLayout {
        private static final long serialVersionUID = -953019786438622408L;
        private final ContributionVO contributionVO;
        private final String userName;
        private final Function<ContributionRow, Boolean> rowMatcher;

        public ContributionRow(ContributionVO contributionVO, Function<ContributionRow, Boolean> rowMatcher, UserHelper userHelper) {
            this.contributionVO = contributionVO;
            this.userName = userHelper.convertToPresentation(contributionVO.getUsername()).toLowerCase();
            this.rowMatcher = rowMatcher;
            addStyleName("version-row");
        }

        public ContributionVO getContributionVO() {
            return contributionVO;
        }

        public String getUserName() {
            return userName;
        }

        public void applyMatcher() {
            setStyleName("unmatched-card", !rowMatcher.apply(this));
        }
    }

}
