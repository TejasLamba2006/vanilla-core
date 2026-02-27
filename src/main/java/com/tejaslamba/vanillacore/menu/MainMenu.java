package com.tejaslamba.vanillacore.menu;

import com.tejaslamba.vanillacore.VanillaCorePlugin;
import com.tejaslamba.vanillacore.manager.CDNManager;
import com.tejaslamba.vanillacore.manager.MessageManager;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.profile.PlayerProfile;
import org.bukkit.profile.PlayerTextures;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MainMenu extends BaseMenu {

    private static final int[] VALID_SLOTS = calculateValidSlots();
    private static final int SLOTS_PER_PAGE = VALID_SLOTS.length;

    private static final String DISCORD_TEXTURE = "http://textures.minecraft.net/texture/5f865bb88f56ce010a8d9aeaace44a2ddcd3d6317aed8990b41b4ffa039836c3";
    private static final String WIKI_TEXTURE = "http://textures.minecraft.net/texture/1028ded8e389808d93571282d9490b581c9e8581113c3c1d329e7a8c9c410f";
    private static final String MODRINTH_TEXTURE = "http://textures.minecraft.net/texture/518b7dbdcf4ef9b365770c9fd10cbba1b0e81e567f7d8e8781b7f21507981183";

    private static final String DONATE_TEXTURE = "http://textures.minecraft.net/texture/e36e94f6c34a35465fce4a90f2e25976389eb9709a12273574ff70fd4daa6852";

    private static final String DISCORD_URL = "https://discord.gg/7fQPG4Grwt";
    private static final String WIKI_URL = "https://vanillacore.tejaslamba.com";
    private static final String MODRINTH_URL = "https://modrinth.com/plugin/vanillacorewastaken";
    private static final String PAYPAL_URL = "https://paypal.me/TejasLamba2006";

    private int currentPage = 0;

    public MainMenu(VanillaCorePlugin plugin) {
        super(plugin, null);
    }

    private static int[] calculateValidSlots() {
        List<Integer> slots = new ArrayList<>();
        for (int row = 1; row < 5; row++) {
            for (int col = 1; col < 8; col++) {
                slots.add(row * 9 + col);
            }
        }
        return slots.stream().mapToInt(Integer::intValue).toArray();
    }

    @Override
    protected Inventory createInventory() {
        return Bukkit.createInventory(this, 54, MessageManager.parse("<gold><bold>Vanilla Core Settings"));
    }

    public void setupItems() {
        inventory.clear();
        List<ItemStack> items = plugin.getFeatureManager().getMenuItems();

        int totalPages = getTotalPages(items.size());
        int startIndex = currentPage * SLOTS_PER_PAGE;

        int itemIndex = 0;
        int lastUsedSlot = -1;
        for (int slot : VALID_SLOTS) {
            int actualIndex = startIndex + itemIndex;
            if (actualIndex >= items.size())
                break;
            inventory.setItem(slot, items.get(actualIndex));
            lastUsedSlot = slot;
            itemIndex++;
        }

        int itemsOnThisPage = Math.min(items.size() - startIndex, SLOTS_PER_PAGE);
        if (itemsOnThisPage < SLOTS_PER_PAGE && itemsOnThisPage > 0) {
            int nextSlotIndex = itemsOnThisPage;
            if (nextSlotIndex < VALID_SLOTS.length) {
                inventory.setItem(VALID_SLOTS[nextSlotIndex], createComingSoonItem());
                nextSlotIndex++;
            }
            if (nextSlotIndex < VALID_SLOTS.length) {
                inventory.setItem(VALID_SLOTS[nextSlotIndex], createDonateItem());
            }
        }

        CDNManager cdnManager = plugin.getCDNManager();
        if (cdnManager != null && cdnManager.isUpdateAvailable()) {
            inventory.setItem(4, createUpdateIndicator(cdnManager));
        }

        inventory.setItem(47, createDiscordItem());
        inventory.setItem(48, createWikiItem());
        inventory.setItem(49, createModrinthItem());
        inventory.setItem(50, createMenuItem(Material.OAK_DOOR, "<red><bold>Close Menu", "<gray>Close this menu"));
        inventory.setItem(51, createDonateItem());

        if (totalPages > 1) {
            if (currentPage > 0) {
                inventory.setItem(45, createNavItem(Material.ARROW, "<green>« Previous Page", currentPage, totalPages));
            }

            inventory.setItem(52, createPageIndicator(currentPage + 1, totalPages));

            if (currentPage < totalPages - 1) {
                inventory.setItem(53, createNavItem(Material.ARROW, "<green>Next Page »", currentPage + 2, totalPages));
            }
        }
    }

    private ItemStack createDiscordItem() {
        ItemStack item = createCustomSkull(DISCORD_TEXTURE);
        SkullMeta meta = (SkullMeta) item.getItemMeta();
        if (meta != null) {
            meta.displayName(MessageManager.parse("<!italic><aqua><bold>Discord"));
            List<Component> lore = new ArrayList<>();
            lore.add(Component.empty());
            lore.add(MessageManager.parse("<!italic><gray>Join our Discord community!"));
            lore.add(Component.empty());
            lore.add(MessageManager.parse("<!italic><gray>Get support, report bugs,"));
            lore.add(MessageManager.parse("<!italic><gray>and suggest new features."));
            lore.add(Component.empty());
            lore.add(MessageManager.parse("<!italic><yellow>Click to join!"));
            meta.lore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createWikiItem() {
        ItemStack item = createCustomSkull(WIKI_TEXTURE);
        SkullMeta meta = (SkullMeta) item.getItemMeta();
        if (meta != null) {
            meta.displayName(MessageManager.parse("<!italic><green><bold>Wiki / Documentation"));
            List<Component> lore = new ArrayList<>();
            lore.add(Component.empty());
            lore.add(MessageManager.parse("<!italic><gray>View the full documentation"));
            lore.add(MessageManager.parse("<!italic><gray>for Vanilla Core plugin."));
            lore.add(Component.empty());
            lore.add(MessageManager.parse("<!italic><gray>Features, commands, configs,"));
            lore.add(MessageManager.parse("<!italic><gray>permissions and more!"));
            lore.add(Component.empty());
            lore.add(MessageManager.parse("<!italic><yellow>Click to open!"));
            meta.lore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createModrinthItem() {
        ItemStack item = createCustomSkull(MODRINTH_TEXTURE);
        SkullMeta meta = (SkullMeta) item.getItemMeta();
        if (meta != null) {
            meta.displayName(MessageManager.parse("<!italic><dark_green><bold>Modrinth"));
            List<Component> lore = new ArrayList<>();
            lore.add(Component.empty());
            lore.add(MessageManager.parse("<!italic><gray>Download on Modrinth!"));
            lore.add(Component.empty());
            lore.add(MessageManager.parse("<!italic><gray>Leave a review, check for"));
            lore.add(MessageManager.parse("<!italic><gray>updates, and see changelog."));
            lore.add(Component.empty());
            lore.add(MessageManager.parse("<!italic><yellow>Click to open!"));
            meta.lore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createComingSoonItem() {
        ItemStack item = new ItemStack(Material.CLOCK);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(MessageManager.parse("<!italic><yellow><bold>More Features Coming Soon!"));
            List<Component> lore = new ArrayList<>();
            lore.add(Component.empty());
            lore.add(MessageManager.parse("<!italic><gray>We're always working on new"));
            lore.add(MessageManager.parse("<!italic><gray>features for Vanilla Core!"));
            lore.add(Component.empty());
            lore.add(MessageManager.parse("<!italic><gray>Have a suggestion? Join our"));
            lore.add(MessageManager.parse("<!italic><gray>Discord and let us know!"));
            lore.add(Component.empty());
            lore.add(MessageManager.parse("<!italic><light_purple><italic>Donate to speed up development!"));
            meta.lore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createDonateItem() {
        ItemStack item = createCustomSkull(DONATE_TEXTURE);
        SkullMeta meta = (SkullMeta) item.getItemMeta();
        if (meta != null) {
            meta.displayName(MessageManager.parse("<!italic><gold><bold>Donate"));
            List<Component> lore = new ArrayList<>();
            lore.add(Component.empty());
            lore.add(MessageManager.parse("<!italic><gray>Support Vanilla Core development!"));
            lore.add(Component.empty());
            lore.add(MessageManager.parse("<!italic><gray>Your donations help us:"));
            lore.add(MessageManager.parse("<!italic><green>• <gray>Add new features faster"));
            lore.add(MessageManager.parse("<!italic><green>• <gray>Fix bugs quicker"));
            lore.add(MessageManager.parse("<!italic><green>• <gray>Keep the plugin free"));
            lore.add(Component.empty());
            lore.add(MessageManager.parse("<!italic><yellow><bold>Click to donate via PayPal!"));
            lore.add(MessageManager.parse("<!italic><gray>Thank you for your support! <red>❤"));
            meta.lore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createCustomSkull(String textureUrl) {
        ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) skull.getItemMeta();
        if (meta != null) {
            PlayerProfile profile = Bukkit.createPlayerProfile(UUID.randomUUID());
            PlayerTextures textures = profile.getTextures();
            try {
                textures.setSkin(new URL(textureUrl));
                profile.setTextures(textures);
                meta.setOwnerProfile(profile);
                skull.setItemMeta(meta);
            } catch (MalformedURLException e) {
                plugin.getLogger().warning("Failed to set skull texture: " + e.getMessage());
            }
        }
        return skull;
    }

    private ItemStack createUpdateIndicator(CDNManager cdnManager) {
        ItemStack item = new ItemStack(Material.NETHER_STAR);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(MessageManager.parse("<!italic><green><bold>Update Available!"));
            List<Component> lore = new ArrayList<>();
            lore.add(Component.empty());
            lore.add(MessageManager.parse("<!italic><gray>Current: <red>" + cdnManager.getCurrentVersion()));
            lore.add(MessageManager.parse("<!italic><gray>Latest: <green>" + cdnManager.getLatestVersion()));
            lore.add(Component.empty());
            lore.add(MessageManager.parse("<!italic><yellow>Click to open download page"));
            lore.add(Component.empty());
            lore.add(MessageManager.parse("<!italic><gray>Config Builder:"));
            lore.add(MessageManager.parse("<!italic><aqua>" + cdnManager.getConfigBuilderUrl()));
            meta.lore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createNavItem(Material material, String name, int targetPage, int totalPages) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(MessageManager.parse("<!italic>" + name));
            List<Component> lore = new ArrayList<>();
            lore.add(Component.empty());
            lore.add(
                    MessageManager.parse("<!italic><gray>Page <yellow>" + targetPage + "<gray>/<yellow>" + totalPages));
            lore.add(MessageManager.parse("<!italic><yellow>Click to navigate"));
            meta.lore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createPageIndicator(int current, int total) {
        ItemStack item = new ItemStack(Material.BOOK);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(
                    MessageManager.parse("<!italic><gold>Page <yellow>" + current + "<gold>/<yellow>" + total));
            List<Component> lore = new ArrayList<>();
            lore.add(Component.empty());
            lore.add(MessageManager.parse("<!italic><gray>Use arrows to navigate"));
            lore.add(Component.empty());
            lore.add(MessageManager.parse("<!italic><red>Click to close menu"));
            meta.lore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    private int getTotalPages(int totalItems) {
        return Math.max(1, (int) Math.ceil((double) totalItems / SLOTS_PER_PAGE));
    }

    @Override
    public void open(Player player) {
        setupItems();
        player.openInventory(inventory);
    }

    public void openPage(Player player, int page) {
        List<ItemStack> items = plugin.getFeatureManager().getMenuItems();
        int totalPages = getTotalPages(items.size());
        currentPage = Math.max(0, Math.min(page, totalPages - 1));
        setupItems();
        player.openInventory(inventory);
    }

    public void handleClick(InventoryClickEvent event) {
        event.setCancelled(true);
        Player player = (Player) event.getWhoClicked();
        int slot = event.getRawSlot();
        boolean isRightClick = event.isRightClick();

        List<ItemStack> items = plugin.getFeatureManager().getMenuItems();
        int totalPages = getTotalPages(items.size());

        if (slot == 4) {
            ItemStack item = event.getCurrentItem();
            if (item != null && item.getType() == Material.NETHER_STAR) {
                player.closeInventory();
                player.sendMessage(MessageManager.parse(
                        "<gold><bold>[Vanilla Core] <gray>Download: <aqua>https://modrinth.com/plugin/vanillacorewastaken"));
                player.sendMessage(MessageManager.parse(
                        "<gold><bold>[Vanilla Core] <gray>Config Builder: <aqua>"
                                + plugin.getCDNManager().getConfigBuilderUrl()));
                return;
            }
        }

        if (slot == 45 && currentPage > 0) {
            currentPage--;
            setupItems();
            return;
        }

        if (slot == 53 && currentPage < totalPages - 1) {
            currentPage++;
            setupItems();
            return;
        }

        if (slot == 47) {
            player.closeInventory();
            player.sendMessage(MessageManager.parse("<aqua><bold>[Vanilla Core] <gray>Discord: <aqua>" + DISCORD_URL));
            return;
        }

        if (slot == 50) {
            player.closeInventory();
            CDNManager cdnManager = plugin.getCDNManager();
            String wikiUrl = cdnManager != null ? cdnManager.getDocumentationUrl() : WIKI_URL;
            player.sendMessage(MessageManager.parse("<green><bold>[Vanilla Core] <gray>Wiki: <green>" + wikiUrl));
            return;
        }

        if (slot == 49) {
            player.closeInventory();
            player.sendMessage(MessageManager
                    .parse("<dark_green><bold>[Vanilla Core] <gray>Modrinth: <dark_green>" + MODRINTH_URL));
            return;
        }

        if (slot == 50) {
            player.closeInventory();
            return;
        }
        ItemStack item = event.getCurrentItem();

        if (slot == 51) {
            player.closeInventory();
            player.sendMessage(
                    MessageManager.parse("<gold><bold>[Vanilla Core] <gray>Thank you for considering a donation!"));
            player.sendMessage(MessageManager.parse("<gold><bold>[Vanilla Core] <gray>PayPal: <yellow>" + PAYPAL_URL));
            player.sendMessage(
                    MessageManager.parse("<gray>Your support helps keep Vanilla Core free and updated! <red>❤"));
            return;
        }

        if (slot == 52) {
            player.closeInventory();
            return;
        }

        if (item != null && item.getType() == Material.CLOCK) {
            return;
        }

        if (item != null && item.hasItemMeta() && item.getItemMeta().hasLore()) {
            List<String> lore = item.getItemMeta().getLore();
            if (lore != null) {
                for (String line : lore) {
                    if (line.contains("§8Config: §7")) {
                        String configPath = line.replace("§8Config: §7", "").replaceAll("§.", "");
                        String featureConfigPath = configPath.replace(".enabled", "");

                        com.tejaslamba.vanillacore.feature.Feature feature = plugin.getFeatureManager()
                                .getFeatureByConfigPath(featureConfigPath);
                        if (feature != null) {
                            if (isRightClick) {
                                feature.onRightClick(player);
                            } else {
                                feature.onLeftClick(player);
                            }
                            refresh(player);
                            setupItems();
                        }
                        return;
                    }
                }
            }
        }
    }

}
