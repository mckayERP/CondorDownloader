package org.mckayerp.condor_downloader;

import javafx.fxml.FXMLLoader;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AboutDialog extends Dialog<Boolean>
{

    Logger logger = Logger.getLogger(AboutDialog.class.getName());
    public AboutDialog()
    {
        try
        {

            FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(getClass().getResource("About.fxml")));
            DialogPane aboutPane = loader.load();
            getDialogPane().getScene().getStylesheets().add(getStyleSheets());
            IconAdder.addIcons((Stage) getDialogPane().getScene().getWindow());
            setTitle("Condor Downloader About");
            initModality(Modality.APPLICATION_MODAL);
            setResizable(false);
            setDialogPane(aboutPane);
            setResultConverter(buttonType -> !Objects.equals(ButtonBar.ButtonData.OK_DONE, buttonType.getButtonData()));
            HelpAboutController controller = loader.getController();
            controller.loadHelpContent();
        } catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    private String getStyleSheets()
    {
        URL url = this.getClass().getResource("aboutDialog.css");
        if (url == null) {
            logger.log(Level.SEVERE, "Can't find the aboutDialog.css file. Aborting");
            throw new RuntimeException("Can't find the resource file aboutDialog.css");
        }
        return url.toExternalForm();
    }

}
