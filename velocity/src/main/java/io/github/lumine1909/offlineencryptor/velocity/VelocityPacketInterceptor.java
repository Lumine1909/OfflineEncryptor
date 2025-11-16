package io.github.lumine1909.offlineencryptor.velocity;

import com.velocitypowered.proxy.connection.MinecraftConnection;
import com.velocitypowered.proxy.network.Connections;
import com.velocitypowered.proxy.protocol.packet.EncryptionRequestPacket;
import com.velocitypowered.proxy.protocol.packet.EncryptionResponsePacket;
import com.velocitypowered.proxy.protocol.packet.HandshakePacket;
import com.velocitypowered.proxy.protocol.packet.ServerLoginPacket;
import io.github.lumine1909.offlineencryptor.NetworkProcessor;
import io.github.lumine1909.offlineencryptor.PacketInterceptor;
import io.github.lumine1909.offlineencryptor.ViaVersionUtil;
import io.github.lumine1909.reflexion.Field;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;

import java.security.KeyPair;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import static com.velocitypowered.proxy.crypto.EncryptionUtils.decryptRsa;
import static io.github.lumine1909.offlineencryptor.velocity.OfflineEncryptor.plugin;

public class VelocityPacketInterceptor extends PacketInterceptor<HandshakePacket, ServerLoginPacket, EncryptionResponsePacket> {

    private static final Field<Boolean> field$authenticate = Field.of(
        EncryptionRequestPacket.class, "shouldAuthenticate", boolean.class
    );

    private static final ViaVersionUtil viaUtil = ViaVersionUtil.create(true, plugin.getServer().getPluginManager().getPlugin("viaversion").isPresent());

    private final MinecraftConnection connection;
    private byte[] verify;

    protected VelocityPacketInterceptor(Channel channel, NetworkProcessor<ServerLoginPacket> processor) {
        super(channel, processor);
        connection = (MinecraftConnection) channel.pipeline().get(Connections.HANDLER);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (!enabled) {
            super.channelRead(ctx, msg);
            return;
        }
        if (msg instanceof HandshakePacket packet) {
            processC2SHandshake(ctx, packet);
        }
        if (msg instanceof ServerLoginPacket packet) {
            if (!validateVersion(viaUtil.getProtocolVersion(channel))) {
                super.channelRead(ctx, msg);
                return;
            }
            processC2SHello(ctx, packet);
        } else if (msg instanceof EncryptionResponsePacket packet) {
            processC2SResponse(ctx, packet);
        } else {
            super.channelRead(ctx, msg);
        }
    }

    @Override
    protected void processC2SHandshake(ChannelHandlerContext ctx, HandshakePacket packet) {
        if (!viaUtil.hasVia()) {
            validateVersion(packet.getProtocolVersion().getProtocol());
        }
    }

    @Override
    protected void processC2SHello(ChannelHandlerContext ctx, ServerLoginPacket packet) {
        this.username = packet.getUsername();
        processor.getCache().put(username, packet);
        EncryptionRequestPacket request = generateEncryptionRequest();
        verify = Arrays.copyOf(request.getVerifyToken(), 4);
        connection.write(request);
    }

    private EncryptionRequestPacket generateEncryptionRequest() {
        byte[] verify = new byte[4];
        ThreadLocalRandom.current().nextBytes(verify);

        EncryptionRequestPacket request = new EncryptionRequestPacket();
        request.setPublicKey(plugin.getServer().getServerKeyPair().getPublic().getEncoded());
        request.setVerifyToken(verify);
        field$authenticate.set(request, false);
        return request;
    }

    @Override
    protected void processC2SResponse(ChannelHandlerContext ctx, EncryptionResponsePacket packet) {
        try {
            KeyPair serverKeyPair = plugin.getServer().getServerKeyPair();
            byte[] decryptedVerifyToken = decryptRsa(serverKeyPair, packet.getVerifyToken());
            if (!MessageDigest.isEqual(verify, decryptedVerifyToken)) {
                throw new IllegalStateException("Unable to successfully decrypt the verification token.");
            }
            byte[] decryptedSharedSecret = decryptRsa(serverKeyPair, packet.getSharedSecret());
            connection.enableEncryption(decryptedSharedSecret);
            channel.eventLoop().schedule(() -> {
                processor.uninject(channel);
                ctx.fireChannelRead(processor.getCache().remove(username));
            }, 500, TimeUnit.MILLISECONDS); // Let you know you are using encryption :)
        } catch (Exception e) {
            throw new IllegalStateException("Protocol error", e);
        }
    }
}
