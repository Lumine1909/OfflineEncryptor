package io.github.lumine1909;

import com.destroystokyo.paper.event.player.PlayerConnectionCloseEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import static io.github.lumine1909.OfflineEncryptor.plugin;

public class PlayerListener implements Listener {

    @EventHandler
    public void onConnectionClose(PlayerConnectionCloseEvent event) {
        plugin.getPacketProcessor().invalidateCache(event.getPlayerName());
    }
}
