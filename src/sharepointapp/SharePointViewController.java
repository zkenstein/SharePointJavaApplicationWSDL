package sharepointapp;

public class SharePointViewController {
	private static SharePointViewController instance = new SharePointViewController();
	
	private SharePointWebController webController = SharePointWebController.getInstance();
	private SPToolbarView toolbar = SPToolbarView.getInstance();
	private SPListView lists = SPListView.getInstance();
	private SPItemView items = SPItemView.getInstance();
	
	
	private SharePointViewController() {
		// TODO Auto-generated constructor stub
	}
	
	public static SharePointViewController getInstance() {
		return instance;
	}
}
