package edu.cmu.tetrad.search;

import edu.cmu.tetrad.data.IKnowledge;
import edu.cmu.tetrad.graph.Graph;
import edu.cmu.tetrad.graph.Node;
import edu.cmu.tetrad.graph.Triple;

import java.util.List;
import java.util.Set;

/**
 * Created by josephramsey on 4/2/14.
 */
public interface IFas {
    boolean isAggressivelyPreventCycles();

    void setAggressivelyPreventCycles(boolean aggressivelyPreventCycles);

    IndependenceTest getIndependenceTest();

    IKnowledge getKnowledge();

    void setKnowledge(IKnowledge knowledge);

    SepsetMap getSepsets();

    int getDepth();

    void setDepth(int depth);

    Graph search();

    Graph search(List<Node> nodes);

    long getElapsedTime();

    int getNumIndependenceTests();

    void setTrueGraph(Graph trueGraph);

    List<Node> getNodes();

    List<Triple> getAmbiguousTriples(Node node);

    void setInitialGraph(Graph initialGraph);

    boolean isVerbose();

    void setVerbose(boolean verbose);

    int getNumFalseDependenceJudgments();

    int getNumDependenceJudgments();
}
