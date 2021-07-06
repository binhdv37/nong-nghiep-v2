package org.thingsboard.server.dft.common.service;

import org.apache.commons.io.FilenameUtils;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Date;

@Component
public class FilesStorageService {

    public String save(MultipartFile file, String savePath, String fileName) {
        try {
            String extension = FilenameUtils.getExtension(fileName);
            String name = FilenameUtils.getBaseName(fileName);
            fileName = name + "_" + new Date().getTime() + "." + extension;
            Path root = Paths.get(savePath);
            Files.createDirectories(root);
            Files.copy(file.getInputStream(), root.resolve(fileName));
            return fileName;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Could not store the file. Error: " + e.getMessage());
        }
    }

    public Resource download(String filesPath, String filename) {
        try {
            Path file = Paths.get(filesPath)
                    .resolve(filename);
            Resource resource = new UrlResource(file.toUri());

            if (resource.exists() || resource.isReadable()) {
                return resource;
            } else {
                throw new RuntimeException("Could not read the file!");
            }
        } catch (MalformedURLException e) {
            throw new RuntimeException("Error: " + e.getMessage());
        }
    }

    public boolean deleteFileInPath(String filePath, String fileName) {
        File file = new File(filePath, fileName);
        if (file.delete()) {
            System.out.println("File deleted successfully");
            return true;
        } else {
            throw new RuntimeException("File delete failed: " + fileName);
        }
    }

    public void deleteAllFileInPath(String filePath) {
        Arrays.stream(new File(filePath).listFiles()).forEach(File::delete);
    }
}
