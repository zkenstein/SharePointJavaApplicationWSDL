package sharepointapp;

import java.awt.Color;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;

import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.SpringLayout;

/**
 * This class is responsible for showing the toolbar buttons, messages, and url
 * input.
 * 
 * @author allarj3
 *
 */
public class SPToolbarView extends SPBaseView {
	private static final int BUTTON_SIZE = 110;
	private static final long serialVersionUID = -7024445311746875546L;
	private static final String DEFAULT_DOWNLOAD_LOCATION = ".";
	private static SPToolbarView instance = new SPToolbarView();
	private static final String URL_LIST_KEY = "Sharepoint URL History";
	private static final String DOWNLOAD_LOCATION_KEY = "Last Download Location";
	private JButton download;
	private JButton upload;
	private JLabel message;
	private JButton refreshButton;
	private JButton preferencesButton;
	private JButton searchButton;
	private JTextField search;
	private JComboBox<String> urlSelection;

	/**
	 * Returns the static instance of this class.
	 * 
	 * @return
	 */
	public static SPToolbarView getInstance() {
		return instance;
	}
	

	/**
	 * Creates the toolbar view that can be added to the main frame.
	 */
	private SPToolbarView() {

		prefs.registerNoteForKey(DOWNLOAD_LOCATION_KEY, "This is automatically changed when using the Download Popout.");
		prefs.registerNoteForKey(URL_LIST_KEY, "These are what populate your URL drop down. They are automatically added. Delete them here to 'forget' them.");
		prefs.registerSingleValueOnlyKey(DOWNLOAD_LOCATION_KEY);
		SpringLayout layout = new SpringLayout();
		setLayout(layout);

		final JFileChooser fileUploadChooser = new JFileChooser();
		fileUploadChooser.setDialogTitle("Upload Files");
		fileUploadChooser.setMultiSelectionEnabled(true);

		final JFileChooser downloadFolderChooser = new JFileChooser();
		downloadFolderChooser.setMultiSelectionEnabled(true);
		downloadFolderChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		downloadFolderChooser.setCurrentDirectory(
				new File(prefs.getFirstOrDefault(DOWNLOAD_LOCATION_KEY, DEFAULT_DOWNLOAD_LOCATION)));
		downloadFolderChooser.setDialogTitle("Choose Destination Folder");

		setBackground(SPUtilities.getDarkThemeColor());
		prefs.add(URL_LIST_KEY, "New URL");
		List<String> urls = prefs.getAll(URL_LIST_KEY);
		String[] urlArray = urls.toArray(new String[urls.size()]);
		// urlSelection = new JComboBox<String>(urlArray);
		urlSelection = new JComboBox<String>(new DefaultComboBoxModel<String>(urlArray));
		urlSelection.setEnabled(true);
		urlSelection.setEditable(true);
		this.add(urlSelection);

		download = new JButton("Download");
		download.setEnabled(false);
		upload = new JButton("Upload");
		upload.setEnabled(false);

		searchButton = new JButton("Search Site");
		searchButton.setEnabled(false);
		search = new JTextField("");
		
		
		
		message = new JLabel("");
		
		this.add(download);
		this.add(upload);
		this.add(message);
		this.add(searchButton);
		this.add(search);
		message.setHorizontalAlignment(JLabel.CENTER);

		URL url = getClass().getResource("/refresh.png");
		URL url2 = getClass().getResource("/refreshHover.png");
		if (url != null && url2 != null) {
			ImageIcon imageIcon = new ImageIcon(url);
			refreshButton = new JButton(imageIcon);
			refreshButton.setBorderPainted(false);
			refreshButton.setBorder(null);
			// button.setFocusable(false);
			refreshButton.setMargin(new Insets(0, 0, 0, 0));
			refreshButton.setContentAreaFilled(false);
			refreshButton.setRolloverIcon(new ImageIcon(url2));

		} else {
			refreshButton = new JButton("Refresh");
		}
		this.add(refreshButton);
		refreshButton.setEnabled(false);

		preferencesButton = new JButton("Preferences");
		this.add(preferencesButton);

		layout.putConstraint(SpringLayout.WEST, urlSelection, 5, SpringLayout.EAST, refreshButton);
		layout.putConstraint(SpringLayout.NORTH, urlSelection, 5, SpringLayout.NORTH, this);
		layout.putConstraint(SpringLayout.EAST, urlSelection, -5, SpringLayout.EAST, this);
		layout.putConstraint(SpringLayout.SOUTH, urlSelection, 37, SpringLayout.NORTH, this);

		layout.putConstraint(SpringLayout.WEST, refreshButton, 5, SpringLayout.WEST, this);
		layout.putConstraint(SpringLayout.NORTH, refreshButton, 5, SpringLayout.NORTH, this);
		layout.putConstraint(SpringLayout.EAST, refreshButton, 37, SpringLayout.WEST, this);
		layout.putConstraint(SpringLayout.SOUTH, refreshButton, 37, SpringLayout.NORTH, this);

		layout.putConstraint(SpringLayout.WEST, preferencesButton, -BUTTON_SIZE - 5, SpringLayout.EAST, this);
		layout.putConstraint(SpringLayout.NORTH, preferencesButton, 5, SpringLayout.SOUTH, urlSelection);
		layout.putConstraint(SpringLayout.EAST, preferencesButton, 0, SpringLayout.EAST, urlSelection);
		layout.putConstraint(SpringLayout.SOUTH, preferencesButton, -5, SpringLayout.SOUTH, this);

		layout.putConstraint(SpringLayout.WEST, download, -BUTTON_SIZE - 5, SpringLayout.WEST, preferencesButton);
		layout.putConstraint(SpringLayout.NORTH, download, 5, SpringLayout.SOUTH, urlSelection);
		layout.putConstraint(SpringLayout.EAST, download, -5, SpringLayout.WEST, preferencesButton);
		layout.putConstraint(SpringLayout.SOUTH, download, -5, SpringLayout.SOUTH, this);

		layout.putConstraint(SpringLayout.WEST, upload, -BUTTON_SIZE - 5, SpringLayout.WEST, download);
		layout.putConstraint(SpringLayout.NORTH, upload, 5, SpringLayout.SOUTH, urlSelection);
		layout.putConstraint(SpringLayout.EAST, upload, -5, SpringLayout.WEST, download);
		layout.putConstraint(SpringLayout.SOUTH, upload, -5, SpringLayout.SOUTH, this);
		
		layout.putConstraint(SpringLayout.WEST, searchButton, 5, SpringLayout.EAST, search);
		layout.putConstraint(SpringLayout.NORTH, searchButton, 5, SpringLayout.SOUTH, urlSelection);
		layout.putConstraint(SpringLayout.EAST, searchButton, BUTTON_SIZE + 5, SpringLayout.EAST, search);
		layout.putConstraint(SpringLayout.SOUTH, searchButton, -5, SpringLayout.SOUTH, this);

		layout.putConstraint(SpringLayout.WEST, message, 5, SpringLayout.EAST, searchButton);
		layout.putConstraint(SpringLayout.NORTH, message, 5, SpringLayout.SOUTH, urlSelection);
		layout.putConstraint(SpringLayout.EAST, message, -5, SpringLayout.WEST, upload);
		layout.putConstraint(SpringLayout.SOUTH, message, -5, SpringLayout.SOUTH, this);

		layout.putConstraint(SpringLayout.WEST, search, 5, SpringLayout.WEST, this);
		layout.putConstraint(SpringLayout.NORTH, search, 5, SpringLayout.SOUTH, urlSelection);
		layout.putConstraint(SpringLayout.EAST, search, 195, SpringLayout.WEST, this);
		layout.putConstraint(SpringLayout.SOUTH, search, -5, SpringLayout.SOUTH, this);

		urlSelection.addActionListener(new URLSelectionActionListener());

		download.addActionListener(new DownloadButtonActionListener(downloadFolderChooser));

		upload.addActionListener(new UploadButtonActionListener(fileUploadChooser));

		refreshButton.addActionListener(new RefreshButtonActionListener());

		preferencesButton.addActionListener(new SPPreferencesWindow.SPPreferencesActionListener(new Runnable() {

			@Override
			public void run() {
				List<String> newUrls = prefs.getAll(URL_LIST_KEY);
				String[] newUrlArray = newUrls.toArray(new String[newUrls.size()]);
				urlSelection.setModel(new DefaultComboBoxModel<String>(newUrlArray));
			}
		}));

		searchButton.addActionListener(new SPSearchActionListener());
		search.addKeyListener(new KeyAdapter() {
			public void keyReleased(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					searchButton.doClick();
				}
			}
		});
	}

	/**
	 * This class is used to dictate what happens when the search button is
	 * pressed.
	 * 
	 * @author Joshua
	 *
	 */
	private class SPSearchActionListener implements ActionListener {

		
		@Override
		public void actionPerformed(ActionEvent e) {
			try {
				mainController.displayLoading(true);
				(new Thread() {
					public void run() {
						mainController.getSearch(search.getText());
					};
				}).start();
				
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}

	/**
	 * This class is used to dictate what happens when the UrlSelection drop
	 * down list is modified.
	 * 
	 * @author Joshua
	 *
	 */
	private class URLSelectionActionListener implements ActionListener {
		private String last = "";

		@Override
		public void actionPerformed(ActionEvent e) {

			if (last.compareTo((String) urlSelection.getSelectedItem()) != 0) {
				mainController.connectToSite((String) urlSelection.getSelectedItem());
			}
			last = (String) urlSelection.getSelectedItem();
		}
	}

	/**
	 * This class is used to dictate what happens when the Download button is
	 * activated.
	 * 
	 * @author Joshua
	 *
	 */
	private class DownloadButtonActionListener implements ActionListener {

		JFileChooser downloadFolderChooser;

		/**
		 * This class is used to dictate what happens when the Download button
		 * is activated.
		 * 
		 * @param downloadFolderChooser
		 *            - the selector used for picking the download directory.
		 */
		public DownloadButtonActionListener(JFileChooser downloadFolderChooser) {
			this.downloadFolderChooser = downloadFolderChooser;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			clearMessageText();

			int returnVal = downloadFolderChooser.showOpenDialog(MainView.frame);

			if (returnVal == JFileChooser.APPROVE_OPTION) {
				File downloadFolder = downloadFolderChooser.getSelectedFile();

				if (!downloadFolder.exists() || !downloadFolder.isDirectory()) {
					downloadFolder = new File(DEFAULT_DOWNLOAD_LOCATION);
				}

				try {
					prefs.put(DOWNLOAD_LOCATION_KEY, downloadFolder.getCanonicalPath());
					prefs.flush();
				} catch (IOException e2) {
					e2.printStackTrace();
				}
				List<String> paths = mainController.getCurrentItemPaths();
				//message.setText("Downloading " + paths.size() + " " + getFileString(paths.size()) + "...");
				mainController.displayMessages("Downloading " + paths.size() + " " + getFileString(paths.size()) + "...", null, true);
				mainController.messageLock = true;

				final File downloadFolderFinal = downloadFolder;

				(new Thread() {
					public void run() {
						int count = 0;
						for (String path : paths) {
							String[] pathParts = path.split("/");
							try {
								String downloadFilePath = downloadFolderFinal.getCanonicalPath() + "\\"
										+ pathParts[pathParts.length - 1];
								if (webController.downloadFile(downloadFilePath, path)) {
									count++;
								}
							} catch (Exception e1) {
								e1.printStackTrace();
							}
						}
						mainController.messageLock = false;
						//setMessageText(getFileString(paths.size()) + " Downloaded: " + count + " / " + paths.size(),
						//		count == paths.size());
						mainController.displayMessages(getFileString(paths.size()) + " Downloaded: " + count + " / " + paths.size(), null, count == paths.size());
					};

				}).start();
			}
		}
	}

	/**
	 * This class is used to dictate what happens when the upload button is
	 * pressed.
	 * 
	 * @author Joshua
	 *
	 */
	private class UploadButtonActionListener implements ActionListener {

		JFileChooser fileUploadChooser;

		/**
		 * This class is used to dictate what happens when the upload button is
		 * pressed.
		 * 
		 * @param fileUploadChooser
		 *            - the tool for selecting the files to upload.
		 */
		public UploadButtonActionListener(JFileChooser fileUploadChooser) {
			this.fileUploadChooser = fileUploadChooser;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			clearMessageText();
			int returnVal = fileUploadChooser.showOpenDialog(MainView.frame);

			if (returnVal == JFileChooser.APPROVE_OPTION) {
				final String listName = mainController.getCurrentList();
				final String folderPath = mainController.getCurrentFolderPath();
				final File[] files = fileUploadChooser.getSelectedFiles();
				//message.setText("Uploading " + files.length + " " + getFileString(files.length) + "...");
				mainController.displayMessages("Uploading " + files.length + " " + getFileString(files.length) + "...", null, true);
				mainController.messageLock = true;

				(new Thread() {
					public void run() {
						int count = 0;
						for (File file : files) {
							try {
								String listPath = webController.getDefaultUrlForList(listName) + folderPath;
								System.out.println(listPath);
								if (listPath != null && webController.uploadFile(file.getAbsolutePath(),
										listPath + "/" + file.getName())) {
									count++;
								}

							} catch (Exception e1) {
								e1.printStackTrace();
							}
						}
						mainController.setCurrentList(listName);
						mainController.updateItems(true);
						mainController.messageLock = false;
						//setMessageText(getFileString(files.length) + " Uploaded: " + count + " / " + files.length,
						//		count == files.length);
						mainController.displayMessages(getFileString(files.length) + " Uploaded: " + count + " / " + files.length, null, count == files.length);
					};
				}).start();
			}
		}
	}

	/**
	 * This class is used to dictate what happens when the refresh button is
	 * pressed.
	 * 
	 * @author Joshua
	 *
	 */
	private class RefreshButtonActionListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			search.setText("");
			String currentList = mainController.getCurrentList();
			webController.connectToUrl((String) urlSelection.getSelectedItem(), currentList);
		}
	}

	/**
	 * Gets Files/File based upon the count
	 * 
	 * @param size
	 *            - how many files there are
	 * @return "File" if size is 1, "Files" otherwise.
	 */
	protected String getFileString(int size) {
		if (size != 1) {
			return "Files";
		} else {
			return "File";
		}
	}

	/**
	 * Clears the messages associated with the view.
	 */
	public void clearMessageText() {
		if (!mainController.messageLock) {
			setMessageText("", true);
		}
	}

	/**
	 * Sets the message associated with this view.
	 * 
	 * @param messageText
	 *            - the message to show.
	 * @param isSuccessful
	 *            - used to determine if the message was for a success or
	 *            failure.
	 */
	public void setMessageText(String messageText, boolean isSuccessful) {
		if (!mainController.messageLock) {
			message.setText(messageText);
			if (isSuccessful) {
				message.setForeground(SPUtilities.getDarkThemeFontColor());
			} else {
				message.setForeground(Color.red);
			}
		}
	}

	/**
	 * Enables/Disables the upload button
	 * 
	 * @param enable
	 *            - whether to enable or disable the button
	 * @param listName
	 *            - the list name, used to determine if the upload button can be
	 *            enabled
	 */
	public void enableUploadButton(boolean enable, String listName) {
		if (webController.getDefaultUrlForList(listName) != null) {
			upload.setEnabled(enable);
		} else {
			upload.setEnabled(false);
		}
	}

	/**
	 * Enables/Disables the refresh button
	 * 
	 * @param willEnable
	 *            - true to enable the button, false to disable
	 */
	public void enableRefreshButton(boolean willEnable) {
		refreshButton.setEnabled(willEnable);
	}

	/**
	 * Enables/Disables the download button, based on if there are downloadable
	 * items.
	 * 
	 * @param areDownloadble
	 */
	public void enableDownloadButton(boolean areDownloadble) {
		download.setEnabled(areDownloadble);
	}

	/**
	 * Configures the view during initial site connection.
	 * 
	 * @param url
	 *            - the url that is being connected
	 */
	public void configureConnectingViewStart(String url) {
		enableRefreshButton(false);
		upload.setEnabled(false);
		searchButton.setEnabled(false);
		mainController.clearMessages();
		if (prefs.add(URL_LIST_KEY, url)) {
			urlSelection.addItem(url);
			// urlSelection.removeAllItems();
			// urlSelection.revalidate();
			prefs.flush();
		}
	}

	/**
	 * The action when the connection is completed
	 * 
	 * @param listName
	 *            - the list that was selected to be displayed
	 */
	public void finishedConnection(String listName) {
		enableUploadButton(true, listName);
		enableRefreshButton(true);
		searchButton.setEnabled(true);
	}

}
