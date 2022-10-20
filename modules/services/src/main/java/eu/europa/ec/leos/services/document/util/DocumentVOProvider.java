package eu.europa.ec.leos.services.document.util;

import eu.europa.ec.leos.domain.cmis.document.XmlDocument;
import eu.europa.ec.leos.domain.vo.DocumentVO;
import org.springframework.stereotype.Component;

@Component
public class DocumentVOProvider {

    public DocumentVO createDocumentVO(XmlDocument xmlDocument, byte[] updatedContent) {
        DocumentVO documentVO = new DocumentVO(xmlDocument);
        if (!xmlDocument.getCollaborators().isEmpty()) {
            documentVO.addCollaborators(xmlDocument.getCollaborators());
        }
        documentVO.setSource(updatedContent);
        return documentVO;
    }
}