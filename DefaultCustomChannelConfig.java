import io.netty.channel.DefaultChannelConfig;

final class DefaultCustomChannelConfig extends DefaultChannelConfig implements CustomChannelConfig {
    DefaultCustomChannelConfig(CustomChannel channel) {
        super(channel);
    }
}
