/*
 * Copyright 2018 European Commission
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

package eu.europa.ec.leos.model.event;

import eu.europa.ec.leos.domain.cmis.document.LegDocument;

public class MilestoneUpdatedEvent {

    private final LegDocument legDocument;
    private final Boolean displayNotif;

    public MilestoneUpdatedEvent(LegDocument legDocument, Boolean displayNotif) {
        this.legDocument = legDocument;
        this.displayNotif = displayNotif;
    }

    public LegDocument getLegDocument() {
        return legDocument;
    }

    public Boolean getDisplayNotif() { return displayNotif; }
}
