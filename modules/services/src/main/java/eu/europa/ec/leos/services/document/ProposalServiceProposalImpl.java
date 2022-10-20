/*
 * Copyright 2018 European Commission
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
package eu.europa.ec.leos.services.document;

import eu.europa.ec.leos.domain.cmis.document.Proposal;
import eu.europa.ec.leos.domain.cmis.metadata.ProposalMetadata;
import eu.europa.ec.leos.domain.common.InstanceType;
import eu.europa.ec.leos.domain.vo.CloneProposalMetadataVO;
import eu.europa.ec.leos.i18n.MessageHelper;
import eu.europa.ec.leos.instance.Instance;
import eu.europa.ec.leos.repository.document.ProposalRepository;
import eu.europa.ec.leos.repository.store.PackageRepository;
import eu.europa.ec.leos.services.processor.content.TableOfContentProcessor;
import eu.europa.ec.leos.services.processor.content.XmlContentProcessor;
import eu.europa.ec.leos.services.processor.node.XmlNodeConfigProcessor;
import eu.europa.ec.leos.services.processor.node.XmlNodeProcessor;
import eu.europa.ec.leos.services.support.XPathCatalog;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static eu.europa.ec.leos.services.support.XmlHelper.XML_DOC_EXT;

@Service
@Instance(instances = {InstanceType.COMMISSION, InstanceType.OS})
public class ProposalServiceProposalImpl extends ProposalServiceImpl {

    private static final Logger LOG = LoggerFactory.getLogger(ProposalServiceProposalImpl.class);

    @Autowired
    public ProposalServiceProposalImpl(ProposalRepository proposalRepository,
                                       XmlNodeProcessor xmlNodeProcessor,
                                       XmlContentProcessor xmlContentProcessor,
                                       XmlNodeConfigProcessor xmlNodeConfigProcessor, PackageRepository packageRepository,
                                       XPathCatalog xPathCatalog, TableOfContentProcessor tableOfContentProcessor,
                                       MessageHelper messageHelper) {
        super(proposalRepository, xmlNodeProcessor, xmlContentProcessor, xmlNodeConfigProcessor, packageRepository,
                xPathCatalog, tableOfContentProcessor, messageHelper);
    }

    @Override
    public Proposal createClonedProposalFromContent(String path, ProposalMetadata metadata,
                                                    CloneProposalMetadataVO cloneProposalMetadataVO, byte[] content) {
        LOG.trace("Creating Proposal From Content... [path={}, metadata={}]", path, metadata);
        String ref = generateProposalReference(metadata.getLanguage());
        metadata = metadata.withRef(ref);
        return proposalRepository.createClonedProposalFromContent(path, ref + XML_DOC_EXT, metadata, cloneProposalMetadataVO,
                updateDataInXml(content, metadata));

    }

    @Override
    public String generateProposalName(String ref, String language) {
        if (!ref.startsWith(PROPOSAL_NAME_PREFIX)) { // Compatibility with old reference formats
            ref = super.generateProposalReference(language);
        } else if (StringUtils.countMatches(ref, "-") < 2) {
            ref = ref + language.toLowerCase();
        }
        return ref + XML_DOC_EXT;
    }
}
