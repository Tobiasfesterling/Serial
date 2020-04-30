package protocol;
import protocol.constants;

public class UART_EIVE_Protocol_Recv {
	
	//CRC
	public byte last_crc_send = constants.INIT_CRC;
	public byte last_crc_rcv = constants.INIT_CRC;
	public byte calc_crc = constants.INIT_CRC;
	
	//flags
	public byte new_flags = 0x00;
	
	//connection ID
	public byte conn_id = 0x00;
	
	//Long buffer for receiving data
	public static byte databuffer[] = new byte[573483];
	
	public byte header[] = new byte[constants.HEADER_SIZE];
	public byte data[] = new byte[constants.PACKAGE_DATA_SIZE];

	public static int UART_Recv_Data() {
		
		int status = constants.XST_SUCCESS;
		
		if((status = recv_data) //empfangsmethode anpassen, define method
				System.out.println("Error receiving data!!");
		
		if(status == constants.XST_FAILURE)
			return constants.XST_FAILURE;
		
		status = receive();
		
		if(status != constants.XST_SUCCESS)
			return constants.XST_FAILURE;
		
		return constants.XST_SUCCESS;
	}
	
	public int receive() {
		int status = constants.XST_SUCCESS;
		
		//connection establishment
		status = connection_establishment();
		
		if(status == constants.XST_FAILURE)
			return constants.XST_FAILURE;
		
		//receive the TM/TCs
		status = receive_data(); //define method!!
		
		if(status == constants.XST_FAILURE)
			return constants.XST_FAILURE;
		
		return constants.XST_SUCCESS;
	}
	
	public int connection_establishment() {
		byte header[] = new byte[constants.HEADER_SIZE];
		
		byte data[] = new byte[constants.PACKAGE_DATA_SIZE];
		
		extract_header(); //define method!!
		
		conn_id = header[constants.ID_POS];
		
		if(check_crc(header[constants.CRC_POS], RecvBuffer, constants.INIT_CRC) != constants.XST_SUCCESS) {
			//define method check_crc
			send_failure(header[constants.ID_POS]); //define method
			return constants.XST_FAILURE;
		}
		
		set_ACK_Flag(); //define method
		
		//check request to send
		if(get_Req_to_send_flag(header[constants.FLAGS_POS]) == 0) {
			//define method
			//Send answer without set ACK flag
			send_failure(); //define method
			
			return constants.XST_FAILURE;
		}
		
		set_Rdy_to_rcv_Flag(); //define method
		
		int status;
		
		last_crc_rcv = header[constants.CRC_POS];
		
		return status;
	}
	
	public int receive_data(byte last_sent_falgs) {
		byte next_header[] = new byte[constants.HEADER_SIZE];
		byte new_data[] = new byte[constants.PACKAGE_DATA_SIZE];
		
		byte flags_to_send = last_sent_falgs;
		
		int datacounter = 0;
		int pkgCounter = 0;
		int end = 0;
		int status = constants.XST_NO_DATA;
		int timer = 1;
		int success = constants.SET;
		
		while(end != constants.SET) {
			//receiving answer
			while(status == constants.XST_NO_DATA) {
				//timeout for receiving, reset timer for new sending
				if(timer == constants.MAX_TIMER) {
					timer = 0;
				}
				if(timer == 0) {
					if(success == constants.SET) {
						send_success(); //define method
					}
					else {
						send_failure(); //define method
					}
				}
				//increase timer
				timer++;
				
				//check status of receiving
				if(recv) //method for receiving
					return constants.XST_FAILURE;	
			}
			//data received
			
			flags_to_send = 0x00;
			extract_header(); //define method
			
			if(check_crc != constants.XST_SUCCESS) {
				//failure
				success = 0;
				timer = 0;
				status = constants.XST_NO_DATA;
				continue;
			}
			
			set_ACK_Flag(); //define method
			
			if(get_ACK_Flag() != constants.ACK) {
				//failure
				timer = 0;
				status = constants.XST_NO_DATA;
				continue;
			}
			
			last_crc_send = calc_crc;
			last_crc_rcv = next_header[constants.CRC_POS];
			
			//data buffer
			for(int bytes = 0; bytes < next_header[constants.DATA_SIZE_POS]; bytes++) {
				databuffer[pkgCounter * constants.PACKAGE_DATA_SIZE + bytes] = new_data[bytes];
			}
			datacounter += next_header[constants.DATA_SIZE_POS];
			pkgCounter++;
			
			if(get_end_flag(next_header[constants.FLAGS_POS]) == 1) {
				end = 1;
				send_success(); //define method
			}
			timer = 0;
			status = constants.XST_NO_DATA;
			success = 1;
		}
		//check type
		byte id = (byte) (next_header[constants.ID_POS] & constants.TM_MASK);
		
		//data array with exact length
		byte data[] = new byte[datacounter];
		
		//fill data array
		for(int bytes = 0; bytes < datacounter; bytes++)
			data[bytes] = databuffer[bytes];
		
		int size_of_data = data.length;
		
		switch(id) {
			//received data is tc
			case constants.TC_MASK: recv_TC(); break;
			//received data is tm
			case constants.TM_MASK: recv_TM(); break;
			
			default: default_operation();
		}
		
		return constants.XST_SUCCESS;
	}
	
	public int extract_header() {
		for(int header_pos = 0; header_pos < constants.HEADER_SIZE; header_pos++)
			header[header_pos] = constants.RecvBuffer[header_pos];
		
		for(int data_byte = constants.HEADER_SIZE; data_byte < constants.BUFFER_SIZE; data_byte++)
			data[data_byte - constants.HEADER_SIZE] = constants.RecvBuffer[data_byte];
		
		return constants.XST_SUCCESS;
	}
	
	public int send_failure(byte old_id) {
		byte failure_flags = UNSET_ALL_FLAGS;
		
		byte header[] = new byte[constants.HEADER_SIZE];
		
		set_ACK_Flag();
		
		calc_crc = fill_header_for_empty_data();
		
		int status = UART_answer(header);
		return status;
	}
	
	public int send_success(byte id, byte flags) {
		byte header[] = new byte[constants.HEADER_SIZE]; 
		
		calc_crc = fill_header_for_empty_data();
		
		int status = UART_answer(header);
		return status;
	}
	
	public int UART_answer(byte header) {
		byte temp[] = new byte[constants.BUFFER_SIZE];
		temp = {header[constants.ID_POS], header[constants.CRC_POS], header[constants.DATA_SIZE_POS], header[constants.FLAGS_POS]};
		
		//send method
		
		return constants.XST_SUCCESS;
	}
	
	int recv_TC(byte[] databytes)
	{
		byte id = header[constants.ID_POS];
		//FILE *fptr;

		switch(id)
		{
			case constants.CAMERA_TC: break;
			case constants.UART_TC: 	System.out.println("Done!");
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
			case constants.CPU_TC: break;
			case constants.BRAM_TC: break;
			case constants.DOWNLINK_TC: break;
			case constants.DAC_TC: break;
			default: return constants.XST_FAILURE;
		}

		return constants.XST_SUCCESS;
	}

}
