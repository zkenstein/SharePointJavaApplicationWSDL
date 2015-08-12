package sharepointapp;

import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * This is the class responsible for communicating between the different views and for managing different processes.
 * This allows the ListView, ItemView, ToolbarView, and Web Controller to exist without knowing about each other.
 * @author allarj3
 *
 */
public class SharePointMainController {
	
	private static SharePointMainController instance = new SharePointMainController();
	private SharePointWebController webController;
	private SPToolbarView toolbar;
	private SPListView listView;
	private SPItemView itemView;
	private String currentListName = "";
	private GetItemsThread currentItemsThread = null;
	public boolean messageLock;

	/**
	 * Create a new SharePointMainController. This is empty, only because the constructor needed to be private.
	 */
	private SharePointMainController() {
		
	}

	/**
	 * Returns the MainController instance that everything uses.
	 * @return
	 */
	public static SharePointMainController getInstance() {
		return instance;
	}

	/**
	 * Because of concurrency issues, the main controller needs to be initialized after all of the other views and controllers are created.
	 * This must be called before any functions cna run on the main controller, though.
	 */
	public void initialize() {
		webController = SharePointWebController.getInstance();
		toolbar = SPToolbarView.getInstance();
		listView = SPListView.getInstance();
		itemView = SPItemView.getInstance();
	}

	/**
	 * Connects the views to a specific site
	 * @param url - the site to connect to
	 */
	public void connectToSite(String url) {

		toolbar.configureConnectingViewStart(url);
		listView.configureConnectingViewStart();
		itemView.configureConnectingViewStart();
		webController.connectToUrl(url);
	}

	/**
	 * This is called when the web controller is done loading a site and it will then pass the lists to the list view.
	 * @param allLists - the lists that the web controller loaded from the server
	 * @param listToLoad - the list to load, if one was specified during a refresh. It will be null when loading a new site.
	 */
	public void finishedConnectingToSite(List<String> allLists, String listToLoad) {
		listView.displayLists(allLists, listToLoad);

		MainView.frame.requestFocus();
	}

	/**
	 * Updates the list of items to view.
	 * @param isRefresh 
	 */
	public void updateItems(boolean isRefresh) {
		toolbar.clearMessageText();
		itemView.startedLoadingItems();
		toolbar.enableUploadButton(false, currentListName);

		if (currentItemsThread != null && currentItemsThread.isAlive()) {
			currentItemsThread.interrupt();
		}
		currentItemsThread = new GetItemsThread(currentListName, isRefresh);
		currentItemsThread.start();
	}


	/**
	 * This class is used to populate the item view in a new thread, allowing the GUI not to be frozen during the process.
	 * @author allarj3
	 *
	 */
	private class GetItemsThread extends Thread {

		String listName;
		boolean isRefresh = false;

		public GetItemsThread(String listName, boolean isRefresh) {
			this.listName = listName;
			this.isRefresh = isRefresh;
		}

		@Override
		public void run() {
			try {
				SPFolder folder = webController.getFolder(listName);
				if (itemView.UpdateTable(folder, listName, isRefresh)) {
					itemView.finishedLoadingItems();
					// toolbar.enableUploadButton(true, listName);
					toolbar.finishedConnection(listName);
				}
				toolbar.enableUploadButton(true, listName);
			} catch (Exception e) {
				e.printStackTrace();
			}

			currentItemsThread = null;
		}
	}
	
	/**
	 * Tells the components whether or not to display loading signs
	 * @param willDisplay - true if loading signs are wanted.
	 */
	public void displayLoading(boolean willDisplay){
		if(willDisplay){
			itemView.startedLoadingItems();
		}
		else{
			itemView.finishedLoadingItems();
		}
	}
	
	/**
	 * Configures the view for loading a search result.
	 * @param search - the string to search SharePoint for.
	 */
	public void getSearch(String search){
		Document doc = webController.getSearch(search);
		if(doc == null){
			displayLoading(false);
			displayMessages("Failed to search site", "Search Failed", false);
			return;
		}
		listView.setSelected(null);

		NodeList fileCountElements = doc.getDocumentElement().getElementsByTagName("FileCount");
		if (fileCountElements.getLength() == 1) {
			Node fileCount = fileCountElements.item(0);
			displayLoading(false);
			displayMessages("Files Found: " + fileCount.getTextContent(), null, true);
		}
		
		NodeList fileElements = doc.getDocumentElement().getElementsByTagName("File");
		if (fileElements != null && fileElements.getLength() > 0) {
			itemView.UpdateTableWithSearch(fileElements);
		}
		else{
			displayMessages(null, "No Files Found", false);
		}
	}

	/**
	 * Displays error messages to the different views.
	 * @param toolbarMessage - the message to display on the tool bar.
	 * @param itemViewMessage - the message to display in the items view
	 * @param isSuccessful - used to indicate if the message was concerned with an error or a success
	 */
	public void displayMessages(String toolbarMessage, String itemViewMessage, boolean isSuccessful) {
		if (toolbarMessage != null) {
			//toolbar.setMessageText(toolbarMessage, isSuccessful);
			listView.setMessageText(toolbarMessage, isSuccessful);
		}
		if (itemViewMessage != null) {
			itemView.setErrorMessage(itemViewMessage);
		}
	}

	/**
	 * Clears the messages from the different views.
	 */
	public void clearMessages() {
		toolbar.clearMessageText();
		listView.clearMessageText();
	}

	/**
	 * Gets the current paths selected in the items view.
	 * @return The list of items paths in string form
	 */
	public List<String> getCurrentItemPaths() {
		return itemView.getCurrentItemPaths();
	}

	/**
	 * Is used to state that selected items can be downloaded
	 * @param areDownloadble - the value to set it to
	 */
	public void setFilesDownloadable(boolean areDownloadble) {
		toolbar.enableDownloadButton(areDownloadble);
	}
	
	/**
	 * Returns the current list's name
	 * @return the list
	 */
	public String getCurrentList() {
		return currentListName;
	}
	
	/**
	 * Returns the current list's name
	 * @return the list
	 */
	public String getCurrentFolderPath() {
		return itemView.getCurrentFolderPath();
	}

	/**
	 * Sets the current list's name
	 * @param listName - the list to make current
	 */
	public void setCurrentList(String listName) {
		currentListName = listName;
	}
}
