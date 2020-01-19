package application;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/**
 * @author Azeez G. Shola
 * @version 1.0
 */
public class Main extends Application {

    Scene primaryScene, SecondaryScene;


    /**
     *  method to start the application
     * @param primaryStage the main window/stage
     */
    @Override
    public void start(Stage primaryStage) {
        try {
            BorderPane root = (BorderPane) FXMLLoader.load(getClass().getResource("Sample.fxml"));
            Scene scene = new Scene(root, 1350, 720);

            scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
            primaryStage.getIcons().add(new Image("logo-15.png"));
            primaryStage.setTitle("Face  Recognization module");
            Button buttonNavigate = new Button("Go to next");


            buttonNavigate.setOnAction(e -> {
                primaryStage.setScene(SecondaryScene);
            });
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (Exception e) {
        }
    }

    /**
     *  the entry point of the application
     * @param args parameters passed into the system
     */
    public static void main(String[] args) {
        launch(args);


    }
}
