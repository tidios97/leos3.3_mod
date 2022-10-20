package eu.europa.ec.leos.ui.window.export;

import com.google.common.eventbus.EventBus;
import com.vaadin.ui.RadioButtonGroup;
import com.vaadin.ui.themes.ValoTheme;
import eu.europa.ec.leos.i18n.MessageHelper;
import eu.europa.ec.leos.services.export.ExportOptions;
import eu.europa.ec.leos.services.export.RelevantElements;
import eu.europa.ec.leos.ui.event.CreateExportPackageCleanVersionRequestEvent;
import eu.europa.ec.leos.ui.event.CreateExportPackageRequestEvent;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class ExplanatoryExportPackageWindow extends ExportPackageWindow {

    public static final String PRINT_STYLE_DOCUWRITE = "docuwrite";
    public static final String PRINT_STYLE_INTERNAL = "internal";

    public ExplanatoryExportPackageWindow(MessageHelper messageHelper, EventBus eventBus) {
        super(messageHelper, eventBus, null);
    }


    @Override
    protected List<RelevantElements> getRelevantElementsOptions() {
        return Arrays.asList(RelevantElements.ANNOTATIONS, RelevantElements.ALL);
    }

    @Override
    protected RelevantElements getRelevantElementsFromSelected(Set<RelevantElements> relevantElementsSelected) {
        return relevantElementsSelected.stream().parallel().filter(elt -> !elt.equals(RelevantElements.ANNOTATIONS)).findFirst().orElse(RelevantElements.ALL);
    }

    @Override
    protected RadioButtonGroup buildCleanVersionRadioButtonGroup() {
        RadioButtonGroup<String> cleanVersionRadioButtonGroup = new RadioButtonGroup<>(messageHelper.getMessage("document.export.package.printStyle.caption"));
        cleanVersionRadioButtonGroup.setItems(PRINT_STYLE_DOCUWRITE, PRINT_STYLE_INTERNAL);
        cleanVersionRadioButtonGroup.setValue(PRINT_STYLE_DOCUWRITE);
        cleanVersionRadioButtonGroup.addStyleName(ValoTheme.OPTIONGROUP_HORIZONTAL);
        cleanVersionRadioButtonGroup.setDescription(messageHelper.getMessage("document.export.package.printStyle.caption"));
        cleanVersionRadioButtonGroup.setItemCaptionGenerator(item -> messageHelper.getMessage("document.export.package.printStyle." + item));
        cleanVersionRadioButtonGroup.setEnabled(false);
        return cleanVersionRadioButtonGroup;
    }

    protected void doSendLogic(String title, RelevantElements relevantElements, Boolean isWithAnnotations, ExportOptions exportOptions) {
        if (exportOptions != null) {
            exportOptions.setWithFilteredAnnotations(isWithAnnotations);
            exportOptions.setRelevantElements(relevantElements);
            eventBus.post(new CreateExportPackageRequestEvent(title, exportOptions));
        } else {
            eventBus.post(new CreateExportPackageCleanVersionRequestEvent(title, relevantElements, isWithAnnotations, cleanVersionRadioButtonGroup.getValue().toString()));
        }
    }
}
