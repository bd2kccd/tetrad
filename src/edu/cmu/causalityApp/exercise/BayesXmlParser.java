package edu.cmu.causalityApp.exercise;

import edu.cmu.tetrad.bayes.BayesIm;
import edu.cmu.tetrad.bayes.BayesPm;
import edu.cmu.tetrad.bayes.MlBayesIm;
import edu.cmu.tetrad.data.DiscreteVariable;
import edu.cmu.tetrad.data.Variable;
import edu.cmu.tetrad.graph.Dag;
import edu.cmu.tetrad.graph.NodeType;
import nu.xom.Element;
import nu.xom.Elements;
import nu.xom.Text;

import java.util.*;

/**
 * This class takes an xml element representing a bayes im and converts it to
 * a BayesIM
 *
 * @author mattheweasterday
 */
class BayesXmlParser {

    private HashMap<String, Variable> namesToVars;

    /**
     * Takes an xml representation of a Bayes IM and reinstantiates the IM
     *
     * @param element the xml of the IM
     * @return the BayesIM
     */
    public BayesIm getBayesIm(Element element) {
        if (!BayesXmlConstants.BAYES_NET.equals(element.getQualifiedName())) {
            throw new IllegalArgumentException("Expecting " +
                    BayesXmlConstants.BAYES_NET + " element.");
        }

        Elements elements = element.getChildElements();

        Element element0 = elements.get(0);
        Element element1 = elements.get(1);
        Element element2 = elements.get(2);

        List<DiscreteVariable> variables = getVariables(element0);
        BayesPm bayesPm = makeBayesPm(variables, element1);

        return makeBayesIm(bayesPm, element2);
    }


    private List<DiscreteVariable> getVariables(Element element0) {
        if (!BayesXmlConstants.BN_VARIABLES.equals(element0.getQualifiedName())) {
            throw new IllegalArgumentException("Expecting " +
                    BayesXmlConstants.BN_VARIABLES + " element.");
        }

        List<DiscreteVariable> variables = new LinkedList<DiscreteVariable>();

        Elements elements = element0.getChildElements();

        for (int i = 0; i < elements.size(); i++) {
            Element e1 = elements.get(i);
            Elements e2Elements = e1.getChildElements();


            if (!BayesXmlConstants.DISCRETE_VARIABLE.equals(e1.getQualifiedName())) {
                throw new IllegalArgumentException("Expecting " +
                        BayesXmlConstants.DISCRETE_VARIABLE + " " + "element.");
            }

            String name = e1.getAttributeValue(BayesXmlConstants.NAME);

            String isLatentVal = e1.getAttributeValue(BayesXmlConstants.LATENT);
            boolean isLatent = (isLatentVal != null) && ((isLatentVal.equals(BayesXmlConstants.YES)));
            Integer x = new Integer(e1.getAttributeValue(BayesXmlConstants.X));
            Integer y = new Integer(e1.getAttributeValue(BayesXmlConstants.Y));

            int numCategories = e2Elements.size();
            //String[] categories = new String[numCategories];
            List<String> categories = new ArrayList<String>();

            for (int j = 0; j < numCategories; j++) {
                Element e2 = e2Elements.get(j);

                if (!BayesXmlConstants.CATEGORY.equals(e2.getQualifiedName())) {
                    throw new IllegalArgumentException("Expecting " +
                            BayesXmlConstants.CATEGORY + " " + "element.");
                }

                categories.add(e2.getAttributeValue(BayesXmlConstants.NAME));
            }

            DiscreteVariable var = new DiscreteVariable(name, categories);
            if (isLatent) {
                var.setNodeType(NodeType.LATENT);
            }
            var.setCenterX(x.intValue());
            var.setCenterY(y.intValue());
            variables.add(var);
        }

        namesToVars = new HashMap<String, Variable>();

        for (DiscreteVariable variable : variables) {
            String name = variable.getName();
            namesToVars.put(name, variable);
        }

        return variables;
    }


    private BayesPm makeBayesPm(List<DiscreteVariable> variables, Element element1) {
        if (!BayesXmlConstants.PARENTS.equals(element1.getQualifiedName())) {
            throw new IllegalArgumentException("Expecting " +
                    BayesXmlConstants.PARENTS + " element.");
        }

        Dag graph = new Dag();

        for (DiscreteVariable variable : variables) {
            graph.addNode(variable);
        }

        Elements elements = element1.getChildElements();

        for (int i = 0; i < elements.size(); i++) {
            Element e1 = elements.get(i);

            if (!BayesXmlConstants.PARENTS_FOR.equals(e1.getQualifiedName())) {
                throw new IllegalArgumentException("Expecting " +
                        BayesXmlConstants.PARENTS_FOR + " element.");
            }

            String varName = e1.getAttributeValue(BayesXmlConstants.NAME);
            Variable var = namesToVars.get(varName);

            Elements elements1 = e1.getChildElements();

            for (int j = 0; j < elements1.size(); j++) {
                Element e2 = elements1.get(j);

                if (!BayesXmlConstants.PARENT.equals(e2.getQualifiedName())) {
                    throw new IllegalArgumentException("Expecting " +
                            BayesXmlConstants.PARENT + " element.");
                }

                String parentName = e2.getAttributeValue(BayesXmlConstants.NAME);
                Variable parent = namesToVars.get(parentName);

                graph.addDirectedEdge(parent, var);
            }
        }

        BayesPm bayesPm = new BayesPm(graph);

        for (DiscreteVariable graphVariable : variables) {
            List<String> categories = graphVariable.getCategories();
            bayesPm.setCategories(graphVariable, categories);
        }

        return bayesPm;
    }


    private BayesIm makeBayesIm(BayesPm bayesPm, Element element2) {
        if (!BayesXmlConstants.CPTS.equals(element2.getQualifiedName())) {
            throw new IllegalArgumentException("Expecting " +
                    BayesXmlConstants.CPTS + " element.");
        }

        MlBayesIm bayesIm = new MlBayesIm(bayesPm);

        Elements elements2 = element2.getChildElements();

        for (int nodeIndex = 0; nodeIndex < elements2.size(); nodeIndex++) {
            Element e1 = elements2.get(nodeIndex);

            if (!BayesXmlConstants.CPT.equals(e1.getQualifiedName())) {
                throw new IllegalArgumentException("Expecting " +
                        BayesXmlConstants.CPT + " element.");
            }

            String numRowsString = e1.getAttributeValue(BayesXmlConstants.NUM_ROWS);
            String numColsString = e1.getAttributeValue(BayesXmlConstants.NUM_COLS);
            //String rowSumTolerance = e1.getAttributeValue("rowSumTolerance");

            int numRows = Integer.parseInt(numRowsString);
            int numCols = Integer.parseInt(numColsString);

            Elements e1Elements = e1.getChildElements();

            if (e1Elements.size() != numRows) {
                throw new IllegalArgumentException("Element cpt claimed " +
                        +numRows + " rows, but there are only "
                        + e1Elements.size() + " rows in the file.");
            }

            for (int rowIndex = 0; rowIndex < numRows; rowIndex++) {
                Element e2 = e1Elements.get(rowIndex);

                if (!BayesXmlConstants.ROW.equals(e2.getQualifiedName())) {
                    throw new IllegalArgumentException("Expecting " +
                            BayesXmlConstants.ROW + " element.");
                }

                Text rowNode = (Text) e2.getChild(0);
                String rowString = rowNode.getValue();

                StringTokenizer t = new StringTokenizer(rowString);

                for (int colIndex = 0; colIndex < numCols; colIndex++) {
                    String token = t.nextToken();

                    try {
                        double value = Double.parseDouble(token);
                        bayesIm.setProbability(nodeIndex, rowIndex, colIndex, value);
                        //System.out.println("value:" + value + "token: " + token);
                    } catch (NumberFormatException e) {
                        // Skip.
                    }
                }

                if (t.hasMoreTokens()) {
                    throw new IllegalArgumentException("Element cpt claimed " +
                            numCols + " columnns , but there are more that that " +
                            "in the file.");
                }
            }
        }

        return bayesIm;
    }
}

