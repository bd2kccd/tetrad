package edu.cmu.causality2.model.command;

import edu.cmu.oli.CausalityActivity;
import nu.xom.Attribute;
import nu.xom.Element;

/**
 * This abstract class follows the "Command Pattern" and is used wherever the
 * user does some semantically meaningful action.  This allows us to do several
 * things:
 * 1. saving student work in the exercise file
 * 2. replaying saved student work
 * 3. logging to OLI
 * 4. undo (if we wanted to)
 *
 * @author mattheweasterday
 */
abstract public class AbstractCommand implements Command {

    // ========================================================
    //    ABSTRACT METHODS
    // ========================================================

    /**
     * Every subclass must provide a name for writing moves to xml.
     */
    protected abstract String getCommandName();


    // ========================================================
    //    PUBLIC METHODS
    // ========================================================

    /**
     * Call this method to run the moves.  It will first run the moves,
     * send a log message to the OLI databases (if logging is on) and then add
     * the moves to the list of executed commands to be written out if the
     * exercise is saved.
     */
    public void doIt() {
        justDoIt();
        logMe();
        ExerciseHistory.getInstance().add(this);
    }

    /**
     * Call this method to run the moves.  It will first run the moves,
     * send a log message to the OLI databases (if logging is on) and then add
     * the moves to the list of executed commands to be written out if the
     * exercise is saved.
     */
    public void redo() {
        justDoIt();

    }

    /**
     * Call this method to run a moves with out sending a log.
     * Occassionally, you will want to run a moves without sending a log
     * message to the OLI database, for instance if you are replaying students
     * worked.
     */
    public void doItNoLog() {
        justDoIt();
        ExerciseHistory.getInstance().add(this);
    }

    /**
     * Sends an xml version of the moves to the OLI database.  Called by
     * the doIt method.
     * <p/>
     * SuperActivity stuff:
     * <p/>
     * <p/>
     * //  Simple logging example
     * //
     * <p/>
     * <p/>
     * // action, info type, info
     * logging.logActionLog("SAMPLE_INIT", "className", getClass().getName());
     */
    private void logMe() {
//        ActionLog log = ActionLogFactory.getActionLog(
//                "moves", getCommandName(), render().toXML());
        CausalityActivity.getInstance().logAction("moves", getCommandName(), render().toXML());
    }


    /**
     * Default method for converting moves to xml to send to OLI database
     * or record/save work to file.  This method calls its helper methods
     * "renderAttributes" and "renderChildren" so in most cases, subclasses
     * can override those methods and do not need to alter "render"
     *
     * @return an xml Element that represents the moves
     */
    public Element render() {
        Element commandElement = new Element(getCommandName());
        Attribute[] attributes = renderAttributes();

        if (attributes != null) {
            for (Attribute attribute : attributes) {
                commandElement.addAttribute(attribute);
            }
        }

        Element[] elms = renderChildren();

        if (elms != null) {
            for (Element elm : elms) {
                commandElement.appendChild(elm);
            }
        }

        return commandElement;
    }


    // ========================================================
    //    PROTECTED METHODS
    // ========================================================

    /**
     * Helper method for "render" that specifies all the parameters the moves
     * might want to save, e.g. a CreateSample moves would have "sample size",
     * 100; or "sample seed", 5032342342 as attribute-value pairs. In some
     * cases, the moves will want to include additional xml elements as part
     * of the moves, e.g. a "create hypothesis" moves might have an xml
     * element representing the hypothesis--in that case, use the
     * "renderChildren" method.
     *
     * @return an array of xml <code>Attribute</code>
     */
    protected Attribute[] renderAttributes() {
        return null;
    }


    /**
     * Default implemenatation of helper method for "render" that specifies
     * additional xml elements that are part of the moves, e.g. a
     * "create hypothesis" moves might have an xml element representing the
     * hypothesis.  If the xml moves needs simple attribute value pairs,
     * override the "renderAttributes" moves.
     *
     * @return an array of xml <code>Element</code>
     */
    protected Element[] renderChildren() {
        return null;
    }

}
