package io.github.lumine1909.offlineencryptor.compat;

import io.github.lumine1909.reflexion.Method;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.dreeam.leaf.event.AsyncPreAuthenticateEvent;

import java.net.SocketAddress;
import java.util.List;
import java.util.UUID;
import java.util.function.BooleanSupplier;

public class AuthenticateCompats {

    public static final List<AuthCompat> authCompats = List.of(new LeafEvent(), new LOMCompat());
    private final BooleanSupplier disableByDefault;

    private AuthenticateCompats(BooleanSupplier disableByDefault) {
        this.disableByDefault = disableByDefault;
    }

    public static AuthenticateCompats create(BooleanSupplier disableByDefault) {
        return new AuthenticateCompats(disableByDefault);
    }

    public boolean hasAuthenticate(String username, UUID uuid, SocketAddress socketAddress) {
        boolean hasEnabled = false;
        boolean hasAuth = false;
        for (AuthCompat authCompat : authCompats) {
            if (!authCompat.isEnable()) {
                continue;
            }
            hasEnabled = true;
            hasAuth |= authCompat.hasAuthenticate(username, uuid, socketAddress);
        }
        return hasEnabled ? hasAuth : disableByDefault.getAsBoolean();
    }

    public interface AuthCompat {

        boolean isEnable();

        boolean hasAuthenticate(String username, UUID uuid, SocketAddress socketAddress);
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

        @Override
        public boolean hasAuthenticate(String username, UUID uuid, SocketAddress socketAddress) {
            return new AsyncPreAuthenticateEvent(username, uuid, socketAddress, true).callEvent();
        }
    }

    static class LOMCompat implements AuthCompat {

        private final boolean enable;
        private Method<Boolean> method$isUserAllowed;

        public LOMCompat() {
            boolean enable = true;
            try {
                Class<?> clazz = Class.forName("de.moritxius.limitedofflinemode.LimitedOfflineModePaper");
                method$isUserAllowed = Method.of(clazz, "isUserAllowed", boolean.class, String.class);
            } catch (ClassNotFoundException e) {
                enable = false;
            }
            this.enable = enable;
        }

        @Override
        public boolean isEnable() {
            // Thank you dynamic unloading
            return enable && Bukkit.getPluginManager().getPlugin("LimitedOfflineMode") != null;
        }

        @Override
        public boolean hasAuthenticate(String username, UUID uuid, SocketAddress socketAddress) {
            Plugin plugin = Bukkit.getPluginManager().getPlugin("LimitedOfflineMode");
            return plugin != null && !method$isUserAllowed.invoke(plugin, username);
        }
    }
}