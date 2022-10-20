package eu.europa.ec.leos.vo.coedition;

import java.util.List;

public class CoEditionActionInfo {

    public static enum Operation {
        STORE("store"),
        REMOVE("remove"),
        EXISTS("exists");
        
        private final String value;
        
        Operation(String value) {
            this.value=value;
        }
        public String getValue(){
            return value;
        }
    }
    
    private final boolean isSucessful;
    private final Operation operation;
    
    private final CoEditionVO coEditionVo;
    private final List<CoEditionVO> coEditionVos;//current/remaining list of locks after operation.
    
    public CoEditionActionInfo(boolean isSucessful, Operation operation, CoEditionVO coEditionVo, List<CoEditionVO> coEditionVos) {
        this.isSucessful = isSucessful;
        this.operation=operation;
        this.coEditionVo = coEditionVo;
        this.coEditionVos = coEditionVos;
    }

    public boolean sucesss() {
        return isSucessful;
    }

    public Operation getOperation() {
        return operation;
    }
    
    public CoEditionVO getInfo() {
        return coEditionVo;
    }
    
    public List<CoEditionVO> getCoEditionVos() {
        return coEditionVos;
    }
}
