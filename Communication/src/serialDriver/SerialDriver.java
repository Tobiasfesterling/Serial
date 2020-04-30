package serialDriver;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Set;
import java.util.TooManyListenersException;

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.UnsupportedCommOperationException;

public class SerialDriver {

	static SerialDriver comm = null;

	// contains the ports that will be found
	private Enumeration ports = null;

	// maps the port names to ComportIdentifier
	private HashMap<String, CommPortIdentifier> portMap = new HashMap<>();

	// Object that contains the opened port
	private CommPortIdentifier portIdentifier = null;
	private SerialPort serialPort = null;

	// Streams to send or receive data
	private BufferedInputStream inStream = null;
	private OutputStream out = null;

	// State of connection for GUI
	private boolean connected = false;

	private byte[] recvBuffer;

	private boolean serialDataAvailable = false;

	private String progName;

	private PrintStream logStream;

	// Timeout value for connecting with the port
	public final static int TIMEOUT = 2000;

	public static final int DEFAULT_BAUDRATE = 115200;
	
	/**
	 * Constructor of the Communicator class which cannot be called. It is only
	 * called from the "getInstance()"-method.
	 * 
	 * @param buffersize: The size in bytes of the receive buffer to
	 */
	private SerialDriver(int buffersize, String progName)
	{
		this.logStream = System.out;
		recvBuffer = new byte[buffersize];
		this.progName = progName;
	}

	/**
	 * Constructor of the Communicator class which cannot be called. It is only
	 * called from the "getInstance()"-method.
	 * 
	 * @param buffersize: The size in bytes of the receive buffer to
	 */
	private SerialDriver(int buffersize, String progName, PrintStream out) {
		this.logStream = out;
		recvBuffer = new byte[buffersize];
		this.progName = progName;
	}

	/**
	 * 
	 * @param buffersize
	 * @return
	 */
	public static SerialDriver getInstance(int buffersize, String progName) {
		if (comm == null)
			comm = new SerialDriver(buffersize, progName);
		return comm;
	}

	/**
	 * 
	 * @param buffersize
	 * @return
	 */
	public static SerialDriver getInstance(int buffersize, String progName, PrintStream out) {
		if (comm == null)
			comm = new SerialDriver(buffersize, progName, out);
		return comm;
	}

	/**
	 * 
	 * 
	 * @return
	 */
	public Set<String> identifySerialPorts() {
		ports = CommPortIdentifier.getPortIdentifiers();

		while (ports.hasMoreElements()) {
			CommPortIdentifier current = (CommPortIdentifier) ports.nextElement();

			if (current.getPortType() == CommPortIdentifier.PORT_SERIAL) {
				portMap.put(current.getName(), current);
			}
			logStream.println("Start: " + current.getName());
		}

		return portMap.keySet();
	}

	/**
	 * 
	 * @param port
	 */
	public void connect(String port)
	{
		connect(port, DEFAULT_BAUDRATE, SerialPort.DATABITS_8, SerialPort.STOPBITS_1,
				SerialPort.PARITY_NONE);
	}
	
	/**
	 * 
	 * @param port
	 * @param baudrate
	 */
	public void connect(String port, int baudrate)
	{
		connect(port, baudrate, SerialPort.DATABITS_8, SerialPort.STOPBITS_1,
					SerialPort.PARITY_NONE);
	}
	
	/**
	 * 
	 * @param port
	 * @param baudrate
	 * @param databits
	 * @param stopbits
	 * @param parity
	 */
	public void connect(String port, int baudrate, int databits, int stopbits, int parity) {
		connected = false;

		try {
			openSerialPort(port, baudrate, databits, stopbits, parity);
			setupListener();

			connected = initIOStreams();

			logStream.println("Connected sucsessfully to " + port);

		} catch (PortInUseException e) {
			String logText = "!!!No connection possible!!!\n" + port + " is already in use (" + e.getMessage() + ")";
			System.err.println(logText);

		} catch (Exception e) {
			String logText = "!!!No connection possible!!!\n" + port + ": unknown Error exists (" + e.getMessage()
					+ ")";
			System.err.println(logText);
		}
	}
	
	/**
	 * 
	 * @param portname
	 * @param baudrate
	 * @param databits
	 * @param stopbits
	 * @param parity
	 * 
	 * @throws UnsupportedCommOperationException
	 * @throws PortInUseException
	 */
	private void openSerialPort(String portname, int baudrate, int databits, 
				int stopbits, int parity) throws UnsupportedCommOperationException, PortInUseException
	{
		// get selected port
		portIdentifier = (CommPortIdentifier) portMap.get(portname);

		if (portIdentifier == null)
			throw new NullPointerException("No Port with such name!");

		CommPort commPort = null;
		
		commPort = portIdentifier.open(progName, TIMEOUT);
			
		serialPort = (SerialPort) commPort;

		serialPort.setSerialPortParams(115200, databits, stopbits, parity);
	}

	/**
	 * 
	 * @param interrupt
	 * @throws TooManyListenersException
	 */
	private void setupListener() throws TooManyListenersException {

		serialPort.addEventListener(new EventListener(this) {
			@Override
			public void dataAvailable() {
				System.out.println("ReadFromPort");
				readBytes(recvBuffer.length);
			}
		});
		serialPort.notifyOnDataAvailable(true);
	}

	/**
	 * 
	 * @return
	 */
	private boolean initIOStreams() {
		// return value if IOstreams opened successfully or not

		try {
			// get IStream from port
			inStream = new BufferedInputStream(serialPort.getInputStream());

			// get OStream from port
			out = serialPort.getOutputStream();

			System.out.println("Streams opened successfully");

		} catch (IOException e) {

			System.err.println(e.toString());
			return false;
		}

		return true;
	}

	
	/**
	 * 
	 * Reading incoming bytes with the length as parameter.
	 * 
	 * 
	 * @param	byteCount: the count of bytes which will be received.
	 * 
	 * @return	boolean value if an error occurs or not.
	 */
	private boolean readBytes(int byteCount) {

		if (!connected)
			return false;

		int data = -1, byteCounter = 0, available = 0;

		try {
			if ((available = inStream.available()) > 0) 
			{
				while ((data = inStream.read()) != -1 )
				{
					if(data == -1)
					{
						break;
					}
					available = inStream.available();
					synchronized (recvBuffer) 
					{	
						recvBuffer[byteCounter++] = (byte) data;
						//System.out.println((char) data + " -> " + available + " <- " + byteCounter);
					}
					
					if(byteCounter == byteCount)
					{
						System.out.println("break2");
						break;
					}
				}
				
				if (data == -1)
					System.err.println("Failure by receiving data!!");
			} 
			else 
				return false;
		} 
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		serialDataAvailable = true;
		return true;
	}

	/**
	 * 
	 * This method is used to send a byte buffer over the serial port.
	 * 
	 * 
	 * @param	sendBuffer: The buffer to transmit over the serial port.
	 * 
	 * @return	boolean value if an error occurs or not.
	 */
	public boolean writeToPort(byte[] sendBuffer) {
		if (!connected)
			return false;

		for (int i = 0; i < sendBuffer.length; i++) {
			try {
				out.write(sendBuffer[i]);
			} catch (IOException e) {

				e.printStackTrace();
				return false;
			}
		}

		try {
			out.flush();
		} catch (IOException e) {
			// TODO: handle exception
		}

		return true;
	}

	/**
	 * This method is used
	 * @return
	 */
	public boolean isConnected() {
		return connected;
	}

	/**
	 * 
	 * @return
	 */
	public byte[] getRecvBuffer() {
		synchronized (recvBuffer) {
			serialDataAvailable = false;
			return recvBuffer;
		}
	}

	/**
	 * 
	 * @return
	 */
	public synchronized boolean isSerialDataAvailable() {
		return serialDataAvailable;
	}

	/**
	 * 
	 * @return
	 */
	public boolean disconnect() {
		try {
			serialPort.close();
			inStream.close();
			out.close();
			connected = false;
		} catch (Exception e) {
			return false;
		}

		return true;
	}
}
