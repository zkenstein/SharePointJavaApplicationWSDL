package sharepointapp;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SpringLayout;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

/**
 * This class creates a view that allows a user to see all of the saved preferences and to edit them.
 * @author Joshua
 *
 */
public class SPPreferencesWindow {

	private SPPreferences prefs = SPPreferences.GetPreferences();
	public JFrame frame = new JFrame("SharePoint in Java - Preferences");

	private List<PrefElement> preferenceElements = new ArrayList<PrefElement>();

	private static SPPreferencesWindow instance = new SPPreferencesWindow();
	
	Runnable postMethodToRun;

	/**
	 * This class is responsible for creating the main frame to view and edit the preferences 
	 * @author Joshua
	 *
	 */
	private class FrameCreation extends Thread {
		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Thread#run()
		 */
		Container pane;
		JPanel contentPanel;
		SpringLayout paneLayout;
		JScrollPane scroll;
		Component previous;
		JButton saveButton;

		
		/* (non-Javadoc)
		 * @see java.lang.Thread#run()
		 */
		@Override
		public void run() {
			frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
			frame.setMinimumSize(new Dimension(600, 500));
			frame.setVisible(true);
			frame.setLocationRelativeTo(null);
			SPUtilities.setFrameIcon(frame);

			pane = frame.getContentPane();
			paneLayout = new SpringLayout();
			contentPanel = new JPanel();
			scroll = new JScrollPane();
			previous = contentPanel;
			contentPanel.setLayout(paneLayout);
			contentPanel.setBackground(SPUtilities.getLightThemeColor());
			pane.add(scroll);

			List<String> keys = prefs.getKeys();
			java.util.Collections.sort(keys);
			for (int i = 0; i < keys.size(); i++) {
				PrefElement element = new PrefElement(keys.get(i));
				preferenceElements.add(element);
				contentPanel.add(element);
			}

			int boxSizes = 0;
			for (PrefElement element : preferenceElements) {
				boxSizes += SetSpringLayout(element);

			}

			saveButton = new JButton("Save");
			saveButton.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					saveAndClose();
				}
			});
			contentPanel.add(saveButton);
			paneLayout.putConstraint(SpringLayout.WEST, saveButton, 150, SpringLayout.WEST, contentPanel);
			paneLayout.putConstraint(SpringLayout.NORTH, saveButton, -30, SpringLayout.SOUTH, contentPanel);
			paneLayout.putConstraint(SpringLayout.EAST, saveButton, -150, SpringLayout.EAST, contentPanel);
			paneLayout.putConstraint(SpringLayout.SOUTH, saveButton, -10, SpringLayout.SOUTH, contentPanel);
			
			JLabel infoDisplay = new JLabel("Remember to press 'Enter' when finished writing in a cell!");
			contentPanel.add(infoDisplay);
			infoDisplay.setHorizontalAlignment(SwingConstants.CENTER);
			infoDisplay.setForeground(SPUtilities.getLightThemeFontColor());

			paneLayout.putConstraint(SpringLayout.WEST, infoDisplay, 10, SpringLayout.WEST, contentPanel);
			paneLayout.putConstraint(SpringLayout.NORTH, infoDisplay, -30, SpringLayout.NORTH, saveButton);
			paneLayout.putConstraint(SpringLayout.EAST, infoDisplay, -10, SpringLayout.EAST, contentPanel);
			paneLayout.putConstraint(SpringLayout.SOUTH, infoDisplay, -10, SpringLayout.NORTH, saveButton);

			contentPanel.setPreferredSize(new Dimension(550, boxSizes + 70));
			scroll.setViewportView(contentPanel);
		}

		/**
		 * This will save all of the values into the Preference manager.
		 * After it will close the frame.
		 */
		protected void saveAndClose() {
			for (PrefElement element : preferenceElements) {
				List<String> values = new ArrayList<String>();
				for (int i = 0; i < element.table.getRowCount(); i++) {
					String value = (String) element.table.getValueAt(i, 0);
					if (value.length() > 0) {
						values.add((String) element.table.getValueAt(i, 0));
					}
				}
				prefs.put(element.key, values);
			}
			prefs.flush();
			frame.dispose();
			if(postMethodToRun != null){
				postMethodToRun.run();
			}
		}

		/**
		 * Sets the Spring layout for the current element.
		 * @param element - the element to set the layout for.
		 * @return the vertical pixels needed for this element.
		 */
		private int SetSpringLayout(PrefElement element) {
			
			int boxSize = 200;
			if(prefs.isSingleValueOnlyKey(element.key)){
				boxSize = 100;
			}

			paneLayout.putConstraint(SpringLayout.WEST, element, 25, SpringLayout.WEST, contentPanel);
			if (previous == contentPanel) {
				paneLayout.putConstraint(SpringLayout.NORTH, element, 15, SpringLayout.NORTH, previous);
				paneLayout.putConstraint(SpringLayout.SOUTH, element, boxSize, SpringLayout.NORTH, previous);
			} else {
				paneLayout.putConstraint(SpringLayout.NORTH, element, 15, SpringLayout.SOUTH, previous);
				paneLayout.putConstraint(SpringLayout.SOUTH, element, boxSize, SpringLayout.SOUTH, previous);
			}
			paneLayout.putConstraint(SpringLayout.EAST, element, -25, SpringLayout.EAST, contentPanel);

			previous = element;
			return boxSize;
		}
	}

	/**
	 * This class represents the information from one preference key, displaying all of the values in a table.
	 * @author Joshua
	 *
	 */
	private class PrefElement extends JPanel {
		/**
		 * 
		 */
		private static final long serialVersionUID = -8024138165488748111L;

		public String key;
		public JTable table;

		/**
		 * This class represents the information from one preference key, displaying all of the values in a table.
		 * @param key - the for this element.
		 */
		public PrefElement(String key) {
			this.key = key;
			List<String> items = prefs.getAll(key);
			if (!prefs.isSingleValueOnlyKey(key)) {
				items.add("");
				items.add("");
				items.add("");
				items.add("");
				items.add("");
			}
			Object[][] tableData = new Object[items.size()][1];
			for (int i = 0; i < items.size(); i++) {
				tableData[i][0] = items.get(i);
			}

			SpringLayout layout = new SpringLayout();
			this.setLayout(layout);

			String noteAsterisk = "";
			if(!prefs.getNote(key).isEmpty()){
				noteAsterisk = "*";
			}
			
			table = new JTable(tableData, new String[] { key + noteAsterisk});
			table.setBackground(Color.white);
			table.getTableHeader().setBackground(SPUtilities.getDarkThemeColor());
			table.getTableHeader().setForeground(SPUtilities.getDarkThemeFontColor());
			table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 16));
			
			JScrollPane scroll = new JScrollPane(table);
			scroll.setBackground(Color.white);
			scroll.getViewport().setBackground(Color.white);
			this.add(scroll);
			layout.putConstraint(SpringLayout.WEST, scroll, 0, SpringLayout.WEST, this);
			layout.putConstraint(SpringLayout.NORTH, scroll, 0, SpringLayout.NORTH, this);
			layout.putConstraint(SpringLayout.EAST, scroll, 0, SpringLayout.EAST, this);
			layout.putConstraint(SpringLayout.SOUTH, scroll, -40, SpringLayout.SOUTH, this);
			
			JLabel noteLabel = new JLabel("<html>" + noteAsterisk + prefs.getNote(key) + "</html>");
			noteLabel.setBackground(SPUtilities.getLightThemeColor());
			noteLabel.setForeground(SPUtilities.getLightThemeFontColor());
			this.add(noteLabel);
			layout.putConstraint(SpringLayout.WEST, noteLabel, 0, SpringLayout.WEST, this);
			layout.putConstraint(SpringLayout.NORTH, noteLabel, -40, SpringLayout.SOUTH, this);
			layout.putConstraint(SpringLayout.EAST, noteLabel, 0, SpringLayout.EAST, this);
			layout.putConstraint(SpringLayout.SOUTH, noteLabel, 0, SpringLayout.SOUTH, this);
			
			this.setBackground(SPUtilities.getLightThemeColor());
		}
	}

	/**
	 * Opens the main frame window for the preferences view.
	 */
	private void open() {
		SwingUtilities.invokeLater(new FrameCreation());

	}

	/**
	 * Use this listener to automatically set up a button to open this window.
	 * @author Joshua
	 *
	 */
	public static class SPPreferencesActionListener implements ActionListener {
		
		private Runnable postMethod;
		
		public SPPreferencesActionListener(Runnable postMethod) {
			this.postMethod = postMethod;
		}

		@Override
		public void actionPerformed(ActionEvent e) {

			instance = new SPPreferencesWindow();
			instance.open();
			instance.postMethodToRun = postMethod;
		}

	}
}
