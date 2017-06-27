/**
 * File: Main.java
 * @author Victoria Chistolini
 * Date: January 3, 2017
 */


package gamiDataset_version1;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class Main extends Application {

    /**
     * Start method that loads the FXML file, sets the controller class
     * and initiates the stage.
     *
     * @param primaryStage
     * @throws Exception
     */
    @Override
    public void start(Stage primaryStage) throws Exception{
        FXMLLoader loader = new FXMLLoader(getClass().getResource("View/main.fxml"));

        Parent root = loader.load();

        Scene scene = new Scene(root, 600, 415);
        primaryStage.setTitle("Noncoding Sequence Curation 1.0");
        primaryStage.setScene(scene);
        primaryStage.setOnCloseRequest(e -> System.exit(0));
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

}
