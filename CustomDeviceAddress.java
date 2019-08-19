import java.net.SocketAddress;

public class CustomDeviceAddress extends SocketAddress {
    private static final long serialVersionUID = -2907820090993709523L;

    private final String value;

    public CustomDeviceAddress(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }
}
