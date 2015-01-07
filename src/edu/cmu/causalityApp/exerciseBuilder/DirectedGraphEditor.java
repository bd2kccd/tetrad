package edu.cmu.causalityApp.exerciseBuilder;

import edu.cmu.tetrad.data.Knowledge;
import edu.cmu.tetrad.graph.*;
import edu.cmu.tetrad.session.DelegatesEditing;
import edu.cmu.tetrad.util.JOptionUtils;
import edu.cmu.tetradapp.editor.GraphEditable;
import edu.cmu.tetradapp.editor.RandomGraphEditor;
import edu.cmu.tetradapp.editor.SaveComponentImage;
import edu.cmu.tetradapp.editor.SaveScreenshot;
import edu.cmu.tetradapp.model.GraphWrapper;
import edu.cmu.tetradapp.util.LayoutEditable;
import edu.cmu.tetradapp.util.LayoutMenu;
import edu.cmu.tetradapp.workbench.GraphWorkbench;

import javax.swing.*;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

//import edu.cmu.tetradapp.editor.RandomDagEditor;


/**
 * Displays a workbench editing workbench area together with a toolbench
 * for editing tetrad graphs.
 *
 * @author Aaron Powers
 * @author Joseph Ramsey
 * @version $Revision: 811 $ $Date: 2013-06-09 20:53:26 -0400 (Sun, 09 Jun 2013) $
 */
class DirectedGraphEditor extends JComponent
        implements DelegatesEditing, GraphEditable, LayoutEditable {

    private final GraphWorkbench workbench;
    private GraphWrapper graphWrapper;

    /**
     * Constructs a new GraphEditor for the given EdgeListGraph.
     */
    DirectedGraphEditor(Graph graph, boolean isBayesEditor) {
        setPreferredSize(new Dimension(550, 450));
        setLayout(new BorderLayout());

        this.workbench = new GraphWorkbench(graph);
        GraphToolbar toolbar;
        if (isBayesEditor) {
            toolbar = new BayesGraphToolbar(this.getWorkbench());
        } else {
            toolbar = new SemGraphToolbar(this.getWorkbench());
        }

        JMenuBar menuBar = createGraphMenuBar();

        add(new JScrollPane(this.getWorkbench()), BorderLayout.CENTER);
        add(toolbar, BorderLayout.WEST);
        add(menuBar, BorderLayout.NORTH);

        JLabel label = new JLabel("Double click variable to change name.");
        label.setFont(new Font("SansSerif", Font.PLAIN, 12));
        Box b = Box.createHorizontalBox();
        b.add(Box.createHorizontalStrut(2));
        b.add(label);
        b.add(Box.createHorizontalGlue());
        b.setBorder(new MatteBorder(0, 0, 1, 0, Color.GRAY));

        add(b, BorderLayout.SOUTH);

        this.workbench.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                String propertyName = evt.getPropertyName();
                if ("graph".equals(propertyName)) {
                    Graph _graph = (Graph) evt.getNewValue();

                    if (getWorkbench() != null) {
                        getGraphWrapper().setGraph(_graph);
                    }
                }
            }
        });
    }

    /**
     * Sets the name of this editor.
     */
    public final void setName(String name) {
        String oldName = getName();
        super.setName(name);
        firePropertyChange("name", oldName, getName());
    }

    /**
     * Returns a list of all the SessionNodeWrappers (TetradNodes) and
     * SessionNodeEdges that are model components for the respective
     * SessionNodes and SessionEdges selected in the workbench.
     * Note that the workbench, not the SessionEditorNodes themselves,
     * keeps track of the selection.
     *
     * @return the set of selected model nodes.
     */
    public java.util.List getSelectedModelComponents() {
        List<Component> selectedComponents = this.getWorkbench().getSelectedComponents();
        List<Object> selectedModelComponents = new ArrayList<Object>();

        for (Object comp : selectedComponents) {
            if (comp instanceof Node) {
                selectedModelComponents.add(comp);
            } else if (comp instanceof Edge) {
                selectedModelComponents.add(comp);
            }
        }

        return selectedModelComponents;
    }

    /**
     * @return the workbench as a JComponent.
     */
    public JComponent getEditDelegate() {
        return getWorkbench();
    }

    /**
     * @return the workbench as a GraphWorkBench.
     */
    public GraphWorkbench getWorkbench() {
        return workbench;
    }

    /**
     * @return the graphwrapper in use.
     */
    GraphWrapper getGraphWrapper() {
        return graphWrapper;
    }


    private JMenuBar createGraphMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        JMenu fileMenu = createFileMenu();
        JMenu toolsMenu = createToolsMenu();
//        JMenu layoutMenu = createLayoutMenu();

        menuBar.add(fileMenu);
        menuBar.add(toolsMenu);
        menuBar.add(new LayoutMenu(this));

        return menuBar;
    }


    /**
     * Creates the "file" menu, which allows the user to load, save, and post
     * workbench models.
     *
     * @return this menu.
     */
    private JMenu createFileMenu() {
        JMenu file = new JMenu("File");

//        file.add(new SaveGraph(this, "Save Graph..."));
        file.add(new SaveScreenshot(this, true, "Save Screenshot..."));
        file.add(new SaveComponentImage(workbench, "Save Graph Image..."));

        return file;
    }

    private JMenu createToolsMenu() {
        JMenu tools = new JMenu("Tools");

        JMenuItem graphProperties = new JMenuItem("Graph Properties");

        tools.add(graphProperties);
//        tools.addSeparator();

//        JMenuItem correlateExogenous = new JMenuItem(
//                "Correlate Exogenous Variables");
//        JMenuItem uncorrelateExogenous = new JMenuItem(
//                "Uncorrelate Exogenous Variables");
//        tools.add(correlateExogenous);
//        tools.add(uncorrelateExogenous);
//        tools.addSeparator();

        graphProperties.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Box b = Box.createVerticalBox();

                Box b1 = Box.createHorizontalBox();
                b1.add(new JLabel("Number of nodes: "));
                b1.add(Box.createHorizontalGlue());
                b1.add(new JLabel(
                        "" + getWorkbench().getGraph().getNumNodes()));
                b.add(b1);

                List<Node> nodes = getWorkbench().getGraph().getNodes();
                int numLatents = 0;

                for (Object node1 : nodes) {
                    Node node = (Node) node1;
                    if (node.getNodeType() == NodeType.LATENT) {
                        numLatents++;
                    }
                }

                Box b2 = Box.createHorizontalBox();
                b2.add(new JLabel("Number of latents: "));
                b2.add(Box.createHorizontalGlue());
                b2.add(new JLabel("" + numLatents));
                b.add(b2);

                Box b3 = Box.createHorizontalBox();
                b3.add(new JLabel("Number of edges: "));
                b3.add(Box.createHorizontalGlue());
                b3.add(new JLabel(
                        "" + getWorkbench().getGraph().getNumEdges()));
                b.add(b3);

                Box b4 = Box.createHorizontalBox();
                b4.add(new JLabel("Connectivity: "));
                b4.add(Box.createHorizontalGlue());
                b4.add(new JLabel(
                        "" + getWorkbench().getGraph().getConnectivity()));
                b.add(b4);

                JOptionPane.showMessageDialog(JOptionUtils.centeringComp(), b,
                        "Graph Properties", JOptionPane.INFORMATION_MESSAGE);
            }
        });

//        correlateExogenous.addActionListener(new ActionListener() {
//            public void actionPerformed(ActionEvent e) {
//                correlateExogenousVariables();
//                getWorkbench().invalidate();
//                getWorkbench().repaint();
//            }
//        });
//
//        uncorrelateExogenous.addActionListener(new ActionListener() {
//            public void actionPerformed(ActionEvent e) {
//                uncorrelateExogenousVariables();
//                getWorkbench().invalidate();
//                getWorkbench().repaint();
//            }
//        });

        JMenuItem randomDagModel = new JMenuItem("Random DAG");
        tools.add(randomDagModel);

        randomDagModel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                RandomGraphEditor editor = new RandomGraphEditor(false);

                int ret = JOptionPane.showConfirmDialog(
                        JOptionUtils.centeringComp(), editor,
                        "Edit Random DAG Parameters",
                        JOptionPane.PLAIN_MESSAGE);

                if (ret == JOptionPane.OK_OPTION) {
                    int numNodes = editor.getNumNodes();
                    int numLatentNodes = editor.getNumLatents();
                    int maxEdges = editor.getMaxEdges();
                    int maxDegree = editor.getMaxDegree();
                    int maxIndegree = editor.getMaxIndegree();
                    int maxOutdegree = editor.getMaxOutdegree();
                    boolean connected = editor.isConnected();

                    Dag dag = GraphUtils.randomDag(numNodes,
                            numLatentNodes, maxEdges, maxDegree, maxIndegree,
                            maxOutdegree, connected);

                    workbench.setGraph(dag);
                }
            }
        });

        return tools;
    }

    public Graph getGraph() {
        return workbench.getGraph();
    }

    @Override
    public Map getModelToDisplay() {
        return workbench.getModelToDisplay();
    }

    public Knowledge getKnowledge() {
        return null;
    }

    public Graph getSourceGraph() {
        return getWorkbench().getGraph();
    }

    public void layoutByGraph(Graph graph) {
        getWorkbench().layoutByGraph(graph);
    }

    public void layoutByKnowledge() {
        // Does nothing.
    }

    /**
     * Pastes list of session elements into the workbench.
     */
    public void pasteSubsession(List sessionElements, Point upperLeft) {
        getWorkbench().pasteSubgraph(sessionElements, upperLeft);
        getWorkbench().deselectAll();

        for (Object sessionElement : sessionElements) {
            if (sessionElement instanceof GraphNode) {
                Node modelNode = (Node) sessionElement;
                getWorkbench().selectNode(modelNode);
            }
        }

        getWorkbench().selectConnectingEdges();
    }


    public void setGraph(Graph graph) {
        getWorkbench().setGraph(graph);
    }
}
