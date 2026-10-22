package github.xhserver123.arcanumcraft;

import github.xhserver123.arcanumcraft.commands.*;
import github.xhserver123.arcanumcraft.listeners.*;
import github.xhserver123.arcanumcraft.managers.*;
import github.xhserver123.arcanumcraft.utils.*;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * ArcanumCraft 魔导工匠 - 主插件类
 * 
 * @author XHServer
 * @version 1.0.0
 */
public class ArcanumCraftPlugin extends JavaPlugin {

    private CrystalManager crystalManager;
    private PortalManager portalManager;
    private ForgeManager forgeManager;
    private EnchantManager enchantManager;
    private DataManager dataManager;
    private LanguageUtils languageUtils;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        
        dataManager = new DataManager(this);
        languageUtils = new LanguageUtils(this);
        crystalManager = new CrystalManager(this);
        portalManager = new PortalManager(this);
        forgeManager = new ForgeManager(this);
        enchantManager = new EnchantManager(this);

        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(new OreGenerationListener(this), this);
        pm.registerEvents(new PortalInteractionListener(this), this);
        pm.registerEvents(new ForgeInteractionListener(this), this);
        pm.registerEvents(new CrystalEarnListener(this), this);
        pm.registerEvents(new EnchantAltarListener(this), this);

        registerCommands();
        startBackgroundTasks();

        getLogger().info("ArcanumCraft 魔导工匠已加载！版本: " + getDescription().getVersion());
    }

    @Override
    public void onDisable() {
        crystalManager.saveAllCrystals();
        portalManager.saveAllPortals();
        forgeManager.saveAllForges();
        dataManager.saveAllData();
    }

    private void registerCommands() {
        getCommand("crystal").setExecutor(new CrystalCommand(this));
        getCommand("portal").setExecutor(new PortalCommand(this));
        getCommand("forge").setExecutor(new ForgeCommand(this));
        getCommand("enchant").setExecutor(new EnchantCommand(this));
        getCommand("arcanumcraft").setExecutor(new MainCommand(this));
    }

    private void startBackgroundTasks() {
        Bukkit.getScheduler().runTaskTimer(this, () -> forgeManager.processAllForges(), 200L, 200L);
    }

    public CrystalManager getCrystalManager() { return crystalManager; }
    public PortalManager getPortalManager() { return portalManager; }
    public ForgeManager getForgeManager() { return forgeManager; }
    public EnchantManager getEnchantManager() { return enchantManager; }
    public DataManager getDataManager() { return dataManager; }
    public LanguageUtils getLanguageUtils() { return languageUtils; }
}
