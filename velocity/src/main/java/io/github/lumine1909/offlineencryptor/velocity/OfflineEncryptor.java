package io.github.lumine1909.offlineencryptor.velocity;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.proxy.VelocityServer;
import com.velocitypowered.proxy.connection.client.InitialInboundConnection;
import com.velocitypowered.proxy.connection.client.LoginInboundConnection;
import com.velocitypowered.proxy.protocol.packet.ServerLoginPacket;
import io.github.lumine1909.offlineencryptor.NetworkProcessor;
import io.github.lumine1909.offlineencryptor.velocity.metrics.Metrics;
import io.github.lumine1909.reflexion.Field;

import javax.inject.Inject;
import java.util.logging.Logger;

public class OfflineEncryptor {

    private static final Field<InitialInboundConnection> field$delegate = Field.of(
        LoginInboundConnection.class, "delegate", InitialInboundConnection.class);

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
        ServerChannelInitializerInjector.injectToServer(this.server);
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
    }

    @Subscribe
    public void onPlayerDisconnect(DisconnectEvent event) {
        processor.getCache().remove(event.getPlayer().getUsername());
    }
}