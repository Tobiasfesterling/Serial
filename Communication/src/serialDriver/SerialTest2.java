package serialDriver;

import com.fazecast.jSerialComm.*;
import java.util.*;
import java.io.*;

public class SerialTest2 {

	public static SerialPort userPort;
	static InputStream in;

	public static void main(String[] args) throws IOException {

		Scanner input = new Scanner(System.in);
		/*
		 * This returns an array of commport addresses, not useful for the client but
		 * useful for iterating through to get an actual list of com parts available
		 */
		SerialPort ports[] = SerialPort.getCommPorts();
		int i = 1;
		// User port selection
		System.out.println("COM Ports available on machine");
		for (SerialPort port : ports) {
			// iterator to pass through port array
			System.out.println(i++ + ": " + port.getSystemPortName()); // print windows com ports
		}
		System.out.println("Please select COM PORT: 'COM#'");
		SerialPort userPort = SerialPort.getCommPort(input.nextLine());
		userPort.setBaudRate(115200);

		// Initializing port
		userPort.openPort();

		userPort.addDataListener(new SerialPortDataListener() {
			@Override
			public int getListeningEvents() {
				return SerialPort.LISTENING_EVENT_DATA_AVAILABLE;
			}

			public void serialEvent(SerialPortEvent event) {
				if (event.getEventType() != SerialPort.LISTENING_EVENT_DATA_AVAILABLE)
					return;
				byte[] newData = new byte[32];
				int numRead = userPort.readBytes(newData, newData.length);
				System.out.println("Read " + numRead + " bytes.");
				for(byte b: newData)
					System.out.print((char) b);
			}
		});
		if (userPort.isOpen()) {
			System.out.println("Port initialized!");

			while (true) {
				for (byte b : (input.nextLine() + "/n").getBytes())
					userPort.getOutputStream().write(b);
				userPort.getOutputStream().flush();
			}

			// timeout not needed for event based reading
			// userPort.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, 100, 0);
		} else {
			System.out.println("Port not available");
			return;
		}

	}
}
