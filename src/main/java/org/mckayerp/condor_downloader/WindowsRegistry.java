package org.mckayerp.condor_downloader;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;

/**
 * @author Oleg Ryaboy, based on work by Miguel Enriquez
 */
public class WindowsRegistry
{

    /**
     * @param location path in the registry
     * @param key      registry key
     * @return registry value or null if not found
     */
    public static String readRegistry(String location, String key, String parameters)
    {
        try
        {
            // Run reg query, then read output with StreamReader (internal class)
            String cmd = "reg";
            String query = "query";
            String params = "\"" + location + "\"" + " /v \"" + key + "\"";
            if (parameters != null && !parameters.isEmpty())
                params += " " + parameters;
            String command = cmd + " " + query + " " + params;

            Process process = Runtime.getRuntime().exec(command);

            StreamReader reader = new StreamReader(process.getInputStream());
            reader.start();
            process.waitFor();
            reader.join();
            String output = reader.getResult();

            if (output.contains("ERROR:"))
            {
                return null;
            }

            /*
                The results should look like this:
                "
                HKEY_LOCAL_MACHINE\SOFTWARE\MOZILLA\MOZILLA FIREFOX\133.0 (x64 en-US)\Main
                    PathToExe    REG_SZ    C:\Program Files\Mozilla Firefox\firefox.exe

                End of search: 1 match(es) found.
                "

                Removing the blank lines and trimming whitespace, and then splitting on multiple spaces, the
                result will be the second from the end
             */
            // Parse out the value
            String[] parsed = output.replace("\r\n", "  ").trim().split("\s\s+");
            if (parsed.length < 2)
                return null;
            return parsed[parsed.length - 2];
        } catch (Exception e)
        {
            throw new RuntimeException(e);
        }

    }

    public static void main(String[] args)
    {

        // Sample usage
        String value = WindowsRegistry.readRegistry("HKLM\\SOFTWARE\\MOZILLA\\MOZILLA FIREFOX", "PathToExe", "/s");
        System.out.println("Value is -> " + value);
    }

    static class StreamReader extends Thread
    {
        private final InputStream is;
        private final StringWriter sw = new StringWriter();

        public StreamReader(InputStream is)
        {
            this.is = is;
        }

        public void run()
        {
            try
            {
                int c;
                while ((c = is.read()) != -1)
                    sw.write(c);
            } catch (IOException ignored)
            {
            }
        }

        public String getResult()
        {
            return sw.toString();
        }
    }
}
