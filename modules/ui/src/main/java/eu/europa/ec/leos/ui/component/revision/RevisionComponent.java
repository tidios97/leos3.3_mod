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
package eu.europa.ec.leos.ui.component.revision;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.vaadin.addon.onoffswitch.OnOffSwitch;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.shared.ui.ContentMode;
import com.vaadin.spring.annotation.SpringComponent;
import com.vaadin.spring.annotation.ViewScope;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import eu.europa.ec.leos.domain.cmis.LeosCategory;
import eu.europa.ec.leos.domain.cmis.document.XmlDocument;
import eu.europa.ec.leos.i18n.MessageHelper;
import eu.europa.ec.leos.model.action.ContributionVO;
import eu.europa.ec.leos.security.SecurityContext;
import eu.europa.ec.leos.ui.component.LeosDisplayField;
import eu.europa.ec.leos.ui.component.navigation.NavigationHelper;
import eu.europa.ec.leos.ui.event.EnableSyncScrollRequestEvent;
import eu.europa.ec.leos.ui.event.contribution.ContributionSelectionEvent;
import eu.europa.ec.leos.ui.event.contribution.FetchContributionsListEvent;
import eu.europa.ec.leos.ui.event.revision.RevisionDocumentProcessedEvent;
import eu.europa.ec.leos.ui.extension.MathJaxExtension;
import eu.europa.ec.leos.ui.extension.MergeContributionExtension;
import eu.europa.ec.leos.ui.extension.ScrollPaneExtension;
import eu.europa.ec.leos.ui.extension.SliderPinsExtension;
import eu.europa.ec.leos.ui.extension.SoftActionsExtension;
import eu.europa.ec.leos.web.event.component.NavigationRequestEvent;
import eu.europa.ec.leos.web.event.view.document.ComparisonEvent;
import eu.europa.ec.leos.web.ui.component.ContentPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vaadin.dialogs.ConfirmDialog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ViewScope
@SpringComponent
public class RevisionComponent<T extends XmlDocument> extends CustomComponent implements ContentPane {
    private static final long serialVersionUID = -826802129383432798L;
    private static final Logger LOG = LoggerFactory.getLogger(RevisionComponent.class);
    private static final String LEOS_RELATIVE_FULL_WDT = "100%";

    private final EventBus eventBus;
    private final MessageHelper messageHelper;
    private final SecurityContext securityContext;
    private LeosDisplayField revisionContent;
    private ContributionVO contributionVO;
    private Button syncScrollSwitch;
    private Button revisionNextButton;
    private Button revisionPrevButton;
    private Label separatorLabel;
    private OnOffSwitch revisionStatus;
    private ComboBox<String> actions;
    private Button applyButton;
    private Button searchButton = new Button();
    private int contributionSelection = 0;
    private String acceptSelectedChanges;
    private String acceptAllOption;

    public RevisionComponent(EventBus eventBus, MessageHelper messageHelper, SecurityContext securityContext) {
        this.eventBus = eventBus;
        this.messageHelper = messageHelper;
        this.securityContext = securityContext;

        setSizeFull();
        VerticalLayout revisionLayout = new VerticalLayout();
        revisionLayout.setSizeFull();
        revisionLayout.setSpacing(false);
        revisionLayout.setMargin(false);
        
        // create toolbar
        revisionLayout.addComponent(buildRevisionToolbar());
        // create content
        final Component textContent = buildRevisionContent();

        revisionLayout.addComponent(textContent);
        revisionLayout.setExpandRatio(textContent, 1.0f);
        setCompositionRoot(revisionLayout);

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

    private Component buildRevisionToolbar() {
        LOG.debug("Building revision Text toolbar...");

        // create text toolbar layout
        final HorizontalLayout toolsLayout = new HorizontalLayout();
        toolsLayout.setId("revisionToolbar");
        toolsLayout.setStyleName("leos-revision-bar");

        // set toolbar style
        toolsLayout.setWidth(LEOS_RELATIVE_FULL_WDT);

        //create sync scroll
        syncScrollSwitch = syncScrollSwitch();
        syncScrollSwitch.setDescription(messageHelper.getMessage("leos.button.tooltip.enable.sync"), ContentMode.HTML);
        toolsLayout.addComponent(syncScrollSwitch);
        toolsLayout.setComponentAlignment(syncScrollSwitch, Alignment.MIDDLE_LEFT);
        
        revisionPrevButton = revisionPrevNavigationButton();
        revisionPrevButton.setDescription(messageHelper.getMessage("version.changes.navigation.prev"));
        toolsLayout.addComponent(revisionPrevButton);
        toolsLayout.setComponentAlignment(revisionPrevButton, Alignment.MIDDLE_LEFT);

        revisionNextButton = revisionNextNavigationButton();
        revisionNextButton.setDescription(messageHelper.getMessage("version.changes.navigation.next"));
        toolsLayout.addComponent(revisionNextButton);
        toolsLayout.setComponentAlignment(revisionNextButton, Alignment.MIDDLE_LEFT);

        separatorLabel = new Label();
        separatorLabel.setWidth(87,Unit.PERCENTAGE);
        separatorLabel.setContentMode(ContentMode.HTML);
        toolsLayout.addComponent(separatorLabel);
        toolsLayout.setComponentAlignment(separatorLabel, Alignment.MIDDLE_CENTER);
        toolsLayout.setExpandRatio(separatorLabel,0.5f);

        List<String> mergeActions = new ArrayList<>();
        acceptSelectedChanges = messageHelper.getMessage("contribution.accept.selected.changes.label");
        acceptAllOption = messageHelper.getMessage("contribution.accept.all.action.label");
        mergeActions.add(acceptSelectedChanges);
        mergeActions.add(acceptAllOption);
        actions = new ComboBox<>();
        actions.setEmptySelectionAllowed(false);
        actions.setTextInputAllowed(false);
        actions.setItems(mergeActions);
        actions.setValue(mergeActions.get(0));
        actions.addStyleName("merge-action-list");
        actions.setWidth(210,Unit.PIXELS);
        toolsLayout.addComponent(actions);
        toolsLayout.setComponentAlignment(actions, Alignment.MIDDLE_CENTER);
        actions.addSelectionListener(event -> {
            if (actions.getSelectedItem().isPresent()
                    && ((acceptAllOption.equals(actions.getSelectedItem().get())
                    || (acceptSelectedChanges.equals(actions.getSelectedItem().get()) && contributionSelection > 0)))) {
                applyButton.setEnabled(true);
            } else {
                applyButton.setEnabled(false);
            }
        });
        applyButton = new Button(messageHelper.getMessage("contribution.apply.action.button.caption"));
        applyButton.addStyleName("primary leos-toolbar-button");
        applyButton.setEnabled(false);
        applyButton.addClickListener(event -> {
            if (actions.getSelectedItem().isPresent() && acceptAllOption.equals(actions.getSelectedItem().get())) {
                ConfirmDialog confirmDialog = ConfirmDialog.getFactory().create(
                        messageHelper.getMessage("contribution.bulk.accept.dialog.caption"),
                        messageHelper.getMessage("contribution.bulk.accept.dialog.message"),
                        messageHelper.getMessage("contribution.bulk.accept.dialog.ok.title"),
                        messageHelper.getMessage("contribution.bulk.accept.dialog.cancel.title"),
                        null
                );
                confirmDialog.setContentMode(ConfirmDialog.ContentMode.HTML);
                confirmDialog.show(getUI(), dialog -> {
                    if (dialog.isConfirmed()) {
                        contributionSelection = 0;
                        eventBus.post(new FetchContributionsListEvent(contributionVO, true));
                    }
                }, true);
            } else if (actions.getSelectedItem().isPresent() && acceptSelectedChanges.equals(actions.getSelectedItem().get())) {
                contributionSelection = 0;
                eventBus.post(new FetchContributionsListEvent(contributionVO, false));
            }
            applyButton.setEnabled(false);
        });
        toolsLayout.addComponent(applyButton);
        toolsLayout.setComponentAlignment(applyButton, Alignment.MIDDLE_RIGHT);

        Label switchLabel = new Label();
        switchLabel.setValue(messageHelper.getMessage("contribution.done.switch.label"));
        revisionStatus = new OnOffSwitch();
        revisionStatus.setCaptionAsHtml(true);
        revisionStatus.addValueChangeListener(event -> {
            if(event.getValue() == true && event.getOldValue() == false && revisionStatus.isEnabled()) {
                ConfirmDialog confirmDialog = ConfirmDialog.getFactory().create(
                        messageHelper.getMessage("contribution.process.revision.dialog.caption"),
                        messageHelper.getMessage("contribution.process.revision.dialog.message"),
                        messageHelper.getMessage("contribution.process.revision.dialog.ok.title"),
                        messageHelper.getMessage("contribution.process.revision.dialog.cancel.title"),
                        null
                );
                confirmDialog.setContentMode(ConfirmDialog.ContentMode.HTML);
                confirmDialog.show(getUI(), dialog -> {
                    if (dialog.isConfirmed()) {
                        toggleRevisionContent(revisionStatus.getValue());
                        eventBus.post(new RevisionDocumentProcessedEvent(contributionVO.getDocumentId()));
                    }
                }, true);
            } else {
                toggleRevisionContent(revisionStatus.getValue());
            }
        });
        toolsLayout.addComponent(switchLabel);
        toolsLayout.setComponentAlignment(switchLabel, Alignment.MIDDLE_RIGHT);
        toolsLayout.addComponent(revisionStatus);
        toolsLayout.setComponentAlignment(revisionStatus, Alignment.MIDDLE_RIGHT);

        searchButton.setVisible(false);
        searchButton.setIcon(VaadinIcons.SEARCH);
        searchButton.addClickListener(event -> {});
        searchButton.addStyleName("leos-toolbar-button revision-search");
        toolsLayout.addComponent(searchButton);
        toolsLayout.setComponentAlignment(searchButton, Alignment.MIDDLE_RIGHT);

        Button closeButton = closeRevisionComponent();
        toolsLayout.addComponent(closeButton);
        toolsLayout.setComponentAlignment(closeButton, Alignment.MIDDLE_RIGHT);

        return toolsLayout;
    }

    private Button closeRevisionComponent() {
        Button closeButton = new Button();
        closeButton.setDescription(messageHelper.getMessage("version.compare.close.button.description"));
        closeButton.addStyleName("link leos-toolbar-button");
        closeButton.setIcon(VaadinIcons.CLOSE_CIRCLE);
        closeButton.addClickListener(event -> {
            eventBus.post(new ComparisonEvent(false));
        });
        return closeButton;
    }

    private Button revisionPrevNavigationButton() {
        VaadinIcons revisionPrevIcon = VaadinIcons.CARET_UP;
        final Button revisionPrevButton = new Button();
        revisionPrevButton.setIcon(revisionPrevIcon);
        revisionPrevButton.addStyleName("link leos-toolbar-button navigation-btn");
        revisionPrevButton.addClickListener(event -> eventBus.post(new NavigationRequestEvent(NavigationRequestEvent.NAV_DIRECTION.PREV)));
        return revisionPrevButton;
    }
    
    private Button revisionNextNavigationButton() {
        VaadinIcons revisionNextIcon = VaadinIcons.CARET_DOWN;
        final Button revisionNextButton = new Button();
        revisionNextButton.setIcon(revisionNextIcon);
        revisionNextButton.addStyleName("link leos-toolbar-button navigation-btn");
        revisionNextButton.addClickListener(event -> eventBus.post(new NavigationRequestEvent(NavigationRequestEvent.NAV_DIRECTION.NEXT)));
        return revisionNextButton;
    }
    
    private Button syncScrollSwitch() {
        VaadinIcons syncIcon = VaadinIcons.EXCHANGE;
        final Button syncScrollSwitch = new Button();
        syncScrollSwitch.setIcon(syncIcon);
        syncScrollSwitch.setStyleName("link");
        syncScrollSwitch.addStyleName("leos-toolbar-button enable-sync");
        syncScrollSwitch.setData(true);
        syncScrollSwitch.setDescription(messageHelper.getMessage("leos.button.tooltip.disable.sync"));

        syncScrollSwitch.addClickListener(event -> {
            Button button = event.getButton();
            boolean syncState = !(boolean) button.getData();
            updateStyle(button, syncState);
            eventBus.post(new EnableSyncScrollRequestEvent(syncState));
            button.setDescription(syncState
                    ? messageHelper.getMessage("leos.button.tooltip.disable.sync")
                    : messageHelper.getMessage("leos.button.tooltip.enable.sync"));
            
            button.setData(syncState);
        });
        return syncScrollSwitch;
    }
    
    private void updateStyle(Button button, boolean syncState) {
        if(syncState) {
            button.removeStyleName("disable-sync");
            button.addStyleName("enable-sync");
        } else {
            button.removeStyleName("enable-sync");
            button.addStyleName("disable-sync");
        }
    }

    private Component buildRevisionContent() {
        LOG.debug("Building Revision Text content...");

        // create placeholder to display revision content
        revisionContent = new LeosDisplayField();
        revisionContent.setSizeFull();
        revisionContent.setId("leos-revision-content");
        revisionContent.setStyleName("leos-revision-content");

        // create revision content extensions
        new MathJaxExtension<>(revisionContent);
        new SoftActionsExtension<>(revisionContent);
        ScrollPaneExtension scrollPaneExtension = new ScrollPaneExtension(revisionContent, eventBus);
        scrollPaneExtension.getState().idPrefix = "revision-";
        scrollPaneExtension.getState().containerSelector = ".leos-revision-content";
        SliderPinsExtension<LeosDisplayField> sliderPins = new SliderPinsExtension<>(revisionContent, getSelectorStyleMap());
        NavigationHelper navHelper = new NavigationHelper(sliderPins);
        new MergeContributionExtension<>(revisionContent, eventBus);
        this.eventBus.register(navHelper);//Registering helper object to eventBus. Presently this method is called only once if multiple invocation occurs in future will need to unregister the object on close of document.
        return revisionContent;
    }

    protected Map<String, String> getSelectorStyleMap() {
        Map<String, String> selectorStyleMap = new HashMap<>();
        selectorStyleMap.put(".leos-revision-content-removed", "pin-leos-revision-content-removed-hidden");
        selectorStyleMap.put(".leos-revision-content-added", "pin-leos-revision-content-added-hidden");
        selectorStyleMap.put(".leos-content-removed", "pin-leos-content-removed-hidden");
        selectorStyleMap.put(".leos-content-new", "pin-leos-content-new-hidden");
        return selectorStyleMap;
    }

    public void populateRevisionContent(String revisionContentText, LeosCategory leosCategory, ContributionVO contributionVO) {
        this.contributionVO = contributionVO;
        applyButton.setEnabled(false);
        revisionContent.addStyleName(leosCategory.name().toLowerCase());
        revisionContent.setValue(revisionContentText.replaceAll("(?i) id=\"", " id=\"revision-").
                replaceAll("(?i) leos:softmove_to=\"", " leos:softmove_to=\"revision-").
                replaceAll("(?i) leos:softmove_from=\"", " leos:softmove_from=\"revision-"));

        if (ContributionVO.ContributionStatus.CONTRIBUTION_DONE.getValue().equalsIgnoreCase(contributionVO.getContributionStatus().getValue())) {
            //Revision done or declined
            disableMergePane(true);
        } else {
            eventBus.post(new EnableSyncScrollRequestEvent(true));
            disableMergePane(false);
        }
    }

    public void disableMergePane() {
        disableMergePane(true);
    }

    private void disableMergePane(boolean enable) {
        revisionContent.setEnabled(!enable);
        revisionStatus.setEnabled(!enable);
        syncScrollSwitch.setEnabled(!enable);
        revisionNextButton.setEnabled(!enable);
        revisionPrevButton.setEnabled(!enable);
        actions.setEnabled(!enable);
        revisionStatus.setValue(enable);
    }

    private void toggleRevisionContent(boolean enable) {
        revisionContent.setEnabled(!enable);
        syncScrollSwitch.setEnabled(!enable);
        revisionNextButton.setEnabled(!enable);
        revisionPrevButton.setEnabled(!enable);
        actions.setEnabled(!enable);
        applyButton.setEnabled(!enable);
    }

    public void scrollToRevisionChange(String elementId) {
        LOG.trace("Navigating to (elementId={})...", elementId);
        com.vaadin.ui.JavaScript.getCurrent().execute("LEOS.scrollToElement('" + elementId + "' , '\" revision \"');");
    }
    
    @Override
    public float getDefaultPaneWidth(int numberOfFeatures, boolean tocPresent) {
        final float featureWidth;
        if (numberOfFeatures == 1) {
            featureWidth = 100f;
        } else {
            if (tocPresent) {
                featureWidth = 42.5f;
            } else {
                featureWidth = 50f;
            }
        }
        return featureWidth;
    }

    @Subscribe
    public void handleSelectedContribution(ContributionSelectionEvent event) {
        Boolean selected = event.isSelected();
        contributionSelection = getSelectedCount(selected);
        if (contributionSelection > 0) {
            applyButton.setEnabled(true);
        } else {
            applyButton.setEnabled(false);
        }
    }

    private int getSelectedCount(boolean selected) {
        if (selected) {
            contributionSelection++;
        } else {
            contributionSelection--;
        }
        return contributionSelection;
    }

    public LeosDisplayField getRevisionContent() {
        return this.revisionContent;
    }
}