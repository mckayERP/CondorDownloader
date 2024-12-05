package org.mckayerp.condor_downloader;

import java.io.File;

public class FolderUtilities
{

    public static void deleteFolderContents(File folder)
    {
        deleteFolder(folder, false);
    }

    public static void deleteFolder(File folder, boolean deleteTopLevel)
    {
        File[] files = folder.listFiles();
        if (files != null)
        { //some JVMs return null for empty dirs
            for (File f : files)
            {
                if (f.isDirectory())
                {
                    deleteFolder(f, true);
                } else
                {
                    f.delete();
                }
            }
        }
        if (deleteTopLevel)
            folder.delete();
    }
}
