package org.mckayerp.condor_downloader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ZipFileManager {

    private static final Logger logger = Logger.getLogger(ZipFileManager.class.getName());
    /**
     * This method is used to extract the contents of a zip archive that only contains a single file.  The file is
     * renamed in the process and saved in a specific location.  If the archive contains more than one file, the last
     * file will be saved.
     */
    public static void extractSingleFileFromArchiveAndRename(Path zipPath, Path filePath, String newFileName) {
        try {
            ZipInputStream zis = new ZipInputStream(new FileInputStream(zipPath.toString()));
            ZipEntry zipEntry = zis.getNextEntry();
            while (zipEntry != null) {
                File newFile = ZipFileManager.newFile(filePath, zipEntry, newFileName);
                if (zipEntry.isDirectory()) {
                    if (!newFile.isDirectory() && !newFile.mkdirs()) {
                        throw new IOException("Failed to create directory " + newFile);
                    }
                } else {
                    // fix for Windows-created archives
                    File parent = newFile.getParentFile();
                    if (!parent.isDirectory() && !parent.mkdirs()) {
                        throw new IOException("Failed to create directory " + parent);
                    }

                    ZipFileManager.writeFileContents(newFile, zis);
                }
                zipEntry = zis.getNextEntry();
            }
            zis.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void writeFileContents(File newFile, ZipInputStream zis) throws IOException {
        // write file content
        FileOutputStream fos = new FileOutputStream(newFile);
        byte[] buffer = new byte[1024];
        int len;
        while ((len = zis.read(buffer)) > 0) {
            fos.write(buffer, 0, len);
        }
        fos.close();
    }

    public static File newFile(Path destinationDir, ZipEntry zipEntry, String newName) throws IOException {

        File destFile = new File(destinationDir.toFile(), newName);

        String destDirPath = destinationDir.toFile().getCanonicalPath();
        String destFilePath = destFile.getCanonicalPath();

        if (!destFilePath.startsWith(destDirPath + File.separator)) {
            throw new IOException("Entry is outside of the target dir: " + zipEntry.getName());
        }

        return destFile;
    }

    public static void deleteZipFiles(Path directoryPath) {
        try (Stream<Path> files = Files.walk(directoryPath)) {
            files.filter(path -> path.getParent().equals(directoryPath)
                            && path.toFile().getName().endsWith(".zip"))
                    .forEach(path -> path.toFile().delete());
        }
        catch(IOException e) {
            logger.log(Level.WARNING,"Couldn't delete zip files: " + e.getMessage());
        }
    }
}
