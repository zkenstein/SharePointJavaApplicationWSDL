package sharepointapp;

import java.awt.Color;
import java.util.List;

import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.SpringLayout;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

public class SPListView extends JPanel {
	private static final long serialVersionUID = 8559762913706624106L;
	private SharePointWebController spController = SharePointWebController.getInstance();
	private List<String> lists;
	private JScrollPane scroll;
	private JList<String> emptyList = new JList<String>(new String[] { "No Lists Found" });

	private List<Object> items = null;
	private GetItemsThread currentItemsThread = null;
	private String listName = "";

	private static SPListView instance = new SPListView();

	public static SPListView getInstance() {
		return instance;
	}

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

	private class GetItemsThread extends Thread {
		@Override
		public void run() {
			try {
				items = spController.getAllListItems(getListName());
			} catch (Exception e) {
				e.printStackTrace();
			}
			if (SPItemView.getInstance().UpdateTable(items, getListName())) {
				SPItemView.getInstance().displayLoading(false);
				SPToolbarView.getInstance().enableUploadButton(true, getListName());
			}
			SPToolbarView.getInstance().enableUploadButton(true, getListName());
			currentItemsThread = null;
		}
	}

	public void getLists(String listToLoad) {
		SPToolbarView.getInstance().download.setEnabled(false);
		SPItemView.getInstance().displayLoading(true);
		scroll.setViewportView(emptyList);
		lists = spController.getAllLists();
		if (lists == null) {
			SPToolbarView.getInstance().setMessageText("Unable to connect to server. Please make sure the URL is correct.",
					false);
			SPItemView.getInstance().setErrorMessage("No Items to Display.");
			return;
		}
		scroll.setVisible(true);
		if (listToLoad == null) {
			setListName(lists.get(0));
		} else {
			setListName(listToLoad);
		}
		updateItems();

		JList<String> listView = new JList<String>(lists.toArray(new String[lists.size()]));
		listView.setSelectedValue(listToLoad, true);
		ListSelectionModel listSelectionModel = listView.getSelectionModel();
		listSelectionModel.addListSelectionListener(new ListSelectionListener() {

			@Override
			public void valueChanged(ListSelectionEvent arg0) {
				if (!arg0.getValueIsAdjusting()) {
					try {
						SPToolbarView.getInstance().clearMessageText();
						SPItemView.getInstance().displayLoading(true);
						SPToolbarView.getInstance().upload.setEnabled(false);
						listView.setSelectedIndex(listView.getSelectedIndex());
						setListName(listView.getSelectedValue());
						updateItems();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		});

		scroll.setViewportView(listView);
		SPToolbarView.getInstance().enableRefreshButton(true);
	}

	public void updateItems() {
		if (currentItemsThread != null && currentItemsThread.isAlive()) {
			currentItemsThread.interrupt();
		}
		currentItemsThread = new GetItemsThread();
		currentItemsThread.start();
	}

	public String getListName() {
		return listName;
	}

	public void setListName(String listName) {
		this.listName = listName;
	}
}
