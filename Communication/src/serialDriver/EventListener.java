package serialDriver;

import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;

public abstract class EventListener implements SerialPortEventListener
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
