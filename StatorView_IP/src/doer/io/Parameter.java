package doer.io;

public class Parameter {
	// class to perform operations on parameter settings table
	private String paramName = "";
	private String devName = "";
	private Integer paramAdr = 0;
	private String convFactor = "";
	private String formatText = "";
	private String paramRegType = "";
	
	public Parameter() {
		
	}
	public Parameter(String paramName, String devName, Integer paramAdr, String convFactor, String formatText, String paramRegType) {
		this.setParamName(paramName);
		this.setDevName(devName);
		this.setParamAdr(paramAdr);
		this.setConvFactor(convFactor);
		this.setFormatText(formatText);
		this.setParamRegType(paramRegType);
	}

	public String getParamName() {
		return paramName;
	}

	public void setParamName(String paramName) {
		this.paramName = paramName;
	}

	public String getDevName() {
		return devName;
	}

	public void setDevName(String devName) {
		this.devName = devName;
	}

	public Integer getParamAdr() {
		return paramAdr;
	}

	public void setParamAdr(Integer paramAdr) {
		this.paramAdr = paramAdr;
	}

	public String getConvFactor() {
		return convFactor;
	}

	public void setConvFactor(String convFactor) {
		this.convFactor = convFactor;
	}

	public String getFormatText() {
		return formatText;
	}

	public void setFormatText(String formatText) {
		this.formatText = formatText;
	}

	public String getParamRegType() {
		return paramRegType;
	}

	public void setParamRegType(String paramRegType) {
		this.paramRegType = paramRegType;
	}
	
}
