/**
 * File: FileMenuController.java
 * @author Victoria Chistolini
 * Date: January 9, 2017
 */
package gamiDataset_version1.Controller;

import gamiDataset_version1.Model.SpeciesData;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import gamiDataset_version1.Model.ExecuteSearch;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.control.ButtonType;

/**
 * This class handles all of the MenuItems associated
 * with the File menu
 */
public class FileMenuController {


    // filename containing the species list
    private String taxIdfileName;
    // target gene of dataset
    private String geneName;
    // name of resulting results directory
    private String projectName;
    // data from current search
    private ExecuteSearch currentSearch;

    @FXML
    // menu button for verify search
    private MenuItem verifyButton;

    @FXML
    // menu button for change settings
    private MenuItem currentButton;

    @FXML
    // menu button for build dataset
    private MenuItem buildDataset;

    @FXML
    // menu button for create project
    private MenuItem createProject;

    // connection to the DisplayPannelController
    private DisplayPanelController panelController;

    /**
     * Initializes the controller by disabling correct menu opttions
     */
    public void init(DisplayPanelController panelController) {
        this.menuItemsdisabled(true);
        this.currentSearch = null;
        this.panelController = panelController;
    }


    /**
     * Dialog window that gives information about the application
     */
    @FXML
    public void about() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("About");
        alert.setHeaderText("About");
        alert.setContentText("This is a dataset generation app " +
                "software \n Authors: Victoria Chistolini");
        alert.show();
    }


    @FXML
    /**
     * Sets the settings for a new dataset build project
     */
    //TODO : should also save as!!!
    public void newProject() {

        Dialog<HashMap<String,String>> newProjSetting = this.generateSimpleDialog("proj1", "ccdc39", "Master List", "Apply");
        Optional<HashMap<String, String>> result = newProjSetting.showAndWait();
        this.editProjectSettings(result);
        this.menuItemsdisabled(false);


    }

    @FXML
    /**
     * Allows the user to change the current settings
     */
    public void currentprojectSettings(){
        String listName;
        if(this.taxIdfileName.equals("MasterList.txt")){
            listName = "Master List";
        } else if (this.taxIdfileName.equals("MammalsOnly.txt")){
            listName = "Mammals Only";
        } else {
            listName = "Custom List";
        }
        Dialog<HashMap<String,String>> changeSettings =
                this.generateSimpleDialog(this.projectName,this.geneName,listName,"Change");
        Optional<HashMap<String, String>> result = changeSettings.showAndWait();
        this.changeSettings(result);

    }


    @FXML
    /**
     * reports the search results for the current species list
     */
    public void verifySearchMetrics() {
        this.checkCurrentSearch();
        this.verifySearchDialog();

    }

    @FXML
    /**
     * builds the dataset given the current parameter set
     * Disables the change settings
     */
    public void buildDataset(){

        new Thread(() -> {

            Platform.runLater(()->
                    this.panelController.showProgress());

            Platform.runLater(()->
                    this.panelController.changeMessage("Collecting Basic Gene Data"));

            this.checkCurrentSearch();

            Platform.runLater(()->
                this.panelController.updateProgress(0.16));

            Platform.runLater(()->
                    this.panelController.changeMessage("Collecting Exon Data"));

            this.currentSearch.getExonData();
            Platform.runLater(()->
                this.panelController.updateProgress(0.33));
            Platform.runLater(()->
                    this.panelController.changeMessage("Calculating Intron Regions"));

            this.currentSearch.getIntrons();

            Platform.runLater(()->
                    this.panelController.updateProgress(0.5));

            Platform.runLater(()->
                    this.panelController.changeMessage("Finding Upstream/Downstream Genes"));

            this.currentSearch.findUpstreamDownstreamGenes();

            Platform.runLater(()->
                    this.panelController.updateProgress(0.66));
            Platform.runLater(()->
                    this.panelController.changeMessage("Writing Gene Summary Files"));

            this.currentSearch.writeNotesFile();

            Platform.runLater(()->
                    this.panelController.updateProgress(0.83));
            Platform.runLater(()->
                    this.panelController.changeMessage("Downloading Sequence"));

            this.currentSearch.fetchSequence();

            Platform.runLater(()->
                    this.panelController.updateProgress(1));
            Platform.runLater(()->
                    this.panelController.changeMessage("Dataset Complete!"));


        }).start();

    }


    /**
     * TODO: check if there is currently a process running-> send warning
     * Ensures that all processes are killed on the
     * destruction of the window.
     */
    @FXML
    public void cleanUpOnExit() {
        Platform.exit();
        System.exit(0);
    }


    /**
     * checks if currentSearch object settings matches user input settings
     */
    private void checkCurrentSearch(){

        if (this.currentSearch!=null){
            if(!this.currentSearch.getGeneName().equals(this.geneName) ||
                    !this.currentSearch.getSpeciesListFilename().equals(this.taxIdfileName)){

                this.currentSearch = new ExecuteSearch(this.taxIdfileName,this.geneName);
            }
        } else{
            this.currentSearch = new ExecuteSearch(this.taxIdfileName,this.geneName);
        }


    }

    /**
     * sets the modifier menu items visibility
     * @param val true if disabled
     */
    private void menuItemsdisabled(boolean val){
        this.verifyButton.setDisable(val);
        this.currentButton.setDisable(val);
        this.buildDataset.setDisable(val);
    }


    /**
     * sets results when there is a new project created. Enforces that all fields must be filled out
     * @param result dialog results
     *
     * @return correct setting return 1
     */
    public int editProjectSettings(Optional<HashMap<String, String>> result){
        if(result.isPresent()) { //If the result isn't present, then cancel was pressed
            HashMap<String, String> results = result.get();

            if(results.get("name").isEmpty() || results.get("geneName").isEmpty()){
                this.errorAlert("Missing Information", "Please make sure to enter a project name\n and gene name.");
                return 0;
            }

            this.projectName =results.get("name");
            this.geneName = results.get("geneName");


            if (results.get("speciesList").equals("Master List")){
                this.taxIdfileName ="MasterList.txt";
            }
            else if (results.get("speciesList").equals("Mammals Only")){
                this.taxIdfileName = "MammalsOnly.txt";
            }
            else {
                this.taxIdfileName = "custum_"+ this.projectName + ".txt";
                this.handleCustomList();
            }

        }
        this.createProject.setDisable(true);
        return 1;
    }


    /**
     * sets results a user want to change settings.
     * Does not enforce all fields must be filled.
     * @param result dialog results
     *
     */
    public void changeSettings(Optional<HashMap<String, String>> result){
        if(result.isPresent()) { //If the result isn't present, then cancel was pressed
            HashMap<String, String> results = result.get();

            if(!results.get("name").isEmpty() ){
                this.projectName=results.get("name");
            }

            if(!results.get("geneName").isEmpty()){
                this.geneName = results.get("geneName");
            }

            if (results.get("speciesList").equals("Master List")){
                this.taxIdfileName ="MasterList.txt";
            }
            else if (results.get("speciesList").equals("Mammals Only")){
                this.taxIdfileName = "MammalsOnly.txt";
            }
            else {
                if (!this.taxIdfileName.contains("custum")){
                        this.handleCustomList();
                }
                this.taxIdfileName = "custum_"+ this.projectName + ".txt";

            }

        }
    }



    //TODO : we should be able to go back and edit preferences...AKA DOES NOT WORK AT ALL what about multiple custom Lists

    /**
     * adds custom species list input by user
     * puts in SystemFiles dir
     *
     */
    public void handleCustomList(){
        Dialog<HashMap<String,String>> customList = this.customListDialog();
        Optional<HashMap<String, String>> result = customList.showAndWait();

        if(result.isPresent()){
            System.out.println("here");
        }
    }


    /**
     * dialog for editing project settings
     *
     * @param initProj project name
     * @param initGene target gene
     * @param initList name of species list file
     * @param appB text for accept button
     * @return the dialog box
     */
    private Dialog<HashMap<String, String>> generateSimpleDialog(String initProj,
                                                                 String initGene, String initList, String appB)
    {
        // Create the custom dialog.
        Dialog<HashMap<String, String>> dialog = new Dialog<>();
        dialog.setTitle("Project Settings");

        // Set the button types and add them to the pane
        ButtonType applyButton = new ButtonType(appB);
        dialog.getDialogPane().getButtonTypes().addAll(applyButton, ButtonType.CLOSE);

        // Create the grid to place the questions on
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 20, 10, 10));

        //Set up the preferences questions
        TextField projectName = new TextField();
        //projectName.setText(cu);
        projectName.setPromptText(initProj);
        TextField geneName = new TextField();
        geneName.setPromptText(initGene);
        ChoiceBox speciesList = new ChoiceBox<Integer>();
        speciesList.getItems().addAll("Master List", "Mammals Only","Custom List");
        speciesList.setValue(initList);

        //Add items to the grid
        grid.add(new Label("Select Project Name:"), 0, 0);
        grid.add(projectName, 1, 0);
        grid.add(new Label("Gene Name:"), 0, 1);
        grid.add(geneName, 1, 1);
        grid.add(new Label("Species List:"), 0, 2);
        grid.add(speciesList, 1, 2);


        dialog.getDialogPane().setContent(grid);

        // Convert the result to a hashmap of results
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == applyButton) {
                HashMap<String, String> preferences = new HashMap<>();
                preferences.put("name", projectName.getText());
                preferences.put("geneName", geneName.getText());
                preferences.put("speciesList", (String) speciesList.getValue());
                return preferences;
            }
            return null;
        });

        return dialog;
    }

    /**
     * dialog for the custom species list
     * @return dialog box
     */
    private Dialog<HashMap<String, String>> customListDialog() {
        // Create the custom dialog.
        Dialog<HashMap<String, String>> dialog = new Dialog<>();
        dialog.setTitle("Select Custom Species List");

        // Set the button types and add them to the pane
        ButtonType applyButton = new ButtonType("Apply");
        ButtonType backButton = new ButtonType("Back", ButtonBar.ButtonData.BACK_PREVIOUS);


        dialog.getDialogPane().getButtonTypes().addAll(applyButton, backButton);

        // Create the grid to place the questions on
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 20, 10, 10));

        //Set up the preferences questions
        TextField speciesName = new TextField();
        //projectName.setText(cu);
        speciesName.setPromptText("9606");

        //Add items to the grid
        grid.add(new Label("Input Desired TaxIds:"), 0, 0);
        grid.add(speciesName, 1, 0);



        dialog.getDialogPane().setContent(grid);

        // Convert the result to a hashmap of results
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == applyButton) {
                HashMap<String, String> preferences = new HashMap<>();
                preferences.put("taxIDS", speciesName.getText());
                return preferences;
            } else if (dialogButton == backButton){
                this.currentprojectSettings();

            }
            return null;

        });

        return dialog;
    }


    /**
     * Displays results from a search of the given gene on the given speceis list
     */
    public void verifySearchDialog(){

        Dialog d = new Dialog();
        d.setTitle("Search Results for Gene" );
        DialogPane dialogPane = new DialogPane();
        VBox outer = new VBox();
        List<SpeciesData> searchInfo = this.currentSearch.getSpeciesDataList();

        ListView<Text> inner = new ListView<Text>();
        inner.getItems().add(new Text("Species Name         Taxonomy ID             GeneID"));

        for( SpeciesData speciesData : searchInfo){
            String stuff = String.format("%s                    %s                    %s",
                    speciesData.getCommonName(),speciesData.getTaxID(), speciesData.getGeneId());
            System.out.println(stuff);
            inner.getItems().add(new Text(stuff));
        }

        inner.setMaxWidth(400);
        inner.setMaxHeight(80);
        outer.getChildren().addAll(inner);
        dialogPane.setContent(outer);
        d.setDialogPane(dialogPane);
        d.getDialogPane().getButtonTypes().addAll(ButtonType.CLOSE);
        d.showAndWait();
    }

    /**
     * Pop up an error box
     *
     * @param type the type of error that occurred
     * @param e    the message to be displayed in the box
     */
    public void errorAlert(String type, String e) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(type);
        alert.setHeaderText(type);
        alert.setContentText(e);
        alert.show();
    }

}