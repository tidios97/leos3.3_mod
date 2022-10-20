package eu.europa.ec.leos.ui.component.toc;

import com.vaadin.shared.ui.grid.GridClientRpc;
import com.vaadin.shared.ui.grid.ScrollDestination;
import com.vaadin.ui.TreeGrid;
import com.vaadin.ui.components.grid.GridSelectionModel;

public class MultiSelectTreeGrid<T> extends TreeGrid<T> {
    private static final long serialVersionUID = 1462333925534793763L;

    @Override
    public void setSelectionModel(GridSelectionModel<T> model) {
     super.setSelectionModel(model);
    }
    
    
    @Override
    public void scrollTo(int row) throws IllegalArgumentException {
        getRpcProxy(GridClientRpc.class).scrollToRow(row, ScrollDestination.ANY);
    }
}
