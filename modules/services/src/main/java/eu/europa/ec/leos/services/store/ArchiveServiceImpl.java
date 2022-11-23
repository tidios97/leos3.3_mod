package eu.europa.ec.leos.services.store;

import eu.europa.ec.leos.domain.cmis.document.LeosDocument;
import eu.europa.ec.leos.domain.cmis.metadata.AnnexMetadata;
import eu.europa.ec.leos.domain.vo.DocumentVO;
import eu.europa.ec.leos.repository.LeosRepository;
import eu.europa.ec.leos.repository.store.PackageRepository;
import eu.europa.ec.leos.repository.store.WorkspaceRepository;
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ArchiveServiceImpl implements ArchiveService {

    private final PackageRepository packageRepository;
    private final WorkspaceRepository workspaceRepository;
    private final LeosRepository leosRepository;
    private String archiveFolder = "archive";

    @Autowired
    public ArchiveServiceImpl(PackageRepository packageRepository, WorkspaceRepository workspaceRepository, LeosRepository leosRepository) {
        this.packageRepository = packageRepository;
        this.workspaceRepository = workspaceRepository;
        this.leosRepository = leosRepository;
    }

    @Override
    public <D extends LeosDocument> void archiveDocument(DocumentVO documentVO, Class<? extends D> type, String packagePath) {

        LeosDocument document = leosRepository.findDocumentById(documentVO.getId(), type, true);
        byte[] contentBytes = document.getContent().get().getSource().getBytes();
        String name = document.getName();
        String path = packagePath + "/" + archiveFolder;
        final AnnexMetadata annexMetadata = new AnnexMetadata(documentVO.getMetadata().getDocStage(),
                documentVO.getMetadata().getDocType(), documentVO.getMetadata().getDocPurpose(), documentVO.getMetadata().getTemplate(),
                documentVO.getMetadata().getLanguage(), documentVO.getMetadata().getDocTemplate(), documentVO.getMetadata().getInternalRef(),
                0, documentVO.getMetadata().getNumber(), documentVO.getMetadata().getTitle(),
                documentVO.getId(), documentVO.getVersionSeriesId(), false, null);
        try {
            leosRepository.findFolderByPath(path);
        } catch (CmisObjectNotFoundException exception) {
            leosRepository.createFolder(packagePath, archiveFolder);
        }
        leosRepository.createDocumentFromContent(path, name, annexMetadata, type, documentVO.getCategory().name(), contentBytes);
    }
}