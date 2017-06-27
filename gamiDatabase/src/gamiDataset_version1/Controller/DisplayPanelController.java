/**
 * File: FileMenuController.java
 * @author Victoria Chistolini
 * Date: January 9, 2017
 */

package gamiDataset_version1.Controller;

import gamiDataset_version1.Main;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;


/**
 * Handles actions displayed on the display panel
 */
public class DisplayPanelController {

    @FXML
    // main background panel
    private BorderPane displayPanel;

    @FXML
    // display small progress window
    private AnchorPane ap;

    @FXML
    //progress bar on ap
    private ProgressBar progressBar;

    @FXML
    //change progress message
    private Label textOut;

    /**
     * set-up default background for display pannel and hide the ap
     */
    public void init(){
       String backgroundImage = Main.class.getResource("Images/dna.png").toExternalForm();
        displayPanel.setStyle("-fx-background-image: url('" + backgroundImage + "')");
        this.ap.setVisible(false);


    }


    /**
     * update progress bar value
     * @param val current progress
     */
    public void updateProgress(double val){
        this.progressBar.setProgress(val);

    }


    /**
     * sets progress menu to visable
     */
    public void showProgress(){
        this.ap.setVisible(true);
    }


    /**
     * closes the progress menu
     */
    public void close(){
        this.ap.setVisible(false);
    }


    /**
     * changes message displayed on progress panel
     * @param message message to be displayed
     */
    public void changeMessage(String message){

        this.textOut.setText(message);
    }
}
