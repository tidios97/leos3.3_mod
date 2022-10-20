package eu.europa.ec.leos.vo.toc;

import eu.europa.ec.leos.model.action.SoftActionType;
import org.w3c.dom.Node;

import java.util.ArrayList;
import java.util.List;

public class TocItemVOBuilder {

    private TocItem tocItem;
    private String id;
    private String originAttr;
    private String number;
    private String elementNumberId;
    private String originNumAttr;
    private String heading;
    private String originHeadingAttr;
    private Node node;
    private String content;
    private List<TableOfContentItemVO> childItems = new ArrayList<>();
    private TableOfContentItemVO parentItem;
    private int itemDepth;
    private int itemIndentLevel;
    private SoftActionType softActionAttr;
    private Boolean isSoftActionRoot;
    private String softMoveFrom;
    private String softMoveTo;
    private boolean affected;
    private String list;

    private TocItemVOBuilder() {
    }

    public static TocItemVOBuilder getBuilder() {
        return new TocItemVOBuilder();
    }

    public TocItemVOBuilder withChild(TableOfContentItemVO child) {
        childItems.add(child);
        return this;
    }

    public TocItemVOBuilder withChildItems(List<TableOfContentItemVO> childItems) {
        this.childItems = childItems;
        return this;
    }

    public TocItemVOBuilder withTocItem(TocItem tocItem) {
        this.tocItem = tocItem;
        return this;
    }

    public TocItemVOBuilder withId(String id) {
        this.id = id;
        return this;
    }

    public TocItemVOBuilder withOriginAttr(String originAttr) {
        this.originAttr = originAttr;
        return this;
    }

    public TocItemVOBuilder withNumber(String number) {
        this.number = number;
        return this;
    }
    
    public TocItemVOBuilder withElementNumberId(String elementNumberId) {
        this.elementNumberId = elementNumberId;
        return this;
    }

    public TocItemVOBuilder withOriginNumAttr(String originNumAttr) {
        this.originNumAttr = originNumAttr;
        return this;
    }

    public TocItemVOBuilder withHeading(String heading) {
        this.heading = heading;
        return this;
    }

    public TocItemVOBuilder withOriginHeadingAttr(String originHeadingAttr) {
        this.originHeadingAttr = originHeadingAttr;
        return this;
    }
    
    public TocItemVOBuilder withList(String list) {
        this.list = list;
        return this;
    }

    public TocItemVOBuilder withNode(Node node) {
        this.node = node;
        return this;
    }

    public TocItemVOBuilder withContent(String content) {
        this.content = content;
        return this;
    }

    public TocItemVOBuilder withItemDepth(int itemDepth) {
        this.itemDepth = itemDepth;
        return this;
    }

    public TocItemVOBuilder withIndentLevel(int itemIndentLevel) {
        this.itemIndentLevel = itemIndentLevel;
        return this;
    }

    public TocItemVOBuilder withSoftActionAttr(SoftActionType softActionAttr) {
        this.softActionAttr = softActionAttr;
        return this;
    }

    public TocItemVOBuilder withIsSoftActionRoot(Boolean isSoftActionRoot) {
        this.isSoftActionRoot = isSoftActionRoot;
        return this;
    }

    public TocItemVOBuilder withSoftMoveFrom(String softMoveFrom) {
        this.softMoveFrom = softMoveFrom;
        return this;
    }

    public TocItemVOBuilder withSoftMoveTo(String softMoveTo) {
        this.softMoveTo = softMoveTo;
        return this;
    }

    public TocItemVOBuilder withParentItem(TableOfContentItemVO parentItem) {
        this.parentItem = parentItem;
        return this;
    }

    public TocItemVOBuilder withAffected(boolean affected) {
        this.affected = affected;
        return this;
    }

    public TableOfContentItemVO build() {
        TableOfContentItemVO tocVO = new TableOfContentItemVO(tocItem, id, originAttr, number, originNumAttr, heading,
                node, content);
        tocVO.setElementNumberId(elementNumberId);
        tocVO.addAllChildItems(childItems);
        tocVO.setItemDepth(itemDepth);
        tocVO.setIndentLevel(itemIndentLevel);
        tocVO.setSoftActionAttr(softActionAttr);
        tocVO.setSoftActionRoot(isSoftActionRoot);
        tocVO.setSoftMoveFrom(softMoveFrom);
        tocVO.setSoftMoveTo(softMoveTo);
        tocVO.setOriginHeadingAttr(originHeadingAttr);
        tocVO.setParentItem(parentItem);
        tocVO.setAffected(affected);
        tocVO.setList(list);
        return tocVO;
    }
}
