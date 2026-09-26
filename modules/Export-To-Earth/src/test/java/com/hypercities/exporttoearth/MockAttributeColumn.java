package com.hypercities.exporttoearth;

import org.gephi.graph.api.Column;
import org.gephi.graph.api.ColumnObserver;
import org.gephi.graph.api.Estimator;
import org.gephi.graph.api.Origin;
import org.gephi.graph.api.Table;

/**
 *
 * @author Dave Shepard
 */
public class MockAttributeColumn<T> implements Column {
    private String title;
    private T value;

    public MockAttributeColumn(String title, T value) {
        this.title = title;
        this.value = value;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public int getIndex() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getId() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Object getDefaultValue() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Class getTypeClass() {
        return value.getClass();
    }

    public Origin getOrigin() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public Estimator getEstimator() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public Table getTable() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public boolean isIndexed() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public boolean isArray() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public boolean isDynamic() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public boolean isNumber() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public boolean isProperty() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public boolean isReadOnly() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public void setEstimator(Estimator estmtr) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public ColumnObserver createColumnObserver() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public ColumnObserver createColumnObserver(boolean withDiff) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean exists() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean isDynamicAttribute() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
