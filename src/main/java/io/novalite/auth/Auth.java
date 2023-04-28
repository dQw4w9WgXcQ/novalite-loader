package io.novalite.auth;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpServer;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.RuneLite;
import net.runelite.client.util.LinkBrowser;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;

@Slf4j
public class Auth {
    public static final int PORT = 34085;
    public static final int BACKUP_PORT = 57252;

    public static final boolean DEV = false;
    public static final String URL = DEV ? "http://localhost:8080" : "https://novalite.up.railway.app";
    public static final HttpUrl API_BASE = HttpUrl.get(URL);
    public static final File SESSION_FILE = new File(RuneLite.RUNELITE_DIR, "session2");

    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    private JLabel jLabel = null;
    private volatile String session = null;
    private HttpServer server = null;

    @SneakyThrows
    public void login() {
        if (server == null) {
            log.info("new server");
            server = HttpServer.create(new InetSocketAddress("localhost", PORT), 1);
            server.createContext("/", req ->
            {
                String code = null;
                try {
                    HttpUrl url = HttpUrl.get("http://localhost" + req.getRequestURI());
                    code = url.queryParameter("code");

                    req.getResponseHeaders().set("Content-Type", "text/html; charset=utf-8");
                    req.sendResponseHeaders(200, 0);
                    req.getResponseBody().write("You can close this page.  If you do not get logged in, check the logger for errors.".getBytes(StandardCharsets.UTF_8));
                } catch (Exception e) {
                    log.warn("failure serving oauth response", e);
                    req.sendResponseHeaders(400, 0);
                    req.getResponseBody().write(e.getMessage().getBytes(StandardCharsets.UTF_8));
                } finally {
                    req.close();
                    server.stop(0);
                    server = null;
                }

                HttpUrl url = API_BASE.newBuilder()
                        .addPathSegment("auth")
                        .addPathSegment("discord")
                        .build();

                var stringMap = Map.of("code", Objects.requireNonNull(code), "port", PORT);
                RequestBody body = RequestBody.create(JSON, new Gson().toJson(stringMap));

                Request request = new Request.Builder()
                        .url(url)
                        .post(body)
                        .build();

                try (Response response = new OkHttpClient().newCall(request).execute()) {
                    if (!response.isSuccessful()) {
                        log.warn("failed to login: " + response.code() + " " + response.body().string());
                        SwingUtilities.invokeLater(() -> jLabel.setText("Failed to login: " + response.code()));
                        return;
                    }

                    SESSION_FILE.delete();
                    Map map = new Gson().fromJson(response.body().string(), Map.class);
                    session = (String) map.get("session");
                    sessionCheck();
                }
            });

            server.start();
        }

        LinkBrowser.browse("https://discord.com/api/oauth2/authorize?client_id=1100474876298543114&redirect_uri=http%3A%2F%2Flocalhost%3A" + PORT + "&response_type=code&scope=identify");
    }

    public void sessionCheck() throws IOException {
        if (session == null) throw new IllegalStateException("session null");

        HttpUrl url = API_BASE.newBuilder()
                .addPathSegment("auth")
                .addPathSegment("check")
                .build();

        Request request = new Request.Builder()
                .header("Authorization", "Bearer " + session)
                .url(url)
                .build();

        try (Response response = new OkHttpClient().newCall(request).execute()) {
            if (response.code() == 403) {
                jLabel.setText("Session expired.  Please sign in again");
                SESSION_FILE.delete();
                this.session = null;
            } else if (response.isSuccessful()) {
                Map map = new Gson().fromJson(response.body().string(), Map.class);
                String username = (String) map.get("username");
                log.info("Session valid for: {}", username);
                jLabel.setText("Logged in as " + username);
                if (!SESSION_FILE.exists()) {
                    SESSION_FILE.createNewFile();
                    //write the session to file
                    String hostname = InetAddress.getLocalHost().getHostName();
                    byte[] encrypted = Crypto.encrypt(hostname + session);
                    try (FileOutputStream out = new FileOutputStream(SESSION_FILE)) {
                        out.write(encrypted);
                    }
                }
            } else {
                throw new IOException("Unexpected response code: " + response.code());
            }
        }
    }

    public void init(JLabel jlabel) {
        jlabel.setText("Connecting to server...");
        this.jLabel = jlabel;

        session = loadSession();
        if (session == null) {
            jlabel.setText("Not logged in");
            return;
        }

        try {
            sessionCheck();//todo retries
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @SneakyThrows
    private @Nullable String loadSession() {
        if (!SESSION_FILE.exists()) {
            log.debug("No session file exists");
            return null;
        }

        byte[] bytes;
        try (FileInputStream in = new FileInputStream(SESSION_FILE)) {
            bytes = in.readAllBytes();
        } catch (Exception e) {
            log.warn("Unable to load session file", e);
            throw e;
        }

        String decrypted;
        try {
            decrypted = Crypto.decrypt(bytes);
        } catch (Exception e) {
            log.warn("Unable to read session file", e);
            throw e;
        }

        String hostname = InetAddress.getLocalHost().getHostName();
        if (!decrypted.startsWith(hostname)) {
            log.warn("Unable to read session file.");
            SESSION_FILE.delete();
            return null;
        }

        String session = decrypted.substring(hostname.length());
        log.debug("Loaded session file");
        return session;
    }
}
