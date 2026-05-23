package io.github.lumine1909.offlineencryptor;

import com.viaversion.viaversion.api.connection.ProtocolInfo;
import com.viaversion.viaversion.api.connection.UserConnection;
import io.github.lumine1909.reflexion.Field;
import io.github.lumine1909.reflexion.exception.NotFoundException;
import io.netty.channel.Channel;

import java.util.Optional;

public interface ViaVersionUtil {

    static ViaVersionUtil create(boolean isVelocity, boolean hasVia) {
        if (!hasVia) {
            return new Dummy();
        }
        return isVelocity ? new Velocity() : new Paper();
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

        private static final Field<UserConnection> field$UserConnection;

        static {
            Field<UserConnection> field;
            try {
                field = Field.of("com.viaversion.viaversion.platform.ViaEncodeHandler", "connection");
            } catch (NotFoundException e) {
                field = Field.of("com.viaversion.viaversion.velocity.handlers.VelocityEncodeHandler", "info");
            }
            field$UserConnection = field;
        }

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

        private static final Field<UserConnection> field$UserConnection;

        static {
            Field<UserConnection> field;
            try {
                field = Field.of("com.viaversion.viaversion.platform.ViaEncodeHandler", "connection");
            } catch (NotFoundException e) {
                field = Field.of("com.viaversion.viaversion.bukkit.handlers.BukkitEncodeHandler", "connection");
            }
            field$UserConnection = field;
        }

        public Optional<UserConnection> getConnection(Channel channel) {
            Object viaEncoder = channel.pipeline().get("via-encoder");
            try {
                return Optional.of(field$UserConnection.get(viaEncoder));
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