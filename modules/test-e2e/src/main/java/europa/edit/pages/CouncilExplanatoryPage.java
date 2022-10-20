package europa.edit.pages;

import lombok.experimental.UtilityClass;
import org.openqa.selenium.By;

@UtilityClass
public class CouncilExplanatoryPage {
    public static final By CLOSE_BUTTON = By.xpath("//*[text()='Close']");
    public static final By LEOS_EXPLANATORY_VIEW_TITLE = By.cssSelector("#_preface__longtitle__p__doctitle");
    public static final By CREATE_BUTTON = By.cssSelector(".popupContent .primary");
    public static final By V_TREE_NODE_LEAF_SPAN = By.cssSelector(".v-tree-node-leaf span");
    public static final String V_TREE_NODE_LEAF = "//*[contains(@class,'v-tree-node-leaf')]";
    public static final String DIVISION = "//division";
    public static final String HEADING = "//heading";
    public static final String LEVEL = "//level";
    public static final String CONTENT = "//content";
    public static final String AKNP = "//aknp";
    public static final String PARAGRAPH = "//paragraph";
}

