package org.mckayerp.condor_downloader;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main extends Application
{

    private static final Logger logger = Logger.getLogger(Main.class.getName());

    public static void main(String[] args)
    {

        launch(args);

    }

    @Override
    public void start(Stage primaryStage) throws Exception
    {

        logger.log(Level.FINE, "Starting application.");
        FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(getClass().getResource("CondorDownloader.fxml")));
        Parent root = loader.load();
        Scene scene = new Scene(root);

        logger.log(Level.FINE, "Loading controller.");
        Controller controller = loader.getController();
        controller.setSettingsDialog(new SettingsDialog(primaryStage));
        controller.setHelpDialog(new HelpDialog());

        logger.log(Level.FINE, "Launching primary stage.");
        primaryStage.setTitle("Condor Downloader");
        primaryStage.setScene(scene);
        primaryStage.show();

    }
}
