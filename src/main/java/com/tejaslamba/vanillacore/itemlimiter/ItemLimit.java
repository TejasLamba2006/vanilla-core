package com.tejaslamba.vanillacore.itemlimiter;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionType;

public class ItemLimit {

    private final Material material;
    private int limit;
    private final Integer customModelData;
    private final String displayName;
    private final PotionType potionType;

    public ItemLimit(Material material, int limit, Integer customModelData, String displayName) {
        this(material, limit, customModelData, displayName, null);
    }

    public ItemLimit(Material material, int limit, Integer customModelData, String displayName, PotionType potionType) {
        if (material == null) {
            throw new IllegalArgumentException("Material cannot be null");
        }
        if (limit < 0) {
            throw new IllegalArgumentException("Limit must be non-negative");
        }
        this.material = material;
        this.limit = limit;
        this.customModelData = customModelData;
        this.displayName = displayName;
        this.potionType = potionType;
    }

    public boolean matches(ItemStack item) {
        if (item == null || item.getType() != material) {
            return false;
        }

        ItemMeta meta = item.getItemMeta();

        if (material.toString().contains("POTION") && meta instanceof PotionMeta potionMeta) {
            if (potionType != null) {
                return potionMeta.getBasePotionType() == potionType;
            }
            return true;
        }

        if (customModelData == null && displayName == null) {
            return meta == null || (!meta.hasCustomModelData() && !meta.hasDisplayName());
        }

        if (meta == null) {
            return false;
        }

        if (customModelData != null) {
            if (!meta.hasCustomModelData()) {
                return false;
            }
            if (meta.getCustomModelData() != customModelData) {
                return false;
            }
        }

        if (displayName != null) {
            if (!meta.hasDisplayName() || !meta.getDisplayName().equals(displayName)) {
                return false;
            }
        } else if (meta.hasDisplayName()) {
            return false;
        }

        return true;
    }

    public ItemStack createItemStack() {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            boolean metaChanged = false;

            if (material.toString().contains("POTION") && meta instanceof PotionMeta potionMeta && potionType != null) {
                potionMeta.setBasePotionType(potionType);
                metaChanged = true;
            }

            if (customModelData != null) {
                meta.setCustomModelData(customModelData);
                metaChanged = true;
            }

            if (displayName != null && !displayName.isEmpty()) {
                meta.setDisplayName(displayName);
                metaChanged = true;
            }

            if (metaChanged) {
                item.setItemMeta(meta);
            }
        }

        return item;
    }

    public Material getMaterial() {
        return material;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        if (limit < 0) {
            throw new IllegalArgumentException("Limit must be non-negative");
        }
        this.limit = limit;
    }

    public Integer getCustomModelData() {
        return customModelData;
    }

    public String getDisplayName() {
        return displayName;
    }

    public PotionType getPotionType() {
        return potionType;
    }

    public boolean isValid() {
        return material != null && limit >= 0 && limit <= 64000;
    }

    public String generateKey() {
        StringBuilder key = new StringBuilder(material.name());

        if (potionType != null) {
            key.append("_").append(potionType.toString());
        }

        if (customModelData != null) {
            key.append("_CMD_").append(customModelData);
        }

        if (displayName != null && !displayName.isEmpty()) {
            String cleanName = displayName.replaceAll("§.", "")
                    .replaceAll("[^a-zA-Z0-9_]", "_")
                    .replaceAll("_+", "_");
            key.append("_").append(cleanName);
        }

        return key.toString();
    }

    public ItemLimit copy() {
        return new ItemLimit(material, limit, customModelData, displayName, potionType);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("ItemLimit{material=").append(material);
        sb.append(", limit=").append(limit);
        if (customModelData != null) {
            sb.append(", customModelData=").append(customModelData);
        }
        if (displayName != null) {
            sb.append(", displayName='").append(displayName).append("'");
        }
        if (potionType != null) {
            sb.append(", potionType=").append(potionType);
        }
        sb.append("}");
        return sb.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;

        ItemLimit itemLimit = (ItemLimit) obj;

        if (limit != itemLimit.limit)
            return false;
        if (!material.equals(itemLimit.material))
            return false;
        if (customModelData != null ? !customModelData.equals(itemLimit.customModelData)
                : itemLimit.customModelData != null)
            return false;
        if (displayName != null ? !displayName.equals(itemLimit.displayName) : itemLimit.displayName != null)
            return false;
        return potionType == itemLimit.potionType;
    }

    @Override
    public int hashCode() {
        int result = material.hashCode();
        result = 31 * result + limit;
        result = 31 * result + (customModelData != null ? customModelData.hashCode() : 0);
        result = 31 * result + (displayName != null ? displayName.hashCode() : 0);
        result = 31 * result + (potionType != null ? potionType.hashCode() : 0);
        return result;
    }
}
