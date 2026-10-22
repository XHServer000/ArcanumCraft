package github.xhserver123.arcanumcraft.commands;

import github.xhserver123.arcanumcraft.ArcanumCraftPlugin;
import github.xhserver123.arcanumcraft.utils.LanguageUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.*;

public class MainCommand implements CommandExecutor, TabCompleter {
    private final ArcanumCraftPlugin plugin;
    private final LanguageUtils lang;

    public MainCommand(ArcanumCraftPlugin plugin) {
        this.plugin = plugin;
        this.lang = plugin.getLanguageUtils();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 0) { showHelp(sender); return true; }
        switch (args[0].toLowerCase()) {
            case "reload":
                if (!sender.hasPermission("arcanumcraft.admin")) { lang.send(sender, "messages.no-permission"); return true; }
                plugin.reloadConfig();
                lang.loadLanguage(plugin.getConfig().getString("messages.language", "zh_cn"));
                lang.sendPrefixed(sender, "arcanumcraft.reload.success");
                break;
            case "help": showHelp(sender); break;
            case "version": case "ver":
                lang.sendPrefixed(sender, "arcanumcraft.version", plugin.getDescription().getVersion());
                break;
            default: showHelp(sender);
        }
        return true;
    }

    private void showHelp(CommandSender sender) {
        lang.sendPrefixed(sender, "arcanumcraft.help.header");
        lang.send(sender, "arcanumcraft.help.crystal");
        lang.send(sender, "arcanumcraft.help.portal");
        lang.send(sender, "arcanumcraft.help.forge");
        lang.send(sender, "arcanumcraft.help.enchant");
        lang.send(sender, "arcanumcraft.help.main");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        if (args.length == 1) {
            for (String sub : new String[]{"reload", "help", "version", "ver"}) {
                if (sub.startsWith(args[0].toLowerCase())) completions.add(sub);
            }
        }
        return completions;
    }
}
