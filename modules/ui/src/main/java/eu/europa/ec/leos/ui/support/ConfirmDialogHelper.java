package eu.europa.ec.leos.ui.support;

import com.google.common.eventbus.EventBus;
import eu.europa.ec.leos.i18n.MessageHelper;
import eu.europa.ec.leos.ui.event.search.ShowConfirmDialogEvent;
import org.vaadin.dialogs.ConfirmDialog;
import com.vaadin.ui.UI;

public class ConfirmDialogHelper {
    public static void showOpenEditorDialog( final UI ui, final ShowConfirmDialogEvent event, final EventBus eventBus, final MessageHelper messageHelper) {
        ConfirmDialog.show(ui,
                messageHelper.getMessage("document.editor.open.title"),
                messageHelper.getMessage("document.editor.open.message"),
                messageHelper.getMessage("document.editor.open.confirm"),
                messageHelper.getMessage("leos.button.cancel"),
                new ConfirmDialog.Listener() {
                    private static final long serialVersionUID = -2086246080635984781L;

                    public void onClose(ConfirmDialog dialog) {
                        if (dialog.isConfirmed() && event.getPostConfirmEvent() != null) {
                            eventBus.post(event.getPostConfirmEvent());
                        } else if (dialog.isCanceled() && event.getPostCancelEvent() != null) {
                            eventBus.post(event.getPostCancelEvent());
                        }
                    }
                }
        );
    }
}
