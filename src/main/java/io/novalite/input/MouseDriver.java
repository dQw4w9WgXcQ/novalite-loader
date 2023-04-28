package io.novalite.input;

import io.novalite.commons.Mouse;
import lombok.RequiredArgsConstructor;
import net.runelite.api.Client;

@RequiredArgsConstructor
public class MouseDriver implements Mouse {
    private final Client client;
    private final FocusDriver focusDriver;
    private final CanvasInput canvasInput;

    @Override
    public void click(int x, int y, boolean left) {
        throw new RuntimeException("todo");//todo
//        int button;
//        if (left) {
//            button = MouseEvent.BUTTON1;
//        } else {
//            button = MouseEvent.BUTTON3;
//        }
//        canvasInput.mousePressed(x, y, !left, button, false, false);
//        canvasInput.mouseReleased(false);
//        canvasInput.mouseClicked(false);
    }
}
