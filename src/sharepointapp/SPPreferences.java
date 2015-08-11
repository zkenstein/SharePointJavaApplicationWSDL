package sharepointapp;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class allows values to be stored during current and future runs of the
 * program.
 * 
 * @author allarj3
 *
 */
public class SPPreferences {
	private static final String VALUE_SEPARATOR = "---";
	private static final String KEY_SEPARATOR = ";#";
	private Map<String, ArrayList<String>> prefsMap = new HashMap<String, ArrayList<String>>();
	private File prefsFile = null;
	static SPPreferences instance = new SPPreferences();
	private List<String> singleValueKeys = new ArrayList<String>();
	private Map<String, String> notes = new HashMap<String, String>();

	/**
	 * Returns the static instance of the preference manager
	 * 
	 * @return
	 */
	public static SPPreferences GetPreferences() {
		return instance;
	}

	/**
	 * Creates the preference manager object.
	 */
	private SPPreferences() {
		
		String userHomeDir = System.getProperty("user.home");
		prefsFile = new File(userHomeDir + "/SPJavaFiles/SPJavaPrefs.txt");
		try {
			if (!prefsFile.exists()) {
				prefsFile.getParentFile().mkdirs();
				prefsFile.createNewFile();
			} else {
				readPrefsFile();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Registers a key to only have one value.
	 * 
	 * @param key
	 *            - the key to limit the value count of.
	 */
	public void registerSingleValueOnlyKey(String key) {
		if (prefsMap.containsKey(key) && prefsMap.get(key).size() > 1) {
			put(key, prefsMap.get(key).get(0));
		}
		
		if (!singleValueKeys.contains(key)) {
			singleValueKeys.add(key);
		}
	}

	/**
	 * Checks to see if the key is registered for only one object.
	 * 
	 * @param key
	 *            - the key to check.
	 * @return true if it is for single values only.
	 */
	public boolean isSingleValueOnlyKey(String key) {
		return singleValueKeys.contains(key);
	}

	/**
	 * Reads the prefs file, and adds all of the data into the prefs manager.
	 */
	private void readPrefsFile() {
		try {
			BufferedReader reader = new BufferedReader(new FileReader(prefsFile));
			String line;
			while ((line = reader.readLine()) != null) {
				processLine(line);
			}
			reader.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Reads the current line, processing the keys and values.
	 * 
	 * @param line
	 *            - the line in the file to read.
	 */
	private void processLine(String line) {
		String[] args = line.split(KEY_SEPARATOR);
		if (args.length == 2) {
			String key = args[0];
			String[] values = args[1].split(VALUE_SEPARATOR);
			put(key, Arrays.asList(values));
		}
	}

	/**
	 * Returns either the first value in the list of values, or the default
	 * value.
	 * 
	 * @param key
	 *            - the key to find a value for.
	 * @param defaultReturn
	 *            - the value to return if there are no found values.
	 * @return the string value or default string value.
	 */
	public String getFirstOrDefault(String key, String defaultReturn) {
		if (prefsMap.containsKey(key) && !prefsMap.get(key).isEmpty()) {
			return prefsMap.get(key).get(0);
		}
		return defaultReturn;
	}

	/**
	 * Returns all of the values for the given key.
	 * 
	 * @param key
	 *            - the key to get the values of.
	 * @return the list of string values
	 */
	public List<String> getAll(String key) {
		if (prefsMap.containsKey(key)) {
			// return (List<String>) prefsMap.get(key).clone();
			return new ArrayList<>(prefsMap.get(key));
		}
		return new ArrayList<String>();
	}

	/**
	 * Puts the current value into the preference manager, overriding if there
	 * is already a value there.
	 * 
	 * @param key
	 *            - the key to place the value with.
	 * @param value
	 *            - the value to store.
	 */
	public void put(String key, String value) {
		prefsMap.put(key, new ArrayList<String>());
		prefsMap.get(key).add(value);
	}

	/**
	 * Puts the current value into the preference manager, adding it alongside
	 * any values that may be there already.
	 * 
	 * @param key
	 *            - the key to place the value with.
	 * @param value
	 *            - the value to store.
	 * @return true if the item is added, false otherwise.
	 */
	public boolean add(String key, String value) {
		if (prefsMap.containsKey(key) && !prefsMap.get(key).contains(value)) {

			if (isSingleValueOnlyKey(key)) {
				put(key, value);
			} else {
				prefsMap.get(key).add(value);
			}
			return true;
		} else if (!prefsMap.containsKey(key)) {
			put(key, value);
			return true;
		}
		return false;
	}

	/**
	 * Puts a list of values into the prefs manager
	 * 
	 * @param key
	 *            - the key to store the values with.
	 * @param values
	 *            - the values to store.
	 */
	public void put(String key, List<String> values) {
		if (isSingleValueOnlyKey(key) && values.size() > 1) {
			put(key, values.get(0));
		}
		prefsMap.put(key, new ArrayList<>(values));
	}

	/**
	 * @return The list of keys in the preference manager.
	 */
	public List<String> getKeys() {
		return new ArrayList<String>(prefsMap.keySet());
	}

	/**
	 * Checks to see if the given key is in the preference manager
	 * 
	 * @param key
	 *            - the key to check.
	 * @return true if the key is contained in the preference manager, false
	 *         otherwise.
	 */
	public boolean keyExists(String key) {
		return prefsMap.containsKey(key);
	}

	public void registerNoteForKey(String key, String note) {
		notes.put(key, note);
	}

	public String getNote(String key) {
		if (notes.containsKey(key)) {
			return notes.get(key);
		}
		return "";
	}

	/**
	 * Saves the preference manager to the prefs files.
	 */
	@SuppressWarnings("resource")
	public void flush() {
		try {
			FileChannel outChan = new FileOutputStream(prefsFile, true).getChannel();
			outChan.truncate(0);
			outChan.close();
			BufferedWriter writer = new BufferedWriter(new FileWriter(prefsFile));
			for (String key : prefsMap.keySet()) {
				String outLine = key + KEY_SEPARATOR;
				boolean isFirst = true;
				for (String value : prefsMap.get(key)) {
					if (!isFirst) {
						outLine += VALUE_SEPARATOR;
					}
					outLine += value;
					isFirst = false;
				}
				outLine += "\n";
				writer.write(outLine);
			}
			writer.flush();
			writer.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
