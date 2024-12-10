package org.mckayerp.condor_downloader;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class HelpDialog extends Stage
{

    public HelpDialog()
    {
        try
        {
            FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(getClass().getResource("Help.fxml")));
            StackPane helpPane = loader.load();
            Scene helpScene = new Scene(helpPane, 800,600);
            setScene(helpScene); // set the scene
            setTitle("Condor Downloader Help");

            HelpDialogController controller = loader.getController();
            controller.loadHelpContent();
            IconAdder.addIcons((Stage) helpScene.getWindow());
        } catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

//    public HelpDialogController getController()
//    {
//        return controller;
//    }

}
