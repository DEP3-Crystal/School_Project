package model;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class FileUtils {
    private static final Properties properties = new Properties();

    public static Properties loadProperties(String fileName) throws IOException {
        InputStream inputStream = FileUtils.class.getResourceAsStream("/" + extractFileName(fileName));
        if (inputStream == null) {
            throw new FileNotFoundException(fileName);
        }

        properties.load(inputStream);
        return properties;
    }

    private static String extractFileName(String fileName) {
        boolean startsWithSlash = fileName.startsWith("/") || fileName.startsWith("\\") ;
        if (startsWithSlash) {
            return fileName.substring(1);
        }
        return fileName;
    }
}
