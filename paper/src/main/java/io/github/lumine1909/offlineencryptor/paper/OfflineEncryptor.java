package io.github.lumine1909.offlineencryptor.paper;

import io.github.lumine1909.offlineencryptor.NetworkProcessor;
import io.github.lumine1909.offlineencryptor.paper.metrics.Metrics;
import io.papermc.paper.network.ChannelInitializeListenerHolder;
import net.kyori.adventure.key.Key;
import net.minecraft.network.protocol.login.ServerboundHelloPacket;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class OfflineEncryptor extends JavaPlugin {

    private static final Key KEY = Key.key("oe:handler");

    public static OfflineEncryptor plugin;
    private final NetworkProcessor<ServerboundHelloPacket> networkProcessor = new PaperNetworkProcessor();
    private Metrics metrics;

    @Override
    public void onEnable() {
        plugin = this;
        if (Bukkit.getOnlineMode()) {
            throw new IllegalStateException("Encryption is already enabled in online mode!");
        }
        if (Bukkit.getServerConfig().isProxyEnabled()) {
            throw new IllegalStateException("This plugin doesn't work behind the proxy server, please install this in proxy instead!");
        }
        metrics = new Metrics(this, 27945);
        Bukkit.getPluginManager().registerEvents(new PlayerListener(), this);
        ChannelInitializeListenerHolder.addListener(KEY, networkProcessor::inject);
    }

    @Override
    public void onDisable() {
        if (metrics != null) {
            metrics.shutdown();
        }
        ChannelInitializeListenerHolder.removeListener(KEY);
    }

    public NetworkProcessor<ServerboundHelloPacket> getPacketProcessor() {
        return networkProcessor;
    }
}