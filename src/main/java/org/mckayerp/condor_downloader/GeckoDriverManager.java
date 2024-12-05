package org.mckayerp.condor_downloader;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

class GeckoDriverManager
{

    private static final Logger logger = Logger.getLogger(GeckoDriverManager.class.getName());

    private GeckoDriverManager()
    {
    }

    static void setup()
    {
        String geckoPath = System.getProperty("user.dir") + "\\drivers\\geckodriver\\geckodriver.exe";
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
                throw new RuntimeException("Can't located the Gecko driver executable. Expected to find it in the " + "installed directory in" + " drivers/geckodriver.");
            }
        }
        System.setProperty("webdriver.gecko.driver", geckoPath);
    }

    static void kill()
    {

        try
        {
            Runtime.getRuntime().exec("taskkill /F /IM geckodriver.exe");
        } catch (IOException e)
        {
            throw new RuntimeException(e);
        }

    }
}
