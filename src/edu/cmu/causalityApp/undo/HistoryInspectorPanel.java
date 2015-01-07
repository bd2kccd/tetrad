package edu.cmu.causalityApp.undo;

import edu.cmu.command.ExerciseHistory;
import edu.cmu.command.ExerciseHistoryListener;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * This describes the history inspector panel, used by the instructor to view a
 * student's exercise history.
 *
 * @author mattheweasterday
 */
public class HistoryInspectorPanel extends JPanel implements ExerciseHistoryListener {

    private final DefaultListModel listModel = new DefaultListModel();
    private final JList list;
    private int INDEX = 0;

    /**
     * Constructor.
     */
    public HistoryInspectorPanel() {
        super();

        this.setLayout(new BorderLayout());

        ExerciseHistory.getInstance().addHistoryListener(this);
        list = new JList(listModel);

        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setLayoutOrientation(JList.VERTICAL);
        list.setVisibleRowCount(-1);

        list.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                //this makes it so user can't jump to other places in the
                //moves history, they have to use the buttons provided
                list.setSelectedIndex(INDEX);
            }
        });

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
        JLabel label = new JLabel("History");
        label.setLabelFor(list);
        listPane.add(label);
        listPane.add(Box.createRigidArea(new Dimension(0, 5)));
        listPane.add(listScroller);
        listPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        add(listPane);
        add(makeToolbar(), BorderLayout.SOUTH);

        historyChanged();
        list.setSelectedIndex(0);
    }

    /**
     * When the history changes, refreshes the list model to reflect the change.
     */
    public void historyChanged() {
        ExerciseHistory h = ExerciseHistory.getInstance();

        listModel.removeAllElements();
        for (int i = 0; i < h.getNumCommands(); i++) {
            listModel.addElement(h.getCommandName(i));
        }
    }


    private JToolBar makeToolbar() {
        JToolBar toolbar = new JToolBar();
        toolbar.setFloatable(false);

        JLabel replay = new JLabel("Replay work:");
        JButton minus = new JButton(" Rewind");
        JButton plus = new JButton(" Forward");

        toolbar.add(replay);
        toolbar.add(minus);
        toolbar.add(plus);


        plus.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent event) {
                        if (INDEX == listModel.getSize() - 1) {
                            return;
                        }
                        INDEX++;
                        list.setSelectedIndex(INDEX);
                        ExerciseHistory.getInstance().doCommand(INDEX);
                    }
                }
        );

        minus.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent event) {
                        if (INDEX == 0) {
                            return;
                        }
                        ExerciseHistory.getInstance().undoCommand(INDEX);
                        INDEX--;
                        list.setSelectedIndex(INDEX);
                    }
                }
        );

        return toolbar;
    }
}
