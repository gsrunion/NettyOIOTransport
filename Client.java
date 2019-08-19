import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.oio.OioEventLoopGroup;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

/**
 * Sends one message to a serial device
 */
public final class Client {
    @SuppressWarnings("deprecation")
	public static void main(String[] args) throws Exception {
        EventLoopGroup group = new OioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap();
            b.group(group)
             .channel(HidChannel.class)
             .handler(new ChannelInitializer<HidChannel>() {
                 @Override
                 public void initChannel(HidChannel ch) throws Exception {
                     ch.pipeline().addLast(
                         new LoggingHandler(LogLevel.ERROR),
                         new LineBasedFrameDecoder(128),
                         new StringEncoder(),
                         new StringDecoder(),
                         new Handler()
                     );
                 }
             });

            ChannelFuture f = b.connect(new HidDeviceAddress(0x01, 0x02)).sync();

            f.channel().closeFuture().sync();
        } finally {
            group.shutdownGracefully();
        }
    }
}
