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
package eu.europa.ec.leos.ui.view.collection;

import com.vaadin.server.Resource;
import eu.europa.ec.leos.domain.cmis.document.LegDocument;
import eu.europa.ec.leos.domain.vo.DocumentVO;
import eu.europa.ec.leos.model.user.User;
import eu.europa.ec.leos.ui.model.ExportPackageVO;
import eu.europa.ec.leos.ui.model.MilestonesVO;
import eu.europa.ec.leos.vo.catalog.CatalogItem;
import eu.europa.ec.leos.vo.coedition.CoEditionVO;
import eu.europa.ec.leos.web.model.UserVO;
import eu.europa.ec.leos.web.support.xml.DownloadStreamResource;
import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.Set;

interface CollectionScreen {

    void populateData(DocumentVO proposal, Authentication authentication);

    void populateMilestones(Set<MilestonesVO> milestonesVO);
    
    void showMilestoneExplorer(LegDocument legDocument, String milestoneTitle, String proposalRef);

    void proposeUsers(List<UserVO> users);

    void confirmCollectionDeletion();

    void confirmAnnexDeletion(DocumentVO annex);

    void confirmExplanatoryDeletion(DocumentVO explanatory);

    void setDownloadStreamResource(Resource downloadStreamResource);

    void setExportPdfStreamResource(Resource exportPdfStreamResource);

    void openCreateMilestoneWindow();

    void updateUserCoEditionInfo(List<CoEditionVO> coEditionVos, User user);

    void openCreateRevisionWindow(MilestonesVO milestonesVO);

    boolean isExportPackageBlockVisible();

    void populateExportPackages(Set<ExportPackageVO> exportPackages);

    void setExportPackageStreamResource(DownloadStreamResource exportPackageStreamResource);

    void populateExplanatory(DocumentVO proposalVO);

    void setSearchContextHolder(boolean isShowSearchContext);

    void showCreateDocumentWizard(List<CatalogItem> templates);

    void reset();

    boolean isCoverPageVisible();

}
