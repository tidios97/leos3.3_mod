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
package eu.europa.ec.leos.cmis.extensions;

import eu.europa.ec.leos.cmis.mapping.CmisProperties;
import eu.europa.ec.leos.domain.cmis.metadata.*;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static eu.europa.ec.leos.cmis.mapping.CmisProperties.*;
import static java.util.stream.Collectors.toMap;
import static org.springframework.util.StringUtils.isEmpty;

public class LeosMetadataExtensions {

    //todo: fix this generics..
    public static Map<String, ? extends Object> toCmisProperties(LeosMetadata leosMetadata) {

        Map<String, ? extends Object> cmisProperties;
        if (leosMetadata instanceof ProposalMetadata) {
            cmisProperties = toCmisProperties((ProposalMetadata) leosMetadata);
        } else if (leosMetadata instanceof MemorandumMetadata) {
            cmisProperties = toCmisProperties((MemorandumMetadata) leosMetadata);
        } else if (leosMetadata instanceof BillMetadata) {
            cmisProperties = toCmisProperties((BillMetadata) leosMetadata);
        } else if (leosMetadata instanceof AnnexMetadata) {
            cmisProperties = toCmisProperties((AnnexMetadata) leosMetadata);
        } else if (leosMetadata instanceof ExplanatoryMetadata) {
            cmisProperties = toCmisProperties((ExplanatoryMetadata) leosMetadata);
        } else if (leosMetadata instanceof FinancialStatementMetadata) {
            cmisProperties = toCmisProperties((FinancialStatementMetadata) leosMetadata);
        } else {
            throw new IllegalStateException("Unknown LEOS Metadata! [type=" + leosMetadata.getClass().getSimpleName() + ']');
        }

        return cmisProperties.entrySet()
                .stream()
                .filter(mapEntry -> !isEmpty(mapEntry.getValue()))
                .collect(toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private static Map<String, ? extends Object> toCmisProperties(ProposalMetadata proposalMetadata) {

        String title = Stream.of(proposalMetadata.getStage(), proposalMetadata.getType(), proposalMetadata.getPurpose())
                .filter(s -> s != null && !s.isEmpty())
                .collect(Collectors.joining(" "));

        return buildCommonProperties(proposalMetadata, title);
    }

    private static Map<String, ? extends Object> toCmisProperties(ExplanatoryMetadata explanatoryMetadata) {
        String title = explanatoryMetadata.getTitle();

        return buildCommonProperties(explanatoryMetadata, title);
    }

    private static Map<String, ? extends Object> toCmisProperties(MemorandumMetadata memorandumMetadata) {
        String title = memorandumMetadata.getType();

        return buildCommonProperties(memorandumMetadata, title);
    }

    private static Map<String, ? extends Object> toCmisProperties(BillMetadata billMetadata) {
        String title = Stream.of(billMetadata.getStage(), billMetadata.getType(), billMetadata.getPurpose())
                .filter(s -> s != null && !s.isEmpty())
                .collect(Collectors.joining(" "));

        return buildCommonProperties(billMetadata, title);
    }

    private static Map<String, ? extends Object> toCmisProperties(AnnexMetadata annexMetadata) {
        String title = annexMetadata.getType();

        Map<String, Object> cmisProperties = new HashMap<>();

        cmisProperties.putAll(buildCommonProperties(annexMetadata, title));

        cmisProperties.put(CmisProperties.ANNEX_INDEX.getId(), annexMetadata.getIndex());
        cmisProperties.put(CmisProperties.ANNEX_NUMBER.getId(), annexMetadata.getNumber());
        cmisProperties.put(CmisProperties.ANNEX_TITLE.getId(), annexMetadata.getTitle());
        cmisProperties.put(CmisProperties.ANNEX_CLONED_REF.getId(), annexMetadata.getClonedRef());

        return cmisProperties;
    }

    private static Map<String, ? extends Object> toCmisProperties(FinancialStatementMetadata financialStatementMetadata) {
        String title = financialStatementMetadata.getTitle();

        return buildCommonProperties(financialStatementMetadata, title);
    }

    private static Map<String, ? extends Object> buildCommonProperties(LeosMetadata leosMetadata, String title) {
        Map<String, Object> cmisProperties = new HashMap<>();
        cmisProperties.put(METADATA_STAGE.getId(), leosMetadata.getStage());
        cmisProperties.put(METADATA_TYPE.getId(), leosMetadata.getType());
        cmisProperties.put(METADATA_PURPOSE.getId(), leosMetadata.getPurpose());
        cmisProperties.put(METADATA_EEA_RELEVANCE.getId(), leosMetadata.getEeaRelevance());
        cmisProperties.put(DOCUMENT_TEMPLATE.getId(), leosMetadata.getTemplate());
        cmisProperties.put(DOCUMENT_LANGUAGE.getId(), leosMetadata.getLanguage());
        cmisProperties.put(METADATA_DOCTEMPLATE.getId(), leosMetadata.getDocTemplate());

        String ref = leosMetadata.getRef();
        cmisProperties.put(METADATA_REF.getId(), ref != null ? ref : "");

        cmisProperties.put(DOCUMENT_TITLE.getId(), title);

        return cmisProperties;
    }
}
