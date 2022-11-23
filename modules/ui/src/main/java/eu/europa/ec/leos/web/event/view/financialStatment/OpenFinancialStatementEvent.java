package eu.europa.ec.leos.web.event.view.financialStatment;

import eu.europa.ec.leos.domain.vo.DocumentVO;

public class OpenFinancialStatementEvent {

    private DocumentVO financialStatement;

    public OpenFinancialStatementEvent(DocumentVO financialStatement) {
        this.financialStatement = financialStatement;
    }

    public DocumentVO getFinancialStatement() {
        return financialStatement;
    }

    public void setFinancialStatement(DocumentVO financialStatement) {
        this.financialStatement = financialStatement;
    }
}
