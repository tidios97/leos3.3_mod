package eu.europa.ec.leos.services.template;

import com.fasterxml.jackson.databind.JsonNode;
import eu.europa.ec.leos.domain.cmis.document.XmlDocument;
import eu.europa.ec.leos.repository.store.ConfigurationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class TemplateStructureServiceImpl implements TemplateStructureService {

    @Value("${leos.templates.path}")
    private String templatesStructurePath;

    @Value("${leos.templates.structure}")
    private String structure;

    @Autowired
    TemplateConfigurationService templateConfigurationService;

    @Autowired
    ConfigurationRepository configurationRepository;

    @Override
    public byte[] getStructure(String templateID) {
        JsonNode structureJson = templateConfigurationService.getTemplateConfigurationJson(templateID, structure);
        XmlDocument structureXmlDocument = configurationRepository.findTemplate(templatesStructurePath, structureJson.get(0).get("name").asText());
        return structureXmlDocument.getContent().get().getSource().getBytes();
    }
}
