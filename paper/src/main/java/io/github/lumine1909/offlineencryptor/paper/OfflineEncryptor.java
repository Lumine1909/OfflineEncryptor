package io.github.lumine1909.offlineencryptor.paper;

import io.github.lumine1909.offlineencryptor.NetworkProcessor;
import io.github.lumine1909.offlineencryptor.compat.EncryptionCompats;
import io.github.lumine1909.offlineencryptor.compat.ViaVersionCompat;
import io.github.lumine1909.offlineencryptor.paper.metrics.Metrics;
import io.papermc.paper.network.ChannelInitializeListenerHolder;
import net.kyori.adventure.key.Key;
import net.minecraft.network.protocol.login.ServerboundHelloPacket;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class OfflineEncryptor extends JavaPlugin {

    private static final Key KEY = Key.key("oe:handler");

    public static OfflineEncryptor plugin;
    private final EncryptionCompats encryptionCompats = EncryptionCompats.create(Bukkit::getOnlineMode);
    private final ViaVersionCompat viaVersionCompat = ViaVersionCompat.create(false, Bukkit.getPluginManager().getPlugin("ViaVersion") != null);
    private final NetworkProcessor<ServerboundHelloPacket> networkProcessor = new PaperNetworkProcessor();

    private Metrics metrics;

    @Override
    public void onEnable() {
        plugin = this;
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

    public EncryptionCompats getEncryptionCompats() {
        return encryptionCompats;
    }

    public ViaVersionCompat getViaVersionCompat() {
        return viaVersionCompat;
    }
}