import java.net.SocketAddress;

public class HidDeviceAddress extends SocketAddress {
    private static final long serialVersionUID = -2907820090993709523L;

    private final int pid;
    private final int vid;
    
	public HidDeviceAddress(int pid, int vid) {
		super();
		this.pid = pid;
		this.vid = vid;
	}

	public int getPid() {
		return pid;
	}

	public int getVid() {
		return vid;
	}

	@Override
	public String toString() {
		return "HidDeviceAddress [pid=" + pid + ", vid=" + vid + "]";
	}
}
