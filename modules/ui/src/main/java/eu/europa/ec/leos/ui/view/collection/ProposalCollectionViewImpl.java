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
package eu.europa.ec.leos.ui.view.collection;

import com.vaadin.spring.annotation.SpringView;
import eu.europa.ec.leos.domain.common.InstanceType;
import eu.europa.ec.leos.instance.Instance;
import org.springframework.beans.factory.annotation.Autowired;

@SpringView(name = CollectionView.VIEW_ID)
@Instance(instances = {InstanceType.COMMISSION, InstanceType.OS})
class ProposalCollectionViewImpl extends CollectionViewImpl {

    private static final long serialVersionUID = 1L;

    @Autowired
    ProposalCollectionViewImpl(CollectionScreenImpl screen, CollectionPresenter presenter) {
        super(screen, presenter);
    }
}
