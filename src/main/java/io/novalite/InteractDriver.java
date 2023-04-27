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
import net.runelite.api.events.MenuEntryAdded;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.widgets.Widget;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;

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
        mouse.click();
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
    public void onMenuEntryAdded(MenuEntryAdded e) {
        if (identifier != -1) {
            MenuEntry menuEntry = Arrays.stream(client.getMenuEntries())
                    .filter(it -> it.getIdentifier() == identifier && actionMatches.test(it.getOption()))
                    .findFirst()
                    .orElse(null);

            if (menuEntry == null) {
                return;
            }

            client.setMenuEntries(new MenuEntry[]{menuEntry});
            return;
        }

        if (widget != null) {
            System.out.println(Arrays.toString(client.getMenuEntries()));
            MenuEntry menuEntry = Arrays.stream(client.getMenuEntries())
                    .filter(it -> actionMatches.test(it.getOption()))
                    .findFirst()
                    .orElse(null);

            if (menuEntry == null) {
                return;
            }

            client.setMenuEntries(new MenuEntry[]{menuEntry});
            return;
        }
    }

    @Subscribe
    public void onMenuOptionClicked(MenuOptionClicked e) {
        if (identifier != -1) {
            identifier = -1;
            resetTargetTag();
        }

        if (widget != null) {
            resetWidget();
        }

        actionMatches = null;
    }

    private int identifier = -1;
    private Predicate<String> actionMatches = null;
    private Widget widget = null;

    @SneakyThrows
    private void interactEntity(long tag, int identifier, Predicate<String> actionMatches) {
        ClientThreadUtil.invoke(clientThread, () -> setTargetTag(tag, identifier, actionMatches));
        mouse.click();
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
        if (widget != null) {
            System.out.println("setting widget " + widget.getId());
        }

        this.widget = widget;
        this.actionMatches = actionMatches;
        reflection.setField(new ReflDef("rl10", "widget", null), null, widget);
    }

    private void resetWidget() {
        setWidget(null, null);
    }

    private final ReflDef sceneSelectedX = ObfuscationMapping.SCENE_SELECTED_X.getDef();
    private final ReflDef sceneSelectedY = ObfuscationMapping.SCENE_SELECTED_Y.getDef();
    private final ReflDef viewportWalking = ObfuscationMapping.VIEWPORT_WALKING.getDef();

    private void setWalkFields(int x, int y) {
        reflection.setField(sceneSelectedX, null, x);
        reflection.setField(sceneSelectedY, null, y);
        reflection.setField(viewportWalking, null, true);
    }

    private long calculateTag(int arg1, int arg2, int type, boolean uninteractable, int identifier) {
        long tag = (long) ((arg1 & 127) | (arg2 & 127) << 7 | (type & 3) << 14) | ((long) identifier & 4294967295L) << 17;
        if (uninteractable) {
            tag |= 65536L;
        }

        return tag;
    }
}
