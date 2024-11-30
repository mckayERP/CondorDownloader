package org.mckayerp.condor_downloader;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.mckayerp.condor_downloader.WindowsRegistry.readRegistry;

public class FireFoxFinder
{

    private static final Logger logger = Logger.getLogger(FireFoxFinder.class.getName());
    public static String getPathToExe()
    {

        String fireFoxExePath = readRegistry("HKLM\\SOFTWARE\\MOZILLA\\MOZILLA FIREFOX", "PathToExe", "/s");

        if (fireFoxPathIsBad(fireFoxExePath))
        {
            logger.log(Level.WARNING, "Firefox EXE not found in the registry. It is not installed or will have " +
                    "to be set manually.");
            fireFoxExePath = "Select the path to firefox.exe";
        }

        return fireFoxExePath;

    }

    public static boolean fireFoxPathIsBad(String firefoxPath) throws NullPointerException, IllegalArgumentException
    {
        return firefoxPath == null || firefoxPath.isEmpty() || !new File(firefoxPath).canExecute();

    }
}
