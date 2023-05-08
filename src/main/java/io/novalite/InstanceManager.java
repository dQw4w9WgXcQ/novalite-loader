package io.novalite;

import com.google.gson.Gson;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;

@Slf4j
public class InstanceManager implements Runnable {
    private static final InstanceManager instanceManager = new InstanceManager();
    private static final Thread thread = new Thread(new InstanceManager());

    private static volatile String instance = null;
    private static volatile long lastRefresh;

    public static void launch(String session, String type) throws IOException {
        var url = NovaLite.API_BASE.newBuilder()
                .addPathSegment("instance")
                .addPathSegment("launch")
                .build();

        var body = Map.of(
                "session", Objects.requireNonNull(session),
                "type", Objects.requireNonNull(type)
        );

        var request = new Request.Builder()
                .url(url)
                .post(RequestBody.create(NovaLite.JSON, new Gson().toJson(body)))
                .build();

        try (var response = new OkHttpClient().newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Failed to start instance: " + response.code());
            }

            var map = new Gson().fromJson(response.body().string(), Map.class);

            InstanceManager.instance = (String) map.get("instance");
            InstanceManager.lastRefresh = System.currentTimeMillis();
            thread.notify();
        }
    }

    public static void end() {
        var url = NovaLite.API_BASE.newBuilder()
                .addPathSegment("instance")
                .addPathSegment("end")
                .build();

        var instance = InstanceManager.instance;
        if (instance == null) {
            return;
        }

        log.info("Ending instance");
        var body = Map.of(
                "instance", instance
        );

        var request = new Request.Builder()
                .url(url)
                .post(RequestBody.create(NovaLite.JSON, new Gson().toJson(body)))
                .build();

        new OkHttpClient().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                log.warn("Failed to end instance", e);
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    log.debug("Ended instance");
                } else {
                    log.warn("Failed to end instance: {}", response.code());
                }
            }
        });

        InstanceManager.instance = null;
    }

    public static boolean isExpired() {
        return instance == null || System.currentTimeMillis() - lastRefresh > 11000;
    }

    @SuppressWarnings({"BusyWait", "InfiniteLoopStatement"})
    @SneakyThrows(InterruptedException.class)
    @Override
    public void run() {
        var fails = 0;
        while (true) {
            if (isExpired()) {
                wait();
            }

            var url = NovaLite.API_BASE.newBuilder()
                    .addPathSegment("instance")
                    .addPathSegment("refresh")
                    .build();

            Map<String, ?> stringMap = Map.of(
                    "instance", Objects.requireNonNull(instance)
            );

            var body = RequestBody.create(NovaLite.JSON, new Gson().toJson(stringMap));

            var request = new Request.Builder()
                    .url(url)
                    .post(body)
                    .build();

            try (var response = new OkHttpClient().newCall(request).execute()) {
                if (response.isSuccessful()) {
                    lastRefresh = System.currentTimeMillis();
                } else if (response.code() == 404) {
                    instance = null;
                } else {
                    log.error("{} when refreshing instance", response.code());
                    if (fails++ > 5) {
                        instance = null;
                        continue;
                    }
                }
            } catch (IOException e) {
                log.error("Failed to refresh instance", e);
                if (fails++ > 5) {
                    instance = null;
                    continue;
                }

                Thread.sleep(1000);
                continue;
            }

            Thread.sleep(5000);
        }
    }
}
