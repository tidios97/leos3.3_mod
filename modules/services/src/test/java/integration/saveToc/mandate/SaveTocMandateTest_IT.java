package integration.saveToc.mandate;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import eu.europa.ec.leos.services.label.ReferenceLabelService;
import eu.europa.ec.leos.services.numbering.NumberServiceMandate;
import eu.europa.ec.leos.services.numbering.processor.*;
import eu.europa.ec.leos.services.processor.content.XmlContentProcessor;
import eu.europa.ec.leos.services.label.ReferenceLabelServiceImplMandate;
import eu.europa.ec.leos.services.processor.content.TableOfContentProcessorImpl;
import eu.europa.ec.leos.services.processor.content.XmlContentProcessorMandate;
import org.junit.Before;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

import eu.europa.ec.leos.services.numbering.NumberProcessorHandler;
import eu.europa.ec.leos.services.numbering.NumberProcessorHandlerMandate;
import eu.europa.ec.leos.services.numbering.NumberService;
import eu.europa.ec.leos.services.numbering.config.NumberConfigFactory;
import eu.europa.ec.leos.services.numbering.depthBased.ParentChildConverter;
import eu.europa.ec.leos.services.processor.content.TableOfContentProcessor;
import integration.saveToc.SaveTocTest_IT;

public abstract class SaveTocMandateTest_IT extends SaveTocTest_IT {

    @InjectMocks
    protected XmlContentProcessor xmlContentProcessor = Mockito.spy(new XmlContentProcessorMandate());
    @InjectMocks
    protected TableOfContentProcessor tableOfContentProcessor = Mockito.spy(new TableOfContentProcessorImpl());
    @InjectMocks
    protected ReferenceLabelService referenceLabelService = Mockito.spy(new ReferenceLabelServiceImplMandate());

    protected ParentChildConverter parentChildConverter = new ParentChildConverter();
    @InjectMocks
    protected NumberConfigFactory numberConfigFactory = Mockito.spy(new NumberConfigFactory());
    @InjectMocks
    protected NumberProcessorHandler numberProcessorHandler = new NumberProcessorHandlerMandate();
    private NumberProcessor numberProcessorArticle = new NumberProcessorArticle(messageHelper, numberProcessorHandler);
    private NumberProcessor numberProcessorPoint = new NumberProcessorParagraphAndPoint(messageHelper, numberProcessorHandler);
    private NumberProcessor numberProcessorDefault = new NumberProcessorDefault(messageHelper, numberProcessorHandler);
    private NumberProcessorDepthBased numberProcessorDepthBasedDefault = new NumberProcessorDepthBasedDefault(messageHelper, numberProcessorHandler);
    private NumberProcessorDepthBased numberProcessorLevel = new eu.europa.ec.leos.services.numbering.processor.NumberProcessorLevel(messageHelper, numberProcessorHandler);
    @InjectMocks
    protected List<NumberProcessor> numberProcessors = Mockito.spy(Stream.of(numberProcessorArticle,
            numberProcessorPoint,
            numberProcessorDefault).collect(Collectors.toList()));
    @InjectMocks
    protected List<NumberProcessorDepthBased> numberProcessorsDepthBased = Mockito.spy(Stream.of(numberProcessorDepthBasedDefault, numberProcessorLevel).collect(Collectors.toList()));

    protected NumberService numberService ;

    @Before
    public void onSetUp() throws Exception {
        super.onSetUp();
        ReflectionTestUtils.setField(numberProcessorHandler, "numberConfigFactory", numberConfigFactory);
        ReflectionTestUtils.setField(numberProcessorHandler, "numberProcessorsDepthBased", numberProcessorsDepthBased);
        ReflectionTestUtils.setField(numberProcessorHandler, "numberProcessors", numberProcessors);
        numberService = new NumberServiceMandate(xmlContentProcessor, structureContextProvider, numberProcessorHandler, parentChildConverter);
    }

}
