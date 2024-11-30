package org.mckayerp.condor_downloader;

import javafx.fxml.FXMLLoader;
import javafx.scene.control.SplitPane;

import java.io.IOException;

public class HelpDialog extends SplitPane
{

    public HelpDialog()
    {
        try
        {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("Help.fxml"));
            loader.load();

            HelpDialogController controller = loader.getController();
            controller.setHelpDialog(this);
            controller.loadHelpContent();

        } catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }
}
