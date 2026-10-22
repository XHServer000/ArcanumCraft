package github.xhserver123.arcanumcraft.commands;

import github.xhserver123.arcanumcraft.ArcanumCraftPlugin;
import github.xhserver123.arcanumcraft.managers.EnchantManager;
import github.xhserver123.arcanumcraft.utils.LanguageUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class EnchantCommand implements CommandExecutor, TabCompleter {
    private final ArcanumCraftPlugin plugin;
    private final LanguageUtils lang;
    private final EnchantManager enchantManager;

    public EnchantCommand(ArcanumCraftPlugin plugin) {
        this.plugin = plugin;
        this.lang = plugin.getLanguageUtils();
        this.enchantManager = plugin.getEnchantManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(lang.translate("&c此命令只能由玩家执行！"));
            return true;
        }
        if (args.length == 0) { lang.send(player, "enchant.usage"); return true; }
        switch (args[0].toLowerCase()) {
            case "info": handleInfo(player); break;
            case "upgrade": handleUpgrade(player); break;
            case "imbue": handleImbue(player, args); break;
            default: lang.send(player, "enchant.usage");
        }
        return true;
    }

    private void handleInfo(Player player) {
        lang.sendPrefixed(player, "enchant.info.header");
        for (EnchantManager.CustomEnchantment enchant : enchantManager.getAvailableEnchantments()) {
            lang.send(player, "enchant.info.entry", enchant.getDisplayName(), enchant.getMinLevel(), enchant.getMaxLevel(), enchant.getBaseCost());
        }
    }

    private void handleUpgrade(Player player) {
        ItemStack held = player.getInventory().getItemInMainHand();
        if (held.getType().isAir()) { lang.send(player, "enchant.upgrade.no-item"); return; }
        lang.sendPrefixed(player, "enchant.upgrade.header", held.getType().name());
    }

    private void handleImbue(Player player, String[] args) {
        if (args.length < 3) { lang.send(player, "enchant.imbue.usage"); return; }
        String enchantName = args[1];
        int level;
        try { level = Integer.parseInt(args[2]); }
        catch (NumberFormatException e) { lang.send(player, "enchant.imbue.invalid-level"); return; }
        ItemStack held = player.getInventory().getItemInMainHand();
        if (held.getType().isAir()) { lang.send(player, "enchant.imbue.no-item"); return; }
        if (enchantManager.imbueEnchantment(player, held, enchantName, level)) {
            lang.sendPrefixed(player, "enchant.imbue.success", enchantName, level);
        } else { lang.send(player, "enchant.imbue.failed"); }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        if (args.length == 1) {
            for (String sub : new String[]{"info", "upgrade", "imbue"}) {
                if (sub.startsWith(args[0].toLowerCase())) completions.add(sub);
            }
        } else if (args.length == 2 && "imbue".equalsIgnoreCase(args[0])) {
            for (EnchantManager.CustomEnchantment enchant : enchantManager.getAvailableEnchantments()) {
                if (enchant.getName().toLowerCase().startsWith(args[1].toLowerCase())) completions.add(enchant.getName());
            }
        }
        return completions;
    }
}
