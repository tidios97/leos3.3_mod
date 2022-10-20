package eu.europa.ec.leos.services.numbering.depthBased;

import eu.europa.ec.leos.services.util.TestUtils;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import java.util.List;

import static eu.europa.ec.leos.services.support.XmlHelper.DIVISION;
import static eu.europa.ec.leos.services.support.XercesUtils.createXercesDocument;
import static eu.europa.ec.leos.services.support.XercesUtils.getId;
import static org.junit.Assert.assertEquals;

public class ParentChildConverterTest {

    protected final static String FILE_PREFIX = "/numbering/explanatory/";

    private ParentChildConverter parentChildConverter = new ParentChildConverter();

    /**
     * Should build the following parent-child structure.
     * I.
     * II.
     *    A.
     *       I)
     *          A)
     * III.
     *    A.
     *      I)
     *          A)
     *          B)
     *      II)
     *    B.
     * IV.
     */
    @Test
    public void test_basedOnDepth_to_parentChildStructure() {
        final byte[] xmlInput = TestUtils.getFileContent(FILE_PREFIX, "test_division_expected.xml");

        Document document = createXercesDocument(xmlInput);
        NodeList elements = document.getElementsByTagName(DIVISION);

        List<ParentChildNode> parentChildList = parentChildConverter.getParentChildStructure(elements, false);
//        System.out.println(ParentChildNode.getBasedOnDepthNodeListAsString(parentChildList));
        assertEquals(4, parentChildList.size()); // I., II, III., IV

        ParentChildNode div_i = parentChildList.get(0);
        ParentChildNode div_ii = parentChildList.get(1);
        ParentChildNode div_iii = parentChildList.get(2);
        ParentChildNode div_iv = parentChildList.get(3);
        assertEquals("division_i", getId(div_i.getNode()));
        assertEquals("division_ii", getId(div_ii.getNode()));
        assertEquals("division_iii", getId(div_iii.getNode()));
        assertEquals("division_iv", getId(div_iv.getNode()));

        assertEquals(0, div_i.getChildren().size()); // I. has no children
        assertEquals(1, div_ii.getChildren().size()); // II. has 1 children
        assertEquals(2, div_iii.getChildren().size()); // III. has 2 children
        assertEquals(0, div_iv.getChildren().size()); // IV. has no children

        final ParentChildNode div_ii_a = div_ii.getChildren().get(0);
        assertEquals("division_ii_a", getId(div_ii_a.getNode()));
        assertEquals(1, div_ii_a.getChildren().size()); // II.A. has 1 children

        final ParentChildNode div_ii_a_i = div_ii_a.getChildren().get(0);
        assertEquals("division_ii_a_i", getId(div_ii_a_i.getNode()));
        assertEquals(1, div_ii_a_i.getChildren().size()); // II.A.I) has 1 children

        final ParentChildNode div_ii_a_i_a = div_ii_a_i.getChildren().get(0);
        assertEquals("division_ii_a_i_a", getId(div_ii_a_i_a.getNode()));
        assertEquals(0, div_ii_a_i_a.getChildren().size()); // II.A.I)A) has no children

        final ParentChildNode div_iii_a = div_iii.getChildren().get(0);
        assertEquals("division_iii_a", getId(div_iii_a.getNode()));
        assertEquals(2, div_iii_a.getChildren().size()); // III.A. has 2 children

        final ParentChildNode div_iii_a_i = div_iii_a.getChildren().get(0);
        final ParentChildNode div_iii_a_ii = div_iii_a.getChildren().get(1);
        assertEquals("division_iii_a_i", getId(div_iii_a_i.getNode()));
        assertEquals("division_iii_a_ii", getId(div_iii_a_ii.getNode()));

        assertEquals(2, div_iii_a_i.getChildren().size()); // III.A.I) has 2 children
        final ParentChildNode div_iii_a_i_a = div_iii_a_i.getChildren().get(0);
        final ParentChildNode div_iii_a_i_b = div_iii_a_i.getChildren().get(1);
        assertEquals("division_iii_a_i_a", getId(div_iii_a_i_a.getNode()));
        assertEquals("division_iii_a_i_b", getId(div_iii_a_i_b.getNode()));
        assertEquals(0, div_iii_a_i_a.getChildren().size());
        assertEquals(0, div_iii_a_i_b.getChildren().size());
    }

    @Test
    public void test_basedOnDepth_to_parentChildStructure_missingTypeAttribute() {
        final byte[] xmlInput = TestUtils.getFileContent(FILE_PREFIX, "test_division_missingTypeAttribute.xml");

        Document document = createXercesDocument(xmlInput);
        NodeList elements = document.getElementsByTagName(DIVISION);

        List<ParentChildNode> parentChildList = parentChildConverter.getParentChildStructure(elements, false);
//        System.out.println(ParentChildNode.getBasedOnDepthNodeListAsString(parentChildList));
        assertEquals(13, parentChildList.size()); // all in the first level
    }


}
