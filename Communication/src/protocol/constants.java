package protocol;

public class constants {

	//XST statics
	public static final int XST_SUCCESS = 0;
	public static final int XST_NO_DATA = 13;
	public static final int XST_FAILURE = -1;
	
	//Header, Data and Package size
	public static final int HEADER_SIZE = 4;
	public static final int DATA_SIZE = 28;
	public static final int PACKAGE_SIZE = 32;
	
	//Header positions
	public static final int ID_POS = 0;
	public static final int CRC_POS = 1;
	public static final int DATA_POS = 2;
	public static final int FLAGS_POS = 3;
	
	//Receive Buffer
	public byte RecvBuffer[] = new byte[PACKAGE_SIZE];
	
	//ACK , NACK
	public static final int ACK = 1;
	public static final int NACK = 0;
	public static final int SET = 1;
	
	public static final byte INIT_CRC = 0x00;
	
	public static final int MAX_TIMER = 5000; //need to be calculcated!!
	
	
	
}
