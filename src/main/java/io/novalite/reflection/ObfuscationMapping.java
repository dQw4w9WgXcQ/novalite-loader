package io.novalite.reflection;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum ObfuscationMapping {
    ACTOR_PATH_LENGTH(new ReflDef("de", "dn", 1)),
    SCENE_SELECTED_X(new ReflDef("ie", "bd", 1)),
    SCENE_SELECTED_Y(new ReflDef("ie", "bt", 1)),
    VIEWPORT_WALKING(new ReflDef("ie", "bj", null)),
    WIDGET_INTERFACE_COMPONENTS(new ReflDef("hn", "ap", null));

    @Getter
    private final ReflDef def;
}
