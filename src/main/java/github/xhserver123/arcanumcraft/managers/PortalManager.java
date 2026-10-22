package github.xhserver123.arcanumcraft.managers;

import github.xhserver123.arcanumcraft.ArcanumCraftPlugin;
import org.bukkit.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;

public class PortalManager {
    private final ArcanumCraftPlugin plugin;
    private final File portalFile;
    private final FileConfiguration portalConfig;
    private final Map<String, Location> portals;
    private final Map<UUID, Set<String>> ownerPortals;

    public PortalManager(ArcanumCraftPlugin plugin) {
        this.plugin = plugin;
        this.portalFile = new File(plugin.getDataFolder(), "portals.yml");
        this.portalConfig = YamlConfiguration.loadConfiguration(portalFile);
        this.portals = new HashMap<>();
        this.ownerPortals = new HashMap<>();
        loadPortals();
    }

    private void loadPortals() {
        if (!portalFile.exists()) return;
        for (String name : portalConfig.getKeys(false)) {
            Location loc = new Location(
                    Bukkit.getWorld(portalConfig.getString(name + ".world")),
                    portalConfig.getDouble(name + ".x"),
                    portalConfig.getDouble(name + ".y"),
                    portalConfig.getDouble(name + ".z"),
                    (float) portalConfig.getDouble(name + ".yaw"),
                    (float) portalConfig.getDouble(name + ".pitch"));
            portals.put(name, loc);
            try {
                UUID owner = UUID.fromString(portalConfig.getString(name + ".owner"));
                ownerPortals.computeIfAbsent(owner, k -> new HashSet<>()).add(name);
            } catch (IllegalArgumentException ignored) {}
        }
    }

    public void saveAllPortals() {
        portalConfig.set(null, null);
        for (Map.Entry<String, Location> entry : portals.entrySet()) {
            Location loc = entry.getValue();
            String name = entry.getKey();
            portalConfig.set(name + ".x", loc.getX());
            portalConfig.set(name + ".y", loc.getY());
            portalConfig.set(name + ".z", loc.getZ());
            portalConfig.set(name + ".yaw", loc.getYaw());
            portalConfig.set(name + ".pitch", loc.getPitch());
            portalConfig.set(name + ".world", loc.getWorld() != null ? loc.getWorld().getName() : "");
            portalConfig.set(name + ".owner", ownerPortals.entrySet().stream()
                    .filter(e -> e.getValue().contains(name)).findFirst()
                    .map(Map.Entry::getKey).map(UUID::toString).orElse(""));
        }
        try {
            portalConfig.save(portalFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "保存传送门数据失败", e);
        }
    }

    public boolean createPortal(String name, Location loc, UUID owner) {
        if (portals.containsKey(name)) return false;
        portals.put(name, loc);
        ownerPortals.computeIfAbsent(owner, k -> new HashSet<>()).add(name);
        saveAllPortals();
        plugin.getDataManager().incrementStat(owner, DataManager.StatType.PORTALS_CREATED, 1);
        return true;
    }

    public boolean removePortal(String name, UUID owner) {
        if (!portals.containsKey(name)) return false;
        portals.remove(name);
        Set<String> owned = ownerPortals.getOrDefault(owner, Collections.emptySet());
        owned.remove(name);
        saveAllPortals();
        return true;
    }

    public boolean activatePortal(Player player, String name) {
        Location dest = portals.get(name);
        if (dest == null) return false;
        if (plugin.getConfig().getBoolean("portal.force-chunk-load", true)) {
            Chunk chunk = dest.getChunk();
            if (!chunk.isLoaded()) chunk.load(true);
        }
        double delay = plugin.getConfig().getDouble("portal.teleport-delay", 0);
        if (delay > 0) {
            int taskId = Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> teleportPlayer(player, dest), (long) (delay * 20));
        } else {
            teleportPlayer(player, dest);
        }
        return true;
    }

    private void teleportPlayer(Player player, Location loc) {
        player.teleport(loc);
        player.getWorld().playSound(loc, Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);
        int protectionSeconds = plugin.getConfig().getInt("portal.protection-seconds", 2);
        if (protectionSeconds > 0) {
            player.setInvulnerable(true);
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (player.isOnline()) player.setInvulnerable(false);
            }, protectionSeconds * 20L);
        }
    }

    public List<String> getPortalNames() { return new ArrayList<>(portals.keySet()); }
    public boolean hasPortal(String name) { return portals.containsKey(name); }
    public Location getPortalLocation(String name) { return portals.get(name); }
}
