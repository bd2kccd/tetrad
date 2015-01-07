package edu.cmu.causalityApp.exercise;

/**
 * This class holds the data type that specifies whethere a window in the
 * navigator should be included, and if included, whether it is hidable or not
 * hidable.
 *
 * @author mattheweasterday
 */
public class WindowInclusionStatus {
    public static final WindowInclusionStatus NOT_INCLUDED
            = new WindowInclusionStatus("NOT_INCLUDED");
    public static final WindowInclusionStatus HIDABLE
            = new WindowInclusionStatus("HIDABLE");
    public static final WindowInclusionStatus NOT_HIDABLE
            = new WindowInclusionStatus("NOT_HIDABLE");

    /**
     * The name of this type.
     */
    private final String name;

    /**
     * Private, not allowed to add new types
     *
     * @param name name of the variable
     */
    private WindowInclusionStatus(String name) {
        this.name = name;
    }

    /**
     * Prints out the name of the type.
     */
    public String toString() {
        return name;
    }
}
