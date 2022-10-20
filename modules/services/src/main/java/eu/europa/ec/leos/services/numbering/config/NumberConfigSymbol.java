package eu.europa.ec.leos.services.numbering.config;

public class NumberConfigSymbol extends NumberConfigAbstract implements NumberConfig {

    public NumberConfigSymbol(String symbol, String prefix, String suffix) {
        this.numberToShow = symbol;
        this.prefix = prefix;
        this.suffix = suffix;
    }

    public NumberConfigSymbol() {
        this("-", "", "");
    }

    @Override
    public String getActualNumberToShow() {
        return numberToShow;
    }

    @Override
    public void parseInitialValue(String numAsString) { }

    @Override
    public void parseValue(String numAsString) { }

    @Override
    protected String getImplName() {
        return this.getClass().getSimpleName();
    }
}
