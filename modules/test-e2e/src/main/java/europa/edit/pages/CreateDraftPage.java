package europa.edit.pages;

import org.openqa.selenium.By;

public class CreateDraftPage {
    public static final By DRAFT_TITLE = By.xpath("//span[text()='Draft title:']/ancestor::tr[position() = 1]//input");
    public static final String V_TREE_NODE_CAPTION = "//*[@class='v-tree-node-caption']";
}
