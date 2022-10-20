/*
 * Copyright 2018 European Commission
 *
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 *     https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and limitations under the Licence.
 */
package eu.europa.ec.leos.services.store;

import cool.graph.cuid.Cuid;
import eu.europa.ec.leos.domain.cmis.LeosCategory;
import eu.europa.ec.leos.domain.cmis.LeosPackage;
import eu.europa.ec.leos.domain.cmis.document.Annex;
import eu.europa.ec.leos.domain.cmis.document.Bill;
import eu.europa.ec.leos.domain.cmis.document.LeosDocument;
import eu.europa.ec.leos.domain.cmis.document.Memorandum;
import eu.europa.ec.leos.domain.cmis.document.XmlDocument;
import eu.europa.ec.leos.domain.cmis.metadata.LeosMetadata;
import eu.europa.ec.leos.domain.common.TocMode;
import eu.europa.ec.leos.repository.store.PackageRepository;
import eu.europa.ec.leos.services.processor.content.TableOfContentProcessor;
import eu.europa.ec.leos.services.toc.StructureContext;
import eu.europa.ec.leos.vo.toc.TableOfContentItemVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.inject.Provider;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static eu.europa.ec.leos.services.support.XmlHelper.BILL;
import static eu.europa.ec.leos.services.support.XmlHelper.DOC;

@Service
class PackageServiceImpl implements PackageService {
    private static final Logger LOG = LoggerFactory.getLogger(PackageServiceImpl.class);
    private static final String PACKAGE_NAME_PREFIX = "package_";

    private final PackageRepository packageRepository;
    private final Provider<StructureContext> structureContextProvider;
    private final TableOfContentProcessor tableOfContentProcessor;

    @Value("${leos.workspaces.path}")
    protected String storagePath;
    
    PackageServiceImpl(PackageRepository packageRepository,
                       Provider<StructureContext> structureContextProvider,
                       TableOfContentProcessor tableOfContentProcessor) {
        this.packageRepository = packageRepository;
        this.structureContextProvider = structureContextProvider;
        this.tableOfContentProcessor = tableOfContentProcessor;
    }

    @Override
    public LeosPackage createPackage() {
        String name = generatePackageName();
        return packageRepository.createPackage(storagePath, name);
    }

    @Override
    public void deletePackage(LeosPackage leosPackage) {
        packageRepository.deletePackage(leosPackage.getPath());
    }

    @Override
    public LeosPackage findPackageByDocumentId(String documentId) {
        return packageRepository.findPackageByDocumentId(documentId);
    }

    @Override
    public <T extends LeosDocument> List<T> findDocumentsByPackagePath(String path, Class<T> filterType, Boolean fetchContent) {
        return packageRepository.findDocumentsByPackagePath(path, filterType, fetchContent);
    }

    @Override
    public <T extends LeosDocument> T findDocumentByPackagePathAndName(String path, String name, Class<T> filterType) {
        return packageRepository.findDocumentByPackagePathAndName(path, name, filterType);
    }
    
    @Override
    public <T extends LeosDocument> List<T> findDocumentsByPackageId(String id, Class<T> filterType, Boolean allVersions, Boolean fetchContent) {
        return packageRepository.findDocumentsByPackageId(id, filterType, allVersions, fetchContent);
    }

    @Override
    public <T extends LeosDocument> List<T> findDocumentsByUserId(String userId, Class<T> filterType, String leosAuthority) {
        return packageRepository.findDocumentsByUserId(userId, filterType, leosAuthority);
    }

    private String generatePackageName() {
        return PACKAGE_NAME_PREFIX + Cuid.createCuid();
    }

    @Override
    public Map<String, List<TableOfContentItemVO>> getTableOfContent(String documentId, TocMode mode) {
        LeosPackage leosPackage = findPackageByDocumentId(documentId);
        List<XmlDocument> documents = findDocumentsByPackagePath(leosPackage.getPath(), XmlDocument.class, true);
        Map<String, List<TableOfContentItemVO>> tocItemsMap = new HashMap<>();
        for (XmlDocument document : documents) {
            final String startingNode;
            if (document instanceof Bill) {
                startingNode = BILL;
                structureContextProvider.get().useDocumentTemplate(document.getMetadata().get().getDocTemplate());
            } else if (document instanceof Annex || document instanceof Memorandum) {
                startingNode = DOC;
                structureContextProvider.get().useDocumentTemplate(document.getMetadata().get().getDocTemplate());
            } else {
                continue;  //skip proposal
            }

            List<TableOfContentItemVO> toc = tableOfContentProcessor.buildTableOfContent(startingNode, document.getContent().get().getSource().getBytes(), mode);
            tocItemsMap.put(document.getMetadata().get().getRef(), toc);
        }
        return tocItemsMap;
    }

    @Override
    public List<LeosMetadata> getDocumentsMetadata(String documentId) {
        LeosPackage leosPackage = findPackageByDocumentId(documentId);
        List<XmlDocument> documents = findDocumentsByPackagePath(leosPackage.getPath(), XmlDocument.class, false);
        Comparator<XmlDocument> annexIndexComparator = Comparator.comparing(o -> {
            if (o instanceof Annex) {
                Annex annex = (Annex) o;
                return annex.getMetadata().get().getIndex();
            }
            return 0;
        });
        return documents.stream()
                .filter(p -> p.getCategory() != LeosCategory.PROPOSAL)
                .sorted(Comparator.<XmlDocument, String>comparing(o -> o.getCategory().name(), Comparator.reverseOrder())
                        .thenComparing(annexIndexComparator))
                .map(p -> p.getMetadata().get()).collect(Collectors.toList());
    }
}
