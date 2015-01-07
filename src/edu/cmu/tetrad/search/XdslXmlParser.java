package edu.cmu.tetrad.search;

import edu.cmu.tetrad.bayes.BayesIm;
import edu.cmu.tetrad.bayes.BayesPm;
import edu.cmu.tetrad.bayes.MlBayesIm;
import edu.cmu.tetrad.data.Variable;
import edu.cmu.tetrad.graph.*;
import edu.cmu.tetrad.graph.Node;
import nu.xom.*;

import java.util.*;

/**
 * This class takes an xml element representing a bayes im and converts it to
 * a BayesIM
 *
 * @author mattheweasterday
 */
public class XdslXmlParser {

    private HashMap<String, Variable> namesToVars;

    private boolean useDisplayNames = false;

    /**
     * Takes an xml representation of a Bayes IM and reinstantiates the IM
     *
     * @param element the xml of the IM
     * @return the BayesIM
     */
    public BayesIm getBayesIm(Element element) {
        if (!"smile".equals(element.getQualifiedName())) {
            throw new IllegalArgumentException("Expecting " +
                    "smile" + " element.");
        }

        Elements elements = element.getChildElements();

        Element element0 = null, element1 = null;

        for (int i = 0; i < elements.size(); i++) {
            Element _element = elements.get(i);

            if ("nodes".equals(_element.getQualifiedName())) {
                element0 = _element;
            }

            if ("extensions".equals(_element.getQualifiedName())) {
                element1 = _element.getFirstChildElement("genie");
            }
        }

        Map<String, String> displayNames = mapDisplayNames(element1, useDisplayNames);

        BayesIm bayesIm = buildIM(element0, displayNames);

        return bayesIm;
    }

    private BayesIm buildIM(Element element0, Map<String, String> displayNames) {
        Elements elements = element0.getChildElements();

        for (int i = 0; i < elements.size(); i++) {
            if (!"cpt".equals(elements.get(i).getQualifiedName())) {
                throw new IllegalArgumentException("Expecting cpt element.");
            }
        }

        Dag dag = new Dag();

        // Get the nodes.
        for (int i = 0; i < elements.size(); i++) {
            Element cpt = elements.get(i);
            String name = cpt.getAttribute(0).getValue();

            if (displayNames == null) {
                dag.addNode(new GraphNode(name));
            } else {
                dag.addNode(new GraphNode(displayNames.get(name)));
            }

        }

        // Get the edges.
        for (int i = 0; i < elements.size(); i++) {
            Element cpt = elements.get(i);

            Elements cptElements = cpt.getChildElements();

            for (int j = 0; j < cptElements.size(); j++) {
                Element cptElement = cptElements.get(j);

                if (cptElement.getQualifiedName().equals("parents")) {
                    String list = cptElement.getValue();
                    String[] parentNames = list.split(" ");

                    for (String name : parentNames) {
                        if (displayNames == null) {
                            edu.cmu.tetrad.graph.Node parent = dag.getNode(name);
                            edu.cmu.tetrad.graph.Node child = dag.getNode(cpt.getAttribute(0).getValue());
                            dag.addDirectedEdge(parent, child);
                        } else {
                            edu.cmu.tetrad.graph.Node parent = dag.getNode(displayNames.get(name));
                            edu.cmu.tetrad.graph.Node child = dag.getNode(displayNames.get(cpt.getAttribute(0).getValue()));
                            dag.addDirectedEdge(parent, child);
                        }
                    }
                }
            }

            String name;

            if (displayNames == null) {
                name = cpt.getAttribute(0).getValue();
            } else {
                name = displayNames.get(cpt.getAttribute(0).getValue());
            }

            dag.addNode(new GraphNode(name));
        }

        // PM
        BayesPm pm = new BayesPm(dag);

        for (int i = 0; i < elements.size(); i++) {
            Element cpt = elements.get(i);

            String varName = cpt.getAttribute(0).getValue();
            Node node;

            if (displayNames == null) {
                node = dag.getNode(varName);
            } else {
                node = dag.getNode(displayNames.get(varName));
            }

            Elements cptElements = cpt.getChildElements();

            List<String> stateNames = new ArrayList<String>();

            for (int j = 0; j < cptElements.size(); j++) {
                Element cptElement = cptElements.get(j);

                if (cptElement.getQualifiedName().equals("state")) {
                    Attribute attribute = cptElement.getAttribute(0);
                    String stateName = attribute.getValue();
                    stateNames.add(stateName);
                }

            }

            pm.setCategories(node, stateNames);
        }

        // IM
        BayesIm im = new MlBayesIm(pm);

        for (int nodeIndex = 0; nodeIndex < elements.size(); nodeIndex++) {
            Element cpt = elements.get(nodeIndex);

            Elements cptElements = cpt.getChildElements();

            for (int j = 0; j < cptElements.size(); j++) {
                Element cptElement = cptElements.get(j);

                if (cptElement.getQualifiedName().equals("probabilities")) {
                    String list = cptElement.getValue();
                    String[] probsStrings = list.split(" ");
                    List<Double> probs = new ArrayList<Double>();

                    for (String probString : probsStrings) {
                        probs.add(Double.parseDouble(probString));
                    }

                    int count = -1;

                    for (int row = 0; row < im.getNumRows(nodeIndex); row++) {
                        for (int col = 0; col < im.getNumColumns(nodeIndex); col++) {
                            im.setProbability(nodeIndex, row, col, probs.get(++count));
                        }
                    }
                }
            }
        }

        return im;
    }

    private Map<String, String> mapDisplayNames(Element element1, boolean useDisplayNames) {
        if (useDisplayNames) {
            Map<String, String> displayNames = new HashMap<String, String>();

            Elements elements = element1.getChildElements();

            for (int i = 0; i < elements.size(); i++) {
                Element nodeElement = elements.get(i);

                if (nodeElement.getLocalName().equals("textbox")) continue;

                String varName = nodeElement.getAttribute(0).getValue();

                Elements nodeElements = nodeElement.getChildElements();
                String displayName = null;

                for (int j = 0; j < nodeElements.size(); j++) {
                    Element e = nodeElements.get(j);

                    if (e.getQualifiedName().equals("name")) {
                        String value = e.getValue();
                        value = value.replace(" ", "_");
                        displayName = value;
                        break;
                    }

                }

                displayNames.put(varName, displayName);
            }

            return displayNames;
        } else {
            return null;
        }
    }

    public boolean isUseDisplayNames() {
        return useDisplayNames;
    }

    public void setUseDisplayNames(boolean useDisplayNames) {
        this.useDisplayNames = useDisplayNames;
    }
}

