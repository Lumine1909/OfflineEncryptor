package io.github.lumine1909.offlineencryptor.velocity;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.proxy.VelocityServer;
import com.velocitypowered.proxy.protocol.packet.ServerLoginPacket;
import io.github.lumine1909.offlineencryptor.NetworkProcessor;
import io.github.lumine1909.offlineencryptor.compat.AuthenticateCompats;
import io.github.lumine1909.offlineencryptor.compat.ViaVersionCompat;
import io.github.lumine1909.offlineencryptor.velocity.metrics.Metrics;
import org.slf4j.Logger;

public class OfflineEncryptor {

    public static OfflineEncryptor plugin;

    private final VelocityServer server;
    private final Logger logger;
    private final Metrics.Factory metricsFactory;
    private final AuthenticateCompats authenticateCompats;
    private final ViaVersionCompat viaVersionCompat;
    private final NetworkProcessor<ServerLoginPacket> processor = new VelocityNetworkProcessor();

    @Inject
    public OfflineEncryptor(ProxyServer server, Logger logger, Metrics.Factory metricsFactory) {
        this.server = (VelocityServer) server;
        this.logger = logger;
        this.metricsFactory = metricsFactory;
        this.authenticateCompats = AuthenticateCompats.create(server.getConfiguration()::isOnlineMode);
        this.viaVersionCompat = ViaVersionCompat.create(true, server.getPluginManager().getPlugin("viaversion").isPresent());
        plugin = this;
    }

    public VelocityServer getServer() {
        return server;
    }

    public NetworkProcessor<ServerLoginPacket> getNetworkProcessor() {
        return processor;
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        metricsFactory.make(this, 27988);
        ServerChannelInitializerInjector.injectToServer(this.server);
    }

    @Subscribe
    public void onPlayerDisconnect(DisconnectEvent event) {
        processor.getCache().remove(event.getPlayer().getUsername());
    }

    public AuthenticateCompats getAuthenticateCompats() {
        return authenticateCompats;
    }

    public ViaVersionCompat getViaVersionCompat() {
        return viaVersionCompat;
    }
}