package sharepointapp;

import java.awt.Color;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SpringLayout;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

public class SPItemView extends JPanel {
	
	private static final long serialVersionUID = 9001415591452156169L;

	private static SPItemView instance = new SPItemView();

	JScrollPane scroll = new JScrollPane();
	JLabel loadingImage;
	JLabel errorLabel = new JLabel("Connect to a site using the address bar above.");
	String currentList = "";

	public static SPItemView getInstance() {
		return instance;
	}

	JTable table;

	private SPItemView() {

		this.add(scroll);
		scroll.setBackground(new Color(240, 255, 242));
		scroll.getViewport().setBackground(new Color(240, 255, 242));
		scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		setBackground(new Color(240, 255, 242));
		SpringLayout layout = new SpringLayout();
		setLayout(layout);

		URL url = getClass().getResource("/715.GIF");
		if(url != null){
		ImageIcon imageIcon = new ImageIcon(url);
		loadingImage = new JLabel(imageIcon);
		}
		else{
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

	public List<String> getCurrentItemPaths() {
		List<String> paths = new ArrayList<String>();
		for (int i : table.getSelectedRows()) {
			String path = displayRowValues(i);
			if (path != "" && !path.endsWith(".000")) {
				paths.add(path);
			}
		}
		return paths;
	}

	private String displayRowValues(int rowIndex) {
		int columns = table.getColumnCount();
		String s = "";

		String[] siteUrlParts = SharePointWebController.getInstance().getBasesharepointUrl().split("/");
		String currentSiteName = siteUrlParts[siteUrlParts.length - 1];
		for (int col = 0; col < columns; col++) {
			Object o = table.getValueAt(rowIndex, col);
			if (table.getColumnName(col).equalsIgnoreCase("FileRef")) {
				String cellValue = o.toString();
				cellValue = cellValue.replaceAll(".*;#", "");
				cellValue = cellValue.replaceAll(".*" + currentSiteName + "/", "");
				s = "/" + cellValue;
			}
		}
		return s;
	}

	public boolean UpdateTable(List<Object> items, String listName) {
		SPToolbarView.getInstance().download.setEnabled(false);
		currentList = listName;
		boolean returnResult = true;
		String[] columnNames = null;
		if (items != null) {
			columnNames = SharePointWebController.getInstance().getAttributeNames(items);
		}
		Object[][] data = null;
		if (columnNames != null) {
			data = SharePointWebController.getInstance().getData(items, columnNames);
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
						SPToolbarView.getInstance().clearMessageText();
						if (getCurrentItemPaths().size() > 0) {
							SPToolbarView.getInstance().download.setEnabled(true);
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

	public void setErrorMessage(String message) {
		System.out.println("Displaying error message: " + message);
		scroll.setVisible(false);
		loadingImage.setVisible(false);
		errorLabel.setVisible(true);
		errorLabel.setText(message);
		errorLabel.setHorizontalAlignment(JLabel.CENTER);
	}

	public void displayLoading(boolean willDisplay) {
		scroll.setVisible(!willDisplay);
		loadingImage.setVisible(willDisplay);
		errorLabel.setVisible(false);
		this.revalidate();
	}
}
