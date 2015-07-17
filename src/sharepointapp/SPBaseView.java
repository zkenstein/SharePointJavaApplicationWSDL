package sharepointapp;

import javax.swing.JPanel;

/**
 * This class is used to represent the base for the views used in this project.
 * It stores shared variables between the other views.
 * @author allarj3
 *
 */
public abstract class SPBaseView extends JPanel {
	
	private static final long serialVersionUID = 1L;
	
	/**
	 * The main controller for the views. Use this to configure what is going on in the local program.
	 */
	protected SharePointMainController mainController = SharePointMainController.getInstance();
	
	/**
	 * The web controller for the views. Use this to communicate to and from the SharePoint Servers.
	 */
	protected SharePointWebController webController = SharePointWebController.getInstance();
}
