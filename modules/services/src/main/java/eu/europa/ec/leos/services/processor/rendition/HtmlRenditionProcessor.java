package eu.europa.ec.leos.services.processor.rendition;

import eu.europa.ec.leos.model.rendition.RenderedDocument;
import eu.europa.ec.leos.services.support.freemarker.XmlNodeModelHandler;
import freemarker.ext.dom.NodeModel;
import freemarker.template.Configuration;
import freemarker.template.MalformedTemplateNameException;
import freemarker.template.Template;
import freemarker.template.TemplateNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

@Component
public class HtmlRenditionProcessor {

    @Value("${leos.freemarker.ftl.rendition}")
    private String renditionTemplate;
    
    @Value("${leos.freemarker.ftl.rendition_toc_file}")
    private String renditionJsTocTemplate;

    @Value("${leos.freemarker.ftl.coverpage.rendition}")
    private String coverPageRenditionTemplate;

    private final Configuration freemarkerConfiguration;

    public HtmlRenditionProcessor(Configuration freemarkerConfiguration) {
        this.freemarkerConfiguration = freemarkerConfiguration;
    }

    public String processTemplate(RenderedDocument document) {
        return processTocTemplate(document, null);
    }

    public String processCoverPage(RenderedDocument document) {
        try{
            final Template template = getTemplate(coverPageRenditionTemplate);
            final NodeModel nodeModel = XmlNodeModelHandler.parseXmlStream(document.getContent());
            final Map root = new HashMap<String, Object>();
            root.put("styleSheetName", document.getStyleSheetName());
            root.put("cover_data", nodeModel);
            return process(template, root);
        } catch (Exception exception) {
            throw new RuntimeException(exception.getMessage());
        }
    }
    
    public String processTocTemplate(RenderedDocument document, String tocFile) {
        try{
            final Template template = getTemplate(renditionTemplate);
            final NodeModel nodeModel = XmlNodeModelHandler.parseXmlStream(document.getContent());
            final Map root = new HashMap<String, Object>();
            root.put("xml_data", nodeModel);
            root.put("toc_file", tocFile);
            root.put("styleSheetName", document.getStyleSheetName());

            return process(template, root);
        } catch (Exception exception) {
            throw new RuntimeException(exception.getMessage());
        }
    }

    public String processCoverPageTocTemplate(RenderedDocument document, String tocFile) {
        try{
            final Template template = getTemplate(coverPageRenditionTemplate);
            final NodeModel nodeModel = XmlNodeModelHandler.parseXmlStream(document.getContent());
            final Map root = new HashMap<String, Object>();
            root.put("cover_data", nodeModel);
            root.put("toc_file", tocFile);
            root.put("styleSheetName", document.getStyleSheetName());

            return process(template, root);
        } catch (Exception exception) {
            throw new RuntimeException(exception.getMessage());
        }
    }
    
    public String processJsTemplate(String tocJson) {
        final Template template = getTemplate(renditionJsTocTemplate);
        final Map root = new HashMap<String, Object>();
        root.put("tocJson", tocJson);
    
        return process(template, root);
    }
    
    private String process(Template template, Map root) {
        try {
            StringWriter outputWriter = new StringWriter();
            template.process(root, outputWriter);
            return outputWriter.getBuffer().toString();
        } catch (Exception e) {
            throw new RuntimeException("Error occurred while Template processing" + e);
        }
    }
    
    private Template getTemplate(String templateName) {
        try {
            return freemarkerConfiguration.getTemplate(templateName);
        } catch (MalformedTemplateNameException malformedTemplateNameException) {
            throw new RuntimeException("Invalid Template", malformedTemplateNameException);
        } catch (TemplateNotFoundException templateNotFoundException) {
            throw new RuntimeException("Template not found", templateNotFoundException);
        } catch (Exception exception) {
            throw new RuntimeException(exception.getMessage());
        }
    }
}