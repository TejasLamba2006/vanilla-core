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
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

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
        return Bukkit.createInventory(this, 54, plugin.getMessageManager().get("menu.main.title"));
    }

    public void setupItems() {
        inventory.clear();
        List<ItemStack> items = plugin.getFeatureManager().getMenuItems();

        int totalPages = getTotalPages(items.size());
        currentPage = Math.max(0, Math.min(currentPage, totalPages - 1));
        int startIndex = currentPage * SLOTS_PER_PAGE;

        int itemIndex = 0;
        for (int slot : VALID_SLOTS) {
            int actualIndex = startIndex + itemIndex;
            if (actualIndex >= items.size())
                break;
            inventory.setItem(slot, items.get(actualIndex));
            itemIndex++;
        }

        CDNManager cdnManager = plugin.getCDNManager();
        if (cdnManager != null && cdnManager.isUpdateAvailable()) {
            inventory.setItem(4, createUpdateIndicator(cdnManager));
        }

        inventory.setItem(47, createDiscordItem());
        inventory.setItem(48, createWikiItem());
        inventory.setItem(49, createModrinthItem());
        inventory.setItem(50,
                createMenuItem(Material.OAK_DOOR, plugin.getMessageManager().getRaw("menu.main.close.name"),
                        plugin.getMessageManager().getRaw("menu.main.close.lore-1")));
        inventory.setItem(51, createDonateItem());

        if (totalPages > 1) {
            if (currentPage > 0) {
                inventory.setItem(45, createNavItem(Material.ARROW,
                        plugin.getMessageManager().getRaw("menu.main.nav.previous.name"), currentPage, totalPages));
            }

            inventory.setItem(52, createPageIndicator(currentPage + 1, totalPages));

            if (currentPage < totalPages - 1) {
                inventory.setItem(53, createNavItem(Material.ARROW,
                        plugin.getMessageManager().getRaw("menu.main.nav.next.name"), currentPage + 2, totalPages));
            }
        }
    }

    private ItemStack createDiscordItem() {
        ItemStack item = createCustomSkull(DISCORD_TEXTURE);
        SkullMeta meta = (SkullMeta) item.getItemMeta();
        if (meta != null) {
            meta.displayName(plugin.getMessageManager().get("menu.main.discord.name"));
            List<Component> lore = new ArrayList<>();
            lore.add(Component.empty());
            lore.add(plugin.getMessageManager().get("menu.main.discord.lore-1"));
            lore.add(Component.empty());
            lore.add(plugin.getMessageManager().get("menu.main.discord.lore-2"));
            lore.add(plugin.getMessageManager().get("menu.main.discord.lore-3"));
            lore.add(Component.empty());
            lore.add(plugin.getMessageManager().get("menu.main.discord.action"));
            meta.lore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createWikiItem() {
        ItemStack item = createCustomSkull(WIKI_TEXTURE);
        SkullMeta meta = (SkullMeta) item.getItemMeta();
        if (meta != null) {
            meta.displayName(plugin.getMessageManager().get("menu.main.wiki.name"));
            List<Component> lore = new ArrayList<>();
            lore.add(Component.empty());
            lore.add(plugin.getMessageManager().get("menu.main.wiki.lore-1"));
            lore.add(plugin.getMessageManager().get("menu.main.wiki.lore-2"));
            lore.add(Component.empty());
            lore.add(plugin.getMessageManager().get("menu.main.wiki.lore-3"));
            lore.add(plugin.getMessageManager().get("menu.main.wiki.lore-4"));
            lore.add(Component.empty());
            lore.add(plugin.getMessageManager().get("menu.main.wiki.action"));
            meta.lore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createModrinthItem() {
        ItemStack item = createCustomSkull(MODRINTH_TEXTURE);
        SkullMeta meta = (SkullMeta) item.getItemMeta();
        if (meta != null) {
            meta.displayName(plugin.getMessageManager().get("menu.main.modrinth.name"));
            List<Component> lore = new ArrayList<>();
            lore.add(Component.empty());
            lore.add(plugin.getMessageManager().get("menu.main.modrinth.lore-1"));
            lore.add(Component.empty());
            lore.add(plugin.getMessageManager().get("menu.main.modrinth.lore-2"));
            lore.add(plugin.getMessageManager().get("menu.main.modrinth.lore-3"));
            lore.add(Component.empty());
            lore.add(plugin.getMessageManager().get("menu.main.modrinth.action"));
            meta.lore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createComingSoonItem() {
        ItemStack item = new ItemStack(Material.CLOCK);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(plugin.getMessageManager().get("menu.main.coming-soon.name"));
            List<Component> lore = new ArrayList<>();
            lore.add(Component.empty());
            lore.add(plugin.getMessageManager().get("menu.main.coming-soon.lore-1"));
            lore.add(plugin.getMessageManager().get("menu.main.coming-soon.lore-2"));
            lore.add(Component.empty());
            lore.add(plugin.getMessageManager().get("menu.main.coming-soon.lore-3"));
            lore.add(plugin.getMessageManager().get("menu.main.coming-soon.lore-4"));
            lore.add(Component.empty());
            lore.add(plugin.getMessageManager().get("menu.main.coming-soon.action"));
            meta.lore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createDonateItem() {
        ItemStack item = createCustomSkull(DONATE_TEXTURE);
        SkullMeta meta = (SkullMeta) item.getItemMeta();
        if (meta != null) {
            meta.displayName(plugin.getMessageManager().get("menu.main.donate.name"));
            List<Component> lore = new ArrayList<>();
            lore.add(Component.empty());
            lore.add(plugin.getMessageManager().get("menu.main.donate.lore-1"));
            lore.add(Component.empty());
            lore.add(plugin.getMessageManager().get("menu.main.donate.lore-2"));
            lore.add(plugin.getMessageManager().get("menu.main.donate.lore-3"));
            lore.add(plugin.getMessageManager().get("menu.main.donate.lore-4"));
            lore.add(plugin.getMessageManager().get("menu.main.donate.lore-5"));
            lore.add(Component.empty());
            lore.add(plugin.getMessageManager().get("menu.main.donate.action"));
            lore.add(plugin.getMessageManager().get("menu.main.donate.thanks"));
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
            meta.displayName(plugin.getMessageManager().get("menu.main.update.name"));
            List<Component> lore = new ArrayList<>();
            lore.add(Component.empty());
            lore.add(plugin.getMessageManager().get("menu.main.update.current", "version",
                    cdnManager.getCurrentVersion()));
            lore.add(plugin.getMessageManager().get("menu.main.update.latest", "version",
                    cdnManager.getLatestVersion()));
            lore.add(Component.empty());
            lore.add(plugin.getMessageManager().get("menu.main.update.action"));
            lore.add(Component.empty());
            lore.add(plugin.getMessageManager().get("menu.main.update.config-builder"));
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
                    plugin.getMessageManager().get("menu.main.nav.page-info", "page", targetPage, "total", totalPages));
            lore.add(plugin.getMessageManager().get("menu.main.nav.click"));
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
                    plugin.getMessageManager().get("menu.main.page-indicator.name", "page", current, "total", total));
            List<Component> lore = new ArrayList<>();
            lore.add(Component.empty());
            lore.add(plugin.getMessageManager().get("menu.main.page-indicator.lore-1"));
            lore.add(Component.empty());
            lore.add(plugin.getMessageManager().get("menu.main.page-indicator.lore-2"));
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
                player.sendMessage(plugin.getMessageManager().get("menu.main.chat.download", "url", MODRINTH_URL));
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
            player.sendMessage(plugin.getMessageManager().get("menu.main.chat.discord", "url", DISCORD_URL));
            return;
        }

        if (slot == 50) {
            player.closeInventory();
            CDNManager cdnManager = plugin.getCDNManager();
            String wikiUrl = cdnManager != null ? cdnManager.getDocumentationUrl() : WIKI_URL;
            player.sendMessage(plugin.getMessageManager().get("menu.main.chat.wiki", "url", wikiUrl));
            return;
        }

        if (slot == 49) {
            player.closeInventory();
            player.sendMessage(plugin.getMessageManager().get("menu.main.chat.modrinth", "url", MODRINTH_URL));
            return;
        }

        ItemStack item = event.getCurrentItem();

        if (slot == 51) {
            player.closeInventory();
            player.sendMessage(plugin.getMessageManager().get("menu.main.chat.donate-thanks"));
            player.sendMessage(plugin.getMessageManager().get("menu.main.chat.paypal", "url", PAYPAL_URL));
            player.sendMessage(plugin.getMessageManager().get("menu.main.chat.support"));
            return;
        }

        if (slot == 52) {
            player.closeInventory();
            return;
        }

        if (item != null && item.hasItemMeta() && item.getItemMeta().lore() != null) {
            List<Component> loreComponents = item.getItemMeta().lore();
            if (loreComponents != null) {
                for (Component loreComp : loreComponents) {
                    String plain = PlainTextComponentSerializer.plainText().serialize(loreComp);
                    if (plain.contains("Config: ")) {
                        String configPath = plain.replace("Config: ", "").trim();
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
