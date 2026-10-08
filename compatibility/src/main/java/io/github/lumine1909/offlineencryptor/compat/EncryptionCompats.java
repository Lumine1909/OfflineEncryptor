package io.github.lumine1909.offlineencryptor.compat;

import com.github.games647.fastlogin.bukkit.FastLoginBukkit;
import com.velocitypowered.api.event.connection.PreLoginEvent;
import com.velocitypowered.api.proxy.InboundConnection;
import com.velocitypowered.proxy.VelocityServer;
import com.velocitypowered.proxy.connection.MinecraftConnection;
import fr.xephi.authme.AuthMe;
import fr.xephi.authme.service.PremiumLoginVerifier;
import io.github.lumine1909.reflexion.Field;
import io.github.lumine1909.reflexion.Method;
import io.github.lumine1909.reflexion.exception.NotFoundException;
import io.netty.channel.Channel;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.dreeam.leaf.event.AsyncPreAuthenticateEvent;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BooleanSupplier;

public class EncryptionCompats {

    public final List<EncryptionCompat> encryptionCompats;
    private final BooleanSupplier disableByDefault;

    private EncryptionCompats(BooleanSupplier disableByDefault, Object serverInstance) {
        this.disableByDefault = disableByDefault;
        this.encryptionCompats = List.of(new DisableWithProxy(), new ChannelHasEncryptor(), new LeafEvent(), new LOM(), new FastLogin(), new AuthMePremium(), new VelocityEvent(serverInstance));
    }

    public static EncryptionCompats create(BooleanSupplier disableByDefault) {
        return new EncryptionCompats(disableByDefault, null);
    }

    public static EncryptionCompats create(BooleanSupplier disableByDefault, Object serverInstance) {
        return new EncryptionCompats(disableByDefault, serverInstance);
    }

    public boolean hasEncryption(String username, UUID uuid, SocketAddress socketAddress, Channel channel, Object... otherParams) {
        try {
            for (EncryptionCompat encryptionCompat : encryptionCompats) {
                if (!encryptionCompat.isEnable()) {
                    continue;
                }
                if (encryptionCompat.hasEncryption(username, uuid, socketAddress, channel, otherParams)) {
                    return true;
                }
            }
            return disableByDefault.getAsBoolean();
        } catch (Throwable t) {
            t.printStackTrace(); // What's happened?
            return true; // Disable encryption for safety
        }
    }

    public interface EncryptionCompat {

        boolean isEnable();

        boolean hasEncryption(String username, UUID uuid, SocketAddress socketAddress, Channel channel, Object... otherParams);
    }

    static class DisableWithProxy implements EncryptionCompat {

        private final boolean enable;

        DisableWithProxy() {
            boolean enable;
            try {
                Class.forName("org.bukkit.Bukkit");
                enable = true;
            } catch (ClassNotFoundException e) {
                enable = false;
            }
            this.enable = enable;
        }

        @Override
        public boolean isEnable() {
            return enable;
        }

        @Override
        public boolean hasEncryption(String username, UUID uuid, SocketAddress socketAddress, Channel channel, Object... otherParams) {
            return Bukkit.getServerConfig().isProxyEnabled();
        }
    }

    static class ChannelHasEncryptor implements EncryptionCompat {

        @Override
        public boolean isEnable() {
            return true;
        }

        @Override
        public boolean hasEncryption(String username, UUID uuid, SocketAddress socketAddress, Channel channel, Object... otherParams) {
            return channel.pipeline().get("encrypt") != null || channel.pipeline().get("cipher-encoder") != null;
        }
    }

    static class LeafEvent implements EncryptionCompat {

        private final boolean enable;

        LeafEvent() {
            boolean enable;
            try {
                Class.forName("org.dreeam.leaf.event.AsyncPreAuthenticateEvent");
                enable = true;
            } catch (ClassNotFoundException e) {
                enable = false;
            }
            this.enable = enable;
        }

        @Override
        public boolean isEnable() {
            return enable;
        }

        // Awful but I have to do that
        @Override
        public boolean hasEncryption(String username, UUID uuid, SocketAddress socketAddress, Channel channel, Object... otherParams) {
            return new AsyncPreAuthenticateEvent(username, uuid, socketAddress, !Bukkit.getOnlineMode()).callEvent();
        }
    }

    static class LOM implements EncryptionCompat {

        private final boolean enable;
        private final Method<Boolean> method$isUserAllowed;

        LOM() {
            boolean enable = true;
            Method<Boolean> method$isUserAllowed;
            try {
                method$isUserAllowed = Method.of("de.moritxius.limitedofflinemode.LimitedOfflineModePaper", "isUserAllowed", boolean.class, String.class);
            } catch (NotFoundException e) {
                enable = false;
                method$isUserAllowed = null;
            }
            this.method$isUserAllowed = method$isUserAllowed;
            this.enable = enable;
        }

        @Override
        public boolean isEnable() {
            // Thank you dynamic unloading
            return enable && Bukkit.getPluginManager().getPlugin("LimitedOfflineMode") != null;
        }

        @Override
        public boolean hasEncryption(String username, UUID uuid, SocketAddress socketAddress, Channel channel, Object... otherParams) {
            Plugin plugin = Bukkit.getPluginManager().getPlugin("LimitedOfflineMode");
            return plugin != null && plugin.isEnabled() && !method$isUserAllowed.invoke(plugin, username);
        }
    }

    static class FastLogin implements EncryptionCompat {

        private final boolean enable;

        FastLogin() {
            boolean enable = true;
            try {
                Class.forName("com.github.games647.fastlogin.bukkit.FastLoginBukkit");
            } catch (ClassNotFoundException e) {
                enable = false;
            }
            this.enable = enable;
        }

        @Override
        public boolean isEnable() {
            return enable;
        }

        @Override
        public boolean hasEncryption(String username, UUID uuid, SocketAddress socketAddress, Channel channel, Object... otherParams) {
            if (Bukkit.getPluginManager().getPlugin("FastLogin") instanceof FastLoginBukkit plugin && plugin.isEnabled()) {
                return plugin.getSession((InetSocketAddress) socketAddress).getVerifyToken().length != 0;
            } else {
                return false;
            }
        }
    }

    static class AuthMePremium implements EncryptionCompat {

        private static final Field<?> field$injector = Field.of("fr.xephi.authme.AuthMe", "injector", 1);
        private static final Method<?> method$getSingleton = Method.of("ch.jalu.injector.Injector", "getSingleton", 1, Object.class, Class.class);

        private final boolean enable;
        private final Field<ConcurrentHashMap<String, ?>> field$verified;

        AuthMePremium() {
            boolean enable = true;
            Field<ConcurrentHashMap<String, ?>> field$verified;
            try {
                field$verified = Field.of("fr.xephi.authme.service.PremiumLoginVerifier", "verified");
            } catch (NotFoundException e) {
                enable = false;
                field$verified = null;
            }
            this.enable = enable;
            this.field$verified = field$verified;
        }

        @Override
        public boolean isEnable() {
            return enable;
        }

        @Override
        public boolean hasEncryption(String username, UUID uuid, SocketAddress socketAddress, Channel channel, Object... otherParams) {
            if (Bukkit.getPluginManager().getPlugin("AuthMe") instanceof AuthMe plugin && plugin.isEnabled()) {
                return field$verified.get(method$getSingleton.invoke(field$injector.get(plugin), PremiumLoginVerifier.class)).containsKey(username.toLowerCase(Locale.ROOT));
            } else {
                return false;
            }
        }
    }

    static class VelocityEvent implements EncryptionCompat {

        private final boolean enable;
        private final Object proxyServer;
        private final Field<?> field$inbound;

        VelocityEvent(Object proxyServer) {
            Field<?> field$inbound;
            boolean enable = true;
            try {
                field$inbound = Field.of("com.velocitypowered.proxy.connection.client.InitialLoginSessionHandler", "inbound");
            } catch (NotFoundException e) {
                enable = false;
                field$inbound = null;
            }
            this.field$inbound = field$inbound;
            this.enable = enable;
            this.proxyServer = proxyServer;
        }

        @Override
        public boolean isEnable() {
            return enable;
        }

        // Awful but I have to do that
        @Override
        public boolean hasEncryption(String username, UUID uuid, SocketAddress socketAddress, Channel channel, Object... otherParams) {
            VelocityServer server = (VelocityServer) proxyServer;
            MinecraftConnection mcConnection = (MinecraftConnection) otherParams[0];
            Object inbound = field$inbound.get(otherParams[1]);

            final PreLoginEvent event = new PreLoginEvent((InboundConnection) inbound, username, uuid);
            server.getEventManager().fire(event).join();
            if (mcConnection.isClosed()) {
                return true;
            }
            PreLoginEvent.PreLoginComponentResult result = event.getResult();
            return !result.isForceOfflineMode() && (server.getConfiguration().isOnlineMode() || result.isOnlineModeAllowed());
        }
    }
}