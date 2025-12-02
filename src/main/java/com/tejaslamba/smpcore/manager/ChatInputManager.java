package com.tejaslamba.smpcore.manager;

import org.bukkit.entity.Player;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ChatInputManager {

    private final Map<UUID, ChatInputHandler> awaitingInput = new ConcurrentHashMap<>();

    public void requestInput(Player player, ChatInputHandler handler) {
        awaitingInput.put(player.getUniqueId(), handler);
    }

    public boolean hasAwaitingInput(Player player) {
        return awaitingInput.containsKey(player.getUniqueId());
    }

    public void handleInput(Player player, String input) {
        ChatInputHandler handler = awaitingInput.remove(player.getUniqueId());
        if (handler != null) {
            handler.onInput(player, input);
        }
    }

    public void cancelInput(Player player) {
        awaitingInput.remove(player.getUniqueId());
    }

    public interface ChatInputHandler {
        void onInput(Player player, String input);
    }

}
