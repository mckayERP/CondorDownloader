package org.mckayerp.condor_downloader;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.firefox.FirefoxProfile;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static java.util.Objects.requireNonNull;
import static org.mckayerp.condor_downloader.GhostFileManager.extractGhostFilesFromZipFiles;
import static org.mckayerp.condor_downloader.GhostIDFinder.findGhostIDs;
import static org.mckayerp.condor_downloader.ZipFileManager.deleteZipFiles;

public class Controller implements Initializable
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
    private SettingsDialogController settingsController;
    public HelpDialog helpDialog;

    private String taskCode = "NotDefined";
    private Path ghostFolderPath;
    private int numberToDownload;
    Path condorFolderPath;  // Package private for testing
    private Path newGhostFolderPath;
    private String firefoxPath;
    public TextArea statusText;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle)
    {

        logger.log(Level.FINE, "Initializing the Controller.");
        statusText.maxHeightProperty().bind(Bindings.createDoubleBinding(() -> statusText.getFont().getSize() * 5, statusText.fontProperty()));
        statusText.minHeightProperty().bind(Bindings.createDoubleBinding(() -> statusText.getFont().getSize() * 5, statusText.fontProperty()));

        NumberFieldFormatter numberFieldFormatter = new NumberFieldFormatter();
        numberToDownloadField.setTextFormatter(numberFieldFormatter);
        numberToDownloadField.setText("5");
        numberToDownloadField.textProperty().addListener((observable, oldValue, newValue) ->
        {
            enableDownloadButton();
            numberToDownload = Integer.parseInt(numberToDownloadField.getText());
        });

        TaskCodeFormatter taskCodeFormatter = new TaskCodeFormatter();
        taskCodeField.setTextFormatter(taskCodeFormatter);
        taskCodeField.setText("");
        taskCodeField.textProperty().addListener((observable, oldValue, newValue) ->
        {
            taskCode = taskCodeField.getText();
            enableDownloadButton();
        });

        downloadButton.setOnAction(actionEvent ->
        {
            // Create a Task to perform the download operation in the background
            logger.log(Level.FINE, "Download button handle event detected. " + actionEvent);
            Task<Void> downloadTask = new Task<>()
            {
                @Override
                protected Void call()
                {
                    download();
                    return null;
                }
            };
            // Start the download task in a background thread
            Thread downloadThread = new Thread(downloadTask);
            downloadThread.setDaemon(true); // Daemon thread to ensure it stops if the application exits
            downloadThread.start();
        });

        updateStatus("""
                Welcome to the Condor downloader.
                Please edit and save the settings to as required before using this tool.
                To download the ghosts of a flight, enter the task code (e.g. "ABCDEF") and the number of ghost tracks\s
                to download.  You can also select to download the flight plan as well. The download button will be\s
                enabled once the settings and task code have been entered.""", false, false);
    }

    public void setSettingsDialog(SettingsDialog dialog)
    {
        settingsDialog = requireNonNull(dialog);
        settingsController = settingsDialog.getController();
    }


    private void download()
    {

        logger.log(Level.FINE, "->download()");
        setup();
        saveSettings();
        clearStatus();
        updateStatus("Starting download...");
        WebDriver driver = getWebDriverAndLogInToCondorClubOnTaskPage();
        if (driver == null)
            return;
        if (downloadFlightPlanCheckbox.isSelected())
            downloadFlightPlan(driver);
        downloadGhosts(getBestTimesForTask(driver), driver);
        driver.close();
        updateStatus("Download complete!");
        clearTaskCodeField();

    }

    private void setup()
    {
        setupGeckoDriver();
        numberToDownload = Integer.parseInt(numberToDownloadField.getText());
        condorFolderPath = settingsController.getCondorFolderPath();
        ghostFolderPath = settingsController.getGhostFolderPath();
        newGhostFolderPath = createOrTestGhostFolder();
        taskCode = taskCodeField.getText();
        firefoxPath = settingsController.getFirefoxExecutablePath();
    }

    private void saveSettings()
    {
        settingsController.saveSettings();
    }

    private void updateStatus(String update)
    {
        boolean ADD_LINEBREAK_BEFORE = true;
        boolean SCROLL_TO_BOTTOM = true;
        updateStatus(update, ADD_LINEBREAK_BEFORE, SCROLL_TO_BOTTOM);
    }

    private void updateStatus(Level level, String update)
    {
        boolean ADD_LINEBREAK_BEFORE = true;
        boolean SCROLL_TO_BOTTOM = true;
        updateStatus(level, update, ADD_LINEBREAK_BEFORE, SCROLL_TO_BOTTOM);
    }

    private void updateStatus(String update, boolean addLineBreakBefore, boolean scroll_to_bottom)
    {
        updateStatus(Level.INFO, update, addLineBreakBefore, scroll_to_bottom);
    }
    private void updateStatus(Level logLevel, String update, boolean addLineBreakBefore, boolean scroll_to_bottom)
    {
        logger.log(logLevel, "Status update: " + update);
        Platform.runLater(() ->
        {

            if (addLineBreakBefore)
                statusText.appendText("\n" + update);
            else
            {
                statusText.setText(update);
            }
            if (scroll_to_bottom)
                statusText.appendText("");

        });

    }

    private void clearStatus()
    {
        updateStatus("", false, false);
    }

    private void clearTaskCodeField()
    {
        Platform.runLater(() -> taskCodeField.setText(""));

    }

    private void downloadGhosts(String bestTimesSource, WebDriver driver)
    {
        updateStatus("Looking for " + numberToDownload + " ghost flight track" + (numberToDownload > 1 ? "s" : "") + " to download...");
        int[] count = {0};
        List<String> matches = findGhostIDs(bestTimesSource, numberToDownload);
        updateStatus("Found " + matches.size() + " matching flight track" + (matches.size() > 1 ? "s..." : "..."));
        matches.forEach(ghostID ->
        {
            if (downloadGhostZipFile(ghostID, driver))
                count[0]++;
        });
        if (count[0] == 0)
            updateStatus("All ghost flight tracks already exist and won't be downloaded again "
                    + "OR the download of ghost tracks for this task is not yet enabled "
                    + "OR there are no ghost tracks to download.");
        else if (count[0] < numberToDownload)
            updateStatus("Only downloaded " + count[0] + " flight track" + (count[0] > 1 ? "s" : "")
                    + " as that was all there was "
                    + "OR the other ghost flight tracks were previously downloaded.");
        else
            updateStatus("Was able to downloaded " + count[0] + " ghost flight track" + (count[0] > 1 ? "s..." : "..."));

        extractGhostFilesFromZipFiles(newGhostFolderPath, taskCode);
        deleteZipFiles(newGhostFolderPath);
        if (copyGhostsToFlightTrackFolderCheckBox.isSelected())
            copyGhostFilesToFlightTrackFolder(newGhostFolderPath);
    }

    private String getBestTimesForTask(WebDriver driver)
    {
        String taskPageSource = driver.getPageSource();
        String taskID = getCondorClubTaskIDFromPageSource(taskPageSource);
        String bestTimesPageURL = findBestTimesPageURL(taskPageSource, taskID);
        driver.get(bestTimesPageURL);
        String bestTimesSource = driver.getPageSource();
        updateStatus("Found the best times list...");
        return bestTimesSource;
    }

    private void setupGeckoDriver()
    {
        String geckoPath =  System.getProperty("user.dir") + "\\drivers\\geckodriver\\geckodriver.exe";
        logger.log(Level.FINE, "Looking for gecko driver in path " + geckoPath);
        File file = new File(geckoPath);
        if (!file.exists())
        {
            logger.log(Level.FINE, "Gecko driver not found in installed location. Defaulting to dev environment.");
            geckoPath = System.getProperty("user.dir") + "\\target\\CondorDownloader\\bin\\drivers\\geckodriver\\geckodriver.exe";
            file = new File(geckoPath);
            if (!file.exists())
            {
                logger.log(Level.SEVERE, "Gecko driver not found!");
                throw new RuntimeException("Can't located the Gecko driver executable. Expected to find it in the installed directory in" + " drivers/geckodriver.");
            }
        }
        System.setProperty("webdriver.gecko.driver", geckoPath);
    }

    private String getCondorClubTaskIDFromPageSource(String content)
    {
        if (content == null)
            throw new IllegalArgumentException("The page content can't be null.");
        String startString = "<meta property=\"og:url\" content=\"https://www.condor.club/stub/task/0/?id=";
        int metaTagStart = content.indexOf(startString) + startString.length();
        String idTag = content.substring(metaTagStart);
        return idTag.substring(0, idTag.indexOf("&amp;l=1"));
    }

    private WebDriver getWebDriverAndLogInToCondorClubOnTaskPage()
    {

        if (FireFoxFinder.fireFoxPathIsBad(firefoxPath))
        {
            clearStatus();
            updateStatus(Level.SEVERE, "Error: The path to the Firefox executable is not defined or does not point at " +
                    "an executable file.  Please ensure Firefox is installed and the path to the executable is " +
                    "defined in the settings.");
            return null;
        }


        updateStatus("Searching for the task on condor.club with code " + taskCode + "...");
        String urlWithLogin = "https://condor.club/showtask/0/?netid=" + taskCode;
        FirefoxProfile fxProfile = new FirefoxProfile();
        fxProfile.setPreference("browser.download.folderList", 2);
        fxProfile.setPreference("browser.download.manager.showWhenStarting", false);
        fxProfile.setPreference("browser.download.dir", newGhostFolderPath.toString());
        fxProfile.setPreference("browser.helperApps.neverAsk.saveToDisk", "zip");
        FirefoxOptions options = new FirefoxOptions();
        options.setProfile(fxProfile);
        options.setBinary(firefoxPath);
        options.addArguments("-headless");
        options.setPageLoadTimeout(Duration.ofSeconds(10));
        WebDriver driver = new FirefoxDriver(options);
        driver.get(urlWithLogin);
        if (driver.getPageSource().contains("Task not available yet."))
        {
            clearStatus();
            updateStatus("The task was not found or is not available yet.  Please try again with a different task code.", false, true);
            driver.close();
            return null;
        }
        updateStatus("Found the task page.", false, true);
        updateStatus("Logging in...");
        WebElement userEmailElement = driver.findElement(By.name("login"));
        WebElement passwordElement = driver.findElement(By.name("pwd"));
        WebElement login = driver.findElement(By.xpath("//a[contains(@href,\"#\")]"));
        userEmailElement.sendKeys(settingsController.getCondorClubUserEmail());
        passwordElement.sendKeys(settingsController.getCondorClubUserPassword());
        login.click();
        updateStatus("Login successful.", false, true);
        return driver;

    }

    private Path createOrTestGhostFolder()
    {
        Path newGhostDirectoryPath = Paths.get(ghostFolderPath.toString(), taskCode);
        try
        {
            Files.createDirectories(newGhostDirectoryPath);
        } catch (IOException e)
        {
            updateStatus(Level.WARNING, "Could not create or find the ghost folder path " + ghostFolderPath.toString()
                    + "\n" + e);
            throw new RuntimeException(e);
        }
        return newGhostDirectoryPath;
    }

    private String findBestTimesPageURL(String content, String id)
    {
        boolean wasUsedInCompetition = content.contains("This task has been used in a competition.");
        String bestTimesPageURL = "https://www.condor.club/besttimes/0/?id=" + id;
        if (wasUsedInCompetition && copyGhostsFromCompetition.isSelected())
        {
            bestTimesPageURL = "https://www.condor.club/comp/besttimes/0/?id=" + id;
            updateStatus("Using competition results.");
        }
        return bestTimesPageURL;
    }

    private void copyGhostFilesToFlightTrackFolder(Path sourceDir)
    {

        Path destDir = Paths.get(condorFolderPath.toString(), "FlightTracks");
        try (Stream<Path> paths = Files.walk(sourceDir))
        {
            paths.filter(Files::isRegularFile).forEach(sourceFile ->
            {
                try
                {
                    Path destFile = destDir.resolve(sourceDir.relativize(sourceFile));
                    Files.createDirectories(destFile.getParent());
                    Files.copy(sourceFile, destFile, REPLACE_EXISTING);
                } catch (IOException e)
                {
                    updateStatus(Level.WARNING, "Failed to copy file " + sourceFile.toString());
                }
            });

            updateStatus("All existing and/or new ghost flight tracks copied successfully to the flight track folder: " + destDir);
        } catch (IOException e)
        {
            updateStatus(Level.WARNING, "Failed to find or walk the ghost folder " + sourceDir.toString()
                            + "\n" + e);
        }

    }

    private boolean downloadGhostZipFile(String ghostID, WebDriver driver)
    {
        if (ghostFileAlreadyExists(ghostID))
        {
            updateStatus("Ghost file with ID " + ghostID + " already exists and won't be downloaded.");
            return false;
        }
        try
        {
            driver.navigate().to("https://www.condor.club/download2/0/?res=" + ghostID + "&next=1");
        } catch (TimeoutException ignored)
        {
            // Timeout is expected
        }
        boolean success = waitForDownload(ghostFolderPath, ghostID);
        if (success)
            updateStatus("Flight track for flight ID " + ghostID + " downloaded.");
        else
            updateStatus(Level.WARNING, "Not able to download flight track for flight ID " + ghostID + ". A timeout occurred.");
        return success;
    }

    private boolean ghostFileAlreadyExists(String ghostID)
    {
        boolean found;
        try (Stream<Path> files = Files.walk(ghostFolderPath))
        {
            found = files.anyMatch(path -> path.toFile().getName().contains(ghostID));
        } catch (IOException e)
        {
            throw new RuntimeException(e);
        }
        return found;
    }

    private boolean waitForDownload(Path pathToCheck, String portionOfFileNameToLookFor)
    {
        boolean found = false;
        int timeoutInSeconds = 60; // Timeout after 60 seconds
        int elapsedTime = 0;

        try
        {
            while (!found && elapsedTime < timeoutInSeconds)
            {
                try (Stream<Path> files = Files.walk(pathToCheck))
                {
                    found = files.anyMatch(path -> path.toFile().getName().contains(portionOfFileNameToLookFor));
                }
                if (!found)
                {
                    Thread.sleep(1000); // Wait for 1 second before checking again
                    elapsedTime++;
                }
            }
        } catch (InterruptedException | IOException e)
        {
            updateStatus(Level.WARNING, "Download directory not found or download was interrupted.");
        }
        return found;
    }

    private void enableDownloadButton()
    {

        boolean ghostDirectoryExists = Files.exists(settingsController.getGhostFolderPath());
        boolean condorDirectoryExists = Files.exists(settingsController.getCondorFolderPath());
        if (!ghostDirectoryExists)
            updateStatus(Level.WARNING,"The ghost directory does not exist. Please edit settings and select an existing directory.  "
                    + "The ghost files will be added to a new folder within the selected one.", true, true);
        if (!condorDirectoryExists)
            updateStatus(Level.WARNING,"The Condor directory does not exist. Please edit settings and select the Condor directory, "
                    + "typically Documents\\Condor. This is required to save ghosts in the flight track folder and to "
                    + "download the flight plan.", true, true);
        boolean enable = Integer.parseInt(numberToDownloadField.getText()) > 0 && !taskCodeField.getText().isEmpty() && ghostDirectoryExists && condorDirectoryExists;

        downloadButton.setDisable(!enable);

    }

    public void handleMenuItemFileEditSettings(ActionEvent ignoredActionEvent)
    {

        logger.log(Level.FINE, "File menu Edit Settings called. Displaying the settings dialog.");
        settingsDialog.showAndWait();
        settingsDialog.close();

    }

    public void handleMenuItemFileExit(ActionEvent ignoredActionEvent)
    {
        logger.log(Level.FINE, "File menu exit called. Exiting the platform.");
        Platform.exit();
    }

    public void handleMenuItemRemoveGhostFilesFromFlightTrackFolder(ActionEvent ignoredActionEvent)
    {
        logger.log(Level.FINE, "File menu Remove ghost files called. Cleaning up the flight track folder.");
        condorFolderPath = settingsController.getCondorFolderPath();
        GhostFileManager.deleteGhostFiles(condorFolderPath);
    }

    public void downloadFlightPlan(WebDriver driver)
    {

        updateStatus("Downloading the flight plan...");
        String currentURL = driver.getCurrentUrl();
        if (currentURL == null)
            throw new RuntimeException("Condor.club task page URL is null");

        requireNonNull(condorFolderPath);
        if (!condorFolderPath.toFile().exists())
            throw new RuntimeException("Condor folder does not exist: " + condorFolderPath);

        String taskPageSource = driver.getPageSource();
        String taskID = getCondorClubTaskIDFromPageSource(taskPageSource);
        boolean downloadPossible = taskPageSource.contains("Download it now!");
        if (!downloadPossible)
        {
            updateStatus("The flight plan can't be downloaded. It may be active in a competition.");
            driver.navigate().to(currentURL);
            return;
        }

        try
        {
            driver.navigate().to("https://www.condor.club/download2/0/?id=" + taskID + "&next=1");
        } catch (TimeoutException ignored)
        {
            // Timeout is expected
        }

        if (waitForDownload(newGhostFolderPath, ".fpl"))
            updateStatus("Downloaded flight plan for task " + taskID + "...");
        else
            updateStatus("Unable to downloaded flight plan for task " + taskID + ". A timeout occurred.");

        Path sourceDir = newGhostFolderPath;
        Path destDir = Paths.get(condorFolderPath.toString(), "FlightPlans");
        Path[] destFile = {null};
        try (Stream<Path> paths = Files.walk(newGhostFolderPath))
        {
            paths.filter(Files::isRegularFile).filter(path -> path.toFile().getName().endsWith((".fpl"))).forEach(sourceFile ->
            {
                try
                {
                    destFile[0] = destDir.resolve(sourceDir.relativize(sourceFile));
                    Files.createDirectories(destFile[0].getParent());
                    Files.move(sourceFile, destFile[0], REPLACE_EXISTING);
                } catch (IOException e)
                {
                    updateStatus(Level.WARNING, "Failed to move file: " + e.getMessage());
                }
            });

            updateStatus("Flight plan downloaded successfully to " + destFile[0]);
        } catch (IOException e)
        {
            updateStatus(Level.WARNING, "Failed to walk the ghost folder path: " + e.getMessage());
        }

        driver.navigate().to(currentURL);

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
        about .showAndWait();
        about.close();
    }

    public void setHelpDialog(HelpDialog helpDialog)
    {
        this.helpDialog = helpDialog;
    }
}
