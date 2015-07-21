package sharepointapp;

import java.awt.Container;
import java.awt.Dimension;

import javax.swing.JFrame;
import javax.swing.SpringLayout;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;;

/**
 * @author allarj3
 * This is the main class, that contains the frame and adds all of the views to it.
 * It also initialized the Main Controller.
 */
public class MainView {

	/**
	 * The main frame that all content will be added to, except the Login Window
	 */
	public static JFrame frame = new JFrame("SharePoint in Java");

	/**
	 * @author allarj3
	 * This class is simply the main program that can be run in a separate thread. This allows it to be run after the program has started, so everything is initialize properly.
	 */
	private static class FrameCreation extends Thread {
		/* (non-Javadoc)
		 * @see java.lang.Thread#run()
		 */
		@Override
		public void run() {
			frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
			frame.setMinimumSize(new Dimension(600, 500));
			frame.setVisible(true);

			SPListView listView = SPListView.getInstance();
			SPToolbarView toolbar = SPToolbarView.getInstance();
			SPItemView itemView = SPItemView.getInstance();
			SharePointMainController.getInstance().initialize();
			Container pane = frame.getContentPane();
			
			SPUtilities.setFrameIcon(frame); 
			
			
			SpringLayout paneLayout = new SpringLayout();
			pane.setLayout(paneLayout);
			pane.add(toolbar);
			pane.add(listView);
			pane.add(itemView);

			paneLayout.putConstraint(SpringLayout.WEST, toolbar, 0, SpringLayout.WEST, pane);
			paneLayout.putConstraint(SpringLayout.NORTH, toolbar, 0, SpringLayout.NORTH, pane);
			paneLayout.putConstraint(SpringLayout.EAST, toolbar, 0, SpringLayout.EAST, pane);
			paneLayout.putConstraint(SpringLayout.SOUTH, toolbar, 70, SpringLayout.NORTH, pane);

			paneLayout.putConstraint(SpringLayout.WEST, listView, 0, SpringLayout.WEST, pane);
			paneLayout.putConstraint(SpringLayout.NORTH, listView, 0, SpringLayout.SOUTH, toolbar);
			paneLayout.putConstraint(SpringLayout.EAST, listView, 200, SpringLayout.WEST, pane);
			paneLayout.putConstraint(SpringLayout.SOUTH, listView, 0, SpringLayout.SOUTH, pane);

			paneLayout.putConstraint(SpringLayout.WEST, itemView, 0, SpringLayout.EAST, listView);
			paneLayout.putConstraint(SpringLayout.NORTH, itemView, 0, SpringLayout.SOUTH, toolbar);
			paneLayout.putConstraint(SpringLayout.EAST, itemView, 0, SpringLayout.EAST, pane);
			paneLayout.putConstraint(SpringLayout.SOUTH, itemView, 0, SpringLayout.SOUTH, pane);
		}
	}

	/**
	 * This is the main program that will invoke the GUI creation after everything else has been initialized.
	 * @param args - command line arguments for the program. Currently unused.
	 */
	public static void main(String[] args) {
		try {
			SwingUtilities.invokeLater((new FrameCreation()));

		} catch (Exception ex) {
			ex.printStackTrace();
			System.err.println(ex);
		}

	}

	

}
