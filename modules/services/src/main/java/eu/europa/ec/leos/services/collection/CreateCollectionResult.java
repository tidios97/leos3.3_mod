package eu.europa.ec.leos.services.collection;

import eu.europa.ec.leos.services.support.url.CollectionIdsAndUrlsHolder;
import java.util.Map;

public class CreateCollectionResult {

    private String proposalId;
    private String billId;
    private String proposalUrl;
    private String billUrl;
    private String memorandumUrl;
    private String memorandumId;
    private String coverpageUrl;
    private String coverpageId;
    private Map<String, String> annexIdUrl;
    private Map<String, String> docCloneAndOriginIdMap;
    private boolean collectionCreated;
    private CreateCollectionError error;

    // For Jackson
    public CreateCollectionResult() {
    }

    public CreateCollectionResult(CollectionIdsAndUrlsHolder idsAndUrlsHolder,
                                  boolean collectionCreated,
                                  CreateCollectionError error) {
        this.proposalId = idsAndUrlsHolder.getProposalId();
        this.proposalUrl = idsAndUrlsHolder.getProposalUrl();
        this.billId = idsAndUrlsHolder.getBillId();
        this.billUrl = idsAndUrlsHolder.getBillUrl();
        this.memorandumId = idsAndUrlsHolder.getMemorandumId();
        this.memorandumUrl = idsAndUrlsHolder.getMemorandumUrl();
        this.coverpageId = idsAndUrlsHolder.getCoverpageId();
        this.coverpageUrl = idsAndUrlsHolder.getCoverpageUrl();
        this.annexIdUrl = idsAndUrlsHolder.getAnnexIdAndUrl();
        this.docCloneAndOriginIdMap = idsAndUrlsHolder.getDocCloneAndOriginIdMap();
        this.collectionCreated = collectionCreated;
        this.error = error;
    }

    public String getProposalId() {
        return proposalId;
    }

    public String getProposalUrl() {
        return proposalUrl;
    }

    public String getBillId() {
        return billId;
    }

    public String getBillUrl() {
        return billUrl;
    }

    public String getMemorandumId() { return memorandumId; }

    public String getMemorandumUrl() { return memorandumUrl; }

    public String getCoverpageId() {
        return coverpageId;
    }

    public String getCoverpageUrl() {
        return coverpageUrl;
    }

    public Map<String, String> getAnnexIdUrl() { return annexIdUrl; }

    public Map<String, String> getDocCloneAndOriginIdMap() { return docCloneAndOriginIdMap; }

    public boolean isCollectionCreated() {
        return collectionCreated;
    }

    public CreateCollectionError getError() {
        return error;
    }
}