package eu.europa.ec.leos.services.document;

import eu.europa.ec.leos.domain.cmis.LeosCategory;
import eu.europa.ec.leos.domain.common.ErrorCode;
import eu.europa.ec.leos.domain.common.InstanceType;
import eu.europa.ec.leos.domain.common.Result;
import eu.europa.ec.leos.domain.vo.DocumentVO;
import eu.europa.ec.leos.instance.Instance;
import eu.europa.ec.leos.domain.vo.CloneProposalMetadataVO;
import eu.europa.ec.leos.services.processor.content.XmlContentProcessor;
import eu.europa.ec.leos.services.support.XPathCatalog;
import eu.europa.ec.leos.services.support.url.CollectionIdsAndUrlsHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Collections;

import static eu.europa.ec.leos.services.support.XmlHelper.LEOS_DELETABLE_ATTR;
import static eu.europa.ec.leos.services.support.XmlHelper.LEOS_EDITABLE_ATTR;
import static eu.europa.ec.leos.services.support.XmlHelper.LEOS_ORIGIN_ATTR;
import static eu.europa.ec.leos.services.support.XmlHelper.ARTICLE;
import static eu.europa.ec.leos.services.support.XmlHelper.BILL;
import static eu.europa.ec.leos.services.support.XmlHelper.BODY;
import static eu.europa.ec.leos.services.support.XmlHelper.CITATIONS;
import static eu.europa.ec.leos.services.support.XmlHelper.DOC;
import static eu.europa.ec.leos.services.support.XmlHelper.EC;
import static eu.europa.ec.leos.services.support.XmlHelper.RECITALS;

@Service
@Instance(InstanceType.COUNCIL)
public class PostProcessingMandateServiceImpl extends PostProcessingDocumentService {

    @Autowired
    PostProcessingMandateServiceImpl(XmlContentProcessor xmlContentProcessor, XPathCatalog xPathCatalog) {
        super(xmlContentProcessor, xPathCatalog);
    }

    public Result<?> processDocument(DocumentVO documentVO) {
        if (documentVO.getCategory().equals(LeosCategory.PROPOSAL)) {
            byte[] updatedDocContent = preserveDocumentReference(documentVO.getSource());
            documentVO.setSource(updatedDocContent);
            for (DocumentVO doc : documentVO.getChildDocuments()) {
                try {
                    if (!doc.getCategory().equals(LeosCategory.PROPOSAL)) {
                        byte[] docContent = doc.getSource();
                        if (doc.getCategory().equals(LeosCategory.BILL)) {
                            
                            updatedDocContent = xmlContentProcessor.setAttributeForAllChildren(docContent, BILL, Collections.emptyList(), LEOS_ORIGIN_ATTR, EC);
                            updatedDocContent = xmlContentProcessor.setAttributeForAllChildren(updatedDocContent, BODY, Arrays.asList(ARTICLE),
                                    LEOS_DELETABLE_ATTR, "false");
                            updatedDocContent = xmlContentProcessor.setAttributeForAllChildren(updatedDocContent, BILL,
                                    Arrays.asList(CITATIONS, RECITALS), LEOS_EDITABLE_ATTR, "false");
                            updatedDocContent = preserveDocumentReference(updatedDocContent);
                            
                            doc.setSource(updatedDocContent);
                            
                            for (DocumentVO annex : doc.getChildDocuments()) {
                                byte[] annexContent = annex.getSource();
                                byte[] updatedDocContentAnnex = xmlContentProcessor.setAttributeForAllChildren(annexContent, DOC, Collections.emptyList(), LEOS_ORIGIN_ATTR, EC);
                                updatedDocContentAnnex = preserveDocumentReference(updatedDocContentAnnex);
                                annex.setSource(updatedDocContentAnnex);
                            }
                        } else {
                            updatedDocContent = xmlContentProcessor.setAttributeForAllChildren(docContent, DOC, Collections.emptyList(), LEOS_ORIGIN_ATTR, EC);
                            updatedDocContent = preserveDocumentReference(updatedDocContent);
                            doc.setSource(updatedDocContent);
                        }
                    }
                } catch (Exception e) {
                    return new Result<String>(e.getMessage(), ErrorCode.EXCEPTION);
                }
            }
        }
        return new Result<String>("OK", null);
    }

    @Override
    public Result<?> saveOriginalProposalIdToClonedProposal(DocumentVO documentVO, String legFileName, String iscRef) {
        return new Result<String>("Not implemented", ErrorCode.EXCEPTION);
    }

    @Override
    public Result<?> saveClonedProposalIdToOriginalProposal(DocumentVO documentVO, CollectionIdsAndUrlsHolder idsAndUrlsHolder, CloneProposalMetadataVO cloneProposalMetadataVO) {
        return new Result<String>("Not implemented", ErrorCode.EXCEPTION);
    }

}
