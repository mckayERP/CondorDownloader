module org.mckayerp.condor_downloader {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;
    requires org.seleniumhq.selenium.firefox_driver;
    requires java.datatransfer;
    requires java.management;
    requires dev.failsafe.core;
    requires org.apache.commons.configuration2;
    requires java.logging;

    opens org.mckayerp.condor_downloader to javafx.fxml;
    exports org.mckayerp.condor_downloader;
}