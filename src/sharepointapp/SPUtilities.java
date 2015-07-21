package sharepointapp;

import java.awt.Color;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JFrame;

public class SPUtilities {
	
	public static void setFrameIcon(JFrame theFrame) {
		URL url = (new Object()).getClass().getResource("/sampleIcon.png");
		if (url != null) {
			ImageIcon imageIcon = new ImageIcon(url);
			theFrame.setIconImage(imageIcon.getImage());
		}
	}
	
	public static Color getDarkThemeColor(){
		return new Color(132, 178, 139);
	}
	
	public static Color getLightThemeColor(){
		return new Color(240, 255, 242);
	}

}
