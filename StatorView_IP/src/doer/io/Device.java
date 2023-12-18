package doer.io;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.util.HashMap;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import doer.sv.Configuration;

public class Device {
	
	// class to perform operations on device settings table
	private String op = "Output1";
	private HashMap<String, CommParameters> commParamList = null;
	private HashMap<String, HashMap<String, Parameter>> paramList = null;
	Float floatReading = 0F;
	Parameter param = null;
	DecimalFormat decFormat = new DecimalFormat();
	ScriptEngineManager jsMgr = new ScriptEngineManager();
	ScriptEngine jsEng = jsMgr.getEngineByName("JavaScript");

	private Connection conn = null;
	private Statement stmt = null;
	private Statement stmt2 = null;
	private String protocol = null;
	
	public Device(String output){
		this.op = output;
		commParamList = new HashMap<String, CommParameters>();
		paramList = new HashMap<String, HashMap<String, Parameter>>();
	}

	// function to refresh device settings from db
	public void refresh(String output) throws SQLException {
		this.op = output;
		try {
			// clear existing list
			commParamList.clear();
			paramList.clear();
			
			conn = DriverManager.getConnection(Configuration.DB_URL);
			stmt = conn.createStatement();
			stmt.setQueryTimeout(30);
			stmt2 = conn.createStatement();
			stmt2.setQueryTimeout(30);
			
			// devices
			ResultSet res = stmt.executeQuery("select * from " + Configuration.DEVICE + " where line='" + Configuration.LINE_NAME + "' and line_op='" + op +"'");
			ResultSet res2 = null; 
			String curNm = "";
			while (res.next()) {
				curNm = res.getString("dev_name");
				protocol = res.getString("comm_protocol");
				CommParameters tmpCommParam = new CommParameters();
				if(protocol.equals("RTU")) {
					// RTU Communication
					tmpCommParam.setPortName(res.getString("dev_port"));
					tmpCommParam.setBaudRate(res.getString("baud_rt"));
					tmpCommParam.setDatabits(res.getString("data_bits"));
					tmpCommParam.setParity(res.getString("parity"));
					tmpCommParam.setStopbits(res.getString("stop_bits"));
					tmpCommParam.setEncoding("rtu");
					tmpCommParam.setEcho(false);
					tmpCommParam.setWc(res.getInt("wc"));
					tmpCommParam.setEndianness(res.getString("endianness"));
					tmpCommParam.setProtocol(protocol);
					tmpCommParam.setDevId(Integer.parseInt(res.getString("dev_id")));
					commParamList.put(curNm, tmpCommParam);
				}else {
					// TCP_IP Communication
					
					tmpCommParam.setIpAddress(res.getString("ip_address"));
					tmpCommParam.setIpPort(Integer.parseInt(res.getString("ip_port")));
					tmpCommParam.setWc(res.getInt("wc"));
					tmpCommParam.setEndianness(res.getString("endianness"));
					tmpCommParam.setProtocol(protocol);
					tmpCommParam.setDevId(Integer.parseInt(res.getString("dev_id")));
					commParamList.put(curNm, tmpCommParam);
					
				}
				
				// registers
				res2 = stmt2.executeQuery("select * from DEVICE_PARAM where line='" + Configuration.LINE_NAME + "' and line_op='" + op +"' and dev_name = '" + curNm + "'");
				HashMap<String, Parameter> tmpParam = new HashMap<String, Parameter>();
				while (res2.next()) {
					tmpParam.put(res2.getString("param_name"), new Parameter(res2.getString("param_name"), res2.getString("dev_name"), res2.getInt("param_adr"), res2.getString("conv_factor"), res2.getString("format_text"), res2.getString("reg_type")));
					
				}
				res2.close();
				paramList.put(curNm, tmpParam);
			}
			res.close();
			
			stmt2.close();
			stmt.close();
			conn.close();
			System.out.println("all closed");

		} catch (SQLException se) {
			se.printStackTrace();
			throw new SQLException("Error loading device settings:" + se.getMessage());
		}
	}
	
	// function to get dev comm params
	public CommParameters getCommParameters(String devName) throws Exception {
		if (commParamList.containsKey(devName)) {
			return commParamList.get(devName);
		} else {
			throw new Exception("Key <" + devName + "> not found");
		}
	}
	
	// function to get dev register
	public Integer getDevRegister(String devName, String param) throws Exception {
		if (paramList.containsKey(devName)) {
			if (paramList.get(devName).containsKey(param)) {
				return paramList.get(devName).get(param).getParamAdr();
			} else {
				throw new Exception("Key <" + devName + ":" + param + "> not found");
			}
		} else {
			throw new Exception("Key <" + devName + "> not found");
		}
	}
	
	// function to read value for a register
	public synchronized String readParam(DeviceModbusReader devModRdr, String devName, String paramName, String errAdj) throws Exception {
		param = paramList.get(devName).get(paramName);
		protocol = commParamList.get(devName).getProtocol();
		// 1. read device reading
		if (param.getParamRegType().equals("Coil")) {
			return devModRdr.readCoil(param.getParamAdr(),protocol) ? "true" : "false";
		} else if (param.getParamRegType().equals("Input")) {
				floatReading = (devModRdr.readInputReg(Integer.valueOf(param.getParamAdr()),protocol))/1.0F;
		} else if (param.getParamRegType().equals("Holding")) {
			floatReading = (devModRdr.readHoldingReg(Integer.valueOf(param.getParamAdr()),protocol))/1.0F;
		} else if (param.getParamRegType().equals("Input Float")) {
			floatReading = devModRdr.readInputRegFloat(Integer.valueOf(param.getParamAdr()),protocol);
		} else if (param.getParamRegType().equals("Holding Float")){
			floatReading = devModRdr.readHoldingRegFloat(Integer.valueOf(param.getParamAdr()),protocol);
		} else {
			throw new Exception("Invalid register type:" + param.getParamRegType());	
		}
		
		// 2. apply conversion formula & error correction
		if (!param.getConvFactor().isEmpty() || !errAdj.isEmpty()) {
			floatReading = Float.parseFloat(jsEng.eval(floatReading + param.getConvFactor() + errAdj).toString());
		}
		
		// 3. format
		decFormat.applyPattern(param.getFormatText());
		return (decFormat.format(floatReading)); 
	}
}
