package protocol;
//import com.sun.org.apache.bcel.internal.Const;

import protocol.Constants;
import protocol.flags.UART_EIVE_Protocol_Flags;

public class UART_EIVE_Protocol_Recv {
	
	//CRC
	public byte last_crc_send = Constants.INIT_CRC;
	public byte last_crc_rcv = Constants.INIT_CRC;
	public byte calc_crc = Constants.INIT_CRC;
	
	//flags
	public byte new_flags = 0x00;
	
	//connection ID
	public byte conn_id = 0x00;
	
	//Long buffer for receiving data
	public static byte databuffer[] = new byte[573483];
	
	public byte header[] = new byte[Constants.HEADER_SIZE];
	public byte data[] = new byte[Constants.PACKAGE_DATA_SIZE];

	public static int UART_Recv_Data() {
		
		int status = Constants.XST_SUCCESS;
		
		if((status = recv_data) //empfangsmethode anpassen, define method
				System.out.println("Error receiving data!!");
		
		if(status == Constants.XST_FAILURE)
			return Constants.XST_FAILURE;
		
		status = receive();
		
		if(status != Constants.XST_SUCCESS)
			return Constants.XST_FAILURE;
		
		return Constants.XST_SUCCESS;
	}
	
	public static int receive() {
		int status = Constants.XST_SUCCESS;
		
		//connection establishment
		status = connection_establishment();
		
		if(status == Constants.XST_FAILURE)
			return Constants.XST_FAILURE;
		
		//receive the TM/TCs
		status = receive_data(); //define method!!
		
		if(status == Constants.XST_FAILURE)
			return Constants.XST_FAILURE;
		
		return Constants.XST_SUCCESS;
	}
	
	public int connection_establishment() {
		byte header[] = new byte[Constants.HEADER_SIZE];
		
		byte data[] = new byte[Constants.PACKAGE_DATA_SIZE];
		
		extract_header(); //define method!!
		
		conn_id = header[Constants.ID_POS];
		
		if(check_crc(header[Constants.CRC_POS], RecvBuffer, Constants.INIT_CRC) != Constants.XST_SUCCESS) {
			//define method check_crc
			send_failure(header[Constants.ID_POS]); //define method
			return Constants.XST_FAILURE;
		}
		
		UART_EIVE_Protocol_Flags.set_ACK_Flag(new_flags, Constants.ACK); //define method
		
		//check request to send
		if(UART_EIVE_Protocol_Flags.get_Req_to_send_flag(header[Constants.FLAGS_POS]) == 0) {
			//define method
			//Send answer without set ACK flag
			send_failure(); //define method
			
			return Constants.XST_FAILURE;
		}
		
		UART_EIVE_Protocol_Flags.set_Rdy_to_rcv_Flag(new_flags, Constants.ACK); //define method
		
		int status;
		
		last_crc_rcv = header[Constants.CRC_POS];
		
		return status;
	}
	
	public int receive_data(byte last_sent_falgs) {
		byte next_header[] = new byte[Constants.HEADER_SIZE];
		byte new_data[] = new byte[Constants.PACKAGE_DATA_SIZE];
		
		byte flags_to_send = last_sent_falgs;
		
		int datacounter = 0;
		int pkgCounter = 0;
		int end = 0;
		int status = Constants.XST_NO_DATA;
		int timer = 1;
		int success = Constants.SET;
		
		while(end != Constants.SET) {
			//receiving answer
			while(status == Constants.XST_NO_DATA) {
				//timeout for receiving, reset timer for new sending
				if(timer == Constants.MAX_TIMER) {
					timer = 0;
				}
				if(timer == 0) {
					if(success == Constants.SET) {
						send_success(conn_id, flags_to_send); //define method
					}
					else {
						send_failure(next_header[Constants.ID_POS]); //define method
					}
				}
				//increase timer
				timer++;
				
				//check status of receiving
				if(recv) //method for receiving
					return Constants.XST_FAILURE;	
			}
			//data received
			
			flags_to_send = 0x00;
			extract_header(); //define method
			
			if(check_crc != Constants.XST_SUCCESS) {
				//failure
				success = 0;
				timer = 0;
				status = Constants.XST_NO_DATA;
				continue;
			}
			
			UART_EIVE_Protocol_Flags.set_ACK_Flag(flags_to_send, Constants.ACK); //define method
			
			if(UART_EIVE_Protocol_Flags.get_ACK_flag(next_header[Constants.FLAGS_POS]) != Constants.ACK) {
				//failure
				timer = 0;
				status = Constants.XST_NO_DATA;
				continue;
			}
			
			last_crc_send = calc_crc;
			last_crc_rcv = next_header[Constants.CRC_POS];
			
			//data buffer
			for(int bytes = 0; bytes < next_header[Constants.DATA_SIZE_POS]; bytes++) {
				databuffer[pkgCounter * Constants.PACKAGE_DATA_SIZE + bytes] = new_data[bytes];
			}
			datacounter += next_header[Constants.DATA_SIZE_POS];
			pkgCounter++;
			
			if(UART_EIVE_Protocol_Flags.get_end_flag(next_header[Constants.FLAGS_POS]) == 1) {
				end = 1;
				send_success(); //define method
			}
			timer = 0;
			status = Constants.XST_NO_DATA;
			success = 1;
		}
		//check type
		byte id = (byte) (next_header[Constants.ID_POS] & Constants.TM_MASK);
		
		//data array with exact length
		byte data[] = new byte[datacounter];
		
		//fill data array
		for(int bytes = 0; bytes < datacounter; bytes++)
			data[bytes] = databuffer[bytes];
		
		int size_of_data = data.length;
		
		switch(id) {
			//received data is tc
			case Constants.TC_MASK: recv_TC(); break;
			//received data is tm
			case Constants.TM_MASK: recv_TM(); break;
			
			default: default_operation();
		}
		
		return Constants.XST_SUCCESS;
	}
	
	public int extract_header() {
		for(int header_pos = 0; header_pos < Constants.HEADER_SIZE; header_pos++)
			header[header_pos] = Constants.RecvBuffer[header_pos];
		
		for(int data_byte = Constants.HEADER_SIZE; data_byte < Constants.BUFFER_SIZE; data_byte++)
			data[data_byte - Constants.HEADER_SIZE] = Constants.RecvBuffer[data_byte];
		
		return Constants.XST_SUCCESS;
	}
	
	public int send_failure(byte old_id) {
		byte failure_flags = UART_EIVE_Protocol_Flags.UNSET_ALL_FLAGS;
		
		byte header[] = new byte[Constants.HEADER_SIZE];
		
		UART_EIVE_Protocol_Flags.set_ACK_Flag(failure_flags, Constants.NACK);
		
		calc_crc = fill_header_for_empty_data();
		
		int status = UART_answer(header);
		return status;
	}
	
	public int send_success(byte id, byte flags) {
		byte header[] = new byte[Constants.HEADER_SIZE]; 
		
		calc_crc = UART_EIVE_Protocol_Send.fill_header_for_empty_data(header, conn_id, flags, last_crc_send);
		
		int status = UART_answer(header);
		return status;
	}
	
	public int UART_answer(byte header) {
		byte temp[] = new byte[Constants.BUFFER_SIZE];
		temp = {header[Constants.ID_POS], header[Constants.CRC_POS], header[Constants.DATA_SIZE_POS], header[Constants.FLAGS_POS]};
		
		//send method
		
		return Constants.XST_SUCCESS;
	}
	
	int recv_TC(byte[] databytes)
	{
		byte id = header[Constants.ID_POS];
		//FILE *fptr;

		switch(id)
		{
			case Constants.CAMERA_TC: break;
			case Constants.UART_TC: 	System.out.println("Done!");
							//puts((char*) databytes);
							/*for(int i = 0; i < size_of_data; i ++)
								printf("%c", (char) databytes[i]);
							printf("\n");*/

							//fopen, fclose, Test to write on a document
							/*if((fptr = fopen("/Users/valentinstadtlander/Desktop/doc/Test.pdf", "wb")) == NULL)
								printf("Error opening File\n");
							fwrite(databytes, size_of_data, 1, fptr);
							fclose(fptr);*/
							break;
			case Constants.CPU_TC: break;
			case Constants.BRAM_TC: break;
			case Constants.DOWNLINK_TC: break;
			case Constants.DAC_TC: break;
			default: return Constants.XST_FAILURE;
		}

		return Constants.XST_SUCCESS;
	}

}
