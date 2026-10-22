package github.xhserver123.arcanumcraft.listeners;

import github.xhserver123.arcanumcraft.ArcanumCraftPlugin;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public class PortalInteractionListener implements Listener {
    private final ArcanumCraftPlugin plugin;

    public PortalInteractionListener(ArcanumCraftPlugin plugin) { this.plugin = plugin; }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        Block block = event.getClickedBlock();
        if (block == null) return;
        // 检测传送门激活逻辑
    }
}
