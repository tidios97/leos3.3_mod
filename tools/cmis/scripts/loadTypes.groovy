import org.apache.chemistry.opencmis.commons.*
import org.apache.chemistry.opencmis.commons.data.*
import org.apache.chemistry.opencmis.commons.definitions.*
import org.apache.chemistry.opencmis.commons.enums.*
import org.apache.chemistry.opencmis.client.api.*
import org.apache.chemistry.opencmis.client.util.*

// NOTE set variable with full path on local file system where types .xml files are stored
// ex. C:/devel/sources/pilot/tools/cmis/chemistry-opencmis-server-inmemory/src/main/resources/leos/types
def typesLocalPath = '<TODO>'

def loader = new LeosTypesLoader(typesLocalPath, session)
loader.loadTypes()

class LeosTypesLoader {

    final String TYPES_LOCAL_PATH
    final Session SESSION

    LeosTypesLoader(String typesLocalPath, Session session) {
        this.TYPES_LOCAL_PATH = typesLocalPath
        this.SESSION = session
    }

    void loadTypes() {
        println 'Loading LEOS CMIS types...'
        loadType('cmis:document', TYPES_LOCAL_PATH + '/01_leos_document.xml')
        loadType('leos:document', TYPES_LOCAL_PATH + '/02_leos_xml.xml')
        loadType('leos:document', TYPES_LOCAL_PATH + '/02_leos_media.xml')
        loadType('leos:document', TYPES_LOCAL_PATH + '/02_leos_config.xml')
        loadType('leos:document', TYPES_LOCAL_PATH + '/02_leos_leg.xml')
        println '...finished!'
    }

    private void loadType(String parentTypeId, String xmlFileTypeDefinition) {
        ObjectType parentType = SESSION.getTypeDefinition(parentTypeId);
        TypeMutability typeMutability = parentType.getTypeMutability();
        if (typeMutability != null && Boolean.TRUE.equals(typeMutability.canCreate())){
            InputStream xmlFileStream = new FileInputStream(xmlFileTypeDefinition);
            TypeDefinition typeDefinition = TypeUtils.readFromXML(xmlFileStream);
            ObjectType createdType = SESSION.createType(typeDefinition);
        }
    }

}
