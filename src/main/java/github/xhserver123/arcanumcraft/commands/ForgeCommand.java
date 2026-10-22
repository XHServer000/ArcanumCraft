package github.xhserver123.arcanumcraft.commands;

import github.xhserver123.arcanumcraft.ArcanumCraftPlugin;
import github.xhserver123.arcanumcraft.managers.ForgeManager;
import github.xhserver123.arcanumcraft.utils.LanguageUtils;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.*;

public class ForgeCommand implements CommandExecutor, TabCompleter {
    private final ArcanumCraftPlugin plugin;
    private final LanguageUtils lang;
    private final ForgeManager forgeManager;

    public ForgeCommand(ArcanumCraftPlugin plugin) {
        this.plugin = plugin;
        this.lang = plugin.getLanguageUtils();
        this.forgeManager = plugin.getForgeManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(lang.translate("&c此命令只能由玩家执行！"));
            return true;
        }
        if (args.length == 0) { lang.send(player, "forge.usage"); return true; }
        switch (args[0].toLowerCase()) {
            case "status": handleStatus(player); break;
            case "mine": handleMine(player); break;
            default: lang.send(player, "forge.usage");
        }
        return true;
    }

    private void handleStatus(Player player) {
        Block target = player.getTargetBlock(null, 5);
        if (forgeManager.getForgeType(target.getLocation()) != null) {
            ForgeManager.ForgeInfo info = forgeManager.getForgeInfo(target.getLocation());
            if (info != null) lang.sendPrefixed(player, "forge.status.self", info.type.getName(), info.remainingDurability, info.maxDurability);
        } else { lang.send(player, "forge.status.none"); }
    }

    private void handleMine(Player player) {
        Block target = player.getTargetBlock(null, 5);
        if (forgeManager.getForgeType(target.getLocation()) == null) { lang.send(player, "forge.mine.no-forge"); return; }
        ForgeManager.ForgeInfo info = forgeManager.getForgeInfo(target.getLocation());
        if (info != null && info.owner.equals(player.getUniqueId())) {
            forgeManager.removeForge(target.getLocation());
            int refund = info.remainingDurability / 2;
            lang.sendPrefixed(player, "forge.mine.success", info.type.getName(), refund);
            target.getWorld().playSound(target.getLocation(), org.bukkit.Sound.BLOCK_STONE_BREAK, 1.0f, 1.0f);
        } else { lang.send(player, "forge.mine.no-permission"); }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        if (args.length == 1) {
            for (String sub : new String[]{"status", "mine"}) {
                if (sub.startsWith(args[0].toLowerCase())) completions.add(sub);
            }
        }
        return completions;
    }
}
