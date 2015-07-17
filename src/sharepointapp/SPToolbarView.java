package sharepointapp;

import java.awt.Color;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.SpringLayout;

/**
 * @author allarj3
 *
 */
public class SPToolbarView extends SPBaseView {
	private static final long serialVersionUID = -7024445311746875546L;
	private static final String DEFAULT_DOWNLOAD_LOCATION = ".";
	private static SPToolbarView instance = new SPToolbarView();
	private static final String URL_LIST_KEY = "URLS";
	private static final String DOWNLOAD_LOCATION_KEY = "DOWNLOAD_LOCATION";
	private SPPreferences prefs = SPPreferences.GetPreferences();
	private JButton download;
	private JButton upload;
	private JLabel message;
	private JButton loadingImage;
	private JComboBox<String> urlSelection;
	protected boolean messageLock;

	/**
	 * @return
	 */
	public static SPToolbarView getInstance() {
		return instance;
	}

	/**
	 * 
	 */
	private SPToolbarView() {
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

		setBackground(new Color(132, 178, 139));
		prefs.add(URL_LIST_KEY, "New URL");
		List<String> urls = prefs.getAll(URL_LIST_KEY);
		String[] urlArray = urls.toArray(new String[urls.size()]);
		urlSelection = new JComboBox<String>(urlArray);
		urlSelection.setEnabled(true);
		urlSelection.setEditable(true);
		this.add(urlSelection);

		download = new JButton("Download File(s)");
		download.setEnabled(false);
		upload = new JButton("Upload File(s)");
		upload.setEnabled(false);
		message = new JLabel("");
		this.add(download);
		this.add(upload);
		this.add(message);
		message.setHorizontalAlignment(JLabel.CENTER);

		URL url = getClass().getResource("/refresh.png");
		URL url2 = getClass().getResource("/refreshHover.png");
		if (url != null && url2 != null) {
			ImageIcon imageIcon = new ImageIcon(url);
			loadingImage = new JButton(imageIcon);
			loadingImage.setBorderPainted(false);
			loadingImage.setBorder(null);
			// button.setFocusable(false);
			loadingImage.setMargin(new Insets(0, 0, 0, 0));
			loadingImage.setContentAreaFilled(false);
			loadingImage.setRolloverIcon(new ImageIcon(url2));

		} else {
			loadingImage = new JButton("Refresh");
		}
		this.add(loadingImage);
		loadingImage.setEnabled(false);

		layout.putConstraint(SpringLayout.WEST, urlSelection, 5, SpringLayout.EAST, loadingImage);
		layout.putConstraint(SpringLayout.NORTH, urlSelection, 5, SpringLayout.NORTH, this);
		layout.putConstraint(SpringLayout.EAST, urlSelection, -5, SpringLayout.EAST, this);
		layout.putConstraint(SpringLayout.SOUTH, urlSelection, 37, SpringLayout.NORTH, this);

		layout.putConstraint(SpringLayout.WEST, loadingImage, 5, SpringLayout.WEST, this);
		layout.putConstraint(SpringLayout.NORTH, loadingImage, 5, SpringLayout.NORTH, this);
		layout.putConstraint(SpringLayout.EAST, loadingImage, 37, SpringLayout.WEST, this);
		layout.putConstraint(SpringLayout.SOUTH, loadingImage, 37, SpringLayout.NORTH, this);

		layout.putConstraint(SpringLayout.WEST, download, -155, SpringLayout.EAST, this);
		layout.putConstraint(SpringLayout.NORTH, download, 5, SpringLayout.SOUTH, urlSelection);
		layout.putConstraint(SpringLayout.EAST, download, 0, SpringLayout.EAST, urlSelection);
		layout.putConstraint(SpringLayout.SOUTH, download, -5, SpringLayout.SOUTH, this);

		layout.putConstraint(SpringLayout.WEST, upload, -155, SpringLayout.WEST, download);
		layout.putConstraint(SpringLayout.NORTH, upload, 5, SpringLayout.SOUTH, urlSelection);
		layout.putConstraint(SpringLayout.EAST, upload, -5, SpringLayout.WEST, download);
		layout.putConstraint(SpringLayout.SOUTH, upload, -5, SpringLayout.SOUTH, this);

		layout.putConstraint(SpringLayout.WEST, message, 5, SpringLayout.WEST, this);
		layout.putConstraint(SpringLayout.NORTH, message, 5, SpringLayout.SOUTH, urlSelection);
		layout.putConstraint(SpringLayout.EAST, message, -5, SpringLayout.WEST, upload);
		layout.putConstraint(SpringLayout.SOUTH, message, -5, SpringLayout.SOUTH, this);

		urlSelection.addActionListener(new ActionListener() {
			private String last = "";

			@Override
			public void actionPerformed(ActionEvent e) {
				if (last.compareTo((String) urlSelection.getSelectedItem()) != 0) {
					mainController.connectToSite((String) urlSelection.getSelectedItem());
				}
				last = (String) urlSelection.getSelectedItem();
			}
		});

		download.addActionListener(new ActionListener() {

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
					message.setText("Downloading " + paths.size() + " " + getFileString(paths.size()) + "...");
					messageLock = true;

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
							messageLock = false;
							setMessageText(getFileString(paths.size()) + " Downloaded: " + count + " / " + paths.size(),
									count == paths.size());
						};

					}).start();
				}
			}
		});

		upload.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				clearMessageText();
				int returnVal = fileUploadChooser.showOpenDialog(MainView.frame);

				if (returnVal == JFileChooser.APPROVE_OPTION) {
					final String listName = mainController.getCurrentList();
					final File[] files = fileUploadChooser.getSelectedFiles();
					message.setText("Uploading " + files.length + " " + getFileString(files.length) + "...");
					messageLock = true;

					(new Thread() {
						public void run() {
							int count = 0;
							for (File file : files) {
								try {
									String listPath = webController.getDefaultUrlForList(listName);
									if (listPath != null && webController.uploadFile(file.getAbsolutePath(),
											listPath + "/" + file.getName())) {
										count++;
									}

								} catch (Exception e1) {
									e1.printStackTrace();
								}
							}
							mainController.setCurrentList(listName);
							mainController.updateItems();
							messageLock = false;
							setMessageText(getFileString(files.length) + " Uploaded: " + count + " / " + files.length,
									count == files.length);
						};
					}).start();
				}
			}
		});

		loadingImage.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				String currentList = mainController.getCurrentList();
				webController.connectToUrl((String) urlSelection.getSelectedItem(), currentList);
			}
		});
	}

	/**
	 * @param size
	 * @return
	 */
	protected String getFileString(int size) {
		if (size != 1) {
			return "Files";
		} else {
			return "File";
		}
	}

	/**
	 * 
	 */
	public void clearMessageText() {
		if (!messageLock) {
			setMessageText("", true);
		}
	}

	/**
	 * @param messageText
	 * @param isSuccessful
	 */
	public void setMessageText(String messageText, boolean isSuccessful) {
		if (!messageLock) {
			message.setText(messageText);
			if (isSuccessful) {
				message.setForeground(Color.black);
			} else {
				message.setForeground(Color.red);
			}
		}
	}

	/**
	 * @param enable
	 * @param listName
	 */
	public void enableUploadButton(boolean enable, String listName) {
		if (webController.getDefaultUrlForList(listName) != null) {
			upload.setEnabled(enable);
		} else {
			upload.setEnabled(false);
		}
	}

	/**
	 * @param willEnable
	 */
	public void enableRefreshButton(boolean willEnable) {
		loadingImage.setEnabled(willEnable);
	}

	/**
	 * @param url
	 */
	public void configureConnectingViewStart(String url) {
		enableRefreshButton(false);
		upload.setEnabled(false);
		clearMessageText();
		if (prefs.add(URL_LIST_KEY, url)) {
			urlSelection.addItem(url);
			prefs.flush();
		}
	}

	/**
	 * @param listName
	 */
	public void finishedConnection(String listName) {
		enableUploadButton(true, listName);
		enableRefreshButton(true);
	}

	/**
	 * @param areDownloadble
	 */
	public void enableDownloadButton(boolean areDownloadble) {
		download.setEnabled(areDownloadble);
	}
}
