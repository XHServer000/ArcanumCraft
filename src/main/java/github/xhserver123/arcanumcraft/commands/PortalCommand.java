package github.xhserver123.arcanumcraft.commands;

import github.xhserver123.arcanumcraft.ArcanumCraftPlugin;
import github.xhserver123.arcanumcraft.managers.PortalManager;
import github.xhserver123.arcanumcraft.utils.LanguageUtils;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.*;

public class PortalCommand implements CommandExecutor, TabCompleter {
    private final ArcanumCraftPlugin plugin;
    private final LanguageUtils lang;
    private final PortalManager portalManager;

    public PortalCommand(ArcanumCraftPlugin plugin) {
        this.plugin = plugin;
        this.lang = plugin.getLanguageUtils();
        this.portalManager = plugin.getPortalManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(lang.translate("&c此命令只能由玩家执行！"));
            return true;
        }
        if (args.length == 0) { lang.send(player, "portal.usage"); return true; }
        switch (args[0].toLowerCase()) {
            case "set": handleSet(player, args); break;
            case "list": handleList(player); break;
            case "remove": handleRemove(player, args); break;
            case "goto": case "tp": handleGoto(player, args); break;
            default: lang.send(player, "portal.usage");
        }
        return true;
    }

    private void handleSet(Player player, String[] args) {
        if (args.length < 2) { lang.send(player, "portal.set.usage"); return; }
        String name = args[1];
        if (portalManager.hasPortal(name)) { lang.send(player, "portal.set.already-exists", name); return; }
        Location loc = player.getLocation();
        if (portalManager.createPortal(name, loc, player.getUniqueId())) {
            lang.sendPrefixed(player, "portal.set.success", name);
            player.getWorld().playSound(loc, org.bukkit.Sound.BLOCK_PORTAL_TRAVEL, 1.0f, 1.0f);
        } else { lang.send(player, "portal.set.failed"); }
    }

    private void handleList(Player player) {
        List<String> names = portalManager.getPortalNames();
        if (names.isEmpty()) { lang.send(player, "portal.list.empty"); return; }
        lang.sendPrefixed(player, "portal.list.header");
        for (String name : names) {
            Location loc = portalManager.getPortalLocation(name);
            if (loc != null) lang.send(player, "portal.list.entry", name, loc.getWorld().getName(), (int) loc.getX(), (int) loc.getY(), (int) loc.getZ());
        }
    }

    private void handleRemove(Player player, String[] args) {
        if (args.length < 2) { lang.send(player, "portal.remove.usage"); return; }
        if (portalManager.removePortal(args[1], player.getUniqueId())) {
            lang.sendPrefixed(player, "portal.remove.success", args[1]);
        } else { lang.send(player, "portal.remove.not-found", args[1]); }
    }

    private void handleGoto(Player player, String[] args) {
        if (args.length < 2) { lang.send(player, "portal.goto.usage"); return; }
        if (portalManager.activatePortal(player, args[1])) {
            lang.sendPrefixed(player, "portal.goto.success", args[1]);
        } else { lang.send(player, "portal.goto.not-found", args[1]); }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        if (args.length == 1) {
            for (String sub : new String[]{"set", "list", "remove", "goto"}) {
                if (sub.startsWith(args[0].toLowerCase())) completions.add(sub);
            }
        } else if (args.length == 2) {
            for (String name : portalManager.getPortalNames()) {
                if (name.startsWith(args[1].toLowerCase())) completions.add(name);
            }
        }
        return completions;
    }
}
