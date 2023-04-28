package io.novalite.ui;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Locale;

public class PrintStreamInterceptor extends PrintStream {
    private final JTextArea logTextArea;
    private final boolean err;

    public PrintStreamInterceptor(OutputStream originalOut, JTextArea logTextArea, boolean err) {
        super(originalOut, true);
        this.logTextArea = logTextArea;
        this.err = err;
    }

    @Override
    public void write(byte @NotNull [] b) throws IOException {
        logTextArea.append(new String(b));
        super.write(b);
    }

    @Override
    public void write(int b) {
        logTextArea.append(String.valueOf(b));
        super.write(b);
    }

    @Override
    public void print(boolean b) {
        logTextArea.append(String.valueOf(b));
        super.print(b);
    }

    @Override
    public void println(boolean b) {
        logTextArea.append(b + "\n");
        super.println(b);
    }

    @Override
    public void println(char c) {
        logTextArea.append(c + "\n");
        super.println(c);
    }

    @Override
    public void println(int i) {
        logTextArea.append(i + "\n");
        super.println(i);
    }

    @Override
    public void println(long l) {
        logTextArea.append(l + "\n");
        super.println(l);
    }

    @Override
    public void println(float f) {
        logTextArea.append(f + "\n");
        super.println(f);
    }

    @Override
    public void println(double d) {
        logTextArea.append(d + "\n");
        super.println(d);
    }

    @Override
    public void println(String s) {
        logTextArea.append(s + "\n");
        super.println(s);
    }

    @Override
    public void println(Object obj) {
        logTextArea.append(obj + "\n");
        super.println(obj);
    }

    @Override
    public void println() {
        logTextArea.append("\n");
        super.println();
    }

    @Override
    public void print(char c) {
        logTextArea.append(String.valueOf(c));
        super.print(c);
    }

    @Override
    public void print(int i) {
        logTextArea.append(String.valueOf(i));
        super.print(i);
    }

    @Override
    public void print(long l) {
        logTextArea.append(String.valueOf(l));
        super.print(l);
    }

    @Override
    public void print(float f) {
        logTextArea.append(String.valueOf(f));
        super.print(f);
    }

    @Override
    public void print(double d) {
        logTextArea.append(String.valueOf(d));
        super.print(d);
    }

    @Override
    public void print(char @NotNull [] s) {
        logTextArea.append(String.valueOf(s));
        super.print(s);
    }

    @Override
    public void print(@Nullable String s) {
        logTextArea.append(String.valueOf(s));
        super.print(s);
    }

    @Override
    public void print(@Nullable Object obj) {
        logTextArea.append(String.valueOf(obj));
        super.print(obj);
    }

    @Override
    public void println(char @NotNull [] x) {
        logTextArea.append(String.valueOf(x));
        super.println(x);
    }

    @Override
    public PrintStream printf(@NotNull String format, Object... args) {
        logTextArea.append(String.format(format, args));
        return super.printf(format, args);
    }

    @Override
    public PrintStream printf(Locale l, @NotNull String format, Object... args) {
        logTextArea.append(String.format(l, format, args));
        return super.printf(l, format, args);
    }
}
