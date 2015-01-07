package edu.cmu.causalityApp.exercise;

import edu.cmu.tetrad.bayes.BayesIm;
import edu.cmu.tetrad.bayes.BayesPm;
import edu.cmu.tetrad.data.DiscreteVariable;
import edu.cmu.tetrad.graph.Node;
import edu.cmu.tetrad.graph.NodeType;
import nu.xom.Attribute;
import nu.xom.Element;
import nu.xom.Text;

import java.text.NumberFormat;


/**
 * Renders Bayes nets and related models in XML.
 *
 * @author Joseph Ramsey
 * @version $Revision: 812 $ $Date: 2013-06-09 23:36:35 -0400 (Sun, 09 Jun 2013) $
 */
class BayesXmlRenderer {

    //static final private NumberFormat nf = new DecimalFormat("0.0000");
    static final private NumberFormat nf;

    static {
        NumberFormat nf1 = NumberFormat.getInstance();
        nf1.setMinimumFractionDigits(4);
        nf1.setMaximumFractionDigits(4);
        nf = nf1;
    }

    /**
     * Turns a bayes IM into xml
     *
     * @param bayesIm the im to convert
     * @return an xml representation of the bayes im
     */
    public static Element getElement(BayesIm bayesIm) {
        if (bayesIm == null) {
            throw new NullPointerException();
        }

        Element element = new Element(BayesXmlConstants.BAYES_NET);
        element.appendChild(getVariablesElement(bayesIm));
        element.appendChild(getParentsElement(bayesIm));
        element.appendChild(getCptsElement(bayesIm));

        return element;
    }


    private static Element getVariablesElement(BayesIm bayesIm) {
        Element element = new Element(BayesXmlConstants.BN_VARIABLES);

        for (int i = 0; i < bayesIm.getNumNodes(); i++) {
            Node node = bayesIm.getNode(i);
            BayesPm bayesPm = bayesIm.getBayesPm();
            DiscreteVariable variable = (DiscreteVariable) bayesPm.getVariable(node);
            Element element1 = new Element(BayesXmlConstants.DISCRETE_VARIABLE);
            element1.addAttribute(new Attribute(BayesXmlConstants.NAME, variable.getName()));
            element1.addAttribute(new Attribute(BayesXmlConstants.INDEX, "" + i));

            boolean latent = node.getNodeType() == NodeType.LATENT;

            if (latent) {
                element1.addAttribute(new Attribute(BayesXmlConstants.LATENT,
                        BayesXmlConstants.YES));
            }

            element1.addAttribute(new Attribute(BayesXmlConstants.X, "" + node.getCenterX()));
            element1.addAttribute(new Attribute(BayesXmlConstants.Y, "" + node.getCenterY()));

            for (int j = 0; j < variable.getNumCategories(); j++) {
                Element category = new Element(BayesXmlConstants.CATEGORY);
                category.addAttribute(new Attribute(BayesXmlConstants.NAME, variable.getCategory(j)));
                category.addAttribute(new Attribute(BayesXmlConstants.INDEX, "" + j));
                element1.appendChild(category);
            }

            element.appendChild(element1);
        }

        return element;
    }


    private static Element getParentsElement(BayesIm bayesIm) {
        Element parents = new Element(BayesXmlConstants.PARENTS);

        for (int i = 0; i < bayesIm.getNumNodes(); i++) {
            Element variable = new Element(BayesXmlConstants.PARENTS_FOR);
            parents.appendChild(variable);

            String varName = bayesIm.getNode(i).getName();
            variable.addAttribute(new Attribute(BayesXmlConstants.NAME, varName));

            int[] parentIndices = bayesIm.getParents(i);

            for (int j = 0; j < parentIndices.length; j++) {
                Element parent = new Element(BayesXmlConstants.PARENT);
                variable.appendChild(parent);

                Node parentNode = bayesIm.getNode(parentIndices[j]);
                parent.addAttribute(new Attribute(BayesXmlConstants.NAME, parentNode.getName()));
                parent.addAttribute(new Attribute(BayesXmlConstants.INDEX, "" + j));
            }
        }

        return parents;
    }


    private static Element getCptsElement(BayesIm bayesIm) {
        Element cpts = new Element(BayesXmlConstants.CPTS);
        cpts.addAttribute(new Attribute(BayesXmlConstants.ROW_SUM_TOLERANCE, "0.0001"));

        for (int i = 0; i < bayesIm.getNumNodes(); i++) {
            Element cpt = new Element(BayesXmlConstants.CPT);
            cpts.appendChild(cpt);

            String varName = bayesIm.getNode(i).getName();
            int numRows = bayesIm.getNumRows(i);
            int numCols = bayesIm.getNumColumns(i);

            cpt.addAttribute(new Attribute(BayesXmlConstants.VARIABLE, varName));
            cpt.addAttribute(new Attribute(BayesXmlConstants.NUM_ROWS, "" + numRows));
            cpt.addAttribute(new Attribute(BayesXmlConstants.NUM_COLS, "" + numCols));

            for (int j = 0; j < numRows; j++) {
                Element row = new Element(BayesXmlConstants.ROW);
                cpt.appendChild(row);

                StringBuilder buf = new StringBuilder();

                for (int k = 0; k < numCols; k++) {
                    double probability = bayesIm.getProbability(i, j, k);
                    buf.append(nf.format(probability)).append(" ");
                }

                String s = buf.toString();
                row.appendChild(new Text(s.trim()));
            }
        }

        return cpts;
    }
}