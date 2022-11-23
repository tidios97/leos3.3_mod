package eu.europa.ec.leos.ui.event.view.collection;

import eu.europa.ec.leos.domain.vo.DocumentVO;

public class DeleteFinancialStatementEvent {

    DocumentVO financialStatement;

    public DeleteFinancialStatementEvent(DocumentVO financialStatement) {
        this.financialStatement = financialStatement;
    }

    public DocumentVO getFinancialStatement() {
        return financialStatement;
    }
}
