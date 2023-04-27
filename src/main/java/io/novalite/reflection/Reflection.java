package io.novalite.reflection;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;

@RequiredArgsConstructor
public class Reflection {
    private final ClassLoader rlClassLoader;

    @SneakyThrows
    @SuppressWarnings("unchecked")
    public <T> T getField(ReflDef def, @Nullable Object instance) {
        Field field = Class.forName(def.getClassName(), false, rlClassLoader).getDeclaredField(def.getFieldName());
        @SuppressWarnings("deprecation") boolean access = field.isAccessible();
        if (!access) {
            field.setAccessible(true);
        }

        T value = (T) field.get(instance);

        if (!access) {
            field.setAccessible(false);
        }

        return value;
    }

    @SneakyThrows
    public void setField(ReflDef def, @Nullable Object instance, Object value) {
        Field field = Class.forName(def.getClassName(), false, rlClassLoader).getDeclaredField(def.getFieldName());
        @SuppressWarnings("deprecation") boolean access = field.isAccessible();
        if (!access) {
            field.setAccessible(true);
        }

        field.set(instance, value);

        if (!access) {
            field.setAccessible(false);
        }
    }
}
