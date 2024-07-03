package com.verytroll.book_network.file;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Slf4j
public class FileUtils {
    public static byte[] readFileFromLocation(String fileUrl) {
        byte[] result = null;
        if(!StringUtils.isBlank(fileUrl)) {
            try {
                Path filePath = new File(fileUrl).toPath();
                result = Files.readAllBytes(filePath);
            } catch(IOException e) {
                log.warn("No file found in the path {}", fileUrl);
            }
        }
        return result;
    }
}
