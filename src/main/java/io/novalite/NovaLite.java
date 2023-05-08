package io.novalite;

import io.novalite.commons.*;
import io.novalite.input.CanvasInput;
import io.novalite.input.FocusDriver;
import io.novalite.input.KeyboardDriver;
import io.novalite.input.MouseDriver;
import io.novalite.reflection.Reflection;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.runelite.api.Client;
import net.runelite.client.RuneLite;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.eventbus.EventBus;
import okhttp3.HttpUrl;
import okhttp3.MediaType;

import javax.inject.Inject;

@NoArgsConstructor
public class NovaLite {
    public static final boolean DEV = false;
    public static final String URL = DEV ? "http://localhost:8080" : "https://novalite.up.railway.app";
    public static final HttpUrl API_BASE = HttpUrl.get(URL);
    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    @Inject
    private Client client;

    @Inject
    private ClientThread clientThread;

    @Inject
    private EventBus eventBus;

    @Getter
    private static ApiExtensionsDriver apiExtensions;

    @SuppressWarnings("UnstableApiUsage")
    public static void init() {
        var novaLite = RuneLite.getInjector().getInstance(NovaLite.class);

        var reflection = new Reflection(novaLite.client.getClass().getClassLoader());

        var canvasInput = new CanvasInput(novaLite.client);
        var focusDriver = new FocusDriver();
        var keyboard = new KeyboardDriver(focusDriver, canvasInput);
        var mouse = new MouseDriver(novaLite.client, focusDriver, canvasInput);

        var novaLiteConfig = new NovaLiteConfig();
        NovaLite.apiExtensions = new ApiExtensionsDriver(reflection);
        Interact interact = new InteractDriver(novaLite.clientThread, novaLite.client, reflection, novaLite.eventBus);
        Definitions definitions = new DefinitionCache(novaLite.client, novaLite.clientThread, novaLite.eventBus);

        ApiContext.init(
                new ApiContext(
                        novaLiteConfig,
                        apiExtensions,
                        interact,
                        definitions,
                        keyboard,
                        mouse,
                        novaLite.client,
                        novaLite.clientThread,
                        novaLite.eventBus
                )
        );
    }
}
