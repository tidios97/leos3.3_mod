/*
 * Copyright 2022 European Commission
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
package eu.europa.ec.leos.web.ui.component;

import com.google.common.eventbus.EventBus;
import com.vaadin.annotations.DesignRoot;
import com.vaadin.data.Binder;
import com.vaadin.data.ReadOnlyHasValue;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.shared.ui.ContentMode;
import com.vaadin.spring.annotation.SpringComponent;
import com.vaadin.spring.annotation.ViewScope;
import com.vaadin.ui.Button;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.declarative.Design;
import eu.europa.ec.leos.domain.vo.DocumentVO;
import eu.europa.ec.leos.i18n.LanguageHelper;
import eu.europa.ec.leos.i18n.MessageHelper;
import eu.europa.ec.leos.model.user.User;
import eu.europa.ec.leos.security.LeosPermission;
import eu.europa.ec.leos.security.SecurityContext;
import eu.europa.ec.leos.ui.event.view.collection.DeleteFinancialStatementRequest;
import eu.europa.ec.leos.ui.event.view.collection.SaveFinancialStatementMetaDataRequest;
import eu.europa.ec.leos.vo.coedition.CoEditionVO;
import eu.europa.ec.leos.vo.coedition.InfoType;
import eu.europa.ec.leos.web.event.view.financialStatment.OpenFinancialStatementEvent;
import eu.europa.ec.leos.web.support.user.UserHelper;
import eu.europa.ec.leos.web.ui.converter.LangCodeToDescriptionV8Converter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import javax.annotation.PostConstruct;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

@SpringComponent
@ViewScope
@DesignRoot("SupportingDocumentsBlockDesign.html")
public class FinancialStatementBlockComponent extends VerticalLayout {
    public static SimpleDateFormat dataFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm");
    private static final int TITLE_MAX_LEGTH  = 2000;

    protected HeadingComponent heading;
    protected Label titleCaption;
    protected EditBoxComponent title;
    protected Label docUserCoEdition;
    protected Button openButton;
    protected Label supportDocLanguage;
    protected Label supportDocLastUpdated;

    private MessageHelper messageHelper;
    private EventBus eventBus;
    private UserHelper userHelper;
    private SecurityContext securityContext;
    private LangCodeToDescriptionV8Converter langConverter;

    private boolean enableSave;
    private Binder<DocumentVO> documentVOBinder;

    @Value("${leos.coedition.sip.enabled}")
    private boolean coEditionSipEnabled;

    @Value("${leos.coedition.sip.domain}")
    private String coEditionSipDomain;

    @Value("${leos.supporting.documents.enable:false}")
    private boolean supportingDocEnabled;

    @Autowired
    public FinancialStatementBlockComponent(LanguageHelper languageHelper, MessageHelper messageHelper, EventBus eventBus, UserHelper userHelper,
                                            SecurityContext securityContext) {
        this.messageHelper = messageHelper;
        this.eventBus = eventBus;
        this.userHelper = userHelper;
        this.securityContext = securityContext;
        this.langConverter = new LangCodeToDescriptionV8Converter(languageHelper);
        Design.read(this);
    }

    @PostConstruct
    private void init() {
        addStyleName("supportingdocument-block");

        documentVOBinder = new Binder<>();
        documentVOBinder.forField(new ReadOnlyHasValue<>(supportDocLanguage::setValue))
                .withConverter(langConverter)
                .bind(DocumentVO::getLanguage, DocumentVO::setLanguage);
        documentVOBinder.forField(title).bind(DocumentVO::getTitle, DocumentVO::setTitle);



        titleCaption.setCaption(messageHelper.getMessage("collection.block.caption.financial.statement.title"));
        openButton.setCaption(messageHelper.getMessage("leos.button.open"));// using same caption as of card
        supportDocLanguage.setCaption(messageHelper.getMessage("collection.caption.language"));
        heading.setCaption(messageHelper.getMessage("collection.block.caption.financial.statement"));

        if(supportingDocEnabled) {
            heading.addRightButton(createDeleteDocumentButton());
        }
        openButton.addClickListener(event -> openDocument());
        title.addValueChangeListener(event -> saveData());
    }

    private Button createDeleteDocumentButton() {
        Button button = new Button();
        button.setIcon(VaadinIcons.MINUS_CIRCLE);
        button.setDescription(messageHelper.getMessage("collection.description.button.delete.financial.statement"));
        button.addStyleName("delete-button");
        button.addClickListener(listener -> deleteFinancialStatement());
        return button;
    }

    private void deleteFinancialStatement() {
        eventBus.post(new DeleteFinancialStatementRequest((DocumentVO) this.getData()));
    }

    public void populateData(DocumentVO documentVO) {
        enableSave = false; // To avoid triggering save on load of data
        resetBasedOnPermissions(documentVO);
        this.setData(documentVO);
        documentVOBinder.setBean(documentVO);
        setLastUpdated(documentVO.getUpdatedBy(), documentVO.getUpdatedOn());
        enableSave = true;
        title.setTitleMaxSize(TITLE_MAX_LEGTH);
    }

    private void resetBasedOnPermissions(DocumentVO documentVO) {
        boolean enableUpdate = securityContext.hasPermission(documentVO, LeosPermission.CAN_UPDATE);
        if(heading.getRightButton() != null) {
            heading.getRightButton().setVisible(enableUpdate);
        }
        title.setEnabled(enableUpdate);
    }

    private void openDocument() {
        eventBus.post(new OpenFinancialStatementEvent((DocumentVO) this.getData()));
    }

    private void saveData() {
        if (enableSave) {
            // get original vo and update with latest value and fire save
            DocumentVO documentVO = ((DocumentVO) this.getData());
            documentVO.setTitle(title.getValue());
            eventBus.post(new SaveFinancialStatementMetaDataRequest(documentVO));
        }
    }

    public void setLastUpdated(String lastUpdatedBy, Date lastUpdatedOn) {
        supportDocLastUpdated.setValue(messageHelper.getMessage("collection.caption.document.lastupdated", dataFormat.format(lastUpdatedOn),
                userHelper.convertToPresentation(lastUpdatedBy)));
    }

    public void updateUserCoEditionInfo(List<CoEditionVO> coEditionVos, User user) {
        // Update docuemnt user CoEdition information
        docUserCoEdition.setIcon(null);
        docUserCoEdition.setDescription("");
        docUserCoEdition.removeStyleName("leos-user-coedition-self-user");
        DocumentVO documentVO = (DocumentVO) this.getData();
        coEditionVos.stream()
                .filter((x) -> (InfoType.ELEMENT_INFO.equals(x.getInfoType()) || InfoType.TOC_INFO.equals(x.getInfoType())) && x.getDocumentId().equals(documentVO.getVersionSeriesId()))
                .sorted(Comparator.comparing(CoEditionVO::getUserName).thenComparingLong(CoEditionVO::getEditionTime))
                .forEach(x -> {
                    StringBuilder userDescription = new StringBuilder();
                    if (!x.getUserLoginName().equals(user.getLogin())) {
                        userDescription.append("<a class=\"leos-user-coedition-lync\" href=\"")
                                .append(StringUtils.isEmpty(x.getUserEmail()) ? "" : (coEditionSipEnabled ? new StringBuilder("sip:").append(x.getUserEmail().replaceFirst("@.*", "@" + coEditionSipDomain)).toString()
                                        : new StringBuilder("mailto:").append(x.getUserEmail()).toString()))
                                .append("\">").append(x.getUserName()).append(" (").append(StringUtils.isEmpty(x.getEntity()) ? "-" : x.getEntity())
                                .append(")</a>");
                    } else {
                        userDescription.append(x.getUserName()).append(" (").append(StringUtils.isEmpty(x.getEntity()) ? "-" : x.getEntity()).append(")");
                    }
                    docUserCoEdition.setDescription(
                            docUserCoEdition.getDescription() +
                                    messageHelper.getMessage("coedition.tooltip.message", userDescription, dataFormat.format(new Date(x.getEditionTime()))) +
                                    "<br>",
                            ContentMode.HTML);
                });
        if (!docUserCoEdition.getDescription().isEmpty()) {
            docUserCoEdition.setIcon(VaadinIcons.USER);
            if (!docUserCoEdition.getDescription().contains("href=\"")) {
                docUserCoEdition.addStyleName("leos-user-coedition-self-user");
            }
        }
    }
}
