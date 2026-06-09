package io.github.lumine1909.offlineencryptor;

import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;

import java.util.function.BooleanSupplier;

public abstract class PacketInterceptor<C2SHandshake, C2SHello, C2SResponse> extends ChannelDuplexHandler {

    private static final int PROTOCOL_1_20_5 = 766;

    protected final Channel channel;
    protected final NetworkProcessor<C2SHello> processor;

    protected boolean enabled = true;

    protected String username;

    protected PacketInterceptor(Channel channel, NetworkProcessor<C2SHello> processor) {
        this.channel = channel;
        this.processor = processor;
    }

    protected boolean validate(int protocolVersion) {
        return validate(protocolVersion, false);
    }

    protected boolean validate(int protocolVersion, boolean invalid) {
        if (protocolVersion < PROTOCOL_1_20_5 || invalid) {
            processor.uninject(channel);
            enabled = false;
            return false;
        }
        return true;
    }


    protected abstract void processC2SHandshake(ChannelHandlerContext ctx, C2SHandshake packet);

    protected abstract void processC2SHello(ChannelHandlerContext ctx, C2SHello packet);

    protected abstract void processC2SResponse(ChannelHandlerContext ctx, C2SResponse packet);
}