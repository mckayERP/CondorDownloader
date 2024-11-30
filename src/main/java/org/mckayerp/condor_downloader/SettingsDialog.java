package org.mckayerp.condor_downloader;

import javafx.fxml.FXMLLoader;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.stage.Modality;
import javafx.stage.Window;

import java.io.IOException;
import java.util.Objects;

public class SettingsDialog extends Dialog<Boolean> {

    private final SettingsDialogController controller;
    public SettingsDialog(Window owner) {

        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("SettingsDialog.fxml"));
            DialogPane dialogPane = loader.load();
            controller = loader.getController();
            controller.setDialog(this);
            controller.setWindow(owner);
            controller.loadSettings();
            initModality(Modality.APPLICATION_MODAL);
            setResizable(true);
            setTitle("Application Settings");
            setDialogPane(dialogPane);
            setResultConverter(buttonType -> !Objects.equals(ButtonBar.ButtonData.OK_DONE, buttonType.getButtonData()));

        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public SettingsDialogController getController() {
        return controller;
    }



}
