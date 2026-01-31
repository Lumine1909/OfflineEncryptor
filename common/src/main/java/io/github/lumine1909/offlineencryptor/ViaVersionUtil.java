package io.github.lumine1909.offlineencryptor;

import com.viaversion.viaversion.api.connection.ProtocolInfo;
import com.viaversion.viaversion.api.connection.UserConnection;
import io.github.lumine1909.reflexion.Field;
import io.netty.channel.Channel;

import java.util.Optional;

public interface ViaVersionUtil {

    static ViaVersionUtil create(boolean isVelocity, boolean hasVia) {
        if (!hasVia) {
            return new Dummy();
        }
        if (isVelocity) {
            return new Velocity();
        }
        return new Paper();
    }

    @SuppressWarnings("deprecation")
    default int getProtocolVersion(Channel channel) {
        return getConnection(channel)
            .map(UserConnection::getProtocolInfo)
            .map(ProtocolInfo::getProtocolVersion)
            .orElse(-1);
    }

    Optional<UserConnection> getConnection(Channel channel);

    default boolean hasVia() {
        return true;
    }

    class Velocity implements ViaVersionUtil {

        private final Field<UserConnection> field$UserConnection = Field.of("com.viaversion.viaversion.velocity.handlers.VelocityEncodeHandler", "info", UserConnection.class);

        public Optional<UserConnection> getConnection(Channel channel) {
            Object viaEncoder = channel.pipeline().get("via-encoder");
            try {
                return Optional.of(field$UserConnection.get(viaEncoder));
            } catch (Exception e) {
                return Optional.empty();
            }
        }
    }

    class Paper implements ViaVersionUtil {

        private final Field<UserConnection> field$UserConnection = Field.of("com.viaversion.viaversion.bukkit.handlers.BukkitEncodeHandler", "connection", UserConnection.class);

        public Optional<UserConnection> getConnection(Channel channel) {
            Object viaEncoder = channel.pipeline().get("via-encoder");
            try {
                return Optional.of(field$UserConnection.getFast(viaEncoder));
            } catch (Exception e) {
                return Optional.empty();
            }
        }
    }

    class Dummy implements ViaVersionUtil {

        @Override
        public int getProtocolVersion(Channel channel) {
            return 0x66ccff; // A large enough number to enable encryption.
        }

        @Override
        public Optional<UserConnection> getConnection(Channel channel) {
            return Optional.empty();
        }

        @Override
        public boolean hasVia() {
            return false;
        }
    }
}