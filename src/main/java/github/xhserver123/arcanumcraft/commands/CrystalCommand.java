package github.xhserver123.arcanumcraft.commands;

import github.xhserver123.arcanumcraft.ArcanumCraftPlugin;
import github.xhserver123.arcanumcraft.managers.CrystalManager;
import github.xhserver123.arcanumcraft.utils.LanguageUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.*;

public class CrystalCommand implements CommandExecutor, TabCompleter {
    private final ArcanumCraftPlugin plugin;
    private final LanguageUtils lang;
    private final CrystalManager crystalManager;

    public CrystalCommand(ArcanumCraftPlugin plugin) {
        this.plugin = plugin;
        this.lang = plugin.getLanguageUtils();
        this.crystalManager = plugin.getCrystalManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(lang.translate("&c此命令只能由玩家执行！"));
            return true;
        }
        if (args.length == 0) { showInfo(player); return true; }
        switch (args[0].toLowerCase()) {
            case "info": showInfo(player); break;
            case "pay": handlePay(player, args); break;
            case "top": showTop(player); break;
            default: lang.send(sender, "messages.unknown-subcommand");
        }
        return true;
    }

    private void showInfo(Player player) {
        int balance = crystalManager.getCrystalBalance(player.getUniqueId());
        lang.sendPrefixed(player, "crystal.balance", balance);
    }

    private void handlePay(Player player, String[] args) {
        if (args.length < 3) { lang.send(player, "crystal.pay.usage"); return; }
        Player target = plugin.getServer().getPlayerExact(args[1]);
        if (target == null) { lang.send(player, "crystal.pay.player-not-found"); return; }
        int amount;
        try { amount = Integer.parseInt(args[2]); }
        catch (NumberFormatException e) { lang.send(player, "crystal.pay.invalid-amount"); return; }
        if (amount <= 0) { lang.send(player, "crystal.pay.negative"); return; }
        boolean success = crystalManager.transferCrystal(player.getUniqueId(), target.getUniqueId(), amount);
        if (success) {
            lang.sendPrefixed(player, "crystal.pay.success", target.getName(), amount);
            lang.sendPrefixed(target, "crystal.pay.received", player.getName(), amount);
        } else { lang.send(player, "crystal.pay.insufficient"); }
    }

    private void showTop(Player player) {
        Map<String, Integer> top = crystalManager.getTopCrystals(10);
        if (top.isEmpty()) { lang.send(player, "crystal.top.empty"); return; }
        lang.sendPrefixed(player, "crystal.top.header");
        int rank = 1;
        for (Map.Entry<String, Integer> entry : top.entrySet()) {
            lang.send(player, "crystal.top.entry", rank, entry.getKey(), entry.getValue());
            rank++;
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        if (args.length == 1) {
            for (String sub : new String[]{"info", "pay", "top"}) {
                if (sub.startsWith(args[0].toLowerCase())) completions.add(sub);
            }
        } else if (args.length == 2 && "pay".equalsIgnoreCase(args[0])) {
            for (Player p : plugin.getServer().getOnlinePlayers()) {
                if (p.getName().toLowerCase().startsWith(args[1].toLowerCase())) completions.add(p.getName());
            }
        }
        return completions;
    }
}
