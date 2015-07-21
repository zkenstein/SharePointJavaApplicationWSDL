package sharepointapp;

import java.awt.Color;
import java.util.List;

import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.SpringLayout;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 * This class is used to present the lists associated with a SharePoint site.
 * @author allarj3
 *
 */
public class SPListView extends SPBaseView {
	private static final long serialVersionUID = 8559762913706624106L;
	private List<String> lists;
	private JScrollPane scroll;
	private JList<String> emptyList = new JList<String>(new String[] { "No Lists Found" });

	private static SPListView instance = new SPListView();

	/**
	 * Returns the static instance of this class.
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
		setBackground(new Color(132, 178, 139));

		scroll = new JScrollPane();
		scroll.setViewportView(emptyList);
		this.add(scroll);

		layout.putConstraint(SpringLayout.WEST, scroll, 5, SpringLayout.WEST, this);
		layout.putConstraint(SpringLayout.NORTH, scroll, 0, SpringLayout.NORTH, this);
		layout.putConstraint(SpringLayout.EAST, scroll, -5, SpringLayout.EAST, this);
		layout.putConstraint(SpringLayout.SOUTH, scroll, -5, SpringLayout.SOUTH, this);

	}

	/**
	 * Used during the start of a site connection.
	 */
	public void configureConnectingViewStart() {
		scroll.setViewportView(emptyList);
	}

	/**
	 * Used to display all of the lists associated with a site.
	 * @param allLists - the lists to display.
	 * @param listToLoad - the list to select for a default, if null the first will be selected.
	 */
	public void displayLists(List<String> allLists, String listToLoad) {
		lists = allLists;
		if (lists == null) {
			mainController.displayMessages("Unable to connect to server. Please make sure the URL is correct.",
					"No Items to Display.", false);
			return;
		}
		scroll.setVisible(true);
		if (listToLoad == null) {
			mainController.setCurrentList(lists.get(0));
		} else {
			mainController.setCurrentList(listToLoad);
		}

		mainController.updateItems();

		JList<String> listView = new JList<String>(lists.toArray(new String[lists.size()]));
		listView.setSelectedValue(listToLoad, true);
		ListSelectionModel listSelectionModel = listView.getSelectionModel();
		listSelectionModel.addListSelectionListener(new SPListSelectionListener(listView));

		scroll.setViewportView(listView);
	}
	
	/**
	 * This class is used to dictate what happens when the list items are selected.
	 * @author Joshua
	 *
	 */
	private class SPListSelectionListener implements ListSelectionListener {
		JList<String> listView;
		
		/**
		 * This class is used to dictate what happens when the list items are selected.
		 * @param listView - the lists to select from.
		 */
		public SPListSelectionListener(JList<String> listView) {
			this.listView = listView;
		}

		@Override
		public void valueChanged(ListSelectionEvent event) {
			if (!event.getValueIsAdjusting()) {
				try {
					listView.setSelectedIndex(listView.getSelectedIndex());
					mainController.setCurrentList(listView.getSelectedValue());

					mainController.updateItems();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
}
