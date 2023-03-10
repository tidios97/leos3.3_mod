package eu.europa.ec.leos.cmis.types;

import java.math.BigInteger;
import java.util.GregorianCalendar;

import org.apache.chemistry.opencmis.commons.definitions.PropertyDefinition;
import org.apache.chemistry.opencmis.commons.enums.Updatability;
import org.apache.chemistry.opencmis.inmemory.types.PropertyCreationHelper;

class LeosPropertyDefinition {

    static final PropertyDefinition<String> LEOS_CATEGORY =
            PropertyCreationHelper.createStringDefinition("leos:category", "LEOS Category", Updatability.READWRITE);

    static final PropertyDefinition<String> LEOS_TEMPLATE =
            PropertyCreationHelper.createStringDefinition("leos:template", "LEOS Template", Updatability.READWRITE);

    static final PropertyDefinition<String> LEOS_LANGUAGE =
            PropertyCreationHelper.createStringDefinition("leos:language", "Document Language", Updatability.READWRITE);

    static final PropertyDefinition<String> LEOS_TITLE =
            PropertyCreationHelper.createStringDefinition("leos:title", "Document Title", Updatability.READWRITE);

    static final PropertyDefinition<String> LEOS_METADATA_REF =
            PropertyCreationHelper.createStringDefinition("metadata:ref", "LEOS REF", Updatability.READWRITE);

    static final PropertyDefinition<String> LEOS_MILESTONE_COMMENTS =
            PropertyCreationHelper.createStringMultiDefinition("leos:milestoneComments", "LEOS milestone comments", Updatability.READWRITE);

    static final PropertyDefinition<String> LEOS_JOB_ID =
            PropertyCreationHelper.createStringDefinition("leos:jobId", "LEOS job id", Updatability.READWRITE);

    static final PropertyDefinition<GregorianCalendar> LEOS_JOB_DATE =
            PropertyCreationHelper.createDateTimeDefinition("leos:jobDate", "LEOS job date", Updatability.READWRITE);

    static final PropertyDefinition<String> LEOS_STATUS =
            PropertyCreationHelper.createStringDefinition("leos:status", "LEOS status", Updatability.READWRITE);
    
    static final PropertyDefinition<String> LEOS_COLLABORATORS =
            PropertyCreationHelper.createStringMultiDefinition("leos:collaborators", "Document Collaborators", Updatability.READWRITE);

    static final PropertyDefinition<String> LEOS_METADATA_DOC_TEMPLATE =
            PropertyCreationHelper.createStringDefinition("metadata:docTemplate", "Document Template", Updatability.READWRITE);

    static final PropertyDefinition<String> LEOS_METADATA_DOC_STAGE =
            PropertyCreationHelper.createStringDefinition("metadata:docStage", "Document Stage", Updatability.READWRITE);

    static final PropertyDefinition<String> LEOS_METADATA_DOC_TYPE =
            PropertyCreationHelper.createStringDefinition("metadata:docType", "Document Type", Updatability.READWRITE);

    static final PropertyDefinition<String> LEOS_METADATA_DOC_PURPOSE =
            PropertyCreationHelper.createStringDefinition("metadata:docPurpose", "Document Purpose", Updatability.READWRITE);

    static final PropertyDefinition<String> LEOS_METADATA_PROCEDURE_TYPE =
            PropertyCreationHelper.createStringDefinition("metadata:procedureType", "Procedure Type", Updatability.READWRITE);

    static final PropertyDefinition<String> LEOS_METADATA_ACT_TYPE =
            PropertyCreationHelper.createStringDefinition("metadata:actType", "Act Type", Updatability.READWRITE);

    static final PropertyDefinition<Boolean> LEOS_METADATA_EEA_RELEVANCE =
            PropertyCreationHelper.createBooleanDefinition("metadata:eeaRelevance", "EEA Relevance", Updatability.READWRITE);

    static final PropertyDefinition<BigInteger> LEOS_ANNEX_DOC_INDEX =
            PropertyCreationHelper.createIntegerDefinition("annex:docIndex", "Annex Index", Updatability.READWRITE);

    static final PropertyDefinition<String> LEOS_ANNEX_DOC_NUMBER =
            PropertyCreationHelper.createStringDefinition("annex:docNumber", "Annex Number", Updatability.READWRITE);

    static final PropertyDefinition<String> LEOS_ANNEX_DOC_TITLE =
            PropertyCreationHelper.createStringDefinition("annex:docTitle", "Annex Title", Updatability.READWRITE);

    static final PropertyDefinition<String> LEOS_ANNEX_CLONED_REF =
            PropertyCreationHelper.createStringDefinition("annex:clonedRef", "Annex cloned ref", Updatability.READWRITE);

    static final PropertyDefinition<String> LEOS_INITIAL_CREATED_BY =
            PropertyCreationHelper.createStringDefinition("leos:initialCreatedBy", "LEOS Initial Created By", Updatability.READWRITE);

    static final PropertyDefinition<GregorianCalendar> LEOS_INITIAL_CREATION_DATE =
            PropertyCreationHelper.createDateTimeDefinition("leos:initialCreationDate", "LEOS Initial Creation Date", Updatability.READWRITE);

    static final PropertyDefinition<String> LEOS_VERSION_LABEL =
            PropertyCreationHelper.createStringDefinition("leos:versionLabel", "LEOS Version Label", Updatability.READWRITE);

    static final PropertyDefinition<BigInteger> LEOS_VERSION_TYPE =
            PropertyCreationHelper.createIntegerDefinition("leos:versionType", "LEOS Version Type", Updatability.READWRITE);

    static final PropertyDefinition<String> LEOS_CONTAINED_DOCUMENTS =
            PropertyCreationHelper.createStringMultiDefinition("leos:containedDocuments", "LEOS contained documents", Updatability.READWRITE);

    static final PropertyDefinition<Boolean> LEOS_CLONED_PROPOSAL =
            PropertyCreationHelper.createBooleanDefinition("leos:clonedProposal", "LEOS cloned proposal", Updatability.READWRITE);

    static final PropertyDefinition<String> LEOS_ORIGIN_REF =
            PropertyCreationHelper.createStringDefinition("leos:originRef", "LEOS origin reference", Updatability.READWRITE);

    static final PropertyDefinition<String> LEOS_CLONED_FROM =
            PropertyCreationHelper.createStringDefinition("leos:clonedFrom", "LEOS original proposal reference", Updatability.READWRITE);

    static final PropertyDefinition<String> LEOS_REVISION_STATUS =
            PropertyCreationHelper.createStringDefinition("leos:revisionStatus", "LEOS revision status", Updatability.READWRITE);

    static final PropertyDefinition<String> LEOS_CLONED_MILESTONE_ID =
            PropertyCreationHelper.createStringMultiDefinition("leos:clonedMilestoneId", "LEOS Cloned milestone id", Updatability.READWRITE);

    static final PropertyDefinition<String> LEOS_CONTRIBUTION_STATUS =
            PropertyCreationHelper.createStringDefinition("leos:contributionStatus", "LEOS contribution status", Updatability.READWRITE);

    static final PropertyDefinition<String> LEOS_COMMENTS =
            PropertyCreationHelper.createStringMultiDefinition("leos:comments", "LEOS comments", Updatability.READWRITE);

    static final PropertyDefinition<String> LEOS_BASE_REVISION_ID =
            PropertyCreationHelper.createStringDefinition("leos:baseRevisionId", "LEOS Base revision document id", Updatability.READWRITE);

    static final PropertyDefinition<Boolean> LEOS_LIVE_DIFFING_REQUIRED =
            PropertyCreationHelper.createBooleanDefinition("leos:liveDiffingRequired", "Live Diffing Flag", Updatability.READWRITE);

}
