package sharepointapp;

import java.awt.Color;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SpringLayout;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.w3c.dom.NodeList;

/**
 * This class is used to show all of the items associated with a particular
 * list/library.
 * 
 * @author allarj3
 *
 */
public class SPItemView extends SPBaseView {

	private static final long serialVersionUID = 9001415591452156169L;

	private static SPItemView instance = new SPItemView();

	private JScrollPane scroll = new JScrollPane();
	private JLabel loadingImage;
	private JLabel errorLabel = new JLabel("Connect to a site using the address bar above.");

	private JTable table;

	/**
	 * This returns the itemView's static instance
	 * 
	 * @return
	 */
	public static SPItemView getInstance() {
		return instance;
	}

	/**
	 * Creates a new item view using a SpringLayout
	 */
	private SPItemView() {

		this.add(scroll);
		scroll.setBackground(SPUtilities.getLightThemeColor());
		scroll.getViewport().setBackground(SPUtilities.getLightThemeColor());
		scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		setBackground(SPUtilities.getLightThemeColor());
		SpringLayout layout = new SpringLayout();
		setLayout(layout);

		URL url = getClass().getResource("/715.GIF");
		if (url != null) {
			ImageIcon imageIcon = new ImageIcon(url);
			loadingImage = new JLabel(imageIcon);
		} else {
			loadingImage = new JLabel();
		}
		this.add(loadingImage);
		this.add(errorLabel);
		loadingImage.setVisible(false);
		errorLabel.setVisible(false);

		layout.putConstraint(SpringLayout.WEST, scroll, 0, SpringLayout.WEST, this);
		layout.putConstraint(SpringLayout.NORTH, scroll, 0, SpringLayout.NORTH, this);
		layout.putConstraint(SpringLayout.EAST, scroll, 0, SpringLayout.EAST, this);
		layout.putConstraint(SpringLayout.SOUTH, scroll, 0, SpringLayout.SOUTH, this);

		layout.putConstraint(SpringLayout.WEST, loadingImage, 0, SpringLayout.WEST, this);
		layout.putConstraint(SpringLayout.NORTH, loadingImage, 0, SpringLayout.NORTH, this);
		layout.putConstraint(SpringLayout.EAST, loadingImage, 0, SpringLayout.EAST, this);
		layout.putConstraint(SpringLayout.SOUTH, loadingImage, 0, SpringLayout.SOUTH, this);

		layout.putConstraint(SpringLayout.WEST, errorLabel, 0, SpringLayout.WEST, this);
		layout.putConstraint(SpringLayout.NORTH, errorLabel, 0, SpringLayout.NORTH, this);
		layout.putConstraint(SpringLayout.EAST, errorLabel, 0, SpringLayout.EAST, this);
		layout.putConstraint(SpringLayout.SOUTH, errorLabel, 0, SpringLayout.SOUTH, this);

	}

	/**
	 * Gets the list of item paths currently selected
	 * 
	 * @return the list of string paths
	 */
	public List<String> getCurrentItemPaths() {
		List<String> paths = new ArrayList<String>();
		for (int i : table.getSelectedRows()) {
			String path = getPathFromRow(i);
			if (path.length() != 0 && !path.endsWith(".000")) {
				paths.add(path);
			}
		}
		return paths;
	}

	/**
	 * Gets the path string from a certain row
	 * 
	 * @param rowIndex
	 *            - the row to ascertain the path from
	 * @return the path as a string
	 */
	private String getPathFromRow(int rowIndex) {
		int columns = table.getColumnCount();
		String s = "";

		String[] siteUrlParts = webController.getBasesharepointUrl().split("/");
		String currentSiteName = siteUrlParts[siteUrlParts.length - 1];
		for (int col = 0; col < columns; col++) {
			Object o = table.getValueAt(rowIndex, col);
			if (table.getColumnName(col).equalsIgnoreCase("FileRef") || table.getColumnName(col).equalsIgnoreCase("WebRelativeUrl")) {
				String cellValue = o.toString();
				cellValue = cellValue.replaceAll(".*;#", "");
				cellValue = cellValue.replaceAll(".*" + currentSiteName + "/", "");
				s = "/" + cellValue;
			}
		}
		return s;
	}

	/**
	 * This used to update the item display with new items.
	 * 
	 * @param items
	 *            - the new items to display.
	 * @param listName
	 *            - the list of the current items.
	 * @return true if the operation was successful.
	 */
	public boolean UpdateTable(List<Object> items, String listName) {
		mainController.setFilesDownloadable(false);
		boolean returnResult = true;
		String[] columnNames = null;
		if (items != null) {
			columnNames = webController.getAttributeNames(items);
		}
		Object[][] data = null;
		if (columnNames != null) {
			data = webController.getData(items, columnNames);
		}

		if (data != null && columnNames != null) {
			table = new JTable(data, columnNames) {

				private static final long serialVersionUID = -4122451180044627689L;

				@Override
				public boolean isCellEditable(int row, int column) {
					return false;
				}
			};
			List<String> columns = Arrays.asList(columnNames);
			int indexOfName = columns.indexOf("FileName (FileLeafRef)");
			if (indexOfName > -1) {
				table.moveColumn(columns.indexOf("FileName (FileLeafRef)"), 0);
				table.getColumnModel().getColumn(0).setMinWidth(150);
			}
			table.setBackground(Color.white);
			table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {

				@Override
				public void valueChanged(ListSelectionEvent e) {
					if (!e.getValueIsAdjusting()) {
						mainController.clearMessages();
						if (getCurrentItemPaths().size() > 0) {
							mainController.setFilesDownloadable(true);
						} else {
							mainController.setFilesDownloadable(false);
						}
					}
				}
			});
			scroll.setViewportView(table);
			errorLabel.setVisible(false);
		} else {
			System.err.println("Data didn't read.");
			setErrorMessage("There are no items to view.");
			returnResult = false;
		}
		this.revalidate();
		return returnResult;
	}

	/**
	 * This used to update the item display with new items.
	 * 
	 * @param items
	 *            - the new items to display.
	 * @param listName
	 *            - the list of the current items.
	 * @return true if the operation was successful.
	 */
	public boolean UpdateTableWithSearch(NodeList files) {
		mainController.setFilesDownloadable(false);
		boolean returnResult = true;
		String[] columnNames = null;
		if (files != null) {
			NodeList firstFilesElements = files.item(0).getChildNodes();
			int count = 0;
			for (int i = 0; i < firstFilesElements.getLength(); i++) {
				if (!firstFilesElements.item(i).getNodeName().contains("#text")) {
					count++;
				}
			}
			columnNames = new String[count];
			count = 0;
			for (int i = 0; i < firstFilesElements.getLength(); i++) {
				if (!firstFilesElements.item(i).getNodeName().contains("#text")) {
					columnNames[count++] = firstFilesElements.item(i).getNodeName();
				}
			}
		}

		Object[][] data = null;
		if (columnNames != null) {
			data = new Object[files.getLength()][columnNames.length];

			NodeList firstFilesElements = files.item(0).getChildNodes();
			for (int i = 0; i < files.getLength(); i++) {
				int count = 0;
				for (int j = 0; j < firstFilesElements.getLength(); j++) {

					if (!files.item(i).getChildNodes().item(j).getNodeName().contains("#text")) {
						data[i][count++] = files.item(i).getChildNodes().item(j).getTextContent();
					}
				}
			}
		}

		if (data != null && columnNames != null) {
			table = new JTable(data, columnNames) {

				private static final long serialVersionUID = -4122451180044627689L;

				@Override
				public boolean isCellEditable(int row, int column) {
					return false;
				}
			};
			List<String> columns = Arrays.asList(columnNames);
			int indexOfName = columns.indexOf("Name");
			if (indexOfName > -1) {
				table.moveColumn(indexOfName, 0);
				table.getColumnModel().getColumn(0).setMinWidth(150);
			}
			table.setBackground(Color.white);
			table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {

				@Override
				public void valueChanged(ListSelectionEvent e) {
					if (!e.getValueIsAdjusting()) {
						mainController.clearMessages();
						if (getCurrentItemPaths().size() > 0) {
							mainController.setFilesDownloadable(true);
						} else {
							mainController.setFilesDownloadable(false);
						}
					}
				}
			});
			scroll.setViewportView(table);
			errorLabel.setVisible(false);
		} else {
			System.err.println("Data didn't read.");
			setErrorMessage("There are no items to view.");
			returnResult = false;
		}
		this.revalidate();
		return returnResult;
	}

	/**
	 * Sets the error message for the display
	 * 
	 * @param message
	 *            the message to show
	 */
	public void setErrorMessage(String message) {
		System.out.println("Displaying error message: " + message);
		scroll.setVisible(false);
		loadingImage.setVisible(false);
		errorLabel.setVisible(true);
		errorLabel.setText(message);
		errorLabel.setHorizontalAlignment(JLabel.CENTER);
	}

	/**
	 * This is used to display the loading gif during operations by the web
	 * controller.
	 * 
	 * @param willDisplay
	 *            - true to show the loading gif, false to hide it.
	 */
	private void displayLoading(boolean willDisplay) {
		scroll.setVisible(!willDisplay);
		loadingImage.setVisible(willDisplay);
		errorLabel.setVisible(false);
		this.revalidate();
	}

	/**
	 * Is the action associated with starting to connect to a site.
	 */
	public void configureConnectingViewStart() {
		displayLoading(true);
	}

	/**
	 * Is the action associated with starting to load a new list's items.
	 */
	public void startedLoadingItems() {
		displayLoading(true);
	}

	/**
	 * Is the action associated with list items being done loading.
	 */
	public void finishedLoadingItems() {
		displayLoading(false);
	}
}
