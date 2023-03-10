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
package eu.europa.ec.leos.ui.extension;

import eu.europa.ec.leos.ui.shared.js.LeosJavaScriptExtensionState;

public class AnnotateState extends LeosJavaScriptExtensionState {
    private static final long serialVersionUID = 1L;

    public String oauthClientId;
    public String authority;
    public String anotClient;
    public String anotHost;
    public String annotationContainer;
    public String operationMode;
    public boolean showStatusFilter;
    public boolean showGuideLinesButton;
    public String annotationPopupDefaultStatus;
    public String proposalRef;
    public String connectedEntity;
    public boolean isSpellCheckerEnabled;
    public String spellCheckerServiceUrl;
    public String spellCheckerSourceUrl;
    public String temporaryDataId;
    public String temporaryDataDocument;
    public String sidebarAppId;
}
