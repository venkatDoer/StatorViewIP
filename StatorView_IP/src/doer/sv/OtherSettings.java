/*
 * Created by JFormDesigner on Thu Nov 08 12:01:44 IST 2012
 */

package doer.sv;

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import javax.swing.*;
import javax.swing.border.*;

import org.jfree.ui.FilesystemFilter;

import doer.lic.LicenseFile;
import doer.print.PrintBarcode;
import info.clearthought.layout.*;

/**
 * @author VENKATESAN SELVARAJ
 */
public class OtherSettings extends JDialog {
	public OtherSettings(Frame owner) {
		super(owner);
		initComponents();
		customInit();
	}

	public OtherSettings(Dialog owner) {
		super(owner);
		initComponents();
	}

	private void cmdSaveActionPerformed() {
		// save the changes
		int res = JOptionPane.showConfirmDialog(this, "Save the changes?");
		if ( res != 0 ) {
			return;
		}
		this.setCursor(new Cursor(Cursor.WAIT_CURSOR));
		
		// automatic backup
		Configuration.LAST_USED_BACKUP_LOCATION = txtBkLoc.getText();
		Configuration.saveCommonConfigValues("LAST_USED_BACKUP_LOCATION");
		if (!cmbDuration.getSelectedItem().toString().equals(Configuration.LAST_USED_BACKUP_DURATION)) {
			Configuration.LAST_USED_BACKUP_DURATION = cmbDuration.getSelectedItem().toString();
			
			Calendar cal = Calendar.getInstance();
			cal.add(Calendar.DATE, cmbDuration.getSelectedIndex()+1);
			Date dt = cal.getTime();
			
			Configuration.NEXT_BACKUP_DATE = reqDtFormat.format(dt);
			lblNext.setText(Configuration.NEXT_BACKUP_DATE);
			
			Configuration.saveCommonConfigValues("LAST_USED_BACKUP_DURATION", "NEXT_BACKUP_DATE");
		}
		
		//db name
		String dbMsg = "";
		if (!Configuration.APP_DB_NAME.equals(txtDB.getText())) {
			try {
				Configuration.APP_DB_NAME = txtDB.getText();
				LicenseFile lFile = new LicenseFile(Configuration.APP_DIR + Configuration.CONFIG_DIR + Configuration.CONFIG_FILE_LIC);
				lFile.setDbName(Configuration.APP_DB_NAME);
				lFile.rewriteFile();
				dbMsg = "\nNote:Database change will be effective only up on relogin to application";
			} catch (Exception e) {
				JOptionPane.showMessageDialog(this, "Error updating DB name:" + e.getMessage());
			}
		}
		
		this.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
		JOptionPane.showMessageDialog(this, "Changes are saved successfully!" + dbMsg); 
		
	}

	private void cmdExitActionPerformed() {
		this.setVisible(false);
	}

	private void cmdBackupActionPerformed() {
		// on demand back up
		try {
			setCursor(new Cursor(Cursor.WAIT_CURSOR));
			Configuration.backupData(txtBkLoc.getText());
			Configuration.LAST_BACKUP_DATE = reqDtFormat.format(Calendar.getInstance().getTime());
			Configuration.saveCommonConfigValues("LAST_BACKUP_DATE");
			lblDate.setText(Configuration.LAST_BACKUP_DATE);
		} catch (Exception e) {
			JOptionPane.showMessageDialog(this, e.getMessage());
		}
		setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
	}

	private void cmdChangeActionPerformed() {
		JFileChooser fileDlg = new JFileChooser();
		fileDlg.setFileSelectionMode(1); // directories only
		fileDlg.setDialogTitle("Choose backup folder");
		fileDlg.showOpenDialog(this);
		File selFile = fileDlg.getSelectedFile();
		if (selFile == null) {
			return;
		} else {
			txtBkLoc.setText(selFile.getAbsolutePath());
			
		}
	}

	private void cmdChangeDBActionPerformed() {
		JOptionPane.showMessageDialog (this, "WARNING:Changing current database will affect configuration and test data (after relogin) based on new database you choose");
		JFileChooser fileDlg = new JFileChooser();
		fileDlg.setFileFilter(new FilesystemFilter("db", "PumpViewPro Database (*.db)"));
		fileDlg.setDialogTitle("Choose Database");
		fileDlg.showOpenDialog(this);
		File selFile = fileDlg.getSelectedFile();
		if (selFile == null) {
			return;
		} else if (!selFile.getName().endsWith(".db")){
			JOptionPane.showMessageDialog(this, "Invalid database file. Please try again!","Error", JOptionPane.ERROR_MESSAGE);
		} else {
			txtDB.setText(selFile.getAbsolutePath());
		}
	}

	private void cmdPrintLblActionPerformed() {
		// save the changes
		Configuration.QR_CODE_PRINTER_PATH = txtPath.getText();
		Configuration.saveCommonConfigValues("QR_CODE_PRINTER_PATH");
		
		// print test qr code
		PrintBarcode pq = new PrintBarcode();
		SimpleDateFormat reqDtFormat = new SimpleDateFormat("dd-MM-yyyy");
		try {
			String line3 = "";
			String line4 = "";
			if (Configuration.QR_LINE3.equals("SM")) {
				line3 = "TEST MODEL";
			} else if (Configuration.QR_LINE3.equals("VR")) {
				line3 = "TEST VENDOR";
			} else {
				line3 = Configuration.USER;
			}
			if (Configuration.QR_LINE4.equals("SM")) {
				line4 = "TEST MODEL";
			} else if (Configuration.QR_LINE4.equals("VR")) {
				line4 = "TEST VENDOR";
			} else if (Configuration.QR_LINE4.equals("TN")) {
				line4 = Configuration.USER;
			} else {
				line4 = "";
			}
			String code = "ABCD 12345";
			if (Configuration.QR_IS_INCLUDE_LINE2.equals("YES")) {
				code += "," + reqDtFormat.format(new Date());
			}
			if (Configuration.QR_IS_INCLUDE_LINE3.equals("YES")) {
				code += "," + line3;
			}
			if (Configuration.QR_IS_INCLUDE_LINE4.equals("YES")) {
				code += "," + line4;
			}
			pq.print(code, "ABCD 12345", reqDtFormat.format(new Date()), line3, line4);
		} catch (Exception e) {
			JOptionPane.showMessageDialog(this, e.getMessage());
		}
		
	}

	private void initComponents() {
		// JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
		tabbedPane1 = new JTabbedPane();
		panel1 = new JPanel();
		pnlBackup = new JPanel();
		label6 = new JLabel();
		cmbDuration = new JComboBox();
		label8 = new JLabel();
		label9 = new JLabel();
		txtBkLoc = new JTextField();
		cmdChange = new JButton();
		label10 = new JLabel();
		lblDate = new JLabel();
		label11 = new JLabel();
		lblNext = new JLabel();
		cmdBackup = new JButton();
		pnlDB = new JPanel();
		label12 = new JLabel();
		txtDB = new JTextField();
		cmdChangeDB = new JButton();
		cmdSave = new JButton();
		cmdExit = new JButton();
		panel2 = new JPanel();
		pnlDB2 = new JPanel();
		label1 = new JLabel();
		txtPath = new JTextField();
		cmdPrintLbl = new JButton();
		cmdExit2 = new JButton();

		//======== this ========
		setTitle("Doer StatorView: Other Settings");
		setFont(new Font("Arial", Font.PLAIN, 12));
		setModal(true);
		setResizable(false);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		Container contentPane = getContentPane();
		contentPane.setLayout(new TableLayout(new double[][] {
			{10, TableLayout.FILL, TableLayout.FILL, 10},
			{5, TableLayout.PREFERRED, 10}}));
		((TableLayout)contentPane.getLayout()).setHGap(5);
		((TableLayout)contentPane.getLayout()).setVGap(5);

		//======== tabbedPane1 ========
		{
			tabbedPane1.setFont(new Font("Arial", Font.BOLD, 16));

			//======== panel1 ========
			{
				panel1.setLayout(new TableLayout(new double[][] {
					{TableLayout.FILL, TableLayout.FILL},
					{TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.PREFERRED}}));
				((TableLayout)panel1.getLayout()).setHGap(5);
				((TableLayout)panel1.getLayout()).setVGap(5);

				//======== pnlBackup ========
				{
					pnlBackup.setBorder(new TitledBorder(null, "Backup", TitledBorder.LEADING, TitledBorder.DEFAULT_POSITION,
						new Font("Arial", Font.PLAIN, 16), new Color(0x006699)));
					pnlBackup.setLayout(new TableLayout(new double[][] {
						{5, TableLayout.PREFERRED, 100, TableLayout.PREFERRED, 100, TableLayout.FILL, TableLayout.PREFERRED},
						{TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.PREFERRED}}));
					((TableLayout)pnlBackup.getLayout()).setHGap(5);
					((TableLayout)pnlBackup.getLayout()).setVGap(5);

					//---- label6 ----
					label6.setText("Backup Data For Every");
					label6.setFont(new Font("Arial", Font.PLAIN, 14));
					label6.setIcon(null);
					pnlBackup.add(label6, new TableLayoutConstraints(1, 0, 1, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.CENTER));

					//---- cmbDuration ----
					cmbDuration.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
					cmbDuration.setBackground(Color.white);
					pnlBackup.add(cmbDuration, new TableLayoutConstraints(2, 0, 2, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.CENTER));

					//---- label8 ----
					label8.setText("Days");
					label8.setFont(new Font("Arial", Font.PLAIN, 14));
					label8.setIcon(null);
					pnlBackup.add(label8, new TableLayoutConstraints(3, 0, 3, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.CENTER));

					//---- label9 ----
					label9.setText("Backup Location");
					label9.setFont(new Font("Arial", Font.PLAIN, 14));
					label9.setIcon(null);
					pnlBackup.add(label9, new TableLayoutConstraints(1, 1, 1, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.CENTER));

					//---- txtBkLoc ----
					txtBkLoc.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
					txtBkLoc.setBorder(new LineBorder(Color.lightGray));
					txtBkLoc.setEditable(false);
					txtBkLoc.setBackground(new Color(0xf7f7f7));
					pnlBackup.add(txtBkLoc, new TableLayoutConstraints(2, 1, 5, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

					//---- cmdChange ----
					cmdChange.setText("Change...");
					cmdChange.setFont(new Font("Arial", Font.PLAIN, 14));
					cmdChange.setToolTipText("Click on this to change backup location");
					cmdChange.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							cmdChangeActionPerformed();
						}
					});
					pnlBackup.add(cmdChange, new TableLayoutConstraints(6, 1, 6, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

					//---- label10 ----
					label10.setText("Last Backup Date");
					label10.setFont(new Font("Arial", Font.PLAIN, 14));
					label10.setIcon(null);
					pnlBackup.add(label10, new TableLayoutConstraints(1, 2, 1, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.CENTER));

					//---- lblDate ----
					lblDate.setText("Backup Date");
					lblDate.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
					lblDate.setIcon(null);
					lblDate.setOpaque(true);
					lblDate.setBackground(Color.lightGray);
					lblDate.setHorizontalTextPosition(SwingConstants.LEFT);
					lblDate.setHorizontalAlignment(SwingConstants.CENTER);
					pnlBackup.add(lblDate, new TableLayoutConstraints(2, 2, 2, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.CENTER));

					//---- label11 ----
					label11.setText("Next Backup Date");
					label11.setFont(new Font("Arial", Font.PLAIN, 14));
					label11.setIcon(null);
					pnlBackup.add(label11, new TableLayoutConstraints(3, 2, 3, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.CENTER));

					//---- lblNext ----
					lblNext.setText("Backup Date");
					lblNext.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
					lblNext.setIcon(null);
					lblNext.setOpaque(true);
					lblNext.setBackground(Color.lightGray);
					lblNext.setHorizontalTextPosition(SwingConstants.LEFT);
					lblNext.setHorizontalAlignment(SwingConstants.CENTER);
					pnlBackup.add(lblNext, new TableLayoutConstraints(4, 2, 4, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.CENTER));

					//---- cmdBackup ----
					cmdBackup.setText("Take A Backup Now");
					cmdBackup.setFont(new Font("Arial", Font.PLAIN, 14));
					cmdBackup.setToolTipText("Click on this to take a back on demand. This does not affect scheduled automatic backups");
					cmdBackup.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							cmdBackupActionPerformed();
						}
					});
					pnlBackup.add(cmdBackup, new TableLayoutConstraints(6, 2, 6, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
				}
				panel1.add(pnlBackup, new TableLayoutConstraints(0, 0, 1, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//======== pnlDB ========
				{
					pnlDB.setBorder(new TitledBorder(null, "Database", TitledBorder.LEADING, TitledBorder.DEFAULT_POSITION,
						new Font("Arial", Font.PLAIN, 16), new Color(0x006699)));
					pnlDB.setLayout(new TableLayout(new double[][] {
						{5, 142, TableLayout.FILL, 156},
						{TableLayout.PREFERRED}}));
					((TableLayout)pnlDB.getLayout()).setHGap(5);
					((TableLayout)pnlDB.getLayout()).setVGap(5);

					//---- label12 ----
					label12.setText("Current Database");
					label12.setFont(new Font("Arial", Font.PLAIN, 14));
					label12.setIcon(null);
					pnlDB.add(label12, new TableLayoutConstraints(1, 0, 1, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.CENTER));

					//---- txtDB ----
					txtDB.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
					txtDB.setBorder(new LineBorder(Color.lightGray));
					txtDB.setEditable(false);
					txtDB.setBackground(new Color(0xf7f7f7));
					pnlDB.add(txtDB, new TableLayoutConstraints(2, 0, 2, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

					//---- cmdChangeDB ----
					cmdChangeDB.setText("Change...");
					cmdChangeDB.setFont(new Font("Arial", Font.PLAIN, 14));
					cmdChangeDB.setToolTipText("Click on this to browse your computer and select the database");
					cmdChangeDB.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							cmdChangeDBActionPerformed();
						}
					});
					pnlDB.add(cmdChangeDB, new TableLayoutConstraints(3, 0, 3, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
				}
				panel1.add(pnlDB, new TableLayoutConstraints(0, 1, 1, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- cmdSave ----
				cmdSave.setText("Save");
				cmdSave.setFont(new Font("Arial", Font.PLAIN, 14));
				cmdSave.setIcon(new ImageIcon(getClass().getResource("/img/save.PNG")));
				cmdSave.setToolTipText("Click on this to save the changes");
				cmdSave.setMnemonic('S');
				cmdSave.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						cmdSaveActionPerformed();
					}
				});
				panel1.add(cmdSave, new TableLayoutConstraints(0, 2, 0, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- cmdExit ----
				cmdExit.setText("<html>Close&nbsp;&nbsp<font size=-2>[Esc]</html>");
				cmdExit.setFont(new Font("Arial", Font.PLAIN, 14));
				cmdExit.setIcon(new ImageIcon(getClass().getResource("/img/exit.PNG")));
				cmdExit.setToolTipText("Click on this to close this window");
				cmdExit.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						cmdExitActionPerformed();
					}
				});
				panel1.add(cmdExit, new TableLayoutConstraints(1, 2, 1, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
			}
			tabbedPane1.addTab("Bakup and DB Settings", panel1);

			//======== panel2 ========
			{
				panel2.setLayout(new TableLayout(new double[][] {
					{0, TableLayout.FILL, TableLayout.FILL},
					{5, TableLayout.FILL, TableLayout.PREFERRED}}));
				((TableLayout)panel2.getLayout()).setHGap(5);
				((TableLayout)panel2.getLayout()).setVGap(5);

				//======== pnlDB2 ========
				{
					pnlDB2.setBorder(new TitledBorder(null, "Printer Settings", TitledBorder.LEADING, TitledBorder.DEFAULT_POSITION,
						new Font("Arial", Font.PLAIN, 16), new Color(0x006699)));
					pnlDB2.setLayout(new TableLayout(new double[][] {
						{5, 142, TableLayout.FILL},
						{TableLayout.PREFERRED}}));
					((TableLayout)pnlDB2.getLayout()).setHGap(5);
					((TableLayout)pnlDB2.getLayout()).setVGap(5);

					//---- label1 ----
					label1.setText("QR Code Printer Path");
					label1.setFont(new Font("Arial", Font.PLAIN, 14));
					pnlDB2.add(label1, new TableLayoutConstraints(1, 0, 1, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

					//---- txtPath ----
					txtPath.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
					pnlDB2.add(txtPath, new TableLayoutConstraints(2, 0, 2, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
				}
				panel2.add(pnlDB2, new TableLayoutConstraints(1, 1, 2, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- cmdPrintLbl ----
				cmdPrintLbl.setText("Save and Print Test Label");
				cmdPrintLbl.setFont(new Font("Arial", Font.PLAIN, 14));
				cmdPrintLbl.setIcon(new ImageIcon(getClass().getResource("/img/save.PNG")));
				cmdPrintLbl.setToolTipText("Click on this to save the changes");
				cmdPrintLbl.setMnemonic('S');
				cmdPrintLbl.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						cmdPrintLblActionPerformed();
					}
				});
				panel2.add(cmdPrintLbl, new TableLayoutConstraints(1, 2, 1, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- cmdExit2 ----
				cmdExit2.setText("<html>Close&nbsp;&nbsp<font size=-2>[Esc]</html>");
				cmdExit2.setFont(new Font("Arial", Font.PLAIN, 14));
				cmdExit2.setIcon(new ImageIcon(getClass().getResource("/img/exit.PNG")));
				cmdExit2.setToolTipText("Click on this to close this window");
				cmdExit2.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						cmdExitActionPerformed();
					}
				});
				panel2.add(cmdExit2, new TableLayoutConstraints(2, 2, 2, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
			}
			tabbedPane1.addTab("QR Code", panel2);
		}
		contentPane.add(tabbedPane1, new TableLayoutConstraints(1, 1, 2, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
		setSize(770, 310);
		setLocationRelativeTo(getOwner());
		// JFormDesigner - End of component initialization  //GEN-END:initComponents
	}

	// JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
	private JTabbedPane tabbedPane1;
	private JPanel panel1;
	private JPanel pnlBackup;
	private JLabel label6;
	private JComboBox cmbDuration;
	private JLabel label8;
	private JLabel label9;
	private JTextField txtBkLoc;
	private JButton cmdChange;
	private JLabel label10;
	private JLabel lblDate;
	private JLabel label11;
	private JLabel lblNext;
	private JButton cmdBackup;
	private JPanel pnlDB;
	private JLabel label12;
	private JTextField txtDB;
	private JButton cmdChangeDB;
	private JButton cmdSave;
	private JButton cmdExit;
	private JPanel panel2;
	private JPanel pnlDB2;
	private JLabel label1;
	private JTextField txtPath;
	private JButton cmdPrintLbl;
	private JButton cmdExit2;
	// JFormDesigner - End of variables declaration  //GEN-END:variables

	// custom variables and functions
	private SimpleDateFormat reqDtFormat = new SimpleDateFormat("dd-MM-yyyy");
	
	private void customInit() {
		associateFunctionKeys();
		
		// load duration
		for (int i=1; i<=90;i++) {
			cmbDuration.addItem(i);
		}
		cmbDuration.setSelectedItem(Integer.valueOf(Configuration.LAST_USED_BACKUP_DURATION));
		
		txtDB.setText(Configuration.APP_DB_NAME);
		txtDB.setCaretPosition(0);
		txtBkLoc.setText(Configuration.LAST_USED_BACKUP_LOCATION);
		txtBkLoc.setCaretPosition(0);
		lblDate.setText(Configuration.LAST_BACKUP_DATE);
		lblNext.setText(Configuration.NEXT_BACKUP_DATE);
		txtPath.setText(Configuration.QR_CODE_PRINTER_PATH);
	}
	
private void associateFunctionKeys() {
		
		// associate f4 for save
		/*String SAVE_ACTION_KEY = "saveAction";
		Action saveAction = new AbstractAction() {
		      public void actionPerformed(ActionEvent actionEvent) {
		        cmdSaveActionPerformed();
		      }
		    };
		KeyStroke f4 = KeyStroke.getKeyStroke(KeyEvent.VK_F4, 0);
		InputMap saveInputMap = cmdSave.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
		saveInputMap.put(f4, SAVE_ACTION_KEY);
		ActionMap saveActionMap = cmdSave.getActionMap();
		saveActionMap.put(SAVE_ACTION_KEY, saveAction);
		cmdSave.setActionMap(saveActionMap);*/
		
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
		
		closeInputMap = cmdExit2.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
		closeInputMap.put(esc, CLOSE_ACTION_KEY);
		closeActionMap = cmdExit2.getActionMap();
		closeActionMap.put(CLOSE_ACTION_KEY, closeAction);
		cmdExit2.setActionMap(closeActionMap);
	}
}
