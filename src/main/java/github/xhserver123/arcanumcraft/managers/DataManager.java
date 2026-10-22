package github.xhserver123.arcanumcraft.managers;

import github.xhserver123.arcanumcraft.ArcanumCraftPlugin;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

public class DataManager {
    private final ArcanumCraftPlugin plugin;
    private final File dataFile;
    private final Map<UUID, PlayerData> playerDataMap;

    public static class PlayerData {
        public int totalCrystalsEarned = 0;
        public int totalPortalsCreated = 0;
        public int totalForgesBuilt = 0;
        public int totalEnchantmentsUsed = 0;
        public int lastDailyResetDay = -1;
    }

    public enum StatType {
        CRYSTALS_EARNED, PORTALS_CREATED, FORGES_BUILT, ENCHANTMENTS_USED
    }

    public DataManager(ArcanumCraftPlugin plugin) {
        this.plugin = plugin;
        this.dataFile = new File(plugin.getDataFolder(), "data.yml");
        this.playerDataMap = new HashMap<>();
    }

    public void loadAllData() {
        if (!dataFile.exists()) return;
        FileConfiguration config = YamlConfiguration.loadConfiguration(dataFile);
        for (String key : config.getKeys(false)) {
            try {
                UUID uuid = UUID.fromString(key);
                PlayerData data = new PlayerData();
                data.totalCrystalsEarned = config.getInt(key + ".total-crystals-earned", 0);
                data.totalPortalsCreated = config.getInt(key + ".total-portals-created", 0);
                data.totalForgesBuilt = config.getInt(key + ".total-forges-built", 0);
                data.totalEnchantmentsUsed = config.getInt(key + ".total-enchantments-used", 0);
                data.lastDailyResetDay = config.getInt(key + ".last-daily-reset-day", -1);
                playerDataMap.put(uuid, data);
            } catch (IllegalArgumentException ignored) {}
        }
    }

    public void saveAllData() {
        FileConfiguration config = new YamlConfiguration();
        for (Map.Entry<UUID, PlayerData> entry : playerDataMap.entrySet()) {
            PlayerData data = entry.getValue();
            String key = entry.getKey().toString();
            config.set(key + ".total-crystals-earned", data.totalCrystalsEarned);
            config.set(key + ".total-portals-created", data.totalPortalsCreated);
            config.set(key + ".total-forges-built", data.totalForgesBuilt);
            config.set(key + ".total-enchantments-used", data.totalEnchantmentsUsed);
            config.set(key + ".last-daily-reset-day", data.lastDailyResetDay);
        }
        try {
            config.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "保存数据失败", e);
        }
    }

    public PlayerData getOrCreateData(UUID uuid) {
        return playerDataMap.computeIfAbsent(uuid, k -> new PlayerData());
    }

    public void incrementStat(UUID uuid, StatType type, int amount) {
        PlayerData data = getOrCreateData(uuid);
        switch (type) {
            case CRYSTALS_EARNED: data.totalCrystalsEarned += amount; break;
            case PORTALS_CREATED: data.totalPortalsCreated += amount; break;
            case FORGES_BUILT: data.totalForgesBuilt += amount; break;
            case ENCHANTMENTS_USED: data.totalEnchantmentsUsed += amount; break;
        }
    }
}
