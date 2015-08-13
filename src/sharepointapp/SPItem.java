package sharepointapp;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.swing.ImageIcon;

/**
 * This class represents an item/file/folder received from SharePoint.
 * @author allarj3
 *
 */
public class SPItem implements Comparable<SPItem> {

	Map<String, Object> fields = new HashMap<String, Object>();
	ItemType itemType = ItemType.ListItem;
	SPFolder folder = null;
	String folderPath = "";
	boolean isSpecialFolder = false;
	String fileNameDisplay = SharePointWebController.FILE_NAME_DISPLAY;

	/**
	 * Creates a new item from the object array.
	 * @param fieldArray - the fields to parse.
	 * @param columnNames - the field names to parse.
	 */
	public SPItem(Object[] fieldArray, String[] columnNames) {
		for (int i = 0; i < columnNames.length; i++) {
			fields.put(columnNames[i], fieldArray[i]);
		}

		if (fields.containsKey("FileRef") && fields.containsKey("FSObjType")) {
			itemType = ItemType.getItemType(fields.get("FSObjType").toString(), fields.get("FileRef").toString());
			fields.put("FSObjType", itemType);
		}
		else if(fields.containsKey("FileRef")){
			itemType = ItemType.getItemType(fields.get("FileRef").toString());
			fields.put("FSObjType", itemType);
		}
		else if(fields.containsKey("WebRelativeUrl")){
			itemType = ItemType.getItemType(fields.get("WebRelativeUrl").toString());
			fields.put("FSObjType", itemType);
		}

		if (fields.containsKey("FileRef")) {
			String ref = fields.get("FileRef").toString();
			folderPath = ref.substring(0, ref.lastIndexOf("/"));
		}
	}

	/**
	 * Creates a blank special folder item. Used for Root and Parent items.
	 */
	public SPItem() {
		isSpecialFolder = true;
	}

	/**
	 * Get the all of the fields as an array, matching the columnNames format!
	 * @param columnNames - the fields to look for.
	 * @return the fields as an object array.
	 */
	public Object[] getFields(String[] columnNames) {
		Object[] dataRow = new Object[columnNames.length];
		for (int i = 0; i < columnNames.length; i++) {
			if (fields.containsKey(columnNames[i])) {
				dataRow[i] = fields.get(columnNames[i]);
			} else if (columnNames[i].equals("Icon") && itemType == ItemType.Folder) {
				URL url = getClass().getResource("/folder.gif");
				if (url != null) {
					dataRow[i] = new ImageIcon(url);

				}
			} else if (columnNames[i].equals("Icon") && itemType == ItemType.File) {
				URL url = getClass().getResource("/file.gif");
				if (url != null) {
					dataRow[i] = new ImageIcon(url);

				}
			} else if (columnNames[i].equals("Icon") && itemType == ItemType.ListItem) {
				URL url = getClass().getResource("/item.png");
				if (url != null) {
					dataRow[i] = new ImageIcon(url);

				}
			} else {
				dataRow[i] = "";
			}
		}
		return dataRow;
	}

	/**
	 * This enum represents the different things an item can represent.
	 * @author allarj3
	 *
	 */
	public enum ItemType {
		Folder("Folder", 0), File("File", 1), ListItem("Item", 2);

		private String name;
		private int order;

		/**
		 * Creates a new item type.
		 * @param name - the name of the type.
		 * @param order - the order to sort it by.
		 */
		private ItemType(String name, int order) {
			this.name = name;
			this.order = order;
		}
		
		@Override
		public String toString() {
			return name;
		}

		/**
		 * Returns the item type based on the number type and file ref.
		 * @param num - the FSObjType value.
		 * @param fileRef - the file reference value.
		 * @return the wanted ItemType.
		 */
		public static ItemType getItemType(String num, String fileRef) {
			try {
				int type = Integer.parseInt(num);
				if (type == 1) {
					return Folder;
				}

				if (!fileRef.endsWith(".000")) {
					return File;
				}

				return ListItem;
			} catch (Exception e) {
				return ListItem;
			}
		}

		/**
		 * Returns the item type based on an item's url.
		 * @param url - the url location of the file.
		 * @return the wanted ItemType.
		 */
		public static ItemType getItemType(String url) {

			if (!url.endsWith(".000")) {
				return File;
			}

			return ListItem;
		}
	}

	@Override
	public int compareTo(SPItem arg0) {
		if (isSpecialFolder == arg0.isSpecialFolder) {

			if (itemType.order == arg0.itemType.order) {
				if (fields.containsKey(fileNameDisplay) && arg0.fields.containsKey(fileNameDisplay)) {
					if (fields.get(fileNameDisplay).toString().equals("Root Folder")
							|| arg0.fields.get(fileNameDisplay).toString().equals("Root Folder")) {
						return fields.get(fileNameDisplay).toString().equals("Root Folder") ? -1 : 1;
					}
					return fields.get(fileNameDisplay).toString()
							.compareToIgnoreCase(arg0.fields.get(fileNameDisplay).toString());
				}
			}
		} else {
			return isSpecialFolder ? -1 : 1;
		}
		return itemType.order - arg0.itemType.order;
	}

	/**
	 * Checks to see if the item belongs to the provided folder.
	 * @param spFolder - the folder to see if this item belongs to.
	 * @return true if it the folder should contain it, false otherwise.
	 */
	public boolean belongsToFolder(SPFolder spFolder) {
		// TODO Auto-generated method stub
		return spFolder.folderPath.equalsIgnoreCase(folderPath);
	}
}
