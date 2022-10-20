package eu.europa.ec.leos.services.validation.handlers;

import com.google.common.base.Stopwatch;
import eu.europa.ec.leos.domain.cmis.LeosCategory;
import eu.europa.ec.leos.domain.common.ErrorCode;
import eu.europa.ec.leos.domain.vo.DocumentVO;
import eu.europa.ec.leos.domain.vo.ErrorVO;
import eu.europa.ec.leos.services.util.TestUtils;
import eu.europa.ec.leos.test.support.LeosTest;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class AkomantosoXsdValidatorTest extends LeosTest {
    private static final Logger LOG = LoggerFactory.getLogger(AkomantosoXsdValidatorTest.class);

    private AkomantosoXsdValidator akomantosoXsdValidator = new AkomantosoXsdValidator();

    @Before
    public void setUp() throws Exception {
        ReflectionTestUtils.setField(akomantosoXsdValidator,"SCHEMA_PATH", "eu/europa/ec/leos/xsd" );
        ReflectionTestUtils.setField(akomantosoXsdValidator,"SCHEMA_NAME", "akomantoso30.xsd" );
        akomantosoXsdValidator.initXSD();
    }

    @Test
    public void test_validate_OK() {
        // setup
        byte[] xmlContent = ("<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<akomaNtoso xmlns=\"http://docs.oasis-open.org/legaldocml/ns/akn/3.0\" xmlns:leos=\"urn:eu:europa:ec:leos\">"
                + "<bill name=\"x\"><meta>"
                + "<identification source=\"~COM\">"
                + "<FRBRWork>"
                + "    <FRBRthis value=\"\"/>"
                + "    <FRBRuri value=\"\"/>"
                + "    <FRBRdate date=\"9999-01-01\" name=\"\"/>"
                + "    <FRBRauthor href=\"\"/>"
                + "    <FRBRcountry value=\"eu\"/>"
                + "</FRBRWork>"
                + "<FRBRExpression>"
                + "    <FRBRthis value=\"\"/>"
                + "    <FRBRuri value=\"\"/>"
                + "    <FRBRdate date=\"9999-01-01\" name=\"\"/>"
                + "    <FRBRauthor href=\"\"/>"
                + "    <FRBRlanguage language=\"eng\"/>"
                + "</FRBRExpression>"
                + "<FRBRManifestation>"
                + "    <FRBRthis value=\"\"/>"
                + "    <FRBRuri value=\"\"/>"
                + "    <FRBRdate date=\"9999-01-01\" name=\"\"/>"
                + "    <FRBRauthor href=\"\"/>"
                + "</FRBRManifestation>"
                + "</identification>"
                + "</meta><body><article xml:id=\"art486\"></article></body></bill>" +
                "</akomaNtoso>").getBytes(UTF_8);
        DocumentVO documentVO = new DocumentVO(LeosCategory.BILL);
        List<ErrorVO> result = new ArrayList<>();
        documentVO.setSource(xmlContent);

        //actual Call
        akomantosoXsdValidator.validate(documentVO, result);

        //validate
        result.forEach(errorVO -> LOG.trace("Error found:{}",errorVO));
        assertThat(result, Matchers.equalTo(Collections.emptyList()));
    }

    @Test
    public void test_validate_with_error() {
        // setup
        byte[] xmlContent = ("<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<akomaNtoso xmlns=\"http://docs.oasis-open.org/legaldocml/ns/akn/3.0\" xmlns:leos=\"urn:eu:europa:ec:leos\">"
                + "<bill name=\"x\"><meta>"
                + "<identification>"
                + "<FRBRWork>"
                + "    <FRBRthis value=\"\"/>"
                + "    <FRBRuri value=\"\"/>"
                + "    <FRBRdate date=\"9999-01-01\" name=\"\"/>"
                + "    <FRBRauthor href=\"\"/>"
                + "    <FRBRcountry value=\"eu\"/>"
                + "</FRBRWork>"
                + "<FRBRExpression>"
                + "    <FRBRthis value=\"\"/>"
                + "    <FRBRuri value=\"\"/>"
                + "    <FRBRdate date=\"9999-01-01\" name=\"\"/>"
                + "    <FRBRauthor href=\"\"/>"
                + "    <FRBRlanguage language=\"eng\"/>"
                + "</FRBRExpression>"
                + "<FRBRManifestation>"
                + "    <FRBRthis value=\"\"/>"
                + "    <FRBRuri value=\"\"/>"
                + "    <FRBRdate date=\"9999-01-01\" name=\"\"/>"
                + "    <FRBRauthor href=\"\"/>"
                + "</FRBRManifestation>"
                + "</identification>"
                + "</meta><body><article xml:id=\"art486\"></article></body></bill>" +
                "</akomaNtoso>").getBytes(UTF_8);
        DocumentVO documentVO = new DocumentVO(LeosCategory.BILL);
        List<ErrorVO> result = new ArrayList<>();
        documentVO.setSource(xmlContent);

        //actual Call
        akomantosoXsdValidator.validate(documentVO, result);

        //validate
        result.forEach(errorVO -> LOG.trace("Error found:{}",errorVO));
        assertThat(result.size(), Matchers.equalTo(1));
        assertThat(result.get(0).getErrorCode(), Matchers.equalTo(ErrorCode.DOCUMENT_XSD_VALIDATION_FAILED));
    }

    @Test
    public void test_validate_with_bigFile() {
        // setup
        byte[] xmlContent = TestUtils.getFileContent("/bill_big.xml");
        DocumentVO documentVO = new DocumentVO(LeosCategory.BILL);
        List<ErrorVO> result = new ArrayList<>();
        documentVO.setSource(xmlContent);

        Stopwatch stopwatch = Stopwatch.createStarted();
        //actual Call
        akomantosoXsdValidator.validate(documentVO, result);

        //validate
        stopwatch.stop();
        long timeTaken = stopwatch.elapsed(TimeUnit.MILLISECONDS);
        result.forEach(errorVO -> LOG.trace("Error found:{}",errorVO));
        assertTrue(timeTaken < 50_000);// should not take more than 10 sec..
    }
    
}