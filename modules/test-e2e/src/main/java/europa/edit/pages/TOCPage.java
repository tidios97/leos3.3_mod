package europa.edit.pages;

import lombok.experimental.UtilityClass;
import org.openqa.selenium.By;

@UtilityClass
public class TOCPage {
    public static final By TABLE_TREE_GRID_TBODY_TR = By.cssSelector("table[role='treegrid'] tbody tr");
    public static final String ANCESTOR_TR_CLASS_LEOS_SOFT_REMOVED = "//ancestor::tr[contains(@class,'leos-soft-removed')]";
}

