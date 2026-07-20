package io.github.lumine1909.offlineencryptor.compat;

import com.github.games647.fastlogin.bukkit.FastLoginBukkit;
import com.velocitypowered.api.event.connection.PreLoginEvent;
import com.velocitypowered.proxy.VelocityServer;
import com.velocitypowered.proxy.connection.MinecraftConnection;
import com.velocitypowered.proxy.connection.client.LoginInboundConnection;
import io.github.lumine1909.reflexion.Field;
import io.github.lumine1909.reflexion.Method;
import io.github.lumine1909.reflexion.exception.NotFoundException;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.dreeam.leaf.event.AsyncPreAuthenticateEvent;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.List;
import java.util.UUID;
import java.util.function.BooleanSupplier;

public class AuthenticateCompats {

    public final List<AuthCompat> authCompats;
    private final BooleanSupplier disableByDefault;

    private AuthenticateCompats(BooleanSupplier disableByDefault, Object serverInstance) {
        this.disableByDefault = disableByDefault;
        this.authCompats = List.of(new LeafEvent(), new LOMCompat(), new FastLoginBukkitCompat(), new VelocityPreLoginCompat(serverInstance));
    }

    public static AuthenticateCompats create(BooleanSupplier disableByDefault) {
        return new AuthenticateCompats(disableByDefault, null);
    }

    public static AuthenticateCompats create(BooleanSupplier disableByDefault, Object serverInstance) {
        return new AuthenticateCompats(disableByDefault, serverInstance);
    }

    public boolean hasAuthenticate(String username, UUID uuid, SocketAddress socketAddress, Object... otherParams) {
        boolean hasEnabled = false;
        boolean hasAuth = false;
        for (AuthCompat authCompat : authCompats) {
            if (!authCompat.isEnable()) {
                continue;
            }
            hasEnabled = true;
            hasAuth |= authCompat.hasAuthentication(username, uuid, socketAddress, otherParams);
        }
        return hasEnabled ? hasAuth : disableByDefault.getAsBoolean();
    }

    public interface AuthCompat {

        boolean isEnable();

        boolean hasAuthentication(String username, UUID uuid, SocketAddress socketAddress, Object... otherParams);
    }

    static class LeafEvent implements AuthCompat {

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
        public boolean hasAuthentication(String username, UUID uuid, SocketAddress socketAddress, Object... otherParams) {
            return new AsyncPreAuthenticateEvent(username, uuid, socketAddress, !Bukkit.getOnlineMode()).callEvent();
        }
    }

    static class LOMCompat implements AuthCompat {

        private final boolean enable;
        private final Method<Boolean> method$isUserAllowed;

        LOMCompat() {
            Method<Boolean> method$isUserAllowed;
            boolean enable = true;
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
        public boolean hasAuthentication(String username, UUID uuid, SocketAddress socketAddress, Object... otherParams) {
            Plugin plugin = Bukkit.getPluginManager().getPlugin("LimitedOfflineMode");
            return plugin != null && plugin.isEnabled() && !method$isUserAllowed.invoke(plugin, username);
        }
    }

    static class FastLoginBukkitCompat implements AuthCompat {

        private final boolean enable;

        FastLoginBukkitCompat() {
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
        public boolean hasAuthentication(String username, UUID uuid, SocketAddress socketAddress, Object... otherParams) {
            if (Bukkit.getPluginManager().getPlugin("FastLogin") instanceof FastLoginBukkit plugin && plugin.isEnabled()) {
                return plugin.getSession((InetSocketAddress) socketAddress).getVerifyToken().length != 0;
            } else {
                return false;
            }
        }
    }

    static class VelocityPreLoginCompat implements AuthCompat {

        private final boolean enable;
        private final Object proxyServer;
        private final Field<?> field$inbound;

        VelocityPreLoginCompat(Object proxyServer) {
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
        public boolean hasAuthentication(String username, UUID uuid, SocketAddress socketAddress, Object... otherParams) {
            try {
                VelocityServer server = (VelocityServer) proxyServer;
                MinecraftConnection mcConnection = (MinecraftConnection) otherParams[0];
                LoginInboundConnection inbound = field$inbound.getUntyped(otherParams[1]);

                final PreLoginEvent event = new PreLoginEvent(inbound, username, uuid);
                server.getEventManager().fire(event).join();
                if (mcConnection.isClosed()) {
                    return true;
                }
                PreLoginEvent.PreLoginComponentResult result = event.getResult();
                return !result.isForceOfflineMode() && (server.getConfiguration().isOnlineMode() || result.isOnlineModeAllowed());
            } catch (Exception e) {
                e.printStackTrace();
            }
            return true; // Disable encryption for safety
        }
    }
}