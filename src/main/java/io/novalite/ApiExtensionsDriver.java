package io.novalite;

import io.novalite.commons.ApiExtensions;
import io.novalite.reflection.ObfuscationMapping;
import io.novalite.reflection.ReflDef;
import io.novalite.reflection.Reflection;
import lombok.RequiredArgsConstructor;
import net.runelite.api.Actor;
import org.jetbrains.annotations.Nullable;

@RequiredArgsConstructor
public class ApiExtensionsDriver implements ApiExtensions {
    private final Reflection reflection;
    private final ReflDef actorPathLength = ObfuscationMapping.ACTOR_PATH_LENGTH.getDef();
    private final ReflDef widgetInterfaceComponents = ObfuscationMapping.WIDGET_INTERFACE_COMPONENTS.getDef();

    @Override
    public int actorPathLength(Actor actor) {
        return reflection.getField(actorPathLength, actor);
    }

    @Override
    public @Nullable Object @Nullable [] widgetGroup(int id) {
        return reflection.getField(widgetInterfaceComponents, null);
    }
}
