package eu.europa.ec.leos.ui.view.workspace;

import com.google.common.eventbus.EventBus;
import com.vaadin.spring.annotation.SpringComponent;
import com.vaadin.spring.annotation.ViewScope;
import com.vaadin.ui.UI;
import eu.europa.ec.leos.domain.common.InstanceType;
import eu.europa.ec.leos.i18n.LanguageHelper;
import eu.europa.ec.leos.i18n.MessageHelper;
import eu.europa.ec.leos.instance.Instance;
import eu.europa.ec.leos.security.LeosPermissionAuthorityMapHelper;
import eu.europa.ec.leos.security.SecurityContext;
import eu.europa.ec.leos.ui.wizard.document.CreateMandateWizard;
import eu.europa.ec.leos.web.event.view.repository.ExplanatoryCreateWizardRequestEvent;
import eu.europa.ec.leos.web.event.view.repository.MandateCreateWizardRequestEvent;
import eu.europa.ec.leos.web.support.user.UserHelper;
import org.springframework.beans.factory.annotation.Autowired;

import static eu.europa.ec.leos.security.LeosPermission.CAN_UPLOAD;

@ViewScope
@SpringComponent
@Instance(InstanceType.COUNCIL)
public class WorkspaceScreenMandateImpl extends WorkspaceScreenImpl {

    private static final long serialVersionUID = 1L;

    @Autowired
    WorkspaceScreenMandateImpl(SecurityContext securityContext, EventBus eventBus, MessageHelper messageHelper, LanguageHelper langHelper, UserHelper userHelper, LeosPermissionAuthorityMapHelper authorityMapHelper) {
        super(securityContext, eventBus, messageHelper, langHelper, userHelper, authorityMapHelper);
        initSpecificStaticData();
        initSpecificListeners();
    }

    private void initSpecificStaticData() {
        createDocumentButton.setCaption(messageHelper.getMessage("repository.create.mandate"));
        createDocumentButton.setDescription(messageHelper.getMessage("repository.create.mandate.tooltip"));
        createCouncilDocumentButton.setCaption(messageHelper.getMessage("repository.create.council.internal.document"));
        createCouncilDocumentButton.setDescription(messageHelper.getMessage("repository.create.council.internal.document.tooltip"));

        resetBasedOnPermissions();
    }

    private void resetBasedOnPermissions() {
        //Upload button should only be visible to Support or higher role
        boolean enableUpload = securityContext.hasPermission(null, CAN_UPLOAD);
        createCouncilDocumentButton.setVisible(enableUpload);
        uploadDocumentButton.setVisible(false);
    }

    private void initSpecificListeners() {
        createDocumentButton.addClickListener(clickEvent -> eventBus.post(new MandateCreateWizardRequestEvent()));
        createCouncilDocumentButton.addClickListener(clickEvent -> eventBus.post(new ExplanatoryCreateWizardRequestEvent()));
    }

    @Override
    public void showCreateMandateWizard() {
        CreateMandateWizard createMandateWizard = new CreateMandateWizard(messageHelper, langHelper, eventBus);
        UI.getCurrent().addWindow(createMandateWizard);
        createMandateWizard.focus();
    }

}
