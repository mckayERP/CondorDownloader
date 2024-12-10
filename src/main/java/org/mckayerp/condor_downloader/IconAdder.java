package org.mckayerp.condor_downloader;

import javafx.scene.image.Image;
import javafx.stage.Stage;

public class IconAdder
{
    public static void addIcons(Stage stage)
    {
        stage.getIcons().add(new Image(IconAdder.class.getResourceAsStream("cdicon_16x16.png")));
        stage.getIcons().add(new Image(IconAdder.class.getResourceAsStream("cdicon_24x24.png")));
        stage.getIcons().add(new Image(IconAdder.class.getResourceAsStream("cdicon_32x32.png")));
        stage.getIcons().add(new Image(IconAdder.class.getResourceAsStream("cdicon_64x64.png")));

    }
}
