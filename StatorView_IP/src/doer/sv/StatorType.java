/*
 * Created by JFormDesigner on Thu Apr 22 13:00:42 PDT 2010
 */

package doer.sv;

import javax.swing.*;
import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstraints;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;

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
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;

/**
 * @author venkatesan selvaraj
 */
public class StatorType extends JDialog {
	
	// CUSTOM VARIABLES - BEGIN
	private StatorView mainFormRef = null;
	private String lastStatorType = "";
	
	private Connection conn = null;
	private Statement stmt = null;
	private Integer defSelRow = 0;
	
	private DecimalFormat dotOne = new DecimalFormat("#0.0");
	// CUSTOM VARIABLES - END
	public StatorType(Frame owner, String lastType) {
		super(owner);
		if (owner != null) {
			mainFormRef = (StatorView) owner;
		}
		lastStatorType = lastType;
		initComponents();
		customInit();
		loadExistingRecords();
		// select and focus the default record
		tblTypeList.getSelectionModel().setSelectionInterval(0, defSelRow);
	}

	public StatorType(Dialog owner) {
		super(owner);
		initComponents();
	}

	private void associateFunctionKeys() {
		// associate enter for choose
		String CHOOSE_ACTION_KEY = "chooseAction";
		Action chooseAction = new AbstractAction() {
		      public void actionPerformed(ActionEvent actionEvent) {
		        cmdChooseActionPerformed();
		      }
		    };
		KeyStroke entr = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);
		InputMap chooseInputMap = cmdChoose.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
		chooseInputMap.put(entr, CHOOSE_ACTION_KEY);
		ActionMap chooseActionMap = cmdChoose.getActionMap();
		chooseActionMap.put(CHOOSE_ACTION_KEY, chooseAction);
		cmdChoose.setActionMap(chooseActionMap);
		
		// associate Esc for exit
		String CLOSE_ACTION_KEY = "closeAction";
		Action closeAction = new AbstractAction() {
		      public void actionPerformed(ActionEvent actionEvent) {
		        cmdCloseActionPerformed();
		      }
		    };
	    KeyStroke esc = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
		InputMap closeInputMap = cmdClose.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
		closeInputMap.put(esc, CLOSE_ACTION_KEY);
		ActionMap closeActionMap = cmdClose.getActionMap();
		closeActionMap.put(CLOSE_ACTION_KEY, closeAction);
		cmdClose.setActionMap(closeActionMap);
	}
	
	// function to load existing stator types
	private void loadExistingRecords() {
		// open db and read existing stator types
		try {
			conn = DriverManager.getConnection(Configuration.DB_URL);
			stmt = conn.createStatement();
			stmt.setQueryTimeout(30);
			
			tblTypeList.setModel(new DefaultTableModel(
					new Object[][] {
					},
					new String[] {
						null
					}
				) {
					boolean[] columnEditable = new boolean[] {
						false
					};
					@Override
					public boolean isCellEditable(int rowIndex, int columnIndex) {
						return columnEditable[columnIndex];
					}
				});
			DefaultTableModel defModel = (DefaultTableModel) tblTypeList.getModel();
			
			// fetch and load the result
			String filterText = txtSearch.getText().trim().isEmpty() ? "" : " where lower(type) like '%" + txtSearch.getText().trim().toLowerCase() + "%'";
			ResultSet res = stmt.executeQuery("select * from " + Configuration.STATOR_TYPE + filterText +  " order by type");

			if (res != null) {
				int curRow = -1;
				while (res.next()) {
					// add the stator type into the grid and ignore its features
					++curRow;
					defModel.addRow( new Object[] {null});
					tblTypeList.setValueAt(res.getString("type"), curRow, 0);
					if (mainFormRef.curStatorType.equals(tblTypeList.getValueAt(curRow, 0))) {
						defSelRow = curRow;
					}
				}
				res.close();
			}
			
		} catch (Exception sqle) {
			JOptionPane.showMessageDialog(this, "Error reading existing stator types:" + sqle.getMessage());
			return;
		}
	}
	
	// CUSTOM FUNCTIONS - END
	
	private void cmdAddActionPerformed() {
		// add the new type
		String curType = txtType.getText().trim();
		if ( curType.isEmpty()) {
			JOptionPane.showMessageDialog(this, "Please enter valid stator type to add");
			txtType.requestFocusInWindow();
			return;
		}

		// check for duplication
		for(int i=0; i<tblTypeList.getRowCount(); i++) {
			if (curType.equals(tblTypeList.getValueAt(i, 0))) {
				JOptionPane.showMessageDialog(this, "Stator '" + curType + "' already exist. Please use different stator type and try again");
				return;
			}
		}
		
		String curPhase = cmbPhase.getSelectedItem().toString();
		String curCon = cmbCon.getSelectedItem().toString();
		String curKw = txtKw.getText();
		String curHp = txtHp.getText();
		String curDir = cmbDir.getSelectedItem().toString();
		String curVenorRef = txtVendorRef.getText().trim();
		try {
			// no duplicate - so add it
			String curResL1 = txtResL1.getText().trim();;
			String curResH1 = txtResH1.getText().trim();
			String curResL2 = txtResL2.getText().trim();;
			String curResH2 = txtResH2.getText().trim();
			String curResL3 = txtResL3.getText().trim();;
			String curResH3 = txtResH3.getText().trim();
			String curInsResL = txtInsResL.getText().trim();
			String curMAH = txtMAH.getText().trim();
			try {
				Float.valueOf(curResL1);
				Float.valueOf(curResH1);
			} catch (NumberFormatException e) {
				JOptionPane.showMessageDialog(this, "Please enter valid numbers for Resitance Lower and Upper Limits");
				txtResL1.requestFocus();
				return;
			}
			// for single phase, additional resistances
			if (curPhase.equals("Single")) {
				try {
					Float.valueOf(curResL2);
					Float.valueOf(curResH2);
					Float.valueOf(curResL3);
					Float.valueOf(curResH3);
				} catch (NumberFormatException e) {
					JOptionPane.showMessageDialog(this, "Please enter valid numbers for Resitance Lower and Upper Limits");
					txtResL2.requestFocus();
					return;
				}
			} else {
				curResL2 = "";
				curResH2 = "";
				curResL3 = "";
				curResH3 = "";
			}
			try {
				Float.valueOf(curInsResL);
			} catch (NumberFormatException e) {
				JOptionPane.showMessageDialog(this, "Please enter valid number for Insulation Resistance Lower Limit");
				txtInsResL.requestFocus();
				return;
			}
			try {
				Float.valueOf(curMAH);
			} catch (NumberFormatException e) {
				JOptionPane.showMessageDialog(this, "Please enter valid number for mA Upper Limit");
				txtMAH.requestFocus();
				return;
			}
			
			// append into stator table
			stmt.executeUpdate("insert into " + Configuration.STATOR_TYPE + "(type, phase, conn, kw, hp, vendor_ref, res_low_limit1, res_up_limit1, "+
			"res_low_limit2, res_up_limit2, res_low_limit3, res_up_limit3, ins_res_low_limit, hv_ma_up_limit, dir) values ('" + curType + "','" + curPhase + "','" + curCon + "','" + curKw + "','" + curHp + "','" + curVenorRef + "','" + curResL1 + "','" + curResH1 + "'," + 
					"'" + curResL2 + "','" + curResH2 + "','" + curResL3 + "','" + curResH3 + "','" + curInsResL + "','" + curMAH + "','" + curDir + "')");
		
			// save error adjustment
			for(int curRow = 0; curRow < tblErrAdj.getRowCount(); curRow++) {
				stmt.executeUpdate("insert into " + Configuration.ERROR_ADJ + " values('" + curType +"','" + tblErrAdj.getValueAt(curRow, 0) +"','" + tblErrAdj.getValueAt(curRow, 1) + "', '" + tblErrAdj.getValueAt(curRow, 2) + "', '" + tblErrAdj.getValueAt(curRow, 3) + "', '" + tblErrAdj.getValueAt(curRow, 4) + "')"); 
			}
		} catch (Exception sqle) {
			JOptionPane.showMessageDialog(this, "Error inserting new stator type:" + sqle.getMessage());
			return;
		}
		
		// add into list
		DefaultTableModel defModel = (DefaultTableModel) tblTypeList.getModel();
		defModel.addRow( new Object[] {null});
		tblTypeList.setValueAt(curType, tblTypeList.getRowCount()-1, 0);
		tblTypeList.getSelectionModel().setSelectionInterval(tblTypeList.getRowCount()-1, tblTypeList.getRowCount()-1);
		
	}

	private void cmdUpdateActionPerformed() {
		// update the selected type
		String selType = null;
		if ( tblTypeList.getSelectedRow() < 0) {
			JOptionPane.showMessageDialog(this, "Please select an existing stator type from the list before updating it"); 
			return;
		}
		else {
			selType = tblTypeList.getValueAt(tblTypeList.getSelectedRow(), 0).toString();
		}
		
		String curType = txtType.getText().trim();
		if ( curType.isEmpty()) {
			JOptionPane.showMessageDialog(this, "Please enter a stator type before updating"); 
			return;
		}
		
		int response = JOptionPane.showConfirmDialog(this, "Do you want to update stator type '" + selType + "'?");
		if (response != 0) {
			JOptionPane.showMessageDialog(this, "Update is cancelled");
			return;
		}
		
		// check for readings 
		try {
			if (!selType.equals(curType)) {
				ResultSet res = stmt.executeQuery("select count(distinct(test_slno)) as tot from " + Configuration.READING_DETAIL + " where stator_type='" + selType + "'");
				if (res.next()) {
					if (res.getLong("tot") > 0) {
						response = JOptionPane.showConfirmDialog(this, "WARNING: " + res.getLong("tot") + " tests were performed for stator type '" + selType + "'\n" +
								"Updating the stator type will also update corresponding tests to '" + curType +"', do you want to continue?", "Warning", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);					
						if (response != 0) {
							JOptionPane.showMessageDialog(this, "Update is cancelled");
							res.close();
							return;
						}
					}
				}
				res.close();
			}
		
			String curPhase = cmbPhase.getSelectedItem().toString();
			String curCon = cmbCon.getSelectedItem().toString();
			String curKw = txtKw.getText();
			String curHp = txtHp.getText();
			String curDir = cmbDir.getSelectedItem().toString();
			String curVendorRef = txtVendorRef.getText().trim();
			
			// update the tables
			String curResL1 = txtResL1.getText().trim();;
			String curResH1 = txtResH1.getText().trim();
			String curResL2 = txtResL2.getText().trim();;
			String curResH2 = txtResH2.getText().trim();
			String curResL3 = txtResL3.getText().trim();;
			String curResH3 = txtResH3.getText().trim();
			String curInsResL = txtInsResL.getText().trim();
			String curMAH = txtMAH.getText().trim();
			
			try {
				Float.valueOf(curResL1);
				Float.valueOf(curResH1);
			} catch (NumberFormatException e) {
				JOptionPane.showMessageDialog(this, "Please enter valid numbers for Resitance Lower and Upper Limits");
				txtResL1.requestFocus();
				return;
			}
			// for single phase, additional resistances
			if (curPhase.equals("Single")) {
				try {
					Float.valueOf(curResL2);
					Float.valueOf(curResH2);
					Float.valueOf(curResL3);
					Float.valueOf(curResH3);
				} catch (NumberFormatException e) {
					JOptionPane.showMessageDialog(this, "Please enter valid numbers for Resitance Lower and Upper Limits");
					txtResL2.requestFocus();
					return;
				}
			} else {
				curResL2 = "";
				curResH2 = "";
				curResL3 = "";
				curResH3 = "";
			}
			try {
				Float.valueOf(curInsResL);
			} catch (NumberFormatException e) {
				JOptionPane.showMessageDialog(this, "Please enter valid number for Insulation Resistance Lower Limit");
				txtInsResL.requestFocus();
				return;
			}
			try {
				Float.valueOf(curMAH);
			} catch (NumberFormatException e) {
				JOptionPane.showMessageDialog(this, "Please enter valid number for mA Upper Limit");
				txtMAH.requestFocus();
				return;
			}
			
			stmt.executeUpdate("update " + Configuration.STATOR_TYPE + " set " + "type='" + curType + "', phase='" + curPhase + "', conn='" + curCon + "', kw='" + curKw + "', hp='" + curHp + "', vendor_ref='" + curVendorRef + "', res_low_limit1='" + curResL1 + "', res_up_limit1='" + curResH1 + "'," +
								"res_low_limit2='" + curResL2 + "', res_up_limit2='" + curResH2 + "', res_low_limit3='" + curResL3 + "', res_up_limit3='" + curResH3 + "', ins_res_low_limit='" + curInsResL + "', hv_ma_up_limit='" + curMAH + "', dir='" + curDir + "' " +
								"where type='" + selType + "'");
		
			// update detail tables in case of change of stator type
			if (!selType.equals(curType)) {
				stmt.executeUpdate("update " + Configuration.READING_DETAIL + " set stator_type='" + curType +"' where stator_type='" + selType +"'");
				stmt.executeUpdate("update " + Configuration.ERROR_ADJ + " set stator_type='" + curType +"' where stator_type='" + selType +"'");
			}
			// save error adjustment
			for(int curRow = 0; curRow < tblErrAdj.getRowCount(); curRow++) {
				stmt.executeUpdate("update " + Configuration.ERROR_ADJ + " set res_1='" + tblErrAdj.getValueAt(curRow, 1) + "', res_2='" + tblErrAdj.getValueAt(curRow, 2) + "', res_3='" + tblErrAdj.getValueAt(curRow, 3) + "', ins_res='" + tblErrAdj.getValueAt(curRow, 4) + "' where line='" + tblErrAdj.getValueAt(curRow, 0) +"' and stator_type ='" + curType +"'"); 
			}
		} catch (Exception sqle) {
			JOptionPane.showMessageDialog(this, "Error updating stator type:" + sqle.getMessage());
			return;
		}
		
		// update the list
		tblTypeList.setValueAt(curType, tblTypeList.getSelectedRow(), 0);
		
		// update the main form stator parameters if the stator just updated is the one already chosen in main form
		if (curType.equals(mainFormRef.curStatorType)) {
			mainFormRef.setStatorType(curType, false);
		}
		
	}

	private void cmdDelActionPerformed() {
		// delete the selected type
		String selType = null;
		if ( tblTypeList.getSelectedRow() < 0) {
			JOptionPane.showMessageDialog(this, "Please select an existing stator type from the list before deleting it"); 
			return;
		}
		else {
			selType = tblTypeList.getValueAt(tblTypeList.getSelectedRow(), 0).toString();
		}
	
		int response = JOptionPane.showConfirmDialog(this, "Do you want to delete stator type '" + selType + "'?");
		if (response != 0) {
			JOptionPane.showMessageDialog(this, "Delete is cancelled");
			return;
		}
		
		// check for readings 
		try {
			ResultSet res = stmt.executeQuery("select count(distinct(test_slno)) as tot from " + Configuration.READING_DETAIL + " where stator_type='" + selType + "'");
			if (res.next()) {
				if (res.getLong("tot") > 0) {
					response = JOptionPane.showConfirmDialog(this, "WARNING: " + res.getLong("tot") + " tests were performed for stator type '" + selType + "'\n" +
												"Deleting the stator type will also delete corresponding tests, do you want to continue?", "Warning", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE); 
					if (response != 0) {
						JOptionPane.showMessageDialog(this, "Delete is cancelled");
						res.close();
						return;
					}
				}
			}
			res.close();
			// delete master
			stmt.executeUpdate("delete from " + Configuration.STATOR_TYPE + " where type='" + selType + "'");
			// delete from related tables
			stmt.executeUpdate("delete from " + Configuration.ERROR_ADJ + " where stator_type='" + selType + "'");
			stmt.executeUpdate("delete from " + Configuration.READING_DETAIL + " where stator_type='" + selType + "'");
			
		} catch (Exception sqle) {
			JOptionPane.showMessageDialog(new JDialog(), "Error deleting the stator type:" + sqle.getMessage());
			return;
		}
		
		// update the list
		int selRow = tblTypeList.getSelectedRow();
		for(int i=tblTypeList.getSelectedRow(); i<tblTypeList.getRowCount()-1; i++) {
			tblTypeList.setValueAt(tblTypeList.getValueAt(i+1, 0), i, 0);	
		}
		// delete last row
		DefaultTableModel defModel = (DefaultTableModel) tblTypeList.getModel();
		defModel.removeRow(tblTypeList.getRowCount()-1);
		// focus previous row
		if (tblTypeList.getRowCount() > 0) {
			if (selRow == tblTypeList.getRowCount()) {
				tblTypeList.setRowSelectionInterval(selRow-1, selRow-1);
				txtType.setText(tblTypeList.getValueAt(selRow-1, 0).toString());
			} else {
				tblTypeList.setRowSelectionInterval(selRow, selRow);
				txtType.setText(tblTypeList.getValueAt(selRow, 0).toString());
			}
			if (selRow != 0) {
				tblTypeList.getSelectionModel().setSelectionInterval(selRow-1, selRow-1);
			}
		} else {
			txtType.setText("");
			cmbPhase.setSelectedIndex(0);
			cmbCon.setSelectedIndex(0);
			cmbDir.setSelectedIndex(0);
			txtKw.setText("");
			txtHp.setText("");
			txtResL1.setText("");
			txtResH1.setText("");
			txtResL2.setText("");
			txtResH2.setText("");
			txtResL3.setText("");
			txtResH3.setText("");
			txtInsResL.setText("");
			// clear error adj
			for(int i=0; i<tblErrAdj.getRowCount(); i++) {
				for(int j=1; i<tblErrAdj.getColumnCount(); j++) {
					if (j == 4) {
						tblErrAdj.setValueAt("1", i, j);
					} else {
						tblErrAdj.setValueAt("0.000", i, j);
					}
				}
			}
		}
	}

	private void cmdCloseActionPerformed() {
		thisWindowClosing(null);
		this.setVisible(false);
	}

	private void cmdChooseActionPerformed() {
		// delete the selected type
		if ( tblTypeList.getSelectedRow() < 0) {
			JOptionPane.showMessageDialog(this, "Please select an existing stator type from the list to choose it"); 
			return;
		}

		// set the stator type and features in main form 
		String curType = tblTypeList.getValueAt(tblTypeList.getSelectedRow(), 0).toString();
		if(!curType.equals(lastStatorType)) {
			mainFormRef.setStatorType(curType,true);
		}
		this.setVisible(false);
	}

	private void tblTypeListKeyPressed(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_ENTER) {
			cmdChooseActionPerformed();
		}
	}

	private void thisWindowClosing(WindowEvent e) {
		try {
			stmt.close();
			conn.close();
		} catch (SQLException se) {
			// just ignore it
		}
	}

	private void cmbPhaseActionPerformed() {
		if (cmbPhase.getSelectedItem().toString().equals("Single")) {
			lblResL1.setText("Starting Res. Lower Limit (Ohms)");
			lblResL2.setVisible(true);
			txtResL2.setVisible(true);
			txtResH2.setVisible(true);
			lblResU2.setVisible(true);
			lblResL3.setVisible(true);
			lblResL3.setVisible(true);
			txtResL3.setVisible(true);
			lblResU3.setVisible(true);
			txtResH3.setVisible(true);
			cmbCon.setEnabled(false);
		} else {
			lblResL1.setText("Res. Lower Limit (Ohms)");
			lblResL2.setVisible(false);
			txtResL2.setVisible(false);
			lblResU2.setVisible(false);
			txtResH2.setVisible(false);
			lblResL3.setVisible(false);
			txtResL3.setVisible(false);
			lblResU3.setVisible(false);
			txtResH3.setVisible(false);
			cmbCon.setEnabled(true);
		}
	}

	private void txtSearchKeyReleased() {
		loadExistingRecords();
	}

	private void txtKwKeyReleased() {
		// calculate HP
		if (!txtKw.getText().trim().isEmpty()) {
			Float tmpKw = 0F;
			String strKw = txtKw.getText();
			if (strKw.contains(" ")) {
				tmpKw = Float.valueOf(strKw.substring(0, strKw.indexOf(' ')));
			} else {
				tmpKw = Float.valueOf(strKw);
			}
			txtHp.setText(dotOne.format(tmpKw * 1.34102209));
		} 
	}
	
	class SelectionListener implements ListSelectionListener {
		  JTable table;
		  JDialog pr;

		  SelectionListener(JTable table, StatorType parent) {
		    this.table = table;
		    this.pr = parent;
		    
		  }
		  public void valueChanged(ListSelectionEvent le) {
			  try {
				  String selType = table.getValueAt(table.getSelectedRow(), 0).toString();
				  
			// fetch corresponding record for the table
				try {
					ResultSet res = stmt.executeQuery("select * from " + Configuration.STATOR_TYPE + " where type='" + selType + "'");
					
					if (res.next()) {
						txtType.setText(res.getString("type"));
						cmbPhase.setSelectedItem(res.getString("phase"));
						cmbCon.setSelectedItem(res.getString("conn"));
						cmbDir.setSelectedItem(res.getString("dir"));
						cmbPhaseActionPerformed();
						txtKw.setText(res.getString("kw"));
						txtHp.setText(res.getString("hp"));
						txtVendorRef.setText(res.getString("vendor_ref"));
						txtResL1.setText(res.getString("res_low_limit1"));
						txtResH1.setText(res.getString("res_up_limit1"));
						txtResL2.setText(res.getString("res_low_limit2"));
						txtResH2.setText(res.getString("res_up_limit2"));
						txtResL3.setText(res.getString("res_low_limit3"));
						txtResH3.setText(res.getString("res_up_limit3"));
						txtInsResL.setText(res.getString("ins_res_low_limit"));
						txtMAH.setText(res.getString("hv_ma_up_limit"));
						res.close();
						
						// load error adjustment
						res = stmt.executeQuery("select * from " + Configuration.ERROR_ADJ + " where stator_type = '" + selType +"'");
						DefaultTableModel defModel = (DefaultTableModel) tblErrAdj.getModel();
						
						if (res != null) {
							int curRow = 0;
							while (res.next()) {
								if (curRow > tblErrAdj.getRowCount()-1) {
									defModel.addRow( new Object[] {"","","","",""});
								}
									tblErrAdj.setValueAt(res.getString("line"), curRow, 0);
									tblErrAdj.setValueAt(res.getString("res_1"), curRow, 1);
									tblErrAdj.setValueAt(res.getString("res_2"), curRow, 2);
									tblErrAdj.setValueAt(res.getString("res_3"), curRow, 3);
									tblErrAdj.setValueAt(res.getString("ins_res"), curRow, 4);
									++curRow;
							}
							res.close();
						}
						// load
					}
				} catch (Exception sqle) {
					JOptionPane.showMessageDialog(new JDialog(), "Error reading the stator type:" + sqle.getMessage());
					return;
				}
			  } catch (ArrayIndexOutOfBoundsException ex) {
				  // just ignore it
			  }
		  }
	}
	
	private void initComponents() {
		// JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
		panel1 = new JPanel();
		label2 = new JLabel();
		txtSearch = new JTextField();
		scrlTypeList = new JScrollPane();
		tblTypeList = new JTable();
		cmdChoose = new JButton();
		pnlPump = new JPanel();
		label1 = new JLabel();
		txtType = new JTextField();
		pnlParam = new JPanel();
		pnlInsRes = new JPanel();
		lblResL1 = new JLabel();
		txtResL1 = new JTextField();
		lblResU1 = new JLabel();
		txtResH1 = new JTextField();
		lblResL2 = new JLabel();
		txtResL2 = new JTextField();
		lblResU2 = new JLabel();
		txtResH2 = new JTextField();
		lblResL3 = new JLabel();
		txtResL3 = new JTextField();
		lblResU3 = new JLabel();
		txtResH3 = new JTextField();
		pnlInsRes2 = new JPanel();
		lblResL4 = new JLabel();
		txtInsResL = new JTextField();
		cmdAdd = new JButton();
		cmdClose = new JButton();
		pnlFeat = new JPanel();
		label4 = new JLabel();
		cmbPhase = new JComboBox();
		label85 = new JLabel();
		txtKw = new JTextField();
		label86 = new JLabel();
		txtHp = new JTextField();
		label5 = new JLabel();
		cmbCon = new JComboBox();
		label6 = new JLabel();
		cmbDir = new JComboBox<>();
		label7 = new JLabel();
		txtVendorRef = new JTextField();
		pnlInsRes3 = new JPanel();
		lblResL6 = new JLabel();
		txtMAH = new JTextField();
		pnlErrAdj = new JPanel();
		scrollPane1 = new JScrollPane();
		tblErrAdj = new JTable();
		cmdUpdate = new JButton();
		cmdDel = new JButton();

		//======== this ========
		setTitle("Doer StatorView: Choose Stator");
		setFont(new Font("Arial", Font.PLAIN, 12));
		setModal(true);
		setResizable(false);
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				thisWindowClosing(e);
			}
		});
		Container contentPane = getContentPane();
		contentPane.setLayout(new TableLayout(new double[][] {
			{10, 325, 546, 10},
			{10, TableLayout.FILL, 10}}));
		((TableLayout)contentPane.getLayout()).setHGap(5);
		((TableLayout)contentPane.getLayout()).setVGap(5);

		//======== panel1 ========
		{
			panel1.setBorder(new TitledBorder(null, "Existing Stator Models [All Assembly Lines]", TitledBorder.LEADING, TitledBorder.TOP,
				new Font("Arial", Font.PLAIN, 16), new Color(0x006699)));
			panel1.setFocusable(false);
			panel1.setLayout(new TableLayout(new double[][] {
				{TableLayout.PREFERRED, TableLayout.FILL},
				{TableLayout.PREFERRED, TableLayout.FILL, TableLayout.PREFERRED}}));
			((TableLayout)panel1.getLayout()).setHGap(5);
			((TableLayout)panel1.getLayout()).setVGap(5);

			//---- label2 ----
			label2.setText("Search Model");
			label2.setFont(new Font("Arial", Font.PLAIN, 14));
			panel1.add(label2, new TableLayoutConstraints(0, 0, 0, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

			//---- txtSearch ----
			txtSearch.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
			txtSearch.setBackground(Color.white);
			txtSearch.addKeyListener(new KeyAdapter() {
				@Override
				public void keyReleased(KeyEvent e) {
					txtSearchKeyReleased();
				}
			});
			panel1.add(txtSearch, new TableLayoutConstraints(1, 0, 1, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

			//======== scrlTypeList ========
			{
				scrlTypeList.setToolTipText("List of existing motor types");

				//---- tblTypeList ----
				tblTypeList.setModel(new DefaultTableModel(
					new Object[][] {
					},
					new String[] {
						null
					}
				) {
					boolean[] columnEditable = new boolean[] {
						false
					};
					@Override
					public boolean isCellEditable(int rowIndex, int columnIndex) {
						return columnEditable[columnIndex];
					}
				});
				{
					TableColumnModel cm = tblTypeList.getColumnModel();
					cm.getColumn(0).setResizable(false);
				}
				tblTypeList.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
				tblTypeList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
				tblTypeList.addKeyListener(new KeyAdapter() {
					@Override
					public void keyPressed(KeyEvent e) {
						tblTypeListKeyPressed(e);
					}
				});
				scrlTypeList.setViewportView(tblTypeList);
			}
			panel1.add(scrlTypeList, new TableLayoutConstraints(0, 1, 1, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

			//---- cmdChoose ----
			cmdChoose.setText("<html>Choose Selected Stator&nbsp;&nbsp<font size=-2>[Enter]</html>");
			cmdChoose.setFont(new Font("Arial", Font.PLAIN, 16));
			cmdChoose.setIcon(new ImageIcon(getClass().getResource("/img/choose.PNG")));
			cmdChoose.setToolTipText("Click on this to choose the selected stator for your test");
			cmdChoose.setRolloverSelectedIcon(null);
			cmdChoose.setDefaultCapable(false);
			cmdChoose.addActionListener(e -> cmdChooseActionPerformed());
			panel1.add(cmdChoose, new TableLayoutConstraints(0, 2, 1, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
		}
		contentPane.add(panel1, new TableLayoutConstraints(1, 1, 1, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

		//======== pnlPump ========
		{
			pnlPump.setBorder(new TitledBorder(null, "Stator Details", TitledBorder.LEADING, TitledBorder.TOP,
				new Font("Arial", Font.PLAIN, 16), new Color(0x006699)));
			pnlPump.setFocusable(false);
			pnlPump.setLayout(new TableLayout(new double[][] {
				{TableLayout.FILL, TableLayout.FILL, TableLayout.FILL, TableLayout.FILL},
				{35, TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.PREFERRED}}));
			((TableLayout)pnlPump.getLayout()).setHGap(5);
			((TableLayout)pnlPump.getLayout()).setVGap(5);

			//---- label1 ----
			label1.setText("Stator Model");
			label1.setFont(new Font("Arial", Font.PLAIN, 16));
			pnlPump.add(label1, new TableLayoutConstraints(0, 0, 0, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

			//---- txtType ----
			txtType.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 16));
			txtType.setToolTipText("Enter the text that how you name your stator");
			pnlPump.add(txtType, new TableLayoutConstraints(1, 0, 3, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

			//======== pnlParam ========
			{
				pnlParam.setBorder(new TitledBorder(null, "Test Reading Limits", TitledBorder.LEADING, TitledBorder.DEFAULT_POSITION,
					new Font("Arial", Font.PLAIN, 14), new Color(0x006699)));
				pnlParam.setLayout(new TableLayout(new double[][] {
					{TableLayout.FILL},
					{TableLayout.PREFERRED, TableLayout.PREFERRED}}));
				((TableLayout)pnlParam.getLayout()).setHGap(5);
				((TableLayout)pnlParam.getLayout()).setVGap(5);

				//======== pnlInsRes ========
				{
					pnlInsRes.setBorder(new TitledBorder(null, "Resistance", TitledBorder.LEADING, TitledBorder.DEFAULT_POSITION,
						new Font("Arial", Font.PLAIN, 14), new Color(0x006699)));
					pnlInsRes.setLayout(new TableLayout(new double[][] {
						{TableLayout.PREFERRED, 75, 135, 75},
						{TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.PREFERRED}}));
					((TableLayout)pnlInsRes.getLayout()).setHGap(5);
					((TableLayout)pnlInsRes.getLayout()).setVGap(2);

					//---- lblResL1 ----
					lblResL1.setText("Starting Res. Lower Limit (Ohms)");
					lblResL1.setFont(new Font("Arial", Font.PLAIN, 14));
					pnlInsRes.add(lblResL1, new TableLayoutConstraints(0, 0, 0, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.CENTER));

					//---- txtResL1 ----
					txtResL1.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
					txtResL1.setBackground(Color.white);
					pnlInsRes.add(txtResL1, new TableLayoutConstraints(1, 0, 1, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

					//---- lblResU1 ----
					lblResU1.setText("Upper Limit (Ohms)");
					lblResU1.setFont(new Font("Arial", Font.PLAIN, 14));
					pnlInsRes.add(lblResU1, new TableLayoutConstraints(2, 0, 2, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.CENTER));

					//---- txtResH1 ----
					txtResH1.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
					pnlInsRes.add(txtResH1, new TableLayoutConstraints(3, 0, 3, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

					//---- lblResL2 ----
					lblResL2.setText("Running Res. Lower Limit (Ohms)");
					lblResL2.setFont(new Font("Arial", Font.PLAIN, 14));
					pnlInsRes.add(lblResL2, new TableLayoutConstraints(0, 1, 0, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.CENTER));

					//---- txtResL2 ----
					txtResL2.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
					txtResL2.setBackground(Color.white);
					pnlInsRes.add(txtResL2, new TableLayoutConstraints(1, 1, 1, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

					//---- lblResU2 ----
					lblResU2.setText("Upper Limit (Ohms)");
					lblResU2.setFont(new Font("Arial", Font.PLAIN, 14));
					pnlInsRes.add(lblResU2, new TableLayoutConstraints(2, 1, 2, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.CENTER));

					//---- txtResH2 ----
					txtResH2.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
					pnlInsRes.add(txtResH2, new TableLayoutConstraints(3, 1, 3, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

					//---- lblResL3 ----
					lblResL3.setText("Common Res. Lower Limit (Ohms)");
					lblResL3.setFont(new Font("Arial", Font.PLAIN, 14));
					pnlInsRes.add(lblResL3, new TableLayoutConstraints(0, 2, 0, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.CENTER));

					//---- txtResL3 ----
					txtResL3.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
					txtResL3.setBackground(Color.white);
					pnlInsRes.add(txtResL3, new TableLayoutConstraints(1, 2, 1, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

					//---- lblResU3 ----
					lblResU3.setText("Upper Limit (Ohms)");
					lblResU3.setFont(new Font("Arial", Font.PLAIN, 14));
					pnlInsRes.add(lblResU3, new TableLayoutConstraints(2, 2, 2, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.CENTER));

					//---- txtResH3 ----
					txtResH3.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
					pnlInsRes.add(txtResH3, new TableLayoutConstraints(3, 2, 3, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
				}
				pnlParam.add(pnlInsRes, new TableLayoutConstraints(0, 0, 0, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//======== pnlInsRes2 ========
				{
					pnlInsRes2.setBorder(new TitledBorder(null, "Ins. Resistance", TitledBorder.LEADING, TitledBorder.DEFAULT_POSITION,
						new Font("Arial", Font.PLAIN, 14), new Color(0x006699)));
					pnlInsRes2.setLayout(new TableLayout(new double[][] {
						{214, 75},
						{TableLayout.PREFERRED}}));
					((TableLayout)pnlInsRes2.getLayout()).setHGap(5);
					((TableLayout)pnlInsRes2.getLayout()).setVGap(2);

					//---- lblResL4 ----
					lblResL4.setText("Ins. Res. Lower Limit (MOhms)");
					lblResL4.setFont(new Font("Arial", Font.PLAIN, 14));
					pnlInsRes2.add(lblResL4, new TableLayoutConstraints(0, 0, 0, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.CENTER));

					//---- txtInsResL ----
					txtInsResL.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
					txtInsResL.setBackground(Color.white);
					pnlInsRes2.add(txtInsResL, new TableLayoutConstraints(1, 0, 1, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
				}
				pnlParam.add(pnlInsRes2, new TableLayoutConstraints(0, 1, 0, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
			}
			pnlPump.add(pnlParam, new TableLayoutConstraints(0, 2, 3, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

			//---- cmdAdd ----
			cmdAdd.setText("Add");
			cmdAdd.setFont(new Font("Arial", Font.PLAIN, 16));
			cmdAdd.setIcon(new ImageIcon(getClass().getResource("/img/Add.png")));
			cmdAdd.setToolTipText("Click on this to add new stator model and its features that you have entered above");
			cmdAdd.setMnemonic('A');
			cmdAdd.setMargin(new Insets(2, 5, 2, 5));
			cmdAdd.addActionListener(e -> cmdAddActionPerformed());
			pnlPump.add(cmdAdd, new TableLayoutConstraints(0, 5, 0, 5, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

			//---- cmdClose ----
			cmdClose.setText("<html>Close&nbsp;&nbsp<font size=-2>[Esc]</html>");
			cmdClose.setFont(new Font("Arial", Font.PLAIN, 16));
			cmdClose.setIcon(new ImageIcon(getClass().getResource("/img/exit.PNG")));
			cmdClose.setToolTipText("Click on this to close this window");
			cmdClose.setMargin(new Insets(2, 5, 2, 5));
			cmdClose.addActionListener(e -> cmdCloseActionPerformed());
			pnlPump.add(cmdClose, new TableLayoutConstraints(3, 5, 3, 5, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

			//======== pnlFeat ========
			{
				pnlFeat.setBorder(new TitledBorder(null, "Features", TitledBorder.LEADING, TitledBorder.DEFAULT_POSITION,
					new Font("Arial", Font.PLAIN, 14), new Color(0x006699)));
				pnlFeat.setLayout(new TableLayout(new double[][] {
					{122, 172, TableLayout.PREFERRED, 75, 30, TableLayout.FILL},
					{TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.PREFERRED}}));
				((TableLayout)pnlFeat.getLayout()).setHGap(5);
				((TableLayout)pnlFeat.getLayout()).setVGap(5);

				//---- label4 ----
				label4.setText("Phase");
				label4.setFont(new Font("Arial", Font.PLAIN, 14));
				pnlFeat.add(label4, new TableLayoutConstraints(0, 0, 0, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.CENTER));

				//---- cmbPhase ----
				cmbPhase.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
				cmbPhase.addActionListener(e -> cmbPhaseActionPerformed());
				pnlFeat.add(cmbPhase, new TableLayoutConstraints(1, 0, 1, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- label85 ----
				label85.setText("KW");
				label85.setFont(new Font("Arial", Font.PLAIN, 14));
				pnlFeat.add(label85, new TableLayoutConstraints(2, 0, 2, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.CENTER));

				//---- txtKw ----
				txtKw.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
				txtKw.addKeyListener(new KeyAdapter() {
					@Override
					public void keyReleased(KeyEvent e) {
						txtKwKeyReleased();
					}
				});
				pnlFeat.add(txtKw, new TableLayoutConstraints(3, 0, 3, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- label86 ----
				label86.setText("HP");
				label86.setFont(new Font("Arial", Font.PLAIN, 14));
				pnlFeat.add(label86, new TableLayoutConstraints(4, 0, 4, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.CENTER));

				//---- txtHp ----
				txtHp.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
				pnlFeat.add(txtHp, new TableLayoutConstraints(5, 0, 5, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- label5 ----
				label5.setText("Connection");
				label5.setFont(new Font("Arial", Font.PLAIN, 14));
				pnlFeat.add(label5, new TableLayoutConstraints(0, 1, 0, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.CENTER));

				//---- cmbCon ----
				cmbCon.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
				pnlFeat.add(cmbCon, new TableLayoutConstraints(1, 1, 1, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- label6 ----
				label6.setText("Direction");
				label6.setFont(new Font("Arial", Font.PLAIN, 14));
				pnlFeat.add(label6, new TableLayoutConstraints(2, 1, 2, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.CENTER));

				//---- cmbDir ----
				cmbDir.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
				cmbDir.setModel(new DefaultComboBoxModel<>(new String[] {
					"FWD",
					"REV"
				}));
				pnlFeat.add(cmbDir, new TableLayoutConstraints(3, 1, 3, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- label7 ----
				label7.setText("Vendor Reference (If needed to be printed in QR sticker)");
				label7.setFont(new Font("Arial", Font.PLAIN, 14));
				pnlFeat.add(label7, new TableLayoutConstraints(0, 2, 2, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.CENTER));

				//---- txtVendorRef ----
				txtVendorRef.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
				txtVendorRef.addKeyListener(new KeyAdapter() {
					@Override
					public void keyReleased(KeyEvent e) {
						txtKwKeyReleased();
					}
				});
				pnlFeat.add(txtVendorRef, new TableLayoutConstraints(3, 2, 5, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
			}
			pnlPump.add(pnlFeat, new TableLayoutConstraints(0, 1, 3, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

			//======== pnlInsRes3 ========
			{
				pnlInsRes3.setBorder(new TitledBorder(null, "High Voltage", TitledBorder.LEADING, TitledBorder.DEFAULT_POSITION,
					new Font("Arial", Font.PLAIN, 14), new Color(0x006699)));
				pnlInsRes3.setLayout(new TableLayout(new double[][] {
					{219, 75, 135, 75},
					{TableLayout.PREFERRED}}));
				((TableLayout)pnlInsRes3.getLayout()).setHGap(5);
				((TableLayout)pnlInsRes3.getLayout()).setVGap(2);

				//---- lblResL6 ----
				lblResL6.setText("mA Upper Limit (mA)");
				lblResL6.setFont(new Font("Arial", Font.PLAIN, 14));
				pnlInsRes3.add(lblResL6, new TableLayoutConstraints(0, 0, 0, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.CENTER));

				//---- txtMAH ----
				txtMAH.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
				txtMAH.setBackground(Color.white);
				pnlInsRes3.add(txtMAH, new TableLayoutConstraints(1, 0, 1, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
			}
			pnlPump.add(pnlInsRes3, new TableLayoutConstraints(0, 3, 3, 3, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

			//======== pnlErrAdj ========
			{
				pnlErrAdj.setBorder(new TitledBorder(null, "Error Adjustment", TitledBorder.LEADING, TitledBorder.DEFAULT_POSITION,
					new Font("Arial", Font.PLAIN, 14), new Color(0x006699)));
				pnlErrAdj.setLayout(new TableLayout(new double[][] {
					{TableLayout.FILL},
					{107}}));
				((TableLayout)pnlErrAdj.getLayout()).setHGap(5);
				((TableLayout)pnlErrAdj.getLayout()).setVGap(2);

				//======== scrollPane1 ========
				{

					//---- tblErrAdj ----
					tblErrAdj.setModel(new DefaultTableModel(
						new Object[][] {
						},
						new String[] {
							"Line", "<html><body align='center'>Resistance 1<br>\u00b1 \u03a9</br></html>", "<html><body align='center'>Resistance 2<br>\u00b1 \u03a9</br></html>", "<html><body align='center'>Resistance 3<br>\u00b1 \u03a9</br></html>", "<html><body align='center'>Ins. Resistance<br>X M\u03a9</br></html>"
						}
					) {
						Class<?>[] columnTypes = new Class<?>[] {
							String.class, String.class, String.class, String.class, String.class
						};
						boolean[] columnEditable = new boolean[] {
							false, true, true, true, true
						};
						@Override
						public Class<?> getColumnClass(int columnIndex) {
							return columnTypes[columnIndex];
						}
						@Override
						public boolean isCellEditable(int rowIndex, int columnIndex) {
							return columnEditable[columnIndex];
						}
					});
					{
						TableColumnModel cm = tblErrAdj.getColumnModel();
						cm.getColumn(0).setResizable(false);
						cm.getColumn(1).setResizable(false);
						cm.getColumn(2).setResizable(false);
						cm.getColumn(3).setResizable(false);
						cm.getColumn(4).setResizable(false);
					}
					tblErrAdj.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
					scrollPane1.setViewportView(tblErrAdj);
				}
				pnlErrAdj.add(scrollPane1, new TableLayoutConstraints(0, 0, 0, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
			}
			pnlPump.add(pnlErrAdj, new TableLayoutConstraints(0, 4, 3, 4, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

			//---- cmdUpdate ----
			cmdUpdate.setText("Update");
			cmdUpdate.setFont(new Font("Arial", Font.PLAIN, 16));
			cmdUpdate.setIcon(new ImageIcon(getClass().getResource("/img/save.PNG")));
			cmdUpdate.setToolTipText("Click on this to update selected stator model with the details you have entered above");
			cmdUpdate.setMnemonic('U');
			cmdUpdate.setMargin(new Insets(2, 5, 2, 5));
			cmdUpdate.addActionListener(e -> cmdUpdateActionPerformed());
			pnlPump.add(cmdUpdate, new TableLayoutConstraints(1, 5, 1, 5, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

			//---- cmdDel ----
			cmdDel.setText("Delete");
			cmdDel.setFont(new Font("Arial", Font.PLAIN, 16));
			cmdDel.setIcon(new ImageIcon(getClass().getResource("/img/delete.PNG")));
			cmdDel.setToolTipText("Click on this to delete the selected stator model");
			cmdDel.setMnemonic('D');
			cmdDel.setMargin(new Insets(2, 5, 2, 5));
			cmdDel.addActionListener(e -> cmdDelActionPerformed());
			pnlPump.add(cmdDel, new TableLayoutConstraints(2, 5, 2, 5, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
		}
		contentPane.add(pnlPump, new TableLayoutConstraints(2, 1, 2, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
		pack();
		setLocationRelativeTo(getOwner());
		// JFormDesigner - End of component initialization  //GEN-END:initComponents
	}

	// JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
	private JPanel panel1;
	private JLabel label2;
	private JTextField txtSearch;
	private JScrollPane scrlTypeList;
	private JTable tblTypeList;
	private JButton cmdChoose;
	private JPanel pnlPump;
	private JLabel label1;
	private JTextField txtType;
	private JPanel pnlParam;
	private JPanel pnlInsRes;
	private JLabel lblResL1;
	private JTextField txtResL1;
	private JLabel lblResU1;
	private JTextField txtResH1;
	private JLabel lblResL2;
	private JTextField txtResL2;
	private JLabel lblResU2;
	private JTextField txtResH2;
	private JLabel lblResL3;
	private JTextField txtResL3;
	private JLabel lblResU3;
	private JTextField txtResH3;
	private JPanel pnlInsRes2;
	private JLabel lblResL4;
	private JTextField txtInsResL;
	private JButton cmdAdd;
	private JButton cmdClose;
	private JPanel pnlFeat;
	private JLabel label4;
	private JComboBox cmbPhase;
	private JLabel label85;
	private JTextField txtKw;
	private JLabel label86;
	private JTextField txtHp;
	private JLabel label5;
	private JComboBox cmbCon;
	private JLabel label6;
	private JComboBox<String> cmbDir;
	private JLabel label7;
	private JTextField txtVendorRef;
	private JPanel pnlInsRes3;
	private JLabel lblResL6;
	private JTextField txtMAH;
	private JPanel pnlErrAdj;
	private JScrollPane scrollPane1;
	private JTable tblErrAdj;
	private JButton cmdUpdate;
	private JButton cmdDel;
	// JFormDesigner - End of variables declaration  //GEN-END:variables

	// CUSTOM CODE - BEGIN
	private void customInit() {
		// CUSTOM CODE - BEGIN
		tblTypeList.setTableHeader(null);
		tblTypeList.setRowHeight(20);
		SelectionListener listener = new SelectionListener(tblTypeList, this);
		tblTypeList.getSelectionModel().addListSelectionListener(listener);
		tblTypeList.getColumnModel().getSelectionModel().addListSelectionListener(listener);
		scrlTypeList.setViewportView(tblTypeList);
		
		// default phase
		cmbPhase.addItem("Single");
		cmbPhase.addItem("Three");
		cmbPhase.setSelectedIndex(0);
		
		// default connections
		cmbCon.addItem("");
		cmbCon.addItem("Star");
		cmbCon.addItem("Star Delta");
		cmbCon.setSelectedIndex(0);
		cmbDir.setSelectedIndex(0);
		
		// disable buttons if user does not have motor access
		if (Configuration.USER_HAS_STATOR_ACCESS.equals("0")) {
			cmdAdd.setEnabled(false);
			cmdUpdate.setEnabled(false);
			cmdDel.setEnabled(false);
		}
		
		associateFunctionKeys();
	}
	// CUSTOM CODE - END

}
