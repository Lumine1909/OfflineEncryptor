package io.github.lumine1909;

import io.github.lumine1909.metrics.Metrics;
import io.papermc.paper.network.ChannelInitializeListenerHolder;
import net.kyori.adventure.key.Key;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class OfflineEncryptor extends JavaPlugin {

    private static final Key KEY = Key.key("oe:handler");

    public static OfflineEncryptor plugin;

    private Metrics metrics;

    private final PacketProcessor packetProcessor = new PacketProcessor();

    @Override
    public void onEnable() {
        plugin = this;
        if (Bukkit.getOnlineMode()) {
            throw new IllegalStateException("Encryption is already enabled in online mode");
        }
        metrics = new Metrics(this, 27945);
        Bukkit.getPluginManager().registerEvents(new PlayerListener(), this);
        ChannelInitializeListenerHolder.addListener(KEY, packetProcessor::inject);
    }

    @Override
    public void onDisable() {
        if (metrics != null) {
            metrics.shutdown();
        }
        ChannelInitializeListenerHolder.removeListener(KEY);
    }

    public PacketProcessor getPacketProcessor() {
        return packetProcessor;
    }
}
