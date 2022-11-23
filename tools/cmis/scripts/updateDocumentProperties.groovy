import org.apache.chemistry.opencmis.commons.*
import org.apache.chemistry.opencmis.commons.data.*
import org.apache.chemistry.opencmis.commons.enums.*
import org.apache.chemistry.opencmis.client.api.*

Folder folder = (Folder) session.getObjectByPath("/leos/workspaces")
updateDocumentProperties(folder)

def updateDocumentProperties(Folder folder) {
    folder.getChildren().each { child ->
        if (child instanceof Folder) {
            updateDocumentProperties(child);
        } else if ((child instanceof Document)) {
            println("Document inside folder " + folder.name + " (" + folder.id + ") docName " + child.name);

            Map<String, Object> updateProperties = new HashMap<String, Object>();
            String value = child.getProperty("metadata:docType").getValue();
            if(value.equalsIgnoreCase( "REGULATION OF THE EUROPEAN PARLIAMENT AND OF THE COUNCIL")) {
                updateProperties.put("metadata:procedureType", "ORDINARY_LEGISLATIVE_PROC");
                updateProperties.put("metadata:actType", "LEGISLATIVE_ACTS");
                child.updateProperties(updateProperties);
            }else if( value.equalsIgnoreCase( "COUNCIL DECISION"))
            {
                updateProperties.put("metadata:procedureType", "COUNCIL_LEGAL_ACTS");
                updateProperties.put("metadata:actType", "LEGISLATIVE_ACTS");
                child.updateProperties(updateProperties);
            }
        }
    }
}