package eu.europa.ec.leos.services.dto.request;

public class FilterProposalsRequest {

    private int startIndex;
    private int limit;
    private boolean sortOrder;
    public Filter[] filters;

    public int getStartIndex() {
        return startIndex;
    }

    public void setStartIndex(int startIndex) {
        this.startIndex = startIndex;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public boolean isSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(boolean sortOrder) {
        this.sortOrder = sortOrder;
    }

    public Filter[] getFilters() {
        return filters;
    }

    public void setFilters(Filter[] filters) {
        this.filters = filters;
    }

    public static class Filter {
        private String type;
        private String[] value;

        public String getType() {
            return type;
        }

        public String[] getValue() {
            return value;
        }

        public void setType(String type) {
            this.type = type;
        }

        public void setValue(String[] value) {
            this.value = value;
        }
    }
}
