/*
 * Copyright 2021 European Commission
 *
 * Licensed under the EUPL, Version 1.1 or – as soon they will be approved by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 *     https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and limitations under the Licence.
 */
package eu.europa.ec.leos.ui.event;

public class ChangeBaseVersionEvent {
    private String versionId;
    private String versionLabel;
    private String baseVersionTitle;

    public ChangeBaseVersionEvent(String versionId, String versionLabel, String baseVersionTitle) {
        this.versionId = versionId;
        this.versionLabel = versionLabel;
        this.baseVersionTitle = baseVersionTitle;
    }

    public String getVersionId() {
        return versionId;
    }
    public String getVersionLabel() { return versionLabel; }
    public String getBaseVersionTitle() { return baseVersionTitle; }
}
