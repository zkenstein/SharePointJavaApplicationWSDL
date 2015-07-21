package sharepointapp;

import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.WindowConstants;

/**
 * This class is used for authenticating users for a SharePoint server/site.
 * @author allarj3
 *
 */
public class SharePointAuthenticator extends Authenticator {

	private static final String DOMAIN_KEY = "DOMAIN";

	private static final String USERNAME_KEY = "USERNAME";

	private boolean exited;

	// This will accept all SSL Certificates without using a trust store.
	// Beware. This should be fixed later.
	// Borrowed from:
	// https://kkarthikeyanblog.wordpress.com/2013/01/03/java-httphttps-client-example-ignore-ssl/
	static {
		try {
			TrustManager[] trustAllCerts = { new X509TrustManager() {
				public X509Certificate[] getAcceptedIssuers() {
					return null;
				}

				public void checkClientTrusted(X509Certificate[] certs, String authType) {
				}

				public void checkServerTrusted(X509Certificate[] certs, String authType) {
				}
			} };
			SSLContext sc = SSLContext.getInstance("SSL");

			HostnameVerifier hv = new HostnameVerifier() {
				public boolean verify(String arg0, SSLSession arg1) {
					return true;
				}
			};
			sc.init(null, trustAllCerts, new SecureRandom());

			HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
			HttpsURLConnection.setDefaultHostnameVerifier(hv);
		} catch (Exception localException) {
			localException.printStackTrace();
		}
	}

	private SPPreferences prefs = SPPreferences.GetPreferences();

	private String domain = prefs.getFirstOrDefault(DOMAIN_KEY, "");
	private String username = prefs.getFirstOrDefault(USERNAME_KEY, "");
	private String password = "";
	private final Object waiter = new Object();
	private JFrame frame;

	/**
	 * This class is used to display the authentication screen with the fields for inputing arguments.
	 * @author allarj3
	 *
	 */
	private class AuthenticationDisplay extends Thread {

		@Override
		public void run() {
			frame = new JFrame("SharePoint in Java - Authenticate");
			Container pane = frame.getContentPane();

			SpringLayout paneLayout = new SpringLayout();
			pane.setLayout(paneLayout);
			JPanel formPanel = new JPanel();
			MainView.frame.setEnabled(false);
			frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
			frame.setSize(400, 200);
			domain = prefs.getFirstOrDefault(DOMAIN_KEY, "");
			username = prefs.getFirstOrDefault(USERNAME_KEY, "");

			final JTextField domainBox = new JTextField(domain);
			final JLabel domainLabel = new JLabel("Domain: ");
			final JTextField usernameBox = new JTextField(username);
			final JLabel usernameLabel = new JLabel("Username: ");
			final JTextField passwordBox = new JPasswordField("");
			final JLabel passwordLabel = new JLabel("Password: ");
			SpringLayout formPanelLayout = new SpringLayout();
			formPanel.setLayout(formPanelLayout);

			formPanel.add(domainLabel);
			formPanel.add(domainBox);
			formPanel.add(usernameLabel);
			formPanel.add(usernameBox);
			formPanel.add(passwordLabel);
			formPanel.add(passwordBox);

			SpringUtilities.makeCompactGrid(formPanel, 3, 2, // rows, cols
					6, 6, // initX, initY
					6, 6); // xPad, yPad

			pane.add(formPanel);
			final JButton submitBtn = new JButton("Log in");
			submitBtn.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					domain = domainBox.getText();
					prefs.put(DOMAIN_KEY, domain);
					username = usernameBox.getText();
					prefs.put(USERNAME_KEY, username);
					prefs.flush();
					password = passwordBox.getText();
					frame.dispose();
					synchronized (waiter) {
						waiter.notify();
					}
				}
			});
			pane.add(submitBtn);

			paneLayout.putConstraint(SpringLayout.WEST, formPanel, 0, SpringLayout.WEST, pane);
			paneLayout.putConstraint(SpringLayout.NORTH, formPanel, 0, SpringLayout.NORTH, pane);
			paneLayout.putConstraint(SpringLayout.EAST, formPanel, 0, SpringLayout.EAST, pane);
			paneLayout.putConstraint(SpringLayout.SOUTH, formPanel, -50, SpringLayout.SOUTH, pane);

			paneLayout.putConstraint(SpringLayout.WEST, submitBtn, 5, SpringLayout.WEST, pane);
			paneLayout.putConstraint(SpringLayout.NORTH, submitBtn, 5, SpringLayout.SOUTH, formPanel);
			paneLayout.putConstraint(SpringLayout.EAST, submitBtn, -5, SpringLayout.EAST, pane);
			paneLayout.putConstraint(SpringLayout.SOUTH, submitBtn, -5, SpringLayout.SOUTH, pane);

			frame.setVisible(true);
			frame.addWindowListener(new WindowListener() {

				@Override
				public void windowOpened(WindowEvent e) {

				}

				@Override
				public void windowIconified(WindowEvent e) {

				}

				@Override
				public void windowDeiconified(WindowEvent e) {

				}

				@Override
				public void windowDeactivated(WindowEvent e) {

				}

				@Override
				public void windowClosing(WindowEvent e) {

				}

				@Override
				public void windowClosed(WindowEvent e) {

					synchronized (waiter) {
						waiter.notify();
						exited = true;
					}
				}

				@Override
				public void windowActivated(WindowEvent e) {

				}
			});
		}

	}

	/**
	 * This class is used for authentication the user for SharePoint websites.
	 */
	public SharePointAuthenticator() {
		super();
		prefs.registerSingleValueOnlyKey(DOMAIN_KEY);
		prefs.registerSingleValueOnlyKey(USERNAME_KEY);
	}

	/* (non-Javadoc)
	 * @see java.net.Authenticator#getPasswordAuthentication()
	 */
	@Override
	public PasswordAuthentication getPasswordAuthentication() {

		try {
			exited = false;
			(new AuthenticationDisplay()).start();

			synchronized (waiter) {
				waiter.wait();
			}

			MainView.frame.setEnabled(true);
			MainView.frame.requestFocus();
			MainView.frame.revalidate();
			MainView.frame.repaint();
			if (!exited) {
				return (new PasswordAuthentication(getDomainAndUsername(), password.toCharArray()));
			} else {
				return null;
			}
		} catch (Exception e) {
			System.out.println("Error: " + e.getMessage());
			e.printStackTrace();
			MainView.frame.setEnabled(true);
			return (new PasswordAuthentication("", new char[] {}));
		}
	}

	/**
	 * Returns the user's password.
	 * @return
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * Returns the user's domain and username
	 * @return returns a string in the following format: DOMAIN\\username
	 */
	public String getDomainAndUsername() {
		String domainBars = "";
		if (!domain.isEmpty()) {
			domainBars = "\\";
		}
		return domain + domainBars + username;
	}
}
