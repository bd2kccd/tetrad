package edu.cmu.causalityApp.component;

/**
 * This interface contains all of the methods required for one of the view classes to
 * draw the component
 */
public interface ViewComponent {

    /**
     * @return if the given point is within this view.
     */
    boolean contains(Pos point);

    /**
     * Select or unselect this view.
     */
    void setSelected(boolean selected);
}
