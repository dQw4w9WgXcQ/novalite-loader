package io.novalite;

import io.novalite.commons.Definitions;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.ItemComposition;
import net.runelite.api.NPCComposition;
import net.runelite.api.ObjectComposition;
import net.runelite.api.events.PostItemComposition;
import net.runelite.api.events.PostObjectComposition;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;

import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class DefinitionCache implements Definitions {
    private final ConcurrentHashMap<Integer, ItemComposition> itemDefCache = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Integer, NPCComposition> npcDefCache = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Integer, ObjectComposition> objectDefCache = new ConcurrentHashMap<>();

    private final Client client;
    private final ClientThread clientThread;

    public DefinitionCache(Client client, ClientThread clientThread, EventBus eventBus) {
        this.client = client;
        this.clientThread = clientThread;
        eventBus.register(this);
    }

    @Override
    public ItemComposition getItemDefinition(int id) {
        return itemDefCache.computeIfAbsent(id, (key) -> ClientThreadUtil.invoke(clientThread, () -> client.getItemDefinition(key)));
    }

    @Override
    public NPCComposition getNpcDefinition(int id) {
        return npcDefCache.computeIfAbsent(id, (key) -> ClientThreadUtil.invoke(clientThread, () -> client.getNpcDefinition(key)));
    }

    @Override
    public ObjectComposition getObjectDefinition(int id) {
        return objectDefCache.computeIfAbsent(id, (key) -> ClientThreadUtil.invoke(clientThread, () -> client.getObjectDefinition(key)));
    }

    @Subscribe
    public void onPostItemComposition(PostItemComposition e) {
        ItemComposition prev = itemDefCache.putIfAbsent(e.getItemComposition().getId(), e.getItemComposition());
        if (prev == null) {
            log.debug("Item {} was added", e.getItemComposition().getId());
        }
    }

    @Subscribe
    public void onPostObjectComposition(PostObjectComposition e) {
        ObjectComposition prev = objectDefCache.putIfAbsent(e.getObjectComposition().getId(), e.getObjectComposition());
        if (prev == null) {
            log.debug("Object {} was added", e.getObjectComposition().getId());
        }
    }
}
