package io.github.lumine1909.offlineencryptor.paper;

import io.github.lumine1909.offlineencryptor.NetworkProcessor;
import io.netty.channel.Channel;
import net.minecraft.network.protocol.login.ServerboundHelloPacket;

public class PaperNetworkProcessor extends NetworkProcessor<ServerboundHelloPacket> {

    public void inject(Channel channel) {
        // Thank you LimitedOfflineMode
        if (channel.pipeline().get("lom_login_interceptor") != null) {
            channel.pipeline().addBefore("lom_login_interceptor", "oe_handler", new PaperPacketInterceptor(channel, this));
        } else {
            channel.pipeline().addBefore("packet_handler", "oe_handler", new PaperPacketInterceptor(channel, this));
        }
    }

    public void uninject(Channel channel) {
        if (channel.pipeline().get("oe_handler") != null) {
            channel.pipeline().remove("oe_handler");
        }
    }
}
