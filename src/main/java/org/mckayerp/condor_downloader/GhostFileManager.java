package org.mckayerp.condor_downloader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

import static org.mckayerp.condor_downloader.CondorVersion.CONDOR_2;
import static org.mckayerp.condor_downloader.CondorVersion.CONDOR_3;
import static org.mckayerp.condor_downloader.DownloadManager.getCondorFolderPath;

public class GhostFileManager
{

    static Logger logger = Logger.getLogger(GhostFileManager.class.getName());

    public static String findNewFileName(String zipFileName, String taskCode, String ghostID)
    {

        String contestLetters = zipFileName;
        contestLetters = contestLetters.substring(0, contestLetters.lastIndexOf("-"));
        contestLetters = contestLetters.substring(contestLetters.lastIndexOf("-") + 1);

        return "Ghost_" + taskCode + "_" + contestLetters + "_" + ghostID + ".ftr";

    }

    public static void extractGhostFilesFromZipFiles(Path downloadDirectory, Path newGhostDirectoryPath, String taskCode)
    {
        try (Stream<Path> files = Files.walk(downloadDirectory))
        {
            files.filter(zipPath -> zipPath.toFile().getName().endsWith(".zip")).forEach(zipPath ->
            {
                String zipFileName = zipPath.toFile().getName();
                String ghostID = zipFileName.substring(zipFileName.lastIndexOf("-") + 1, zipFileName.lastIndexOf(".zip"));
                String newFileName = findNewFileName(zipFileName, taskCode, ghostID);
                ZipFileManager.extractSingleFileFromArchiveAndRename(zipPath, newGhostDirectoryPath, newFileName);
                logger.log(Level.FINE, "Extracted " + zipFileName + " to " + newFileName);
            });
        } catch (IOException ignored)
        {
        }
    }

    public static void deleteGhostFiles()
    {
        if (Files.exists(getCondorFolderPath(CONDOR_2)))
            deleteGhostFiles(getCondorFolderPath(CONDOR_2).resolve("FlightTracks"));
        if (Files.exists(getCondorFolderPath(CONDOR_3)))
            deleteGhostFiles(getCondorFolderPath(CONDOR_3).resolve("FlightTracks"));

    }

    public static void deleteGhostFiles(Path directoryPath)
    {
        try (Stream<Path> files = Files.walk(directoryPath))
        {
            files.filter(path -> path.getParent().equals(directoryPath) && path.toFile().getName().startsWith("Ghost_") && (path.toFile().getName().endsWith(".ftr") || path.toFile().getName().endsWith(".igc"))).forEach(path ->
            {
                if (!path.toFile().delete())
                    logger.log(Level.WARNING,"Couldn't delete file " + path);
            });
        } catch (IOException e)
        {
            logger.log(Level.WARNING,"Couldn't delete ghost .ftr and .igc files: " + e);
        }
    }

}
