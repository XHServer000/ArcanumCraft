package github.xhserver123.arcanumcraft.managers;

import github.xhserver123.arcanumcraft.ArcanumCraftPlugin;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.OfflinePlayer;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;

public class CrystalManager {
    private final ArcanumCraftPlugin plugin;
    private final File crystalFile;
    private final FileConfiguration crystalConfig;
    private final Map<UUID, Integer> crystalBalances;
    private int lastResetDay = -1;

    public CrystalManager(ArcanumCraftPlugin plugin) {
        this.plugin = plugin;
        this.crystalFile = new File(plugin.getDataFolder(), "crystals.yml");
        this.crystalConfig = YamlConfiguration.loadConfiguration(crystalFile);
        this.crystalBalances = new HashMap<>();
        loadCrystals();
    }

    private void loadCrystals() {
        crystalBalances.clear();
        for (String key : crystalConfig.getKeys(false)) {
            try {
                UUID uuid = UUID.fromString(key);
                crystalBalances.put(uuid, crystalConfig.getInt(key + ".balance", 0));
            } catch (IllegalArgumentException ignored) {}
        }
    }

    public void saveAllCrystals() {
        crystalConfig.set(null, null);
        for (Map.Entry<UUID, Integer> entry : crystalBalances.entrySet()) {
            crystalConfig.set(entry.getKey().toString() + ".balance", entry.getValue());
        }
        try {
            crystalConfig.save(crystalFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "保存魔晶数据失败", e);
        }
    }

    public int getCrystalBalance(UUID uuid) {
        return crystalBalances.getOrDefault(uuid, 0);
    }

    public void addCrystal(UUID uuid, int amount) {
        int current = crystalBalances.getOrDefault(uuid, 0);
        crystalBalances.put(uuid, current + amount);
        plugin.getDataManager().incrementStat(uuid, DataManager.StatType.CRYSTALS_EARNED, amount);
    }

    public boolean transferCrystal(UUID from, UUID to, int amount) {
        int fromBalance = crystalBalances.getOrDefault(from, 0);
        if (fromBalance < amount) return false;
        crystalBalances.put(from, fromBalance - amount);
        crystalBalances.put(to, crystalBalances.getOrDefault(to, 0) + amount);
        return true;
    }

    public void resetDailyLimits(int day) {
        if (day == lastResetDay) return;
        lastResetDay = day;
        saveAllCrystals();
    }

    public Map<String, Integer> getTopCrystals(int topN) {
        Map<String, Integer> sorted = new LinkedHashMap<>();
        crystalBalances.entrySet().stream()
                .sorted(Map.Entry.<UUID, Integer>comparingByValue().reversed())
                .limit(topN)
                .forEach(e -> {
                    OfflinePlayer player = org.bukkit.Bukkit.getOfflinePlayer(e.getKey());
                    String name = player.getName() != null ? player.getName() : e.getKey().toString();
                    sorted.put(name, e.getValue());
                });
        return sorted;
    }
}
