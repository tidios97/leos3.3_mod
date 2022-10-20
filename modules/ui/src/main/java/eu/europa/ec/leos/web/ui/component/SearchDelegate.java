package eu.europa.ec.leos.web.ui.component;

import com.google.common.eventbus.EventBus;
import com.vaadin.event.ShortcutAction;
import com.vaadin.event.ShortcutListener;
import com.vaadin.shared.Registration;
import com.vaadin.shared.ui.ContentMode;
import com.vaadin.ui.Button;
import com.vaadin.ui.VerticalLayout;
import eu.europa.ec.leos.i18n.MessageHelper;
import eu.europa.ec.leos.ui.component.LeosDisplayField;
import eu.europa.ec.leos.ui.component.search.SearchBar;
import eu.europa.ec.leos.ui.event.StateChangeEvent;
import eu.europa.ec.leos.ui.event.search.SearchBarClosedEvent;
import eu.europa.ec.leos.ui.extension.SearchTargetExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class SearchDelegate {
    private static final Logger LOG = LoggerFactory.getLogger(SearchDelegate.class);

    private SearchTargetExtension<LeosDisplayField> searchTargetExtension;
    private final VerticalLayout layout;
    private final Button searchButton;
    private SearchBar searchBar;
    private final EventBus eventBus;
    private final MessageHelper messageHelper;
    private final LeosDisplayField displayField;
    private final List<Registration> shortcuts = new ArrayList<>();
    private final boolean searchAndReplaceEnabled;

    public SearchDelegate(Button searchButton, VerticalLayout layout, MessageHelper messageHelper, EventBus eventBus, LeosDisplayField displayField, boolean searchAndReplaceEnabled) {
        this.layout = layout;
        this.searchButton = searchButton;
        this.messageHelper = messageHelper;
        this.eventBus =eventBus;
        this.displayField =displayField;
        this.searchAndReplaceEnabled = searchAndReplaceEnabled;

        buildSearchButton();
    }

    private Button buildSearchButton() {
        searchButton.setDescription(messageHelper.getMessage("document.search.minimized.description"), ContentMode.HTML);

        searchButton.setEnabled(searchAndReplaceEnabled);

        //Handle Search
        searchButton.addClickListener(event -> {
            if (searchBar == null || !searchBar.isAttached()) {
                openSearchBar();
                searchBar.showReplaceBar(false);
            } else {
                closeSearchBar();
            }
        });

        if (searchAndReplaceEnabled) {
            //BEWARE, this is global shortcut
            Button.ClickShortcut clickShortcut = new Button.ClickShortcut(searchButton, ShortcutAction.KeyCode.F, ShortcutAction.ModifierKey.CTRL);
            shortcuts.add(layout.addShortcutListener(clickShortcut));

            //handle Replace shortcurt
            shortcuts.add(layout.addShortcutListener(new ShortcutListener("ReplaceAll", ShortcutAction.KeyCode.R, new int[]{ShortcutAction.ModifierKey.CTRL}) {
                private static final long serialVersionUID = 1L;

                @Override
                public void handleAction(Object sender, Object target) {
                    if (searchBar == null || !searchBar.isAttached()) {
                        openSearchBar();
                        searchBar.showReplaceBar(true);
                    } else if (!searchBar.isReplaceBarVisible()) {
                        searchBar.showReplaceBar(true);
                    } else {
                        closeSearchBar();
                    }
                }
            }));
        }

        return searchButton;
    }

    private void openSearchBar() {
        if (searchBar == null) {
            searchBar = new SearchBar(messageHelper, eventBus);
        }
        if (searchTargetExtension == null) {
            searchTargetExtension = new SearchTargetExtension<>(displayField, eventBus);
        }
        layout.addComponent(searchBar, 1);
        layout.setSizeFull();
    }

    private void closeSearchBar() {
        // Fire event to cleanup on presenter side
        eventBus.post(new SearchBarClosedEvent());
    }

    public void closeSearchBarComponent() {
        if (searchBar != null && searchBar.isAttached()) {
            searchBar.barClosed();
            layout.removeComponent(searchBar);
        }
        layout.setSizeFull();
    }

    public void detach() {
        LOG.trace("Removing shortcutListener from search and replace popup...");
        shortcuts.forEach(Registration::remove);
    }

    public void handleElementState(StateChangeEvent event) {
        if (event.getState() != null) {
            searchButton.setEnabled(event.getState().isState() && searchAndReplaceEnabled);
            if (searchBar != null) {
                searchBar.setEnabled(event.getState().isState() && searchAndReplaceEnabled);
            }
        }
    }
}
