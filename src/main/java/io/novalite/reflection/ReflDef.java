package io.novalite.reflection;

import lombok.Value;
import org.jetbrains.annotations.Nullable;

@Value
public class ReflDef {
    String className;
    String fieldName;
    @Nullable Object mult;
}
