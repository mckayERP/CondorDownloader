package org.mckayerp.condor_downloader;

import javafx.fxml.Initializable;
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
    public WebView webView;
    Logger logger = Logger.getLogger(HelpDialogController.class.getName());

    private static final String HELP_CSS = "help.css";

    public void loadHelpContent()
    {
        logger.log(Level.FINE,"Loading help content");
        String[] helpData = {""};
        Optional.ofNullable(getClass().getResource("README.md"))
                .ifPresent(readme -> {
                    logger.log(Level.FINE,"Loading help: " + readme);

                    try {
                        helpData[0] = IOUtils.toString(readme, StandardCharsets.UTF_8);
                    } catch (IOException ex) {
                        logger.log(Level.SEVERE, "Error reading help file", ex);
                    }
                });

        if(helpData[0].isEmpty())
            logger.log(Level.WARNING, "Unable to find or load the README.md help file.");

        Parser parser = Parser.builder().build();
        Node document = parser.parse(helpData[0]);
        HtmlRenderer renderer = HtmlRenderer.builder().build();
        WebEngine webEngine = webView.getEngine();
        webEngine.setUserStyleSheetLocation(getClass().getResource(HELP_CSS).toString());
        webEngine.loadContent(renderer.render(document));

    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle)
    {

    }

}
