package io.novalite.input;

import lombok.RequiredArgsConstructor;
import net.runelite.api.Client;

import java.awt.event.*;

@RequiredArgsConstructor
public class CanvasInput {
    private final Client client;

    public void mouseClicked(boolean isPopupTrigger) {
        var canvas = client.getCanvas();
        var mouseCanvasPosition = client.getMouseCanvasPosition();
        var event = new MouseEvent(
                canvas,
                MouseEvent.MOUSE_CLICKED,
                System.currentTimeMillis(),
                0,
                mouseCanvasPosition.getX(),
                mouseCanvasPosition.getY(),
                0,
                isPopupTrigger
        );
        for (var listener : canvas.getMouseListeners()) {
            listener.mouseClicked(event);
        }
    }

    public void mousePressed(int x, int y, boolean isPopupTrigger, int button, boolean isAltDown, boolean isMetaDown) {
        var canvas = client.getCanvas();
        int buttonModifier;
        switch (button) {
            case MouseEvent.BUTTON1:
                buttonModifier = InputEvent.BUTTON1_DOWN_MASK;
                break;
            case MouseEvent.BUTTON2:
                buttonModifier = InputEvent.BUTTON2_DOWN_MASK;
                break;
            case MouseEvent.BUTTON3:
                buttonModifier = InputEvent.BUTTON3_DOWN_MASK;
                break;
            default:
                throw new IllegalArgumentException("no mask for button: " + button);
        }
        var modifiers = buttonModifier | (isAltDown ? InputEvent.ALT_DOWN_MASK : 0) | (isMetaDown ? InputEvent.META_DOWN_MASK : 0);
        var event = new MouseEvent(
                canvas,
                MouseEvent.MOUSE_PRESSED,
                System.currentTimeMillis(),
                modifiers,
                x,
                y,
                0,
                isPopupTrigger,
                button
        );
        for (var listener : canvas.getMouseListeners()) {
            listener.mousePressed(event);
        }
    }

    public void mouseReleased(boolean isPopupTrigger) {
        var canvas = client.getCanvas();
        var mouseCanvasPosition = client.getMouseCanvasPosition();
        var event = new MouseEvent(
                canvas,
                MouseEvent.MOUSE_RELEASED,
                System.currentTimeMillis(),
                0,
                mouseCanvasPosition.getX(),
                mouseCanvasPosition.getY(),
                0,
                isPopupTrigger
        );
        for (var listener : canvas.getMouseListeners()) {
            listener.mouseReleased(event);
        }
    }

    public void mouseEntered(int x, int y, long when) {
        var canvas = client.getCanvas();
        var event = new MouseEvent(canvas, MouseEvent.MOUSE_ENTERED, when, 0, x, y, 0, false);
        for (var listener : canvas.getMouseListeners()) {
            listener.mouseEntered(event);
        }
    }

    public void mouseExited(long when) {
        var canvas = client.getCanvas();
        var mouseCanvasPosition = client.getMouseCanvasPosition();
        var event = new MouseEvent(
                canvas,
                MouseEvent.MOUSE_EXITED,
                when,
                0,
                mouseCanvasPosition.getX(),
                mouseCanvasPosition.getY(),
                0,
                false
        );
        for (var listener : canvas.getMouseListeners()) {
            listener.mouseExited(event);
        }
    }

    public void mouseDragged(int x, int y, long when) {
        var canvas = client.getCanvas();
        var event = new MouseEvent(canvas, MouseEvent.MOUSE_DRAGGED, when, 0, x, y, 0, false);
        for (var listener : canvas.getMouseMotionListeners()) {
            listener.mouseDragged(event);
        }
    }

    public void mouseMoved(int x, int y, long when) {
        var canvas = client.getCanvas();
        var event = new MouseEvent(canvas, MouseEvent.MOUSE_MOVED, when, 0, x, y, 0, false);
        for (var listener : canvas.getMouseMotionListeners()) {
            listener.mouseMoved(event);
        }

    }

    public void mouseWheel(int rotations) {
        if (rotations != 1 && rotations != -1) {
            throw new IllegalArgumentException("rotations " + rotations + " must be 1 or -1");
        }
        var canvas = client.getCanvas();
        var mouseCanvasPosition = client.getMouseCanvasPosition();
        var event = new MouseWheelEvent(
                canvas,
                MouseEvent.MOUSE_WHEEL,
                System.currentTimeMillis(),
                0,
                mouseCanvasPosition.getX(),
                mouseCanvasPosition.getY(),
                0,
                false,
                MouseWheelEvent.WHEEL_UNIT_SCROLL,
                rotations,
                rotations
        );
        for (var listener : canvas.getMouseWheelListeners()) {
            listener.mouseWheelMoved(event);
        }
    }

    public void focusGained() {
        var canvas = client.getCanvas();
        var event = new FocusEvent(canvas, FocusEvent.FOCUS_GAINED, false, null);
        for (var listener : canvas.getFocusListeners()) {
            listener.focusGained(event);
        }
    }

    public void focusLost() {
        var canvas = client.getCanvas();
        var event = new FocusEvent(canvas, FocusEvent.FOCUS_LOST, true, null);
        for (var listener : canvas.getFocusListeners()) {
            listener.focusLost(event);
        }
    }

    public void keyPressed(int keyCode, int modifiers) {
        var canvas = client.getCanvas();
        var event = new KeyEvent(
                canvas,
                KeyEvent.KEY_PRESSED,
                System.currentTimeMillis(),
                modifiers,
                keyCode,
                (char) KeyEvent.getExtendedKeyCodeForChar(keyCode),
                KeyEvent.KEY_LOCATION_STANDARD
        );
        for (var listener : canvas.getKeyListeners()) {
            listener.keyPressed(event);
        }
    }

    public void keyTyped(char keyChar) {
        if (keyChar == KeyEvent.VK_UNDEFINED) {
            throw new IllegalArgumentException("keyChar must not be VK_UNDEFINED");
        }
        var canvas = client.getCanvas();
        var event = new KeyEvent(
                canvas,
                KeyEvent.KEY_TYPED,
                System.currentTimeMillis(),
                0,
                KeyEvent.VK_UNDEFINED,
                keyChar,
                KeyEvent.KEY_LOCATION_UNKNOWN
        );
        for (var listener : canvas.getKeyListeners()) {
            listener.keyTyped(event);
        }
    }

    public void keyReleased(int keyCode) {
        var canvas = client.getCanvas();
        var event = new KeyEvent(
                canvas,
                KeyEvent.KEY_RELEASED,
                System.currentTimeMillis(),
                0,
                keyCode,
                (char) keyCode,
                KeyEvent.KEY_LOCATION_STANDARD
        );
        for (var listener : canvas.getKeyListeners()) {
            listener.keyReleased(event);
        }
    }
}