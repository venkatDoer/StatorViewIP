package doer.io;
// Developed by Venkat. Copyright 2014 Doer

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashMap;

import javax.swing.JDialog;
import javax.swing.JOptionPane;

import java.net.InetAddress;

import doer.sv.Configuration;
import net.wimpi.modbus.ModbusCoupler;
import net.wimpi.modbus.ModbusException;
import net.wimpi.modbus.io.ModbusSerialTransaction;
import net.wimpi.modbus.io.ModbusTCPTransaction;
import net.wimpi.modbus.msg.ReadCoilsRequest;
import net.wimpi.modbus.msg.ReadCoilsResponse;
import net.wimpi.modbus.msg.ReadInputDiscretesRequest;
import net.wimpi.modbus.msg.ReadInputDiscretesResponse;
import net.wimpi.modbus.msg.ReadInputRegistersRequest;
import net.wimpi.modbus.msg.ReadInputRegistersResponse;
import net.wimpi.modbus.msg.ReadMultipleRegistersRequest;
import net.wimpi.modbus.msg.ReadMultipleRegistersResponse;
import net.wimpi.modbus.msg.WriteCoilRequest;
import net.wimpi.modbus.msg.WriteMultipleRegistersRequest;
import net.wimpi.modbus.net.SerialConnection;
import net.wimpi.modbus.net.TCPMasterConnection;
import net.wimpi.modbus.procimg.Register;
import net.wimpi.modbus.procimg.SimpleDigitalIn;
import net.wimpi.modbus.procimg.SimpleRegister;
import net.wimpi.modbus.util.SerialParameters;

/* Implementation class for handling PID 500 PLC*/

public class DeviceModbusReader {
	
	// device changeable params
	private Integer devId = 1; 
	private Integer wCount = 2;
	private Boolean isLsbFirst = false;
	private String Protocol = null;
	private InetAddress ipaddr = null;
	private Integer ipport = 2200;
	
	// RTU communication
	SerialConnection con1 = null;
	ModbusSerialTransaction transCoil = null;
	ModbusSerialTransaction writeTransCoil = null;
	ModbusSerialTransaction transInReg = null;
	ModbusSerialTransaction transHolRegRead = null;
	ModbusSerialTransaction transHolRegWrite = null;
	
	ReadInputDiscretesRequest icoilRead = null;
	ReadCoilsRequest ocoilRead = null;
	WriteCoilRequest ocoilWrite = null;
	ReadInputRegistersRequest ireq = null;
	ReadMultipleRegistersRequest mreq = null;
	WriteMultipleRegistersRequest wreq = null;
	
	// TCP_IP Communication
	TCPMasterConnection con2 = null;
	ModbusTCPTransaction iptransCoil = null;
	ModbusTCPTransaction ipwriteTransCoil = null;
	ModbusTCPTransaction iptransInReg = null;
	ModbusTCPTransaction iptransHolRegRead = null;
	ModbusTCPTransaction iptransHolRegWrite = null;
	
//	ReadInputDiscretesRequest ipicoilRead = null;
//	ReadCoilsRequest ipocoilRead = null;
//	WriteCoilRequest ipocoilWrite = null;
//	ReadInputRegistersRequest ipireq = null;
//	ReadMultipleRegistersRequest ipmreq = null;
//	WriteMultipleRegistersRequest ipwreq = null;
	
	
	
	// others
	private SerialParameters params = null;
	private String errMsg = null;
	
	private String resHexStr = null;
	private Boolean initialized = false;
	
	
	
	/* function to initialize the device */
	public void initialize(CommParameters commParam) throws Exception {
		
		initialized = false;
		Protocol = commParam.getProtocol();
		devId = commParam.getDevId();
		wCount = commParam.getWc();
		this.isLsbFirst = commParam.getEndianness().equals("LSB First");
		
		// serial port parameters
		params = new SerialParameters();
		params.setPortName(commParam.getPortName());
		params.setBaudRate(commParam.getBaudRate());
		params.setDatabits(commParam.getDatabits());
		params.setParity(commParam.getParity());
		params.setStopbits(commParam.getStopbits());
		params.setEncoding("rtu");
		params.setEcho(false);
		
		//Set master identifier
		ModbusCoupler.getReference().setUnitID(1);
		
		// Open the connection for RTU Communication
		con1 = new SerialConnection(params);
		
		// Open the connection for TCP_IP Communication
		ipaddr = InetAddress.getByName(commParam.getIpAddress());
		ipport = commParam.getIpPort();
		con2 = new TCPMasterConnection(ipaddr);
        con2.setPort(ipport);
		
		try {
			if(Protocol.equals("RTU")) {
				con1.open();
			}else {
				con2.connect();
//				System.out.println("ip Connected");
			}
		} catch (UnsatisfiedLinkError le) {
			// copy the missing lib
			String javaDir = System.getProperty("java.home");
			String libFile = Configuration.APP_DIR + "\\lib\\doer\\rxtxSerial.dll";
			
			try {
				String cmd = "xcopy /YV  \"" + libFile + "\" \"" +  javaDir + "\\bin\"";
				Process p = Runtime.getRuntime().exec(cmd);
				BufferedReader er = new BufferedReader(new InputStreamReader(p.getErrorStream()));
				BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
				String tmpStr = "";
				String filesError = "";
				// check for any error or output
				
				Thread.sleep(1000);
				if (er.ready()) {
					while ((tmpStr = er.readLine()) != null) {
						filesError += tmpStr + "\n";
					}
					if (!filesError.isEmpty()) {
						throw new Exception(filesError);
					}
				}
				
				// ignore the output
				Thread.sleep(1000);
				if (in.ready()) {
					while ((tmpStr = in.readLine()) != null) {
						filesError += tmpStr + "\n";
					}
				}
				
				er.close();
				in.close();
				p.destroy();
				
				JOptionPane.showMessageDialog(new JDialog(), "It seems Java got updated recently. Hence, a file required to run this application has just been copied.\nPlease close this dialog and re-open the application", "Warning", JOptionPane.WARNING_MESSAGE);
				System.exit(-1);
			} catch (Exception e) {
				JOptionPane.showMessageDialog(new JDialog(), "Error while copying the missing communication library:" + e.getMessage() + "\nYou have two options now.\n1.Run the application as administrator by righ clicking the icon in desktop to copy the required file automatically OR\n2.Note down the file mentioned below and manually copy it to the directory \"" + javaDir + "\\bin\"\n" + libFile, "Error", JOptionPane.ERROR_MESSAGE);
				System.exit(-1);
			}
		} catch (Exception e) {
			if(Protocol.equals("RTU")) {
				errMsg = params.getPortName().isEmpty()?"Port not yet configured for this device":"Error while opening connection to port" + e.getMessage()==null?"":":"+e.getMessage();
				throw new Exception(errMsg);
			}else {
				errMsg = commParam.getIpAddress().isEmpty()?"IP Address not yet configured for this device":"Error while opening connection to IP Connection" + e.getMessage()==null?"":":"+e.getMessage();
				throw new Exception(errMsg);
			}
		}
		
		// Prepare transactions for RTU
		transCoil = new ModbusSerialTransaction(con1);
		transCoil.setRetries(3);
		writeTransCoil = new ModbusSerialTransaction(con1);
		writeTransCoil.setRetries(3);
		transInReg = new ModbusSerialTransaction(con1);
		transInReg.setRetries(3);
		transHolRegRead = new ModbusSerialTransaction(con1);
		transHolRegRead.setRetries(1);
		transHolRegWrite = new ModbusSerialTransaction(con1);
		transHolRegWrite.setRetries(1);
		
		// Prepare transactions  for TCP_IP
		iptransCoil = new ModbusTCPTransaction(con2);
		iptransCoil.setRetries(3);
		ipwriteTransCoil = new ModbusTCPTransaction(con2);
		ipwriteTransCoil.setRetries(3);
		iptransInReg = new ModbusTCPTransaction(con2);
		iptransInReg.setRetries(3);
		iptransHolRegRead = new ModbusTCPTransaction(con2);
		iptransHolRegRead.setRetries(1);
		iptransHolRegWrite = new ModbusTCPTransaction(con2);
		
		// Prepare registers
		icoilRead = new ReadInputDiscretesRequest(0, 1);
		icoilRead.setHeadless();
		ocoilRead = new ReadCoilsRequest(0, 1);
		ocoilWrite = new WriteCoilRequest();
		
		ireq = new ReadInputRegistersRequest();
		ireq.setWordCount(wCount);
				
		mreq = new ReadMultipleRegistersRequest();
		mreq.setWordCount(wCount);
		
		
		wreq = new WriteMultipleRegistersRequest();
		
		initialized = true;
	}
	
	public Boolean isInitialized() {
		return initialized;
	}
	
	public void close() {
		con1.close();
		con2.close();
	}
	
	/* public synchronized Boolean readInputCoil(Integer adr) throws Exception {
		return readInputCoil(devId, adr);
	}
	public synchronized Boolean readInputCoil(Integer deviceId, Integer adr) throws Exception {
		icoilRead.setUnitID(deviceId);
		icoilRead.setReference(adr);
		transCoil.setRequest(icoilRead);
		
		try {
			transCoil.execute();
			return ((ReadInputDiscretesResponse) transCoil.getResponse()).getDiscreteStatus(0);
		} catch (ModbusException e) {
			errMsg = "Error while executing transaction:" + transCoil.getRequest().getHexMessage() + " in port:" + params.getPortName() + ":" + e.getMessage();
			throw new Exception(errMsg);
		}
	} */
	
	
	/* function to read input coil */
	public synchronized Boolean readCoil(Integer adr,String Protocol) throws Exception {
		return readCoil(devId, adr, Protocol);
	}
	public synchronized Boolean readCoil(Integer deviceId, Integer adr,String Protocol) throws Exception {
		if(Protocol.equals("RTU")) {
			// RTU Transaction
			ocoilRead.setUnitID(deviceId);
			ocoilRead.setReference(adr);
			ocoilRead.setHeadless();
			transCoil.setRequest(ocoilRead);
			
			try {
				transCoil.execute();
				return ((ReadCoilsResponse) transCoil.getResponse()).getCoilStatus(0);
			} catch (ModbusException e) {
				errMsg = "Error while executing transaction:" + transCoil.getRequest().getHexMessage() + " in port:" + params.getPortName() + ":" + e.getMessage();
				throw new Exception(errMsg);
			}
		}else {
			// TCP_IP Transaction
			ocoilRead.setUnitID(deviceId);
			ocoilRead.setReference(adr);
			iptransCoil.setRequest(ocoilRead);
			
			try {
				iptransCoil.execute();
				return ((ReadCoilsResponse) iptransCoil.getResponse()).getCoilStatus(0);
			} catch (ModbusException e) {
				errMsg = "Error while executing transaction:" + iptransCoil.getRequest().getHexMessage() + " in IP Address:" + ipaddr + ":" + e.getMessage();
				throw new Exception(errMsg);
			}
		}
	}
	
	/* function to write output coil */
	public synchronized void writeCoil(Integer adr, Boolean val,String Protocol) throws Exception {
		writeCoil(devId, adr, val, Protocol);
	}
	public synchronized void writeCoil(Integer deviceId, Integer adr, Boolean val,String Protocol) throws Exception {
		if (adr >= 0) {
			if(Protocol.equals("RTU")) {
				// RTU Transaction
				ocoilWrite.setUnitID(deviceId);
				ocoilWrite.setReference(adr);
				ocoilWrite.setCoil(val);
				ocoilWrite.setHeadless();
				writeTransCoil.setRequest(ocoilWrite);
				
				try {
					writeTransCoil.execute();
				} catch (ModbusException e) {
					errMsg = "Error while executing transaction:" + writeTransCoil.getRequest().getHexMessage() + " in port:" + params.getPortName() + ":" + e.getMessage();
					throw new Exception(errMsg);
				}
			}else {
				// TCP_IP Transaction
				ocoilWrite = new WriteCoilRequest();
				ocoilWrite.setUnitID(deviceId);
				ocoilWrite.setReference(adr);
				ocoilWrite.setCoil(val);
				ipwriteTransCoil.setRequest(ocoilWrite);
				
				try {
					ipwriteTransCoil.execute();
				} catch (ModbusException e) {
					errMsg = "Error while executing transaction:" + ipwriteTransCoil.getRequest().getHexMessage() + " in IP Address:" + ipaddr + ":" + e.getMessage();
					throw new Exception(errMsg);
				}
			}
		}
	}
	
	/* function to read current available data in input registers as singed integer  */
	public synchronized Integer readInputReg(Integer adr,String Protocol) throws Exception {
		return readInputReg(devId, adr, Protocol);
	}
	
	public synchronized Integer readInputReg(Integer deviceId, Integer adr,String Protocol) throws Exception {
		if(Protocol.equals("RTU")) {
			// RTU Transaction
			ireq.setUnitID(deviceId);
			ireq.setReference(adr);
			ireq.setHeadless();
			transInReg.setRequest(ireq);
			
			try {
				transInReg.execute();
			} catch (ModbusException e) {
				errMsg = "Error while executing transaction:" + transInReg.getRequest().getHexMessage() + " in port:" + params.getPortName() + ":" + e.getMessage();
				throw new Exception(errMsg);
			}
			
			try {
				if (wCount == 1) {
					return ((ReadInputRegistersResponse) transInReg.getResponse()).getRegisterValue(0);
				} else {
					resHexStr = ((ReadInputRegistersResponse) transInReg.getResponse()).getHexMessage().substring(9).replace(" ", "");
					// reverse lsb and msb if required
					if (isLsbFirst) {
						resHexStr = resHexStr.substring(4) + resHexStr.substring(0,4);
					}
					return ((int) Long.parseLong(resHexStr, 16));
				}
			} catch (NumberFormatException e) {
				errMsg = "Error reading response:" + e.getMessage();
				throw new Exception(errMsg);
			}
		}else {
			// TCP_IP Transaction
			ireq.setUnitID(deviceId);
			ireq.setReference(adr);
			iptransInReg.setRequest(ireq);
			
			try {
				iptransInReg.execute();
			} catch (ModbusException e) {
				errMsg = "Error while executing transaction:" + iptransInReg.getRequest().getHexMessage() + " in IP Address" + ipaddr + ":" + e.getMessage();
				throw new Exception(errMsg);
			}
			
			try {
				if (wCount == 1) {
					return ((ReadInputRegistersResponse) iptransInReg.getResponse()).getRegisterValue(0);
				} else {
					resHexStr = ((ReadInputRegistersResponse) iptransInReg.getResponse()).getHexMessage().substring(9).replace(" ", "");
					// reverse lsb and msb if required
					if (isLsbFirst) {
						resHexStr = resHexStr.substring(4) + resHexStr.substring(0,4);
					}
					return ((int) Long.parseLong(resHexStr, 16));
				}
			} catch (NumberFormatException e) {
				errMsg = "Error reading response:" + e.getMessage();
				throw new Exception(errMsg);
			}
		}
	}
	
	/* function to read current available data in input registers as float  */
	public synchronized Float readInputRegFloat(Integer adr,String Protocol) throws Exception {
		return readInputRegFloat(devId, adr, Protocol);
	}
	
	public synchronized Float readInputRegFloat(Integer deviceId, Integer adr,String Protocol) throws Exception {
		if(Protocol.equals("RTU")) {
			// RTU Transaction
			ireq.setUnitID(deviceId);
			ireq.setReference(adr);
			ireq.setHeadless();
			transInReg.setRequest(ireq);
			
			try {
				transInReg.execute();
			} catch (ModbusException e) {
				errMsg = "Error while executing transaction:" + transInReg.getRequest().getHexMessage() + " in port:" + params.getPortName() + ":" + e.getMessage();
				throw new Exception(errMsg);
			}
			
			try {
				resHexStr = ((ReadInputRegistersResponse) transInReg.getResponse()).getHexMessage().substring(9).replace(" ", "");
				// reverse lsb and msb if required
				if (isLsbFirst) {
					resHexStr = resHexStr.substring(4) + resHexStr.substring(0,4); 
				}
				return (Float.intBitsToFloat((int) Long.parseLong(resHexStr, 16))); // convert the 32 bits hex string to IE-754 float
			} catch (NumberFormatException e) {
				errMsg = "Error reading response:" + e.getMessage();
				throw new Exception(errMsg);
			}
		}else {
			// TCP_IP Transaction
			ireq.setUnitID(deviceId);
			ireq.setReference(adr);
			iptransInReg.setRequest(ireq);
			
			try {
				iptransInReg.execute();
			} catch (ModbusException e) {
				errMsg = "Error while executing transaction:" + iptransInReg.getRequest().getHexMessage() + " in IP Address" + ipaddr + ":" + e.getMessage();
				throw new Exception(errMsg);
			}
			
			try {
				resHexStr = ((ReadInputRegistersResponse) iptransInReg.getResponse()).getHexMessage().substring(9).replace(" ", "");
				// reverse lsb and msb if required
				if (isLsbFirst) {
					resHexStr = resHexStr.substring(4) + resHexStr.substring(0,4); 
				}
				return (Float.intBitsToFloat((int) Long.parseLong(resHexStr, 16))); // convert the 32 bits hex string to IE-754 float
			} catch (NumberFormatException e) {
				errMsg = "Error reading response:" + e.getMessage();
				throw new Exception(errMsg);
			}
		}
	}
	
	/* functions to read current available data in holding registers as signed integer */
	public synchronized Integer readHoldingReg(Integer adr,String Protocol) throws Exception{
		return readHoldingReg(devId, adr, Protocol);
	}

	public synchronized Integer readHoldingReg(Integer deviceId, Integer adr,String Protocol) throws Exception {
		if(Protocol.equals("RTU")) {
			// RTU Transaction
			mreq.setUnitID(deviceId);
			mreq.setReference(adr);
			mreq.setHeadless();	
			transHolRegRead.setRequest(mreq);
			
			try {
				transHolRegRead.execute();
			} catch (ModbusException e) {
				errMsg = "Error while executing transaction:" + transHolRegRead.getRequest().getHexMessage() + " in port:" + params.getPortName() + ":" + e.getMessage();
				throw new Exception(errMsg);
			}
			
			try {
				if (wCount == 1) {
					return ((ReadMultipleRegistersResponse) transHolRegRead.getResponse()).getRegisterValue(0);
				} else {
					resHexStr = ((ReadMultipleRegistersResponse) transHolRegRead.getResponse()).getHexMessage().substring(9).replace(" ", "");
					// reverse lsb and msb if required
					if (isLsbFirst) {
						resHexStr = resHexStr.substring(4) + resHexStr.substring(0,4);
					}
					return ((int) Long.parseLong(resHexStr, 16));
				}
			} catch (NumberFormatException e) {
				errMsg = "Error reading response:" + e.getMessage();
				throw new Exception(errMsg);
			}
		}else {
			// TCP_IP Transaction
			mreq.setReference(adr);
			mreq.setUnitID(deviceId);
			iptransHolRegRead.setRequest(mreq);
			
			try {
				iptransHolRegRead.execute();
			} catch (ModbusException e) {
				errMsg = "Error while executing transaction:" + iptransHolRegRead.getRequest().getHexMessage() + " in IP Address" + ipaddr + ":" + e.getMessage();
				throw new Exception(errMsg);
			}
			
			try {
				if (wCount == 1) {
					return ((ReadMultipleRegistersResponse) iptransHolRegRead.getResponse()).getRegisterValue(0);
				} else {
					resHexStr = ((ReadMultipleRegistersResponse) iptransHolRegRead.getResponse()).getHexMessage().substring(9).replace(" ", "");
					// reverse lsb and msb if required
					if (isLsbFirst) {
						resHexStr = resHexStr.substring(4) + resHexStr.substring(0,4);
					}
					return ((int) Long.parseLong(resHexStr, 16));
				}
			} catch (NumberFormatException e) {
				errMsg = "Error reading response:" + e.getMessage();
				throw new Exception(errMsg);
			}
		}
	}
	
	/* function to read current available data in input registers as float  */
	public Float readHoldingRegFloat(Integer adr,String Protocol) throws Exception{
		return readHoldingRegFloat(devId, adr, Protocol);
	}
	public Float readHoldingRegFloat(Integer deviceId, Integer adr,String Protocol) throws Exception {
		if(Protocol.equals("RTU")) {
			// RTU Transaction
			mreq.setUnitID(deviceId);
			mreq.setReference(adr);
			mreq.setHeadless();
			transHolRegRead.setRequest(mreq);
			try {
					transHolRegRead.execute();
				} catch (ModbusException e) {
					errMsg = "Error while executing transaction:" + transHolRegRead.getRequest().getHexMessage() + " in port:" + params.getPortName() + ":" + e.getMessage();
					throw new Exception(errMsg);
				}
				
				try {
					resHexStr = ((ReadMultipleRegistersResponse) transHolRegRead.getResponse()).getHexMessage().substring(9).replace(" ", "");
					// reverse lsb and msb if required
					if (isLsbFirst) {
						resHexStr = resHexStr.substring(4) + resHexStr.substring(0,4);
					}
					return (Float.intBitsToFloat((int) Long.parseLong(resHexStr, 16))); // convert the 32 bits hex string to IE-754 float
				} catch (NumberFormatException e) {
					errMsg = "Error reading response:" + e.getMessage();
					throw new Exception(errMsg);
				}
		}else {
			// TCP_IP Transaction
			mreq.setReference(adr);
			mreq.setUnitID(deviceId);
			iptransHolRegRead.setRequest(mreq);
			try {
					iptransHolRegRead.execute();
				} catch (ModbusException e) {
					errMsg = "Error while executing transaction:" + iptransHolRegRead.getRequest().getHexMessage() + " in IP Address" + ipaddr + ":" + e.getMessage();
					throw new Exception(errMsg);
				}
				
				try {
					resHexStr = ((ReadMultipleRegistersResponse) iptransHolRegRead.getResponse()).getHexMessage().substring(9).replace(" ", "");
					// reverse lsb and msb if required
					if (isLsbFirst) {
						resHexStr = resHexStr.substring(4) + resHexStr.substring(0,4);
					}
					return (Float.intBitsToFloat((int) Long.parseLong(resHexStr, 16))); // convert the 32 bits hex string to IE-754 float
				} catch (NumberFormatException e) {
					errMsg = "Error reading response:" + e.getMessage();
					throw new Exception(errMsg);
				}
		}
	}
	
	/* function to write into a holding register */
	public synchronized void writeHoldingReg(Integer adr, Integer val,String Protocol) throws Exception {
		writeHoldingReg(devId, adr, val, Protocol);
	}
	public synchronized void writeHoldingReg(Integer deviceId, Integer adr, Integer val,String Protocol) throws Exception {
		if (adr >= 0) {
			// prepare request for the address to be written and write the data
			Register r[] = {null,null};
			r[0] = new SimpleRegister(val); // msb
			r[1] = new SimpleRegister(0);// lsb
			
			if(Protocol.equals("RTU")) {
				// RTU Transaction
				wreq.setUnitID(deviceId);
				wreq.setReference(adr);
				wreq.setRegisters(r);
				wreq.setHeadless();
			
				transHolRegWrite.setRequest(wreq);
				try {
					transHolRegWrite.execute();
				} catch (ModbusException e) {
					errMsg = "Error while executing transaction:" + transHolRegWrite.getRequest().getHexMessage() + " in port:" + params.getPortName() + ":" + e.getMessage();
					throw new Exception(errMsg);
				}
			}else {
				// TCP_IP Transaction
				wreq.setUnitID(deviceId);
				wreq.setReference(adr);
				wreq.setRegisters(r);
				iptransHolRegWrite.setRequest(wreq);
				try {
					iptransHolRegWrite.execute();
				} catch (ModbusException e) {
					errMsg = "Error while executing transaction:" + iptransHolRegWrite.getRequest().getHexMessage() + " in IP Address" + ipaddr + ":" + e.getMessage();
					throw new Exception(errMsg);
				}
			}
		}
	}
}
