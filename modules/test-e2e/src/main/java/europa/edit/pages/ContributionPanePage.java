package europa.edit.pages;

import org.openqa.selenium.By;

public class ContributionPanePage {
    public static final By CONTRIBUTION_PANE_ACCORDION = By.xpath("//*[text()='Contribution pane']//ancestor::div[@class='v-accordion-item-caption']");
    public static final By SECOND_CONTAINER_REVISION_BAR = By.cssSelector(".v-splitpanel-second-container .v-slot-leos-revision-bar");
    public static final By SECOND_CONTAINER_REVISION_CONTENT = By.cssSelector(".v-splitpanel-second-container .v-slot-leos-revision-content");
    public static final By SUBMENU_ITEM_CAPTION = By.cssSelector(".v-menubar-submenu-leos-actions-menu .v-menubar-menuitem-caption");
    public static final By CONTRIBUTION_VIEW_MERGE_CLOSE_BUTTON = By.xpath("(//*[contains(@class,'v-slot-leos-revision-bar')]//*[contains(@class,'leos-toolbar-button') and @role='button'])[5]");
    public static final String CONTRIBUTION_CARD = "//*[@id='contributionCard']";
    public static final String VERSION_CARD_READER = "//*[contains(@class,'v-horizontallayout-version-card-header')]";
    public static final String CONTRIBUTION_CARD_ACTION = "//*[@id='contributionCardAction']";
    public static final String V_MENUBAR_SUBMENU = "//*[contains(@class,'v-menubar-submenu-leos-actions-menu')]";
    public static final String ROLE_MENUITEM = "//*[@role='menuitem']";
}
