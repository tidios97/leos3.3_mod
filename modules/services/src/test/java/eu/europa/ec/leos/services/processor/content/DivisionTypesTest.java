package eu.europa.ec.leos.services.processor.content;

import eu.europa.ec.leos.domain.common.TocMode;
import eu.europa.ec.leos.services.util.TestUtils;
import eu.europa.ec.leos.vo.toc.TableOfContentItemVO;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mockito;

import java.util.List;

import static eu.europa.ec.leos.services.TestVOCreatorUtils.getJaneTestUser;
import static eu.europa.ec.leos.services.support.XmlHelper.DIVISION;
import static eu.europa.ec.leos.services.processor.content.TableOfContentProcessor.updateStyleClassOfTocItems;
import static eu.europa.ec.leos.services.util.TestUtils.squeezeXml;
import static org.junit.Assert.assertEquals;

public class DivisionTypesTest extends TableOfXmlContentProcessorTest {

    @InjectMocks
    private TableOfContentProcessor tableOfContentProcessor = Mockito.spy(new TableOfContentProcessorImpl());
    @InjectMocks
    protected XmlContentProcessor xmlContentProcessor = Mockito.spy(new XmlContentProcessorMandate());

    private final static String DIVISION_NUMBERING = "/numbering/explanatory/division";

    @Override
    protected void getStructureFile() {
        docTemplate = "CE-003";
        configFile = "/structure-test-division-explanatory-CN.xml";
    }

    @Test
    public void test_updateStyleClassOfToc_division_addType1BetweenType1AndType2() {
        byte[] testXml = TestUtils.getFileContent(DIVISION_NUMBERING, "/test_updateStyleClassOfToc_division_add.xml");
        List<TableOfContentItemVO> tableOfContentItemVOList = tableOfContentProcessor
                .buildTableOfContent("doc", testXml, TocMode.SIMPLIFIED).get(0).getChildItems();
        updateStyleClassOfTocItems(tableOfContentItemVOList, DIVISION);
        byte[] resultXml = xmlContentProcessor.createDocumentContentWithNewTocList(tableOfContentItemVOList, testXml, getJaneTestUser());
        String resultStr = new String(resultXml, UTF_8);
        byte[] expected = TestUtils.getFileContent(DIVISION_NUMBERING + "/test_updateStyleClassOfToc_division_add_expected.xml");
        String expectedStr = new String(expected, UTF_8);
        expectedStr = squeezeXml(expectedStr);
        resultStr = squeezeXml(resultStr);
        assertEquals(expectedStr, resultStr);
    }

    @Test
    public void test_updateStyleClassOfToc_division_addType1BeforeType4() {
        byte[] testXml = TestUtils.getFileContent(DIVISION_NUMBERING, "/test_updateStyleClassOfToc_division_addTyp1BeforeType4.xml");
        List<TableOfContentItemVO> tableOfContentItemVOList = tableOfContentProcessor
                .buildTableOfContent("doc", testXml, TocMode.SIMPLIFIED).get(0).getChildItems();
        updateStyleClassOfTocItems(tableOfContentItemVOList, DIVISION);
        byte[] resultXml = xmlContentProcessor.createDocumentContentWithNewTocList(tableOfContentItemVOList, testXml, getJaneTestUser());
        String resultStr = new String(resultXml, UTF_8);
        byte[] expected = TestUtils.getFileContent(DIVISION_NUMBERING + "/test_updateStyleClassOfToc_division_addTyp1BeforeType4_expected.xml");
        String expectedStr = new String(expected, UTF_8);
        expectedStr = squeezeXml(expectedStr);
        resultStr = squeezeXml(resultStr);
        assertEquals(expectedStr, resultStr);
    }

    @Test
    public void test_updateStyleClassOfToc_division_addType1BetweenType2AndType3() {
        byte[] testXml = TestUtils.getFileContent(DIVISION_NUMBERING, "/test_updateStyleClassOfToc_division_addTyp1BetweenType2AndType3.xml");
        List<TableOfContentItemVO> tableOfContentItemVOList = tableOfContentProcessor
                .buildTableOfContent("doc", testXml, TocMode.SIMPLIFIED).get(0).getChildItems();
        updateStyleClassOfTocItems(tableOfContentItemVOList, DIVISION);
        byte[] resultXml = xmlContentProcessor.createDocumentContentWithNewTocList(tableOfContentItemVOList, testXml, getJaneTestUser());
        String resultStr = new String(resultXml, UTF_8);
        byte[] expected = TestUtils.getFileContent(DIVISION_NUMBERING + "/test_updateStyleClassOfToc_division_addTyp1BetweenType2AndType3_expected.xml");
        String expectedStr = new String(expected, UTF_8);
        expectedStr = squeezeXml(expectedStr);
        resultStr = squeezeXml(resultStr);
        assertEquals(expectedStr, resultStr);
    }

    /**
     * Removed as below
     * <division xml:id="body_division_8" class="type_4">  --->  Moved here
     * <division xml:id="body_division_1" class="type_1">
     * <division xml:id="body_division_2" class="type_1">
     * <division xml:id="body_division_3" class="type_2">
     * <division xml:id="body_division_4" class="type_1">
     * <division xml:id="body_division_5" class="type_2">
     * <division xml:id="body_division_6" class="type_3">
     * <division xml:id="body_division_7" class="type_4">
     * <division xml:id="body_division_8" class="type_4">  --->  Moved
     * <division xml:id="body_division_9" class="type_1">
     * <division xml:id="body_division_10" class="type_1">
     *
     * ---------------Result-----------------------------
     * <division xml:id="body_division_8" class="type_1">  ---> changed to 1
     * <division xml:id="body_division_1" class="type_1">
     * <division xml:id="body_division_2" class="type_1">
     * <division xml:id="body_division_3" class="type_2">
     * <division xml:id="body_division_4" class="type_1">
     * <division xml:id="body_division_5" class="type_2">
     * <division xml:id="body_division_6" class="type_3">
     * <division xml:id="body_division_9" class="type_1">
     * <division xml:id="body_division_7" class="type_2">
     */
    @Test
    public void test_updateStyleClassOfToc_division_move_At_Top() {
        byte[] testXml = TestUtils.getFileContent(DIVISION_NUMBERING, "/test_updateStyleClassOfToc_division_move_At_Top.xml");
        List<TableOfContentItemVO> tableOfContentItemVOList = tableOfContentProcessor
                .buildTableOfContent("doc", testXml, TocMode.SIMPLIFIED).get(0).getChildItems();
        updateStyleClassOfTocItems(tableOfContentItemVOList, DIVISION);
        byte[] resultXml = xmlContentProcessor.createDocumentContentWithNewTocList(tableOfContentItemVOList, testXml, getJaneTestUser());
        String resultStr = new String(resultXml, UTF_8);
        byte[] expected = TestUtils.getFileContent(DIVISION_NUMBERING + "/test_updateStyleClassOfToc_division_move_At_Top_expected.xml");
        String expectedStr = new String(expected, UTF_8);
        expectedStr = squeezeXml(expectedStr);
        resultStr = squeezeXml(resultStr);
        assertEquals(expectedStr, resultStr);
    }

    /**
     * Removed as below
     * <division xml:id="body_division_1" class="type_4"> (removed its 3 ancestors)
     * <division xml:id="body_division_2" class="type_1">
     * <division xml:id="body_division_3" class="type_2">
     * <division xml:id="body_division_4" class="type_1">
     * <division xml:id="body_division_5" class="type_2">
     * <division xml:id="body_division_6" class="type_3">
     * <division xml:id="body_division_7" class="type_4">
     * <division xml:id="body_division_8" class="type_4">  --->  Moved
     * <division xml:id="body_division_9" class="type_1">
     * <division xml:id="body_division_10" class="type_1">
     * <division xml:id="body_division_8" class="type_4">  --->  Moved here
     *
     * ---------------Result-----------------------------
     * <division xml:id="body_division_1" class="type_1"> --->  Changed
     * <division xml:id="body_division_2" class="type_1">
     * <division xml:id="body_division_3" class="type_2">
     * <division xml:id="body_division_4" class="type_1">
     * <division xml:id="body_division_5" class="type_2">
     * <division xml:id="body_division_6" class="type_3">
     * <division xml:id="body_division_7" class="type_4">
     * <division xml:id="body_division_9" class="type_1">
     * <division xml:id="body_division_10" class="type_1">
     * <division xml:id="body_division_8" class="type_2">  --->  changed to 2
     */
    @Test
    public void test_updateStyleClassOfToc_division_move_At_Last() {
        byte[] testXml = TestUtils.getFileContent(DIVISION_NUMBERING, "/test_updateStyleClassOfToc_division_move_at_last.xml");
        List<TableOfContentItemVO> tableOfContentItemVOList = tableOfContentProcessor
                .buildTableOfContent("doc", testXml, TocMode.SIMPLIFIED).get(0).getChildItems();
        updateStyleClassOfTocItems(tableOfContentItemVOList, DIVISION);
        byte[] resultXml = xmlContentProcessor.createDocumentContentWithNewTocList(tableOfContentItemVOList, testXml, getJaneTestUser());
        String resultStr = new String(resultXml, UTF_8);
        byte[] expected = TestUtils.getFileContent(DIVISION_NUMBERING + "/test_updateStyleClassOfToc_division_move_at_last_expected.xml");
        String expectedStr = new String(expected, UTF_8);
        expectedStr = squeezeXml(expectedStr);
        resultStr = squeezeXml(resultStr);
        assertEquals(expectedStr, resultStr);
    }

    /**
     * <division xml:id="body_division_1" class="type_1"> --->  Moved
     * <division xml:id="body_division_2" class="type_2">
     * <division xml:id="body_division_1" class="type_1"> --->  Moved here
     * <division xml:id="body_division_3" class="type_1">
     *
     * ---------------Result-----------------------------
     * <division xml:id="body_division_2" class="type_1"> --->  changed to 1
     * <division xml:id="body_division_1" class="type_1">
     * <division xml:id="body_division_3" class="type_1">
     */
    @Test
    public void test_updateStyleClassOfToc_division_move_theTop() {
        byte[] testXml = TestUtils.getFileContent(DIVISION_NUMBERING, "/test_updateStyleClassOfToc_division_move_theTop.xml");
        List<TableOfContentItemVO> tableOfContentItemVOList = tableOfContentProcessor
                .buildTableOfContent("doc", testXml, TocMode.SIMPLIFIED).get(0).getChildItems();
        updateStyleClassOfTocItems(tableOfContentItemVOList, DIVISION);
        byte[] resultXml = xmlContentProcessor.createDocumentContentWithNewTocList(tableOfContentItemVOList, testXml, getJaneTestUser());
        String resultStr = new String(resultXml, UTF_8);
        byte[] expected = TestUtils.getFileContent(DIVISION_NUMBERING + "/test_updateStyleClassOfToc_division_move_theTop_expected.xml");
        String expectedStr = new String(expected, UTF_8);
        expectedStr = squeezeXml(expectedStr);
        resultStr = squeezeXml(resultStr);
        assertEquals(expectedStr, resultStr);
    }

    /**
     * Removed as below
     * <division xml:id="body_division_2" class="type_1"> --> X Removed
     * <division xml:id="body_division_3" class="type_2">
     * <division xml:id="body_division_4" class="type_1">
     * <division xml:id="body_division_5" class="type_2">
     * <division xml:id="body_division_6" class="type_3">
     * <division xml:id="body_division_7" class="type_4">
     * <division xml:id="body_division_9" class="type_1">
     * <division xml:id="body_division_10" class="type_1">
     *
     * ---------------Result-----------------------------
     * <division xml:id="body_division_3" class="type_1">  ---> changed to 1
     * <division xml:id="body_division_4" class="type_1">
     * <division xml:id="body_division_5" class="type_2">
     * <division xml:id="body_division_6" class="type_3">
     * <division xml:id="body_division_7" class="type_4">
     * <division xml:id="body_division_9" class="type_1">
     * <division xml:id="body_division_10" class="type_1">
     */
    @Test
    public void test_updateStyleClassOfToc_division_remove_topType1() {
        byte[] testXml = TestUtils.getFileContent(DIVISION_NUMBERING, "/test_updateStyleClassOfToc_division_remove_topType1.xml");
        List<TableOfContentItemVO> tableOfContentItemVOList = tableOfContentProcessor
                .buildTableOfContent("doc", testXml, TocMode.SIMPLIFIED).get(0).getChildItems();
        updateStyleClassOfTocItems(tableOfContentItemVOList, DIVISION);
        byte[] resultXml = xmlContentProcessor.createDocumentContentWithNewTocList(tableOfContentItemVOList, testXml, getJaneTestUser());
        String resultStr = new String(resultXml, UTF_8);
        byte[] expected = TestUtils.getFileContent(DIVISION_NUMBERING + "/test_updateStyleClassOfToc_division_remove_topType1_expected.xml");
        String expectedStr = new String(expected, UTF_8);
        expectedStr = squeezeXml(expectedStr);
        resultStr = squeezeXml(resultStr);
        assertEquals(expectedStr, resultStr);
    }

    /**
     * Removed as below
     * <division xml:id="body_division_1" class="type_1">
     * <division xml:id="body_division_2" class="type_1">
     * <division xml:id="body_division_3" class="type_2">
     * <division xml:id="body_division_4" class="type_1">
     * <division xml:id="body_division_5" class="type_2">
     * <division xml:id="body_division_6" class="type_3"> ---> X Removed
     * <division xml:id="body_division_7" class="type_4">
     * <division xml:id="body_division_8" class="type_4">
     * <division xml:id="body_division_9" class="type_1">
     * <division xml:id="body_division_10" class="type_1">
     *
     * ---------------Result-----------------------------
     * <division xml:id="body_division_1" class="type_1">
     * <division xml:id="body_division_2" class="type_1">
     * <division xml:id="body_division_3" class="type_2">
     * <division xml:id="body_division_4" class="type_1">
     * <division xml:id="body_division_5" class="type_2">
     * <division xml:id="body_division_7" class="type_3"> --> changed to 3
     * <division xml:id="body_division_8" class="type_4">
     * <division xml:id="body_division_9" class="type_1">
     * <division xml:id="body_division_10" class="type_1">
     */
    @Test
    public void test_updateStyleClassOfToc_division_remove_middleType3() {
        byte[] testXml = TestUtils.getFileContent(DIVISION_NUMBERING, "/test_updateStyleClassOfToc_division_remove.xml");
        List<TableOfContentItemVO> tableOfContentItemVOList = tableOfContentProcessor
                .buildTableOfContent("doc", testXml, TocMode.SIMPLIFIED).get(0).getChildItems();
        updateStyleClassOfTocItems(tableOfContentItemVOList, DIVISION);
        for(TableOfContentItemVO i : tableOfContentItemVOList) {
            System.out.println(i);
        }
        byte[] resultXml = xmlContentProcessor.createDocumentContentWithNewTocList(tableOfContentItemVOList, testXml, getJaneTestUser());
        String resultStr = new String(resultXml, UTF_8);
        byte[] expected = TestUtils.getFileContent(DIVISION_NUMBERING + "/test_updateStyleClassOfToc_division_remove_expected.xml");
        String expectedStr = new String(expected, UTF_8);
        expectedStr = squeezeXml(expectedStr);
        resultStr = squeezeXml(resultStr);
        assertEquals(expectedStr, resultStr);
    }

    /**
     * <division class="type_5">
     * <division class="type_1">
     * <division class="type_3">
     * <division class="type_1">
     * <division class="type_7">
     *
     * ---------------Result-----------------------------
     * <division class="type_1">
     * <division class="type_1">
     * <division class="type_2">
     * <division class="type_1">
     * <division class="type_1">
     */
    @Test
    public void test_updateStyleClassOfToc_division_correctWrongStructure() {
        byte[] testXml = TestUtils.getFileContent(DIVISION_NUMBERING, "/test_updateStyleClassOfToc_division_correctWrongStructure.xml");
        List<TableOfContentItemVO> tableOfContentItemVOList = tableOfContentProcessor
                .buildTableOfContent("doc", testXml, TocMode.SIMPLIFIED).get(0).getChildItems();
        updateStyleClassOfTocItems(tableOfContentItemVOList, DIVISION);
        byte[] resultXml = xmlContentProcessor.createDocumentContentWithNewTocList(tableOfContentItemVOList, testXml, getJaneTestUser());
        String resultStr = new String(resultXml, UTF_8);
        byte[] expected = TestUtils.getFileContent(DIVISION_NUMBERING + "/test_updateStyleClassOfToc_division_correctWrongStructure_expected.xml");
        String expectedStr = new String(expected, UTF_8);
        expectedStr = squeezeXml(expectedStr);
        resultStr = squeezeXml(resultStr);
        assertEquals(expectedStr, resultStr);
    }
}
