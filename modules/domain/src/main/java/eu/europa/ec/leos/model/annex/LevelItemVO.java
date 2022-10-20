package eu.europa.ec.leos.model.annex;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class LevelItemVO implements Serializable {

    private static final long serialVersionUID = 3551047180046009042L;
    
    private String id;
    private String levelNum;
    private int levelDepth;
    private String origin;
    
    private List<LevelItemVO> children = new ArrayList<LevelItemVO>();
    
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    
    public String getLevelNum() {
        return levelNum;
    }
    
    public void setLevelNum(String levelNum) {
        this.levelNum = levelNum;
    }
    
    public int getLevelDepth() {
        return levelDepth;
    }
    
    public void setLevelDepth(int levelDepth) {
        this.levelDepth = levelDepth;
    }

    public String getOrigin() { return origin; }

    public void setOrigin(String origin) { this.origin = origin; }
    
    public List<LevelItemVO> getChildren() {
        return children;
    }
    
    public void addChildLevelItemVO(LevelItemVO levelItemVO) {
        children.add(levelItemVO);
    }
    
    @Override
    public String toString() {
        return "LevelItemVO [id=" + id + ", levelNum=" + levelNum + ", levelDepth=" + levelDepth + ", children=" + children + "]";
    }
}
