package edu.cmu.tetrad.search;

import edu.cmu.tetrad.graph.Node;
import edu.cmu.tetrad.util.TetradSerializable;

import java.util.List;

/**
 * Created by josephramsey on 3/11/14.
 */
public interface IDeltaSextadTest extends TetradSerializable {
    double calcChiSquare(Sextad... sextads);

    double getPValue();

    double getPValue(Sextad... sextads);

    void setCacheFourthMoments(boolean cacheFourthMoments);

    List<Node> getVariables();
}
