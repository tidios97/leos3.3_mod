package eu.europa.ec.leos.vo.toc;

import java.util.ArrayList;
import java.util.List;

public class TableOfContentItemHtmlVO {
    
    private String name;
    private String href;
    private List<TableOfContentItemHtmlVO> children = new ArrayList<>();
    
    public TableOfContentItemHtmlVO(String name) {
        this.name = name;
    }
    
    public TableOfContentItemHtmlVO(String name, String href) {
        this.name = name;
        this.href = href;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getHref() {
        return href;
    }
    
    public void setHref(String href) {
        this.href = href;
    }
    
    public List<TableOfContentItemHtmlVO> getChildren() {
        return children;
    }
    
    public void setChildren(List<TableOfContentItemHtmlVO> children) {
        this.children = children;
    }
    
    @Override
    public String toString() {
        return "TableOfContentItemHtmlVO [name=" + name + ", href=" + href + ", children=" + children + "]";
    }
}
