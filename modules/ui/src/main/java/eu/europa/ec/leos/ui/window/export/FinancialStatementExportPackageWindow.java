package eu.europa.ec.leos.ui.window.export;

import com.google.common.eventbus.EventBus;
import com.vaadin.ui.RadioButtonGroup;
import com.vaadin.ui.themes.ValoTheme;
import eu.europa.ec.leos.i18n.MessageHelper;
import eu.europa.ec.leos.services.export.ExportOptions;
import eu.europa.ec.leos.services.export.RelevantElements;
import eu.europa.ec.leos.ui.event.CreateExportPackageActualVersionRequestEvent;
import eu.europa.ec.leos.ui.event.CreateExportPackageCleanVersionRequestEvent;
import eu.europa.ec.leos.ui.event.CreateExportPackageRequestEvent;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class FinancialStatementExportPackageWindow extends ExportPackageWindow {

    public FinancialStatementExportPackageWindow(MessageHelper messageHelper, EventBus eventBus) {
        this(messageHelper, eventBus, null);
    }

    public FinancialStatementExportPackageWindow(MessageHelper messageHelper, EventBus eventBus, ExportOptions exportOptions) {
        super(messageHelper, eventBus, exportOptions);
    }

    @Override
    protected List<RelevantElements> getRelevantElementsOptions() {
        return Arrays.asList(RelevantElements.ANNOTATIONS, RelevantElements.ALL);
    }

    @Override
    protected RelevantElements getRelevantElementsFromSelected(Set<RelevantElements> relevantElementsSelected) {
        return relevantElementsSelected.stream().parallel().filter(elt -> !elt.equals(RelevantElements.ANNOTATIONS)).findFirst().orElse(RelevantElements.ALL);
    }

    protected RadioButtonGroup buildCleanVersionRadioButtonGroup() {
        RadioButtonGroup<Boolean> cleanVersionRadioButtonGroup = new RadioButtonGroup<>(messageHelper.getMessage("document.export.package.clean.version.caption"));
        cleanVersionRadioButtonGroup.setItems(Boolean.FALSE, Boolean.TRUE);
        cleanVersionRadioButtonGroup.setValue(Boolean.FALSE);
        cleanVersionRadioButtonGroup.addStyleName(ValoTheme.OPTIONGROUP_HORIZONTAL);
        cleanVersionRadioButtonGroup.setDescription(messageHelper.getMessage("document.export.package.clean.version.description"));
        cleanVersionRadioButtonGroup.setItemCaptionGenerator(item -> item ? messageHelper.getMessage("document.export.package.clean.version.option.true")
                : messageHelper.getMessage("document.export.package.clean.version.option.false"));
        return cleanVersionRadioButtonGroup;
    }

    protected void doSendLogic(String title, RelevantElements relevantElements, Boolean isWithAnnotations, ExportOptions exportOptions) {
        if (exportOptions != null) {
            exportOptions.setWithFilteredAnnotations(isWithAnnotations);
            exportOptions.setRelevantElements(relevantElements);
            eventBus.post(new CreateExportPackageRequestEvent(title, exportOptions));
        } else if (Boolean.parseBoolean(cleanVersionRadioButtonGroup.getValue().toString())) {
            eventBus.post(new CreateExportPackageCleanVersionRequestEvent(title, relevantElements, isWithAnnotations));
        } else {
            eventBus.post(new CreateExportPackageActualVersionRequestEvent(title, relevantElements, isWithAnnotations));
        }
    }
}
