/**
 * Class MouseHandler
 * Created: Mar 22, 2002
 * @author juan
 */

package edu.cmu.causalityApp.graphEditors;

import javax.swing.event.MouseInputListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;


/**
 * This class describes the mouse input handler used for the graph creation
 * and editing in the hypothetical graph wizard.
 */
class InputHandler implements MouseInputListener, KeyListener {

    //////// MouseInput
    public final void mouseClicked(MouseEvent e) {
        mouseClicked(e.getX(), e.getY(), e.getModifiersEx(), e.getClickCount());
    }

    public final void mouseEntered(MouseEvent e) {
        mouseEntered(e.getX(), e.getY(), e.getModifiersEx());
    }

    public final void mouseExited(MouseEvent e) {
        mouseExited(e.getX(), e.getY(), e.getModifiersEx());
    }

    public final void mousePressed(MouseEvent e) {
        mousePressed(e.getX(), e.getY(), e.getModifiersEx());
    }

    public final void mouseReleased(MouseEvent e) {
        mouseReleased(e.getX(), e.getY(), e.getModifiersEx());
    }

    public final void mouseDragged(MouseEvent e) {
        mouseDragged(e.getX(), e.getY(), e.getModifiersEx());
    }

    public final void mouseMoved(MouseEvent e) {
        mouseMoved(e.getX(), e.getY(), e.getModifiersEx());
    }

    //////// Keyboard input
    public final void keyPressed(KeyEvent e) {
        keyPressed(e.getKeyChar(), e.getKeyCode(), e.getModifiersEx());
    }

    public final void keyReleased(KeyEvent e) {
        keyReleased(e.getKeyChar(), e.getKeyCode(), e.getModifiersEx());
    }

    public final void keyTyped(KeyEvent e) {
        keyTyped(e.getKeyCode());
    }


    //////// Custom Events
    public void mouseClicked(int x, int y, int modifiersEx, int clickCount) {
    }

    public void mouseEntered(int x, int y, int modifiersEx) {
    }

    public void mouseExited(int x, int y, int modifiersEx) {
    }

    public void mousePressed(int x, int y, int modifiersEx) {
    }

    public void mouseReleased(int x, int y, int modifiersEx) {
    }

    public void mouseDragged(int x, int y, int modifiersEx) {
    }

    public void mouseMoved(int x, int y, int modifiersEx) {
    }


    public void keyPressed(char keyChar, int keyCode, int modifiersEx) {
    }

    public void keyReleased(char keyChar, int keyCode, int modifiersEx) {
    }

    public void keyTyped(int keyCode) {
    }
}
