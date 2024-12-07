package org.mckayerp.condor_downloader;

import javafx.concurrent.Task;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.firefox.FirefoxProfile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static org.mckayerp.condor_downloader.CondorVersion.CONDOR_2;
import static org.mckayerp.condor_downloader.CondorVersion.CONDOR_3;
import static org.mckayerp.condor_downloader.GhostFileManager.extractGhostFilesFromZipFiles;
import static org.mckayerp.condor_downloader.GhostIDFinder.findGhostIDs;
import static org.mckayerp.condor_downloader.ZipFileManager.deleteZipFiles;

public class DownloadManager
{

    private final static Logger logger = Logger.getLogger(DownloadManager.class.getName());
    private final DownloadData downloadData;
    private Integer numberToDownload;
    private String taskCode;
    private Path firefoxPath;
    private Path downloadDirectory;
    private StatusProvider statusProvider;
    private CondorVersion condorVersion;
    private boolean condor2DirectoryExists;
    private boolean condor3DirectoryExists;
    private Path ghostFolderPath;
    private Path ghostTaskFolder;
    private boolean flightPlanSelected;
    private boolean isCopyGhostsToFlightTrackFolderSelected;
    private boolean isCopyGhostsFromCompetitionSelected;
    Task<Void> downloadTask = new Task<>()
    {
        @Override
        protected Void call()
        {
            download();
            return null;
        }
    };

    public DownloadManager(DownloadData data)
    {
        this.downloadData = data;
    }

    public void startInBackground()
    {
        // Start the download task in a background thread
        Thread downloadThread = new Thread(downloadTask);
        downloadThread.setDaemon(true); // Daemon thread to ensure it stops if the application exits
        downloadThread.start();
    }

    private void download()
    {

        GeckoDriverManager.setup();
        logger.log(Level.FINE, "->download()");
        setup();
        downloadDirectory = ApplicationFolderManager.createOrClearTmpFolder();
        statusProvider.updateStatus("Starting download...", true, false);
        WebDriver driver = getWebDriver();
        if (getTaskPageAndLogIn(driver))
        {
            if (flightPlanSelected)
                downloadFlightPlan(driver);
            if (numberToDownload > 0)
                downloadGhosts(getBestTimesForTask(driver), driver);
            driver.close();
        }
        GeckoDriverManager.kill();
        statusProvider.updateStatus("Download complete!  Enjoy the flight.");
        statusProvider.updateStatus("Or, you can download a different task.");


    }

    private void setup()
    {
        statusProvider = downloadData.getStatusProvider();
        numberToDownload = downloadData.getNumberToDownload();
        taskCode = downloadData.getTaskCode();
        firefoxPath = downloadData.getFirefoxExecutablePath();
        condor2DirectoryExists = downloadData.doesCondor2DirectoryExist();
        condor3DirectoryExists = downloadData.doesCondor3DirectoryExist();
        flightPlanSelected = downloadData.isDownloadFlightPlanSelected();
        isCopyGhostsToFlightTrackFolderSelected = downloadData.isCopyGhostsToFlightTrackFolderSelected();
        isCopyGhostsFromCompetitionSelected = downloadData.isCopyGhostsFromCompetitionSelected();
    }

    private boolean getTaskPageAndLogIn(WebDriver driver)
    {

        statusProvider.updateStatus("Searching for the task on condor.club with code " + taskCode + "...");
        String urlWithLogin = "https://condor.club/showtask/0/?netid=" + taskCode;

        driver.get(urlWithLogin);
        String pageSource = driver.getPageSource();
        if (pageSource == null) {
            statusProvider.updateStatus(Level.SEVERE, "The page source was null. Unable to load the web page.",
                    true, true);
            return false;
        }
        if (pageSource.contains("Task not available yet."))
        {
            statusProvider.updateStatus("The task was not found or is not available yet.  Please try again " +
                    "with a different task code.", false, true);
            return false;
        }
        if (pageSource.contains("This task is currently used in competition and is not available now."))
        {
            statusProvider.updateStatus("The task is being used in a competition and is not available now.  " +
                    "Please try again with a different task code.", false, true);
            return false;
        }

        statusProvider.updateStatus("Found the task page...");
        condorVersion = getCondorVersion(driver);
        if (versionIsNotCompatible())
            return false;

        if (!logInFromTaskPage(driver))
            return false;

        ghostFolderPath = ApplicationFolderManager.getGhostFolder(condorVersion);
        ghostTaskFolder = ApplicationFolderManager.getGhostTaskFolder(condorVersion, taskCode);

        return true;

    }

    private boolean logInFromTaskPage(WebDriver driver)
    {
        statusProvider.updateStatus("Logging in...");
        WebElement userEmailElement = driver.findElement(By.name("login"));
        WebElement passwordElement = driver.findElement(By.name("pwd"));
        WebElement login = driver.findElement(By.xpath("//a[contains(@href,\"#\")]"));
        userEmailElement.sendKeys(downloadData.getCondorClubUserEmail());
        passwordElement.sendKeys(downloadData.getCondorClubUserPassword());
        login.click();
        return waitForLoginReturningSuccess(driver);
    }

    private boolean waitForLoginReturningSuccess(WebDriver driver)
    {

        int maxWaitCount = 10;
        int count = 0;
        String pageSource = driver.getPageSource();
        boolean loginSuccessful = pageSource != null && pageSource.contains("Logout");
        boolean loginFailed = pageSource != null && pageSource.contains("Your e-mail or your password is incorrect.");
        boolean pageLoaded = loginSuccessful || loginFailed;
        while (!pageLoaded && count++ < maxWaitCount)
        {
            try
            {
                Thread.sleep(500);
            } catch (InterruptedException ignored)
            {
            }
            pageSource = driver.getPageSource();
            loginSuccessful = pageSource != null && pageSource.contains("Logout");
            loginFailed = pageSource != null && pageSource.contains("Your e-mail or your password is incorrect.");
            pageLoaded = loginSuccessful || loginFailed;
        }

        if (loginSuccessful)
        {
            statusProvider.updateStatus("Login successful...");
        } else if (loginFailed)
        {
            statusProvider.updateStatus("Login was not successful. Please check the username and password in " + "settings and try again.", true, true);
        } else
            statusProvider.updateStatus("Login was not successful. There was a timeout.", true, true);

        return loginSuccessful;
    }

    private WebDriver getWebDriver()
    {
        FirefoxProfile fxProfile = new FirefoxProfile();
        fxProfile.setPreference("browser.download.folderList", 2);
        fxProfile.setPreference("browser.download.manager.showWhenStarting", false);
        fxProfile.setPreference("browser.download.dir", downloadDirectory.toString());
        fxProfile.setPreference("browser.helperApps.neverAsk.saveToDisk", "zip");
        FirefoxOptions options = new FirefoxOptions();
        options.setProfile(fxProfile);
        options.setBinary(firefoxPath);
        options.addArguments("-headless");
        options.setPageLoadTimeout(Duration.ofSeconds(10));
        return new FirefoxDriver(options);
    }

    CondorVersion getCondorVersion(WebDriver driver)
    {
        if (driver == null)
            throw new IllegalArgumentException("WebDriver cannot be null!");

        String pageSource = driver.getPageSource();
        if (pageSource == null)
            throw new RuntimeException("Page source from the driver is null. Unable to determine what " + "the Condor version is.");

        statusProvider.updateStatus("Determining the Condor version...");
        String tagString = "Condor version:";
        String subPage = pageSource.substring(pageSource.indexOf(tagString));
        Pattern pattern = Pattern.compile("Condor version:\\D*?>(\\d)");
        Matcher matcher = pattern.matcher(subPage);
        String version = "0";
        if (matcher.find())
            version = matcher.group(1);
        if (version.startsWith("2"))
        {
            statusProvider.updateStatus("Found Condor2...");
            condorVersion = CONDOR_2;
        } else if (version.startsWith("3"))
        {
            statusProvider.updateStatus("Found Condor3...");
            condorVersion = CONDOR_3;
        } else
        {
            statusProvider.updateStatus(Level.SEVERE, "Unable to tell what Condor version was or the version is incompatible with " +
                    "Condor2 or Condor3");
            throw new RuntimeException("Unable to tell from the page source what the Condor version is.");
        }

        return condorVersion;
    }

    private boolean versionIsNotCompatible()
    {

        // @formatter:off
        if (condorVersion == CONDOR_2 && !condor2DirectoryExists
                || condorVersion == CONDOR_3 && !condor3DirectoryExists)
        {
            warnIncompatibleVersion(condorVersion);
            return true;
        }
        return false;
        // @formatter:on
    }

    private void warnIncompatibleVersion(CondorVersion version)
    {
        String warning = "The task selected is version * but it does not seem Condor * is " +
                "installed. Please check the settings and try again or choose a task compatible with the " +
                "installed version of Condor.";
        String versionNumber = version == CONDOR_2 ? "2" : "3";
        statusProvider.clearStatus();
        statusProvider.updateStatus(Level.WARNING, warning.replace("*", versionNumber));
    }


    public void downloadFlightPlan(WebDriver driver)
    {

        statusProvider.updateStatus("Downloading the flight plan...");
        String currentURL = driver.getCurrentUrl();
        if (currentURL == null)
            throw new RuntimeException("Condor.club task page URL is null");

        String taskPageSource = driver.getPageSource();
        if (taskPageSource == null || taskPageSource.isEmpty())
        {
            statusProvider.updateStatus(Level.SEVERE, "The task page source is null or empty!");
            return;
        }
        String taskID = getCondorClubTaskIDFromPageSource(taskPageSource);
        boolean downloadPossible = taskPageSource.contains("Download it now!");
        if (!downloadPossible)
        {
            statusProvider.updateStatus("The flight plan can't be downloaded. It may be active in a competition " +
                    "or not available yet.");
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

        if (waitForDownload(downloadDirectory, ".fpl"))
            statusProvider.updateStatus("Downloaded flight plan for task " + taskCode + "...");
        else
            statusProvider.updateStatus("Unable to downloaded flight plan for task " + taskCode +
                    ". A timeout occurred.");

        Path sourceDir = downloadDirectory;
        Path destDir = ApplicationFolderManager.getFlightPlanFolder(condorVersion);
        Path[] destFile = {null};
        try (Stream<Path> paths = Files.walk(sourceDir))
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
                    statusProvider.updateStatus(Level.WARNING, "Failed to move file: " + e.getMessage());
                }
            });

            statusProvider.updateStatus("Flight plan downloaded successfully to " + destFile[0]);
        } catch (IOException e)
        {
            statusProvider.updateStatus(Level.WARNING, "Failed to walk the ghost folder path: " + e.getMessage());
        }

        driver.navigate().to(currentURL);

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
            statusProvider.updateStatus(Level.WARNING, "Download directory not found or download was interrupted.");
        }
        return found;
    }

    String getCondorClubTaskIDFromPageSource(String content)
    {
        if (content == null)
            throw new IllegalArgumentException("The page content can't be null.");
        String startString = "<meta property=\"og:url\" content=\"https://www.condor.club/stub/task/0/?id=";
        int metaTagStart = content.indexOf(startString) + startString.length();
        String idTag = content.substring(metaTagStart);
        return idTag.substring(0, idTag.indexOf("&amp;l=1"));
    }

    private void downloadGhosts(String bestTimesSource, WebDriver driver)
    {
        statusProvider.updateStatus("Looking for " + numberToDownload + " ghost flight track" + (numberToDownload > 1 ? "s" : "") + " to download...");
        int[] count = {0};
        List<String> matches = findGhostIDs(bestTimesSource, numberToDownload);
        statusProvider.updateStatus("Found " + matches.size() + " matching flight track" + (matches.size() > 1 ? "s..." : "..."));
        matches.forEach(ghostID ->
        {
            if (downloadGhostZipFile(ghostID, driver))
                count[0]++;
        });
        if (count[0] == 0)
            statusProvider.updateStatus("All ghost flight tracks already exist and won't be downloaded again " + "OR the download of ghost tracks for this task is not yet enabled " + "OR there are no ghost tracks to download.");
        else if (count[0] < numberToDownload)
            statusProvider.updateStatus("Only downloaded " + count[0] + " flight track" + (count[0] > 1 ? "s" : "") + " as that was all there was " + "OR the other ghost flight tracks were previously downloaded.");
        else
            statusProvider.updateStatus("Was able to downloaded " + count[0] + " ghost flight track" + (count[0] > 1 ? "s..." : "..."));

        extractGhostFilesFromZipFiles(downloadDirectory, ghostTaskFolder, taskCode);
        deleteZipFiles(downloadDirectory);
        if (isCopyGhostsToFlightTrackFolderSelected)
            copyGhostFilesToFlightTrackFolder(ghostTaskFolder);
    }

    private String getBestTimesForTask(WebDriver driver)
    {
        String taskPageSource = driver.getPageSource();
        String taskID = getCondorClubTaskIDFromPageSource(taskPageSource);
        String bestTimesPageURL = findBestTimesPageURL(taskPageSource, taskID);
        driver.get(bestTimesPageURL);
        String bestTimesSource = driver.getPageSource();
        statusProvider.updateStatus("Found the best times list...");
        return bestTimesSource;
    }

    private String findBestTimesPageURL(String content, String id)
    {
        if (content == null || content.isEmpty())
            throw new IllegalArgumentException("Content can't be null");

        boolean wasUsedInCompetition = content.contains("This task has been used in a competition.");
        String bestTimesPageURL = "https://www.condor.club/besttimes/0/?id=" + id;
        if (wasUsedInCompetition && isCopyGhostsFromCompetitionSelected)
        {
            bestTimesPageURL = "https://www.condor.club/comp/besttimes/0/?id=" + id;
            statusProvider.updateStatus("Using competition results.");
        }
        return bestTimesPageURL;
    }

    private void copyGhostFilesToFlightTrackFolder(Path sourceDir)
    {

        Path destDir = ApplicationFolderManager.getFlightTracksFolder(condorVersion);
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
                    statusProvider.updateStatus(Level.WARNING, "Failed to copy file " + sourceFile.toString());
                }
            });

            statusProvider.updateStatus("All existing and/or new ghost flight tracks copied successfully to the flight track folder: " + destDir);
        } catch (IOException e)
        {
            statusProvider.updateStatus(Level.WARNING, "Failed to find or walk the ghost folder " + sourceDir.toString() + "\n" + e);
        }

    }

    private boolean downloadGhostZipFile(String ghostID, WebDriver driver)
    {
        if (ghostFileAlreadyExists(ghostID))
        {
            statusProvider.updateStatus("Ghost file with ID " + ghostID + " already exists and won't be downloaded.");
            return false;
        }
        try
        {
            driver.navigate().to("https://www.condor.club/download2/0/?res=" + ghostID + "&next=1");
        } catch (TimeoutException ignored)
        {
            // Timeout is expected
        }
        boolean success = waitForDownload(downloadDirectory, ghostID);
        if (success)
            statusProvider.updateStatus("Flight track for flight ID " + ghostID + " downloaded.");
        else
            statusProvider.updateStatus(Level.WARNING, "Not able to download flight track for flight ID " + ghostID + ". A timeout occurred.");
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

}
