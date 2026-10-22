package github.xhserver123.arcanumcraft.listeners;

import github.xhserver123.arcanumcraft.ArcanumCraftPlugin;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

public class EnchantAltarListener implements Listener {
    private final ArcanumCraftPlugin plugin;

    public EnchantAltarListener(ArcanumCraftPlugin plugin) { this.plugin = plugin; }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        Player player = event.getPlayer();
        ItemStack held = player.getInventory().getItemInMainHand();
        if (held.getType() == Material.ENCHANTED_BOOK && held.hasItemMeta()) {
            ItemMeta meta = held.getItemMeta();
            if (meta != null && meta.hasLore()) {
                for (String line : meta.getLore()) {
                    if (line.contains("魔导")) {
                        event.setCancelled(true);
                        openEnchantInterface(player);
                        break;
                    }
                }
            }
        }
    }

    private void openEnchantInterface(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, ChatColor.DARK_PURPLE + "魔导祭坛");
        int slot = 0;
        for (var enchant : plugin.getEnchantManager().getAvailableEnchantments()) {
            ItemStack display = new ItemStack(Material.ENCHANTED_BOOK);
            ItemMeta meta = display.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(ChatColor.GOLD + enchant.getDisplayName());
                meta.setLore(Arrays.asList(
                        ChatColor.GRAY + "等级范围: " + enchant.getMinLevel() + "-" + enchant.getMaxLevel(),
                        ChatColor.GRAY + "基础费用: " + enchant.getBaseCost() + " 魔晶"));
                meta.addItemFlags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES);
                display.setItemMeta(meta);
            }
            inv.setItem(slot++, display);
        }
        player.openInventory(inv);
    }
}
