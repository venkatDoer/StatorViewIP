package doer.io;
import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.TooManyListenersException;


public class Port {
	
	   /* get available serial ports */
	/**
     * @return    A HashSet containing the CommPortIdentifier for all serial ports that are not currently being used.
     */
	public static HashSet<CommPortIdentifier> getAvailableSerialPorts() {
	    HashSet<CommPortIdentifier> h = new HashSet<CommPortIdentifier>();
	    Enumeration<?> thePorts = CommPortIdentifier.getPortIdentifiers();
	    
	    while (thePorts.hasMoreElements()) {
	        CommPortIdentifier com = (CommPortIdentifier) thePorts.nextElement();
	        
	        switch (com.getPortType()) {
	        case CommPortIdentifier.PORT_SERIAL:
	            try {
	            	if(!com.isCurrentlyOwned()  || com.getCurrentOwner().contains("Modbus Serial Master")) {
	    		h.add(com);
	    	}
	    } catch (Exception e) {
	    	// ignore
	            }
	        }
	    }
	    return h;
	}
	
    /* attach a serial port to given device */
    public static void attachPort(SerialPort port, SerialPortEventListener deviceHandlerClass) throws Exception
    {
    	// registering the device handler class as port's event handler
    	try {
			port.addEventListener(deviceHandlerClass);
		} catch (TooManyListenersException e) {
			
			throw new Exception("The port is already registered:" + e.getMessage());
		}
		// set other properties of the port
		port.notifyOnDataAvailable(true);
    }
    
	/* utility function to convert port event to readable format */
	public static String getEventTypeString(int eventType)
	{
		String typeInString = null;
		switch (eventType) {
			case SerialPortEvent.BI:
				typeInString = "BI";
				break;
			case SerialPortEvent.CD:
				typeInString = "CD";
				break;
			case SerialPortEvent.CTS:
				typeInString = "CTS";
				break;
			case SerialPortEvent.DATA_AVAILABLE:
				typeInString = "DATA_AVAILABLE";
				break;
			case SerialPortEvent.DSR:
				typeInString = "DSR";
				break;
			case SerialPortEvent.FE:
				typeInString = "FE";
				break;
			case SerialPortEvent.OE:
				typeInString = "OE";
				break;
			case SerialPortEvent.OUTPUT_BUFFER_EMPTY:
				typeInString = "OUTPUT_BUFFER_EMPTY";
				break;
			case SerialPortEvent.PE:
				typeInString = "PE";
				break;
			case SerialPortEvent.RI:
				typeInString = "RI";
				break;
			default:
				typeInString = "UNKNOWN";
				break;
		}
		return typeInString;
	}
	
	/* read data from a serial port */
	public static String readDataFromPortEvent(SerialPortEvent event) {
		InputStream portIn = null;
		SerialPort port = (SerialPort) event.getSource();
		try {
			portIn = port.getInputStream();
		} catch (IOException e) {
			System.err.println("Error while getting input stream of port:" + port.getName() + ":" + e.getMessage());
		}
		StringBuffer sBuff = new StringBuffer();
		char c;
		try {
			try {
				while ( portIn.available() > 0 ) {
					c = (char)portIn.read();
					sBuff.append(c);
				}
			} catch (IOException e) {
				portIn.close();
				System.err.println("Error while reading the data from port:" + port.getName() + ":" + e.getMessage());
			}
			portIn.close();
		} catch (IOException e) {
			System.err.println("Error while closing input stream of port:" + port.getName() + ":" + e.getMessage());
		}
		return sBuff.toString();
	}

}
