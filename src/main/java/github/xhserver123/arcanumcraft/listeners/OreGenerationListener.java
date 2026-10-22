package github.xhserver123.arcanumcraft.listeners;

import github.xhserver123.arcanumcraft.ArcanumCraftPlugin;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkPopulateEvent;

import java.util.Random;

public class OreGenerationListener implements Listener {
    private final ArcanumCraftPlugin plugin;
    private final Random random = new Random();

    public OreGenerationListener(ArcanumCraftPlugin plugin) { this.plugin = plugin; }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onChunkPopulate(ChunkPopulateEvent event) {
        if (!plugin.getConfig().getBoolean("crystal.ore-generation.enabled", true)) return;
        Chunk chunk = event.getChunk();
        World world = chunk.getWorld();
        int maxPerChunk = plugin.getConfig().getInt("crystal.ore-generation.max-per-chunk", 3);
        int minHeight = plugin.getConfig().getInt("crystal.ore-generation.min-height", 10);
        int maxHeight = plugin.getConfig().getInt("crystal.ore-generation.max-height", 40);
        int count = random.nextInt(maxPerChunk) + 1;
        for (int i = 0; i < count; i++) {
            int x = chunk.getX() * 16 + random.nextInt(16);
            int z = chunk.getZ() * 16 + random.nextInt(16);
            int y = minHeight + random.nextInt(maxHeight - minHeight + 1);
            Block block = world.getBlockAt(x, y, z);
            if (block.getType() == Material.STONE || block.getType() == Material.DEEPSLATE) {
                block.setType(Material.AMETHYST_BLOCK);
            }
        }
    }
}
