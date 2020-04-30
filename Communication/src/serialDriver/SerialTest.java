package serialDriver;
import java.io.BufferedInputStream;
import java.io.IOException;

import com.fazecast.jSerialComm.SerialPort;
/**
 * Simple application that is part of an tutorial. 
 * The tutorial shows how to establish a serial connection between a Java and Arduino program.
 * @author Michael Schoeffler (www.mschoeffler.de)
 *
 */
public class SerialTest {
  public static void main(String[] args) throws IOException, InterruptedException {
    
	byte[] readbuffer = new byte[32];	 
	SerialPort sp = SerialPort.getCommPort("COM3"); // device name TODO: must be changed
    sp.setComPortParameters(115200, 8, 1, 0); // default connection settings for Arduino
    sp.setComPortTimeouts(SerialPort.TIMEOUT_WRITE_BLOCKING, 0, 0); // block until bytes can be written
    sp.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, 10000, 0);
 
    if (sp.openPort()) {
      System.out.println("Port is open :)");
    } else {
      System.out.println("Failed to open port :(");
      return;
    }    
    
    Thread.sleep(1000);
    //for (Integer i = 0; i < 5; ++i) {     
    for(byte b: "hello world".getBytes())
      sp.getOutputStream().write(b);
      
    sp.getOutputStream().flush();
      System.out.println("Done");
      //Thread.sleep(1000);
      while(sp.getInputStream().available() == 0)
    	  System.out.println("Available: " + sp.getInputStream().available());
      new BufferedInputStream(sp.getInputStream()).read(readbuffer);
      for(byte b: readbuffer)
      	System.out.println((char) b);
     // System.out.println("Sent number: " + i);
      //Thread.sleep(1000);
  //  }    
    
    //while(sp.getInputStream().available() == 0);
    
  
    
    if (sp.closePort()) {
      System.out.println("Port is closed :)");
    } else {
      System.out.println("Failed to close port :(");
      return;
    }
  }
}
