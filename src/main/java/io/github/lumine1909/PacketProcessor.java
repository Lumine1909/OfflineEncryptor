package io.github.lumine1909;

import io.github.lumine1909.reflexion.Field;
import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.login.ClientboundHelloPacket;
import net.minecraft.network.protocol.login.ServerboundHelloPacket;
import net.minecraft.network.protocol.login.ServerboundKeyPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerLoginPacketListenerImpl;
import net.minecraft.util.CryptException;

import javax.crypto.SecretKey;
import java.security.PrivateKey;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

public class PacketProcessor {

    private static final Field<byte[]> field$challenge = Field.of(ServerLoginPacketListenerImpl.class, "challenge", byte[].class);
    private static final Function<ServerLoginPacketListenerImpl, ClientboundHelloPacket> HELLO_PACKET_FACTORY = listener ->
        new ClientboundHelloPacket("", MinecraftServer.getServer().getKeyPair().getPublic().getEncoded(), field$challenge.get(listener), false);
    private static final Map<String, ServerboundHelloPacket> SERVERBOUND_HELLO_CACHE = new HashMap<>();
    private static final MinecraftServer server = MinecraftServer.getServer();

    public void inject(Channel channel) {
        channel.pipeline().addBefore("packet_handler", "oe_handler", new Interceptor(channel));
    }

    public void uninject(Channel channel) {
        if (channel.pipeline().get("oe_handler") != null) {
            channel.pipeline().remove("oe_handler");
        }
    }

    public void invalidateCache(String name) {
        SERVERBOUND_HELLO_CACHE.remove(name);
    }

    class Interceptor extends ChannelDuplexHandler {

        private final Channel channel;
        private final Connection connection;
        private String name;

        public Interceptor(Channel channel) {
            this.channel = channel;
            connection = (Connection) channel.pipeline().get("packet_handler");
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            if (msg instanceof ServerboundHelloPacket packet) {
                this.name = packet.name();
                ServerLoginPacketListenerImpl login = (ServerLoginPacketListenerImpl) connection.getPacketListener();
                SERVERBOUND_HELLO_CACHE.put(name, packet);
                connection.send(HELLO_PACKET_FACTORY.apply(login));
                return;
            } else if (msg instanceof ServerboundKeyPacket packet) {
                ServerLoginPacketListenerImpl login = (ServerLoginPacketListenerImpl) connection.getPacketListener();
                try {
                    PrivateKey privateKey = server.getKeyPair().getPrivate();
                    if (!packet.isChallengeValid(field$challenge.get(login), privateKey)) {
                        throw new IllegalStateException("Protocol error");
                    }

                    SecretKey secretKey = packet.getSecretKey(privateKey);
                    this.connection.setEncryptionKey(secretKey);
                    channel.eventLoop().schedule(() -> {
                        uninject(channel);
                        ctx.fireChannelRead(SERVERBOUND_HELLO_CACHE.remove(name));
                    }, 500, TimeUnit.MILLISECONDS); // Let you know you are using encryption : )
                } catch (CryptException var7) {
                    throw new IllegalStateException("Protocol error", var7);
                }
                return;
            }
            super.channelRead(ctx, msg);
        }
    }
}
