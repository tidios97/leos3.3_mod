package eu.europa.ec.leos.services.milestone;

import eu.europa.ec.leos.domain.cmis.LeosLegStatus;
import eu.europa.ec.leos.domain.cmis.document.LegDocument;
import eu.europa.ec.leos.domain.common.InstanceType;
import eu.europa.ec.leos.instance.Instance;
import eu.europa.ec.leos.services.clone.CloneContext;
import eu.europa.ec.leos.services.export.ExportLeos;
import eu.europa.ec.leos.services.export.ExportOptions;
import eu.europa.ec.leos.services.export.LegPackage;
import eu.europa.ec.leos.services.store.LegService;
import eu.europa.ec.leos.services.store.PackageService;
import org.apache.commons.lang3.Validate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import java.io.IOException;

@Service
@Instance(InstanceType.OS)
public class LeosMilestoneServiceImpl extends AbstractMilestoneService {

    @Autowired
    public LeosMilestoneServiceImpl(LegService legService, CloneContext cloneContext) {
        super(legService, cloneContext);
    }

    @Override
    @Nonnull
    public LegDocument createLegDocument(String proposalId, LegPackage legPackage) throws IOException {
        Validate.notNull(legService, "Leg Service is not available!!");
        return legService.createLegDocument(proposalId, PackageService.NOT_AVAILABLE, legPackage, LeosLegStatus.FILE_READY);
    }

    @Override
    protected LegPackage createLegPackage(String proposalId) throws IOException {
        if(cloneContext != null && cloneContext.isClonedProposal()) {
            ExportLeos exportOptions = new ExportLeos(ExportOptions.Output.PDF);
            exportOptions.setWithSuggestions(false);
            exportOptions.setWithAnonymization(true);
            exportOptions.setWithAnnotations(true);
            return legService.createLegPackageForClone(proposalId, exportOptions);
        }
        ExportLeos exportOptions = new ExportLeos(ExportOptions.Output.WORD);
        exportOptions.setWithSuggestions(false);
        return legService.createLegPackage(proposalId, exportOptions);
    }
}
