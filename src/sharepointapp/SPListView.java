package sharepointapp;

import java.awt.Color;
import java.awt.Component;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.List;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.SpringLayout;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 * This class is used to present the lists associated with a SharePoint site.
 * 
 * @author allarj3
 *
 */
public class SPListView extends SPBaseView {
	private static final long serialVersionUID = 8559762913706624106L;
	private List<String> lists;
	private JScrollPane scroll;
	private JList<String> emptyList = new JList<String>(new String[] { "No Lists Found" });
	private JLabel message;

	private static SPListView instance = new SPListView();
	private JList<String> listView;

	/**
	 * Returns the static instance of this class.
	 * 
	 * @return
	 */
	public static SPListView getInstance() {
		return instance;
	}

	/**
	 * Creates the new GUI to display the lists.
	 */
	private SPListView() {
		SpringLayout layout = new SpringLayout();
		setLayout(layout);
		setBackground(SPUtilities.getDarkThemeColor());

		scroll = new JScrollPane();
		scroll.setViewportView(emptyList);
		emptyList.setBackground(SPUtilities.getLightThemeColor());
		emptyList.setCellRenderer(new NewListCellRenderer());
		this.add(scroll);

		message = new JLabel("");
		message.setHorizontalAlignment(JLabel.CENTER);
		message.setForeground(SPUtilities.getDarkThemeFontColor());
		this.add(message);

		layout.putConstraint(SpringLayout.WEST, scroll, 5, SpringLayout.WEST, this);
		layout.putConstraint(SpringLayout.NORTH, scroll, 0, SpringLayout.NORTH, this);
		layout.putConstraint(SpringLayout.EAST, scroll, -5, SpringLayout.EAST, this);
		layout.putConstraint(SpringLayout.SOUTH, scroll, -20, SpringLayout.SOUTH, this);

		layout.putConstraint(SpringLayout.WEST, message, 0, SpringLayout.WEST, this);
		layout.putConstraint(SpringLayout.NORTH, message, -20, SpringLayout.SOUTH, this);
		layout.putConstraint(SpringLayout.EAST, message, 0, SpringLayout.EAST, this);
		layout.putConstraint(SpringLayout.SOUTH, message, 0, SpringLayout.SOUTH, this);
	}

	/**
	 * Used during the start of a site connection.
	 */
	public void configureConnectingViewStart() {
		scroll.setViewportView(emptyList);
	}

	/**
	 * Used to display all of the lists associated with a site.
	 * 
	 * @param allLists
	 *            - the lists to display.
	 * @param listToLoad
	 *            - the list to select for a default, if null the first will be
	 *            selected.
	 */
	public void displayLists(List<String> allLists, String listToLoad) {
		lists = allLists;
		if (lists == null) {
			mainController.displayMessages("Unable to connect to server.",
					"Unable to connect to the supplied URL. Please double check the entered URL.", false);
			return;
		}
		scroll.setVisible(true);
		if (listToLoad == null) {
			mainController.setCurrentList(lists.get(0));
			mainController.updateItems(false);
		} else {
			mainController.setCurrentList(listToLoad);
			mainController.updateItems(true);
		}


		listView = new JList<String>(lists.toArray(new String[lists.size()]));
		listView.setBackground(SPUtilities.getLightThemeColor());
		setSelected(listToLoad);
		ListSelectionModel listSelectionModel = listView.getSelectionModel();
		listSelectionModel.addListSelectionListener(new SPListSelectionListener(listView));

		listView.setCellRenderer(new NewListCellRenderer());

		scroll.setViewportView(listView);
	}

	public void setSelected(String listToSelect) {
		if (listToSelect == null) {
			listView.setSelectedValue("Search", false);
		} else {
			listView.setSelectedValue(listToSelect, true);
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
	 * Clears the messages associated with the view.
	 */
	public void clearMessageText() {
		if (!mainController.messageLock) {
			setMessageText("", true);
		}
	}

	/**
	 * Used to present the cells in the list view.
	 * @author allarj3
	 *
	 */
	private final class NewListCellRenderer implements ListCellRenderer<String> {
		@Override
		public Component getListCellRendererComponent(JList<? extends String> list, String value, int index,
				boolean isSelected, boolean cellHasFocus) {
			
			BufferedImage img = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
			Graphics2D g2d = img.createGraphics();
			FontMetrics fm = g2d.getFontMetrics();

;
		    if(fm.stringWidth(value) > 150){
		    	int split = value.substring(0, value.length()/2 + 5).lastIndexOf(" ");
		    	value = value.substring(0, split) + "<br /><div style='margin-left: 15'>" + value.substring(split) + "</div>";
		    }
		    //JLabel panel = new JLabel("<html><p style='margin-left: 5'>  " + value + "</p></html>");
		    value = "<html><p style='margin-left: 5'>  " + value + "</p></html>";
		    
		    Component panel = new DefaultListCellRenderer().getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
		    panel.setForeground(SPUtilities.getLightThemeFontColor());
			return panel;
		}
	}

	/**
	 * This class is used to dictate what happens when the list items are
	 * selected.
	 * 
	 * @author Joshua
	 *
	 */
	private class SPListSelectionListener implements ListSelectionListener {
		JList<String> listView;

		/**
		 * This class is used to dictate what happens when the list items are
		 * selected.
		 * 
		 * @param listView
		 *            - the lists to select from.
		 */
		public SPListSelectionListener(JList<String> listView) {
			this.listView = listView;
		}

		@Override
		public void valueChanged(ListSelectionEvent event) {
			if (!event.getValueIsAdjusting()) {
				try {
					mainController.clearMessages();
					listView.setSelectedIndex(listView.getSelectedIndex());
					mainController.setCurrentList(listView.getSelectedValue());

					mainController.updateItems(false);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
}
