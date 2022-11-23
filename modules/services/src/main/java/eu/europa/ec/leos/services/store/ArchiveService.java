package eu.europa.ec.leos.services.store;

import eu.europa.ec.leos.domain.cmis.document.LeosDocument;
import eu.europa.ec.leos.domain.cmis.document.XmlDocument;
import eu.europa.ec.leos.domain.cmis.metadata.LeosMetadata;
import eu.europa.ec.leos.domain.vo.DocumentVO;

public interface ArchiveService {

    <D extends LeosDocument> void archiveDocument(DocumentVO documentVO, Class<? extends D> type, String packagePath);
}
