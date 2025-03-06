package com.microservices.smmsb_inventory_service.service;

import io.minio.*;
import io.minio.errors.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@Service
public class MinioService {

    private final MinioClient minioClient;

    @Value("${application.minio.url}")
    private String minioUrl;  // Ambil URL dari application.properties

    @Value("${application.minio.bucketName}")
    private String defaultBucketName;

    @Autowired
    public MinioService(MinioClient minioClient) {
        this.minioClient = minioClient;
    }

    public String uploadFile(MultipartFile file, String objectName, String bucketName) {
        if (bucketName == null || bucketName.isEmpty()) {
            bucketName = defaultBucketName;
        }
        try {
            // Pastikan bucket ada
            createBucketIfNotExists(bucketName);

            // Upload file
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .stream(file.getInputStream(), file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build());

            // Kembalikan URL
            return minioUrl + "/" + bucketName + "/" + objectName;

        } catch (MinioException | IOException | InvalidKeyException | NoSuchAlgorithmException e) {
            throw new RuntimeException("Error uploading file to MinIO", e);
        }
    }

    public void removeObject(String bucketName, String objectName) throws Exception {
        if (bucketName == null || bucketName.isEmpty()) {
            bucketName = defaultBucketName;
        }
        minioClient.removeObject(
                RemoveObjectArgs.builder().bucket(bucketName).object(objectName).build());
    }

    // Buat bucket jika tidak ada
    private void createBucketIfNotExists(String bucketName) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        boolean found = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
        if (!found) {
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
        }
    }

    public InputStream getObject(String bucketName, String objectName) {
        if (bucketName == null || bucketName.isEmpty()) {
            bucketName = defaultBucketName;
        }
        try {
            return minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .build());
        } catch (Exception e) {
            throw new RuntimeException("Error getting file from MinIO", e);
        }
    }
}
