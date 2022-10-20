package eu.europa.ec.leos.services.support.url;

import java.util.HashMap;
import java.util.Map;

public class CollectionIdsAndUrlsHolder {
    private String proposalId;
    private String proposalUrl;
    private String billId;
    private String billUrl;
    private String memorandumId;
    private String memorandumUrl;
    private String coverpageId;
    private String coverpageUrl;
    private Map<String,String> annexIdAndUrl;
    private Map<String, String> docCloneAndOriginIdMap;

    public CollectionIdsAndUrlsHolder() {
        this.annexIdAndUrl = new HashMap<>();
        this.docCloneAndOriginIdMap = new HashMap<>();
    }

    public String getProposalId() { return proposalId; }

    public void setProposalId(String proposalId) { this.proposalId = proposalId; }

    public String getProposalUrl() {return proposalUrl; }

    public void setProposalUrl(String proposalUrl) { this.proposalUrl = proposalUrl; }

    public String getBillId() { return billId; }

    public void setBillId(String billId) { this.billId = billId; }

    public String getBillUrl() {
        return billUrl;
    }

    public void setBillUrl(String billUrl) {
        this.billUrl = billUrl;
    }

    public String getMemorandumId() { return memorandumId; }

    public void setMemorandumId(String memorandumId) { this.memorandumId = memorandumId; }

    public String getMemorandumUrl() {
        return memorandumUrl;
    }

    public void setMemorandumUrl(String memorandumUrl) {
        this.memorandumUrl = memorandumUrl;
    }

    public String getCoverpageId() { return coverpageId; }

    public void setCoverpageId(String coverpageId) { this.coverpageId = coverpageId; }

    public String getCoverpageUrl() {
        return coverpageUrl;
    }

    public void setCoverpageUrl(String coverpageUrl) {
        this.coverpageUrl = coverpageUrl;
    }

    public Map<String, String> getAnnexIdAndUrl() {
        return annexIdAndUrl;
    }

    public void addAnnexIdAndUrl(String id, String annexUrl) {
        this.annexIdAndUrl.put(id, annexUrl);
    }

    public Map<String, String> getDocCloneAndOriginIdMap() { return docCloneAndOriginIdMap; }

    public void addDocCloneAndOriginIdMap(String cloneId, String originId) {
        this.docCloneAndOriginIdMap.put(cloneId, originId);
    }
}