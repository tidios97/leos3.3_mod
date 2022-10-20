package eu.europa.ec.leos.services.label.ref;

import eu.europa.ec.leos.test.support.LeosTest;
import org.junit.Test;

import java.util.Locale;

import static eu.europa.ec.leos.services.support.XmlHelper.PARAGRAPH;
import static eu.europa.ec.leos.services.support.XmlHelper.POINT;
import static org.junit.Assert.assertEquals;

public class NumFormatterTest extends LeosTest {

    @Test
    public void isUnnumbered_unNum_paragraph() {
        //setup
        TreeNode testNode = new TreeNode(PARAGRAPH, 0, 1, "id", null, null, null, null);
        assertEquals(true, NumFormatter.isUnnumbered(testNode));
    }

    @Test
    public void isUnnumbered_unNum_paragraph2() {
        //setup
        TreeNode testNode = new TreeNode(PARAGRAPH, 0, 1, "id", "", null, null, null);
        assertEquals(true, NumFormatter.isUnnumbered(testNode));
    }

    @Test
    public void isUnnumbered_unNum_point() {
        //setup
        TreeNode testNode = new TreeNode(POINT, 0, 1, "id", "-", null, null, null);
        assertEquals(true, NumFormatter.isUnnumbered(testNode));
    }

    @Test
    public void isUnnumbered_unNum_point2() {
        //setup
        TreeNode testNode = new TreeNode(POINT, 0, 1, "id", " ", null, null, null);
        assertEquals(true, NumFormatter.isUnnumbered(testNode));
    }

    @Test
    public void isUnnumbered_num_paragraph() {
        //setup
        TreeNode testNode = new TreeNode(PARAGRAPH, 0, 1, "id", "1", null, null, null);
        assertEquals(false, NumFormatter.isUnnumbered(testNode));
    }

    @Test
    public void formatUnnumbered_first() {
        //setup
        TreeNode testNode = new TreeNode(PARAGRAPH, 0, 1, "id", "", null, null, null);
        assertEquals("first", NumFormatter.formatUnnumbered(testNode, new Locale("en")));
    }
    
    @Test
    public void formatUnnumbered_first_fr() {
        //setup
        TreeNode testNode = new TreeNode(PARAGRAPH, 0, 1, "id", "", null, null, null);
        assertEquals("un", NumFormatter.formatUnnumbered(testNode, new Locale("fr")));
    }

    @Test
    public void formatUnnumbered_eighth_en() {
        //setup
        TreeNode testNode = new TreeNode(PARAGRAPH, 0, 8, "id", "", null, null, null);
        assertEquals("eighth", NumFormatter.formatUnnumbered(testNode, new Locale("en")));
    }

    @Test
    public void formatUnnumbered_eighth_fr() {
        //setup
        TreeNode testNode = new TreeNode(PARAGRAPH, 0, 8, "id", "", null, null, null);
        //Need to define spellout/ordinal rules for french. as of now using simple representation
        assertEquals("huit", NumFormatter.formatUnnumbered(testNode, new Locale("fr")));
    }

    @Test
    public void formatUnnumbered_third() {
        //setup
        TreeNode testNode = new TreeNode(PARAGRAPH, 0, 3, "id", "", null, null, null);
        //Need to define spellout/ordinal rules for french. as of now using simple representation
        assertEquals("third", NumFormatter.formatUnnumbered(testNode, new Locale("en")));
    }
    
    @Test
    public void formatPlural_multiple_paragraph() {
        //setup
        TreeNode testNode = new TreeNode(PARAGRAPH, 0, 1, "id", "", null, null, null);
        assertEquals("paragraphs", NumFormatter.formatPlural(testNode, 0, new Locale("en")));
    }
    
    @Test
    public void formatPlural_single_paragraph() {
        //setup
        TreeNode testNode = new TreeNode(PARAGRAPH, 0, 1, "id", "", null, null, null);
        assertEquals(PARAGRAPH, NumFormatter.formatPlural(testNode, 1, new Locale("en")));
    }
}
