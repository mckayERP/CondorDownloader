package org.mckayerp.condor_downloader;

import javafx.scene.control.Alert;

public class AboutDialog extends Alert
{
    public AboutDialog()
    {
        super(AlertType.INFORMATION);
        setTitle("About");
        setHeaderText("Condor Downloader");
        setGraphic(null);
        // @formatter:off
        setContentText(
                "Copyright Michael McKay (McKayERP) 2024\n\n" +
                "Version: " + VersionInfo.getGitProperty("git.tags") + "\n" +
                "Commit: " + VersionInfo.getGitProperty("git.commit.id.abbrev") + "\n" +
                "Build Time: " + VersionInfo.getGitProperty("git.build.time") + "\n"
        );
        // @formatter:on
    }

}
