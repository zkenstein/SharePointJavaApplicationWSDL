package sharepointapp;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.swing.ImageIcon;

public class SPItem implements Comparable<SPItem> {

	Map<String, Object> fields = new HashMap<String, Object>();
	ItemType itemType = ItemType.ListItem;
	SPFolder folder = null;
	String folderPath = "";
	boolean isSpecialFolder = false;
	String fileNameDisplay = SharePointWebController.FILE_NAME_DISPLAY;

	public SPItem(Object[] itemArray, String[] columnNames) {
		for (int i = 0; i < columnNames.length; i++) {
			fields.put(columnNames[i], itemArray[i]);
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

	public SPItem() {
		isSpecialFolder = true;
	}

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
			} else if (itemType == ItemType.ListItem) {
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

	public enum ItemType {
		Folder("Folder", 0), File("File", 1), ListItem("Item", 2);

		private String name;
		private int order;

		private ItemType(String name, int order) {
			this.name = name;
			this.order = order;
		}

		@Override
		public String toString() {
			return name;
		}

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

	public boolean belongsToFolder(SPFolder spFolder) {
		// TODO Auto-generated method stub
		return spFolder.folderPath.equalsIgnoreCase(folderPath);
	}
}
