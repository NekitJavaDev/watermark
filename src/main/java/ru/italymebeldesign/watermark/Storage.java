package ru.italymebeldesign.watermark;

import lombok.Getter;
import lombok.Setter;
import ru.italymebeldesign.watermark.ui.AmTray;
import ru.italymebeldesign.watermark.utils.AmProperties;
import ru.italymebeldesign.watermark.utils.Utils;

import java.io.File;

@Getter
public class Storage {
    private static Storage instance;
    private final AmProperties amProperties;
    @Setter
    private AmTray amTray;

    public Storage() {
        try {
            if (Utils.isRunningFromJar()) {
                amProperties = Utils.loadProperties();
            } else {
                amProperties = Utils.loadLocalProperties();
            }
            fillDesktopUserPathFolder(amProperties);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }
    }

    public static synchronized Storage getInstance() {
        if (instance == null) {
            instance = new Storage();
        }
        return instance;
    }

    private void fillDesktopUserPathFolder(AmProperties amProperties) {
        if (amProperties.getSavingFolderPath().startsWith("/Desktop")) {
            amProperties.setSavingFolderPath(System.getProperty("user.home") + amProperties.getSavingFolderPath());
        }
        if (amProperties.getSingleSelectPath().startsWith("/Desktop")) {
            amProperties.setSingleSelectPath(System.getProperty("user.home") + amProperties.getSingleSelectPath());
        }
        if (amProperties.getMultiplySelectPath().startsWith("/Desktop")) {
            amProperties.setMultiplySelectPath(System.getProperty("user.home") + amProperties.getMultiplySelectPath());
        }
        if (amProperties.getFolderSelectPath().startsWith("/Desktop")) {
            amProperties.setFolderSelectPath(System.getProperty("user.home") + amProperties.getFolderSelectPath());
        }

        boolean isWindowsOS = File.separator.equals("\\");
        amProperties.setSavingFolderPath(amProperties.getSavingFolderPath().replace(isWindowsOS ? "/" : "\\", File.separator));
        amProperties.setSingleSelectPath(amProperties.getSingleSelectPath().replace(isWindowsOS ? "/" : "\\", File.separator));
        amProperties.setMultiplySelectPath(amProperties.getMultiplySelectPath().replace(isWindowsOS ? "/" : "\\", File.separator));
        amProperties.setFolderSelectPath(amProperties.getFolderSelectPath().replace(isWindowsOS ? "/" : "\\", File.separator));
    }
}