/*
 * Created by JFormDesigner on Mon Dec 01 19:20:02 IST 2014
 */

package doer.sv;

import java.awt.event.*;
import javax.swing.table.*;
import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstraints;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.net.InetAddress;
import java.nio.channels.SelectableChannel;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.EventObject;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.DefaultCellEditor;
import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

import purejavahidapi.*;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamMotionDetector;
import com.github.sarxos.webcam.WebcamMotionEvent;
import com.github.sarxos.webcam.WebcamMotionListener;
import com.github.sarxos.webcam.WebcamPanel;

import doer.io.Device;
import doer.io.DeviceModbusReader;
import doer.print.PrintBarcode;
import doer.sv.StatorType.SelectionListener;

/**
 * @author VENKATESAN SELVARAJ
 */
public class StatorView extends JFrame {
	public StatorView() {
		initComponents();
		customInit();
	}
	
	// custom variable - begin
	public String curStatorType = "";
	private String curStatorPhase = "";
	private String curStatorCon = "";
	private String curStatorDir = "";
	public String curVendorRef = "";
	private DefaultListModel errLogModel = null; 
	private String devGrindexProtocol = "RTU";
	private String devPLCProtocol = "RTU";
	//private Integer defCurRow = 0;
	private Integer curRow = 0;
	private Integer tblTotCol = 0;
	private Integer testNo = 0;
	//private String lastUsedSNo = "";
	private String scannedSNo = "";
	private Connection conn = null;
	private Statement stmt = null;
	private Device devCfg = null;
	
	private Integer startR1 = -1;
	private Integer startR2 = -1;
	private Integer startR3 = -1;
	private Integer startIR = -1;
	private Integer startHV1 = -1;
	private Integer startHV2 = -1;
	private Integer startIRAfterHV = -1;
	private Integer startSurge1 = -1;
	private Integer startSurge2 = -1;
	private Integer startDirFwd = -1;
	private Integer startDirRev = -1;
	private Integer startDis = -1;
	
	private Integer selAutoMan = -1;
	private Integer selPhase = -1;
	private Integer sigAutoStart = -1;
	
	private Integer sigHVFail = -1;
	private Integer sigDirPass = -1;
	private Integer sigSurgePass = -1;
	private Integer sigSurgeFail = -1;
	private Integer sigStation1 = -1;
	private Integer sigStation2= -1;
	private Integer sigEmergency= -1;
	
	private SimpleDateFormat reqDtFormat = new SimpleDateFormat("dd-MM-yyyy");
	private SimpleDateFormat reqTmFormat = new SimpleDateFormat("HH:mm:ss");
	private SimpleDateFormat dbDtFormat = new SimpleDateFormat("yyyy-MM-dd");
	private SimpleDateFormat dbDtTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	java.util.Date today = Calendar.getInstance().getTime();
	private DecimalFormat dotOne = new DecimalFormat("#0.0");
	private DecimalFormat dotTwo = new DecimalFormat("#0.00");
	private DecimalFormat dotThree = new DecimalFormat("#0.000");
	private DecimalFormat dotZero = new DecimalFormat("#");
	
	private Boolean captureInProgress = false;
	private Boolean autoCaptureInProgress = false;
	private Boolean captureError = false;
	private Boolean sqlError = false;
	private Boolean firstRetest = false; // enable Retest_on button for fail condition
	private Color signalColor = Color.yellow;
	private String capStatus = "";
	private Cursor waitCursor = new Cursor(Cursor.WAIT_CURSOR);
	
	private Thread threadAutoCapture = null;
	private Boolean isAutoCaptureOn = false;
	private Thread threadLiveRead = null;
	private Thread threadBlink = null;
	private boolean blinkEmergency = false;
	
	// device comm
	boolean deviceInitialized = false;
	boolean dataReadError = false;
	
	DeviceModbusReader devGrindex = null;
	DeviceModbusReader devPLC = null;
	DeviceModbusReader devPLC2 = null;
	boolean PLCPortError = false;
	DeviceModbusReader devRes = null;  // udain
	//DeviceSerialReader devSerRdr = null; // haiku
	HidDevice devHid = null;
	boolean serPortError = false;
	private Float readData = 0F;
	private String curResData = "";
	private Double[] resErrAdj1 = new Double[10];
	private Double[] resErrAdj2 = new Double[10];
	private Double[] resErrAdj3 = new Double[10];
	private Double[] insResErrAdj = new Double[10];
	Integer curResOn = 1;
	
	private String curIRData = "";
	private byte[] curIRBytes = new byte[15];
	private Integer noOfIRBytes = 0;
	private String dig = "";
	private Boolean ignoreRemaining = false;
	
	boolean lastHVTestFailed = false;
	boolean lastSurgeFailed = false;
	boolean lastDirFailed = false;
	Integer lastCoilNo = 0;
	String curStatorSNo = "";
		
	java.util.Date logTime = null;
	Webcam webCam = null;
	WebcamPanel pnlWebCam = null;
	WebcamMotion motDWebCam = null;
	BufferedImage curImg = null;
	Integer availCount = 0;
	Integer waitTime = 0;
	Float curVolt = 0F;
	byte[] serCmd = {0x10, 0x02, 0x42, 0x00, 0x00, 0x42, 0x10, 0x03}; // mm
	//String serCmd = "FETCH?\n"; // haiku
	// stator perf factors config
	private Float declaredResLow1 = -1.0F;
	private Float declaredResUp1 = 99999.0F;
	private Float declaredResLow2 = -1.0F;
	private Float declaredResUp2 = 99999.0F;
	private Float declaredResLow3 = -1.0F;
	private Float declaredResUp3 = 99999.0F;
	private Float declaredInsResL = -1.0F;
	private Float declaredMAH = 99999.0F;
	private LinkedList<JLabel> stationList = new LinkedList<JLabel>();
	private Integer curActiveStation = -1;
	private String curTestMode = "A";
	private String tmpTestMode = "";
	
	private ImageIcon idleImg = new ImageIcon(getClass().getResource("/img/idle.png"));
	private ImageIcon runImg = new ImageIcon(getClass().getResource("/img/running.png"));
	private ImageIcon passImg = new ImageIcon(getClass().getResource("/img/pass.png"));
	private ImageIcon failImg = new ImageIcon(getClass().getResource("/img/fail.png"));
	
	// all used in calculate result
	String failReason = "";
	String testRes = "";
	
	String strActRes1 = "";
	String strActRes2 = "";
	String strActRes3 = "";
	String strActInsBH = "";
	String strActInsAH = "";
	String strActMAH = "";
	
	// others
	private Color clrDis = new Color(240,240,240);
	PrintBarcode printQRCode = new PrintBarcode();
			
	//class to customize table look & feel
	class MyTableCellRender extends DefaultTableCellRenderer {  
		public Component getTableCellRendererComponent(  
				JTable table, Object value, boolean isSelected, 
				boolean hasFocus, int row, int col) {
				     super.getTableCellRendererComponent(
				                      table,  value, isSelected, hasFocus, row, col);
				     try {
					     if (!table.getValueAt(row, tblTotCol-2).toString().isEmpty() && table.getValueAt(row, tblTotCol-2).toString().equals("FAIL") && !isSelected) {
						    	 setForeground(Color.RED); 
						 } else if (!isSelected){
							 setForeground(Color.BLACK);
						 }
				     } catch (Exception e) {
				    	 // ignore
				     }
				     // disabled tests
				     if (col >= 4 && col <=7 && Configuration.IS_RES_DISABLED.equals("1") ) {
				    	 setBackground(clrDis);
				     } else if ((col == 8) && Configuration.IS_INS_RES_DISABLED.equals("1") ) {
				    	 setBackground(clrDis);
				     } else if ((col == 9 || col == 10) && Configuration.IS_HV_DISABLED.equals("1") ) {
				    	 setBackground(clrDis);
				     } else if ((col == 11) && Configuration.IS_INS_RES_AFTER_HV_DISABLED.equals("1") ) {
				    	 setBackground(clrDis);
				     } else if (col == 12  && Configuration.IS_SURGE_DISABLED.equals("1") ) {
				    	 setBackground(clrDis);
				     } else if (col == 13  && Configuration.IS_DIR_TEST_DISABLED.equals("1") ) {
				    	 setBackground(clrDis);
				     } else if (!isSelected){
				    	 setBackground(Color.white);
				     }
				     if (col == 6 && Configuration.IS_COM_RES_DISABLED.equals("1") && curStatorPhase.equals("Single")) {
				    	 setBackground(clrDis);
				     }
			return this;
			}
	}
	
	// class to listen and select images from db
	class SelectionListener implements ListSelectionListener {
		  JTable table;
		  JFrame pr;

		  SelectionListener(JTable table, StatorView parent) {
		    this.table = table;
		    this.pr = parent;
		  }
		  public void valueChanged(ListSelectionEvent le) {
			  try {
				  	// remove existing imgs if any
					while (pnlWaveImg.getComponentCount() > 0) {
						pnlWaveImg.remove(0);
					}
					pnlWaveImg.update(pnlWaveImg.getGraphics());
				  	// fetch images for this row
					availCount = 0;
					ResultSet res = stmt.executeQuery("select * from " + Configuration.SURGE_IMAGE + " where test_slno='" + table.getValueAt(table.getSelectedRow(), 1).toString().trim() + "'");
					while (res.next()) {
						JLabel lblImg = new JLabel();
						lblImg.setBorder(new LineBorder(Color.lightGray));
						lblImg.setIcon(new ImageIcon(res.getBytes("wave_img")));
						pnlWaveImg.add(lblImg, new TableLayoutConstraints(0, availCount, 0, availCount, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
						++availCount;
					}
					pnlWaveImg.revalidate();
			  } catch (Exception ex) {
				 // ignore
			  }
		  }
	}
	// custom variable - end
	
	// custom functions - begin
	private void customInit() {
		setExtendedState(JFrame.MAXIMIZED_BOTH);
		if (Configuration.IS_TRIAL_ON) {
			Configuration.APP_VERSION += "     [Trial Version]";
		}
		setTitle(Configuration.APP_AUTHOR + " " + Configuration.APP_VERSION  + "     [User: " + Configuration.USER + "]" );
		
		// set last used values
		lblCompNm.setText(Configuration.LICENCEE_NAME);
		curStatorType = Configuration.LAST_USED_STATOR_TYPE;
		setTestModeLabel();
		lblEmergency.setVisible(false);
		
		// set default text to error log
		errLogModel.addElement("Description is shown here while an error occurs; Double click to expand, Delete key to clear.");
		lstError.setSelectedIndex(0);
		
		// set number of stations
		TableLayout tmpLayout = (TableLayout) pnlSt.getLayout();
		for(int i=0; i<Integer.valueOf(Configuration.NUMBER_OF_STATIONS) ; i++) {
			JLabel lblStImg = new JLabel();
			lblStImg.setIcon(idleImg);
			lblStImg.setHorizontalAlignment(SwingConstants.CENTER);
			lblStImg.setText("" + (i+1));
			lblStImg.setHorizontalTextPosition(SwingConstants.CENTER);
			lblStImg.setFont(new Font("Arial", Font.BOLD, 16));
			lblStImg.setIconTextGap(0);
			lblStImg.setBorder(null);
			tmpLayout.insertColumn(i, TableLayout.FILL);
			pnlSt.add(lblStImg, new TableLayoutConstraints(i, 0, i, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
			stationList.add(lblStImg);
		}
		
		// db initialization
		try {
			conn = DriverManager.getConnection(Configuration.DB_URL);
			stmt = conn.createStatement();
			stmt.setQueryTimeout(30);
			
			// create reading table if not exit (Other tables created inline wherever required)
			try {
				stmt.executeQuery("select * from " + Configuration.READING_DETAIL + "");
			} catch (SQLException se) {
				if (se.getMessage().contains("no such table")) {
					// it seems software runs for fist time, create the table
					stmt.executeUpdate("create table " + Configuration.READING_DETAIL + " (test_slno integer primary key autoincrement, stator_type text, test_date date, stator_slno text, " +
							"res_start float, res_run float, res_com float, temp float, ins_res_bef_hv float, hv_kv float, hv_amps float, ins_res_aft_hv float, surge text, dir text, test_result text, remark text, vendor_ref text, " +
							"line text, user text)");
				}
			}
		} catch (SQLException e) {
			JOptionPane.showMessageDialog(this, "DB Error:" + e.getMessage());
			return;
		}
		
		tblTotCol = tblTestEntry.getColumnCount();
		
		// table selection listener
		SelectionListener listener = new SelectionListener(tblTestEntry, this);
		tblTestEntry.getSelectionModel().addListSelectionListener(listener);
		
		// load dev config
		loadDevSet();
		
		// intialize devices
		initializeDevices();
		
		//assign Protocol
		initProtocol();
		
		// load last used stator type
		setStatorType(curStatorType, false);
		
		// others
		associateFunctionKeys();
		
		// remove table header and set table properties
		tblTestEntry.setTableHeader(null);
		scrlTest.setViewportView(tblTestEntry);
		tblTestEntry.setDefaultRenderer(Object.class, new MyTableCellRender());
		
		// disable save if user does not have modify access
		if (Configuration.USER_HAS_MODIFY_ACCESS.equals("0")) {
			cmdSave.setEnabled(false);
			cmdDel.setEnabled(false);
		}
		
		// clear test entry if any
		clearForm();
		
		// load last ten readings by default
		openFile("select * from (select * from " + Configuration.READING_DETAIL + " where stator_type='" + curStatorType + "' order by rowid desc limit 10) order by test_slno", false);
		
		checkReminders();
		
		// current mode refresh from panel
		if (deviceInitialized) {
			try {
				Configuration.LAST_USED_CAPTURE_METHOD = devPLC.readCoil(selAutoMan,devPLCProtocol)?"A":"M";
				Configuration.saveConfigValues("LAST_USED_CAPTURE_METHOD");
				setTestModeLabel();
			} catch (Exception e) {
				logError("Unable to synch test mode from panel; Please check device testings");
			}
		}
		
		enableDisableScanner();
		
		// check for qr code printer path - will be use full if software upgraded recently
		if (Configuration.QR_CODE_PRINTER_PATH.isEmpty()) {
			String printerPath = "";
			try {
				InetAddress addr;
			    addr = InetAddress.getLocalHost();
			    printerPath = "\\\\" + addr.getHostName() + "\\" + "barcode-printer";
			    Configuration.QR_CODE_PRINTER_PATH = printerPath;
			    Configuration.saveCommonConfigValues("QR_CODE_PRINTER_PATH");
			} catch (Exception e) {
				System.out.println("Error finding the host name");
			}
		}
		
		refreshTestCount();
		// few init are done in window opened
	}
	
	// function to get protocol
	public void initProtocol() {
		try {
			ResultSet res = stmt.executeQuery("select dev_name,comm_protocol from " + Configuration.DEVICE + "");
			while(res.next()) {
				if(res.getString("dev_name").equals("PLC")) {
					devPLCProtocol = res.getString("comm_protocol");
				}
				if(res.getString("dev_name").equals("Grindex Electrik Analyser")) {
					devGrindexProtocol = res.getString("comm_protocol");
				}
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	// function to set test mode
	public void setTestModeLabel() {
		if (curTestMode.equals("A")) {
			lblTestMode.setText("AUTO");
			tglRetest.setSelected(false);
		} else {
			lblTestMode.setText("MANUAL");
			tglRetest.setSelected(true);
		}
		lblTestMode.revalidate();
		tglRetestActionPerformed();
	}
	
	// function to check reminders at start up (like calibration, backup, etc...)
	private void checkReminders() {
		//1. check for calibration reminders
			ResultSet res = null;
			try {
				Calendar nextWeek = Calendar.getInstance();
				nextWeek.add(Calendar.DATE, +7);
				
				String curDt = dbDtFormat.format(nextWeek.getTime());
				res = stmt.executeQuery("select * from " + Configuration.CALIBRATION + " where due_date != '' and due_date <= '" + curDt + "' and reminder=1");
				String calDue = "";
				String curLine = "";
				String formatStr = "%-11s%-40s%-11s%s";
				while (res.next()) {
					curLine = String.format(formatStr, res.getString("ins_id"), res.getString("ins_name"), reqDtFormat.format(dbDtFormat.parse(res.getString("due_date"))), res.getString("agency"));
					calDue += curLine + "\n";
				}
				if (!calDue.isEmpty()) {
					calDue = String.format(formatStr,"ID", "INSTRUMENT NAME", "DUE DATE", "AGENCY") + "\n" + String.format(formatStr, "==", "===============", "========", "======") + "\n" + calDue;
					JTextArea msgTxt = new JTextArea();
					msgTxt.setText("Calibration Reminder\n\n" + calDue);
					msgTxt.setEditable(false);
					msgTxt.setBackground(this.getBackground());
					JOptionPane.showMessageDialog(this, msgTxt);
					
					// set reminder icon
					cmdDev.setIcon(new ImageIcon(getClass().getResource("/img/bell.PNG")));
				} else {
					cmdDev.setIcon(null);
				}
			} catch (ParseException pe) {
				// ignore
			} catch (Exception se) {
				if (!se.getMessage().contains("no such table")) {
					JOptionPane.showMessageDialog(this, "Error checking for calibration reminders:" + se.getMessage());
				}
			} finally {
				try {
					res.close();
				} catch (Exception e) {
					// ignore
				}
			}
			
			//2. check for automatic backup
			Calendar cal = Calendar.getInstance();
			try {
				cal.setTime(reqDtFormat.parse(reqDtFormat.format(cal.getTime())));
				if (!Configuration.NEXT_BACKUP_DATE.isEmpty()) {
					Calendar nextCal =Calendar.getInstance();
					nextCal.setTime(reqDtFormat.parse(Configuration.NEXT_BACKUP_DATE));
					if (cal.compareTo(nextCal) >= 0) {
						// backup required today
						changeApplicationStatus("BACKING UP THE DATA...");
						lblStatus.update(lblStatus.getGraphics());
						Configuration.backupData(Configuration.LAST_USED_BACKUP_LOCATION);
						Configuration.LAST_BACKUP_DATE = reqDtFormat.format(cal.getTime());
						changeApplicationStatus("AUTOMATIC BACKUP COMPLETED");
					} else {
						return;
					}
				}
			} catch (Exception e) {
					logError(e.getMessage());
					changeApplicationStatus("AUTOMATIC BACKUP FAILED");
					return;
			}
			
			// set next back up date (when either last automatic back completed successfully or the software runs first time)
			cal.add(Calendar.DATE, Integer.valueOf(Configuration.LAST_USED_BACKUP_DURATION));
			Configuration.NEXT_BACKUP_DATE = reqDtFormat.format(cal.getTime());
			Configuration.saveCommonConfigValues("NEXT_BACKUP_DATE", "LAST_BACKUP_DATE");
	}

	private void associateFunctionKeys() {
		
		// associate f3 for open
		String OPEN_ACTION_KEY = "openAction";
		Action openAction = new AbstractAction() {
		      public void actionPerformed(ActionEvent actionEvent) {
		        cmdOpenActionPerformed();
		      }
		    };
		KeyStroke f3 = KeyStroke.getKeyStroke(KeyEvent.VK_F3, 0);
		InputMap openInputMap = cmdOpen.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
		openInputMap.put(f3, OPEN_ACTION_KEY);
		ActionMap openActionMap = cmdOpen.getActionMap();
		openActionMap.put(OPEN_ACTION_KEY, openAction);
		cmdOpen.setActionMap(openActionMap);
		

		// associate f4 for print
		String PRINT_ACTION_KEY = "printAction";
		Action printAction = new AbstractAction() {
		      public void actionPerformed(ActionEvent actionEvent) {
		        cmdRepActionPerformed();
		      }
		    };
		KeyStroke f4 = KeyStroke.getKeyStroke(KeyEvent.VK_F4, 0);
		InputMap printInputMap = cmdRep.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
		printInputMap.put(f4, PRINT_ACTION_KEY);
		ActionMap printActionMap = cmdRep.getActionMap();
		printActionMap.put(PRINT_ACTION_KEY, printAction);
		cmdRep.setActionMap(printActionMap);
		
		// associate f5 for save
		String SAVE_ACTION_KEY = "saveAction";
		Action saveAction = new AbstractAction() {
		      public void actionPerformed(ActionEvent actionEvent) {
		        cmdSaveActionPerformed();
		      }
		    };
		KeyStroke f5 = KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0);
		InputMap saveInputMap = cmdSave.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
		saveInputMap.put(f5, SAVE_ACTION_KEY);
		ActionMap saveActionMap = cmdSave.getActionMap();
		saveActionMap.put(SAVE_ACTION_KEY, saveAction);
		cmdSave.setActionMap(saveActionMap);
			
		// associate f6 for undo
		String UNDO_ACTION_KEY = "delAction";
		Action delAction = new AbstractAction() {
		      public void actionPerformed(ActionEvent actionEvent) {
		        cmdDelActionPerformed();
		      }
		    };
		KeyStroke f6 = KeyStroke.getKeyStroke(KeyEvent.VK_F6, 0);
		InputMap delInputMap = cmdDel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
		delInputMap.put(f6, UNDO_ACTION_KEY);
		ActionMap delActionMap = cmdDel.getActionMap();
		delActionMap.put(UNDO_ACTION_KEY, delAction);
		cmdDel.setActionMap(delActionMap);
				
		// associate F10 for auto capture
		String TEST_ACTION_KEY = "testAction";
		Action testAction = new AbstractAction() {
		      public void actionPerformed(ActionEvent actionEvent) {
		    	tglTest.setSelected(!tglTest.isSelected());
		        tglTestActionPerformed();
		      }
		    };
		KeyStroke f10 = KeyStroke.getKeyStroke(KeyEvent.VK_F10, 0);
		InputMap testInputMap = tglTest.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
		testInputMap.put(f10, TEST_ACTION_KEY);
		ActionMap testActionMap = tglTest.getActionMap();
		testActionMap.put(TEST_ACTION_KEY, testAction);
		tglTest.setActionMap(testActionMap);
		
		// associate F11 for pump
		String STATOR_ACTION_KEY = "statorAction";
		Action statorAction = new AbstractAction() {
		      public void actionPerformed(ActionEvent actionEvent) {
		        cmdStatorActionPerformed();
		      }
		    };
		KeyStroke f11 = KeyStroke.getKeyStroke(KeyEvent.VK_F11, 0);
		InputMap statorInputMap = cmdStator.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
		statorInputMap.put(f11, STATOR_ACTION_KEY);
		ActionMap statorActionMap = cmdStator.getActionMap();
		statorActionMap.put(STATOR_ACTION_KEY, statorAction);
		cmdStator.setActionMap(statorActionMap);
		
		// remove function key bindings from test entry table
		DefaultCellEditor editor = new DefaultCellEditor(new JTextField()) {
			@Override
		    public boolean isCellEditable(EventObject e) {
		        if (e instanceof KeyEvent) {
		            return startWithKeyEvent((KeyEvent) e);
		        }
		        return super.isCellEditable(e);
		    }
			private boolean startWithKeyEvent(KeyEvent e) {
		        // check modifiers as needed, this here is just a quick example
		        if (e.getKeyCode() >= KeyEvent.VK_F1 && e.getKeyCode() <= KeyEvent.VK_F24) {
		            return false;
		        }    
		        return true;
		    }
		};
		tblTestEntry.setDefaultEditor(Object.class, editor);
	}
	
	// check & create device setting table if needed
	public void loadDevSet() {
		try {
			ResultSet res = null;
			Boolean insertNeeded = false;
			try {
				res = stmt.executeQuery("select * from DEVICE_PARAM where line='" + Configuration.LINE_NAME + "' order by dev_name");
				res.close();
			} catch (SQLException se1) {
				if (se1.getMessage().contains("no such table")) {
					// create table
					stmt.executeUpdate("CREATE TABLE DEVICE (line text, line_op text, dev_name text, dev_id text, dev_port text, dev_type text, baud_rt integer, data_bits integer, stop_bits integer, parity integer, wc integer, endianness text, fc integer, ip_cmd text, comm_protocol text, ip_address text, ip_port integer, primary key (line, line_op, dev_name));");
					stmt.executeUpdate("CREATE TABLE DEVICE_PARAM (line text, line_op text, dev_name text, param_name text, param_adr text, conv_factor text, format_text text, reg_type text, visible boolean, primary key (line, line_op, dev_name, param_name))");
					// devices
					stmt.executeUpdate("INSERT INTO DEVICE VALUES ('" + Configuration.LINE_NAME + "', 'Output1', 'PLC', '1', '', 'M', 19200, 8, 2, 0, 2, 'LSB First', 0, '','RTU','',2200)");
					stmt.executeUpdate("INSERT INTO DEVICE VALUES ('" + Configuration.LINE_NAME + "', 'Output1', 'Grindex Electrik Analyser', '1', '', 'M', 19200, 8, 1, 0, 2, 'MSB First', 0, '','RTU','',2200)");
					// device params
					stmt.executeUpdate("insert into DEVICE_PARAM values ('" + Configuration.LINE_NAME + "', 'Output1', 'PLC', 'Temperature 1', '71', '/10', '#0.0', 'Input', 1)");
					stmt.executeUpdate("insert into DEVICE_PARAM values ('" + Configuration.LINE_NAME + "', 'Output1', 'PLC', 'Temperature 2', '71', '/10', '#0.0', 'Input',  1)");
					
					stmt.executeUpdate("insert into DEVICE_PARAM values ('" + Configuration.LINE_NAME + "', 'Output1', 'PLC', 'R1 Test Switch', '60', '', '', 'Coil', 0)");
					stmt.executeUpdate("insert into DEVICE_PARAM values ('" + Configuration.LINE_NAME + "', 'Output1', 'PLC', 'R2 Test Switch', '61', '', '', 'Coil', 0)");
					stmt.executeUpdate("insert into DEVICE_PARAM values ('" + Configuration.LINE_NAME + "', 'Output1', 'PLC', 'R3 Test Switch', '62', '', '', 'Coil', 0)");
					stmt.executeUpdate("insert into DEVICE_PARAM values ('" + Configuration.LINE_NAME + "', 'Output1', 'PLC', 'IR Test Switch', '63', '', '', 'Coil', 0)");
					stmt.executeUpdate("insert into DEVICE_PARAM values ('" + Configuration.LINE_NAME + "', 'Output1', 'PLC', 'HV Test Switch 1', '64', '', '', 'Coil', 0)");
					stmt.executeUpdate("insert into DEVICE_PARAM values ('" + Configuration.LINE_NAME + "', 'Output1', 'PLC', 'HV Test Switch 2', '65', '', '', 'Coil', 0)");
					stmt.executeUpdate("insert into DEVICE_PARAM values ('" + Configuration.LINE_NAME + "', 'Output1', 'PLC', 'IR After HV Test Switch', '63', '', '', 'Coil', 0)");
					stmt.executeUpdate("insert into DEVICE_PARAM values ('" + Configuration.LINE_NAME + "', 'Output1', 'PLC', 'Surge Test Switch 1', '67', '', '', 'Coil', 0)");
					stmt.executeUpdate("insert into DEVICE_PARAM values ('" + Configuration.LINE_NAME + "', 'Output1', 'PLC', 'Surge Test Switch 2', '67', '', '', 'Coil', 0)");
					stmt.executeUpdate("insert into DEVICE_PARAM values ('" + Configuration.LINE_NAME + "', 'Output1', 'PLC', 'Direction Fwd Test Switch', '68', '', '', 'Coil', 0)");
					stmt.executeUpdate("insert into DEVICE_PARAM values ('" + Configuration.LINE_NAME + "', 'Output1', 'PLC', 'Direction Rev Test Switch', '68', '', '', 'Coil', 0)");
					stmt.executeUpdate("insert into DEVICE_PARAM values ('" + Configuration.LINE_NAME + "', 'Output1', 'PLC', 'Discharge Switch', '66', '', '', 'Coil', 0)");
					
					stmt.executeUpdate("insert into DEVICE_PARAM values ('" + Configuration.LINE_NAME + "', 'Output1', 'PLC', 'Auto Start Signal', '8', '', '', 'Coil', 0)");
					stmt.executeUpdate("insert into DEVICE_PARAM values ('" + Configuration.LINE_NAME + "', 'Output1', 'PLC', 'Auto/Manual Selection', '5', '', '', 'Coil', 0)");
					stmt.executeUpdate("insert into DEVICE_PARAM values ('" + Configuration.LINE_NAME + "', 'Output1', 'PLC', 'Phase Selection', '6', '', '', 'Coil', 0)");
					stmt.executeUpdate("insert into DEVICE_PARAM values ('" + Configuration.LINE_NAME + "', 'Output1', 'PLC', 'HV Fail Signal', '7', '', '', 'Coil', 0)");
					stmt.executeUpdate("insert into DEVICE_PARAM values ('" + Configuration.LINE_NAME + "', 'Output1', 'PLC', 'Direction Pass Signal', '16', '', '', 'Coil', 0)");
					stmt.executeUpdate("insert into DEVICE_PARAM values ('" + Configuration.LINE_NAME + "', 'Output1', 'PLC', 'Surge Pass Signal', '89', '', '', 'Coil', 0)");
					stmt.executeUpdate("insert into DEVICE_PARAM values ('" + Configuration.LINE_NAME + "', 'Output1', 'PLC', 'Surge Fail Signal', '90', '', '', 'Coil', 0)");
					stmt.executeUpdate("insert into DEVICE_PARAM values ('" + Configuration.LINE_NAME + "', 'Output1', 'PLC', 'Direction Pass Lamp', '195', '', '', 'Coil', 0)");
					stmt.executeUpdate("insert into DEVICE_PARAM values ('" + Configuration.LINE_NAME + "', 'Output1', 'PLC', 'Direction Fail Lamp', '190', '', '', 'Coil', 0)");
					stmt.executeUpdate("insert into DEVICE_PARAM values ('" + Configuration.LINE_NAME + "', 'Output1', 'PLC', 'Surge Pass Lamp', '18', '', '', 'Coil', 0)");
					stmt.executeUpdate("insert into DEVICE_PARAM values ('" + Configuration.LINE_NAME + "', 'Output1', 'PLC', 'Surge Fail Lamp', '19', '', '', 'Coil', 0)");
					stmt.executeUpdate("insert into DEVICE_PARAM values ('" + Configuration.LINE_NAME + "', 'Output1', 'PLC', 'Red Lamp', '57', '', '', 'Coil', 0)");
					stmt.executeUpdate("insert into DEVICE_PARAM values ('" + Configuration.LINE_NAME + "', 'Output1', 'PLC', 'Green Lamp', '58', '', '', 'Coil', 0)");
					stmt.executeUpdate("insert into DEVICE_PARAM values ('" + Configuration.LINE_NAME + "', 'Output1', 'PLC', 'Yellow Lamp', '59', '', '', 'Coil', 0)");
					stmt.executeUpdate("insert into DEVICE_PARAM values ('" + Configuration.LINE_NAME + "', 'Output1', 'PLC', 'Test Off Lamp', '70', '', '', 'Coil', 0)");
					stmt.executeUpdate("insert into DEVICE_PARAM values ('" + Configuration.LINE_NAME + "', 'Output1', 'PLC', 'Station 1 Signal', '13', '', '', 'Coil', 0)");
					stmt.executeUpdate("insert into DEVICE_PARAM values ('" + Configuration.LINE_NAME + "', 'Output1', 'PLC', 'Station 2 Signal', '14', '', '', 'Coil', 0)");
					stmt.executeUpdate("insert into DEVICE_PARAM values ('" + Configuration.LINE_NAME + "', 'Output1', 'PLC', 'Emergency Signal', '15', '', '', 'Coil', 0)");
					
					stmt.executeUpdate("insert into DEVICE_PARAM values ('" + Configuration.LINE_NAME + "', 'Output1', 'Grindex Electrik Analyser', 'Resistance', '0', '', '#0.00', 'Holding Float', 1)");
					stmt.executeUpdate("insert into DEVICE_PARAM values ('" + Configuration.LINE_NAME + "', 'Output1', 'Grindex Electrik Analyser', 'Ins. Resistance', '4', '', '#', 'Holding Float', 1)");
					stmt.executeUpdate("INSERT INTO DEVICE_PARAM VALUES ('" + Configuration.LINE_NAME + "', 'Output1', 'Grindex Electrik Analyser', 'kV', '6', '*0.00869565217', '#0.000', 'Holding Float', 1)");
					stmt.executeUpdate("INSERT INTO DEVICE_PARAM VALUES ('" + Configuration.LINE_NAME + "', 'Output1', 'Grindex Electrik Analyser', 'mA', '8', '', '#0.0', 'Holding Float', 1)");
				} else {
					throw se1;
				}
			}
			
			// construct dev object
			devCfg = new Device("Output1");
			
		} catch (SQLException se) {
			JOptionPane.showMessageDialog(this, "Error loading device settings:" + se.getMessage());
		}
	}
	
	// reset test mode in PLC
	public void resetTestMode(Boolean isAuto) throws Exception {
		if (devPLC.isInitialized()) {
			// stop capture & live reading and reset the test mode
			if (isAutoCaptureOn) {
				tglTest.setSelected(false);
				tglTestActionPerformed();
			}
			stopLiveReading();
			
			devPLC.writeCoil(selAutoMan, isAuto, devPLCProtocol);
			
			// restart auto capture & live reading
			tglTest.setSelected(true);
			tglTestActionPerformed();
			startLiveReading();
			
		} else {
			throw new Exception("Unable to communicate to PLC as it is not initialized yet");
		}
	}
	
	// check & restart test & live reading
	public void restartTest() {
		// stop running threads
		stopLiveReading();
		tglTest.setSelected(false);
		tglTestActionPerformed();
		
		// re-initialize devices
		initializeDevices();
		initProtocol();
		
		startLiveReading();
		tglTest.setSelected(true);
		tglTestActionPerformed();
	}
	
	public void enableDisableScanner() {
		if (!Configuration.SNO_GEN_METHOD.equals("A")) {
			txtScannedSNo.setText("");
			txtScannedSNo.setEnabled(true);
		} else {
			txtScannedSNo.setText("Barcode scanner is not enabled");
			txtScannedSNo.setEnabled(false);
		}
	}
	
	// reset error adj
	private void resetErrAdj() {
		try {
			// fetch and load the result
			int i=0;
			ResultSet res = null;
			try {
			res = stmt.executeQuery("select * from " + Configuration.ERROR_ADJ + " where stator_type = '" + curStatorType + "' order by line");
			} catch (Exception se) {
				if (se.getMessage().contains("no such table")) {
					// create table and insert default rows
					stmt.executeUpdate("create table " + Configuration.ERROR_ADJ + " (stator_type text, line text,  res_1 text, res_2 text, res_3 text, ins_res text, primary key(stator_type, line))");
					for (int j=1; j<=Integer.valueOf(Configuration.NUMBER_OF_STATIONS); j++) {
						stmt.executeUpdate("insert into " + Configuration.ERROR_ADJ + " values ('" + curStatorType +"', '" + j +"', '0.000', '0.000', '0.000', '1')");
					}
					res = stmt.executeQuery("select * from " + Configuration.ERROR_ADJ + " where stator_type = '" + curStatorType + "' order by line");
				}
			}
			while (res.next()) {
				resErrAdj1[i] = res.getDouble("res_1");
				resErrAdj2[i] = res.getDouble("res_2");
				resErrAdj3[i] = res.getDouble("res_3");
				insResErrAdj[i] = res.getDouble("ins_res");
				++i;
			}
			//System.out.println("cur st:" + curActiveStation + ":" + insResErrAdj[curActiveStation]);
			res.close();
		} catch (Exception sqle) {
			sqle.printStackTrace();
			JOptionPane.showMessageDialog(this, "Error loading error adjustment constants:" + sqle.getMessage());
			return;
		}
	}
	// enable / disable tests
	public void enableTests() {
		// resistance
		if (Configuration.IS_RES_DISABLED.equals("1")) {
			lblLiveRes.setVisible(false);
			lblResTestStat.setVisible(true);
			lblLiveTemp.setVisible(false);
			lblTempTestStat.setVisible(true);
			
			// table
			lblHRes.setEnabled(false);
			lblStCl.setEnabled(false);
			lblRunCl.setEnabled(false);
			lblComCl.setEnabled(false);
			lblHTemp.setEnabled(false);
		} else {
			lblLiveRes.setVisible(true);
			lblResTestStat.setVisible(false);
			lblLiveTemp.setVisible(true);
			lblTempTestStat.setVisible(false);
			
			// table
			lblHRes.setEnabled(true);
			lblStCl.setEnabled(true);
			lblRunCl.setEnabled(true);
			if (Configuration.IS_COM_RES_DISABLED.equals("0") || curStatorPhase.equals("Three")) {
				lblComCl.setEnabled(true);
			} else {
				lblComCl.setEnabled(false);
			}
			lblHTemp.setEnabled(true);
		}
		// ins resistance
		if (Configuration.IS_INS_RES_DISABLED.equals("1")) {
			// table
			lblHIR1.setEnabled(false);
		} else {
			// table
			lblHIR1.setEnabled(true);
		}
		// hv
		if (Configuration.IS_HV_DISABLED.equals("1")) {
			lblLiveV.setVisible(false);
			lblVTestStat.setVisible(true);
			lblLiveCur.setVisible(false);
			lblCurTestStat.setVisible(true);
			
			// table
			lblHHV.setEnabled(false);
			lblHHV1.setEnabled(false);
			lblHHV2.setEnabled(false);
		} else {
			lblLiveV.setVisible(true);
			lblVTestStat.setVisible(false);
			lblLiveCur.setVisible(true);
			lblCurTestStat.setVisible(false);
			
			// table
			lblHHV.setEnabled(true);
			lblHHV1.setEnabled(true);
			lblHHV2.setEnabled(true);
		}
		
		// ins resistance after hv
		if (Configuration.IS_INS_RES_AFTER_HV_DISABLED.equals("1")) {
			// table
			lblHIR2.setEnabled(false);
		} else {
			// table
			lblHIR2.setEnabled(true);
		}
		
		if (Configuration.IS_INS_RES_DISABLED.equals("1") && Configuration.IS_INS_RES_AFTER_HV_DISABLED.equals("1")) {
			lblLiveIR.setVisible(false);
			lblInsResTestStat.setVisible(true);
		} else {
			lblLiveIR.setVisible(true);
			lblInsResTestStat.setVisible(false);
		}
				
		// surge
		if (Configuration.IS_SURGE_DISABLED.equals("1")) {
			lblSurge.setEnabled(false);
			disableSurgeWave();
		} else {
			lblSurge.setEnabled(true);
			if (Configuration.IS_SURGE_WAVE_CAPTURED.equals("1")) {
				enableSurgeWave();
			} else {
				disableSurgeWave();
			}
		}
		
		// direction
		if (Configuration.IS_DIR_TEST_DISABLED.equals("1")) {
			lblDir.setEnabled(false);
		} else {
			lblDir.setEnabled(true);
		}
			
		tblTestEntry.updateUI();
	}
	
	// function to enable surge wave capturing
	public void enableSurgeWave() {
		if (((TableLayout)pnlTbl.getLayout()).getNumColumn() < 3) {
			// earlier it was disabled, hence prompt the user to restart the app
			JOptionPane.showMessageDialog(this, "Wait!, you need to close and reopen the application to enable surge wave camera");
			return;
		}
		
		if (webCam != null && webCam.isOpen()) {
			// webcam already in open state
			return;
		}
		
		changeApplicationStatus("INITIALIZING WEBCAM...");
		lblStatus.update(lblStatus.getGraphics());
		// web cam window
		this.setCursor(waitCursor);
		if (Webcam.getWebcams().size() > 0) {
			try {
				webCam = Webcam.getWebcams().get(Integer.valueOf(Configuration.LAST_USED_WEBCAM));
			} catch (IndexOutOfBoundsException e) {
				webCam = Webcam.getWebcams().get(0);
			}
		}
		if (webCam != null) {
			// live panel
			pnlWebCam = new WebcamPanel(webCam);
			pnlWebCam.setImageSizeDisplayed(true);
			pnlWC.add(pnlWebCam, new TableLayoutConstraints(0, 0, 0, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
			motDWebCam = new WebcamMotion(webCam);
			this.setCursor(Cursor.getDefaultCursor());
			
			changeApplicationStatus("WEBCAM INITIALIZED");
			
		} else {
			logError("Error:Webcam is not available");
			changeApplicationStatus("WEBCAM INITIALIZATION FAILED");
		}
	}
	
	// function to disable surge wave capturing
	public void disableSurgeWave() {
		if (((TableLayout)pnlTbl.getLayout()).getNumColumn() < 3) {
			// disabled already
			return;
		}
		
		// 1. remove surge wave related stuffs
		// close webcam if opened already
		if (webCam != null) {
			webCam.close();
		}
		TableLayout tl = (TableLayout) pnlTbl.getLayout(); // surge wave in test table
		tl.deleteColumn(2); 
					
		tl = (TableLayout) pnlLive.getLayout(); // surge wave in live reading
		tl.removeLayoutComponent(lbllblLSurge);
		tl.removeLayoutComponent(pnlWC);
		tl.deleteRow(7);
		tl.deleteRow(6);
		tl.insertRow(0, 92);
		pnlLive.revalidate();
		
		changeApplicationStatus("SURGE WAVE DISABLED");
	}
	
	// function to map LCD hex to numeric value
	private String mapHexToDig(byte hexByte, boolean decChk) {
		dig = "";
		switch (hexByte & (byte)0xf7) { // byte except dot
			case (byte)0x20:
				dig = "-";
				break;
			case (byte)0xd7:
				dig = "0";
				break;
			case (byte)0x06:
				dig = "1";
				break;
			case (byte)0xe3:
				dig = "2";
				break;
			case (byte)0xa7:
				dig = "3";
				break;
			case (byte)0x36:
				dig = "4";
				break;
			case (byte)0xb5:
				dig = "5";
				break;
			case (byte)0xf5:
				dig = "6";
				break;
			case (byte)0x07:
				dig = "7";
				break;
			case (byte)0xf7:
				dig = "8";
				break;
			case (byte)0xb7:
				dig = "9";
		}
		// add decimal point if required
		if (decChk) {
			if ((hexByte & (byte)0x08) == (byte)0x08) { 
				dig += "."; 
			}
		}
		return dig;
	}
	// initialize devices & its communication
	private void initializeDevices() {
		changeApplicationStatus("INITIALIZING DEVICE COMMUNICATION...");
		this.setCursor(waitCursor);
		PLCPortError = false;
		serPortError = false;
		
		try {
			// load from table
			devCfg.refresh("Output1");
			
			startR1 = devCfg.getDevRegister("PLC","R1 Test Switch");
			startR2 = devCfg.getDevRegister("PLC","R2 Test Switch");
			startR3 = devCfg.getDevRegister("PLC","R3 Test Switch");
			// startR4 = devCfg.getDevRegister("PLC","R4 Test Switch");
			// startR5 = devCfg.getDevRegister("PLC","R5 Test Switch");
			// startR6 = devCfg.getDevRegister("PLC","R6 Test Switch");
			// selR456 = devCfg.getDevRegister("PLC","R456 Switch");
			startIR = devCfg.getDevRegister("PLC","IR Test Switch");
			startHV1 = devCfg.getDevRegister("PLC","HV Test Switch 1");
			startHV2 = devCfg.getDevRegister("PLC","HV Test Switch 2");
			startIRAfterHV = devCfg.getDevRegister("PLC","IR After HV Test Switch");
			startSurge1 = devCfg.getDevRegister("PLC","Surge Test Switch 1");
			startSurge2 = devCfg.getDevRegister("PLC","Surge Test Switch 2");
			startDirFwd = devCfg.getDevRegister("PLC","Direction Fwd Test Switch");
			startDirRev = devCfg.getDevRegister("PLC","Direction Rev Test Switch");
			startDis = devCfg.getDevRegister("PLC","Discharge Switch");
			
			selAutoMan = devCfg.getDevRegister("PLC","Auto/Manual Selection");
			selPhase = devCfg.getDevRegister("PLC","Phase Selection");
			
			sigAutoStart = devCfg.getDevRegister("PLC","Auto Start Signal");
			sigHVFail = devCfg.getDevRegister("PLC","HV Fail Signal");
			sigDirPass = devCfg.getDevRegister("PLC","Direction Pass Signal");
			sigSurgePass = devCfg.getDevRegister("PLC","Surge Pass Signal");
			sigSurgeFail = devCfg.getDevRegister("PLC","Surge Fail Signal");
			sigStation1 = devCfg.getDevRegister("PLC","Station 1 Signal");
			sigStation2= devCfg.getDevRegister("PLC","Station 2 Signal");
			sigEmergency= devCfg.getDevRegister("PLC","Emergency Signal");
			
			// initialize
			// 1. PLC
			try {
				if (devPLC != null) {
					try {
						devPLC.close();
					} catch (Exception e) {
						// ignore
					}
				}
				devPLC = new DeviceModbusReader();
				devPLC.initialize(devCfg.getCommParameters("PLC"));
			} catch (Exception e) {
				PLCPortError = true;
				logError("Error initializing PLC: Please check device settings: " + e.getMessage());
			}
			
			// 1. Grindex
			try {
				if (devGrindex != null) {
					try {
						devGrindex.close();
					} catch (Exception e) {
						// ignore
					}
				}
				devGrindex = new DeviceModbusReader();
				devGrindex.initialize(devCfg.getCommParameters("Grindex Electrik Analyser"));
			} catch (Exception e) {
				PLCPortError = true;
				logError("Error initializing Grindex Electrik Analyser: Please check device settings: " + e.getMessage());
			}
			
			if (PLCPortError) {
				changeApplicationStatus("DEVICE COMMUNICATION FAILED");
			} else {
				changeApplicationStatus("DEVICE COMMUNICATION SUCCESS");
			}
		} catch (Exception e) {
			logError(e.getMessage());
			changeApplicationStatus("DEVICE INITIALIZATION FAILED");
		}
		deviceInitialized = true;
		this.setCursor(Cursor.getDefaultCursor());
	}
	
	private void logError(String msg) {
		logTime = Calendar.getInstance().getTime();
		errLogModel.add(errLogModel.getSize()-1, reqDtFormat.format(logTime) + " " + reqTmFormat.format(logTime) + ":" + msg);
		lstError.ensureIndexIsVisible(errLogModel.getSize()-1);
	}
	
	// function to change the status label which is displayed at the bottom
	private void changeApplicationStatus(String status) {
		lblStatus.setText(status);
		lblStatus.revalidate();
	}
	
	// function to open a test file
	public void openFile(String queryText, boolean clearTable) {
		changeApplicationStatus("OPENING STATOR TEST...");
		// clear test tables
		if (clearTable) {
			clearForm();
		}
		
		try {
			// open and set reading detail
			String tmpStatorType = "";
			ResultSet res = stmt.executeQuery(queryText);
			if (res != null) {
				int r = 0;
				int c = 0;
				while (res.next()) {
					c = 0;
					tmpStatorType = res.getString("stator_type");
					tblTestEntry.setValueAt(res.getString("line"), r, c++);
					tblTestEntry.setValueAt(res.getString("test_slno"), r, c++);
					tblTestEntry.setValueAt(reqDtFormat.format(dbDtFormat.parse(res.getString("test_date"))), r, c++);
					tblTestEntry.setValueAt(res.getString("stator_slno"), r, c++);
					tblTestEntry.setValueAt(dotTwo.format(res.getDouble("res_start")), r, c++);
					tblTestEntry.setValueAt(dotTwo.format(res.getDouble("res_run")), r, c++);
					if (res.getString("res_com") == null || res.getString("res_com").equals("NA")) {
						tblTestEntry.setValueAt("NA", r, c++);
					} else {
						tblTestEntry.setValueAt(dotTwo.format(res.getDouble("res_com")), r, c++);
					}
					tblTestEntry.setValueAt(dotOne.format(res.getDouble("temp")), r, c++);
					tblTestEntry.setValueAt(dotZero.format(res.getDouble("ins_res_bef_hv")), r, c++);
					tblTestEntry.setValueAt(dotTwo.format(res.getDouble("hv_kv")), r, c++);
					tblTestEntry.setValueAt(dotTwo.format(res.getDouble("hv_amps")), r, c++);
					tblTestEntry.setValueAt(dotZero.format(res.getDouble("ins_res_aft_hv")), r, c++);
					tblTestEntry.setValueAt(res.getString("surge"), r, c++);
					tblTestEntry.setValueAt(res.getString("dir"), r, c++);
					tblTestEntry.setValueAt(res.getString("test_result"), r, c++);
					tblTestEntry.setValueAt(res.getString("remark"), r, c++);
					
					// surge waves
					
					++r;
					if (r == tblTestEntry.getRowCount()) {
						// add a new row
						DefaultTableModel defModel = (DefaultTableModel) tblTestEntry.getModel();
						defModel.addRow( new String[] {"", "", "", "", "", "", "", "", "", "", "", "", "", "", "",""});
					}
				}
				r=r==0?1:r;
				tblTestEntry.setRowSelectionInterval(r-1, r-1);
				tblTestEntry.scrollRectToVisible(tblTestEntry.getCellRect(r-1,0,true));
				
				res.close();
				tblTestEntry.requestFocusInWindow();
			}
			// stator type change
			if (!curStatorType.equals(tmpStatorType) && !tmpStatorType.isEmpty()) {
				setStatorType(tmpStatorType, false);
			}
			changeApplicationStatus("FILE OPENED");
			
		} catch (Exception se) {
			se.printStackTrace();
			JOptionPane.showMessageDialog(this, "Error opening existing test:" + se.getMessage());
			changeApplicationStatus("FILE OPEN FAILED");
			return;
		}
	}
	
	// function to set the chosen stator type
	public Boolean setStatorType(String type, boolean clearForm) {
		// fetch the record from table and set the params
		try {
			ResultSet res = null;
			try {
				res = stmt.executeQuery("select * from " + Configuration.STATOR_TYPE + " where type='" + type + "'");
			} catch (SQLException sqle) {
				if (sqle.getMessage().contains("no such table")) {
					// seems the software is run first time
					// create the table and insert a sample
					String curType = "SAMPLE STATOR MODEL";
					String curPhase = "Single";
					String curCon = "";
					String curKw = "0.37";
					String curHp = "0.5";
					String curDir = "FWD";
					String curVendorRef = "";
					String curResL1 = "18.5";
					String curResH1 = "20.0";
					String curResL2 = "2.8";
					String curResH2 = "3.2";
					String curResL3 = "2.8";
					String curResH3 = "3.2";
					String curInsResL = "2600";
					String curMAL = "0";
					String curMAH = "10";
					
					
					// create pump table and insert its default row
					stmt.executeUpdate("create table " + Configuration.STATOR_TYPE + " (type text primary key, phase text, conn text, kw float, hp float, vendor_ref text, res_low_limit1 float, res_up_limit1 float, res_low_limit2 float, " +
										" res_up_limit2 float, res_low_limit3 float, res_up_limit3 float, ins_res_low_limit float, hv_ma_low_limit float, hv_ma_up_limit float,  dir text)");
					stmt.executeUpdate("insert into " + Configuration.STATOR_TYPE + "(type, phase, conn, kw, hp, vendor_ref, res_low_limit1, res_up_limit1, " +
										"res_low_limit2, res_up_limit2, res_low_limit3, res_up_limit3, ins_res_low_limit, hv_ma_low_limit, hv_ma_up_limit, dir) values ('" + curType + "','" + curPhase + "','" + curCon + "','" + curKw + "','" + curHp + "','" + curVendorRef + "','" + curResL1 + "','" + curResH1 + "','" + 
										curResL2 + "','" + curResH2 + "','" + curResL3 + "','" + curResH3 + "','" + curInsResL + "','" + curMAL + "','" + curMAH + "','" + curDir + "')");
					res = stmt.executeQuery("select * from " + Configuration.STATOR_TYPE + " where type='" + curType + "'");
				}
			}

			if (res.next()) {
				lblStator.setText(res.getString("type"));
				lblPh.setText(res.getString("phase"));
				lblRat.setText(res.getString("kw") + " / " + res.getString("hp"));
				lblDirection.setText(res.getString("dir"));
				
				// global variables
				curStatorType= res.getString("type");
				curStatorPhase = res.getString("phase");
				curStatorCon = res.getString("conn");
				curVendorRef = res.getString("vendor_ref");
				declaredResLow1 = res.getFloat("res_low_limit1");
				declaredResUp1 = res.getFloat("res_up_limit1");
				declaredResLow2 = res.getFloat("res_low_limit2");
				declaredResUp2 = res.getFloat("res_up_limit2");
				declaredResLow3 = res.getFloat("res_low_limit3");
				declaredResUp3 = res.getFloat("res_up_limit3");
				declaredInsResL = res.getFloat("ins_res_low_limit");
				declaredMAH = res.getFloat("hv_ma_up_limit");
				curStatorDir = res.getString("dir");
				res.close();
				
				Configuration.LAST_USED_STATOR_TYPE = curStatorType;
				Configuration.saveConfigValues("LAST_USED_STATOR_TYPE");
				// load error adj
				resetErrAdj();
				
				// enable/disable according to phase
				if (curStatorPhase.equals("Three")) {
					Configuration.IS_DIR_TEST_DISABLED = "1";
				}
				
				// set connection relay flag in PLC
				/* if (!PLCPortError) {
					if (!curStatorCon.equals("Star Delta") || curStatorPhase.equals("Single")) {
						devPLC.writeCoil(devCfg.getDevRegister("PLC","Connection Relay Flag"), false);
					} else {
						devPLC.writeCoil(devCfg.getDevRegister("PLC","Connection Relay Flag"), true);
					}
				} */
				enableTests();
			} else {
				JOptionPane.showMessageDialog(this, "Unable find the stator model using the scanned code: " + type, "Model Not Found", JOptionPane.ERROR_MESSAGE);
				return false;
			}
		} catch (Exception se) {
			JOptionPane.showMessageDialog(this, "Error while setting stator type:" + se.getMessage());
			return false;
		}
		
		if (clearForm) {
			clearForm();
		}
		
		// table column headings and setting unit values  based on phase
		if (curStatorPhase.equals("Single")) {
			lblStCl.setText("<html><body  align='center'>Start<br><font size=-2>(Ω)</font><br><font style='color:#00FFFF'><b>" + declaredResLow1 + " - " + declaredResUp1 + "</b></p></html>");
			lblRunCl.setText("<html><body  align='center'>Run<br><font size=-2>(Ω)</font><br><font style='color:#00FFFF'><b>" + declaredResLow2 +" - " + declaredResUp2 + "</b></font></html>");
			lblComCl.setText("<html><body  align='center'>Com<br><font size=-2>(Ω)</font><br><font style='color:#00FFFF'><b>" + declaredResLow3 +" - " + declaredResUp3 + "</b></font></html>");
		} else {
			lblStCl.setText("<html><body  align='center'>R<br><font size=-2>(Ω)</font><br><font style='color:#00FFFF'><b>" + declaredResLow1 + " - " + declaredResUp1 + "</b></font></html>");
			lblRunCl.setText("<html><body  align='center'>Y<br><font size=-2>(Ω)</font><br><font style='color:#00FFFF'><b>" + declaredResLow1 + " - " + declaredResUp1 + "</b></font></html>");
			lblComCl.setText("<html><body  align='center'>B<br><font size=-2>(Ω)</font><br><font style='color:#00FFFF'><b>" + declaredResLow1 + " - " + declaredResUp1 + "</b></font></html>");

			Configuration.IS_DIR_TEST_DISABLED = "1";
		}
		// set unit labels for Ins
		lblHIR1.setText("<html><body  align='center'>IR<br>Bef. HV<font size=-2>(M\u03a9)</font><br><font style='color:#00FFFF'><b>Min " + declaredInsResL + "</b></font></html>");
		lblHIR2.setText("<html><body  align='center'>IR<br>Aft. HV<font size=-2>(M\u03a9)</font><br><font style='color:#00FFFF'><b>Min " + declaredInsResL + "</b></font></html>");

		
		// set unit labels  for HV
		lblHHV2.setText("<html><body  align='center'>mA<br><font style='color:#00FFFF'><b>Max " + declaredMAH + "</b></font></html>");

		
		tblTestEntry.update(tblTestEntry.getGraphics());
		return true;
	}
	
	Runnable autoCapture = new Runnable() {
		public void run() {
			isAutoCaptureOn = true;
			try {
				while (true) {
					if (!autoCaptureInProgress) {
						performAutoCapture();
					}
				}
			} catch (InterruptedException e) {
				if (!e.getMessage().contains("sleep interrupted")) {
					logError("INFO:" + e.getMessage());
					changeApplicationStatus(e.getMessage());
				} else {
					changeApplicationStatus("IDLE");
				}
			} catch (Exception e) {
				changeApplicationStatus("ERROR - TEST TURNED OFF");
				if (e.getMessage() == null) {
					logError("Auto Capture:Unable to start the test; Please check device settings");
				} else {
					logError("Auto Capture:" + e.getMessage());
				}
				e.printStackTrace();
			} finally {
				autoCaptureInProgress = false;
				isAutoCaptureOn = false;
				tglTest.setSelected(false);
				tglTest.setIcon(new ImageIcon(getClass().getResource("/img/start.png")));
				tglTest.setBackground(Color.RED);
				tglTest.setText("<html>Start Test&nbsp;<font size=-2>[F7]</html>");
				txtSignal.setText("");
			}
			// make idle all stations 
			for(int i=0; i<stationList.size(); i++) {
				stationList.get(i).setIcon(idleImg);
			}
		}
	};
	
	// device live reading thread
	Runnable liveReading = new Runnable() {
		public void run() {
			// read current readings from all the connected devices periodically
			try {
				int ms=500;
				String strResAdj = "";
				String strInsResAdj = "";
				while (true) {
					// Grindex Electrik Analyser
					if (Configuration.IS_RES_DISABLED.equals("0")) {
						try {
							strResAdj = "";
							if (curActiveStation > -1) {
								switch(curResOn) {
									case 1:
										strResAdj = "+" + resErrAdj1[curActiveStation].toString();
										break;
									case 2:
										strResAdj = "+" + resErrAdj2[curActiveStation].toString();
										break;
									case 3:
										strResAdj = "+" + resErrAdj3[curActiveStation].toString();
										break;
								}
							}
							
							curResData = devCfg.readParam(devGrindex,"Grindex Electrik Analyser","Resistance", strResAdj);
							lblLiveRes.setText(curResData);
						} catch (Exception e) {
							lblLiveRes.setText("-");
						}
					}
					
					//Ins resistance
					if (Configuration.IS_INS_RES_DISABLED.equals("0")) {
						try {
							if (curActiveStation > -1) {
								strInsResAdj = "*" + insResErrAdj[curActiveStation].toString();
							} else {
								strInsResAdj = "";
							}
							curIRData =devCfg.readParam(devGrindex,"Grindex Electrik Analyser","Ins. Resistance", strInsResAdj);
							lblLiveIR.setText(curIRData);
						} catch (Exception e) {
							logError("Live:Ins. Resistance:" + e.getMessage());
							lblLiveIR.setText("-");
						}
					}
					
					// voltage & cur
					if (Configuration.IS_HV_DISABLED.equals("0")) {
						try {
							lblLiveV.setText(devCfg.readParam(devGrindex,"Grindex Electrik Analyser","kV", ""));
							lblLiveCur.setText(devCfg.readParam(devGrindex,"Grindex Electrik Analyser","mA", ""));
						} catch (Exception e) {
							logError("Live:Power:" + e.getMessage());
							lblLiveV.setText("-");
							lblLiveCur.setText("-");
						}
					}
					
						
					// temp
					if (devPLC.isInitialized()) {
						if (Configuration.IS_RES_DISABLED.equals("0")) {
							try {
								lblLiveTemp.setText(devCfg.readParam(devPLC,"PLC","Temperature 1", ""));
							} catch (Exception e) {
								logError("Live:Temperature 1:" + e.getMessage());
								lblLiveTemp.setText("-");
							}
						}
					}
					
					Thread.sleep(ms);
				} // while
			} catch (Exception e) {
				// exits from thread
				e.printStackTrace();
			} finally {
				lblLiveRes.setText("-");
				lblLiveTemp.setText("-");
				lblLiveV.setText("-");
				lblLiveCur.setText("-");
			}
		}
	};
	
	// function to switch off all outputs and reset certain outputs to its defaults
	private void resetOutputCoils() throws Exception {
		devPLC.writeCoil(startDis, false, devPLCProtocol);
		devPLC.writeCoil(startR1, false, devPLCProtocol);
		devPLC.writeCoil(startR2, false, devPLCProtocol);
		devPLC.writeCoil(startR3, false, devPLCProtocol);
		devPLC.writeCoil(startIR, false, devPLCProtocol);
		devPLC.writeCoil(startHV1, false, devPLCProtocol);
		devPLC.writeCoil(startHV2, false, devPLCProtocol);
		devPLC.writeCoil(startIRAfterHV, false, devPLCProtocol);
		devPLC.writeCoil(startSurge1, false, devPLCProtocol);
		devPLC.writeCoil(startSurge2, false, devPLCProtocol);
		devPLC.writeCoil(startDirFwd, false, devPLCProtocol);
		devPLC.writeCoil(startDirRev, false, devPLCProtocol);
	}
	
	// function to perform auto capture
	private void performAutoCapture() throws Exception {
		// check for the signal
		autoCaptureInProgress = true;
		try {
			// swith off test in case
			devPLC.writeCoil(sigAutoStart, false, devPLCProtocol);
			
			// wait for signal from panel to start test
			changeApplicationStatus("WAITING FOR SIGNAL FROM PANEL");
			
			if (tglRetest.isSelected()) {
				txtSignal.setText("Slide the clousure to retest the stator");
			} else {
				txtSignal.setText("Slide the clousure to start new test once stator is ready");
			}
			
			while (findCurrentStation() == -1 || !devPLC.readCoil(sigAutoStart, devPLCProtocol)) {
				Thread.sleep(500);
			}
			
			// indicate the corresponding station
			stationList.get(curActiveStation).setIcon(runImg);
						
			// reset lamps if any
			devPLC.writeCoil(devCfg.getDevRegister("PLC","Yellow Lamp"), true, devPLCProtocol);
			devPLC.writeCoil(devCfg.getDevRegister("PLC","Red Lamp"), false, devPLCProtocol);
			devPLC.writeCoil(devCfg.getDevRegister("PLC","Green Lamp"), false, devPLCProtocol);
			
			resetOutputCoils();

			// SIGNAL METHOD: SCANNER
			// wait for signal until time out
			if (Configuration.SNO_GEN_METHOD.equals("S") || Configuration.SNO_GEN_METHOD.equals("B")) {  // scanner signal
				txtSignal.setText("Scan barcode of stator when it is ready for testing");
				txtScannedSNo.setText("");
				txtScannedSNo.requestFocus();
				scannedSNo = "";
				do {
					changeApplicationStatus("SCAN BARCODE OF STATOR");
					txtSignal.setText("Scan Now");
					Thread.sleep(2000);
					scannedSNo = txtScannedSNo.getText().trim();
					if (!scannedSNo.isEmpty()) {
						txtSignal.setText("Barcode scanned [" + scannedSNo + "]");
						break;
					}
					txtSignal.setText("");
				} while (true);
				
				// load motor model from scanned number if opted
				if (Configuration.IS_MODEL_FROM_BARCODE.equals("1")) {
					String tmpModel = "";
					try {
						if (Configuration.IS_MODEL_FROM_BARCODE_DELIMITER_BASED.equals("1")) {
							tmpModel = scannedSNo.split(Configuration.MODEL_DELIMITER_IN_BARCODE)[Integer.valueOf(Configuration.MODEL_LOCATION_IN_BARCODE) - 1];
						} else {
							tmpModel = scannedSNo.substring(Integer.valueOf(Configuration.MODEL_START_POS_IN_BARCODE)-1, Integer.valueOf(Configuration.MODEL_END_POS_IN_BARCODE));
						}
						if (!tmpModel.equals(curStatorType)) {
							if(!setStatorType(tmpModel, false)) {
								return; // scanned model not found, retry
							}
						}
					} catch (Exception e) {
						JOptionPane.showMessageDialog(this, "Unable to locate stator model from barcode", "Error", JOptionPane.ERROR_MESSAGE);
						return;
					}
				}
			}
		
			// determine the test row - same row for retest, new row for fresh test
			if (tglRetest.isSelected()) {
				curRow = tblTestEntry.getSelectedRow();
			} else {
				curRow = findNextEmptyRow();
			}
			
			lastHVTestFailed = false;
			lastSurgeFailed = false;
			lastDirFailed = false;
			
			String testName = "";
			
			// perform all the tests one by one once start signal received upon first test
			txtSignal.setText("Signal Received - Test In Progress");
			
			// set date and serial number
			setCaptureDateAndSNo(curRow);
			
			doDischarge();
			
			if (Configuration.IS_RES_DISABLED.equals("0")) {
				// do all three resistance one by one
				testName = "RES TEST 1";
				startTest(startR1, testName);
				curResOn = 1;
				capture(curRow, testName);
				stopTest(startR1);
				
				testName = "RES TEST 2";
				startTest(startR2, testName);
				curResOn = 2;
				capture(curRow, testName);
				stopTest(startR2);
				
				if (Configuration.IS_COM_RES_DISABLED.equals("0") || curStatorPhase.equals("Three")) {
					testName = "RES TEST 3";
					startTest(startR3, testName);
					curResOn = 3;
					capture(curRow, testName);
					stopTest(startR3);
				} else {
					tblTestEntry.setValueAt("NA", curRow, 6); 
				}
				// find the result and proceed further only it is passed
				findResult(curRow, "RES TEST");
			}
			
			if (Configuration.IS_INS_RES_DISABLED.equals("0")) {
				testName = "INS RES TEST";
				startTest(startIR, testName);
				capture(curRow, testName);
				stopTest(startIR);
				// find the result and proceed further only it is passed
				findResult(curRow, testName);
			}
			
			if (Configuration.IS_HV_DISABLED.equals("0")) {
				System.out.println("---STARTING HV---");
				testName = "HV TEST";
				startTest(startHV1, testName);
				startTest(startHV2, testName);
				Thread.sleep(2000);
				lastHVTestFailed = devPLC.readCoil(sigHVFail, devPLCProtocol);
				System.out.println("HV Result: " + lastHVTestFailed);
				if (!lastHVTestFailed) {
					if (Integer.valueOf(Configuration.REL_TIME_HV) > 2) {
						Thread.sleep(1000 * Integer.valueOf(Configuration.REL_TIME_HV) - 2);
					}
					capture(curRow, testName);
				}
				stopTest(startHV1);
				stopTest(startHV2);
				doDischarge();
				// find the result and proceed further only it is passed
				findResult(curRow, testName);
			}
			
			if (Configuration.IS_INS_RES_AFTER_HV_DISABLED.equals("0")) {
				System.out.println("---STARTING INS RES---");
				testName = "INS RES TEST AFTER HV";
				startTest(startIRAfterHV, testName);
				capture(curRow, testName);
				stopTest(startIRAfterHV);
				// find the result and proceed further only it is passed
				findResult(curRow, testName);
			}
			
			if (Configuration.IS_SURGE_DISABLED.equals("0")) {
				System.out.println("---SURGE---");
				testName = "SURGE TEST";
				System.out.println("Starting Surge...");
				startTest(startSurge1, testName);
				startTest(startSurge2, testName);
				capture(curRow, testName);
				if (lastSurgeFailed) {
					tblTestEntry.setValueAt("NOT OK", curRow, 12); 
				} else {
					tblTestEntry.setValueAt("OK", curRow, 12); 
				}
				System.out.println("Stoping Surge...");
				stopTest(startSurge1);
				stopTest(startSurge2);
				// find the result and proceed further only it is passed
				findResult(curRow, testName);
			} else {
				tblTestEntry.setValueAt("NA", curRow, 12); 
			}
			
			// continue with other test only if Surge test was successful
			if (Configuration.IS_DIR_TEST_DISABLED.equals("0")) {
				System.out.println("---DIR---");
				testName = "DIRECTION TEST";
				startTest((curStatorDir.equals("FWD") ? startDirFwd : startDirRev), testName);
				for(int i=0; i < Integer.valueOf(Configuration.REL_TIME_DIR) * 2; i++) {
					Thread.sleep(500);
					lastDirFailed = !devPLC.readCoil(sigDirPass, devPLCProtocol);
					if (!lastDirFailed) { // break when the direction test is passed
						break;
					}
				}
				System.out.println("direction result " + devPLC.readCoil(sigDirPass, devPLCProtocol));
				if (lastDirFailed) {
					tblTestEntry.setValueAt("NOT OK", curRow, 13); 
				} else {
					tblTestEntry.setValueAt("OK", curRow, 13); 
				}
				stopTest((curStatorDir.equals("FWD") ? startDirFwd : startDirRev));
				// find the result and proceed further only it is passed
				findResult(curRow, testName);
			} else {
				tblTestEntry.setValueAt("NA", curRow, 13); 
			}
			
			// final discharge after successful test
			firstRetest = false;
			doDischarge();
			// stop the panel
			devPLC.writeCoil(sigAutoStart, false, devPLCProtocol); // no use for now
			
		} catch (TestFailedException fe) {
				autoCaptureInProgress = false;
				// stop the panel
				devPLC.writeCoil(sigAutoStart, false, devPLCProtocol); // no use for now
				if (!tglRetest.isSelected()) {
					if (JOptionPane.showConfirmDialog(this, "Do you want to enable retest?", "Retest?", JOptionPane.YES_NO_OPTION) == 0) {
						firstRetest = true;
						tglRetest.setSelected(true);
						tglRetestActionPerformed();
					}
				} else {
					// disable retest 
					tglRetest.setSelected(false);
					tglRetest.setIcon(new ImageIcon(getClass().getResource("/img/start.png")));
					tglRetest.setBackground(Color.RED);
					tglRetest.setText("<html>Retest&nbsp;&nbsp;<font size=-2>[F8]</html>");
				}
				return;
		} catch (Exception e) {
			if (e.getMessage().contains("TEST ABORTED")) {
				autoCaptureInProgress = false;
				// stop the panel
				devPLC.writeCoil(sigAutoStart, false, devPLCProtocol); // no use for now
				changeApplicationStatus("TEST WAS ABORTED");
				resetOutputCoils();
				Thread.sleep(3000);
				return;
			} else {
				throw e;
			}
		} finally {
			// reset signal label
			txtSignal.setText("");
			autoCaptureInProgress = false;
			
			// disable retest in case enabled earlier
			if (tglRetest.isSelected()&& !firstRetest) {
				firstRetest = false;
				tglRetest.setSelected(false);
				tglRetest.setIcon(new ImageIcon(getClass().getResource("/img/start.png")));
				tglRetest.setBackground(Color.RED);
				tglRetest.setText("<html>Retest&nbsp;&nbsp;<font size=-2>[F8]</html>");
			}
		}
		
		// find final result & save changes
		findResult(curRow, "ALL DONE");
	}
	
	// function to find current station
	private Integer findCurrentStation() throws Exception {
		// station
		int i=0;
		if (Integer.valueOf(Configuration.NUMBER_OF_STATIONS) == 1) {
			curActiveStation = 0;
		} else {// more than one station
			for(i=0; i< Integer.valueOf(Configuration.NUMBER_OF_STATIONS); i++) {
				if (devPLC.readCoil(devCfg.getDevRegister("PLC","Station " + (i+1) + " Signal"), devPLCProtocol)) {
					if (curActiveStation != i) {
						curActiveStation = i;
					}
					break;
				}
			}
		}
		
		if (i == Integer.valueOf(Configuration.NUMBER_OF_STATIONS) && !devPLC.readCoil(sigAutoStart, devPLCProtocol)) { // all stations are off
			if (curActiveStation > -1) {
				// hence, reset previous station
				stationList.get(curActiveStation).setIcon(idleImg);
				devPLC.writeCoil(devCfg.getDevRegister("PLC","Red Lamp"), false, devPLCProtocol);
				devPLC.writeCoil(devCfg.getDevRegister("PLC","Yellow Lamp"), false, devPLCProtocol);
				devPLC.writeCoil(devCfg.getDevRegister("PLC","Green Lamp"), false, devPLCProtocol);
				curActiveStation = -1;
			}
		}
		
		// test mode
		tmpTestMode = devPLC.readCoil(selAutoMan, devPLCProtocol)?"A":"M";
		if (!curTestMode.equals(tmpTestMode)) {
			curTestMode = tmpTestMode;
			setTestModeLabel();
		}
					
		return curActiveStation;
	}
	
	private void doDischarge() throws Exception{
		changeApplicationStatus("DISCHARGE IN PROGRESS...");
		txtSignal.setText("");
		devPLC.writeCoil(startDis, true, devPLCProtocol);
		Thread.sleep(1950);
		devPLC.writeCoil(startDis, false, devPLCProtocol);
		Thread.sleep(50);
	}
	
	private Integer startTest(Integer startCoil, String testNm) throws Exception {
		// check for abrupt panel stop
		if (!devPLC.readCoil(sigAutoStart, devPLCProtocol)) {
			throw new Exception("TEST ABORTED");
		}
		changeApplicationStatus(testNm + " IS IN PROGRESS...");
		txtSignal.setText("");
		devPLC.writeCoil(startCoil, true, devPLCProtocol);
		Thread.sleep(50);
		return startCoil;
	}
	
	private void stopTest(Integer stopCoil) throws Exception {
		devPLC.writeCoil(stopCoil, false, devPLCProtocol);
		Thread.sleep(50);
	}
	
	
	// function to fetch only number part of a string
	private String numberPartOfString(String SNo) {
		String c = "";
		String no = "";
		while (!SNo.isEmpty()) {
			c = SNo.substring(SNo.length()-1);
			try {
				Integer.parseInt(c);
				no = c + no;
			} catch (NumberFormatException e) {
				break;
			}
			SNo = SNo.substring(0, SNo.length()-1);
		}
		return no;
	}
		
	// function to set capture date and sno
	private void setCaptureDateAndSNo(int row) throws Exception{
		tblTestEntry.setRowSelectionInterval(row, row);
		
		// set date & reading number
		today = Calendar.getInstance().getTime();
		tblTestEntry.setValueAt(reqDtFormat.format(today), row, 2);
		tblTestEntry.setValueAt(curActiveStation+1, row, 0); 
		
		if (!tglRetest.isSelected()) { // no serial number change for retest
			String testNo = "";
			if (tblTestEntry.getValueAt(row, 1).toString().trim().isEmpty()) {
				// insert next test in master
				stmt.executeUpdate("insert into READING_DETAIL(stator_type, test_date, vendor_ref, user, line) values ('" + curStatorType + "','" + dbDtTimeFormat.format(today) + "','" + curVendorRef + "','" + Configuration.USER + "','" + Configuration.LINE_NAME + "')");
				ResultSet res = stmt.executeQuery("select seq from sqlite_sequence where name='READING_DETAIL'");
				if (res.next()) {
					testNo = res.getString("seq");
					tblTestEntry.setValueAt(testNo, row, 1);
				}
				res.close();
			} else {
				testNo = tblTestEntry.getValueAt(row, 1).toString().trim();
			}
			
			if ((Configuration.SNO_GEN_METHOD.equals("S") && !scannedSNo.isEmpty())) {
				tblTestEntry.setValueAt(scannedSNo, row, 3);
				// save master record for scanned motor sno
				saveChanges(row, row);
				// save the value for future reference
				Configuration.RECENT_STATOR_SNO = scannedSNo;
				Configuration.saveCommonConfigValues("RECENT_STATOR_SNO");
			}
		}
	}
	
	// function to capture readings
	private boolean capture(int row, String testNm) throws Exception {
		captureError = false;
		captureInProgress = true;
		if (testNm.equals("RES TEST 1")) {
			Thread.sleep(1000 * Integer.valueOf(Configuration.REL_TIME_START_COIL));
			changeApplicationStatus("CAPTURING READING " + (row +1) + " [" + testNm + "]...");
			tblTestEntry.setValueAt(curResData, row, 4); // 1.1 starting coil
			
			try {
				tblTestEntry.setValueAt(devCfg.readParam(devPLC,"PLC","Temperature 1",""), row, 7); // 1.4 temp
			} catch (Exception e) {
				logError("Temperature 1:" + e.getMessage());
				captureError = true;
			}	
				
		} else if (testNm.equals("RES TEST 2")) {
			Thread.sleep(1000 * Integer.valueOf(Configuration.REL_TIME_RUN_COIL)); 
			changeApplicationStatus("CAPTURING READING " + (row +1) + " [" + testNm + "]...");
			tblTestEntry.setValueAt(curResData, row, 5); // 1.2 running coil
		} else if (testNm.equals("RES TEST 3")) {
			Thread.sleep(1000 * Integer.valueOf(Configuration.REL_TIME_COM_COIL));
			changeApplicationStatus("CAPTURING READING " + (row +1) + " [" + testNm + "]...");
			tblTestEntry.setValueAt(curResData, row, 6); // 1.3 common coil
		} else if (testNm.equals("INS RES TEST")) {
			try {
				Thread.sleep(1000 * Integer.valueOf(Configuration.REL_TIME_INS_RES));
				changeApplicationStatus("CAPTURING READING " + (row +1) + " [" + testNm + "]...");
				tblTestEntry.setValueAt(curIRData, row, 8);
			} catch (Exception e) {
				logError("Ins Resistance:" + e.getMessage());
				captureError = true;
			}
		} else if (testNm.equals("HV TEST")) {
			try {
				changeApplicationStatus("CAPTURING READING " + (row +1) + " [" + testNm + "]...");
				tblTestEntry.setValueAt(devCfg.readParam(devGrindex,"Grindex Electrik Analyser","kV",""), row, 9); // 3.1 voltage
				tblTestEntry.setValueAt(devCfg.readParam(devGrindex,"Grindex Electrik Analyser","mA",""), row, 10); // 3.2 current
			} catch (Exception e) {
				logError("HV:" + e.getMessage());
				captureError = true;
			}
		} else if (testNm.equals("INS RES TEST AFTER HV")) {
			try {
				Thread.sleep(1000 * Integer.valueOf(Configuration.REL_TIME_INS_RES));
				changeApplicationStatus("CAPTURING READING " + (row +1) + " [" + testNm + "]...");
				tblTestEntry.setValueAt(curIRData, row, 11);
			} catch (Exception e) {
				logError("Ins Resistance After HV:" + e.getMessage());
				captureError = true;
			}
		} else if (testNm.equals("SURGE TEST")) {
			try {
				Configuration.CUR_TEST_SLNO = tblTestEntry.getValueAt(row, 1).toString().trim();
				
				// capture surge wave if enabled
				if (Configuration.IS_SURGE_WAVE_CAPTURED.equals("1")) {
					// clean up the db if needed
					try {
						stmt.executeUpdate("delete from " + Configuration.SURGE_IMAGE + " where test_slno='" + Configuration.CUR_TEST_SLNO + "'");
					} catch (SQLException e) {
						// ignore as exception may occur very first time when the table itself is not exist
					}
					
					// remove existing images in test table if any
					while (pnlWaveImg.getComponentCount() > 0) {
						pnlWaveImg.remove(0);
					}
					pnlWaveImg.revalidate();
					// capture images
					changeApplicationStatus("CAPTURING READING " + (row +1) + " [" + testNm + "]...");
					Thread.sleep(1750); // time to wait for first image
					for(availCount=0; availCount < Integer.valueOf(Configuration.NUMBER_OF_SURGE_IMG); availCount++) {
						// show the image in test table
						JLabel lblImg = new JLabel();
						lblImg.setBorder(new LineBorder(Color.lightGray));
						curImg = webCam.getImage();
						lblImg.setIcon(new ImageIcon(curImg));
						pnlWaveImg.add(lblImg, new TableLayoutConstraints(0, availCount, 0, availCount, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
						pnlWaveImg.revalidate();
						// add it into db
						Configuration.storeSurgeWave(curImg);
						
						// wait until next image is available
						Thread.sleep((int) (Float.valueOf(Configuration.SURGE_WAVE_INTERVAL) * 955));
					}
				} else {
					// wait to capture till test completion or failure
					for(int i=0; i<Integer.valueOf(Configuration.REL_TIME_SURGE); i++) {
						Thread.sleep(1000); 
						if (devPLC.readCoil(sigSurgeFail, devPLCProtocol)) {
							lastSurgeFailed = true;
							break;
						}
					}
					changeApplicationStatus("CAPTURING READING " + (row +1) + " [" + testNm + "]...");
				}
			} catch (Exception e) {
				logError("Surge:" + e.getMessage());
				captureError = true;
			}
		}
		
		if (captureError) {
			capStatus = "CAPTURE FAILED";
		} else {
			capStatus = "CAPTURE SUCCEEDED";
		}
		
		capStatus += " [" + testNm +"]";
		
		changeApplicationStatus(capStatus);
			
		captureInProgress = false;
		return true;
	}
	
	// function to calculate result
	private void findResult(int row, String testName) throws TestFailedException {
		testRes = "";
		failReason = "";
		try {
			if (!testName.equals("ALL DONE")) {
				if (testName.equals("RES TEST")) {
					
					strActRes1 = tblTestEntry.getValueAt(row, 4).toString().isEmpty() ? "0" : tblTestEntry.getValueAt(row, 4).toString().trim();
					strActRes2 = tblTestEntry.getValueAt(row, 5).toString().isEmpty() ? "0" : tblTestEntry.getValueAt(row, 5).toString().trim();
					strActRes3 = tblTestEntry.getValueAt(row, 6).toString().isEmpty() ? "0" : tblTestEntry.getValueAt(row, 6).toString().trim();
					
					if (curStatorPhase.equals("Single")) {
						if (Float.valueOf(strActRes1) < declaredResLow1) {
							failReason += "SR<LL;";
						} else if (Float.valueOf(strActRes1) > declaredResUp1) {
							failReason += "SR>UL;";
						}
						if (Float.valueOf(strActRes2) < declaredResLow2) {
							failReason += "RR<LL;";
						} else if (Float.valueOf(strActRes2) > declaredResUp2) {
							failReason += "RR>UL;";
						}
						if (Configuration.IS_COM_RES_DISABLED.equals("0") || curStatorPhase.equals("Three")) {
							if (Float.valueOf(strActRes3) < declaredResLow3) {
								failReason += "CR<LL;";
							} else if (Float.valueOf(strActRes3) > declaredResUp3) {
								failReason += "CR>UL;";
							}
						}
					} else {
						if (Float.valueOf(strActRes1) < declaredResLow1) {
							failReason += "R-RES<LL;";
						} else if (Float.valueOf(strActRes1) > declaredResUp1) {
							failReason += "R-RES>UL;";
						}
						if (Float.valueOf(strActRes2) < declaredResLow1) {
							failReason += "Y-RES<LL;";
						} else if (Float.valueOf(strActRes2) > declaredResUp1) {
							failReason += "Y-RES>UL;";
						}
						if (Float.valueOf(strActRes3) < declaredResLow1) {
							failReason += "B-RES<LL;";
						} else if (Float.valueOf(strActRes3) > declaredResUp1) {
							failReason += "B-RES>UL;";
						}
					}
				} else if (testName.equals("INS RES TEST")) {
					strActInsBH = tblTestEntry.getValueAt(row, 8).toString().isEmpty() ? "0" : tblTestEntry.getValueAt(row, 8).toString().trim();
					if (Float.valueOf(strActInsBH) < declaredInsResL) {
						failReason += "IR BEF HV<LL;";
					}
				} else if (testName.equals("HV TEST")) {
					if (lastHVTestFailed) {
						failReason += "HV FAILED;";
					}
					strActMAH = tblTestEntry.getValueAt(row, 10).toString().isEmpty() ? "0" : tblTestEntry.getValueAt(row, 10).toString().trim();
					if (Float.valueOf(strActMAH) > declaredMAH) {
						failReason += "HV mA>UL;";
					}
				} else if (testName.equals("INS RES TEST AFTER HV")) {
					strActInsAH = tblTestEntry.getValueAt(row, 11).toString().isEmpty() ? "0" : tblTestEntry.getValueAt(row, 11).toString().trim();
					if (Float.valueOf(strActInsAH) < declaredInsResL) {
						failReason += "IR AFT HV<LL;";
					}
				} else if (testName.equals("SURGE TEST")) {
					if (lastSurgeFailed) {
						failReason += "SURGE FAILED;";
					}
				} else if (testName.equals("DIRECTION TEST")) {
					if (lastDirFailed) {
						failReason += "DIR FAILED";
					}
				}
				
				// fail and abort the test if any test was failed
				if (!failReason.isEmpty()) {
					testRes = "FAIL";
					tblTestEntry.setValueAt(testRes, row, 14);
					tblTestEntry.setValueAt(failReason, row, 15);
					stationList.get(curActiveStation).setIcon(failImg);
					// switch on red lamp in panel
					devPLC.writeCoil(devCfg.getDevRegister("PLC","Red Lamp"), true, devPLCProtocol);
					devPLC.writeCoil(devCfg.getDevRegister("PLC","Yellow Lamp"), false, devPLCProtocol);
					devPLC.writeCoil(devCfg.getDevRegister("PLC","Green Lamp"), false, devPLCProtocol);
					
					// generate stator sno for passed stator or even for failed stator if opted
					if (tblTestEntry.getValueAt(row, 3).toString().isEmpty() && Configuration.IS_GEN_SNO_FOR_FAILED_TEST.equals("1")) {
						curStatorSNo = Configuration.setAndGetSNo(tblTestEntry.getValueAt(row, 1).toString());
						tblTestEntry.setValueAt(curStatorSNo, row, 3);
					}
					
					doDischarge();
				}
				
			} else {
				// pass the test if as all tests were passed
				testRes = "PASS";
				tblTestEntry.setValueAt(testRes, row, 14);
				tblTestEntry.setValueAt("", row, 15);
				stationList.get(curActiveStation).setIcon(passImg);
				devPLC.writeCoil(devCfg.getDevRegister("PLC","Green Lamp"), true, devPLCProtocol);
				devPLC.writeCoil(devCfg.getDevRegister("PLC","Yellow Lamp"), false, devPLCProtocol);
				devPLC.writeCoil(devCfg.getDevRegister("PLC","Red Lamp"), false, devPLCProtocol);
				
				// generate stator sno for passed stator 
				if (tblTestEntry.getValueAt(row, 3).toString().isEmpty()) {
					curStatorSNo = Configuration.setAndGetSNo(tblTestEntry.getValueAt(row, 1).toString());
					tblTestEntry.setValueAt(curStatorSNo, row, 3);
				}
				
				// scroll to bottom
				// tblTestEntry.scrollRectToVisible(tblTestEntry.getCellRect(row, 0, true));
				// tblMotList.setRowSelectionInterval(row, row);
			}
			
			
			// add next row if req
			addRowIfReq(row);
			
			// save the changes
			saveChanges(row, row);
			refreshTestCount();
						
			// print QR code if enabled (even for failed cases if opted)
			if (Configuration.IS_QR_PRINT_ENABLED.equals("1") && !tblTestEntry.getValueAt(row, 3).toString().trim().isEmpty()) {
				if (testRes.equals("PASS") || (testRes.equals("FAIL") && Configuration.IS_QR_PRINT_ENABLED_FOR_FAIL.equals("1"))) {
					String sNo = tblTestEntry.getValueAt(row, 3).toString().trim();
					String dt = tblTestEntry.getValueAt(row, 2).toString().trim();
					String line3 = "";
					String line4 = "";
					if (Configuration.QR_LINE3.equals("SM")) {
						line3 = curStatorType;
					} else if (Configuration.QR_LINE3.equals("VR")) {
						line3 = curVendorRef;
					} else {
						line3 = Configuration.USER;
					}
					if (Configuration.QR_LINE4.equals("SM")) {
						line4 = curStatorType;
					} else if (Configuration.QR_LINE4.equals("VR")) {
						line4 = curVendorRef;
					} else if (Configuration.QR_LINE4.equals("TN")) {
						line4 = Configuration.USER;
					} else {
						line4 = "";
					}
					String code = sNo;
					if (Configuration.QR_IS_INCLUDE_LINE2.equals("YES")) {
						code += "," + dt;
					}
					if (Configuration.QR_IS_INCLUDE_LINE3.equals("YES")) {
						code += "," + line3;
					}
					if (Configuration.QR_IS_INCLUDE_LINE4.equals("YES")) {
						code += "," + line4;
					}
					printQRCode.print(code, sNo, dt, line3, line4);
				}
			}
			
			// raise an exception if a test was failed so that text will abort
			if (testRes.equals("FAIL")) {
				changeApplicationStatus("TEST FAILED");
				JOptionPane.showMessageDialog(this, testName + " FAILED\n" + failReason, "Test Failed", JOptionPane.ERROR_MESSAGE);
				throw new TestFailedException();
			}
		} catch(TestFailedException fe) {
			throw fe;
		} catch(Exception e) {
			logError("Error calculating the result. Please make sure you have chosen a stator");
		}
	}
	
	private boolean saveChanges(int fromRow, int toRow) throws Exception {
		changeApplicationStatus("SAVING CURRENT READING...");
		Boolean sqlError = false;
				
		for(int i=fromRow; i<=toRow; i++) {
			// insert or update detail reading
			if (tblTestEntry.getValueAt(i, 2).toString().trim().isEmpty()) {
				continue;
			}
			
			try {
				stmt.executeUpdate("update READING_DETAIL set test_date ='" + dbDtFormat.format(reqDtFormat.parse(tblTestEntry.getValueAt(i, 2).toString().trim())) + "',  stator_slno= '" + tblTestEntry.getValueAt(i, 3).toString().trim() + 
						"', res_start='" + tblTestEntry.getValueAt(i, 4).toString().trim() + 
						"',res_run='" + tblTestEntry.getValueAt(i, 5).toString().trim() + "',res_com='" + tblTestEntry.getValueAt(i, 6).toString().trim() + 
						"',temp='" + tblTestEntry.getValueAt(i, 7).toString().trim() + "',ins_res_bef_hv='" + tblTestEntry.getValueAt(i, 8).toString().trim() + 
						"', hv_kv='" + tblTestEntry.getValueAt(i, 9).toString().trim() + "',hv_amps='" + tblTestEntry.getValueAt(i, 10).toString().trim() + 
						"', ins_res_aft_hv='" + tblTestEntry.getValueAt(i, 11).toString().trim()  + "',surge='" + tblTestEntry.getValueAt(i, 12).toString().trim() + 
						"',dir='" + tblTestEntry.getValueAt(i, 13).toString().trim() + "',test_result='" + tblTestEntry.getValueAt(i, 14).toString().trim() + "',remark='" + tblTestEntry.getValueAt(i, 15).toString().trim() + 
						"',vendor_ref='" + curVendorRef + "', line = '" + tblTestEntry.getValueAt(i, 0).toString().trim()  + "',user = '" + Configuration.USER + 
						"' where test_slno= '" + tblTestEntry.getValueAt(i, 1).toString().trim() + "'");
				
			} catch (SQLException se) {
				logError("Failed saving the test #" + tblTestEntry.getValueAt(i, 1).toString().trim() + ":" + se.getMessage());
				sqlError = true;
			}
		}
		
		if (sqlError) {
			changeApplicationStatus("SAVE FAILED");
		} else {
			changeApplicationStatus("CHANGES ARE SAVED");
		}
		
		return sqlError;
	}
	private void clearForm() {
		// clear the test table
		for (int r=0; r<tblTestEntry.getRowCount(); r++) {
			for (int c=0; c<tblTestEntry.getColumnCount(); c++) {
				tblTestEntry.setValueAt("", r, c);
			}
		}
		
		tblTestEntry.setRowSelectionInterval(0, 0);
		tblTestEntry.scrollRectToVisible(tblTestEntry.getCellRect(0, 0, true));
	}
	
	// function to find next empty row in the reading list
	private int findNextEmptyRow() {
		int r = 0;
		// add new row if table is full already
		if (!tblTestEntry.getValueAt(tblTestEntry.getRowCount() - 1, 0).toString().isEmpty()) {
			r = tblTestEntry.getRowCount();
			addRowIfReq(r);
		} else {
			// empty row from bottom of the table
			for(int i=tblTestEntry.getRowCount()-1; i>0; i--) {
				if(tblTestEntry.getValueAt(i, 0).toString().isEmpty() && !tblTestEntry.getValueAt(i-1, 0).toString().isEmpty()) {
					r = i;
					break;
				}
			}
		}
		return r;
	}
	
	// function to add row if required
	private void addRowIfReq(int curRow) {
		// add an empty row if required
		if (curRow >= tblTestEntry.getRowCount()) {
			// add a new row
			DefaultTableModel defModel = (DefaultTableModel) tblTestEntry.getModel();
			defModel.addRow( new Object[] {"", "", "", "", "", "", "", "", "", "", "", "", "","", "", ""});
			scrlTest.getVerticalScrollBar().setValue(scrlTest.getVerticalScrollBar().getMaximum() + 10);
		}
		// tblTestEntry.update(tblTestEntry.getGraphics());
		tblTestEntry.setRowSelectionInterval(curRow, curRow);
		tblTestEntry.scrollRectToVisible(tblTestEntry.getCellRect(curRow, 0, true));
	}
	
	// custom functions - end
	

	private void lblLogoMouseReleased() {
		ContactUs dlgContact = new ContactUs(this);
		dlgContact.setVisible(true);
	}

	private void lstErrorMouseClicked(MouseEvent e) {
		if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 2) {
			JDialog dlg = new JDialog();
			dlg.setSize(1000,500);
			dlg.setTitle("Error Log: SurgeView");
			dlg.setLocationRelativeTo(this);
			JScrollPane scrl = new JScrollPane();
			JTextArea txt = new JTextArea();
			DefaultListModel model = (DefaultListModel) lstError.getModel();
			String err = "";
			for (int i=0; i<model.getSize(); i++) {
				err += model.getElementAt(i) + "\n";
			}
			txt.setText(err);
			scrl.setViewportView(txt);
			dlg.add(scrl);
			dlg.setVisible(true);
		}
	}

	private void lstErrorKeyPressed(KeyEvent e) {
		if (e.getKeyChar() == KeyEvent.VK_DELETE) {
			if (JOptionPane.showConfirmDialog(this, "Sure, clear the error list?") == 0) {
				DefaultListModel model = (DefaultListModel) lstError.getModel();
				model.clear();
				errLogModel.addElement("Description is shown here while an error occurs; Double click to expand, Delete key to clear.");
				lstError.setSelectedIndex(0);
			}
		}
	}

	private void cmdUserActionPerformed() {
		UserManagement frmUser = new UserManagement(this);
		frmUser.setVisible(true);
	}

	private void cmdDevActionPerformed() {
		DeviceSettings frmDev = new DeviceSettings(this);
		frmDev.setVisible(true);
	}

	private void cmdTestActionPerformed() {
		TestSettings frmTest = new TestSettings(this, curStatorPhase);
		frmTest.setVisible(true);
	}

	private void cmdOtherActionPerformed() {
		OtherSettings frmOther = new OtherSettings(this);
		frmOther.setVisible(true);
	}

	private void tglTestActionPerformed() {
		if (tglTest.isSelected()) {
			// start auto capture after initializing devices
			if (!deviceInitialized) {
				initializeDevices();
				initProtocol();
			}
			
			tglTest.setIcon(new ImageIcon(getClass().getResource("/img/stop.png")));
			tglTest.setBackground(Color.GREEN);
			tglTest.setText("<html>Stop Test&nbsp;<font size=-2>[F7]</html>");
			
			startAutoCapture();
//			refreshTestCount();
		} else {
			// switch it off
			stopAutoCapture();
		}
	}

	private void startLiveReading() {
		// start live reading
		System.out.println("Starting live reading");
		changeApplicationStatus("STARTING LIVE READING...");
		threadLiveRead = new Thread(liveReading);
		threadLiveRead.start();
		changeApplicationStatus("LIVE READING STARTED");
		System.out.println("Started");
	}
	private void stopLiveReading() {
		System.out.println("Stopping live reading");
		// stop live reading
		threadLiveRead.interrupt();
		while(threadLiveRead.isAlive()) {
			// wait until the thread dies completely
		}
		System.out.println("Stopped");
	}
	
	private void startAutoCapture() {
		System.out.println("Starting auto capture");
		threadAutoCapture = new Thread(autoCapture);
		threadAutoCapture.start();
		System.out.println("Started");
	}
	
	private void stopAutoCapture() {
		System.out.println("Stopping auto capture");
		threadAutoCapture.interrupt();
		while(threadAutoCapture.isAlive()) {
			// wait until the thread dies completely
		}
		System.out.println("Stopped");
	}
	
	private void cmdOpenActionPerformed() {
		FileOpen dlgOpen = new FileOpen(this);
		dlgOpen.setVisible(true);
	}

	private void cmdRepActionPerformed() {
		// prepare printing document and print preview
		this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		String recentSNo = "";
		if (tblTestEntry.getSelectedRow() >= 0) {
			recentSNo = tblTestEntry.getValueAt(tblTestEntry.getSelectedRow(), 2).toString();
		}
		if (recentSNo.isEmpty()) {
			recentSNo = Configuration.RECENT_STATOR_SNO;
		}
		
		Calendar recentDt = Calendar.getInstance();
		try {
			recentDt.setTime(reqDtFormat.parse(tblTestEntry.getValueAt(tblTestEntry.getSelectedRow(), 2).toString()));
		} catch (ParseException e) {
			// ignore
		}
		
		Reports dlgRpt = new Reports(this,curStatorType, recentSNo, recentDt);
		dlgRpt.setVisible(true);
		this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
	}

	private void cmdSaveActionPerformed() {
		testRes = "";
		failReason = "";
		int row = tblTestEntry.getSelectedRow();
		
		if (row < 0 || tblTestEntry.getValueAt(row, 1).toString().isEmpty()) {
			JOptionPane.showMessageDialog(this, "Nothing to save.\nPlease select a reading to be saved");
			return;
		}
		
		try {
			// check limits for all tests
			// 1. res
			if (Configuration.IS_RES_DISABLED.equals("0")) {
				strActRes1 = tblTestEntry.getValueAt(row, 4).toString().isEmpty() ? "0" : tblTestEntry.getValueAt(row, 4).toString().trim();
				strActRes2 = tblTestEntry.getValueAt(row, 5).toString().isEmpty() ? "0" : tblTestEntry.getValueAt(row, 5).toString().trim();
				strActRes3 = tblTestEntry.getValueAt(row, 6).toString().isEmpty() ? "0" : tblTestEntry.getValueAt(row, 6).toString().trim();
				
				if (curStatorPhase.equals("Single")) {
					if (Float.valueOf(strActRes1) < declaredResLow1) {
						failReason += "SR<LL;";
					} else if (Float.valueOf(strActRes1) > declaredResUp1) {
						failReason += "SR>UL;";
					}
					if (Float.valueOf(strActRes2) < declaredResLow2) {
						failReason += "RR<LL;";
					} else if (Float.valueOf(strActRes2) > declaredResUp2) {
						failReason += "RR>UL;";
					}
					if (!strActRes3.equals("NA") && (Configuration.IS_COM_RES_DISABLED.equals("0") || curStatorPhase.equals("Three"))) {
						if (Float.valueOf(strActRes3) < declaredResLow3) {
							failReason += "CR<LL;";
						} else if (Float.valueOf(strActRes3) > declaredResUp3) {
							failReason += "CR>UL;";
						}
					}
				} else {
					if (Float.valueOf(strActRes1) < declaredResLow1) {
						failReason += "R-RES<LL;";
					} else if (Float.valueOf(strActRes1) > declaredResUp1) {
						failReason += "R-RES>UL;";
					}
					if (Float.valueOf(strActRes2) < declaredResLow1) {
						failReason += "Y-RES<LL;";
					} else if (Float.valueOf(strActRes2) > declaredResUp1) {
						failReason += "Y-RES>UL;";
					}
					if (Float.valueOf(strActRes3) < declaredResLow1) {
						failReason += "B-RES<LL;";
					} else if (Float.valueOf(strActRes3) > declaredResUp1) {
						failReason += "B-RES>UL;";
					}
				}
			}
			
			// ins res
			if (Configuration.IS_INS_RES_DISABLED.equals("0")) {
				strActInsBH = tblTestEntry.getValueAt(row, 8).toString().isEmpty() ? "0" : tblTestEntry.getValueAt(row, 8).toString().trim();
				if (Float.valueOf(strActInsBH) < declaredInsResL) {
					failReason += "IR BEF HV<LL;";
				}
			}
			
			// hv
			if (Configuration.IS_HV_DISABLED.equals("0") && tblTestEntry.getValueAt(row, 15) != null) {
				if (tblTestEntry.getValueAt(row, 15).toString().contains("HV FAILED")) {
					failReason += "HV FAILED;";
				}
				strActMAH = tblTestEntry.getValueAt(row, 10).toString().isEmpty() ? "0" : tblTestEntry.getValueAt(row, 10).toString().trim();
				if (Float.valueOf(strActMAH) > declaredMAH) {
					failReason += "HV mA>UL;";
				}
			}
			
			// ins res after hv
			if (Configuration.IS_INS_RES_AFTER_HV_DISABLED.equals("0")) {
				strActInsAH = tblTestEntry.getValueAt(row, 11).toString().isEmpty() ? "0" : tblTestEntry.getValueAt(row, 11).toString().trim();
				if (Float.valueOf(strActInsAH) < declaredInsResL) {
					failReason += "IR AFT HV<LL;";
				}
			}
			
			// surge
			if (Configuration.IS_SURGE_DISABLED.equals("0") && tblTestEntry.getValueAt(row, 12) != null) {
				if (!tblTestEntry.getValueAt(row, 12).toString().trim().equals("OK")) {
					failReason += "SURGE FAILED;";
				}
			}
			
			// dir
			if (Configuration.IS_DIR_TEST_DISABLED.equals("0") && tblTestEntry.getValueAt(row, 13) != null) {
				if (!tblTestEntry.getValueAt(row, 13).toString().trim().equals("OK")) {
					failReason += "DIR FAILED";
				}
			}
			
			// fail and abort the test if any test was failed
			if (!failReason.isEmpty()) {
				testRes = "FAIL";
				tblTestEntry.setValueAt(testRes, row, 14);
				tblTestEntry.setValueAt(failReason, row, 15);
			} else {
				// pass the test if as all tests were passed
				testRes = "PASS";
				tblTestEntry.setValueAt(testRes, row, 14);
				tblTestEntry.setValueAt("", row, 15);
			}
			
			// generate stator sno for passed stator or even for failed stator if opted
			if (tblTestEntry.getValueAt(row, 3) != null && tblTestEntry.getValueAt(row, 3).toString().isEmpty() && (failReason.isEmpty() || Configuration.IS_GEN_SNO_FOR_FAILED_TEST.equals("1"))) {
				curStatorSNo = Configuration.setAndGetSNo(tblTestEntry.getValueAt(row, 1).toString());
				tblTestEntry.setValueAt(curStatorSNo, row, 3);
			}
			
			// save the changes
			saveChanges(row, row);
			
			if (testRes.equals("FAIL")) {
				JOptionPane.showMessageDialog(this, "Changes saved, but test failed\n" + failReason, "Test Failed", JOptionPane.ERROR_MESSAGE);
			} else {
				JOptionPane.showMessageDialog(this, "Changes saved successfully");
			}
						
			// print QR code if enabled (even for failed cases if opted)
			if (Configuration.IS_QR_PRINT_ENABLED.equals("1") && !tblTestEntry.getValueAt(row, 3).toString().trim().isEmpty()) {
				if (testRes.equals("PASS") || (testRes.equals("FAIL") && Configuration.IS_QR_PRINT_ENABLED_FOR_FAIL.equals("1"))) {
					if (JOptionPane.showConfirmDialog(this, "Do you want to print QR Code of this test?\nSNo:" + tblTestEntry.getValueAt(row, 3).toString().trim()) == 0) {
						String sNo = tblTestEntry.getValueAt(row, 3).toString().trim();
						String dt = tblTestEntry.getValueAt(row, 2).toString().trim();
						String line3 = "";
						String line4 = "";
						if (Configuration.QR_LINE3.equals("SM")) {
							line3 = curStatorType;
						} else if (Configuration.QR_LINE3.equals("VR")) {
							line3 = curVendorRef;
						} else {
							line3 = Configuration.USER;
						}
						if (Configuration.QR_LINE4.equals("SM")) {
							line4 = curStatorType;
						} else if (Configuration.QR_LINE4.equals("VR")) {
							line4 = curVendorRef;
						} else if (Configuration.QR_LINE4.equals("TN")) {
							line4 = Configuration.USER;
						} else {
							line4 = "";
						}
						String code = sNo;
						if (Configuration.QR_IS_INCLUDE_LINE2.equals("YES")) {
							code += "," + dt;
						}
						if (Configuration.QR_IS_INCLUDE_LINE3.equals("YES")) {
							code += "," + line3;
						}
						if (Configuration.QR_IS_INCLUDE_LINE4.equals("YES")) {
							code += "," + line4;
						}
						printQRCode.print(code, sNo, dt, line3, line4);
					}
				}
			}
		} catch(Exception e) {
			e.printStackTrace();
			changeApplicationStatus("SAVE FAILED");
			JOptionPane.showMessageDialog(this, e.getMessage());
		}
		refreshTestCount();
	}

	private void cmdDelActionPerformed() {
		int selC = tblTestEntry.getSelectedRowCount();
		int row = tblTestEntry.getSelectedRow();
		int response = 0;
		
		if (selC == 0 || tblTestEntry.getValueAt(row, 1).toString().isEmpty()) {
			JOptionPane.showMessageDialog(this, "Nothing to delete.\nPlease select a reading to be deleted");
			return;
		} else if (selC == 1 ) {
			response = JOptionPane.showConfirmDialog(this, "Are you sure that you want to delete selected reading (Test SNo:" + tblTestEntry.getValueAt(tblTestEntry.getSelectedRow(), 1).toString() + ")?\nWARNING:This will permanently delete the reading from database");
		} else {
			response = JOptionPane.showConfirmDialog(this, "Are you sure that you want to delete selected " + tblTestEntry.getSelectedRowCount() + " rows?\nWARNING:This will permanently delete the readings from database");
		} 
		
		if (response == 0) {
			// delete it
			int selRws[]= tblTestEntry.getSelectedRows();
			try {
				for (int i : selRws) {
					stmt.executeUpdate("delete from " + Configuration.READING_DETAIL+ " where test_slno='" + tblTestEntry.getValueAt(i, 1).toString().trim() + "'");
					// clear the reading
					for(int j=0; j < tblTestEntry.getColumnCount(); j++) {
						tblTestEntry.setValueAt("", i, j);
					}
				}
			} catch (SQLException se){
				logError("Error deleting reading:" + se.getMessage());
				return;
			}
			changeApplicationStatus("READING DELETED");
		}
		refreshTestCount();
	}

	private void cmdHelpActionPerformed() {
		JOptionPane.showMessageDialog(this, "This option is not available yet and coming very soon");
	}

	private void cmdStatorActionPerformed() {
		StatorType frmStat = new StatorType(this, curStatorType);
		frmStat.setVisible(true);
	}

	private void thisWindowOpened() {
		// on live reading, web cam and test monitoring threads
		startLiveReading();
		// on testing
		tglTest.setSelected(true);
		tglTestActionPerformed();
		
		if (Integer.valueOf(Configuration.NUMBER_OF_STATIONS) == 1) {
			curActiveStation = 0;
			stationList.get(curActiveStation).setIcon(idleImg);
		}
		// start blinker
		threadBlink = new Thread(textBlinker);
		threadBlink.start();
	}

	private void tglRetestActionPerformed() {
		int row = tblTestEntry.getSelectedRow();
		if (row < 0 || tblTestEntry.getValueAt(row, 1).toString().isEmpty()) {
			return;
		}
		if (tglRetest.isSelected()) {
			tglRetest.setIcon(new ImageIcon(getClass().getResource("/img/stop.png")));
			tglRetest.setBackground(Color.GREEN);
			tglRetest.setText("<html>Retest ON&nbsp;<font size=-2>[F8]</html>");
		} else {
			tglRetest.setIcon(new ImageIcon(getClass().getResource("/img/start.png")));
			tglRetest.setBackground(Color.RED);
			tglRetest.setText("<html>Retest&nbsp;&nbsp;<font size=-2>[F8]</html>");
		}
	}

	private void txtScannedSNoFocusGained() {
		txtScannedSNo.setSelectionStart(0);
	}
	
	//text blinker thread - runs always and make required text to blink
	Runnable textBlinker = new Runnable() {
		public void run() {
			try {
				while(true) {
					/* if (blinkPhase) {
						if (lblStatus.getBackground() == Color.gray) {
							lblStatus.setBackground(clrStatusFG);
							lblStatus.setForeground(Color.gray);
						} else {
							lblStatus.setBackground(Color.gray);
							lblStatus.setForeground(clrStatusFG);
						}
					} else if (!lblStatus.getBackground().equals(Color.gray)){
						lblStatus.setBackground(Color.gray);
						lblStatus.setForeground(clrStatusFG);
					} */
					try {
						blinkEmergency = devPLC.readCoil(sigEmergency, devPLCProtocol);
					} catch (Exception e) {
						continue;
					}
					if (!blinkEmergency) {
						lblEmergency.setVisible(!lblEmergency.isVisible());
					} else if (lblEmergency.isVisible()) {
						lblEmergency.setVisible(false);
					}
					Thread.sleep(500);
				}
			} catch (InterruptedException ie) {
				// ignore
			} catch (Exception e) {
				logError("Text Blinker:" + e.getMessage());
			}
		}
	};

	
	// function to refresh today's test counts
	private void refreshTestCount() {
		try {
			Integer passCount = 0;
			Integer failCount = 0;
			Integer unknownCount = 0;
			Integer totCount = 0;
			ResultSet res = stmt.executeQuery("select test_result, count(1) as tot from " + Configuration.READING_DETAIL + " where date(test_date) = date() group by 1 order by 1");
			while (res.next()) {
				totCount = res.getInt("tot");
				if (res.getString("test_result") != null) {
					if (res.getString("test_result").toLowerCase().equals("pass")) {
						passCount = totCount;
						
					} else if (res.getString("test_result").toLowerCase().equals("fail")) {
						failCount = totCount;
						
					} else {
						unknownCount += totCount;
					}
				} else {
					unknownCount += totCount;
				}
			
			}
			lblPass.setText(passCount.toString());
			lblFail.setText(failCount.toString());
			lblUnknown.setText(unknownCount.toString());
			totCount = passCount + failCount + unknownCount;
			lblTot.setText(totCount.toString());
			
		} catch (Exception e) {
			JOptionPane.showMessageDialog(this, "Error refreshing test summary:" + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
		}
	}

	private void tglRetest(ActionEvent e) {
		// TODO add your code here
	}
	
	
	private void initComponents() {
		// JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
		lblCompNm = new JLabel();
		pnlStator = new JPanel();
		cmdStator = new JButton();
		lblStator4 = new JLabel();
		lblStator = new JLabel();
		lblStator2 = new JLabel();
		lblPh = new JLabel();
		lblStator5 = new JLabel();
		lblDirection = new JLabel();
		lblStator3 = new JLabel();
		lblRat = new JLabel();
		pnlSet = new JPanel();
		cmdTest = new JButton();
		cmdDev = new JButton();
		cmdUser = new JButton();
		cmdOther = new JButton();
		pnlStOut = new JPanel();
		pnlSt = new JPanel();
		panel3 = new JPanel();
		lbllblTot = new JLabel();
		lbllblPass = new JLabel();
		lbllblFail = new JLabel();
		lbllblUknown = new JLabel();
		lblTot = new JLabel();
		lblPass = new JLabel();
		lblFail = new JLabel();
		lblUnknown = new JLabel();
		pnlTest = new JPanel();
		lblTestMode = new JLabel();
		pnlTbl = new JPanel();
		pnlTblHdr = new JPanel();
		lblCol18 = new JLabel();
		lblCol15 = new JLabel();
		lblCol12 = new JLabel();
		lblHRes = new JLabel();
		lblStCl = new JLabel();
		lblRunCl = new JLabel();
		lblComCl = new JLabel();
		lblHTemp = new JLabel();
		lblHIR1 = new JLabel();
		lblHHV = new JLabel();
		lblHIR2 = new JLabel();
		lblHHV1 = new JLabel();
		lblHHV2 = new JLabel();
		lblCol13 = new JLabel();
		lblCol10 = new JLabel();
		lblDir = new JLabel();
		lblSurge = new JLabel();
		lblCol16 = new JLabel();
		lblHSurge = new JLabel();
		scrlTest = new JScrollPane();
		tblTestEntry = new JTable();
		pnlWaveImg = new JPanel();
		lblEmergency = new JLabel();
		tglTest = new JToggleButton();
		cmdOpen = new JButton();
		cmdRep = new JButton();
		cmdSave = new JButton();
		cmdDel = new JButton();
		tglRetest = new JToggleButton();
		panel2 = new JPanel();
		label1 = new JLabel();
		txtScannedSNo = new JTextField();
		pnlLive = new JPanel();
		lbllblL1 = new JLabel();
		lbllblL2 = new JLabel();
		lblLiveRes = new JLabel();
		lblLiveTemp = new JLabel();
		lbllblL3 = new JLabel();
		lbllblL4 = new JLabel();
		lblLiveV = new JLabel();
		lblLiveCur = new JLabel();
		lbllblL6 = new JLabel();
		lblLiveIR = new JLabel();
		lbllblLSurge = new JLabel();
		pnlWC = new JPanel();
		lblResTestStat = new JLabel();
		lblTempTestStat = new JLabel();
		lblInsResTestStat = new JLabel();
		lblVTestStat = new JLabel();
		lblCurTestStat = new JLabel();
		pnlBot = new JPanel();
		lblLogo = new JLabel();
		lblStatus = new JLabel();
		txtSignal = new JTextField();
		errLogModel = new DefaultListModel();
		lstError = new JList(errLogModel);

		//======== this ========
		setFont(new Font("Candara", Font.PLAIN, 14));
		setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		setIconImage(new ImageIcon(getClass().getResource("/img/app_logo_stator.png")).getImage());
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowOpened(WindowEvent e) {
				thisWindowOpened();
			}
		});
		Container contentPane = getContentPane();
		contentPane.setLayout(new TableLayout(new double[][] {
			{TableLayout.FILL, 300},
			{40, TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.FILL, 70, 5}}));

		//---- lblCompNm ----
		lblCompNm.setText("Company Name");
		lblCompNm.setHorizontalAlignment(SwingConstants.CENTER);
		lblCompNm.setFont(new Font("Arial Rounded MT Bold", Font.PLAIN, 24));
		lblCompNm.setOpaque(true);
		lblCompNm.setBackground(Color.gray);
		lblCompNm.setForeground(Color.white);
		lblCompNm.setBorder(new MatteBorder(0, 0, 1, 0, Color.gray));
		contentPane.add(lblCompNm, new TableLayoutConstraints(0, 0, 1, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

		//======== pnlStator ========
		{
			pnlStator.setBorder(new TitledBorder(null, "Stator", TitledBorder.LEADING, TitledBorder.DEFAULT_POSITION,
				new Font("Arial", Font.PLAIN, 16), new Color(0x006699)));
			pnlStator.setLayout(new TableLayout(new double[][] {
				{150, TableLayout.PREFERRED, TableLayout.FILL, 100, 125, TableLayout.PREFERRED, 75},
				{TableLayout.PREFERRED, TableLayout.PREFERRED}}));
			((TableLayout)pnlStator.getLayout()).setHGap(5);

			//---- cmdStator ----
			cmdStator.setText("<html>Choose Stator<br><font size=-2>[F11]</html>");
			cmdStator.setIcon(new ImageIcon(getClass().getResource("/img/stator.png")));
			cmdStator.setFont(new Font("Arial", Font.PLAIN, 15));
			cmdStator.setHorizontalTextPosition(SwingConstants.RIGHT);
			cmdStator.setMargin(new Insets(2, 7, 2, 7));
			cmdStator.addActionListener(e -> cmdStatorActionPerformed());
			pnlStator.add(cmdStator, new TableLayoutConstraints(0, 0, 0, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

			//---- lblStator4 ----
			lblStator4.setText("Model");
			lblStator4.setHorizontalAlignment(SwingConstants.LEFT);
			lblStator4.setFont(new Font("Arial", Font.PLAIN, 22));
			lblStator4.setBackground(new Color(0x00cccc));
			pnlStator.add(lblStator4, new TableLayoutConstraints(1, 0, 1, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

			//---- lblStator ----
			lblStator.setText("No Stator Chosen");
			lblStator.setHorizontalAlignment(SwingConstants.CENTER);
			lblStator.setFont(new Font("Arial", Font.BOLD, 22));
			lblStator.setBackground(new Color(0x00cccc));
			lblStator.setBorder(new MatteBorder(1, 1, 2, 2, new Color(0x009999)));
			lblStator.setOpaque(true);
			lblStator.setForeground(Color.white);
			pnlStator.add(lblStator, new TableLayoutConstraints(2, 0, 2, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

			//---- lblStator2 ----
			lblStator2.setText(" Phase");
			lblStator2.setHorizontalAlignment(SwingConstants.LEFT);
			lblStator2.setFont(new Font("Arial", Font.PLAIN, 22));
			lblStator2.setBackground(new Color(0x00cccc));
			pnlStator.add(lblStator2, new TableLayoutConstraints(3, 0, 3, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

			//---- lblPh ----
			lblPh.setHorizontalAlignment(SwingConstants.CENTER);
			lblPh.setFont(new Font("Arial", Font.BOLD, 22));
			lblPh.setBackground(new Color(0x00cccc));
			lblPh.setBorder(new MatteBorder(1, 1, 2, 2, new Color(0x009999)));
			lblPh.setOpaque(true);
			lblPh.setForeground(Color.white);
			pnlStator.add(lblPh, new TableLayoutConstraints(4, 0, 4, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

			//---- lblStator5 ----
			lblStator5.setText("Dir.");
			lblStator5.setHorizontalAlignment(SwingConstants.LEFT);
			lblStator5.setFont(new Font("Arial", Font.PLAIN, 22));
			lblStator5.setBackground(new Color(0x00cccc));
			pnlStator.add(lblStator5, new TableLayoutConstraints(5, 0, 5, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

			//---- lblDirection ----
			lblDirection.setHorizontalAlignment(SwingConstants.CENTER);
			lblDirection.setFont(new Font("Arial", Font.BOLD, 22));
			lblDirection.setBackground(new Color(0x00cccc));
			lblDirection.setBorder(new MatteBorder(1, 1, 2, 2, new Color(0x009999)));
			lblDirection.setOpaque(true);
			lblDirection.setForeground(Color.white);
			pnlStator.add(lblDirection, new TableLayoutConstraints(6, 0, 6, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

			//---- lblStator3 ----
			lblStator3.setText(" KW / HP");
			lblStator3.setHorizontalAlignment(SwingConstants.LEFT);
			lblStator3.setFont(new Font("Arial", Font.PLAIN, 22));
			lblStator3.setBackground(new Color(0x00cccc));
			pnlStator.add(lblStator3, new TableLayoutConstraints(3, 1, 3, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

			//---- lblRat ----
			lblRat.setHorizontalAlignment(SwingConstants.CENTER);
			lblRat.setFont(new Font("Arial", Font.BOLD, 22));
			lblRat.setBackground(new Color(0x00cccc));
			lblRat.setBorder(new MatteBorder(1, 1, 2, 2, new Color(0x009999)));
			lblRat.setOpaque(true);
			lblRat.setForeground(Color.white);
			pnlStator.add(lblRat, new TableLayoutConstraints(4, 1, 4, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
		}
		contentPane.add(pnlStator, new TableLayoutConstraints(0, 1, 0, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

		//======== pnlSet ========
		{
			pnlSet.setBorder(new TitledBorder(null, "Settings", TitledBorder.LEADING, TitledBorder.DEFAULT_POSITION,
				new Font("Arial", Font.PLAIN, 16), new Color(0x006699)));
			pnlSet.setLayout(new TableLayout(new double[][] {
				{TableLayout.FILL, TableLayout.FILL},
				{TableLayout.FILL, TableLayout.FILL}}));
			((TableLayout)pnlSet.getLayout()).setHGap(1);

			//---- cmdTest ----
			cmdTest.setText("Test");
			cmdTest.setIcon(null);
			cmdTest.setFont(new Font("Arial", Font.PLAIN, 15));
			cmdTest.setHorizontalTextPosition(SwingConstants.CENTER);
			cmdTest.setVerticalTextPosition(SwingConstants.BOTTOM);
			cmdTest.setIconTextGap(2);
			cmdTest.addActionListener(e -> cmdTestActionPerformed());
			pnlSet.add(cmdTest, new TableLayoutConstraints(0, 0, 0, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

			//---- cmdDev ----
			cmdDev.setText("Device");
			cmdDev.setIcon(null);
			cmdDev.setFont(new Font("Arial", Font.PLAIN, 15));
			cmdDev.setHorizontalTextPosition(SwingConstants.CENTER);
			cmdDev.setVerticalTextPosition(SwingConstants.BOTTOM);
			cmdDev.setIconTextGap(2);
			cmdDev.addActionListener(e -> cmdDevActionPerformed());
			pnlSet.add(cmdDev, new TableLayoutConstraints(1, 0, 1, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

			//---- cmdUser ----
			cmdUser.setText("User");
			cmdUser.setIcon(null);
			cmdUser.setFont(new Font("Arial", Font.PLAIN, 15));
			cmdUser.setHorizontalTextPosition(SwingConstants.CENTER);
			cmdUser.setVerticalTextPosition(SwingConstants.BOTTOM);
			cmdUser.setIconTextGap(2);
			cmdUser.addActionListener(e -> cmdUserActionPerformed());
			pnlSet.add(cmdUser, new TableLayoutConstraints(0, 1, 0, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

			//---- cmdOther ----
			cmdOther.setText("Others");
			cmdOther.setIcon(null);
			cmdOther.setFont(new Font("Arial", Font.PLAIN, 15));
			cmdOther.setHorizontalTextPosition(SwingConstants.CENTER);
			cmdOther.setVerticalTextPosition(SwingConstants.BOTTOM);
			cmdOther.setIconTextGap(2);
			cmdOther.addActionListener(e -> cmdOtherActionPerformed());
			pnlSet.add(cmdOther, new TableLayoutConstraints(1, 1, 1, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
		}
		contentPane.add(pnlSet, new TableLayoutConstraints(1, 1, 1, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

		//======== pnlStOut ========
		{
			pnlStOut.setBorder(new TitledBorder(null, "Station", TitledBorder.LEADING, TitledBorder.DEFAULT_POSITION,
				new Font("Arial", Font.PLAIN, 16), new Color(0x006699)));
			pnlStOut.setLayout(new TableLayout(new double[][] {
				{150, TableLayout.FILL, TableLayout.PREFERRED, TableLayout.FILL},
				{TableLayout.PREFERRED}}));
			((TableLayout)pnlStOut.getLayout()).setHGap(1);
			((TableLayout)pnlStOut.getLayout()).setVGap(1);

			//======== pnlSt ========
			{
				pnlSt.setLayout(new TableLayout(new double[][] {
					{TableLayout.PREFERRED},
					{TableLayout.PREFERRED}}));
				((TableLayout)pnlSt.getLayout()).setHGap(35);
				((TableLayout)pnlSt.getLayout()).setVGap(1);
			}
			pnlStOut.add(pnlSt, new TableLayoutConstraints(2, 0, 2, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
		}
		contentPane.add(pnlStOut, new TableLayoutConstraints(0, 2, 0, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

		//======== panel3 ========
		{
			panel3.setBorder(new TitledBorder(null, "Today's Test Summary", TitledBorder.CENTER, TitledBorder.TOP,
				new Font("Arial", Font.BOLD, 14), Color.darkGray));
			panel3.setBackground(Color.white);
			panel3.setOpaque(false);
			panel3.setLayout(new TableLayout(new double[][] {
				{TableLayout.FILL, TableLayout.FILL, TableLayout.FILL, TableLayout.FILL},
				{TableLayout.PREFERRED, 30}}));
			((TableLayout)panel3.getLayout()).setHGap(3);
			((TableLayout)panel3.getLayout()).setVGap(3);

			//---- lbllblTot ----
			lbllblTot.setText("Total");
			lbllblTot.setFont(new Font("Arial", Font.BOLD, 24));
			lbllblTot.setHorizontalAlignment(SwingConstants.CENTER);
			lbllblTot.setForeground(new Color(0x000099));
			lbllblTot.setBackground(new Color(0xccffff));
			lbllblTot.setOpaque(true);
			lbllblTot.setToolTipText("Total tests carried on today in this line");
			panel3.add(lbllblTot, new TableLayoutConstraints(0, 0, 0, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

			//---- lbllblPass ----
			lbllblPass.setText("Pass");
			lbllblPass.setFont(new Font("Arial", Font.BOLD, 24));
			lbllblPass.setHorizontalAlignment(SwingConstants.CENTER);
			lbllblPass.setForeground(new Color(0x009933));
			lbllblPass.setBackground(new Color(0xccffcc));
			lbllblPass.setOpaque(true);
			lbllblPass.setToolTipText("Passed tests today in this line");
			panel3.add(lbllblPass, new TableLayoutConstraints(1, 0, 1, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

			//---- lbllblFail ----
			lbllblFail.setText("Fail");
			lbllblFail.setFont(new Font("Arial", Font.BOLD, 24));
			lbllblFail.setHorizontalAlignment(SwingConstants.CENTER);
			lbllblFail.setForeground(new Color(0xcc0000));
			lbllblFail.setBackground(new Color(0xffcccc));
			lbllblFail.setOpaque(true);
			lbllblFail.setToolTipText("Failed tests today in this line");
			panel3.add(lbllblFail, new TableLayoutConstraints(2, 0, 2, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

			//---- lbllblUknown ----
			lbllblUknown.setText("?");
			lbllblUknown.setFont(new Font("Arial", Font.BOLD, 24));
			lbllblUknown.setHorizontalAlignment(SwingConstants.CENTER);
			lbllblUknown.setForeground(Color.darkGray);
			lbllblUknown.setBackground(Color.lightGray);
			lbllblUknown.setOpaque(true);
			lbllblUknown.setToolTipText("Tests with unknown results (neither PASS nor FAIL) today in this line");
			panel3.add(lbllblUknown, new TableLayoutConstraints(3, 0, 3, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

			//---- lblTot ----
			lblTot.setText("0");
			lblTot.setFont(new Font("Arial", Font.BOLD, 30));
			lblTot.setHorizontalAlignment(SwingConstants.CENTER);
			lblTot.setForeground(new Color(0x000099));
			lblTot.setBackground(new Color(0xccffff));
			lblTot.setOpaque(true);
			lblTot.setToolTipText("Total tests carried on today in this line");
			panel3.add(lblTot, new TableLayoutConstraints(0, 1, 0, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

			//---- lblPass ----
			lblPass.setText("0");
			lblPass.setFont(new Font("Arial", Font.BOLD, 30));
			lblPass.setHorizontalAlignment(SwingConstants.CENTER);
			lblPass.setForeground(new Color(0x009933));
			lblPass.setBackground(new Color(0xccffcc));
			lblPass.setOpaque(true);
			lblPass.setToolTipText("Passed tests today in this line");
			panel3.add(lblPass, new TableLayoutConstraints(1, 1, 1, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

			//---- lblFail ----
			lblFail.setText("0");
			lblFail.setFont(new Font("Arial", Font.BOLD, 30));
			lblFail.setHorizontalAlignment(SwingConstants.CENTER);
			lblFail.setForeground(new Color(0xcc0000));
			lblFail.setBackground(new Color(0xffcccc));
			lblFail.setOpaque(true);
			lblFail.setToolTipText("Failed tests today in this line");
			panel3.add(lblFail, new TableLayoutConstraints(2, 1, 2, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

			//---- lblUnknown ----
			lblUnknown.setText("0");
			lblUnknown.setFont(new Font("Arial", Font.BOLD, 30));
			lblUnknown.setHorizontalAlignment(SwingConstants.CENTER);
			lblUnknown.setForeground(Color.darkGray);
			lblUnknown.setBackground(Color.lightGray);
			lblUnknown.setOpaque(true);
			lblUnknown.setToolTipText("Tests with unknown results (neither PASS nor FAIL) today in this line");
			panel3.add(lblUnknown, new TableLayoutConstraints(3, 1, 3, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
		}
		contentPane.add(panel3, new TableLayoutConstraints(1, 2, 1, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

		//======== pnlTest ========
		{
			pnlTest.setBorder(new TitledBorder(null, "Test Control", TitledBorder.LEADING, TitledBorder.DEFAULT_POSITION,
				new Font("Arial", Font.PLAIN, 16), new Color(0x006699)));
			pnlTest.setLayout(new TableLayout(new double[][] {
				{150, TableLayout.FILL, 162},
				{47, 22, TableLayout.FILL, TableLayout.FILL, TableLayout.FILL, TableLayout.FILL, TableLayout.FILL, TableLayout.FILL, TableLayout.FILL, 1, TableLayout.PREFERRED}}));
			((TableLayout)pnlTest.getLayout()).setHGap(5);
			((TableLayout)pnlTest.getLayout()).setVGap(2);

			//---- lblTestMode ----
			lblTestMode.setText("AUTO");
			lblTestMode.setFont(new Font("Arial", Font.PLAIN, 34));
			lblTestMode.setHorizontalAlignment(SwingConstants.CENTER);
			pnlTest.add(lblTestMode, new TableLayoutConstraints(0, 0, 0, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

			//======== pnlTbl ========
			{
				pnlTbl.setBorder(LineBorder.createGrayLineBorder());
				pnlTbl.setBackground(Color.darkGray);
				pnlTbl.setLayout(new TableLayout(new double[][] {
					{TableLayout.FILL, 16, 162},
					{TableLayout.PREFERRED, TableLayout.FILL}}));
				((TableLayout)pnlTbl.getLayout()).setHGap(1);
				((TableLayout)pnlTbl.getLayout()).setVGap(1);

				//======== pnlTblHdr ========
				{
					pnlTblHdr.setBackground(Color.darkGray);
					pnlTblHdr.setLayout(new TableLayout(new double[][] {
						{35, 70, 77, 125, TableLayout.FILL, TableLayout.FILL, TableLayout.FILL, TableLayout.FILL, TableLayout.FILL, TableLayout.FILL, TableLayout.FILL, TableLayout.FILL, TableLayout.FILL, TableLayout.FILL, TableLayout.FILL, 100, 16},
						{TableLayout.PREFERRED, TableLayout.PREFERRED}}));
					((TableLayout)pnlTblHdr.getLayout()).setHGap(1);
					((TableLayout)pnlTblHdr.getLayout()).setVGap(1);

					//---- lblCol18 ----
					lblCol18.setText("St");
					lblCol18.setHorizontalAlignment(SwingConstants.CENTER);
					lblCol18.setFont(new Font("Arial", Font.PLAIN, 16));
					lblCol18.setOpaque(true);
					lblCol18.setBackground(Color.gray);
					lblCol18.setForeground(Color.white);
					lblCol18.setBorder(null);
					lblCol18.setHorizontalTextPosition(SwingConstants.CENTER);
					pnlTblHdr.add(lblCol18, new TableLayoutConstraints(0, 0, 0, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

					//---- lblCol15 ----
					lblCol15.setText("Date");
					lblCol15.setHorizontalAlignment(SwingConstants.CENTER);
					lblCol15.setFont(new Font("Arial", Font.PLAIN, 16));
					lblCol15.setOpaque(true);
					lblCol15.setBackground(Color.gray);
					lblCol15.setForeground(Color.white);
					lblCol15.setBorder(null);
					lblCol15.setHorizontalTextPosition(SwingConstants.CENTER);
					pnlTblHdr.add(lblCol15, new TableLayoutConstraints(2, 0, 2, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

					//---- lblCol12 ----
					lblCol12.setText("Stator SNo.");
					lblCol12.setHorizontalAlignment(SwingConstants.CENTER);
					lblCol12.setFont(new Font("Arial", Font.PLAIN, 16));
					lblCol12.setOpaque(true);
					lblCol12.setBackground(Color.gray);
					lblCol12.setBorder(null);
					lblCol12.setHorizontalTextPosition(SwingConstants.CENTER);
					lblCol12.setForeground(Color.white);
					pnlTblHdr.add(lblCol12, new TableLayoutConstraints(3, 0, 3, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

					//---- lblHRes ----
					lblHRes.setText("<html><body  align=\"center\">Resistance</html>");
					lblHRes.setHorizontalAlignment(SwingConstants.CENTER);
					lblHRes.setFont(new Font("Arial", Font.PLAIN, 16));
					lblHRes.setOpaque(true);
					lblHRes.setBackground(Color.gray);
					lblHRes.setBorder(null);
					lblHRes.setHorizontalTextPosition(SwingConstants.CENTER);
					lblHRes.setForeground(Color.white);
					pnlTblHdr.add(lblHRes, new TableLayoutConstraints(4, 0, 6, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

					//---- lblStCl ----
					lblStCl.setText("<html><body  align=\"center\">Start <br><font size=-2>(\u03a9)</html>");
					lblStCl.setHorizontalAlignment(SwingConstants.CENTER);
					lblStCl.setFont(new Font("Arial", Font.PLAIN, 16));
					lblStCl.setOpaque(true);
					lblStCl.setBackground(Color.gray);
					lblStCl.setBorder(null);
					lblStCl.setHorizontalTextPosition(SwingConstants.CENTER);
					lblStCl.setForeground(Color.white);
					pnlTblHdr.add(lblStCl, new TableLayoutConstraints(4, 1, 4, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

					//---- lblRunCl ----
					lblRunCl.setText("<html><body  align=\"center\">Run<br><font size=-2>(\u03a9)</html>");
					lblRunCl.setHorizontalAlignment(SwingConstants.CENTER);
					lblRunCl.setFont(new Font("Arial", Font.PLAIN, 16));
					lblRunCl.setOpaque(true);
					lblRunCl.setBackground(Color.gray);
					lblRunCl.setBorder(null);
					lblRunCl.setHorizontalTextPosition(SwingConstants.CENTER);
					lblRunCl.setForeground(Color.white);
					pnlTblHdr.add(lblRunCl, new TableLayoutConstraints(5, 1, 5, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

					//---- lblComCl ----
					lblComCl.setHorizontalAlignment(SwingConstants.CENTER);
					lblComCl.setFont(new Font("Arial", Font.PLAIN, 16));
					lblComCl.setOpaque(true);
					lblComCl.setBackground(Color.gray);
					lblComCl.setBorder(null);
					lblComCl.setHorizontalTextPosition(SwingConstants.CENTER);
					lblComCl.setText("<html><body  align=\"center\">Com<br><font size=-2>(\u03a9)</html>");
					lblComCl.setForeground(Color.white);
					pnlTblHdr.add(lblComCl, new TableLayoutConstraints(6, 1, 6, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

					//---- lblHTemp ----
					lblHTemp.setText("<html><body  align=\"center\">T<br><font size=-2>(\u00b0C)</html>");
					lblHTemp.setHorizontalAlignment(SwingConstants.CENTER);
					lblHTemp.setFont(new Font("Arial", Font.PLAIN, 16));
					lblHTemp.setOpaque(true);
					lblHTemp.setBackground(Color.gray);
					lblHTemp.setBorder(null);
					lblHTemp.setHorizontalTextPosition(SwingConstants.CENTER);
					lblHTemp.setForeground(Color.white);
					pnlTblHdr.add(lblHTemp, new TableLayoutConstraints(7, 0, 7, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

					//---- lblHIR1 ----
					lblHIR1.setText("<html><body  align=\"center\">IR<br>Bef. HV<font size=-2>(M\u03a9)</html>");
					lblHIR1.setHorizontalAlignment(SwingConstants.CENTER);
					lblHIR1.setFont(new Font("Arial", Font.PLAIN, 16));
					lblHIR1.setOpaque(true);
					lblHIR1.setBackground(Color.gray);
					lblHIR1.setBorder(null);
					lblHIR1.setHorizontalTextPosition(SwingConstants.CENTER);
					lblHIR1.setForeground(Color.white);
					pnlTblHdr.add(lblHIR1, new TableLayoutConstraints(8, 0, 8, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

					//---- lblHHV ----
					lblHHV.setText("<html><body  align=\"center\">High Voltage</html>");
					lblHHV.setHorizontalAlignment(SwingConstants.CENTER);
					lblHHV.setFont(new Font("Arial", Font.PLAIN, 16));
					lblHHV.setOpaque(true);
					lblHHV.setBackground(Color.gray);
					lblHHV.setBorder(null);
					lblHHV.setHorizontalTextPosition(SwingConstants.CENTER);
					lblHHV.setForeground(Color.white);
					pnlTblHdr.add(lblHHV, new TableLayoutConstraints(9, 0, 10, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

					//---- lblHIR2 ----
					lblHIR2.setText("<html><body  align=\"center\">IR<br>Aft. HV<font size=-2>(M\u03a9)</html>");
					lblHIR2.setHorizontalAlignment(SwingConstants.CENTER);
					lblHIR2.setFont(new Font("Arial", Font.PLAIN, 16));
					lblHIR2.setOpaque(true);
					lblHIR2.setBackground(Color.gray);
					lblHIR2.setBorder(null);
					lblHIR2.setHorizontalTextPosition(SwingConstants.CENTER);
					lblHIR2.setForeground(Color.white);
					pnlTblHdr.add(lblHIR2, new TableLayoutConstraints(11, 0, 11, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

					//---- lblHHV1 ----
					lblHHV1.setText("<html><body  align=\"center\">kV</html>");
					lblHHV1.setHorizontalAlignment(SwingConstants.CENTER);
					lblHHV1.setFont(new Font("Arial", Font.PLAIN, 16));
					lblHHV1.setOpaque(true);
					lblHHV1.setBackground(Color.gray);
					lblHHV1.setBorder(null);
					lblHHV1.setHorizontalTextPosition(SwingConstants.CENTER);
					lblHHV1.setForeground(Color.white);
					pnlTblHdr.add(lblHHV1, new TableLayoutConstraints(9, 1, 9, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

					//---- lblHHV2 ----
					lblHHV2.setText("<html><body  align=\"center\">mA</html>");
					lblHHV2.setHorizontalAlignment(SwingConstants.CENTER);
					lblHHV2.setFont(new Font("Arial", Font.PLAIN, 16));
					lblHHV2.setOpaque(true);
					lblHHV2.setBackground(Color.gray);
					lblHHV2.setBorder(null);
					lblHHV2.setHorizontalTextPosition(SwingConstants.CENTER);
					lblHHV2.setForeground(Color.white);
					pnlTblHdr.add(lblHHV2, new TableLayoutConstraints(10, 1, 10, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

					//---- lblCol13 ----
					lblCol13.setText("Result");
					lblCol13.setHorizontalAlignment(SwingConstants.CENTER);
					lblCol13.setFont(new Font("Arial", Font.PLAIN, 16));
					lblCol13.setOpaque(true);
					lblCol13.setBackground(Color.gray);
					lblCol13.setBorder(null);
					lblCol13.setHorizontalTextPosition(SwingConstants.CENTER);
					lblCol13.setForeground(Color.white);
					pnlTblHdr.add(lblCol13, new TableLayoutConstraints(14, 0, 14, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

					//---- lblCol10 ----
					lblCol10.setText("Remark");
					lblCol10.setHorizontalAlignment(SwingConstants.CENTER);
					lblCol10.setFont(new Font("Arial", Font.PLAIN, 16));
					lblCol10.setOpaque(true);
					lblCol10.setBackground(Color.gray);
					lblCol10.setBorder(null);
					lblCol10.setHorizontalTextPosition(SwingConstants.CENTER);
					lblCol10.setForeground(Color.white);
					pnlTblHdr.add(lblCol10, new TableLayoutConstraints(15, 0, 16, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

					//---- lblDir ----
					lblDir.setText("<html><body  align=\"center\">Dir</html>");
					lblDir.setHorizontalAlignment(SwingConstants.CENTER);
					lblDir.setFont(new Font("Arial", Font.PLAIN, 16));
					lblDir.setOpaque(true);
					lblDir.setBackground(Color.gray);
					lblDir.setBorder(null);
					lblDir.setHorizontalTextPosition(SwingConstants.CENTER);
					lblDir.setForeground(Color.white);
					pnlTblHdr.add(lblDir, new TableLayoutConstraints(13, 0, 13, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

					//---- lblSurge ----
					lblSurge.setText("<html><body  align=\"center\">Surge</surge>");
					lblSurge.setHorizontalAlignment(SwingConstants.CENTER);
					lblSurge.setFont(new Font("Arial", Font.PLAIN, 16));
					lblSurge.setOpaque(true);
					lblSurge.setBackground(Color.gray);
					lblSurge.setBorder(null);
					lblSurge.setHorizontalTextPosition(SwingConstants.CENTER);
					lblSurge.setForeground(Color.white);
					pnlTblHdr.add(lblSurge, new TableLayoutConstraints(12, 0, 12, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

					//---- lblCol16 ----
					lblCol16.setText("Test SNo.");
					lblCol16.setHorizontalAlignment(SwingConstants.CENTER);
					lblCol16.setFont(new Font("Arial", Font.PLAIN, 16));
					lblCol16.setOpaque(true);
					lblCol16.setBackground(Color.gray);
					lblCol16.setForeground(Color.white);
					lblCol16.setBorder(null);
					lblCol16.setHorizontalTextPosition(SwingConstants.CENTER);
					pnlTblHdr.add(lblCol16, new TableLayoutConstraints(1, 0, 1, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
				}
				pnlTbl.add(pnlTblHdr, new TableLayoutConstraints(0, 0, 1, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- lblHSurge ----
				lblHSurge.setText("Surge Wave");
				lblHSurge.setHorizontalAlignment(SwingConstants.CENTER);
				lblHSurge.setFont(new Font("Arial", Font.PLAIN, 16));
				lblHSurge.setOpaque(true);
				lblHSurge.setBackground(Color.gray);
				lblHSurge.setBorder(null);
				lblHSurge.setHorizontalTextPosition(SwingConstants.CENTER);
				lblHSurge.setForeground(Color.white);
				pnlTbl.add(lblHSurge, new TableLayoutConstraints(2, 0, 2, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//======== scrlTest ========
				{
					scrlTest.setBorder(BorderFactory.createEmptyBorder());
					scrlTest.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

					//---- tblTestEntry ----
					tblTestEntry.setModel(new DefaultTableModel(
						new Object[][] {
							{null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
							{null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
							{null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
							{null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
							{null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
							{null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
							{null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
							{null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
							{null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
							{null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
							{null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
							{null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
							{null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
							{null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
							{null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
						},
						new String[] {
							null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null
						}
					) {
						Class<?>[] columnTypes = new Class<?>[] {
							String.class, String.class, String.class, String.class, String.class, String.class, String.class, String.class, String.class, String.class, String.class, String.class, String.class, String.class, String.class, String.class
						};
						@Override
						public Class<?> getColumnClass(int columnIndex) {
							return columnTypes[columnIndex];
						}
					});
					{
						TableColumnModel cm = tblTestEntry.getColumnModel();
						cm.getColumn(0).setMinWidth(36);
						cm.getColumn(0).setPreferredWidth(36);
						cm.getColumn(1).setMinWidth(71);
						cm.getColumn(1).setPreferredWidth(71);
						cm.getColumn(2).setMinWidth(78);
						cm.getColumn(2).setPreferredWidth(78);
						cm.getColumn(3).setMinWidth(126);
						cm.getColumn(3).setPreferredWidth(126);
						cm.getColumn(15).setMinWidth(100);
						cm.getColumn(15).setPreferredWidth(100);
					}
					tblTestEntry.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
					tblTestEntry.setRowHeight(25);
					tblTestEntry.setBorder(null);
					tblTestEntry.setGridColor(Color.lightGray);
					scrlTest.setViewportView(tblTestEntry);
				}
				pnlTbl.add(scrlTest, new TableLayoutConstraints(0, 1, 1, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//======== pnlWaveImg ========
				{
					pnlWaveImg.setBorder(null);
					pnlWaveImg.setBackground(Color.white);
					pnlWaveImg.setLayout(new TableLayout(new double[][] {
						{TableLayout.FILL},
						{TableLayout.FILL, TableLayout.FILL, TableLayout.FILL, TableLayout.FILL}}));
				}
				pnlTbl.add(pnlWaveImg, new TableLayoutConstraints(2, 1, 2, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
			}
			pnlTest.add(pnlTbl, new TableLayoutConstraints(1, 0, 2, 8, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

			//---- lblEmergency ----
			lblEmergency.setText("EMERGENCY ON");
			lblEmergency.setFont(new Font("Arial", Font.BOLD, 16));
			lblEmergency.setHorizontalAlignment(SwingConstants.CENTER);
			lblEmergency.setBackground(Color.red);
			lblEmergency.setOpaque(true);
			lblEmergency.setForeground(Color.yellow);
			pnlTest.add(lblEmergency, new TableLayoutConstraints(0, 1, 0, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

			//---- tglTest ----
			tglTest.setText("<html>Start Test&nbsp;<font size=-2>[F7]</html>");
			tglTest.setFont(new Font("Arial", Font.PLAIN, 15));
			tglTest.setIcon(new ImageIcon(getClass().getResource("/img/start.png")));
			tglTest.setMargin(new Insets(2, 14, 2, 10));
			tglTest.setOpaque(true);
			tglTest.setHorizontalAlignment(SwingConstants.LEFT);
			tglTest.setToolTipText("Switch on auto capture");
			tglTest.addActionListener(e -> tglTestActionPerformed());
			pnlTest.add(tglTest, new TableLayoutConstraints(0, 2, 0, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

			//---- cmdOpen ----
			cmdOpen.setText("<html>Open&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<font size=-2>[F3]</html>");
			cmdOpen.setIcon(new ImageIcon(getClass().getResource("/img/open.png")));
			cmdOpen.setFont(new Font("Arial", Font.PLAIN, 15));
			cmdOpen.setHorizontalTextPosition(SwingConstants.RIGHT);
			cmdOpen.setHorizontalAlignment(SwingConstants.LEFT);
			cmdOpen.setMargin(new Insets(2, 10, 2, 10));
			cmdOpen.setToolTipText("To open existing tests");
			cmdOpen.addActionListener(e -> cmdOpenActionPerformed());
			pnlTest.add(cmdOpen, new TableLayoutConstraints(0, 5, 0, 5, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

			//---- cmdRep ----
			cmdRep.setText("<html>Reports&nbsp;<font size=-2>[F4]</html>");
			cmdRep.setIcon(new ImageIcon(getClass().getResource("/img/print.png")));
			cmdRep.setFont(new Font("Arial", Font.PLAIN, 15));
			cmdRep.setHorizontalTextPosition(SwingConstants.RIGHT);
			cmdRep.setHorizontalAlignment(SwingConstants.LEFT);
			cmdRep.setMargin(new Insets(2, 10, 2, 10));
			cmdRep.setToolTipText("To view and print test reports");
			cmdRep.addActionListener(e -> cmdRepActionPerformed());
			pnlTest.add(cmdRep, new TableLayoutConstraints(0, 6, 0, 6, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

			//---- cmdSave ----
			cmdSave.setText("<html>Save&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<font size=-2>[F5]</html>");
			cmdSave.setIcon(new ImageIcon(getClass().getResource("/img/save.png")));
			cmdSave.setFont(new Font("Arial", Font.PLAIN, 15));
			cmdSave.setHorizontalTextPosition(SwingConstants.RIGHT);
			cmdSave.setHorizontalAlignment(SwingConstants.LEFT);
			cmdSave.setToolTipText("To save any changes manually made in current chosen reading");
			cmdSave.setMargin(new Insets(2, 10, 2, 10));
			cmdSave.addActionListener(e -> cmdSaveActionPerformed());
			pnlTest.add(cmdSave, new TableLayoutConstraints(0, 7, 0, 7, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

			//---- cmdDel ----
			cmdDel.setText("<html>Delete&nbsp;&nbsp;&nbsp;<font size=-2>[F6]</html>");
			cmdDel.setIcon(new ImageIcon(getClass().getResource("/img/delete.png")));
			cmdDel.setFont(new Font("Arial", Font.PLAIN, 15));
			cmdDel.setHorizontalTextPosition(SwingConstants.RIGHT);
			cmdDel.setHorizontalAlignment(SwingConstants.LEFT);
			cmdDel.setToolTipText("To delete currently chosen reading");
			cmdDel.setMargin(new Insets(2, 10, 2, 10));
			cmdDel.addActionListener(e -> cmdDelActionPerformed());
			pnlTest.add(cmdDel, new TableLayoutConstraints(0, 8, 0, 8, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

			//---- tglRetest ----
			tglRetest.setText("<html>Retest&nbsp;&nbsp;&nbsp;<font size=-2>[F8]</html>");
			tglRetest.setFont(new Font("Arial", Font.PLAIN, 15));
			tglRetest.setIcon(new ImageIcon(getClass().getResource("/img/start.png")));
			tglRetest.setMargin(new Insets(2, 10, 2, 10));
			tglRetest.setOpaque(true);
			tglRetest.setHorizontalAlignment(SwingConstants.LEFT);
			tglRetest.setToolTipText("Switch on to retest a previously tested stator");
			tglRetest.addActionListener(e -> {
			tglRetestActionPerformed();
			tglRetest(e);
		});
			pnlTest.add(tglRetest, new TableLayoutConstraints(0, 4, 0, 4, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

			//======== panel2 ========
			{
				panel2.setLayout(new TableLayout(new double[][] {
					{162, TableLayout.FILL},
					{TableLayout.PREFERRED}}));
				((TableLayout)panel2.getLayout()).setHGap(5);
				((TableLayout)panel2.getLayout()).setVGap(5);

				//---- label1 ----
				label1.setText("Scan Barcode");
				label1.setFont(new Font("Arial", Font.PLAIN, 15));
				label1.setHorizontalAlignment(SwingConstants.RIGHT);
				panel2.add(label1, new TableLayoutConstraints(0, 0, 0, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- txtScannedSNo ----
				txtScannedSNo.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));
				txtScannedSNo.setForeground(Color.blue);
				txtScannedSNo.setHorizontalAlignment(SwingConstants.CENTER);
				txtScannedSNo.addFocusListener(new FocusAdapter() {
					@Override
					public void focusGained(FocusEvent e) {
						txtScannedSNoFocusGained();
					}
				});
				panel2.add(txtScannedSNo, new TableLayoutConstraints(1, 0, 1, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
			}
			pnlTest.add(panel2, new TableLayoutConstraints(1, 10, 1, 10, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
		}
		contentPane.add(pnlTest, new TableLayoutConstraints(0, 3, 0, 3, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

		//======== pnlLive ========
		{
			pnlLive.setBorder(new TitledBorder(null, "Live Reading", TitledBorder.LEADING, TitledBorder.DEFAULT_POSITION,
				new Font("Arial", Font.PLAIN, 16), new Color(0x006699)));
			pnlLive.setLayout(new TableLayout(new double[][] {
				{TableLayout.FILL, TableLayout.FILL},
				{30, TableLayout.FILL, 30, TableLayout.FILL, 30, TableLayout.FILL, 30, 215, 5}}));

			//---- lbllblL1 ----
			lbllblL1.setText("<html>Resistance<font size=-1> (\u03a9)</html>");
			lbllblL1.setFont(new Font("Arial", Font.PLAIN, 20));
			lbllblL1.setHorizontalAlignment(SwingConstants.CENTER);
			lbllblL1.setBackground(Color.lightGray);
			lbllblL1.setOpaque(true);
			lbllblL1.setBorder(LineBorder.createGrayLineBorder());
			pnlLive.add(lbllblL1, new TableLayoutConstraints(0, 0, 0, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

			//---- lbllblL2 ----
			lbllblL2.setText("<html>Temp.<font size=-1> (\u00b0C)</html>");
			lbllblL2.setFont(new Font("Arial", Font.PLAIN, 20));
			lbllblL2.setHorizontalAlignment(SwingConstants.CENTER);
			lbllblL2.setBackground(Color.lightGray);
			lbllblL2.setOpaque(true);
			lbllblL2.setBorder(LineBorder.createGrayLineBorder());
			pnlLive.add(lbllblL2, new TableLayoutConstraints(1, 0, 1, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

			//---- lblLiveRes ----
			lblLiveRes.setText("-");
			lblLiveRes.setFont(new Font("Consolas", Font.PLAIN, 42));
			lblLiveRes.setHorizontalAlignment(SwingConstants.CENTER);
			lblLiveRes.setBackground(Color.darkGray);
			lblLiveRes.setOpaque(true);
			lblLiveRes.setForeground(Color.orange);
			lblLiveRes.setBorder(LineBorder.createGrayLineBorder());
			pnlLive.add(lblLiveRes, new TableLayoutConstraints(0, 1, 0, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

			//---- lblLiveTemp ----
			lblLiveTemp.setText("-");
			lblLiveTemp.setFont(new Font("Consolas", Font.PLAIN, 46));
			lblLiveTemp.setHorizontalAlignment(SwingConstants.CENTER);
			lblLiveTemp.setBackground(Color.darkGray);
			lblLiveTemp.setOpaque(true);
			lblLiveTemp.setForeground(Color.orange);
			lblLiveTemp.setBorder(LineBorder.createGrayLineBorder());
			pnlLive.add(lblLiveTemp, new TableLayoutConstraints(1, 1, 1, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

			//---- lbllblL3 ----
			lbllblL3.setText("<html>Voltage<font size=-1> (kV)</html>");
			lbllblL3.setFont(new Font("Arial", Font.PLAIN, 20));
			lbllblL3.setHorizontalAlignment(SwingConstants.CENTER);
			lbllblL3.setBackground(Color.lightGray);
			lbllblL3.setOpaque(true);
			lbllblL3.setBorder(LineBorder.createGrayLineBorder());
			pnlLive.add(lbllblL3, new TableLayoutConstraints(0, 2, 0, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

			//---- lbllblL4 ----
			lbllblL4.setText("<html>Current<font size=-1> (mA)</html>");
			lbllblL4.setFont(new Font("Arial", Font.PLAIN, 20));
			lbllblL4.setHorizontalAlignment(SwingConstants.CENTER);
			lbllblL4.setBackground(Color.lightGray);
			lbllblL4.setOpaque(true);
			lbllblL4.setBorder(LineBorder.createGrayLineBorder());
			pnlLive.add(lbllblL4, new TableLayoutConstraints(1, 2, 1, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

			//---- lblLiveV ----
			lblLiveV.setText("-");
			lblLiveV.setFont(new Font("Consolas", Font.PLAIN, 46));
			lblLiveV.setHorizontalAlignment(SwingConstants.CENTER);
			lblLiveV.setBackground(Color.darkGray);
			lblLiveV.setOpaque(true);
			lblLiveV.setForeground(Color.orange);
			lblLiveV.setBorder(LineBorder.createGrayLineBorder());
			pnlLive.add(lblLiveV, new TableLayoutConstraints(0, 3, 0, 3, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

			//---- lblLiveCur ----
			lblLiveCur.setText("-");
			lblLiveCur.setFont(new Font("Consolas", Font.PLAIN, 46));
			lblLiveCur.setHorizontalAlignment(SwingConstants.CENTER);
			lblLiveCur.setBackground(Color.darkGray);
			lblLiveCur.setOpaque(true);
			lblLiveCur.setForeground(Color.orange);
			lblLiveCur.setBorder(LineBorder.createGrayLineBorder());
			pnlLive.add(lblLiveCur, new TableLayoutConstraints(1, 3, 1, 3, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

			//---- lbllblL6 ----
			lbllblL6.setText("<html>Ins. Res.<font size=-1> (M\u03a9)</html>");
			lbllblL6.setFont(new Font("Arial", Font.PLAIN, 20));
			lbllblL6.setHorizontalAlignment(SwingConstants.CENTER);
			lbllblL6.setBackground(Color.lightGray);
			lbllblL6.setOpaque(true);
			lbllblL6.setBorder(LineBorder.createGrayLineBorder());
			pnlLive.add(lbllblL6, new TableLayoutConstraints(0, 4, 1, 4, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

			//---- lblLiveIR ----
			lblLiveIR.setText("-");
			lblLiveIR.setFont(new Font("Consolas", Font.PLAIN, 46));
			lblLiveIR.setHorizontalAlignment(SwingConstants.CENTER);
			lblLiveIR.setBackground(Color.darkGray);
			lblLiveIR.setOpaque(true);
			lblLiveIR.setForeground(Color.orange);
			lblLiveIR.setBorder(LineBorder.createGrayLineBorder());
			pnlLive.add(lblLiveIR, new TableLayoutConstraints(0, 5, 1, 5, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

			//---- lbllblLSurge ----
			lbllblLSurge.setText("Surge Wave");
			lbllblLSurge.setFont(new Font("Arial", Font.PLAIN, 20));
			lbllblLSurge.setHorizontalAlignment(SwingConstants.CENTER);
			lbllblLSurge.setBackground(Color.lightGray);
			lbllblLSurge.setOpaque(true);
			lbllblLSurge.setBorder(LineBorder.createGrayLineBorder());
			pnlLive.add(lbllblLSurge, new TableLayoutConstraints(0, 6, 1, 6, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

			//======== pnlWC ========
			{
				pnlWC.setBackground(Color.darkGray);
				pnlWC.setLayout(new TableLayout(new double[][] {
					{TableLayout.FILL},
					{TableLayout.FILL}}));
			}
			pnlLive.add(pnlWC, new TableLayoutConstraints(0, 7, 1, 7, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

			//---- lblResTestStat ----
			lblResTestStat.setText("Test Is Disabled");
			lblResTestStat.setFont(new Font("Trebuchet MS", Font.ITALIC, 12));
			lblResTestStat.setHorizontalAlignment(SwingConstants.CENTER);
			lblResTestStat.setForeground(Color.lightGray);
			lblResTestStat.setOpaque(true);
			lblResTestStat.setBackground(Color.darkGray);
			lblResTestStat.setBorder(LineBorder.createGrayLineBorder());
			pnlLive.add(lblResTestStat, new TableLayoutConstraints(0, 1, 0, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

			//---- lblTempTestStat ----
			lblTempTestStat.setText("Test Is Disabled");
			lblTempTestStat.setFont(new Font("Trebuchet MS", Font.ITALIC, 12));
			lblTempTestStat.setHorizontalAlignment(SwingConstants.CENTER);
			lblTempTestStat.setForeground(Color.lightGray);
			lblTempTestStat.setBackground(Color.darkGray);
			lblTempTestStat.setOpaque(true);
			lblTempTestStat.setBorder(LineBorder.createGrayLineBorder());
			pnlLive.add(lblTempTestStat, new TableLayoutConstraints(1, 1, 1, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

			//---- lblInsResTestStat ----
			lblInsResTestStat.setText("Test Is Disabled");
			lblInsResTestStat.setFont(new Font("Trebuchet MS", Font.ITALIC, 12));
			lblInsResTestStat.setHorizontalAlignment(SwingConstants.CENTER);
			lblInsResTestStat.setForeground(Color.lightGray);
			lblInsResTestStat.setBackground(Color.darkGray);
			lblInsResTestStat.setOpaque(true);
			lblInsResTestStat.setBorder(LineBorder.createGrayLineBorder());
			pnlLive.add(lblInsResTestStat, new TableLayoutConstraints(0, 5, 1, 5, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

			//---- lblVTestStat ----
			lblVTestStat.setText("Test Is Disabled");
			lblVTestStat.setFont(new Font("Trebuchet MS", Font.ITALIC, 12));
			lblVTestStat.setHorizontalAlignment(SwingConstants.CENTER);
			lblVTestStat.setForeground(Color.lightGray);
			lblVTestStat.setBackground(Color.darkGray);
			lblVTestStat.setOpaque(true);
			lblVTestStat.setBorder(LineBorder.createGrayLineBorder());
			pnlLive.add(lblVTestStat, new TableLayoutConstraints(0, 3, 0, 3, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

			//---- lblCurTestStat ----
			lblCurTestStat.setText("Test Is Disabled");
			lblCurTestStat.setFont(new Font("Trebuchet MS", Font.ITALIC, 12));
			lblCurTestStat.setHorizontalAlignment(SwingConstants.CENTER);
			lblCurTestStat.setForeground(Color.lightGray);
			lblCurTestStat.setBackground(Color.darkGray);
			lblCurTestStat.setOpaque(true);
			lblCurTestStat.setBorder(LineBorder.createGrayLineBorder());
			pnlLive.add(lblCurTestStat, new TableLayoutConstraints(1, 3, 1, 3, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
		}
		contentPane.add(pnlLive, new TableLayoutConstraints(1, 3, 1, 3, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

		//======== pnlBot ========
		{
			pnlBot.setBackground(Color.gray);
			pnlBot.setLayout(new TableLayout(new double[][] {
				{160, TableLayout.FILL, 300},
				{TableLayout.FILL, 25}}));
			((TableLayout)pnlBot.getLayout()).setHGap(5);
			((TableLayout)pnlBot.getLayout()).setVGap(5);

			//---- lblLogo ----
			lblLogo.setIcon(new ImageIcon(getClass().getResource("/img/doer_logo.png")));
			lblLogo.setFocusable(false);
			lblLogo.setIconTextGap(0);
			lblLogo.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
			lblLogo.setHorizontalAlignment(SwingConstants.CENTER);
			lblLogo.setToolTipText("Click on this to know about the software and contact details");
			lblLogo.setBackground(new Color(0x003399));
			lblLogo.setOpaque(true);
			lblLogo.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseReleased(MouseEvent e) {
					lblLogoMouseReleased();
				}
			});
			pnlBot.add(lblLogo, new TableLayoutConstraints(0, 0, 0, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

			//---- lblStatus ----
			lblStatus.setText("IDLE");
			lblStatus.setFont(new Font("Arial Rounded MT Bold", Font.PLAIN, 24));
			lblStatus.setHorizontalAlignment(SwingConstants.CENTER);
			lblStatus.setHorizontalTextPosition(SwingConstants.CENTER);
			lblStatus.setBackground(Color.gray);
			lblStatus.setForeground(Color.yellow);
			lblStatus.setFocusable(false);
			lblStatus.setToolTipText("Displays the current status of this application");
			lblStatus.setAlignmentY(0.0F);
			lblStatus.setOpaque(true);
			pnlBot.add(lblStatus, new TableLayoutConstraints(1, 0, 1, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

			//---- txtSignal ----
			txtSignal.setFont(new Font("Arial", Font.BOLD, 15));
			txtSignal.setForeground(Color.white);
			txtSignal.setHorizontalAlignment(SwingConstants.CENTER);
			txtSignal.setBackground(Color.gray);
			txtSignal.setBorder(null);
			txtSignal.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
			txtSignal.setCaretColor(Color.lightGray);
			txtSignal.setDisabledTextColor(Color.white);
			pnlBot.add(txtSignal, new TableLayoutConstraints(1, 1, 1, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.TOP));

			//---- lstError ----
			lstError.setForeground(Color.white);
			lstError.setToolTipText("List of recent capture errors. Double click to expand, Delete key to clear.");
			lstError.setVisibleRowCount(2);
			lstError.setSelectionBackground(new Color(0xffe4c4));
			lstError.setSelectionForeground(Color.red);
			lstError.setBackground(Color.gray);
			lstError.setBorder(null);
			lstError.setFont(new Font("Arial", Font.PLAIN, 11));
			lstError.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			lstError.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					lstErrorMouseClicked(e);
				}
			});
			lstError.addKeyListener(new KeyAdapter() {
				@Override
				public void keyPressed(KeyEvent e) {
					lstErrorKeyPressed(e);
				}
			});
			pnlBot.add(lstError, new TableLayoutConstraints(2, 0, 2, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
		}
		contentPane.add(pnlBot, new TableLayoutConstraints(0, 4, 1, 4, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
		pack();
		setLocationRelativeTo(null);
		// JFormDesigner - End of component initialization  //GEN-END:initComponents
	}

	// JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
	private JLabel lblCompNm;
	private JPanel pnlStator;
	private JButton cmdStator;
	private JLabel lblStator4;
	private JLabel lblStator;
	private JLabel lblStator2;
	private JLabel lblPh;
	private JLabel lblStator5;
	private JLabel lblDirection;
	private JLabel lblStator3;
	private JLabel lblRat;
	private JPanel pnlSet;
	private JButton cmdTest;
	private JButton cmdDev;
	private JButton cmdUser;
	private JButton cmdOther;
	private JPanel pnlStOut;
	private JPanel pnlSt;
	private JPanel panel3;
	private JLabel lbllblTot;
	private JLabel lbllblPass;
	private JLabel lbllblFail;
	private JLabel lbllblUknown;
	private JLabel lblTot;
	private JLabel lblPass;
	private JLabel lblFail;
	private JLabel lblUnknown;
	private JPanel pnlTest;
	private JLabel lblTestMode;
	private JPanel pnlTbl;
	private JPanel pnlTblHdr;
	private JLabel lblCol18;
	private JLabel lblCol15;
	private JLabel lblCol12;
	private JLabel lblHRes;
	private JLabel lblStCl;
	private JLabel lblRunCl;
	private JLabel lblComCl;
	private JLabel lblHTemp;
	private JLabel lblHIR1;
	private JLabel lblHHV;
	private JLabel lblHIR2;
	private JLabel lblHHV1;
	private JLabel lblHHV2;
	private JLabel lblCol13;
	private JLabel lblCol10;
	private JLabel lblDir;
	private JLabel lblSurge;
	private JLabel lblCol16;
	private JLabel lblHSurge;
	private JScrollPane scrlTest;
	private JTable tblTestEntry;
	private JPanel pnlWaveImg;
	private JLabel lblEmergency;
	private JToggleButton tglTest;
	private JButton cmdOpen;
	private JButton cmdRep;
	private JButton cmdSave;
	private JButton cmdDel;
	private JToggleButton tglRetest;
	private JPanel panel2;
	private JLabel label1;
	private JTextField txtScannedSNo;
	private JPanel pnlLive;
	private JLabel lbllblL1;
	private JLabel lbllblL2;
	private JLabel lblLiveRes;
	private JLabel lblLiveTemp;
	private JLabel lbllblL3;
	private JLabel lbllblL4;
	private JLabel lblLiveV;
	private JLabel lblLiveCur;
	private JLabel lbllblL6;
	private JLabel lblLiveIR;
	private JLabel lbllblLSurge;
	private JPanel pnlWC;
	private JLabel lblResTestStat;
	private JLabel lblTempTestStat;
	private JLabel lblInsResTestStat;
	private JLabel lblVTestStat;
	private JLabel lblCurTestStat;
	private JPanel pnlBot;
	private JLabel lblLogo;
	private JLabel lblStatus;
	private JTextField txtSignal;
	private JList lstError;
	// JFormDesigner - End of variables declaration  //GEN-END:variables
}

// webcam motion detector implementation
class WebcamMotion implements WebcamMotionListener {
	WebcamMotionDetector motD = null;
	Webcam webCam = null;
	public WebcamMotion(Webcam webCam) {
		this.webCam = webCam;
		motD = new WebcamMotionDetector(webCam);
		motD.setInterval(500);
		motD.addMotionListener(this);
		start();
	}
	public void start() {
		motD.start();
	}
	public void stop() {
		motD.stop();
	}


	@Override
	public void motionDetected(WebcamMotionEvent arg0) {
		if (Configuration.IS_SURGE_IN_PROGRESS && Configuration.CUR_SURGE_IMG_COUNT < (Integer.valueOf(Configuration.NUMBER_OF_SURGE_IMG) * 2)) {
			// store the images
			try {
				// skip alternative one
				if (Configuration.CUR_SURGE_IMG_COUNT%2 == 0) {
					Configuration.storeSurgeWave(webCam.getImage());
					Thread.sleep(200);
				} else {
					// motion skipped
				}
				Configuration.CUR_SURGE_IMG_COUNT++;
			} catch (Exception e) {
				// ignore
			}
		}
	}
}

// extendted class to catch test failure
class TestFailedException extends Exception {
	TestFailedException() {
	}
}