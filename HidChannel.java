import io.netty.channel.ChannelConfig;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelPromise;
import io.netty.channel.DefaultChannelConfig;
import io.netty.channel.oio.OioByteStreamChannel;
import java.net.SocketAddress;
import java.util.concurrent.atomic.AtomicBoolean;

@SuppressWarnings("deprecation")
public class HidChannel extends OioByteStreamChannel {
    private AtomicBoolean isOpen = new AtomicBoolean(true);
    private HidDeviceAddress deviceAddress;
    private HidDevice device;

    public HidChannel() {
        super(null);
    }


    @Override
    public boolean isOpen() {
        return isOpen.get();
    }

    @Override
    protected AbstractUnsafe newUnsafe() {
        return new JSCUnsafe();
    }

    @Override
    protected void doConnect(SocketAddress remoteAddress, SocketAddress localAddress) throws Exception {
        deviceAddress = (HidDeviceAddress) remoteAddress;
        device = new HidDevice().open(deviceAddress);
    }

    protected void doInit() throws Exception {
        activate(device.getInputStream(), device.getOutputStream());
    }

    @Override
    protected void doDisconnect() throws Exception {
        doClose();
    }

    @Override
    protected void doClose() throws Exception {
        isOpen.set(false);
        try {
           super.doClose();
        } finally {
            if (device != null) {
                device.close();
                device = null;
            }
        }
    }

    @Override
    protected boolean isInputShutdown() {
        return !isOpen.get();
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
    
    @Override
    public HidDeviceAddress localAddress() {
        return deviceAddress;
    }

    @Override
    public HidDeviceAddress remoteAddress() {
        return deviceAddress;
    }

    @Override
    protected HidDeviceAddress localAddress0() {
        return deviceAddress;
    }

    @Override
    protected HidDeviceAddress remoteAddress0() {
        return deviceAddress;
    }
    
    @Override
    public ChannelConfig config() {
        return new DefaultChannelConfig(this);
    }
    
    @Override
    protected ChannelFuture shutdownInput() {
        return newFailedFuture(new UnsupportedOperationException("shutdownInput"));
    }
    
    @Override
    protected void doBind(SocketAddress localAddress) throws Exception {
        throw new UnsupportedOperationException();
    }
}
