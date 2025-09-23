package com.sboot.website.service;

import java.nio.file.Path;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface StorageService {
    String getSorageFilename(MultipartFile file, String id);
    void store(MultipartFile file, String storeFilename);
    Resource loadAsResource(String filename);
    Path load(String filename);
    void delete(String storeFilename) throws Exception;
    void init();
}