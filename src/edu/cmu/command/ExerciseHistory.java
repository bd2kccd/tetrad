package edu.cmu.command;

import nu.xom.Element;

import java.util.ArrayList;
import java.util.List;

/**
 * This class holds all the commands that have been run as the user has worked
 * on the exercise
 *
 * @author mattheweasterday
 */
public class ExerciseHistory {
    private static ExerciseHistory ourInstance = new ExerciseHistory();
    private static List<Command> commandList;
    private static List<ExerciseHistoryListener> listeners;


    /**
     * Use this class to reset all the exercise history at present, when a new
     * exercise is read.
     */
    public static void resetInstance() {
        ourInstance = new ExerciseHistory();
    }

    /**
     * This class implements the Singleton pattern--there is only one instance
     * of the history and you can use this method to get it.
     *
     * @return instance of ExerciseHistory
     */
    public static ExerciseHistory getInstance() {
        return ourInstance;
    }

    /**
     * Use this to initialize the history, i.e. if you are loading saved work
     * get the commands executed in the last session and run them
     */
    public void add(Command[] cmds, boolean runCommand) {
        for (Command cmd : cmds) {

            if (runCommand) {
                cmd.doItNoLog();
            } else {
                add(cmd);
            }

            //This following moves is commented because it adds additional
            //commands to the exercise history
            //add(cmds[i]);

        }
    }


    /**
     * Add a moves to the exercise history and notify any listeners that the
     * history has been changed.
     *
     * @param cmd the moves that was run
     */
    public void add(Command cmd) {
        commandList.add(cmd);
        for (Object listener : listeners) {
            ((ExerciseHistoryListener) listener).historyChanged();
        }
    }


    /**
     * Get rid of all the commands in the history, e.g. like when a new exercise
     * is loaded.
     */
    public void clear() {
        commandList.clear();
    }


    /**
     * Gets size of moves history
     *
     * @return the amount of commands in the history.
     */
    public int getNumCommands() {
        return commandList.size();
    }


    /**
     * Get the name of the ith moves
     *
     * @param index of the moves whose name we want
     * @return the name of the moves
     */
    public String getCommandName(int index) {
        Command cmd = commandList.get(index);
        if (cmd == null) {
            return null;
        }
        return cmd.toString();
    }


    /**
     * Run the ith moves in the exercise history
     *
     * @param index of the moves to run
     */
    public void doCommand(int index) {
        Command cmd = commandList.get(index);
        // todo here! the place replay calls justDoIt()
        // probably pass in a variable to state that its going to redo??
//        if(cmd instanceof ShowAnswerCommand){
//            ((ShowAnswerCommand)cmd).redo();
//        }
//        else if(cmd instanceof CheckAnswerCommand){
//            ((CheckAnswerCommand)cmd).redo();
//        }
//        else{
        cmd.redo();
//        }
    }


    /**
     * Undoo the ith moves in the exercise history
     *
     * @param index of the moves to undo
     */
    public void undoCommand(int index) {
        Command cmd = commandList.get(index);
        cmd.undo();
    }


    /**
     * Notify the listener when any commands are added or removed.
     */
    public void addHistoryListener(ExerciseHistoryListener hl) {
        listeners.add(hl);
    }


    /**
     * Convert the whole moves history to an xml element
     *
     * @return an xml representation of the moves history
     */
    public Element render() {
        Element cmdElements = new Element("commands");

        // Double check. It was an iterator. jdramsey 6/9/2013
        for (Command cmd : commandList) {
            cmdElements.appendChild(cmd.render());
        }

        return cmdElements;
    }

    /*
    *  Private constructor for the singleton pattern
    */
    private ExerciseHistory() {
        commandList = new ArrayList<Command>();
        commandList.add(new StartCommand());
        listeners = new ArrayList<ExerciseHistoryListener>();
    }
}
