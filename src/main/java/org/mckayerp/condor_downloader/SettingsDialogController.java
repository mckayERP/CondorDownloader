package org.mckayerp.condor_downloader;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import org.apache.commons.configuration2.INIConfiguration;
import org.apache.commons.configuration2.SubnodeConfiguration;
import org.apache.commons.configuration2.ex.ConfigurationException;

import java.io.*;
import java.net.URL;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.util.Objects.requireNonNull;

public class SettingsDialogController implements Initializable
{

    private static final String USER_INFO_SECTION = "User_Info";
    private static final String USER_EMAIL = "CondorClub_User_Email";
    private static final String USER_PASSWORD = "CondorClub_User_Password";
    private static final String FIREFOX_SECTION = "Firefox";
    private static final String EXECUTABLE_PATH = "executable_path";
    private static final String INI_FILE = "CondorDownloader.ini";
    private static Logger logger;
    public DialogPane dialogPane;
    public TextField condorClubUserEmailField;
    public PasswordField condorClubUserPasswordField;
    public TextField firefoxExecutablePathField;
    public ButtonType saveSettingsButtonType;
    public Button firefoxPathBrowseButton;
    private Window owner;
    private String firefoxExecutablePath;
    private INIConfiguration iniConfiguration;
    private String condorClubUserEmail;
    private String condorClubUserPassword;

    public SettingsDialogController()
    {
        logger = Logger.getLogger(SettingsDialogController.class.getName());
    }

    public String getCondorClubUserEmail()
    {
        return condorClubUserEmail;
    }

    public String getCondorClubUserPassword()
    {
        return condorClubUserPassword;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle)
    {

        dialogPane.lookupButton(saveSettingsButtonType).addEventFilter(ActionEvent.ANY, this::saveSettings);

        firefoxPathBrowseButton.setOnAction(actionEvent ->
        {
            FileChooser chooser = getExecutableFileChooser(firefoxExecutablePathField);
            File firefoxExecutable = chooser.showOpenDialog(owner);
            if (firefoxExecutable != null && firefoxExecutable.exists() && firefoxExecutable.canExecute())
            {
                firefoxExecutablePathField.setText(firefoxExecutable.getAbsolutePath());
                setFirefoxExecutablePath(firefoxExecutablePathField.getText());
                logger.log(Level.FINE, "Firefox executable set to " + firefoxExecutablePath);
            } else
                throw new RuntimeException("Please select the Firefox executable: " + firefoxExecutablePathField.getText());
        });

    }

    private FileChooser getExecutableFileChooser(TextField textField)
    {
        FileChooser chooser = new FileChooser();
        chooser.setSelectedExtensionFilter(new FileChooser.ExtensionFilter("Executable (*.exe)", "*.exe"));
        chooser.setInitialFileName("firefox.exe");
        chooser.setTitle("Select firefox executable");
        if (!textField.getText().isEmpty())
            chooser.setInitialDirectory(new File(textField.getText()).getParentFile());
        else
            chooser.setInitialDirectory(new File("C:\\"));
        return chooser;
    }

    void saveSettings(ActionEvent actionEvent)
    {

        logger.log(Level.FINE, "Saving settings!");
        saveSettings();

    }

    public void setDialog(SettingsDialog settingsDialog)
    {
        settingsDialog.setOnShowing(dialogEvent -> Platform.runLater(() -> firefoxExecutablePathField.requestFocus()));
    }

    public void setWindow(Window owner)
    {
        this.owner = requireNonNull(owner);
    }

    public String getFirefoxExecutablePath()
    {
        return firefoxExecutablePath;
    }

    public void setFirefoxExecutablePath(String firefoxExecutablePath)
    {
        this.firefoxExecutablePath = firefoxExecutablePath;
    }

    public void loadSettings()
    {

        readIniConfigurationFromFile();
        SubnodeConfiguration userInfoSection = iniConfiguration.getSection(USER_INFO_SECTION);
        condorClubUserEmail = userInfoSection.getString(USER_EMAIL);
        String encryptedPassword = userInfoSection.getString(USER_PASSWORD);
        condorClubUserPassword = decryptPassword(encryptedPassword);

        Path path = getPathFromIniFile(iniConfiguration, FIREFOX_SECTION, EXECUTABLE_PATH);
        if (path != null && path.toFile().exists() && path.toFile().canExecute())
            firefoxExecutablePath = path.toString();
        else
        {
            firefoxExecutablePath = FireFoxFinder.get().getPathToExe();
        }

        updateSettingFields();


    }

    private void updateSettingFields()
    {
        condorClubUserEmailField.setText(condorClubUserEmail);
        condorClubUserPasswordField.setText(condorClubUserPassword);
        firefoxExecutablePathField.setText(firefoxExecutablePath);
    }

    private String decryptPassword(String encryptedPassword)
    {
        String pw;
        if (encryptedPassword == null)
            return "";
        try
        {
            pw = new EncryptionManager().decrypt(encryptedPassword);
        } catch (Exception e)
        {
            logger.log(Level.WARNING, "Unable to decrypt password");
            throw new RuntimeException(e);
        }
        return pw;
    }

    private void readIniConfigurationFromFile()
    {
        try
        {
            File iniFile = getIniConfiguration();
            Reader iniFileReader = new FileReader(iniFile);
            logger.log(Level.FINE, "Looking for ini file " + iniFile.getPath());
            iniConfiguration = new INIConfiguration();
            iniConfiguration.read(iniFileReader);
            logger.log(Level.FINE, "Found " + INI_FILE);
        } catch (IOException | NullPointerException e)
        {
            logger.log(Level.WARNING, "Unable to open or " + "find the file " + INI_FILE + ". Creating it.");
            iniConfiguration = createDefaultINIConfigAndSaveFile(createNewINIFileIfNeeded());
        } catch (ConfigurationException e)
        {
            throw new RuntimeException(e);
        }
    }

    private File getIniConfiguration()
    {
        File iniFile;
        iniFile = Paths.get(ApplicationFolderManager.getApplicationFolder().toString(), INI_FILE).toFile();
        return iniFile;
    }

    private Path getPathFromIniFile(INIConfiguration ini, String iniSection, String iniOptionName)
    {
        String folder = ini.getSection(iniSection).getString(iniOptionName);
        Path path = null;
        if (folder != null && !folder.isEmpty())
        {
            try
            {
                path = Paths.get(folder);
            } catch (InvalidPathException e)
            {
                logger.log(Level.WARNING, "Ini value for path at " + iniSection + "->" + iniOptionName + " is not a valid path.");
            }
        }
        return path;
    }

    public void saveSettings()
    {


        updateFieldsFromGUI();

        iniConfiguration = new INIConfiguration();
        SubnodeConfiguration firefoxSection = iniConfiguration.getSection(FIREFOX_SECTION);
        SubnodeConfiguration userInfoSection = iniConfiguration.getSection(USER_INFO_SECTION);

        firefoxSection.addProperty(EXECUTABLE_PATH, firefoxExecutablePath);
        userInfoSection.addProperty(USER_EMAIL, condorClubUserEmail);
        userInfoSection.addProperty(USER_PASSWORD, getEncryptedPassword());

        writeIniConfigurationToFile();

    }

    private void updateFieldsFromGUI()
    {
        condorClubUserEmail = condorClubUserEmailField.getText();
        condorClubUserPassword = condorClubUserPasswordField.getText();
        firefoxExecutablePath = firefoxExecutablePathField.getText();
    }

    private void writeIniConfigurationToFile()
    {
        try
        {
            File iniFile = getIniConfiguration();
            Writer iniFileWriter = new FileWriter(iniFile);
            iniConfiguration.write(iniFileWriter);
        } catch (IOException | ConfigurationException e)
        {
            throw new RuntimeException(e);
        }
    }

    private String getEncryptedPassword()
    {
        String encryptedPassword;
        try
        {
            encryptedPassword = new EncryptionManager().encrypt(condorClubUserPassword);
        } catch (Exception e)
        {
            logger.log(Level.WARNING, "Unable to encrypt password");
            throw new RuntimeException(e);
        }
        return encryptedPassword;
    }

    private INIConfiguration createDefaultINIConfigAndSaveFile(File file)
    {
        INIConfiguration config = new INIConfiguration();
        String fireFoxExePath = FireFoxFinder.get().getPathToExe();
        SubnodeConfiguration firefoxSection = config.getSection(FIREFOX_SECTION);
        firefoxSection.addProperty(EXECUTABLE_PATH, fireFoxExePath);
        try
        {
            Writer iniWriter = new FileWriter(file);
            config.write(iniWriter);
        } catch (Exception e)
        {
            logger.log(Level.SEVERE, e.getMessage());
        }
        return config;
    }

    private File createNewINIFileIfNeeded()
    {
        File file = getIniConfiguration();
        try
        {
            if (file.createNewFile())
                logger.log(Level.INFO, "INI file not found so it was created.");
        } catch (IOException e)
        {
            throw new RuntimeException(e);
        }
        logger.log(Level.FINE, "INI File path: " + file.getPath());
        return file;
    }

}
