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
 * @author allarj3
 *
 */
public class SPPreferences {
	private static final String VALUE_SEPARATOR = "---";
	private static final String KEY_SEPARATOR = ";#";
	private Map<String, ArrayList<String>> prefsMap = new HashMap<String, ArrayList<String>>();
	private File prefsFile = new File("SPJavaPrefs.txt");
	static SPPreferences instance = new SPPreferences();

	/**
	 * @return
	 */
	public static SPPreferences GetPreferences() {
		return instance;
	}
	
	/**
	 * 
	 */
	private SPPreferences() {
		try {
			if(!prefsFile.exists()){
				prefsFile.createNewFile();
			}
			else{
				readPrefsFile();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 
	 */
	private void readPrefsFile() {
		try {
			BufferedReader reader = new BufferedReader(new FileReader(prefsFile));
			String line;
			while((line = reader.readLine()) != null){
				processLine(line);
			}
			reader.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * @param line
	 */
	private void processLine(String line) {
		String[] args = line.split(KEY_SEPARATOR);
		if(args.length == 2){
			String key = args[0];
			String[] values = args[1].split(VALUE_SEPARATOR);
			put(key, Arrays.asList(values));
		}
	}

	/**
	 * @param key
	 * @param defaultReturn
	 * @return
	 */
	public String getFirstOrDefault(String key, String defaultReturn) {
		if(prefsMap.containsKey(key) && !prefsMap.get(key).isEmpty()){
			return prefsMap.get(key).get(0);
		}
		return defaultReturn;
	}
	
	/**
	 * @param key
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<String> getAll(String key) {
		if(prefsMap.containsKey(key)){
			return (List<String>) prefsMap.get(key).clone();
		}
		return new ArrayList<String>();
	}

	/**
	 * @param key
	 * @param value
	 */
	public void put(String key, String value) {
		prefsMap.put(key, new ArrayList<String>());
		prefsMap.get(key).add(value);
	}
	
	/**
	 * @param key
	 * @param value
	 * @return
	 */
	public boolean add(String key, String value) {
		if(prefsMap.containsKey(key) && !prefsMap.get(key).contains(value)){
			prefsMap.get(key).add(value);
			return true;
		}
		else if(!prefsMap.containsKey(key)){
			put(key, value);
			return true;
		}
		return false;
	}
	
	/**
	 * @param key
	 * @param values
	 */
	public void put(String key, List<String> values) {
		
		prefsMap.put(key, new ArrayList<>(values));
	}
	
	/**
	 * @param key
	 * @return
	 */
	public boolean keyExists(String key){
		return prefsMap.containsKey(key);
	}

	/**
	 * 
	 */
	@SuppressWarnings("resource")
	public void flush() {
		try {
			FileChannel outChan = new FileOutputStream(prefsFile, true).getChannel();
		    outChan.truncate(0);
		    outChan.close();
			BufferedWriter writer = new BufferedWriter(new FileWriter(prefsFile));
			for(String key: prefsMap.keySet()){
				String outLine = key + KEY_SEPARATOR;
				boolean isFirst = true;
				for(String value: prefsMap.get(key)){
					if(!isFirst){
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
