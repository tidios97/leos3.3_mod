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
package eu.europa.ec.leos.ui.component.listener;

import com.vaadin.event.ShortcutAction;
import com.vaadin.event.ShortcutListener;

/**
 * Listener for CTRL + Home keyboard shortcut. <br/>
 * Note: A CSS class name should be provided as constructor parameter. This
 * helps to identify the element in JS.
 */
public class CtrlHomeListener extends ShortcutListener {

	private static final long serialVersionUID = 1L;
	private String styleName = "leos-doc-content";

	public CtrlHomeListener(String styleName) {
		super("Ctrl+Home", ShortcutAction.KeyCode.HOME, new int[] { ShortcutAction.ModifierKey.CTRL });
		this.styleName = styleName;
	}

	@Override
	public void handleAction(Object sender, Object target) {
		com.vaadin.ui.JavaScript.getCurrent().execute("LEOS.scrollTop('" + this.styleName + "');");
	}

}