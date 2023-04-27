package io.novalite.input;

import io.novalite.commons.Mouse;
import lombok.RequiredArgsConstructor;
import net.runelite.api.Client;
import net.runelite.api.Point;

import java.awt.event.MouseEvent;

@RequiredArgsConstructor
public class MouseDriver implements Mouse {
    private final Client client;
    private final FocusDriver focusDriver;
    private final CanvasInput canvasInput;

    @Override
    public void click(int x, int y, boolean left) {
        throw new UnsupportedOperationException("todo");//todo
    }

    public void click() {
        boolean left = true;
        int button;
        if (left) {
            button = MouseEvent.BUTTON1;
        } else {
            button = MouseEvent.BUTTON3;
        }
        Point mouseCanvasPosition = client.getMouseCanvasPosition();
        canvasInput.mousePressed(mouseCanvasPosition.getX(), mouseCanvasPosition.getY(), !left, button, false, false);
        canvasInput.mouseReleased(false);
        canvasInput.mouseClicked(false);
    }
}
