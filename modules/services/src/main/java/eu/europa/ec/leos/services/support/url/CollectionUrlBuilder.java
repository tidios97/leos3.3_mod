package eu.europa.ec.leos.services.support.url;

import org.springframework.stereotype.Component;

import java.text.MessageFormat;
import java.util.Properties;

@Component
public class CollectionUrlBuilder {

    private Properties applicationProperties;

    public CollectionUrlBuilder(Properties applicationProperties) {
        this.applicationProperties = applicationProperties;
    }


    public String buildProposalViewUrl(String proposalId) {
        String proposalViewUrl = applicationProperties.getProperty("leos.mapping.url") +
                applicationProperties.getProperty("leos.document.view.proposal.uri");
        return MessageFormat.format(proposalViewUrl, proposalId);
    }


    public String buildBillViewUrl(String billId) {
        return  MessageFormat.format(
                applicationProperties.getProperty("leos.mapping.url") +
                        applicationProperties.getProperty("leos.document.view.bill.uri")
                , billId);
    }

    public String buildMemorandumViewUrl(String memorandumId) {
        return  MessageFormat.format(
                applicationProperties.getProperty("leos.mapping.url") +
                        applicationProperties.getProperty("leos.document.view.memorandum.uri")
                , memorandumId);
    }

    public String buildAnnexViewUrl(String annexId) {
        return MessageFormat.format(
                applicationProperties.getProperty("leos.mapping.url") +
                        applicationProperties.getProperty("leos.document.view.annex.uri")
                , annexId);
    }

    public String buildCoverPageViewUrl(String coverpageId) {
        return  MessageFormat.format(
                applicationProperties.getProperty("leos.mapping.url") +
                        applicationProperties.getProperty("leos.document.view.coverpage.uri")
                , coverpageId);
    }
}
