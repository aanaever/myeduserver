package com.anastasiaeverstova.myeduserver.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;


import org.springframework.beans.factory.annotation.Value;


@Service
public class VideoService {

    private final Path rootLocation;

    public VideoService(@Value("${video.storage.path}") String videoStoragePath) {
        this.rootLocation = Paths.get(videoStoragePath);
    }

    public String saveVideo(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IOException("File is empty");
        }
        String filename = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
        Files.copy(file.getInputStream(), this.rootLocation.resolve(filename), StandardCopyOption.REPLACE_EXISTING);
        return filename;
    }

    public Resource loadVideo(String filename) {
        try {
            Path file = rootLocation.resolve(filename);
            Resource resource = new UrlResource(file.toUri());

            if (resource.exists() || resource.isReadable()) {
                return resource;
            } else {
                throw new RuntimeException("Can not read the file " + filename);
            }
        } catch (MalformedURLException e) {
            throw new RuntimeException("File load exception: " + filename, e);
        }
    }
}
