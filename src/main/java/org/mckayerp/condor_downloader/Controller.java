package org.mckayerp.condor_downloader;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.net.URL;
import java.nio.file.Files;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.util.Objects.requireNonNull;
import static org.mckayerp.condor_downloader.CondorVersion.CONDOR_2;
import static org.mckayerp.condor_downloader.CondorVersion.CONDOR_3;
import static org.mckayerp.condor_downloader.DownloadManager.getCondorFolderPath;

public class Controller implements Initializable, StatusProvider
{

    private static final Logger logger = Logger.getLogger(Controller.class.getName());

    public TextField numberToDownloadField;
    public TextField taskCodeField;
    public CheckBox copyGhostsToFlightTrackFolderCheckBox;
    public CheckBox copyGhostsFromCompetition;
    public CheckBox downloadFlightPlanCheckbox;
    public Button downloadButton;
    public SettingsDialog settingsDialog;
    public MenuBar menuBar;
    public GridPane mainGridPane;
    public HelpDialog helpDialog;
    public TextArea statusText;
    private SettingsDialogController settingsController;
    private String taskCode;
    private int numberToDownload;
    private String firefoxPath;
    private boolean condor2DirectoryExists = false;
    private boolean condor3DirectoryExists = false;
    private boolean firefoxPathIsBad = false;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle)
    {

        logger.log(Level.FINE, "Initializing the Controller.");

        NumberFieldFormatter numberFieldFormatter = new NumberFieldFormatter();
        numberToDownload = 5;
        numberToDownloadField.setTextFormatter(numberFieldFormatter);
        numberToDownloadField.setText(String.valueOf(numberToDownload));
        numberToDownloadField.textProperty().addListener((observable, oldValue, newValue) ->
        {
            try
            {
                numberToDownload = Integer.parseInt(numberToDownloadField.getText());
            } catch (NumberFormatException e)
            {
                numberToDownloadField.setText("0");
                numberToDownload = 0;
            }
            enableDownloadButton();
        });

        TaskCodeFormatter taskCodeFormatter = new TaskCodeFormatter();
        taskCodeField.setTextFormatter(taskCodeFormatter);
        taskCodeField.setText("");
        taskCodeField.focusedProperty().addListener((observable, oldValue, hasFocus) ->
        {
            if (!hasFocus)
            {
                taskCode = taskCodeField.getText();
                enableDownloadButton();
            }
        });

        downloadFlightPlanCheckbox.setOnAction(actionEvent -> enableDownloadButton());

        downloadButton.setOnAction(actionEvent ->
        {
            logger.log(Level.FINE, "Download button handle event detected. " + actionEvent);
            settingsController.saveSettings();
            clearStatus();
            DownloadManager downloader = new DownloadManager(getData());
            downloader.startInBackground();
        });

        updateStatus("""
                Welcome to the Condor downloader.
                Please edit and save the settings as required before using this tool.
                To download the ghosts of a flight, enter the task code (e.g. "ABCDEF") and the number of ghost tracks\s
                to download.  You can also select to download the flight plan as well. The download button will be\s
                enabled once the settings and task code have been entered.""", true, false);
    }

    public void setSettingsDialog(SettingsDialog dialog)
    {
        settingsDialog = requireNonNull(dialog);
        settingsController = settingsDialog.getController();
        updateAndCheckSettings();
    }

    public void setHelpDialog(HelpDialog helpDialog)
    {
        this.helpDialog = helpDialog;
    }

    private void enableDownloadButton()
    {

        boolean enable = (numberToDownload > 0 || downloadFlightPlanCheckbox.isSelected()) && !taskCodeField.getText().isEmpty() && !firefoxPathIsBad && (condor2DirectoryExists || condor3DirectoryExists);
        downloadButton.setDisable(!enable);

    }

    public void handleMenuItemFileEditSettings(ActionEvent ignoredActionEvent)
    {

        logger.log(Level.FINE, "File menu Edit Settings called. Displaying the settings dialog.");
        settingsDialog.showAndWait();
        settingsDialog.close();
        updateAndCheckSettings();

    }

    public void handleMenuItemFileExit(ActionEvent ignoredActionEvent)
    {
        logger.log(Level.FINE, "File menu exit called. Exiting the platform.");
        GeckoDriverManager.kill();
        Platform.exit();
    }

    public void handleMenuItemRemoveGhostFilesFromFlightTrackFolder(ActionEvent ignoredActionEvent)
    {
        logger.log(Level.FINE, "File menu Remove ghost files called. Cleaning up the flight track folder.");
        GhostFileManager.deleteGhostFiles();
    }

    public void handleMenuItemShowHelpContent(ActionEvent ignoredActionEvent)
    {
        logger.log(Level.FINE, "File menu Help called. Displaying the Help dialog.");
        Stage stage = new Stage();
        stage.setTitle("My New Stage Title");
        stage.setScene(new Scene(helpDialog, 450, 450));
        stage.showAndWait();
        stage.close();
    }

    public void handleMenuItemHelpAbout(ActionEvent ignoredActionEvent)
    {
        logger.log(Level.FINE, "File menu Help About called. Displaying the Help About dialog.");
        AboutDialog about = new AboutDialog();
        about.showAndWait();
        about.close();
    }

    DownloadData getData()
    {
        // @formatter:off
        return new CondorDownloadData()
                .withNumberToDownload(numberToDownload)
                .withTaskCode(taskCode)
                .withFirefoxExecutablePath(firefoxPath)
                .withCondor2DirectoryExists(condor2DirectoryExists)
                .withCondor3DirectoryExists(condor3DirectoryExists)
                .withCondorClubUserEmail(settingsController.getCondorClubUserEmail())
                .withCondorClubUserPassword(settingsController.getCondorClubUserPassword())
                .withDownloadFligthPlanSelected(downloadFlightPlanCheckbox.isSelected())
                .withCopyGhostsToFlightTrackFolderSelected(copyGhostsToFlightTrackFolderCheckBox.isSelected())
                .withCopyGhostsFromCompetitionSelected(copyGhostsFromCompetition.isSelected())
                .withStatusProvider(this);
        // @formatter:on
    }

    private void updateAndCheckSettings()
    {
        firefoxPath = settingsController.getFirefoxExecutablePath();
        firefoxPathIsBad = FireFoxFinder.get().fireFoxPathIsBad(firefoxPath);
        condor2DirectoryExists = Files.exists(getCondorFolderPath(CONDOR_2));
        condor3DirectoryExists = Files.exists(getCondorFolderPath(CONDOR_3));

        // @formatter:off
        if (firefoxPathIsBad)
        {
            clearStatus();
            updateStatus(Level.SEVERE, "Error: The path to the Firefox executable is not defined or does not point at " +
                    "an executable file.  Please ensure Firefox is installed and the path to the executable is " +
                    "defined in the settings.");
        }
        if (!condor2DirectoryExists)
        {
            updateStatus(Level.WARNING, "Condor 2 does not appear to be installed. You will " +
                    "not be able to download Condor3 tasks or flight tracks.", true, true);
        }
        if (!condor3DirectoryExists)
        {
            updateStatus(Level.WARNING, "Condor3 does not appear to be installed. You will " +
                    "not be able to download Condor3 tasks or flight tracks.", true, true);
        }
        // @formatter:on
    }

    @Override
    public void updateStatus(Level logLevel, String update, boolean clearStatus, boolean scroll_to_bottom)
    {
        logger.log(logLevel, "Status update: " + update);
        Platform.runLater(() ->
        {

            if (clearStatus)
                statusText.setText(update);
            else
            {
                statusText.appendText("\n" + update);
            }
            if (scroll_to_bottom)
                statusText.appendText("");

        });

    }
}
