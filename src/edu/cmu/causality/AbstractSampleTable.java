package edu.cmu.causality;

import javax.swing.table.AbstractTableModel;

/**
 * This is the abstract sample table model.
 */
public abstract class AbstractSampleTable extends AbstractTableModel {
    abstract public Object[] getLongestValues();
}
