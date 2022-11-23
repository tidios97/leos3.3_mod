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
package eu.europa.ec.leos.ui.wizard.document;

import com.google.common.eventbus.EventBus;
import eu.europa.ec.leos.domain.cmis.LeosCategory;
import eu.europa.ec.leos.domain.vo.DocumentVO;
import eu.europa.ec.leos.i18n.LanguageHelper;
import eu.europa.ec.leos.i18n.MessageHelper;
import eu.europa.ec.leos.ui.event.view.collection.CancelCreateSupportDocumentRequest;
import eu.europa.ec.leos.ui.event.view.collection.CreateSupportDocumentRequest;
import eu.europa.ec.leos.ui.wizard.AbstractWizard;
import eu.europa.ec.leos.ui.wizard.WizardStep;
import eu.europa.ec.leos.vo.catalog.CatalogItem;
import eu.europa.ec.leos.web.support.cfg.ConfigurationHelper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@Scope("prototype")
public class CreateSupportDocumentWizard extends AbstractWizard {

    private static final long serialVersionUID = 1L;

    private LanguageHelper languageHelper;
    private final DocumentVO document;
    private ConfigurationHelper cfgfHelper;

    String supportDocumentCatalogKey;

    public CreateSupportDocumentWizard(List<CatalogItem> templates, List<String> templateDocPresent, MessageHelper messageHelper,
                                       LanguageHelper languageHelper, EventBus eventBus, ConfigurationHelper cfgfHelper) {
        super(messageHelper, eventBus);
        this.languageHelper = languageHelper;
        this.cfgfHelper = cfgfHelper;
        document = new DocumentVO(LeosCategory.SUPPORT_DOCUMENT);
        document.setUploaded(false);
        init(templates, templateDocPresent);
    }

    public void init(List<CatalogItem> templates, List<String> templateDocPresent) {
        List<CatalogItem> supportingDocumentTemplate = new ArrayList<>();
        supportDocumentCatalogKey = cfgfHelper.getProperty("leos.supporting.documents.catalog.key");
        supportingDocumentTemplate.add(templates.stream().filter(template -> template.getKey().equals(supportDocumentCatalogKey)).findAny().get());
        checkForAlreadyPresentSupportDoc(supportingDocumentTemplate, templateDocPresent);
        registerWizardStep(new TemplateSelectionStep(document, supportingDocumentTemplate, messageHelper, true, true));
        setWizardStep(0);
    }

    private void checkForAlreadyPresentSupportDoc(List<CatalogItem> supportingDocumentTemplate, List<String> templateDocPresent) {
        List<CatalogItem> catalogItems = supportingDocumentTemplate.get(0).getItems();
        for(CatalogItem item : catalogItems) {
            if(templateDocPresent.contains(item.getKey())) {
                item.setEnabled(false);
            }
        }
    }

    @Override
    protected String getWizardTitle() {
        return messageHelper.getMessage("wizard.document.create.title");
    }

    @Override
    protected boolean handleFinishAction(List<WizardStep> stepList) {
        String template = "";
        if(StringUtils.isNotEmpty(document.getMetadata().getDocTemplate())) {
            String[] docTemplates = document.getMetadata().getDocTemplate().split(";");
            if(docTemplates != null && docTemplates.length > 0) {
                template = docTemplates[docTemplates.length - 1];
            }
        }
        eventBus.post(new CreateSupportDocumentRequest(template));
        return true;
    }

    @Override
    protected void handleCancelAction() {
        close();
        eventBus.post(new CancelCreateSupportDocumentRequest());
    }
}
