package rcvProtocol;

public class UART_EIBVE_Protocol_Recv {
	
	//XST statics
	public static final int XST_SUCCESS = 0;
	public static final int XST_NO_DATA = 13;
	public static final int XST_FAILURE = -1;
	
	//CRC
	public byte INIT_CRC = 0x00;
	public byte last_crc_send = INIT_CRC;
	public byte last_crc_rcv = INIT_CRC;
	public byte calc_crc = INIT_CRC;
	
	//flags
	public byte new_flags = 0x00;
	
	//connection ID
	public byte conn_id = 0x00;
	
	//Header and Data size
	public static final int HEADER_SIZE = 4;
	public static final int DATA_SIZE = 28;
	

	int UART_Recv_Data() {
		
		int status = XST_SUCCESS;
		
		if((status = recv_data) //empfangsmethode anpassen!!
				System.out.println("Error receiving data!!");
		
		if(status == XST_FAILURE)
			return XST_FAILURE;
		
		status = receive();
		
		if(status != XST_SUCCESS)
			return XST_FAILURE;
		
		return XST_SUCCESS;
	}
	
	int receive() {
		int status = XST_SUCCESS;
		
		//connection establishment
		status = connection_establishment();
		
		if(status == XST_FAILURE)
			return XST_FAILURE;
		
		//receive the TM/TCs
		status = receive_data();
		
		if(status == XST_FAILURE)
			return XST_FAILURE;
		
		return XST_SUCCESS;
	}
	
	int connection_establishment() {
		byte header[] = new byte[HEADER_SIZE];
		
		byte data[] = new byte[DATA_SIZE];
		return 0;
	}
	
	int receive_data() {
		return 0;
		
	}
	

	public byte getLast_crc_send() {
		return last_crc_send;
	}

	public void setLast_crc_send(byte last_crc_send) {
		this.last_crc_send = last_crc_send;
	}

	public byte getLast_crc_rcv() {
		return last_crc_rcv;
	}

	public void setLast_crc_rcv(byte last_crc_rcv) {
		this.last_crc_rcv = last_crc_rcv;
	}

	public byte getCalc_crc() {
		return calc_crc;
	}

	public void setCalc_crc(byte calc_crc) {
		this.calc_crc = calc_crc;
	}

	public byte getNew_flags() {
		return new_flags;
	}

	public void setNew_flags(byte new_flags) {
		this.new_flags = new_flags;
	}

	public byte getConn_id() {
		return conn_id;
	}

	public void setConn_id(byte conn_id) {
		this.conn_id = conn_id;
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
