package serialDriver;

import com.fazecast.jSerialComm.SerialPortDataListener;

import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;

public abstract class EventListener implements SerialPortDataListener
{

	private SerialDriver comm;

	public EventListener(SerialDriver comm)
	{
		this.comm = comm;
	}
	
	@Override
	public void serialEvent(SerialPortEvent evt) 
	{
		if(evt.getEventType() == SerialPortEvent.DATA_AVAILABLE)
		{
			dataAvailable();
		}
	}

	public abstract void dataAvailable();
}
