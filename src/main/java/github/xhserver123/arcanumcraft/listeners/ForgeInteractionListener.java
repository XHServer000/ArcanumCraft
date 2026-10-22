package github.xhserver123.arcanumcraft.listeners;

import github.xhserver123.arcanumcraft.ArcanumCraftPlugin;
import github.xhserver123.arcanumcraft.managers.ForgeManager;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public class ForgeInteractionListener implements Listener {
    private final ArcanumCraftPlugin plugin;

    public ForgeInteractionListener(ArcanumCraftPlugin plugin) { this.plugin = plugin; }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        Block block = event.getClickedBlock();
        if (block == null) return;
        ForgeManager.ForgeType type = null;
        if (block.getType() == Material.FURNACE) type = ForgeManager.ForgeType.SMELTING_FURNACE;
        else if (block.getType() == Material.SMOKER) type = ForgeManager.ForgeType.SMOKER;
        else if (block.getType() == Material.BLAST_FURNACE) type = ForgeManager.ForgeType.BLAST_FURNACE;
        if (type != null) {
            ForgeManager.ForgeInfo info = plugin.getForgeManager().getForgeInfo(block.getLocation());
            if (info != null) {
                event.setCancelled(true);
                Player player = event.getPlayer();
                player.openInventory(info.inventory);
            }
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        ForgeManager.ForgeType type = plugin.getForgeManager().getForgeType(block.getLocation());
        if (type == null) return;
        ForgeManager.ForgeInfo info = plugin.getForgeManager().getForgeInfo(block.getLocation());
        if (info == null) return;
        event.setCancelled(true);
        block.getWorld().dropItemNaturally(block.getLocation(), new org.bukkit.inventory.ItemStack(type.getBlockMaterial()));
        if (info.inventory != null) {
            for (org.bukkit.inventory.ItemStack item : info.inventory.getContents()) {
                if (item != null && !item.getType().isAir()) block.getWorld().dropItemNaturally(block.getLocation(), item.clone());
            }
        }
        plugin.getForgeManager().removeForge(block.getLocation());
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getInventory().getHolder() == null) return;
        for (ForgeManager.ForgeInfo forgeInfo : plugin.getForgeManager().getAllForges()) {
            if (forgeInfo.inventory == event.getInventory()) {
                Player player = (Player) event.getWhoClicked();
                if (!player.getUniqueId().equals(forgeInfo.owner) && !player.hasPermission("arcanumcraft.admin")) {
                    event.setCancelled(true);
                }
                return;
            }
        }
    }
}
