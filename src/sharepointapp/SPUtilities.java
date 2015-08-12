package sharepointapp;

import java.awt.Color;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JFrame;

/**
 * This class is used as a simple utility to store functions/objects that are
 * used in multiple places, independent of controllers.
 * 
 * @author allarj3
 *
 */
public class SPUtilities {
	private static final String DEFAULT_DARK_THEME_COLOR = "Default Dark Theme Color";
	private static final String DEFAULT_LIGHT_THEME_COLOR = "Default Light Theme Color";
	private static final String DEFAULT_DARK_THEME_FONT_COLOR = "Default Dark Theme Font Color";
	private static final String DEFAULT_LIGHT_THEME_FONT_COLOR = "Default Light Theme Font Color";
	static SPPreferences prefs = SPPreferences.GetPreferences();

	/**
	 * Set's the icon for the frame to be the default one used for the
	 * application.
	 * 
	 * @param theFrame
	 *            - the frame to add the icon to.
	 */
	public static void setFrameIcon(JFrame theFrame) {
		URL url = (new Object()).getClass().getResource("/sampleIcon.png");
		if (url != null) {
			ImageIcon imageIcon = new ImageIcon(url);
			theFrame.setIconImage(imageIcon.getImage());
		}
	}

	/**
	 * Returns the darker color used in the application's theme.
	 * 
	 * @return
	 */
	public static Color getDarkThemeColor() {
		Color defaultColor = new Color(132, 178, 139);
		return handleColorGeneration(defaultColor, DEFAULT_DARK_THEME_COLOR);
	}

	/**
	 * Returns the lighter color used in the application's theme.
	 * 
	 * @return
	 */
	public static Color getLightThemeColor() {

		Color defaultColor = new Color(240, 255, 242);
		return handleColorGeneration(defaultColor, DEFAULT_LIGHT_THEME_COLOR);
	}
	
	/**
	 * Returns the font color for darker-theme components.
	 * 
	 * @return
	 */
	public static Color getDarkThemeFontColor() {

		Color defaultColor = Color.WHITE;
		return handleColorGeneration(defaultColor,  DEFAULT_DARK_THEME_FONT_COLOR);
	}
	
	/**
	 * Returns the font color for lighter-theme components.
	 * 
	 * @return
	 */
	public static Color getLightThemeFontColor() {

		Color defaultColor = Color.BLACK;
		return handleColorGeneration(defaultColor,  DEFAULT_LIGHT_THEME_FONT_COLOR);
	}

	public static Color handleColorGeneration(Color defaultColor, String prefsKey) {

		String defaultColorHex = "0x" + Integer.toHexString(defaultColor.getRGB()).substring(2);
		try {
			if (!prefs.keyExists(prefsKey) || prefs.getFirstOrDefault(prefsKey, "").isEmpty()) {
				prefs.put(prefsKey, defaultColorHex);
				prefs.flush();
			}

			prefs.registerSingleValueOnlyKey(prefsKey);
			prefs.registerNoteForKey(prefsKey, "Format: 0xRRGGBB where R,G,B are hex values for the RGB color format. Restart the application after changing these values.");

			return Color.decode(prefs.getFirstOrDefault(prefsKey, defaultColorHex));
		} catch (Exception e) {
			prefs.put(prefsKey, defaultColorHex);
			prefs.flush();
			return defaultColor;
		}
	}

}
