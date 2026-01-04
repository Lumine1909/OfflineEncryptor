package io.github.lumine1909.offlineencryptor.paper;

import io.github.lumine1909.offlineencryptor.NetworkProcessor;
import io.github.lumine1909.offlineencryptor.PacketInterceptor;
import io.github.lumine1909.offlineencryptor.ViaVersionUtil;
import io.github.lumine1909.reflexion.Field;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.handshake.ClientIntentionPacket;
import net.minecraft.network.protocol.login.ClientboundHelloPacket;
import net.minecraft.network.protocol.login.ServerboundHelloPacket;
import net.minecraft.network.protocol.login.ServerboundKeyPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerLoginPacketListenerImpl;
import net.minecraft.util.CryptException;
import org.bukkit.Bukkit;

import javax.crypto.SecretKey;
import java.security.PrivateKey;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

public class PaperPacketInterceptor extends PacketInterceptor<ClientIntentionPacket, ServerboundHelloPacket, ServerboundKeyPacket> {

    private static final Field<byte[]> field$challenge = Field.of(ServerLoginPacketListenerImpl.class, "challenge", byte[].class);
    private static final Function<ServerLoginPacketListenerImpl, ClientboundHelloPacket> HELLO_PACKET_FACTORY = listener ->
        new ClientboundHelloPacket("", MinecraftServer.getServer().getKeyPair().getPublic().getEncoded(), field$challenge.getFast(listener), false);
    private static final MinecraftServer server = MinecraftServer.getServer();
    private static final ViaVersionUtil viaUtil = ViaVersionUtil.create(false, Bukkit.getPluginManager().getPlugin("ViaVersion") != null);

    private final Connection connection;

    protected PaperPacketInterceptor(Channel channel, NetworkProcessor<ServerboundHelloPacket> processor) {
        super(channel, processor);
        connection = (Connection) channel.pipeline().get("packet_handler");
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (!enabled) {
            super.channelRead(ctx, msg);
            return;
        }
        if (msg instanceof ClientIntentionPacket packet) {
            processC2SHandshake(ctx, packet);
        }
        if (msg instanceof ServerboundHelloPacket packet) {
            if (!validateVersion(viaUtil.getProtocolVersion(channel))) {
                super.channelRead(ctx, msg);
                return;
            }
            processC2SHello(ctx, packet);
        } else if (msg instanceof ServerboundKeyPacket packet) {
            processC2SResponse(ctx, packet);
        } else {
            super.channelRead(ctx, msg);
        }
    }

    @Override
    protected void processC2SHandshake(ChannelHandlerContext ctx, ClientIntentionPacket packet) {
        if (!viaUtil.hasVia()) {
            validateVersion(packet.protocolVersion());
        }
    }

    @Override
    protected void processC2SHello(ChannelHandlerContext ctx, ServerboundHelloPacket packet) {
        this.username = packet.name();
        ServerLoginPacketListenerImpl login = (ServerLoginPacketListenerImpl) connection.getPacketListener();
        processor.getCache().put(username, packet);
        connection.send(HELLO_PACKET_FACTORY.apply(login));
    }

    @Override
    protected void processC2SResponse(ChannelHandlerContext ctx, ServerboundKeyPacket packet) {
        ServerLoginPacketListenerImpl login = (ServerLoginPacketListenerImpl) connection.getPacketListener();
        try {
            PrivateKey privateKey = server.getKeyPair().getPrivate();
            if (!packet.isChallengeValid(field$challenge.getFast(login), privateKey)) {
                throw new IllegalStateException("Protocol error");
            }
            SecretKey secretKey = packet.getSecretKey(privateKey);
            this.connection.setEncryptionKey(secretKey);
            channel.eventLoop().schedule(() -> {
                processor.uninject(channel);
                ctx.fireChannelRead(processor.getCache().remove(username));
            }, 500, TimeUnit.MILLISECONDS); // Let you know you are using encryption :)
        } catch (CryptException e) {
            throw new IllegalStateException("Protocol error", e);
        }
    }
}