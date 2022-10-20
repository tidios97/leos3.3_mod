/*
 * Copyright 2017 European Commission
 *
 * Licensed under the EUPL, Version 1.1 or – as soon they will be approved by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 *     https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and limitations under the Licence.
 */
package eu.europa.ec.leos.ui.component.toc;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.vaadin.annotations.DesignRoot;
import com.vaadin.data.Binder;
import com.vaadin.data.Converter;
import com.vaadin.data.HasValue;
import com.vaadin.data.Result;
import com.vaadin.data.TreeData;
import com.vaadin.data.ValueContext;
import com.vaadin.data.ValueProvider;
import com.vaadin.data.provider.TreeDataProvider;
import com.vaadin.data.validator.RegexpValidator;
import com.vaadin.event.selection.MultiSelectionEvent;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.server.Page;
import com.vaadin.shared.Registration;
import com.vaadin.shared.ui.ContentMode;
import com.vaadin.shared.ui.dnd.DropEffect;
import com.vaadin.shared.ui.dnd.EffectAllowed;
import com.vaadin.shared.ui.grid.DropMode;
import com.vaadin.spring.annotation.SpringComponent;
import com.vaadin.ui.AbstractComponent;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.DescriptionGenerator;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.MenuBar.MenuItem;
import com.vaadin.ui.RadioButtonGroup;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.components.grid.GridDragStartListener;
import com.vaadin.ui.components.grid.TreeGridDragSource;
import com.vaadin.ui.declarative.Design;
import com.vaadin.ui.renderers.HtmlRenderer;
import com.vaadin.ui.themes.ValoTheme;
import eu.europa.ec.leos.domain.common.TocMode;
import eu.europa.ec.leos.i18n.MessageHelper;
import eu.europa.ec.leos.model.action.ActionType;
import eu.europa.ec.leos.model.action.CheckinElement;
import eu.europa.ec.leos.model.action.SoftActionType;
import eu.europa.ec.leos.model.annex.AnnexStructureType;
import eu.europa.ec.leos.model.user.User;
import eu.europa.ec.leos.security.SecurityContext;
import eu.europa.ec.leos.services.processor.content.TableOfContentHelper;
import eu.europa.ec.leos.services.support.XmlHelper;
import eu.europa.ec.leos.services.processor.content.TableOfContentProcessor;
import eu.europa.ec.leos.services.toc.StructureContext;
import eu.europa.ec.leos.ui.event.StateChangeEvent;
import eu.europa.ec.leos.ui.event.toc.CloseTocAndDocumentEvent;
import eu.europa.ec.leos.ui.event.toc.CloseAndRefreshTocEvent;
import eu.europa.ec.leos.ui.event.toc.DisableEditTocEvent;
import eu.europa.ec.leos.ui.event.toc.ExpandTocSliderPanel;
import eu.europa.ec.leos.ui.event.toc.InlineTocCloseRequestEvent;
import eu.europa.ec.leos.ui.event.toc.InlineTocEditRequestEvent;
import eu.europa.ec.leos.ui.event.toc.RefreshTocEvent;
import eu.europa.ec.leos.ui.event.toc.SaveTocRequestEvent;
import eu.europa.ec.leos.ui.event.toc.TocChangedEvent;
import eu.europa.ec.leos.ui.event.toc.TocResizedEvent;
import eu.europa.ec.leos.ui.extension.dndscroll.TreeGridScrollDropTargetExtension;
import eu.europa.ec.leos.vo.coedition.CoEditionVO;
import eu.europa.ec.leos.vo.coedition.InfoType;
import eu.europa.ec.leos.vo.toc.NumberingConfig;
import eu.europa.ec.leos.vo.toc.NumberingType;
import eu.europa.ec.leos.vo.toc.OptionsType;
import eu.europa.ec.leos.vo.toc.TableOfContentItemVO;
import eu.europa.ec.leos.vo.toc.TocItem;
import eu.europa.ec.leos.vo.toc.StructureConfigUtils;
import eu.europa.ec.leos.web.event.view.document.CheckDeleteLastEditingTypeEvent;
import eu.europa.ec.leos.web.event.view.document.CloseDocumentEvent;
import eu.europa.ec.leos.web.event.view.document.RefreshDocumentEvent;
import eu.europa.ec.leos.web.support.cfg.ConfigurationHelper;
import eu.europa.ec.leos.web.ui.component.ContentPane;
import eu.europa.ec.leos.web.ui.themes.LeosTheme;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.vaadin.dialogs.ConfirmDialog;
import org.vaadin.teemusa.gridextensions.client.tableselection.TableSelectionState.TableSelectionMode;
import org.vaadin.teemusa.gridextensions.tableselection.TableSelectionModel;

import javax.inject.Provider;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static eu.europa.ec.leos.model.action.SoftActionType.DELETE;
import static eu.europa.ec.leos.model.action.SoftActionType.MOVE_TO;
import static eu.europa.ec.leos.services.processor.content.TableOfContentHelper.DEFAULT_CAPTION_MAX_SIZE;
import static eu.europa.ec.leos.services.processor.content.TableOfContentHelper.hasTocItemSoftAction;
import static eu.europa.ec.leos.services.processor.content.TableOfContentProcessor.updateUserInfo;
import static eu.europa.ec.leos.services.support.XmlHelper.ARTICLE;
import static eu.europa.ec.leos.services.support.XmlHelper.BLOCK;
import static eu.europa.ec.leos.services.support.XmlHelper.CROSSHEADING;
import static eu.europa.ec.leos.services.support.XmlHelper.DIVISION;
import static eu.europa.ec.leos.services.support.XmlHelper.EC;

import static eu.europa.ec.leos.services.support.XmlHelper.INDENT;
import static eu.europa.ec.leos.services.support.XmlHelper.POINT;
import static eu.europa.ec.leos.services.processor.content.TableOfContentProcessor.getTagValueFromTocItemVo;
import static eu.europa.ec.leos.services.processor.content.TableOfContentProcessor.updateDepthOfTocItems;
import static eu.europa.ec.leos.services.processor.content.TableOfContentProcessor.updateStyleClassOfTocItems;

@SpringComponent
@Scope("prototype")
@DesignRoot("TableOfContentDesign.html")
public class TableOfContentComponent extends VerticalLayout implements ContentPane {

    private static final long serialVersionUID = -4752609567267410718L;
    private static final Logger LOG = LoggerFactory.getLogger(TableOfContentComponent.class);
    public static SimpleDateFormat dataFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm");
    private static final float TOC_MIN_WIDTH = 190F;
    private static final int MAX_CHECKIN_COMMENTS = 15;

    private ConfigurationHelper cfgHelper;
    private MessageHelper messageHelper;
    private EventBus eventBus;
    protected Button tocExpandCollapseButton;
    protected TreeAction treeAction;
    protected Button inlineTocEditButton;
    protected Label spacerLabel;
    protected Label tocUserCoEdition;
    protected MultiSelectTreeGrid<TableOfContentItemVO> tocTree;
    protected Label statusLabel;
    protected MenuBar menuBar;
    protected MenuItem tocExpandCollapseMenuItem;
    protected MenuItem inlineTocEditMenuItem;
    protected MenuItem separatorMenuItem;
    protected MenuItem saveMenuItem;
    protected MenuItem saveCloseMenuItem;
    protected MenuItem cancelMenuItem;
    protected boolean expandedNavigationPane = true;

    private Registration dropTargetRegistration;
    private TreeGridScrollDropTargetExtension<TableOfContentItemVO> dropTarget;
    private TreeGridDragSource<TableOfContentItemVO> dragSource;
    private User user;
    private boolean updateEnabled = true;
    private boolean editionEnabled = false;
    private boolean editorPanelOpened = false;
    private boolean userOriginated = false;
    private final String STATUS_STYLE = "leos-toc-tree-status";
    private final String STATUS_DEFAULT_VALUE = "&nbsp;";
    private Boolean dataChanged = false;
    private VerticalLayout itemEditorLayout = new VerticalLayout(); // dummy implementation to avoid design exception
    private Button saveButton;
    private Button saveCloseButton;
    private Button cancelButton;
    private boolean isToggledByUser;
    private TocEditor tocEditor;
    private TextField itemTypeField = new TextField();
    private TreeDataProvider<TableOfContentItemVO> dataProvider;
    private Registration dataProviderRegistration;

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private Set<CheckinElement> tocChangedElements = new HashSet<>();
    private Set<String> expandedNodes;

    private Provider<StructureContext> structureContextProvider;
    private List<TocItem> tocItems;
    private List<NumberingConfig> numberingConfigs;
    private TableOfContentProcessor tableOfContentProcessor;
    private List<String> indentListRadioButtonGroupItemsToEnable;

    public TableOfContentComponent() {
    }

    public enum TreeAction {
        EXPAND,
        COLLAPSE
    }

    private boolean coEditionSipEnabled;
    private String coEditionSipDomain;

    @Autowired
    public TableOfContentComponent(final MessageHelper messageHelper, final EventBus eventBus, final SecurityContext securityContext,
                                   final ConfigurationHelper cfgHelper, final TocEditor tocEditor, Provider<StructureContext> structureContextProvider, TableOfContentProcessor tableOfContentProcessor) {
        // If the list indentListRadioButtonGroupItemsToEnable is empty, all items will be enabled in method buildIndentListRadioButtonGroup
        this(messageHelper, eventBus, securityContext, cfgHelper, tocEditor, structureContextProvider, tableOfContentProcessor, null);
    }

    @Autowired
    public TableOfContentComponent(final MessageHelper messageHelper, final EventBus eventBus, final SecurityContext securityContext,
                                   final ConfigurationHelper cfgHelper, final TocEditor tocEditor, Provider<StructureContext> structureContextProvider,
                                   TableOfContentProcessor tableOfContentProcessor, final List<String> indentListRadioButtonGroupItemsToEnable) {
        LOG.trace("Initializing table of content...");
        Validate.notNull(messageHelper, "MessageHelper must not be null!");
        this.messageHelper = messageHelper;
        Validate.notNull(eventBus, "EventBus must not be null!");
        this.eventBus = eventBus;
        Validate.notNull(securityContext, "SecurityContext must not be null!");
        this.user = securityContext.getUser();
        Validate.notNull(user, "User must not be null!");
        Validate.notNull(cfgHelper, "cfgHelper must not be null!");
        this.coEditionSipEnabled = Boolean.parseBoolean(cfgHelper.getProperty("leos.coedition.sip.enabled"));
        this.coEditionSipDomain = cfgHelper.getProperty("leos.coedition.sip.domain");
        this.tocEditor = tocEditor;
        this.cfgHelper = cfgHelper;
        this.structureContextProvider = structureContextProvider;
        this.tableOfContentProcessor = tableOfContentProcessor;

        this.indentListRadioButtonGroupItemsToEnable = indentListRadioButtonGroupItemsToEnable;
        Design.read(this);
        buildToc();
        this.checkDeleteLastEditingTypeConsumer = new CheckDeleteLastEditingTypeConsumer(tocTree, messageHelper, eventBus);
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

    private void buildToc() {
        LOG.debug("Building table of contents...");
        setId("tableOfContentComponent");
        buildTocToolbar();
        buildTocTree();
        buildItemEditorLayout();
    }

    private void buildTocToolbar() {
        LOG.debug("Building table of contents toolbar...");
        // create toc expand/collapse button
        tocExpandCollapseButton();
        //inline toc edit button
        inlineTocEditButton();
        // spacer label will use all available space
        spacerLabel.setContentMode(ContentMode.HTML);
        spacerLabel.setValue("&nbsp;");
        tocSaveButton();
        tocSaveCloseButton();
        tocCancelButton();
        tocMenuBar();
    }

    private void buildItemEditorLayout() {
        itemEditorLayout.addStyleName("leos-bottom-slider-panel");
        itemEditorLayout.setSpacing(true);
        itemEditorLayout.setMargin(false);

        Button closeButton = buildCloseButton();
        HorizontalLayout toolbar = new HorizontalLayout();
        toolbar.setWidth(100, Unit.PERCENTAGE);
        toolbar.setSpacing(true);
        toolbar.setMargin(false);
        toolbar.setStyleName("leos-viewdoc-tocbar");
        toolbar.addStyleName("leos-slider-toolbar");
        Label sliderLabel = new Label(messageHelper.getMessage("toc.edit.window.item.selected"), ContentMode.HTML);

        toolbar.addComponent(sliderLabel);
        toolbar.addComponent(closeButton);
        toolbar.setComponentAlignment(closeButton, Alignment.TOP_RIGHT);
        itemEditorLayout.addComponent(toolbar);

        final Editor tocItemEditor   = new Editor(tocEditor);
        tocItemEditor.addStyleName("leos-toc-editor");
        tocItemEditor.setSpacing(true);
        tocItemEditor.setMargin(false);
        tocTree.asMultiSelect().addValueChangeListener(tocItemEditor);
        itemEditorLayout.addComponent(tocItemEditor);
    }

    private Button buildCloseButton() {
        Button button = new Button();

        button.setDescription(messageHelper.getMessage("leos.button.close.item.toc.description"));
        button.setIcon(VaadinIcons.CLOSE_CIRCLE);
        button.addStyleName("link");
        button.addClickListener(new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
                closeItemTocEditor();
            }
        });
        return button;
    }

    // create toc expand/collapse button
    private void tocExpandCollapseButton() {
        treeAction = TreeAction.EXPAND;
        tocExpandCollapseButton.setDescription(messageHelper.getMessage("toc.expand.button.description"));
        tocExpandCollapseButton.setIcon(LeosTheme.LEOS_TOC_TREE_ICON_16);
        tocExpandCollapseButton.addClickListener(event -> toggleTreeItems());
    }

    private void toggleTreeItems() {
        if (TreeAction.EXPAND.equals(treeAction)) {
            LOG.debug("Expanding the tree...");
            expandFullTree(tocTree.getTreeData().getRootItems()); //expand items recursively
            treeAction = TreeAction.COLLAPSE;
            tocExpandCollapseButton.setDescription(messageHelper.getMessage("toc.collapse.button.description"));
            tocExpandCollapseMenuItem.setText(tocExpandCollapseButton.getDescription());
        } else if (TreeAction.COLLAPSE.equals(treeAction)) {
            LOG.debug("Collapsing the tree...");
            tocTree.collapse(tocTree.getTreeData().getRootItems());
            treeAction = TreeAction.EXPAND;
            tocExpandCollapseButton.setDescription(messageHelper.getMessage("toc.expand.button.description"));
            tocExpandCollapseMenuItem.setText(tocExpandCollapseButton.getDescription());
        } else {
            LOG.debug("Ignoring unknown tree control button click action! [action={}]", treeAction);
        }
    }

    private void expandFullTree(List<TableOfContentItemVO> items) {
        items.forEach(item -> {
            if (!item.getChildItems().isEmpty()) {
                tocTree.expand(item);
                expandFullTree(item.getChildItems());
            }
        });
    }

    private void expandTree(List<TableOfContentItemVO> items) {
        items.forEach(item -> {
            if (!item.getChildItems().isEmpty()) {
                TableOfContentItemVO tocItem = editionEnabled ? tocEditor.getSimplifiedTocItem(item) : item;
                if (expandedNodes.contains(tocItem.getId())) {
                    tocTree.expand(item);
                } else {
                    tocTree.collapse(item);
                }
                expandTree(item.getChildItems());
            }
        });
    }

    private void expandDefaultTreeNodes(List<TableOfContentItemVO> items) {
        items.forEach(item -> {
            if (!item.getChildItems().isEmpty() && item.getTocItem().isExpandedByDefault()) {
                tocTree.expand(item);
                expandDefaultTreeNodes(item.getChildItems());
            }
        });
    }

    // Toc edit button
    private void inlineTocEditButton() {
        inlineTocEditButton.setDescription(messageHelper.getMessage("toc.inline.edit.button.description"));
        inlineTocEditButton.setIcon(LeosTheme.LEOS_TOC_EDIT_ICON_16);
        inlineTocEditButton.addClickListener(event -> {
            if (!editionEnabled) {
                if (isTocCoEditionActive()) {
                    ConfirmDialog confirmDialog = ConfirmDialog.getFactory().create(
                            messageHelper.getMessage("coedition.edit.element.confirmation.title"),
                            messageHelper.getMessage("coedition.edit.element.confirmation.message", tocUserCoEdition.getDescription().replace("leos-toc-user-coedition-lync", "")),
                            messageHelper.getMessage("coedition.edit.element.confirmation.confirm"),
                            messageHelper.getMessage("coedition.edit.element.confirmation.cancel"), null);
                    confirmDialog.setContentMode(ConfirmDialog.ContentMode.HTML);
                    confirmDialog.getContent().setHeightUndefined();
                    confirmDialog.setHeightUndefined();
                    confirmDialog.show(getUI(), dialog -> {
                        if (dialog.isConfirmed()) {
                            enableTocEdition();
                        }
                    }, true);
                } else {
                    enableTocEdition();
                }
            } else {
                disableTocEdition();
            }
        });
    }

    private void enableTocEdition() {
        editionEnabled = true;
        tocTree.addStyleName("leos-toc-tree-editable");
        inlineTocEditButton.setEnabled(false);
        inlineTocEditMenuItem.setEnabled(false);
        setVisibility();
        eventBus.post(new InlineTocEditRequestEvent());
    }

    private void disableTocEdition() {
        if (dataChanged) {
            ConfirmDialog.show(getUI(),
                    messageHelper.getMessage("edit.close.not.saved.title"),
                    messageHelper.getMessage("edit.close.not.saved.message"),
                    messageHelper.getMessage("edit.close.not.saved.confirm"),
                    messageHelper.getMessage("edit.close.not.saved.close"),
                    new ConfirmDialog.Listener() {
                        private static final long serialVersionUID = -1441968814274639475L;
                        public void onClose(ConfirmDialog dialog) {
                            if (dialog.isConfirmed()) {
                            	if(invalidToc(tocTree.getTreeData(), null))
                            		return;
                            	
                                onSaveToc(TocMode.SIMPLIFIED);
                                closeTocEditor();
                                // no need to refresh the toc since save will fire the full refresh document
                            } else if (dialog.isCanceled()) {
                                closeTocAndRefresh();
                            }
                            statusLabel.setValue(STATUS_DEFAULT_VALUE);
                            statusLabel.setStyleName(STATUS_STYLE);
                        }
                    });
        } else {
            closeTocEditor();
            eventBus.post(new RefreshDocumentEvent()); //to refresh the annotations
            statusLabel.setValue(STATUS_DEFAULT_VALUE);
            statusLabel.setStyleName(STATUS_STYLE);
        }
    }

    private void closeTocAndRefresh() {
        closeTocEditor();
        eventBus.post(new RefreshTocEvent(TocMode.SIMPLIFIED));
    }

    @Subscribe
    public void closeTocAndRefresh(CloseAndRefreshTocEvent event) {
        if(editionEnabled) {
            closeTocAndRefresh();
        }
    }

    @Subscribe
    void closeEditToc(CloseTocAndDocumentEvent event) {
        if (dataChanged) {
            ConfirmDialog.show(getUI(),
                    messageHelper.getMessage("edit.close.not.saved.title"),
                    messageHelper.getMessage("edit.close.not.saved.message"),
                    messageHelper.getMessage("edit.close.not.saved.confirm"),
                    messageHelper.getMessage("edit.close.not.saved.close"),
                    new ConfirmDialog.Listener() {
                        private static final long serialVersionUID = -1741968814231539431L;
                        public void onClose(ConfirmDialog dialog) {
                            if (dialog.isConfirmed() || dialog.isCanceled()) {
                                if (dialog.isConfirmed()) {
                                	if(invalidToc(tocTree.getTreeData(), null))
                                		return;
                                	
                                    eventBus.post(new SaveTocRequestEvent(TableOfContentItemConverter
                                            .buildTocItemVOList(tocTree.getTreeData()), tocChangedElements));
                                }
                                eventBus.post(new InlineTocCloseRequestEvent());
                                eventBus.post(new CloseDocumentEvent());
                                statusLabel.setValue(STATUS_DEFAULT_VALUE);
                                statusLabel.setStyleName(STATUS_STYLE);
                            }
                        }
                    });
        } else {
            eventBus.post(new InlineTocCloseRequestEvent());
            eventBus.post(new CloseDocumentEvent());
            statusLabel.setValue(STATUS_DEFAULT_VALUE);
            statusLabel.setStyleName(STATUS_STYLE);
        }
    }

    private void closeTocEditor() {
    	dataChanged = false;
        editionEnabled = false;
        editorPanelOpened = false;
        tocTree.removeStyleName("leos-toc-tree-editable");
        enableSave(false);
        inlineTocEditButton.setEnabled(true);
        inlineTocEditMenuItem.setEnabled(true);
        dropTargetRegistration.remove();
        dropTarget.setDropEffect(DropEffect.NONE);
        dataProviderRegistration.remove();
        tocEditor.setTocTreeDataFilter(false, dataProvider);
        setVisibility();
        eventBus.post(new DisableEditTocEvent());
        removeComponent(itemEditorLayout);
    }

    private void closeItemTocEditor() {
        editorPanelOpened = false;
        removeComponent(itemEditorLayout);
    }

    private void setVisibility() {
        menuBar.setVisible(!expandedNavigationPane);
        tocExpandCollapseButton.setVisible(expandedNavigationPane);
        inlineTocEditButton.setVisible(updateEnabled && expandedNavigationPane);
        saveButton.setVisible(editionEnabled && expandedNavigationPane);
        saveCloseButton.setVisible(editionEnabled && expandedNavigationPane);
        cancelButton.setVisible(editionEnabled && expandedNavigationPane);
        inlineTocEditMenuItem.setVisible(updateEnabled);
        separatorMenuItem.setVisible(editionEnabled);
        saveMenuItem.setVisible(editionEnabled);
        saveCloseMenuItem.setVisible(editionEnabled);
        cancelMenuItem.setVisible(editionEnabled);
    }

    private MenuItem addMenuItem(MenuItem parentMenuItem, Button button) {
        MenuItem menuItem = parentMenuItem.addItem(button.getDescription(), button.getIcon(), item -> button.click());
        menuItem.setStyleName("leos-actions-sub-menu-item");
        menuItem.setEnabled(button.isEnabled());
        return menuItem;
    }

    private void tocMenuBar() {
        menuBar.addStyleName("leos-actions-menu");
        menuBar.setEnabled(true);

        MenuItem mainMenuItem = menuBar.addItem("", LeosTheme.LEOS_HAMBURGUER_16, null);
        mainMenuItem.setStyleName("leos-actions-menu-selector");
        mainMenuItem.setDescription(messageHelper.getMessage("toc.title"));

        tocExpandCollapseMenuItem = addMenuItem(mainMenuItem, tocExpandCollapseButton);
        inlineTocEditMenuItem = addMenuItem(mainMenuItem, inlineTocEditButton);

        separatorMenuItem = mainMenuItem.addSeparator();

        saveMenuItem = addMenuItem(mainMenuItem, saveButton);
        saveCloseMenuItem = addMenuItem(mainMenuItem, saveCloseButton);
        cancelMenuItem = addMenuItem(mainMenuItem, cancelButton);

        setVisibility();
    }

    @Subscribe
    public void tocResized(TocResizedEvent event) {
        expandedNavigationPane = event.getPaneSize() > TOC_MIN_WIDTH;
        setVisibility();
    }

    private void tocSaveButton() {
        saveButton.setDescription(messageHelper.getMessage("leos.button.save"));
        saveButton.setIcon(LeosTheme.LEOS_TOC_SAVE_ICON_16);
        saveButton.setEnabled(false);
        saveButton.addClickListener(event -> {
        	if(invalidToc(tocTree.getTreeData(), null))
        		return;
        	
        	onSaveToc(TocMode.NOT_SIMPLIFIED);
        });
    }

    private void tocSaveCloseButton() {
        saveCloseButton.setDescription(messageHelper.getMessage("leos.button.save.and.close"));
        saveCloseButton.setIcon(LeosTheme.LEOS_TOC_SAVE_CLOSE_ICON_16);
        saveCloseButton.setEnabled(false);
        saveCloseButton.addClickListener(event -> {
        	if(invalidToc(tocTree.getTreeData(), null))
        		return;
        	
        	onSaveToc(TocMode.SIMPLIFIED);
            closeTocEditor();
        });
    }

    private void tocCancelButton() {
        cancelButton.setDescription(messageHelper.getMessage("leos.button.cancel"));
        cancelButton.setIcon(LeosTheme.LEOS_TOC_CANCEL_ICON_16);
        cancelButton.addClickListener(event -> disableTocEdition());
    }

    private void onSaveToc(TocMode mode) {
        enableSave(false);
        editionEnabled = TocMode.NOT_SIMPLIFIED.equals(mode);
        List<TableOfContentItemVO> list = TableOfContentItemConverter.buildTocItemVOList(tocTree.getTreeData());
        eventBus.post(new SaveTocRequestEvent(list, tocChangedElements));
        eventBus.post(new RefreshDocumentEvent(mode));
        dataChanged = false;
    }

    private boolean isElementHeadingOrContentEmpty(TableOfContentItemVO item, String element) {
        if (item.getTocItem().getAknTag().value().equalsIgnoreCase(element) &&
        		item.getTocItem().getItemHeading() == OptionsType.OPTIONAL) {
            return StringUtils.isEmpty(item.getHeading()) && StringUtils.isEmpty(item.getContent());
        } else
            return false;
    }

    private boolean isCrossHeading(TocItem tocItem) {
        return tocItem.getAknTag().value().equalsIgnoreCase(CROSSHEADING) ||
                tocItem.getAknTag().value().equalsIgnoreCase(BLOCK);
    }
    
    private boolean invalidToc(TreeData<TableOfContentItemVO> treeData, TableOfContentItemVO parent) {
    	if (parent != null) {
        	List<TableOfContentItemVO> items = treeData.getChildren(parent);
        	for (TableOfContentItemVO item : items) {
        		if(invalidTocItem(item)) {
        			statusLabel.setValue("Please fill mandatory fields before saving!");
                    statusLabel.setStyleName("leos-toc-tree-status error");
                    scheduler.schedule(new Runnable() {
                        public void run() {
                            statusLabel.setValue(STATUS_DEFAULT_VALUE);
                            statusLabel.setStyleName(STATUS_STYLE);
                        }
                    }, 3, TimeUnit.SECONDS);
                    return true;
        		}
        	}
        }
    	
    	List<TableOfContentItemVO> children = treeData.getChildren(parent);
    	for (TableOfContentItemVO child : children) {
    		if(invalidToc(treeData, child))
    			return true;
    	}
        
		return false;
    }
    
    private boolean isSoftDeletedOrMovedToItem(TableOfContentItemVO item) {
        return (hasTocItemSoftAction(item, DELETE) || hasTocItemSoftAction(item, MOVE_TO));
    }
    
    private String getTocTreeStyle(TableOfContentItemVO item) {
    	String tocTreeStyle = tocEditor.getTocTreeStyling(item, tocTree, dataProvider);
    	if (invalidTocItem(item)) {
            tocTreeStyle = (tocTreeStyle + " leos-toc-row-error").trim();
    	}
    	return tocTreeStyle;
    }
    
    private boolean invalidTocItem(TableOfContentItemVO item) {
    	if(isSoftDeletedOrMovedToItem(item))
    		return false;
    	
		if (invalidHeading(item) || invalidContent(item))
    		return true;
    	
    	return false;
    }

	private boolean invalidHeading(TableOfContentItemVO item) {
		return (item.getTocItem().getItemHeading() == OptionsType.MANDATORY 
				|| isElementHeadingOrContentEmpty(item, AnnexStructureType.LEVEL.getType())) && StringUtils.isBlank(item.getHeading());
	}

	private boolean invalidContent(TableOfContentItemVO item) {
		return isCrossHeading(item.getTocItem()) && (StringUtils.isBlank(item.getContent()) || XmlHelper.containsXmlTags(item.getContent()));
	}

    /**
     * On read only mode, it is populating only tocItems.
     * TocRules and numConfigs are populating on runtime through handleEditTocRequest() when user decides to edit.
     */
    public void handleEditTocRequest(TocEditor tocEditor) {
        dataProviderRegistration = dataProvider.addDataProviderListener(event -> {
            if (userOriginated) {
                LOG.debug("data changed logged in DataProviderListener");
                enableSave(true);
            }
        });
        tocEditor.setTocTreeDataFilter(true, dataProvider);

        dropTarget = new TreeGridScrollDropTargetExtension<>(tocTree, DropMode.ON_TOP_OR_BETWEEN);
        dropTarget.setDropEffect(DropEffect.MOVE);
        dropTargetRegistration = dropTarget.addTreeGridDropListener(new EditTocDropHandler(tocTree, messageHelper, eventBus, structureContextProvider.get().getTocRules(), tocEditor));

        eventBus.post(new ExpandTocSliderPanel());
        tocChangedElements = new HashSet<>();
        tocItems = structureContextProvider.get().getTocItems();
        numberingConfigs = structureContextProvider.get().getNumberingConfigs();
    }

    private void enableSave(boolean enable) {
        if(invalidToc(tocTree.getTreeData(), null) && enable) {
       		return;
        }
        
        saveButton.setEnabled(enable);
        saveMenuItem.setEnabled(enable);
        saveButton.setDisableOnClick(enable);
        saveCloseButton.setEnabled(enable);
        saveCloseMenuItem.setEnabled(enable);
        saveCloseButton.setDisableOnClick(enable);
    }

    @Subscribe
    public void handleTocChange(TocChangedEvent event) {
        statusLabel.setValue(messageHelper.getMessage(event.getMessage()));
        StringBuffer styleName = new StringBuffer(STATUS_STYLE)
                .append(" ").append(event.getResult().toString().toLowerCase());
        statusLabel.setStyleName(styleName.toString());
        scheduler.schedule(new Runnable() {
            public void run() {
                statusLabel.setValue("&nbsp;");
                statusLabel.setStyleName(STATUS_STYLE);
            }
        }, 3, TimeUnit.SECONDS);

        if(TocChangedEvent.Result.SUCCESSFUL.equals(event.getResult())) {
            userOriginated = true;
            dataChanged = true;
            enableSave(true);

            if (tocChangedElements.size() <= MAX_CHECKIN_COMMENTS && event.getCheckinElements() != null && event.getCheckinElements().size() > 0) {
                int numCheckinCommentsCanBeAdded = MAX_CHECKIN_COMMENTS - tocChangedElements.size();
                if (numCheckinCommentsCanBeAdded >= event.getCheckinElements().size()) {
                    tocChangedElements.addAll(event.getCheckinElements());
                } else {
                    tocChangedElements.addAll(event.getCheckinElements().stream().limit(numCheckinCommentsCanBeAdded).collect(Collectors.toList()));
                    tocChangedElements.add(new CheckinElement(ActionType.MORE_CHANGES, "...", "..."));
                }
            }
        } else {
            userOriginated = false;
        }
    }

    /**
     * Creates a CheckinElement object and add it to the list of the changed element
     */
    private void addElementInTocChangedList(ActionType actionType, String elementId, String elementTagName) {
        if (tocChangedElements.size() < MAX_CHECKIN_COMMENTS) {
            tocChangedElements.add(new CheckinElement(actionType, elementId, elementTagName));
        } else if (tocChangedElements.size() == MAX_CHECKIN_COMMENTS) {
            tocChangedElements.add(new CheckinElement(ActionType.MORE_CHANGES, "...", "..."));
        }
    }

    private ValueProvider<TableOfContentItemVO, String> getColumnHtml() {
        return this::getColumnItemHtml;
    }

    private String getColumnItemHtml(TableOfContentItemVO tableOfContentItemVO) {
        StringBuilder itemHtml = new StringBuilder(StringUtils.stripEnd(TableOfContentHelper.buildItemCaption(tableOfContentItemVO, DEFAULT_CAPTION_MAX_SIZE, messageHelper), null));
        if (isItemCoEditionActive(tableOfContentItemVO)) {
            if (tableOfContentItemVO.getCoEditionVos().stream().anyMatch(x -> !x.getUserLoginName().equals(user.getLogin()))) {
                itemHtml.insert(0, "<span class=\"leos-toc-user-coedition Vaadin-Icons\">&#xe80d</span>");
            } else {
                itemHtml.insert(0, "<span class=\"leos-toc-user-coedition leos-toc-user-coedition-self-user Vaadin-Icons\">&#xe80d</span>");
            }
        }
        return itemHtml.toString();
    }

    private DescriptionGenerator<TableOfContentItemVO> getColumnDescription() {
        return this::getColumnItemDescription;
    }

    private String getColumnItemDescription(TableOfContentItemVO tableOfContentItemVO) {
        StringBuilder itemDescription = new StringBuilder();
        for (CoEditionVO coEditionVO : tableOfContentItemVO.getCoEditionVos()) {
            StringBuilder userDescription = new StringBuilder();
            if (!coEditionVO.getUserLoginName().equals(user.getLogin())) {
                userDescription.append("<a class=\"leos-toc-user-coedition-lync\" href=\"")
                        .append(StringUtils.isEmpty(coEditionVO.getUserEmail()) ? "" : (coEditionSipEnabled ? new StringBuilder("sip:").append(coEditionVO.getUserEmail().replaceFirst("@.*", "@" + coEditionSipDomain)).toString()
                                : new StringBuilder("mailto:").append(coEditionVO.getUserEmail()).toString()))
                        .append("\">").append(coEditionVO.getUserName()).append(" (").append(StringUtils.isEmpty(coEditionVO.getEntity()) ? "-" : coEditionVO.getEntity())
                        .append(")</a>");
            } else {
                userDescription.append(coEditionVO.getUserName()).append(" (").append(StringUtils.isEmpty(coEditionVO.getEntity()) ? "-" : coEditionVO.getEntity()).append(")");
            }
            itemDescription.append(messageHelper.getMessage("coedition.tooltip.message", userDescription, dataFormat.format(new Date(coEditionVO.getEditionTime()))) + "<br>");
        }
        return itemDescription.toString();
    }

    /**
     * Build elements of tocLayout:
     * - tocTree
     * - statusLabel
     */
    private void buildTocTree() {
        TableSelectionModel<TableOfContentItemVO> model = new TableSelectionModel<>();
        model.setMode(TableSelectionMode.CTRL);
        model.addMultiSelectionListener(this::handleMultiSelection);

        tocTree.setSelectionModel(model);
        tocTree.addColumn(getColumnHtml(), new HtmlRenderer());
        tocTree.setStyleGenerator(this::getTocTreeStyle);
        tocTree.setDescriptionGenerator(getColumnDescription(), ContentMode.HTML);
        tocTree.removeHeaderRow(tocTree.getDefaultHeaderRow());
        tocTree.setSizeFull();

        dataProvider = new TreeDataProvider<TableOfContentItemVO>(tocTree.getTreeData());
        tocTree.setDataProvider(dataProvider);

        tocEditor.setTocTreeDataFilter(false, dataProvider);

        tocTree.addExpandListener(event -> {
            TableOfContentItemVO expandedItem = editionEnabled ? tocEditor.getSimplifiedTocItem(event.getExpandedItem()) : event.getExpandedItem();
            if (expandedItem != null && expandedItem.getNode() != null) {
                expandedNodes.add(expandedItem.getId());
            }
        });

        tocTree.addCollapseListener(event -> {
            TableOfContentItemVO collapsedItem = editionEnabled ? tocEditor.getSimplifiedTocItem(event.getCollapsedItem()) : event.getCollapsedItem();
            if (collapsedItem.getNode() != null) {
                expandedNodes.remove(collapsedItem.getId());
            }
        });

        dragSource = new TreeGridDragSource<>(tocTree);
        dragSource.setEffectAllowed(EffectAllowed.MOVE);
        dragSource.addGridDragStartListener((GridDragStartListener<TableOfContentItemVO>) event -> {
            List<TableOfContentItemVO> draggedItems = event.getDraggedItems();

            Optional<String> areSameType = areSameType(draggedItems);
            if (areSameType.isPresent()) {
                eventBus.post(new TocChangedEvent(areSameType.get(), TocChangedEvent.Result.ERROR, null));
            }
            if (draggedItems.size() > 0) {
                dragSource.setDragData(draggedItems);
                userOriginated = true;
            }
        });
        dragSource.setDragDataGenerator("nodetype", tocVO -> {
            String dragType = tocVO.getTocItem().getAknTag().value();
            dragSource.clearDataTransferData();
            dragSource.setDataTransferData("nodetype", dragType);
            dragSource.setDataTransferText(dragType);
            return dragType;
        });

        statusLabel.setContentMode(ContentMode.HTML);
        statusLabel.setHeight(21, Unit.PIXELS);
    }

    private Optional<String> areSameType(List<TableOfContentItemVO> items) {
        if (items.size() > 1) {
            for (int i = 1; i < items.size(); i++) {
                if (!items.get(i - 1).getTocItem().getAknTag().value().equalsIgnoreCase(items.get(i).getTocItem().getAknTag().value())) {
                    final String prevSelectedTocItem = messageHelper.getMessage("toc.item.type." + items.get(i).getTocItem().getAknTag().value().toLowerCase());
                    final String nextSelectedTocItem = messageHelper.getMessage("toc.item.type." + items.get(i - 1).getTocItem().getAknTag().value().toLowerCase());
                    final String statusMsg = messageHelper.getMessage("toc.item.cross.item.selection.error.message", nextSelectedTocItem, prevSelectedTocItem);
                    return Optional.of(statusMsg);
                }
            }
        }
        return Optional.empty();
    }

    private void handleMultiSelection(MultiSelectionEvent<TableOfContentItemVO> listener) {
        if (listener.isUserOriginated() || editionEnabled) {
            Optional<TableOfContentItemVO> itemId = listener.getFirstSelectedItem();
            LOG.trace("ToC selection changed: id='{}'", itemId);
            itemId.ifPresent(tableOfContentItemVO -> {
                String id = tableOfContentItemVO.getId();
                LOG.trace("ToC navigating to (id={})...", id);
                com.vaadin.ui.JavaScript.getCurrent().execute("LEOS.scrollTo('" + id + "');");
            });
        }
    }

    public void setTableOfContent(TreeData<TableOfContentItemVO> newTocData) {
        userOriginated = false;
        Set<TableOfContentItemVO> items = tocTree.getSelectedItems();
        TreeData<TableOfContentItemVO> tocData = tocTree.getTreeData();
        tocData.removeItem(null);//remove all old data
        tocData.addItems(newTocData.getRootItems(), TableOfContentItemVO::getChildItemsView);//add all new data
        tocTree.getDataProvider().refreshAll();
        refreshSelectedItem(newTocData, items);

        if (expandedNodes != null) {
            expandTree(tocTree.getTreeData().getRootItems());
        } else {
            expandedNodes = new HashSet<>();
            expandDefaultTreeNodes(tocTree.getTreeData().getRootItems()); //expand default items recursively
        }
    }

    private void refreshSelectedItem(TreeData<TableOfContentItemVO> tocData, Set<TableOfContentItemVO> selectedItems) {
        if (!selectedItems.isEmpty()) {
            TableOfContentItemVO selectedItem = selectedItems.iterator().hasNext() ? selectedItems.iterator().next() : null;
            tocTree.deselectAll();
            Optional<TableOfContentItemVO> newSelectedItem = selectedItem != null ?
                    TableOfContentHelper.getItemFromTocById(selectedItem.getId(), tocData.getRootItems()) : Optional.empty();
            if (newSelectedItem.isPresent()) {
                tocTree.select(newSelectedItem.get());
            }
        } else if (editorPanelOpened) {
            closeItemTocEditor();
        }
    }

    public void setPermissions(boolean visible) {
        updateEnabled = visible;
        setVisibility();
    }

    @Override
    public float getDefaultPaneWidth(int numberOfFeatures, boolean tocPresent) {
        final float featureWidth;
        switch(numberOfFeatures){
            case 1:
                featureWidth=100f;
                break;
            default:
                featureWidth = 20f;
                break;
        }//end switch
        return featureWidth;
    }

    @Subscribe
    public void handleElementState(StateChangeEvent event) {
        if (event.getState() != null) {
            inlineTocEditButton.setEnabled(event.getState().isState());
            inlineTocEditMenuItem.setEnabled(event.getState().isState());
        }
    }

    private void updateItemsUserCoEditionInfo(List<TableOfContentItemVO> tableOfContentItemVOList, List<CoEditionVO> coEditionVos, String presenterId) {
        tableOfContentItemVOList.forEach(tableOfContentItemVO -> {
            tableOfContentItemVO.removeAllUserCoEdition();
            coEditionVos.stream()
                    .filter((x) -> InfoType.ELEMENT_INFO.equals(x.getInfoType()) && x.getElementId().replace("__blockcontainer", "").equals(tableOfContentItemVO.getId()) &&
                            !x.getPresenterId().equals(presenterId))
                    .sorted(Comparator.comparing(CoEditionVO::getUserName).thenComparingLong(CoEditionVO::getEditionTime))
                    .forEach(x -> {
                        tableOfContentItemVO.addUserCoEdition(x);
                    });
            if (!tableOfContentItemVO.getChildItems().isEmpty()) {
                updateItemsUserCoEditionInfo(tableOfContentItemVO.getChildItems(), coEditionVos, presenterId);
            }
        });
    }

    private void updateTreeUserCoEditionInfo(List<CoEditionVO> coEditionVos, String presenterId) {
        updateItemsUserCoEditionInfo(tocTree.getTreeData().getRootItems(), coEditionVos, presenterId);
        tocTree.getDataProvider().refreshAll();
    }

    private boolean isTocCoEditionActive() {
        return tocUserCoEdition.getIcon() != null;
    }

    private boolean isItemCoEditionActive(TableOfContentItemVO item) {
        return !item.getCoEditionVos().isEmpty();
    }

    private Optional<TableOfContentItemVO> isItemsCoEditionActive(Collection<TableOfContentItemVO> items) {
        for (TableOfContentItemVO item : items) {
            if (!item.getCoEditionVos().isEmpty()) {
                return Optional.of(item);
            }
        };
        return Optional.empty();
    }

    private void updateToolbarUserCoEditionInfo(List<CoEditionVO> coEditionVos, String presenterId) {
        tocUserCoEdition.setIcon(null);
        tocUserCoEdition.setDescription("");
        tocUserCoEdition.removeStyleName("leos-toolbar-user-coedition-self-user");
        coEditionVos.stream()
                .filter((x) -> InfoType.TOC_INFO.equals(x.getInfoType()) && !x.getPresenterId().equals(presenterId))
                .sorted(Comparator.comparing(CoEditionVO::getUserName).thenComparingLong(CoEditionVO::getEditionTime))
                .forEach(x -> {
                    StringBuilder userDescription = new StringBuilder();
                    if (!x.getUserLoginName().equals(user.getLogin())) {
                        userDescription.append("<a class=\"leos-toc-user-coedition-lync\" href=\"")
                                .append(StringUtils.isEmpty(x.getUserEmail()) ? "" : (coEditionSipEnabled ? new StringBuilder("sip:").append(x.getUserEmail().replaceFirst("@.*", "@" + coEditionSipDomain)).toString()
                                        : new StringBuilder("mailto:").append(x.getUserEmail()).toString()))
                                .append("\">").append(x.getUserName()).append(" (").append(StringUtils.isEmpty(x.getEntity()) ? "-" : x.getEntity())
                                .append(")</a>");
                    } else {
                        userDescription.append(x.getUserName()).append(" (").append(StringUtils.isEmpty(x.getEntity()) ? "-" : x.getEntity()).append(")");
                    }
                    tocUserCoEdition.setDescription(
                            tocUserCoEdition.getDescription() +
                                    messageHelper.getMessage("coedition.tooltip.message", userDescription, dataFormat.format(new Date(x.getEditionTime()))) +
                                    "<br>",
                            ContentMode.HTML);
                });
        if (!tocUserCoEdition.getDescription().isEmpty()) {
            tocUserCoEdition.setIcon(VaadinIcons.USER);
            if (!tocUserCoEdition.getDescription().contains("href=\"")) {
                tocUserCoEdition.addStyleName("leos-toolbar-user-coedition-self-user");
            }
        }
    }

    public void updateUserCoEditionInfo(List<CoEditionVO> coEditionVos, String presenterId) {
        updateToolbarUserCoEditionInfo(coEditionVos, presenterId);
        updateTreeUserCoEditionInfo(coEditionVos, presenterId);
    }

    private void showTocItemEditor() {
        if (editionEnabled && !editorPanelOpened) {
            addComponent(itemEditorLayout);
            editorPanelOpened = true;
        }
    }

    /**
     * This inner class handles the lower panel for editing and deletions
     */
    class Editor extends VerticalLayout implements HasValue.ValueChangeListener<Set<TableOfContentItemVO>> {

        private Binder<TableOfContentItemVO> formBinder = new Binder<>();
        private final TocEditor tocEditor;

        private TextField headingField;
        private TextField contentField;
        private TextField numberField;
        private RadioButtonGroup<String> numberToggleButtons;
        private RadioButtonGroup<String> divisionTypesButtons;
        private RadioButtonGroup<String> listRadioButtonGroup;
        private HorizontalLayout listButtonLayout;
        private RadioButtonGroup<String> indentListRadioButtonGroup;
        private HorizontalLayout indentListButtonLayout;
        private Button deleteButton;
        private Button dapButton;
        private Label warningLabel;

        public Editor(TocEditor tocEditor) {
            this.tocEditor = tocEditor;
            initEditor();
        }

        private void initEditor() {
            itemTypeField = buildTocTypeField(formBinder);
            itemTypeField.setWidth(50, Unit.PERCENTAGE);
            numberField = buildNumberField(formBinder);
            numberField.setWidth(50, Unit.PERCENTAGE);
            headingField = buildHeadingField(formBinder);
            headingField.setWidth(100, Unit.PERCENTAGE);
            contentField = buildContentField(formBinder);
            contentField.setWidth(100, Unit.PERCENTAGE);
            warningLabel = buildLabelWarning();

            divisionTypesButtons = buildDivisionTypeGroupButton(formBinder);
            divisionTypesButtons.setHeight(22, Unit.PIXELS);
            divisionTypesButtons.setWidth(100, Unit.PERCENTAGE);

            FormLayout formLayout = new FormLayout(divisionTypesButtons, itemTypeField, numberField, headingField, contentField, warningLabel);
            addComponent(formLayout);

            listRadioButtonGroup = buildListRadioButtonGroup(formBinder);
            listRadioButtonGroup.setHeight(22, Unit.PIXELS);

            indentListRadioButtonGroup = buildIndentListRadioButtonGroup(formBinder);
            indentListRadioButtonGroup.setHeight(22, Unit.PIXELS);

            listButtonLayout = new HorizontalLayout(listRadioButtonGroup);
            addComponent(listButtonLayout);
            listButtonLayout.addStyleName("leos-list-optiongroup");
            listButtonLayout.setMargin(false);
            listButtonLayout.setWidth(100, Unit.PERCENTAGE);

            indentListButtonLayout = new HorizontalLayout(indentListRadioButtonGroup);
            addComponent(indentListButtonLayout);
            indentListButtonLayout.addStyleName("leos-list-optiongroup");
            indentListButtonLayout.setMargin(false);
            indentListButtonLayout.setWidth(100, Unit.PERCENTAGE);

            numberToggleButtons = buildNumToggleGroupButton(formBinder);
            numberToggleButtons.setHeight(55, Unit.PIXELS);
            numberToggleButtons.setWidth(55, Unit.PERCENTAGE);
            HorizontalLayout toggleButtonLayout = new HorizontalLayout(numberToggleButtons);
            addComponent(toggleButtonLayout);
            toggleButtonLayout.setMargin(false);

            dapButton = buildDapButton(cfgHelper.getProperty("leos.dap.edit.toc.url"));
            deleteButton = buildDeleteButton(formBinder);

            Boolean dapButtonDisabled = Boolean.valueOf(cfgHelper.getProperty("leos.dap.edit.toc.disabled"));

            HorizontalLayout buttonsLayout;
            if(dapButtonDisabled) {
                buttonsLayout = new HorizontalLayout(deleteButton);
            } else {
                buttonsLayout = new HorizontalLayout(dapButton, deleteButton);
            }

            buttonsLayout.setSpacing(true);
            buttonsLayout.setWidth(100, Unit.PERCENTAGE);

            if(!dapButtonDisabled) {
                buttonsLayout.setComponentAlignment(dapButton, Alignment.BOTTOM_LEFT);
            }
            buttonsLayout.setComponentAlignment(deleteButton, Alignment.BOTTOM_RIGHT);
            addComponent(buttonsLayout);
            setComponentAlignment(buttonsLayout, Alignment.BOTTOM_CENTER);
        }

        // selection value change for tree
        @Override
        public void valueChange(HasValue.ValueChangeEvent<Set<TableOfContentItemVO>> event) {
            if(editionEnabled) {
                List<TableOfContentItemVO> selectedItems = new ArrayList<TableOfContentItemVO>(((MultiSelectionEvent) event).getAllSelectedItems());
                if (!areSameType(selectedItems).isPresent()) {
                    dragSource.setEffectAllowed(EffectAllowed.MOVE);
                } else {
                    dragSource.setEffectAllowed(EffectAllowed.NONE);
                }
                boolean isEditionEnabled = false;
                boolean isNumberFieldEnabled = true;
                boolean hasItemNumber = false;
                boolean hasList = false;
                boolean hasIndentList = false;
                boolean isItemHeadingVisible = false;
                boolean isItemHeadingEnabled = false;
                boolean showNumberToggle = false;
                boolean divisionTypes = false;
                boolean isDeleteButtonEnabled = false;
                boolean showItemContent = false;
                boolean enableItemContent = false;
                boolean showWarningLabel = false;
                String listType = "";
                String numberToggleValue = "";
                // is mandatory to set it first to null!
                // the set value in the textField will trigger a value change event which will be ignored if the tocItemForm data is null
                deleteButton.setData(null);
                formBinder.removeBean();

                Set<TableOfContentItemVO> items = event.getValue();
                if (items != null) {
                    TableOfContentItemVO item = items.iterator().hasNext() ? items.iterator().next() : null;
                    LOG.trace("ToC selection changed to:'{}'", item);

                    if (items.size() > 1) { // disable edition for multi-selection
                        isEditionEnabled = false;
                        boolean isDeletedItem = areAllItemsDeleted(tocTree.getSelectedItems());
                        isDeleteButtonEnabled = !isDeletedItem;
                        final String caption = isDeletedItem ? messageHelper.getMessage("toc.edit.window.item.selected.undelete") : messageHelper.getMessage("toc.edit.window.item.selected.delete");
                        final String description = isDeletedItem ? messageHelper.getMessage("toc.edit.window.undelete.confirmation.not") : messageHelper.getMessage(tocEditor.getNotDeletableMessageKey());
                        renderDeleteButton(caption, description, isDeleteButtonEnabled);
                    } else if (item != null) {
                        showTocItemEditor();
                        formBinder.setBean(item);

                        TocItem tocItem = item.getTocItem();
                        if (isCrossHeading(tocItem)) {
                            hasList = true;
                            listType = item.getTocItem().getNumberingType().name();
                            showItemContent = true;
                            enableItemContent = true;
                            if (tableOfContentProcessor.containsInlineElement(item)) {
                                showWarningLabel = true;
                            }
                        } else if (getTagValueFromTocItemVo(item).equals(INDENT) || getTagValueFromTocItemVo(item).equals(POINT)) {
                            hasIndentList = true;
                            listType = tocItem.getNumberingType().name();
                        }

                        itemTypeField.setVisible(true);
                        isNumberFieldEnabled = tocItem.isNumberEditable();
                        hasItemNumber = OptionsType.MANDATORY.equals(tocItem.getItemNumber()) || OptionsType.OPTIONAL.equals(tocItem.getItemNumber());
                        isItemHeadingVisible = OptionsType.MANDATORY.equals(tocItem.getItemHeading()) || OptionsType.OPTIONAL.equals(tocItem.getItemHeading());
                        isItemHeadingEnabled = getTagValueFromTocItemVo(item).equals(DIVISION) ? false : isItemHeadingVisible;
                        isEditionEnabled = !tocItem.isRoot() && tocItem.isDraggable();
                        boolean isDeletedItem = tocEditor.isDeletedItem(item) || tocEditor.isMoveToItem(item);
                        // If toc item is configured to be deletable, then check:
                        // - if the item has already been deleted => check if it can be undelete
                        // - if has not been deleted => check if it can be deleted (Ex: when mixed EC/CN element are present)
                        isDeleteButtonEnabled = tocItem.isDeletable() &&
                                (isDeletedItem ? tocEditor.isUndeletableItem(item) : tocEditor.isDeletableItem(tocTree.getTreeData(), item));
                        final String caption = isDeletedItem ? messageHelper.getMessage("toc.edit.window.item.selected.undelete") : messageHelper.getMessage("toc.edit.window.item.selected.delete");
                        final String description = isDeletedItem ? messageHelper.getMessage("toc.edit.window.undelete.confirmation.not") : messageHelper.getMessage(tocEditor.getNotDeletableMessageKey());
                        renderDeleteButton(caption, description, isDeleteButtonEnabled);
                        deleteButton.setData(item);

                        // num/UnNum to be shown only for 1.article from 2.proposal and are 3.not soft deleted
                        if (item.getOriginAttr() != null && EC.equals(item.getOriginAttr()) && ARTICLE.equals(tocItem.getAknTag().value()) && !item.getChildItems().isEmpty()
                                && !(SoftActionType.DELETE.equals(item.getSoftActionAttr()) || SoftActionType.MOVE_TO.equals(item.getSoftActionAttr()))) {
                            showNumberToggle = true;
                            numberToggleValue = getNumberToggleValue(item);
                            unToggleSiblings(item);
                        }

                        divisionTypes = DIVISION.equals(tocItem.getAknTag().value());
                    }
                }

                // Number
                renderField(numberField, hasItemNumber, isEditionEnabled && isNumberFieldEnabled);
                // Heading
                renderField(headingField, isItemHeadingVisible, isItemHeadingEnabled);
                // Content
                renderField(contentField, showItemContent, enableItemContent);
                // Warning
                renderField(warningLabel, showWarningLabel, showWarningLabel);
                //division number types
                renderDivisionType(divisionTypesButtons, divisionTypes);
                // Number toggle button
                renderNumberToggle(numberToggleButtons, showNumberToggle, numberToggleValue);

                renderList(listRadioButtonGroup, hasList, listType);

                renderIndentList(indentListRadioButtonGroup, hasIndentList, listType);
                //Delete button
                deleteButton.setEnabled(isDeleteButtonEnabled);
                
                formBinder.validate();
            }
        }

        private TextField buildTocTypeField(Binder<TableOfContentItemVO> binder) {
            TextField itemTypeField = new TextField(messageHelper.getMessage("toc.edit.window.item.selected.type"));
            itemTypeField.setEnabled(false);
            binder.forField(itemTypeField)
                    .withConverter(new Converter<String, TocItem>() {
                        @Override
                        public Result<TocItem> convertToModel(String value, ValueContext context) {
                            return Result.ok(null);
                        }

                        @Override
                        public String convertToPresentation(TocItem value, ValueContext context) {
                            return TableOfContentHelper.getDisplayableTocItem(value, messageHelper);
                        }
                    })
                    .bind(TableOfContentItemVO::getTocItem, null);

            itemTypeField.addValueChangeListener(event -> {
            	if(event.isUserOriginated())
            		dataChanged = true;
            });
            return itemTypeField;
        }

        private TextField buildNumberField(Binder<TableOfContentItemVO> binder) {
            final TextField numberField = new TextField(messageHelper.getMessage("toc.edit.window.item.selected.number"));
            numberField.setEnabled(false);
            numberField.setVisible(false);

            //by default we just populate the field without validating. We trust the data inserted previously is correct
            binder.forField(numberField)
                    .bind(TableOfContentItemVO::getNumber, TableOfContentItemVO::setNumber);

            numberField.addValueChangeListener(event -> {
                if (event.isUserOriginated()) {
                	dataChanged = true;
                    TableOfContentItemVO item = binder.getBean();
                    item.setNumber(event.getValue()); //new value
                    tocTree.getDataProvider().refreshItem(item);

                    // remove old binder added previously to the field numberField, and add a specific one depending on the type
                    binder.removeBinding(numberField);
                    switch (item.getTocItem().getNumberingType()) {
                        case NONE:
                            binder.forField(numberField)
                                    .withNullRepresentation("")
                                    .withConverter(StringEscapeUtils::unescapeXml, StringEscapeUtils::escapeXml10, null)
                                    .bind(TableOfContentItemVO::getNumber, TableOfContentItemVO::setNumber);
                            break;
                        default:
                            NumberingConfig config = StructureConfigUtils.getNumberingByName(numberingConfigs, item.getTocItem().getNumberingType());
                            if (config.getRegex() != null) {
                                binder.forField(numberField)
                                        .withNullRepresentation("")
                                        .withConverter(StringEscapeUtils::unescapeXml, StringEscapeUtils::escapeXml10, null)
                                        .withValidator(new RegexpValidator(messageHelper.getMessage(config.getMsgValidationError()), config.getRegex()))
                                        .bind(TableOfContentItemVO::getNumber, TableOfContentItemVO::setNumber);
                            } else {
                                binder.forField(numberField)
                                        .withNullRepresentation("")
                                        .withConverter(StringEscapeUtils::unescapeXml, StringEscapeUtils::escapeXml10, null)
                                        .bind(TableOfContentItemVO::getNumber, TableOfContentItemVO::setNumber);

                            }
                    }
                    item.setAutoNumOverwritten(true);
                    updateUserInfo(item, user);
                    addElementInTocChangedList(ActionType.UPDATED, item.getId(), item.getTocItem().getAknTag().name());
                    enableSave(binder.validate().isOk());
                }
            });
            return numberField;
        }

        private RadioButtonGroup buildListRadioButtonGroup(Binder<TableOfContentItemVO> binder) {
            RadioButtonGroup<String> listRadioButtonGroup = new RadioButtonGroup<>(messageHelper.getMessage("toc.edit.window.item.list.type"));
            listRadioButtonGroup.setItems(NumberingType.NONE.name(), NumberingType.BULLET_BLACK_CIRCLE.name(), NumberingType.INDENT.name());
            listRadioButtonGroup.setValue(NumberingType.NONE.name());
            listRadioButtonGroup.addStyleName(ValoTheme.OPTIONGROUP_HORIZONTAL);
            listRadioButtonGroup.setDescription(messageHelper.getMessage("toc.edit.window.item.list.type"));
            listRadioButtonGroup.setItemCaptionGenerator(item -> messageHelper.getMessage("toc.edit.window.item.list.type." + item.toLowerCase()));

            listRadioButtonGroup.addValueChangeListener(event -> {
                if (event.isUserOriginated()) {
                	dataChanged = true;
                    TableOfContentItemVO item = binder.getBean();
                    NumberingConfig numberingConfig = StructureConfigUtils.getNumberingConfig(numberingConfigs, NumberingType.valueOf(event.getValue()));
                    item.setTocItem(StructureConfigUtils.getTocItemByNumberingConfig(tocItems, NumberingType.valueOf(event.getValue()), item.getTocItem().getAknTag().name()));
                    item.setNumber(numberingConfig.getSequence());
                    updateUserInfo(item, user);
                    tocTree.getDataProvider().refreshItem(item);
                    addElementInTocChangedList(ActionType.UPDATED, item.getId(), item.getTocItem().getAknTag().name());
                    enableSave(binder.validate().isOk());
                }
            });
            return listRadioButtonGroup;
        }


        private RadioButtonGroup buildIndentListRadioButtonGroup(Binder<TableOfContentItemVO> binder) {
            RadioButtonGroup<String> listRadioButtonGroup = new RadioButtonGroup<>(messageHelper.getMessage("toc.edit.window.item.list.type"));
            listRadioButtonGroup.setItems(Arrays.asList(NumberingType.BULLET_NUM.name(), NumberingType.INDENT.name(), NumberingType.POINT_NUM.name()));
            // If the list is empty, all items will be enabled
            if (indentListRadioButtonGroupItemsToEnable != null) {
                listRadioButtonGroup.setItemEnabledProvider(item -> indentListRadioButtonGroupItemsToEnable.contains(item));
            }
            listRadioButtonGroup.setValue(NumberingType.BULLET_NUM.name());
            listRadioButtonGroup.addStyleName(ValoTheme.OPTIONGROUP_HORIZONTAL);
            listRadioButtonGroup.setDescription(messageHelper.getMessage("toc.edit.window.item.list.type"));
            listRadioButtonGroup.setItemCaptionGenerator(item -> messageHelper.getMessage("toc.edit.window.item.list.type." + item.toLowerCase()));

            listRadioButtonGroup.addValueChangeListener(event -> {
                if (event.isUserOriginated()) {
                	dataChanged = true;
                    TableOfContentItemVO item = binder.getBean();
                    NumberingType numberingType = NumberingType.valueOf(event.getValue());
                    TocItem tocItem = StructureConfigUtils.getTocItemByNumberingConfig(tocItems, numberingType, INDENT);
                    tocEditor.propagateChangeListType(item, tocItem, numberingConfigs, tocTree);
                    updateUserInfo(item, user);
                    tocTree.getDataProvider().refreshItem(item);
                    addElementInTocChangedList(ActionType.UPDATED, item.getId(), item.getTocItem().getAknTag().name());
                    enableSave(binder.validate().isOk());
                }
            });
            return listRadioButtonGroup;
        }

        private TextField buildHeadingField(Binder<TableOfContentItemVO> binder) {
            final TextField headingField = new TextField(messageHelper.getMessage("toc.edit.window.item.selected.heading"));
            headingField.setVisible(false);
            headingField.setWidth(100, Unit.PERCENTAGE);
            binder.forField(headingField)
                    .withNullRepresentation("")
                    .withValidator(heading -> {
                        if (binder.getBean().getTocItem().getItemHeading() == OptionsType.MANDATORY ||
                                isElementHeadingOrContentEmpty(binder.getBean(), AnnexStructureType.LEVEL.getType())) {
                            return StringUtils.isNotBlank(heading);
                        }
                        return true;
                    }, messageHelper.getMessage("toc.edit.window.item.selected.heading.error.message"))
                    // is resolved
                    .bind(
                            it -> {
                                return StringEscapeUtils.unescapeXml(it.getHeading());
                            },
                            (it, heading) -> {
                                it.setHeading(heading);
                            });

            headingField.addValueChangeListener(event -> {
                if (event.isUserOriginated()) {
                	dataChanged = true;
                    TableOfContentItemVO item = binder.getBean();
                    if(StringUtils.isEmpty(event.getValue())) {
                        if(EC.equalsIgnoreCase(item.getOriginAttr()) && (item.getOriginHeadingAttr() == null
                                || EC.equalsIgnoreCase(item.getOriginHeadingAttr()))) {
                            item.setHeadingSoftActionAttr(SoftActionType.DELETE);
                            item.setOriginHeadingAttr(EC);
                        }
                    }
                    item.setHeading(StringEscapeUtils.escapeXml10(event.getValue()));
                    tocTree.getDataProvider().refreshItem(item);
                    addElementInTocChangedList(ActionType.UPDATED, item.getId(), item.getTocItem().getAknTag().name());
                    enableSave(binder.validate().isOk());
                }
            });
            return headingField;
        }

        private Label buildLabelWarning() {
            Label warningLabel = new Label();

            warningLabel.setValue(messageHelper.getMessage("toc.edit.window.content.warning"));
            warningLabel.addStyleName("leos-toc-warning");
            return warningLabel;
        }

        private TextField buildContentField(Binder<TableOfContentItemVO> binder) {
            final TextField contentField = new TextField(messageHelper.getMessage("toc.edit.window.item.selected.content"));
            contentField.setVisible(false);
            binder.forField(contentField)
                    .withValidator(content -> {
                        return (!isCrossHeading(binder.getBean().getTocItem()) || !content.isEmpty());
                    }, messageHelper.getMessage("toc.edit.window.item.selected.content.empty.message"))
                    .withValidator(content -> {
                        return (!isCrossHeading(binder.getBean().getTocItem()) || !XmlHelper.containsXmlTags(content));
                    }, messageHelper.getMessage("toc.edit.window.item.selected.content.error.message"))
                    .bind(
                            it -> {
                                return StringEscapeUtils.unescapeXml(XmlHelper.removeXmlTags(XmlHelper.extractContentFromTocItem(it)));
                            },
                            (it, content) -> {
                                tableOfContentProcessor.replaceContentFromTocItem(it, content);
                            });

            contentField.addValueChangeListener(event -> {
                if (event.isUserOriginated()) {
                	dataChanged = true;
                    TableOfContentItemVO item = binder.getBean();
                    tableOfContentProcessor.replaceContentFromTocItem(item, event.getValue());
                    updateUserInfo(item, user);
                    tocTree.getDataProvider().refreshItem(item);
                    addElementInTocChangedList(ActionType.UPDATED, item.getId(), item.getTocItem().getAknTag().name());
                    enableSave(binder.validate().isOk());
                }
            });
            return contentField;
        }

        private RadioButtonGroup<String> buildNumToggleGroupButton(Binder<TableOfContentItemVO> binder) {
            RadioButtonGroup<String> buttons = new RadioButtonGroup<>();
            buttons.setCaption("Paragraph Numbering");
            buttons.setItems("Numbered", "Unnumbered");
            buttons.setVisible(false);

            buttons.addSelectionListener(event -> {
            	if(event.isUserOriginated())
            		dataChanged = true;
            	
                TableOfContentItemVO item = binder.getBean();

                if (tocTree.getTreeData().contains(item) && EC.equals(item.getOriginAttr()) && !item.getChildItems().isEmpty()) {

                    TableOfContentItemVO updatedItemVO = tocTree.getTreeData().getChildren(item).get(0).getParentItem();
                    TableOfContentItemVO firstChild = updatedItemVO.getChildItems().get(0);
                    boolean flag = false;

                    for (TableOfContentItemVO itemVo : updatedItemVO.getChildItems()) {
                        if (itemVo.getNumSoftActionAttr() != null && SoftActionType.DELETE.equals(itemVo.getNumSoftActionAttr())) {
                            flag = true;
                            break;
                        }
                    }

                    if ("Numbered".equals(buttons.getSelectedItem().get())) {
                        if ((firstChild.getNumber() == null || firstChild.getNumber() == "")
                                || flag) {
                            updatedItemVO.setNumberingToggled(true);
                            updatedItemVO.getChildItemsView().stream().filter(itemVo -> itemVo.getNode() == null).forEach(itemVo -> {
                                itemVo.setNumber("#");
                            });
                            updateUserInfo(item, user);
                            tocTree.getDataProvider().refreshItem(updatedItemVO);
                            enableSave(true);
                        } else if (isToggledByUser) {// check if save is not enabled before
                            enableSave(false);
                        }
                    } else if ("Unnumbered".equals(buttons.getSelectedItem().get())) {
                        if ((firstChild.getNumber() != null && firstChild.getNumber() != "")
                                && !flag) {
                            updatedItemVO.setNumberingToggled(false);
                            updatedItemVO.getChildItemsView().stream().filter(itemVo -> itemVo.getNode() == null).forEach(itemVo -> {
                                itemVo.setNumber(null);
                            });
                            updateUserInfo(item, user);
                            tocTree.getDataProvider().refreshItem(updatedItemVO);
                            enableSave(true);
                        } else if (isToggledByUser) {
                            enableSave(false);
                        }
                    } else {
                        enableSave(false);
                    }
                    if (!isToggledByUser) {
                        isToggledByUser = true;
                    }
                }
            });

            return buttons;
        }

        private RadioButtonGroup<String> buildDivisionTypeGroupButton(Binder<TableOfContentItemVO> binder) {
            RadioButtonGroup<String> buttons = new RadioButtonGroup<>(messageHelper.getMessage("toc.edit.window.item.list.type"));
            buttons.setCaption("Type");
            buttons.setItems("type_1", "type_2", "type_3", "type_4");
            buttons.setItemCaptionGenerator(item -> messageHelper.getMessage("toc.division.number.caption."+item));
            buttons.setDescription(messageHelper.getMessage("toc.edit.window.item.list.type"));
            buttons.setVisible(false);
            buttons.addStyleName(ValoTheme.OPTIONGROUP_HORIZONTAL);
            itemTypeField.setVisible(false);
            return buttons;
        }

        private void MultipleDeleteWithConfirmationCheck(Collection<TableOfContentItemVO> items) {
            ConfirmDialog confirmDialog = ConfirmDialog.getFactory().create(
                    messageHelper.getMessage("toc.edit.window.multi.delete.confirmation.title"),
                    messageHelper.getMessage("toc.edit.window.multi.delete.confirmation.message"),
                    messageHelper.getMessage("toc.edit.window.delete.confirmation.confirm"),
                    messageHelper.getMessage("toc.edit.window.delete.confirmation.cancel"), null);
            confirmDialog.setContentMode(ConfirmDialog.ContentMode.HTML);
            confirmDialog.show(getUI(), dialog -> {
                if (dialog.isConfirmed()) {
                    items.forEach(item -> {
                        checkDeleteLastEditingTypeConsumer.accept(item.getId(), () -> deleteItem(item));
                    });
                }
            }, true);
        }

        private void DeleteWithConfirmationCheck(TableOfContentItemVO item) {
            if (tocEditor.checkIfConfirmDeletion(tocTree.getTreeData(), item)) {
                confirmItemDeletion(item);
            } else {
                checkDeleteLastEditingTypeConsumer.accept(item.getId(), () -> deleteItem(item));
            }
        }

        private Button buildDeleteButton(Binder<TableOfContentItemVO> binder) {
            final Button button = new Button(messageHelper.getMessage("toc.edit.window.item.selected.delete"));
            button.setEnabled(false);
            button.addClickListener(event -> {
                TableOfContentItemVO item = binder.getBean();
                if (item != null && tocTree.getTreeData().contains(item)) {
                    if (tocEditor.isDeletedItem(item)) {
                        undeleteItem(item);
                    } else {
                        if (isTocCoEditionActive() || isItemCoEditionActive(item)) {
                            String description=tocUserCoEdition.getDescription() + getColumnItemDescription(item);
                            description = description.replace("leos-toc-user-coedition-lync", "");
                            ConfirmDialog confirmDialog = ConfirmDialog.getFactory().create(
                                    messageHelper.getMessage("coedition.delete.element.confirmation.title"),
                                    messageHelper.getMessage("coedition.delete.element.confirmation.message", description),
                                    messageHelper.getMessage("coedition.delete.element.confirmation.confirm"),
                                    messageHelper.getMessage("coedition.delete.element.confirmation.cancel"), null);
                            confirmDialog.setContentMode(ConfirmDialog.ContentMode.HTML);
                            confirmDialog.getContent().setHeightUndefined();
                            confirmDialog.setHeightUndefined();
                            confirmDialog.show(getUI(), dialog -> {
                                if (dialog.isConfirmed()) {
                                    DeleteWithConfirmationCheck(item);
                                }
                            }, true);
                        } else {
                            DeleteWithConfirmationCheck(item);
                        }
                    }
                } else if (tocTree.getSelectedItems().size() > 1) {
                    Optional<TableOfContentItemVO> isCoEditionActive = isItemsCoEditionActive(tocTree.getSelectedItems());
                    if (areItemsDeleted(tocTree.getSelectedItems())) {
                        for (TableOfContentItemVO selectedItem: tocTree.getSelectedItems()) {
                            undeleteItem(selectedItem);
                        }
                    } else if (isTocCoEditionActive() || isCoEditionActive.isPresent()) {
                        String description=tocUserCoEdition.getDescription() + getColumnItemDescription(isCoEditionActive.get());
                        description = description.replace("leos-toc-user-coedition-lync", "");
                        ConfirmDialog confirmDialog = ConfirmDialog.getFactory().create(
                                messageHelper.getMessage("coedition.delete.element.confirmation.title"),
                                messageHelper.getMessage("coedition.delete.element.confirmation.message", description),
                                messageHelper.getMessage("coedition.delete.element.confirmation.confirm"),
                                messageHelper.getMessage("coedition.delete.element.confirmation.cancel"), null);
                        confirmDialog.setContentMode(ConfirmDialog.ContentMode.HTML);
                        confirmDialog.getContent().setHeightUndefined();
                        confirmDialog.setHeightUndefined();
                        confirmDialog.show(getUI(), dialog -> {
                            if (dialog.isConfirmed()) {
                                MultipleDeleteWithConfirmationCheck(tocTree.getSelectedItems());
                            }
                        }, true);
                    } else {
                        MultipleDeleteWithConfirmationCheck(tocTree.getSelectedItems());
                    }
                }
            });
            return button;
        }

        private boolean areItemsDeleted(Collection<TableOfContentItemVO> items) {
            for (TableOfContentItemVO item: items) {
               if (!tocEditor.isDeletedItem(item)) {
                   return false;
               }
            }
            return true;
        }

        private boolean areAllItemsDeleted(Collection<TableOfContentItemVO> items) {
            for (TableOfContentItemVO item: items) {
                if (!tocEditor.isDeletedItem(item) && !tocEditor.isMoveToItem(item)) {
                    return false;
                }
            }
            return true;
        }

        private void undeleteItem(TableOfContentItemVO item) {
            tocEditor.undeleteItem(tocTree, item);
            tocTree.getDataProvider().refreshAll();
            tocTree.deselectAll();
            final CheckinElement checkinElement = new CheckinElement(ActionType.UNDELETED, item.getId(), item.getTocItem().getAknTag().name());
            final String statusMsg = messageHelper.getMessage("toc.edit.window.undelete.confirmation.success", TableOfContentHelper.getDisplayableTocItem(item.getTocItem(), messageHelper));
            eventBus.post(new TocChangedEvent(statusMsg, TocChangedEvent.Result.SUCCESSFUL, Arrays.asList(checkinElement)));
            enableSave(true);
            dataChanged = true;
        }

        private void deleteItem(TableOfContentItemVO item) {
            final ActionType actionType = tocEditor.deleteItem(tocTree, item);
            final CheckinElement checkinElement = new CheckinElement(actionType, item.getId(), item.getTocItem().getAknTag().name());
            final String statusMsg = messageHelper.getMessage("toc.edit.window.delete.message", TableOfContentHelper.getDisplayableTocItem(item.getTocItem(), messageHelper));
            eventBus.post(new TocChangedEvent(statusMsg, TocChangedEvent.Result.SUCCESSFUL, Arrays.asList(checkinElement)));
            closeItemTocEditor();
            enableSave(true);
            dataChanged = true;
        }

        private void confirmItemDeletion(TableOfContentItemVO item) {
            ConfirmDialog confirmDialog = ConfirmDialog.getFactory().create(
                    messageHelper.getMessage("toc.edit.window.delete.confirmation.title"),
                    messageHelper.getMessage("toc.edit.window.delete.confirmation.message"),
                    messageHelper.getMessage("toc.edit.window.delete.confirmation.confirm"),
                    messageHelper.getMessage("toc.edit.window.delete.confirmation.cancel"), null);
            confirmDialog.setContentMode(ConfirmDialog.ContentMode.HTML);
            confirmDialog.show(getUI(), dialog -> {
                if (dialog.isConfirmed()) {
                    checkDeleteLastEditingTypeConsumer.accept(item.getId(), () -> deleteItem(item));
                }
            }, true);
        }

        private Button buildDapButton(final String dapUrl) {
            Button button = new Button(messageHelper.getMessage("leos.button.dap"));
            button.setDescription(messageHelper.getMessage("leos.button.dap.description"));
            button.setIcon(LeosTheme.LEOS_DAP_ICON_16);
            button.addClickListener(new Button.ClickListener() {

                private static final long serialVersionUID = -5633348109667050418L;

                @Override
                public void buttonClick(Button.ClickEvent event) {
                    Page.getCurrent().open(dapUrl, "_new");
                }
            });
            return button;
        }

        private void renderDeleteButton(String caption, String description, boolean enable) {
            deleteButton.setEnabled(enable);
            deleteButton.setCaption(caption);
            deleteButton.setDescription(enable ? "" : description);
        }

        private void renderField(AbstractComponent field, boolean display, boolean enable) {
            field.setVisible(display);
            field.setEnabled(enable);
        }

        private void renderList(RadioButtonGroup<String> radioButtons, boolean display, String listValue) {
            radioButtons.setVisible(display);
            radioButtons.setEnabled(display);
            listButtonLayout.setVisible(display);
            if (listValue == null || listValue.isEmpty()) {
                radioButtons.setSelectedItem(NumberingType.NONE.name());
            } else {
                radioButtons.setSelectedItem(listValue.toUpperCase());
            }
        }

        private void renderIndentList(RadioButtonGroup<String> radioButtons, boolean display, String listValue) {
            radioButtons.setVisible(display);
            radioButtons.setEnabled(display);
            indentListButtonLayout.setVisible(display);
            radioButtons.setSelectedItem(listValue == null ? NumberingType.BULLET_NUM.name() : listValue);
        }

        private void renderDivisionType(RadioButtonGroup<String> radioButtons, boolean display) {
            TableOfContentItemVO itemVO = formBinder.getBean();
            if (display) {
                String currentType = itemVO.isAutoNumOverwritten() ? null : itemVO.getStyle();
                radioButtons.getDataProvider().refreshAll();
                radioButtons.setVisible(true);
                radioButtons.setEnabled(true);
                radioButtons.setSelectedItem(currentType);
                itemTypeField.setVisible(false);
                List<String> possibleDivisionType = getDivisionTypesToEnable(getPreviousDivisionType(itemVO));
                radioButtons.setItemEnabledProvider(item -> possibleDivisionType.contains(item));
                radioButtons.addSelectionListener(event -> {
                    if (event.isUserOriginated()) {
                    	dataChanged = true;
                        TableOfContentItemVO item = formBinder.getBean();
                        if(item.equals(itemVO)){
                            int index = item.getParentItem().getChildItems().indexOf(item);
                            tocTree.getTreeData().getChildren(item.getParentItem()).get(index).setStyle(event.getSelectedItem().get());
                            item.setAutoNumOverwritten(false);
                            updateStyleClassOfTocItems(tocTree.getTreeData().getChildren(item.getParentItem()), DIVISION);
                            updateDepthOfTocItems(tocTree.getTreeData().getChildren(item.getParentItem()));
                            enableSave(!item.getStyle().equals(currentType));
                        }
                    }
                });
                radioButtons.getItemEnabledProvider();
            } else {
                radioButtons.setVisible(false);
            }
        }

        private List<String> getDivisionTypesToEnable(int previousDivisionType){
            List<String> possibleDivision = new ArrayList<>();
            while(previousDivisionType >= 0){
                possibleDivision.add("type_"+previousDivisionType);
                previousDivisionType--;
            }
            if (possibleDivision.isEmpty()) {
                possibleDivision.add("type_1");
            }
            return possibleDivision;
        }

        private int getPreviousDivisionType(TableOfContentItemVO itemVO) {
            List<TableOfContentItemVO> tableOfContentItemVODivisionList =
                    itemVO.getParentItem().getChildItems().stream()
                            .filter(tocItemVO -> tocItemVO.getTocItem().getAknTag().value().equals(DIVISION))
                            .collect(Collectors.toList());
            int itemIndex = tableOfContentItemVODivisionList.indexOf(itemVO);
            if (itemIndex > 0) {
                TableOfContentItemVO previousDivision = tableOfContentItemVODivisionList.get(itemIndex - 1);
                String previousDivisionStyle = previousDivision.getStyle();
                return Integer.parseInt(previousDivisionStyle.substring(previousDivisionStyle.indexOf("_") + 1)) + 1;
            }
            return -1;
        }

        private void renderNumberToggle(RadioButtonGroup<String> radioButtons, boolean display, String toggleValue) {
            if (display) {
                radioButtons.setStyleName("leos-toc-content-hide",false);
                radioButtons.setVisible(true);
                radioButtons.setEnabled(true);
                radioButtons.setSelectedItem(toggleValue);
                isToggledByUser = false;
            } else {
                radioButtons.setStyleName("leos-toc-content-hide",true);
            }
        }

        private String getNumberToggleValue(TableOfContentItemVO item) {
            final String toggleValue;
            final TableOfContentItemVO firstChild = item.getChildItems().get(0);
            if (StringUtils.isNotEmpty(firstChild.getNumber())
                    && !SoftActionType.DELETE.equals(firstChild.getNumSoftActionAttr())) {
                toggleValue = "Numbered";
            } else {
                toggleValue = "Unnumbered";
            }

            return toggleValue;
        }

        /**
         * UnToggle all NumberingToggle for other articles.
         */
        private void unToggleSiblings(TableOfContentItemVO item) {
            List<TableOfContentItemVO> listOfArticle = tocTree.getTreeData().getParent(item).getChildItems();
            for (TableOfContentItemVO itemVO : listOfArticle) {
                if (ARTICLE.equals(itemVO.getTocItem().getAknTag().value()) && !itemVO.equals(item) && itemVO.isNumberingToggled() != null) {
                    itemVO.setNumberingToggled(null);
                }
            }
        }
    }

    private CheckDeleteLastEditingTypeConsumer checkDeleteLastEditingTypeConsumer;

    @Subscribe
    public void checkDeleteLastEditingType(CheckDeleteLastEditingTypeEvent event) {
        checkDeleteLastEditingTypeConsumer.accept(event.getElementId(), () -> eventBus.post(event.getActionEvent()));
    }
}
