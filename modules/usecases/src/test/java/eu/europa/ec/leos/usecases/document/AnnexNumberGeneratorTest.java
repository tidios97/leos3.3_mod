package eu.europa.ec.leos.usecases.document;

import eu.europa.ec.leos.i18n.LanguageHelper;
import eu.europa.ec.leos.i18n.MessageHelper;
import eu.europa.ec.leos.i18n.ProposalMessageHelper;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.context.MessageSource;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.Locale;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

public class AnnexNumberGeneratorTest {

	@Mock
	protected LanguageHelper languageHelper;

	@InjectMocks
	protected MessageHelper messageHelper = Mockito.spy(getMessageHelper());

	protected MessageHelper getMessageHelper() {
		try (ClassPathXmlApplicationContext applicationContext = new ClassPathXmlApplicationContext("test-servicesContext.xml")) {
			MessageSource servicesMessageSource = (MessageSource) applicationContext.getBean("servicesMessageSource");
			MessageHelper messageHelper = new ProposalMessageHelper(servicesMessageSource);
			return messageHelper;
		}
	}

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		when(languageHelper.getCurrentLocale()).thenReturn(new Locale("en"));
	}

	@Test
	public void teset_getAnnexNumber_For_ValidNumberRange() {

		String actaulResult = AnnexNumberGenerator.getAnnexNumber(14, messageHelper);
		String expectedResult = messageHelper.getMessage("document.annex.title.prefix") + " XIV";
		assertThat(expectedResult, is(actaulResult));
	}

	@Test
	public void teset_getAnnexNumber_For_Zero() {

		String actaulResult = AnnexNumberGenerator.getAnnexNumber(0, messageHelper);
		String expectedResult = messageHelper.getMessage("document.annex.title.prefix");
		assertThat(expectedResult, is(actaulResult));

	}

	@Test
	public void teset_getAnnexNumber_For_InValidNumberRange() {

		String actaulResult = AnnexNumberGenerator.getAnnexNumber(4040, messageHelper);
		String expectedResult = messageHelper.getMessage("document.annex.title.prefix") + " 4040";
		assertThat(expectedResult, is(actaulResult));

	}

}
