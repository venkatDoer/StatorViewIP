/*
 * Created by JFormDesigner on Mon Aug 27 16:47:29 IST 2012
 */

package doer.sv;

import java.awt.event.*;
import java.beans.*;
import javax.swing.*;
import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstraints;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Font;
import java.awt.Frame;
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
import java.text.SimpleDateFormat;

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
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;

import com.toedter.calendar.JDateChooser;

/**
 * @author VENKATESAN SELVARAJ
 */
public class FileOpen extends JDialog {
	public FileOpen(Frame owner) {
		super(owner);
		initProg = true;
		initComponents();
		mainFormRef = (StatorView) owner;
		statorType = mainFormRef.curStatorType;
		custInit();
		initProg = false;
		loadExistingTest();
	}

	private void cmdCancelActionPerformed() {
		thisWindowClosing();
		this.setVisible(false);
	}

	private void cmdOpenActionPerformed() {
		loadExistingTest();
		if (result == 0) {
			JOptionPane.showMessageDialog(this, "No records found to open.\nChange your filter criteria and try again.");
			return;
		}
		mainFormRef.openFile("select * from " + Configuration.READING_DETAIL + " " + filterText + " order by test_slno", true);
		this.setVisible(false);
	}

	private void thisWindowClosing() {
		try {
			stmt.close();
			conn.close();
		} catch (SQLException e) {
			// ignore
		}
	}

	private void cmbTypeActionPerformed() {
		loadExistingTest();
	}

	private void cmbLineActionPerformed() {
		loadExistingTest();
	}

	private void txtSlNoFromFocusLost() {
		loadExistingTest();
	}

	private void txtSlNoToFocusLost() {
		loadExistingTest();
	}

	private void fromDtPropertyChange() {
		loadExistingTest();
	}

	private void toDtPropertyChange() {
		loadExistingTest();
	}

	private void cmbRemarksActionPerformed() {
		loadExistingTest();
	}

	private void cmbResActionPerformed() {
		loadExistingTest();
	}

	private void cmbOPActionPerformed() {
		loadExistingTest();
	}

	private void initComponents() {
		// JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
		panel2 = new JPanel();
		pnlFilter = new JPanel();
		label5 = new JLabel();
		cmbType = new JComboBox();
		label12 = new JLabel();
		label8 = new JLabel();
		fromDt = new JDateChooser();
		label9 = new JLabel();
		toDt = new JDateChooser();
		label4 = new JLabel();
		label10 = new JLabel();
		txtSlNoFrom = new JTextField();
		label11 = new JLabel();
		txtSlNoTo = new JTextField();
		label7 = new JLabel();
		cmbRes = new JComboBox();
		label6 = new JLabel();
		cmbRemarks = new JComboBox();
		label3 = new JLabel();
		cmbLine = new JComboBox();
		label13 = new JLabel();
		cmbVendorRef = new JComboBox();
		lblResult = new JLabel();
		cmdOpen = new JButton();
		cmdCancel = new JButton();

		//======== this ========
		setTitle("Doer StatorView: File Open");
		setFont(new Font("Arial", Font.PLAIN, 12));
		setModal(true);
		setResizable(false);
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				thisWindowClosing();
			}
		});
		Container contentPane = getContentPane();
		contentPane.setLayout(new TableLayout(new double[][] {
			{10, TableLayout.PREFERRED, 10},
			{10, TableLayout.PREFERRED, 10}}));
		((TableLayout)contentPane.getLayout()).setHGap(5);
		((TableLayout)contentPane.getLayout()).setVGap(5);

		//======== panel2 ========
		{
			panel2.setBorder(new TitledBorder(null, "Existing Tests", TitledBorder.LEADING, TitledBorder.TOP,
				new Font("Arial", Font.BOLD, 16), new Color(0x006699)));
			panel2.setFocusable(false);
			panel2.setLayout(new TableLayout(new double[][] {
				{TableLayout.PREFERRED, TableLayout.PREFERRED},
				{TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.PREFERRED, 5}}));
			((TableLayout)panel2.getLayout()).setHGap(5);
			((TableLayout)panel2.getLayout()).setVGap(5);

			//======== pnlFilter ========
			{
				pnlFilter.setFont(new Font("Arial", Font.PLAIN, 12));
				pnlFilter.setBorder(new TitledBorder(null, "Filter", TitledBorder.LEADING, TitledBorder.DEFAULT_POSITION,
					new Font("Arial", Font.PLAIN, 12), new Color(0x006699)));
				pnlFilter.setLayout(new TableLayout(new double[][] {
					{TableLayout.PREFERRED, TableLayout.PREFERRED, 150, TableLayout.PREFERRED, 150},
					{TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.PREFERRED}}));
				((TableLayout)pnlFilter.getLayout()).setHGap(5);
				((TableLayout)pnlFilter.getLayout()).setVGap(5);

				//---- label5 ----
				label5.setText("Stator Type");
				label5.setFont(new Font("Arial", Font.PLAIN, 14));
				pnlFilter.add(label5, new TableLayoutConstraints(0, 0, 0, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- cmbType ----
				cmbType.setFont(new Font("Microsoft Sans Serif", Font.PLAIN, 14));
				cmbType.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						cmbTypeActionPerformed();
					}
				});
				pnlFilter.add(cmbType, new TableLayoutConstraints(2, 0, 4, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- label12 ----
				label12.setText("Date (DD-MM-YYYY)");
				label12.setFont(new Font("Arial", Font.PLAIN, 14));
				pnlFilter.add(label12, new TableLayoutConstraints(0, 1, 0, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- label8 ----
				label8.setText("From");
				label8.setFont(new Font("Arial", Font.BOLD, 14));
				pnlFilter.add(label8, new TableLayoutConstraints(1, 1, 1, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- fromDt ----
				fromDt.setFont(new Font("Microsoft Sans Serif", Font.PLAIN, 14));
				fromDt.setDateFormatString("dd-MM-yyyy");
				fromDt.addPropertyChangeListener(new PropertyChangeListener() {
					@Override
					public void propertyChange(PropertyChangeEvent e) {
						fromDtPropertyChange();
					}
				});
				pnlFilter.add(fromDt, new TableLayoutConstraints(2, 1, 2, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- label9 ----
				label9.setText("To");
				label9.setFont(new Font("Arial", Font.BOLD, 14));
				pnlFilter.add(label9, new TableLayoutConstraints(3, 1, 3, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- toDt ----
				toDt.setFont(new Font("Microsoft Sans Serif", Font.PLAIN, 14));
				toDt.setDateFormatString("dd-MM-yyyy");
				toDt.addPropertyChangeListener(new PropertyChangeListener() {
					@Override
					public void propertyChange(PropertyChangeEvent e) {
						toDtPropertyChange();
					}
				});
				pnlFilter.add(toDt, new TableLayoutConstraints(4, 1, 4, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- label4 ----
				label4.setText("Serial Number");
				label4.setFont(new Font("Arial", Font.PLAIN, 14));
				pnlFilter.add(label4, new TableLayoutConstraints(0, 2, 0, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- label10 ----
				label10.setText("From");
				label10.setFont(new Font("Arial", Font.BOLD, 14));
				pnlFilter.add(label10, new TableLayoutConstraints(1, 2, 1, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- txtSlNoFrom ----
				txtSlNoFrom.setFont(new Font("Microsoft Sans Serif", Font.PLAIN, 14));
				txtSlNoFrom.addFocusListener(new FocusAdapter() {
					@Override
					public void focusLost(FocusEvent e) {
						txtSlNoFromFocusLost();
					}
				});
				pnlFilter.add(txtSlNoFrom, new TableLayoutConstraints(2, 2, 2, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- label11 ----
				label11.setText("To");
				label11.setFont(new Font("Arial", Font.BOLD, 14));
				pnlFilter.add(label11, new TableLayoutConstraints(3, 2, 3, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- txtSlNoTo ----
				txtSlNoTo.setFont(new Font("Microsoft Sans Serif", Font.PLAIN, 14));
				txtSlNoTo.addFocusListener(new FocusAdapter() {
					@Override
					public void focusLost(FocusEvent e) {
						txtSlNoToFocusLost();
					}
				});
				pnlFilter.add(txtSlNoTo, new TableLayoutConstraints(4, 2, 4, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- label7 ----
				label7.setText("Result");
				label7.setFont(new Font("Arial", Font.PLAIN, 14));
				pnlFilter.add(label7, new TableLayoutConstraints(0, 3, 0, 3, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- cmbRes ----
				cmbRes.setFont(new Font("Microsoft Sans Serif", Font.PLAIN, 14));
				cmbRes.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						cmbResActionPerformed();
					}
				});
				pnlFilter.add(cmbRes, new TableLayoutConstraints(2, 3, 2, 3, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- label6 ----
				label6.setText("Remark");
				label6.setFont(new Font("Arial", Font.PLAIN, 14));
				pnlFilter.add(label6, new TableLayoutConstraints(0, 4, 0, 4, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- cmbRemarks ----
				cmbRemarks.setFont(new Font("Microsoft Sans Serif", Font.PLAIN, 14));
				cmbRemarks.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						cmbRemarksActionPerformed();
					}
				});
				pnlFilter.add(cmbRemarks, new TableLayoutConstraints(2, 4, 2, 4, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- label3 ----
				label3.setText("Station");
				label3.setFont(new Font("Arial", Font.PLAIN, 14));
				pnlFilter.add(label3, new TableLayoutConstraints(0, 5, 0, 5, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- cmbLine ----
				cmbLine.setFont(new Font("Microsoft Sans Serif", Font.PLAIN, 14));
				cmbLine.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						cmbLineActionPerformed();
					}
				});
				pnlFilter.add(cmbLine, new TableLayoutConstraints(2, 5, 2, 5, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- label13 ----
				label13.setText("Vendor Reference");
				label13.setFont(new Font("Arial", Font.PLAIN, 14));
				pnlFilter.add(label13, new TableLayoutConstraints(0, 6, 0, 6, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- cmbVendorRef ----
				cmbVendorRef.setFont(new Font("Microsoft Sans Serif", Font.PLAIN, 14));
				cmbVendorRef.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						cmbLineActionPerformed();
					}
				});
				pnlFilter.add(cmbVendorRef, new TableLayoutConstraints(2, 6, 2, 6, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
			}
			panel2.add(pnlFilter, new TableLayoutConstraints(0, 0, 1, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

			//---- lblResult ----
			lblResult.setText("0 Record(s) Found");
			lblResult.setFont(new Font("Arial", Font.BOLD, 14));
			lblResult.setForeground(Color.blue);
			panel2.add(lblResult, new TableLayoutConstraints(0, 1, 1, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

			//---- cmdOpen ----
			cmdOpen.setText("<html>Open&nbsp;&nbsp<font size=-2>[Enter]</html>");
			cmdOpen.setFont(new Font("Arial", Font.PLAIN, 16));
			cmdOpen.setIcon(new ImageIcon(getClass().getResource("/img/open.PNG")));
			cmdOpen.setBackground(new Color(0xece9d5));
			cmdOpen.setToolTipText("Click on this to open selected tests");
			cmdOpen.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					cmdOpenActionPerformed();
				}
			});
			panel2.add(cmdOpen, new TableLayoutConstraints(0, 2, 0, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

			//---- cmdCancel ----
			cmdCancel.setText("<html>Cancel&nbsp;&nbsp<font size=-2>[Esc]</html>");
			cmdCancel.setFont(new Font("Arial", Font.PLAIN, 16));
			cmdCancel.setIcon(new ImageIcon(getClass().getResource("/img/exit.PNG")));
			cmdCancel.setToolTipText("Click on this to close this window");
			cmdCancel.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					cmdCancelActionPerformed();
				}
			});
			panel2.add(cmdCancel, new TableLayoutConstraints(1, 2, 1, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
		}
		contentPane.add(panel2, new TableLayoutConstraints(1, 1, 1, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
		pack();
		setLocationRelativeTo(getOwner());
		// JFormDesigner - End of component initialization  //GEN-END:initComponents
	}

	// JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
	private JPanel panel2;
	private JPanel pnlFilter;
	private JLabel label5;
	private JComboBox cmbType;
	private JLabel label12;
	private JLabel label8;
	private JDateChooser fromDt;
	private JLabel label9;
	private JDateChooser toDt;
	private JLabel label4;
	private JLabel label10;
	private JTextField txtSlNoFrom;
	private JLabel label11;
	private JTextField txtSlNoTo;
	private JLabel label7;
	private JComboBox cmbRes;
	private JLabel label6;
	private JComboBox cmbRemarks;
	private JLabel label3;
	private JComboBox cmbLine;
	private JLabel label13;
	private JComboBox cmbVendorRef;
	private JLabel lblResult;
	private JButton cmdOpen;
	private JButton cmdCancel;
	// JFormDesigner - End of variables declaration  //GEN-END:variables

	// custom code - begin
	private void custInit() {
		// associate func keys
		associateFunctionKeys();
		
		// other ini
		ResultSet res = null;
		try {
			
			conn = DriverManager.getConnection(Configuration.DB_URL);
			stmt = conn.createStatement();
			stmt.setQueryTimeout(30);
			
			// load existing stator type and choose the default one
			res = stmt.executeQuery("select type from " + Configuration.STATOR_TYPE);
			while (res.next()) {
				cmbType.addItem(res.getString("type"));
			}
			
			if (statorType.isEmpty()) {
				cmbType.setSelectedIndex(0);
			}
			else {
				cmbType.setSelectedItem(statorType);
			}
			res.close();
			
			cmbRes.addItem("ALL");
			cmbRes.addItem("PASS");
			cmbRes.addItem("FAIL");
			
			// load existing remark
			cmbRemarks.addItem("ALL");
			res = stmt.executeQuery("select distinct(remark) as remark from " + Configuration.READING_DETAIL + " where remark <> ''");
			while (res.next()) {
				cmbRemarks.addItem(res.getString("remark"));
			}
			res.close();
			
			// load existing vendor ref
			cmbVendorRef.addItem("ALL");
			res = stmt.executeQuery("select distinct(vendor_ref) as vendor_ref from " + Configuration.READING_DETAIL + " where vendor_ref <> ''");
			while (res.next()) {
				cmbVendorRef.addItem(res.getString("vendor_ref"));
			}
			res.close();
						
			// load existing lines
			cmbLine.addItem("ALL");
			for (int i=1; i<=Integer.parseInt(Configuration.NUMBER_OF_STATIONS); i++) {
				cmbLine.addItem("" + i);
			}
			cmbLine.setSelectedItem(Configuration.LINE_NAME);
			
			
		} catch (SQLException se) {
			if (se.getMessage() != null) {
				JOptionPane.showMessageDialog(this, "DB Error:" + se.getMessage());
			}
			return;
		} 
	}
	
	private void loadExistingTest() {
		// load available tests based on filter selection
		if (initProg) {
			return;
		}
		
		filterText = "";
		result = 0;
		String typeFilter = "";
		String dateFilter = "";
		String snoFilter = "";
		String resFilter = "";
		String remFilter = "";
		String lineFilter = "";
		String vendorFilter = "";
		String filterTitle = "Filter [None]";
		if ( cmbType.getSelectedIndex() >= 0) {
			typeFilter = "stator_type='" + cmbType.getSelectedItem().toString() + "'";
		}
		
		if (fromDt.getDate() != null && toDt.getDate() != null) {
			if (!fromDt.getDate().toString().isEmpty() && !toDt.getDate().toString().isEmpty()) {
			dateFilter = "test_date between '" + dbDtFormat.format(fromDt.getDate()) + "' and '" + dbDtFormat.format(toDt.getDate()) + "'";
			}
		}
		
		if (!txtSlNoFrom.getText().isEmpty() && !txtSlNoTo.getText().isEmpty()) {
			snoFilter = "test_slno between '" + txtSlNoFrom.getText() + "' and '" + txtSlNoTo.getText() + "'";
		}
		
		if ( cmbRes.getSelectedIndex() > 0) {
			resFilter = "upper(test_result)='" + cmbRes.getSelectedItem().toString() + "'";
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
		
		if (!typeFilter.isEmpty() || !dateFilter.isEmpty() || !snoFilter.isEmpty() || !resFilter.isEmpty() || !remFilter.isEmpty() || !vendorFilter.isEmpty() 
				|| !lineFilter.isEmpty() ) {
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
			filterTitle = "Filter [" + filterText + "]";
		}
		
		pnlFilter.setBorder(new TitledBorder(null, filterTitle, TitledBorder.LEADING, TitledBorder.DEFAULT_POSITION,
				new Font("Trebuchet MS", Font.PLAIN, 12), new Color(0, 102, 153)));
		pnlFilter.setToolTipText(filterTitle);
		
		try {
			ResultSet res = stmt.executeQuery("select count(distinct(test_slno)) as tot from " + Configuration.READING_DETAIL + " " + filterText);
			result = res.getLong("tot");
			if (res != null) {
				res.close();
			}
				
		} catch (Exception se) {
			if (se.getMessage() != null) {
				JOptionPane.showMessageDialog(this, "Error loading the list:" + se.getMessage());
			}
		}
		lblResult.setText(result + " Test(s) Found");
	}
	
	private void associateFunctionKeys() {
		// associate enter for choose
		String CHOOSE_ACTION_KEY = "chooseAction";
		Action chooseAction = new AbstractAction() {
		      public void actionPerformed(ActionEvent actionEvent) {
		        cmdOpenActionPerformed();
		      }
		    };
		KeyStroke entr = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);
		InputMap chooseInputMap = cmdOpen.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
		chooseInputMap.put(entr, CHOOSE_ACTION_KEY);
		ActionMap chooseActionMap = cmdOpen.getActionMap();
		chooseActionMap.put(CHOOSE_ACTION_KEY, chooseAction);
		cmdOpen.setActionMap(chooseActionMap);
		
		// associate Esc for exit
		String CLOSE_ACTION_KEY = "closeAction";
		Action closeAction = new AbstractAction() {
		      public void actionPerformed(ActionEvent actionEvent) {
		        cmdCancelActionPerformed();
		      }
		    };
	    KeyStroke esc = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
		InputMap closeInputMap = cmdCancel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
		closeInputMap.put(esc, CLOSE_ACTION_KEY);
		ActionMap closeActionMap = cmdCancel.getActionMap();
		closeActionMap.put(CLOSE_ACTION_KEY, closeAction);
		cmdCancel.setActionMap(closeActionMap);
	}
	
	private Connection conn = null;
	private Statement stmt = null;
	private String statorType = ""; 
	private SimpleDateFormat reqDtFormat = new SimpleDateFormat("dd-MM-yyyy");
	private SimpleDateFormat dbDtFormat = new SimpleDateFormat("yyyy-MM-dd");
	private StatorView mainFormRef;
	private String filterText = "";
	private long result = 0;
	private boolean initProg = false;
	
	// custom code - end
}
