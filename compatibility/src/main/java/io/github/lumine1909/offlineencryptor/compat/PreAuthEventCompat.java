package io.github.lumine1909.offlineencryptor.compat;

import org.dreeam.leaf.event.AsyncPreAuthenticateEvent;

import java.net.SocketAddress;
import java.util.UUID;

public interface PreAuthEventCompat {

    static PreAuthEventCompat create() {
        try {
            Class.forName("org.dreeam.leaf.event.AsyncPreAuthenticateEvent");
            return new Enable();
        } catch (ClassNotFoundException e) {
            return new Disable();
        }
    }

    boolean checkForPreAuthEvent(String username, UUID uuid, SocketAddress socketAddress);

    class Enable implements PreAuthEventCompat {

        @Override
        public boolean checkForPreAuthEvent(String username, UUID uuid, SocketAddress socketAddress) {
            return new AsyncPreAuthenticateEvent(username, uuid, socketAddress, true).callEvent();
        }
    }

    class Disable implements PreAuthEventCompat {

        @Override
        public boolean checkForPreAuthEvent(String username, UUID uuid, SocketAddress socketAddress) {
            return false;
        }
    }
}