package org.mckayerp.condor_downloader;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.mckayerp.condor_downloader.CondorVersion.CONDOR_2;
import static org.mckayerp.condor_downloader.CondorVersion.CONDOR_3;

class ApplicationFolderManager
{

    private ApplicationFolderManager()
    {
    }

    private static final Logger logger = Logger.getLogger(ApplicationFolderManager.class.getName());
    private static final String APPLICATION_FOLDER_NAME = ".condorDownloader";
    private static final String TEMPORARY_DOWNLOAD_FOLDER_NAME = "tmp";
    private static final String GECKO_DRIVER_PATH = "bin";
    private static final String GECKO_EXE_NAME = "geckodriver.exe";

    static Path createOrClearTmpFolder()
    {

        String userHome = System.getProperty("user.home");
        Path downloadDirectory = Paths.get(userHome, APPLICATION_FOLDER_NAME, TEMPORARY_DOWNLOAD_FOLDER_NAME);
        if (downloadDirectory.toFile().exists() && downloadDirectory.toFile().isDirectory())
        {
            FolderUtilities.deleteFolderContents(downloadDirectory.toFile());
        } else if (!downloadDirectory.toFile().mkdir())
            logger.log(Level.SEVERE, "Did not manage to create temporary download directory " + downloadDirectory);

        return downloadDirectory;

    }

    static Path getDefaultCondorDirectory(CondorVersion version)
    {

        String userHome = System.getProperty("user.home");
        Path condorFolderPath;
        if (version == CONDOR_3)
            condorFolderPath = Paths.get(userHome, "Documents", "Condor3");
        else
            condorFolderPath = Paths.get(userHome, "Documents", "Condor");

        if (!Files.exists(condorFolderPath) && Files.isDirectory(condorFolderPath))
        {
            condorFolderPath = null;
        }
        return condorFolderPath;

    }

    static Path getCondorFolder(CondorVersion condorVersion)
    {

        Path condorPath = getDefaultCondorDirectory(condorVersion);
        if (condorPath == null)
        {

            logger.log(Level.WARNING, "Could not find the default condor directory for condor version");
            throw new RuntimeException("The correct version of Condor is not installed for the task.");

        }

        return condorPath;

    }

    static boolean condor2DirectoryExists()
    {
        return Files.exists(getCondorFolder(CONDOR_2));
    }

    static boolean condor3DirectoryExists()
    {
        return Files.exists(getCondorFolder(CONDOR_3));
    }

    static Path getGhostTaskFolder(CondorVersion version, String taskCode)
    {

        Path ghostFolderPath = getGhostFolder(version);
        Path newGhostDirectoryPath = Paths.get(ghostFolderPath.toString(), taskCode);
        try
        {
            Files.createDirectories(newGhostDirectoryPath);
        } catch (IOException e)
        {
            logger.log(Level.WARNING, "Could not create or find the ghost folder path " + ghostFolderPath + "\n" + e);
            throw new RuntimeException(e);
        }
        return newGhostDirectoryPath;

    }

    static Path getGhostFolder(CondorVersion version)
    {

        Path condorPath = getCondorFolder(version);
        return Paths.get(condorPath.toString(), "Ghosts");

    }

    static Path getFlightPlanFolder(CondorVersion condorVersion)
    {
        return Paths.get(getCondorFolder(condorVersion).toString(), "FlightPlans");
    }

    static Path getFlightTracksFolder(CondorVersion condorVersion)
    {

        return Paths.get(getCondorFolder(condorVersion).toString(), "FlightTracks");

    }

    static Path getApplicationFolder()
    {
        Path applicationFolder = Paths.get(System.getProperty("user.home"), APPLICATION_FOLDER_NAME);
        if (!Files.exists(applicationFolder) && applicationFolder.toFile().mkdir())
            logger.log(Level.SEVERE, "Unable to find or make the application folder in user home.");
        return applicationFolder;
    }

    public static Path getGeckoPath()
    {
        Path geckoPath = Paths.get(System.getProperty("user.dir"), GECKO_DRIVER_PATH, GECKO_EXE_NAME);
        logger.log(Level.FINE, "Looking for gecko driver in path " + geckoPath);
        File file = geckoPath.toFile();
        if (!file.exists())
        {
            logger.log(Level.FINE, "Gecko driver not found in installed location. Defaulting to dev environment.");
            geckoPath = Paths.get(System.getProperty("user.dir"),"target","CondorDownloaderBat",GECKO_DRIVER_PATH,GECKO_EXE_NAME);
            file = geckoPath.toFile();
            if (!file.exists())
            {
                logger.log(Level.SEVERE, "Gecko driver not found!");
                throw new RuntimeException("Can't located the Gecko driver executable. Expected to find it in the " +
                        "installed directory in bin/drivers/geckodriver.");
            }
        }
        return geckoPath;
    }

    public static Path getLoggerPropertiesPath()
    {
        Path propertiesPath = Paths.get(System.getProperty("user.dir"), "conf", "logging.properties");
        System.out.println("Trying to load logger config from " + propertiesPath);
        if (!propertiesPath.toFile().exists())
        {
            System.out.println("Failed.");
            propertiesPath = Paths.get(System.getProperty("user.dir"),"target", "CondorDownloader", "conf", "logging.properties");
            System.out.println("Trying to load logger config from " + propertiesPath);
        }
        return propertiesPath;
    }
}
