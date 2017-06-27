/**
 * File: ControllersInitializer.java
 * @author Victoria Chistolini
 * Date: January 10, 2017
 */

package gamiDataset_version1.Controller;

import javafx.fxml.FXML;

/**
 * Handles menu GUI interactions with other controllers
 */
public class ControllersInitializer {


    @FXML
    private FileMenuController fileMenuController;

    @FXML
    private DisplayPanelController displayPanelController;


    /** Initializes the controllers so they can communicate properly */
    @FXML
    public void initialize() {

        //Set up the Menu Controllers with the needed tools
        initializeMenuControllers();
        displayPanelController.init();

    }

    /**
     * Initializes the FileMenuController providing them with the
     * necessary connections
     */
    private void initializeMenuControllers() {
        //File Menu ControllersInitializer init
        this.fileMenuController.init(this.displayPanelController);
    }
}
