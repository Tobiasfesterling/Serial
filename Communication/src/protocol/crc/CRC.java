package protocol.crc;

import protocol.constants;

public class CRC {

	/*
	 * Generator polynom for cyclic redundancy check
	 *
	 *
	 */
	public static final byte CRC_GENERATOR = 0x1D;

	/**
	 *
	 * Initial CRC value for calculating first crc
	 *
	 *
	 */
	public static final byte INIT_CRC = 0x00;

	/*
	 * CRC calculation function for one byte
	 *
	 * start_crc: start value for crc calculation byte: byte for crc calculation
	 *
	 * returns the calculated 8-bit crc value for the given byte
	 */
public static byte calc_crc8_for_one_byte(byte start_crc, byte byteForCRC)
{
	byte crc = (byte) (start_crc ^ byteForCRC);
	for(int i = 0; i < 8; i++)
	{
		if((crc & 0x80) != 0)
		{
			crc = (byte) ((crc<<1) ^ CRC_GENERATOR);
		} else
		{
			crc <<= 1;
		}
	}

	return crc;
}

	/*
	 * CRC calculation function for a message more than one byte
	 *
	 * bytes[]: message with bytes for crc calculation length: length of the byte
	 * array crc_initval: the start value for calculating crc of the data
	 *
	 * returns the calculated 8-bit crc value for the whole message
	 */
public static byte calc_crc8_for_data(byte bytes[], int length, byte crc_initval)
{

	byte crc_val = crc_initval;
	for(int i = 0; i < length; i++)
	{
		crc_val = calc_crc8_for_one_byte(crc_val, bytes[i]);
	}

	System.out.println("calculated crc: " + crc_val);
	return crc_val;
}

byte calc_crc8(byte send_array[], byte crc_initval)
{
	System.out.println("crc8: initval: " + crc_initval);
	byte temp31[] = new byte[constants.BUFFER_SIZE - 1];

	for(int i = 0; i < constants.CRC_POS; i++)
	{
		temp31[i] = send_array[i];
	}

	for(int j = constants.CRC_POS; j < constants.BUFFER_SIZE - 1; j++)
	{
		temp31[j] = send_array[j + 1];
	}

	System.out.println("calc crc8");
	return calc_crc8_for_data(temp31, constants.BUFFER_SIZE - 1, crc_initval);
}

	/*
	 *
	 */
int check_crc(byte crc_val, byte rcv_buffer[], byte crc_initval)
{
	byte calculated_crc;

	calculated_crc = calc_crc8(rcv_buffer, crc_initval);

	if(calculated_crc != crc_val)
		return constants.XST_FAILURE;


	//*crc_initval = calculated_crc;

	return constants.XST_SUCCESS;
}

}
