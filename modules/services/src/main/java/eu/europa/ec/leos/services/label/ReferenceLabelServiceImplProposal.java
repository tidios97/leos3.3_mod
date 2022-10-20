/*
 * Copyright 2021 European Commission
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
package eu.europa.ec.leos.services.label;

import eu.europa.ec.leos.domain.common.InstanceType;
import eu.europa.ec.leos.domain.common.Result;
import eu.europa.ec.leos.instance.Instance;
import eu.europa.ec.leos.services.clone.CloneContext;
import eu.europa.ec.leos.services.label.ref.Ref;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.w3c.dom.Node;

@Service
@Instance(instances = {InstanceType.OS, InstanceType.COMMISSION})
public class ReferenceLabelServiceImplProposal extends ReferenceLabelServiceImpl {

    @Autowired
    private CloneContext cloneContext;

    @Override
    public Result<String> generateSoftMoveLabel(Ref ref, String referenceLocation, Node sourceNode,
                                                String direction, String documentRefSource) {
        if (cloneContext.isClonedProposal()) {
            return super.generateSoftMoveLabel(ref, referenceLocation, sourceNode, direction, documentRefSource);
        }
        return new Result<String>("", null);
    }
}
