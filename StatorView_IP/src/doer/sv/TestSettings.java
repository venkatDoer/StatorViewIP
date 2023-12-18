package doer.sv;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dialog;
import java.awt.Font;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstraints;

public class TestSettings extends JDialog {
	// CUSTOM CODE - BEGIN
	StatorView frmMain = null;
	String curPhase = "";
	private Cursor waitCursor = new Cursor(Cursor.WAIT_CURSOR);
	private Cursor defCursor = new Cursor(Cursor.DEFAULT_CURSOR);
	// CUSTOM CODE - END
		
	public TestSettings(Frame owner, String phase) {
		super(owner);
		frmMain = (StatorView) owner;
		curPhase = phase;
		initComponents();
		loadSettings();
	}
	
	public TestSettings(Dialog owner) {
		super(owner);
		initComponents();
	}
	
	// function to load existing settings
	private void loadSettings() {
		// test settings
		chkRes.setSelected(Configuration.IS_RES_DISABLED.equals("0"));
		chkCommon.setSelected(Configuration.IS_COM_RES_DISABLED.equals("1"));
		chkInsRes.setSelected(Configuration.IS_INS_RES_DISABLED.equals("0"));
		chkHV.setSelected(Configuration.IS_HV_DISABLED.equals("0"));
		chkInsResAfterHV.setSelected(Configuration.IS_INS_RES_AFTER_HV_DISABLED.equals("0"));
		chkSurge.setSelected(Configuration.IS_SURGE_DISABLED.equals("0"));
		chkDir.setSelected(Configuration.IS_DIR_TEST_DISABLED.equals("0"));
		
		txtRelStCoil.setText(Configuration.REL_TIME_START_COIL);
		txtRelRunCoil.setText(Configuration.REL_TIME_RUN_COIL);
		txtRelCom.setText(Configuration.REL_TIME_COM_COIL);
		txtRelIns.setText(Configuration.REL_TIME_INS_RES);
		txtRelHV.setText(Configuration.REL_TIME_HV);
		txtRelSurge.setText(Configuration.REL_TIME_SURGE);
		txtSWNo.setText(Configuration.NUMBER_OF_SURGE_IMG);
		optCapWaveYes.setSelected(Configuration.IS_SURGE_WAVE_CAPTURED.equals("1"));
		optCapWaveNo.setSelected(Configuration.IS_SURGE_WAVE_CAPTURED.equals("0"));
		txtSWInt.setText(Configuration.SURGE_WAVE_INTERVAL);
		txtRelDir.setText(Configuration.REL_TIME_DIR);
		
		chkResActionPerformed();
		chkInsResActionPerformed();
		chkHVActionPerformed();
		chkDisSurgeActionPerformed();
		
		// label changes according to phase
		if (curPhase.equals("Three")) {
			lblStCl.setText("Reliable Time For R COIL Reading");
			lblRunCl.setText("Reliable Time For Y COIL Reading");
			lblComCoil.setText("Reliable Time For B COIL Reading");
			chkCommon.setSelected(false);
			chkCommon.setEnabled(false);
		}
		
		// SNO settings
		optSNoAuto.setSelected(Configuration.SNO_GEN_METHOD.equals("A"));
		optSNoBarCode.setSelected(Configuration.SNO_GEN_METHOD.equals("S"));
		optSNoBoth.setSelected(Configuration.SNO_GEN_METHOD.equals("B"));
		chkPrependDt.setSelected(Configuration.IS_PREPEND_DATE_TO_SNO.equals("1"));
		chkModel.setSelected(Configuration.IS_MODEL_FROM_BARCODE.equals("1"));
		optDelimiter.setSelected(Configuration.IS_MODEL_FROM_BARCODE_DELIMITER_BASED.equals("1"));
		optPosition.setSelected(Configuration.IS_MODEL_FROM_BARCODE_DELIMITER_BASED.equals("0"));
		txtDelimiter.setText(Configuration.MODEL_DELIMITER_IN_BARCODE);
		txtLocation.setText(Configuration.MODEL_LOCATION_IN_BARCODE);
		txtModelStartPos.setText(Configuration.MODEL_START_POS_IN_BARCODE);
		txtModelEndPos.setText(Configuration.MODEL_END_POS_IN_BARCODE);
		chkSNo.setSelected(Configuration.IS_CONFIRM_SNO.equals("1"));
		chkFailSNo.setSelected(Configuration.IS_GEN_SNO_FOR_FAILED_TEST.equals("1"));
		// serial number settings
		getLatestSNo();
		
		chkPrintQR.setSelected(Configuration.IS_QR_PRINT_ENABLED.equals("1"));
		cmbQR.setSelectedIndex(Configuration.IS_QR_PRINT_ENABLED_FOR_FAIL.equals("0") ? 0 : 1);
		
		cmbLine3.setSelectedIndex(Configuration.QR_LINE3.equals("SM") ? 0 : Configuration.QR_LINE3.equals("VR") ? 1 : 2);
		cmbLine4.setSelectedIndex(Configuration.QR_LINE4.equals("NO") ? 0 : Configuration.QR_LINE4.equals("SM") ? 1 : Configuration.QR_LINE4.equals("VR") ? 2 : 3);
		chkQRIncLine2.setSelected(Configuration.QR_IS_INCLUDE_LINE2.equals("YES"));
		chkQRIncLine3.setSelected(Configuration.QR_IS_INCLUDE_LINE3.equals("YES"));
		chkQRIncLine4.setSelected(Configuration.QR_IS_INCLUDE_LINE4.equals("YES"));
		
		
		optSNoAutoItemStateChanged();
		cmbLine4();
	}
	
	private void getLatestSNo() {
		Configuration.RECENT_STATOR_SNO = Configuration.getCommonConfigValue("RECENT_STATOR_SNO");
		txtRecentNo.setText(Configuration.RECENT_STATOR_SNO);
		txtRecentNoKeyReleased();
	}
	
	// function to update the config values
	private void saveSettings() {
		// test settings
		String prevResSt = Configuration.IS_RES_DISABLED;
		Configuration.IS_RES_DISABLED = chkRes.isSelected()?"0":"1";
		String prevComSt = Configuration.IS_COM_RES_DISABLED;
		Configuration.IS_COM_RES_DISABLED = chkCommon.isSelected()?"1":"0";
		String prevInsResSt = Configuration.IS_INS_RES_DISABLED;
		Configuration.IS_INS_RES_DISABLED = chkInsRes.isSelected()?"0":"1";
		String prevInsResAfterHVSt = Configuration.IS_INS_RES_AFTER_HV_DISABLED;
		Configuration.IS_INS_RES_AFTER_HV_DISABLED = chkInsResAfterHV.isSelected()?"0":"1";
		String prevHVSt = Configuration.IS_HV_DISABLED;
		Configuration.IS_HV_DISABLED = chkHV.isSelected()?"0":"1";
		String prevSurgeSt = Configuration.IS_SURGE_DISABLED;
		Configuration.IS_SURGE_DISABLED = chkSurge.isSelected()?"0":"1";
		String prevDirSt = Configuration.IS_DIR_TEST_DISABLED;
		Configuration.IS_DIR_TEST_DISABLED = chkDir.isSelected()?"0":"1";
		
		Configuration.REL_TIME_START_COIL = txtRelStCoil.getText();
		Configuration.REL_TIME_RUN_COIL = txtRelRunCoil.getText();
		Configuration.REL_TIME_COM_COIL = txtRelCom.getText();
		Configuration.REL_TIME_HV = txtRelHV.getText();
		Configuration.REL_TIME_INS_RES = txtRelIns.getText();
		Configuration.REL_TIME_SURGE = txtRelSurge.getText();
		Configuration.NUMBER_OF_SURGE_IMG = txtSWNo.getText();
		String prevWaveOpt = Configuration.IS_SURGE_WAVE_CAPTURED;
		Configuration.IS_SURGE_WAVE_CAPTURED = optCapWaveYes.isSelected() ? "1":"0";
		Configuration.SURGE_WAVE_INTERVAL = txtSWInt.getText();
		Configuration.REL_TIME_DIR = txtRelDir.getText();
		Configuration.saveCommonConfigValues("IS_COM_RES_DISABLED", "IS_RES_DISABLED", "IS_INS_RES_DISABLED", "IS_HV_DISABLED", "IS_INS_RES_AFTER_HV_DISABLED", "IS_SURGE_DISABLED","REL_TIME_START_COIL","REL_TIME_RUN_COIL", "REL_TIME_COM_COIL", "REL_TIME_INS_RES", "REL_TIME_HV", "REL_TIME_SURGE", "REL_TIME_DIR", "NUMBER_OF_SURGE_IMG", "IS_SURGE_WAVE_CAPTURED", "SURGE_WAVE_INTERVAL", "IS_DIR_TEST_DISABLED");
		
		// enable / disable  tests in main window
		if (!prevResSt.equals(Configuration.IS_RES_DISABLED) || !prevComSt.equals(Configuration.IS_COM_RES_DISABLED) || !prevInsResSt.equals(Configuration.IS_INS_RES_DISABLED) || !prevInsResAfterHVSt.equals(Configuration.IS_INS_RES_AFTER_HV_DISABLED) || !prevHVSt.equals(Configuration.IS_HV_DISABLED) || !prevSurgeSt.equals(Configuration.IS_SURGE_DISABLED) || !prevWaveOpt.equals(Configuration.IS_SURGE_WAVE_CAPTURED) || !prevDirSt.equals(Configuration.IS_DIR_TEST_DISABLED)) {
			frmMain.enableTests();
		}
		// restart tests
		frmMain.restartTest();
		frmMain.enableDisableScanner();
	}

	private void cmdSaveActionPerformed() {
		// required field validation
		if (!(chkRes.isSelected() || chkInsRes.isSelected() || chkHV.isSelected() || chkInsResAfterHV.isSelected() || chkSurge.isSelected() || chkDir.isSelected() ) ) {
			JOptionPane.showMessageDialog(this, "It is not possible to disable all tests, atleast one has to be enabled");
			chkRes.requestFocus();
			return;
		}
		if (chkRes.isSelected()) {
			if (!mandatoryCheck(pnlRes)) {
				return;
			}
		}
		if (chkInsRes.isSelected() || chkInsResAfterHV.isSelected()) {
			if (!mandatoryCheck(pnlInsRes)) {
				return;
			}
		}
		if (chkHV.isSelected()) {
			if (!mandatoryCheck(pnlHV)) {
				return;
			}
		}
		if (chkSurge.isSelected()) {
			if (!mandatoryCheck(pnlSurge)) {
				return;
			}
		}
		if (chkDir.isSelected()) {
			if (!mandatoryCheck(pnlDir)) {
				return;
			}
		}
				
		this.setCursor(waitCursor);
		saveSettings();
		this.setCursor(defCursor);
		JOptionPane.showMessageDialog(this, "Changes are saved successfully!"); 
	}
	
	private Boolean mandatoryCheck(JPanel pnl) {
		JTextField txtFld = new JTextField();
		for(Component comp : pnl.getComponents()) {
			if (comp instanceof JTextField) {
				txtFld = (JTextField) comp;
				if (!txtFld.getCaretColor().equals(Color.darkGray) && txtFld.isVisible() && txtFld.isEnabled()) {
					try {
						Float.valueOf(txtFld.getText());
					} catch (NumberFormatException e) {
						JOptionPane.showMessageDialog(this, "Please enter valid number for " + txtFld.getToolTipText(), "Error", JOptionPane.ERROR_MESSAGE);
						txtFld.requestFocusInWindow();
						return false;
					}
				}
			}
		}
		return true;
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

	private void cmdExitActionPerformed() {
		this.setVisible(false);
	}


	private void chkDisSurgeActionPerformed() {
		boolean surgeSt = chkSurge.isSelected();
		txtSWNo.setEnabled(surgeSt);
		optCapWaveYes.setEnabled(surgeSt);
		optCapWaveNo.setEnabled(surgeSt);
		optCapWaveYesActionPerformed();
	}

	private void chkResActionPerformed() {
		if (chkRes.isSelected()) {
			txtRelStCoil.setEnabled(true);
			txtRelRunCoil.setEnabled(true);
			txtRelCom.setEnabled(true);
			chkCommon.setEnabled(true);
		} else {
			txtRelStCoil.setEnabled(false);
			txtRelRunCoil.setEnabled(false);
			txtRelCom.setEnabled(false);
			chkCommon.setEnabled(false);
		}
	}

	private void chkInsResActionPerformed() {
		txtRelIns.setEnabled(chkInsResAfterHV.isSelected() || chkInsRes.isSelected());
	}

	private void chkHVActionPerformed() {
		txtRelHV.setEnabled(chkHV.isSelected());
	}

	private void chkCommonActionPerformed() {
		if (chkCommon.isSelected()) {
			lblComCoil.setEnabled(false);
			txtRelCom.setEnabled(false);
		} else {
			lblComCoil.setEnabled(true);
			txtRelCom.setEnabled(true);
		}
	}

	private void chkDirActionPerformed() {
		txtRelDir.setEnabled(chkDir.isSelected());
	}

	private void chkInsResAfterHVActionPerformed() {
		txtRelIns.setEnabled(chkInsResAfterHV.isSelected() || chkInsRes.isSelected());
	}

	private void optCapWaveYesActionPerformed() {
		if (optCapWaveYes.isSelected() && chkSurge.isSelected()) {
			txtSWNo.setEnabled(true);
			txtSWInt.setEnabled(true);
		} else {
			txtSWNo.setEnabled(false);
			txtSWInt.setEnabled(false);
		}
	}

	private void optCapWaveNoActionPerformed() {
		optCapWaveYesActionPerformed();
	}
	
	private void optSNoAutoItemStateChanged() {
		Boolean isAuto = optSNoAuto.isSelected();
		Boolean isBoth = optSNoBoth.isSelected();
		pnlSNoAuto.setBackground(isAuto || isBoth ? this.getBackground() : Color.lightGray);
		pnlSNoBarcode.setBackground(!isAuto || isBoth ? this.getBackground() : Color.lightGray);
		lblRecentNo.setEnabled(isAuto || isBoth);
		txtRecentNo.setEnabled(isAuto || isBoth);
		lbllblNSno.setEnabled(isAuto || isBoth);
		lblNSno.setEnabled(isAuto || isBoth);
		chkFailSNo.setEnabled(isAuto || isBoth);
		chkSNo.setEnabled(isAuto || isBoth);
		chkModel.setEnabled(!isAuto || isBoth);
		chkModelItemStateChanged();
	}

	private void chkModelItemStateChanged() {
		optDelimiter.setEnabled(chkModel.isSelected() && chkModel.isEnabled());
		optPosition.setEnabled(chkModel.isSelected() && chkModel.isEnabled());
		optDelimiterItemStateChanged();
		optPositionItemStateChanged();
	}

	private void optDelimiterItemStateChanged() {
		Boolean isDel = optDelimiter.isSelected() && optDelimiter.isEnabled();
		lblDelimiter.setEnabled(isDel);
		lblLocation.setEnabled(isDel);
		txtDelimiter.setEnabled(isDel);
		txtLocation.setEnabled(isDel);
	}

	private void optPositionItemStateChanged() {
		Boolean isPos = optPosition.isSelected() && optPosition.isEnabled();
		lblModelPos.setEnabled(isPos);
		lblModelPos2.setEnabled(isPos);
		txtModelStartPos.setEnabled(isPos);
		txtModelEndPos.setEnabled(isPos);
	}

	private void txtRecentNoKeyReleased() {
		String lastUsedSNo = txtRecentNo.getText();
		String nextSno = Configuration.findNextNo(lastUsedSNo);
		if (chkPrependDt.isSelected()) {
			java.util.Date today = Calendar.getInstance().getTime();
			SimpleDateFormat reqDtFormat = new SimpleDateFormat("yyMMdd");
			nextSno = reqDtFormat.format(today) + "-" + nextSno;
		}
		lblNSno.setText(nextSno);
	}

	private void cmdSaveSNoActionPerformed() {
		Configuration.SNO_GEN_METHOD = optSNoAuto.isSelected()?"A":optSNoBarCode.isSelected()?"S":"B";
		Configuration.IS_PREPEND_DATE_TO_SNO = chkPrependDt.isSelected() ? "1" : "0";
		Configuration.IS_MODEL_FROM_BARCODE = chkModel.isSelected() ? "1" : "0";
		Configuration.IS_MODEL_FROM_BARCODE_DELIMITER_BASED = optDelimiter.isSelected() ? "1" : "0";
		Configuration.MODEL_DELIMITER_IN_BARCODE = txtDelimiter.getText().toString().trim();
		try {
			if (optDelimiter.isSelected()) {
				Configuration.MODEL_LOCATION_IN_BARCODE = Integer.valueOf(txtLocation.getText().toString().trim()).toString();
			}
		} catch (NumberFormatException ne) {
			JOptionPane.showMessageDialog(this, "Enter valid number for location of model");
			txtLocation.requestFocus();
			return;
		}
		
		try {
			if (optPosition.isSelected()) {
				Configuration.MODEL_START_POS_IN_BARCODE = Integer.valueOf(txtModelStartPos.getText().toString()).toString();
				Configuration.MODEL_END_POS_IN_BARCODE = Integer.valueOf(txtModelEndPos.getText().toString()).toString();
			}
		} catch (NumberFormatException ne) {
			JOptionPane.showMessageDialog(this, "Enter valid numbers for start and end position of model");
			txtModelStartPos.requestFocus();
			return;
		}
		
		Configuration.IS_CONFIRM_SNO = chkSNo.isSelected() ? "1" : "0";
		Configuration.IS_GEN_SNO_FOR_FAILED_TEST = chkFailSNo.isSelected() ? "1" : "0";
		Configuration.IS_QR_PRINT_ENABLED = chkPrintQR.isSelected()?"1":"0";
		Configuration.IS_QR_PRINT_ENABLED_FOR_FAIL = cmbQR.getSelectedIndex() == 0 ? "0" : "1";
		Configuration.QR_LINE3 = cmbLine3.getSelectedIndex() == 0 ? "SM" : cmbLine3.getSelectedIndex() == 1 ? "VR"  : "TN";
		Configuration.QR_LINE4 = cmbLine4.getSelectedIndex() == 0 ? "NO" : cmbLine4.getSelectedIndex() == 1 ? "SM" : cmbLine4.getSelectedIndex() == 2 ? "VR"  : "TN";
		Configuration.QR_IS_INCLUDE_LINE2 = chkQRIncLine2.isSelected() ? "YES" : "NO";
		Configuration.QR_IS_INCLUDE_LINE3 = chkQRIncLine3.isSelected() ? "YES" : "NO";
		Configuration.QR_IS_INCLUDE_LINE4 = chkQRIncLine4.isSelected() && chkQRIncLine4.isEnabled() ? "YES" : "NO";
		
		Configuration.saveConfigValues("SNO_GEN_METHOD", "IS_PREPEND_DATE_TO_SNO", "IS_MODEL_FROM_BARCODE", "IS_MODEL_FROM_BARCODE_DELIMITER_BASED", "MODEL_DELIMITER_IN_BARCODE", "MODEL_LOCATION_IN_BARCODE", "MODEL_START_POS_IN_BARCODE", "MODEL_END_POS_IN_BARCODE", "IS_CONFIRM_SNO", "IS_GEN_SNO_FOR_FAILED_TEST", "IS_QR_PRINT_ENABLED","IS_QR_PRINT_ENABLED_FOR_FAIL", "QR_LINE3", "QR_LINE4", "QR_IS_INCLUDE_LINE2", "QR_IS_INCLUDE_LINE3", "QR_IS_INCLUDE_LINE4");
		
		// check for sno change
		if (!Configuration.RECENT_STATOR_SNO.equals(txtRecentNo.getText().toString().trim().toUpperCase())) {
			Configuration.RECENT_STATOR_SNO = txtRecentNo.getText().trim().toUpperCase();
			Configuration.saveCommonConfigValues("RECENT_STATOR_SNO");
		}
		
		frmMain.enableDisableScanner();
		
		JOptionPane.showMessageDialog(this, "Changes are saved successfully!"); 
	}

	private void optSNoBothItemStateChanged() {
		optSNoAutoItemStateChanged();
	}

	private void chkPrependDtItemStateChanged() {
		txtRecentNoKeyReleased();
	}

	private void cmbLine4() {
		chkQRIncLine4.setEnabled(!cmbLine4.getSelectedItem().toString().equals("None"));
	}

	private void initComponents() {
		// JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
		tabbedPane1 = new JTabbedPane();
		pnlTest = new JPanel();
		pnlTests = new JPanel();
		chkRes = new JCheckBox();
		chkInsRes = new JCheckBox();
		chkHV = new JCheckBox();
		chkInsResAfterHV = new JCheckBox();
		chkSurge = new JCheckBox();
		chkDir = new JCheckBox();
		pnlTests2 = new JPanel();
		pnlRes = new JPanel();
		chkCommon = new JCheckBox();
		lblStCl = new JLabel();
		lblRunCl = new JLabel();
		lblComCoil = new JLabel();
		txtRelStCoil = new JTextField();
		txtRelRunCoil = new JTextField();
		txtRelCom = new JTextField();
		label82 = new JLabel();
		label83 = new JLabel();
		label84 = new JLabel();
		pnlInsRes = new JPanel();
		lblInsRes = new JLabel();
		txtRelIns = new JTextField();
		label86 = new JLabel();
		pnlHV = new JPanel();
		lblHV = new JLabel();
		txtRelHV = new JTextField();
		label85 = new JLabel();
		pnlSurge = new JPanel();
		lblHV2 = new JLabel();
		txtRelSurge = new JTextField();
		label88 = new JLabel();
		lblSurgeMethod = new JLabel();
		optCapWaveYes = new JRadioButton();
		optCapWaveNo = new JRadioButton();
		lblSurgeNo = new JLabel();
		lblSWInt = new JLabel();
		txtSWNo = new JTextField();
		txtSWInt = new JTextField();
		lblSWSec = new JLabel();
		pnlDir = new JPanel();
		lblInsRes2 = new JLabel();
		txtRelDir = new JTextField();
		label87 = new JLabel();
		cmdSave = new JButton();
		cmdExit = new JButton();
		pnlSNo = new JPanel();
		panel2 = new JPanel();
		panel1 = new JPanel();
		optSNoAuto = new JRadioButton();
		optSNoBarCode = new JRadioButton();
		optSNoBoth = new JRadioButton();
		pnlSNoAuto = new JPanel();
		lblRecentNo = new JLabel();
		txtRecentNo = new JTextField();
		lbllblNSno = new JLabel();
		lblNSno = new JLabel();
		chkPrependDt = new JCheckBox();
		chkFailSNo = new JCheckBox();
		chkSNo = new JCheckBox();
		pnlSNoBarcode = new JPanel();
		chkModel = new JCheckBox();
		optDelimiter = new JRadioButton();
		optPosition = new JRadioButton();
		lblDelimiter = new JLabel();
		txtDelimiter = new JTextField();
		lblLocation = new JLabel();
		txtLocation = new JTextField();
		lblModelPos = new JLabel();
		txtModelStartPos = new JTextField();
		lblModelPos2 = new JLabel();
		txtModelEndPos = new JTextField();
		pnlSignal = new JPanel();
		chkPrintQR = new JCheckBox();
		cmbQR = new JComboBox<>();
		panel3 = new JPanel();
		lblQRF4 = new JLabel();
		lblQRF5 = new JLabel();
		lblQR = new JLabel();
		lblQRF = new JLabel();
		lblQRF3 = new JLabel();
		lblQR2 = new JLabel();
		lblQRF2 = new JLabel();
		chkQRIncLine2 = new JCheckBox();
		lblQR3 = new JLabel();
		cmbLine3 = new JComboBox<>();
		chkQRIncLine3 = new JCheckBox();
		lblQR4 = new JLabel();
		cmbLine4 = new JComboBox<>();
		chkQRIncLine4 = new JCheckBox();
		label1 = new JLabel();
		cmdSaveSNo = new JButton();
		cmdExitSNo = new JButton();

		//======== this ========
		setTitle("Doer StatorView: Test Settings");
		setFont(new Font("Arial", Font.PLAIN, 12));
		setResizable(false);
		setModal(true);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		Container contentPane = getContentPane();
		contentPane.setLayout(new TableLayout(new double[][] {
			{5, 650, 5},
			{5, TableLayout.FILL, 5}}));
		((TableLayout)contentPane.getLayout()).setHGap(5);
		((TableLayout)contentPane.getLayout()).setVGap(1);

		//======== tabbedPane1 ========
		{
			tabbedPane1.setFont(new Font("Arial", Font.BOLD, 16));

			//======== pnlTest ========
			{
				pnlTest.setLayout(new TableLayout(new double[][] {
					{TableLayout.FILL, TableLayout.FILL},
					{TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.PREFERRED}}));
				((TableLayout)pnlTest.getLayout()).setHGap(5);
				((TableLayout)pnlTest.getLayout()).setVGap(5);

				//======== pnlTests ========
				{
					pnlTests.setBorder(new TitledBorder(null, "Available Tests", TitledBorder.LEADING, TitledBorder.DEFAULT_POSITION,
						new Font("Arial", Font.PLAIN, 16), new Color(0x006699)));
					pnlTests.setLayout(new TableLayout(new double[][] {
						{5, TableLayout.FILL, TableLayout.FILL, TableLayout.FILL},
						{TableLayout.PREFERRED, TableLayout.PREFERRED}}));
					((TableLayout)pnlTests.getLayout()).setHGap(5);
					((TableLayout)pnlTests.getLayout()).setVGap(2);

					//---- chkRes ----
					chkRes.setText("Resistance Test");
					chkRes.setFont(new Font("Arial", Font.PLAIN, 13));
					chkRes.addActionListener(e -> chkResActionPerformed());
					pnlTests.add(chkRes, new TableLayoutConstraints(1, 0, 1, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

					//---- chkInsRes ----
					chkInsRes.setText("IR Test");
					chkInsRes.setFont(new Font("Arial", Font.PLAIN, 13));
					chkInsRes.addActionListener(e -> chkInsResActionPerformed());
					pnlTests.add(chkInsRes, new TableLayoutConstraints(2, 0, 2, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

					//---- chkHV ----
					chkHV.setText("High Voltage Test");
					chkHV.setFont(new Font("Arial", Font.PLAIN, 13));
					chkHV.addActionListener(e -> chkHVActionPerformed());
					pnlTests.add(chkHV, new TableLayoutConstraints(3, 0, 3, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

					//---- chkInsResAfterHV ----
					chkInsResAfterHV.setText("IR Test After HV");
					chkInsResAfterHV.setFont(new Font("Arial", Font.PLAIN, 13));
					chkInsResAfterHV.addActionListener(e -> chkInsResAfterHVActionPerformed());
					pnlTests.add(chkInsResAfterHV, new TableLayoutConstraints(1, 1, 1, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

					//---- chkSurge ----
					chkSurge.setText("Surge Test");
					chkSurge.setFont(new Font("Arial", Font.PLAIN, 13));
					chkSurge.addActionListener(e -> chkDisSurgeActionPerformed());
					pnlTests.add(chkSurge, new TableLayoutConstraints(2, 1, 2, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

					//---- chkDir ----
					chkDir.setText("Direction Test");
					chkDir.setFont(new Font("Arial", Font.PLAIN, 13));
					chkDir.addActionListener(e -> chkDirActionPerformed());
					pnlTests.add(chkDir, new TableLayoutConstraints(3, 1, 3, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
				}
				pnlTest.add(pnlTests, new TableLayoutConstraints(0, 0, 1, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//======== pnlTests2 ========
				{
					pnlTests2.setBorder(new TitledBorder(null, "Capture Settings", TitledBorder.LEADING, TitledBorder.DEFAULT_POSITION,
						new Font("Arial", Font.PLAIN, 16), new Color(0x006699)));
					pnlTests2.setLayout(new TableLayout(new double[][] {
						{5, TableLayout.FILL, TableLayout.FILL, TableLayout.FILL},
						{TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.PREFERRED}}));
					((TableLayout)pnlTests2.getLayout()).setHGap(5);
					((TableLayout)pnlTests2.getLayout()).setVGap(2);

					//======== pnlRes ========
					{
						pnlRes.setBorder(new TitledBorder(null, "Resistance", TitledBorder.LEADING, TitledBorder.DEFAULT_POSITION,
							new Font("Arial", Font.PLAIN, 14), new Color(0x006699)));
						pnlRes.setLayout(new TableLayout(new double[][] {
							{5, 280, TableLayout.FILL, TableLayout.FILL},
							{TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.PREFERRED}}));
						((TableLayout)pnlRes.getLayout()).setHGap(5);
						((TableLayout)pnlRes.getLayout()).setVGap(2);

						//---- chkCommon ----
						chkCommon.setText("Exclude common resistance");
						chkCommon.setFont(new Font("Arial", Font.PLAIN, 13));
						chkCommon.addActionListener(e -> chkCommonActionPerformed());
						pnlRes.add(chkCommon, new TableLayoutConstraints(1, 0, 1, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

						//---- lblStCl ----
						lblStCl.setText("Reliable Time For STARTING COIL Reading");
						lblStCl.setFont(new Font("Arial", Font.PLAIN, 13));
						lblStCl.setIcon(null);
						pnlRes.add(lblStCl, new TableLayoutConstraints(1, 1, 1, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.CENTER));

						//---- lblRunCl ----
						lblRunCl.setText("Reliable Time For RUNNING COIL Reading");
						lblRunCl.setFont(new Font("Arial", Font.PLAIN, 14));
						lblRunCl.setIcon(null);
						pnlRes.add(lblRunCl, new TableLayoutConstraints(1, 2, 1, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.CENTER));

						//---- lblComCoil ----
						lblComCoil.setText("Reliable Time For COMMON COIL Reading");
						lblComCoil.setFont(new Font("Arial", Font.PLAIN, 13));
						lblComCoil.setIcon(null);
						pnlRes.add(lblComCoil, new TableLayoutConstraints(1, 3, 1, 3, TableLayoutConstraints.FULL, TableLayoutConstraints.CENTER));

						//---- txtRelStCoil ----
						txtRelStCoil.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
						txtRelStCoil.setToolTipText("Reliable Time For Resistance Reading");
						pnlRes.add(txtRelStCoil, new TableLayoutConstraints(2, 1, 2, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

						//---- txtRelRunCoil ----
						txtRelRunCoil.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
						txtRelRunCoil.setToolTipText("Reliable Time For Resistance Reading");
						pnlRes.add(txtRelRunCoil, new TableLayoutConstraints(2, 2, 2, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

						//---- txtRelCom ----
						txtRelCom.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
						txtRelCom.setToolTipText("Reliable Time For Resistance Reading");
						pnlRes.add(txtRelCom, new TableLayoutConstraints(2, 3, 2, 3, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

						//---- label82 ----
						label82.setText("Sec");
						label82.setFont(new Font("Arial", Font.PLAIN, 13));
						pnlRes.add(label82, new TableLayoutConstraints(3, 1, 3, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.CENTER));

						//---- label83 ----
						label83.setText("Sec");
						label83.setFont(new Font("Arial", Font.PLAIN, 13));
						pnlRes.add(label83, new TableLayoutConstraints(3, 2, 3, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.CENTER));

						//---- label84 ----
						label84.setText("Sec");
						label84.setFont(new Font("Arial", Font.PLAIN, 14));
						pnlRes.add(label84, new TableLayoutConstraints(3, 3, 3, 3, TableLayoutConstraints.FULL, TableLayoutConstraints.CENTER));
					}
					pnlTests2.add(pnlRes, new TableLayoutConstraints(1, 0, 3, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

					//======== pnlInsRes ========
					{
						pnlInsRes.setBorder(new TitledBorder(null, "Ins. Resistance", TitledBorder.LEADING, TitledBorder.DEFAULT_POSITION,
							new Font("Arial", Font.PLAIN, 14), new Color(0x006699)));
						pnlInsRes.setLayout(new TableLayout(new double[][] {
							{5, 280, TableLayout.FILL, TableLayout.FILL},
							{TableLayout.PREFERRED}}));
						((TableLayout)pnlInsRes.getLayout()).setHGap(5);
						((TableLayout)pnlInsRes.getLayout()).setVGap(2);

						//---- lblInsRes ----
						lblInsRes.setText("Reliable Time For INS. RESISTANCE Reading");
						lblInsRes.setFont(new Font("Arial", Font.PLAIN, 13));
						lblInsRes.setIcon(null);
						pnlInsRes.add(lblInsRes, new TableLayoutConstraints(1, 0, 1, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.CENTER));

						//---- txtRelIns ----
						txtRelIns.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
						txtRelIns.setToolTipText("Reliable Time For Ins Res Reading");
						pnlInsRes.add(txtRelIns, new TableLayoutConstraints(2, 0, 2, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

						//---- label86 ----
						label86.setText("Sec");
						label86.setFont(new Font("Arial", Font.PLAIN, 13));
						pnlInsRes.add(label86, new TableLayoutConstraints(3, 0, 3, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.CENTER));
					}
					pnlTests2.add(pnlInsRes, new TableLayoutConstraints(1, 1, 3, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

					//======== pnlHV ========
					{
						pnlHV.setBorder(new TitledBorder(null, "HV", TitledBorder.LEADING, TitledBorder.DEFAULT_POSITION,
							new Font("Arial", Font.PLAIN, 14), new Color(0x006699)));
						pnlHV.setLayout(new TableLayout(new double[][] {
							{5, 280, TableLayout.FILL, TableLayout.FILL},
							{TableLayout.PREFERRED}}));
						((TableLayout)pnlHV.getLayout()).setHGap(5);
						((TableLayout)pnlHV.getLayout()).setVGap(2);

						//---- lblHV ----
						lblHV.setText("Reliable Time For HIGH VOLTAGE Reading");
						lblHV.setFont(new Font("Arial", Font.PLAIN, 13));
						lblHV.setIcon(null);
						pnlHV.add(lblHV, new TableLayoutConstraints(1, 0, 1, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.CENTER));

						//---- txtRelHV ----
						txtRelHV.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
						txtRelHV.setToolTipText("Reliable Time For HV Reading");
						pnlHV.add(txtRelHV, new TableLayoutConstraints(2, 0, 2, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

						//---- label85 ----
						label85.setText("Sec");
						label85.setFont(new Font("Arial", Font.PLAIN, 13));
						pnlHV.add(label85, new TableLayoutConstraints(3, 0, 3, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.CENTER));
					}
					pnlTests2.add(pnlHV, new TableLayoutConstraints(1, 2, 3, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

					//======== pnlSurge ========
					{
						pnlSurge.setBorder(new TitledBorder(null, "Surge", TitledBorder.LEADING, TitledBorder.DEFAULT_POSITION,
							new Font("Arial", Font.PLAIN, 14), new Color(0x006699)));
						pnlSurge.setLayout(new TableLayout(new double[][] {
							{5, 280, TableLayout.FILL, TableLayout.FILL},
							{TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.PREFERRED}}));
						((TableLayout)pnlSurge.getLayout()).setHGap(5);
						((TableLayout)pnlSurge.getLayout()).setVGap(2);

						//---- lblHV2 ----
						lblHV2.setText("Reliable Time For SURGE Result");
						lblHV2.setFont(new Font("Arial", Font.PLAIN, 13));
						lblHV2.setIcon(null);
						pnlSurge.add(lblHV2, new TableLayoutConstraints(1, 0, 1, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.CENTER));

						//---- txtRelSurge ----
						txtRelSurge.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
						txtRelSurge.setToolTipText("Reliable Time For HV Reading");
						pnlSurge.add(txtRelSurge, new TableLayoutConstraints(2, 0, 2, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

						//---- label88 ----
						label88.setText("Sec");
						label88.setFont(new Font("Arial", Font.PLAIN, 13));
						pnlSurge.add(label88, new TableLayoutConstraints(3, 0, 3, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.CENTER));

						//---- lblSurgeMethod ----
						lblSurgeMethod.setText("Capture Surge Wave");
						lblSurgeMethod.setFont(new Font("Arial", Font.PLAIN, 13));
						lblSurgeMethod.setIcon(null);
						pnlSurge.add(lblSurgeMethod, new TableLayoutConstraints(1, 1, 1, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.CENTER));

						//---- optCapWaveYes ----
						optCapWaveYes.setText("Yes");
						optCapWaveYes.setFont(new Font("Arial", Font.PLAIN, 13));
						optCapWaveYes.addActionListener(e -> optCapWaveYesActionPerformed());
						pnlSurge.add(optCapWaveYes, new TableLayoutConstraints(2, 1, 2, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

						//---- optCapWaveNo ----
						optCapWaveNo.setText("No");
						optCapWaveNo.setFont(new Font("Arial", Font.PLAIN, 13));
						optCapWaveNo.addActionListener(e -> optCapWaveNoActionPerformed());
						pnlSurge.add(optCapWaveNo, new TableLayoutConstraints(3, 1, 3, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

						//---- lblSurgeNo ----
						lblSurgeNo.setText("Number of Surge Waves Per Test");
						lblSurgeNo.setFont(new Font("Arial", Font.PLAIN, 13));
						lblSurgeNo.setIcon(null);
						pnlSurge.add(lblSurgeNo, new TableLayoutConstraints(1, 2, 1, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.CENTER));

						//---- lblSWInt ----
						lblSWInt.setText("Interval Between Surge Waves");
						lblSWInt.setFont(new Font("Arial", Font.PLAIN, 13));
						lblSWInt.setIcon(null);
						pnlSurge.add(lblSWInt, new TableLayoutConstraints(1, 3, 1, 3, TableLayoutConstraints.FULL, TableLayoutConstraints.CENTER));

						//---- txtSWNo ----
						txtSWNo.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
						txtSWNo.setToolTipText("Number of Surge Waves");
						pnlSurge.add(txtSWNo, new TableLayoutConstraints(2, 2, 2, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

						//---- txtSWInt ----
						txtSWInt.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
						txtSWInt.setToolTipText("Interval Between Surge Waves");
						pnlSurge.add(txtSWInt, new TableLayoutConstraints(2, 3, 2, 3, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

						//---- lblSWSec ----
						lblSWSec.setText("Sec");
						lblSWSec.setFont(new Font("Arial", Font.PLAIN, 13));
						pnlSurge.add(lblSWSec, new TableLayoutConstraints(3, 3, 3, 3, TableLayoutConstraints.FULL, TableLayoutConstraints.CENTER));
					}
					pnlTests2.add(pnlSurge, new TableLayoutConstraints(1, 3, 3, 3, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

					//======== pnlDir ========
					{
						pnlDir.setBorder(new TitledBorder(null, "Direction", TitledBorder.LEADING, TitledBorder.DEFAULT_POSITION,
							new Font("Arial", Font.PLAIN, 14), new Color(0x006699)));
						pnlDir.setLayout(new TableLayout(new double[][] {
							{5, 280, TableLayout.FILL, TableLayout.FILL},
							{TableLayout.PREFERRED}}));
						((TableLayout)pnlDir.getLayout()).setHGap(5);
						((TableLayout)pnlDir.getLayout()).setVGap(2);

						//---- lblInsRes2 ----
						lblInsRes2.setText("Reliable Time For DIRECTION Reading");
						lblInsRes2.setFont(new Font("Arial", Font.PLAIN, 13));
						lblInsRes2.setIcon(null);
						pnlDir.add(lblInsRes2, new TableLayoutConstraints(1, 0, 1, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.CENTER));

						//---- txtRelDir ----
						txtRelDir.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
						txtRelDir.setToolTipText("Reliable Time For Direction Reading");
						pnlDir.add(txtRelDir, new TableLayoutConstraints(2, 0, 2, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

						//---- label87 ----
						label87.setText("Sec");
						label87.setFont(new Font("Arial", Font.PLAIN, 13));
						pnlDir.add(label87, new TableLayoutConstraints(3, 0, 3, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.CENTER));
					}
					pnlTests2.add(pnlDir, new TableLayoutConstraints(1, 4, 3, 4, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
				}
				pnlTest.add(pnlTests2, new TableLayoutConstraints(0, 1, 1, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- cmdSave ----
				cmdSave.setText("Save");
				cmdSave.setFont(new Font("Arial", Font.PLAIN, 16));
				cmdSave.setIcon(new ImageIcon(getClass().getResource("/img/save.PNG")));
				cmdSave.setToolTipText("Click on this to save the changes");
				cmdSave.setMnemonic('S');
				cmdSave.addActionListener(e -> cmdSaveActionPerformed());
				pnlTest.add(cmdSave, new TableLayoutConstraints(0, 2, 0, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- cmdExit ----
				cmdExit.setText("<html>Close&nbsp;&nbsp<font size=-2>[Esc]</html>");
				cmdExit.setFont(new Font("Arial", Font.PLAIN, 16));
				cmdExit.setIcon(new ImageIcon(getClass().getResource("/img/exit.PNG")));
				cmdExit.setToolTipText("Click on this to close this window");
				cmdExit.addActionListener(e -> cmdExitActionPerformed());
				pnlTest.add(cmdExit, new TableLayoutConstraints(1, 2, 1, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
			}
			tabbedPane1.addTab("Available Tests", pnlTest);

			//======== pnlSNo ========
			{
				pnlSNo.setLayout(new TableLayout(new double[][] {
					{TableLayout.FILL, TableLayout.FILL},
					{TableLayout.PREFERRED, TableLayout.FILL, TableLayout.PREFERRED}}));
				((TableLayout)pnlSNo.getLayout()).setHGap(5);
				((TableLayout)pnlSNo.getLayout()).setVGap(5);

				//======== panel2 ========
				{
					panel2.setBorder(new TitledBorder(null, "Serial Number", TitledBorder.LEADING, TitledBorder.DEFAULT_POSITION,
						new Font("Arial", Font.PLAIN, 16), new Color(0x006699)));
					panel2.setLayout(new TableLayout(new double[][] {
						{TableLayout.FILL, TableLayout.FILL},
						{TableLayout.PREFERRED, TableLayout.PREFERRED}}));
					((TableLayout)panel2.getLayout()).setHGap(5);
					((TableLayout)panel2.getLayout()).setVGap(5);

					//======== panel1 ========
					{
						panel1.setLayout(new TableLayout(new double[][] {
							{TableLayout.FILL, TableLayout.FILL, TableLayout.FILL},
							{TableLayout.PREFERRED}}));
						((TableLayout)panel1.getLayout()).setHGap(5);
						((TableLayout)panel1.getLayout()).setVGap(5);

						//---- optSNoAuto ----
						optSNoAuto.setText("Auto Generation");
						optSNoAuto.setFont(new Font("Arial", Font.BOLD, 12));
						optSNoAuto.setToolTipText("Enable this if stator SNo needs to be generated automatically by software");
						optSNoAuto.addItemListener(e -> optSNoAutoItemStateChanged());
						panel1.add(optSNoAuto, new TableLayoutConstraints(0, 0, 0, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

						//---- optSNoBarCode ----
						optSNoBarCode.setText("Barcode Scanner");
						optSNoBarCode.setFont(new Font("Arial", Font.BOLD, 12));
						optSNoBarCode.setToolTipText("Enable this if stator SNo is scanned from bar or QR code");
						panel1.add(optSNoBarCode, new TableLayoutConstraints(1, 0, 1, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

						//---- optSNoBoth ----
						optSNoBoth.setText("Combination Of Both");
						optSNoBoth.setFont(new Font("Arial", Font.BOLD, 12));
						optSNoBoth.setToolTipText("Enable this if model is scanned from bar or QR code, but the SNo is auto generated");
						optSNoBoth.addItemListener(e -> optSNoBothItemStateChanged());
						panel1.add(optSNoBoth, new TableLayoutConstraints(2, 0, 2, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
					}
					panel2.add(panel1, new TableLayoutConstraints(0, 0, 1, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

					//======== pnlSNoAuto ========
					{
						pnlSNoAuto.setBorder(new EtchedBorder());
						pnlSNoAuto.setLayout(new TableLayout(new double[][] {
							{1, TableLayout.PREFERRED, TableLayout.FILL, 1},
							{1, TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.PREFERRED}}));
						((TableLayout)pnlSNoAuto.getLayout()).setHGap(5);
						((TableLayout)pnlSNoAuto.getLayout()).setVGap(5);

						//---- lblRecentNo ----
						lblRecentNo.setText("Recent Stator SNo.");
						lblRecentNo.setFont(new Font("Arial", Font.PLAIN, 12));
						pnlSNoAuto.add(lblRecentNo, new TableLayoutConstraints(1, 1, 1, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

						//---- txtRecentNo ----
						txtRecentNo.setFont(new Font("Microsoft Sans Serif", Font.PLAIN, 16));
						txtRecentNo.setToolTipText("Last tested motor serial number");
						txtRecentNo.addKeyListener(new KeyAdapter() {
							@Override
							public void keyReleased(KeyEvent e) {
								txtRecentNoKeyReleased();
							}
						});
						pnlSNoAuto.add(txtRecentNo, new TableLayoutConstraints(2, 1, 2, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

						//---- lbllblNSno ----
						lbllblNSno.setText("Next SNo. will be");
						lbllblNSno.setFont(new Font("Arial", Font.PLAIN, 12));
						pnlSNoAuto.add(lbllblNSno, new TableLayoutConstraints(1, 2, 1, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

						//---- lblNSno ----
						lblNSno.setText("Next No.");
						lblNSno.setFont(new Font("Microsoft Sans Serif", Font.BOLD | Font.ITALIC, 16));
						lblNSno.setForeground(new Color(0x006699));
						pnlSNoAuto.add(lblNSno, new TableLayoutConstraints(2, 2, 2, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.CENTER));

						//---- chkPrependDt ----
						chkPrependDt.setText("Prepend Date with SNo.");
						chkPrependDt.setFont(new Font("Arial", Font.PLAIN, 12));
						chkPrependDt.setMnemonic('P');
						chkPrependDt.setOpaque(false);
						chkPrependDt.setToolTipText("Enable this if serial number needs to be generated for failed test as well");
						chkPrependDt.addItemListener(e -> chkPrependDtItemStateChanged());
						pnlSNoAuto.add(chkPrependDt, new TableLayoutConstraints(1, 3, 2, 3, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

						//---- chkFailSNo ----
						chkFailSNo.setText("Generate Stator SNo. For Failed Test As Well");
						chkFailSNo.setFont(new Font("Arial", Font.PLAIN, 12));
						chkFailSNo.setMnemonic('F');
						chkFailSNo.setOpaque(false);
						chkFailSNo.setToolTipText("Enable this if serial number needs to be generated for failed test as well");
						pnlSNoAuto.add(chkFailSNo, new TableLayoutConstraints(1, 4, 2, 4, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

						//---- chkSNo ----
						chkSNo.setText("<html>Confirm Stator SNo. Before Assigning  To A <br/>Concluded Test</html>");
						chkSNo.setFont(new Font("Arial", Font.PLAIN, 12));
						chkSNo.setMnemonic('C');
						chkSNo.setOpaque(false);
						chkSNo.setToolTipText("Enable this in case you want to review the motor serial number before assigning to a concluded test");
						pnlSNoAuto.add(chkSNo, new TableLayoutConstraints(1, 5, 2, 5, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
					}
					panel2.add(pnlSNoAuto, new TableLayoutConstraints(0, 1, 0, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

					//======== pnlSNoBarcode ========
					{
						pnlSNoBarcode.setBorder(new EtchedBorder());
						pnlSNoBarcode.setLayout(new TableLayout(new double[][] {
							{1, TableLayout.PREFERRED, TableLayout.FILL, 1},
							{1, TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.PREFERRED, 1}}));
						((TableLayout)pnlSNoBarcode.getLayout()).setHGap(5);
						((TableLayout)pnlSNoBarcode.getLayout()).setVGap(5);

						//---- chkModel ----
						chkModel.setText("Detect Stator Model From Barcode");
						chkModel.setFont(new Font("Arial", Font.PLAIN, 12));
						chkModel.setOpaque(false);
						chkModel.setToolTipText("Enable this if motor model needs to be chosen automatically from bar or QR code");
						chkModel.addItemListener(e -> chkModelItemStateChanged());
						pnlSNoBarcode.add(chkModel, new TableLayoutConstraints(1, 1, 2, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

						//---- optDelimiter ----
						optDelimiter.setText("Delimiter Based");
						optDelimiter.setFont(new Font("Arial", Font.PLAIN, 12));
						optDelimiter.setOpaque(false);
						optDelimiter.setToolTipText("Enable this if model is identified using a delimiter in scanned barcode like comma, hyphen etc.");
						optDelimiter.addItemListener(e -> optDelimiterItemStateChanged());
						pnlSNoBarcode.add(optDelimiter, new TableLayoutConstraints(1, 2, 1, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

						//---- optPosition ----
						optPosition.setText("Position Based");
						optPosition.setFont(new Font("Arial", Font.PLAIN, 12));
						optPosition.setOpaque(false);
						optPosition.setToolTipText("Enable this if model is identified at certain position in scanned barcode");
						optPosition.addItemListener(e -> optPositionItemStateChanged());
						pnlSNoBarcode.add(optPosition, new TableLayoutConstraints(2, 2, 2, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

						//---- lblDelimiter ----
						lblDelimiter.setText("Delimiter");
						lblDelimiter.setFont(new Font("Arial", Font.PLAIN, 12));
						pnlSNoBarcode.add(lblDelimiter, new TableLayoutConstraints(1, 3, 1, 3, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

						//---- txtDelimiter ----
						txtDelimiter.setFont(new Font("Microsoft Sans Serif", Font.PLAIN, 16));
						txtDelimiter.setToolTipText("Delimiter like comma, hyphen, underscore (, - _) etc");
						pnlSNoBarcode.add(txtDelimiter, new TableLayoutConstraints(2, 3, 2, 3, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

						//---- lblLocation ----
						lblLocation.setText("Location of Model");
						lblLocation.setFont(new Font("Arial", Font.PLAIN, 12));
						pnlSNoBarcode.add(lblLocation, new TableLayoutConstraints(1, 4, 1, 4, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

						//---- txtLocation ----
						txtLocation.setFont(new Font("Microsoft Sans Serif", Font.PLAIN, 16));
						txtLocation.setToolTipText("Location of model after splitting the barcode based on given delimiter");
						pnlSNoBarcode.add(txtLocation, new TableLayoutConstraints(2, 4, 2, 4, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

						//---- lblModelPos ----
						lblModelPos.setText("Starting Charecter Position");
						lblModelPos.setFont(new Font("Arial", Font.PLAIN, 12));
						pnlSNoBarcode.add(lblModelPos, new TableLayoutConstraints(1, 5, 1, 5, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

						//---- txtModelStartPos ----
						txtModelStartPos.setFont(new Font("Microsoft Sans Serif", Font.PLAIN, 16));
						txtModelStartPos.setToolTipText("Starting position of model in scanned barcode");
						pnlSNoBarcode.add(txtModelStartPos, new TableLayoutConstraints(2, 5, 2, 5, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

						//---- lblModelPos2 ----
						lblModelPos2.setText("Ending Charecter Position");
						lblModelPos2.setFont(new Font("Arial", Font.PLAIN, 12));
						pnlSNoBarcode.add(lblModelPos2, new TableLayoutConstraints(1, 6, 1, 6, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

						//---- txtModelEndPos ----
						txtModelEndPos.setFont(new Font("Microsoft Sans Serif", Font.PLAIN, 16));
						txtModelEndPos.setToolTipText("Ending position of model in scanned barcode");
						pnlSNoBarcode.add(txtModelEndPos, new TableLayoutConstraints(2, 6, 2, 6, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
					}
					panel2.add(pnlSNoBarcode, new TableLayoutConstraints(1, 1, 1, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
				}
				pnlSNo.add(panel2, new TableLayoutConstraints(0, 0, 1, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//======== pnlSignal ========
				{
					pnlSignal.setBorder(new TitledBorder(null, "QR Code Label", TitledBorder.LEADING, TitledBorder.DEFAULT_POSITION,
						new Font("Arial", Font.PLAIN, 16), new Color(0x006699)));
					pnlSignal.setLayout(new TableLayout(new double[][] {
						{TableLayout.FILL, TableLayout.FILL},
						{TableLayout.PREFERRED, TableLayout.FILL}}));
					((TableLayout)pnlSignal.getLayout()).setHGap(5);
					((TableLayout)pnlSignal.getLayout()).setVGap(5);

					//---- chkPrintQR ----
					chkPrintQR.setText("Print Label Upon Test Completion");
					chkPrintQR.setFont(new Font("Arial", Font.PLAIN, 13));
					chkPrintQR.setHorizontalTextPosition(SwingConstants.RIGHT);
					pnlSignal.add(chkPrintQR, new TableLayoutConstraints(0, 0, 0, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

					//---- cmbQR ----
					cmbQR.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
					cmbQR.setModel(new DefaultComboBoxModel<>(new String[] {
						"For Passed Tests Only",
						"For Both Passed & Failed Tests"
					}));
					pnlSignal.add(cmbQR, new TableLayoutConstraints(1, 0, 1, 0, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

					//======== panel3 ========
					{
						panel3.setBorder(new EtchedBorder());
						panel3.setLayout(new TableLayout(new double[][] {
							{1, TableLayout.FILL, TableLayout.FILL, TableLayout.FILL, TableLayout.FILL, 1},
							{5, 22, 22, 22, TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.PREFERRED}}));
						((TableLayout)panel3.getLayout()).setHGap(5);
						((TableLayout)panel3.getLayout()).setVGap(5);

						//---- lblQRF4 ----
						lblQRF4.setText("Visible Text In QR Code Label");
						lblQRF4.setFont(new Font("Arial", Font.PLAIN, 12));
						lblQRF4.setOpaque(true);
						lblQRF4.setBackground(Color.gray);
						lblQRF4.setForeground(Color.white);
						lblQRF4.setHorizontalAlignment(SwingConstants.CENTER);
						panel3.add(lblQRF4, new TableLayoutConstraints(1, 1, 2, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

						//---- lblQRF5 ----
						lblQRF5.setText("Data Inside The QR Code");
						lblQRF5.setFont(new Font("Arial", Font.PLAIN, 12));
						lblQRF5.setOpaque(true);
						lblQRF5.setBackground(Color.gray);
						lblQRF5.setForeground(Color.white);
						lblQRF5.setHorizontalAlignment(SwingConstants.CENTER);
						panel3.add(lblQRF5, new TableLayoutConstraints(3, 1, 4, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

						//---- lblQR ----
						lblQR.setText("Line 1");
						lblQR.setFont(new Font("Arial", Font.PLAIN, 12));
						panel3.add(lblQR, new TableLayoutConstraints(1, 2, 1, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

						//---- lblQRF ----
						lblQRF.setText("Stator Serial Number");
						lblQRF.setFont(new Font("Arial", Font.ITALIC, 12));
						lblQRF.setForeground(Color.darkGray);
						panel3.add(lblQRF, new TableLayoutConstraints(2, 2, 2, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

						//---- lblQRF3 ----
						lblQRF3.setText("QR Code made up of only SNo by default");
						lblQRF3.setFont(new Font("Arial", Font.PLAIN, 12));
						panel3.add(lblQRF3, new TableLayoutConstraints(3, 2, 4, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

						//---- lblQR2 ----
						lblQR2.setText("Line 2");
						lblQR2.setFont(new Font("Arial", Font.PLAIN, 12));
						panel3.add(lblQR2, new TableLayoutConstraints(1, 3, 1, 3, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

						//---- lblQRF2 ----
						lblQRF2.setText("Test Date");
						lblQRF2.setFont(new Font("Arial", Font.ITALIC, 12));
						lblQRF2.setForeground(Color.darkGray);
						panel3.add(lblQRF2, new TableLayoutConstraints(2, 3, 2, 3, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

						//---- chkQRIncLine2 ----
						chkQRIncLine2.setText("Also Include As Part Of QR Code");
						chkQRIncLine2.setFont(new Font("Arial", Font.PLAIN, 12));
						chkQRIncLine2.setOpaque(false);
						chkQRIncLine2.setToolTipText("QR code contains only SNo. by default, enable this to include this field as well");
						chkQRIncLine2.addItemListener(e -> chkModelItemStateChanged());
						panel3.add(chkQRIncLine2, new TableLayoutConstraints(3, 3, 4, 3, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

						//---- lblQR3 ----
						lblQR3.setText("Line 3");
						lblQR3.setFont(new Font("Arial", Font.PLAIN, 12));
						panel3.add(lblQR3, new TableLayoutConstraints(1, 4, 1, 4, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

						//---- cmbLine3 ----
						cmbLine3.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
						cmbLine3.setModel(new DefaultComboBoxModel<>(new String[] {
							"Stator Model",
							"Vendor Reference",
							"Tester Name"
						}));
						panel3.add(cmbLine3, new TableLayoutConstraints(2, 4, 2, 4, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

						//---- chkQRIncLine3 ----
						chkQRIncLine3.setText("Also Include As Part Of QR Code");
						chkQRIncLine3.setFont(new Font("Arial", Font.PLAIN, 12));
						chkQRIncLine3.setOpaque(false);
						chkQRIncLine3.setToolTipText("QR code contains only SNo. by default, enable this to include this field as well");
						chkQRIncLine3.addItemListener(e -> chkModelItemStateChanged());
						panel3.add(chkQRIncLine3, new TableLayoutConstraints(3, 4, 4, 4, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

						//---- lblQR4 ----
						lblQR4.setText("Line 4");
						lblQR4.setFont(new Font("Arial", Font.PLAIN, 12));
						panel3.add(lblQR4, new TableLayoutConstraints(1, 5, 1, 5, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

						//---- cmbLine4 ----
						cmbLine4.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
						cmbLine4.setModel(new DefaultComboBoxModel<>(new String[] {
							"None",
							"Stator Model",
							"Vendor Reference",
							"Tester Name"
						}));
						cmbLine4.addActionListener(e -> cmbLine4());
						panel3.add(cmbLine4, new TableLayoutConstraints(2, 5, 2, 5, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

						//---- chkQRIncLine4 ----
						chkQRIncLine4.setText("Also Include As Part Of QR Code");
						chkQRIncLine4.setFont(new Font("Arial", Font.PLAIN, 12));
						chkQRIncLine4.setOpaque(false);
						chkQRIncLine4.setToolTipText("QR code contains only SNo. by default, enable this to include this field as well");
						chkQRIncLine4.addItemListener(e -> chkModelItemStateChanged());
						panel3.add(chkQRIncLine4, new TableLayoutConstraints(3, 5, 4, 5, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

						//---- label1 ----
						label1.setText("<html><b>Note:</b> It is not advised to include all the fields in QR code as sticker size may not accomodate them all. Hence, choose max 2 or 3 fields.</html>");
						label1.setFont(new Font("Arial", Font.PLAIN, 12));
						panel3.add(label1, new TableLayoutConstraints(3, 6, 4, 6, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
					}
					pnlSignal.add(panel3, new TableLayoutConstraints(0, 1, 1, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
				}
				pnlSNo.add(pnlSignal, new TableLayoutConstraints(0, 1, 1, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- cmdSaveSNo ----
				cmdSaveSNo.setText("Save");
				cmdSaveSNo.setFont(new Font("Arial", Font.PLAIN, 14));
				cmdSaveSNo.setIcon(new ImageIcon(getClass().getResource("/img/save.PNG")));
				cmdSaveSNo.setToolTipText("Click on this to save the changes");
				cmdSaveSNo.setMnemonic('S');
				cmdSaveSNo.addActionListener(e -> cmdSaveSNoActionPerformed());
				pnlSNo.add(cmdSaveSNo, new TableLayoutConstraints(0, 2, 0, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

				//---- cmdExitSNo ----
				cmdExitSNo.setText("<html>Close&nbsp;&nbsp<font size=-2>[Esc]</html>");
				cmdExitSNo.setFont(new Font("Arial", Font.PLAIN, 14));
				cmdExitSNo.setIcon(new ImageIcon(getClass().getResource("/img/exit.PNG")));
				cmdExitSNo.setToolTipText("Click on this to close this window");
				cmdExitSNo.addActionListener(e -> cmdExitActionPerformed());
				pnlSNo.add(cmdExitSNo, new TableLayoutConstraints(1, 2, 1, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
			}
			tabbedPane1.addTab("Serial No. & Barcode", pnlSNo);
		}
		contentPane.add(tabbedPane1, new TableLayoutConstraints(1, 1, 1, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
		pack();
		setLocationRelativeTo(getOwner());

		//---- buttonGroup2 ----
		ButtonGroup buttonGroup2 = new ButtonGroup();
		buttonGroup2.add(optCapWaveYes);
		buttonGroup2.add(optCapWaveNo);

		//---- buttonGroup1 ----
		ButtonGroup buttonGroup1 = new ButtonGroup();
		buttonGroup1.add(optSNoAuto);
		buttonGroup1.add(optSNoBarCode);
		buttonGroup1.add(optSNoBoth);

		//---- buttonGroup4 ----
		ButtonGroup buttonGroup4 = new ButtonGroup();
		buttonGroup4.add(optDelimiter);
		buttonGroup4.add(optPosition);
		// JFormDesigner - End of component initialization  //GEN-END:initComponents
		
		// CUSTOM CODE - BEGIN
		associateFunctionKeys();
		// CUSTOM CODE - END
	}

	// JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
	private JTabbedPane tabbedPane1;
	private JPanel pnlTest;
	private JPanel pnlTests;
	private JCheckBox chkRes;
	private JCheckBox chkInsRes;
	private JCheckBox chkHV;
	private JCheckBox chkInsResAfterHV;
	private JCheckBox chkSurge;
	private JCheckBox chkDir;
	private JPanel pnlTests2;
	private JPanel pnlRes;
	private JCheckBox chkCommon;
	private JLabel lblStCl;
	private JLabel lblRunCl;
	private JLabel lblComCoil;
	private JTextField txtRelStCoil;
	private JTextField txtRelRunCoil;
	private JTextField txtRelCom;
	private JLabel label82;
	private JLabel label83;
	private JLabel label84;
	private JPanel pnlInsRes;
	private JLabel lblInsRes;
	private JTextField txtRelIns;
	private JLabel label86;
	private JPanel pnlHV;
	private JLabel lblHV;
	private JTextField txtRelHV;
	private JLabel label85;
	private JPanel pnlSurge;
	private JLabel lblHV2;
	private JTextField txtRelSurge;
	private JLabel label88;
	private JLabel lblSurgeMethod;
	private JRadioButton optCapWaveYes;
	private JRadioButton optCapWaveNo;
	private JLabel lblSurgeNo;
	private JLabel lblSWInt;
	private JTextField txtSWNo;
	private JTextField txtSWInt;
	private JLabel lblSWSec;
	private JPanel pnlDir;
	private JLabel lblInsRes2;
	private JTextField txtRelDir;
	private JLabel label87;
	private JButton cmdSave;
	private JButton cmdExit;
	private JPanel pnlSNo;
	private JPanel panel2;
	private JPanel panel1;
	private JRadioButton optSNoAuto;
	private JRadioButton optSNoBarCode;
	private JRadioButton optSNoBoth;
	private JPanel pnlSNoAuto;
	private JLabel lblRecentNo;
	private JTextField txtRecentNo;
	private JLabel lbllblNSno;
	private JLabel lblNSno;
	private JCheckBox chkPrependDt;
	private JCheckBox chkFailSNo;
	private JCheckBox chkSNo;
	private JPanel pnlSNoBarcode;
	private JCheckBox chkModel;
	private JRadioButton optDelimiter;
	private JRadioButton optPosition;
	private JLabel lblDelimiter;
	private JTextField txtDelimiter;
	private JLabel lblLocation;
	private JTextField txtLocation;
	private JLabel lblModelPos;
	private JTextField txtModelStartPos;
	private JLabel lblModelPos2;
	private JTextField txtModelEndPos;
	private JPanel pnlSignal;
	private JCheckBox chkPrintQR;
	private JComboBox<String> cmbQR;
	private JPanel panel3;
	private JLabel lblQRF4;
	private JLabel lblQRF5;
	private JLabel lblQR;
	private JLabel lblQRF;
	private JLabel lblQRF3;
	private JLabel lblQR2;
	private JLabel lblQRF2;
	private JCheckBox chkQRIncLine2;
	private JLabel lblQR3;
	private JComboBox<String> cmbLine3;
	private JCheckBox chkQRIncLine3;
	private JLabel lblQR4;
	private JComboBox<String> cmbLine4;
	private JCheckBox chkQRIncLine4;
	private JLabel label1;
	private JButton cmdSaveSNo;
	private JButton cmdExitSNo;
	// JFormDesigner - End of variables declaration  //GEN-END:variables
}
