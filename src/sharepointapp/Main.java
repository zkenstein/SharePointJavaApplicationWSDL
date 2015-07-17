package sharepointapp;

import java.awt.Container;
import java.awt.Dimension;

import javax.swing.JFrame;
import javax.swing.SpringLayout;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;;


public class Main {

	public static JFrame frame = new JFrame("SharePoint in Java");
	
	private static class FrameCreation extends Thread {
		@Override
		public void run() {
			frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
			frame.setMinimumSize(new Dimension(600, 500));
			frame.setVisible(true);
			
			SPListView listView = SPListView.getInstance();
			SPToolbarView toolbar = SPToolbarView.getInstance();
			SPItemView itemView = SPItemView.getInstance();
			Container pane = frame.getContentPane();
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

	public static void main(String[] args) {
		try {
			SwingUtilities.invokeLater((new FrameCreation()));

		} catch (Exception ex) {
			ex.printStackTrace();
			System.err.println(ex);
		}

	}

}
