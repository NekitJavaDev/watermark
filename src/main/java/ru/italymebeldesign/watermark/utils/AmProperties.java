package ru.italymebeldesign.watermark.utils;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class AmProperties {
    private String text;
    private String savingFolderPath;
    private String singleSelectPath;
    private String multiplySelectPath;
    private String folderSelectPath;
}