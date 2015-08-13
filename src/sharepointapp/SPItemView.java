package sharepointapp;

import java.awt.Color;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
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
import javax.swing.table.TableColumnModel;

import org.w3c.dom.NodeList;

import sharepointapp.SPItem.ItemType;

/**
 * This class is used to show all of the items associated with a particular
 * list/library.
 * 
 * @author allarj3
 *
 */
public class SPItemView extends SPBaseView {

	private static final String HIDDEN_COLUMNS_KEY = "Hidden Columns in Item View";

	private static final long serialVersionUID = 9001415591452156169L;

	private static SPItemView instance = new SPItemView();

	private JScrollPane scroll = new JScrollPane();
	private JLabel locationLabel = new JLabel("");
	private JLabel loadingImage;
	private JLabel errorLabel = new JLabel("Connect to a site using the address bar above.");

	private JTable table;

	private SPFolder currentFolder;

	private List<Integer> folderIndexes;

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

		prefs.registerNoteForKey(HIDDEN_COLUMNS_KEY,
				"Hide columns by holding Ctrl and double clicking on the headers of the item view table. Remove them here to unhide them.");
		if (!prefs.keyExists(HIDDEN_COLUMNS_KEY)) {
			prefs.add(HIDDEN_COLUMNS_KEY, "Example_Hidden_Column");
		}

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
		this.add(locationLabel);
		loadingImage.setVisible(false);
		errorLabel.setVisible(false);
		locationLabel.setVisible(true);

		errorLabel.setForeground(SPUtilities.getLightThemeFontColor());

		layout.putConstraint(SpringLayout.WEST, scroll, 0, SpringLayout.WEST, this);
		layout.putConstraint(SpringLayout.NORTH, scroll, 0, SpringLayout.NORTH, this);
		layout.putConstraint(SpringLayout.EAST, scroll, 0, SpringLayout.EAST, this);
		layout.putConstraint(SpringLayout.SOUTH, scroll, 0, SpringLayout.NORTH, locationLabel);

		layout.putConstraint(SpringLayout.WEST, locationLabel, 0, SpringLayout.WEST, this);
		layout.putConstraint(SpringLayout.NORTH, locationLabel, -20, SpringLayout.SOUTH, this);
		layout.putConstraint(SpringLayout.EAST, locationLabel, 0, SpringLayout.EAST, this);
		layout.putConstraint(SpringLayout.SOUTH, locationLabel, 0, SpringLayout.SOUTH, this);

		layout.putConstraint(SpringLayout.WEST, loadingImage, 0, SpringLayout.WEST, this);
		layout.putConstraint(SpringLayout.NORTH, loadingImage, 0, SpringLayout.NORTH, this);
		layout.putConstraint(SpringLayout.EAST, loadingImage, 0, SpringLayout.EAST, this);
		layout.putConstraint(SpringLayout.SOUTH, loadingImage, 0, SpringLayout.NORTH, locationLabel);

		layout.putConstraint(SpringLayout.WEST, errorLabel, 0, SpringLayout.WEST, this);
		layout.putConstraint(SpringLayout.NORTH, errorLabel, 0, SpringLayout.NORTH, this);
		layout.putConstraint(SpringLayout.EAST, errorLabel, 0, SpringLayout.EAST, this);
		layout.putConstraint(SpringLayout.SOUTH, errorLabel, 0, SpringLayout.NORTH, locationLabel);

		locationLabel.setBackground(SPUtilities.getDarkThemeColor());
		locationLabel.setOpaque(true);
		locationLabel.setForeground(SPUtilities.getDarkThemeFontColor());
		locationLabel.setHorizontalAlignment(JLabel.CENTER);

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
			if (table.getColumnName(col).equalsIgnoreCase("FileRef")
					|| table.getColumnName(col).equalsIgnoreCase("WebUrl")) {
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
	 * @param isRefresh
	 * @return true if the operation was successful.
	 */
	public boolean UpdateTable(SPFolder folder, String listName, boolean isRefresh) {

		if (folder == null) {
			return false;
		}

		if (isRefresh && currentFolder != null) {
			String previousFolderPath = currentFolder.folderPath;
			folder = folder.getFolderByPath(previousFolderPath);
		}

		currentFolder = folder;
		mainController.setFilesDownloadable(false);

		boolean returnResult = createNewTable(folder, listName, SharePointWebController.FILE_NAME_DISPLAY);
		this.revalidate();
		return returnResult;
	}

	/**
	 * This used to update the item display with new items.
	 * 
	 * @param files
	 *            - the XML list of files.
	 * @return true if the operation was successful.
	 */
	public boolean UpdateTableWithSearch(NodeList files) {
		mainController.setFilesDownloadable(false);
		boolean returnResult = true;

		currentFolder = new SPFolder(files);
		returnResult = createNewTable(currentFolder, "", SharePointWebController.FILE_SEARCH_NAME);

		this.revalidate();
		return returnResult;
	}

	/**
	 * Creates a new table for the item view.
	 * 
	 * @param folder
	 *            - the collection of items and folders to display.
	 * @param listName
	 *            - the list that's being displayed.
	 * @param nameField
	 *            - the 'name' field that should be pulled to the front of the
	 *            columns.
	 * @return true if the table was created successfully.
	 */
	public boolean createNewTable(SPFolder folder, String listName, String nameField) {
		locationLabel.setText("Current Location: " + folder.folderPath);
		boolean returnResult = true;

		String[] columnNames = folder.getColumnNames();
		Object[][] data = folder.getData();

		if (data != null && data.length > 0 && columnNames != null && columnNames.length > 0) {
			table = new ItemViewTable(data, columnNames);

			List<String> columns = Arrays.asList(columnNames);
			int indexOfName = columns.indexOf(nameField);
			if (indexOfName > -1) {
				table.moveColumn(columns.indexOf(nameField), 0);
				table.getColumnModel().getColumn(0).setMinWidth(150);
			}
			int indexOfImage = columns.indexOf("Icon");
			if (indexOfImage > -1) {
				table.moveColumn(columns.indexOf("Icon"), 0);
				table.getColumnModel().getColumn(0).setMaxWidth(20);
			}
			table.setBackground(Color.white);

			folderIndexes = folder.getIndexes(ItemType.Folder);
			table.getSelectionModel().addListSelectionListener(new ItemRowListener());
			table.getTableHeader().addMouseListener(new HeaderMouseListener());

			TableColumnModel colModel = table.getColumnModel();
			for (int i = 0; i < table.getColumnCount(); i++) {

				int columnModelIndex = i;
				if (prefs.getAll(HIDDEN_COLUMNS_KEY)
						.contains(colModel.getColumn(columnModelIndex).getHeaderValue().toString())) {
					// colModel.removeColumn(colModel.getColumn(columnModelIndex));

					colModel.getColumn(columnModelIndex).setMinWidth(0);
					colModel.getColumn(columnModelIndex).setMaxWidth(0);
				}
			}

			scroll.setViewportView(table);
			errorLabel.setVisible(false);
		} else {
			setErrorMessage("There are no items to view.");
			returnResult = false;
		}
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
		if (willDisplay)
			locationLabel.setText("");
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

	/**
	 * Gets the folder path to the current folder. Prints only the portion after
	 * the list path.
	 * 
	 * @return the folder path string.
	 */
	public String getCurrentFolderPath() {
		if (currentFolder != null) {
			String returnString = currentFolder.folderPath;

			if (returnString.contains("/")) {
				returnString = returnString.substring(returnString.indexOf("/"));
			} else {
				returnString = "";
			}
			return returnString;
		} else {
			return "";
		}
	}
	
	/**
	 * A class that will react to a user clicking a table's header.
	 * - CTRL + double click will hide a column
	 * @author allarj3
	 *
	 */
	private final class HeaderMouseListener extends MouseAdapter {
		@Override
		public void mouseClicked(MouseEvent e) {
			if (e.getClickCount() > 1 && e.isControlDown()) {
				TableColumnModel colModel = table.getColumnModel();
				int columnModelIndex = colModel.getColumnIndexAtX(e.getX());
				prefs.add(HIDDEN_COLUMNS_KEY, colModel.getColumn(columnModelIndex).getHeaderValue().toString());
				prefs.flush();
				// colModel.removeColumn(colModel.getColumn(columnModelIndex));

				colModel.getColumn(columnModelIndex).setMinWidth(0);
				colModel.getColumn(columnModelIndex).setMaxWidth(0);
			}
		}
	}

	/**
	 * Listens for rows to be selected on the table.
	 * @author allarj3
	 *
	 */
	private final class ItemRowListener implements ListSelectionListener {
		@Override
		public void valueChanged(ListSelectionEvent e) {
			if (!e.getValueIsAdjusting()) {
				mainController.clearMessages();
				if (getCurrentItemPaths().size() > 0) {
					mainController.setFilesDownloadable(true);
				} else {
					mainController.setFilesDownloadable(false);
				}

				if (folderIndexes.contains(table.getSelectedRow()) && table.getSelectedRowCount() == 1) {
					UpdateTable(currentFolder.getFolderAt(table.getSelectedRow()), mainController.getCurrentList(),
							false);
				}
			}
		}
	}

	/**
	 * Creates a new extension of the JTable class.
	 * Originally had cells un-editable, but re-enabled it to allow copying from cells.
	 * @author allarj3
	 *
	 */
	private final class ItemViewTable extends JTable {
		private static final long serialVersionUID = -4122451180044627689L;

		private ItemViewTable(Object[][] rowData, Object[] columnNames) {
			super(rowData, columnNames);
		}

		@Override
		public boolean isCellEditable(int row, int column) {
			return true;
		}

		@SuppressWarnings({ "unchecked", "rawtypes" })
		public Class getColumnClass(int column) {
			if (getValueAt(0, column) != null) {
				return getValueAt(0, column).getClass();
			} else {
				return this.getClass();
			}
		}
	}
}
