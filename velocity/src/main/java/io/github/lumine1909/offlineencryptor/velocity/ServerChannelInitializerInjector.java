package io.github.lumine1909.offlineencryptor.velocity;

import com.velocitypowered.proxy.VelocityServer;
import com.velocitypowered.proxy.network.ConnectionManager;
import io.github.lumine1909.reflexion.Field;
import io.github.lumine1909.reflexion.Method;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;

import static io.github.lumine1909.offlineencryptor.velocity.OfflineEncryptor.plugin;

public class ServerChannelInitializerInjector {

    private static final Field<ConnectionManager> field$cm = Field.of(
        VelocityServer.class, "cm"
    );

    private static final Method<Void> method$initChannel = Method.of(
        ChannelInitializer.class, "initChannel", void.class, Channel.class
    );

    @SuppressWarnings("deprecation")
    public static void injectToServer(VelocityServer server) {
        ConnectionManager cm = field$cm.getFast(server);
        ChannelInitializer<Channel> initializer = cm.serverChannelInitializer.get();
        cm.serverChannelInitializer.set(createDelegatedInitializer(initializer));
    }

    private static ChannelInitializer<Channel> createDelegatedInitializer(ChannelInitializer<Channel> delegate) {
        return new ChannelInitializer<>() {
            @Override
            protected void initChannel(Channel channel) {
                method$initChannel.invoke(delegate, channel);
                plugin.getNetworkProcessor().inject(channel);
            }
        };
    }
}