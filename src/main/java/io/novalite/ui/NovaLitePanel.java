package io.novalite.ui;

import io.novalite.NovaLite;
import io.novalite.auth.Auth;
import io.novalite.commons.IBotScript;
import io.novalite.commons.ScriptMeta;
import io.novalite.script.LocalScriptManager;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.ui.PluginPanel;

import javax.swing.*;
import java.awt.*;
import java.util.Vector;

@Slf4j
public class NovaLitePanel extends PluginPanel {
    private final JList<ScriptListEntry> scriptList = new JList<>();

    private final JFrame logFrame;
    private final TrimmingJTextArea logTextArea;
    private final LocalScriptManager localScriptManager;
    private final Auth auth;

    public NovaLitePanel() {
        super(false);
        NovaLite.init();

        logTextArea = new TrimmingJTextArea();
        logTextArea.setEditable(false);
        System.setOut(new PrintStreamInterceptor(System.out, logTextArea, false));
        System.setErr(new PrintStreamInterceptor(System.err, logTextArea, true));

        this.localScriptManager = new LocalScriptManager(NovaLite.getApiExtensions());

        this.auth = new Auth();

        setLayout(new BorderLayout());

        refreshScriptList();

        //north
        Panel northPanel = new Panel(new GridLayout(1, 0));

        //startButton
        JButton startButton = new JButton("Start");
        startButton.addActionListener(e -> {
            ScriptListEntry entry = scriptList.getSelectedValue();
            if (entry != null) {
                localScriptManager.startScript(entry.getScriptClass());
            }
        });
        northPanel.add(startButton);

        //stopButton
        JButton stopButton = new JButton("Stop");
        stopButton.addActionListener(e -> {
            localScriptManager.stopScript();
            refreshScriptList();
        });
        northPanel.add(stopButton);

        //loginButton
        JButton loginButton = new JButton("Login");
        loginButton.addActionListener(e -> auth.login());
        northPanel.add(loginButton);

        //paint
//		JButton drawMouseButton = new JButton("Paint");
//		drawMouseButton.addActionListener(e -> BotOverlay.togglePaint());
//		northPanel.add(drawMouseButton);

        add(northPanel, BorderLayout.NORTH);

        //center
        Panel centerPanel = new Panel(new GridLayout(0, 1));

        //scriptScrollPane
        JScrollPane scriptScrollPane = new JScrollPane(scriptList);
        scriptScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        centerPanel.add(scriptScrollPane);

        add(centerPanel, BorderLayout.CENTER);

        //south
        Panel southPanel = new Panel(new GridLayout(0, 1));

        JLabel accountJLabel = new JLabel();
        auth.init(accountJLabel);
        southPanel.add(accountJLabel);

        //logButton
        JButton logButton = new JButton("Logger");
        logButton.addActionListener(e -> openLogger());
        southPanel.add(logButton);

        add(southPanel, BorderLayout.SOUTH);

        JScrollPane scrollPane = new JScrollPane(logTextArea);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        logFrame = new JFrame();
        logFrame.getContentPane().add(scrollPane);
        logFrame.setSize(900, 600);
    }

    private void openLogger() {
        logFrame.setVisible(!logFrame.isVisible());
        logTextArea.scrollToBottom();//doesnt work
    }

    private void refreshScriptList() {
        Vector<ScriptListEntry> scriptListEntries = new Vector<>();
        for (Class<? extends IBotScript> scriptClass : localScriptManager.loadScripts()) {
            scriptListEntries.add(new ScriptListEntry(scriptClass));
        }

        scriptList.setListData(scriptListEntries);
    }

    private static final class ScriptListEntry {
        @Getter
        private final Class<? extends IBotScript> scriptClass;
        private final ScriptMeta meta;

        private ScriptListEntry(Class<? extends IBotScript> scriptClass, ScriptMeta meta) {
            this.scriptClass = scriptClass;
            this.meta = meta;
        }

        private ScriptListEntry(Class<? extends IBotScript> scriptClass) {
            this(scriptClass, scriptClass.getAnnotationsByType(ScriptMeta.class)[0]);
        }

        @Override
        public String toString() {
            return meta.value();
        }
    }
}
