package io.novalite;

import io.novalite.commons.ApiExtensions;
import io.novalite.reflection.ObfuscationMapping;
import io.novalite.reflection.ReflDef;
import io.novalite.reflection.Reflection;
import lombok.RequiredArgsConstructor;
import net.runelite.api.Actor;
import net.runelite.client.plugins.Plugin;
import org.jetbrains.annotations.Nullable;

@RequiredArgsConstructor
public class ApiExtensionsDriver implements ApiExtensions {
    private final Reflection reflection;
    private final ReflDef actorPathLength = ObfuscationMapping.ACTOR_PATH_LENGTH.getDef();
    private final ReflDef widgetInterfaceComponents = ObfuscationMapping.WIDGET_INTERFACE_COMPONENTS.getDef();

    public void setDisableInput(boolean disableInput) {
        reflection.setField(new ReflDef("rl10", "disableInput", null), null, disableInput);
    }

    @Override
    public int actorPathLength(Actor actor) {
        return reflection.getField(actorPathLength, actor);
    }

    @Override
    public @Nullable Object @Nullable [] @Nullable [] widgets() {
        return reflection.getField(widgetInterfaceComponents, null);
    }

    @Override
    public boolean isWorldSelectorOpen() {
        throw new RuntimeException("TODO: not implemented");//todo
    }

    @Override
    public <T extends Plugin> T getPlugin(Class<T> aClass) {
        throw new RuntimeException("TODO: not implemented");//todo
    }

    @Override
    public boolean isPluginEnabled(Class<? extends Plugin> aClass) {
        throw new RuntimeException("TODO: not implemented");//todo
    }

    @Override
    public String loginResponse0() {
        throw new RuntimeException("TODO: not implemented");//todo
    }

    @Override
    public String loginResponse1() {
        throw new RuntimeException("TODO: not implemented");//todo
    }

    @Override
    public String loginResponse2() {
        throw new RuntimeException("TODO: not implemented");//todo
    }

    @Override
    public String loginResponse3() {
        throw new RuntimeException("TODO: not implemented");//todo
    }

    @Override
    public int banType() {
        throw new RuntimeException("TODO: not implemented");//todo
    }
}
