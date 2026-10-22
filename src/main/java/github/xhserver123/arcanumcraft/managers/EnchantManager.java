package github.xhserver123.arcanumcraft.managers;

import github.xhserver123.arcanumcraft.ArcanumCraftPlugin;
import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.logging.Level;

public class EnchantManager {
    private final ArcanumCraftPlugin plugin;
    private final Map<String, CustomEnchantment> customEnchantments;

    public EnchantManager(ArcanumCraftPlugin plugin) {
        this.plugin = plugin;
        this.customEnchantments = new HashMap<>();
        registerCustomEnchantments();
    }

    private void registerCustomEnchantments() {
        customEnchantments.put("soul_fire", new CustomEnchantment("soul_fire", "灵魂烈焰", "SOUL_FIRE", 10, 3, 5.0));
        customEnchantments.put("void_touch", new CustomEnchantment("void_touch", "虚空之触", "VOID_TOUCH", 8, 2, 8.0));
        customEnchantments.put("arcane_mastery", new CustomEnchantment("arcane_mastery", "奥术精通", "ARCANE_MASTERY", 5, 1, 10.0));
        customEnchantments.put("temporal_shift", new CustomEnchantment("temporal_shift", "时移", "TEMPORAL_SHIFT", 15, 2, 15.0));
        customEnchantments.put("elemental_resonance", new CustomEnchantment("elemental_resonance", "元素共鸣", "ELEMENTAL_RESONANCE", 20, 3, 20.0));
    }

    public boolean imbueEnchantment(Player player, ItemStack target, String enchantName, int level) {
        CustomEnchantment enchant = customEnchantments.get(enchantName.toLowerCase());
        if (enchant == null) return false;
        if (level < enchant.getMinLevel() || level > enchant.getMaxLevel()) return false;
        if (!enchant.supports(target.getType())) return false;

        double costMult = plugin.getConfig().getDouble("enchantment.cost-multiplier", 2.0);
        int crystalCost = (int) (level * 5 * costMult);
        if (crystalCost > plugin.getCrystalManager().getCrystalBalance(player.getUniqueId())) return false;
        plugin.getCrystalManager().addCrystal(player.getUniqueId(), -crystalCost);

        Enchantment bukkitEnchant = getBukkitEquivalent(enchant);
        if (bukkitEnchant != null) target.addUnsafeEnchantment(bukkitEnchant, level);

        player.getWorld().spawnParticle(Particle.VILLAGER_HAPPY, player.getLocation().add(0, 1, 0), 20, 0.5, 0.5, 0.5, 0.05);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.5f);
        plugin.getDataManager().incrementStat(player.getUniqueId(), DataManager.StatType.ENCHANTMENTS_USED, 1);
        return true;
    }

    private Enchantment getBukkitEquivalent(CustomEnchantment enchant) {
        switch (enchant.getName()) {
            case "soul_fire": return Enchantment.FIRE_ASPECT;
            case "void_touch": return Enchantment.KNOCKBACK;
            case "arcane_mastery": return Enchantment.getByName("SMITE");
            case "temporal_shift": return Enchantment.SWEEPING_EDGE;
            case "elemental_resonance": return Enchantment.getByName("PROTECTION");
            default: return null;
        }
    }

    public List<CustomEnchantment> getAvailableEnchantments() { return new ArrayList<>(customEnchantments.values()); }
    public CustomEnchantment getCustomEnchantment(String name) { return customEnchantments.get(name.toLowerCase()); }

    public static class CustomEnchantment {
        private final String internalName;
        private final String displayName;
        private final String bukkitKey;
        private final int minLevel;
        private final int maxLevel;
        private final double baseCost;
        private final Set<Material> applicableMaterials;

        public CustomEnchantment(String internalName, String displayName, String bukkitKey, int minLevel, int maxLevel, double baseCost) {
            this.internalName = internalName;
            this.displayName = displayName;
            this.bukkitKey = bukkitKey;
            this.minLevel = minLevel;
            this.maxLevel = maxLevel;
            this.baseCost = baseCost;
            this.applicableMaterials = EnumSet.of(
                    Material.DIAMOND_SWORD, Material.IRON_SWORD, Material.GOLDEN_SWORD,
                    Material.STONE_SWORD, Material.NETHERITE_SWORD,
                    Material.BOW, Material.CROSSBOW, Material.TRIDENT);
        }

        public String getName() { return internalName; }
        public String getDisplayName() { return displayName; }
        public int getMinLevel() { return minLevel; }
        public int getMaxLevel() { return maxLevel; }
        public double getBaseCost() { return baseCost; }
        public boolean supports(Material material) { return applicableMaterials.contains(material); }
    }
}
