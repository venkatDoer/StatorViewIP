/*
 * Created by JFormDesigner on Fri Nov 30 11:29:38 IST 2012
 */

package doer.sv;

import java.awt.*;
import java.awt.event.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.swing.*;

//import com.sun.org.apache.xerces.internal.impl.dtd.models.CMBinOp;

import doer.io.Encrypt;

import info.clearthought.layout.*;



/**
 * @author VENKATESAN SELVARAJ
 */
public class Login extends JDialog {
	public Login(Frame owner) {
		super(owner);
		initComponents();
	}

	public Login(Dialog owner) {
		super(owner);
		initComponents();
	}

	// CUSTOM FUNCTIONS - BEGIN
	
	private void associateFunctionKeys() {
		// associate enter for choose
		String CHOOSE_ACTION_KEY = "loginAction";
		Action loginAction = new AbstractAction() {
		      public void actionPerformed(ActionEvent actionEvent) {
		        cmdLoginActionPerformed();
		      }
		    };
		KeyStroke entr = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);
		InputMap loginInputMap = cmdLogin.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
		loginInputMap.put(entr, CHOOSE_ACTION_KEY);
		ActionMap loginActionMap = cmdLogin.getActionMap();
		loginActionMap.put(CHOOSE_ACTION_KEY, loginAction);
		cmdLogin.setActionMap(loginActionMap);
		
		// associate F10 for exit
		String CANCEL_ACTION_KEY = "cancelAction";
		Action cancelAction = new AbstractAction() {
		      public void actionPerformed(ActionEvent actionEvent) {
		        cmdCancelActionPerformed();
		      }
		    };
		KeyStroke esc = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
		InputMap cancelInputMap = cmdCancel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
		cancelInputMap.put(esc, CANCEL_ACTION_KEY);
		ActionMap cancelActionMap = cmdCancel.getActionMap();
		cancelActionMap.put(CANCEL_ACTION_KEY, cancelAction);
		cmdCancel.setActionMap(cancelActionMap);
	}
	
	// CUSTOM FUNCTIONS - END

	private void cmdCancelActionPerformed() {
		System.exit(0);
	}

	private void cmdLoginActionPerformed() {
		// validate entry
		if (txtName.getText().trim().isEmpty()) {
			JOptionPane.showMessageDialog(this, "Please enter an user name");
			txtName.requestFocus();
			return;
		}
		if (txtPass.getText().trim().isEmpty()) {
			JOptionPane.showMessageDialog(this, "Please enter password");
			txtPass.requestFocus();
			return;
		}
		
		// validate user
		
		String curName = "";
		String curPass = "";
		String curAdmin = "";
		String curStatorAccess = "";
		String curModifyAccess = "";
		
		try {
			Connection conn = DriverManager.getConnection(Configuration.DB_URL);
			Statement stmt = conn.createStatement();
			stmt.setQueryTimeout(30);
			
			// fetch and load the result
			ResultSet res = null;
			try {
				res = stmt.executeQuery("select * from USER where name='" + txtName.getText().trim() + "'");
			} catch (SQLException se) {
				if (se.getMessage().contains("no such table")) {
					// seems the software is run first time
					// create the config table and insert config params
					curName = "admin";
					curPass = Encrypt.encrypt("doer");
					curAdmin = Encrypt.encrypt("1");
					curStatorAccess = Encrypt.encrypt("1");
					curModifyAccess = Encrypt.encrypt("1");
					
					stmt.executeUpdate("create table USER (name text primary key, password text, admin text, stator_access text, modify_access text)");
					stmt.executeUpdate("insert into USER values ('" + curName + "','" + curPass + "','" + curAdmin + "','" + curStatorAccess + "','" + curModifyAccess + "')");
					res = stmt.executeQuery("select * from USER where name='" + txtName.getText().trim() + "'");
				}
			}
			
			if (res.next()) {
				curName = res.getString("name");
				curPass = Encrypt.decrypt(res.getString("password"));
				curAdmin = Encrypt.decrypt(res.getString("admin"));
				curStatorAccess = Encrypt.decrypt(res.getString("stator_access"));
				curModifyAccess = Encrypt.decrypt(res.getString("modify_access"));
				res.close();
			}
			stmt.close();
			conn.close();
		} catch (Exception sqle) {
			JOptionPane.showMessageDialog(new JDialog(), "DB Error:" + sqle.getMessage());
			return;
		}
		
		if (curName.equals(txtName.getText().trim()) && curPass.equals((txtPass.getText().trim()))) {
			// user found
			// open main screen if valid
			Configuration.USER = curName;
			Configuration.USER_IS_ADMIN = curAdmin;
			Configuration.USER_HAS_STATOR_ACCESS = curStatorAccess;
			Configuration.USER_HAS_MODIFY_ACCESS = curModifyAccess;
			
			this.setCursor(new Cursor(Cursor.WAIT_CURSOR));
			this.setVisible(false);
			StatorView frmMain = new StatorView();
			this.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
			frmMain.setVisible(true);
		} else {
			JOptionPane.showMessageDialog(this, "Invalid user name or password\nNote: Both user name & password are case sensitive");
		}
	}

	private void initComponents() {
		// custom code begin
		// set windows theme
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			System.err.println("Unable to set theme: SystemLookAndFeel");
		}
		// custom code end
		// JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
		label42 = new JLabel();
		label1 = new JLabel();
		txtName = new JTextField();
		label2 = new JLabel();
		txtPass = new JPasswordField();
		separator1 = new JSeparator();
		lblVer = new JLabel();
		cmdCancel = new JButton();
		cmdLogin = new JButton();

		//======== this ========
		setTitle("Doer StatorView: Login");
		setFont(new Font("Arial", Font.PLAIN, 12));
		setResizable(false);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		Container contentPane = getContentPane();
		contentPane.setLayout(new TableLayout(new double[][] {
			{5, 150, 10, TableLayout.PREFERRED, 160, 5},
			{5, TableLayout.PREFERRED, TableLayout.PREFERRED, 5, TableLayout.PREFERRED, 5}}));
		((TableLayout)contentPane.getLayout()).setHGap(5);
		((TableLayout)contentPane.getLayout()).setVGap(5);

		//---- label42 ----
		label42.setIcon(new ImageIcon(getClass().getResource("/img/doer_logo.png")));
		label42.setFocusable(false);
		label42.setHorizontalAlignment(SwingConstants.CENTER);
		label42.setOpaque(true);
		label42.setBackground(new Color(0, 51, 153));
		contentPane.add(label42, new TableLayoutConstraints(1, 1, 1, 3, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

		//---- label1 ----
		label1.setText("User Name");
		label1.setFont(new Font("Arial", Font.PLAIN, 14));
		contentPane.add(label1, new TableLayoutConstraints(3, 1, 3, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

		//---- txtName ----
		txtName.setFont(new Font("Microsoft Sans Serif", Font.PLAIN, 16));
		contentPane.add(txtName, new TableLayoutConstraints(4, 1, 4, 1, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

		//---- label2 ----
		label2.setText("Password");
		label2.setFont(new Font("Arial", Font.PLAIN, 14));
		contentPane.add(label2, new TableLayoutConstraints(3, 2, 3, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

		//---- txtPass ----
		txtPass.setFont(new Font("Microsoft Sans Serif", Font.PLAIN, 16));
		contentPane.add(txtPass, new TableLayoutConstraints(4, 2, 4, 2, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
		contentPane.add(separator1, new TableLayoutConstraints(3, 3, 4, 3, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

		//---- lblVer ----
		lblVer.setText("Version");
		lblVer.setFont(new Font("Arial", Font.BOLD | Font.ITALIC, 14));
		lblVer.setForeground(Color.orange);
		lblVer.setIcon(null);
		lblVer.setHorizontalAlignment(SwingConstants.CENTER);
		lblVer.setBackground(new Color(0, 51, 153));
		lblVer.setOpaque(true);
		contentPane.add(lblVer, new TableLayoutConstraints(1, 3, 1, 4, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

		//---- cmdCancel ----
		cmdCancel.setText("<html>Cancel&nbsp;&nbsp;<font size=-2>[Esc]</font></html>");
		cmdCancel.setFont(new Font("Arial", Font.PLAIN, 14));
		cmdCancel.setIcon(new ImageIcon(getClass().getResource("/img/exit.PNG")));
		cmdCancel.setToolTipText("Click on this to cancel user login and close this window");
		cmdCancel.setNextFocusableComponent(txtName);
		cmdCancel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				cmdCancelActionPerformed();
			}
		});
		contentPane.add(cmdCancel, new TableLayoutConstraints(3, 4, 3, 4, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));

		//---- cmdLogin ----
		cmdLogin.setText("<html>Login&nbsp;&nbsp;<font size=-2>[Enter]</font></html>");
		cmdLogin.setFont(new Font("Arial", Font.PLAIN, 14));
		cmdLogin.setIcon(new ImageIcon(getClass().getResource("/img/login.PNG")));
		cmdLogin.setToolTipText("Click on this to login after entering the user name and password");
		cmdLogin.setNextFocusableComponent(cmdCancel);
		cmdLogin.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				cmdLoginActionPerformed();
			}
		});
		contentPane.add(cmdLogin, new TableLayoutConstraints(4, 4, 4, 4, TableLayoutConstraints.FULL, TableLayoutConstraints.FULL));
		pack();
		setLocationRelativeTo(getOwner());
		// JFormDesigner - End of component initialization  //GEN-END:initComponents
	
		// custom code begin
		lblVer.setText(Configuration.APP_VERSION);
		associateFunctionKeys();
		
		// load config and set defaults
		Configuration.loadConfigValues();
		
		// custom code end
	}

	// JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
	private JLabel label42;
	private JLabel label1;
	private JTextField txtName;
	private JLabel label2;
	private JPasswordField txtPass;
	private JSeparator separator1;
	private JLabel lblVer;
	private JButton cmdCancel;
	private JButton cmdLogin;
	// JFormDesigner - End of variables declaration  //GEN-END:variables
	
}
