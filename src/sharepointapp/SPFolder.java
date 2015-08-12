package sharepointapp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.w3c.dom.NodeList;

import sharepointapp.SPItem.ItemType;

public class SPFolder {
	List<Object> items;
	private String[] originalNames = null;
	private Object[][] originalData = null;
	private List<SPItem> spItems = new ArrayList<SPItem>();
	private List<String> columnNames = new ArrayList<String>();
	private List<SPFolder> subFolders = new ArrayList<SPFolder>();
	private List<SPFolder> allFolders = new ArrayList<SPFolder>();
	String folderPath = "";
	SPFolder rootFolder = null;

	SharePointWebController webController = SharePointWebController.getInstance();

	public SPFolder(List<Object> allListItems, String folderPath) {
		this.folderPath = folderPath;
		items = new ArrayList<Object>(allListItems);

		if (items != null) {
			originalNames = webController.getAttributeNames(items);
		}

		if (originalNames != null) {
			columnNames = new ArrayList<String>(Arrays.asList(originalNames));
			originalData = webController.getData(items, originalNames);
			List<SPItem> lowerLevelItems = new ArrayList<SPItem>();
			for (Object[] itemArray : originalData) {
				SPItem item = new SPItem(itemArray, originalNames);
				if (item.belongsToFolder(this)) {
					spItems.add(item);
					if (item.itemType.equals(ItemType.Folder)) {
						SPFolder newFolder = new SPFolder(new ArrayList<SPItem>(), columnNames, item.fields.get("FileRef").toString(), this);
						subFolders.add(newFolder);
						allFolders.add(newFolder);
						item.folder = newFolder;
					}
				}
				else{
					lowerLevelItems.add(item);
					if (item.itemType.equals(ItemType.Folder)) {
						SPFolder newFolder = new SPFolder(new ArrayList<SPItem>(), columnNames, item.fields.get("FileRef").toString(), this);
						allFolders.add(newFolder);
						item.folder = newFolder;
					}
				}

			}
			List<SPItem> unadded = new ArrayList<SPItem>();
			for(SPItem item: lowerLevelItems){
				boolean added = false;
				for(SPFolder folder: allFolders){
					if(item.belongsToFolder(folder)){
						folder.spItems.add(item);
						if(item.folder != null){
							item.folder.addParentFolder(folder);
						}
						added = true;
						break;
					}
				}
				
				if(!added){
					unadded.add(item);
				}
			}
			
			spItems.addAll(unadded);
			java.util.Collections.sort(spItems);
		}

		if (originalData != null) {
			columnNames.add("Icon");
		}

	}
	
	public SPFolder(NodeList files){
		this.folderPath = "Search";
		
		String[] columns = null;
		if (files != null) {
			NodeList firstFilesElements = files.item(0).getChildNodes();
			int count = 0;
			for (int i = 0; i < firstFilesElements.getLength(); i++) {
				if (!firstFilesElements.item(i).getNodeName().contains("#text")) {
					count++;
				}
			}
			columns = new String[count];
			count = 0;
			for (int i = 0; i < firstFilesElements.getLength(); i++) {
				if (!firstFilesElements.item(i).getNodeName().contains("#text")) {
					columns[count++] = firstFilesElements.item(i).getNodeName();
				}
			}
		}

		columnNames = new ArrayList<String>(Arrays.asList(columns));

		Object[][] data = null;
		if (columns != null) {
			data = new Object[files.getLength()][columns.length];

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
		
		for (Object[] itemArray : data) {
			SPItem item = new SPItem(itemArray, columns);
			spItems.add(item);
			item.fileNameDisplay = SharePointWebController.FILE_SEARCH_NAME;
		}
		

		columnNames.add("Icon");
		
	}

	private void addParentFolder(SPFolder folder) {
		SPItem parentFolderItem = new SPItem();
		parentFolderItem.fields.put(SharePointWebController.FILE_NAME_DISPLAY, "Parent Folder");
		parentFolderItem.fields.put("FileRef", folder.folderPath);
		parentFolderItem.folder = folder;
		parentFolderItem.itemType = ItemType.Folder;
		
		spItems.add(parentFolderItem);
	}

	public SPFolder(List<SPItem> items, List<String> names, String folderPath, SPFolder root) {
		this.spItems = items;
		this.columnNames = names;
		this.folderPath = folderPath;
		this.rootFolder = root;
		
		SPItem rootFolderItem = new SPItem();
		rootFolderItem.fields.put(SharePointWebController.FILE_NAME_DISPLAY, "Root Folder");
		rootFolderItem.fields.put("FileRef", rootFolder.folderPath);
		rootFolderItem.folder = rootFolder;
		rootFolderItem.itemType = ItemType.Folder;
		
		spItems.add(rootFolderItem);
	}

	public List<Object> getItems() {
		return items;
	}

	public String[] getColumnNames() {
		return columnNames.toArray(new String[columnNames.size()]);
	}

	public Object[][] getData() {
		// return data;

		java.util.Collections.sort(spItems);
		Object[][] newData = new Object[spItems.size()][];
		for (int i = 0; i < spItems.size(); i++) {
			newData[i] = spItems.get(i).getFields(getColumnNames());
		}
		return newData;
	}

	public List<Integer> getIndexes(ItemType type) {
		List<Integer> indexes = new ArrayList<Integer>();
		for (int i = 0; i < spItems.size(); i++) {
			if (spItems.get(i).itemType.equals(type)) {
				indexes.add(i);
			}
		}

		return indexes;
	}

	public SPFolder getFolderAt(int rowIndex) {
		System.out.println(spItems.get(rowIndex).folder.folderPath);
		return spItems.get(rowIndex).folder;
	}

	public SPFolder getFolderByPath(String previousFolderPath) {
		for(SPFolder folder: allFolders){
			if(folder.folderPath.equals(previousFolderPath)){
				return folder;
			}
		}
		return this;
	}

}
