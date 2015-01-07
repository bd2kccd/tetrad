package edu.cmu.causality2.model.exercise;

import edu.cmu.causality2.model.command.Command;
import edu.cmu.tetrad.util.TetradSerializable;

import java.util.LinkedList;

/**
 * Stores an exercise for the Causality Lab, in the form of a problem plus student work.
 *
 * @author Joseph Ramsey
 */
public class  WorkedOnExercise implements TetradSerializable {
    static final long serialVersionUID = 23L;

    private Exercise exercise;
    private String essayAnswer;
    private String instructorFeedback;
    private String grade;
    private Finances finances;
    private boolean submitted = false; // Should be set to false if any editing is done.
    private LinkedList<Command> commands;

    public WorkedOnExercise(Exercise exercise) {
        this.exercise = exercise;
        this.setEssayAnswer("");
        this.setInstructorFeedback("");
        this.setGrade("");
        this.setFinances(new Finances(exercise.getResourceTotal(), exercise.getResourcePerObs(),
                exercise.getResourcePerInt()));
        this.setSubmitted(false);
        this.setCommands(new LinkedList<Command>());
    }

    /**
     * Generates a simple exemplar of this class to test serialization.
     *
     * @see edu.cmu.TestSerialization
     * @see edu.cmu.tetradapp.util.TetradSerializableUtils
     */
    public static WorkedOnExercise serializableInstance() {
        return new WorkedOnExercise(Exercise.serializableInstance());
    }

    public Exercise getExercise() {
        return exercise;
    }

    public String getEssayAnswer() {
        return essayAnswer;
    }

    public void setEssayAnswer(String essayAnswer) {
        this.essayAnswer = essayAnswer;
    }

    public String getInstructorFeedback() {
        return instructorFeedback;
    }

    public void setInstructorFeedback(String instructorFeedback) {
        this.instructorFeedback = instructorFeedback;
    }

    public String getGrade() {
        return grade;
    }

    public void setGrade(String grade) {
        this.grade = grade;
    }

    public Finances getFinances() {
        return finances;
    }

    public void setFinances(Finances finances) {
        this.finances = finances;
    }

    public boolean isSubmitted() {
        return submitted;
    }

    public void setSubmitted(boolean submitted) {
        this.submitted = submitted;
    }

    public LinkedList<Command> getCommands() {
        return commands;
    }

    public void setCommands(LinkedList<Command> commands) {
        this.commands = commands;
    }
}
