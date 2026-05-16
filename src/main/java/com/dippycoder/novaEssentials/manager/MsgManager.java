package com.dippycoder.novaEssentials.manager;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MsgManager {

    /** Maps player UUID → UUID of the last player they messaged or received a message from. */
    private final Map<UUID, UUID> lastMessaged = new HashMap<>();

    public void recordMessage(UUID sender, UUID receiver) {
        lastMessaged.put(sender, receiver);
        lastMessaged.put(receiver, sender);
    }

    public UUID getLastMessaged(UUID player) {
        return lastMessaged.get(player);
    }

    public void remove(UUID player) {
        // Remove as a reply target for everyone who was messaging this player
        UUID partner = lastMessaged.remove(player);
        if (partner != null) {
            UUID partnerLast = lastMessaged.get(partner);
            if (player.equals(partnerLast)) lastMessaged.remove(partner);
        }
    }
}
