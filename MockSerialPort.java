import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ArrayBlockingQueue;

public class MockSerialPort {
	ArrayBlockingQueue<Byte> queue = new ArrayBlockingQueue<>(256);
		
	public InputStream getInputStream() {
		return new InputStream() {
			@Override
			public int read() throws IOException {
				try {
					byte b = queue.take();
					System.out.printf("reading %s in read()\n", new String(new byte[] { b }, StandardCharsets.ISO_8859_1));
					return b;
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
					System.out.printf("Writing %s in write()\n", new String(new byte[] { (byte)b }, StandardCharsets.ISO_8859_1));
					queue.put((byte) b);
				} catch(InterruptedException e) {
					throw new IOException();
				}
			}
		};
	}

	public void closePort() {
		System.out.println("closePort");
	}

	public boolean openPort() {
		System.out.println("openPort");
		return true;
	}

}

