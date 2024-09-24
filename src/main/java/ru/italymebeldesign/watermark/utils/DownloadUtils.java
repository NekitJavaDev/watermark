package ru.italymebeldesign.watermark.utils;

import com.google.common.io.Files;
import lombok.extern.slf4j.Slf4j;
import ru.italymebeldesign.watermark.Constants;
import ru.italymebeldesign.watermark.Storage;

import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.FileDialog;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Graphics2D;
import java.awt.MenuItem;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Locale;

import static ru.italymebeldesign.watermark.Constants.IMAGE_EXTENSIONS;

@Slf4j
public class DownloadUtils {
    public static MenuItem drawSingleDownloadMenuItem(String singleSelectPath) {
        MenuItem openFileItem = new MenuItem("Загрузите фотографию");
        openFileItem.addActionListener(actionEvent -> {
            FileDialog fileDialog = new FileDialog((Frame) null, "Загрузите оригинальную фотографию", FileDialog.LOAD);
            fileDialog.setDirectory(singleSelectPath);
            fileDialog.setVisible(true);

            String directory = fileDialog.getDirectory();
            String file = fileDialog.getFile();

            if (directory != null && file != null) {
                String fileNameInLowerCase = file.toLowerCase(Locale.ROOT);
                if (IMAGE_EXTENSIONS.contains(fileNameInLowerCase
                        .substring(fileNameInLowerCase.lastIndexOf("."))
                )) {
                    saveFileWithWatermark(directory + file, Storage.getInstance().getAmProperties().getSavingFolderPath());
                } else {
                    log.error("The selected image {} doesn't contain valid file extensions.", fileNameInLowerCase);
                }
            }
        });
        return openFileItem;
    }

    public static MenuItem drawSelectMultipleFilesItem(String multiplySelectPath) {
        MenuItem selectMultipleFilesItem = new MenuItem("Выбрать несколько файлов");
        selectMultipleFilesItem.addActionListener(actionEvent -> {
            File[] selectedFiles = new File[0];

            while (true) {
                FileDialog fileDialog = new FileDialog((Frame) null, "Выберите изображения", FileDialog.LOAD);
                // Choose several files
                fileDialog.setMultipleMode(true);
                fileDialog.setDirectory(multiplySelectPath);
                fileDialog.setVisible(true);

                String[] fileNames = Arrays.stream(fileDialog.getFiles())
                        .map(File::getAbsolutePath)
                        .filter(name ->
                                IMAGE_EXTENSIONS.contains(
                                        name.toLowerCase(Locale.ROOT)
                                                .substring(name.toLowerCase(Locale.ROOT).lastIndexOf("."))
                                ))
                        .toArray(String[]::new);
                if (fileNames.length > 0) {
                    selectedFiles = new File[fileNames.length];
                    for (int i = 0; i < fileNames.length; i++) {
                        selectedFiles[i] = new File(fileNames[i]);
                    }
                    break;
                } else {
                    int option = JOptionPane.showConfirmDialog(
                            null,
                            "Вы не выбрали файлы. Хотите повторить выбор?",
                            "Повторить выбор?",
                            JOptionPane.YES_NO_OPTION
                    );
                    if (option == JOptionPane.NO_OPTION) {
                        break;
                    }
                }
            }

            for (File file : selectedFiles) {
                saveFileWithWatermark(file.getAbsolutePath(), Storage.getInstance().getAmProperties().getSavingFolderPath());
            }
            log.info("All images have been processed and saved.");
        });
        return selectMultipleFilesItem;
    }

    public static MenuItem drawDownloadFolderMenuItem(String folderSelectPath) {
        MenuItem openFolderItem = new MenuItem("Загрузите папку с фотографиями");
        openFolderItem.addActionListener(actionEvent -> {
            JFileChooser folderChooser = new JFileChooser();
            folderChooser.setDialogTitle("Выберите папку с фотографиями");
            folderChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            folderChooser.setCurrentDirectory(new File(folderSelectPath));

            if (folderChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                File folder = folderChooser.getSelectedFile();
                processAllImagesInFolder(folder);
            }
        });
        return openFolderItem;
    }

    private static void processAllImagesInFolder(File folder) {
        FilenameFilter filenameFilter = (dir, name) ->
                IMAGE_EXTENSIONS.contains(
                        name.contains(".")
                                ? name.toLowerCase(Locale.ROOT).substring(name.toLowerCase(Locale.ROOT).lastIndexOf("."))
                                : "null"
                );
        File[] files = folder.listFiles(filenameFilter);

        if (files != null && files.length > 0) {
            for (File file : files) {
                saveFileWithWatermark(file.getAbsolutePath(), Storage.getInstance().getAmProperties().getSavingFolderPath());
            }
            log.info("All images from the folder have been processed and saved.");
        } else {
            log.error("The selected folder doesn't contain images with valid file extensions.");
        }
    }

    private static void saveFileWithWatermark(String filePath, String saveDirectory) {
        File destDir = new File(saveDirectory);
        if (!destDir.exists()) {
            destDir.mkdirs();
        }

        File originalFile = new File(filePath);
        File destFile = new File(destDir, Constants.WATERMARKED_FILE_PREFIX + originalFile.getName());
        // all supported ImageIO file extensions to write
        String[] writerFormatNames = ImageIO.getWriterFormatNames();

        try {
            BufferedImage originalImage = ImageIO.read(originalFile);

            // New image with watermarked text
            BufferedImage watermarkedImage = new BufferedImage(
                    originalImage.getWidth(),
                    originalImage.getHeight(),
                    BufferedImage.TYPE_3BYTE_BGR
            );

            Graphics2D g2d = (Graphics2D) watermarkedImage.getGraphics();
            g2d.drawImage(originalImage, 0, 0, null);

            // Color of watermarked text
            g2d.setColor(Color.WHITE);

            // Динамически изменяем размер шрифта
            float fontSize = originalImage.getWidth() / 20f; // Устанавливаем размер шрифта в зависимости от ширины изображения
            g2d.setFont(new Font("Arial", Font.BOLD, Math.max(20, (int) fontSize))); // Минимальный размер 20

            // Вычисляем положение текста
            FontRenderContext frc = g2d.getFontRenderContext();
            TextLayout layout = new TextLayout(Storage.getInstance().getAmProperties().getText(), g2d.getFont(), frc);
            int textWidth = (int) layout.getBounds().getWidth();
            int x = (originalImage.getWidth() - textWidth) / 2; // Центрируем текст по горизонтали
//            int y = originalImage.getHeight() / 2 + (int) layout.getBounds().getHeight() / 2; // Центрируем текст по вертикали
            int y = (int) (originalImage.getHeight() * 0.75) + (int) layout.getBounds().getHeight() / 2; // Центрируем текст по нижней части (3/4)

            // Устанавливаем прозрачность для текста
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));

            // Рисуем текст водяного знака
            g2d.drawString(Storage.getInstance().getAmProperties().getText(), x, y);
            g2d.dispose();

            boolean isNotContainsFileExtension = Arrays.stream(writerFormatNames)
                    .noneMatch(ext -> ext.equals(Files.getFileExtension(originalFile.getName())));
            String fileExtension = isNotContainsFileExtension ? "jpg" : Files.getFileExtension(originalFile.getName());

            // Saved image with watermark
            ImageIO.write(watermarkedImage, fileExtension, destFile);
            log.info("Watermark added, image saved to path: {}", destFile.getAbsolutePath());
        } catch (IOException ex) {
            ex.printStackTrace();
            log.error("Error saving image {} with watermark {}", originalFile.getName(), ex.getMessage());
        }
    }

    private static void saveOriginalFile(String filePath, String saveDirectory) {
        File destDir = new File(saveDirectory);
        if (!destDir.exists()) {
            destDir.mkdirs();
        }

        File destFile = new File(destDir, new File(filePath).getName());

        try (InputStream inputStream = new FileInputStream(filePath);
             OutputStream outputStream = new FileOutputStream(destFile)) {
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }
            log.info("Original file added, image saved to path: {}", destFile.getAbsolutePath());
        } catch (IOException ex) {
            ex.printStackTrace();
            log.error("Error saving original image {}", ex.getMessage());
        }
    }

}