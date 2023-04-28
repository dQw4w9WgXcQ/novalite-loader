package io.novalite;

import io.novalite.commons.Interact;
import io.novalite.input.MouseDriver;
import io.novalite.reflection.ObfuscationMapping;
import io.novalite.reflection.ReflDef;
import io.novalite.reflection.Reflection;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.MenuEntry;
import net.runelite.api.Point;
import net.runelite.api.events.PostClientTick;
import net.runelite.api.widgets.Widget;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.function.Predicate;

@Slf4j
public class InteractDriver implements Interact {
    private final ClientThread clientThread;
    private final Client client;
    private final Reflection reflection;
    private final MouseDriver mouse;

    public InteractDriver(ClientThread clientThread, Client client, Reflection reflection, MouseDriver mouse, EventBus eventBus) {
        this.clientThread = clientThread;
        this.client = client;
        this.reflection = reflection;
        this.mouse = mouse;
        eventBus.register(this);
    }

    @Override
    public void interactPlayer(int index, Predicate<String> actionMatches) {
        interactEntity(calculateTag(0, 0, 0, false, index), index, actionMatches);
    }

    @Override
    public void interactNpc(int index, Predicate<String> actionMatches) {
        interactEntity(calculateTag(0, 0, 1, false, index), index, actionMatches);
    }

    @Override
    public void interactTileObject(Point sceneLocation, int id, Predicate<String> actionMatches) {
        interactEntity(calculateTag(sceneLocation.getX(), sceneLocation.getY(), 2, false, id), id, actionMatches);
    }

    @Override
    public void interactTileItem(Point sceneLocation, int id, Predicate<String> actionMatches) {
        interactEntity(calculateTag(sceneLocation.getX(), sceneLocation.getY(), 3, false, id), id, actionMatches);
    }

    @Override
    public void interactWidget(Widget widget, Predicate<String> actionMatches) {
        ClientThreadUtil.invoke(clientThread, () -> setWidget(widget, actionMatches));
    }

    @Override
    public void walk(int x, int y) {
        if (client.isClientThread()) {
            setWalkFields(x, y);
        } else {
            //if not on client thread, need to check if base changes
            int baseX = client.getBaseX();
            int baseY = client.getBaseY();

            ClientThreadUtil.invoke(clientThread, () -> {
                if (baseX != client.getBaseX() || baseY != client.getBaseY()) {
                    throw new RuntimeException("Base position changed");//todo better exception
                }

                setWalkFields(x, y);
            });
        }
    }

    @Subscribe
    public void onPostClientTick(PostClientTick e) {
        MenuEntry menuEntry = null;
        if (identifier != -1) {
            menuEntry = Arrays.stream(client.getMenuEntries())
                    .filter(it -> it.getIdentifier() == identifier && actionMatches.test(it.getOption()))
                    .findFirst()
                    .orElse(null);

            if (menuEntry != null) {
                resetTargetTag();
                actionMatches = null;
                identifier = -1;
            }
        } else if (widget != null) {
            log.debug(Arrays.toString(client.getMenuEntries()));
            menuEntry = Arrays.stream(client.getMenuEntries())
                    .filter(it -> actionMatches.test(it.getOption()))
                    .findFirst()
                    .orElse(null);

            if (menuEntry != null) {
                resetWidget();
                actionMatches = null;
                widget = null;
            }
        }

        if (menuEntry != null) {
            doAction(menuEntry);
        }
    }

    private int identifier = -1;
    private Predicate<String> actionMatches = null;
    private Widget widget = null;

    @SneakyThrows
    private void interactEntity(long tag, int identifier, Predicate<String> actionMatches) {
        ClientThreadUtil.invoke(clientThread, () -> setTargetTag(tag, identifier, actionMatches));
    }

    private void setTargetTag(long tag, int identifier, Predicate<String> actionMatches) {
        reflection.setField(new ReflDef("rl10", "tag", 1), null, tag);
        this.actionMatches = actionMatches;
        this.identifier = identifier;
    }

    private void resetTargetTag() {
        setTargetTag(-1, -1, null);
    }

    private void setWidget(Widget widget, Predicate<String> actionMatches) {
        this.widget = widget;
        this.actionMatches = actionMatches;
        reflection.setField(new ReflDef("rl10", "widget", null), null, widget);
    }

    private void resetWidget() {
        setWidget(null, null);
    }

    private void setWalkFields(int x, int y) {
        reflection.setField(ObfuscationMapping.SCENE_SELECTED_X.getDef(), null, x);
        reflection.setField(ObfuscationMapping.SCENE_SELECTED_Y.getDef(), null, y);
        reflection.setField(ObfuscationMapping.VIEWPORT_WALKING.getDef(), null, true);
    }

    private void doAction(MenuEntry menuEntry) {
        log.debug("doaction: + " + menuEntry);
        Point mouseCanvasPosition = client.getMouseCanvasPosition();
        doAction(
                menuEntry.getParam0(),
                menuEntry.getParam1(),
                menuEntry.getType().getId(),
                menuEntry.getIdentifier(),
                menuEntry.getItemId(),
                menuEntry.getOption(),
                menuEntry.getTarget(),
                mouseCanvasPosition.getX(),
                mouseCanvasPosition.getY()
        );
    }

    @SneakyThrows
    private void doAction(int param0, int param1, int opcode, int identifier, int itemId, String action, String target, int x, int y) {
        log.debug("doaction: " +
                "param0: " + param0 +
                " param1: " + param1 +
                " opcode: " + opcode +
                " identifier: " + identifier +
                " itemId: " + itemId +
                " action: " + action +
                " target: " + target +
                " x: " + x +
                " y: " + y);
        ReflDef def = ObfuscationMapping.MENU_ACTION.getDef();
        Method method = Class.forName(def.getClassName(), false, reflection.getRlClassLoader()).getDeclaredMethod(
                def.getFieldName(),
                int.class,
                int.class,
                int.class,
                int.class,
                int.class,
                String.class,
                String.class,
                int.class,
                int.class,
                int.class//garbage
        );
        @SuppressWarnings("deprecation") boolean access = method.isAccessible();
        if (!access) {
            method.setAccessible(true);
        }

        try {
            method.invoke(null, param0, param1, opcode, identifier, itemId, action, target, x, y, 1875049190);
        } finally {
            if (!access) {
                method.setAccessible(false);
            }
        }
    }

    private long calculateTag(int arg1, int arg2, int type, boolean uninteractable, int identifier) {
        long tag = (long) ((arg1 & 127) | (arg2 & 127) << 7 | (type & 3) << 14) | ((long) identifier & 4294967295L) << 17;
        if (uninteractable) {
            tag |= 65536L;
        }

        return tag;
    }
}
