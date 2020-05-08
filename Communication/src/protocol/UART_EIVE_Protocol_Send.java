package protocol;

import protocol.crc.CRC;
import protocol.flags.UART_EIVE_Protocol_Flags;

public class UART_EIVE_Protocol_Send 
{
	/*
	 * lastCRC_send saves the calculated CRC for the last send package
	 * submittedCRC always saves the new received CRC
	 * lastCRC_rcv saves the last received CRC for the initval for checking the received package
	 */
	byte lastCRC_send = 0x00, lastCRC_rcvd = 0x00;
	
	
	byte snd_flags = UART_EIVE_Protocol_Flags.UNSET_ALL_FLAGS;
	byte rcv_flags = UART_EIVE_Protocol_Flags.UNSET_ALL_FLAGS;
	
/*
 * Main Method of the EIVE UART Protocol to send data
 *
 * @param:	ID		Identification number of the data to send
 * @param:	*databytes	Pointer to the data which is going to be send
 * @param:	dataLength	Length of the data which is going to be send
 *
 * @return:	XST_SUCCES	If the data was send properly
 * @return:	XST_FAILURE	If the data was not send properly
 *
 * This method uses the connection_establishment() -Method to establish a connection between the sender and the receiver.
 * It also uses the send_data() -method to send the transfered data to the receiver after a connection was established.
 * It returns an error if the data could not to be send and a success, if the transmission was possible
 */
int UART_Send_Data(byte ID, byte databytes[], byte dataLength)
{

	int status;

	//connection establishment
	status = connect_(ID);

	System.out.println("connect");
	//return Failure if connection could not be established
	if(status == Constants.XST_FAILURE)
		return Constants.XST_FAILURE;

	//send data
	System.out.println("before send_data");
	status = send_data(ID, databytes, dataLength);
System.out.println("after send_data, returnValue: " + status);


	//return failure if the data could not be send
	if(status == Constants.XST_FAILURE)
		return Constants.XST_FAILURE;


	return Constants.XST_SUCCESS;

}

/*
 * Method to establish a connection to the receiver
 *
 * @param:	ID				Identification number of the package to send
 * @param:	*databytes		Pointer to the array of data which are going to be send
 * @param:	dataLength		Length of the data which is going to be send
 * @param:	*lastCRC_send	Pointer to the last send CRC value
 * @param:	*lastCRC_rcvd	Pointer to the last received CRC value
 *
 * @return:	XST_SUCCES	If the connection was established properly
 * @return:	XST_FAILURE	If the connection was not established properly
 */
int connect_(byte ID)
{

	//int packageCount = package_count(dataLength);
	//uint8_t temp[BUFFER_SIZE * packageCount];
	byte[] temp32 = new byte[Constants.BUFFER_SIZE];
	
	byte[] header = new byte[Constants.HEADER_SIZE];
	
	byte[] data = new byte[Constants.PACKAGE_DATA_SIZE];

	byte submittedCRC = CRC.INIT_CRC;

	int status;

	int connection = Constants.NACK;
	int conn_counter = 0;

	while(connection != UART_EIVE_Protocol_Flags.ACK && conn_counter < 10)
	{
		System.out.println("before req2send");
		//Request to send, CRC initval = 0x00
		lastCRC_send = CRC.INIT_CRC;
		status = send_request_to_send(ID, temp32);

		System.out.println("nach req2send");

		//check status of sending
		if(status != Constants.XST_SUCCESS)
			return Constants.XST_FAILURE;

		int acknowledge = Constants.NACK; //for CRC
		int succes = 1;

		while(acknowledge != Constants.ACK)
		{
			if(succes == 1)
			{
				System.out.println("Wait on answer a");
				//wait on answer sending again temp32
				System.out.println("Value: %i\n", wait_on_answer(temp32, ID, lastCRC_send));
			}
			else
			{
				System.out.println("Wait on answer b");
				//wait on answer sending again NACK
				System.out.println("Value: %i\n", wait_on_answer(null, ID, lastCRC_send));
			}

			System.out.println("connect: get_received_data");
			//fill header, data, receive flags and submittedCRC with the received values
			get_received_data(header, data, submittedCRC);

			//check received CRC
			System.out.println("Check crc in connect");
			System.out.println("Next crc check: Initval -> Last_CRC_rcvd: " + lastCRC_rcvd);
			if(check_crc(submittedCRC, Constants.RecvBuffer, *lastCRC_rcvd)!= Constants.XST_SUCCESS)
			{
				//CRC values defeer, send failure
				send_failure(lastCRC_send, ID, lastCRC_send);

			}
			else
			{
				acknowledge = Constants.ACK;

				//wird in check_crc übernommen
				//*lastCRC_rcvd = submittedCRC; //Test
			}
		}

		//set ACK = 1
		System.out.println("Set ack flag in connect\n");
		snd_flags = UART_EIVE_Protocol_Flags.set_ACK_Flag(snd_flags, Constants.ACK);

		//Received CRC is correct
		//check ACK

		System.out.println("get ack flag\n");
		if( UART_EIVE_Protocol_Flags.get_ACK_flag(rcv_flags) == Constants.SET)
		{

				//check ready to receive
			System.out.println("get ready to rcv flag\n");
				if( UART_EIVE_Protocol_Flags.get_ready_to_recv_flag(rcv_flags) == Constants.SET)
				{
					//send_data();
					connection = Constants.ACK;
					*lastCRC_rcvd = submittedCRC; //Test
				}
				else
				{
					//NOT ready to receive
					conn_counter++;
				}
			}
		}

	System.out.println("return at connect: conn_counter: "+ conn_counter);
	if(conn_counter == 10)
		return Constants.XST_FAILURE;


	return Constants.XST_SUCCESS;
}

/*
 *Request to send, to establish a connection
 *
 *@param:	ID			Identification number of the package to send
 *@param:	*lastCRC	Pointer, last CRC value to save the new CRC value for the next package
 *
 *Configures a package to send a request to send and saves the first CRC
 */
int send_request_to_send(byte ID, byte temp32[])
{
	System.out.println("asd");

	if(snd_flags == 0x00)
	{
		snd_flags = UART_EIVE_Protocol_Flags.REQ_TO_SEND_MASK;
		System.out.println("flags");
	}

	System.out.println("ID_POS"); //try puts
	temp32[Constants.ID_POS] = ID;

	System.out.println("DATA_SIZE_POS");
	temp32[Constants.DATA_SIZE_POS] = 0;

	System.out.println("FLAG_POS");
	temp32[Constants.FLAGS_POS] = snd_flags;


	System.out.println("Test: " + lastCRC_send);//, temp32[0]);
	System.out.println("Test2: " + temp32[0]);

	System.out.println("Next crc calc: Initval -> Last_CRC_send: %i\n", lastCRC_send);
	int crc = calc_crc8(temp32, lastCRC_send);

	*lastCRC_send = crc; //Kann auf nach gesetztem ACK-Flag verschoben werden.

	System.out.println("nach crc");

	temp32[Constants.CRC_POS] = *lastCRC_send;
	//(*lastCRC_send) = newCRC;

	int status = Constants.XST_FAILURE;
	int tries = 0;

	System.out.println("vor while");
	while(status != Constants.XST_SUCCESS)
	{
		// -> Network now
		//status = UART_Send(temp32, 1);
		System.out.println("SEND DATA!!!!!\n");
		status = Constants.XST_SUCCESS;
		if(send(sock, temp32, Constants.BUFFER_SIZE, 0) != Constants.BUFFER_SIZE)
		{
			status = Constants.XST_FAILURE;
			System.out.println("ERROR sendreq2send");
		}

		System.out.println("while");
		if(tries == 50)
		{
			System.out.println("try == 50\n");
			return Constants.XST_FAILURE;
		}

		tries++;
	}

	return Constants.XST_SUCCESS;
}

/*
 * Package counter
 *
 * @param:	dataLength	number of bytes of the data to send
 *
 * returns the number of the needed packages to send all the databytes
 */
int package_count(int dataLength)
{
	int ret = 0;
	System.out.println("DataLength: %i ------- %i\n", dataLength, (ret = dataLength/Constants.PACKAGE_DATA_SIZE));

	if (dataLength % Constants.PACKAGE_DATA_SIZE > 0)
		return (dataLength / Constants.PACKAGE_DATA_SIZE + 1);
	else
		return ret; //(dataLength / PACKAGE_DATA_SIZE);
}

/*
 * Method to save the submitted header, data, flags and CRC from the receiver
 *
 * @param:	*header			Pointer to an array of the size of HEADER_SIZE to save the received header
 * @param:	*data			Pointer to an array of the size of PACKAGE_DATA_SIZE to save the received data
 * @param:	*flags			Pointer, to save the received Flags
 * @param: 	*submittedCRC	Pointer, to save the submitted CRC value
 *
 * This method stores in the delivered parameters the received information
 */
void get_received_data(uint8_t *header, uint8_t *data, uint8_t *flags, uint8_t *submittedCRC)
{
	System.out.println("getReceivedData\n");
	extract_header(RecvBuffer, header, data);
	*flags = header[FLAGS_POS];
	*submittedCRC = header[CRC_POS];
}

/*
 * Method to send the data
 *
 * @param:	ID				Identification number of the package to send
 * @param:	*databytes		Pointer to the array of data which are going to be send
 * @param:	dataLength		Length of the data which is going to be send
 * @param:	*lastCRC_send	Pointer to the last send CRC value
 * @param:	*lastCRC_rcvd	Pointer to the last received CRC value
 *
 * @return:	XST_SUCCES	If the data was send properly
 * @return:	XST_FAILURE	If the data was not send properly
 */
int send_data(uint8_t ID, uint8_t *databytes, int dataLength, uint8_t *lastCRC_send, uint8_t *lastCRC_rcvd)
{
	uint8_t send_array[Constants.BUFFER_SIZE];
	int packageCount = package_count(dataLength);
	uint8_t header[Constants.HEADER_SIZE];
	uint8_t data[Constants.PACKAGE_DATA_SIZE];
	byte flags;
	byte submittedCRC;
	byte temp[] = new byte[Constants.BUFFER_SIZE * packageCount];
	int crc = 0;
	int status = Constants.XST_SUCCESS;

	System.out.println("fill packages: %i\n", packageCount);
	//fill array temp with the databytes and the header to send
	fill_packages(ID, dataLength, databytes, temp, packageCount);

	int package_counter = 0;
	int tries = 0;

	while(package_counter < packageCount && tries <= 10)
	{
		System.out.println("while package_counter < packageCount: %i\n", packageCount);
		//Get packages
		for(int i = 0; i < Constants.BUFFER_SIZE; i++)
			send_array[i] = temp[package_counter * Constants.BUFFER_SIZE + i];

		System.out.println("setAckflag\n");
		//Set acknowledge flag
		set_ACK_Flag(&send_array[Constants.FLAGS_POS], ACK);

		System.out.println("before crc in while\n");
		System.out.println("Next crc calc: Initval -> Last_CRC_send: %i\n", *lastCRC_send);
		//Calculate CRC value
		crc = calc_crc8(send_array, *lastCRC_send);

		send_array[Constants.CRC_POS] = crc;

		System.out.println("after crc in while\n");
		//Send package -> Network now
		//status = UART_Send(send_array, 1);
		if(send(sock, send_array, Constants.BUFFER_SIZE, 0) != Constants.BUFFER_SIZE)
			System.out.println("EEROR");

		System.out.println("after send in while\n");
		if(status != XST_SUCCESS)
		{
			tries++;
			continue;
		}

		//Wait on acknowledge package and check
		uint8_t acknowledge = NACK;
		int succes = 1;

		while(acknowledge != ACK)
		{
			System.out.println("wait on ack in send_data\n");
			//wait on receive buffer to be filled
			if(succes == 1)
			{
				System.out.println("SendArray\n");
				wait_on_answer(send_array, send_array[Constants.ID_POS], &send_array[Constants.CRC_POS]);
			}
			else
			{
				//wait_on_answer with NACK
				wait_on_answer(NULL, ID, lastCRC_send);
				System.out.println("Send NACK-Package\n");
			}

			//get received information
			get_received_data(header, data, &flags, &submittedCRC);

			System.out.println("Next crc check: Initval -> Last_CRC_rcvd: %i\n", lastCRC_rcvd);
			//check received CRC
			if(check_crc(submittedCRC, RecvBuffer, lastCRC_rcvd)!= Constants.XST_SUCCESS)
			{
				send_failure(lastCRC_send, ID, &crc);
				succes = 0;
			}
			else
			{
				acknowledge = ACK;

				//wird von check_crc übernommen
				lastCRC_rcvd = submittedCRC; //Test
			}
		}

		System.out.println("crc correct and get ack\n");
		//Received CRC is correct
		//check ACK
		if(get_ACK_flag(flags) != SET)
		{
			/* CRC is correct, NACK */
			System.out.println("CRC corr, received NACK");
			continue;
		}

		System.out.println("get last crc sent \n");
		lastCRC_send = send_array[CRC_POS];

		package_counter++;
	}

	if(tries == 10)
		return XST_FAILURE;

	return XST_SUCCESS;
}

/*
 * Method to wait on an answer of the receiver
 *
 * @param:	*send_array:	pointer to the array which is going to be send again if the timer expires and the RecvBuffer does not get filled
 *							NULL if the send_array is NACK
 * @param:	ID				Identification number of the package to send
 * @param:	*lastCRC_send	Pointer to the last send CRC value
 *
 * @return:	XST_SUCCES		If an answer was received
 * @return:	XST_FAILURE		If no answer was received
 */
int wait_on_answer(byte send_array[], byte ID, byte lastCRC_send)
{
	byte[] nack_header = new byte[Constants.BUFFER_SIZE];

	if(null == send_array)
	{
		lastCRC_send = fill_header_for_empty_data(nack_header, ID, UART_EIVE_Protocol_Flags.UNSET_ALL_FLAGS, lastCRC_send);
	}

	int status = Constants.XST_NO_DATA;
	int timer;

	while(status != Constants.XST_SUCCESS)
	{
		System.out.println("Wait for answer: while\n");
		// -> Network now
		//status = UART_Recv_Buffer();
		if(recv(sock, Constants.RecvBuffer, Constants.BUFFER_SIZE, 0) < 0)
			System.out.println("ERROR waitOnAnswer1");


		status = Constants.XST_SUCCESS; //test

		System.out.println("Answer received!!: \n");
		if(status != Constants.XST_NO_DATA && status != Constants.XST_SUCCESS)
			return Constants.XST_FAILURE;

		timer++;

		if(timer == Constants.MAX_TIMER)
		{
			//Timeout
			//send again array to send
			if(null == send_array)
			{
				System.out.println("Timer is max timer\n");
				byte temp[] = new byte[Constants.BUFFER_SIZE];
				temp[Constants.ID_POS]= nack_header[Constants.ID_POS];
				temp[Constants.CRC_POS] = nack_header[Constants.CRC_POS];
				temp[Constants.DATA_SIZE_POS] = nack_header[Constants.DATA_SIZE_POS];
				temp[Constants.FLAGS_POS] = nack_header[Constants.FLAGS_POS];
				// -> Network now
				//UART_Send(temp, 1);
				if(send(sock, temp, Constants.BUFFER_SIZE, 0) != Constants.BUFFER_SIZE)
					System.out.println("ERROR waitOnAnswer2");
			}
			else
			{
				// -> Network now
				//UART_Send(send_array, 1);
				if(send(sock, send_array, Constants.BUFFER_SIZE, 0) != Constants.BUFFER_SIZE)
					System.out.println("ERROR waitOnAnswer3");
			}

			//reset timer
			timer = 0;
		}
	}
	return Constants.XST_SUCCESS;
}


/*
 * Method to fill the packages to send
 *
 * @param:	ID 				Identification number of the package to send
 * @param: 	dataLength		length of the data to send, must be given by the user
 * @param:	*databytes		Pointer to the data to send
 * @param:	temp			Pointer to the temporary array in the main method with the length of BUFFER_SIZE * packageCount,
 * 							which is filled with the header and the databytes
 * @param: 	packageCount	numbers of packages needed to send all the databytes
 *
 * Fills the submitted variable temp with the databytes and the headers
 */
void fill_packages(byte ID, int dataLength, byte[] databytes, uint8_t *temp, int packageCount)
{

	/*Temporary arrays for header and data*/
	//uint8_t header[4];

	System.out.println("Fill %i packages with: ", packageCount);
	System.out.println((char*) databytes);
	uint8_t header[Constants.HEADER_SIZE] = {ID, INIT_CRC, 0, UNSET_ALL_FLAGS};

	//uint8_t flags = UNSET_ALL_FLAGS;

	for (int i = 0; i < packageCount; i++)
	{

		/*first package*/
		if(i == 0)
		{
			System.out.println("fill header first pkg\n");
			//Fill header[DATA_SIZE_POS]
			header[DATA_SIZE_POS] = PACKAGE_DATA_SIZE;

			System.out.println("Set start flag\n");
			/*
			 * fill header with the given information
			 * flags for the start package
			 */
			UART_EIVE_Protocol_Flags.set_Start_Flag(header[FLAGS_POS], SET);

			//Setting end-flag if only one package will be send
			if(packageCount == 1)
				UART_EIVE_Protocol_Flags.set_End_Flag(header[FLAGS_POS], SET);

			System.out.println("Start flag set\n");

			/*fill temporary array temp with the headers*/
			for (int k = 0; k < HEADER_SIZE; k++)
			{
				temp[k] = header[k];
			}

			System.out.println("Chars in this package: ");
			for (int j = HEADER_SIZE; j < BUFFER_SIZE; j++)
			{
				/*fill temporary arrays temp*/
				temp[j] = databytes[j - HEADER_SIZE];
				System.out.println("%c", temp[j]);
			}
			System.out.println("\n");
		}

		/*all packages except the first and the last one*/
		else if (i > 0 && i != packageCount - 1)
		{
			System.out.println("fill header %i. pkg\n", (i+1));

			//Fill header[DATA_SIZE_POS]
			header[DATA_SIZE_POS] = PACKAGE_DATA_SIZE;

			/*
			 * fill header with the given information*/
			/* flags for the middle packages
			 */
			//flags = 0b00000000; //anpassen, ACK flag ist gesetzt!!!
			UART_EIVE_Protocol_Flags.set_Start_Flag(header[FLAGS_POS], NOT_SET);


			/*fill temporary array temp with the headers*/
			for (int k = 0; k < HEADER_SIZE; k++)
			{
				temp[i * BUFFER_SIZE + k] = header[k];
			}

			System.out.println("Chars in this package: ");
			/*fill temporary arrays temp and temp28*/
			for (int j = HEADER_SIZE; j < BUFFER_SIZE; j++)
			{
				temp[i * BUFFER_SIZE + j] = databytes[i * PACKAGE_DATA_SIZE + j - HEADER_SIZE];
				System.out.println("%c", temp[i*BUFFER_SIZE + j]);
			}
			System.out.println("\n");
		}

		/*last package*/
		else
		{
			int restsize = dataLength - PACKAGE_DATA_SIZE * (packageCount - 1);

			System.out.println("fill header last pkg, restsize: %i\n", restsize);

			//Fill header[DATA_SIZE_POS]
			header[DATA_SIZE_POS] = restsize;

			/*fill header with the given information*/
			/*flags for the end package*/
			UART_EIVE_Protocol_Flags.set_End_Flag(header[FLAGS_POS], SET);

			/*fill temporary array temp with the headers*/
			for (int k = 0; k < HEADER_SIZE; k++)
			{
				temp[i * BUFFER_SIZE + k] = header[k];
			}


			System.out.println("chars in this package: ");
			for(int j = HEADER_SIZE; j < BUFFER_SIZE; j++)
			{
				/*fill temp and temp28*/
				if(j - HEADER_SIZE < restsize)
				{
					/*fill with the rest databytes from position 0 to restsize*/
					temp[i * BUFFER_SIZE + j] = databytes[i * PACKAGE_DATA_SIZE + j - HEADER_SIZE];
					System.out.println("%c", temp[i*BUFFER_SIZE + j]);
				}
				else
				{
					/*fill with 0 from position restsize to 28*/
					temp[i * BUFFER_SIZE + j] = 0;
				}
			}
			System.out.println("\n");


		}

		System.out.println("Package no. %i: data size %i\n", i,header[DATA_SIZE_POS]);
	}

}

/*
 * Fill Header with submitted parameters
 *
 * @param:	*header			Pointer to store the header
 * @param:	ID 				Identification number of the package to send
 * @param: 	flags			Flags of the package which is going to be send
 * @param:	*lastCRC_send	Pointer to the last calculated CRC of the last send package
 *
 * This method fills the header of empty packages which are going to be send
 */
byte fill_header_for_empty_data(byte[] header, byte ID, byte flags, byte lastCRC_send)
{
	System.out.println("fill header for empty dataaa\n");
	byte[] temp_array_CRC = new byte[Constants.BUFFER_SIZE];
	temp_array_CRC[Constants.ID_POS] = ID;
	temp_array_CRC[Constants.FLAGS_POS] = flags;

	System.out.printf("Next crc calc: Initval -> Last_CRC_send: %i\n", lastCRC_send);
	/*calculate new CRC value*/
	byte newCRC = CRC.calc_crc8(temp_array_CRC, lastCRC_send);

	/*save new CRC value in old variable*/
	//(*lastCRC_send) = newCRC;

	/*fill header*/
	header[Constants.ID_POS] = ID;
	header[Constants.CRC_POS] = newCRC;
	header[Constants.DATA_SIZE_POS] = Constants.EMPTY_DATA_LENGTH;
	header[Constants.FLAGS_POS] = flags;

	return newCRC;
}


}
