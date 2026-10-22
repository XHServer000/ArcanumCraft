package github.xhserver123.arcanumcraft.utils;

import github.xhserver123.arcanumcraft.ArcanumCraftPlugin;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;

public class LanguageUtils {
    private final ArcanumCraftPlugin plugin;
    private FileConfiguration messages;
    private final String defaultLang;

    public LanguageUtils(ArcanumCraftPlugin plugin) {
        this.plugin = plugin;
        this.defaultLang = plugin.getConfig().getString("messages.language", "zh_cn");
        loadLanguage(defaultLang);
    }

    public void loadLanguage(String lang) {
        File langFile = new File(plugin.getDataFolder(), "lang/" + lang + ".yml");
        if (langFile.exists()) {
            messages = YamlConfiguration.loadConfiguration(langFile);
        } else {
            messages = new YamlConfiguration();
        }
    }

    public String get(String key) {
        return messages != null && messages.contains(key) ? translate(messages.getString(key)) : key;
    }

    public String get(String key, Object... replacements) {
        String msg = get(key);
        for (int i = 0; i < replacements.length; i += 2) {
            if (i + 1 < replacements.length) {
                msg = msg.replace("{" + replacements[i] + "}", String.valueOf(replacements[i + 1]));
            }
        }
        return msg;
    }

    public void send(CommandSender sender, String key, Object... replacements) {
        if (sender instanceof ConsoleCommandSender) {
            sender.sendMessage(get(key, replacements));
        } else {
            sender.sendMessage(translate(get(key, replacements)));
        }
    }

    public void sendPrefixed(CommandSender sender, String key, Object... replacements) {
        String prefix = translate(plugin.getConfig().getString("messages.prefix", "&9[魔导工匠] &f"));
        sender.sendMessage(prefix + " " + get(key, replacements));
    }

    public static String translate(String text) {
        return net.md_5.bungee.api.ChatColor.translateAlternateColorCodes('&', text);
    }
}
