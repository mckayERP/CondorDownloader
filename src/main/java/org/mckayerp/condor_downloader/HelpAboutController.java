package org.mckayerp.condor_downloader;

import javafx.scene.control.DialogPane;
import javafx.scene.control.TextArea;

public class HelpAboutController
{
    public TextArea aboutText;
    public DialogPane aboutPane;

    public void loadHelpContent()
    {
        String aboutString = "\nCopyright 2024 Michael McKay (McKayerp)\n" +
                "Contact: mckayerp@gmail.com\n" +
                "Report issues to https://github.com/mckayERP/CondorDownloader/issues\n\n" +
                VersionInfo.getGitInfo();
        aboutText.setText(aboutString);
    }
}
