///////////////////////////////////////////////////////////////////////////////
// For information as to what this class does, see the Javadoc, below.       //
// Copyright (C) 1998, 1999, 2000, 2001, 2002, 2003, 2004, 2005, 2006,       //
// 2007, 2008, 2009, 2010 by Peter Spirtes, Richard Scheines, Joseph Ramsey, //
// and Clark Glymour.                                                        //
//                                                                           //
// This program is free software; you can redistribute it and/or modify      //
// it under the terms of the GNU General Public License as published by      //
// the Free Software Foundation; either version 2 of the License, or         //
// (at your option) any later version.                                       //
//                                                                           //
// This program is distributed in the hope that it will be useful,           //
// but WITHOUT ANY WARRANTY; without even the implied warranty of            //
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the             //
// GNU General Public License for more details.                              //
//                                                                           //
// You should have received a copy of the GNU General Public License         //
// along with this program; if not, write to the Free Software               //
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA //
///////////////////////////////////////////////////////////////////////////////

package edu.cmu.causality2.model;

import edu.cmu.causality2.model.command.ExerciseHistory;
import edu.cmu.causality2.model.command.StartCommand;
import edu.cmu.causality2.model.exercise.Exercise;
import edu.cmu.causality2.model.exercise.WorkedOnExercise;
import edu.cmu.causality2.model.experiment.Experiment;
import edu.cmu.causality2.model.experiment.ExperimentalSetup;
import edu.cmu.causality2.model.experiment.Randomized;
import edu.cmu.causality2.model.population.BayesJoint;
import edu.cmu.causality2.model.sample.Sample;
import edu.cmu.tetrad.bayes.BayesIm;
import edu.cmu.tetrad.bayes.BayesPm;
import edu.cmu.tetrad.bayes.MlBayesIm;
import edu.cmu.tetrad.data.CorrelationMatrix;
import edu.cmu.tetrad.data.CovarianceMatrix;
import edu.cmu.tetrad.graph.*;
import edu.cmu.tetrad.sem.SemIm;
import edu.cmu.tetrad.sem.SemPm;
import edu.cmu.tetrad.util.IM;
import edu.cmu.tetrad.util.dist.Uniform;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.util.ArrayList;
import java.util.List;

/**
 * Tests the PC search.
 *
 * @author Joseph Ramsey
 */
public class TestModel extends TestCase {

    /**
     * Standard constructor for JUnit test cases.
     */
    public TestModel(String name) {
        super(name);
    }

    public void test1() {

        List<Node> nodes = new ArrayList<Node>();
        GraphNode x1 = new GraphNode("X1");
        GraphNode x2 = new GraphNode("X2");
        GraphNode x3 = new GraphNode("X3");

        nodes.add(x1);
        nodes.add(x2);
        nodes.add(x3);

        Graph graph = new EdgeListGraph(nodes);

        graph.addDirectedEdge(x1, x2);
        graph.addDirectedEdge(x2, x3);

        SemPm pm = new SemPm(graph);
        SemIm im = new SemIm(pm);

        ExperimentalSetup setup = new ExperimentalSetup("Test1", graph);
        setup.setManipulation(x1, new Randomized(new Uniform(-1, 1)));

        Exercise exercise = new Exercise(im);
        WorkedOnExercise workedOnExercise = new WorkedOnExercise(exercise);

        Model model = new Model(workedOnExercise);

        model.addIm(im);
        model.addExperimentalSetup(setup);

        Graph hypotheticalGraph = new EdgeListGraph(graph);
        hypotheticalGraph.removeEdge(x2, x3);

        model.addHypotheticalGraph(hypotheticalGraph);

        Graph manipulatedHypotheticalGraph = model.getManipulatedHypotheticalGraph();

        System.out.println(graph);
        System.out.println(manipulatedHypotheticalGraph);

        Experiment experiment = model.getExperiment();

        model.drawSample(100);
        model.drawSample(50);

        for (Sample sample : model.getSamples()) {
            System.out.println(sample);
        }

        Graph manipulatedGraph = model.getManipulatedGraph();

        System.out.println(manipulatedGraph);

        CovarianceMatrix population = model.getSemPopulation();

//        System.out.println(population);


        Graph graph2 = new EdgeListGraph(nodes);

        graph2.addDirectedEdge(x1, x3);
        graph2.addDirectedEdge(x2, x3);

        SemPm pm2 = new SemPm(graph2);
        SemIm im2 = new SemIm(pm2);

        model.addIm(im2);
        model.drawSample(400);
        model.drawSample(600);
        model.drawSample(800);

        for (Sample sample : model.getSamples()) {
            System.out.println(sample);
        }

        model.addIm(im);

        for (Sample sample : model.getSamples()) {
            System.out.println(sample);
        }

        new StartCommand().doIt();

    }

    public void test2() {

        List<Node> nodes = new ArrayList<Node>();
        GraphNode x1 = new GraphNode("X1");
        GraphNode x2 = new GraphNode("X2");
        GraphNode x3 = new GraphNode("X3");

        nodes.add(x1);
        nodes.add(x2);
        nodes.add(x3);

        Dag graph = new Dag(nodes);

        graph.addDirectedEdge(x1, x2);
        graph.addDirectedEdge(x2, x3);

        BayesPm pm = new BayesPm(graph);
        BayesIm im = new MlBayesIm(pm, MlBayesIm.RANDOM);

        Exercise exercise = new Exercise(im);
        WorkedOnExercise workedOnExercise = new WorkedOnExercise(exercise);

        Model model = new Model(workedOnExercise);

        ExperimentalSetup setup = new ExperimentalSetup("Test1", graph);
        setup.setManipulation(x1, new Randomized(new Uniform(-1, 1)));

        model.addIm(im);
        model.addExperimentalSetup(setup);

        Graph hypotheticalGraph = new EdgeListGraph(graph);
        hypotheticalGraph.removeEdge(x2, x3);

        model.addHypotheticalGraph(hypotheticalGraph);

        Graph manipulatedHypotheticalGraph = model.getManipulatedHypotheticalGraph();

        System.out.println(graph);
        System.out.println(manipulatedHypotheticalGraph);

        Experiment experiment = model.getExperiment();

        model.drawSample(100);
        model.drawSample(50);

        for (Sample sample : model.getSamples()) {
            System.out.println(sample);
        }

        Graph manipulatedGraph = model.getManipulatedGraph();

        System.out.println(manipulatedGraph);

        BayesJoint population = model.getBayesPopulation();

//        System.out.println(population);


        Dag graph2 = new Dag(nodes);

        graph2.addDirectedEdge(x1, x3);
        graph2.addDirectedEdge(x2, x3);

        BayesPm pm2 = new BayesPm(graph2);
        BayesIm im2 = new MlBayesIm(pm2, MlBayesIm.RANDOM);

        model.addIm(im2);
        model.drawSample(400);
        model.drawSample(600);
        model.drawSample(800);

        for (Sample sample : model.getSamples()) {
            System.out.println(sample);
        }

        model.addIm(im);

        for (Sample sample : model.getSamples()) {
            System.out.println(sample);
        }
    }

    /**
     * This method uses reflection to collect up all of the test methods from this class and return them to the test
     * runner.
     */
    public static Test suite() {

        // Edit the name of the class in the parens to match the name
        // of this class.
        return new TestSuite(TestModel.class);
    }
}



