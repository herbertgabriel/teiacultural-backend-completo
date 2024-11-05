package com.proa.teiacultural.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

@Service
public class StoreFileService {
    @Autowired
    private S3Client s3Client;

    @Value("${aws.bucket-name}")
    private String bucketName;

    @Value("${aws.endpoint}")
    private String endpoint;

    public String uploadFile(MultipartFile file, String entityFile, String fileUri) {
        String fileName = entityFile + fileUri;
        File fileObj = convertMultiPartFileToFile(file);

        PutObjectRequest objectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(fileName)
                .build();
        s3Client.putObject(objectRequest, fileObj.toPath());

        return endpoint + "/" + bucketName + "/" + fileName;
    }

    public void deleteFile(String fileUrl) {
        try {
            String fileName = extractFileNameFromUrl(fileUrl);
            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileName)
                    .build();
            s3Client.deleteObject(deleteObjectRequest);
        } catch (IllegalArgumentException e) {
            // Log the error and continue
            System.err.println("Failed to delete file: " + e.getMessage());
        }
    }

    public void deleteFolder(String folderName) {
        ListObjectsV2Request listObjectsV2Request = ListObjectsV2Request.builder()
                .bucket(bucketName)
                .prefix(folderName + "/")
                .build();

        ListObjectsV2Response listObjectsV2Response;
        do {
            listObjectsV2Response = s3Client.listObjectsV2(listObjectsV2Request);
            List<S3Object> objects = listObjectsV2Response.contents();

            for (S3Object object : objects) {
                DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                        .bucket(bucketName)
                        .key(object.key())
                        .build();
                s3Client.deleteObject(deleteObjectRequest);
            }

            listObjectsV2Request = listObjectsV2Request.toBuilder()
                    .continuationToken(listObjectsV2Response.nextContinuationToken())
                    .build();
        } while (listObjectsV2Response.isTruncated());
    }

    private String extractFileNameFromUrl(String fileUrl) {
        String prefix = endpoint + "/" + bucketName + "/";
        if (fileUrl.startsWith(prefix)) {
            return fileUrl.substring(prefix.length());
        } else {
            throw new IllegalArgumentException("Invalid file URL: " + fileUrl);
        }
    }

    private File convertMultiPartFileToFile(MultipartFile file) {
        File convertedFile = new File(file.getOriginalFilename());
        try (FileOutputStream fos = new FileOutputStream(convertedFile)) {
            fos.write(file.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return convertedFile;
    }
}