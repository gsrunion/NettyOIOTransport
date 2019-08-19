import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.ArrayBlockingQueue;

public class HidDevice {
	ArrayBlockingQueue<Byte> queue = new ArrayBlockingQueue<>(256);
		
	public InputStream getInputStream() {
		return new InputStream() {
			@Override
			public int read() throws IOException {
				try {
					return queue.take();
				} catch (InterruptedException e) {
					throw new IOException();
				}
			}
		};
	}

	public OutputStream getOutputStream() {
		return new OutputStream() {
			@Override
			public void write(int b) throws IOException {
				try {
					queue.put((byte) b);
				} catch(InterruptedException e) {
					throw new IOException();
				}
			}
		};
	}

	public void close() {
		System.out.println("closePort");
	}

	public HidDevice open(HidDeviceAddress address) throws IOException {
		System.out.printf("opening with %s\n", address);
		return this;
	}

}

