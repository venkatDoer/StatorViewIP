/*
 * Created by JFormDesigner on Fri Jan 13 13:27:06 EST 2012
 */

package doer.sv;

import javax.swing.*;
import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstraints;
import jxl.Workbook;
import jxl.format.Alignment;
import jxl.write.Label;
import jxl.write.Number;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.print.PrinterException;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;

import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;

import com.toedter.calendar.JDateChooser;

import doer.io.Parameter;
import doer.print.PrintUtility;

/**
 * @author VENKATESAN SELVARAJ
 */
public class Reports extends JDialog {
	public Reports(Frame owner, String curStator, String recentStat, Calendar recentDt) {
		super(owner);
		curStatorType = curStator;
		recSlNo = recentStat;
		recDt = recentDt;
		initInProgress = true;
		initComponents();
		customInit();
	}


	private void cmdCloseActionPerformed() {
		thisWindowClosing();
		this.setVisible(false);
	}

	private void cmdPrintActionPerformed() {
		// print current page
		try {
			
			PrintUtility pu = new PrintUtility();
			if (!pu.initialize()) {
				return;
			}
			
			PrintProgressDlg prgDlg = new PrintProgressDlg();
			
			prgDlg.showMessage("Printing current page");
			if (prgDlg.getCancelPrint()) {
				JOptionPane.showMessageDialog(this, "Print aborted!");
				return;
			}
			
			// else continue with printing
			
			switch(tabReport.getSelectedIndex()) {
			case 0:
				pu.printComponent(pnlPrint);
				break;
			}
			
			JOptionPane.showMessageDialog(this, "Print completed");
				
		} catch (PrinterException e) {
			JOptionPane.showMessageDialog(this, "Printer error occured while printing this document.\nActual Error:" + e.getMessage());
		}
	}

	private void cmdRefreshActionPerformed() {
		if (tblTest.getRowCount() == 0) {
			JOptionPane.showMessageDialog(this, "No records found to preview.\nChange your filter criteria and try again.");
			clearTable();
			return;
		}
		// construct test list
		String tmpstatorList = "";
		statorList.clear();
		curPage = 1;
		int j = 0;
		for (int i=0; i<tblTest.getRowCount(); i++) {
			if (tblTest.getValueAt(i, 0).toString().equals("true")) {
				tmpstatorList += "'" + tblTest.getValueAt(i, 1) + "',";
				statorList.put(j++,  tblTest.getValueAt(i, 1).toString());
			}
		}
		
		if (statorList.isEmpty()) {
			clearTable();
			JOptionPane.showMessageDialog(this, "No stators selected to preview.\nSelect any stators and try again.");
			return;
		}
		
		tmpstatorList = tmpstatorList.substring(0,tmpstatorList.length()-1);
		statorFilterText = filterText + " and stator_slno in(" + tmpstatorList + ")";
		
		// refresh header
		setReportHeader();
		
		// refresh current tab
		tabReport.requestFocusInWindow();
		tabReportStateChanged();
	}

	private void cmdNextActionPerformed() {
		switch(tabReport.getSelectedIndex()) {
		case 0: // register report
			gotoPageRegRep(++curPageRegRep);
			break;
		}
	}

	private void cmdPrevActionPerformed() {
		switch(tabReport.getSelectedIndex()) {
		case 0: // register report
			gotoPageRegRep(--curPageRegRep);
			break;
		}
	}

	private void cmdFirstActionPerformed() {
		curPage = 1;
		switch(tabReport.getSelectedIndex()) {
		case 0: // register report
			curPageRegRep = 1;
			gotoPageRegRep(curPageRegRep);
			break;
		}
	}

	private void cmdLastActionPerformed() {
		curPage = totAvailPages;
		switch(tabReport.getSelectedIndex()) {
		case 0: // register report
			curPageRegRep = totAvailPagesRegRep;
			gotoPageRegRep(curPageRegRep);
			break;
		}
	}
	
	private void cmdPrintAllActionPerformed() {
		// print all pages by going through one by one and reset the user to the page where they were
		try {
			
			PrintUtility pu = new PrintUtility();
			if (!pu.initialize()) {
				return;
			}
			
			PrintProgressDlg prgDlg = new PrintProgressDlg();
			
			switch(tabReport.getSelectedIndex()) {
			case 0:
				for(int i=1; i<=totAvailPagesRegRep; i++) {
					gotoPageRegRep(i);
					
					prgDlg.showMessage("Printing page " + i + " of " + totAvailPagesRegRep);
					if (prgDlg.getCancelPrint()) {
						break;
					} else {
						pu.printComponent(pnlPrint);
					}
				}
				gotoPageRegRep(curPageRegRep); 
				break;
			}
			
			if (prgDlg.getCancelPrint()) {
				JOptionPane.showMessageDialog(this, "Print aborted!");
			} else {
				JOptionPane.showMessageDialog(this, "Print completed");
			}
			
		} catch (PrinterException e) {
			JOptionPane.showMessageDialog(this, "Printer error occured while printing this document.\nActual Error:" + e.getMessage());
		}
	}

	private void cmbTypeActionPerformed() {
		if (initInProgress) {
			return;
		}
		curStatorType = cmbType.getSelectedItem().toString();
		refreshTestList();
	}

	private void cmbLineActionPerformed() {
		refreshTestList();
	}

	private void txtSlNoFromFocusLost() {
		refreshTestList();
	}

	private void txtSlNoToFocusLost() {
		refreshTestList();
	}

	private void fromDtPropertyChange() {
		refreshTestList();
	}

	private void toDtPropertyChange() {
		refreshTestList();
	}
	
	private void clearTable() {
		DefaultTableModel defModel = (DefaultTableModel) tblRes.getModel();
		while (tblRes.getRowCount() > 0) {
			defModel.removeRow(0);
		}
	}
	
	private void associateFunctionKeys() {
		// associate f5 for open
		String REFRESH_ACTION_KEY = "refreshAction";
		Action refreshAction = new AbstractAction() {
		      public void actionPerformed(ActionEvent actionEvent) {
		        cmdRefreshActionPerformed();
		      }
		    };
		KeyStroke f5 = KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0);
		InputMap refreshInputMap = cmdRefresh.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
		refreshInputMap.put(f5, REFRESH_ACTION_KEY);
		ActionMap refreshActionMap = cmdRefresh.getActionMap();
		refreshActionMap.put(REFRESH_ACTION_KEY, refreshAction);
		cmdRefresh.setActionMap(refreshActionMap);
		
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

	private void thisWindowClosing() {
		try {
			stmt.close();
			stmt2.close();
			conn.close();
		} catch (SQLException e) {
			// ignore
		}
	}

	private void optBothActionPerformed() {
		refreshTestList();
	}

	private void cmbChActionPerformed() {
		refreshTestList();
	}

	private void cmdSelActionPerformed() {
		for(int i=0; i<tblTest.getRowCount(); i++) {
			tblTest.setValueAt(true, i, 0);
		}
	}

	private void cmdClearActionPerformed() {
		for(int i=0; i<tblTest.getRowCount(); i++) {
			tblTest.setValueAt(false, i, 0);
		}
	}

	private void tabReportStateChanged() {
		if (initInProgress) {
			return;
		}
		refreshReport();
		enableDisableButtons();
	}

	private void cmbRemarksActionPerformed() {
		refreshTestList();
	}

	private void optPassActionPerformed() {
		refreshTestList();
	}

	private void optFailActionPerformed() {
		refreshTestList();
	}

	private void cmdExpActionPerformed() {
		// export current report as csv file
		SimpleDateFormat dtFormat = new SimpleDateFormat("yyyyMMddHms");
		String fileName = cmbType.getSelectedItem().toString().replaceAll("[\\s\\\\/\\.]+", "_") + "_" + dtFormat.format(new Date()) + ".xls";
		
		// prompt user to choose folder
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setSelectedFile(new File(fileName));
		if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
			File file = fileChooser.getSelectedFile();
			// save to file
			WritableWorkbook w = null;
			try {
				// excel workbook
				w = Workbook.createWorkbook(file);
				WritableSheet s = w.createSheet(file.getName(), 0);
				
				// file header
				WritableCellFormat cFormat = new WritableCellFormat();
				cFormat.setAlignment(Alignment.CENTRE);
				cFormat.setFont(new WritableFont(WritableFont.ARIAL, 10, WritableFont.BOLD));
				int c=0;
				s.addCell(new Label(c, 0, "Date", cFormat));
				s.mergeCells(c, 0, c, 1);
				++c;
				s.addCell(new Label(c, 0, "Test SNo.", cFormat));
				s.mergeCells(c, 0, c, 1);
				++c;
				s.addCell(new Label(c, 0, "Stator SNo.", cFormat));
				s.mergeCells(c, 0, c, 1);
				++c;
				s.addCell(new Label(c, 0, "Model", cFormat));
				s.mergeCells(c, 0, c, 1);
				++c;
				
				// testss
				s.addCell(new Label(c, 0, "Resistance", cFormat));
				if (lblPh.getText().equals("Single")) {
					s.addCell(new Label(c++, 1, "Starting (Ω)"));
					s.addCell(new Label(c++, 1, "Running (Ω)"));
					s.addCell(new Label(c++, 1, "Common (Ω)"));
				} else {
					if (cmbType.getSelectedIndex() == 0) {
						s.addCell(new Label(c++, 1, "R1 (Ω)"));
						s.addCell(new Label(c++, 1, "R2 (Ω)"));
						s.addCell(new Label(c++, 1, "R3 (Ω)"));
					} else {
						s.addCell(new Label(c++, 1, "R (Ω)"));
						s.addCell(new Label(c++, 1, "Y (Ω)"));
						s.addCell(new Label(c++, 1, "B (Ω)"));
					}
				}
				
				s.addCell(new Label(c++, 1, "Average (Ω)"));
				s.mergeCells(c-4, 0, c-1, 0);
				
				s.addCell(new Label(c, 0, "Temp. (°C)", cFormat));
				s.mergeCells(c, 0, c, 1);
				++c;
				
				s.addCell(new Label(c, 0, "IR Bef. HV (MΩ)", cFormat));
				s.mergeCells(c, 0, c, 1);
				++c;
				
				s.addCell(new Label(c, 0, "High Voltage Test", cFormat));
				s.addCell(new Label(c++, 1, "Voltage (kV)"));
				s.addCell(new Label(c++, 1, "Current (mA)"));
				s.mergeCells(c-2, 0, c-1, 0);
				
				s.addCell(new Label(c, 0, "IR Aft. HV (MΩ)", cFormat));
				s.mergeCells(c, 0, c, 1);
				++c;
				
				if (Configuration.IS_SURGE_DISABLED.equals("0")) {
					s.addCell(new Label(c, 0, "Surge", cFormat));
					s.mergeCells(c, 0, c, 1);
					++c;
				}
				
				s.addCell(new Label(c, 0, "Direction", cFormat));
				s.mergeCells(c, 0, c, 1);
				++c;
				
				// add result and remark column
				s.addCell(new Label(c, 0, "Result", cFormat));
				s.mergeCells(c, 0, c, 1);
				++c;
				
				s.addCell(new Label(c, 0, "Remark", cFormat));
				s.mergeCells(c, 0, c, 1);
				
				/* body */
				String lastDt = "";
				String lastSlno = "";
				String lastStatorSlno = "";
				String lastModel = "";
				String lastRes = "";
				String lastRem = "";
				
				int ir = 0;
				for(int idx=1; idx<=totAvailPagesRegRep; idx++) {
					gotoPageRegRep(idx);
					for(int i=0; i<tblRes.getRowCount(); i++) {
						for(int j=0; j<tblRes.getColumnCount(); j++) {
							// fixed values for single test
							if ((j > 0 && j < 4) || j >= tblRes.getColumnCount() - 2) {
								if (j == 1) {
									s.addCell(new Number(j, 2+ir,  Double.valueOf(lastSlno)));
								} else if (j == 2) {
									s.addCell(new Label(j, 2+ir, lastStatorSlno));
								} else if (j == 3) {
									s.addCell(new Label(j, 2+ir, lastModel));
								} else if (j == tblRes.getColumnCount() - 2) {
									s.addCell(new Label(j, 2+ir, lastRes));
								} else {
									s.addCell(new Label(j, 2+ir, lastRem));
								}
								continue;
							}
							if (j == 0 && !tblRes.getValueAt(i, j).toString().isEmpty()) {
								lastDt = tblRes.getValueAt(i, 0).toString();
								lastSlno = tblRes.getValueAt(i, 1).toString();
								lastStatorSlno = tblRes.getValueAt(i, 2).toString();
								lastModel = tblRes.getValueAt(i, 3).toString();
								try {
									lastRes = tblRes.getValueAt(i, tblRes.getColumnCount() - 2).toString();
								} catch (Exception e) {
									// ignore
								}
								try {
									lastRem = tblRes.getValueAt(i, tblRes.getColumnCount() - 1).toString();
								} catch (Exception e) {
									// ignore
								}
								s.addCell(new Label(j, 2+ir, lastDt));
							} else { // detail of test
								try {
									// skip surge waves (tbd: need to check whether it is working)
									if (Configuration.IS_SURGE_WAVE_CAPTURED.equals("1") && ((j >= 14 && j <= 17 && lblPh.getText().equals("Three")))) {
										continue;
									}
									s.addCell(new Number(j, 2+ir, Double.valueOf(tblRes.getValueAt(i, j).toString())));
								} catch (NullPointerException ce) {
									s.addCell(new Label(j, 2+ir, ""));
								} catch (Exception ce) {
									s.addCell(new Label(j, 2+ir, tblRes.getValueAt(i, j).toString()));
								}
							}
						}
						ir++;
					}
				}
				// go back to chosen page
				gotoPageRegRep(curPageRegRep); 
				
				w.write();
				JOptionPane.showMessageDialog(this, "Export completed");
			} catch (Exception e) {
				JOptionPane.showMessageDialog(this, "Error while exporting report:" + e.getMessage());
				e.printStackTrace();
			} finally {
				if (w != null) {
					try {
						w.close();
					} catch (Exception e) {
						JOptionPane.showMessageDialog(this, "Error while exporting report:" + e.getMessage());
					}
				}
			}
		}
	}

	private void cmbVendorRef() {
		refreshTestList();
	}

	private void initComponents() {
		// JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
		pnlOpt = new JPanel();
		pnlFilter = new JPanel();
		optPass = new JRadioButton();
		optFail = new JRadioButton();
		optBoth = new JRadioButton();
		cmdSel = new JButton();
		cmdClear = new JButton();
		scrlDev = new JScrollPane();
		tblTest = new JTable();
		cmdRefresh = new JButton();
		lblResult = new JLabel();
		pnlAdFilter = new JPanel();
		label11 = new JLabel();
		cmbType = new JComboBox();
		label12 = new JLabel();
		label13 = new JLabel();
		fromDt = new JDateChooser();
		label14 = new JLabel();
		toDt = new JDateChooser();
		label15 = new JLabel();
		label16 = new JLabel();
		txtSlNoFrom = new JTextField();
		label17 = new JLabel();
		txtSlNoTo = new JTextField();
		label20 = new JLabel();
		cmbRemarks = new JComboBox();
		label18 = new JLabel();
		cmbLine = new JComboBox();
		label22 = new JLabel();
		cmbVendorRef = new JComboBox();
		cmdExp = new JButton();
		cmdPrint = new JButton();
		cmdPrintAll = new JButton();
		cmdClose = new JButton();
		printContainer = new JPanel();
		pnlBut = new JPanel();
		cmdFirst = new JButton();
		cmdPrev = new JButton();
		cmdNext = new JButton();
		cmdLast = new JButton();
		tabReport = new JTabbedPane();
		scrollPane1 = new JScrollPane();
		printArea = new JPanel();
		pnlPrint = new JPanel();
		pnlRep = new JPanel();
		lblCustLogo = new JLabel();
		lblCompName = new JLabel();
		lblCompAdr = new JLabel();
		lblTitle = new JLabel();
		lblPage = new JLabel();
		pnlTop = new JPanel();
		label19 = new JLabel();
		lblType = new JLabel();
		label34 = new JLabel();
		lblPh = new JLabel();
		label35 = new JLabel();
		lblRat = new JLabel();
		pnlPrime = new JPanel();
		label122 = new JLabel();
		label228 = new JLabel();
		label5 = new JLabel();
		label8 = new JLabel();
		label229 = new JLabel();
		label230 = new JLabel();
		label2 = new JLabel();
		label1 = new JLabel();
		label7 = new JLabel();
		lbllblSurge = new JLabel();
		lbllblSurgeWave = new JLabel();
		lblStCl = new JLabel();
		lblRunCl = new JLabel();
		lblComCl = new JLabel();
		lblAveCl = new JLabel();
		label131 = new JLabel();
		label132 = new JLabel();
		label21 = new JLabel();
		label231 = new JLabel();
		label232 = new JLabel();
		label159 = new JLabel();
		label160 = new JLabel();
		label137 = new JLabel();
		label3 = new JLabel();
		label4 = new JLabel();
		label6 = new JLabel();
		label139 = new JLabel();
		label140 = new JLabel();
		label9 = new JLabel();
		tblRes = new JTable();
		pnlBot = new JPanel();
		lblTestedBy = new JLabel();
		label102 = new JLabel();
		lblVerBy14 = new JLabel();

		//======== this ========
		setTitle("Doer StatorView: Reports");
		setFont(new Font("Arial", Font.PLAIN, 12));
		setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
		setResizable(false);
		setMinimumSize(new Dimension(300, 300));
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				thisWindowClosing();
			}
		});
		Container contentPane = getContentPane();
		contentPane.setLayout(new TableLayout(new double[][] {
			{405, TableLayout.FILL},
			{TableLayout.FILL}}));
		((TableLayout)contentPane.getLayout()).setHGap(3);
		((TableLayout)contentPane.getLayout()).setVGap(5);

		//======== pnlOpt ========
		{
			pnlOpt.setBorder(new TitledBorder(null, "Print Options", TitledBorder.LEADING, TitledBorder.TOP,
				new Font("Arial", Font.PLAIN, 16), new Color(0x006699)));
			pnlOpt.setFocusable(false);
			pnlOpt.setLayout(new TableLayout(new double[][] {
				{TableLayout.FILL, TableLayout.FILL},
				{TableLayout.FILL, TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.PREFERRED}}));
			((TableLayout)pnlOpt.getLayout()).setHGap(5);
			((TableLayout)pnlOpt.getLayout()).setVGap(5);

			//======== pnlFilter ========
			{
				pnlFilter.setBorder(new TitledBorder(null, "Test List [Last one week tests are displayed by default]", TitledBorder.LEADING, TitledBorder.DEFAULT_POSITION,
					new Font("Arial", Font.PLAIN, 14), new Color(0x006699)));
				pnlFilter.setLayout(new TableLayout(new double[][] {
					{TableLayout.FILL, TableLayout.FILL, 95},
					{TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.FILL, TableLayout.PREFERRED}}));
				((TableLayout)pnlFilter.getLayout()).setHGap(5);
				((TableLayout)pnlFilter.getLayout()).setVGap(5);

				//---- optPass ----
				optPass.setText("PASSed Test Only");
				optPass.setFont(new Font("Arial", Font.PLAIN, 13));
				optPass.setMnemonic('P');
				optPass.setToolTipText("Show only passed tests");
				optPass.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						optPassActionPerformed();
					}
				});
				pnlFilter.add(optPass, new TableLayoutConstraints(0, 0, 0, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- optFail ----
				optFail.setText("FAILed Test Only");
				optFail.setFont(new Font("Arial", Font.PLAIN, 13));
				optFail.setMnemonic('F');
				optFail.setToolTipText("Show only failed tests");
				optFail.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						optFailActionPerformed();
					}
				});
				pnlFilter.add(optFail, new TableLayoutConstraints(1, 0, 1, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- optBoth ----
				optBoth.setText("All");
				optBoth.setFont(new Font("Arial", Font.PLAIN, 13));
				optBoth.setSelected(true);
				optBoth.setMnemonic('B');
				optBoth.setToolTipText("Show all tests");
				optBoth.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						optBothActionPerformed();
					}
				});
				pnlFilter.add(optBoth, new TableLayoutConstraints(2, 0, 2, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- cmdSel ----
				cmdSel.setText("Select All");
				cmdSel.setFont(new Font("Arial", Font.PLAIN, 13));
				cmdSel.setMnemonic('S');
				cmdSel.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						cmdSelActionPerformed();
					}
				});
				pnlFilter.add(cmdSel, new TableLayoutConstraints(0, 1, 0, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- cmdClear ----
				cmdClear.setText("Clear All");
				cmdClear.setFont(new Font("Arial", Font.PLAIN, 13));
				cmdClear.setMnemonic('C');
				cmdClear.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						cmdClearActionPerformed();
					}
				});
				pnlFilter.add(cmdClear, new TableLayoutConstraints(1, 1, 1, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//======== scrlDev ========
				{

					//---- tblTest ----
					tblTest.setModel(new DefaultTableModel(
						new Object[][] {
						},
						new String[] {
							"Select", "Stator SNo."
						}
					) {
						Class<?>[] columnTypes = new Class<?>[] {
							Boolean.class, Object.class
						};
						boolean[] columnEditable = new boolean[] {
							true, false
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
						TableColumnModel cm = tblTest.getColumnModel();
						cm.getColumn(0).setResizable(false);
						cm.getColumn(0).setMaxWidth(50);
						cm.getColumn(0).setPreferredWidth(59);
						cm.getColumn(1).setResizable(false);
					}
					tblTest.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
					tblTest.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
					tblTest.setShowVerticalLines(false);
					tblTest.setSelectionBackground(new Color(0xffffcc));
					tblTest.setSelectionForeground(Color.black);
					scrlDev.setViewportView(tblTest);
				}
				pnlFilter.add(scrlDev, new TableLayoutConstraints(0, 2, 1, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- cmdRefresh ----
				cmdRefresh.setText("<html><body align=\"center\">Refresh<br>Preview<font size=-2><br>[F5]</html>");
				cmdRefresh.setFont(new Font("Arial", Font.PLAIN, 15));
				cmdRefresh.setIcon(new ImageIcon(getClass().getResource("/img/refresh.PNG")));
				cmdRefresh.setBackground(new Color(0xece9d5));
				cmdRefresh.setToolTipText("Click on this to refresh print preview based on test selection");
				cmdRefresh.setHorizontalTextPosition(SwingConstants.CENTER);
				cmdRefresh.setVerticalTextPosition(SwingConstants.BOTTOM);
				cmdRefresh.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						cmdRefreshActionPerformed();
					}
				});
				pnlFilter.add(cmdRefresh, new TableLayoutConstraints(2, 2, 2, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- lblResult ----
				lblResult.setText("0 Test(s) Found");
				lblResult.setFont(new Font("Arial", Font.BOLD, 14));
				lblResult.setForeground(Color.blue);
				pnlFilter.add(lblResult, new TableLayoutConstraints(0, 3, 2, 3, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
			}
			pnlOpt.add(pnlFilter, new TableLayoutConstraints(0, 0, 1, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

			//======== pnlAdFilter ========
			{
				pnlAdFilter.setFont(new Font("Arial", Font.PLAIN, 12));
				pnlAdFilter.setBorder(new TitledBorder(null, "Advanced Filter", TitledBorder.LEADING, TitledBorder.DEFAULT_POSITION,
					new Font("Arial", Font.PLAIN, 14), new Color(0x006699)));
				pnlAdFilter.setLayout(new TableLayout(new double[][] {
					{TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.FILL, TableLayout.PREFERRED, TableLayout.FILL},
					{TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.PREFERRED}}));
				((TableLayout)pnlAdFilter.getLayout()).setHGap(5);
				((TableLayout)pnlAdFilter.getLayout()).setVGap(5);

				//---- label11 ----
				label11.setText("Stator Type");
				label11.setFont(new Font("Arial", Font.PLAIN, 13));
				pnlAdFilter.add(label11, new TableLayoutConstraints(0, 0, 0, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- cmbType ----
				cmbType.setFont(new Font("Microsoft Sans Serif", Font.PLAIN, 12));
				cmbType.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						cmbTypeActionPerformed();
					}
				});
				pnlAdFilter.add(cmbType, new TableLayoutConstraints(2, 0, 4, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- label12 ----
				label12.setText("Date (DD-MM-YYYY)");
				label12.setFont(new Font("Arial", Font.PLAIN, 13));
				pnlAdFilter.add(label12, new TableLayoutConstraints(0, 1, 0, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- label13 ----
				label13.setText("From");
				label13.setFont(new Font("Arial", Font.BOLD, 12));
				pnlAdFilter.add(label13, new TableLayoutConstraints(1, 1, 1, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- fromDt ----
				fromDt.setFont(new Font("Microsoft Sans Serif", Font.PLAIN, 12));
				fromDt.setDateFormatString("dd-MM-yyyy");
				fromDt.addPropertyChangeListener(new PropertyChangeListener() {
					@Override
					public void propertyChange(PropertyChangeEvent e) {
						fromDtPropertyChange();
					}
				});
				pnlAdFilter.add(fromDt, new TableLayoutConstraints(2, 1, 2, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- label14 ----
				label14.setText("To");
				label14.setFont(new Font("Arial", Font.BOLD, 12));
				pnlAdFilter.add(label14, new TableLayoutConstraints(3, 1, 3, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- toDt ----
				toDt.setFont(new Font("Microsoft Sans Serif", Font.PLAIN, 12));
				toDt.setDateFormatString("dd-MM-yyyy");
				toDt.addPropertyChangeListener(new PropertyChangeListener() {
					@Override
					public void propertyChange(PropertyChangeEvent e) {
						toDtPropertyChange();
					}
				});
				pnlAdFilter.add(toDt, new TableLayoutConstraints(4, 1, 4, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- label15 ----
				label15.setText("Stator Serial Number");
				label15.setFont(new Font("Arial", Font.PLAIN, 13));
				pnlAdFilter.add(label15, new TableLayoutConstraints(0, 2, 0, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- label16 ----
				label16.setText("From");
				label16.setFont(new Font("Arial", Font.BOLD, 12));
				pnlAdFilter.add(label16, new TableLayoutConstraints(1, 2, 1, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- txtSlNoFrom ----
				txtSlNoFrom.setFont(new Font("Microsoft Sans Serif", Font.PLAIN, 12));
				txtSlNoFrom.addFocusListener(new FocusAdapter() {
					@Override
					public void focusLost(FocusEvent e) {
						txtSlNoFromFocusLost();
					}
				});
				pnlAdFilter.add(txtSlNoFrom, new TableLayoutConstraints(2, 2, 2, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- label17 ----
				label17.setText("To");
				label17.setFont(new Font("Arial", Font.BOLD, 12));
				pnlAdFilter.add(label17, new TableLayoutConstraints(3, 2, 3, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- txtSlNoTo ----
				txtSlNoTo.setFont(new Font("Microsoft Sans Serif", Font.PLAIN, 12));
				txtSlNoTo.addFocusListener(new FocusAdapter() {
					@Override
					public void focusLost(FocusEvent e) {
						txtSlNoToFocusLost();
					}
				});
				pnlAdFilter.add(txtSlNoTo, new TableLayoutConstraints(4, 2, 4, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- label20 ----
				label20.setText("Remarks");
				label20.setFont(new Font("Arial", Font.PLAIN, 13));
				pnlAdFilter.add(label20, new TableLayoutConstraints(0, 3, 0, 3, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- cmbRemarks ----
				cmbRemarks.setFont(new Font("Microsoft Sans Serif", Font.PLAIN, 12));
				cmbRemarks.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						cmbRemarksActionPerformed();
					}
				});
				pnlAdFilter.add(cmbRemarks, new TableLayoutConstraints(2, 3, 4, 3, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- label18 ----
				label18.setText("Station");
				label18.setFont(new Font("Arial", Font.PLAIN, 13));
				pnlAdFilter.add(label18, new TableLayoutConstraints(0, 4, 0, 4, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- cmbLine ----
				cmbLine.setFont(new Font("Microsoft Sans Serif", Font.PLAIN, 12));
				cmbLine.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						cmbLineActionPerformed();
					}
				});
				pnlAdFilter.add(cmbLine, new TableLayoutConstraints(2, 4, 4, 4, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- label22 ----
				label22.setText("Vendor Reference");
				label22.setFont(new Font("Arial", Font.PLAIN, 13));
				pnlAdFilter.add(label22, new TableLayoutConstraints(0, 5, 0, 5, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- cmbVendorRef ----
				cmbVendorRef.setFont(new Font("Microsoft Sans Serif", Font.PLAIN, 12));
				cmbVendorRef.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						cmbVendorRef();
					}
				});
				pnlAdFilter.add(cmbVendorRef, new TableLayoutConstraints(2, 5, 4, 5, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
			}
			pnlOpt.add(pnlAdFilter, new TableLayoutConstraints(0, 1, 1, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

			//---- cmdExp ----
			cmdExp.setText("Export as Excel file");
			cmdExp.setFont(new Font("Arial", Font.PLAIN, 14));
			cmdExp.setToolTipText("Click on this to print current page");
			cmdExp.setMnemonic('E');
			cmdExp.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					cmdExpActionPerformed();
				}
			});
			pnlOpt.add(cmdExp, new TableLayoutConstraints(0, 2, 1, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

			//---- cmdPrint ----
			cmdPrint.setText("Print Current Page");
			cmdPrint.setFont(new Font("Arial", Font.PLAIN, 15));
			cmdPrint.setIcon(new ImageIcon(getClass().getResource("/img/print.PNG")));
			cmdPrint.setToolTipText("Click on this to print current page");
			cmdPrint.setMnemonic('P');
			cmdPrint.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					cmdPrintActionPerformed();
				}
			});
			pnlOpt.add(cmdPrint, new TableLayoutConstraints(0, 3, 0, 3, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

			//---- cmdPrintAll ----
			cmdPrintAll.setText("Print All");
			cmdPrintAll.setFont(new Font("Arial", Font.PLAIN, 15));
			cmdPrintAll.setIcon(new ImageIcon(getClass().getResource("/img/printall.PNG")));
			cmdPrintAll.setToolTipText("Click on this to print all pages");
			cmdPrintAll.setMnemonic('A');
			cmdPrintAll.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					cmdPrintAllActionPerformed();
				}
			});
			pnlOpt.add(cmdPrintAll, new TableLayoutConstraints(1, 3, 1, 3, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

			//---- cmdClose ----
			cmdClose.setText("<html>Close&nbsp;&nbsp<font size=-2>[Esc]</html>");
			cmdClose.setFont(new Font("Arial", Font.PLAIN, 15));
			cmdClose.setIcon(new ImageIcon(getClass().getResource("/img/exit.PNG")));
			cmdClose.setToolTipText("Click on this to close this window");
			cmdClose.setMnemonic('L');
			cmdClose.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					cmdCloseActionPerformed();
				}
			});
			pnlOpt.add(cmdClose, new TableLayoutConstraints(0, 4, 1, 4, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
		}
		contentPane.add(pnlOpt, new TableLayoutConstraints(0, 0, 0, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

		//======== printContainer ========
		{
			printContainer.setBorder(new TitledBorder(null, "Print Preview", TitledBorder.LEADING, TitledBorder.TOP,
				new Font("Arial", Font.PLAIN, 16), new Color(0x006699)));
			printContainer.setFocusable(false);
			printContainer.setLayout(new TableLayout(new double[][] {
				{TableLayout.FILL},
				{TableLayout.PREFERRED, TableLayout.FILL}}));
			((TableLayout)printContainer.getLayout()).setHGap(5);
			((TableLayout)printContainer.getLayout()).setVGap(5);

			//======== pnlBut ========
			{
				pnlBut.setLayout(new TableLayout(new double[][] {
					{TableLayout.FILL, TableLayout.FILL, TableLayout.FILL, TableLayout.FILL},
					{TableLayout.PREFERRED}}));
				((TableLayout)pnlBut.getLayout()).setHGap(5);
				((TableLayout)pnlBut.getLayout()).setVGap(5);

				//---- cmdFirst ----
				cmdFirst.setIcon(new ImageIcon(getClass().getResource("/img/first.PNG")));
				cmdFirst.setToolTipText("Go to first page");
				cmdFirst.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						cmdFirstActionPerformed();
					}
				});
				pnlBut.add(cmdFirst, new TableLayoutConstraints(0, 0, 0, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- cmdPrev ----
				cmdPrev.setIcon(new ImageIcon(getClass().getResource("/img/prev.PNG")));
				cmdPrev.setToolTipText("Go to previous page");
				cmdPrev.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						cmdPrevActionPerformed();
					}
				});
				pnlBut.add(cmdPrev, new TableLayoutConstraints(1, 0, 1, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- cmdNext ----
				cmdNext.setIcon(new ImageIcon(getClass().getResource("/img/next.PNG")));
				cmdNext.setToolTipText("Go to next page");
				cmdNext.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						cmdNextActionPerformed();
					}
				});
				pnlBut.add(cmdNext, new TableLayoutConstraints(2, 0, 2, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- cmdLast ----
				cmdLast.setIcon(new ImageIcon(getClass().getResource("/img/last.PNG")));
				cmdLast.setToolTipText("Go to last page");
				cmdLast.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						cmdLastActionPerformed();
					}
				});
				pnlBut.add(cmdLast, new TableLayoutConstraints(3, 0, 3, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
			}
			printContainer.add(pnlBut, new TableLayoutConstraints(0, 0, 0, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

			//======== tabReport ========
			{
				tabReport.setFont(new Font("Arial", Font.BOLD, 16));
				tabReport.setOpaque(true);
				tabReport.addChangeListener(new ChangeListener() {
					@Override
					public void stateChanged(ChangeEvent e) {
						tabReportStateChanged();
					}
				});

				//======== scrollPane1 ========
				{

					//======== printArea ========
					{
						printArea.setBorder(new LineBorder(Color.blue, 2));
						printArea.setBackground(Color.white);
						printArea.setLayout(new TableLayout(new double[][] {
							{1275},
							{1050}}));

						//======== pnlPrint ========
						{
							pnlPrint.setBackground(Color.white);
							pnlPrint.setLayout(new TableLayout(new double[][] {
								{20, TableLayout.FILL, 20},
								{20, TableLayout.FILL, 20}}));

							//======== pnlRep ========
							{
								pnlRep.setBorder(LineBorder.createBlackLineBorder());
								pnlRep.setBackground(Color.white);
								pnlRep.setLayout(new TableLayout(new double[][] {
									{90, TableLayout.FILL, 90},
									{5, TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.PREFERRED, 5, TableLayout.PREFERRED, 5, TableLayout.PREFERRED, TableLayout.FILL, 45, 10}}));

								//---- lblCustLogo ----
								lblCustLogo.setHorizontalAlignment(SwingConstants.CENTER);
								pnlRep.add(lblCustLogo, new TableLayoutConstraints(0, 1, 0, 5, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

								//---- lblCompName ----
								lblCompName.setText("Company Name");
								lblCompName.setFont(new Font("Arial", Font.BOLD, 16));
								lblCompName.setHorizontalAlignment(SwingConstants.CENTER);
								pnlRep.add(lblCompName, new TableLayoutConstraints(1, 1, 1, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

								//---- lblCompAdr ----
								lblCompAdr.setText("Address Line 1 and Line 2");
								lblCompAdr.setFont(new Font("Arial", Font.BOLD, 12));
								lblCompAdr.setHorizontalAlignment(SwingConstants.CENTER);
								pnlRep.add(lblCompAdr, new TableLayoutConstraints(1, 3, 1, 3, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

								//---- lblTitle ----
								lblTitle.setText("STATOR TEST REPORT");
								lblTitle.setFont(new Font("Arial", Font.BOLD, 14));
								lblTitle.setHorizontalAlignment(SwingConstants.CENTER);
								pnlRep.add(lblTitle, new TableLayoutConstraints(1, 5, 1, 5, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

								//---- lblPage ----
								lblPage.setText("Page 1 of 1");
								lblPage.setFont(new Font("Arial", Font.PLAIN, 12));
								lblPage.setHorizontalAlignment(SwingConstants.RIGHT);
								pnlRep.add(lblPage, new TableLayoutConstraints(2, 5, 2, 5, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

								//======== pnlTop ========
								{
									pnlTop.setBackground(Color.black);
									pnlTop.setLayout(new TableLayout(new double[][] {
										{120, 213, 60, 111, 65, TableLayout.FILL},
										{1, 36, 1}}));

									//---- label19 ----
									label19.setText("STATOR MODEL:");
									label19.setFont(new Font("Arial", Font.BOLD, 14));
									label19.setBackground(Color.white);
									label19.setOpaque(true);
									pnlTop.add(label19, new TableLayoutConstraints(0, 1, 0, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- lblType ----
									lblType.setText("type");
									lblType.setFont(new Font("Arial", Font.PLAIN, 14));
									lblType.setBackground(Color.white);
									lblType.setOpaque(true);
									pnlTop.add(lblType, new TableLayoutConstraints(1, 1, 1, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- label34 ----
									label34.setText("PHASE:");
									label34.setFont(new Font("Arial", Font.BOLD, 14));
									label34.setBackground(Color.white);
									label34.setOpaque(true);
									pnlTop.add(label34, new TableLayoutConstraints(2, 1, 2, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- lblPh ----
									lblPh.setText("phase");
									lblPh.setFont(new Font("Arial", Font.PLAIN, 14));
									lblPh.setBackground(Color.white);
									lblPh.setOpaque(true);
									pnlTop.add(lblPh, new TableLayoutConstraints(3, 1, 3, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- label35 ----
									label35.setText("KW / HP:");
									label35.setFont(new Font("Arial", Font.BOLD, 14));
									label35.setBackground(Color.white);
									label35.setOpaque(true);
									pnlTop.add(label35, new TableLayoutConstraints(4, 1, 4, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- lblRat ----
									lblRat.setText("rating");
									lblRat.setFont(new Font("Arial", Font.PLAIN, 14));
									lblRat.setBackground(Color.white);
									lblRat.setOpaque(true);
									pnlTop.add(lblRat, new TableLayoutConstraints(5, 1, 5, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
								}
								pnlRep.add(pnlTop, new TableLayoutConstraints(0, 7, 2, 7, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

								//======== pnlPrime ========
								{
									pnlPrime.setBackground(Color.black);
									pnlPrime.setAutoscrolls(true);
									pnlPrime.setLayout(new TableLayout(new double[][] {
										{75, TableLayout.FILL, 124, 149, TableLayout.FILL, TableLayout.FILL, TableLayout.FILL, TableLayout.FILL, TableLayout.FILL, TableLayout.FILL, TableLayout.FILL, TableLayout.FILL, TableLayout.FILL, TableLayout.FILL, 319, TableLayout.FILL, TableLayout.FILL, 150},
										{25, 20, 20, TableLayout.FILL, TableLayout.PREFERRED}}));
									((TableLayout)pnlPrime.getLayout()).setHGap(1);
									((TableLayout)pnlPrime.getLayout()).setVGap(1);

									//---- label122 ----
									label122.setText("Date");
									label122.setBackground(Color.white);
									label122.setHorizontalAlignment(SwingConstants.CENTER);
									label122.setFont(new Font("Arial", Font.BOLD, 12));
									label122.setOpaque(true);
									pnlPrime.add(label122, new TableLayoutConstraints(0, 0, 0, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- label228 ----
									label228.setText("Stator SNo.");
									label228.setBackground(Color.white);
									label228.setHorizontalAlignment(SwingConstants.CENTER);
									label228.setFont(new Font("Arial", Font.BOLD, 12));
									label228.setOpaque(true);
									pnlPrime.add(label228, new TableLayoutConstraints(2, 0, 2, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- label5 ----
									label5.setText("Resistance");
									label5.setHorizontalAlignment(SwingConstants.CENTER);
									label5.setFont(new Font("Arial", Font.BOLD, 12));
									label5.setOpaque(true);
									label5.setBackground(Color.white);
									pnlPrime.add(label5, new TableLayoutConstraints(4, 0, 7, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- label8 ----
									label8.setText("High Voltage Test");
									label8.setFont(new Font("Arial", Font.BOLD, 12));
									label8.setHorizontalAlignment(SwingConstants.CENTER);
									label8.setOpaque(true);
									label8.setBackground(Color.white);
									pnlPrime.add(label8, new TableLayoutConstraints(10, 0, 11, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- label229 ----
									label229.setText("Result");
									label229.setBackground(Color.white);
									label229.setHorizontalAlignment(SwingConstants.CENTER);
									label229.setFont(new Font("Arial", Font.BOLD, 12));
									label229.setOpaque(true);
									pnlPrime.add(label229, new TableLayoutConstraints(16, 0, 16, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- label230 ----
									label230.setText("Remark");
									label230.setBackground(Color.white);
									label230.setHorizontalAlignment(SwingConstants.CENTER);
									label230.setFont(new Font("Arial", Font.BOLD, 12));
									label230.setOpaque(true);
									pnlPrime.add(label230, new TableLayoutConstraints(17, 0, 17, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- label2 ----
									label2.setText("Temp.");
									label2.setFont(new Font("Arial", Font.BOLD, 12));
									label2.setHorizontalAlignment(SwingConstants.CENTER);
									label2.setOpaque(true);
									label2.setBackground(Color.white);
									pnlPrime.add(label2, new TableLayoutConstraints(8, 0, 8, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- label1 ----
									label1.setText("<html><body align=\"center\">IR <br>Bef. HV</hv></body></html>");
									label1.setHorizontalAlignment(SwingConstants.CENTER);
									label1.setBackground(Color.white);
									label1.setOpaque(true);
									label1.setFont(new Font("Arial", Font.BOLD, 12));
									pnlPrime.add(label1, new TableLayoutConstraints(9, 0, 9, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- label7 ----
									label7.setText("<html><body align=\"center\">IR <br>Aft. HV</hv></body></html>");
									label7.setHorizontalAlignment(SwingConstants.CENTER);
									label7.setBackground(Color.white);
									label7.setOpaque(true);
									label7.setFont(new Font("Arial", Font.BOLD, 12));
									pnlPrime.add(label7, new TableLayoutConstraints(12, 0, 12, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- lbllblSurge ----
									lbllblSurge.setText("<html><body align=\"center\">Surge</body></html>");
									lbllblSurge.setHorizontalAlignment(SwingConstants.CENTER);
									lbllblSurge.setBackground(Color.white);
									lbllblSurge.setOpaque(true);
									lbllblSurge.setFont(new Font("Arial", Font.BOLD, 12));
									pnlPrime.add(lbllblSurge, new TableLayoutConstraints(13, 0, 13, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- lbllblSurgeWave ----
									lbllblSurgeWave.setText("Surge Wave");
									lbllblSurgeWave.setBackground(Color.white);
									lbllblSurgeWave.setHorizontalAlignment(SwingConstants.CENTER);
									lbllblSurgeWave.setFont(new Font("Arial", Font.BOLD, 12));
									lbllblSurgeWave.setOpaque(true);
									pnlPrime.add(lbllblSurgeWave, new TableLayoutConstraints(14, 0, 14, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- lblStCl ----
									lblStCl.setText("Starting");
									lblStCl.setBackground(Color.white);
									lblStCl.setHorizontalAlignment(SwingConstants.CENTER);
									lblStCl.setFont(new Font("Arial", Font.BOLD, 12));
									lblStCl.setOpaque(true);
									lblStCl.setAutoscrolls(true);
									pnlPrime.add(lblStCl, new TableLayoutConstraints(4, 1, 4, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- lblRunCl ----
									lblRunCl.setText("Running");
									lblRunCl.setBackground(Color.white);
									lblRunCl.setHorizontalAlignment(SwingConstants.CENTER);
									lblRunCl.setFont(new Font("Arial", Font.BOLD, 12));
									lblRunCl.setOpaque(true);
									lblRunCl.setAutoscrolls(true);
									pnlPrime.add(lblRunCl, new TableLayoutConstraints(5, 1, 5, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- lblComCl ----
									lblComCl.setText("Common");
									lblComCl.setBackground(Color.white);
									lblComCl.setHorizontalAlignment(SwingConstants.CENTER);
									lblComCl.setFont(new Font("Arial", Font.BOLD, 12));
									lblComCl.setOpaque(true);
									lblComCl.setAutoscrolls(true);
									pnlPrime.add(lblComCl, new TableLayoutConstraints(6, 1, 6, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- lblAveCl ----
									lblAveCl.setText("Average");
									lblAveCl.setFont(new Font("Arial", Font.BOLD, 12));
									lblAveCl.setHorizontalAlignment(SwingConstants.CENTER);
									lblAveCl.setOpaque(true);
									lblAveCl.setBackground(Color.white);
									pnlPrime.add(lblAveCl, new TableLayoutConstraints(7, 1, 7, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- label131 ----
									label131.setText("Voltage");
									label131.setBackground(Color.white);
									label131.setHorizontalAlignment(SwingConstants.CENTER);
									label131.setFont(new Font("Arial", Font.BOLD, 12));
									label131.setOpaque(true);
									label131.setAutoscrolls(true);
									pnlPrime.add(label131, new TableLayoutConstraints(10, 1, 10, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- label132 ----
									label132.setText("Current");
									label132.setBackground(Color.white);
									label132.setHorizontalAlignment(SwingConstants.CENTER);
									label132.setFont(new Font("Arial", Font.BOLD, 12));
									label132.setOpaque(true);
									label132.setAutoscrolls(true);
									pnlPrime.add(label132, new TableLayoutConstraints(11, 1, 11, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- label21 ----
									label21.setText("<html><body align=\"center\">Direction</body></html>");
									label21.setHorizontalAlignment(SwingConstants.CENTER);
									label21.setBackground(Color.white);
									label21.setOpaque(true);
									label21.setFont(new Font("Arial", Font.BOLD, 12));
									pnlPrime.add(label21, new TableLayoutConstraints(15, 0, 15, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- label231 ----
									label231.setText("Test SNo.");
									label231.setBackground(Color.white);
									label231.setHorizontalAlignment(SwingConstants.CENTER);
									label231.setFont(new Font("Arial", Font.BOLD, 12));
									label231.setOpaque(true);
									pnlPrime.add(label231, new TableLayoutConstraints(1, 0, 1, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- label232 ----
									label232.setText("Model");
									label232.setBackground(Color.white);
									label232.setHorizontalAlignment(SwingConstants.CENTER);
									label232.setFont(new Font("Arial", Font.BOLD, 12));
									label232.setOpaque(true);
									pnlPrime.add(label232, new TableLayoutConstraints(3, 0, 3, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- label159 ----
									label159.setText("\u03a9");
									label159.setBackground(Color.white);
									label159.setHorizontalAlignment(SwingConstants.CENTER);
									label159.setFont(new Font("Arial", Font.BOLD, 12));
									label159.setOpaque(true);
									label159.setAutoscrolls(true);
									pnlPrime.add(label159, new TableLayoutConstraints(4, 2, 4, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- label160 ----
									label160.setText("\u03a9");
									label160.setBackground(Color.white);
									label160.setHorizontalAlignment(SwingConstants.CENTER);
									label160.setFont(new Font("Arial", Font.BOLD, 12));
									label160.setOpaque(true);
									label160.setAutoscrolls(true);
									pnlPrime.add(label160, new TableLayoutConstraints(5, 2, 5, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- label137 ----
									label137.setText("\u03a9");
									label137.setBackground(Color.white);
									label137.setHorizontalAlignment(SwingConstants.CENTER);
									label137.setFont(new Font("Arial", Font.BOLD, 12));
									label137.setOpaque(true);
									label137.setAutoscrolls(true);
									pnlPrime.add(label137, new TableLayoutConstraints(6, 2, 6, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- label3 ----
									label3.setText("\u03a9");
									label3.setHorizontalAlignment(SwingConstants.CENTER);
									label3.setFont(new Font("Arial", Font.BOLD, 12));
									label3.setOpaque(true);
									label3.setBackground(Color.white);
									pnlPrime.add(label3, new TableLayoutConstraints(7, 2, 7, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- label4 ----
									label4.setText("\u00b0C");
									label4.setFont(new Font("Arial", Font.BOLD, 12));
									label4.setHorizontalAlignment(SwingConstants.CENTER);
									label4.setOpaque(true);
									label4.setBackground(Color.white);
									pnlPrime.add(label4, new TableLayoutConstraints(8, 2, 8, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- label6 ----
									label6.setText("M\u03a9");
									label6.setHorizontalAlignment(SwingConstants.CENTER);
									label6.setBackground(Color.white);
									label6.setOpaque(true);
									label6.setFont(new Font("Arial", Font.BOLD, 12));
									pnlPrime.add(label6, new TableLayoutConstraints(9, 2, 9, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- label139 ----
									label139.setText("kV");
									label139.setBackground(Color.white);
									label139.setHorizontalAlignment(SwingConstants.CENTER);
									label139.setFont(new Font("Arial", Font.BOLD, 12));
									label139.setOpaque(true);
									label139.setAutoscrolls(true);
									pnlPrime.add(label139, new TableLayoutConstraints(10, 2, 10, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- label140 ----
									label140.setText("mA");
									label140.setBackground(Color.white);
									label140.setHorizontalAlignment(SwingConstants.CENTER);
									label140.setFont(new Font("Arial", Font.BOLD, 12));
									label140.setOpaque(true);
									label140.setAutoscrolls(true);
									pnlPrime.add(label140, new TableLayoutConstraints(11, 2, 11, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- label9 ----
									label9.setText("M\u03a9");
									label9.setHorizontalAlignment(SwingConstants.CENTER);
									label9.setBackground(Color.white);
									label9.setOpaque(true);
									label9.setFont(new Font("Arial", Font.BOLD, 12));
									pnlPrime.add(label9, new TableLayoutConstraints(12, 2, 12, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- tblRes ----
									tblRes.setModel(new DefaultTableModel(
										new Object[][] {
											{null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
										},
										new String[] {
											null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null
										}
									) {
										Class<?>[] columnTypes = new Class<?>[] {
											String.class, String.class, String.class, String.class, String.class, String.class, String.class, String.class, String.class, String.class, String.class, String.class, String.class, String.class, Byte.class, Byte.class, Byte.class, Byte.class, String.class, String.class, String.class
										};
										boolean[] columnEditable = new boolean[] {
											false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false
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
										TableColumnModel cm = tblRes.getColumnModel();
										cm.getColumn(0).setResizable(false);
										cm.getColumn(0).setMinWidth(76);
										cm.getColumn(0).setMaxWidth(76);
										cm.getColumn(0).setPreferredWidth(76);
										cm.getColumn(1).setResizable(false);
										cm.getColumn(2).setResizable(false);
										cm.getColumn(2).setMinWidth(125);
										cm.getColumn(2).setMaxWidth(125);
										cm.getColumn(2).setPreferredWidth(125);
										cm.getColumn(3).setResizable(false);
										cm.getColumn(3).setMinWidth(150);
										cm.getColumn(3).setMaxWidth(150);
										cm.getColumn(3).setPreferredWidth(150);
										cm.getColumn(4).setResizable(false);
										cm.getColumn(5).setResizable(false);
										cm.getColumn(6).setResizable(false);
										cm.getColumn(7).setResizable(false);
										cm.getColumn(8).setResizable(false);
										cm.getColumn(9).setResizable(false);
										cm.getColumn(10).setResizable(false);
										cm.getColumn(11).setResizable(false);
										cm.getColumn(12).setResizable(false);
										cm.getColumn(13).setResizable(false);
										cm.getColumn(14).setResizable(false);
										cm.getColumn(14).setMinWidth(80);
										cm.getColumn(14).setMaxWidth(80);
										cm.getColumn(14).setPreferredWidth(80);
										cm.getColumn(15).setResizable(false);
										cm.getColumn(15).setMinWidth(80);
										cm.getColumn(15).setMaxWidth(80);
										cm.getColumn(15).setPreferredWidth(80);
										cm.getColumn(16).setResizable(false);
										cm.getColumn(16).setMinWidth(80);
										cm.getColumn(16).setMaxWidth(80);
										cm.getColumn(16).setPreferredWidth(80);
										cm.getColumn(17).setResizable(false);
										cm.getColumn(17).setMinWidth(80);
										cm.getColumn(17).setMaxWidth(80);
										cm.getColumn(17).setPreferredWidth(80);
										cm.getColumn(18).setResizable(false);
										cm.getColumn(19).setResizable(false);
										cm.getColumn(20).setResizable(false);
										cm.getColumn(20).setMinWidth(150);
										cm.getColumn(20).setMaxWidth(150);
										cm.getColumn(20).setPreferredWidth(150);
									}
									tblRes.setFont(new Font("Arial", Font.PLAIN, 12));
									tblRes.setRowHeight(26);
									tblRes.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
									tblRes.setGridColor(Color.black);
									tblRes.setRowSelectionAllowed(false);
									tblRes.setAutoscrolls(false);
									tblRes.setFocusable(false);
									tblRes.setEnabled(false);
									tblRes.setIntercellSpacing(new Dimension(2, 2));
									tblRes.setBorder(null);
									pnlPrime.add(tblRes, new TableLayoutConstraints(0, 3, 17, 3, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
								}
								pnlRep.add(pnlPrime, new TableLayoutConstraints(0, 8, 2, 8, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

								//======== pnlBot ========
								{
									pnlBot.setBackground(Color.black);
									pnlBot.setLayout(new TableLayout(new double[][] {
										{TableLayout.FILL, TableLayout.FILL, TableLayout.FILL, TableLayout.FILL},
										{1, TableLayout.FILL}}));

									//---- lblTestedBy ----
									lblTestedBy.setText("Tested By");
									lblTestedBy.setFont(new Font("Garamond", Font.PLAIN, 13));
									lblTestedBy.setHorizontalAlignment(SwingConstants.CENTER);
									lblTestedBy.setVerticalAlignment(SwingConstants.BOTTOM);
									lblTestedBy.setBackground(Color.white);
									lblTestedBy.setOpaque(true);
									pnlBot.add(lblTestedBy, new TableLayoutConstraints(0, 1, 1, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- label102 ----
									label102.setText("Approved By");
									label102.setFont(new Font("Garamond", Font.PLAIN, 13));
									label102.setHorizontalAlignment(SwingConstants.CENTER);
									label102.setVerticalAlignment(SwingConstants.BOTTOM);
									label102.setBackground(Color.white);
									label102.setOpaque(true);
									pnlBot.add(label102, new TableLayoutConstraints(3, 1, 3, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

									//---- lblVerBy14 ----
									lblVerBy14.setText("Verified By");
									lblVerBy14.setFont(new Font("Garamond", Font.PLAIN, 13));
									lblVerBy14.setHorizontalAlignment(SwingConstants.CENTER);
									lblVerBy14.setVerticalAlignment(SwingConstants.BOTTOM);
									lblVerBy14.setBackground(Color.white);
									lblVerBy14.setOpaque(true);
									pnlBot.add(lblVerBy14, new TableLayoutConstraints(2, 1, 2, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
								}
								pnlRep.add(pnlBot, new TableLayoutConstraints(0, 9, 2, 9, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
							}
							pnlPrint.add(pnlRep, new TableLayoutConstraints(1, 1, 1, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
						}
						printArea.add(pnlPrint, new TableLayoutConstraints(0, 0, 0, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
					}
					scrollPane1.setViewportView(printArea);
				}
				tabReport.addTab("1. Test Report", scrollPane1);
			}
			printContainer.add(tabReport, new TableLayoutConstraints(0, 1, 0, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
		}
		contentPane.add(printContainer, new TableLayoutConstraints(1, 0, 1, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
		setSize(1370, 730);
		setLocationRelativeTo(null);

		//---- buttonGroup1 ----
		ButtonGroup buttonGroup1 = new ButtonGroup();
		buttonGroup1.add(optPass);
		buttonGroup1.add(optFail);
		buttonGroup1.add(optBoth);
		// JFormDesigner - End of component initialization  //GEN-END:initComponents
	}
	
	// custom code - begin
	private void customInit() {
		// associate func keys
		associateFunctionKeys();
		
		// set screen size
		//this.setMinimumSize(new Dimension((int)this.getSize().getWidth(), (int)java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds().getHeight()));
		
		// set mnemonic for tabs
		tabReport.setMnemonicAt(0, '1');
		
		// align test tables' content to right
		DefaultTableCellRenderer cRdr = new DefaultTableCellRenderer();
		cRdr.setHorizontalAlignment(JLabel.CENTER);
		
		tblRes.setDefaultRenderer(String.class, cRdr);
		
		ResultSet res = null;
		try {
			
			conn = DriverManager.getConnection(Configuration.DB_URL);
			stmt = conn.createStatement();
			stmt.setQueryTimeout(30);
			stmt2 = conn.createStatement();
			stmt2.setQueryTimeout(30);
			
			// load existing stator type and choose the default one
			cmbType.addItem("ALL");
			res = stmt.executeQuery("select type from " + Configuration.STATOR_TYPE);
			while (res.next()) {
				cmbType.addItem(res.getString("type"));
			}
			res.close();
			
			// load existing lines
			cmbLine.addItem("ALL");
			for (int i=1; i<=Integer.parseInt(Configuration.NUMBER_OF_STATIONS); i++) {
				cmbLine.addItem("" + i);
			}
			cmbLine.setSelectedItem(Configuration.LINE_NAME);
			
			// load distinct remarks
			cmbRemarks.addItem("ALL");
			res = stmt.executeQuery("select distinct(remark) as remark from " + Configuration.READING_DETAIL + " where remark <> ''");
			while (res.next()) {
				cmbRemarks.addItem(res.getString("remark"));
			}
			cmbRemarks.setSelectedIndex(0);
			
			// load distinct vendor ref
			cmbVendorRef.addItem("ALL");
			res = stmt.executeQuery("select distinct(vendor_ref) as vendor_ref from " + Configuration.READING_DETAIL + " where vendor_ref <> ''");
			while (res.next()) {
				cmbVendorRef.addItem(res.getString("vendor_ref"));
			}
			cmbVendorRef.setSelectedIndex(0);
			
			res.close();

			// load list of default tests
			optBoth.setSelected(true);
			Calendar cal = Calendar.getInstance();
			toDt.setDate(cal.getTime());
			// by default last seven days test reported when no particular pump selected for report
			cal.add(Calendar.DATE, -7);
			if (cal.compareTo(recDt) > 0) {
				fromDt.setDate(recDt.getTime());
			} else {
				pnlFilter.setBorder(new TitledBorder(null, "Test List [Last one week tests are displayed by default]", TitledBorder.LEADING, TitledBorder.DEFAULT_POSITION,
						new Font("Trebuchet MS", Font.PLAIN, 14), new Color(0, 102, 153)));
				fromDt.setDate(cal.getTime());
			}
			
			initInProgress = false;
			
			// set default stator type
			/*if (curStatorType.isEmpty()) {
				cmbType.setSelectedIndex(0);
			} else {
				cmbType.setSelectedItem(curStatorType);
			}*/
			cmbType.setSelectedIndex(0);
			
			// remove surge related columns if not required
			if (Configuration.IS_SURGE_DISABLED.equals("1") || Configuration.IS_SURGE_WAVE_CAPTURED.equals("0")) {
				if (lblPh.getText().equals("Single")) {
					if (Configuration.IS_SURGE_DISABLED.equals("1")) {
						lbllblSurge.setVisible(false);
						lbllblSurgeWave.setVisible(false);
						((TableLayout)pnlPrime.getLayout()).deleteColumn(13);
						((TableLayout)pnlPrime.getLayout()).deleteColumn(13);
						tblRes.removeColumn(tblRes.getColumnModel().getColumn(13));
						tblRes.removeColumn(tblRes.getColumnModel().getColumn(13));
						tblRes.removeColumn(tblRes.getColumnModel().getColumn(13));
						tblRes.removeColumn(tblRes.getColumnModel().getColumn(13));
						tblRes.removeColumn(tblRes.getColumnModel().getColumn(13));
					} else {
						lbllblSurgeWave.setVisible(false);
						((TableLayout)pnlPrime.getLayout()).deleteColumn(14);
						tblRes.removeColumn(tblRes.getColumnModel().getColumn(14));
						tblRes.removeColumn(tblRes.getColumnModel().getColumn(14));
						tblRes.removeColumn(tblRes.getColumnModel().getColumn(14));
						tblRes.removeColumn(tblRes.getColumnModel().getColumn(14));
					}
					
				} else {
					if (Configuration.IS_SURGE_DISABLED.equals("1")) {
						lbllblSurge.setVisible(false);
						lbllblSurgeWave.setVisible(false);
						((TableLayout)pnlPrime.getLayout()).deleteColumn(13);
						((TableLayout)pnlPrime.getLayout()).deleteColumn(13);
						tblRes.removeColumn(tblRes.getColumnModel().getColumn(13));
						tblRes.removeColumn(tblRes.getColumnModel().getColumn(13));
						tblRes.removeColumn(tblRes.getColumnModel().getColumn(13));
						tblRes.removeColumn(tblRes.getColumnModel().getColumn(13));
					 	tblRes.removeColumn(tblRes.getColumnModel().getColumn(13));
					} else {
						lbllblSurgeWave.setVisible(false);
						((TableLayout)pnlPrime.getLayout()).deleteColumn(14);
						tblRes.removeColumn(tblRes.getColumnModel().getColumn(14));
						tblRes.removeColumn(tblRes.getColumnModel().getColumn(14));
						tblRes.removeColumn(tblRes.getColumnModel().getColumn(14));
						tblRes.removeColumn(tblRes.getColumnModel().getColumn(14));	
					}
				}
			}
			
			setReportHeader();
			refreshTestList();
			tabReport.setSelectedIndex(0);
			tabReportStateChanged();
			// hide custom report
			
		} catch (SQLException e) {
			JOptionPane.showMessageDialog(this, "Error:" + e.getMessage());
			return;
		} 
		enableDisableButtons();
	}
	
	private void refreshTestList() {
		if (initInProgress) {
			return;
		}
		setFilterText();
		try {
			DefaultTableModel defModel = (DefaultTableModel) tblTest.getModel();
			while (tblTest.getRowCount() > 0) {
				defModel.removeRow(0);
			}
			
			statorList.clear();
			ResultSet res = stmt.executeQuery("select distinct(stator_slno) from " + Configuration.READING_DETAIL + " " + filterText + " and stator_slno != ''");
			if (res !=null) {
				int r = 0;
				while (res.next()) {
					defModel.addRow(new Object[]{null});
					tblTest.setValueAt(true, r, 0);
					tblTest.setValueAt(res.getString(1), r, 1);
					statorList.put(r++, res.getString(1));
				}
				lblResult.setText(r + " Test(s) Found");
				res.close();
			}
		} catch (SQLException se) {
			JOptionPane.showMessageDialog(this, "Error refreshing the test list:" + se.getMessage());
		}
	}
	
	private void setFilterText() {
		filterText = "";
		String typeFilter = "";
		String dateFilter = "";
		String snoFilter = "";
		String remFilter = "";
		String lineFilter = "";
		String resFilter = "";
		String vendorFilter = "";
		
		
		String filterTitle = "Filter [None]";
		if ( cmbType.getSelectedIndex() > 0) {
			typeFilter = "stator_type='" + cmbType.getSelectedItem().toString() + "'";
		}
		
		if (fromDt.getDate() != null && toDt.getDate() != null) {
			if (!fromDt.getDate().toString().isEmpty() && !toDt.getDate().toString().isEmpty()) {
			dateFilter = "test_date between '" + dbDtFormat.format(fromDt.getDate()) + "' and '" + dbDtFormat.format(toDt.getDate()) + "'";
			}
		}
		
		if (!txtSlNoFrom.getText().isEmpty() && !txtSlNoTo.getText().isEmpty()) {
			snoFilter = "stator_slno between '" + txtSlNoFrom.getText() + "' and '" + txtSlNoTo.getText() + "'";
		}
		
		if ( cmbRemarks.getSelectedIndex() > 0) {
			remFilter = "remark='" + cmbRemarks.getSelectedItem().toString() + "'";
		}
		
		if ( cmbLine.getSelectedIndex() > 0) {
			lineFilter = "line='" + cmbLine.getSelectedItem().toString() + "'";
		}
		
		if ( cmbVendorRef.getSelectedIndex() > 0) {
			vendorFilter = "vendor_ref='" + cmbVendorRef.getSelectedItem().toString() + "'";
		}
		
		if (optPass.isSelected()) {
			resFilter = "upper(test_result)='PASS'";
		} else if (optFail.isSelected()) {
			resFilter = "upper(test_result)='FAIL'";
		}
		
		
		if (!typeFilter.isEmpty() || !dateFilter.isEmpty() || !snoFilter.isEmpty() || !remFilter.isEmpty() || 
				!lineFilter.isEmpty() || !vendorFilter.isEmpty() || !resFilter.isEmpty()) {
			if (!typeFilter.isEmpty()) {
				filterText = typeFilter;
			}
			
			if (!dateFilter.isEmpty()) {
				if (filterText.isEmpty()) {
					filterText = dateFilter;
				} else {
					filterText += " and " + dateFilter;
				}
			}
			
			if (!snoFilter.isEmpty()) {
				if (filterText.isEmpty()) {
					filterText = snoFilter;
				} else {
					filterText += " and " + snoFilter;
				}
			}
			
			if (!resFilter.isEmpty()) {
				if (filterText.isEmpty()) {
					filterText = resFilter;
				} else {
					filterText += " and " + resFilter;
				}
			}
			
			if (!remFilter.isEmpty()) {
				if (filterText.isEmpty()) {
					filterText = remFilter;
				} else {
					filterText += " and " + remFilter;
				}
			}
			
			if (!lineFilter.isEmpty()) {
				if (filterText.isEmpty()) {
					filterText = lineFilter;
				} else {
					filterText += " and " + lineFilter;
				}
			}
			
			if (!vendorFilter.isEmpty()) {
				if (filterText.isEmpty()) {
					filterText = vendorFilter;
				} else {
					filterText += " and " + vendorFilter;
				}
			}
			
			filterText = " where " + filterText;
			statorFilterText = filterText;
		}
		
	}
	private void gotoPageRegRep(int page) {
		// clear the table
		DefaultTableModel defModel = (DefaultTableModel) tblRes.getModel();
		while (tblRes.getRowCount() > 0) {
			defModel.removeRow(0);
		}
		
		long toRow = (long) (page * totRowsPerPage);
		long fromRow = (long) (toRow - (totRowsPerPage) + 1);
		
		try {
			ResultSet res = stmt.executeQuery("select * from TEMP_STATOR_REPORT where rowid between " + fromRow + " and " + toRow);
			ResultSet res2 = null;
			
			// add primary tests
			if (res != null) {
				int r=0;
				int c=0;
				int i=0;
				while (res.next()) {
					c = 0;
					defModel.addRow( new Object[] {""});
					tblRes.setValueAt(reqDtFormat.format(dbDtFormat.parse(res.getString("test_date"))), r, c++);
					tblRes.setValueAt(res.getString("test_slno"), r, c++);
					tblRes.setValueAt(res.getString("stator_slno"), r, c++);
					tblRes.setValueAt(res.getString("stator_type"), r, c++);
					tblRes.setValueAt(dotTwo.format(res.getDouble("res_start")), r, c++);
					tblRes.setValueAt(dotTwo.format(res.getDouble("res_run")), r, c++);
					try {
						if (res.getString("res_com") == null || res.getString("res_com").equals("NA") || res.getInt("res_com") <= 0) {
							tblRes.setValueAt("NA", r, c++);
							tblRes.setValueAt("NA", r, c++);
						}else {
							tblRes.setValueAt(dotTwo.format(res.getDouble("res_com")), r, c++);
							if (!lblPh.getText().equals("Single")) {
								tblRes.setValueAt(dotTwo.format((res.getFloat("res_start") + res.getFloat("res_run") + res.getFloat("res_com"))/3), r, c++);
							} else {
								tblRes.setValueAt("NA", r, c++);
							}
						}
					} catch (NumberFormatException ne) {
						tblRes.setValueAt("NA", r, c++);
						if (c == 7) {
							tblRes.setValueAt("NA", r, c++);
						}
					}
					tblRes.setValueAt(dotOne.format(res.getDouble("temp")), r, c++);
					tblRes.setValueAt(dotZero.format(res.getDouble("ins_res_bef_hv")), r, c++);
					tblRes.setValueAt(dotTwo.format(res.getDouble("hv_kv")), r, c++);
					tblRes.setValueAt(dotTwo.format(res.getDouble("hv_amps")), r, c++);
					tblRes.setValueAt(dotZero.format(res.getDouble("ins_res_aft_hv")), r, c++);
					// surge image
					if (Configuration.IS_SURGE_DISABLED.equals("0")) {
						tblRes.setValueAt(res.getString("surge"), r, c++);
						if (Configuration.IS_SURGE_WAVE_CAPTURED.equals("1")) {
							res2 = stmt2.executeQuery("select wave_img from " + Configuration.SURGE_IMAGE + " where test_slno='" + res.getString("test_slno") + "'");
							i=0;
							while (res2.next() && i < 4) {
								tblRes.setValueAt(new ImageIcon((new ImageIcon(res2.getBytes("wave_img"))).getImage().getScaledInstance(100, tblRes.getRowHeight(), Image.SCALE_SMOOTH)), r, c+i);
								++i;
							}
							c+=i;
							res2.close();	
						}
						
					}
					if (lblPh.getText().equals("Three")) {
						tblRes.setValueAt("NA", r, c++);
					} else {
						tblRes.setValueAt(res.getString("dir"), r, c++);
					}
					
					tblRes.setValueAt(res.getString("test_result"), r, c++);
					tblRes.setValueAt(res.getString("remark"), r, c++);
					lblTestedBy.setText("Tested By [" + res.getString("user").toUpperCase() + "]"); // overwrite with latest user
					++r;
				}
				res.close();
			}
		} catch (Exception sqle) {
			JOptionPane.showMessageDialog(this, "Failed to load current page\nError:" + sqle.getMessage());
			sqle.printStackTrace();
			return;
		}
		
		if (totAvailPagesRegRep > 0) {
			lblPage.setText("Page " + page + " of " + totAvailPagesRegRep);
		} else {
			lblPage.setText("No records found");
		}
		
		enableDisableButtons();
		return;
	}
	
	private void enableDisableButtons()
	{
		long cPage = 0;
		long tPage = 0;
		
		if(tabReport.getSelectedIndex() == 0) { // register rep
			cPage = curPageRegRep;
			tPage = totAvailPagesRegRep;
		}
		
		if (cPage == tPage || tPage == 0) {
			cmdNext.setEnabled(false);
			cmdLast.setEnabled(false);
		}
		else if (!cmdNext.isEnabled()) {
			cmdNext.setEnabled(true);
			cmdLast.setEnabled(true);
		}
		
		if (cPage == 1) {
			cmdPrev.setEnabled(false);
			cmdFirst.setEnabled(false);
		}
		else if (!cmdPrev.isEnabled()){
			cmdPrev.setEnabled(true);
			cmdFirst.setEnabled(true);
		}
	}
	
	private void refreshCount() {
		// set page count based on report selection
		try {
			if (tabReport.getSelectedIndex() == 0) {
				// register report
				totAvailPagesRegRep = 0;
				ResultSet res = stmt.executeQuery("select count(*) as tot from " + Configuration.READING_DETAIL + " " + statorFilterText);
				if (res != null) {
					int result = res.getInt("tot");
					totAvailPagesRegRep =  (int) Math.ceil(result/totRowsPerPage);
					res.close();
				}
			}
		} catch (Exception se) {
			JOptionPane.showMessageDialog(this, "Error setting up page numbers:" + se.getMessage());
		}
		
	}
	
	private void refreshReport() {
		refreshCount();
		switch(tabReport.getSelectedIndex()) {
		case 0: // simple register report
			loadRegisterReport();
			break;
		}
	}
	
	// function to set report header of all reports
	private void setReportHeader() {
		// register report
		lblCompName.setText(Configuration.LICENCEE_NAME);
		lblCompAdr.setText(Configuration.LICENCEE_ADR_1 + ", " + Configuration.LICENCEE_ADR_2);
				
		// company logo & isi logos
		try {
			lblCustLogo.setIcon(new ImageIcon(Configuration.APP_DIR + "/img/company_logo.png"));
		} catch (Exception ie) {
			// ignore
		}
		
	}
	
	private void loadRegisterReport() {
		try {
			// set static values
			lblType.setText(curStatorType);
			ResultSet res = stmt.executeQuery("select type, phase, kw, hp from " + Configuration.STATOR_TYPE + " where type='" + curStatorType + "'");
			
			if (res.next()) {
				lblType.setText(res.getString("type"));
				lblPh.setText(res.getString("phase"));
				lblRat.setText(res.getString("kw") + " / " + res.getString("hp"));
				res.close();
			}
			
			// show / hide name plate details
			if ( cmbType.getSelectedIndex() == 0) {
				lblPh.setText("ALL");
				lblRat.setText("ALL");
			}
			
			// result column based on phase
			if (lblPh.getText().equals("Single") && cmbType.getSelectedIndex() > 0) {
				lblStCl.setText("Starting");
				lblRunCl.setText("Running");
				lblComCl.setText("Common");
				// remove average resistance column
				/*if (isAveVisible) {
					TableLayout tblLy = (TableLayout) pnlPrime.getLayout();
					tblLy.deleteColumn(6);
					tblRes.removeColumn(tblRes.getColumnModel().getColumn(6));
					isAveVisible = false;
				}*/
				
				// image columns
				if (Configuration.IS_SURGE_DISABLED.equals("0") && Configuration.IS_SURGE_WAVE_CAPTURED.equals("1")) {
					tblRes.getColumnModel().getColumn(14).setCellRenderer(tblRes.getDefaultRenderer(ImageIcon.class));
					tblRes.getColumnModel().getColumn(15).setCellRenderer(tblRes.getDefaultRenderer(ImageIcon.class));
					tblRes.getColumnModel().getColumn(16).setCellRenderer(tblRes.getDefaultRenderer(ImageIcon.class));
					tblRes.getColumnModel().getColumn(17).setCellRenderer(tblRes.getDefaultRenderer(ImageIcon.class));
				}
			} else {
				if (cmbType.getSelectedIndex() == 0) {
					lblStCl.setText("R1");
					lblRunCl.setText("R2");
					lblComCl.setText("R3");
				} else {
					lblStCl.setText("R");
					lblRunCl.setText("Y");
					lblComCl.setText("B");
				}
				
				// add average column
				/*if (!isAveVisible) {
					isAveVisible = true;
					lblAveCl.setText("Average");
					lblAveCl.setFont(new Font("Arial", Font.BOLD, 12));
					lblAveCl.setHorizontalAlignment(SwingConstants.CENTER);
					lblAveCl.setOpaque(true);
					lblAveCl.setBackground(Color.white);
					pnlPrime.add(lblAveCl, new TableLayoutConstraints(5, 1, 5, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
					tblRes.addColumn(tblRes.getColumnModel().getColumn(7));
					pnlPrime.revalidate();
					tblRes.revalidate();
				}*/
				
				// image columns
				if (Configuration.IS_SURGE_DISABLED.equals("0") && Configuration.IS_SURGE_WAVE_CAPTURED.equals("1")) {
					tblRes.getColumnModel().getColumn(14).setCellRenderer(tblRes.getDefaultRenderer(ImageIcon.class));
					tblRes.getColumnModel().getColumn(15).setCellRenderer(tblRes.getDefaultRenderer(ImageIcon.class));
					tblRes.getColumnModel().getColumn(16).setCellRenderer(tblRes.getDefaultRenderer(ImageIcon.class));
					tblRes.getColumnModel().getColumn(17).setCellRenderer(tblRes.getDefaultRenderer(ImageIcon.class));
				}
			}
			
			// drop temporary table and reconstruct it based on current filter
			try {
				stmt.executeUpdate("drop table TEMP_STATOR_REPORT");
			} catch (SQLException e) {
				// ignore
			}
			
			String qryString = "select * from " + Configuration.READING_DETAIL + " " + statorFilterText + " order by test_slno";
		
			qryString = "create table TEMP_STATOR_REPORT as " + qryString;
			stmt.executeUpdate(qryString);

		} catch (Exception sqle) {
			JOptionPane.showMessageDialog(this, "Failed to refresh the report\nError:" + sqle.getMessage());
			return;
		}
		
		// load first page as a default
		curPageRegRep = 1;
		gotoPageRegRep(curPageRegRep);
	}
	
	// print progress pop up
	class PrintProgressDlg {
		
		JDialog dlgPrint;
		boolean cancelPrint = false;
		JLabel lblMsg;
		JButton cmdCancel;
		Timer tmrPrint;
		
		PrintProgressDlg() {
			
			dlgPrint = new JDialog();
			lblMsg = new JLabel("");
			cmdCancel = new JButton("Cancel");
			
			dlgPrint.setTitle("Print");
			dlgPrint.setModal(true);
			dlgPrint.setResizable(false);
			dlgPrint.setLocationRelativeTo(null);
			dlgPrint.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
			
			JPanel pnlMsg = new JPanel(new GridLayout(2,1,5,5));
			pnlMsg.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
			
			cmdCancel.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					cmdCancelActionPerformed();
				}
			});
			
			pnlMsg.add(lblMsg);
			pnlMsg.add(cmdCancel);
	
			dlgPrint.setContentPane(pnlMsg);
	
			tmrPrint = new Timer(1000, new AbstractAction() {
			    @Override
			    public void actionPerformed(ActionEvent ae) {
			        dlgPrint.dispose();
			    }
			});
			tmrPrint.setRepeats(false);
		}
		
		public void showMessage(String msg) {
			cancelPrint = false;
			lblMsg.setText(msg);
			dlgPrint.pack();
			tmrPrint.start();
			dlgPrint.setVisible(true);
		}
		
		public boolean getCancelPrint() {
			return cancelPrint;
		}
		
		private void cmdCancelActionPerformed() {
			if (JOptionPane.showConfirmDialog(null, "Do you want to cancel printing?", "Print", JOptionPane.YES_NO_OPTION) == 0) {
				cancelPrint = true;
			}
		}
	}
	// custom code - end

	// JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
	private JPanel pnlOpt;
	private JPanel pnlFilter;
	private JRadioButton optPass;
	private JRadioButton optFail;
	private JRadioButton optBoth;
	private JButton cmdSel;
	private JButton cmdClear;
	private JScrollPane scrlDev;
	private JTable tblTest;
	private JButton cmdRefresh;
	private JLabel lblResult;
	private JPanel pnlAdFilter;
	private JLabel label11;
	private JComboBox cmbType;
	private JLabel label12;
	private JLabel label13;
	private JDateChooser fromDt;
	private JLabel label14;
	private JDateChooser toDt;
	private JLabel label15;
	private JLabel label16;
	private JTextField txtSlNoFrom;
	private JLabel label17;
	private JTextField txtSlNoTo;
	private JLabel label20;
	private JComboBox cmbRemarks;
	private JLabel label18;
	private JComboBox cmbLine;
	private JLabel label22;
	private JComboBox cmbVendorRef;
	private JButton cmdExp;
	private JButton cmdPrint;
	private JButton cmdPrintAll;
	private JButton cmdClose;
	private JPanel printContainer;
	private JPanel pnlBut;
	private JButton cmdFirst;
	private JButton cmdPrev;
	private JButton cmdNext;
	private JButton cmdLast;
	private JTabbedPane tabReport;
	private JScrollPane scrollPane1;
	private JPanel printArea;
	private JPanel pnlPrint;
	private JPanel pnlRep;
	private JLabel lblCustLogo;
	private JLabel lblCompName;
	private JLabel lblCompAdr;
	private JLabel lblTitle;
	private JLabel lblPage;
	private JPanel pnlTop;
	private JLabel label19;
	private JLabel lblType;
	private JLabel label34;
	private JLabel lblPh;
	private JLabel label35;
	private JLabel lblRat;
	private JPanel pnlPrime;
	private JLabel label122;
	private JLabel label228;
	private JLabel label5;
	private JLabel label8;
	private JLabel label229;
	private JLabel label230;
	private JLabel label2;
	private JLabel label1;
	private JLabel label7;
	private JLabel lbllblSurge;
	private JLabel lbllblSurgeWave;
	private JLabel lblStCl;
	private JLabel lblRunCl;
	private JLabel lblComCl;
	private JLabel lblAveCl;
	private JLabel label131;
	private JLabel label132;
	private JLabel label21;
	private JLabel label231;
	private JLabel label232;
	private JLabel label159;
	private JLabel label160;
	private JLabel label137;
	private JLabel label3;
	private JLabel label4;
	private JLabel label6;
	private JLabel label139;
	private JLabel label140;
	private JLabel label9;
	private JTable tblRes;
	private JPanel pnlBot;
	private JLabel lblTestedBy;
	private JLabel label102;
	private JLabel lblVerBy14;
	// JFormDesigner - End of variables declaration  //GEN-END:variables

	// custom code - begin
	private double totRowsPerPage = 30.0;
 	private int totAvailPagesRegRep = 0;
	private int curPageRegRep=1;

	private int curPage=1;
	private int totAvailPages = 0;

	private boolean initInProgress = false;
	
	private Connection conn = null;
	private Statement stmt = null;
	private Statement stmt2 = null;
	private SimpleDateFormat reqDtFormat = new SimpleDateFormat("dd-MM-yyyy");
	private SimpleDateFormat dbDtFormat = new SimpleDateFormat("yyyy-MM-dd");
	private String filterText = "";
	private String statorFilterText = "";
	private LinkedHashMap <Integer, String> statorList = new LinkedHashMap<Integer, String>();
	
	private String curStatorType = "";
	//private Boolean stRunEnabled = false;
	private String recSlNo = "";
	private Calendar recDt = null;
	private Boolean isAveVisible = true;
	
	private DecimalFormat dotZero = new DecimalFormat("#");
	private DecimalFormat dotOne = new DecimalFormat("#0.0");
	private DecimalFormat dotTwo = new DecimalFormat("#0.00");
	private DecimalFormat dotThree = new DecimalFormat("#0.000");
	
	// custom code - end
}

