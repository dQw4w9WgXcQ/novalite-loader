package io.novalite;

import com.allatori.annotations.DoNotRename;
import io.novalite.commons.ApiContext;
import io.novalite.commons.ApiExtensions;
import io.novalite.commons.Definitions;
import io.novalite.commons.Interact;
import io.novalite.commons.NovaLiteConfig;
import io.novalite.input.CanvasInput;
import io.novalite.input.FocusDriver;
import io.novalite.input.KeyboardDriver;
import io.novalite.input.MouseDriver;
import io.novalite.reflection.Reflection;
import lombok.NoArgsConstructor;
import net.runelite.api.Client;
import net.runelite.client.RuneLite;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.eventbus.EventBus;

import javax.inject.Inject;

@NoArgsConstructor
public class NovaLite {
    @DoNotRename
    @Inject
    private Client client;

    @DoNotRename
    @Inject
    private ClientThread clientThread;

    @DoNotRename
    @Inject
    private EventBus eventBus;

    @DoNotRename
    @SuppressWarnings("UnstableApiUsage")
    public static void init() {
        NovaLite novaLite = RuneLite.getInjector().getInstance(NovaLite.class);

        Reflection reflection = new Reflection(novaLite.client.getClass().getClassLoader());

        CanvasInput canvasInput = new CanvasInput(novaLite.client);
        FocusDriver focusDriver = new FocusDriver();
        KeyboardDriver keyboard = new KeyboardDriver(focusDriver, canvasInput);
        MouseDriver mouse = new MouseDriver(novaLite.client, focusDriver, canvasInput);

        NovaLiteConfig novaLiteConfig = new NovaLiteConfig();
        ApiExtensions apiExtensions = new ApiExtensionsDriver(reflection);
        Interact interact = new InteractDriver(novaLite.clientThread, novaLite.client, reflection, mouse, novaLite.eventBus);
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
