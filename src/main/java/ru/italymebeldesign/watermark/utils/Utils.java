package ru.italymebeldesign.watermark.utils;

import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Utils {
    private static final String PROPERTIES_FILE_NAME = "properties.yml";

    public static void quit() {
        System.exit(0);
    }

    public static AmProperties loadProperties() throws Exception {
        String jarPath = Utils.class.getProtectionDomain().getCodeSource().getLocation().toURI().toString();
        URI jarUri = new URI(jarPath);
        Path dirPath;

        if (jarUri.getScheme().equals("file")) {
            dirPath = Paths.get(jarUri).getParent();
        } else {
            // Handle other schemes (like jar) if necessary
            dirPath = Paths.get(new URI(jarUri.getSchemeSpecificPart())).getParent();
        }

        String propertiesFilePath = dirPath.resolve(PROPERTIES_FILE_NAME).toString();

        Yaml yaml = new Yaml(new Constructor(AmProperties.class, new LoaderOptions()));
        try (InputStream inputStream = new FileInputStream(propertiesFilePath)) {
            return yaml.load(inputStream);
        }
    }

    public static AmProperties loadLocalProperties() throws Exception {
        String propertiesFilePath = "src/main/resources/properties.yml";

        Yaml yaml = new Yaml(new Constructor(AmProperties.class, new LoaderOptions()));
        try (InputStream inputStream = new FileInputStream(propertiesFilePath)) {
            return yaml.load(inputStream);
        }
    }

    public static boolean isRunningFromJar() {
        String className = Utils.class.getName().replace('.', '/');
        String classJar = Utils.class.getResource("/" + className + ".class").toString();

        return classJar.startsWith("jar:");
    }
}