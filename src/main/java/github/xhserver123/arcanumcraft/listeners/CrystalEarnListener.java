package github.xhserver123.arcanumcraft.listeners;

import github.xhserver123.arcanumcraft.ArcanumCraftPlugin;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.Random;

public class CrystalEarnListener implements Listener {
    private final ArcanumCraftPlugin plugin;
    private final Random random = new Random();

    public CrystalEarnListener(ArcanumCraftPlugin plugin) { this.plugin = plugin; }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        int bonus = plugin.getConfig().getInt("crystal.join-bonus", 10);
        plugin.getCrystalManager().addCrystal(player.getUniqueId(), bonus);
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();
        if (block.getType() == Material.AMETHYST_BLOCK) {
            if (random.nextInt(100) < 30) {
                int amount = random.nextInt(3) + 1;
                plugin.getCrystalManager().addCrystal(player.getUniqueId(), amount);
                player.getWorld().playSound(player.getLocation(), org.bukkit.Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5f, 1.2f);
            }
        }
    }

    @EventHandler
    public void onPlayerFish(PlayerFishEvent event) {
        if (event.getState() == PlayerFishEvent.State.CAUGHT_FISH) {
            Player player = event.getPlayer();
            if (random.nextInt(100) < 15) {
                int amount = random.nextInt(2) + 1;
                plugin.getCrystalManager().addCrystal(player.getUniqueId(), amount);
            }
        }
    }
}
