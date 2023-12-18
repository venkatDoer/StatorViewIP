package doer.sv;


import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;

import javax.imageio.ImageIO;
import javax.swing.JDialog;
import javax.swing.JOptionPane;


/**
 * @author Venkatesan Selvaraj @ Doer
 */

// class to work on application configuration values
public class Configuration {

	// global configuration parameters with its default values
	// params which are stored and managed using a config table
	// GLOBAL
	public static String LINE_NAME = "ALL";
	public static String LICENCEE_NAME = "DOER PUMPS (PVT) LIMITED";
	public static String LICENCEE_ADR_1 = "1234 STREET";
	public static String LICENCEE_ADR_2 = "CITY - 123 456.";
	public static String NUMBER_OF_LINES = "1";
	public static String REG_CODE = ""; 
	public static String APP_DB_NAME = ""; 
	public static boolean IS_TRIAL_ON = false; 
	public static String IS_DB_LOCKED = "0";
	
	// DEVICES
	public static String LAST_USED_PLC_ID = "1";
	public static String LAST_USED_PLC_PORT = "";
	public static String LAST_USED_TEMP_PLC_ID = "2";
	public static String LAST_USED_TEMP_PLC_PORT = "";
	public static String LAST_USED_MUL_MET_PORT = "";
	public static String LAST_USED_WEBCAM="0";
	
	// STATOR PARAMS
	public static String LAST_USED_STATOR_TYPE = "SAMPLE STATOR TYPE";
	
	// CAPTURE
	public static String LAST_USED_CAPTURE_METHOD = "A";
	public static String LAST_LIVE_READING = "K";
	public static String NUMBER_OF_STATIONS = "2";

	// BACKUP
	public static String LAST_USED_BACKUP_DURATION = "7";
	public static String LAST_USED_BACKUP_LOCATION = "";
	public static String LAST_BACKUP_DATE = "NA";
	public static String NEXT_BACKUP_DATE = "";
	
	// REPORTS
	public static String REP_SHOW_APP_WMARK = "1";
	
	// USER
	public static String USER = "";
	public static String USER_IS_ADMIN = "0";
	public static String USER_HAS_STATOR_ACCESS = "0";
	public static String USER_HAS_MODIFY_ACCESS = "0";
	public static boolean APP_DEBUG_MODE = false;
	
	// CAPTURE
	public static String REL_TIME_START_COIL = "8";
	public static String REL_TIME_RUN_COIL = "8";
	public static String REL_TIME_COM_COIL = "8";
	public static String REL_TIME_INS_RES = "10";
	public static String REL_TIME_HV = "5";
	public static String REL_TIME_DIR = "3";
	public static String REL_TIME_SURGE = "4";
	public static String IS_RES_DISABLED = "0";
	public static String IS_COM_RES_DISABLED = "0";
	public static String IS_INS_RES_DISABLED = "0";
	public static String IS_INS_RES_AFTER_HV_DISABLED = "0";
	public static String IS_HV_DISABLED = "0";
	public static String IS_SURGE_DISABLED = "0";
	public static String IS_DIR_TEST_DISABLED = "0";
	public static String NUMBER_OF_SURGE_IMG = "4";
	public static String IS_SURGE_WAVE_CAPTURED = "0";
	public static String SURGE_WAVE_INTERVAL = "2";
	//public static String IS_SCANNER_ENABLED = "0";
	public static String SNO_GEN_METHOD = "A";
	public static String IS_PREPEND_DATE_TO_SNO = "0";
	public static String RECENT_STATOR_SNO = "0000";
	public static String IS_MODEL_FROM_BARCODE = "NO";
	public static String IS_MODEL_FROM_BARCODE_DELIMITER_BASED = "YES";
	public static String MODEL_DELIMITER_IN_BARCODE = ",";
	public static String MODEL_LOCATION_IN_BARCODE = "1";
	public static String MODEL_START_POS_IN_BARCODE = "1";
	public static String MODEL_END_POS_IN_BARCODE = "5";
	public static String IS_CONFIRM_SNO = "NO";
	public static String IS_GEN_SNO_FOR_FAILED_TEST = "NO";
	
	public static String IS_QR_PRINT_ENABLED = "0";
	public static String IS_QR_PRINT_ENABLED_FOR_FAIL = "0";
	public static String QR_LINE3 = "SM";
	public static String QR_LINE4 = "NO";
	public static String QR_IS_INCLUDE_LINE2 = "NO";
	public static String QR_IS_INCLUDE_LINE3 = "NO";
	public static String QR_IS_INCLUDE_LINE4 = "NO";
	
	public static String QR_CODE_PRINTER_PATH = "";
	
	// other global params
	public static String APP_DIR = "c:";
	public static String APP_AUTHOR = "Doer";
	public static String APP_VERSION = "StatorView V 2.0";
	public static String CONFIG_DIR = "\\config";
	public static String CONFIG_FILE_LIC = "\\app.lic";
	public static String DATA_DIR = "\\data";
	public static Boolean IS_SURGE_IN_PROGRESS = false;
	public static String CUR_TEST_SLNO = "";
	public static Integer CUR_SURGE_IMG_COUNT = 0;
	//public static Long MAXIMUM_ALLOWED_READINGS = 9223372036854775806L;
	
	public static String JDBC_DRIVER = "org.sqlite.JDBC";
	public static String DB_NAME = "\\statorview.db";
	public static String JDBC_NAME = "jdbc:sqlite";
	public static String DB_URL = "";
	
	// app table names	
	public static String CALIBRATION = "CALIBRATION";
	public static String CONFIG = "CONFIG";
	public static String STATOR_TYPE = "STATOR_TYPE";
	public static String READING_DETAIL = "READING_DETAIL";
	public static String DEVICE = "DEVICE";
	public static String SURGE_IMAGE = "SURGE_IMAGE";
	public static String ERROR_ADJ = "ERROR_ADJ";
	
	public static java.util.Date today = Calendar.getInstance().getTime();
	public static SimpleDateFormat reqDtFormat = new SimpleDateFormat("yyMMdd");
	
	// constructor
	public Configuration() {
	}
	
	// initialize
	public static boolean initialize() {
		// create config table if the software runs first time
		try {
			if (APP_DB_NAME.isEmpty()) {
				APP_DB_NAME = APP_DIR + DATA_DIR + DB_NAME;
				
				// create DB directory if it does not exist (may happen when software runs first time)
				File dbDir = new File(APP_DIR + DATA_DIR);
				if (!dbDir.exists()) {
					dbDir.mkdir();
				}
			}
			DB_URL = JDBC_NAME + ":" + APP_DB_NAME;
			
			Connection conn = DriverManager.getConnection(DB_URL);
			Statement stmt = conn.createStatement();
			stmt.setQueryTimeout(30);
			
			ResultSet res = null;
			boolean needInsert = false;
			try {
				res = stmt.executeQuery("select * from " + Configuration.CONFIG + " where line='" + LINE_NAME +"'");
			} catch (SQLException se) {
				if (se.getMessage().contains("no such table")) {
					stmt.executeUpdate("create table " + Configuration.CONFIG + " (line text, name text, value text)");
					needInsert = true;
				}
			}
			if (res != null) {
				if (!res.next()) {
					needInsert = true;
				}
			}
			
			if (needInsert) {
				stmt.executeUpdate("insert into " + Configuration.CONFIG + " values ('" + LINE_NAME + "','LICENCEE_NAME','" + LICENCEE_NAME + "')");
				stmt.executeUpdate("insert into " + Configuration.CONFIG + " values ('" + LINE_NAME + "','LICENCEE_ADR_1','" + LICENCEE_ADR_1 + "')");
				stmt.executeUpdate("insert into " + Configuration.CONFIG + " values ('" + LINE_NAME + "','LICENCEE_ADR_2','" + LICENCEE_ADR_2 + "')");
				stmt.executeUpdate("insert into " + Configuration.CONFIG + " values ('" + LINE_NAME + "','NUMBER_OF_LINES','" + NUMBER_OF_LINES + "')");
				stmt.executeUpdate("insert into " + Configuration.CONFIG + " values ('" + LINE_NAME + "','NUMBER_OF_STATIONS','" + NUMBER_OF_STATIONS + "')");
				stmt.executeUpdate("insert into " + Configuration.CONFIG + " values ('" + LINE_NAME + "','APP_DB_NAME','" + APP_DB_NAME  + "')");
				stmt.executeUpdate("insert into " + Configuration.CONFIG + " values ('" + LINE_NAME + "','LAST_USED_MUL_MET_PORT','" + LAST_USED_MUL_MET_PORT + "')");
				stmt.executeUpdate("insert into " + Configuration.CONFIG + " values ('" + LINE_NAME + "','LAST_USED_PLC_ID','" + LAST_USED_PLC_ID + "')");
				stmt.executeUpdate("insert into " + Configuration.CONFIG + " values ('" + LINE_NAME + "','LAST_USED_PLC_PORT','" + LAST_USED_PLC_PORT + "')");
				stmt.executeUpdate("insert into " + Configuration.CONFIG + " values ('" + LINE_NAME + "','LAST_USED_TEMP_PLC_ID','" + LAST_USED_TEMP_PLC_ID + "')");
				stmt.executeUpdate("insert into " + Configuration.CONFIG + " values ('" + LINE_NAME + "','LAST_USED_TEMP_PLC_PORT','" + LAST_USED_TEMP_PLC_PORT + "')");
				stmt.executeUpdate("insert into " + Configuration.CONFIG + " values ('" + LINE_NAME + "','LAST_USED_WEBCAM','" + LAST_USED_WEBCAM + "')");
				stmt.executeUpdate("insert into " + Configuration.CONFIG + " values ('" + LINE_NAME + "','LAST_USED_STATOR_TYPE','" + LAST_USED_STATOR_TYPE + "')");
				stmt.executeUpdate("insert into " + Configuration.CONFIG + " values ('" + LINE_NAME + "','LAST_USED_CAPTURE_METHOD','" + LAST_USED_CAPTURE_METHOD + "')");
				stmt.executeUpdate("insert into " + Configuration.CONFIG + " values ('" + LINE_NAME + "','LAST_USED_BACKUP_DURATION','" + LAST_USED_BACKUP_DURATION  + "')");
				LAST_USED_BACKUP_LOCATION = APP_DIR + "\\backup";
				stmt.executeUpdate("insert into " + Configuration.CONFIG + " values ('" + LINE_NAME + "','LAST_USED_BACKUP_LOCATION','" + LAST_USED_BACKUP_LOCATION + "')");
				stmt.executeUpdate("insert into " + Configuration.CONFIG + " values ('" + LINE_NAME + "','LAST_BACKUP_DATE','" + LAST_BACKUP_DATE + "')");
				stmt.executeUpdate("insert into " + Configuration.CONFIG + " values ('" + LINE_NAME + "','NEXT_BACKUP_DATE','" + NEXT_BACKUP_DATE + "')");
				stmt.executeUpdate("insert into " + Configuration.CONFIG + " values ('" + LINE_NAME + "','REP_SHOW_APP_WMARK','" + REP_SHOW_APP_WMARK + "')");
				
				stmt.executeUpdate("insert into " + Configuration.CONFIG + " values ('" + LINE_NAME + "','REL_TIME_START_COIL','" + REL_TIME_START_COIL + "')");
				stmt.executeUpdate("insert into " + Configuration.CONFIG + " values ('" + LINE_NAME + "','REL_TIME_RUN_COIL','" + REL_TIME_RUN_COIL + "')");
				stmt.executeUpdate("insert into " + Configuration.CONFIG + " values ('" + LINE_NAME + "','REL_TIME_COM_COIL','" + REL_TIME_COM_COIL + "')");
				stmt.executeUpdate("insert into " + Configuration.CONFIG + " values ('" + LINE_NAME + "','REL_TIME_INS_RES','" + REL_TIME_INS_RES + "')");
				stmt.executeUpdate("insert into " + Configuration.CONFIG + " values ('" + LINE_NAME + "','REL_TIME_HV','" + REL_TIME_HV + "')");
				stmt.executeUpdate("insert into " + Configuration.CONFIG + " values ('" + LINE_NAME + "','REL_TIME_DIR','" + REL_TIME_DIR + "')");
				stmt.executeUpdate("insert into " + Configuration.CONFIG + " values ('" + LINE_NAME + "','REL_TIME_SURGE','" + REL_TIME_SURGE + "')");
				stmt.executeUpdate("insert into " + Configuration.CONFIG + " values ('" + LINE_NAME + "','IS_RES_DISABLED','" + IS_RES_DISABLED + "')");
				stmt.executeUpdate("insert into " + Configuration.CONFIG + " values ('" + LINE_NAME + "','IS_COM_RES_DISABLED','" + IS_COM_RES_DISABLED + "')");
				stmt.executeUpdate("insert into " + Configuration.CONFIG + " values ('" + LINE_NAME + "','IS_INS_RES_DISABLED','" + IS_INS_RES_DISABLED + "')");
				stmt.executeUpdate("insert into " + Configuration.CONFIG + " values ('" + LINE_NAME + "','IS_INS_RES_AFTER_HV_DISABLED','" + IS_INS_RES_AFTER_HV_DISABLED + "')");
				stmt.executeUpdate("insert into " + Configuration.CONFIG + " values ('" + LINE_NAME + "','IS_HV_DISABLED','" + IS_HV_DISABLED + "')");
				stmt.executeUpdate("insert into " + Configuration.CONFIG + " values ('" + LINE_NAME + "','IS_SURGE_DISABLED','" + IS_SURGE_DISABLED + "')");
				stmt.executeUpdate("insert into " + Configuration.CONFIG + " values ('" + LINE_NAME + "','IS_DIR_TEST_DISABLED','" + IS_DIR_TEST_DISABLED + "')");
				stmt.executeUpdate("insert into " + Configuration.CONFIG + " values ('" + LINE_NAME + "','NUMBER_OF_SURGE_IMG','" + NUMBER_OF_SURGE_IMG + "')");
				stmt.executeUpdate("insert into " + Configuration.CONFIG + " values ('" + LINE_NAME + "','IS_SURGE_WAVE_CAPTURED','" + IS_SURGE_WAVE_CAPTURED + "')");
				stmt.executeUpdate("insert into " + Configuration.CONFIG + " values ('" + LINE_NAME + "','SURGE_WAVE_INTERVAL','" + SURGE_WAVE_INTERVAL + "')");
				stmt.executeUpdate("insert into " + Configuration.CONFIG + " values ('" + LINE_NAME + "','SNO_GEN_METHOD','" + SNO_GEN_METHOD + "')");
				stmt.executeUpdate("insert into " + Configuration.CONFIG + " values ('" + LINE_NAME + "','IS_PREPEND_DATE_TO_SNO','" + IS_PREPEND_DATE_TO_SNO + "')");
				stmt.executeUpdate("insert into " + Configuration.CONFIG + " values ('" + LINE_NAME + "','RECENT_STATOR_SNO','" + RECENT_STATOR_SNO + "')");
				stmt.executeUpdate("insert into " + Configuration.CONFIG + " values ('" + LINE_NAME + "','IS_MODEL_FROM_BARCODE','" + IS_MODEL_FROM_BARCODE + "')");
				stmt.executeUpdate("insert into " + Configuration.CONFIG + " values ('" + LINE_NAME + "','IS_MODEL_FROM_BARCODE_DELIMITER_BASED','" + IS_MODEL_FROM_BARCODE_DELIMITER_BASED + "')");
				stmt.executeUpdate("insert into " + Configuration.CONFIG + " values ('" + LINE_NAME + "','MODEL_DELIMITER_IN_BARCODE','" + MODEL_DELIMITER_IN_BARCODE + "')");
				stmt.executeUpdate("insert into " + Configuration.CONFIG + " values ('" + LINE_NAME + "','MODEL_LOCATION_IN_BARCODE','" + MODEL_LOCATION_IN_BARCODE + "')");
				stmt.executeUpdate("insert into " + Configuration.CONFIG + " values ('" + LINE_NAME + "','MODEL_START_POS_IN_BARCODE','" + MODEL_START_POS_IN_BARCODE + "')");
				stmt.executeUpdate("insert into " + Configuration.CONFIG + " values ('" + LINE_NAME + "','MODEL_END_POS_IN_BARCODE','" + MODEL_END_POS_IN_BARCODE + "')");
				stmt.executeUpdate("insert into " + Configuration.CONFIG + " values ('" + LINE_NAME + "','IS_CONFIRM_SNO','" + IS_CONFIRM_SNO + "')");
				stmt.executeUpdate("insert into " + Configuration.CONFIG + " values ('" + LINE_NAME + "','IS_GEN_SNO_FOR_FAILED_TEST','" + IS_GEN_SNO_FOR_FAILED_TEST + "')");
				stmt.executeUpdate("insert into " + Configuration.CONFIG + " values ('" + LINE_NAME + "','IS_DB_LOCKED','" + IS_DB_LOCKED + "')");
				stmt.executeUpdate("insert into " + Configuration.CONFIG + " values ('" + LINE_NAME + "','IS_QR_PRINT_ENABLED','" + IS_QR_PRINT_ENABLED + "')");
				stmt.executeUpdate("insert into " + Configuration.CONFIG + " values ('" + LINE_NAME + "','IS_QR_PRINT_ENABLED_FOR_FAIL','" + IS_QR_PRINT_ENABLED_FOR_FAIL + "')");
				// QR code printer path
				String printerPath = "";
				try {
					InetAddress addr;
				    addr = InetAddress.getLocalHost();
				    printerPath = "\\\\" + addr.getHostName() + "\\" + "barcode-printer";
				} catch (Exception e) {
					System.out.println("Error finding the host name");
				}
				stmt.executeUpdate("insert into " + Configuration.CONFIG + " values ('" + LINE_NAME + "','QR_CODE_PRINTER_PATH','" + printerPath + "')");
				
				stmt.close();
				conn.close();
			}
			if (res != null) {
				res.close();
			}
			stmt.close();
			conn.close();
		} catch (Exception sqle) {
			JOptionPane.showMessageDialog(new JDialog(), "Error creating config table:" + sqle.getMessage() + "\nDB Name:" + APP_DB_NAME);
			return false;
		}
		return true;
	}
	
	// function to load configurations from config file and set other globals
	public static boolean loadConfigValues() {
		// open db and load the config values;
		try {
			Connection conn = DriverManager.getConnection(DB_URL);
			Statement stmt = conn.createStatement();
			stmt.setQueryTimeout(30);
			ResultSet res = null;
			
			try {
				// fetch and load the result
				res = stmt.executeQuery("select * from " + Configuration.CONFIG + " where line='" + LINE_NAME +"'");
				
				String configName = "";
				String configValue = "";
				
				while (res.next()) {
					configName = res.getString("name");
					configValue = res.getString("value");
					Configuration.class.getField(configName).set(null, configValue);
				}
			
			} catch (Exception sqle) {
				JOptionPane.showMessageDialog(new JDialog(), "Error loading config:" + sqle.getMessage());
				return false;
			} finally {
				res.close();
				stmt.close();
				conn.close();
			}
		} catch (Exception ie) {
			// ignore
		}
		return true;
	}
	
	// function to save set of config values
	public static void saveConfigValues(String ... names) {
		try {
			Connection conn = DriverManager.getConnection(DB_URL);
			Statement stmt = conn.createStatement();
			stmt.setQueryTimeout(30);
			
			String qry = null;
			for (int i=0; i<names.length; i++) {
				qry = "update " + Configuration.CONFIG + " set value='" + Configuration.class.getField(names[i]).get(null) + "' where line = '" + LINE_NAME + "' and name='" + names[i] + "'";
				if (stmt.executeUpdate(qry) == 0) {
					// config. entry is missed, probably due to upgrade. Hence, insert it
					stmt.executeUpdate("insert into " + Configuration.CONFIG + " values ('" + LINE_NAME + "','" + names[i] + "','" + Configuration.class.getField(names[i]).get(null) + "')");
				}
			}
			
			stmt.close();
			conn.close();
		} catch (Exception sqle) {
			JOptionPane.showMessageDialog(new JDialog(), "Error updating config:" + sqle.getMessage());
			return;
		}
	}
	
	// function to save config values that are common across assembly lines
	public static void saveCommonConfigValues(String ... names) {
		try {
			Connection conn = DriverManager.getConnection(DB_URL);
			Statement stmt = conn.createStatement();
			stmt.setQueryTimeout(30);
			
			String qry = null;
			for (int i=0; i<names.length; i++) {
				qry = "update " + Configuration.CONFIG + " set value='" + Configuration.class.getField(names[i]).get(null) + "' where name='" + names[i] + "'";
				if (stmt.executeUpdate(qry) == 0) {
					// config. entry is missed, probably due to upgrade. Hence, insert it
					stmt.executeUpdate("insert into " + Configuration.CONFIG + " values ('" + LINE_NAME + "','" + names[i] + "','" + Configuration.class.getField(names[i]).get(null) + "')");
				}
			}
			
			stmt.close();
			conn.close();
		} catch (Exception sqle) {
			JOptionPane.showMessageDialog(new JDialog(), "Error updating config:" + sqle.getMessage());
			return;
		}
	}
	
	// function to take a back up of data base
	public static void backupData(String bkDir) throws Exception {
		// create back up dir if not exist
		File dirBkDir = new File(bkDir);
		
		if (!dirBkDir.exists()) {;
			boolean res = (new File(bkDir)).mkdir();
			if (!res) {
				throw new Exception ("Error: Unable to create backup folder:" + bkDir);
			}
		}
		
		// take a backup of db
		String sourceFile = APP_DB_NAME;
		String timeStamp = reqDtFormat.format(today);
		String destFile = bkDir + "\\" + "pumpviewpro" + timeStamp + ".db";
		
		String cmd = "cmd.exe /c echo F | xcopy /YV  \"" + sourceFile + "\" \"" + destFile + "\"";
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
				er.close();
				in.close();
				p.destroy();
				throw new Exception("Error while taking backup of " + sourceFile + ":" + filesError);
			}
		}
		// ignore the output if any
		Thread.sleep(1000);
		if (in.ready()) {
			while ((tmpStr = in.readLine()) != null) {
				filesError += tmpStr + "\n";
			}
		}
		
		
		er.close();
		in.close();
		p.destroy();
		
		// retain only recent five backups, remove rest of them
		File[] files = dirBkDir.listFiles();

		Arrays.sort(files, new Comparator<File>(){
			public int compare(File f1, File f2)
			{
				return Long.valueOf(f1.lastModified()).compareTo(f2.lastModified());
			} 
		});
		
		int retainCopy = 5;
		for(int i=files.length-retainCopy-1; i>=0; i--) {
			files[i].delete();
		}
	}
	
	// function to store surge wave in db
	public static void storeSurgeWave(BufferedImage img) {
		try {
			// convert image to byte array
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ImageIO.write( img, "jpg", baos );
			baos.flush();
			byte[] imageInByte = baos.toByteArray();
			baos.close();
			
			Connection conn = DriverManager.getConnection(DB_URL);
			Statement stmt = conn.createStatement();
			stmt.setQueryTimeout(30);

			String qry = "insert into " + SURGE_IMAGE + "(test_slno, wave_img) values (?,?)";
			PreparedStatement prepStmt = null;
			try {
				prepStmt = conn.prepareStatement(qry);
				prepStmt.setString(1, CUR_TEST_SLNO);
				prepStmt.setBytes(2, imageInByte);
				prepStmt.executeUpdate();
			} catch (SQLException se) {
				if (se.getMessage().contains("no such table")) {
					stmt.executeUpdate("create table " + SURGE_IMAGE + " (test_slno integer, wave_img blob)");
					prepStmt = conn.prepareStatement(qry);
					prepStmt.setString(1, CUR_TEST_SLNO);
					prepStmt.setBytes(2, imageInByte);
					prepStmt.executeUpdate();
				}
			}
			
			prepStmt.close();
			stmt.close();
			conn.close();
		} catch (Exception sqle) {
			sqle.printStackTrace();
			//JOptionPane.showMessageDialog(new JDialog(), "Error inserting surge wave:" + sqle.getMessage());
			return;
		}
	}
	
	// function to get latest config value of given param
	public static String getCommonConfigValue(String paramName) {
		String tmpValue = "";
		try {
			Connection conn = DriverManager.getConnection(DB_URL);
			Statement stmt = conn.createStatement();
			stmt.setQueryTimeout(30);
			
			ResultSet res = stmt.executeQuery("select * from CONFIG where name='" + paramName + "' limit 1");
			
			if (res.next()) {
				tmpValue = res.getString("value");
			}
			res.close();
			stmt.close();
			conn.close();
		} catch (SQLException se) {
			JOptionPane.showMessageDialog(new JDialog(), "Error loading config " + paramName + ":" + se.getMessage());
		}
		return tmpValue;
	}
	
	// function to find next number
	public static String findNextNo(String lastUsedSNo) {
		String nextSno = "";
		Long lastNoPlusOne = 0L;
		
		// 1.split the string to numbers and strings
		String[] lstStr = lastUsedSNo.split("(?<=\\D)(?=\\d)|(?<=\\d)(?=\\D)");
		// 2. increment the last instance of number by 1
		int i = 0;
		for (i=lstStr.length-1 ; i>= 0; i--) {
			try {
				lastNoPlusOne = Long.parseLong(lstStr[i]) + 1;
				lastNoPlusOne = lastNoPlusOne > 0 ? lastNoPlusOne : 0;
				DecimalFormat formatNumber = new DecimalFormat(lstStr[i].replaceAll("\\d","0"));
				lstStr[i] = formatNumber.format(lastNoPlusOne);
				break;
			} catch (NumberFormatException e) {
				continue;
			}
		}
		// 3. join the numbers
		for(int j=0; j<lstStr.length; j++) {
			nextSno += lstStr[j];
		}
		// 4. start with 1 if no number deducted
		if (i < 0) {
			nextSno += "1";
		}
		return nextSno;
	}
	
	// function to set and get next SNo
	public static String setAndGetSNo(String testSNo) {
		String nextSNo = "";
		try {
			Connection conn = DriverManager.getConnection(DB_URL);
			Statement stmt = conn.createStatement();
			stmt.setQueryTimeout(30);
			
			// wait for your turn and mark your turn
			while(stmt.executeUpdate("update CONFIG set value='1' where name='IS_DB_LOCKED'") == 0) {
				// continue
			}
			// get last sno
			ResultSet res = stmt.executeQuery("select value from config where name='RECENT_STATOR_SNO' limit 1");
			// increment by 1, update the master and map to the requested test
			if (res.next()) {
				String lastUsedSNo = res.getString("value");
				nextSNo = findNextNo(lastUsedSNo);
				if (IS_PREPEND_DATE_TO_SNO.equals("1")) {
					today = Calendar.getInstance().getTime();
					nextSNo = reqDtFormat.format(today) + "-" + nextSNo;
				}
				
				stmt.executeUpdate("update CONFIG set value='" + nextSNo + "' where name='RECENT_STATOR_SNO'");
				stmt.executeUpdate("update CONFIG set value='0' where name='IS_DB_LOCKED'"); // I am done with my turn
				stmt.executeUpdate("update READING_DETAIL set stator_slno='" + nextSNo + "' where test_slno='" + testSNo + "'");
			}
			res.close();
			stmt.close();
			conn.close();
		} catch (SQLException e) {
			JOptionPane.showMessageDialog(new JDialog(), "Error setting motor serial number " + e.getMessage());
		}
		return nextSNo;
	}
	
	// function to fetch only number part of a string
	public static String numberPartOfString(String SNo) {
		return SNo.replaceAll("[^0-9]+", "");
	}
}
