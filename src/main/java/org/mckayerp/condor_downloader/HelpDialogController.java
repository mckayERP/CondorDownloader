package org.mckayerp.condor_downloader;

import javafx.fxml.Initializable;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.StackPane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.commonmark.node.*;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;


public class HelpDialogController implements Initializable
{

    public StackPane stackPane;
    public ScrollPane scrollPane;
    public WebView webView;
    Logger logger = Logger.getLogger(HelpDialogController.class.getName());

    private static final String HELP_CSS = "help.css";

    public void loadHelpContent()
    {

        String[] mdfxString = {""};
        Optional.ofNullable(getClass().getResource("README.MD"))
                .ifPresent(readme -> {
                    logger.fine("Loading help: " + readme);

                    try {
                        mdfxString[0] = IOUtils.toString(readme, StandardCharsets.UTF_8);
                    } catch (IOException ex) {
                        logger.log(Level.SEVERE, "Error reading help file", ex);
                    }
                });

        Parser parser = Parser.builder().build();
        Node document = parser.parse(mdfxString[0]);
        HtmlRenderer renderer = HtmlRenderer.builder().build();
        WebEngine webEngine = webView.getEngine();
        webEngine.setUserStyleSheetLocation(getClass().getResource(HELP_CSS).toString());
        webEngine.loadContent(renderer.render(document));
        scrollPane.setStyle("-fx-background-color:transparent");
        scrollPane.setContent(webView);

    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle)
    {

    }
}
