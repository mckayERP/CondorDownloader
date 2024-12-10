package org.mckayerp.condor_downloader;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class Main extends Application
{

    private static final Logger logger = Logger.getLogger(Main.class.getName());

    public static void main(String[] args)
    {

        Path propertiesPath = ApplicationFolderManager.getLoggerPropertiesPath();
        if (propertiesPath.toFile().exists())
        {
            try (InputStream configFile = Files.newInputStream(propertiesPath))
            {
                LogManager.getLogManager().readConfiguration(configFile);
                logger.log(Level.FINE, "Logger configured.");
            } catch (IOException ex)
            {
                logger.log(Level.WARNING, "Could not open configuration file.");
                System.out.println("WARNING: Logging not configured (console output only)");
            }
        }
        else {
            System.out.println("Failed.");
            logger.log(Level.WARNING, "Could not open configuration file.");
            System.out.println("WARNING: Logging not configured (console output only)");
        }
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

        logger.log(Level.FINE, "Launching primary stage.");
        primaryStage.setTitle("Condor Downloader");
        IconAdder.addIcons(primaryStage);
        primaryStage.setScene(scene);
        primaryStage.setOnCloseRequest(t -> controller.exit());
        primaryStage.show();

    }
}
