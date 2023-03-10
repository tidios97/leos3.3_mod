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
package eu.europa.ec.leos.web.event.view.document;

public class FetchElementResponseEvent {

	private String elementId;
	private String elementTagName;
	private String elementFragment;
	private String documentRef;

	public FetchElementResponseEvent(String elementId, String elementTagName, String elementFragment, String documentRef) {
		this.elementId = elementId;
		this.elementTagName = elementTagName;
        this.elementFragment = elementFragment;
        this.documentRef = documentRef;
	}
	
	public String getElementTagName() {
		return elementTagName;
	}
	
	public String getElementId() {
		return elementId;
	}

    public String getElementFragment() {
        return elementFragment;
    }

	public String getDocumentRef() {
		return documentRef;
	}
}
