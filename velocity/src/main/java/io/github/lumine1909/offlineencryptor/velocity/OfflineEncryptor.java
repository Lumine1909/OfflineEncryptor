package io.github.lumine1909.offlineencryptor.velocity;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.proxy.VelocityServer;
import com.velocitypowered.proxy.protocol.packet.ServerLoginPacket;
import io.github.lumine1909.offlineencryptor.NetworkProcessor;
import io.github.lumine1909.offlineencryptor.velocity.metrics.Metrics;
import org.slf4j.Logger;

public class OfflineEncryptor {

    public static OfflineEncryptor plugin;

    private final VelocityServer server;
    private final Logger logger;
    private final Metrics.Factory metricsFactory;
    private final NetworkProcessor<ServerLoginPacket> processor = new VelocityNetworkProcessor();

    @Inject
    public OfflineEncryptor(ProxyServer server, Logger logger, Metrics.Factory metricsFactory) {
        this.server = (VelocityServer) server;
        this.logger = logger;
        this.metricsFactory = metricsFactory;
        if (server.getConfiguration().isOnlineMode()) {
            throw new IllegalStateException("Encryption is already enabled in online mode!");
        }
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
}