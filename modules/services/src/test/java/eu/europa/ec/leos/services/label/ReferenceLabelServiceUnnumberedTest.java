package eu.europa.ec.leos.services.label;

import eu.europa.ec.leos.domain.common.Result;
import eu.europa.ec.leos.services.label.ref.Ref;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;

/** Article 46 used for the unnumbered paragraph:
 * first paragraph (a5_sdplN0)
 *      sub-paragraph (a5_Bxi62k)
 *          (a) point  (a5_fQYvU7)
 * 		    (b) point  (a5_lwMbkL)
 *          (c) point  (a5_v0SBeN)
 * 		    (d) point  (a5_bSsbTj)
 *              sub-point  (a5_T0L37f)
 *              (1) point  (a5_TcYL6X)
 * 			    (2) point  (a5_Vdotsh)
 *              (3) point  (a5_jlazVX)
 *                  sub-point   (a5_nmQFmM)
 *                  (i) point   (a5_HTmAdx)
 * 				    (ii) point  (a5_oFgu8x)
 *                  (iii) point (a5_W9FxgJ)
 * 				    (iv) point  (a5_Pxc9VZ)
 *                      sub-point (a5_csTSSU)
 * 					    - indent  (a5_A8TAMj)
 * 					    - indent  (a5_51QZD5)
 * 					    - indent  (a5_IktngU)
 * 					(v) point   (a5_2Y0ygz)
 * 			            sub-point (a5_JXc2nY)
 * 			            - indent  (a5_nirQOI)
 * 			            - indent  (a5_4Lmhzh)
 * second paragraph (a5_FeJW6z)
 * third paragraph  (art_5_TxunI0)
 * fourth paragraph (art_5_A42pW6)
 * fifth paragraph  (art_5_BGt5eN)
 */
public class ReferenceLabelServiceUnnumberedTest extends ReferenceLabelServiceTest {

    @Test
    public void generateLabel_sameArticle_sourceParagraph2_targetParagraph2() throws Exception {
        Result<String> result = referenceLabelGenerator.generateLabel(Arrays.asList(new Ref("","a5_FeJW6z", "bill", null)), "bill", "a5_FeJW6z",  document, false);
        String expectedResults ="this paragraph";

        assertEquals(expectedResults, result.get());
    }

    @Test
    public void generateLabel_sameArticle_sourceParagraph2_targetParagraph234() throws Exception {
        Result<String> result = referenceLabelGenerator.generateLabel(Arrays.asList(new Ref("","a5_FeJW6z", "bill", null), new Ref("","art_5_TxunI0", "bill", null), new Ref("","art_5_A42pW6", "bill", null)), "bill", "a5_FeJW6z",  document, false);
        String expectedResults ="<ref href=\"bill/a5_FeJW6z\" xml:id=\"\">second</ref>"
                + ", <ref href=\"bill/art_5_TxunI0\" xml:id=\"\">third</ref>"
                + " and <ref href=\"bill/art_5_A42pW6\" xml:id=\"\">fourth</ref>"
                + " paragraphs";

        assertEquals(expectedResults, result.get());
    }

    @Test
    public void generateLabel_sameArticle_sourceParagraph2_targetParagraph345() throws Exception {
        Result<String> result = referenceLabelGenerator.generateLabel(Arrays.asList(new Ref("","art_5_TxunI0", "bill", null), new Ref("","art_5_A42pW6", "bill", null), new Ref("","art_5_BGt5eN", "bill", null)), "bill", "a5_FeJW6z",  document, false);
        String expectedResults ="<ref href=\"bill/art_5_TxunI0\" xml:id=\"\">third</ref>"
                + ", <ref href=\"bill/art_5_A42pW6\" xml:id=\"\">fourth</ref>"
                + " and <ref href=\"bill/art_5_BGt5eN\" xml:id=\"\">fifth</ref>"
                + " paragraphs";

        assertEquals(expectedResults, result.get());
    }

    @Test
    public void generateLabel_sameArticle_sourceParagraph2_targetParagraph345_withCapitalTrue() throws Exception {
        Result<String> result = referenceLabelGenerator.generateLabel(Arrays.asList(new Ref("","art_5_TxunI0", "bill", null), new Ref("","art_5_A42pW6", "bill", null), new Ref("","art_5_BGt5eN", "bill", null)), "bill", "a5_FeJW6z",  document, true);
        String expectedResults ="<ref href=\"bill/art_5_TxunI0\" xml:id=\"\">Third</ref>"
                + ", <ref href=\"bill/art_5_A42pW6\" xml:id=\"\">fourth</ref>"
                + " and <ref href=\"bill/art_5_BGt5eN\" xml:id=\"\">fifth</ref>"
                + " paragraphs";

        assertEquals(expectedResults, result.get());
    }

    @Test
    public void generateLabel_sameArticle_sourcePar1PointA_targetPar1PointA() throws Exception {
        Result<String> result = referenceLabelGenerator.generateLabel(Arrays.asList(new Ref("","a5_fQYvU7", "bill", null)), "bill", "a5_fQYvU7", document, false);
        String expectedResults ="this point";

        assertEquals(expectedResults, result.get());
    }

    @Test
    public void generateLabel_sameArticle_sourcePar1PointDSubPoint_targetPar1DSubPoint() throws Exception {
        Result<String> result = referenceLabelGenerator.generateLabel(Arrays.asList(new Ref("","a5_T0L37f", "bill", null)), "bill", "a5_T0L37f", document, false);
        String expectedResults ="this subparagraph";

        assertEquals(expectedResults, result.get());
    }

    @Test
    public void generateLabel_sameArticle_sourcePar1PointD1_targetPar1PointD1() throws Exception {
        Result<String> result = referenceLabelGenerator.generateLabel(Arrays.asList(new Ref("","a5_TcYL6X", "bill", null)), "bill", "a5_TcYL6X", document, false);
        String expectedResults ="this point";

        assertEquals(expectedResults, result.get());
    }

    @Test
    public void generateLabel_sameArticle_sourcePar1PointD3SubPoint_targetPar1PointD3SubPoint() throws Exception {
        Result<String> result = referenceLabelGenerator.generateLabel(Arrays.asList(new Ref("","a5_nmQFmM", "bill", null)), "bill", "a5_nmQFmM", document, false);
        String expectedResults ="this subparagraph";

        assertEquals(expectedResults, result.get());
    }

    @Test
    public void generateLabel_sameArticle_sourcePar1PointD3I_targetPar1PointD3I() throws Exception {
        Result<String> result = referenceLabelGenerator.generateLabel(Arrays.asList(new Ref("","a5_HTmAdx", "bill", null)), "bill", "a5_HTmAdx", document, false);
        String expectedResults ="this point";

        assertEquals(expectedResults, result.get());
    }

    @Test
    public void generateLabel_sameArticle_sourcePar1PointD3I_targetPar1PointD3I_withCapitalTrue() throws Exception {
        Result<String> result = referenceLabelGenerator.generateLabel(Arrays.asList(new Ref("","a5_HTmAdx", "bill", null)), "bill", "a5_HTmAdx", document, true);
        String expectedResults ="This point";

        assertEquals(expectedResults, result.get());
    }

    @Test
    public void generateLabel_sameArticle_sourcePar1PointD3IVSubPoint_targetPar1PointD3IVSubPoint() throws Exception {
        Result<String> result = referenceLabelGenerator.generateLabel(Arrays.asList(new Ref("","a5_csTSSU", "bill", null)), "bill", "a5_csTSSU", document, false);
        String expectedResults ="this subparagraph";

        assertEquals(expectedResults, result.get());
    }

    @Test
    public void generateLabel_sameArticle_sourcePar1PointD3IVSubPoint_targetPar1PointD3IVSubPoint_withCapitalTrue() throws Exception {
        Result<String> result = referenceLabelGenerator.generateLabel(Arrays.asList(new Ref("","a5_csTSSU", "bill", null)), "bill", "a5_csTSSU", document, true);
        String expectedResults ="This subparagraph";

        assertEquals(expectedResults, result.get());
    }

    @Test
    public void generateLabel_sameArticle_sourcePar1PointD3IVIndent_targetPar1PointD3IVIndent() throws Exception {
        Result<String> result = referenceLabelGenerator.generateLabel(Arrays.asList(new Ref("","a5_A8TAMj", "bill", null)), "bill", "a5_A8TAMj", document, false);
        String expectedResults ="this indent";

        assertEquals(expectedResults, result.get());
    }

    @Test
    public void generateLabel_sameArticle_sourcePar1PointD3IVIndent_targetPar1PointD3IVIndent_withCapitalTrue() throws Exception {
        Result<String> result = referenceLabelGenerator.generateLabel(Arrays.asList(new Ref("","a5_A8TAMj", "bill", null)), "bill", "a5_A8TAMj", document, true);
        String expectedResults ="This indent";

        assertEquals(expectedResults, result.get());
    }

    @Test
    public void generateLabel_sameArticle_sameParagraph_sourcePointD_target3siblingsABC() throws Exception {
        Result<String> result = referenceLabelGenerator.generateLabel(Arrays.asList(new Ref("","a5_fQYvU7", "bill", null), new Ref("","a5_lwMbkL", "bill", null), new Ref("","a5_v0SBeN", "bill", null)), "bill", "a5_bSsbTj", document, false);
        String expectedResults = "points <ref href=\"bill/a5_fQYvU7\" xml:id=\"\">(a)</ref>"
                + ", <ref href=\"bill/a5_lwMbkL\" xml:id=\"\">(b)</ref>"
                + " and <ref href=\"bill/a5_v0SBeN\" xml:id=\"\">(c)</ref>";

        assertEquals(expectedResults, result.get());
    }

    @Test
    public void generateLabel_sameArticle_sameParagraph_sourcePointA_targetPointD_chose2Points() throws Exception {
        Result<String> result = referenceLabelGenerator.generateLabel(Arrays.asList(new Ref("","a5_TcYL6X", "bill", null), new Ref("","a5_Vdotsh", "bill", null)), "bill", "a5_fQYvU7", document, false);
        String expectedResults = "points (d)<ref href=\"bill/a5_TcYL6X\" xml:id=\"\">(1)</ref>"
                + " and <ref href=\"bill/a5_Vdotsh\" xml:id=\"\">(2)</ref>";

        assertEquals(expectedResults, result.get());
    }

    @Test
    public void generateLabel_sameArticle_sameParagraph_sourcePointA_targetPointD3_chose3Points() throws Exception {
        Result<String> result = referenceLabelGenerator.generateLabel(Arrays.asList(new Ref("","a5_W9FxgJ", "bill", null), new Ref("","a5_oFgu8x", "bill", null), new Ref("","a5_HTmAdx", "bill", null)), "bill", "a5_fQYvU7",  document, false);
        String expectedResults = "points (d)(3)<ref href=\"bill/a5_HTmAdx\" xml:id=\"\">(i)</ref>"
                + ", <ref href=\"bill/a5_oFgu8x\" xml:id=\"\">(ii)</ref>"
                + " and <ref href=\"bill/a5_W9FxgJ\" xml:id=\"\">(iii)</ref>";

        assertEquals(expectedResults, result.get());
    }

    @Test
    public void generateLabel_sameArticle_sameParagraph_sourcePointA_targetPointD3IV_chose3Indent() throws Exception {
        Result<String> result = referenceLabelGenerator.generateLabel(Arrays.asList(new Ref("","a5_IktngU", "bill", null), new Ref("","a5_51QZD5", "bill", null), new Ref("","a5_A8TAMj", "bill", null)), "bill", "a5_fQYvU7",  document, false);
        String expectedResults = "point (d)(3)(iv)"
                + ", <ref href=\"bill/a5_A8TAMj\" xml:id=\"\">first</ref>"
                + ", <ref href=\"bill/a5_51QZD5\" xml:id=\"\">second</ref>"
                + " and <ref href=\"bill/a5_IktngU\" xml:id=\"\">third</ref>"
                + " indents";

        assertEquals(expectedResults, result.get());
    }

    @Test
    public void generateLabel_sameArticle_sameParagraph_sourcePointD2_targetPointD3IV_chose3Indent() throws Exception {
        Result<String> result = referenceLabelGenerator.generateLabel(Arrays.asList(new Ref("","a5_IktngU", "bill", null), new Ref("","a5_51QZD5", "bill", null), new Ref("","a5_A8TAMj", "bill", null)), "bill", "a5_Vdotsh", document, false);
        String expectedResults = "point (3)(iv)"
                + ", <ref href=\"bill/a5_A8TAMj\" xml:id=\"\">first</ref>"
                + ", <ref href=\"bill/a5_51QZD5\" xml:id=\"\">second</ref>"
                + " and <ref href=\"bill/a5_IktngU\" xml:id=\"\">third</ref>"
                + " indents";

        assertEquals(expectedResults, result.get());
    }

    @Test
    public void generateLabel_sameArticle_sameParagraph_sourcePointD3III_targetPointD3IV_chose3Indent() throws Exception {
        Result<String> result = referenceLabelGenerator.generateLabel(Arrays.asList(new Ref("","a5_IktngU", "bill", null), new Ref("","a5_51QZD5", "bill", null), new Ref("","a5_A8TAMj", "bill", null)), "bill", "a5_W9FxgJ", document, false);
        String expectedResults = "point (iv)"
                + ", <ref href=\"bill/a5_A8TAMj\" xml:id=\"\">first</ref>"
                + ", <ref href=\"bill/a5_51QZD5\" xml:id=\"\">second</ref>"
                + " and <ref href=\"bill/a5_IktngU\" xml:id=\"\">third</ref>"
                + " indents";

        assertEquals(expectedResults, result.get());
    }

    @Test
    public void generateLabel_sameArticle_sameParagraph_sourcePointD3IVIndent_targetPointD3VIndent() throws Exception {
        Result<String> result = referenceLabelGenerator.generateLabel(Arrays.asList(new Ref("","a5_4Lmhzh", "bill", null), new Ref("","a5_nirQOI", "bill", null)), "bill", "a5_A8TAMj", document, false);
        String expectedResults = "point (v)"
                + ", <ref href=\"bill/a5_nirQOI\" xml:id=\"\">first</ref>"
                + " and <ref href=\"bill/a5_4Lmhzh\" xml:id=\"\">second</ref>"
                + " indents";

        assertEquals(expectedResults, result.get());
    }

    @Test
    public void generateLabel_sameArticle_sameParagraph_sourcePointD3IVIndent_target2Sibilings() throws Exception {
        Result<String> result = referenceLabelGenerator.generateLabel(Arrays.asList(new Ref("","a5_51QZD5", "bill", null), new Ref("","a5_IktngU", "bill", null)), "bill", "a5_A8TAMj", document, false);
        String expectedResults = "<ref href=\"bill/a5_51QZD5\" xml:id=\"\">second</ref>"
                + " and <ref href=\"bill/a5_IktngU\" xml:id=\"\">third</ref>"
                + " indents";

        assertEquals(expectedResults, result.get());
    }

    @Test
    public void generateLabel_sameArticle_sameParagraph_sourcePointD3IVIndent_targetPointABC() throws Exception {
        Result<String> result = referenceLabelGenerator.generateLabel(Arrays.asList(new Ref("","a5_fQYvU7", "bill", null), new Ref("","a5_lwMbkL", "bill", null), new Ref("","a5_v0SBeN", "bill", null)), "bill", "a5_A8TAMj", document, false);
        String expectedResults = "points"
                + " <ref href=\"bill/a5_fQYvU7\" xml:id=\"\">(a)</ref>"
                + ", <ref href=\"bill/a5_lwMbkL\" xml:id=\"\">(b)</ref>"
                + " and <ref href=\"bill/a5_v0SBeN\" xml:id=\"\">(c)</ref>";

        assertEquals(expectedResults, result.get());
    }

    @Test
    public void generateLabel_sameArticle_sameParagraph_sourcePointD3IVIndent_targetPointD12() throws Exception {
        Result<String> result = referenceLabelGenerator.generateLabel(Arrays.asList(new Ref("","a5_TcYL6X", "bill", null), new Ref("","a5_Vdotsh", "bill", null)), "bill", "a5_A8TAMj", document, false);
        String expectedResults = "points"
                + " <ref href=\"bill/a5_TcYL6X\" xml:id=\"\">(1)</ref>"
                + " and <ref href=\"bill/a5_Vdotsh\" xml:id=\"\">(2)</ref>";

        assertEquals(expectedResults, result.get());
    }

    @Test

    public void generateLabel_sameArticle_sameParagraph_sourcePointD3IVIndent_targetPointD3_I_II_III() throws Exception {
        Result<String> result = referenceLabelGenerator.generateLabel(Arrays.asList(new Ref("","a5_W9FxgJ", "bill", null), new Ref("","a5_oFgu8x", "bill", null), new Ref("","a5_HTmAdx", "bill", null)), "bill", "a5_A8TAMj", document, false);
        String expectedResults = "points <ref href=\"bill/a5_HTmAdx\" xml:id=\"\">(i)</ref>"
                + ", <ref href=\"bill/a5_oFgu8x\" xml:id=\"\">(ii)</ref>"
                + " and <ref href=\"bill/a5_W9FxgJ\" xml:id=\"\">(iii)</ref>";

        assertEquals(expectedResults, result.get());
    }

    // Alinea tests
    // source indent, test all upper Alineas
    @Test
    public void generateLabel_sameArticle_sameParagraph_sourcePointD3IVIndent_targetPointD3IVSubPoint() throws Exception {
        Result<String> result = referenceLabelGenerator.generateLabel(Arrays.asList(new Ref("","a5_csTSSU", "bill", null)), "bill", "a5_A8TAMj", document, false);
        String expectedResults = "<ref href=\"bill/a5_csTSSU\" xml:id=\"\">first</ref>"
                + " subparagraph";

        assertEquals(expectedResults, result.get());
    }

    @Test
    public void generateLabel_sameArticle_sameParagraph_sourcePointD3IVIndent_targetPointD3IVSubPoint_withCapitaltrue() throws Exception {
        Result<String> result = referenceLabelGenerator.generateLabel(Arrays.asList(new Ref("","a5_csTSSU", "bill", null)), "bill", "a5_A8TAMj", document, true);
        String expectedResults = "<ref href=\"bill/a5_csTSSU\" xml:id=\"\">First</ref>"
                + " subparagraph";

        assertEquals(expectedResults, result.get());
    }

    @Test
    public void generateLabel_sameArticle_sameParagraph_sourcePointD3IVIndent_targetPointD3IV() throws Exception {
        Result<String> result = referenceLabelGenerator.generateLabel(Arrays.asList(new Ref("","a5_Pxc9VZ", "bill", null)), "bill", "a5_A8TAMj", document, false);
        String expectedResults = "this point";

        assertEquals(expectedResults, result.get());
    }

    @Ignore //same behaviour is actually happening. Waiting to discuss with business how to treat sub-points(alinea)
    @Test
    public void generateLabel_sameArticle_sameParagraph_sourcePointD3IVIndent_targetPointD3SubPoint() throws Exception {
        Result<String> result = referenceLabelGenerator.generateLabel(Arrays.asList(new Ref("","a5_nmQFmM", "bill", null)), "bill", "a5_A8TAMj", document, false);
        String expectedResults = "point <ref href=\"bill/a5_jlazVX\" xml:id=\"\">(3)</ref>"
                + ", <ref href=\"bill/a5_nmQFmM\" xml:id=\"\">first</ref> subparagraph";

        assertEquals(expectedResults, result.get());
    }

    @Test
    public void generateLabel_sameArticle_sameParagraph_sourcePointD3IVIndent_targetPointD3() throws Exception {
        Result<String> result = referenceLabelGenerator.generateLabel(Arrays.asList(new Ref("","a5_jlazVX", "bill", null)), "bill", "a5_A8TAMj", document, false);
        String expectedResults = "point <ref href=\"bill/a5_jlazVX\" xml:id=\"\">(3)</ref>";

        assertEquals(expectedResults, result.get());
    }

    @Ignore //same behaviour is actually happening. Waiting to discuss with business how to treat sub-points(alinea)
    @Test
    public void generateLabel_sameArticle_sameParagraph_sourcePointD3IVIndent_targetPointDSubPoint() throws Exception {
        Result<String> result = referenceLabelGenerator.generateLabel(Arrays.asList(new Ref("","a5_T0L37f", "bill", null)), "bill", "a5_A8TAMj", document, false);
        String expectedResults = "point <ref href=\"bill/a5_bSsbTj\" xml:id=\"\">(d)</ref>"
                + ", <ref href=\"bill/a5_T0L37f\" xml:id=\"\">first</ref> subparagraph";

        assertEquals(expectedResults, result.get());
    }

    @Test
    public void generateLabel_sameArticle_sameParagraph_sourcePointD3IVIndent_targetPointD() throws Exception {
        Result<String> result = referenceLabelGenerator.generateLabel(Arrays.asList(new Ref("","a5_bSsbTj", "bill", null)), "bill", "a5_A8TAMj", document, false);
        String expectedResults = "point <ref href=\"bill/a5_bSsbTj\" xml:id=\"\">(d)</ref>";

        assertEquals(expectedResults, result.get());
    }
    // end source indent, test all upper Alineas

    // source point (a), test all lower Alineas
    @Test
    public void generateLabel_sameArticle_sameParagraph_sourcePointA_targetPointD3IVSubPoint() throws Exception {
        Result<String> result = referenceLabelGenerator.generateLabel(Arrays.asList(new Ref("","a5_csTSSU", "bill", null)), "bill", "a5_fQYvU7", document, false);
        String expectedResults = "point (d)(3)(iv)"
                + ", <ref href=\"bill/a5_csTSSU\" xml:id=\"\">first</ref> subparagraph";

        assertEquals(expectedResults, result.get());
    }

    @Test
    public void generateLabel_sameArticle_sameParagraph_sourcePointA_targetPointD3IV() throws Exception {
        Result<String> result = referenceLabelGenerator.generateLabel(Arrays.asList(new Ref("","a5_Pxc9VZ", "bill", null)), "bill", "a5_fQYvU7", document, false);
        String expectedResults = "point (d)(3)"
                + "<ref href=\"bill/a5_Pxc9VZ\" xml:id=\"\">(iv)</ref>";

        assertEquals(expectedResults, result.get());
    }

    @Test
    public void generateLabel_sameArticle_sameParagraph_sourcePointA_targetPointD3SubPoint() throws Exception {
        Result<String> result = referenceLabelGenerator.generateLabel(Arrays.asList(new Ref("","a5_nmQFmM", "bill", null)), "bill", "a5_fQYvU7", document, false);
        String expectedResults = "point (d)(3)"
                + ", <ref href=\"bill/a5_nmQFmM\" xml:id=\"\">first</ref> subparagraph";

        assertEquals(expectedResults, result.get());
    }

    @Test
    public void generateLabel_sameArticle_sameParagraph_sourcePointA_targetPointD3() throws Exception {
        Result<String> result = referenceLabelGenerator.generateLabel(Arrays.asList(new Ref("","a5_jlazVX", "bill", null)), "bill", "a5_fQYvU7", document, false);
        String expectedResults = "point (d)"
                + "<ref href=\"bill/a5_jlazVX\" xml:id=\"\">(3)</ref>";

        assertEquals(expectedResults, result.get());
    }

    @Test
    public void generateLabel_sameArticle_sameParagraph_sourcePointA_targetPointDSubPoint() throws Exception {
        Result<String> result = referenceLabelGenerator.generateLabel(Arrays.asList(new Ref("","a5_T0L37f", "bill", null)), "bill", "a5_fQYvU7", document, false);
        String expectedResults = "point (d)"
                + ", <ref href=\"bill/a5_T0L37f\" xml:id=\"\">first</ref> subparagraph";

        assertEquals(expectedResults, result.get());
    }

    @Test
    public void generateLabel_sameArticle_sameParagraph_sourcePointA_targetPointD() throws Exception {
        Result<String> result = referenceLabelGenerator.generateLabel(Arrays.asList(new Ref("","a5_bSsbTj", "bill", null)), "bill", "a5_fQYvU7", document, false);
        String expectedResults = "point"
                + " <ref href=\"bill/a5_bSsbTj\" xml:id=\"\">(d)</ref>";

        assertEquals(expectedResults, result.get());
    }

    @Test
    public void generateLabel_sameArticle_sameParagraph_sourcePointA_targetPointD_withCapitalTrue() throws Exception {
        Result<String> result = referenceLabelGenerator.generateLabel(Arrays.asList(new Ref("","a5_bSsbTj", "bill", null)), "bill", "a5_fQYvU7", document, true);
        String expectedResults = "Point"
                + " <ref href=\"bill/a5_bSsbTj\" xml:id=\"\">(d)</ref>";

        assertEquals(expectedResults, result.get());
    }

    @Test
    public void generateLabel_sameArticle_sameParagraph_sourcePointA_targetPointD_withCapitalTrueAndLEOSProperty() throws Exception {
        Result<String> result = referenceLabelGenerator.generateLabel(Arrays.asList(new Ref("","a5_bSsbTj", "bill", "ec")), "bill", "a5_fQYvU7", document, true);
        String expectedResults = "Point"
                + " <ref href=\"bill/a5_bSsbTj\" leos:origin=\"ec\" xml:id=\"\">(d)</ref>";

        assertEquals(expectedResults, result.get());
    }
    //end aliena

    @Test
    public void generateLabel_sameArticle_sameParagraph_sourcePointA_targetSubParagraph1() throws Exception {
        Result<String> result = referenceLabelGenerator.generateLabel(Arrays.asList(new Ref("","a5_Bxi62k", "bill", null)), "bill", "a5_fQYvU7", document, false);
        String expectedResults ="<ref href=\"bill/a5_Bxi62k\" xml:id=\"\">first</ref>"
                + " subparagraph";

        assertEquals(expectedResults, result.get());
    }

    @Test
    public void generateLabel_sameArticle_sourceParagraph2_targetParagraph1SubParagraph1() throws Exception {
        Result<String> result = referenceLabelGenerator.generateLabel(Arrays.asList(new Ref("","a5_Bxi62k", "bill", null)), "bill","a5_FeJW6z", document, false);
        String expectedResults ="first paragraph"
                + ", <ref href=\"bill/a5_Bxi62k\" xml:id=\"\">first</ref> subparagraph";

        assertEquals(expectedResults, result.get());
    }

    @Test
    public void generateLabel_sameArticle_sourceParagraph2_targetParagraph1_chose3Indent() throws Exception {
        Result<String> result = referenceLabelGenerator.generateLabel(Arrays.asList(new Ref("","a5_A8TAMj", "bill", null), new Ref("","a5_51QZD5", "bill", null), new Ref("","a5_IktngU", "bill", null)), "bill", "a5_FeJW6z", document, false);
        String expectedResults ="first paragraph"
                + ", point (d)(3)(iv)"
                + ", <ref href=\"bill/a5_A8TAMj\" xml:id=\"\">first</ref>"
                + ", <ref href=\"bill/a5_51QZD5\" xml:id=\"\">second</ref>"
                + " and <ref href=\"bill/a5_IktngU\" xml:id=\"\">third</ref>"
                + " indents";

        assertEquals(expectedResults, result.get());
    }

    @Test
    public void generateLabel_sameArticle_sourceParagraph2_targetParagraph1_chose3Indent_withCapitalTrue() throws Exception {
        Result<String> result = referenceLabelGenerator.generateLabel(Arrays.asList(new Ref("","a5_A8TAMj", "bill", null), new Ref("","a5_51QZD5", "bill", null), new Ref("","a5_IktngU", "bill", null)), "bill", "a5_FeJW6z", document, true);
        String expectedResults ="First paragraph"
                + ", point (d)(3)(iv)"
                + ", <ref href=\"bill/a5_A8TAMj\" xml:id=\"\">first</ref>"
                + ", <ref href=\"bill/a5_51QZD5\" xml:id=\"\">second</ref>"
                + " and <ref href=\"bill/a5_IktngU\" xml:id=\"\">third</ref>"
                + " indents";

        assertEquals(expectedResults, result.get());
    }

    @Test
    public void generateLabel_differentArticle_targetParagraph234() throws Exception {
        Result<String> result = referenceLabelGenerator.generateLabel(Arrays.asList(new Ref("","a5_FeJW6z", "bill", null), new Ref("","art_5_TxunI0", "bill", null), new Ref("","art_5_A42pW6", "bill", null)), "bill", "art_1_A42pW6",document, false);
        String expectedResults ="Article 46"
                + ", <ref href=\"bill/a5_FeJW6z\" xml:id=\"\">second</ref>"
                + ", <ref href=\"bill/art_5_TxunI0\" xml:id=\"\">third</ref>"
                + " and <ref href=\"bill/art_5_A42pW6\" xml:id=\"\">fourth</ref>"
                + " paragraphs";

        assertEquals(expectedResults, result.get());
    }

    @Test
    public void generateLabel_differentArticle_targetParagraph1PointABC() throws Exception {
        Result<String> result = referenceLabelGenerator.generateLabel(Arrays.asList(new Ref("","a5_v0SBeN", "bill", null), new Ref("","a5_lwMbkL", "bill", null), new Ref("","a5_fQYvU7", "bill", null)), "bill", "art_1_A42pW6",document, false);
        String expectedResults ="Article 46"
                + ", first paragraph"
                + ", points <ref href=\"bill/a5_fQYvU7\" xml:id=\"\">(a)</ref>"
                + ", <ref href=\"bill/a5_lwMbkL\" xml:id=\"\">(b)</ref>"
                + " and <ref href=\"bill/a5_v0SBeN\" xml:id=\"\">(c)</ref>";

        assertEquals(expectedResults, result.get());
    }

    @Test
    public void generateLabel_differentArticle_targetParagraph1SubParagraph() throws Exception {
        Result<String> result = referenceLabelGenerator.generateLabel(Arrays.asList(new Ref("","a5_Bxi62k", "bill", null)), "bill", "art_1_A42pW6", document, false);
        String expectedResults ="Article 46, first paragraph"
                + ", <ref href=\"bill/a5_Bxi62k\" xml:id=\"\">first</ref> subparagraph";

        assertEquals(expectedResults, result.get());
    }

    @Test
    public void generateLabel_differentArticle_targetParagraph1PointDSubPoint() throws Exception {
        Result<String> result = referenceLabelGenerator.generateLabel(Arrays.asList(new Ref("","a5_T0L37f", "bill", null)), "bill", "art_1_A42pW6", document, false);
        String expectedResults ="Article 46, first paragraph, point (d)"
                + ", <ref href=\"bill/a5_T0L37f\" xml:id=\"\">first</ref>"
                + " subparagraph";

        assertEquals(expectedResults, result.get());
    }

    @Test
    public void generateLabel_differentArticle_targetParagraph1_chose3Indent() throws Exception {
        Result<String> result = referenceLabelGenerator.generateLabel(Arrays.asList(new Ref("","a5_A8TAMj", "bill", null), new Ref("","a5_51QZD5", "bill", null), new Ref("","a5_IktngU", "bill", null)), "bill", "art_1_A42pW6", document, false);
        String expectedResults ="Article 46"
                + ", first paragraph"
                + ", point (d)(3)(iv)"
                + ", <ref href=\"bill/a5_A8TAMj\" xml:id=\"\">first</ref>"
                + ", <ref href=\"bill/a5_51QZD5\" xml:id=\"\">second</ref>"
                + " and <ref href=\"bill/a5_IktngU\" xml:id=\"\">third</ref>"
                + " indents";

        assertEquals(expectedResults, result.get());
    }

    @Test
    public void generateLabel_differentArticle_targetParagraph1_chose2Indent() throws Exception {
        Result<String> result = referenceLabelGenerator.generateLabel(Arrays.asList(new Ref("","a5_51QZD5", "bill", null), new Ref("","a5_IktngU", "bill", null)), "bill", "", document, false);
        String expectedResults ="Article 46"
                + ", first paragraph"
                + ", point (d)(3)(iv)"
                + ", <ref href=\"bill/a5_51QZD5\" xml:id=\"\">second</ref>"
                + " and <ref href=\"bill/a5_IktngU\" xml:id=\"\">third</ref>"
                + " indents";
        Assert.assertEquals(expectedResults, result.get());
    }

    @Test
    public void generateLabel_differentArticle_targetParagraph1_chose1Indent() throws Exception {
        Result<String> result = referenceLabelGenerator.generateLabel(Arrays.asList(new Ref("","a5_IktngU", "bill", null)), "bill", "", document, false);
        String expectedResults ="Article 46"
                + ", first paragraph"
                + ", point (d)(3)(iv)"
                + ", <ref href=\"bill/a5_IktngU\" xml:id=\"\">third</ref>"
                + " indent";
        Assert.assertEquals(expectedResults, result.get());
    }



}
