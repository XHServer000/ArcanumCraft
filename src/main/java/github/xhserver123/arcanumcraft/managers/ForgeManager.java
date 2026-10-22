package github.xhserver123.arcanumcraft.managers;

import github.xhserver123.arcanumcraft.ArcanumCraftPlugin;
import org.bukkit.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;

public class ForgeManager {
    private final ArcanumCraftPlugin plugin;
    private final File forgeFile;
    private final FileConfiguration forgeConfig;
    private final Map<String, ForgeInfo> forges;

    public ForgeManager(ArcanumCraftPlugin plugin) {
        this.plugin = plugin;
        this.forgeFile = new File(plugin.getDataFolder(), "forges.yml");
        this.forgeConfig = YamlConfiguration.loadConfiguration(forgeFile);
        this.forges = new HashMap<>();
        loadForges();
    }

    private void loadForges() {
        if (!forgeFile.exists()) return;
        for (String key : forgeConfig.getKeys(false)) {
            ForgeInfo info = new ForgeInfo();
            String typeStr = forgeConfig.getString(key + ".type", "SMELTING_FURNACE").toUpperCase();
            try { info.type = ForgeType.valueOf(typeStr); }
            catch (IllegalArgumentException e) { info.type = ForgeType.SMELTING_FURNACE; }
            try { info.owner = UUID.fromString(forgeConfig.getString(key + ".owner")); }
            catch (IllegalArgumentException e) { info.owner = UUID.randomUUID(); }
            info.remainingDurability = forgeConfig.getInt(key + ".durability", 100);
            info.maxDurability = info.type.getMaxDurability();
            info.lastWorkTick = forgeConfig.getLong(key + ".last-work", 0);
            org.bukkit.configuration.ConfigurationSection invSection = forgeConfig.getConfigurationSection(key + ".inventory");
            if (invSection != null) info.inventory = deserializeInventory(invSection);
            forges.put(key, info);
        }
    }

    private Inventory deserializeInventory(org.bukkit.configuration.ConfigurationSection section) {
        Inventory inv = Bukkit.createInventory(null, 9, "Forge Inventory");
        for (String slotStr : section.getKeys(false)) {
            try {
                int slot = Integer.parseInt(slotStr);
                org.bukkit.configuration.ConfigurationSection itemSection = section.getConfigurationSection(slotStr);
                if (itemSection != null) inv.setItem(slot, deserializeItem(itemSection));
            } catch (NumberFormatException ignored) {}
        }
        return inv;
    }

    private ItemStack deserializeItem(org.bukkit.configuration.ConfigurationSection section) {
        Material mat = Material.getMaterial(section.getString("material", "AIR"));
        if (mat == null || mat == Material.AIR) return new ItemStack(mat);
        int amount = section.getInt("amount", 1);
        ItemStack item = new ItemStack(mat, amount);
        if (section.contains("custom-name")) {
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', section.getString("custom-name")));
                item.setItemMeta(meta);
            }
        }
        return item;
    }

    public void saveAllForges() {
        forgeConfig.set(null, null);
        for (Map.Entry<String, ForgeInfo> entry : forges.entrySet()) {
            ForgeInfo info = entry.getValue();
            String prefix = "forges." + entry.getKey();
            forgeConfig.set(prefix + ".type", info.type.name());
            forgeConfig.set(prefix + ".owner", info.owner.toString());
            forgeConfig.set(prefix + ".durability", info.remainingDurability);
            forgeConfig.set(prefix + ".last-work", info.lastWorkTick);
            if (info.inventory != null) {
                org.bukkit.configuration.ConfigurationSection invSection = forgeConfig.createSection(prefix + ".inventory");
                for (int i = 0; i < info.inventory.getSize(); i++) {
                    ItemStack item = info.inventory.getItem(i);
                    if (item != null && !item.getType().isAir()) {
                        org.bukkit.configuration.ConfigurationSection isection = invSection.createSection(String.valueOf(i));
                        isection.set("material", item.getType().name());
                        isection.set("amount", item.getAmount());
                        if (item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
                            isection.set("custom-name", item.getItemMeta().getDisplayName());
                        }
                    }
                }
            }
        }
        try { forgeConfig.save(forgeFile); }
        catch (IOException e) { plugin.getLogger().log(Level.SEVERE, "保存熔炉数据失败", e); }
    }

    public boolean placeForge(Location loc, UUID owner, ForgeType type) {
        String key = locationKey(loc);
        if (forges.containsKey(key)) return false;
        ForgeInfo info = new ForgeInfo();
        info.type = type;
        info.owner = owner;
        info.location = loc;
        info.remainingDurability = type.getMaxDurability();
        info.maxDurability = type.getMaxDurability();
        info.inventory = Bukkit.createInventory(null, 9, type.getName() + " Forge");
        forges.put(key, info);
        saveAllForges();
        plugin.getDataManager().incrementStat(owner, DataManager.StatType.FORGES_BUILT, 1);
        return true;
    }

    public void processAllForges() {
        for (ForgeInfo info : forges.values()) processSingleForge(info);
    }

    private void processSingleForge(ForgeInfo info) {
        if (info.inventory == null) return;
        long now = System.currentTimeMillis();
        long interval = plugin.getConfig().getLong("forge.work-interval", 5) * 1000L;
        if (now - info.lastWorkTick < interval) return;
        for (int i = 0; i < info.inventory.getSize(); i++) {
            ItemStack stack = info.inventory.getItem(i);
            if (stack == null || stack.getType().isAir()) continue;
            ItemStack result = info.type.getRecipe(stack.getType());
            if (result != null) {
                int targetSlot = findEmptyOrMergeSlot(info.inventory, result);
                if (targetSlot >= 0) {
                    if (info.inventory.getItem(targetSlot) == null) {
                        info.inventory.setItem(targetSlot, result.clone());
                    } else {
                        info.inventory.getItem(targetSlot).setAmount(info.inventory.getItem(targetSlot).getAmount() + result.getAmount());
                    }
                    stack.setAmount(stack.getAmount() - 1);
                    if (stack.getAmount() <= 0) info.inventory.setItem(i, null);
                    info.lastWorkTick = now;
                    break;
                }
            }
        }
    }

    private int findEmptyOrMergeSlot(Inventory inv, ItemStack recipeResult) {
        Material type = recipeResult.getType();
        int maxStack = recipeResult.getType().getMaxStackSize();
        for (int i = 0; i < inv.getSize(); i++) {
            ItemStack existing = inv.getItem(i);
            if (existing != null && existing.getType() == type && existing.getAmount() < maxStack) return i;
        }
        for (int i = 0; i < inv.getSize(); i++) {
            if (inv.getItem(i) == null) return i;
        }
        return -1;
    }

    public Inventory getForgeInventory(Location loc) {
        ForgeInfo info = forges.get(locationKey(loc));
        return info != null ? info.inventory : null;
    }

    public ForgeType getForgeType(Location loc) {
        ForgeInfo info = forges.get(locationKey(loc));
        return info != null ? info.type : null;
    }

    public ForgeInfo getForgeInfo(Location loc) { return forges.get(locationKey(loc)); }

    public boolean removeForge(Location loc) {
        String key = locationKey(loc);
        return forges.remove(key) != null;
    }

    public Collection<ForgeInfo> getAllForges() { return forges.values(); }

    private String locationKey(Location loc) {
        return loc.getWorld().getName() + ":" + (int) loc.getX() + "," + (int) loc.getY() + "," + (int) loc.getZ();
    }

    public static class ForgeInfo {
        public ForgeType type;
        public UUID owner;
        public Location location;
        public int remainingDurability;
        public int maxDurability;
        public long lastWorkTick = 0;
        public Inventory inventory;
    }

    public enum ForgeType {
        SMELTING_FURNACE(Material.FURNACE, 30, "熔炼炉", 100),
        SMOKER(Material.SMOKER, 40, "烟熏炉", 120),
        BLAST_FURNACE(Material.BLAST_FURNACE, 50, "高炉", 150),
        CAULDRON(Material.CAULDRON, 20, "坩埚", 80);

        private final Material blockMaterial;
        private final int cost;
        private final String displayName;
        private final int maxDurability;

        ForgeType(Material blockMaterial, int cost, String displayName, int maxDurability) {
            this.blockMaterial = blockMaterial;
            this.cost = cost;
            this.displayName = displayName;
            this.maxDurability = maxDurability;
        }

        public Material getBlockMaterial() { return blockMaterial; }
        public int getCost() { return cost; }
        public String getName() { return displayName; }
        public int getMaxDurability() { return maxDurability; }

        public ItemStack getRecipe(Material input) {
            if (input == Material.IRON_ORE || input == Material.RAW_IRON) return new ItemStack(Material.IRON_INGOT);
            if (input == Material.GOLD_ORE || input == Material.RAW_GOLD) return new ItemStack(Material.GOLD_INGOT);
            if (input == Material.COAL_ORE) return new ItemStack(Material.COAL);
            if (input == Material.BEEF) return new ItemStack(Material.COOKED_BEEF);
            if (input == Material.PORKCHOP) return new ItemStack(Material.COOKED_PORKCHOP);
            if (input == Material.CHICKEN) return new ItemStack(Material.COOKED_CHICKEN);
            if (input == Material.POTATO) return new ItemStack(Material.BAKED_POTATO);
            if (input == Material.RABBIT) return new ItemStack(Material.COOKED_RABBIT);
            if (input == Material.SAND) return new ItemStack(Material.GLASS);
            if (input == Material.CLAY_BALL) return new ItemStack(Material.BRICK);
            if (input == Material.NETHERRACK) return new ItemStack(Material.SMOOTH_STONE);
            return null;
        }
    }
}
