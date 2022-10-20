package eu.europa.ec.leos.services.processor.content;

import eu.europa.ec.leos.domain.common.TocMode;
import eu.europa.ec.leos.i18n.LanguageHelper;
import eu.europa.ec.leos.model.user.Entity;
import eu.europa.ec.leos.model.user.User;
import eu.europa.ec.leos.security.AuthenticatedUser;
import eu.europa.ec.leos.services.processor.content.indent.IndentConversionHelper;
import eu.europa.ec.leos.services.template.TemplateStructureService;
import eu.europa.ec.leos.services.support.XercesUtils;
import eu.europa.ec.leos.services.toc.StructureContext;
import eu.europa.ec.leos.services.toc.StructureServiceImpl;
import eu.europa.ec.leos.services.util.TestUtils;
import eu.europa.ec.leos.services.validation.handlers.AkomantosoXsdValidator;
import eu.europa.ec.leos.test.support.LeosTest;
import eu.europa.ec.leos.vo.toc.NumberingConfig;
import eu.europa.ec.leos.vo.toc.TableOfContentItemVO;
import eu.europa.ec.leos.vo.toc.TocItem;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import javax.inject.Provider;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import static eu.europa.ec.leos.services.support.XmlHelper.BILL;
import static eu.europa.ec.leos.services.support.XmlHelper.CN;
import static eu.europa.ec.leos.services.support.XmlHelper.CONTENT;
import static eu.europa.ec.leos.services.support.XmlHelper.EC;
import static eu.europa.ec.leos.services.support.XmlHelper.INDENT;
import static eu.europa.ec.leos.services.support.XmlHelper.LEOS_ORIGIN_ATTR;
import static eu.europa.ec.leos.services.support.XmlHelper.LEOS_SOFT_TRANS_FROM;
import static eu.europa.ec.leos.services.support.XmlHelper.NUM;
import static eu.europa.ec.leos.services.support.XmlHelper.POINT;
import static eu.europa.ec.leos.services.support.XmlHelper.SUBPOINT;
import static eu.europa.ec.leos.services.support.XmlHelper.XMLID;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;

public class TableOfContentHelperTest extends LeosTest {
    private final AkomantosoXsdValidator akomantosoXsdValidator = new AkomantosoXsdValidator();

    @Mock
    private StructureContext structureContext;
    @Mock
    private Provider<StructureContext> structureContextProvider;
    @Mock
    private TemplateStructureService templateStructureService;
    @Mock
    private eu.europa.ec.leos.security.SecurityContext leosSecurityContext;
    @Mock
    private SecurityContext securityContext;
    @Mock
    private Authentication authentication;
    @Mock
    private UserDetails userDetails;
    @InjectMocks
    private LanguageHelper languageHelper = Mockito.spy(new LanguageHelper());
    @InjectMocks
    private StructureServiceImpl structureServiceImpl;
    @InjectMocks
    private XmlContentProcessorMandate xmlContentProcessor = Mockito.spy(new XmlContentProcessorMandate());

    private List<TocItem> tocItems;
    private List<NumberingConfig> numberingConfigs;
    private Map<TocItem, List<TocItem>> tocRules;
    private String docTemplate;

    @InjectMocks
    private TableOfContentProcessor tableOfContentProcessor = Mockito.spy(new TableOfContentProcessorImpl());
    @InjectMocks
    private IndentConversionHelper indentConversionHelper = new IndentConversionHelper();

    private final static String INDENT_FOLDER = "/indent/";

    private void setTemplateAndStructureFile(String template, String structureFile) {
        docTemplate = template;
        byte[] bytesFile = TestUtils.getFileContent(structureFile);
        when(templateStructureService.getStructure(docTemplate)).thenReturn(bytesFile);
        ReflectionTestUtils.setField(structureServiceImpl, "structureSchema", "toc/schema/structure_1.xsd");
        tocItems = structureServiceImpl.getTocItems(docTemplate);
        numberingConfigs = structureServiceImpl.getNumberingConfigs(docTemplate);
        tocRules = structureServiceImpl.getTocRules(docTemplate);

        try {
            ReflectionTestUtils.setField(akomantosoXsdValidator, "SCHEMA_PATH", "eu/europa/ec/leos/xsd");
            ReflectionTestUtils.setField(akomantosoXsdValidator, "SCHEMA_NAME", "akomantoso30.xsd");
            akomantosoXsdValidator.initXSD();
        } catch (Exception e) {
            e.printStackTrace();
        }

        when(structureContextProvider.get()).thenReturn(structureContext);
        when(structureContext.getTocItems()).thenReturn(tocItems);
        when(structureContext.getNumberingConfigs()).thenReturn(numberingConfigs);
        when(structureContext.getTocRules()).thenReturn(tocRules);

        User user = new User(new Long(3), "demo", "demo", Arrays.asList(new Entity("7", "DGT.R.3", "DGT")), "demo@mail.com", Arrays.asList("SUPPORT", "USER"));
        AuthenticatedUser authenticatedUser = new AuthenticatedUser(user);
        when(userDetails.getUsername()).thenReturn("demo");
        when(authentication.getPrincipal()).thenReturn(authenticatedUser);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(leosSecurityContext.getUserName()).thenReturn("demo");
        when(languageHelper.getCurrentLocale()).thenReturn(new Locale("en"));
        SecurityContextHolder.setContext(securityContext);
    }

    @Before
    public void onSetUp() {
        super.setup();
        setTemplateAndStructureFile("BL-017", "/structure-test-bill-CN.xml");
    }

    private TableOfContentItemVO getItemFromToc(String fileName, String id) {
        byte[] v0 = TestUtils.getFileContent(INDENT_FOLDER, fileName);
        List<TableOfContentItemVO> toc = tableOfContentProcessor.buildTableOfContent(BILL, v0, TocMode.NOT_SIMPLIFIED);
        Optional<TableOfContentItemVO> item = TableOfContentHelper.getItemFromTocById(id, toc);
        return item.orElseGet(null);
    }

    @Test
    public void test_restore_Subpoint_FromFirstSubpoint() {
        TableOfContentItemVO item = getItemFromToc("indent_subpoint_to_firstsubpoint.xml", "transformed_art_1_6G47GHJ");
        item.removeChildItem(item.getChildItems().get(1));
        TableOfContentItemVO convertedItem = indentConversionHelper.buildSubElementFromFirstElement(tocItems, item, 1, true, false, false);
        Node node =convertedItem.getNode();
        assertEquals(node.getNodeName(), SUBPOINT);
        NamedNodeMap attributes = node.getAttributes();
        List<Node> children = XercesUtils.getChildren(node);
        assertEquals(attributes.getLength(), 2);
        assertEquals(attributes.item(0).getNodeName(), LEOS_ORIGIN_ATTR);
        assertEquals(attributes.item(0).getNodeValue(), EC);
        assertEquals(attributes.item(1).getNodeName(), XMLID);
        assertEquals(attributes.item(1).getNodeValue(), "art_1_6G47GHJ");
        assertEquals(children.size(), 1);
        assertEquals(children.get(0).getNodeName(), CONTENT);
        NamedNodeMap childAttributes = children.get(0).getAttributes();
        assertEquals(childAttributes.getLength(), 2);
        assertEquals(childAttributes.item(0).getNodeName(), LEOS_ORIGIN_ATTR);
        assertEquals(childAttributes.item(0).getNodeValue(), EC);
        assertEquals(childAttributes.item(1).getNodeName(), XMLID);
        assertEquals(childAttributes.item(1).getNodeValue(), "art_1_NGHSZ");
        assertEquals(XercesUtils.getChildren(children.get(0)).get(0).getTextContent(), "subpoint of indent");
    }

    @Test
    public void test_restore_Subpoint_FromPoint() {
        TableOfContentItemVO item = getItemFromToc("indent_subpoint_to_point.xml", "art_1_8g5Tr7");
        TableOfContentItemVO convertedItem = indentConversionHelper.buildSubElementFromElement(tocItems, item, 1, true, false, false);
        Node node = convertedItem.getNode();
        assertEquals(node.getNodeName(), SUBPOINT);
        NamedNodeMap attributes = node.getAttributes();
        List<Node> children = XercesUtils.getChildren(node);
        assertEquals(attributes.getLength(), 5);
        assertEquals(attributes.item(0).getNodeName(), LEOS_ORIGIN_ATTR);
        assertEquals(attributes.item(0).getNodeValue(), CN);
        assertEquals(attributes.item(4).getNodeName(), XMLID);
        assertEquals(attributes.item(4).getNodeValue(), "art_1_8g5Tr7");
        assertEquals(children.size(), 1);
        assertEquals(children.get(0).getNodeName(), CONTENT);
        NamedNodeMap childAttributes = children.get(0).getAttributes();
        assertEquals(childAttributes.getLength(), 1);
        assertEquals(childAttributes.item(0).getNodeName(), XMLID);
        assertEquals(childAttributes.item(0).getNodeValue(), "art_1_rJiXQs");
        assertEquals(XercesUtils.getChildren(children.get(0)).get(0).getTextContent(), "Test");
    }

    @Test
    public void test_restore_Subpoint_FromFirstSubparagraph() {
        TableOfContentItemVO item = getItemFromToc("indent_subpoint_to_firstsubparagraph.xml", "indented_akn_article_ebFhaW_EXRv7x");
        item.removeChildItem(item.getChildItems().get(1));
        item.removeChildItem(item.getChildItems().get(1));
        TableOfContentItemVO convertedItem = indentConversionHelper.buildSubElementFromFirstElement(tocItems, item, 1, true, true, false);
        Node node = convertedItem.getNode();
        assertEquals(node.getNodeName(), SUBPOINT);
        NamedNodeMap attributes = node.getAttributes();
        List<Node> children = XercesUtils.getChildren(node);
        assertEquals(attributes.getLength(), 5);
        assertEquals(attributes.item(0).getNodeName(), LEOS_ORIGIN_ATTR);
        assertEquals(attributes.item(0).getNodeValue(), CN);
        assertEquals(attributes.item(4).getNodeName(), XMLID);
        assertEquals(attributes.item(4).getNodeValue(), "akn_article_ebFhaW_EXRv7x");
        assertEquals(children.size(), 1);
        assertEquals(children.get(0).getNodeName(), CONTENT);
        NamedNodeMap childAttributes = children.get(0).getAttributes();
        assertEquals(childAttributes.getLength(), 2);
        assertEquals(childAttributes.item(0).getNodeName(), LEOS_ORIGIN_ATTR);
        assertEquals(childAttributes.item(0).getNodeValue(), CN);
        assertEquals(childAttributes.item(1).getNodeName(), XMLID);
        assertEquals(childAttributes.item(1).getNodeValue(), "akn_article_ebFhaW_XCnEA2");
        assertEquals(XercesUtils.getChildren(children.get(0)).get(0).getTextContent(), "Second subpoint,");
    }

    @Test
    public void test_restore_Subpoint_FromSubparagraph() {
        TableOfContentItemVO item = getItemFromToc("indent_subpoint_to_firstsubparagraph.xml", "akn_article_ebFhaW_qxNs2C");
        TableOfContentItemVO convertedItem = indentConversionHelper.buildSubElementFromSubElement(tocItems, item, 1, true, false);
        Node node = convertedItem.getNode();
        assertEquals(node.getNodeName(), SUBPOINT);
        NamedNodeMap attributes = node.getAttributes();
        List<Node> children = XercesUtils.getChildren(node);
        assertEquals(attributes.getLength(), 5);
        assertEquals(attributes.item(0).getNodeName(), LEOS_ORIGIN_ATTR);
        assertEquals(attributes.item(0).getNodeValue(), CN);
        assertEquals(attributes.item(4).getNodeName(), XMLID);
        assertEquals(attributes.item(4).getNodeValue(), "akn_article_ebFhaW_qxNs2C");
        assertEquals(children.size(), 1);
        assertEquals(children.get(0).getNodeName(), CONTENT);
        NamedNodeMap childAttributes = children.get(0).getAttributes();
        assertEquals(childAttributes.getLength(), 2);
        assertEquals(childAttributes.item(0).getNodeName(), LEOS_ORIGIN_ATTR);
        assertEquals(childAttributes.item(0).getNodeValue(), CN);
        assertEquals(childAttributes.item(1).getNodeName(), XMLID);
        assertEquals(childAttributes.item(1).getNodeValue(), "akn_article_ebFhaW_uJ1SEb");
        assertEquals(XercesUtils.getChildren(children.get(0)).get(0).getTextContent(), "Third subpoint,");
    }

    @Test
    public void test_point_FromFirstSubpoint() {
        TableOfContentItemVO item = getItemFromToc("indent_subpoint_to_point.xml", "transformed_art_1_urNCcV");
        item.removeChildItem(item.getChildItems().get(1));
        TableOfContentItemVO convertedItem = indentConversionHelper.buildElementFromFirstElement(tocItems, item, 1, false, false, false);
        Node node = convertedItem.getNode();
        assertEquals(node.getNodeName(), POINT);
        NamedNodeMap attributes = node.getAttributes();
        List<Node> children = XercesUtils.getChildren(node);
        assertEquals(attributes.getLength(), 7);
        assertEquals(attributes.item(5).getNodeName(), LEOS_ORIGIN_ATTR);
        assertEquals(attributes.item(5).getNodeValue(), EC);
        assertEquals(attributes.item(6).getNodeName(), XMLID);
        assertEquals(attributes.item(6).getNodeValue(), "art_1_urNCcV");
        assertEquals(children.size(), 2);
        assertEquals(children.get(0).getNodeName(), NUM);
        NamedNodeMap childAttributes = children.get(0).getAttributes();
        assertEquals(childAttributes.getLength(), 2);
        assertEquals(childAttributes.item(0).getNodeName(), LEOS_ORIGIN_ATTR);
        assertEquals(childAttributes.item(0).getNodeValue(), EC);
        assertEquals(childAttributes.item(1).getNodeName(), XMLID);
        assertEquals(childAttributes.item(1).getNodeValue(), "art_1_gFkBde");
        assertEquals(children.get(0).getTextContent(), "(a)");
        assertEquals(children.get(1).getNodeName(), CONTENT);
        childAttributes = children.get(1).getAttributes();
        assertEquals(childAttributes.getLength(), 2);
        assertEquals(childAttributes.item(0).getNodeName(), LEOS_ORIGIN_ATTR);
        assertEquals(childAttributes.item(0).getNodeValue(), EC);
        assertEquals(childAttributes.item(1).getNodeName(), XMLID);
        assertEquals(childAttributes.item(1).getNodeValue(), "art_1_Qzmsjc");
        assertEquals(XercesUtils.getChildren(children.get(1)).get(0).getTextContent(), "test");
    }

    @Test
    public void test_point_FromParagraph() {
        TableOfContentItemVO item = getItemFromToc("indent_subpoint_to_point.xml", "art_1_RXdbC0");
        TableOfContentItemVO convertedItem = indentConversionHelper.buildElementFromElement(tocItems, item, 1, false, false);
        Node node = convertedItem.getNode();
        assertEquals(node.getNodeName(), POINT);
        NamedNodeMap attributes = node.getAttributes();
        List<Node> children = XercesUtils.getChildren(node);
        assertEquals(attributes.getLength(), 7);
        assertEquals(attributes.item(5).getNodeName(), LEOS_ORIGIN_ATTR);
        assertEquals(attributes.item(5).getNodeValue(), EC);
        assertEquals(attributes.item(6).getNodeName(), XMLID);
        assertEquals(attributes.item(6).getNodeValue(), "art_1_RXdbC0");
        assertEquals(children.size(), 2);
        assertEquals(children.get(0).getNodeName(), NUM);
        NamedNodeMap childAttributes = children.get(0).getAttributes();
        assertEquals(childAttributes.getLength(), 2);
        assertEquals(childAttributes.item(0).getNodeName(), LEOS_ORIGIN_ATTR);
        assertEquals(childAttributes.item(0).getNodeValue(), EC);
        assertEquals(childAttributes.item(1).getNodeName(), XMLID);
        assertEquals(childAttributes.item(1).getNodeValue(), "art_1_uXQ0yL");
        assertEquals(children.get(0).getTextContent(), "1.");
        assertEquals(children.get(1).getNodeName(), CONTENT);
        childAttributes = children.get(1).getAttributes();
        assertEquals(childAttributes.getLength(), 2);
        assertEquals(childAttributes.item(0).getNodeName(), LEOS_ORIGIN_ATTR);
        assertEquals(childAttributes.item(0).getNodeValue(), EC);
        assertEquals(childAttributes.item(1).getNodeName(), XMLID);
        assertEquals(childAttributes.item(1).getNodeValue(), "art_1_wmf7Mf");
        assertEquals(XercesUtils.getChildren(children.get(1)).get(0).getTextContent(), "POINT");
    }

    @Test
    public void test_point_FromFirstSubparagraph() {
        TableOfContentItemVO item = getItemFromToc("indent_subpoint_to_point.xml", "art_1_GqEOBU");
        item.removeChildItem(item.getChildItems().get(1));
        TableOfContentItemVO convertedItem = indentConversionHelper.buildElementFromFirstElement(tocItems, item, 1, false, true, false);
        Node node = convertedItem.getNode();
        assertEquals(node.getNodeName(), POINT);
        NamedNodeMap attributes = node.getAttributes();
        List<Node> children = XercesUtils.getChildren(node);
        assertEquals(attributes.getLength(), 9);
        assertEquals(attributes.item(5).getNodeName(), LEOS_ORIGIN_ATTR);
        assertEquals(attributes.item(5).getNodeValue(), EC);
        assertEquals(attributes.item(7).getNodeName(), LEOS_SOFT_TRANS_FROM);
        assertEquals(attributes.item(7).getNodeValue(), "art_1_MU1hKr");
        assertEquals(attributes.item(8).getNodeName(), XMLID);
        assertEquals(attributes.item(8).getNodeValue(), "art_1_GqEOBU");
        assertEquals(children.size(), 2);
        assertEquals(children.get(0).getNodeName(), NUM);
        NamedNodeMap childAttributes = children.get(0).getAttributes();
        assertEquals(childAttributes.getLength(), 2);
        assertEquals(childAttributes.item(0).getNodeName(), LEOS_ORIGIN_ATTR);
        assertEquals(childAttributes.item(0).getNodeValue(), EC);
        assertEquals(childAttributes.item(1).getNodeName(), XMLID);
        assertEquals(childAttributes.item(1).getNodeValue(), "art_1_Woc735");
        assertEquals(children.get(0).getTextContent(), "2.");
        assertEquals(children.get(1).getNodeName(), CONTENT);
        childAttributes = children.get(1).getAttributes();
        assertEquals(childAttributes.getLength(), 2);
        assertEquals(childAttributes.item(0).getNodeName(), LEOS_ORIGIN_ATTR);
        assertEquals(childAttributes.item(0).getNodeValue(), EC);
        assertEquals(childAttributes.item(1).getNodeName(), XMLID);
        assertEquals(childAttributes.item(1).getNodeValue(), "art_1_fCeC1t");
        assertEquals(XercesUtils.getChildren(children.get(1)).get(0).getTextContent(), "Example --\n" +
                "                                Point (c) indented\n" +
                "                            ");
    }

    @Test
    public void test_point_FromSubpoint() {
        TableOfContentItemVO item = getItemFromToc("indent_subpoint_to_point.xml", "art_1_ExfjFV");
        TableOfContentItemVO convertedItem = indentConversionHelper.buildElementFromSubElement(tocItems, item, 1, false, false, false);
        Node node = convertedItem.getNode();
        assertEquals(node.getNodeName(), POINT);
        NamedNodeMap attributes = node.getAttributes();
        List<Node> children = XercesUtils.getChildren(node);
        assertEquals(attributes.getLength(), 7);
        assertEquals(attributes.item(2).getNodeName(), LEOS_ORIGIN_ATTR);
        assertEquals(attributes.item(2).getNodeValue(), CN);
        assertEquals(attributes.item(6).getNodeName(), XMLID);
        assertEquals(attributes.item(6).getNodeValue(), "art_1_ExfjFV");
        assertEquals(children.size(), 2);
        assertEquals(children.get(0).getNodeName(), NUM);
        NamedNodeMap childAttributes = children.get(0).getAttributes();
        assertEquals(childAttributes.getLength(), 0);
        assertEquals(children.get(0).getTextContent(), "");
        assertEquals(children.get(1).getNodeName(), CONTENT);
        childAttributes = children.get(1).getAttributes();
        assertEquals(childAttributes.getLength(), 1);
        assertEquals(childAttributes.item(0).getNodeName(), XMLID);
        assertEquals(childAttributes.item(0).getNodeValue(), "art_1_IBPut4");
        assertEquals(XercesUtils.getChildren(children.get(1)).get(0).getTextContent(), "\n" +
                "                                                storage, comprising customs warehousing and free zones;\n" +
                "                                            ");
    }

    @Test
    public void test_firstsubpoint_FromSubpoint() {
        TableOfContentItemVO item = getItemFromToc("indent_subpoint_to_point.xml", "art_1_ExfjFV");
        item.addChildItem(new TableOfContentItemVO(new TocItem(), "id", CN, null, null, null,  null, ""));
        TableOfContentItemVO convertedItem = indentConversionHelper.buildFirstElementFromSubElement(tocItems, item, 1, false, false, false);

        Node node = convertedItem.getNode();
        assertEquals(node.getNodeName(), POINT);
        NamedNodeMap attributes = node.getAttributes();
        List<Node> children = XercesUtils.getChildren(node);
        assertEquals(attributes.getLength(), 7);
        assertEquals(attributes.item(2).getNodeName(), LEOS_ORIGIN_ATTR);
        assertEquals(attributes.item(2).getNodeValue(), CN);
        assertEquals(attributes.item(6).getNodeName(), XMLID);
        assertEquals(attributes.item(6).getNodeValue(), "indented_art_1_ExfjFV");
        assertEquals(children.size(), 2);
        assertEquals(children.get(0).getNodeName(), NUM);
        NamedNodeMap childAttributes = children.get(0).getAttributes();
        assertEquals(childAttributes.getLength(), 0);
        assertEquals(children.get(0).getTextContent(), "");
        assertEquals(children.get(1).getNodeName(), SUBPOINT);
        childAttributes = children.get(1).getAttributes();
        assertEquals(childAttributes.getLength(), 5);
        assertEquals(childAttributes.item(4).getNodeName(), XMLID);
        assertEquals(childAttributes.item(4).getNodeValue(), "art_1_ExfjFV");
        Node content = XercesUtils.getFirstChild(children.get(1), CONTENT);
        assertNotNull(content);
        childAttributes = content.getAttributes();
        assertEquals(childAttributes.getLength(), 1);
        assertEquals(childAttributes.item(0).getNodeName(), XMLID);
        assertEquals(childAttributes.item(0).getNodeValue(), "art_1_IBPut4");
        assertEquals(XercesUtils.getChildren(content).get(0).getTextContent(), "\n" +
                "                                                storage, comprising customs warehousing and free zones;\n" +
                "                                            ");
    }

    @Test
    public void test_firstsubpoint_FromPoint() {
        TableOfContentItemVO item = getItemFromToc("indent_subpoint_to_point.xml", "art_1_8g5Tr7");
        item.addChildItem(new TableOfContentItemVO(new TocItem(), "id", CN, null, null, null, null, ""));
        TableOfContentItemVO convertedItem = indentConversionHelper.buildFirstElementFromElement(tocItems, item, 1, false, false, false);
        Node node = convertedItem.getNode();
        assertEquals(node.getNodeName(), INDENT);
        NamedNodeMap attributes = node.getAttributes();
        List<Node> children = XercesUtils.getChildren(node);
        assertEquals(attributes.getLength(), 7);
        assertEquals(attributes.item(2).getNodeName(), LEOS_ORIGIN_ATTR);
        assertEquals(attributes.item(2).getNodeValue(), CN);
        assertEquals(attributes.item(6).getNodeName(), XMLID);
        assertEquals(attributes.item(6).getNodeValue(), "indented_art_1_8g5Tr7");
        assertEquals(children.size(), 2);
        assertEquals(children.get(0).getNodeName(), NUM);
        NamedNodeMap childAttributes = children.get(0).getAttributes();
        assertEquals(childAttributes.getLength(), 2);
        assertEquals(childAttributes.item(0).getNodeName(), LEOS_ORIGIN_ATTR);
        assertEquals(childAttributes.item(0).getNodeValue(), EC);
        assertEquals(childAttributes.item(1).getNodeName(), XMLID);
        assertEquals(childAttributes.item(1).getNodeValue(), "num_9yZwYq");
        assertEquals(children.get(0).getTextContent(), "-");
        assertEquals(children.get(1).getNodeName(), SUBPOINT);
        childAttributes = children.get(1).getAttributes();
        assertEquals(childAttributes.getLength(), 5);
        assertEquals(childAttributes.item(4).getNodeName(), XMLID);
        assertEquals(childAttributes.item(4).getNodeValue(), "art_1_8g5Tr7");
        Node content = XercesUtils.getFirstChild(children.get(1), CONTENT);
        assertNotNull(content);
        childAttributes = content.getAttributes();
        assertEquals(childAttributes.getLength(), 1);
        assertEquals(childAttributes.item(0).getNodeName(), XMLID);
        assertEquals(childAttributes.item(0).getNodeValue(), "art_1_rJiXQs");
        assertEquals(XercesUtils.getChildren(content).get(0).getTextContent(), "Test");
    }

    @Test
    public void test_firstsubpoint_FromFirstsubparaph() {
        TableOfContentItemVO item = getItemFromToc("indent_subpoint_to_point.xml", "art_1_GqEOBU");
        TableOfContentItemVO convertedItem = indentConversionHelper.buildFirstElementFromFirstElement(tocItems, item, 1, false, false);
        Node node = convertedItem.getNode();
        assertEquals(node.getNodeName(), POINT);
        NamedNodeMap attributes = node.getAttributes();
        List<Node> children = XercesUtils.getChildren(node);
        assertEquals(attributes.getLength(), 7);
        assertEquals(attributes.item(5).getNodeName(), LEOS_ORIGIN_ATTR);
        assertEquals(attributes.item(5).getNodeValue(), EC);
        assertEquals(attributes.item(6).getNodeName(), XMLID);
        assertEquals(attributes.item(6).getNodeValue(), "art_1_GqEOBU");
        assertEquals(children.size(), 2);
        assertEquals(children.get(0).getNodeName(), NUM);
        NamedNodeMap childAttributes = children.get(0).getAttributes();
        assertEquals(childAttributes.getLength(), 2);
        assertEquals(childAttributes.item(0).getNodeName(), LEOS_ORIGIN_ATTR);
        assertEquals(childAttributes.item(0).getNodeValue(), EC);
        assertEquals(childAttributes.item(1).getNodeName(), XMLID);
        assertEquals(childAttributes.item(1).getNodeValue(), "art_1_Woc735");
        assertEquals(children.get(0).getTextContent(), "2.");
        assertEquals(children.get(1).getNodeName(), SUBPOINT);
        childAttributes = children.get(1).getAttributes();
        assertEquals(childAttributes.getLength(), 2);
        assertEquals(childAttributes.item(1).getNodeName(), XMLID);
        assertEquals(childAttributes.item(1).getNodeValue(), "art_1_MU1hKr");
        Node content = XercesUtils.getFirstChild(children.get(1), CONTENT);
        assertNotNull(content);
        childAttributes = content.getAttributes();
        assertEquals(childAttributes.getLength(), 2);
        assertEquals(childAttributes.item(1).getNodeName(), XMLID);
        assertEquals(childAttributes.item(1).getNodeValue(), "art_1_fCeC1t");
        assertEquals(XercesUtils.getChildren(content).get(0).getTextContent(), "Example --\n" +
                "                                Point (c) indented\n" +
                "                            ");
    }
}
