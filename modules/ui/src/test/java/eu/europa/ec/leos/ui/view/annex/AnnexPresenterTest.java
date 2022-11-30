/*
 * Copyright 2017 European Commission
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
package eu.europa.ec.leos.ui.view.annex;

import com.vaadin.server.VaadinServletService;
import eu.europa.ec.leos.domain.cmis.Content;
import eu.europa.ec.leos.domain.cmis.Content.Source;
import eu.europa.ec.leos.domain.cmis.LeosCategory;
import eu.europa.ec.leos.domain.cmis.LeosPackage;
import eu.europa.ec.leos.domain.cmis.common.VersionType;
import eu.europa.ec.leos.domain.cmis.document.Annex;
import eu.europa.ec.leos.domain.cmis.document.Proposal;
import eu.europa.ec.leos.domain.cmis.document.XmlDocument;
import eu.europa.ec.leos.domain.cmis.metadata.AnnexMetadata;
import eu.europa.ec.leos.domain.cmis.metadata.ProposalMetadata;
import eu.europa.ec.leos.domain.common.TocMode;
import eu.europa.ec.leos.domain.vo.DocumentVO;
import eu.europa.ec.leos.i18n.MessageHelper;
import eu.europa.ec.leos.model.user.Entity;
import eu.europa.ec.leos.model.user.User;
import eu.europa.ec.leos.security.LeosPermission;
import eu.europa.ec.leos.security.SecurityContext;
import eu.europa.ec.leos.services.clone.CloneContext;
import eu.europa.ec.leos.services.template.TemplateStructureService;
import eu.europa.ec.leos.services.processor.AnnexProcessor;
import eu.europa.ec.leos.services.document.DocumentContentService;
import eu.europa.ec.leos.services.processor.ElementProcessor;
import eu.europa.ec.leos.services.document.AnnexService;
import eu.europa.ec.leos.services.document.ContributionService;
import eu.europa.ec.leos.services.document.ProposalService;
import eu.europa.ec.leos.services.messaging.UpdateInternalReferencesProducer;
import eu.europa.ec.leos.services.store.PackageService;
import eu.europa.ec.leos.services.toc.StructureContext;
import eu.europa.ec.leos.services.toc.StructureServiceImpl;
import eu.europa.ec.leos.test.support.model.ModelHelper;
import eu.europa.ec.leos.test.support.web.presenter.LeosPresenterTest;
import eu.europa.ec.leos.ui.event.search.ShowConfirmDialogEvent;
import eu.europa.ec.leos.ui.support.CoEditionHelper;
import eu.europa.ec.leos.vo.coedition.CoEditionActionInfo;
import eu.europa.ec.leos.vo.coedition.CoEditionActionInfo.Operation;
import eu.europa.ec.leos.vo.coedition.CoEditionVO;
import eu.europa.ec.leos.vo.toc.NumberingConfig;
import eu.europa.ec.leos.vo.toc.TableOfContentItemVO;
import eu.europa.ec.leos.vo.toc.TocItem;
import eu.europa.ec.leos.web.event.NavigationRequestEvent;
import eu.europa.ec.leos.web.event.view.document.CloseDocumentEvent;
import eu.europa.ec.leos.web.event.view.document.DeleteElementRequestEvent;
import eu.europa.ec.leos.web.event.view.document.EditElementRequestEvent;
import eu.europa.ec.leos.web.event.view.document.InsertElementRequestEvent;
import eu.europa.ec.leos.web.event.view.document.RefreshDocumentEvent;
import eu.europa.ec.leos.web.event.view.document.SaveElementRequestEvent;
import eu.europa.ec.leos.web.support.SessionAttribute;
import eu.europa.ec.leos.web.support.UrlBuilder;
import eu.europa.ec.leos.web.support.UuidHelper;
import eu.europa.ec.leos.web.support.user.UserHelper;
import eu.europa.ec.leos.web.ui.navigation.Target;
import io.atlassian.fugue.Option;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

import javax.inject.Provider;

import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static eu.europa.ec.leos.services.support.XmlHelper.LEVEL;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.sameInstance;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.hamcrest.MockitoHamcrest.argThat;

public class AnnexPresenterTest extends LeosPresenterTest {

    private static final String PRESENTER_ID = "ab51f419-c6b0-45cb-82ed-77a61099b58f";

    private SecurityContext securityContext;

    private User contextUser;

    private UuidHelper uuidHelper;

    @Mock
    private AnnexScreen annexScreen;

    @Mock
    private AnnexService annexService;

    @Mock
    private ContributionService contributionService;

    @Mock
    private ElementProcessor<Annex> elementProcessor;

    @Mock
    private AnnexProcessor annexProcessor;

    @Mock
    private DocumentContentService documentContentService;

    @Mock
    private UrlBuilder urlBuilder;

    @Mock
    private UserHelper userHelper;

    @Mock
    private CoEditionHelper coEditionHelper;

    @Mock
    private MessageHelper messageHelper;

    @Mock
    private TemplateStructureService templateStructureService;

    @InjectMocks
    private StructureServiceImpl structureServiceImpl = Mockito.spy(new StructureServiceImpl());

    @InjectMocks
    private AnnexPresenter annexPresenter;

    @Mock
    private Provider<StructureContext> structureContextProvider;

    @Mock
    private StructureContext structureContext;

    @Mock
    private UpdateInternalReferencesProducer updateInternalReferencesProducer;

    @Mock
    private PackageService packageService;
    
    @Mock
    private ProposalService proposalService;

    @Mock
    private CloneContext cloneContext;

    private String docRef;
    private String docId;
    private String docTitle;
    private String docNumber;
    private byte[] byteContent;

    private List<TocItem> tocItems;
    private List<NumberingConfig> numberingConfigs;
    String docTemplate = "SG-017";
    private Map<TocItem, List<TocItem>> tocRules;

    @Before
    public void setup(){
        contextUser = ModelHelper.buildUser(45L, "login", "name");
        securityContext = mock(SecurityContext.class);
        when(securityContext.getUser()).thenReturn(contextUser);
        uuidHelper = mock(UuidHelper.class);
        when(uuidHelper.getRandomUUID()).thenReturn(PRESENTER_ID);

        super.setup();

        byte[] bytesFile = getFileContent("/structure-test2.xml");
        when(templateStructureService.getStructure(docTemplate)).thenReturn(bytesFile);
        ReflectionTestUtils.setField(structureServiceImpl, "structureSchema", "toc/schema/structure_1.xsd");

        tocItems = structureServiceImpl.getTocItems(docTemplate);
        tocRules = structureServiceImpl.getTocRules(docTemplate);
        numberingConfigs = structureServiceImpl.getNumberingConfigs(docTemplate);

        when(structureContextProvider.get()).thenReturn(structureContext);
        when(structureContext.getTocItems()).thenReturn(tocItems);
        when(structureContext.getNumberingConfigs()).thenReturn(numberingConfigs);
        when(structureContext.getTocRules()).thenReturn(tocRules);
    }

    @Before
    public void init() {
        docRef = "annex_test.xml";
        docId = "555";
        docTitle = "document title";
        docNumber = "Annex 1";
        byteContent = new byte[]{1, 2, 3};
        when(httpSession.getAttribute(eq("annex#" + docRef))).thenReturn(null);
        when(httpSession.getAttribute(eq(annexPresenter.getId() + "." + SessionAttribute.ANNEX_REF.name()))).thenReturn(docRef);
    }

    @Test
    public void testEnterDocumentView() {
        Content content = mock(Content.class);
        Source source = mock(Source.class);
        String documentVersion="0.0.1";

        when(source.getBytes()).thenReturn(byteContent);
        when(content.getSource()).thenReturn(source);

        AnnexMetadata annexMetadata = new AnnexMetadata("", "REGULATION", "", "AN-000.xml",
                "EN", docTemplate, "annex-id", 1, "Annex 1", docTitle, "", "0.0.1", false, null);
        Instant now = Instant.now();
        Annex annex = getMockedAnnex(content, documentVersion, annexMetadata);
        DocumentVO annexVO = new DocumentVO(docId,"EN", LeosCategory.ANNEX, "login",  Date.from(now));
        annexVO.setDocNumber(1);
        annexVO.setTitle(docTitle);
        annexVO.addCollaborators(Collections.emptyList());
        String displayableContent = "document displayable content";
        List<Entity> entities = new ArrayList<Entity>();
        entities.add(new Entity("1", "DIGIT.B2", "DIGIT"));
        User user = ModelHelper.buildUser(45L, "login", "name", entities);
        List<TableOfContentItemVO> tableOfContentItemVoList = Collections.emptyList();
        List<LeosPermission> permissions = Collections.emptyList();
        List<CoEditionVO> coEditionVos = Collections.emptyList();
        
        LeosPackage leosPackage = new LeosPackage(docId, "Proposal", "");
        String proposalId = "878";
        ProposalMetadata proposalMetadata = new ProposalMetadata("", "REGULATION for EC", "",
                "PR-00.xml", "EN", "", "proposal-id", "", "0.1.0", false);
        Proposal leosProposal = new Proposal(proposalId, "Proposal", "login", Instant.now(),
                "login", Instant.now(),
                "", "", "", "", VersionType.MAJOR, true,
                "REGULATION for EC", Collections.emptyList(), Arrays.asList(""), "login", Instant.now(),
                Option.some(content), Option.some(proposalMetadata), false, "", "",
                "", null, null);

        when(annexScreen.isCoverPageVisible()).thenReturn(true);
        when(annexService.findAnnexByRef(docRef)).thenReturn(annex);
        when(userHelper.getUser("login")).thenReturn(user);
        when(securityContext.getPermissions(annex)).thenReturn(permissions);
        when(messageHelper.getMessage("operation.metadata.updated")).thenReturn("Metadata updated");
        when(annexService.updateAnnex(annex, byteContent, messageHelper.getMessage("operation.metadata.updated"))).
                thenReturn(annex);
        when(documentContentService.isCoverPageExists(leosProposal.getContent().get().getSource().getBytes())).
                thenReturn(false);
        when(documentContentService.getCoverPageContent(leosProposal.getContent().get().getSource().getBytes())).
                thenReturn(byteContent);
        when(documentContentService.toEditableContent(isA(XmlDocument.class), any(), any(), any())).
                thenReturn(displayableContent);
        when(annexService.getTableOfContent(annex, TocMode.SIMPLIFIED)).thenReturn(tableOfContentItemVoList);
        
        when(packageService.findPackageByDocumentId(annex.getId())).thenReturn(leosPackage);
        when(proposalService.findProposalByPackagePath(leosPackage.getPath())).thenReturn(leosProposal);
        when(userHelper.getCollaboratorConnectedEntityByLoggedUser(leosProposal.getCollaborators())).thenReturn("SG");
        when(cloneContext.isClonedProposal()).thenReturn(false);

        // DO THE ACTUAL CALL
        annexPresenter.enter();

        verify(annexService, times(4)).findAnnexByRef(docRef);
        verify(annexService).getTableOfContent(annex, TocMode.SIMPLIFIED);
        verify(userHelper).getUser("login");

        verify(packageService, times(2)).findPackageByDocumentId(annex.getId());
        verify(proposalService, times(2)).findProposalByPackagePath(leosPackage.getPath());
        verify(userHelper).getCollaboratorConnectedEntityByLoggedUser(leosProposal.getCollaborators());

        verify(annexScreen).setDocumentVersionInfo(any());
        verify(documentContentService).isCoverPageExists(leosProposal.getContent().get().getSource().getBytes());
        verify(documentContentService).getCoverPageContent(leosProposal.getContent().get().getSource().getBytes());
        verify(documentContentService).toEditableContent(any(XmlDocument.class), any(), any(), any());

        verify(annexScreen, times(1)).isCoverPageVisible();
        verify(annexScreen).setContent(displayableContent);
        verify(annexScreen).setTitle(docTitle, docNumber);
        verify(annexScreen).setToc(argThat(sameInstance(tableOfContentItemVoList)));
        verify(annexScreen).setPermissions(argThat(org.hamcrest.Matchers.hasProperty("id",equalTo(annexVO.getId()))), eq(false));
        verify(annexScreen).initAnnotations(argThat(org.hamcrest.Matchers.hasProperty("id",equalTo(annexVO.getId()))), any(), any());
        verify(annexScreen).updateUserCoEditionInfo(coEditionVos, PRESENTER_ID);
        verify(annexScreen).setStructureChangeMenuItem();
        verify(annexService).getAllVersions(docId, docRef);
        verify(contributionService).getDocumentContributions(docId, 1, Annex.class);
        verify(annexScreen).setDataFunctions(any(), any(), any(), any(), any(), any(), any(), any(), any());
        verifyNoMoreInteractions(userHelper, annexService, documentContentService, annexScreen);
    }

    @Test
    public void testEnterDocumentView_clonedProposal() {
        Content content = mock(Content.class);
        Source source = mock(Source.class);
        String documentVersion="0.0.1";

        when(source.getBytes()).thenReturn(byteContent);
        when(content.getSource()).thenReturn(source);

        AnnexMetadata annexMetadata = new AnnexMetadata("", "REGULATION", "", "AN-000.xml",
                "EN", docTemplate, "annex-id", 1, "Annex 1", docTitle, "", "0.0.1", false, null);
        Instant now = Instant.now();
        Annex annex = getMockedAnnex(content, documentVersion, annexMetadata);
        DocumentVO annexVO = new DocumentVO(docId,"EN", LeosCategory.ANNEX, "login",  Date.from(now));
        annexVO.setDocNumber(1);
        annexVO.setTitle(docTitle);
        annexVO.addCollaborators(Collections.emptyList());
        String displayableContent = "document displayable content";
        List<Entity> entities = new ArrayList<Entity>();
        entities.add(new Entity("1", "DIGIT.B2", "DIGIT"));
        User user = ModelHelper.buildUser(45L, "login", "name", entities);
        List<TableOfContentItemVO> tableOfContentItemVoList = Collections.emptyList();
        List<LeosPermission> permissions = Collections.emptyList();
        List<CoEditionVO> coEditionVos = Collections.emptyList();

        LeosPackage leosPackage = new LeosPackage(docId, "Proposal", "");
        String proposalId = "878";
        ProposalMetadata proposalMetadata = new ProposalMetadata("", "REGULATION for EC", "",
                "PR-00.xml", "EN", "", "proposal-id", "", "0.1.0", false);
        Proposal leosProposal = new Proposal(proposalId, "Proposal", "login", Instant.now(), "login", Instant.now(),
                "", "", "", "", VersionType.MAJOR, true,
                "REGULATION for EC", Collections.emptyList(), Arrays.asList(""), "login", Instant.now(),
                Option.some(content), Option.some(proposalMetadata), true, "", "",
                "", null, null);

        when(annexScreen.isCoverPageVisible()).thenReturn(true);
        when(annexService.findAnnexByRef(docRef)).thenReturn(annex);
        when(userHelper.getUser("login")).thenReturn(user);
        when(securityContext.getPermissions(annex)).thenReturn(permissions);
        when(messageHelper.getMessage("operation.metadata.updated")).thenReturn("Metadata updated");
        when(annexService.updateAnnex(annex, byteContent, messageHelper.getMessage("operation.metadata.updated"))).
                thenReturn(annex);
        when(documentContentService.isCoverPageExists(leosProposal.getContent().get().getSource().getBytes())).
                thenReturn(false);
        when(documentContentService.getCoverPageContent(leosProposal.getContent().get().getSource().getBytes())).
                thenReturn(byteContent);
        when(documentContentService.toEditableContent(isA(XmlDocument.class), any(), any(), any())).
                thenReturn(displayableContent);
        when(annexService.getTableOfContent(annex, TocMode.SIMPLIFIED)).thenReturn(tableOfContentItemVoList);

        when(packageService.findPackageByDocumentId(annex.getId())).thenReturn(leosPackage);
        when(proposalService.findProposalByPackagePath(leosPackage.getPath())).thenReturn(leosProposal);
        when(userHelper.getCollaboratorConnectedEntityByLoggedUser(leosProposal.getCollaborators())).thenReturn("SG");
        when(cloneContext.isClonedProposal()).thenReturn(true);

        // DO THE ACTUAL CALL
        annexPresenter.enter();

        verify(annexService, times(4)).findAnnexByRef(docRef);
        verify(annexService).getTableOfContent(annex, TocMode.SIMPLIFIED);
        verify(userHelper).getUser("login");

        verify(packageService, times(2)).findPackageByDocumentId(annex.getId());
        verify(proposalService, times(2)).findProposalByPackagePath(leosPackage.getPath());
        verify(proposalService).getClonedProposalMetadata(byteContent);
        verify(userHelper).getCollaboratorConnectedEntityByLoggedUser(leosProposal.getCollaborators());

        verify(annexScreen).setDocumentVersionInfo(any());
        verify(documentContentService).isCoverPageExists(leosProposal.getContent().get().getSource().getBytes());
        verify(documentContentService).getCoverPageContent(leosProposal.getContent().get().getSource().getBytes());
        verify(documentContentService).toEditableContent(any(XmlDocument.class), any(), any(), any());

        verify(annexScreen, times(1)).isCoverPageVisible();
        verify(annexScreen).setContent(displayableContent);
        verify(annexScreen).setTitle(docTitle, docNumber);
        verify(annexScreen).setToc(argThat(sameInstance(tableOfContentItemVoList)));
        verify(annexScreen).setPermissions(argThat(org.hamcrest.Matchers.hasProperty("id",equalTo(annexVO.getId()))), eq(true));
        verify(annexScreen).initAnnotations(argThat(org.hamcrest.Matchers.hasProperty("id",equalTo(annexVO.getId()))), any(), any());
        verify(annexScreen).updateUserCoEditionInfo(coEditionVos, PRESENTER_ID);
        verify(annexScreen).setStructureChangeMenuItem();
        verify(contributionService).getDocumentContributions(docId, 1, Annex.class);
        verify(annexScreen).setDataFunctions(any(), any(), any(), any(), any(), any(), any(), any(), any());
        verify(annexService).getAllVersions(docId, docRef);
        verify(cloneContext, Mockito.times(8)).setCloneProposalMetadataVO(any());
        verifyNoMoreInteractions(userHelper, annexService, documentContentService, annexScreen);
    }

    private Annex getMockedAnnex(Content content, String documentVersion, AnnexMetadata annexMetadata) {
        return new Annex(docId, "Annex", "login", Instant.now(), "login", Instant.now(),
                    documentVersion, "", documentVersion, "", VersionType.MINOR, true,
                    docTitle, Collections.emptyList(), Arrays.asList(""), docId+"0.1.0"+"Document Created", "", "",
                    Option.some(content), Option.some(annexMetadata));
    }
    
    @Test
    public void testCloseDocument() {
        // DO THE ACTUAL CALL
        annexPresenter.handleCloseDocument(new CloseDocumentEvent());
        verify(eventBus).post(argThat(Matchers.<NavigationRequestEvent>hasProperty("target", equalTo(Target.PREVIOUS))));
    }

    @Test
    public void testCloseDocumentUnsavedAnnexInSession() {
        AnnexMetadata annexMetadata = new AnnexMetadata("", "REGULATION", "", "AN-000.xml", "EN", docTemplate, "annex-id", 1, "Annex 1", docTitle, "", "0.0.1", false, null);

        Annex annex = getMockedAnnex(mock(Content.class), "0.0.1", annexMetadata);
        when(httpSession.getAttribute(eq("annex#" + docRef))).thenReturn(annex);

        // DO THE ACTUAL CALL
        annexPresenter.handleCloseDocument(new CloseDocumentEvent());
        verify(eventBus).post(any(ShowConfirmDialogEvent.class));
    }

    @Test
    public void testRefreshDocument() {
        Content content = mock(Content.class);
        Source source = mock(Source.class);
        String documentVersion="0.0.1";

        when(source.getBytes()).thenReturn(byteContent);
        when(content.getSource()).thenReturn(source);
        Instant now = Instant.now();
        AnnexMetadata annexMetadata = new AnnexMetadata("", "REGULATION", "", "AN-000.xml", "EN", docTemplate, "annex-id", 1, "Annex 1", docTitle, "", "0.0.1", false, null);
        Annex annex = getMockedAnnex(content, documentVersion, annexMetadata);
        DocumentVO annexVO = new DocumentVO(docId,"EN", LeosCategory.ANNEX, "login",  Date.from(now));
        annexVO.setDocNumber(1);
        annexVO.setTitle(docTitle);
        String displayableContent = "document displayable content";
        List<Entity> entities = new ArrayList<Entity>();
        entities.add(new Entity("1", "DIGIT.B2", "DIGIT"));
        User user = ModelHelper.buildUser(45L, "login", "name", entities);
        List<TableOfContentItemVO> tableOfContentItemVoList = Collections.emptyList();
        List<LeosPermission> permissions = Collections.emptyList();
        List<CoEditionVO> coEditionVos = Collections.emptyList();

        LeosPackage leosPackage = new LeosPackage(docId, "Proposal", "");
        String proposalId = "878";
        ProposalMetadata proposalMetadata = new ProposalMetadata("", "REGULATION for EC", "",
                "PR-00.xml", "EN", "", "proposal-id", "", "0.1.0", false);
        Proposal leosProposal = new Proposal(proposalId, "Proposal", "login", Instant.now(),
                "login", Instant.now(),
                "", "", "", "", VersionType.MAJOR, true,
                "REGULATION for EC", Collections.emptyList(), Arrays.asList(""), "login", Instant.now(),
                Option.some(content), Option.some(proposalMetadata), false, "", "",
                "", null, null);

        when(annexScreen.isCoverPageVisible()).thenReturn(true);
        when(annexService.findAnnexByRef(docRef)).thenReturn(annex);
        when(urlBuilder.getWebAppPath(VaadinServletService.getCurrentServletRequest())).thenReturn("");
        when(userHelper.getUser("login")).thenReturn(user);
        when(securityContext.getPermissions(annex)).thenReturn(permissions);
        when(packageService.findPackageByDocumentId(annex.getId())).thenReturn(leosPackage);
        when(proposalService.findProposalByPackagePath(leosPackage.getPath())).thenReturn(leosProposal);
        when(documentContentService.getCoverPageContent(leosProposal.getContent().get().getSource().getBytes())).
                thenReturn(byteContent);
        when(documentContentService.toEditableContent(isA(XmlDocument.class), any(), any(), any())).thenReturn(displayableContent);
        when(annexService.getTableOfContent(annex, TocMode.SIMPLIFIED)).thenReturn(tableOfContentItemVoList);
        when(cloneContext.isClonedProposal()).thenReturn(false);

        // DO THE ACTUAL CALL
        annexPresenter.refreshDocument(new RefreshDocumentEvent());

        verify(annexService, times(2)).findAnnexByRef(docRef);
        verify(annexService).getTableOfContent(annex, TocMode.SIMPLIFIED);
        verify(userHelper).getUser("login");
        verify(packageService).findPackageByDocumentId(annex.getId());
        verify(proposalService).findProposalByPackagePath(leosPackage.getPath());

        verify(documentContentService).getCoverPageContent(leosProposal.getContent().get().getSource().getBytes());
        verify(documentContentService).toEditableContent(any(XmlDocument.class), any(), any(), any());
        verify(documentContentService).isCoverPageExists(any());

        verify(annexScreen).isCoverPageVisible();
        verify(annexScreen).setDocumentVersionInfo(any());
        verify(annexScreen).setContent(displayableContent);
        verify(annexScreen).setTitle(docTitle, docNumber);
        verify(annexScreen).setToc(argThat(sameInstance(tableOfContentItemVoList)));
        verify(annexScreen).setPermissions(argThat(org.hamcrest.Matchers.hasProperty("id",equalTo(annexVO.getId()))), eq(false));
        verify(annexScreen).initAnnotations(argThat(org.hamcrest.Matchers.hasProperty("id",equalTo(annexVO.getId()))), any(), any());
        verify(annexScreen).updateUserCoEditionInfo(coEditionVos, PRESENTER_ID);
        verify(annexScreen).setStructureChangeMenuItem();
        verifyNoMoreInteractions(userHelper, annexService, documentContentService, annexScreen);
    }

    @Test
    public void testDeleteAnnexBlock() throws Exception {
        Content content = mock(Content.class);
        Source source = mock(Source.class);

        when(source.getBytes()).thenReturn(byteContent);
        when(content.getSource()).thenReturn(source);

        AnnexMetadata annexMetadata = new AnnexMetadata("", "REGULATION", "", "AN-000.xml", "EN", docTemplate, "annex-id", 1, "Annex 1", docTitle, "", "0.0.1", false, null);
    
        Annex originalDocument = getMockedAnnex(content, "", annexMetadata);
    
        byte[] updatedBytes = new byte[] {'1','2'};
        Annex savedDocument = getMockedAnnex(content, "", annexMetadata);
    
        String elementTag = LEVEL;
        String elementId = "486";

        when(annexService.findAnnexByRef(docRef)).thenReturn(originalDocument);
        when(annexProcessor.deleteAnnexBlock(originalDocument, elementId, elementTag)).thenReturn(updatedBytes);
        when(annexService.updateAnnex(originalDocument, updatedBytes, VersionType.MINOR, messageHelper.getMessage("operation.annex.block.deleted"))).thenReturn(savedDocument);

        // DO THE ACTUAL CALL
        annexPresenter.deleteElement(new DeleteElementRequestEvent(elementId, elementTag ));

        verify(annexProcessor).deleteAnnexBlock(originalDocument, elementId, elementTag);
        verify(annexService).findAnnexByRef(docRef);
        verify(annexService).updateAnnex(originalDocument, updatedBytes, VersionType.MINOR, messageHelper.getMessage("operation.annex.block.deleted"));
        verifyNoMoreInteractions(annexService, elementProcessor);
    }

    @Test
    public void testInsertAnnexBlock_Before() {

        boolean before = true;

        Content content = mock(Content.class);
        Source source = mock(Source.class);

        when(source.getBytes()).thenReturn(byteContent);
        when(content.getSource()).thenReturn(source);

        AnnexMetadata annexMetadata = new AnnexMetadata("", "REGULATION", "", "AN-000.xml", "EN", docTemplate, "annex-id", 1, "Annex 1", docTitle, "", "0.0.1", false, null);
    
        Annex originalDocument = getMockedAnnex(content, "", annexMetadata);
    
        byte[] updatedBytes = new byte[] {'1','2'};
        Annex savedDocument = getMockedAnnex(content, "", annexMetadata);
    
        String elementId = "486";
        String elementTag = LEVEL;

        when(annexService.findAnnexByRef(docRef)).thenReturn(originalDocument);
        when(annexProcessor.insertAnnexBlock(originalDocument, elementId, elementTag, before)).thenReturn(updatedBytes);
        when(annexService.updateAnnex(originalDocument, updatedBytes, VersionType.MINOR, messageHelper.getMessage("operation.annex.block.inserted"))).thenReturn(savedDocument);

        // DO THE ACTUAL CALL
        annexPresenter.insertElement(new InsertElementRequestEvent(elementId, elementTag, InsertElementRequestEvent.POSITION.BEFORE));

        verify(annexProcessor).insertAnnexBlock(originalDocument, elementId, elementTag, before);
        verify(annexService).findAnnexByRef(docRef);
        verify(annexService).updateAnnex(originalDocument, updatedBytes, VersionType.MINOR, messageHelper.getMessage("operation.annex.block.inserted"));
        verifyNoMoreInteractions(annexService, elementProcessor);
    }

    @Test
    public void testEditAnnexBlock() {

        Content content = mock(Content.class);
        Source source = mock(Source.class);

        when(source.getBytes()).thenReturn(byteContent);
        when(content.getSource()).thenReturn(source);

        AnnexMetadata annexMetadata = new AnnexMetadata("", "REGULATION", "", "AN-000.xml", "EN", docTemplate, "annex-id", 1, "Annex 1", docTitle, "", "0.0.1", false, null);
    
        Annex document = getMockedAnnex(content, "", annexMetadata);
        String elementTag = LEVEL;
        String elementId = "486";
        CoEditionActionInfo actionInfo = new CoEditionActionInfo(true, Operation.STORE, null, new ArrayList<CoEditionVO>());
        String contentString = "Content";

        when(annexService.findAnnexByRef(docRef)).thenReturn(document);
        when(elementProcessor.getElement(document, elementTag, elementId)).thenReturn(contentString);
        when(coEditionHelper.getCurrentEditInfo("test")).thenReturn(actionInfo.getCoEditionVos());

        // DO THE ACTUAL CALL
        annexPresenter.editElement(new EditElementRequestEvent(elementId, elementTag));

        verify(annexService).findAnnexByRef(docRef);
        verify(annexScreen).showElementEditor(elementId, elementTag, contentString, null);
        verifyNoMoreInteractions(annexService, annexScreen);
    }

    @Test
    public void testEditAnnexBlockUnsavedAnnexInSession() {

        Content content = mock(Content.class);
        Source source = mock(Source.class);

        when(source.getBytes()).thenReturn(byteContent);
        when(content.getSource()).thenReturn(source);

        AnnexMetadata annexMetadata = new AnnexMetadata("", "REGULATION", "", "AN-000.xml", "EN", docTemplate, "annex-id", 1, "Annex 1", docTitle, "", "0.0.1", false, null);

        Annex document = getMockedAnnex(content, "", annexMetadata);
        String elementTag = LEVEL;
        String elementId = "486";
        CoEditionActionInfo actionInfo = new CoEditionActionInfo(true, Operation.STORE, null, new ArrayList<CoEditionVO>());
        String contentString = "Content";

        when(annexService.findAnnexByRef(docRef)).thenReturn(document);
        when(elementProcessor.getElement(document, elementTag, elementId)).thenReturn(contentString);
        when(coEditionHelper.getCurrentEditInfo("test")).thenReturn(actionInfo.getCoEditionVos());
        when(httpSession.getAttribute(eq("annex#" + docRef))).thenReturn(document);

        // DO THE ACTUAL CALL
        annexPresenter.editElement(new EditElementRequestEvent(elementId, elementTag));
        verify(eventBus).post(any(ShowConfirmDialogEvent.class));

        verify(annexScreen, times(0)).showElementEditor(elementId, elementTag, contentString, null);
        verifyNoMoreInteractions(annexService, annexScreen);
    }
    @Test
    public void testSaveAnnexBlock() {

        Content content = mock(Content.class);
        Source source = mock(Source.class);

        when(source.getBytes()).thenReturn(byteContent);
        when(content.getSource()).thenReturn(source);

        AnnexMetadata annexMetadata = new AnnexMetadata("", "REGULATION", "", "AN-000.xml", "EN", docTemplate, "annex-id", 1, "Annex 1", docTitle, "", "0.0.1", false, null);
    
        Annex originalDocument = getMockedAnnex(content, "", annexMetadata);
    
        byte[] updatedBytes = new byte[] {'1','2'};
    
        Annex savedDocument = getMockedAnnex(content, "", annexMetadata);
    
        String elementTag = LEVEL;
        String elementId = "486";
        String updatedContent = "Updated Content";

        when(annexService.findAnnexByRef(docRef)).thenReturn(originalDocument);
        when(annexProcessor.updateAnnexBlock(originalDocument, elementId, elementTag, updatedContent)).thenReturn(updatedBytes);
        when(annexService.updateAnnex(originalDocument, updatedBytes, VersionType.MINOR, messageHelper.getMessage("operation.annex.block.updated"))).thenReturn(savedDocument);

        // DO THE ACTUAL CALL
        annexPresenter.saveElement(new SaveElementRequestEvent(elementId, elementTag, updatedContent, false));

        verify(annexProcessor).updateAnnexBlock(originalDocument, elementId, elementTag, updatedContent);
        verify(annexService).updateAnnex(originalDocument, updatedBytes, VersionType.MINOR, messageHelper.getMessage("operation.annex.block.updated"));
        verify(annexService).findAnnexByRef(docRef);
        verifyNoMoreInteractions(annexService);
    }

    public byte[] getFileContent(String fileName) {
        try {
            InputStream inputStream = this.getClass().getResource(fileName).openStream();
            byte[] content = new byte[inputStream.available()];
            inputStream.read(content);
            inputStream.close();
            return content;
        } catch (IOException e) {
            throw new IllegalStateException("Cannot read bytes from file: " + fileName);
        }
    }
}