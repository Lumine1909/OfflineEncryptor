package io.github.lumine1909.offlineencryptor.velocity;

import com.velocitypowered.proxy.VelocityServer;
import com.velocitypowered.proxy.network.ConnectionManager;
import io.github.lumine1909.reflexion.Field;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;

import static io.github.lumine1909.offlineencryptor.velocity.OfflineEncryptor.plugin;
import static io.github.lumine1909.reflexion.UnsafeUtil.IMPL_LOOKUP;

public class ServerChannelInitializerInjector {

    private static final Field<ConnectionManager> field$cm = Field.of(
        VelocityServer.class, "cm", ConnectionManager.class
    );

    private static final MethodHandle mh$initChannel;

    static {
        try {
            mh$initChannel = IMPL_LOOKUP.in(ChannelInitializer.class)
                .findVirtual(ChannelInitializer.class, "initChannel", MethodType.methodType(void.class, Channel.class));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("deprecation")
    public static void injectToServer(VelocityServer server) {
        ConnectionManager cm = field$cm.get(server);
        ChannelInitializer<Channel> initializer = cm.serverChannelInitializer.get();
        cm.serverChannelInitializer.set(createDelegatedInitializer(initializer));
    }

    private static ChannelInitializer<Channel> createDelegatedInitializer(ChannelInitializer<Channel> delegate) {
        return new ChannelInitializer<>() {
            @Override
            protected void initChannel(Channel channel) {
                try {
                    mh$initChannel.invoke(delegate, channel);
                } catch (Throwable e) {
                    throw new RuntimeException(e);
                }
                plugin.getNetworkProcessor().inject(channel);
            }
        };
    }
}
