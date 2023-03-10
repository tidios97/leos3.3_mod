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
package eu.europa.ec.leos.web.ui.component.layout;

import com.vaadin.server.ExternalResource;
import com.vaadin.shared.ui.ContentMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Link;
import com.vaadin.ui.VerticalLayout;

import eu.europa.ec.leos.web.support.LeosBuildInfo;
import eu.europa.ec.leos.i18n.MessageHelper;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;

public class Footer extends CustomComponent {

    private static final long serialVersionUID = -424441011003896920L;

    private static final Logger LOG = LoggerFactory.getLogger(Footer.class);
    private static final String DELIMITER = "|";

    private MessageHelper msgHelper;
    private String helpUrl;
    private String privateStatementUrl;

    public Footer(final MessageHelper msgHelper, String helpUrl, String privateStatementUrl) {
        this.msgHelper = msgHelper;
        this.helpUrl = helpUrl;
        this.privateStatementUrl = privateStatementUrl;

        LOG.trace("Initializing footer...");
        initLayout();

    }

    // initialize footer layout
    private void initLayout() {
        // create footer layout
        final VerticalLayout footerLayout = new VerticalLayout();
        footerLayout.setMargin(false);
        footerLayout.setSpacing(false);
        footerLayout.addStyleName("leos-footer-layout");
        footerLayout.setHeight("20px");

        // set footer layout as composition root
        setCompositionRoot(footerLayout);
        addStyleName("leos-footer");

        // info
        final Component info = buildFooterInfo();
        footerLayout.addComponent(info);
        footerLayout.setComponentAlignment(info, Alignment.MIDDLE_CENTER);
    }

    private @Nonnull
    Component buildFooterInfo() {
    	final HorizontalLayout layout = new HorizontalLayout();

    	String helpLabel = msgHelper.getMessage("leos.ui.footer.help");
		if(StringUtils.isNotBlank(helpUrl) && StringUtils.isNotBlank(helpLabel)) {
    		Link helpLink = new Link(helpLabel, new ExternalResource(helpUrl));
    		helpLink.setTargetName("_target");
    		helpLink.addStyleName("resource-link");
    		layout.addComponent(helpLink);
		} else {
			layout.addComponent(new Label(helpLabel));
		}
		
		layout.addComponent(new Label(DELIMITER));
		
		String privacyStatementLabel = msgHelper.getMessage("leos.ui.footer.privacy.statement");
		if(StringUtils.isNotBlank(privateStatementUrl) && StringUtils.isNotBlank(privacyStatementLabel)) {
    		Link privacyStatementLink = new Link(privacyStatementLabel, new ExternalResource(privateStatementUrl));
    		privacyStatementLink.setTargetName("_target");
    		privacyStatementLink.addStyleName("resource-link");
    		layout.addComponent(privacyStatementLink);
		} else {
			layout.addComponent(new Label(privacyStatementLabel));
		}
		
		layout.addComponent(new Label(DELIMITER));
	
		final String infoMsg = msgHelper.getMessage("leos.ui.footer.info", LeosBuildInfo.BUILD_VERSION, LeosBuildInfo.SOURCE_REVISION, LeosBuildInfo.BUILD_DATE);
        StringBuilder info = new StringBuilder();
    	info.append("<span>").append(infoMsg).append("</span>");
        final Label infoLabel = new Label(info.toString(), ContentMode.HTML);
        infoLabel.setSizeUndefined();
        layout.addComponent(infoLabel);
        
        return layout;
    }
}
