package edu.cmu.causalityApp.undo;

import edu.cmu.oli.CausalityActivity;
import edu.cmu.oli.LoggingListener;

import javax.swing.*;
import java.awt.*;

/**
 * This describes the logging inspector panel which shows the previous actions
 * performed so far. This panel is used in the UndoInspectorFrame.
 *
 * @author mattheweasterday
 */
public class LoggingInspectorPanel extends JPanel implements LoggingListener {

    private final DefaultListModel listModel = new DefaultListModel();

    /**
     * Constructor.
     */
    public LoggingInspectorPanel() {
        super();

        this.setLayout(new BorderLayout());

        CausalityActivity.getInstance().addLoggingListener(this);
        JList list = new JList(listModel);

        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setLayoutOrientation(JList.VERTICAL);
        list.setVisibleRowCount(-1);

        JScrollPane listScroller = new JScrollPane(list);
        listScroller.setPreferredSize(new Dimension(250, 80));
        listScroller.setAlignmentX(LEFT_ALIGNMENT);
        listScroller.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        //Create a container so that we can add a title around
        //the scroll pane.  Can't add a title directly to the
        //scroll pane because its background would be white.
        //Lay out the label and scroll pane from top to bottom.
        JPanel listPane = new JPanel();
        listPane.setLayout(new BoxLayout(listPane, BoxLayout.PAGE_AXIS));
        JLabel label = new JLabel("Logging");
        label.setLabelFor(list);
        listPane.add(label);
        listPane.add(Box.createRigidArea(new Dimension(0, 5)));
        listPane.add(listScroller);
        listPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        add(listPane);
    }

    /**
     * Adds a log to the getModel list.
     *
     * @param action, infoType, info
     */
    public void logPerformed(String action, String infoType, String info) {
        listModel.addElement(infoType + "  " + info);
    }
}
