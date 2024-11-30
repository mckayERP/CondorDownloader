package org.mckayerp.condor_downloader;

import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class VersionInfo
{
    static Logger logger = Logger.getLogger(VersionInfo.class.getName());

    public static String getGitProperty(String key) {
        Properties props = new Properties();
        try {
            props.load(VersionInfo.class.getResourceAsStream("git.properties"));
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Unable to open git.properties: " + e.getMessage());
        }
        return props.getProperty(key, "Unknown");
    }

}
