package org.mckayerp.condor_downloader;

import java.io.IOException;
import java.nio.file.Path;

class GeckoDriverManager
{

    private GeckoDriverManager()
    {
    }

    static void setup()
    {
        Path geckoPath = ApplicationFolderManager.getGeckoPath();
        System.setProperty("webdriver.gecko.driver", geckoPath.toString());
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
