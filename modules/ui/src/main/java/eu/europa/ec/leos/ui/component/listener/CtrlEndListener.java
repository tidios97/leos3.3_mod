package eu.europa.ec.leos.ui.component.listener;

import com.vaadin.event.ShortcutAction;
import com.vaadin.event.ShortcutListener;

/**
 * Listener for CTRL + End keyboard shortcut. <br/>
 * Note: A CSS class name should be provided as constructor parameter. This
 * helps to identify the element in JS.
 */
public class CtrlEndListener extends ShortcutListener {

	private static final long serialVersionUID = 1L;
	private String styleName = "leos-doc-content";

	public CtrlEndListener(String styleName) {
		super("Ctrl+End", ShortcutAction.KeyCode.END, new int[] { ShortcutAction.ModifierKey.CTRL });
		this.styleName = styleName;
	}

	@Override
	public void handleAction(Object sender, Object target) {
		com.vaadin.ui.JavaScript.getCurrent().execute("LEOS.scrollBottom('" + this.styleName + "');");

	}

}
