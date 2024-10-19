package com.avoworld.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.PutObjectRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class FileStorageService {

    private static final Logger logger = LoggerFactory.getLogger(FileStorageService.class);

    @Value("${file.upload-dir}")
    private String uploadsDir;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    private final AmazonS3 amazonS3;
    private Path rootLocation;

    public FileStorageService(AmazonS3 amazonS3) {
        this.amazonS3 = amazonS3;
    }

    @PostConstruct
    public void initializeRootLocation() {
        try {
            this.rootLocation = Paths.get(uploadsDir).toAbsolutePath().normalize();
            Files.createDirectories(rootLocation);
        } catch (IOException e) {
            logger.error("Could not initialize storage: {}", e.getMessage());
            throw new RuntimeException("Could not initialize storage: " + e.getMessage(), e);
        }
    }

    public String store(MultipartFile file) {
        String filename = UUID.randomUUID().toString() + "-" + sanitizeFilename(file.getOriginalFilename());
        try {
            if (file.isEmpty()) {
                throw new RuntimeException("Failed to store empty file.");
            }

            File convertedFile = convert(file, filename);
            String s3Url = putS3(convertedFile, filename);
            removeNewFile(convertedFile);
            return s3Url;
        } catch (IOException e) {
            logger.error("Failed to store file: {}", e.getMessage());
            throw new RuntimeException("Failed to store file: " + e.getMessage(), e);
        }
    }

    public Path load(String filename) {
        return rootLocation.resolve(filename);
    }

    public String saveProfileImage(MultipartFile profileImg) throws IOException {
        return store(profileImg);
    }

    private File convert(MultipartFile file, String filename) throws IOException {
        File convertFile = new File(System.getProperty("java.io.tmpdir"), filename);
        if (convertFile.createNewFile()) {
            try (FileOutputStream fos = new FileOutputStream(convertFile)) {
                fos.write(file.getBytes());
            } catch (IOException e) {
                logger.error("Error occurred while converting file: {}", e.getMessage());
                throw e;
            }
            return convertFile;
        }
        throw new IllegalArgumentException(String.format("Failed to convert file: %s", filename));
    }

    private String putS3(File uploadFile, String fileName) {
        amazonS3.putObject(new PutObjectRequest(bucket, fileName, uploadFile)
                .withCannedAcl(CannedAccessControlList.PublicRead));
        return amazonS3.getUrl(bucket, fileName).toString();
    }

    private void removeNewFile(File targetFile) {
        if (targetFile.delete()) {
            logger.info("File deleted successfully.");
        } else {
            logger.info("Failed to delete file.");
        }
    }

    private String sanitizeFilename(String filename) {
        return filename.replaceAll("[^a-zA-Z0-9.-]", "_");
    }

    public void deleteFile(String fileName) {
        try {
            String decodedFileName = java.net.URLDecoder.decode(fileName, "UTF-8");
            logger.info("Deleting file from S3: " + decodedFileName);
            amazonS3.deleteObject(bucket, decodedFileName);
        } catch (IOException e) {
            logger.error("Error while decoding the file name: {}", e.getMessage());
        }
    }

    public String updateFile(MultipartFile newFile, String oldFileName) throws IOException {
        deleteFile(oldFileName);
        return store(newFile);
    }
}
