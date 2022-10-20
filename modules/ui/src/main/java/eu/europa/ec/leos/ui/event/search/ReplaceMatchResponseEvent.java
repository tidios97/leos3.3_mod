/*
 * Copyright 2020 European Commission
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
package eu.europa.ec.leos.ui.event.search;

public class ReplaceMatchResponseEvent {

    private final Long searchId;
    private final int matchIndex;
    private final boolean isReplaced;

    public ReplaceMatchResponseEvent(Long searchId, int matchIndex, boolean isReplaced) {
        this.searchId = searchId;
        this.matchIndex = matchIndex;
        this.isReplaced = isReplaced;
    }

    public Long getSearchId() {
        return searchId;
    }

    public int getMatchIndex() {
        return matchIndex;
    }

    public boolean isReplaced() {
        return isReplaced;
    }
}
