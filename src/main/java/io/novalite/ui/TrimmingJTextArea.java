package io.novalite.ui;

import javax.swing.*;
import javax.swing.text.Caret;

public class TrimmingJTextArea extends JTextArea {
    @Override
    public void append(String str) {
        String text = getText();
        if (text.length() > 20000) {
            setText(text.substring(text.length() - 10000));
        }
        super.append(str);
        scrollToBottom();
    }

    public void scrollToBottom() {
        Caret caret = getCaret();
        if (caret != null) {
            caret.setDot(Integer.MAX_VALUE);
        }
    }
}
