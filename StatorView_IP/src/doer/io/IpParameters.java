package doer.io;


public class IpParameters {
	
	//tcp/ip
	
		private Integer ipPort = 502; //default ip 502
		private String ipAdd= null; // the slave's address

	    
	    		
		public String getIpAdd() {
			return ipAdd;
		}

		public void setIpAdd(String ipAdd) {
			
			this.ipAdd = ipAdd;
		}

		public Integer getIpPort() {
			return ipPort;
		}

		public void setIpPort(Integer ipPort) {
			this.ipPort = ipPort;
		}
		
}
