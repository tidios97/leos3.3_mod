package eu.europa.ec.leos.ui.event.contribution;

import java.util.Arrays;

public class MergeActionRequestEvent {

    public enum MergeAction {
        ACCEPT("accept"),
        REJECT("reject"),
        UNDO("undo");

        private String action;

        MergeAction(String action) {
            this.action = action;
        }

        public String getAction() {
            return action;
        }

        public static MergeAction of(String action) {
            return Arrays.asList(MergeAction.values())
                    .stream()
                    .filter(x -> x.action.equals(action))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Action " + action + " is not handled"));
        }
    }

    public enum ElementState {
        ADD("add"),
        DELETE("delete"),
        MOVE("move"),
        CONTENT_CHANGE("content_change");

        private String state;

        ElementState(String action) {
            this.state = action;
        }

        public String getState() {
            return state;
        }

        public static ElementState of(String state) {
            return Arrays.asList(ElementState.values())
                    .stream()
                    .filter(x -> x.state.equals(state))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("State " + state + " is not handled"));
        }
    }

    private final MergeAction action;
    private final ElementState elementState;
    private final String elementId;
    private final String elementTagName;

    public MergeActionRequestEvent(MergeAction action, ElementState elementState, String elementId, String elementTagName) {
        this.action = action;
        this.elementState = elementState;
        this.elementId = elementId;
        this.elementTagName = elementTagName;
    }

    public MergeAction getAction() {
        return action;
    }
    public ElementState getElementState() { return  elementState; }
    public String getElementId() {
        return elementId;
    }
    public String getElementTagName() {
        return elementTagName;
    }
}

