package org.mckayerp.condor_downloader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

public class GhostFileManager
{

    public static String findNewFileName(String zipFileName, String taskCode, String ghostID)
    {

        String contestLetters = zipFileName;
        contestLetters = contestLetters.substring(0, contestLetters.lastIndexOf("-"));
        contestLetters = contestLetters.substring(contestLetters.lastIndexOf("-") + 1);

        String newFileName = "Ghost_" + taskCode + "_" + contestLetters + "_" + ghostID + ".ftr";
        System.out.println("Creating new file: " + zipFileName + " -> " + newFileName);

        return newFileName;

    }

    public static void extractGhostFilesFromZipFiles(Path downloadDirectory, Path newGhostDirectoryPath, String taskCode)
    {
        try (Stream<Path> files = Files.walk(downloadDirectory))
        {
            files.filter(zipPath -> zipPath.toFile().getName().endsWith(".zip")).forEach(zipPath ->
            {
                String zipFileName = zipPath.toFile().getName();
                String ghostID = zipFileName.substring(zipFileName.lastIndexOf("-") + 1, zipFileName.lastIndexOf(".zip"));
                String newFileName = findNewFileName(zipPath.toFile().getName(), taskCode, ghostID);
                ZipFileManager.extractSingleFileFromArchiveAndRename(zipPath, newGhostDirectoryPath, newFileName);
            });
        } catch (IOException ignored)
        {
        }
    }

    public static void deleteGhostFiles(DownloadData data)
    {
        if (data.doesCondor2DirectoryExist())
            deleteGhostFiles(data.getCondor2Path().resolve("FlightTracks"));
        if (data.doesCondor3DirectoryExist())
            deleteGhostFiles(data.getCondor3Path().resolve("FlightTracks"));
    }

    public static void deleteGhostFiles(Path directoryPath)
    {
        try (Stream<Path> files = Files.walk(directoryPath))
        {
            files.filter(path -> path.getParent().equals(directoryPath) && path.toFile().getName().startsWith("Ghost_") && (path.toFile().getName().endsWith(".ftr") || path.toFile().getName().endsWith(".igc"))).forEach(path ->
            {
                if (!path.toFile().delete())
                    System.out.println("Couldn't delete file " + path);
            });
        } catch (IOException e)
        {
            System.out.println("Couldn't delete ghost .ftr and .igc files: " + e);
        }
    }

}
