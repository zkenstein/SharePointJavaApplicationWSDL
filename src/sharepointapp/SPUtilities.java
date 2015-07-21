package sharepointapp;

import java.awt.Color;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JFrame;

/**
 * This class is used as a simple utility to store functions/objects that are used in multiple places, independent of controllers.
 * @author allarj3
 *
 */
public class SPUtilities {
	
	/**
	 * Set's the icon for the frame to be the default one used for the application.
	 * @param theFrame - the frame to add the icon to.
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
	 * @return
	 */
	public static Color getDarkThemeColor(){
		return new Color(132, 178, 139);
	}
	
	/**
	 * Returns the lighter color used in the application's theme.
	 * @return
	 */
	public static Color getLightThemeColor(){
		return new Color(240, 255, 242);
	}

}
