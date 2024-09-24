package ru.italymebeldesign.watermark.ui;

import lombok.extern.slf4j.Slf4j;
import ru.italymebeldesign.watermark.Constants;
import ru.italymebeldesign.watermark.Storage;
import ru.italymebeldesign.watermark.utils.DownloadUtils;
import ru.italymebeldesign.watermark.utils.Utils;

import javax.swing.JFileChooser;
import java.awt.AWTException;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.io.File;

@Slf4j
public class AmTray {
    public AmTray() {
        log.info("Default properties.yml \n {}", Storage.getInstance().getAmProperties().toString());
        String savingFolderPath = Storage.getInstance().getAmProperties().getSavingFolderPath();
        String singleSelectPath = Storage.getInstance().getAmProperties().getSingleSelectPath();
        String multiplySelectPath = Storage.getInstance().getAmProperties().getMultiplySelectPath();
        String folderSelectPath = Storage.getInstance().getAmProperties().getFolderSelectPath();

        File watermarkedDefaultFolder = new File(savingFolderPath);

        // If folder or one of parent folders are not exist - create them
        if (!watermarkedDefaultFolder.exists()) {
            boolean created = watermarkedDefaultFolder.mkdirs();
            if (created) {
                log.info("Success creating folders - {}", watermarkedDefaultFolder.getPath());
            } else {
                log.error("Error creating one of folders - {}", watermarkedDefaultFolder.getPath());
            }
        }

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Выберите папку для сохранения изображений");
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setCurrentDirectory(watermarkedDefaultFolder);

        if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            File savingUserFolder = chooser.getSelectedFile();
            Storage.getInstance().getAmProperties().setSavingFolderPath(savingUserFolder.getAbsolutePath());
//            savingFolderPath = savingUserFolder.getAbsolutePath();
        } else {
            log.error("Folder for saving watermarked images doesn't choose. Application will be closed");
            Utils.quit();
        }

        log.info("After user choose saving path properties.yml \n {}", Storage.getInstance().getAmProperties().toString());

        final PopupMenu popup = new PopupMenu();
        final TrayIcon trayIcon = new TrayIcon(
                Toolkit.getDefaultToolkit().createImage(
                        AmTray.class.getClassLoader().getResource(Constants.APP_ICON_PATH)
                ));
        trayIcon.setImageAutoSize(true);

        final SystemTray tray = SystemTray.getSystemTray();

        MenuItem quitItem = new MenuItem("Выход");
        quitItem.addActionListener(actionEvent -> Utils.quit());

        MenuItem openFileItem = DownloadUtils.drawSingleDownloadMenuItem(singleSelectPath);
        MenuItem openMultipleFileItem = DownloadUtils.drawSelectMultipleFilesItem(multiplySelectPath);
        MenuItem openFolderItem = DownloadUtils.drawDownloadFolderMenuItem(folderSelectPath);

        popup.add(openFileItem);
        popup.addSeparator();
        popup.add(openMultipleFileItem);
        popup.addSeparator();
        popup.add(openFolderItem);
        popup.addSeparator();
        popup.add(quitItem);

        trayIcon.setPopupMenu(popup);

        try {
            tray.add(trayIcon);
        } catch (AWTException e) {
            log.error("TrayIcon could not be added to right system tray.");
        }
    }
}