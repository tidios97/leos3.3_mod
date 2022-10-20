package eu.europa.ec.leos.cmis.search;

import eu.europa.ec.leos.cmis.mapping.CmisProperties;
import eu.europa.ec.leos.domain.cmis.LeosCategory;
import eu.europa.ec.leos.domain.cmis.LeosLegStatus;
import eu.europa.ec.leos.model.filter.QueryFilter;
import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.ItemIterable;
import org.apache.chemistry.opencmis.client.api.OperationContext;
import org.apache.chemistry.opencmis.client.api.QueryResult;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

abstract public class SearchStrategyImpl implements SearchStrategy {
    
    protected final Session cmisSession;
    
    private static final Logger logger = LoggerFactory.getLogger(SearchStrategyNavigationServices.class);

    public SearchStrategyImpl(Session cmisSession) {
        this.cmisSession = cmisSession;
    }
    
    @Override
    public List<Document> findDocumentsForUser(String userId, String primaryType, String leosAuthority, OperationContext context) {
        logger.trace("Finding documents...");

        String whereClause = CmisProperties.DOCUMENT_CATEGORY.getId() + " IN ('PROPOSAL') AND " + CmisProperties.COLLABORATORS.getId() + " LIKE '" + userId +
                "::" + leosAuthority + "%'" ;
        logger.trace("Ordering by ....." + context.getOrderBy());
        ItemIterable<CmisObject> cmisObjects = cmisSession.queryObjects(primaryType, whereClause, false, context);
        return StreamSupport.stream(cmisObjects.spliterator(), false)
                .map(cmisObject -> (Document) cmisObject)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<Document> findDocumentsByStatus(LeosLegStatus status, String primaryType, OperationContext context) {
        String whereClause = CmisProperties.STATUS.getId() + " IN ('" + status + "')";
        ItemIterable<CmisObject> cmisObjects = cmisSession.queryObjects(primaryType, whereClause, false, context);
        return StreamSupport.stream(cmisObjects.spliterator(), false)
                .map(cmisObject -> (Document) cmisObject)
                .collect(Collectors.toList());
    }
    
    @Override
    public Stream<Document> findDocumentPage(String folderId, String primaryType, Set<LeosCategory> categories,
                                             boolean descendants, boolean allVersion, OperationContext context, int startIndex, QueryFilter workspaceFilter) {
        ItemIterable<CmisObject> cmisObjects = findDocumentIterable(folderId, primaryType, categories, descendants, allVersion, context, workspaceFilter);
        cmisObjects = cmisObjects.skipTo(startIndex).getPage(context.getMaxItemsPerPage());
        return StreamSupport.stream(cmisObjects.spliterator(), false)
                .map(cmisObject -> (Document) cmisObject);
    }

    private String getCountStatement(String primaryType, String whereClause) {
        final StringBuilder statement = new StringBuilder(1024);
        statement.append("SELECT cmis:objectId FROM ");
        statement.append(cmisSession.getTypeDefinition(primaryType).getQueryName());
        if (whereClause.trim().length() > 0) {
            statement.append(" WHERE ");
            statement.append(whereClause);
        }
        return statement.toString();
    }

    private int findCount(String primaryType,  boolean allVersion, OperationContext context, String whereClause) {
        if (primaryType != null && primaryType.trim().length() != 0) {
            context.setMaxItemsPerPage(1);
            String statement = getCountStatement(primaryType, whereClause);
            ItemIterable<QueryResult> resultSet = cmisSession.query(statement, allVersion, context);
            return (int) resultSet.getTotalNumItems();
        } else {
            throw new IllegalArgumentException("Type ID must be set!");
        }
    }

    @Override
    public int findDocumentCount(String folderId, String primaryType, Set<LeosCategory> categories,
                                 boolean descendants, boolean allVersion, OperationContext context, QueryFilter workspaceFilter) {
            String whereClause = QueryUtil.getQuery(folderId, categories, descendants, workspaceFilter);
            return findCount(primaryType, allVersion, context, whereClause);
    }
    
    @Override
    public List<Document> findDocumentsByRef(String ref, String primaryType, OperationContext context) {
        logger.trace("Finding documents by metadataRef...");
        String whereClause = CmisProperties.METADATA_REF.getId() + " = '" + ref + "'";
        ItemIterable<CmisObject> cmisObjects = cmisSession.queryObjects(primaryType, whereClause, false, context);
        return StreamSupport.stream(cmisObjects.spliterator(), false)
                .map(cmisObject -> (Document) cmisObject)
                .collect(Collectors.toList());
    }

    @Override
    public Integer findAllMajorsCount(String primaryType, String docRef, OperationContext context) {
        String whereClause = QueryUtil.getMajorVersionQueryString(docRef);
        ItemIterable<CmisObject> cmisObjects = cmisSession.queryObjects(primaryType, whereClause, true, context);
        return (int) cmisObjects.getTotalNumItems();
    }
    
    @Override
    public Stream<Document> findAllMajors(String primaryType, String docRef, int startIndex, OperationContext context) {
        String whereClause = QueryUtil.getMajorVersionQueryString(docRef);
        ItemIterable<CmisObject> cmisObjects = cmisSession.queryObjects(primaryType, whereClause, true, context);
        cmisObjects = cmisObjects.skipTo(startIndex).getPage(context.getMaxItemsPerPage());
        return StreamSupport.stream(cmisObjects.spliterator(), false)
                .map(cmisObject -> (Document) cmisObject);
    }
    
    @Override
    public List<Document> findVersionsWithoutVersionLabel(String primaryType, String docRef, OperationContext context) {
        String whereClause = QueryUtil.getVersionsWithoutVersionLabelQueryString(docRef);
        ItemIterable<CmisObject> cmisObjects = cmisSession.queryObjects(primaryType, whereClause, true, context);
        return StreamSupport.stream(cmisObjects.spliterator(), false)
                .map(cmisObject -> (Document) cmisObject)
                .collect(Collectors.toList());
    }
    
    @Override
    public Integer findRecentMinorVersionsCount(String primaryType, String documentRef, String lastMajorId, OperationContext context) {
        QueryFilter filter = QueryUtil.getRecentVersionsQuery(documentRef, lastMajorId);
        ItemIterable<CmisObject> cmisObjects = getObjectIterableByFilter(primaryType, filter, true, context);
        return (int) cmisObjects.getTotalNumItems();
    }
    
    @Override
    public Stream<Document> findRecentMinorVersions(String primaryType, String documentRef, String lastMajorId, int startIndex, OperationContext context) {
        QueryFilter filter = QueryUtil.getRecentVersionsQuery(documentRef, lastMajorId);
        ItemIterable<CmisObject> cmisObjects = getObjectIterableByFilter(primaryType, filter, true, context);
        cmisObjects = cmisObjects.skipTo(startIndex).getPage(context.getMaxItemsPerPage());
        return StreamSupport.stream(cmisObjects.spliterator(), false)
                .map(cmisObject -> (Document) cmisObject);
    }
    
    @Override
    public Stream<Document> findAllMinorsForIntermediate(String primaryType, String docRef, String currIntVersion, int startIndex, OperationContext context) {
        QueryFilter filter = QueryUtil.getMinorVersionsQueryFilter(docRef, currIntVersion);
        ItemIterable<CmisObject> cmisObjects = getObjectIterableByFilter(primaryType, filter, true, context);
        cmisObjects = cmisObjects.skipTo(startIndex).getPage(context.getMaxItemsPerPage());
        return StreamSupport.stream(cmisObjects.spliterator(), false)
                .map(cmisObject -> (Document) cmisObject);
    }
    
    @Override
    public int findAllMinorsCountForIntermediate(String primaryType, String docRef, String currIntVersion, OperationContext context) {
        QueryFilter filter = QueryUtil.getMinorVersionsQueryFilter(docRef, currIntVersion);
        ItemIterable<CmisObject> cmisObjects = getObjectIterableByFilter(primaryType, filter, true, context);
        return (int) cmisObjects.getTotalNumItems();
    }
    
    private ItemIterable<CmisObject> getObjectIterableByFilter(String primaryType, QueryFilter workspaceFilter, boolean allVersion, OperationContext context) {
        String whereClause = QueryUtil.formFilterClause(workspaceFilter);
        context.setOrderBy(QueryUtil.formSortClause(workspaceFilter));
        logger.debug("Querying CMIS objects... [primaryType={}, where={}]", primaryType, whereClause);
        return cmisSession.queryObjects(primaryType, whereClause, allVersion, context);
    }
    
    private ItemIterable<CmisObject> findDocumentIterable(String folderId, String primaryType, Set<LeosCategory> categories,
                                                            boolean descendants, boolean allVersion, OperationContext context, QueryFilter workspaceFilter) {
        String whereClause = QueryUtil.getQuery(folderId, categories, descendants, workspaceFilter);
        context.setOrderBy(QueryUtil.formSortClause(workspaceFilter));
        logger.debug("Querying CMIS objects... [primaryType={}, where={}]", primaryType, whereClause);
        return cmisSession.queryObjects(primaryType, whereClause, allVersion, context);
        
    }
    
    @Override
    public Stream<Document> findFirstVersion(String primaryType, String documentRef, OperationContext context) {
        QueryFilter filter = QueryUtil.getVersionsGreaterThanQuery(documentRef, "0.0.0", false);
        ItemIterable<CmisObject> cmisObjects = getObjectIterableByFilter(primaryType, filter, true, context);
        cmisObjects = cmisObjects.skipTo(0).getPage(1);
        return StreamSupport.stream(cmisObjects.spliterator(), false)
                .map(cmisObject -> (Document) cmisObject);
    }

    @Override
    public Stream<Document> findDocumentByVersion(String primaryType, String documentRef, String versionLabel, OperationContext context) {
        QueryFilter filter = QueryUtil.getVersionEqualQuery(documentRef, versionLabel);
        ItemIterable<CmisObject> cmisObjects = getObjectIterableByFilter(primaryType, filter, true, context);
        cmisObjects = cmisObjects.skipTo(0).getPage(1);
        return StreamSupport.stream(cmisObjects.spliterator(), false)
                .map(cmisObject -> (Document) cmisObject);
    }

    @Override
    public Stream<Document> findDocumentByNameAndFolder(String folderId, String name, String primaryType, OperationContext context) {
        logger.trace("Finding documents by name and folder...");
        String whereClause = "IN_FOLDER('" + folderId + "') AND " + PropertyIds.NAME + " = '" + name + "'";
        ItemIterable<CmisObject> cmisObjects = cmisSession.queryObjects(primaryType, whereClause, false, context);
        return StreamSupport.stream(cmisObjects.spliterator(), false)
                .map(cmisObject -> (Document) cmisObject);
    }
}
