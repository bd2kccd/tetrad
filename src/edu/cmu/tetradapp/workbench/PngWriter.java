package edu.cmu.tetradapp.workbench;

import edu.cmu.tetrad.graph.Edge;
import edu.cmu.tetrad.graph.EdgeListGraph;
import edu.cmu.tetrad.graph.Graph;
import edu.cmu.tetrad.graph.Node;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: josephramsey
 * Date: 5/1/13
 * Time: 9:21 AM
 * To change this template use File | Settings | File Templates.
 */
public class PngWriter {
    public static void writePng(Graph graph, File file) {
//        circleLayout(graph, 200, 200, 175);

        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());

        // Remove self-loops.
        graph = new EdgeListGraph(graph);

        for (Node node : graph.getNodes()) {
            for (Edge edge : new ArrayList<Edge>(graph.getEdges(node, node))) {
                graph.removeEdge(edge);
            }
        }

        final GraphWorkbench workbench = new GraphWorkbench(graph);

        int maxx = 0;
        int maxy = 0;

        for (Node node : graph.getNodes()) {
            if (node.getCenterX() > maxx) {
                maxx = node.getCenterX();
            }

            if (node.getCenterY() > maxy) {
                maxy = node.getCenterY();
            }
        }

        workbench.setSize(new Dimension(maxx + 50, maxy + 50));
        panel.add(workbench, BorderLayout.CENTER);

        JDialog dialog = new JDialog();
        dialog.add(workbench);
        dialog.pack();

        Dimension size = workbench.getSize();
        BufferedImage image = new BufferedImage(size.width, size.height,
                BufferedImage.TYPE_BYTE_INDEXED);
        Graphics2D graphics = image.createGraphics();
        workbench.paint(graphics);
        image.flush();

        // Write the image to resultFile.
        try {
            ImageIO.write(image, "PNG", file);
        }
        catch (IOException e1) {
            throw new RuntimeException("Could not write to " + file, e1);
        }
    }
}
