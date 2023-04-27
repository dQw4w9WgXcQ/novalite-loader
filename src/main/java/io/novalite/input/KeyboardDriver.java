package io.novalite.input;

import io.novalite.commons.Keyboard;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.awt.event.KeyEvent;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Slf4j
public class KeyboardDriver implements Keyboard {
    private int threadCount = 0;
    private final ExecutorService exe = Executors.newCachedThreadPool(task -> {
        log.debug("new keyboard thread " + threadCount);
        return new Thread(task, "keyboard-" + threadCount++);
    });
    private final FocusDriver focusDriver;
    private final CanvasInput canvasInput;

    public KeyboardDriver(FocusDriver focusDriver, CanvasInput canvasInput) {
        this.focusDriver = focusDriver;
        this.canvasInput = canvasInput;
    }

    @Override
    public void type(int keyCode) {
        typeKey(keyCode, 0);
    }

    @Override
    public void type(String s) {
        type(s, false);
    }

    public Future<?> type(CharSequence text, boolean sendEnter) {
        return exe.submit(() -> {
            for (char c : text.toString().toCharArray()) {
                type(c);
                sleep(50, 150);
            }

            if (sendEnter) {
                enter();
            }
        });
    }

    public Future<?> typeKey(int charCode, int modifiers) {
        int exKeyCode = KeyEvent.getExtendedKeyCodeForChar(charCode);
        if (exKeyCode == KeyEvent.VK_UNDEFINED) {
            throw new IllegalArgumentException("Cannot type character " + charCode);
        }

        return exe.submit(() -> {
            focusDriver.require();
            log.debug("charCode: " + charCode + " modifiers: " + modifiers + " charCode.toChar: " + (char) charCode + " exKeyCode: " + exKeyCode + " exKeyCode.toChar: " + (char) exKeyCode + " exKeyCode lowercase: " + Character.toLowerCase((char) exKeyCode));
            canvasInput.keyPressed(exKeyCode, modifiers);
            sleep(1, 3);
            canvasInput.keyTyped((char) charCode);
            sleep(50, 150);
            canvasInput.keyReleased(exKeyCode);
        });
    }

    public Future<?> press(int code, int modifiers) {
        return exe.submit(() -> {
            focusDriver.require();
            canvasInput.keyPressed(code, modifiers);
            sleep(50, 150);
            canvasInput.keyReleased(code);
        });
    }

    public Future<?> type(char c) {
        log.debug("char: " + c + " code " + (int) c);
        return typeKey(c, 0);
    }

    public Future<?> enter() {
        return typeKey(KeyEvent.VK_ENTER, 0);
    }

    public Future<?> space() {
        return typeKey(KeyEvent.VK_SPACE, 0);
    }

    public Future<?> esc() {
        return typeKey(KeyEvent.VK_ESCAPE, 0);
    }

    public Future<?> backspace(int reps) {
        if (reps < 1) {
            throw new IllegalArgumentException();
        }

        throw new RuntimeException("todo");//todo
//		return type(String.valueOf((char) KeyEvent.VK_BACK_SPACE).repeat(reps), false);
    }

    @SneakyThrows
    public static void sleep(int min, int max) {
        Thread.sleep(min + (int) (Math.random() * (max - min)));
    }
}