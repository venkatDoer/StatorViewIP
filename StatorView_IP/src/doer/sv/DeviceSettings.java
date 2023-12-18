/*
 * Created by JFormDesigner on Thu Oct 04 11:41:41 IST 2012
 */

package doer.sv;

import java.awt.event.*;
import javax.swing.*;
import gnu.io.CommPortIdentifier;
import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstraints;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dialog;
import java.awt.Font;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Iterator;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import com.github.sarxos.webcam.Webcam;

import doer.io.Port;

/**
 * @author VENKATESAN SELVARAJ
 */
public class DeviceSettings extends JDialog {
	// custom variable declaration
	private Connection conn = null;
	private Statement stmt = null;
	private String protocol = null; 
	private SimpleDateFormat reqDtFormat = new SimpleDateFormat("dd-MM-yyyy");
	private SimpleDateFormat dbDtFormat = new SimpleDateFormat("yyyy-MM-dd");
	Calendar cal = Calendar.getInstance();
	Calendar dueCal = Calendar.getInstance();
	
	private Cursor waitCursor = new Cursor(Cursor.WAIT_CURSOR);
	private Cursor defCursor = new Cursor(Cursor.DEFAULT_CURSOR);
	
	StatorView frmMain = null;
	String visibleFilter = "and visible=1";
	// custom code end
		
	public DeviceSettings(Frame owner) {
		super(owner);
		frmMain = (StatorView) owner;
		initComponents();
		customInit();
	}

	public DeviceSettings(Dialog owner) {
		super(owner);
		initComponents();
	}

	private void cmdSaveActionPerformed() {
		
		this.setCursor(waitCursor);
		
		// save calibration details
		try {
			String calDt = "";
			String dueDt = "";
			String idList = "";
			
			for (int i=0; i<tblCal.getRowCount(); i++) {
				if (tblCal.getValueAt(i, 0) != null) {
					if (!tblCal.getValueAt(i, 0).toString().trim().isEmpty()) {
						idList += tblCal.getValueAt(i, 0).toString().trim() + ",";
						try {
							if (!tblCal.getValueAt(i, 5).toString().isEmpty()) {
								calDt = dbDtFormat.format(reqDtFormat.parse(tblCal.getValueAt(i, 5).toString()));
							}
							if (!tblCal.getValueAt(i, 6).toString().isEmpty()) {
								dueDt = dbDtFormat.format(reqDtFormat.parse(tblCal.getValueAt(i, 6).toString()));
							}
							
							stmt.executeUpdate("insert into " + Configuration.CALIBRATION + " values('" + Configuration.LINE_NAME + "', '" + tblCal.getValueAt(i, 0).toString() + "', '" + tblCal.getValueAt(i, 1).toString() + "', '" + tblCal.getValueAt(i, 2).toString() + "', '" + 
									tblCal.getValueAt(i, 3).toString() + "', '" + tblCal.getValueAt(i, 4).toString() + "', '" + 
									calDt + "', '" + dueDt + "', '" + tblCal.getValueAt(i, 7).toString() + "', " + (tblCal.getValueAt(i, 8).toString().equals("true") ? 1 : 0) + ")");
							
						} catch (SQLException se) {
							if (se.getMessage().contains("not unique")) {
								stmt.executeUpdate("update " + Configuration.CALIBRATION + " set ins_name='" + tblCal.getValueAt(i, 1).toString() + "', make='" + tblCal.getValueAt(i, 2).toString() + "', model='" + tblCal.getValueAt(i, 3).toString() + "', sno='" + 
										tblCal.getValueAt(i, 4).toString() + "', cal_date='" +  
										calDt + "', due_date='" + dueDt + "', agency='" + tblCal.getValueAt(i, 7).toString() + "', reminder=" + (tblCal.getValueAt(i, 8).toString().equals("true") ? 1 : 0) + " where line='" + Configuration.LINE_NAME + "' and ins_id='" + tblCal.getValueAt(i, 0).toString() + "'");
							} else {
								throw se;
							}
						} catch (ParseException e) {
							if (calDt.isEmpty()) {
								JOptionPane.showMessageDialog(this, "Invalid Calibrated Date for Instrument ID:" + tblCal.getValueAt(i, 0).toString() + "\nExpected date format:DD-MM-YYYY");
							} else {
								JOptionPane.showMessageDialog(this, "Invalid Calibration Due Date for Instrument ID:" + tblCal.getValueAt(i, 0).toString() + "\nExpected date format:DD-MM-YYYY");
							}
							return;
						}
						calDt = "";
						dueDt = "";
					}
				}
			}
			// clean up deleted records if any
			if (!idList.isEmpty()) {
				idList = idList.substring(0, idList.length()-1);
				stmt.executeUpdate("delete from " + Configuration.CALIBRATION + " where ins_id not in (" + idList + ")");
			}
		} catch (Exception se) {
			this.setCursor(defCursor);
			JOptionPane.showMessageDialog(this, "Error saving calibration details:" + se.getMessage());
			return;
		}
		this.setCursor(defCursor);
		JOptionPane.showMessageDialog(this, "Changes are saved successfully!"); 
	}

	private void cmdExitActionPerformed() {
		thisWindowClosing();
		this.setVisible(false);
	}

	private void thisWindowClosing() {
		try {
			stmt.close();
			conn.close();
		} catch (SQLException se) {
			// ignore this
		}
	}

	private void jtrOutputValueChanged() {
		if (jtrOutput.getSelectionPath() != null ) {
			if (jtrOutput.getSelectionPath().getPathCount() == 2) { // device level
				// load params belong to selected device
				String devNm = jtrOutput.getSelectionPath().getLastPathComponent().toString();
				lblDev.setText("Device:" + devNm);
				
				// load device details
				try {
					ResultSet res = stmt.executeQuery("select * from DEVICE where line='" + Configuration.LINE_NAME + "' and dev_name='" + devNm + "'" );
					if (res.next()) {
						protocol = res.getString("comm_protocol");
						txtId.setText(res.getString("dev_id"));
						cmbPort.setSelectedIndex(0);
						cmbPort.setSelectedItem(res.getString("dev_port"));
						
						if (cmbPort.getSelectedIndex() <= 0 && !res.getString("dev_port").isEmpty()) {
							Boolean exist = false;
							for(int j=0; j<cmbPort.getItemCount(); j++) {
								if (cmbPort.getItemAt(j).equals(res.getString("dev_port") + " (Invalid)")) {
									exist = true;
									break;
								}
							}
							if (!exist) {
								cmbPort.addItem(res.getString("dev_port") + " (Invalid)");
							}
							cmbPort.setSelectedItem(res.getString("dev_port") + " (Invalid)");
						}
						
						if (res.getString("dev_type").equals("S")) {
							cmbDevTye.setSelectedItem("Serial");
						} else if (res.getString("dev_type").equals("HID")) {
								cmbDevTye.setSelectedItem("HID");
						} else {
							cmbDevTye.setSelectedItem("Modbus");
						}
						cmbBaud.setSelectedItem(res.getString("baud_rt"));
						cmbDB.setSelectedItem(res.getString("data_bits"));
						cmbSB.setSelectedItem(res.getString("stop_bits"));
						cmbParity.setSelectedIndex(res.getInt("parity"));
						cmbWC.setSelectedIndex(res.getInt("wc"));
						cmbEnd.setSelectedItem(res.getString("endianness"));
						txtIpCmd.setText(res.getString("ip_cmd"));
						txtIPAdd.setText(res.getString("ip_address"));
						txtIPPort.setText(res.getString("ip_port"));
						if (protocol.equals("RTU")) {
							rtu();
						}else {
							tcpip();
						}
					}
					
					
					// load parameters belong to this device
					DefaultTableModel defMod = (DefaultTableModel) tblDevCfg.getModel();
					while (tblDevCfg.getRowCount() > 0) {
						defMod.removeRow(0);
					}
					res.close();
					res = stmt.executeQuery("select * from DEVICE_PARAM where line='" + Configuration.LINE_NAME + "' and dev_name='" + devNm + "' order by rowid" );
					int i=0;
					while (res.next()) {
						defMod.addRow(new String[] {"","","","",""});
						tblDevCfg.setValueAt(res.getString("param_name"), i, 0);
						tblDevCfg.setValueAt(res.getString("conv_factor"), i, 1);
						tblDevCfg.setValueAt(res.getString("format_text"), i, 2);
						tblDevCfg.setValueAt(res.getString("param_adr"), i, 3);
						tblDevCfg.setValueAt(res.getString("reg_type"), i, 4);
						++i;
					}
				} catch (Exception se) {
					JOptionPane.showMessageDialog(this, "Error loading device config:" + se.getMessage());
				}
			}
		}
	}

	private void cmdSaveDevActionPerformed() {
		// save the changes
		if (rtuRadio.isSelected()||ipRadio.isSelected()) {
			if (jtrOutput.getSelectionPath() != null) {
				this.setCursor(waitCursor);
				String devNm = jtrOutput.getSelectionPath().getLastPathComponent().toString();
				if (rtuRadio.isSelected()) {
					//protocol
					protocol = "RTU";
				}else {
					protocol = "TCP";
				}
				try {
					// master
					stmt.executeUpdate("update DEVICE set dev_id='" + txtId.getText().trim() + "',dev_port='" + (cmbPort.getSelectedItem().toString().contains("(Invalid)")?cmbPort.getSelectedItem().toString().substring(0, cmbPort.getSelectedItem().toString().indexOf("(Invalid)")-1): cmbPort.getSelectedItem().toString()) + 
							"', dev_type='" + (cmbDevTye.getSelectedIndex()==0?"S":cmbDevTye.getSelectedIndex()==1?"M":"HID") + "', baud_rt=" + cmbBaud.getSelectedItem().toString() + ", data_bits=" + cmbDB.getSelectedItem().toString() + ", stop_bits=" + cmbSB.getSelectedItem().toString() + ", parity=" + cmbParity.getSelectedIndex() + ", wc=" + cmbWC.getSelectedIndex() + ", endianness='" + cmbEnd.getSelectedItem().toString()  + 
							"', fc=0, ip_cmd = '" + txtIpCmd.getText().trim() + "',comm_protocol = '" + protocol + "',ip_address = '" + txtIPAdd.getText().trim() + "',ip_port = '" + txtIPPort.getText().trim() + 
							"' where dev_name='" + devNm + "' and line='" + Configuration.LINE_NAME + "'");
					// detail
					for(int i=0; i < tblDevCfg.getRowCount(); i++) {
						stmt.executeUpdate("update DEVICE_PARAM set param_adr='" + tblDevCfg.getValueAt(i, 3).toString().trim() + "', conv_factor='" + tblDevCfg.getValueAt(i, 1).toString().trim() + "', format_text='" + tblDevCfg.getValueAt(i, 2).toString().trim()  + "', reg_type='" + tblDevCfg.getValueAt(i, 4).toString().trim() + 
								"' where param_name='" + tblDevCfg.getValueAt(i, 0).toString().trim() + 
								"' and dev_name='" + devNm + "' and line='" + Configuration.LINE_NAME  + "'");
					}
				} catch (SQLException se) {
					JOptionPane.showMessageDialog(this, "Error updating device config:" + se.getMessage());
				}
					
				// refresh main form if auto capture / live is running
				frmMain.restartTest();
				
				this.setCursor(defCursor);
			} else {
				JOptionPane.showMessageDialog(this, "No device selected to save");
			}
		}else {
			JOptionPane.showMessageDialog(this, "Select the Communication Mode Either IP communicatin or RTU Communication");
		}
	}

	private void cmdSaveCamActionPerformed() {
		setCursor(waitCursor);
		if (!Configuration.LAST_USED_WEBCAM.equals(String.valueOf(cmbCam.getSelectedIndex()))) {
			Configuration.LAST_USED_WEBCAM = String.valueOf(cmbCam.getSelectedIndex());
			Configuration.saveConfigValues("LAST_USED_WEBCAM");
			frmMain.enableSurgeWave();
		}
		setCursor(defCursor);
	}

	private void cmbDevTyeItemStateChanged() {
		// TODO add your code here
	}

	private void cmbWCActionPerformed() {
		// TODO add your code here
	}

	private void rtu(){
		rtuRadio.setSelected(true);
		if(ipRadio.isSelected()) 
		{
			ipRadio.setSelected(false);
			
		}
		txtIPAdd.setEnabled(false);		
		txtIPPort.setEnabled(false);
		
		cmbPort.setEnabled(true);
		cmbBaud.setEnabled(true);
		cmbDevTye.setEnabled(true);
		txtIpCmd.setEnabled(true);
		cmbParity.setEnabled(true);
		cmbDB.setEnabled(true);
		cmbSB.setEnabled(true);
	}
	
	private void tcpip() {
		ipRadio.setSelected(true);
		if(rtuRadio.isSelected()) 
		{
			rtuRadio.setSelected(false);
			
		}
		txtIPAdd.setEnabled(true);		
		txtIPPort.setEnabled(true);
		
		cmbPort.setEnabled(false);
		cmbBaud.setEnabled(false);
		cmbDevTye.setEnabled(false);
		txtIpCmd.setEnabled(false);
		cmbParity.setEnabled(false);
		cmbDB.setEnabled(false);
		cmbSB.setEnabled(false);
	}
	private void ipRadio(ActionEvent e) {
		// Changing the DeviceSettngs for TCP/IP Communication 
		tcpip();
	}

	private void rtuRadio(ActionEvent e) {
		// Changing the DeviceSettngs for RTU Communication 
		rtu();
		
	}


	private void initComponents() {
		// JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
		pnlHead = new JPanel();
		panel3 = new JPanel();
		scrlTree = new JScrollPane();
		jtrOutput = new JTree();
		pnlDev = new JPanel();
		panel6 = new JPanel();
		lblDev = new JLabel();
		ipRadio = new JRadioButton();
		rtuRadio = new JRadioButton();
		label82 = new JLabel();
		cmbPort = new JComboBox();
		label86 = new JLabel();
		txtIPAdd = new JTextField();
		label87 = new JLabel();
		txtIPPort = new JTextField();
		label81 = new JLabel();
		txtId = new JTextField();
		label9 = new JLabel();
		cmbWC = new JComboBox<>();
		label23 = new JLabel();
		cmbEnd = new JComboBox<>();
		label5 = new JLabel();
		cmbBaud = new JComboBox<>();
		label84 = new JLabel();
		cmbDevTye = new JComboBox<>();
		label85 = new JLabel();
		txtIpCmd = new JTextField();
		label7 = new JLabel();
		cmbParity = new JComboBox<>();
		label6 = new JLabel();
		cmbDB = new JComboBox<>();
		label8 = new JLabel();
		cmbSB = new JComboBox<>();
		panel7 = new JPanel();
		scrlDevCfg = new JScrollPane();
		tblDevCfg = new JTable();
		cmdSaveDev = new JButton();
		pnlWC = new JPanel();
		label83 = new JLabel();
		cmbCam = new JComboBox();
		cmdSaveCam = new JButton();
		pnlCal = new JPanel();
		scrollPane1 = new JScrollPane();
		tblCal = new JTable();
		cmdSave = new JButton();
		cmdExit = new JButton();

		//======== this ========
		setTitle("Doer StatorView: Device Settings");
		setFont(new Font("Arial", Font.PLAIN, 12));
		setModal(true);
		setResizable(false);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				thisWindowClosing();
			}
		});
		Container contentPane = getContentPane();
		contentPane.setLayout(new TableLayout(new double[][] {
			{10, TableLayout.FILL, 10},
			{10, 393, TableLayout.PREFERRED, 145, 10}}));
		((TableLayout)contentPane.getLayout()).setHGap(5);
		((TableLayout)contentPane.getLayout()).setVGap(5);

		//======== pnlHead ========
		{
			pnlHead.setBorder(new TitledBorder(null, "Device Communication", TitledBorder.LEFT, TitledBorder.TOP,
				new Font("Arial", Font.PLAIN, 16), new Color(0x006699)));
			pnlHead.setFocusable(false);
			pnlHead.setLayout(new TableLayout(new double[][] {
				{TableLayout.PREFERRED, TableLayout.FILL},
				{TableLayout.FILL}}));
			((TableLayout)pnlHead.getLayout()).setHGap(5);
			((TableLayout)pnlHead.getLayout()).setVGap(5);

			//======== panel3 ========
			{
				panel3.setLayout(new TableLayout(new double[][] {
					{255},
					{TableLayout.FILL}}));
				((TableLayout)panel3.getLayout()).setHGap(5);
				((TableLayout)panel3.getLayout()).setVGap(5);

				//======== scrlTree ========
				{

					//---- jtrOutput ----
					jtrOutput.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
					jtrOutput.addTreeSelectionListener(e -> jtrOutputValueChanged());
					scrlTree.setViewportView(jtrOutput);
				}
				panel3.add(scrlTree, new TableLayoutConstraints(0, 0, 0, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
			}
			pnlHead.add(panel3, new TableLayoutConstraints(0, 0, 0, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

			//======== pnlDev ========
			{
				pnlDev.setLayout(new TableLayout(new double[][] {
					{TableLayout.FILL, TableLayout.FILL},
					{TableLayout.PREFERRED, TableLayout.FILL, TableLayout.PREFERRED}}));
				((TableLayout)pnlDev.getLayout()).setHGap(5);
				((TableLayout)pnlDev.getLayout()).setVGap(5);

				//======== panel6 ========
				{
					panel6.setLayout(new TableLayout(new double[][] {
						{TableLayout.FILL, TableLayout.FILL, TableLayout.FILL, TableLayout.FILL, TableLayout.FILL, TableLayout.FILL},
						{23, TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.PREFERRED}}));
					((TableLayout)panel6.getLayout()).setHGap(5);
					((TableLayout)panel6.getLayout()).setVGap(5);

					//---- lblDev ----
					lblDev.setIcon(null);
					lblDev.setBackground(Color.darkGray);
					lblDev.setOpaque(true);
					lblDev.setFocusable(false);
					lblDev.setText("Instrument");
					lblDev.setHorizontalAlignment(SwingConstants.CENTER);
					lblDev.setFont(new Font("Arial", Font.PLAIN, 16));
					lblDev.setForeground(Color.white);
					panel6.add(lblDev, new TableLayoutConstraints(0, 0, 5, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

					//---- ipRadio ----
					ipRadio.setText("IP Communication");
					ipRadio.setFont(new Font("Arial", Font.PLAIN, 12));
					ipRadio.addActionListener(e -> ipRadio(e));
					panel6.add(ipRadio, new TableLayoutConstraints(0, 1, 2, 1, TableLayoutConstraints.CENTER, TableLayoutConstraints.FULL));

					//---- rtuRadio ----
					rtuRadio.setText("RTU Communication");
					rtuRadio.setFont(new Font("Arial", Font.PLAIN, 12));
					rtuRadio.addActionListener(e -> rtuRadio(e));
					panel6.add(rtuRadio, new TableLayoutConstraints(3, 1, 5, 1, TableLayoutConstraints.CENTER, TableLayoutConstraints.FULL));

					//---- label82 ----
					label82.setText("Port");
					label82.setFont(new Font("Arial", Font.PLAIN, 12));
					panel6.add(label82, new TableLayoutConstraints(0, 2, 0, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.CENTER));

					//---- cmbPort ----
					cmbPort.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
					cmbPort.setToolTipText("Port name where this instrument is connected.");
					panel6.add(cmbPort, new TableLayoutConstraints(1, 2, 1, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

					//---- label86 ----
					label86.setText("IP Address");
					label86.setFont(new Font("Arial", Font.PLAIN, 12));
					panel6.add(label86, new TableLayoutConstraints(2, 2, 2, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

					//---- txtIPAdd ----
					txtIPAdd.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
					txtIPAdd.setToolTipText("Instrument identification/address");
					panel6.add(txtIPAdd, new TableLayoutConstraints(3, 2, 3, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

					//---- label87 ----
					label87.setText("IP Port");
					label87.setFont(new Font("Arial", Font.PLAIN, 12));
					panel6.add(label87, new TableLayoutConstraints(4, 2, 4, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

					//---- txtIPPort ----
					txtIPPort.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
					txtIPPort.setToolTipText("Instrument identification/address");
					panel6.add(txtIPPort, new TableLayoutConstraints(5, 2, 5, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

					//---- label81 ----
					label81.setText("Device Address");
					label81.setFont(new Font("Arial", Font.PLAIN, 12));
					panel6.add(label81, new TableLayoutConstraints(0, 3, 0, 3, TableLayoutConstraints.FULL, TableLayoutConstraints.CENTER));

					//---- txtId ----
					txtId.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
					txtId.setToolTipText("Instrument identification/address");
					panel6.add(txtId, new TableLayoutConstraints(1, 3, 1, 3, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

					//---- label9 ----
					label9.setText("Word Count");
					label9.setFont(new Font("Arial", Font.PLAIN, 12));
					panel6.add(label9, new TableLayoutConstraints(2, 3, 2, 3, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

					//---- cmbWC ----
					cmbWC.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
					cmbWC.setModel(new DefaultComboBoxModel<>(new String[] {
						"NA",
						"1",
						"2"
					}));
					cmbWC.setSelectedIndex(2);
					cmbWC.addActionListener(e -> cmbWCActionPerformed());
					panel6.add(cmbWC, new TableLayoutConstraints(3, 3, 3, 3, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

					//---- label23 ----
					label23.setText("Endianness");
					label23.setFont(new Font("Arial", Font.PLAIN, 12));
					panel6.add(label23, new TableLayoutConstraints(4, 3, 4, 3, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

					//---- cmbEnd ----
					cmbEnd.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
					cmbEnd.setModel(new DefaultComboBoxModel<>(new String[] {
						"NA",
						"MSB First",
						"LSB First"
					}));
					cmbEnd.setSelectedIndex(1);
					panel6.add(cmbEnd, new TableLayoutConstraints(5, 3, 5, 3, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

					//---- label5 ----
					label5.setText("Baud Rate");
					label5.setFont(new Font("Arial", Font.PLAIN, 12));
					panel6.add(label5, new TableLayoutConstraints(0, 4, 0, 4, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

					//---- cmbBaud ----
					cmbBaud.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
					cmbBaud.setModel(new DefaultComboBoxModel<>(new String[] {
						"110",
						"300",
						"1200",
						"2400",
						"4800",
						"9600",
						"19200",
						"38400",
						"57600",
						"115200",
						"230400",
						"460800",
						"921600"
					}));
					cmbBaud.setSelectedIndex(5);
					panel6.add(cmbBaud, new TableLayoutConstraints(1, 4, 1, 4, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

					//---- label84 ----
					label84.setText("Deivce Type");
					label84.setFont(new Font("Arial", Font.PLAIN, 12));
					panel6.add(label84, new TableLayoutConstraints(2, 4, 2, 4, TableLayoutConstraints.FULL, TableLayoutConstraints.CENTER));

					//---- cmbDevTye ----
					cmbDevTye.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
					cmbDevTye.setToolTipText("Port name where this instrument is connected. This is common for all outputs of this device.");
					cmbDevTye.setModel(new DefaultComboBoxModel<>(new String[] {
						"Serial",
						"Modbus",
						"HID"
					}));
					cmbDevTye.setSelectedIndex(1);
					cmbDevTye.addItemListener(e -> cmbDevTyeItemStateChanged());
					panel6.add(cmbDevTye, new TableLayoutConstraints(3, 4, 3, 4, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

					//---- label85 ----
					label85.setText("Input Command");
					label85.setFont(new Font("Arial", Font.PLAIN, 12));
					panel6.add(label85, new TableLayoutConstraints(4, 4, 4, 4, TableLayoutConstraints.FULL, TableLayoutConstraints.CENTER));

					//---- txtIpCmd ----
					txtIpCmd.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
					txtIpCmd.setToolTipText("Input command for serial device");
					panel6.add(txtIpCmd, new TableLayoutConstraints(5, 4, 5, 4, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

					//---- label7 ----
					label7.setText("Parity");
					label7.setFont(new Font("Arial", Font.PLAIN, 12));
					panel6.add(label7, new TableLayoutConstraints(0, 5, 0, 5, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

					//---- cmbParity ----
					cmbParity.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
					cmbParity.setModel(new DefaultComboBoxModel<>(new String[] {
						"None",
						"Odd",
						"Even",
						"Mark",
						"Space"
					}));
					panel6.add(cmbParity, new TableLayoutConstraints(1, 5, 1, 5, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

					//---- label6 ----
					label6.setText("Data Bits");
					label6.setFont(new Font("Arial", Font.PLAIN, 12));
					panel6.add(label6, new TableLayoutConstraints(2, 5, 2, 5, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

					//---- cmbDB ----
					cmbDB.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
					cmbDB.setModel(new DefaultComboBoxModel<>(new String[] {
						"5",
						"6",
						"7",
						"8"
					}));
					cmbDB.setSelectedIndex(3);
					panel6.add(cmbDB, new TableLayoutConstraints(3, 5, 3, 5, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

					//---- label8 ----
					label8.setText("Stop Bits");
					label8.setFont(new Font("Arial", Font.PLAIN, 12));
					panel6.add(label8, new TableLayoutConstraints(4, 5, 4, 5, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

					//---- cmbSB ----
					cmbSB.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
					cmbSB.setModel(new DefaultComboBoxModel<>(new String[] {
						"1",
						"2"
					}));
					panel6.add(cmbSB, new TableLayoutConstraints(5, 5, 5, 5, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
				}
				pnlDev.add(panel6, new TableLayoutConstraints(0, 0, 1, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//======== panel7 ========
				{
					panel7.setLayout(new TableLayout(new double[][] {
						{TableLayout.FILL, TableLayout.FILL},
						{TableLayout.FILL}}));
					((TableLayout)panel7.getLayout()).setHGap(5);
					((TableLayout)panel7.getLayout()).setVGap(5);

					//======== scrlDevCfg ========
					{
						scrlDevCfg.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
						scrlDevCfg.setBackground(Color.white);

						//---- tblDevCfg ----
						tblDevCfg.setModel(new DefaultTableModel(
							new Object[][] {
								{null, null, null, null, null},
								{null, null, null, null, null},
								{null, null, null, null, null},
							},
							new String[] {
								"Parameter", "Conv. Formula", "Format", "Register", "Register Type"
							}
						) {
							boolean[] columnEditable = new boolean[] {
								false, true, true, true, true
							};
							@Override
							public boolean isCellEditable(int rowIndex, int columnIndex) {
								return columnEditable[columnIndex];
							}
						});
						{
							TableColumnModel cm = tblDevCfg.getColumnModel();
							cm.getColumn(0).setMinWidth(200);
							cm.getColumn(0).setMaxWidth(200);
							cm.getColumn(0).setPreferredWidth(200);
							cm.getColumn(1).setMinWidth(80);
							cm.getColumn(1).setMaxWidth(80);
							cm.getColumn(1).setPreferredWidth(80);
							cm.getColumn(2).setMinWidth(75);
							cm.getColumn(2).setMaxWidth(75);
							cm.getColumn(2).setPreferredWidth(75);
							cm.getColumn(3).setMinWidth(75);
							cm.getColumn(3).setMaxWidth(75);
							cm.getColumn(3).setPreferredWidth(75);
						}
						tblDevCfg.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
						tblDevCfg.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
						tblDevCfg.setBorder(null);
						tblDevCfg.setToolTipText("List of reading parameters and corresponding registers");
						scrlDevCfg.setViewportView(tblDevCfg);
					}
					panel7.add(scrlDevCfg, new TableLayoutConstraints(0, 0, 1, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
				}
				pnlDev.add(panel7, new TableLayoutConstraints(0, 1, 1, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- cmdSaveDev ----
				cmdSaveDev.setText("Save Instrument");
				cmdSaveDev.setFont(new Font("Arial", Font.PLAIN, 16));
				cmdSaveDev.setMnemonic('I');
				cmdSaveDev.setToolTipText("Click this to save changes made for above instrument");
				cmdSaveDev.setIcon(new ImageIcon(getClass().getResource("/img/save.png")));
				cmdSaveDev.addActionListener(e -> cmdSaveDevActionPerformed());
				pnlDev.add(cmdSaveDev, new TableLayoutConstraints(0, 2, 1, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
			}
			pnlHead.add(pnlDev, new TableLayoutConstraints(1, 0, 1, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
		}
		contentPane.add(pnlHead, new TableLayoutConstraints(1, 1, 1, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

		//======== pnlWC ========
		{
			pnlWC.setBorder(new TitledBorder(null, "Surge Camera", TitledBorder.LEADING, TitledBorder.DEFAULT_POSITION,
				new Font("Arial", Font.PLAIN, 16), new Color(0x006699)));
			pnlWC.setLayout(new TableLayout(new double[][] {
				{256, TableLayout.FILL, 140},
				{TableLayout.PREFERRED}}));
			((TableLayout)pnlWC.getLayout()).setHGap(5);
			((TableLayout)pnlWC.getLayout()).setVGap(5);

			//---- label83 ----
			label83.setText("Camera To Capture Surge Wave");
			label83.setFont(new Font("Arial", Font.PLAIN, 14));
			pnlWC.add(label83, new TableLayoutConstraints(0, 0, 0, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.CENTER));

			//---- cmbCam ----
			cmbCam.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
			pnlWC.add(cmbCam, new TableLayoutConstraints(1, 0, 1, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

			//---- cmdSaveCam ----
			cmdSaveCam.setText("Save");
			cmdSaveCam.setFont(new Font("Arial", Font.PLAIN, 16));
			cmdSaveCam.setIcon(new ImageIcon(getClass().getResource("/img/save.png")));
			cmdSaveCam.setToolTipText("Click this to use this camera for capturing surge wave");
			cmdSaveCam.setMnemonic('S');
			cmdSaveCam.addActionListener(e -> cmdSaveCamActionPerformed());
			pnlWC.add(cmdSaveCam, new TableLayoutConstraints(2, 0, 2, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
		}
		contentPane.add(pnlWC, new TableLayoutConstraints(1, 2, 1, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

		//======== pnlCal ========
		{
			pnlCal.setBorder(new TitledBorder(null, "Calibration Record   [Date Format: DD-MM-YYYY]", TitledBorder.LEADING, TitledBorder.DEFAULT_POSITION,
				new Font("Arial", Font.PLAIN, 16), new Color(0x006699)));
			pnlCal.setLayout(new TableLayout(new double[][] {
				{TableLayout.FILL, 140},
				{TableLayout.FILL, TableLayout.PREFERRED}}));
			((TableLayout)pnlCal.getLayout()).setHGap(5);
			((TableLayout)pnlCal.getLayout()).setVGap(5);

			//======== scrollPane1 ========
			{
				scrollPane1.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

				//---- tblCal ----
				tblCal.setModel(new DefaultTableModel(
					new Object[][] {
						{null, null, null, null, null, null, null, null, null},
						{null, null, null, null, null, null, null, null, null},
						{null, null, null, null, null, null, null, null, null},
					},
					new String[] {
						"ID", "Instrument Name", "Make", "Model", "SNo.", "Calib. Date", "Due Date", "Agency", "Reminder On/Off"
					}
				) {
					Class<?>[] columnTypes = new Class<?>[] {
						String.class, String.class, String.class, String.class, String.class, String.class, String.class, String.class, Boolean.class
					};
					@Override
					public Class<?> getColumnClass(int columnIndex) {
						return columnTypes[columnIndex];
					}
				});
				{
					TableColumnModel cm = tblCal.getColumnModel();
					cm.getColumn(0).setResizable(false);
					cm.getColumn(0).setPreferredWidth(60);
					cm.getColumn(1).setResizable(false);
					cm.getColumn(1).setPreferredWidth(184);
					cm.getColumn(2).setResizable(false);
					cm.getColumn(2).setPreferredWidth(75);
					cm.getColumn(3).setResizable(false);
					cm.getColumn(3).setPreferredWidth(75);
					cm.getColumn(4).setResizable(false);
					cm.getColumn(4).setPreferredWidth(60);
					cm.getColumn(5).setResizable(false);
					cm.getColumn(5).setPreferredWidth(90);
					cm.getColumn(6).setResizable(false);
					cm.getColumn(6).setPreferredWidth(90);
					cm.getColumn(7).setResizable(false);
					cm.getColumn(7).setPreferredWidth(80);
					cm.getColumn(8).setResizable(false);
					cm.getColumn(8).setPreferredWidth(60);
				}
				tblCal.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
				tblCal.setToolTipText("Calibration record of all instruments of this panel");
				scrollPane1.setViewportView(tblCal);
			}
			pnlCal.add(scrollPane1, new TableLayoutConstraints(0, 0, 1, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

			//---- cmdSave ----
			cmdSave.setText("Save Calibration Record");
			cmdSave.setFont(new Font("Arial", Font.PLAIN, 16));
			cmdSave.setIcon(new ImageIcon(getClass().getResource("/img/save.png")));
			cmdSave.setToolTipText("Click this to save changes made above for calibration record");
			cmdSave.setMnemonic('C');
			cmdSave.addActionListener(e -> cmdSaveActionPerformed());
			pnlCal.add(cmdSave, new TableLayoutConstraints(0, 1, 0, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

			//---- cmdExit ----
			cmdExit.setText("<html>Close&nbsp;&nbsp<font size=-2>[Esc]</html>");
			cmdExit.setFont(new Font("Arial", Font.PLAIN, 16));
			cmdExit.setIcon(new ImageIcon(getClass().getResource("/img/exit.PNG")));
			cmdExit.setToolTipText("Click on this to close this window");
			cmdExit.addActionListener(e -> cmdExitActionPerformed());
			pnlCal.add(cmdExit, new TableLayoutConstraints(1, 1, 1, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
		}
		contentPane.add(pnlCal, new TableLayoutConstraints(1, 3, 1, 3, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
		pack();
		setLocationRelativeTo(getOwner());
		// JFormDesigner - End of component initialization  //GEN-END:initComponents
	}

	// JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
	private JPanel pnlHead;
	private JPanel panel3;
	private JScrollPane scrlTree;
	private JTree jtrOutput;
	private JPanel pnlDev;
	private JPanel panel6;
	private JLabel lblDev;
	private JRadioButton ipRadio;
	private JRadioButton rtuRadio;
	private JLabel label82;
	private JComboBox cmbPort;
	private JLabel label86;
	private JTextField txtIPAdd;
	private JLabel label87;
	private JTextField txtIPPort;
	private JLabel label81;
	private JTextField txtId;
	private JLabel label9;
	private JComboBox<String> cmbWC;
	private JLabel label23;
	private JComboBox<String> cmbEnd;
	private JLabel label5;
	private JComboBox<String> cmbBaud;
	private JLabel label84;
	private JComboBox<String> cmbDevTye;
	private JLabel label85;
	private JTextField txtIpCmd;
	private JLabel label7;
	private JComboBox<String> cmbParity;
	private JLabel label6;
	private JComboBox<String> cmbDB;
	private JLabel label8;
	private JComboBox<String> cmbSB;
	private JPanel panel7;
	private JScrollPane scrlDevCfg;
	private JTable tblDevCfg;
	private JButton cmdSaveDev;
	private JPanel pnlWC;
	private JLabel label83;
	private JComboBox cmbCam;
	private JButton cmdSaveCam;
	private JPanel pnlCal;
	private JScrollPane scrollPane1;
	private JTable tblCal;
	private JButton cmdSave;
	private JButton cmdExit;
	// JFormDesigner - End of variables declaration  //GEN-END:variables

	// custom code begin
	
	private void customInit() {
		associateFunctionKeys();
		
		// remove table header and set table properties
		scrlDevCfg.setViewportView(tblDevCfg);
		
		// load available devices
		loadDevList();
		
		// load available & selected webcams
		if (Configuration.IS_SURGE_DISABLED.equals("0") && Configuration.IS_SURGE_WAVE_CAPTURED.equals("1")) {
			if (Webcam.getWebcams().size() > 0) {
				for(Webcam wc : Webcam.getWebcams()) {
					cmbCam.addItem(wc.getName());
				}
				try {
					cmbCam.setSelectedIndex(Integer.valueOf(Configuration.LAST_USED_WEBCAM));
				} catch (IllegalArgumentException e) {
					cmbCam.setSelectedIndex(0);
				}
			}
		} else {
			cmbCam.setEnabled(false);
			cmdSaveCam.setEnabled(false);
		}
		
		// hook table renderer for highlighting due dates
		tblCal.getColumnModel().getColumn(6).setCellRenderer(new MyTableCellRender());
		
		// load list of available serial ports into port list
		cmbPort.addItem("");
		
		HashSet<CommPortIdentifier> portHashList = Port.getAvailableSerialPorts();
		Iterator<CommPortIdentifier> portList = portHashList.iterator();
		CommPortIdentifier port = null;
		while ( portList.hasNext() )
		{
			port = (CommPortIdentifier) portList.next();
			cmbPort.addItem(port.getName());
		}
		
		// load calibration details
		int curRow = 0;
		try {
			// fetch and load the result
			ResultSet res = null;
			Boolean needInsert = false;
			try {
				res = stmt.executeQuery("select * from " + Configuration.CALIBRATION + " where line='" + Configuration.LINE_NAME + "'");
			} catch (SQLException sqle) {
				if (sqle.getMessage().contains("no such table")) {
					// create table and insert defaults
						stmt.executeUpdate("create table " + Configuration.CALIBRATION + " (line text, ins_id text, ins_name text, make text, model text, sno text, cal_date date, due_date date, agency text, reminder boolean, primary key (line, ins_id))");
						needInsert = true;
				} else {
					throw sqle;
				}
			}
			if (res != null) {
				if (!res.isBeforeFirst()) {
					needInsert = true;
				}
			}
				
			if (needInsert) {
				stmt.executeUpdate("insert into " + Configuration.CALIBRATION + " values ('" + Configuration.LINE_NAME + "', '1', 'GRINDEX ELECTRIK ANALYSER', '', '', '', '', '', '', 1)");
				stmt.executeUpdate("insert into " + Configuration.CALIBRATION + " values ('" + Configuration.LINE_NAME + "', '2', 'PLC', '', '', '', '', '', '', 1)");
				// reload
				res = stmt.executeQuery("select * from " + Configuration.CALIBRATION + " where line ='" + Configuration.LINE_NAME + "'");
			}
			
		
			while (res.next()) {
				tblCal.setValueAt(res.getString("ins_id"), curRow, 0);
				tblCal.setValueAt(res.getString("ins_name"), curRow, 1);
				tblCal.setValueAt(res.getString("make"), curRow, 2);
				tblCal.setValueAt(res.getString("model"), curRow, 3);
				tblCal.setValueAt(res.getString("sno"), curRow, 4);
				try {
					// default the date first
					tblCal.setValueAt("", curRow, 5);
					tblCal.setValueAt("", curRow, 6);
					// set dates from db
					tblCal.setValueAt(reqDtFormat.format(dbDtFormat.parse(res.getString("cal_date"))), curRow, 5);
					tblCal.setValueAt(reqDtFormat.format(dbDtFormat.parse(res.getString("due_date"))), curRow, 6);
				} catch (ParseException e) {
					// ignore it
				}
				tblCal.setValueAt(res.getString("agency"), curRow, 7);
				tblCal.setValueAt(res.getBoolean("reminder"), curRow, 8);
				++curRow;
			}
			res.close();
		} catch (Exception e) {
				JOptionPane.showMessageDialog(this, "Error reading calibration details:" + e.getMessage());
				return;
		}
		
		// fill rest of rows with empty values to avoid null pointer exception later
		for (int i=curRow; i < tblCal.getRowCount(); i++) {
			for (int j=0; j < tblCal.getColumnCount()-1; j++) {
				tblCal.setValueAt("", i, j);
			}
			tblCal.setValueAt(false, i, 8);
		}
	}
	
	private void loadDevList() {
		DefaultMutableTreeNode trRt = new DefaultMutableTreeNode("Available Devices");
		try {
			conn = DriverManager.getConnection(Configuration.DB_URL);
			stmt = conn.createStatement();
			stmt.setQueryTimeout(30);
			
			ResultSet res = null;
			res = stmt.executeQuery("select distinct(dev_name) from " + Configuration.DEVICE + " where line ='" + Configuration.LINE_NAME + "'");
			while (res.next()) {
					trRt.add(new DefaultMutableTreeNode(res.getString("dev_name")));
			}
			res.close();
		} catch (SQLException se) {
			JOptionPane.showMessageDialog(this, "Error loading available devices:" + se.getMessage());
		}
		DefaultTreeModel trMod = (DefaultTreeModel) jtrOutput.getModel();
		trMod.setRoot(trRt);
	}
  
	
	private void associateFunctionKeys() {
		
		// associate Esc for exit
		String CLOSE_ACTION_KEY = "closeAction";
		Action closeAction = new AbstractAction() {
		      public void actionPerformed(ActionEvent actionEvent) {
		        cmdExitActionPerformed();
		      }
		    };
		KeyStroke esc = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
		InputMap closeInputMap = cmdExit.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
		closeInputMap.put(esc, CLOSE_ACTION_KEY);
		ActionMap closeActionMap = cmdExit.getActionMap();
		closeActionMap.put(CLOSE_ACTION_KEY, closeAction);
		cmdExit.setActionMap(closeActionMap);
	}
	
	class MyTableCellRender extends DefaultTableCellRenderer {  
		   
		public Component getTableCellRendererComponent(  
			JTable table, Object value, boolean isSelected, 
			boolean hasFocus, int row, int col) {
			     super.getTableCellRendererComponent(
			                      table,  value, isSelected, hasFocus, row, col);
	
			     setIcon(null);
			     if (table.getModel().getValueAt(row, 6) != null && table.getModel().getValueAt(row, 8).toString().equals("true")) {
				 String dueDt =  table.getModel().getValueAt(row, 6).toString();
				 if (!dueDt.isEmpty()) {
					 // highlight due dates
					try {
						dueCal.setTime(reqDtFormat.parse(dueDt));
					} catch (ParseException e) {
						return this;
					}
					dueCal.add(Calendar.DATE, -7);
					if (cal.compareTo(dueCal) >= 0) {
						setIcon(new ImageIcon(getClass().getResource("/img/bell.PNG")));
					}
				 }
			}
			return this;
		}
	}
}
