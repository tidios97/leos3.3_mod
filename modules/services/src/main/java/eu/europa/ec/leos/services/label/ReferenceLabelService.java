package eu.europa.ec.leos.services.label;

import eu.europa.ec.leos.domain.common.Result;
import eu.europa.ec.leos.services.label.ref.Ref;
import org.w3c.dom.Node;

import java.util.List;

public interface ReferenceLabelService {
    
    Result<String> generateLabel(List<Ref> refs, Node sourceNode);
    Result<String> generateLabelStringRef(List<String> refs, String sourceDocumentRef, byte[] sourceBytes);
    
    Result<String> generateLabel(List<Ref> refs, String sourceDocumentRef, String sourceRefId, Node sourceNode, boolean capital);
    Result<String> generateLabelStringRef(List<String> refsString, String sourceDocumentRef, String sourceRefId, byte[] sourceBytes);
    
    Result<String> generateLabel(List<Ref> refs, String sourceDocumentRef, String sourceRefId, Node sourceNode, Node targetNode, String targetDocType, boolean withAnchor,
                                 boolean capital);
    Result<String> generateLabelStringRef(List<String> refs, String sourceDocumentRef, String sourceRefId, byte[] sourceBytes, String targetDocumentRef,
                                          boolean capital);

    Result<String> generateSoftMoveLabel(Ref ref, String referenceLocation, Node sourceNode, String direction, String documentRefSource);

    Node getTargetDocument(String targetDocumentRef);

    String getTargetDocumentType(String targetDocumentRef, Node node);
}
