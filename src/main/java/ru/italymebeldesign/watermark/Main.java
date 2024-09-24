package ru.italymebeldesign.watermark;

import ru.italymebeldesign.watermark.ui.AmTray;

public class Main {
    public static void main(String[] args) {
        Storage.getInstance().setAmTray(new AmTray());
    }
}