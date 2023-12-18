package doer.io;

import net.wimpi.modbus.util.SerialParameters;

//serial parameters + custom parameters required for our application
public class CommParameters extends SerialParameters {
	private Integer devId = 1;
	private Integer wc=1;
	private String endianness = "MSB First";
	
	//TCP_IP Parameters
	private String ipAddress = null;
	private Integer ipPort = 2200;
	private String protocol = "RTU";
	
	
	
	public String getIpAddress() {
		return ipAddress;
	}

	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}

	public Integer getIpPort() {
		return ipPort;
	}

	public void setIpPort(Integer ipPort) {
		this.ipPort = ipPort;
	}

	public String getProtocol() {
		return protocol;
	}

	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}

	public Integer getDevId() {
		return devId;
	}

	public void setDevId(Integer devId) {
		this.devId = devId;
	} 

	public Integer getWc() {
		return wc;
	}

	public void setWc(Integer wc) {
		this.wc = wc;
	} 
	
	public String getEndianness() {
		return endianness;
	}

	public void setEndianness(String endianess) {
		this.endianness = endianess;
	}
}