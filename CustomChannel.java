import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelPromise;
import io.netty.channel.oio.OioByteStreamChannel;

import java.io.IOException;
import java.net.SocketAddress;

@SuppressWarnings("deprecation")
public class CustomChannel extends OioByteStreamChannel {

    private static final CustomDeviceAddress LOCAL_ADDRESS = new CustomDeviceAddress("localhost");

    private final CustomChannelConfig config;

    private boolean open = true;
    private CustomDeviceAddress deviceAddress;
    private MockSerialPort serialPort;

    public CustomChannel() {
        super(null);
        config = new DefaultCustomChannelConfig(this);
    }

    @Override
    public CustomChannelConfig config() {
        return config;
    }

    @Override
    public boolean isOpen() {
        return open;
    }

    @Override
    protected AbstractUnsafe newUnsafe() {
        return new JSCUnsafe();
    }

    @Override
    protected void doConnect(SocketAddress remoteAddress, SocketAddress localAddress) throws Exception {
        CustomDeviceAddress remote = (CustomDeviceAddress) remoteAddress;
        MockSerialPort commPort = new MockSerialPort();
        if (!commPort.openPort()) {
            throw new IOException("Could not open port: " + remote.value());
        }

        deviceAddress = remote;
        serialPort = commPort;
    }

    protected void doInit() throws Exception {
        activate(serialPort.getInputStream(), serialPort.getOutputStream());
    }

    @Override
    public CustomDeviceAddress localAddress() {
        return (CustomDeviceAddress) super.localAddress();
    }

    @Override
    public CustomDeviceAddress remoteAddress() {
        return (CustomDeviceAddress) super.remoteAddress();
    }

    @Override
    protected CustomDeviceAddress localAddress0() {
        return LOCAL_ADDRESS;
    }

    @Override
    protected CustomDeviceAddress remoteAddress0() {
        return deviceAddress;
    }

    @Override
    protected void doBind(SocketAddress localAddress) throws Exception {
        throw new UnsupportedOperationException();
    }

    @Override
    protected void doDisconnect() throws Exception {
        doClose();
    }

    @Override
    protected void doClose() throws Exception {
        open = false;
        try {
           super.doClose();
        } finally {
            if (serialPort != null) {
                serialPort.closePort();
                serialPort = null;
            }
        }
    }

    @Override
    protected boolean isInputShutdown() {
        return !open;
    }

    @Override
    protected ChannelFuture shutdownInput() {
        return newFailedFuture(new UnsupportedOperationException("shutdownInput"));
    }


    private final class JSCUnsafe extends AbstractUnsafe {
        @Override
        public void connect(
                final SocketAddress remoteAddress,
                final SocketAddress localAddress, final ChannelPromise promise) {
            if (!promise.setUncancellable() || !isOpen()) {
                return;
            }

            try {
                final boolean wasActive = isActive();
                doConnect(remoteAddress, localAddress);

                doInit();
                safeSetSuccess(promise);
                if (!wasActive && isActive()) {
                    pipeline().fireChannelActive();
                }
                
            } catch (Throwable t) {
                safeSetFailure(promise, t);
                closeIfClosed();
            }
        }
    }
}
