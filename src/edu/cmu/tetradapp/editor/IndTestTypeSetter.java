package edu.cmu.tetradapp.editor;

import edu.cmu.tetrad.data.DataModel;
import edu.cmu.tetrad.search.IndTestType;

/**
 * Helps to set independence test types.
 *
 * @author Joseph Ramsey
 */
public interface IndTestTypeSetter {
    void setTestType(IndTestType testType);
    IndTestType getTestType();
    DataModel getDataModel();
    Object getSourceGraph();
}
