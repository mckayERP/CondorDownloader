package org.mckayerp.condor_downloader;

import javafx.fxml.Initializable;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

import java.net.URL;
import java.util.ResourceBundle;

public class HelpDialogController implements Initializable
{


    public TreeView<String> helpContent;
    public WebView helpViewer;
    private HelpDialog helpDialog;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle)
    {
    }

    public void loadHelpContent()
    {
        helpDialog.getItems().add(helpContent);
        helpDialog.getItems().add(helpViewer);
        TreeItem<String> helpMenu = new TreeItem<>("Contents");
        helpContent.setRoot(helpMenu);

        WebEngine engine = helpViewer.getEngine();
        String url = HelpDialogController.class.getResource("helpPage01.html").toExternalForm();
        engine.load(url);
    }

    public void setHelpDialog(HelpDialog helpDialog)
    {
        this.helpDialog = helpDialog;
    }
}
