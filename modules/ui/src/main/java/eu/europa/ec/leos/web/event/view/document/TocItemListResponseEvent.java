package eu.europa.ec.leos.web.event.view.document;

import eu.europa.ec.leos.vo.toc.TocItem;

import java.util.List;

public class TocItemListResponseEvent {

    List<TocItem> tocItemList;

    public TocItemListResponseEvent(List<TocItem> tocItemList) {
        this.tocItemList = tocItemList;
    }

    public List<TocItem> getTocItemList() {
        return tocItemList;
    }

}
