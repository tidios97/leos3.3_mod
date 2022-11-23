package eu.europa.ec.leos.services.collection;

import eu.europa.ec.leos.domain.common.Result;
import eu.europa.ec.leos.domain.vo.DocumentVO;

import java.io.File;

public interface CreateCollectionService {

    /**
     * Create a collection from document vo
     *
     * @param documentVO
     * @return The collection creation result containing the proposal view url.
     */
    CreateCollectionResult createCollection(DocumentVO documentVO)throws CreateCollectionException;

    /**
     * Create a collection from a Leg document file
     *
     * @param legDocument
     * @return The collection creation result containing the proposal view url and the bill view url
     */
    CreateCollectionResult createCollectionFromLeg(File legDocument) throws CreateCollectionException;

    /**
     * Clone an existing collection from a Leg document file
     *
     * @param legDocument
     * @param iscRef
     * @param connectedEntity
     * @return The collection cloned result containing the documents url and id
     */
    CreateCollectionResult cloneCollection(File legDocument, String iscRef, String user, String connectedEntity) throws CreateCollectionException;

    Result<?> updateOriginalProposalAfterRevisionDone(String cloneProposalRef, String cloneLegFileId);
}
