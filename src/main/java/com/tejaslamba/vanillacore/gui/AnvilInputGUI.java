package com.tejaslamba.vanillacore.gui;

import com.tejaslamba.vanillacore.Main;
import org.bukkit.entity.Player;

import java.util.function.BiConsumer;
import java.util.function.Predicate;

public class AnvilInputGUI {

    private final Main plugin;
    private final Player player;
    private final String prompt;
    private final String defaultValue;
    private final BiConsumer<Player, String> onComplete;
    private final Runnable onCancel;
    private Predicate<String> validator;
    private String errorMessage = "§cInvalid input!";

    public AnvilInputGUI(Main plugin, Player player, String prompt, String defaultValue, 
                        BiConsumer<Player, String> onComplete, Runnable onCancel) {
        this.plugin = plugin;
        this.player = player;
        this.prompt = prompt;
        this.defaultValue = defaultValue;
        this.onComplete = onComplete;
        this.onCancel = onCancel;
    }

    public AnvilInputGUI withValidator(Predicate<String> validator, String errorMessage) {
        this.validator = validator;
        this.errorMessage = errorMessage;
        return this;
    }

    public AnvilInputGUI withNumberValidator(int min, int max) {
        this.validator = input -> {
            try {
                int value = Integer.parseInt(input);
                return value >= min && value <= max;
            } catch (NumberFormatException e) {
                return false;
            }
        };
        this.errorMessage = "§cPlease enter a number between " + min + " and " + max;
        return this;
    }

    public AnvilInputGUI withDoubleValidator(double min, double max) {
        this.validator = input -> {
            try {
                double value = Double.parseDouble(input);
                return value >= min && value <= max;
            } catch (NumberFormatException e) {
                return false;
            }
        };
        this.errorMessage = "§cPlease enter a number between " + min + " and " + max;
        return this;
    }

    public AnvilInputGUI withNonEmptyValidator() {
        this.validator = input -> input != null && !input.trim().isEmpty();
        this.errorMessage = "§cInput cannot be empty!";
        return this;
    }

    public void open() {
        player.closeInventory();
        player.sendMessage("§6[Vanilla Core] §7" + prompt);
        if (defaultValue != null && !defaultValue.isEmpty()) {
            player.sendMessage("§7Current value: §e" + defaultValue);
        }
        player.sendMessage("§7Type your input in chat, or type §ccancel§7 to abort.");

        plugin.getChatInputManager().requestInput(player, (p, input) -> {
            if (input.equalsIgnoreCase("cancel")) {
                p.sendMessage("§c[Vanilla Core] Input cancelled.");
                if (onCancel != null) {
                    onCancel.run();
                }
                return;
            }

            if (validator != null && !validator.test(input)) {
                p.sendMessage("§c[Vanilla Core] " + errorMessage);
                if (onCancel != null) {
                    onCancel.run();
                }
                return;
            }

            onComplete.accept(p, input);
        });
    }

    public static class Builder {
        private Main plugin;
        private Player player;
        private String prompt;
        private String defaultValue = "";
        private BiConsumer<Player, String> onComplete;
        private Runnable onCancel;

        public Builder(Main plugin, Player player) {
            this.plugin = plugin;
            this.player = player;
        }

        public Builder prompt(String prompt) {
            this.prompt = prompt;
            return this;
        }

        public Builder defaultValue(String defaultValue) {
            this.defaultValue = defaultValue;
            return this;
        }

        public Builder onComplete(BiConsumer<Player, String> onComplete) {
            this.onComplete = onComplete;
            return this;
        }

        public Builder onCancel(Runnable onCancel) {
            this.onCancel = onCancel;
            return this;
        }

        public AnvilInputGUI build() {
            return new AnvilInputGUI(plugin, player, prompt, defaultValue, onComplete, onCancel);
        }
    }
}
